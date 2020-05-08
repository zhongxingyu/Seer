 /*
  * A RunThread is the thread that is responsible for running
  * the program in the background, in the GUI.  There is only one RunThread
  * and it should exist for the entire time the yoob GUI is running.
  * The source code in this file has been placed into the public domain.
  */
 package tc.catseye.yoob;
 
 import tc.catseye.yoob.State;
 
 import java.lang.Thread;
 import java.lang.InterruptedException;
 
 import java.util.concurrent.SynchronousQueue;
 import java.util.concurrent.TimeUnit;
 
 import java.awt.Cursor;
 
 
 abstract class RunThreadMessage {
 }
 
 class HaltMessage extends RunThreadMessage {
 }
 
 class ProceedMessage extends RunThreadMessage {
 }
 
 class LoadMessage extends RunThreadMessage {
     public String name;
     public LoadMessage(String name) {
         this.name = name;
     }
 }
 
// probably not really necessary, but it's what we have now
class SetTurboMessage extends RunThreadMessage {
    public boolean turbo;
    public SetTurboMessage(boolean turbo) {
        this.turbo = turbo;
    }
}

 public class RunThread extends Thread {
     private SynchronousQueue<RunThreadMessage> mailbox;
     private ContentPane cp;
     private EsolangLoader loader;
     private int cyclesPerSecond;
     private boolean proceed, turbo;
 
     public RunThread(ContentPane cp, int cyclesPerSecond, EsolangLoader loader) {
         this.cp = cp;
         this.loader = loader;
         this.turbo = false;
         setRunSpeed(cyclesPerSecond);
         mailbox = new SynchronousQueue<RunThreadMessage>();
     }
 
     public void load(String name) {
         try {
             mailbox.put(new LoadMessage(name));
         } catch (InterruptedException e) {
             System.out.println("interrupted!");
         }
     }
 
     public void setTurbo(boolean turbo) {
        try {
            mailbox.put(new SetTurboMessage(turbo));
        } catch (InterruptedException e) {
            System.out.println("interrupted!");
        }
     }
 
     public void proceed() {
         try {
             mailbox.put(new ProceedMessage());
         } catch (InterruptedException e) {
             System.out.println("interrupted!");
         }
     }
 
     public void halt() {
         try {
             mailbox.put(new HaltMessage());
         } catch (InterruptedException e) {
             System.out.println("interrupted!");
         }
     }
 
     public void setRunSpeed(int cyclesPerSecond) {
         this.cyclesPerSecond = cyclesPerSecond;
     }
 
     public void run() {
         RunThreadMessage message;
         long delay;
         for (;;) {
             if (proceed) {
                 if (turbo) {
                    int counter = 512;
                     while (proceed && counter > 0) {
                         proceed = cp.step();
                        counter--;
                     }
                     delay = 0;
                 } else if (cyclesPerSecond > 0) {
                     delay = (long)(1000 / cyclesPerSecond);
                     proceed = cp.step();
                 } else {
                     delay = 100; // and do nothing on this tick
                 }
                 try {
                     message = mailbox.poll(delay, TimeUnit.MILLISECONDS);
                 } catch (InterruptedException e) {
                     message = null;
                 }
             } else {
                 try {
                     message = mailbox.take();
                 } catch (InterruptedException e) {
                     message = null;
                 }
             }
             if (message != null) {
                 if (message instanceof HaltMessage) {
                     proceed = false;
                    turbo = false;
                 } else if (message instanceof ProceedMessage) {
                     proceed = true;
                 } else if (message instanceof LoadMessage) {
                     LoadMessage l = (LoadMessage)message;
                     proceed = false;
                     cp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                     tc.catseye.yoob.State state = loader.getState(l.name);
                     if (state == null) {
                         cp.displayModalMessage("Couldn't load classes for language '" + l.name + "'", false);
                     } else {
                         cp.selectLanguage(state.getLanguage());
                     }
                     cp.setCursor(null);
                } else if (message instanceof SetTurboMessage) {
                    SetTurboMessage t = (SetTurboMessage)message;
                    this.turbo = t.turbo;
                 }
             }
         }
     }
 }
