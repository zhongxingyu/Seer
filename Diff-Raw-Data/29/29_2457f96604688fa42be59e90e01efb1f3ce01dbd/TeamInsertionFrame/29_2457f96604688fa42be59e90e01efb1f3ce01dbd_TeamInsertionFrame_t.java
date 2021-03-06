 package taberystwyth.view;
 
 import java.awt.GridLayout;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class TeamInsertionFrame extends JFrame {
	private static final long serialVersionUID = 1L;

 	TeamInsertionListener listener = new TeamInsertionListener(this);
 	
 	JTextField teamName = new JTextField();
 	JCheckBox swing = new JCheckBox();
 	
 	JTextField speaker1 = new JTextField();
 	JTextField speaker2 = new JTextField();
 	
 	JTextField institution1 = new JTextField();
 	JTextField institution2 = new JTextField();
 	
 	JCheckBox esl1 = new JCheckBox();
 	JCheckBox esl2 = new JCheckBox();
 	
 	JCheckBox novice1 = new JCheckBox();
 	JCheckBox novice2 = new JCheckBox();
 	
 	JTextField inst1 = new JTextField();
 	JTextField inst2 = new JTextField();
 	
 	JButton clear = new JButton("Clear");
 	JButton save = new JButton("Save");
 	
 	JLabel teamNameLabel = new JLabel("Team Name:");
 	JLabel swingLabel = new JLabel("Swing?");
 	JLabel speaker1Label = new JLabel("Speaker Name 1:");
 	JLabel speaker2Label = new JLabel("Speaker Name 2:");
 	JLabel esl1Label = new JLabel("Esl?:");
 	JLabel novice1Label = new JLabel("Novice?:");
 	JLabel esl2Label = new JLabel("Esl?:");
 	JLabel novice2Label = new JLabel("Novice?:");
 	JLabel inst1Label = new JLabel("Institution 1:");
 	JLabel inst2Label = new JLabel("Institution 2:");
 	
 	public TeamInsertionFrame(){
 		setVisible(true);
 		setLayout(new GridLayout(13,2));
 		add(teamNameLabel);
 		add(teamName);
 		add(swingLabel);
 		add(swing);
 		add(new JPanel());
 		add(new JPanel());
 		add(speaker1Label);
 		add(speaker1);
 		add(inst1Label);
 		add(inst1);
 		add(esl1Label);
 		add(esl1);
 		add(novice1Label);
 		add(novice1);
 		add(new JPanel());
 		add(new JPanel());
 		add(speaker2Label);
 		add(speaker2);
 		add(inst2Label);
 		add(inst2);
 		add(esl2Label);
 		add(esl2);
 		add(novice2Label);
 		add(novice2);
 		add(clear);
 		add(save);
 		pack();
 		
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		clear.addActionListener(listener);
 		save.addActionListener(listener);
 		
 	}
 
 	public void clear() {
 		System.out.println("Clear");
 		teamName.setText("");
 		swing.setSelected(false);
 		
 		speaker1.setText("");
 		inst1.setText("");
 		esl1.setSelected(false);
 		novice1.setSelected(false);
 		
 		speaker2.setText("");
 		inst2.setText("");
 		esl2.setSelected(false);
 		novice2.setSelected(false);
 	}
 
 	public void save() {
 		System.out.println("Save");
 		
 	}
 
 }
