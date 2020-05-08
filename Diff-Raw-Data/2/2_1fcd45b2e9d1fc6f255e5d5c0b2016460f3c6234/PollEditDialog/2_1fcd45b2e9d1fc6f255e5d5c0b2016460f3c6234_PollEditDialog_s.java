 package repoll.ui;
 
 import repoll.core.Answer;
 import repoll.core.Poll;
 import repoll.mappers.MapperException;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.util.logging.Logger;
 
 public class PollEditDialog extends JDialog {
     private static final Logger LOG = Logger.getLogger(PollEditDialog.class.getName());
     private JPanel contentPane;
     private JButton buttonOK;
     private JButton buttonCancel;
     private JTextField titleField;
     private JTextArea descriptionText;
     private JList<Answer> answersList;
     private JButton addAnswerButton;
     private JButton removeAnswerButton;
     private DefaultListModel<Answer> listModel = new DefaultListModel<>();;
     private Poll poll;
 
     public PollEditDialog(Poll existingPoll) {
         poll = existingPoll;
         titleField.setText(poll.getTitle());
         descriptionText.setText(poll.getDescription());
         try {
             for (Answer answer : poll.getAnswers()) {
                 listModel.addElement(answer);
             }
         } catch (MapperException e) {
             LOG.throwing("PollEditDialog", "PollEditDialog", e);
         }
         initComponents();
     }
 
     public PollEditDialog() {
         // stub poll instance for new answers
         poll = new Poll(MainApplication.getInstance().getCurrentUser(), "");
         initComponents();
     }
 
     private void initComponents() {
         setContentPane(contentPane);
         setModal(true);
         getRootPane().setDefaultButton(buttonOK);
         pack();
 
         buttonOK.setText(poll.isSaved()? "Update" : "Create");
 
         answersList.setModel(listModel);
         addAnswerButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String description = JOptionPane.showInputDialog(PollEditDialog.this, "Enter new answer description");
                 if (description != null && !description.isEmpty()) {
                     listModel.addElement(new Answer(poll, description));
                 }
             }
         });
         answersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         answersList.addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 Answer selectedAnswer = answersList.getSelectedValue();
                 if (selectedAnswer == null) {
                     return;
                 }
                 removeAnswerButton.setEnabled(!selectedAnswer.isSaved());
             }
         });
         answersList.setCellRenderer(new ListCellRenderer<Answer>() {
             @Override
             public Component getListCellRendererComponent(JList<? extends Answer> list, Answer value, int index, boolean isSelected, boolean cellHasFocus) {
                 return new DefaultListCellRenderer().getListCellRendererComponent(answersList, value.getDescription(), index, isSelected, cellHasFocus);
             }
         });
 
         removeAnswerButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 listModel.removeElement(answersList.getSelectedValue());
             }
         });
 
         buttonOK.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onOK();
             }
         });
 
         buttonCancel.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onCancel();
             }
         });
 
 // call onCancel() when cross is clicked
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 onCancel();
             }
         });
 
 // call onCancel() on ESCAPE
         contentPane.registerKeyboardAction(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onCancel();
             }
         }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
     }
 
     private void onOK() {
         if (validateFields()) {
             poll = updateOrUpdatePoll();
             if (poll != null) {
                 dispose();
             }
         }
     }
 
     private Poll updateOrUpdatePoll() {
         poll.setTitle(titleField.getText());
         poll.setDescription(descriptionText.getText());
         try {
             if (poll.isSaved()) {
                 poll.update();
             } else {
                 poll.insert();
             }
             for (int i = 0; i < listModel.size(); i++) {
                 Answer answer = listModel.elementAt(i);
                 if (!answer.isSaved()) {
                     answer.insert();
                 }
             }
             return poll;
         } catch (MapperException e) {
             JOptionPane.showMessageDialog(this, "Can't save poll: " + e.getMessage(),
                     "Poll creation error", JOptionPane.ERROR_MESSAGE);
             LOG.throwing("PollCreationDialog", "savePoll", e);
         }
         return null;
     }
 
     private void onCancel() {
 // add your code here if necessary
         dispose();
     }
 
     private boolean validateFields() {
         if (ValidationUtil.isEmptyOrWhitespace(titleField.getText())) {
             JOptionPane.showMessageDialog(this, "Title field can't be empty", "Invalid title", JOptionPane.ERROR_MESSAGE);
             return false;
         }
         if (listModel.isEmpty()) {
             JOptionPane.showMessageDialog(this, "No answers set", "No answers set", JOptionPane.ERROR_MESSAGE);
             return false;
         }
         Set<String> answers = new HashSet<>();
         for (int i = 0; i < listModel.size(); i++) {
             answers.add(listModel.get(i).getDescription());
         }
        if (new HashSet<>(answers).size() != answers.size()) {
             JOptionPane.showMessageDialog(this, "Answers contain duplicate values", "Invalid answers", JOptionPane.ERROR_MESSAGE);
             return false;
         }
         return true;
     }
 
     public Poll getPoll() {
         return poll.isSaved()? poll : null;
     }
 }
