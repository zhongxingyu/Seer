 package de.g18.BitBank.Gui.Listener;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JTextField;
 
 import de.g18.BitBank.BankController;
 import de.g18.BitBank.Exception.BetragNegativException;
 import de.g18.BitBank.Exception.KeineGueltigeZahlException;
 import de.g18.BitBank.Exception.KontoLeerException;
 import de.g18.BitBank.Exception.KontoNichtGefundenException;
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
 
 	public ZahlungVornehmenListener(final ZahlungVornehmen zahlungVornehmenFrame) {
 		this.zahlungVornehmenFrame = zahlungVornehmenFrame;
 	}
 
 	public ZahlungVornehmenListener(final JTextField kontoNummerField,
 			final JTextField alterKontoStandField,
 			final JTextField neuerKontoStandField,
 			final JTextField betragField, final BankController controller) {
 		this.kontoNummerField = kontoNummerField;
 		this.alterKontoStandField = alterKontoStandField;
 		this.neuerKontoStandField = neuerKontoStandField;
 		this.betragField = betragField;
 		this.controller = controller;
 	}
 
 	@Override
 	public void actionPerformed(final ActionEvent event) {
 
 		JButton buttonClicked = (JButton) event.getSource();
 		int kontoNummer = 0;
		try {
			kontoNummer = Integer.parseInt(this.kontoNummerField.getText());
		} catch (NumberFormatException e) {
 			try {
				throw new KeineGueltigeZahlException("Kontonummer");
			} catch (KeineGueltigeZahlException e1) {
				e1.showDialog();
				return;
 			}
 		}

 		if (buttonClicked.getText().compareTo("Kontostand") == 0) {
 			try {
 				this.alterKontoStandField.setText(Double
 						.toString(this.controller
 								.kontoStandAnzeigen(kontoNummer)));
 			} catch (KontoNichtGefundenException e1) {
 				e1.showDialog();
 				return;
 			}
 		} else if (buttonClicked.getText().compareTo("Einzahlung") == 0) {
 			double betrag = 0;
 
 			try {
 				betrag = Double.parseDouble(this.betragField.getText());
 			} catch (NumberFormatException e) {
 				try {
 					throw new KeineGueltigeZahlException("Der Betrag");
 				} catch (KeineGueltigeZahlException e1) {
 					e1.showDialog();
 					return;
 				}
 			}
 
 			try {
 				this.controller.einzahlen(kontoNummer, betrag);
 			} catch (KontoNichtGefundenException e) {
 				e.showDialog();
 				return;
 			} catch (BetragNegativException e) {
 				e.showDialog();
 				return;
 			}
 
 			this.aktualisieren(kontoNummer);
 
 		} else if (buttonClicked.getText().compareTo("Auszahlung") == 0) {
 			double betrag = 0;
 
 			try {
 				betrag = Double.parseDouble(this.betragField.getText());
 			} catch (NumberFormatException e) {
 				try {
 					throw new KeineGueltigeZahlException("Der Betrag");
 				} catch (KeineGueltigeZahlException e1) {
 					e1.showDialog();
 					return;
 				}
 			}
 
 			try {
 				this.controller.auszahlen(kontoNummer, betrag);
 			} catch (KontoNichtGefundenException e) {
 				e.showDialog();
 				return;
 			} catch (KontoLeerException e) {
 				e.showDialog();
 				return;
 			} catch (BetragNegativException e) {
 				e.showDialog();
 				return;
 			}
 
 			this.aktualisieren(kontoNummer);
 		} else if (buttonClicked.getText().compareTo("Beenden") == 0) {
 			this.zahlungVornehmenFrame.getTabsPane().remove(
 					this.zahlungVornehmenFrame);
 		}
 	}
 
 	public final void aktualisieren(final int kontoNummer) {
 		if (!this.neuerKontoStandField.getText().equals("")) {
 			this.alterKontoStandField.setText(this.neuerKontoStandField
 					.getText());
 		}
 
 		try {
 			this.neuerKontoStandField.setText(Double.toString(this.controller
 					.kontoStandAnzeigen(kontoNummer)));
 		} catch (KontoNichtGefundenException e) {
 			e.showDialog();
 		}
 	}
 }
