 package de.g18.BitBank.Gui;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 import de.g18.BitBank.Gui.Listener.KontoAnlegenListener;
 
 /**
  * Gui Klasse zum anlegen eines Kundens.
  * 
  * @author it1-markde
  * @since JRE6
  */
 
 @SuppressWarnings("serial")
 public class KontoAnlegen extends JFrame {
 
 	public KontoAnlegen() {
 
 		this.setSize(400, 400);
 		this.setLocationRelativeTo(null);
 		this.setLayout(new GridLayout(0, 3));
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setTitle("Bank-Anwendung - Ein- / Auszahlung durchführen");
 
 		JPanel links = new JPanel();
 		JPanel mitte = new JPanel();
 		JPanel rechts = new JPanel();
 		JLabel kundenNummerLabel = new JLabel("KundenNummer");
 		JTextField kundenNummerField = new JTextField(10);
 		JRadioButton giroKontoRadioButton = new JRadioButton("GiroKonto");
 		JRadioButton sparKontoRadioButton = new JRadioButton("SparKonto");
 		JButton anlegenButton = new JButton("Anlegen");
 		JButton beendenButton = new JButton("Beenden");
 
 		mitte.setLayout(new FlowLayout(FlowLayout.LEFT));
 		anlegenButton.setPreferredSize(new Dimension(100, 25));
 		beendenButton.setPreferredSize(new Dimension(100, 25));
 
 		this.add(links);
 		this.add(mitte);
 		this.add(rechts);
		System.out.println("test");
 		links.add(kundenNummerLabel);
 		mitte.add(kundenNummerField);
 		mitte.add(giroKontoRadioButton);
 		mitte.add(sparKontoRadioButton);
 		rechts.add(anlegenButton);
 		rechts.add(beendenButton);
 
 		anlegenButton.addActionListener(new KontoAnlegenListener(
 				kundenNummerField));
 		beendenButton.addActionListener(new KontoAnlegenListener(this));
 
 		this.setVisible(true);
 
 	}
 
 	public static void main(String[] args) {
 		new KontoAnlegen();
 	}
 }
