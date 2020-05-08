 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package melt.View.marker;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import melt.Controller;
 import melt.Model.Mcq;
 
 /**
  *
  * @author mbaxjah5
  */
 public class McqPanel extends javax.swing.JPanel {
 
     private Controller controller;
     private Mcq mcqQuestion;
     private JCheckBox[] answerCheckBoxes;
     JTextArea txtQuestionText;
     /**
      * Creates new form McqPanel
      */
     public McqPanel() {
         initComponents();
         txtQuestionText = new JTextArea("");
         answerCheckBoxes = new JCheckBox[6];
         for (int i = 0; i < 6; i++) {
             answerCheckBoxes[i] = new JCheckBox();
         }
         //this.controller = controller;
     }
 
     public void setQuestion(Mcq mcq) {
         this.mcqQuestion = mcq;
         preview();
     }
 
     private void clear() {
         txtQuestionText.setText("");
 
         for (JCheckBox box : answerCheckBoxes) {
             box.setText("");
             box.setSelected(false);
             box.setEnabled(false);
             box.setVisible(true);
         }
 
     }
 
     public void preview() {
  
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(this);
         this.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(30, 30, 30)
                 .addComponent(txtQuestionText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(35, 35, 35)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(answerCheckBoxes[5], javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(answerCheckBoxes[4], javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(answerCheckBoxes[3], javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(answerCheckBoxes[2], javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(answerCheckBoxes[1], javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(answerCheckBoxes[0], javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(20, Short.MAX_VALUE)));
 
         jPanel1Layout.setVerticalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addComponent(txtQuestionText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(answerCheckBoxes[0])
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(answerCheckBoxes[1])
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(answerCheckBoxes[2])
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(answerCheckBoxes[3])
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(answerCheckBoxes[4])
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(answerCheckBoxes[5])
                 .addContainerGap(20, 20)));
         
 
         clear();
         txtQuestionText.setWrapStyleWord(true);
         txtQuestionText.setLineWrap(true);
         txtQuestionText.setEditable(false);
        
         //txtQuestionText.setOpaque(false);
         txtQuestionText.setBackground(new Color(0,0,0,0));
         txtQuestionText.setText(this.mcqQuestion.getQuestionText());
         //lblQuestion.setText("<html>" + this.mcqQuestion.getQuestionText() + "<br>Full Marks: " + this.mcqQuestion.getMark() + "<br>Student Marks: " + this.mcqQuestion.getStudentMark() +"</html>");
         txtQuestionText.setFont(new java.awt.Font("MV Boli", 0, 16));
         ArrayList<String> questionAnswers = mcqQuestion.getAnswers();
         ArrayList<Integer> studentAnswers = mcqQuestion.getStudentAnswers();
         ArrayList<Integer> correctAnswers = mcqQuestion.getCorrectAnswers();
 
         int cnt;
 
         for (cnt = 0; cnt < questionAnswers.size(); cnt++) {
             answerCheckBoxes[cnt].setText(questionAnswers.get(cnt));
 
         }
 
         for (cnt = cnt; cnt < 6; cnt++) {
             answerCheckBoxes[cnt].setVisible(false);
         }
 
         if (studentAnswers != null) {
             for (int i = 0; i < studentAnswers.size(); i++) {
                 answerCheckBoxes[studentAnswers.get(i)].setSelected(true);
                 //answerCheckBoxes[studentAnswers.get(i)].setForeground(Color.RED);
             }
         }
 
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 400, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 300, Short.MAX_VALUE)
         );
     }// </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     // End of variables declaration//GEN-END:variables
 }
