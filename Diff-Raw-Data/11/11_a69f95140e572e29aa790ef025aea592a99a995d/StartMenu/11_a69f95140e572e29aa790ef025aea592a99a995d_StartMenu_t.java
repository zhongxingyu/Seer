 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class StartMenu extends JFrame implements ActionListener {
 
 	// Panels und zugehoerige Buttons initialisieren
 	JPanel startpanel = new JPanel();
 	JButton start = new JButton("Start(WIP)");
 
 	JPanel testpanel = new JPanel();
 	JButton test = new JButton("TestRun");
 
 	JPanel setspanel = new JPanel();
 	JButton sets = new JButton("Settings(WIP)");
 
 	JPanel exitpanel = new JPanel();
 	JButton exit = new JButton("Exit");
 
 	// ...ebenso das Titelpanel
 	JPanel titlepanel = new JPanel();
 	JLabel title = new JLabel("Ganz schön dolles Spiel");
 
 	// Konstruktor
 	public StartMenu() {
 
 		getContentPane().setLayout(
 				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
 
 		// Buttons den Panels hinzufuegen
 		titlepanel.add(title);
 
 		startpanel.add(start);
 
 		testpanel.add(test);
 
 		setspanel.add(sets);
 
 		exitpanel.add(exit);
 
 		// Panels dem Oberpanel hinzufuegen
 		getContentPane().add(titlepanel);
 
 		getContentPane().add(startpanel);
 
 		getContentPane().add(testpanel);
 
 		getContentPane().add(setspanel);
 
 		getContentPane().add(exitpanel);
 
 		// Buttons beim ActionListener registrieren
 		start.addActionListener(this);
 		start.setActionCommand("start");
 
 		test.addActionListener(this);
 		test.setActionCommand("test");
 
 		sets.addActionListener(this);
 		sets.setActionCommand("sets");
 
 		exit.addActionListener(this);
 		exit.setActionCommand("exit");
 	}
 
 	// ActionLister: Hier werden den Buttones Aktionen zugeordnet.
 	public void actionPerformed(ActionEvent evt) {
 		if (evt.getActionCommand().equals("start")) { // Startet das Spiel
 
 		}
 		if (evt.getActionCommand().equals("test")) { // Startet den Testrun
			Runtester.go();
 		}
 		if (evt.getActionCommand().equals("sets")) { // Oeffnet ein neues
 														// Fenster: "Settings"
 
 		}
 		if (evt.getActionCommand().equals("exit")) { // Beendet das Programm
 			System.exit(0);
 		}
 
 	}
 }
