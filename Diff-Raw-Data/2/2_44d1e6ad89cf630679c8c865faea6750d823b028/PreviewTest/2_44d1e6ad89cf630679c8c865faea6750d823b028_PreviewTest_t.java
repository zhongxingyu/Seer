 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package melt.View;
 
 import java.awt.BorderLayout;
 import java.awt.ComponentOrientation;
 import java.awt.FlowLayout;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 import javax.swing.BoxLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import melt.Controller;
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
         lblTitle.setFont(new java.awt.Font("DejaVu Sans", 0, 24));
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
         lblName.setFont(new java.awt.Font("DejaVu Sans", 0, 24));
 
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
         }
     }
 
     public void previewSubsection(Subsection sub) {
         final JPanel newPanel = new JPanel();
         JLabel lblName = new javax.swing.JLabel("Subsection");
         lblName.setFont(new java.awt.Font("DejaVu Sans", 0, 20));
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(newPanel);
         newPanel.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(20, 20, 20)
                 .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap()));
 
         jPanel1Layout.setVerticalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(10, 10)));
 
         listContainer.add(newPanel);
         listContainer.revalidate();
 
         //if (sub.getType().equals("MCQ")) {
         for (Question quest : sub.getQuestions()) {
             if (quest instanceof Mcq) {
                 previewQuestion((Mcq) quest);
             }
             if (quest instanceof Fibq) {
                 previewQuestion((Fibq) quest);
             }
         }
         //}
     }
 
     @SuppressWarnings("SillyAssignment")
     public void previewQuestion(Mcq mcq) {
         final JPanel newPanel = new JPanel();
         JLabel lblName = new javax.swing.JLabel(mcq.getQuestionText());
         lblName.setFont(new java.awt.Font("DejaVu Sans", 0, 16));
         ArrayList<String> ans = mcq.getAnswers();
         JCheckBox jCheckBox[] = new javax.swing.JCheckBox[6];
         int cnt;
 
         for (cnt = 0; cnt < ans.size(); cnt++) {
             jCheckBox[cnt] = new JCheckBox();
             jCheckBox[cnt].setText(ans.get(cnt));
         }
 
         for (cnt = cnt; cnt < 6; cnt++) {
             jCheckBox[cnt] = new JCheckBox();
             jCheckBox[cnt].setVisible(false);
         }
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(newPanel);
         newPanel.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(30, 30, 30)
                 .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(35, 35, 35)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(jCheckBox[5], javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jCheckBox[4], javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jCheckBox[3], javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jCheckBox[2], javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jCheckBox[1], javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jCheckBox[0], javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(20, Short.MAX_VALUE)));
 
         jPanel1Layout.setVerticalGroup(
                 jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                 .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jCheckBox[0])
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jCheckBox[1])
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jCheckBox[2])
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jCheckBox[3])
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jCheckBox[4])
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jCheckBox[5])
                 .addContainerGap(20, 20)));
 
         listContainer.add(newPanel);
         listContainer.revalidate();
     }
 
     public void previewQuestion(Fibq fibq) {
         final JPanel newPanel = new JPanel();
         newPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        newPanel.setPreferredSize(new java.awt.Dimension(400, 10));
         JLabel questionLabel = new JLabel("Question:");
         newPanel.add(questionLabel);
         String questionText = fibq.getQuestionText();
         String[] qWithoutBlanks = questionText.split("_", 0);
         char firstChar = questionText.charAt(0);
         char lastChar = questionText.charAt(questionText.length() - 1);
         int noOfBlanks = qWithoutBlanks.length - 1;
 
         if ("_".equals(String.valueOf(lastChar))) {
             noOfBlanks++;
         }
 
         JLabel[] labels = new JLabel[qWithoutBlanks.length];
         JTextField[] blanks = new JTextField[noOfBlanks];
 
         int j = 0;
         if ("_".equals(String.valueOf(firstChar))) {
             blanks[j] = new JTextField();
             blanks[j].setColumns(15);
             newPanel.add(blanks[j]);
             j++;
         }
 
         for (int i = 0; i < qWithoutBlanks.length; i++) {
             if ("".equals(qWithoutBlanks[i])) {
                 continue;
             }
             labels[i] = new JLabel(qWithoutBlanks[i]);
             labels[i].setFont(new java.awt.Font("DejaVu Sans", 0, 16));
             newPanel.add(labels[i]);
             if (i < noOfBlanks) {
                 blanks[j] = new JTextField();
                 blanks[j].setColumns(15);
                 newPanel.add(blanks[j]);
                 j++;
             }
         }
 
         /* if ("_".equals(String.valueOf(lastChar))) {
          blanks[j] = new JTextField();
          newPanel.add(blanks[j]);
          }*/
 
         newPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
         listContainer.add(newPanel);
         listContainer.revalidate();
     }
 }
