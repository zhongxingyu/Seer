 /****************************************************************************
  *   Copyright (C) 2004 by BTU SWP GROUP 04/6.1                             *
  *                                                                          *
  *   This program is free software; you can redistribute it and/or modify   *
  *   it under the terms of the GNU General Public License as published by   *
  *   the Free Software Foundation; either version 2 of the License, or	    *
  *   (at your option) any later version                                     *
  *                                                                          *
  *   This program is distributed in the hope that it will be useful,        *
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of         *
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
  *   GNU General Public License for more details                            *
  *                                                                          *
  *   You should have received a copy of the GNU General Public License      *
  *   along with this program; if not, write to the                          *
  *   Free Software Foundation, Inc.,                                        *
  *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.              *
  ***************************************************************************/
 
 package de.codeforum.wedabecha.ui.dialogs;
 
 // Imports
 import de.codeforum.wedabecha.*;
 
 import javax.swing.*;
 
 import de.codeforum.wedabecha.system.*;
 import de.codeforum.wedabecha.ui.MainWindow;
 
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * @author
  * Dominic Hopf (dmaphy at users.berlios.de),
  * Robert Exner (ashrak at users.berlios.de)
  */
 public class DataImportUI extends JDialog  {
 	/*
 		die bestandteile des dialogs erzeugen
 		ACHTUNG!!! anschauen des Codes kann zu epileptischen Anfällen führen!!!
 		daher nicht zu schnell durch den Code scrollen!!!
 	*/
 
 	private static final long serialVersionUID = 1L;
 
 	private JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 	private JPanel topPanel = new JPanel(new GridLayout(5,1));
 	private JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 
 	// objekte für oben
 	private JPanel LTzeile1 = new JPanel(new FlowLayout());
 	private JPanel LTzeile2 = new JPanel(new FlowLayout());
 	private JPanel LTzeile3 = new JPanel(new FlowLayout());
 	private JPanel LTzeile4 = new JPanel(new FlowLayout());
 	private JPanel LTzeile5 = new JPanel(new FlowLayout());
 
 	private JLabel tabelle1Label = new JLabel(Messages.getString("importiereTabelleUI.0")); //$NON-NLS-1$
 	private JLabel tabelle2Label = new JLabel(Messages.getString("importiereTabelleUI.1")); //$NON-NLS-1$
 	private JLabel tabelle3Label = new JLabel(Messages.getString("importiereTabelleUI.2")); //$NON-NLS-1$
 	private JLabel tabelle4Label = new JLabel(Messages.getString("importiereTabelleUI.3")); //$NON-NLS-1$
 	private JLabel tabelle5Label = new JLabel(Messages.getString("importiereTabelleUI.4")); //$NON-NLS-1$
 
 	protected static JButton oeffneTabelle1 = new JButton(Messages.getString("importiereTabelleUI.5")); //$NON-NLS-1$
 	protected static JButton oeffneTabelle2 = new JButton(Messages.getString("importiereTabelleUI.5")); //$NON-NLS-1$
 	protected static JButton oeffneTabelle3 = new JButton(Messages.getString("importiereTabelleUI.5")); //$NON-NLS-1$
 	protected static JButton oeffneTabelle4 = new JButton(Messages.getString("importiereTabelleUI.5")); //$NON-NLS-1$
 	protected static JButton oeffneTabelle5 = new JButton(Messages.getString("importiereTabelleUI.5")); //$NON-NLS-1$
 
 	private static JTextField pfadTabelle1 = new JTextField(20);
 	private static JTextField pfadTabelle2 = new JTextField(20);
 	private static JTextField pfadTabelle3 = new JTextField(20);
 	private static JTextField pfadTabelle4 = new JTextField(20);
 	private static JTextField pfadTabelle5 = new JTextField(20);
 
 	// mit einer Liste von Checkboxen kann leichter per schleife abgefragt werden
 	// welche nun gesetzt is und welche nich
 	protected static JCheckBox speicherTabelle[] = {
 		new JCheckBox(Messages.getString("importiereTabelleUI.6")), //$NON-NLS-1$
 		new JCheckBox(Messages.getString("importiereTabelleUI.6")), //$NON-NLS-1$
 		new JCheckBox(Messages.getString("importiereTabelleUI.6")), //$NON-NLS-1$
 		new JCheckBox(Messages.getString("importiereTabelleUI.6")), //$NON-NLS-1$
 		new JCheckBox(Messages.getString("importiereTabelleUI.6")) //$NON-NLS-1$
 	};
 
 	protected static JButton darstellungsTypButton1 = new JButton(Messages.getString("importiereTabelleUI.7")); //$NON-NLS-1$
 	protected static JButton darstellungsTypButton2 = new JButton(Messages.getString("importiereTabelleUI.7")); //$NON-NLS-1$
 	protected static JButton darstellungsTypButton3 = new JButton(Messages.getString("importiereTabelleUI.7")); //$NON-NLS-1$
 	protected static JButton darstellungsTypButton4 = new JButton(Messages.getString("importiereTabelleUI.7")); //$NON-NLS-1$
 	protected static JButton darstellungsTypButton5 = new JButton(Messages.getString("importiereTabelleUI.7")); //$NON-NLS-1$
 
 	// für fünf verschiedene Kurven brauchen wir fünf verschiedene Tabellen
 	protected static DataImport tabellen[] = {
 		new DataImport(),
 		new DataImport(),
 		new DataImport(),
 		new DataImport(),
 		new DataImport()
 	};
 
 
 	// objekte unten
 	private JButton okKnopf = new JButton(Messages.getString("importiereTabelleUI.8")); //$NON-NLS-1$
 	private JButton abbrechenKnopf = new JButton(Messages.getString("importiereTabelleUI.9")); //$NON-NLS-1$
 
 
 	// konstruktor
 	/**
 	 * Konstruktor, erwartet zur Zeit keine Parameter
 	 */
 	public DataImportUI(){
 		this.pack();
 	} // importiereTabelleUI()
 
 	
 	/**
 	 * Setzt den Dialog zusammen.
 	 */
 	@Override
 	public void pack(){
 		// pack() setzt das dialogfeld zusammen
 		// zuerst die grundstruktur
 		this.mainPanel.add(this.topPanel);
 		this.mainPanel.add(this.bottomPanel);
 		this.getContentPane().add(this.mainPanel);
 
 		// das Panel links oben, enthaelt die meisten Objekte
 		// zeile 1
 		this.topPanel.add(this.LTzeile1);
 			this.LTzeile1.add(this.tabelle1Label);
 			this.LTzeile1.add(oeffneTabelle1);
 				oeffneTabelle1.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent event){
 						showSubDialog(1);
 
 					} // actionPerformed(ActionEvent event)
 				});
 			this.LTzeile1.add(pfadTabelle1);
 				pfadTabelle1.setEnabled(false);
 			this.LTzeile1.add(speicherTabelle[0]);
 			this.LTzeile1.add(darstellungsTypButton1);
 				darstellungsTypButton1.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent event){
 						showDarstellungsDialog(1);
 					}
 				});
 		// zeile 2
 		this.topPanel.add(this.LTzeile2);
 			this.LTzeile2.add(this.tabelle2Label);
 			this.LTzeile2.add(oeffneTabelle2);
 				oeffneTabelle2.setEnabled(false);
 				oeffneTabelle2.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent event){
 						showSubDialog(2);
 					} // actionPerformed(ActionEvent event)
 				});
 			this.LTzeile2.add(pfadTabelle2);
 				pfadTabelle2.setEnabled(false);
 			this.LTzeile2.add(speicherTabelle[1]);
 				speicherTabelle[1].setEnabled(false);
 			this.LTzeile2.add(darstellungsTypButton2);
 				darstellungsTypButton2.setEnabled(false);
 				darstellungsTypButton2.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent event){
 						showDarstellungsDialog(2);
 					}
 				});
 		// zeile 3
 		this.topPanel.add(this.LTzeile3);
 			this.LTzeile3.add(this.tabelle3Label);
 			this.LTzeile3.add(oeffneTabelle3);
 				oeffneTabelle3.setEnabled(false);
 				oeffneTabelle3.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent event){
 						showSubDialog(3);
 					} // actionPerformed(ActionEvent event)
 				});
 			this.LTzeile3.add(pfadTabelle3);
 				pfadTabelle3.setEnabled(false);
 			this.LTzeile3.add(speicherTabelle[2]);
 				speicherTabelle[2].setEnabled(false);
 			this.LTzeile3.add(darstellungsTypButton3);
 				darstellungsTypButton3.setEnabled(false);
 				darstellungsTypButton3.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent event){
 						showDarstellungsDialog(3);
 					}
 				});
 		// zeile 4
 		this.topPanel.add(this.LTzeile4);
 			this.LTzeile4.add(this.tabelle4Label);
 			this.LTzeile4.add(oeffneTabelle4);
 				oeffneTabelle4.setEnabled(false);
 				oeffneTabelle4.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent event){
 						showSubDialog(4);
 					} // actionPerformed(ActionEvent event)
 				});
 			this.LTzeile4.add(pfadTabelle4);
 				pfadTabelle4.setEnabled(false);
 			this.LTzeile4.add(speicherTabelle[3]);
 				speicherTabelle[3].setEnabled(false);
 			this.LTzeile4.add(darstellungsTypButton4);
 				darstellungsTypButton4.setEnabled(false);
 				darstellungsTypButton4.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent event){
 						showDarstellungsDialog(4);
 					}
 				});
 		// zeile 5
 		this.topPanel.add(this.LTzeile5);
 			this.LTzeile5.add(this.tabelle5Label);
 			this.LTzeile5.add(oeffneTabelle5);
 				oeffneTabelle5.setEnabled(false);
 				oeffneTabelle5.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent event){
 						showSubDialog(5);
 					} // actionPerformed(ActionEvent event)
 				});
 			this.LTzeile5.add(pfadTabelle5);
 				pfadTabelle5.setEnabled(false);
 			this.LTzeile5.add(speicherTabelle[4]);
 				speicherTabelle[4].setEnabled(false);
 			this.LTzeile5.add(darstellungsTypButton5);
 				darstellungsTypButton5.setEnabled(false);
 				darstellungsTypButton5.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent event){
 						showDarstellungsDialog(5);
 					}
 				});
 
 		// das Panel unten nur die Buttons für Ok und Abbrechen
 		this.bottomPanel.add(this.okKnopf);
 			this.okKnopf.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent event){
 					int datenLaengen[] = new int[5];
 					for(int i = 1; i <= 5; i++){
 					    /* beim klick auf [OK] alle importierten Tabellen als
 						 * Kurve zeichnen und die erste Kurve sichtbar machen
 						 */
 
 						if(wedabecha.getCurve(i).isset()){
 							wedabecha.getCurve(i).draw();
 							MainWindow.getToolBar().setKurve1Button();
 							datenLaengen[i] = wedabecha.getCurve(i).getDates().size();
 						} // if
 
 						if (speicherTabelle[i - 1].isSelected()){
 							String name[] = tabellen[i - 1].getImportName().split("\\."); //$NON-NLS-1$
 							WedaFile.writeFile("../daten/" + name[0] + ".weda",i); //$NON-NLS-1$ //$NON-NLS-2$
 						} // if
 					} // for
 
 					java.util.Arrays.sort(datenLaengen);
 					MainWindow.setMaxDate(datenLaengen[4]);
 
 					// hier muss das Koordinatensystem aufgerufen und gezeichnet werden
					MainWindow.getCoords().draw();
 					setVisible(false);
 				} //  actionPerformed(ActionEvent event)
 			});
 
 		this.bottomPanel.add(this.abbrechenKnopf);
 			this.abbrechenKnopf.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent event){
 					setVisible(false);
 				} // actionPerformed(ActionEvent event)
 			});
 
 
 		/*
 			standard zum erzeugen und positionieren des dialogs
 		*/
 		int bildSchirmBreite = getToolkit().getScreenSize().width;
 		int bildSchirmHoehe = getToolkit().getScreenSize().height;
 		int Xposition = (bildSchirmBreite - 600) / 2;
 		int Yposition = (bildSchirmHoehe - 280) / 2;
 		setSize(600,280);
 		setLocation(Xposition,Yposition);
 		setResizable(false);
 		setModal(true);
 		setTitle(Messages.getString("importiereTabelleUI.10")); //$NON-NLS-1$
 		setVisible(true);
 	} // pack()
 
 
 	private void showSubDialog(int tabellenNummer) {
 		new subImportDialogUI(tabellenNummer);
 	} // showSubDialog
 
 
 	private void showDarstellungsDialog(int tabellenNummer){
 		new CurveType(tabellenNummer);
 	}
 
 	/**
 	 * setzt den Text der Textfelder im Dialog DataImport.
 	 * Die Funktion wird von subImportDialogUI aufgerufen,
 	 * sobald dieser mit OK geschlossen wurde.
 	 * 
 	 * @param path Der Pfad, der in das Textfeld geschrieben werden soll.
 	 * @param nr Die Nummer des Textfeldes, entspricht der ID der Kurve/Tabelle.   
 	 */
 	public static void setPath(String path, int nr){
 		switch (nr){
 			case 1: pfadTabelle1.setText(path); break;
 			case 2: pfadTabelle2.setText(path); break;
 			case 3: pfadTabelle3.setText(path); break;
 			case 4: pfadTabelle4.setText(path); break;
 			case 5: pfadTabelle5.setText(path); break;
 		} // switch
 	} // setPfad()
 
 } // class DataImport
