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
        int used = (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
        int total = (int) (Runtime.getRuntime().totalMemory() / 1024 / 1024);
        final String unit;
        if (used >= 1000 || total >= 1000) {
            used /= 1024;
            total /= 1024;
            unit = "GB";
        } else
            unit = "MB";
        final int used_ = used;
        final int total_ = total;
 
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setMaximum(total_);
                setValue(used_);
                setString(used_ + " " + unit + "/" + total_ + " " + unit);
            }
        });
     }
 
     public void setSleepTime(int sleepTime) {
         this.sleepTime = sleepTime;
     }
 }
