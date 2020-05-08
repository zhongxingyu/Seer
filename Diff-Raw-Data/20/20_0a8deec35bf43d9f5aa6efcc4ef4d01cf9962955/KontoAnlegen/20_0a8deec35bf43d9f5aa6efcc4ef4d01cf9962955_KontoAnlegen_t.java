 package de.g18.BitBank.Gui;
 
 import de.g18.BitBank.Gui.Listener.KontoAnlegenListener;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Gui Klasse zum anlegen eines Kundens.
  * 
  * @author it1-markde
  * @since JRE6
  */
 
 public class KontoAnlegen extends JPanel {
 	private static final long serialVersionUID = -4665144187936969278L;
 
 	private JTabbedPane tabsPane;
 
 	public KontoAnlegen(JTabbedPane tabsPane) {
 		this.tabsPane = tabsPane;
 
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(5, 5, 5, 5);
 
		JLabel kontoNummerLabel = new JLabel("Kontonummer");
 		JTextField kundenNummerField = new JTextField();
 		JLabel kontoTypLabel = new JLabel("Kontotyp");
 		JRadioButton giroKontoRadioButton = new JRadioButton("GiroKonto");
 		JRadioButton sparKontoRadioButton = new JRadioButton("SparKonto");
 		JButton anlegenButton = new JButton("Anlegen");
 		JButton beendenButton = new JButton("Beenden");
 
 		ButtonGroup kontoTypSelect = new ButtonGroup();
 		kontoTypSelect.add(giroKontoRadioButton);
 		kontoTypSelect.add(sparKontoRadioButton);
 
 		c.gridx = 0;
 		c.gridy = 0;
		add(kontoNummerLabel, c);
 
 		c.gridx = 1;
 		c.gridy = 0;
 		c.weightx = 1;
 		add(kundenNummerField, c);
 
 		c.gridx = 0;
 		c.gridy = 1;
 		c.weightx = 0;
 		add(kontoTypLabel, c);
 
 		c.gridx = 1;
 		c.gridy = 1;
 		add(giroKontoRadioButton, c);
 
 		c.gridx = 1;
 		c.gridy = 2;
 		add(sparKontoRadioButton, c);
 
 		c.gridx = 2;
 		c.gridy = 2;
 		add(anlegenButton, c);
 
 		c.gridx = 2;
 		c.gridy = 3;
 		add(beendenButton, c);
 
 		c.gridx = 0;
 		c.gridy = 4;
 		c.gridwidth = 3;
 		c.weighty = 1;
 		this.add(new JPanel(), c);
 
 		anlegenButton.addActionListener(new KontoAnlegenListener(
 				kundenNummerField));
 		beendenButton.addActionListener(new KontoAnlegenListener(this));
 	}
 
 	public JTabbedPane getTabsPane() {
 		return tabsPane;
 	}
 }
