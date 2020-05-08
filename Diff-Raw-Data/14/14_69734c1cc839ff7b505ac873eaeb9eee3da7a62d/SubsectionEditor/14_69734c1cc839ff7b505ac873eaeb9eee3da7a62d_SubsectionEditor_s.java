 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package Viewer;
 
 import Model.questionPaper.EssayQuestion;
 import Model.questionPaper.MBQuestion;
 import Model.questionPaper.MultipleChoiceQuestion;
 import Model.questionPaper.Question;
 import javax.swing.JPanel;
 import Viewer.FIBQPanel;
 import Model.questionPaper.SubSection;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 /**
 *
 * @author Daniel
 */
 public class SubsectionEditor extends JPanel{
     private Popup pop=new Popup();
     private JLabel title_label = new JLabel("title:");
     private  JTextArea titleArea = new JTextArea(1,30);
     private JLabel description_label = new JLabel("description:");
     private  JTextArea description = new JTextArea(3,30);
     private JLabel instruction_label = new JLabel("instruction:");
     private  JTextArea instruction = new JTextArea(3,30);
     private JLabel markArea;
     static JTextArea answer = new JTextArea(1,20);
     private JTabbedPane questionType;
     private JPanel MCQ;
     static JButton submit;
     SubSection subSection;
     Question question;
     int j=0;
     private JPanel answerPanel;
     GridBagConstraints apc = new GridBagConstraints();
     private JPanel FIBQ;
     private JPanel MBQ;
     private int mb_part_num = 0;
     private JPanel Essay;
     
     JPanel questionCreator = new JPanel() ;
      ArrayList<JTextArea> answerAreas;
      private TestWizard wizard;
      int num_answers;
      Subsection subsectionPanel;
      MultipleChoiceQuestion mcquestion = new MultipleChoiceQuestion();
      EssayQuestion essayQuestion = new EssayQuestion();
      MBQuestion mbquestion = new MBQuestion();
      
     
     public SubsectionEditor(SubSection subSection, Question Q, TestWizard wizard,Subsection subsectionPanel)
     {
         titleArea.setText(subSection.getTitle());
         description.setText(subSection.getDescription());
         instruction.setText(subSection.getInstructions());
         answerAreas = new ArrayList();
         this.wizard = wizard;
         this.subSection = subSection;
         this.subsectionPanel = subsectionPanel;
         if(Q != null){
             question = Q;
             if (question instanceof MultipleChoiceQuestion) {
                 mcquestion = (MultipleChoiceQuestion)question;
             } else if(question instanceof EssayQuestion){
                 essayQuestion = (EssayQuestion)question;
             } else if(question instanceof MBQuestion){
                 mbquestion = (MBQuestion)question;
             }
         }
         initComponents();
     }
     
     private void initComponents() {
         
        
         setLayout(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.anchor = GridBagConstraints.NORTH;
         gbc.weightx = 0.3;
         gbc.weighty = 0.3;
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.insets = new Insets(10,5,5,10);
         add(title_label,gbc);
         
         gbc.weightx = 0.7;
         gbc.weighty=1.0;
         gbc.gridx = 1;
         add(titleArea,gbc);
         
         titleArea.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 subSection.setTitle(titleArea.getText());
                 ((JTabbedPane)subsectionPanel.getParent()).setTitleAt(((JTabbedPane)subsectionPanel.getParent()).getSelectedIndex(),titleArea.getText());
             }
 
             @Override
             public void removeUpdate(DocumentEvent e) {
                 subSection.setTitle(titleArea.getText());
                 ((JTabbedPane)subsectionPanel.getParent()).setTitleAt(((JTabbedPane)subsectionPanel.getParent()).getSelectedIndex(),titleArea.getText());
             }
             @Override
             public void changedUpdate(DocumentEvent e) {
                 subSection.setTitle(titleArea.getText());
                 ((JTabbedPane)subsectionPanel.getParent()).setTitleAt(((JTabbedPane)subsectionPanel.getParent()).getSelectedIndex(),titleArea.getText());
             }
     });
         
         gbc.weightx = 0.3;
         gbc.weighty=1.0;
         gbc.gridx = 0;
         gbc.gridy = 1;
         add(description_label,gbc);
         
         gbc.weightx = 0.7;
         gbc.gridx = 1;
         add(description,gbc);
         
         description.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 subSection.setDescription(description.getText());
             }
 
             @Override
             public void removeUpdate(DocumentEvent e) {
                 subSection.setDescription(description.getText());
             }
             @Override
             public void changedUpdate(DocumentEvent e) {
                 subSection.setDescription(description.getText());
             }
     });
         
         gbc.weightx = 0.3;
         gbc.gridx = 0;
         gbc.gridy = 2;
         add(instruction_label,gbc);
         
         gbc.weightx = 0.7;
         gbc.gridx = 1;
         add(instruction,gbc);
         
         instruction.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 subSection.setInstructions(instruction.getText());
             }
 
             @Override
             public void removeUpdate(DocumentEvent e) {
                 subSection.setInstructions(instruction.getText());
             }
             @Override
             public void changedUpdate(DocumentEvent e) {
                 subSection.setInstructions(instruction.getText());
             }
     });
         
         questionCreator = renderQuestionCreator();
 
         gbc.weighty = 1.0;
         gbc.gridx = 0;
         gbc.weightx = 1.0;
         gbc.gridwidth = GridBagConstraints.REMAINDER;
         gbc.gridy = 3;
         add(questionCreator,gbc);
     }
     
         public JPanel addAnswer(final int num){
             JPanel tempPanel = new JPanel();
             JLabel answer_label = new JLabel("answer"+num+": ");
             tempPanel.setLayout(new GridBagLayout());
             GridBagConstraints gbc = new GridBagConstraints();
             gbc.gridx = 0;
             gbc.weightx = 0.3;
             tempPanel.add(answer_label,gbc);
             
             final JTextArea answer_new = new JTextArea(1,20);
             if(mcquestion.getAnswers() == null){
                 mcquestion.setAnswers(new String[1]);
                 mcquestion.setPossibleAnswers(new int[1]);
             }else{
                 String[] new_answers = new String[mcquestion.getNumberOfAnswers()+1];
                 System.arraycopy(mcquestion.getAnswers(),0,new_answers,0,mcquestion.getNumberOfAnswers());
                 mcquestion.setAnswers(new_answers);
                 int[] new_possibles = new int[mcquestion.getNumberOfPossibleAnswers()+1];
                 System.arraycopy(mcquestion.getPossibleAnswers(),0,new_possibles,0,mcquestion.getNumberOfPossibleAnswers());
                 mcquestion.setPossibleAnswers(new_possibles);
             }
                 
             answer_new.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 mcquestion.getAnswers()[num-1] = answer_new.getText();
             }
 
             @Override
             public void removeUpdate(DocumentEvent e) {
                 mcquestion.getAnswers()[num-1] = answer_new.getText();
             }
             @Override
             public void changedUpdate(DocumentEvent e) {
                 mcquestion.getAnswers()[num-1] = answer_new.getText();
             }
     });
             answerAreas.add(answer_new);
             gbc.gridx = 1;
             gbc.weightx = 0.7;
             tempPanel.add(answer_new,gbc);
             JCheckBox isRight = new JCheckBox("right answer");
             isRight.addItemListener(new ItemListener() {
 
                 @Override
                 public void itemStateChanged(ItemEvent e) {
                     mcquestion.getPossibleAnswers()[num-1] = e.getStateChange()==ItemEvent.SELECTED?1:0;
                 }
             });
             gbc.weightx = 0;
             gbc.gridx = 2;
             tempPanel.add(isRight,gbc);
             return tempPanel;
             
             
         }
         
         private JPanel renderQuestionCreator(){
             JPanel tempCreator = new JPanel();
             tempCreator.setLayout(new GridBagLayout());
             if(question != null)
                 tempCreator.setBorder(new TitledBorder("Edit Question"));
             else
                 tempCreator.setBorder(new TitledBorder("Create New Question"));
             final GridBagConstraints c3 = new GridBagConstraints();
 
             questionType = new JTabbedPane();
             MCQ = new JPanel();
             
             questionType.addTab("MCQ",MCQ);
 
             FIBQ = new FIBQPanel(subSection, question, subsectionPanel);
             questionType.addTab("FIBQ",FIBQ);
             
             MBQ = new JPanel();
             questionType.addTab("MBQ",MBQ);
             
             Essay = new JPanel();
             questionType.addTab("Essay",Essay);
             
             MCQ.setLayout(new GridBagLayout());
             c3.fill = GridBagConstraints.BOTH;
             c3.insets = new Insets(10,5,5,10);
             c3.weightx = 0.3;
             c3.gridx = 0;
             c3.gridy = 0;
             JLabel title_label2 = new JLabel("title: ");
             MCQ.add(title_label2,c3);
 
             c3.gridx = 1;
             c3.weightx = 0.7;
             titleArea = new JTextArea(1,20);
             
             titleArea.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 mcquestion.setQuestion(titleArea.getText());
             }
 
             @Override
             public void removeUpdate(DocumentEvent e) {
                 mcquestion.setQuestion(titleArea.getText());
             }
             @Override
             public void changedUpdate(DocumentEvent e) {
                  mcquestion.setQuestion(titleArea.getText());
             }
     });
                 
             MCQ.add(titleArea,c3);
 
             JPanel markPane = new JPanel() ;
             markPane.setLayout(new GridBagLayout());
             GridBagConstraints c4 = new GridBagConstraints();
             JLabel mark_label = new JLabel("marks: ");
             c4.gridy = 0;
             c4.gridx = 0;
             c4.weightx = 0;
             markPane.add(mark_label,c4);
             
            
             c4.gridx = 1;
             c4.weightx = 0.3;
             markArea = new JLabel();
             markPane.add(markArea,c4);
             
             c4.gridx++;
              JButton moreTime = new JButton("+");
             moreTime.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 markArea.setText(String.valueOf(Integer.parseInt(markArea.getText())+1));
                 mcquestion.setMark(Integer.parseInt(markArea.getText())+1);
             }
         });
             JButton lessTime = new JButton("-");
       lessTime.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(Integer.parseInt(markArea.getText())>0){
                 markArea.setText(String.valueOf(Integer.parseInt(markArea.getText())-1));
                 mcquestion.setMark(Integer.parseInt(markArea.getText())+1);
                 }
             }
         });
             markPane.add(moreTime,c4);
       c4.gridx++;
       markPane.add(lessTime,c4);
       c3.gridx = 0 ;
       c3.gridy = 1 ;
       c3.weightx = 1 ;
       c3.weighty = 1 ;
       c3.gridwidth = GridBagConstraints.RELATIVE ;
      c3.fill = GridBagConstraints.BOTH;
       MCQ.add(markPane, c3) ;
             
             JButton addAnswer = new JButton("add answer");
             c3.gridx = 2;
             c3.weightx = 0.2;
             c3.weighty = 0;
             c3.fill = GridBagConstraints.NONE;
             MCQ.add(addAnswer,c3);
 
             answerPanel = new JPanel();
             c3.gridx = 0;
             c3.gridwidth = GridBagConstraints.REMAINDER;
             c3.gridy = 2;
             c3.weighty = 1.0;
             c3.weightx = 1.0;
             MCQ.add(answerPanel,c3);
 
             JButton submit = new JButton("submit");
             
             
             
             c3.gridx = 0;
             c3.gridy = 3;
             c3.weighty = 0.2;
             c3.ipady = 20 ;
             MCQ.add(submit,c3);
 
             answerPanel.setLayout(new GridBagLayout());
             apc.gridx = 0;
             apc.gridy = 0;
             
             if(mcquestion.getQuestion() != null){
                 questionType.setSelectedIndex(0);
                 titleArea.setText(mcquestion.getQuestion());
                 markArea.setText(String.valueOf(mcquestion.getMark()));
                 for (int i = 0 ; i < mcquestion.getNumberOfAnswers(); i++)
                 {
                 JPanel tempPanel = new JPanel();
                 JLabel answer_label = new JLabel("answer"+(i+1)+": ");
                 tempPanel.setLayout(new GridBagLayout());
                 GridBagConstraints gbc1 = new GridBagConstraints();
                 gbc1.gridx = 0;
                 gbc1.weightx = 0.3;
                 tempPanel.add(answer_label,gbc1);
                 answer = new JTextArea(1,20);
                 answer.setText(mcquestion.getAnswer(i));
                 answer.getDocument().addDocumentListener(new DocumentListener() {
                 @Override
                 public void insertUpdate(DocumentEvent e) {
                     for(int j = 0;j<answerAreas.size();++j)
                     (mcquestion.getAnswers())[j] = answerAreas.get(j).getText();
                 }
 
                 @Override
                 public void removeUpdate(DocumentEvent e) {
                     for(int j = 0;j<answerAreas.size();++j)
                     (mcquestion.getAnswers())[j] = answerAreas.get(j).getText();            
                 }
                 @Override
                 public void changedUpdate(DocumentEvent e) {
                     for(int j = 0;j<answerAreas.size();++j)
                     (mcquestion.getAnswers())[j] = answerAreas.get(j).getText();            
                 }
                 });
                 answerAreas.add(answer);
                 gbc1.gridx = 1;
                 gbc1.weightx = 0.7;
                 tempPanel.add(answer,gbc1);
                 JCheckBox isRight = new JCheckBox("right answer");
 
                 if (mcquestion.getPossibleAnswer(i) == 1) {
 
                     isRight.setSelected(true);
 
                 }
                 gbc1.gridx = 2;
                 tempPanel.add(isRight,gbc1.gridy);
                 apc.weightx = 1.0;
                 answerPanel.add(tempPanel,apc);
                 apc.gridy++;
                 }
                 answerPanel.revalidate();
                 answerPanel.repaint();
 
                 submit.addActionListener(new ActionListener (){
                 @Override
                 public void actionPerformed(ActionEvent e){
                 mcquestion.setMark(Integer.parseInt(markArea.getText()));
                 subSection.addQuestion(mcquestion);
                 //revalidate();
                 }
                 });
                 }
                 else{
                 markArea.setText("0");
                 submit.addActionListener(new ActionListener (){
                 @Override
                 public void actionPerformed(ActionEvent e){
                     if (mcquestion.getNumberOfAnswers()>1&&mcquestion.getRightAnswers()>0&&mcquestion.getQuestion()!=null){
                         mcquestion.setMark(Integer.parseInt(markArea.getText()));
                         subSection.addQuestion(mcquestion);
                         //revalidate();
                         subsectionPanel.listModel.addElement(mcquestion.getQuestion());
                        }
                     else if(mcquestion.getNumberOfAnswers()<=1){pop.setText("You should create two or more answers!");pop.show();}
                     else if("".equals(mcquestion.getQuestion())){pop.setText("Question Title should not be blank!");pop.show();}
                     else{pop.setText("Question should have at least one right answer!");pop.show();}
                }
                 });
             }
             addAnswer.addActionListener(new java.awt.event.ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     apc.gridy++;
                     apc.weightx = 1.0;
                     answerPanel.add(addAnswer(++num_answers),apc);
                     answerPanel.revalidate();
                     answerPanel.repaint();
                 }
 
             });
             Essay.setLayout(new GridBagLayout());
             GridBagConstraints gbc_essay = new GridBagConstraints();
             gbc_essay.insets = new Insets(5, 5, 5, 5);
             gbc_essay.anchor = GridBagConstraints.NORTH;
             JLabel essay_question_label = new JLabel("Question: ");
             gbc_essay.gridx = 0;
             gbc_essay.gridy = 0;
             Essay.add(essay_question_label,gbc_essay);
             
             final JTextArea essay_question = new JTextArea(1,30);
             gbc_essay.gridx = 1;
             Essay.add(essay_question,gbc_essay);
             
             JLabel essay_instructions_label = new JLabel("Instructions: ");
             gbc_essay.gridx = 0;
             gbc_essay.gridy = 1;
             Essay.add(essay_instructions_label,gbc_essay);
             
             final JTextArea essay_instructions = new JTextArea(5,30);
             gbc_essay.gridx = 1;
             Essay.add(essay_instructions,gbc_essay);
             
             JLabel essay_wordlimit_label = new JLabel("Word Limit: ");
             gbc_essay.gridx = 0;
             gbc_essay.gridy = 2;
             Essay.add(essay_wordlimit_label,gbc_essay);
             
             final JTextArea essay_wordlimit = new JTextArea(1,5);
             gbc_essay.gridx = 1;
             Essay.add(essay_wordlimit,gbc_essay);
             
             JLabel essay_marks_label = new JLabel("Marks: ");
             gbc_essay.gridx = 0;
             gbc_essay.gridy = 3;
             Essay.add(essay_marks_label,gbc_essay);
             
             final JPanel essay_marks_pane = new JPanel();
             essay_marks_pane.setLayout(new GridBagLayout());
             GridBagConstraints c6 = new GridBagConstraints();
             c6.insets = new Insets(5,5,5,5);
             c6.gridy = 0;
             c6.gridx = 0;
             c6.weightx = 0.3;
             final JLabel essay_markArea = new JLabel();
             essay_marks_pane.add(essay_markArea,c6);
             essay_markArea.setText("0");
              JButton essay_moreMark = new JButton("+");
             essay_moreMark.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 essay_markArea.setText(String.valueOf(Integer.parseInt(essay_markArea.getText())+1));
                 essayQuestion.setMark(Integer.parseInt(essay_markArea.getText())+1);
             }
         });
             JButton essay_lessMark = new JButton("-");
       essay_lessMark.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(Integer.parseInt(essay_markArea.getText())>0){
                 essay_markArea.setText(String.valueOf(Integer.parseInt(essay_markArea.getText())-1));
                 essayQuestion.setMark(Integer.parseInt(essay_markArea.getText())+1);
                 }
             }
         });
       c6.gridx++;
       essay_marks_pane.add(essay_moreMark,c6);
       c6.gridx++;
       essay_marks_pane.add(essay_lessMark,c6);
       
 //            final JLabel essay_marks_ = new JLabel();
             gbc_essay.gridx = 1;
             Essay.add(essay_marks_pane,gbc_essay);
 //            essay_marks.setText("0");
             
             gbc_essay.gridx = 0;
             gbc_essay.gridy = 4;
             gbc_essay.gridwidth = 2;
             gbc_essay.weighty = 1.0;
             JButton submitEssay = new JButton("submit");
             Essay.add(submitEssay,gbc_essay);
             
             if(essayQuestion.getQuestion() != null){
                 questionType.setSelectedIndex(3);
                 essay_question.setText(essayQuestion.getQuestion());
                 essay_instructions.setText(essayQuestion.getInstructions());
                 essay_wordlimit.setText(String.valueOf(essayQuestion.getWordLimit()));
                 essay_markArea.setText(String.valueOf(essayQuestion.getMark()));
                
             } 
             submitEssay.addActionListener(new ActionListener (){
                 @Override
                 public void actionPerformed(ActionEvent e){
                 essayQuestion.setQuestion(essay_question.getText());
                 essayQuestion.setInstructions(essay_instructions.getText());
                 essayQuestion.setMark(Integer.parseInt(essay_markArea.getText()));
                 essayQuestion.setWordLimit(Integer.valueOf(essay_wordlimit.getText()));
                 subSection.addQuestion(essayQuestion);
                 subsectionPanel.listModel.addElement(essayQuestion.getQuestion());
                 //revalidate();
                 }
                 });
             
             MBQ.setLayout(new GridBagLayout());
             GridBagConstraints gbc_mb = new GridBagConstraints();
             gbc_mb.insets = new Insets(5, 5, 5, 5);
             gbc_mb.gridx = 0;
             gbc_mb.gridy = 0;
             JLabel mb_question_label = new JLabel("Question");
             MBQ.add(mb_question_label,gbc_mb);
             final JTextArea mb_question = new JTextArea(1,30);
             gbc_mb.gridx = 1;
             MBQ.add(mb_question,gbc_mb);
             gbc_mb.gridx = 0;
             gbc_mb.gridy = 1;
             JLabel mb_instructions_label = new JLabel("Instructions");
             MBQ.add(mb_instructions_label,gbc_mb);
             gbc_mb.gridx = 1;
             final JTextArea mb_instructions = new JTextArea(5,30);
             MBQ.add(mb_instructions,gbc_mb);
             gbc_mb.gridx = 0;
             gbc_mb.gridy = 2;
             gbc_mb.gridwidth = GridBagConstraints.REMAINDER;
             JPanel mb_markPane = new JPanel() ;
             mb_markPane.setLayout(new GridBagLayout());
             GridBagConstraints c5 = new GridBagConstraints();
             c5.insets = new Insets(5,5,5,5);
             JLabel mb_mark_label = new JLabel("marks: ");
             c5.gridy = 0;
             c5.gridx = 0;
             c5.weightx = 0;
             mb_markPane.add(mb_mark_label,c5);
             
            
             c5.gridx = 1;
             c5.weightx = 0.3;
             final JLabel mb_markArea = new JLabel();
             mb_markPane.add(mb_markArea,c5);
             mb_markArea.setText("0");
              JButton mb_moreMark = new JButton("+");
             mb_moreMark.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 mb_markArea.setText(String.valueOf(Integer.parseInt(mb_markArea.getText())+1));
                 mbquestion.setMark(Integer.parseInt(mb_markArea.getText())+1);
             }
         });
             JButton mb_lessMark = new JButton("-");
       mb_lessMark.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(Integer.parseInt(mb_markArea.getText())>0){
                 mb_markArea.setText(String.valueOf(Integer.parseInt(mb_markArea.getText())-1));
                 mbquestion.setMark(Integer.parseInt(mb_markArea.getText())+1);
                 }
             }
         });
       c5.gridx++;
       mb_markPane.add(mb_moreMark,c5);
       c5.gridx++;
       mb_markPane.add(mb_lessMark,c5);
       
       c5.gridx ++;
       JButton add_part = new JButton("Add Part");
       mb_markPane.add(add_part,c5);
       
       MBQ.add(mb_markPane, gbc_mb) ;
             
             
             gbc_mb.gridx = 0;
             gbc_mb.gridy = 3;
             gbc_mb.gridwidth = GridBagConstraints.REMAINDER;
             gbc_mb.weighty = 1.0;
             final JPanel mb_question_parts = new JPanel();
             mb_question_parts.setPreferredSize(new Dimension(400,150));
             mb_question_parts.setBorder(new TitledBorder("MB Question"));
             MBQ.add(mb_question_parts,gbc_mb);
             
             
             if(mbquestion.getQuestionParts()==null){
                 mbquestion.setQuestionParts(new ArrayList<String>());
             }
       add_part.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     JLabel mb_blank = new JLabel("_____");
                     final JTextArea mb_newpart = new JTextArea(1,10);
                     mb_question_parts.add(mb_blank);
                     mb_question_parts.add(mb_newpart);
                     mb_question_parts.revalidate();
                     mbquestion.getQuestionParts().add("");
                     final int num_of_parts = mbquestion.getQuestionParts().size()-1;
                     mb_newpart.getDocument().addDocumentListener(new DocumentListener() {
 
                         @Override
                         public void insertUpdate(DocumentEvent e) {
                             mbquestion.getQuestionParts().set(num_of_parts,mb_newpart.getText());
                         }
 
                         @Override
                         public void removeUpdate(DocumentEvent e) {
                             mbquestion.getQuestionParts().set(num_of_parts,mb_newpart.getText());
                         }
 
                         @Override
                         public void changedUpdate(DocumentEvent e) {
                             mbquestion.getQuestionParts().set(num_of_parts,mb_newpart.getText());
                         }
                     });
                 }
             });
             
       JButton mb_submit = new JButton("Submit");
       gbc_mb.gridx = 0;
       gbc_mb.gridy++;
       gbc_mb.gridwidth = 2;
       gbc_mb.weighty = 0;
       MBQ.add(mb_submit,gbc_mb);
       
       if(question!=null && question instanceof MBQuestion){
           questionType.setSelectedIndex(2);
           mb_instructions.setText(mbquestion.getInstructions());
           mb_question.setText(mbquestion.getQuestion());
           mb_markArea.setText(String.valueOf(mbquestion.getMark()));
           final JTextArea mb_first_part = new JTextArea(mbquestion.getQuestionParts().get(0));
           mb_first_part.getDocument().addDocumentListener(new DocumentListener() {
 
                         @Override
                         public void insertUpdate(DocumentEvent e) {
                             mbquestion.getQuestionParts().set(0,mb_first_part.getText());
                         }
 
                         @Override
                         public void removeUpdate(DocumentEvent e) {
                             mbquestion.getQuestionParts().set(0,mb_first_part.getText());
                         }
 
                         @Override
                         public void changedUpdate(DocumentEvent e) {
                             mbquestion.getQuestionParts().set(0,mb_first_part.getText());
                         }
                     }); 
           mb_question_parts.add(mb_first_part);
           for(int i=1;i<mbquestion.getQuestionParts().size();i++){
               JLabel mb_blank = new JLabel("_____");
                     final JTextArea mb_newpart = new JTextArea(mbquestion.getQuestionParts().get(i));
                     mb_question_parts.add(mb_blank);
                     mb_question_parts.add(mb_newpart);
                     final int position = i;
                      mb_newpart.getDocument().addDocumentListener(new DocumentListener() {
 
                         @Override
                         public void insertUpdate(DocumentEvent e) {
                             mbquestion.getQuestionParts().set(position,mb_newpart.getText());
                         }
 
                         @Override
                         public void removeUpdate(DocumentEvent e) {
                             mbquestion.getQuestionParts().set(position,mb_newpart.getText());
                         }
 
                         @Override
                         public void changedUpdate(DocumentEvent e) {
                             mbquestion.getQuestionParts().set(position,mb_newpart.getText());
                         }
                     }); 
           }
                     mb_question_parts.revalidate();
            mb_submit.addActionListener(new ActionListener() {
 
               @Override
               public void actionPerformed(ActionEvent e) {
                   mbquestion.setMark(Integer.valueOf(mb_markArea.getText()));
                   mbquestion.setInstructions(mb_instructions.getText());
                   subsectionPanel.listModel.setElementAt( mbquestion.getQuestion(),subsectionPanel.questionList.getSelectedIndex());
               }
           });
           
       }else{
           final JTextArea mb_part1 = new JTextArea(1,10);
             mb_question_parts.add(mb_part1);
             mbquestion.setQuestionParts(new ArrayList<String>());
             mbquestion.getQuestionParts().add("");
             mb_part1.getDocument().addDocumentListener(new DocumentListener() {
 
                         @Override
                         public void insertUpdate(DocumentEvent e) {
                             mbquestion.getQuestionParts().set(0,mb_part1.getText());
                         }
 
                         @Override
                         public void removeUpdate(DocumentEvent e) {
                             mbquestion.getQuestionParts().set(0,mb_part1.getText());
                         }
 
                         @Override
                         public void changedUpdate(DocumentEvent e) {
                             mbquestion.getQuestionParts().set(0,mb_part1.getText());
                         }
                     }); 
             mb_submit.addActionListener(new ActionListener() {
 
               @Override
               public void actionPerformed(ActionEvent e) {
                   mbquestion.setMark(Integer.valueOf(mb_markArea.getText()));
                   mbquestion.setInstructions(mb_instructions.getText());
                   subSection.addQuestion(mbquestion);
                   subsectionPanel.listModel.addElement(mbquestion.getQuestion());
               }
           });
       }
      
       questionType.setPreferredSize(new Dimension(500,400));
             tempCreator.add(questionType);
             return tempCreator;
         }
         
         public static void addListeners(FocusListener foc){
     //might be useful in the future
         }
 }
