 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Viewer;
 
 import Model.Modeller;
 import Model.questionPaper.QuestionPaper;
 import Model.StudentSubmission.*;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  *
  * @author mbaxkak4
  */
 
 import Model.StudentSubmission.*;
 import Model.questionPaper.EssayQuestion;
 import Model.questionPaper.FITBQuestion;
 import Model.questionPaper.MBQuestion;
 import Model.questionPaper.MultipleChoiceQuestion;
 import Model.questionPaper.Question;
 import Model.questionPaper.Section;
 import Model.questionPaper.SubSection;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JPopupMenu.Separator;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 
 public class Marker extends JPanel {
     
     JLabel test=new JLabel();
     private static JFrame mainFrame;
     private int counter=0; 
     private Modeller model;
     private QuestionPaper paper;
     private static Submission submission;
     private JList submissionList;
     private DefaultListModel listModel;
     private GridBagConstraints c2 = new GridBagConstraints();
     private ListSelectionModel listSelectionModel;
     private JPanel rightPanel=new JPanel();
     
     public Marker(Modeller model,JFrame frame,QuestionPaper paper) {
     this.mainFrame=frame;
     this.model=model;
     this.paper=paper;
     initComponents();
     
     
     }
     
     void initComponents(){
     
         
        
         
         
         System.err.println(paper.getPaperID());
         setLayout(new GridBagLayout());
         GridBagConstraints con= new GridBagConstraints();
          
         con.gridx = 0;
         con.gridy = 0;
         con.weightx = 1.0;
         con.weighty = 1.0;
         con.fill = GridBagConstraints.BOTH;
         
         JPanel leftPanel=new JPanel();
         JScrollPane leftScroll=new JScrollPane(leftPanel);////////////////may be reversed
         
         
         
         leftPanel.setLayout(new GridBagLayout());
         leftPanel.setBorder(new TitledBorder("Student Submissions for "+paper.getTitle()));
         final ArrayList<Submission> submissions=model.getSubmissions();
         final ArrayList<Submission> listSubm=new ArrayList<>();
         
         GridBagConstraints c1= new GridBagConstraints();
         
         c1.gridx=0;
         c1.gridy=0;
         c1.anchor = GridBagConstraints.FIRST_LINE_START ;      
         c1.weightx = 1 ;
         c1.weighty = 1 ;
         c1.fill = GridBagConstraints.BOTH ;
         
         listModel = new DefaultListModel();
         submissionList=new JList(listModel);
         for(int i=0;i<submissions.size();++i){
         if(submissions.get(i).getPaperID()==paper.getPaperID()){
         submission=submissions.get(i);
         listSubm.add(submission);
         String subID=submission.getStudentID()+"";
         listModel.addElement(subID);
         }
         
         }
         submissionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         submissionList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
         
         listSelectionModel=submissionList.getSelectionModel();
         
         listSelectionModel.addListSelectionListener(new ListSelectionListener(){
              @Override
             public void valueChanged(ListSelectionEvent e) {
                 if (e.getValueIsAdjusting()){
                 rightPanel.removeAll();
                 rightPanel.setLayout(new GridBagLayout());
                 int f=submissionList.getSelectedIndex();
                 submission=listSubm.get(f);
                c2.gridx=0;
                c2.gridy=0;
                c2.anchor = GridBagConstraints.FIRST_LINE_START ;      
               // c2.weightx =  ;
                c2.weighty = 2 ;
                 displaySubmission(submission,c2);
                 
                 rightPanel.revalidate();
                 rightPanel.repaint();
                        
                   }         
                  }
         });
         
         
         
         
         
         JScrollPane listScroller = new JScrollPane(submissionList);
         leftPanel.add(listScroller,c1);
         this.add(leftScroll,con);
         ///////////////////////////////////////////////////
         
         
         con.gridx=1;
        con.weightx=5.0;
         
         JScrollPane rightScroll=new JScrollPane(rightPanel);
     
         rightPanel.setLayout(new GridBagLayout());
         rightPanel.setBorder(new TitledBorder("Submission Marker"));
     
         
         
        
       // con.fill = GridBagConstraints.BOTH ;
         //displaySubmission(submission,c2);
 //        /////////MarkView
   
         this.add(rightScroll,con);
         
     }
     
     
     
   public static void addListeners(MouseListener mouse){
    mainFrame.addMouseListener(mouse);
   }  
 
   void displaySubmission(Submission sub,GridBagConstraints c2){
               
        counter=0;  
        model.loadPapers();
        paper=model.getPaper(sub.getPaperID()-1);
    for (int k=0;k<sub.getSize();++k){
        
        SubmissionSection sec=sub.getSection(k);
        Section paperSec=paper.getSection(k);
        JLabel sectitle=new JLabel("Section: "+paperSec.getTitle());
         Font newLabelFont1=new Font(sectitle.getFont().getName(),Font.BOLD,sectitle.getFont().getSize()+5); 
         sectitle.setFont(newLabelFont1); 
        c2.gridx=0;
        c2.gridy=counter;
        rightPanel.add(sectitle,c2);
        
       // rightPanel.add((new Separator()).setPreferredSize(1000,1000));
        counter++;
       
        for(int l=0;l<sec.getSize();++l){
            SubmissionSubSection subsec=sec.getSubSection(l);
            SubSection paperSubsec=paperSec.getSubSection(l);
            JLabel subsecTitle=new JLabel("Subsection: "+paperSubsec.getTitle());
            Font newLabelFont=new Font(subsecTitle.getFont().getName(),Font.BOLD,subsecTitle.getFont().getSize()+3); 
            subsecTitle.setFont(newLabelFont); 
            
             c2.gridx=0;
             c2.gridy=counter;
             rightPanel.add(subsecTitle, c2);
             rightPanel.add(new Separator());
             counter++;
             populateSubmissionSubSection(subsec, paperSubsec, sec.getID(), null);
           
 
        } 
         
    
    
    }
   
   }
    private void populateSubmissionSubSection(SubmissionSubSection submSubSection, SubSection subSection, int sectionID, ArrayList<Integer> subSectionIDs) {
         if (submSubSection.getType()== SubmissionSubSection.CollectionType.SUBSECTIONS) {
             for (int i = 0; i < subSection.getNumberOfSubSections(); i++) {
                 JLabel subsecTitle=new JLabel("Subsection: "+subSection.getTitle());
           
            
                 c2.gridx=0;
                 c2.gridy=counter;
                 rightPanel.add(subsecTitle, c2);
                 rightPanel.add(new Separator());
                 counter++;
                 
 //                ArrayList<Integer> list = new ArrayList<>();
 //                for (int j = 0; j<subSectionIDs.size(); j++) {
 //                    list.add(subSectionIDs.get(j));
 //                }
 //                list.add(i);
                 populateSubmissionSubSection(submSubSection.getSubSection(i), subSection.getSubSection(i), sectionID, null);
            
             }
         } else if (submSubSection.getType()== SubmissionSubSection.CollectionType.ANSWERS) {
             ArrayList<Answer> unMarked=submSubSection.getUnmarkedQuestions();
             if(unMarked.isEmpty()) {
             c2.gridy=counter;
             c2.gridx=0;
               
                JLabel emptyLabel=new JLabel("There are no unmarked questions in this subsection!");
                rightPanel.add(emptyLabel, c2);
                counter++;
             }
             for (int m = 0; m < unMarked.size(); m++) {
                 
                final JLabel marksGiven=new JLabel();
                
                final Answer ans=submSubSection.getAnswer(m);
                final Question ques=subSection.getQuestion(m);
                    
                c2.gridy=counter;
                c2.gridx=0;
               
                JLabel questionLabel=new JLabel("Question   "+m);
                rightPanel.add(questionLabel, c2);
                c2.gridx=1;
              
                JLabel question=new JLabel("<html>"+ques.getQuestion()+"</html>") {
                    @Override
                    public Dimension getPreferredSize() {
                        Dimension d = super.getPreferredSize();
                        d.width = 400;
                        return d;
                    }
                };
                rightPanel.add(question,c2);
                c2.gridx=2;
               
                JButton plusMark=new JButton("+");
                plusMark.addActionListener(new ActionListener(){
 
                    @Override
                    public void actionPerformed(ActionEvent e) {
                     if(ans.getMark()<ques.getMark()){
                        ans.setMark(ans.getMark()+1);}
                     marksGiven.setText(""+ans.getMark());
                     //marksGiven.repaint();
                    }
                
                });
                rightPanel.add(plusMark,c2);
                c2.gridx=3;
              
                JButton minusMark=new JButton("-");
                minusMark.addActionListener(new ActionListener() {
 
                    @Override
                    public void actionPerformed(ActionEvent e) {
                     if(ans.getMark()>0){
                        ans.setMark(ans.getMark()-1);
                     marksGiven.setText(""+ans.getMark());
                     
                     }
                    }
                });
                rightPanel.add(minusMark,c2);
                c2.ipadx=0;
                counter++;
                c2.gridy=counter;
                c2.gridx=0;
               
                JLabel answerLabel=new JLabel("Answer   "+m);
                rightPanel.add(answerLabel, c2);
                c2.gridx=1;
               
                
                JLabel Answer=new JLabel("<html>"+ans.getAnswerString()+"</html>") {
                    @Override
                    public Dimension getPreferredSize() {
                        Dimension d = super.getPreferredSize();
                        d.width = 400;
                        return d;
                    }
                };
                rightPanel.add(Answer,c2);
                c2.gridx=2;
                marksGiven.setText(""+ans.getMark()+"");
                rightPanel.add(marksGiven,c2);
                c2.gridx=3;
                JLabel markTotal=new JLabel("out of "+ques.getMark()+" total marks");
                rightPanel.add(markTotal,c2);
                c2.ipadx=0;
            
                 counter++;
            }
       
             }
         }
     
 
 
    
    
 public static Submission getSubmission(){
 
 return submission;} 
 }
 
