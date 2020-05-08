 package de.team55.mms.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.ScrollPaneConstants;
 
 public class newModulCard {
 	
 	private static JFrame frame;
 	private static JPanel modul_panel = new JPanel();
 	private HashMap<JButton, Integer> buttonmap = new HashMap<JButton, Integer>();
 	
 	public newModulCard() {
 		// Alle vorhandenen Felder entfernen
 		modul_panel.removeAll();
 		final JPanel pnl_newmod = new JPanel();
 
 		// Liste dynamischer Buttons leeren
 		if (!buttonmap.isEmpty()) {
 			for (int i = 0; i < buttonmap.size(); i++)
 				buttonmap.remove(i);
 		}
 
 		// Liste mit bereits vorhandenen Felder erstellen und mit den
 		// Standartfeldern fllen
 		final ArrayList<String> labels = new ArrayList<String>();
 		labels.addAll(defaultlabels);
 		final Dimension preferredSize = new Dimension(120, 20);
 		pnl_newmod.setLayout(new BorderLayout(0, 0));
 
 		JPanel pnl_bottom = new JPanel();
 		pnl_newmod.add(pnl_bottom, BorderLayout.SOUTH);
 
 		// Button zum erstellen eines neuen Feldes
 		JButton btnNeuesFeld = new JButton("Neues Feld");
 		btnNeuesFeld.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				String text = "Name des Feldes";
 				// Abfrage des Namen des Feldes
 				String name = JOptionPane.showInputDialog(frame, text);
 				try {
 					// Prfe, ob Name leer oder schon vorhanden ist
 					while (name.isEmpty() || labels.contains(name)) {
 						Object[] params = { "Bitte geben Sie eine gltige Bezeichnung ein!", text };
 						name = JOptionPane.showInputDialog(frame, params);
 					}
 					labels.add(name);
 					JPanel pnl_tmp = new JPanel();
 					modul_panel.add(pnl_tmp);
 					// Platzhalter
 					modul_panel.add(Box.createRigidArea(new Dimension(0, 5)));
 
 					// Abfrage der Anzahl an Panels, die bereits vorhanden sind
 					int numOfPanels = modul_panel.getComponentCount();
 					pnl_tmp.setLayout(new BoxLayout(pnl_tmp, BoxLayout.X_AXIS));
 
 					JLabel label_tmp = new JLabel(name);
 					label_tmp.setPreferredSize(preferredSize);
 					pnl_tmp.add(label_tmp);
 
 					JTextArea txt_tmp = new JTextArea();
 					txt_tmp.setLineWrap(true);
 					pnl_tmp.add(txt_tmp);
 
 					JCheckBox dez = new JCheckBox("Dezernat 2", false);
 					pnl_tmp.add(dez);
 
 					// Button, um das Feld wieder zu entfernen
 					JButton btn_tmp_entf = new JButton("Entfernen");
 					btn_tmp_entf.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							int id = buttonmap.get(e.getSource());
 							// Bezeichnung aus Liste entfernen
 							String name = ((JLabel) ((JPanel) modul_panel.getComponent(id)).getComponent(0)).getText();
 							labels.remove(name);
 
 							// Feld mit ID id von Panel entfernen
 							modul_panel.remove(id);
 							// Platzhalter entfernen
 							modul_panel.remove(id - 1);
 							// Aus ButtonMap entfernen
 							buttonmap.remove(e.getSource());
 
 							// ids der Buttons ndern, damit auch ein Feld aus
 							// der Mitte gelcht werden kann
 							HashMap<JButton, Integer> tmpmap = new HashMap<JButton, Integer>();
 							Iterator<Entry<JButton, Integer>> entries = buttonmap.entrySet().iterator();
 							while (entries.hasNext()) {
 								Entry<JButton, Integer> thisEntry = entries.next();
 								JButton key = thisEntry.getKey();
 								int value = thisEntry.getValue();
 								if (value > id) {
 									value = value - 2;
 								}
 								tmpmap.put(key, value);
 							}
 							buttonmap = tmpmap;
 							modul_panel.revalidate();
 
 						}
 					});
 
 					// Button btn_tmp_entf mit ID (numOfPanels-2) zu ButtonMap
 					// hinzufgen
 					buttonmap.put(btn_tmp_entf, numOfPanels - 2);
 
 					pnl_tmp.add(btn_tmp_entf);
 
 					modul_panel.revalidate();
 
 				} catch (NullPointerException npe) {
 					// nichts tuen bei Abbruch
 				}
 			}
 		});
 		pnl_bottom.add(btnNeuesFeld);
 
 		// Zurck zur Startseite
 		JButton btnHome = new JButton("Zur\u00fcck");
 		btnHome.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Card wieder erneuern und zur Startseite wechseln
				newmodulecard();
				modul_panel.revalidate();
				showCard("welcome page");
 			}
 		});
 		pnl_bottom.add(btnHome);
 
 		JScrollPane scrollPane = new JScrollPane(modul_panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		modul_panel.setLayout(new BoxLayout(modul_panel, BoxLayout.Y_AXIS));
 
 		// Panel Zuordnung + Platzhalter
 		JPanel pnl_Z = new JPanel();
 		pnl_Z.setLayout(new BoxLayout(pnl_Z, BoxLayout.X_AXIS));
 		JLabel label_MH = new JLabel("Zuordnung");
 
 		label_MH.setPreferredSize(preferredSize);
 		pnl_Z.add(label_MH);
 
 		// Liste mit ausgewhlten Zuordnungen
 		// final DefaultListModel<Zuordnung> lm = new
 		// DefaultListModel<Zuordnung>();
 		// final JList<Zuordnung> zlist = new JList<Zuordnung>(lm);
 		// zlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		//
 		// pnl_Z.add(zlist);
 		//
 		// // ComboBox mit Zuordnungen
 		// final JComboBox<Zuordnung> cb_Z = new
 		// JComboBox<Zuordnung>(cbmodel_Z);
 		// cb_Z.setMaximumSize(new Dimension(400, 20));
 		//
 		// pnl_Z.add(cb_Z);
 		//
 		// // Auswahl einer Zuordnung aus der ComboBox
 		// JButton z_btn = new JButton("Zuordnung ausw\u00e4hlen");
 		// z_btn.addActionListener(new ActionListener() {
 		// @Override
 		// public void actionPerformed(ActionEvent e) {
 		// if (!lm.contains(cb_Z.getSelectedItem()))
 		// lm.addElement((Zuordnung) cb_Z.getSelectedItem());
 		// }
 		// });
 		// pnl_Z.add(z_btn);
 		//
 		// // In der Liste ausgewhlte Zuordnung wieder entfernen
 		// JButton btnZuordnungEntfernen = new JButton("Zuordnung entfernen");
 		// btnZuordnungEntfernen.addActionListener(new ActionListener() {
 		// @Override
 		// public void actionPerformed(ActionEvent e) {
 		// int i = zlist.getSelectedIndex();
 		// if (i > -1) {
 		// lm.remove(i);
 		// }
 		// }
 		// });
 		// pnl_Z.add(btnZuordnungEntfernen);
 		//
 		// modul_panel.add(pnl_Z);
 		modul_panel.add(Box.createRigidArea(new Dimension(0, 5)));
 
 		// Alle Standartfelder, auer Zuordnung erzeugen
 		for (int i = 3; i < defaultlabels.size(); i++) {
 			modul_panel.add(defaultmodulPanel(defaultlabels.get(i), "", false));
 			modul_panel.add(Box.createRigidArea(new Dimension(0, 5)));
 		}
 
 		// Button zum Annehmen eines Modules
 		JButton btnOk = new JButton("Annehmen");
 		btnOk.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// ArrayList<Zuordnung> zlist = new ArrayList<Zuordnung>();
 				String jg = ((JTextArea) ((JPanel) modul_panel.getComponent(2)).getComponent(1)).getText();
 				int jahrgang;
 				try {
 					jahrgang = Integer.parseInt(jg);
 				} catch (NumberFormatException nfe) {
 					jahrgang = 0;
 				}
 				// for (int i = 0; i < lm.getSize(); i++) {
 				// zlist.add(lm.getElementAt(i));
 				// }
 
 				// Prfe, ob min. eine Zuordnung ausgewhlt und ein gltiger
 				// Jahrgang eingegeben wurde
 				// if (!zlist.isEmpty()) {
 				// if (jahrgang != 0) {
 				//
 				// String Name = ((JTextArea) ((JPanel)
 				// modul_panel.getComponent(4)).getComponent(1)).getText();
 				//
 				// if (Name.isEmpty()) {
 				// JOptionPane.showMessageDialog(frame,
 				// "Bitte geben Sie einen Namen ein!", "Eingabe Fehler",
 				// JOptionPane.ERROR_MESSAGE);
 				// } else {
 				//
 				// boolean filled = true;
 				// ArrayList<Feld> felder = new ArrayList<Feld>();
 				// // Eintraege der Reihe nach auslesen
 				// for (int i = 6; i < modul_panel.getComponentCount(); i = i +
 				// 2) {
 				// JPanel tmp = (JPanel) modul_panel.getComponent(i);
 				// JLabel tmplbl = (JLabel) tmp.getComponent(0);
 				// JTextArea tmptxt = (JTextArea) tmp.getComponent(1);
 				//
 				// boolean dezernat2 = ((JCheckBox)
 				// tmp.getComponent(2)).isSelected();
 				// String value = tmptxt.getText();
 				// String label = tmplbl.getText();
 				// // Prfe, ob alle Felder ausgefllt wurden
 				// if (value.isEmpty()) {
 				// filled = false;
 				// break;
 				// }
 				// felder.add(new Feld(label, value, dezernat2));
 				// }
 				// // Wenn alle aussgefllt wurden, neues Modul
 				// // erzeugen und bei Besttigung einreichen
 				// if (filled == true) {
 				// int version = serverConnection.getModulVersion(Name) + 1;
 				//
 				// Date d = new Date();
 				// ArrayList<String> user = new ArrayList<String>();
 				// user.add(current.geteMail());
 				// Modul neu = new Modul(Name, felder, version, d, 0, false,
 				// user, "");
 				// int n = JOptionPane.showConfirmDialog(frame,
 				// "Sind Sie sicher, dass Sie dieses Modul einreichen wollen?",
 				// "Besttigung", JOptionPane.YES_NO_OPTION);
 				// if (n == 0) {
 				// serverConnection.setModul(neu);
 				// labels.removeAll(labels);
 				// modul_panel.removeAll();
 				// modul_panel.revalidate();
 				// newmodulecard();
 				// showCard("newmodule");
 				// }
 				// } // Fehler, wenn nicht alle ausgefllt wurden
 				// else {
 				// JOptionPane.showMessageDialog(frame,
 				// "Bitte fllen Sie alle Felder aus!", "Eingabe Fehler",
 				// JOptionPane.ERROR_MESSAGE);
 				// }
 				// }
 				// } else {
 				// JOptionPane.showMessageDialog(frame,
 				// "Bitte geben Sie einen gltigen Wert fr den Jahrgang ein!",
 				// "Eingabe Fehler",
 				// JOptionPane.ERROR_MESSAGE);
 				// }
 				// } else {
 				// JOptionPane.showMessageDialog(frame,
 				// "Bitte whlen Sie min. einen Zuordnung aus!",
 				// "Eingabe Fehler",
 				// JOptionPane.ERROR_MESSAGE);
 				// }
 			}
 		});
 		pnl_bottom.add(btnOk);
 
 		pnl_newmod.add(scrollPane);
 		cards.add(pnl_newmod, "newmodule");
 
 		btnOk.setToolTipText("Klicken, um ihr Modul einzureichen.");
 		btnHome.setToolTipText("Klicken, um zurck in den Hauptbildschirm zu gelangen.");
 		btnNeuesFeld.setToolTipText("Klicken, um ein neues Feld in ihrem Modul zu erstellen.");
 
 	}
 }
