 package gui;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class Buttons extends JPanel implements ActionListener {
 
 	private JButton event, meating, groups, logout, rArrow, lArrow;
 	private JLabel curWeek;
 	private int weekNr = 6;
 	private int year = 2013;
 
 	GridBagConstraints gbc = new GridBagConstraints();
 
 	public Buttons() {
 		event = new JButton("Legg Til Avtale");
 		event.addActionListener(this);
 
		meating = new JButton("Opprett Møte");
 		meating.addActionListener(this);
 
 		groups = new JButton("Gruppeinnstillinger");
 		groups.addActionListener(this);
 
 		curWeek = new JLabel();
 		curWeek.setText("Uke 6, 2013");
 
 		rArrow = new JButton(">");
 		rArrow.addActionListener(this);
 
 		lArrow = new JButton("<");
 		lArrow.addActionListener(this);
 
 		logout = new JButton("Logg Ut");
 		logout.addActionListener(this);
 
 		setLayout(new GridBagLayout());
 		gbc.gridy = 0;
 		add(event, gbc);
 		add(meating, gbc);
 		add(groups, gbc);
 		add(curWeek, gbc);
 		add(lArrow, gbc);
 		add(rArrow, gbc);
 		add(logout, gbc);
 	}
 
 	public void nextWeek() {
 		weekNr += 1;
 		curWeek.setText("Uke " + weekNr + ", " + year);
 	}
 
 	public void prevWeek() {
 		weekNr -= 1;
 		curWeek.setText("Uke " + weekNr + ", " + year);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().toString().equals("Legg Til Avtale")) {
 			MainClass.runAddEvent();
 		} else if (e.getActionCommand().toString().equals("Logg Ut")) {
 			MainClass.logout();
 		} else if (e.getActionCommand().toString().equals("<")) {
 			validWeek(weekNr - 1);
 			prevWeek();
 		} else if (e.getActionCommand().toString().equals(">")) {
 			validWeek(weekNr + 1);
 			nextWeek();
 		}
		else if(e.getActionCommand().toString().equals("Opprett Møte")){
 			MainClass.runAddMeating();
 		}
 		else if(e.getActionCommand().toString().equals("Gruppeinnstillinger")){
 			MainClass.runGroupSettings();
 		}
 	}
 
 	public void validWeek(int wNr) {
 		if (wNr > 52) {
 			nextYear();
 		} else if (wNr < 1) {
 			prevYear();
 		}
 	}
 
 	public void prevYear() {
 		year -= 1;
 		weekNr = 52;
 		curWeek.setText("Uke " + weekNr + ", " + year);
 	}
 
 	public void nextYear() {
 		year += 1;
 		weekNr = 1;
 		curWeek.setText("Uke " + weekNr + ", " + year);
 	}
 
 }
