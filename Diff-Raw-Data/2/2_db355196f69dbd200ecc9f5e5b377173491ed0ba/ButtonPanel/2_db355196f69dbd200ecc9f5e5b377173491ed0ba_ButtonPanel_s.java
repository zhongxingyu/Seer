 package matchingAlgorithm.gui;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 
 public class ButtonPanel extends JPanel {
 	JButton findMatches;
 	JCheckBox ethnicity, language, disability, distance, age, income;
 	
 	public ButtonPanel(ActionListener alistener, ItemListener ilistener){
 		super();
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		findMatches = new JButton("Find Matches");
 		findMatches.setActionCommand("find matches");
 		findMatches.addActionListener(alistener);
 		
 		this.add(findMatches, this);
 		
 		ethnicity = new JCheckBox("Ethnicity");
 		this.add(ethnicity, this);
 		ethnicity.addItemListener(ilistener);
 		
 		language = new JCheckBox("Language");
 		this.add(language, this);
 		language.addItemListener(ilistener);
 		
 		disability = new JCheckBox("Disability");
 		this.add(disability, this);
 		disability.addItemListener(ilistener);
 		
 		distance = new JCheckBox("Distance");
 		this.add(distance, this);
 		distance.addItemListener(ilistener);
 		
 		age = new JCheckBox("Age");
 		this.add(age, this);
 		age.addItemListener(ilistener);
 		
		income = new JCheckBox("Language");
 		this.add(income, this);
 		income.addItemListener(ilistener);
 	}
 
 	public JCheckBox getEthnicity() {
 		return ethnicity;
 	}
 
 	public JCheckBox getLanguage() {
 		return language;
 	}
 
 	public JCheckBox getDisability() {
 		return disability;
 	}
 
 	public JCheckBox getDistance() {
 		return distance;
 	}
 
 	public JCheckBox getAge() {
 		return age;
 	}
 
 	public JCheckBox getIncome() {
 		return income;
 	}
 }
