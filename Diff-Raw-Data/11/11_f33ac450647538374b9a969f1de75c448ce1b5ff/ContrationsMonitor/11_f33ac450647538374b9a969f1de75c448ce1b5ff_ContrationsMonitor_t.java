 package org.codg.contraction;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Vector;
 import javax.microedition.midlet.*;
 import javax.microedition.lcdui.*;
 
 public class ContrationsMonitor extends MIDlet implements CommandListener {
 
     public static String CONTRACION_START = "Contraccion";
     private Command exitCommand; // The exit command
     private Command clearCommand; // The exit command
     private Command contrationCommand; // The exit command
     private Command contrationFinishedCommand;
     private Display display;     // The display for this MIDlet
     private long contraction_time_start = 0;
     private long start_time = 0;
     private List contractionList;
     private Form contractionRunning;
     private Vector contractions;
 
     public ContrationsMonitor() {
         display = Display.getDisplay(this);
         exitCommand = new Command("Salir", Command.EXIT, 0);
         clearCommand = new Command("Limpiar", Command.EXIT, 0);
         contrationCommand = new Command("Empezo ", Command.EXIT, 0);
         contrationFinishedCommand = new Command("Termino", Command.EXIT, 0);
         contractions = new Vector();
     }
 
     public void startApp() {
         contractionList = new List("Contracciones", List.IMPLICIT);
         contractionList.addCommand(contrationCommand);
         contractionList.addCommand(clearCommand);
         contractionList.addCommand(exitCommand);
         contractionList.setCommandListener(this);
         contractionList.append("Contracciones cada : 0 minutos", null);
         contractionRunning = new Form("Contracciones");
         contractionRunning.addCommand(contrationFinishedCommand);
         contractionRunning.append(
                 new Gauge(
                 "Contraccion en progreso", false, Gauge.INDEFINITE,
                 Gauge.CONTINUOUS_RUNNING));
         try {
             contractionRunning.append(Image.createImage("/matutes.png"));
         } catch (IOException ex) {
             ex.printStackTrace();
         }
         contractionRunning.setCommandListener(this);
         display.setCurrent(contractionList);
     }
 
     public void pauseApp() {
     }
 
     public void destroyApp(boolean unconditional) {
     }
 
     public void commandAction(Command c, Displayable s) {
         if (c == exitCommand) {
             destroyApp(false);
             notifyDestroyed();
         } else if (c == clearCommand) {
             contractionList.deleteAll();
             contractionList.append("Contracciones cada : 0 minutos", null);
             start_time = 0;
             contractions.removeAllElements();
         } else if (c == contrationCommand) {
             contraction_time_start = System.currentTimeMillis();
             if (start_time == 0) {
                 start_time = System.currentTimeMillis();
             }
 
             display.setCurrent(contractionRunning);
         } else if (c == contrationFinishedCommand) {
             long currentTime = System.currentTimeMillis();
             long enlapsedTime = (currentTime - contraction_time_start) / 1000;
             Date d = new Date();
             Calendar calendar = Calendar.getInstance();
             calendar.setTime(d);
             String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
             long enlapsed = (System.currentTimeMillis() - start_time) / (1000 * 60);
             enlapsed = enlapsed == 0 ? 1 : enlapsed;
            long contractionPerMinute = enlapsed / (contractions.size() +1);
             float interval = 0;
             if (!contractions.isEmpty()) {
                 Contraction lastContraction = (Contraction) contractions.lastElement();
                 int intervalMs = (int) (currentTime - lastContraction.getTime());
                 interval = (intervalMs) / (1000f * 60f);
             }
             contractions.addElement(new Contraction(enlapsedTime, currentTime));
             contractionList.set(0, "Contracciones cada : " + contractionPerMinute + " minutos ", null);
             String intervalSt = String.valueOf(interval);
 
            contractionList.append(contractions.size() + ") " + enlapsedTime + " seg | " +
                     intervalSt.substring(0, intervalSt.indexOf(".") + 2) + " min |" + time, null);
             display.setCurrent(contractionList);
         }
     }
 }
 
 
