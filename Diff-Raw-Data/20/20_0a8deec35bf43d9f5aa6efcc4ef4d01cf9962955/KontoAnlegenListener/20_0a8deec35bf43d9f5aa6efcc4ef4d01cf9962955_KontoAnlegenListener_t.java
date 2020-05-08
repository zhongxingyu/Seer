 package de.g18.BitBank.Gui.Listener;
 
 import de.g18.BitBank.Gui.KontoAnlegen;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Listener zu den Buttons der KontoAnlegen Klasse.
  * 
  * @author it1-markde
  * @since JRE6
  */
 
 public class KontoAnlegenListener implements ActionListener {
	private JTextField kontoNummerField;
 	private KontoAnlegen kontoAnlegenFrame;
 
	public KontoAnlegenListener(JTextField kontoNummerField) {
		this.kontoNummerField = kontoNummerField;
 	}
 
 	public KontoAnlegenListener(KontoAnlegen kontoAnlegenFrame) {
 		this.kontoAnlegenFrame = kontoAnlegenFrame;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 
 		JButton buttonClicked = (JButton) event.getSource();
 
 		if (buttonClicked.getText().compareTo("Anlegen") == 0) {
 			try {
 				// Konto konto1 = kunde1.anlegenKonto(Kontotyp.GIROKONTO);
 
				int kundenNummer = Integer.parseInt(this.kontoNummerField
 						.getText());
 
 				JOptionPane
 						.showMessageDialog(new JFrame(),
 								"Konto unter Nummer \"" + kundenNummer
 										+ "\" angelegt.");
 
 			} catch (java.lang.NumberFormatException exception) {
 
 				JOptionPane.showMessageDialog(new JFrame(),
 						"Die Kundennummer ist keine gültige Zahl.",
 						"Inane error", JOptionPane.ERROR_MESSAGE);
 
 			}
 
 		}
 
 		if (buttonClicked.getText().compareTo("Beenden") == 0) {
 			this.kontoAnlegenFrame.getTabsPane().remove(this.kontoAnlegenFrame);
 		}
 	}
 }
