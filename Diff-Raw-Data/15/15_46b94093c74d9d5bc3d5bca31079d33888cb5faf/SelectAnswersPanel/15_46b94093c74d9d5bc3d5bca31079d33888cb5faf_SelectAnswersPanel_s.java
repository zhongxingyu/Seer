 package lhs.qmaker.select;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 import javax.swing.JButton;
 import javax.swing.border.Border;
 import javax.swing.border.LineBorder;
 
 import lhs.qmaker.AnswersPanel;
 import lhs.qmaker.Controller;
 import lhs.qmaker.WrapButton;
 
 public class SelectAnswersPanel extends AnswersPanel {
 
 	ArrayList<JButton> buttons = new ArrayList<JButton>();
     Border defaultBorder = new JButton().getBorder();
     boolean answers[];
     HashMap<JButton,Boolean> mapping = new HashMap<JButton,Boolean>();
     /**
      * Create the panel.
      */
     public SelectAnswersPanel() {
         int size = 0;
         ArrayList<String> choices = Controller.choices.getChoices();
         
         answers = new boolean[choices.size()];
         for (int i = 0; i < choices.size(); i++) {
             JButton b = new WrapButton(choices.get(i));
             buttons.add(b);
             mapping.put(b, false);
             b.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent arg0) {
                 	JButton b = (JButton) arg0.getSource();
                 	mapping.put(b,!mapping.remove(b));
                 	boolean selected = mapping.get(b);
                 	if (selected)
                 		b.setBorder(new LineBorder(new Color(0,146,204),5,true));
                 	else
                         b.setBorder(defaultBorder);
                 }
             });
             size += b.getPreferredSize().height;
             super.answers.add(b);
         }
         super.answers.setPreferredSize(new Dimension(super.WIDTH, size));
     }
     
 	@Override
 	public ArrayList<String> getAnswers() {
 		ArrayList<String> answers = new ArrayList<String>();
 		Set<JButton> keys = mapping.keySet();
 		for (JButton b : keys) {
 			if (mapping.get(b)) {
				answers.add(b.getText());
 			}
 		}
 		return answers;
 	}
 
 	@Override
 	public void toChoices() {
 		Controller.setPane(Controller.choices);
 	}
 
 	@Override
 	public void toComments() {
 		Controller.comments = new SelectCommentsPanel();
 		Controller.setPane(Controller.comments);
 	}
 
 }
