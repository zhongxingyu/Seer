 /*
  * The MIT License
  *
  * Copyright 2011 Kevin.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 /*
  * ProblemPanel.java
  *
  * Created on Nov 29, 2011, 9:13:09 PM
  */
 package PCC;
 
 import Messages.SubmissionCompilationFailure;
 import Messages.SubmissionOvertimeFailure;
 import Messages.SubmissionResult;
 import Messages.SubmissionRuntimeFailure;
 import PCS.Problem;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.Color;
 import java.io.File;
 import java.util.Collection;
 import javax.swing.Timer;
 //<editor-fold defaultstate="collapsed" desc="comment">
 //</editor-fold>
 
 /**
  *
  * @author Kevin
  */
 public class ProblemPanel extends javax.swing.JPanel implements ActionListener {
 
   Timer timer;
   long startTime;
   // GRADING state has been removed for demo simplicity
 
   protected enum SubmissionState {
     READY, PENDING
   };
   protected SubmissionState state;
   private SubmissionWindow submissionWindow;
   protected Problem problem;
   private SubmissionResult lastResult;
   private Object defaultLanguage;
   private PCCMain main;
 
   /** Creates new form ProblemPanel */
   public ProblemPanel(PCCMain main, Problem problem,
       Collection<String> languages, Object defaultLanguage)
   {
     this.main = main;
     this.problem = problem;
     this.defaultLanguage = defaultLanguage;
     System.out.println("Default lang from ProblemPanel: "+defaultLanguage);
     submissionWindow = new SubmissionWindow(this, languages, defaultLanguage);
     this.state = SubmissionState.READY;
     timer = new Timer(100, this);
     initComponents();
     ProbLabel.setText(problem.getProblemTitle());
   }
 
   public void startPending() {
     submissionWindow.setVisible(false);
     ProbSubmitButton.setEnabled(false);
     state = SubmissionState.PENDING;
     ProbProgressBar.setString("Pending...");
     ProbProgressBar.setForeground(Color.blue);
   }
 
   public void startGrading() {
     timer.start();
     ProbProgressBar.setString("Grading...");
     startTime = System.currentTimeMillis();
   }
 
   public void sendFolder(File folder, String language) {
     main.sendSubmission(problem, folder, language);
     startPending();
   }
 
   public void cancel() {
     ProbSubmitButton.setText("Submit");
     state = SubmissionState.READY;
     if(lastResult == null) {
       // only need to change text because there has never been any other change
       ProbProgressBar.setString("Ready");
     } else if(!lastResult.getSuccess()) {
       setFailure();
     }
     ProbProgressBar.setForeground(null);
     ProbProgressBar.setValue(0);
     timer.stop();
   }
 
   public void registerSubmissionResult(SubmissionResult result) {
     lastResult = result;
     if(result.getSuccess()) {
       setSuccess();
     } else {
       setFailure();
     }
   }
 
   private void setFailure() {
     if(lastResult instanceof SubmissionCompilationFailure) {
       ProbProgressBar.setString("Compilation Failure");
     } else if(lastResult instanceof SubmissionRuntimeFailure) {
       ProbProgressBar.setString("Runtime Failure");
     } else if(lastResult instanceof SubmissionOvertimeFailure) {
       ProbProgressBar.setString("Overtime Failure");
     } else {
       ProbProgressBar.setString("Incorrect");
     }
     ProbProgressBar.setValue(0);
     timer.stop();
     ProbSubmitButton.setText("Submit");
     ProbSubmitButton.setEnabled(true);
    state = SubmissionState.READY;
   }
 
   private void setSuccess() {
     ProbProgressBar.setString("Success!");
     ProbProgressBar.setValue(0);
     timer.stop();
     ProbSubmitButton.setText("Congratulations");
     ProbSubmitButton.setEnabled(false);
     ProbProgressBar.setValue(ProbProgressBar.getMaximum());
   }
 
   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         ProbProgressBar = new javax.swing.JProgressBar();
         ProbSubmitButton = new javax.swing.JButton();
         ProbLabel = new javax.swing.JLabel();
 
         ProbProgressBar.setMaximum(30000);
         ProbProgressBar.setString("Ready");
         ProbProgressBar.setStringPainted(true);
 
         ProbSubmitButton.setText("Submit");
         ProbSubmitButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ProbSubmitButtonActionPerformed(evt);
             }
         });
 
         ProbLabel.setText("jLabel1");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(ProbLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(ProbProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(ProbSubmitButton)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(ProbProgressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                     .addComponent(ProbLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                     .addComponent(ProbSubmitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
   @Override
   public void actionPerformed(ActionEvent evt) {
     int diff = (int) (System.currentTimeMillis() - startTime);
     //System.out.println("diff: "+diff);
     ProbProgressBar.setValue(diff);
     if (diff > 30000) {
       ProbProgressBar.setString("Timeout: contact an Admin!");
       ProbProgressBar.setForeground(Color.green);
     }
     // force a repaint to reduce jerkeyness
     ProbProgressBar.repaint();
   }
 
   private void ProbSubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ProbSubmitButtonActionPerformed
    System.out.println("Submit clicked with state: "+state);
     switch (state) {
       case READY:
         submissionWindow.startNewSubmission();
         break;
       case PENDING:
         cancel();
         break;
     }
   }//GEN-LAST:event_ProbSubmitButtonActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel ProbLabel;
     private javax.swing.JProgressBar ProbProgressBar;
     private javax.swing.JButton ProbSubmitButton;
     // End of variables declaration//GEN-END:variables
 }
