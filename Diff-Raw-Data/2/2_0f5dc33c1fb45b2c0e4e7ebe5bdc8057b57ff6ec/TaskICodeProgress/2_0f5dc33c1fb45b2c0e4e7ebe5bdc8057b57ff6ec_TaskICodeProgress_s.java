 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edacc.model;
 
 import javax.swing.SwingUtilities;
 
 /**
  *
  * @author simon
  */
 public class TaskICodeProgress implements SevenZip.ICodeProgress {
 
     private Long last = 0l;
     private long time;
     private String eta = "";
     private long size;
 
     public TaskICodeProgress(long size) {
         this.size = size;
     }
 
     @Override
     public void SetProgress(final long done, long done_compr) {
         /*  if (System.currentTimeMillis() - time > 1000) {
         long leta = (size - done) * 1000 / (done - last) / (System.currentTimeMillis() - time);
         eta = leta + " sec";
         last = done;
         time = System.currentTimeMillis();
         System.out.println(done + " of " + size + " " + done / (float) size * 100 + "% " + eta);
 
         }*/
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 if (Tasks.getTaskView() != null) {
                    Tasks.getTaskView().setProgress2(done / (float) size);
                 }
             }
         });
 
 
     }
 }
