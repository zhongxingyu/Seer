 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package melt.View;
 
 import java.awt.BorderLayout;
 import java.awt.ComponentOrientation;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.util.ArrayList;
 import javax.swing.BoxLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import melt.Controller;
 import melt.Model.Essay;
 import melt.Model.Fibq;
 import melt.Model.Mcq;
 import melt.Model.Question;
 import melt.Model.Section;
 import melt.Model.Subsection;
 import melt.Model.Test;
 
 /**
  *
  * @author Maria
  */
 public class PreviewTest extends JFrame {
 
     private Controller controller;
     private int i;
     private JPanel listContainer;
     
     private JFrame frame = new JFrame("Preview");
 
     public PreviewTest(Controller controller) {
         this.controller = controller;
         initUI();
     }
 
     private void initUI() {
         listContainer = new JPanel();
         
         listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
         JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
         frame.add(scrollPane, BorderLayout.CENTER);
         final JPanel newPanel = new JPanel();
         JLabel lblTitle = new JLabel("Preview Mode");
         lblTitle.setFont(new java.awt.Font("Snap ITC", 1, 30));
         newPanel.add(lblTitle);
         listContainer.add(newPanel);
         listContainer.revalidate();
         frame.setSize(800, 600);
         frame.setVisible(true);
     }
 
     public void previewTest(Test test) {
         for (Section sec : test.getSections()) {
             previewSection(sec);
         }
     }
 
     public void previewSection(Section sec) {
         final JPanel newPanel = new JPanel();
        JLabel lblName = new javax.swing.JLabel("Section");
         lblName.setFont(new java.awt.Font("MV Boli", 1, 26));

         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(newPanel);
         newPanel.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(10, 10, 10)
                 .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap()));
 
         jPanel1Layout.setVerticalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(10, 10)));
 
         listContainer.add(newPanel);
         listContainer.revalidate();
 
         for (Subsection sub : sec.getSubsections()) {
             previewSubsection(sub);
             listContainer.revalidate();
         }
     }
 
     public void previewSubsection(Subsection sub) {
         final JPanel newPanel = new JPanel();
        JLabel lblName = new javax.swing.JLabel("Subsection");
         lblName.setFont(new java.awt.Font("MV Boli", 1, 22));
         
        javax.swing.BoxLayout jpanel1layout = new javax.swing.BoxLayout(newPanel,BoxLayout.Y_AXIS);
         
         //javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(newPanel);
        newPanel.setLayout(jpanel1layout);
         
         
         listContainer.add(newPanel);
         listContainer.revalidate();
 
         for (Question quest : sub.getQuestions()) {
             if (quest instanceof Mcq) {
                 previewQuestion((Mcq) quest);
             }
             if (quest instanceof Fibq) {
                 previewQuestion((Fibq) quest);
             }
             if(quest instanceof Essay){
                 previewQuestion((Essay) quest);
             }
         }
         
         
     }
 
     @SuppressWarnings("SillyAssignment")
     public void previewQuestion(Mcq mcq) {
         SingleQuestionPanel mcqPanel = new SingleQuestionPanel(mcq);
         JPanel panelforShowing = mcqPanel.showQuestion();
         listContainer.add(panelforShowing);
         listContainer.revalidate();
     }
 
     public void previewQuestion(Fibq fibq) {
         SingleFibqQuestionPanel fibqPanel = new SingleFibqQuestionPanel(fibq);
         JPanel panelforShowing = fibqPanel.showQuestion();
         listContainer.add(panelforShowing);
         listContainer.revalidate();
     }
     public void previewQuestion(Essay essayObject){
        SingleEssayQuestionPanel essayPanel = new SingleEssayQuestionPanel(essayObject);
        JPanel panelforShowing  = essayPanel.showQuestion();
        listContainer.add(panelforShowing);
        listContainer.revalidate();
     }
 }
