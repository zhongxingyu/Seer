 /** This file is part of Approach Avoidance Task.
  *
  * Approach Avoidance Task is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Approach Avoidance Task is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Approach Avoidance Task.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package views;
 
 import AAT.Util.SpringUtilities;
 import DataStructures.QuestionData;
 import DataStructures.Questionnaire;
 import Model.AATModel;
 
 import javax.swing.*;
 import javax.swing.text.Document;
 import javax.swing.text.html.HTMLEditorKit;
 import javax.swing.text.html.StyleSheet;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: marcel
  * Date: 10/29/11
  * Time: 1:15 PM
  * This is a panel that contains all the additional questions a researcher might be interested in.  These
  * questions come from the language file that is specified in the configuration. These questions are displayed for the
  * actual test is started. But only if there are any.
  */
 public class QuestionPanel extends JPanel {
 
 
     private JPanel questionsPanel;
     private Map<String, DisplayQuestion> questionsMap = new HashMap<String, DisplayQuestion>();
     private int maxLabelSize = 0;
     private JScrollPane scrollPane;
     private JTextArea introductionPane;
 
     public QuestionPanel(final AATModel model) {
         //  super(new BoxLayout(this, BoxLayout.Y_AXIS));
         JPanel mainPanel = new JPanel();
         JPanel contentPanel = new JPanel();
         contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
         contentPanel.setBackground(Color.black);
         contentPanel.setForeground(Color.white);
         mainPanel.setBackground(Color.black);
         mainPanel.setForeground(Color.white);
         mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER));    //TODO even naar kijken
         Dimension screen = getToolkit().getScreenSize();
         //  this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         this.setLayout(new GridBagLayout());
         this.setBackground(Color.black);
         this.setForeground(Color.white);
         this.setPreferredSize(new Dimension(screen.width / 2, screen.height));
         this.setMaximumSize(new Dimension(screen.width / 2, screen.height));
 
 
         questionsPanel = new JPanel(new SpringLayout());
         JPanel submitPanel = new JPanel();
         submitPanel.setBackground(Color.BLACK);
         submitPanel.setForeground(Color.WHITE);
         questionsPanel.setBackground(Color.black);
         questionsPanel.setForeground(Color.WHITE);
         JPanel questionnairePanel = new JPanel();
         questionnairePanel.setLayout(new BoxLayout(questionnairePanel, BoxLayout.Y_AXIS));
         questionnairePanel.setBackground(Color.black);
         questionnairePanel.setForeground(Color.white);
         introductionPane = new JTextArea();
         introductionPane.setBorder(BorderFactory.createLineBorder(Color.black, 10));
         //   introductionPane.setColumns(80);
         introductionPane.setLineWrap(true);
         introductionPane.setWrapStyleWord(true);
         introductionPane.setForeground(Color.white);
         introductionPane.setBackground(Color.black);
         introductionPane.setEditable(false);
 
         introductionPane.setFont(new Font("Roman", Font.PLAIN, 24));
         introductionPane.setText(model.getTest().getQuestionnaire().getIntroduction());
         //  introductionPane.setMaximumSize(new Dimension((int) (0.75 * screen.width), screen.height));
         //  introductionPane.setMinimumSize(new Dimension((int) (0.75* screen.width), 50));
         questionnairePanel.add(introductionPane);
         questionnairePanel.add(Box.createVerticalStrut(20)); //small margin
         questionnairePanel.add(questionsPanel);
         contentPanel.add(questionnairePanel);
 
         // mainPanel.add(scrollPane);
         //   questionsPanel.add(introductionPane);
 
         JButton submitButton = new JButton("Submit");
         JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         submitButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent actionEvent) {
                 if (checkAnswered()) {
                     model.addExtraQuestions(getResults());
                     setVisible(false);
                 } else {
                     repaint();
                 }
                 //   setDisabled();
 
             }
         });
         buttonPanel.setBackground(Color.black);
         buttonPanel.setForeground(Color.white);
         buttonPanel.add(submitButton);
         contentPanel.add(buttonPanel);
         //    mainPanel.add(contentPanel);
         scrollPane = new JScrollPane(contentPanel,
                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         //   scrollPane = new JScrollPane(contentPanel);
         scrollPane.setPreferredSize(new Dimension((int) (0.8 * screen.width), (int) (0.75 * screen.height)));
         scrollPane.setMinimumSize(new Dimension((int) (0.8 * screen.width), (int) (0.75 * screen.height)));
         scrollPane.setMaximumSize(new Dimension((int) (0.8 * screen.width), (int) (0.75 * screen.height)));
         scrollPane.getVerticalScrollBar().setUnitIncrement(16);
 
         scrollPane.setBorder(BorderFactory.createEmptyBorder());
         scrollPane.setBackground(Color.black);
         scrollPane.setForeground(Color.white);
         JScrollBar vertical = scrollPane.getVerticalScrollBar();
         InputMap im = vertical.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
         im.put(KeyStroke.getKeyStroke("DOWN"), "positiveUnitIncrement");
         im.put(KeyStroke.getKeyStroke("UP"), "negativeUnitIncrement");
         mainPanel.add(scrollPane);
         this.add(mainPanel, new GridBagConstraints());
         // repaint();
     }
 
 
     private boolean checkAnswered() {
         int count = 0;
         for (String key : questionsMap.keySet()) {
             DisplayQuestion q = questionsMap.get(key);
             if (q.getValue().equals("") && q.isRequired) {
                 q.changeAsteriskColor(Color.red);
                 count++;
             } else {
                 q.changeAsteriskColor(Color.white);  //Change red ones back to white when already answered.
             }
 
         }
         if (count > 0) {
             return false;
         }
         return true;
     }
 
     /**
      * Displays every question Object in the ArrayList<AAT.QuestionData> A question can be open, have a textField on the screen
      * or closed and have a combobox with answers
      *
      * @param questionnaire The optional questionnaire received from the model
      */
     public void displayQuestions(Questionnaire questionnaire) {
         for (QuestionData questionObject : questionnaire.getExtraQuestions()) {
 
             //   JLabel question = new JLabel(questionObject.getQuestion(), JLabel.TRAILING);
             Dimension screen = getToolkit().getScreenSize();
             JEditorPane question = new JEditorPane();
             question.setEditable(false);
             question.setMaximumSize(new Dimension(screen.width / 3, screen.height));
             question.setMinimumSize(new Dimension(screen.width / 3, 20));
             question.setPreferredSize(new Dimension(screen.width / 3, 20));
             question.setContentType("text/html");
 
             //   question.setPreferredSize(new Dimension(screen.width/3,screen.height));
             HTMLEditorKit kit = new HTMLEditorKit();
             question.setEditorKit(kit);
             StyleSheet styleSheet = kit.getStyleSheet();
             styleSheet.addRule("body {color: white; font-family:times; margin: 0px; background-color: black;font : 11px monaco;}");
             Document doc = kit.createDefaultDocument();
 
             question.setDocument(doc);
             question.setText("<body>" + questionObject.getQuestion() + "</body>");
             question.setBackground(Color.black);
             question.setForeground(Color.WHITE);
             //    question.setFont(new Font("Roman", Font.PLAIN, 16));
             questionsPanel.add(question);
             if (questionObject.getType().equals("closed_combo")) {
                 ClosedComboPanel answerOptions = new ClosedComboPanel(questionObject.getOptions().toArray(), questionObject.isRequired());
                 //   question.setLabelFor(answerOptions);
                 questionsPanel.add(answerOptions);
                 questionsMap.put(questionObject.getKey(), answerOptions);
             }
             if (questionObject.getType().equals("closed_buttons")) {
                 ClosedButtonsPanel closedButtons = new ClosedButtonsPanel(questionObject.getOptions().toArray(), questionObject.isRequired());
                 //   question.setLabelFor(closedButtons);
                 questionsPanel.add(closedButtons);
                 questionsMap.put(questionObject.getKey(), closedButtons);
             }
             if (questionObject.getType().equals("open")) {
                 OpenQuestionPanel textInput = new OpenQuestionPanel(questionObject.isRequired());
                 //    question.setLabelFor(textInput);
                 questionsPanel.add(textInput);
                 questionsMap.put(questionObject.getKey(), textInput);
             }
 
             if (questionObject.getType().equals("textarea")) {
                 TextAreaPanel textArea = new TextAreaPanel(questionObject.isRequired());
                 //    question.setLabelFor(textArea);
                 questionsPanel.add(textArea);
                 questionsMap.put(questionObject.getKey(), textArea);
             }
             if (questionObject.getType().equals("likert")) {
                 LikertPanel likertScale = new LikertPanel(questionObject.getSize(), questionObject.getLeftText(), questionObject.getRightText(), questionObject.isRequired());
                 //    question.setLabelFor(likertScale);
                 questionsPanel.add(likertScale);
                 questionsMap.put(questionObject.getKey(), likertScale);
             }
 
             if (questionObject.getType().equals("sem_diff")) {
                 SemDiffPanel semanticDifferential = new SemDiffPanel(questionObject.getSize(), questionObject.getLeftText(), questionObject.getRightText(), questionObject.isRequired());
                 //   question.setLabelFor(semanticDifferential);
                 questionsPanel.add(semanticDifferential);
                 questionsMap.put(questionObject.getKey(), semanticDifferential);
             }
             questionsPanel.setOpaque(true);
         }
         SpringUtilities.makeCompactGrid(questionsPanel,
                 questionnaire.getExtraQuestions().size(), 2, //rows, cols
                 6, 6,        //initX, initY
                 6, 6);       //xPad, yPad
         for (String key : questionsMap.keySet()) {
             DisplayQuestion q = questionsMap.get(key);
             q.changeLabelSize(maxLabelSize);
         }
 //        scrollPane.getVerticalScrollBar().setValue(0);
 
 //        scrollPane.getViewport().setViewPosition(new Point(0, 0));
         //  scrollPane.repaint();
         introductionPane.setSelectionStart(0);
         introductionPane.setSelectionEnd(0);
         //   jv.setViewPosition(new Point(0,0));
         //  this.repaint();
     }
 
     private void calculateLabelWidth(JLabel label) {
         Font labelFont = label.getFont();
         String labelText = label.getText();
 
         int stringWidth = label.getFontMetrics(labelFont).stringWidth(labelText);
         if (stringWidth > maxLabelSize) {
             maxLabelSize = stringWidth;
         }
     }
 
     /*
    All the components are kept in a hash map, This provides a flexibal way to have an unlimited nr of components
    and keep track of them
     */
     private HashMap<String, String> getResults() {
         HashMap<String, String> results = new HashMap<String, String>();
         for (String key : questionsMap.keySet()) {
             DisplayQuestion c = questionsMap.get(key);
             String input = c.getValue();
             if (input.equals("")) {
                 input = "N/A";  //Replace the empty input with "N/A"
             }
             results.put(key, input);
         }
         return results;
     }
 
 
     class SemDiffPanel extends DisplayQuestion {
 
         private ButtonGroup semDiffScale;
         private int size;
         private JLabel leftLabel;
 
         public SemDiffPanel(int size, String left, String right, boolean isRequired) {
             super();
             this.isRequired = isRequired;
             this.size = size;
             this.setLayout(new FlowLayout(FlowLayout.LEFT));
             this.setBackground(Color.black);
             this.setForeground(Color.white);
             JPanel buttonPanel = new JPanel();
             buttonPanel.setLayout(new GridLayout(2, size, 20, 0));
             buttonPanel.setForeground(Color.white);
             buttonPanel.setBackground(Color.black);
             leftLabel = new JLabel(left, JLabel.RIGHT);
             leftLabel.setForeground(Color.WHITE);
             leftLabel.setBackground(Color.black);
             //   leftLabel.setPreferredSize(new Dimension(150,30));
 
 
             this.add(leftLabel);
             calculateLabelWidth(leftLabel);
             this.add(Box.createHorizontalStrut(20));//20px margin
             semDiffScale = new ButtonGroup();
 
             for (int x = 0; x < size; x++) {
                 int step = size / 2;
                 JLabel label = new JLabel(String.valueOf(x - step), JLabel.CENTER);
                 label.setForeground(Color.white);
                 buttonPanel.add(label);
 
             }
 
             for (int x = 0; x < size; x++) {
                 JRadioButton likertButton = new JRadioButton();
                 likertButton.setBackground(Color.black);
                 likertButton.setForeground(Color.white);
                 buttonPanel.add(likertButton);
                 semDiffScale.add(likertButton);
             }
 
 
             this.add(buttonPanel);
             this.add(Box.createHorizontalStrut(20));//20px margin
             JLabel rightLabel = new JLabel(right, JLabel.LEFT);
             rightLabel.setForeground(Color.white);
             rightLabel.setBackground(Color.black);
             this.add(rightLabel);
             //  SpringUtilities.makeCompactGrid(questionsPanel,
             //        1, 3, //rows, cols
             //      6, 6,        //initX, initY
             //    6, 6);       //xPad, yPad
             if (isRequired) {
                 add(asterisks);
             }
         }
 
         public String getValue() {
             int x = 0;
             int step = size / 2;
             for (Enumeration e = semDiffScale.getElements(); e.hasMoreElements(); ) {
                 JRadioButton b = (JRadioButton) e.nextElement();
                 if (b.isSelected()) {
 
                     return String.valueOf(x - step);
                 }
                 x++;
             }
             return "";
         }
 
         @Override
         public String getType() {
             return "sem_diff";
         }
 
         public void changeLabelSize(int width) {
             leftLabel.setPreferredSize(new Dimension(width, 20));
             repaint();
         }
     }
 
     class TextAreaPanel extends DisplayQuestion {
 
         JTextArea textArea;
 
         public TextAreaPanel(boolean isRequired) {
             super();
             this.isRequired = isRequired;
             textArea = new JTextArea(10, 40);
             textArea.setLineWrap(true);
             textArea.setWrapStyleWord(true);
             JScrollPane scrollPane2 = new JScrollPane(textArea);
             scrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
             //  JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             this.setLayout(new FlowLayout(FlowLayout.LEFT));
             add(scrollPane2);
             setBackground(Color.black);
             setForeground(Color.white);
             textArea.setBackground(Color.WHITE);
             textArea.setForeground(Color.BLACK);
             if (isRequired) {
                 add(asterisks);
             }
         }
 
 
         @Override
         public String getValue() {
             return textArea.getText();
         }
 
         @Override
         public String getType() {
             return "textArea";
         }
 
         @Override
         public void changeLabelSize(int width) {
 
         }
 
 
     }
 
     class ClosedButtonsPanel extends DisplayQuestion {
 
         private ButtonGroup closedButtons;
 
         public ClosedButtonsPanel(Object[] options, boolean isRequired) {
             super();
             this.isRequired = isRequired;
             closedButtons = new ButtonGroup();
             JPanel answerPanel = new JPanel(new SpringLayout());
             answerPanel.setBackground(Color.black);
             answerPanel.setForeground(Color.white);
             setLayout(new FlowLayout(FlowLayout.LEFT));
             setBackground(Color.black);
             setForeground(Color.white);
             for (int x = 0; x < options.length; x++) {
                 JRadioButton button = new JRadioButton();
                 button.setBackground(Color.black);
                 button.setForeground(Color.white);
                 closedButtons.add(button);
                 JLabel answerOption = new JLabel(options[x].toString(), JLabel.LEFT);
                 answerOption.setBackground(Color.black);
                 answerOption.setForeground(Color.white);
                 answerOption.setLabelFor(button);
                 answerPanel.add(button);
                 answerPanel.add(answerOption);
             }
             this.add(answerPanel);
             SpringUtilities.makeCompactGrid(answerPanel,
                     options.length, 2, //rows, cols
                     6, 6,        //initX, initY
                     6, 6);       //xPad, yPad
             if (isRequired) {
                 add(asterisks);
             }
         }
 
         @Override
         public String getValue() {
             int x = 1;
             for (Enumeration e = closedButtons.getElements(); e.hasMoreElements(); ) {
                 JRadioButton b = (JRadioButton) e.nextElement();
                 if (b.isSelected()) {
                     return String.valueOf(x);
                 }
                 x++;
 
             }
             return "";
         }
 
         @Override
         public String getType() {
             return "closed_button";
         }
 
         @Override
         public void changeLabelSize(int width) {
 
         }
     }
 
     class OpenQuestionPanel extends DisplayQuestion {
 
         JTextField textInput;
 
         public OpenQuestionPanel(boolean isRequired) {
             super();
             this.isRequired = isRequired;
             textInput = new JTextField(10);
             setLayout(new FlowLayout(FlowLayout.LEFT));
             // JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             setBackground(Color.black);
             setForeground(Color.white);
             add(textInput);
             textInput.setBackground(Color.WHITE);
             textInput.setForeground(Color.BLACK);
             if (isRequired) {
                 add(asterisks);
             }
         }
 
         @Override
         public String getValue() {
             return textInput.getText();
         }
 
         @Override
         public String getType() {
             return "open";
         }
 
         @Override
         public void changeLabelSize(int width) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
 
     }
 
 
     class ClosedComboPanel extends DisplayQuestion {
 
         JComboBox answerOptions;
 
         public ClosedComboPanel(Object[] options, boolean isRequired) {
             super();
             this.isRequired = isRequired;
             Object[] newOptions = new Object[options.length + 1];
             newOptions[0] = "";
             for (int x = 0; x < options.length; x++) {
                 newOptions[x + 1] = options[x];
             }
             answerOptions = new JComboBox(newOptions);
             // JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             setLayout(new FlowLayout(FlowLayout.LEFT));
             add(answerOptions);
             setBackground(Color.BLACK);
             setForeground(Color.WHITE);
             answerOptions.setBackground(Color.WHITE);
             answerOptions.setForeground(Color.BLACK);
             if (isRequired) {
                 add(asterisks);
             }
         }
 
         @Override
         public String getValue() {
             for (int x = 1; x < answerOptions.getItemCount(); x++) {
                 Object option = answerOptions.getItemAt(x);
                 if (option.equals(answerOptions.getSelectedItem())) {
                     return String.valueOf(x);
                 }
             }
             return "";
         }
 
         @Override
         public String getType() {
             return "closed";
         }
 
         @Override
         public void changeLabelSize(int width) {
 
         }
 
     }
 
     class LikertPanel extends DisplayQuestion {
 
 
         private ButtonGroup likertScale;
         private JLabel leftLabel;
 
         public LikertPanel(int size, String left, String right, boolean isRequired) {
             super();
             this.isRequired = isRequired;
             setLayout(new FlowLayout(FlowLayout.LEFT));
             this.setBackground(Color.black);
             this.setForeground(Color.white);
 
             leftLabel = new JLabel(left, JLabel.RIGHT);
             leftLabel.setForeground(Color.WHITE);
             leftLabel.setBackground(Color.black);
             this.add(leftLabel);
             this.add(Box.createHorizontalStrut(20));//20px margin
             calculateLabelWidth(leftLabel);
             likertScale = new ButtonGroup();
             JPanel buttonPanel = new JPanel(new GridLayout(2, size, 20, 0));
             buttonPanel.setForeground(Color.white);
             buttonPanel.setBackground(Color.black);
             for (int x = 0; x < size; x++) {
                 JLabel label = new JLabel(String.valueOf(x + 1), JLabel.CENTER);
                 label.setForeground(Color.white);
                 buttonPanel.add(label);
             }
 
             for (int x = 0; x < size; x++) {
                 JRadioButton likertButton = new JRadioButton();
                 likertButton.setBackground(Color.black);
                 likertButton.setForeground(Color.white);
 
                 buttonPanel.add(likertButton);
                 likertScale.add(likertButton);
             }
             this.add(buttonPanel);
             this.add(Box.createHorizontalStrut(20));//20px margin
             JLabel rightLabel = new JLabel(right, JLabel.LEFT);
             //    rightLabel.setPreferredSize(new Dimension(200,30));
             rightLabel.setForeground(Color.white);
             rightLabel.setBackground(Color.black);
             this.add(rightLabel);
             //  SpringUtilities.makeCompactGrid(questionsPanel,
             //        1, 3, //rows, cols
             //      6, 6,        //initX, initY
             //    6, 6);       //xPad, yPad
             if (isRequired) {
                 add(asterisks);
             }
         }
 
         public void changeLabelSize(int width) {
             leftLabel.setPreferredSize(new Dimension(width, 20));
             repaint();
         }
 
         public String getValue() {
             int x = 1;
             for (Enumeration e = likertScale.getElements(); e.hasMoreElements(); ) {
                 JRadioButton b = (JRadioButton) e.nextElement();
                 if (b.isSelected()) {
 
                     return String.valueOf(x);
                 }
                 x++;
             }
             return "";
         }
 
         @Override
         public String getType() {
             return "likert";
         }
 
     }
 }
 
 abstract class DisplayQuestion extends JPanel {
 
     public JLabel asterisks;
     public boolean isRequired = true;
 
     public DisplayQuestion() {
         asterisks = new JLabel("*", JLabel.TRAILING);
         asterisks.setFont(new Font("Roman", Font.BOLD, 20));
         asterisks.setForeground(Color.WHITE);
     }
 
     public abstract String getValue();
 
     public abstract String getType();
 
     public abstract void changeLabelSize(int width);
 
     public void setRequired(Boolean isRequired) {
         this.isRequired = isRequired;
     }
 
     public void changeAsteriskColor(Color c) {
         asterisks.setForeground(c);
     }
 }
