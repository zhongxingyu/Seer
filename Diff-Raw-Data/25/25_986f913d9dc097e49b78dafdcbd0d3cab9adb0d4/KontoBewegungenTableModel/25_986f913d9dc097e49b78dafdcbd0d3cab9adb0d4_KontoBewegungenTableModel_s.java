 package de.g18.BitBank.Gui.TableModels;
 
 import de.g18.BitBank.Konto;
 import de.g18.BitBank.Kontobewegung;
 import de.g18.BitBank.Ueberweisung;
 
 import javax.swing.table.AbstractTableModel;
 
 /**
  * Tabelle zur Anzeige der Kontobewegungen.
  * 
  * @author it1-korebj
  * @since JRE6
  */
 
 public class KontoBewegungenTableModel extends AbstractTableModel {
 	private static final long serialVersionUID = -5781280913032155581L;
 	private Konto konto;
 	private String[] cols = { "Datum", "von", "nach", "Betrag", "Text" };
 
 	public KontoBewegungenTableModel(final Konto konto) {
 		this.konto = konto;
 	}
 
 	@Override
 	public int getRowCount() {
 		return konto.getKontoBewegungsListe().size();
 	}
 
 	@Override
 	public int getColumnCount() {
 		return cols.length;
 	}
 
 	@Override
 	public String getColumnName(final int col) {
 		return cols[col];
 	}
 
 	@Override
 	public Object getValueAt(final int rowIndex, final int columnIndex) {
 		Kontobewegung kb = konto.getKontoBewegungsListe().get(rowIndex);
 
 		switch (columnIndex) {
 		case 0:
 			return kb.getDatum().toString();
 		case 1:
 			if (kb.getClass().equals(Ueberweisung.class)) {
 				// bei einer überweisung wird die quell kontonummer ausgebene
 				return ((Ueberweisung) kb).getQuellKlasse().getKontoNummer();
 			} else if (kb.getClass().equals(Kontobewegung.class)) {
 				// bei einer kontobewegun passend "einzahlung" oder die
 				// kontonummer
 				if (kb.getBetrag() > 0) {
 					return "Einzahlung";
 				} else {
 					return Long.toString(konto.getKontoNummer());
 				}
 			} else {
 				return "Fehler";
 			}
 		case 2:
 			if (kb.getClass().equals(Ueberweisung.class)) {
 				// bei einer überweisung wird die ziel kontonummer ausgebene
 				return ((Ueberweisung) kb).getZielKlasse().getKontoNummer();
 			} else if (kb.getClass().equals(Kontobewegung.class)) {
 				// bei einer kontobewegun passend "auszahlung" oder die
 				// kontonummer
 				if (kb.getBetrag() > 0) {
 					return Long.toString(konto.getKontoNummer());
 				} else {
 					return "Auszahlung";
 				}
 			} else {
 				return "Fehler";
 			}
 		case 3:
			return Double.toString(kb.getBetrag());
 		case 4:
 			return kb.getText();
 		default:
 			return "Fehler";
 		}
 	}
 }
