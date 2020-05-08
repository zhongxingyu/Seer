 package com.core.test;
 
 import com.core.lesson.*;
 import com.core.util.Utils;
 import com.core.util.ResourceManager;
 import org.json.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 
 public class FeedbackPage extends LessonPage {
 
     private JPanel contentPanel;
     private JSONObject feedback;
     private ButtonGroup[] groups;
     private JTextArea[] comments;
 
     public FeedbackPage(JSONObject json) {
         try {
         feedback = ResourceManager.getJSON(json.getString("data"));
         } catch (JSONException e) {
         }
         comments = new JTextArea[4];
         groups = new ButtonGroup[4];
         contentPanel = getContentPanel();
         contentPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
 
         setLayout(new java.awt.BorderLayout());
 
         JPanel secondaryPanel = new JPanel();
         secondaryPanel.add(contentPanel, BorderLayout.NORTH);
         add(secondaryPanel, BorderLayout.CENTER);
     }
 
     public HashMap getSubmit() {
         LinkedHashMap data = new LinkedHashMap();
         for(int i=0; i<groups.length; i++) {
             JRadioButton button = Utils.getSelection(groups[i]);
             if (button != null)
                 data.put("Question_"+(i+1), button.getText());
             data.put("Comment_"+(i+1), comments[i].getText());
            data.put("linebreak", "\n");
         }
         return data;
     }
 
     private boolean checkAnswer() {
         for(int i=0; i<groups.length; i++) {
             JRadioButton button = Utils.getSelection(groups[i]);
             if (button == null)
                 return false;
         }
         return true;
     }
 
 
     private JPanel getContentPanel() {
         JPanel contentPanel = new JPanel();
         //contentPanel.setLayout(new GridLayout());
         JPanel instructionPanel = createInstructionPanel();
         JPanel introPanel = createIntroPanel();
         contentPanel.setLayout(new BorderLayout());
 
         //contentPanel.add(introPanel, java.awt.BorderLayout.NORTH);
         contentPanel.add(instructionPanel,BorderLayout.NORTH);
         contentPanel.add(introPanel, BorderLayout.CENTER);
 
         return contentPanel;
 
     }
 
     private JPanel createInstructionPanel() {
     	JPanel instructionPanel = new JPanel();
         instructionPanel.setPreferredSize(new Dimension(400, 100));
         instructionPanel.setLayout(new java.awt.BorderLayout());
     	JEditorPane introArea = new JEditorPane();
         introArea.setContentType("text/html; charset=utf-8");
         //introArea.setText((String)textContent);
         try {
             introArea.setText(feedback.getString("instruction"));
         } catch (JSONException e) {
             System.out.println("error when trying to get json value");
         }
         introArea.setEditable(false);
         //Get JFrame background color  
         Color color = getBackground();
         introArea.setBackground(color);
         instructionPanel.add(introArea, BorderLayout.CENTER);
         return instructionPanel;
     }
 
     private JPanel createIntroPanel() {
         JPanel introPanel = new JPanel();
         //Group the radio buttons.
         introPanel.setLayout(new GridBagLayout());
         //introPanel.setBorder(new LineBorder(Color.BLACK, 1));
         introPanel.setPreferredSize(new Dimension(650,600));
         GridBagConstraints gBC = new GridBagConstraints();
         gBC.fill = GridBagConstraints.VERTICAL;
         gBC.weightx = 0.5;
         gBC.gridx = 0;
         gBC.gridy = 0;
         gBC.anchor = GridBagConstraints.CENTER;
         gBC.fill = GridBagConstraints.BOTH;
         gBC.insets=new Insets(2, 2, 2, 2);
         JSONArray questions = null;
         try {
             questions = feedback.getJSONArray("questions");
             for(int i=0; i<questions.length(); i++) {
                 JSONObject question = questions.getJSONObject(i);
                 JPanel feedbackPanel = new JPanel();
                 feedbackPanel.setPreferredSize(new Dimension(340, 10));
                 //feedbackPanel.setBorder(new LineBorder(Color.BLACK, 1));
                 //feedbackPanel.setLayout(new javax.swing.BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                 //feedbackPanel.setLayout(new BorderLayout());
                 gBC.gridy = i*2;
                 gBC.gridx = 0;
                 //gBC.gridheight = 100;
                 //gBC.ipady = 20;     //This component has more breadth compared to other buttons
                 gBC.weightx = 0.0;
                 gBC.gridheight = 2;
                 JLabel situationLabel = new JLabel();
                 situationLabel.setText(question.getString("title"));
                 situationLabel.setPreferredSize(new Dimension(100, 20));
                 introPanel.add(situationLabel, gBC);
                 JLabel subjectLabel = new JLabel();
                 subjectLabel.setText("<html>"+question.getString("description")+"</html>");
                 subjectLabel.setPreferredSize(new Dimension(100, 20));
                 gBC.gridx = 1;
                 introPanel.add(subjectLabel, gBC);
                 ButtonGroup group = new ButtonGroup();
                 JSONArray options = question.getJSONArray("options");
                 for(int j=0; j<options.length(); j++) {
                     JRadioButton radioButton = new JRadioButton(options.getString(j));
                     radioButton.setActionCommand(options.getString(j));
                     //radioButton.setSelected(true);
                     radioButton.addActionListener(new ActionListener(){
                         public void actionPerformed(ActionEvent e){
                             if (checkAnswer()) 
                                 sendMessage("READY");
                         }
                     }) ;
                     group.add(radioButton);
                     feedbackPanel.add(radioButton);
                 }
                 //feedbackPanel.add(group);
                 gBC.gridheight=1;
                 gBC.ipady = 20;
                 gBC.gridx = 2;
                 introPanel.add(feedbackPanel, gBC);
                 //comment.setPreferredSize(new Dimension(300, 40));
                 //JScrollPane scrollpane = new JScrollPane();
                 gBC.ipady = 60;
                 gBC.gridy = i*2+1;
                 //gBC.weightx = 1.0;
                 //gBC.weighty = 1.0;
 
                 //introPanel.add(comment, gBC);
                 JTextArea comment = new JTextArea();
 
                 comment.setBorder (new LineBorder(Color.black, 2));
                 comment.setPreferredSize(new Dimension(300, 20));
                 introPanel.add(comment, gBC);
                 //scrollpane.add(comment);
                 comments[i] = comment;
                 groups[i] = group;
             }
         } catch (JSONException e) {
         }
         //introPanel.setBorder (new LineBorder(Color.blue, 3));
         return introPanel;
     }
 
 }
