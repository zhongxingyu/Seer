 package de.g18.BitBank.Gui.Listener;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JTextField;
 
 import de.g18.BitBank.BankController;
 import de.g18.BitBank.Gui.ZahlungVornehmen;
 
 /**
  * Listener zu den Buttons der ZahlungVornehmen Klasse.
  * 
  * @author it1-markde
  * @since JRE6
  */
 
 public class ZahlungVornehmenListener implements ActionListener {
 
 	private ZahlungVornehmen zahlungVornehmenFrame;
 	private JTextField kontoNummerField;
 	private BankController controller;
 	private JTextField alterKontoStandField;
 	private JTextField neuerKontoStandField;
 	private JTextField betragField;
 
 	public ZahlungVornehmenListener(ZahlungVornehmen zahlungVornehmenFrame) {
 		this.zahlungVornehmenFrame = zahlungVornehmenFrame;
 	}
 
 	public ZahlungVornehmenListener(JTextField kontoNummerField,
 			JTextField alterKontoStandField, JTextField neuerKontoStandField,
 			JTextField betragField, BankController controller) {
 		this.kontoNummerField = kontoNummerField;
 		this.alterKontoStandField = alterKontoStandField;
 		this.neuerKontoStandField = neuerKontoStandField;
 		this.betragField = betragField;
 		this.controller = controller;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 
 		JButton buttonClicked = (JButton) event.getSource();
 
 		int kontoNummer = Integer.parseInt(this.kontoNummerField.getText());
 
 		if (buttonClicked.getText().compareTo("Kontostand") == 0) {
 			this.alterKontoStandField.setText(""
 					+ this.controller.kontoStandAnzeigen(kontoNummer));
 		}
 		if (buttonClicked.getText().compareTo("Einzahlung") == 0) {
 			double betrag = Double.parseDouble(this.betragField.getText());
 			this.controller.einzahlen(kontoNummer, betrag);
 
 			this.aktualisieren(kontoNummer);
 
 		}
 		if (buttonClicked.getText().compareTo("Auszahlung") == 0) {
 			double betrag = Double.parseDouble(this.betragField.getText());
 			this.controller.auszahlen(kontoNummer, betrag);
 
 			this.aktualisieren(kontoNummer);
 		}
 		if (buttonClicked.getText().compareTo("Beenden") == 0) {
 			this.zahlungVornehmenFrame.getTabsPane().remove(
 					this.zahlungVornehmenFrame);
 		}
 	}
 
 	public void aktualisieren(int kontoNummer) {
 		if (!this.neuerKontoStandField.getText().equals("")) {
 			this.alterKontoStandField.setText(""
					+ this.neuerKontoStandField.getText());
 		}
 
 		this.neuerKontoStandField.setText(""
 				+ this.controller.kontoStandAnzeigen(kontoNummer));
 	}
 }
