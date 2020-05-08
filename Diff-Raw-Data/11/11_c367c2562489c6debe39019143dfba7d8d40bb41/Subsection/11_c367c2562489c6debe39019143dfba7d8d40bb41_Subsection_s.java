 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Viewer;
 
 import Viewer.PaperViews.QuestionView;
 import Model.Modeller;
 import Model.questionPaper.Question;
 import Model.questionPaper.QuestionPaper;
 import Model.questionPaper.MultipleChoiceQuestion;
 import Model.questionPaper.Section;
 import Model.questionPaper.SubSection;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.util.ArrayList;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.plaf.TabbedPaneUI;
 
 /**
  *
  * @author Daniel
  */
 public class Subsection extends JPanel{
     private JButton createQuestion;
     private JPanel mainPane;
     private int firstI  ;
     private int lastI  ;
     private int index ;
     private String activeSubsection ;
     JList questionList = new JList() ;
     public Modeller amodel;
     public SubSection subSection;
     private JPanel rightPanel;
     private QuestionPaper paper ;
     GridBagConstraints gbc = new GridBagConstraints();
     Section section;
     public DefaultListModel listModel;
     private TestWizard wizard;
     Subsection subsectionPanel;
     Subsection(SubSection subSection,Section section, Modeller model, QuestionPaper paper, TestWizard wizard){
         this.paper = paper ;
         this.section = section;
         mainPane = this;
         amodel = model;
         if(subSection == null){
            subSection = new SubSection();
            section.AddSubSection(subSection);
         }
         else{
             this.subSection = subSection;
         }
         this.rightPanel = rightPanel;
         this.subsectionPanel = this;
         this.wizard = wizard;
         initComponents();
     }
 
     private void initComponents() {
         
         
         
         //need for list listener
         firstI = -1 ;
         lastI = -1 ;
         index = -1 ;
         
         setLayout(new GridBagLayout());
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.anchor = GridBagConstraints.FIRST_LINE_START ;      
         gbc.weightx = 1 ;
         gbc.weighty = 1 ;
         gbc.fill = GridBagConstraints.BOTH ;
         
         questionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         questionList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
         //questionList.setVisibleRowCount(-1) ;
         
         
         
         listModel = new DefaultListModel();
         
         /*
         amodel.loadPapers("papers/Papers.xml");
         ArrayList<QuestionPaper> papers = amodel.getPapersByStudentID(12301230);
 */
         
         final Question[] questions;
         QuestionView[] questionViews;
         
         //QuestionPaper paper = papers.get(0);
 //        final Section section = paper.getSection(0);
 //        final SubSection subSection = section.getSubSection(0);
         
 //        String[] Answers = new String[2];
 //        Answers[0] = "hsdfbdsjhfb";
 //        Answers[1] = "sdkjgfmldgm";
 //        int [] possibleAnswers = new int[2] ;
 //        possibleAnswers[0] = 0 ;
 //        possibleAnswers[1] = 1 ;
 //        
         listModel.addElement("New question");        
 //        
 //        MultipleChoiceQuestion a ;
 //        a = new MultipleChoiceQuestion("rololol", Answers, "what", possibleAnswers, 5);
 //        //listModel.addElement(questions);
 //        subSection.addQuestion(a);
 //        
         questions = new Question[subSection.getNumberOfQuestions()];
         questionViews = new QuestionView[questions.length];
 
         for (int i = 0; i < subSection.getNumberOfQuestions(); i++) {
             questions[i] = subSection.getQuestion(i);
             // TODO //
             questionViews[i] = new QuestionView(questions[i], null, 0, 0, 0);
             listModel.addElement(questions[i].getQuestion());
             
           }
         //listModel.addElement(questions[1].getQuestion());
         
         /*
         listModel = new DefaultListModel();
         listModel.addElement("New question");
         listModel.addElement("asdsadsadasd");
         listModel.addElement("xcvcxvcxvcxvxc");
         listModel.addElement("asdsadsadasd");
         listModel.addElement("xcvcxvcxvcxvxc");
         listModel.addElement("asdsadsadasd");
         listModel.addElement("xcvcxvcxvcxvxc");
         listModel.addElement("asdsadsadasd");
         listModel.addElement("xcvcxvcxvcxvxc");
         listModel.addElement("asdsadsadasd");
         listModel.addElement("xcvcxvcxvcxvxc");
         listModel.addElement("asdsadsadasd");
         listModel.addElement("xcvcxvcxvcxvxc");
         */
         
         questionList = new JList(listModel) ;
         
         JScrollPane listScroller = new JScrollPane(questionList);
         add(listScroller, gbc) ;
         
         gbc.gridy = 1 ;
         /*
         createQuestion = new JButton("Add Question");
         createQuestion.addActionListener(new java.awt.event.ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 (new QuestionCreator(mainPane,gbc,subSection)).setVisible(true);
             }
             
         });
         
         * 
         add(createQuestion,gbc);
         * */
         JButton delete = new JButton("delete");
             delete.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (questionList.isSelectionEmpty() == false)
                 if (questionList.getSelectedIndex() !=0){
                 listModel.remove(questionList.getSelectedIndex());
                 
                 }
             }
         });
             gbc.gridx = 0;
         
         gbc.anchor = GridBagConstraints.PAGE_END ;      
         gbc.weightx = 1 ;
         gbc.weighty = 0.1 ;
         gbc.fill = GridBagConstraints.BOTH ;
             gbc.gridy++ ;
             add(delete, gbc);
        
         
         questionList.addListSelectionListener(new ListSelectionListener() {
             
             
             public void valueChanged(ListSelectionEvent evt) {
                if (evt.getValueIsAdjusting()){
                if (questionList.getSelectedIndex() !=0) {
                     wizard.repainRightPanel("SubSection information", new SubsectionEditor(subSection, subSection.getQuestion(questionList.getSelectedIndex() - 1),wizard,subsectionPanel));
                     System.out.print(questionList.getSelectedIndex());
                             
                }
                 else {
                    wizard.repainRightPanel("SubSection information", new SubsectionEditor(subSection,null,wizard,subsectionPanel));
                 System.out.print(questionList.getSelectedIndex());
                             
                }
 //                rightPanel.add(new SubsectionEditor(section.getSubSection(0)),gbc);
 //                
 //                rightPanel.revalidate();
 //                rightPanel.repaint();
                 
                 
                 
             }}
         
         
     });
         
         
         
         
     }
   
     
 }
