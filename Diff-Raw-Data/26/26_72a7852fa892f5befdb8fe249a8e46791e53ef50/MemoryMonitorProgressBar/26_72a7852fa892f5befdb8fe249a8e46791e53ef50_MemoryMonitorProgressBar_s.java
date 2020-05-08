 package amber.gui.misc;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JProgressBar;
 import javax.swing.SwingUtilities;
 
 public class MemoryMonitorProgressBar extends JProgressBar {
 
     protected int sleepTime;
 
     public MemoryMonitorProgressBar() {
         sleepTime = 1000 * 10;
 
         setStringPainted(true);
 
         Thread memoryThread = new Thread(new Runnable() {
             public void run() {
                 while (true) {
                     update();
                     try {
                         Thread.sleep(MemoryMonitorProgressBar.this.sleepTime);
                     } catch (InterruptedException e) {
                     }
                 }
             }
         });
         memoryThread.setDaemon(true);
         memoryThread.setPriority(1);
         memoryThread.start();
         this.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent evt) {
                 if (SwingUtilities.isLeftMouseButton(evt)) {                 
                     Runtime.getRuntime().gc();
                     update();
                 }
             }
         });
     }
 
     protected void update() {
        String grabbed = String.valueOf((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
        String total = String.valueOf(Runtime.getRuntime().totalMemory() / 1024 / 1024);
 
        setMaximum(Integer.parseInt(total));
        setValue(Integer.parseInt(grabbed));

        setString(grabbed + " M of " + total + " M");
     }
 
     public void setSleepTime(int sleepTime) {
         this.sleepTime = sleepTime;
     }
 }
