 package ui;
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.TextArea;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Date;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 
import domain.Project;
import domain.Question;
import domain.RecordLog;
import domain.VoterDB;
 
 /**
  * 
  * @author Guy2jz
  */
 public class VoteUI extends RecordLog {
 
 	/**
 	 * 
 	 */
 	private JFrame frame;
 	private JLabel question;
 	private String qname = " : Please select the question";
 	private JButton summit;
 	private JButton exit;
 	private JComboBox<Question> patternList;
 	private Font font = new Font("Tahoma", Font.BOLD, 16);
 	private Font font2 = new Font("Tahoma", Font.PLAIN, 14);
 	public VoterDB voterDB;
 	private ArrayList<Project> arrProject;
 	private JRadioButton[] projectTeam;
 	private ButtonGroup btg = new ButtonGroup();
 	private JPanel pl;
 	private String descrip = "Project Description\n";
 	private Map<String, Project> map = new HashMap<String, Project>();
 	private String s;
 	private ArrayList<Question> listQues;
 	private TextArea tx = new TextArea();
 	private String temp;
 
 	public VoteUI(VoterDB voterDB) {
		frame = new JFrame();
 		this.voterDB = voterDB;
 		arrProject = voterDB.getProject();
 		listQues = voterDB.getQuestion();
 		pl = new JPanel(new GridLayout(10, 1));
 		frame.setTitle("Vote for Projects");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		initComponents();
 		frame.pack();
 		frame.setSize(800, 500);
 		frame.setLocation(280, 100);
 	}
 
 	public void run() {
 		frame.setVisible(true);
 	}
 
 	public void initComponents() {
 		frame.setLayout(new BorderLayout());
 		JPanel scllpl = new JPanel(new GridLayout(1, 2));
 		JPanel questionPlane = new JPanel(new GridLayout(1, 2));
 		questionPlane.setBorder(BorderFactory
 				.createTitledBorder("Question Detail"));
 		JPanel buttonPlane = new JPanel(new BorderLayout());
 		JPanel insideButtonPlane = new JPanel(new FlowLayout());
 		JScrollPane scrollPane = new JScrollPane(pl);
 		scrollPane.setPreferredSize(new Dimension(700, 50));
 		JScrollPane txscll = new JScrollPane(tx);
 		txscll.setBorder(BorderFactory
 				.createTitledBorder("Project Description"));
 		scrollPane.setPreferredSize(new Dimension(700, 50));
 		scrollPane.setBorder(BorderFactory
 				.createTitledBorder("Choose Project Team"));
 
 		Question[] arrQ = new Question[listQues.size()];
 		listQues.toArray(arrQ);
 		patternList = new JComboBox<Question>(arrQ);
 		patternList.setEditable(false);
 		patternList.addActionListener(new QuestionListener());
 		patternList.setFont(font2);
 		summit = new JButton();
 		summit.setFont(font2);
 		summit.setText("summit");
 		summit.addActionListener(new SummitListener());
 		exit = new JButton();
 		exit.setFont(font2);
 		exit.setText("exit");
 		exit.addActionListener(new ExitListener());
 		question = new JLabel();
 		question.setFont(font);
 		question.setText("Question: " + qname);
 
 		tx.setFont(font2);
 		tx.setText(descrip);
 		tx.setEditable(false);
 
 		questionPlane.add(question);
 		questionPlane.add(patternList);
 
 		createButton();
 		insideButtonPlane.add(summit);
 		insideButtonPlane.add(exit);
 		buttonPlane.add(insideButtonPlane, BorderLayout.EAST);
 		scllpl.add(scrollPane);
 		scllpl.add(txscll);
 		frame.add(scllpl, BorderLayout.CENTER);
 		frame.add(questionPlane, BorderLayout.NORTH);
 		frame.add(buttonPlane, BorderLayout.SOUTH);
 
 	}
 
 	private void createButton() {
 		projectTeam = new JRadioButton[arrProject.size()];
 		for (int i = 0; i < arrProject.size(); i++) {
 			map.put(arrProject.get(i).getProjectName(), arrProject.get(i));
 			projectTeam[i] = new JRadioButton(arrProject.get(i)
 					.getProjectName());
 			projectTeam[i].addActionListener(new ShowDesListener());
 			projectTeam[i].setActionCommand(arrProject.get(i).getProjectName());
 			projectTeam[i].setFont(font);
 			btg.add(projectTeam[i]);
 			pl.add(projectTeam[i]);
 		}
 
 	}
 
 	private void clearRadioButton() {
 		btg.clearSelection();
 	}
 
 	class SummitListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			if (btg.getSelection() == null) {
 				JOptionPane.showMessageDialog(null,
 						" Select project before summit!");
 			} else {
 				java.util.Date date = new java.util.Date();
 				java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());
 				if (voterDB.canInsert(
 						(int) voterDB.getVoter().getId(),
 						(int) map
 								.get(btg.getSelection().getActionCommand())
 								.getID(),
 						(int) patternList.getItemAt(
 								patternList.getSelectedIndex())
 								.getQuestionID(), 1, timestamp)) {
 				temp = map.get(btg.getSelection().getActionCommand())
 						.getTeamName();
 				JOptionPane.showMessageDialog(null, " You voted \""
 						+ btg.getSelection().getActionCommand()
 						+ "\".\nTeam Name: " + temp);
 				record("Question : " + patternList.getSelectedItem().toString()
 						+ "| Vote - project : " + "" + temp);
 				try {
 					
 					
 						voterDB.insertVoteDB(
 								(int) voterDB.getVoter().getId(),
 								(int) map.get(
 										btg.getSelection().getActionCommand())
 										.getID(),
 								(int) patternList.getItemAt(
 										patternList.getSelectedIndex())
 										.getQuestionID(), 1, timestamp);
 						record("Vote have been added to database");
 				 
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					record("Can't not add to database");
 				}
 				} else {
 					temp = map.get(btg.getSelection().getActionCommand())
 							.getTeamName();
 					JOptionPane.showMessageDialog(null, "You have vote the same team and question","Error!!",JOptionPane.ERROR_MESSAGE);
 					record("You have vote the same team and question");
 					record("Vote doesn't add to database");
 				}
 			
 				clearRadioButton();
 				frame.dispose();
 				ResultUI resultUI= new ResultUI(voterDB);
 			}
 		}
 	}
 
 	class ExitListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			frame.dispose();
 		}
 	}
 
 	class ShowDesListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			s = e.getActionCommand();
 			tx.setText(descrip + "This project is " + s + " made by "
 					+ map.get(s).getTeamName());
 		}
 	}
 
 	class QuestionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (!qname.equalsIgnoreCase(patternList.getSelectedItem()
 					.toString())) {
 				qname = (patternList.getItemAt(patternList.getSelectedIndex())
 						.getQuestion());
 				question.setText("Question : " + qname);
 				clearRadioButton();
 			}
 		}
 	}
 }
