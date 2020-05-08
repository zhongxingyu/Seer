 package ui.spiel.steuerung;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.util.LinkedList;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.AbstractListModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListSelectionModel;
 import javax.swing.JLabel;
 import javax.swing.JList;
 
 import pd.spiel.spieler.SpielerFarbe;
 
 import applikation.client.pd.Spiel;
 import applikation.client.pd.HinweisSender.Hinweis;
 
 /**
  * Zeit die letzten Aktionen/Hinweise des Spiels an.
  */
 public class HinweisView extends JList implements Observer {
 	private HinweisListModel listModel;
 
 	public HinweisView(Spiel spiel) {
 		spiel.hinweis.addObserver(this);
 
 		listModel = new HinweisListModel();
 		setModel(listModel);
 		setCellRenderer(new HinweisListCellRenderer());
 		setSelectionModel(new HinweisListSelectionModel());
 	}
 
 	public void update(Observable o, Object arg) {
 		listModel.addElement((Hinweis) arg);
 		setSelectedIndex(listModel.getSize() - 1);
 	}
 
 	/**
 	 * Wählt immer den letzten Eintrag aus.
 	 */
 	private class HinweisListSelectionModel extends DefaultListSelectionModel {
 		@Override
 		public void setSelectionInterval(int index0, int index1) {
 			int lastEntry = listModel.getSize() - 1;
 			super.setSelectionInterval(lastEntry, lastEntry);
 		}
 
 		@Override
 		public void addSelectionInterval(int index0, int index1) {
 		}
 	}
 
 	/**
 	 * Färbt die Einträge je nach Spieler ein.
 	 */
 	private class HinweisListCellRenderer extends DefaultListCellRenderer {
 		@Override
 		public Component getListCellRendererComponent(JList list, Object value,
 		                                              int index,
 		                                              boolean isSelected,
 		                                              boolean cellHasFocus) {
 			JLabel lcrc = (JLabel) super.getListCellRendererComponent(list,
 			                                                          value,
 			                                                          index,
 			                                                          isSelected,
 			                                                          cellHasFocus);
 			Hinweis hinweis = (Hinweis) list.getModel().getElementAt(index);
 			SpielerFarbe spielerFarbe = hinweis.spielerFarbe;
 			if (spielerFarbe != null) {
 				switch (spielerFarbe) {
 				case blau:
 					lcrc.setForeground(Color.blue);
 					break;
 				case gelb:
					lcrc.setForeground(Color.yellow);
 					break;
 				case gruen:
					lcrc.setForeground(Color.green);
 					break;
 				case rot:
 					lcrc.setForeground(Color.red);
 					break;
 				default:
 					throw new RuntimeException("Unbekannte Spielerfarbe.");
 				}
 			}
 			return lcrc;
 		}
 	}
 
 	private class HinweisListModel extends AbstractListModel {
 		private final int MAX_ANZAHL_HINWEISE = 8;
 		private LinkedList<Hinweis> hinweise = new LinkedList<Hinweis>();
 
 		public Object getElementAt(int index) {
 			return hinweise.get(index);
 		}
 
 		public void addElement(Hinweis hinweis) {
 			if (hinweise.size() > 0 && hinweise.getLast().ersetzen == true)
 				hinweise.removeLast();
 			hinweise.add(hinweis);
 			if (hinweise.size() > MAX_ANZAHL_HINWEISE)
 				hinweise.removeFirst();
 			fireIntervalAdded(this, hinweise.size() - 1, hinweise.size() - 1);
 		}
 
 		public int getSize() {
 			return hinweise.size();
 		}
 	}
 }
