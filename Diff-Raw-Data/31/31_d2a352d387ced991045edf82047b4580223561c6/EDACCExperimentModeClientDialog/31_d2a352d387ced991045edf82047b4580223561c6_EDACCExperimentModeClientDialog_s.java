 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * EDACCExperimentModeClient.java
  *
  * Created on 15.05.2011, 13:35:38
  */
 package edacc;
 
 import edacc.experiment.ExperimentResultCache;
 import edacc.experiment.ExperimentResultsBrowserTableModel;
 import edacc.experiment.ResultsBrowserTableRowSorter;
 import edacc.model.Client;
 import edacc.model.ClientDAO;
 import edacc.model.ExperimentResult;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 
 /**
  *
  * @author simon
  */
 public class EDACCExperimentModeClientDialog extends javax.swing.JDialog implements Observer {
 
     ExperimentResultsBrowserTableModel jobsTableModel;
     ExperimentResultCache resultCache;
     ResultsBrowserTableRowSorter resultsBrowserTableRowSorter;
     EDACCExperimentModeJobsCellRenderer tableJobsStringRenderer;
     Client client;
     Thread thread;
 
     /** Creates new form EDACCExperimentModeClient */
     public EDACCExperimentModeClientDialog(java.awt.Frame parent, boolean modal, Client client) {
         super(parent, modal);
         initComponents();
         this.client = client;
         jobsTableModel = new ExperimentResultsBrowserTableModel();
         tblJobs.setModel(jobsTableModel);
         resultsBrowserTableRowSorter = new ResultsBrowserTableRowSorter(jobsTableModel);
         resultsBrowserTableRowSorter.setSortsOnUpdates(true);
         tblJobs.setRowSorter(resultsBrowserTableRowSorter);
 
         tableJobsStringRenderer = new EDACCExperimentModeJobsCellRenderer();
         tblJobs.setDefaultRenderer(Object.class, new EDACCExperimentModeJobsCellRenderer());
         tblJobs.setDefaultRenderer(String.class, tableJobsStringRenderer);
         tblJobs.setDefaultRenderer(Integer.class, new EDACCExperimentModeJobsCellRenderer());
         tblJobs.setDefaultRenderer(Float.class, new EDACCExperimentModeJobsCellRenderer());
 
         resultCache = new ExperimentResultCache(client);
         thread = new Thread(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException ex) {
                 }
                 while (EDACCExperimentModeClientDialog.this.isVisible()) {
                     loadJobs();
                     try {
                         Thread.sleep(5000);
                     } catch (InterruptedException ex) {
                         break;
                     }
                 }
             }
         });
 
         client.addObserver(this);
     }
 
     @Override
     public void setVisible(boolean b) {
         if (b == true) {
             thread.start();
         }
         super.setVisible(b);
     }
 
     /**
      * Updates the job browser table
      */
     private synchronized void loadJobs() {
         try {
             resultCache.updateExperimentResults();
             final ExperimentResultsBrowserTableModel sync = jobsTableModel;
             synchronized (sync) {
 
                 ArrayList<ExperimentResult> results = jobsTableModel.getJobs();
                 boolean[] updateRows = null;
                 if (results != null) {
                     updateRows = new boolean[results.size()];
                     if (results.size() != resultCache.size()) {
                         results = null;
                     } else {
                         for (int i = 0; i < results.size(); i++) {
                             ExperimentResult er = results.get(i);
                             ExperimentResult tmp = resultCache.getResult(er.getSolverConfigId(), er.getInstanceId(), er.getRun());
                             if (tmp == null) {
                                 results = null;
                                 break;
                             } else if (!er.getDatemodified().equals(tmp.getDatemodified())) {
                                 results.set(i, tmp);
                                 updateRows[i] = true;
                             }
                         }
                         if (results != null && results.size() != resultCache.size()) {
                             results = null;
                         }
                     }
                 }
                 if (results == null) {
                     results = new ArrayList<ExperimentResult>();
                     results.addAll(resultCache.values());
                     jobsTableModel.setJobs(results);
                     //main.resultBrowserRowFilter.updateFilterTypes();
                     jobsTableModel.fireTableDataChanged();
                 } else {
                     // repaint the table: updates the currently visible rectangle, otherwise there might be duplicates of rows.
                     // this has to be done in the EDT.
                     final boolean[] urows = updateRows;
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             int beg = -1;
                             for (int i = 0; i < urows.length; i++) {
                                 if (urows[i]) {
                                     if (beg == -1) {
                                         beg = i;
                                     }
                                 } else {
                                     if (beg != -1) {
                                         jobsTableModel.fireTableRowsUpdated(beg, i);
                                         beg = -1;
                                     }
                                 }
                             }
                             tblJobs.invalidate();
                             tblJobs.revalidate();
                             tblJobs.repaint();
                         }
                     });
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             // TODO: shouldn't happen but show message if it does
         }
 
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel1 = new javax.swing.JPanel();
         txtMessage = new javax.swing.JTextField();
         btnSendMessage = new javax.swing.JButton();
         jPanel2 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         tblJobs = new javax.swing.JTable();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(edacc.EDACCApp.class).getContext().getResourceMap(EDACCExperimentModeClientDialog.class);
         setTitle(resourceMap.getString("Form.title")); // NOI18N
         setName("Form"); // NOI18N
 
         jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
         jPanel1.setName("jPanel1"); // NOI18N
 
         txtMessage.setText(resourceMap.getString("txtMessage.text")); // NOI18N
         txtMessage.setName("txtMessage"); // NOI18N
         txtMessage.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtMessageKeyReleased(evt);
             }
         });
 
         btnSendMessage.setText(resourceMap.getString("btnSendMessage.text")); // NOI18N
         btnSendMessage.setName("btnSendMessage"); // NOI18N
         btnSendMessage.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSendMessageActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                 .addComponent(txtMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnSendMessage))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txtMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnSendMessage))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
         jPanel2.setName("jPanel2"); // NOI18N
 
         jScrollPane1.setName("jScrollPane1"); // NOI18N
 
         tblJobs.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         tblJobs.setName("tblJobs"); // NOI18N
         jScrollPane1.setViewportView(tblJobs);
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnSendMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendMessageActionPerformed
         try {
             ClientDAO.sendMessage(client, txtMessage.getText());
             txtMessage.setText("");
             txtMessage.requestFocus();
         } catch (Exception ex) {
             javax.swing.JOptionPane.showMessageDialog(this, "Error while trying to send message.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_btnSendMessageActionPerformed
 
     private void txtMessageKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMessageKeyReleased
         if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
             btnSendMessageActionPerformed(null);
         }
     }//GEN-LAST:event_txtMessageKeyReleased
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnSendMessage;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTable tblJobs;
     private javax.swing.JTextField txtMessage;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public void update(Observable o, Object arg) {
         if (arg instanceof Client && arg == client) {
             if (client.isDeleted()) {
                 javax.swing.JOptionPane.showMessageDialog(this, "The client signed off.", "Client is gone", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                 this.setVisible(false);
             }
         }
     }
 
     class LoadJobsThread extends SwingWorker<Void, Integer> {
 
         @Override
         @SuppressWarnings("SleepWhileInLoop")
         protected Void doInBackground() throws Exception {
             while (!this.isCancelled() && EDACCExperimentModeClientDialog.this.isVisible()) {
                 publish(0); // notify
                 Thread.sleep(5000);
             }
             return null;
         }
 
         @Override
         protected void process(List<Integer> chunks) {
             loadJobs();
         }
     }
 }
