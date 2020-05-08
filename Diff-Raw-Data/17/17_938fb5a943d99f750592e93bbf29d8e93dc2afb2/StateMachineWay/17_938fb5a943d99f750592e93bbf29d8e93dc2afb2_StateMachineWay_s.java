 package habrahabrguistatemachine;
 
 import habrahabrguistatemachine.sm.BaseGuiState;
 import habrahabrguistatemachine.sm.GuiStateManager;
 
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.logging.Logger;
 
 import javax.swing.AbstractAction;
 import javax.swing.JOptionPane;
 import javax.swing.SwingWorker.StateValue;
 
 public class StateMachineWay extends javax.swing.JFrame {
 	private Logger logger = Logger.getLogger(StandardWay.class.getName());
 	/**
 	 * controlls switching between gui states
 	 */
 	private GuiStateManager stateManager = new GuiStateManager();
 	private class PreparingAnswererState extends BaseGuiState{
 		@Override
 		public void enterState() {
 			labelStatus.setText("Prepearing... ");
             doButton.setEnabled(false);
 		}
 	}
 	private class ReadyToFindTheAnswer extends BaseGuiState{
 		private final MeaningOfLifeAnswerer answerer;
 		public ReadyToFindTheAnswer(MeaningOfLifeAnswerer answerer){
 			this.answerer = answerer;
 		}
 		@Override
 		public void enterState() {
 			labelStatus.setText("I am prepared to answer the meaning of life");
             doButton.setEnabled(true);
             doButton.setAction(new FindAnswerAction(answerer));
 		}
 	}
 	private class FoundAnswerState extends BaseGuiState{
 		private final Integer answer;
 		public FoundAnswerState(Integer answer){
 			this.answer = answer;
 		}
 		@Override
 		public void enterState() {
 			labelStatus.setText("answer was found");
             doButton.setText("Find again");
             doButton.setEnabled(true);
 			JOptionPane.showMessageDialog(rootPane, "THE ANSWER IS " + answer);
 		}
 	}
 	private class FailedToPrepareAnswerer extends BaseGuiState{
 		@Override
 		public void enterState() {
 			JOptionPane.showMessageDialog(rootPane, "failed to find answerer to the question");
             dispose();
 		}
 	}
 	private class FailedToFoundAnswer extends BaseGuiState{
 		@Override
 		public void enterState() {
 			labelStatus.setText("failed to find answer");
             doButton.setText("Try again");
             doButton.setEnabled(true);
 			JOptionPane.showMessageDialog(rootPane, "Error while searching for meaning of life");
 		}
 	}
 	private class SearchingForAnswer extends BaseGuiState{
 		@Override
 		public void enterState() {
 			labelStatus.setText("searching...");
             doButton.setText("In Search");
             doButton.setEnabled(false);
 		}
 	}
     private class FindAnswerAction extends AbstractAction{
         private final MeaningOfLifeAnswerer answerer;
         public FindAnswerAction(MeaningOfLifeAnswerer answerer){
             super("Find");
             this.answerer = answerer;
         }
         @Override
         public void actionPerformed(ActionEvent e) {
             RetrieveMeaningOfLife retrieveWorker = new RetrieveMeaningOfLife(answerer);
             retrieveWorker.addPropertyChangeListener(new PropertyChangeListener() {
                 @Override
                 public void propertyChange(PropertyChangeEvent evt) {
                     if ("progress".equals(evt.getPropertyName())){
                         progressBar.setValue((Integer)evt.getNewValue());
                     }
                     if ("state".equals(evt.getPropertyName())){
                         if (StateValue.DONE.equals(evt.getNewValue())){
                             RetrieveMeaningOfLife worker = (RetrieveMeaningOfLife)evt.getSource();
                             try{
                                 Integer answer = worker.get();
                                 stateManager.switchTo(new FoundAnswerState(answer));
                                 logger.info("got the answer");
                             }
                             catch(Exception ex){
                             	logger.info("error while retrieving the answer");
                             	stateManager.switchTo(new FailedToFoundAnswer());
                             }
                         }
                         if (StateValue.STARTED.equals(evt.getNewValue())){
                         	stateManager.switchTo(new SearchingForAnswer());
                         }
                     }
                 }
             });
             retrieveWorker.execute();
         }
     }
     /**
      * listener that updates gui state by progress of PrepareToAnswerMeaningOfLife worker
      * @author __nocach
      *
      */
     private class PrepareToAnswerMeaningOfLifeListener implements PropertyChangeListener{
     	 @Override
          public void propertyChange(PropertyChangeEvent evt) {
              if ("state".equals(evt.getPropertyName())){
                  if (StateValue.STARTED.equals(evt.getNewValue())){
                 	 logger.info("preparing...");
                      stateManager.switchTo(new PreparingAnswererState());
                  }
                  if (StateValue.DONE.equals(evt.getNewValue())){
                      PrepareToAnswerMeaningOfLife worker = (PrepareToAnswerMeaningOfLife)evt.getSource();
                      try{
                     	 MeaningOfLifeAnswerer meaningOfLifeAnswerer = worker.get();
                     	 stateManager.switchTo(new ReadyToFindTheAnswer(meaningOfLifeAnswerer));
                          logger.info("prepared");
                      }
                      catch(Exception ex){
                          logger.severe("failed to prepare");
                          stateManager.switchTo(new FailedToPrepareAnswerer());
                      }
                  }
              }
          }
     }
 
     /** Creates new form StandardWay */
     public StateMachineWay() {
         initComponents();
         PrepareToAnswerMeaningOfLife prepareWorker = new PrepareToAnswerMeaningOfLife();
         prepareWorker.addPropertyChangeListener(new PrepareToAnswerMeaningOfLifeListener());
         prepareWorker.execute();
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         doButton = new javax.swing.JButton();
         labelStatus = new javax.swing.JLabel();
         progressBar = new javax.swing.JProgressBar();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         doButton.setText("FindAnswer");
 
         labelStatus.setText("jLabel1");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(doButton, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelStatus)
                     .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap(19, Short.MAX_VALUE)
                 .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(labelStatus)
                 .addGap(18, 18, 18)
                 .addComponent(doButton)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(StandardWay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(StandardWay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(StandardWay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(StandardWay.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new StateMachineWay().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton doButton;
     private javax.swing.JLabel labelStatus;
     private javax.swing.JProgressBar progressBar;
     // End of variables declaration//GEN-END:variables
 }
