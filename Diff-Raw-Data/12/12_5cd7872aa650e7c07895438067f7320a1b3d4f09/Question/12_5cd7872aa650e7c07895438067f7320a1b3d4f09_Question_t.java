 package GUIProject;
 
 import javax.swing.JLabel;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.ButtonGroup;
 /**
  * 
  * @author Stephen Wen
  *
  */
 public class Question implements ActionListener{ 
 	public Answer[] answers = new Answer[3];
 	public JButton confirm = new JButton("Confirm");
 	public ButtonGroup group = new ButtonGroup();
 	public JLabel label;
 	public String question;
 	/**
 	 * 
 	 */
 	public Question(){
 		super();
 		label = new JLabel("Default Question");
 		for(int i = 0; i < answers.length;i++){
 			answers[i] = new Answer();
 			group.add(answers[i]);
 		}
 		confirm.addActionListener(this);
 	}
 	/**
 	 * 
 	 * @param text
 	 */
 	public Question(String text){
 		super();
 		question = text;
 		label = new JLabel(text);
 		for(int i = 0; i < answers.length;i++){
 			answers[i] = new Answer();
 			group.add(answers[i]);
 		}
 		confirm.addActionListener(this);
 	}
 	
 	public void actionPerformed(ActionEvent e){
 		for(int i = 0; i < answers.length;i++){
 			if(answers[i].isSelected()){
 				System.out.println("Button "+i+" is selected");
 			}
 		}
 	}
 }
