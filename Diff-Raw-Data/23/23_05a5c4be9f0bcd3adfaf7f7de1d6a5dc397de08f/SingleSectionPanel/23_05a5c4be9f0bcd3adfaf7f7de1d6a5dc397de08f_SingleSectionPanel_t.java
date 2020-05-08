 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package melt.View;
 
 import java.util.ArrayList;
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import melt.Model.Essay;
 import melt.Model.Fibq;
 import melt.Model.Mcq;
 import melt.Model.Question;
 import melt.Model.Section;
 import melt.Model.Subsection;
 
 /**
  *
  * @author panos
  */
 public class SingleSectionPanel extends JPanel {
 
     private Section sectionObject;
     ArrayList<SingleQuestionPanel> mcqQuestionPanels;
     ArrayList<SingleEssayQuestionPanel> essayQuestionPanels;
     ArrayList<SingleFibqQuestionPanel> fibqQuestionPanels;
     public SingleSectionPanel(Section section) {
 
         mcqQuestionPanels = new ArrayList();
         essayQuestionPanels = new ArrayList();
         fibqQuestionPanels = new ArrayList();
         //fibqQuestionPanels = new ArrayList();
         this.sectionObject = section;
         this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
     }
 
     public void setSectionObject(Section sectionObject) {
         this.sectionObject = sectionObject;
     }
 
     public void showSection() {
         final JPanel newPanel = new JPanel();
         JLabel sectionNameLabel = new JLabel(sectionObject.getName());
         sectionNameLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 16));
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(newPanel);
         newPanel.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(20, 20, 20)
                 .addComponent(sectionNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap()));
 
         jPanel1Layout.setVerticalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addComponent(sectionNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(10, 10)));
 
         this.add(newPanel);
         for (Subsection subsection : sectionObject.getSubsections()) {
             showSubsection(subsection);
 
         }
     }
 
     public void showSubsection(Subsection subsection) {
         final JPanel newPanel = new JPanel();
         JLabel subsectionNameLabel = new JLabel(subsection.getName());
         subsectionNameLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 16));
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(newPanel);
         newPanel.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(20, 20, 20)
                 .addComponent(subsectionNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap()));
 
         jPanel1Layout.setVerticalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addComponent(subsectionNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(10, 10)));
         this.add(newPanel);
         switch (subsection.getType()) {
             case "Mcq":
                 for (Question question : subsection.getQuestions()) {
                     SingleQuestionPanel questionPanel = new SingleQuestionPanel((Mcq) question);
                     this.mcqQuestionPanels.add(questionPanel);
                     this.add(questionPanel.showQuestion());
                 }
                 break;
             case "Essay":
                 for(Question question : subsection.getQuestions()){
                     SingleEssayQuestionPanel essayQuestionPanel = new SingleEssayQuestionPanel((Essay) question);
                     this.essayQuestionPanels.add(essayQuestionPanel);
                     this.add(essayQuestionPanel.showQuestion());
                 }
                break;
             case "Fibq":
                 for(Question question : subsection.getQuestions()){
                     SingleFibqQuestionPanel fibqQuestionPanel = new SingleFibqQuestionPanel((Fibq) question);
                     this.fibqQuestionPanels.add(fibqQuestionPanel);
                     this.add(fibqQuestionPanel.showQuestion());
                 }
                break;
         }
 
 
     }
 
     public static void main(String[] args) {
 
         Section section = new Section(1, 5, "section 1 ", "instructions");
         Subsection subsection = new Subsection("Mcq", "Subsection 1", 1);
         Subsection subsection2 = new Subsection("Essay","Subsection 2",2);
         Essay essay = new Essay(1, "new essay subject", 10);
         subsection2.addQuestion(essay);
         String questionText = "question 1 ";
         ArrayList<String> answers = new ArrayList();
         answers.add("answer1");
         answers.add("answer2");
         ArrayList<Integer> correctAnswers = new ArrayList();
         correctAnswers.add(1);
         
         
         
         
         Mcq question = new Mcq(answers, correctAnswers, 1, questionText, 4.0);
         subsection.addQuestion(question);
         section.addSubsection(subsection);
         section.addSubsection(subsection2);
 
 
 
         SingleSectionPanel sectionPanel = new SingleSectionPanel(section);
         JFrame aframe = new JFrame();
         aframe.add(sectionPanel);
         sectionPanel.showSection();
 
         aframe.setSize(800, 600);
         aframe.setVisible(true);
     }
 
     public void setPanelAnswers() {
         for (SingleQuestionPanel questionPanel : mcqQuestionPanels) {
             questionPanel.setStudentAnswer();
         }
     }
 }
