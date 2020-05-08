 package local.sandbox.otherpeople.projektgui;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Projekt
  */
 public class Projekt extends JFrame implements ActionListener {
 
 	private JMenuBar menubar;
 	private JMenu Menu;
 	private JMenuItem miWyjscie;
 	private JButton importXml, wykonaj, wyczysc;
 	private JScrollPane jscrollpane;
 	private JToggleButton polacz;
 	private ButtonGroup bgWybor;
 	private JTextArea poleText1, poleText2;
 	private int a, b, s;
 
 	public Projekt() {
 
 		setSize(535, 400);
 		setTitle("Baza danych");
 		setLayout(null);
 
 
 		//Menu
 		menubar = new JMenuBar();
 		Menu = new JMenu("Plik");
 
 		setJMenuBar(menubar);
 
 		menubar.add(Menu);
		miWyjscie = new JMenuItem("Zakocz");
 		miWyjscie.addActionListener(this);
 
 		Menu.add(miWyjscie);
 
 		//Przyciski i pola
 
 		bgWybor = new ButtonGroup();
 
 
		polacz = new JToggleButton("Pocz");
 		polacz.setBounds(10, 10, 500, 20);
 		add(polacz);
 		polacz.addActionListener(this);
 
 		importXml = new JButton("Import danych z pliku XML");
 		importXml.setBounds(10, 40, 500, 20);
 		add(importXml);
 		importXml.addActionListener(this);
 		importXml.setEnabled(false);
 
 		poleText1 = new JTextArea("");
 		poleText1.setLineWrap(true);
 		jscrollpane = new JScrollPane(poleText1);
 		jscrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		jscrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		jscrollpane.setBounds(10, 70, 500, 100);
 		add(jscrollpane);
 		poleText1.setEnabled(false);
 
 
 		wykonaj = new JButton("Wykonaj");
 		wykonaj.setBounds(10, 200, 150, 20);
 		add(wykonaj);
 		wykonaj.addActionListener(this);
 		wykonaj.setEnabled(false);
 
		wyczysc = new JButton("Wyczy");
 		wyczysc.setBounds(360, 200, 150, 20);
 		add(wyczysc);
 		wyczysc.addActionListener(this);
 		wyczysc.setEnabled(false);
 
 
 		poleText2 = new JTextArea("");
 		poleText2.setLineWrap(true);
 		jscrollpane = new JScrollPane(poleText2);
 		jscrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		jscrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		jscrollpane.setBounds(10, 230, 500, 100);
 		add(jscrollpane);
 		poleText2.setEditable(false);
 		poleText2.setEnabled(false);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 
 		//Zmiana tekstu w JToggleButton
 		if (polacz.isSelected()) {
			polacz.setText("Rozcz");
 
 		} else {
			polacz.setText("Pocz");
 
 		}
 
		//Blokowanie przyciskw
 		if (polacz.isSelected()) {
 			wyczysc.setEnabled(true);
 			importXml.setEnabled(true);
 			wykonaj.setEnabled(true);
 			poleText1.setEnabled(true);
 			poleText2.setEnabled(true);
 
 		} else {
 			wyczysc.setEnabled(false);
 			importXml.setEnabled(false);
 			wykonaj.setEnabled(false);
 			poleText1.setEnabled(false);
 			poleText2.setEnabled(false);
 		}
 
		//Koczenie pracy i czyszczenie pl
		if (e.getActionCommand().equals("Zakocz")) {
 			dispose();
 		}
		if (e.getActionCommand().equals("Wyczy")) {
 			poleText1.setText(null);
 			poleText2.setText(null);
 		}
 	}
 
 	public static void main(String[] args) {
 
 		Projekt projekt = new Projekt();
 		projekt.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		projekt.setVisible(true);
 	}
 }
