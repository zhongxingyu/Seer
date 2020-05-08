 package de.g18.BitBank.Gui.Listener;
 
 import com.toedter.calendar.JDateChooser;
 import de.g18.BitBank.BankController;
 import de.g18.BitBank.Exception.BetragNegativException;
 import de.g18.BitBank.Exception.KontoLeerException;
 import de.g18.BitBank.Exception.KontoNichtGefundenException;
 import de.g18.BitBank.Gui.Ueberweisung;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Date;
 
 /**
  * Listener zu den Buttons der Ueberweisung Klasse.
  * <p/>
  * /** F Listener zu den Buttons der Ueberweisung Klasse.
  * 
  * @author it1-markde
  * @since JRE6
  */
 
 public class UeberweisungListener implements ActionListener {
 	private Ueberweisung ueberweisungFrame;
 	private JTextField vomKontoField;
 	private JTextField nachKontoField;
 	private JTextField betragField;
 	private BankController controller;
 	private JDateChooser chooser;
 
 	public UeberweisungListener(final Ueberweisung ueberweisungFrame) {
 		this.ueberweisungFrame = ueberweisungFrame;
 	}
 
 	public UeberweisungListener(final JTextField vomKontoField,
 			final JTextField nachKontoField, final JTextField betragField,
 			final BankController controller, final JDateChooser chooser) {
 
 		this.vomKontoField = vomKontoField;
 		this.nachKontoField = nachKontoField;
 		this.betragField = betragField;
 		this.controller = controller;
 		this.chooser = chooser;
 
 	}
 
 	@Override
 	public void actionPerformed(final ActionEvent event) {
 
 		JButton buttonClicked = (JButton) event.getSource();
 
 		if (buttonClicked.getText().compareTo("Überweisen") == 0) {
 			double betrag;
 			int vomKontoNummer;
 			int nachKontoNummer;
 
 			try {
 				betrag = Double.parseDouble(this.betragField.getText());
 				vomKontoNummer = Integer.parseInt(this.vomKontoField.getText());
 				nachKontoNummer = Integer.parseInt(this.nachKontoField
 						.getText());
 			} catch (Exception e) {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"Eingabe konnte nicht gelesen werden. (Alles korrekte Zahlen?)",
 								"Fehler", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			Date datum = chooser.getDate();
 
 			if (datum == null) {
 				JOptionPane.showMessageDialog(null,
						"Bitte wählen sie ein Datum", "Fehlpusher",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			try {
 				this.controller.ueberweisen(nachKontoNummer, vomKontoNummer,
 						betrag, datum);
 			} catch (KontoLeerException e) {
 				JOptionPane.showMessageDialog(null, e.getMessage(), "Fehler",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			} catch (BetragNegativException e) {
 				JOptionPane.showMessageDialog(null, e.getMessage(), "Fehler",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			} catch (KontoNichtGefundenException e) {
 				JOptionPane.showMessageDialog(null, e.getMessage(), "Fehler",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 		} else if (buttonClicked.getText().compareTo("Beenden") == 0) {
 			this.ueberweisungFrame.getTabsPane().remove(this.ueberweisungFrame);
 		}
 	}

 }
