 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Viewer;
 
 import Model.Modeller;
 import Model.QuestionPaper;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  *
  * @author mbaxkak4
  */
 public class TeacherView extends JPanel {
     
     private int j;
     private QuestionPaper paper;
     private JFrame mainFrame;
     private Modeller amodel;
     
     public TeacherView(JFrame frame,Modeller model){
         
         mainFrame = frame;
         amodel = model;
         initComponents();
         
     }
     
     
        private JButton createTest;
        private java.awt.Label previousLabel;
    
     private void initComponents()  {
         
         previousLabel=new Label();
         createTest=new JButton() ;
         
         
         
         setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         
        
         
         c.anchor = GridBagConstraints.CENTER;
         c.gridx = GridBagConstraints.RELATIVE;
         c.gridwidth = GridBagConstraints.REMAINDER;
         c.gridy = 0;
         c.ipady = 50;
         c.ipadx = 30;
         c.insets = new Insets(20,20,20,20);
         c.weighty = 10;
         createTest.setText("Create a New Test");
         createTest.setPreferredSize(new Dimension(200, 40));
         createTest.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                   ((Viewer)mainFrame).guiChanger(new TestWizard(mainFrame,amodel));
             }
         });
         this.add(createTest,c);
         
         
         
         
         GridBagConstraints c1 = new GridBagConstraints();
         c1.fill = GridBagConstraints.HORIZONTAL;
         c1.weightx = 1;
         c1.weighty = 0;
         c1.gridx = GridBagConstraints.RELATIVE;
         c1.gridy = 1;
         c1.ipady = 30;
         c1.ipadx = 80;
         c1.anchor = GridBagConstraints.CENTER;
         c1.gridwidth = GridBagConstraints.REMAINDER;
         previousLabel.setAlignment(java.awt.Label.CENTER);
         previousLabel.setBackground(new java.awt.Color(245, 245, 245));
         previousLabel.setFont(new java.awt.Font("Monospaced", 3, 16)); // NOI18N
         previousLabel.setText("List of older English Language tests ");
         this.add(previousLabel,c1);
         
         GridBagConstraints c2 = new GridBagConstraints();
         c2.anchor = GridBagConstraints.SOUTH;
         c2.gridx = GridBagConstraints.RELATIVE;
         
         //c2.gridx = 0;
         //c2.gridy = 0;
        c2.ipadx = 20;
        c2.ipady = 20;
         c2.insets = new Insets(50, 0, 30, 20);
         c2.weightx = 0.5;
         c2.insets = new Insets(50, 20, 30, 0);
         c2.anchor = GridBagConstraints.SOUTH;
         //c2.gridwidth = GridBagConstraints.REMAINDER;
         c2.gridy = 2;
         
         amodel.loadPapers("papers/Papers.xml");
         
         ArrayList<QuestionPaper> papers = amodel.getPapersByStudentID(12301230);
         
         for(int i=0;i<papers.size();++i){
             j=i;
             c2.gridy=c2.gridy+i;
            c2.gridx = i;
             this.add(new PaperPreviewer(papers.get(i)),c2);
             
            c2.gridx=i+1;
             JButton editButton=new JButton("Edit Test");
             editButton.addActionListener(new EditListener());
             this.add(editButton,c2);
             
            c2.gridx=i+2;
             JButton previewButton=new JButton("Preview Test");
             previewButton.addActionListener(new PreviewListener());
             this.add(previewButton,c2);
            
         }
         
      
     
       }
 
     private class  PreviewListener implements ActionListener{
         
         @Override
         public void actionPerformed(ActionEvent evt){
             TestPreviewer tp=new TestPreviewer(amodel,paper);
         
         }
     }
      private class  EditListener implements ActionListener{
         
         @Override
         public void actionPerformed(ActionEvent evt){
           //  TestPreviewer tp=new TestPreviewer(amodel,j);
             //tp.setVisible(true);
         }
     }
     
     
 
 
 }
