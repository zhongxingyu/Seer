 package de.codeforum.wedabecha.ui.dialogs;
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
 
 
 
 import javax.swing.*;
 
 import de.codeforum.wedabecha.wedabecha;
 import de.codeforum.wedabecha.system.DataImport;
 import de.codeforum.wedabecha.ui.MainWindow;
 
 
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * @author
  * 		Dominic Hopf (dmaphy at users.berlios.de),<br />
  * 		Robert Exner (ashrak at users.berlios.de)<br />
  * <br />
  * TODO: externalize Strings
  */
 public class subImportDialogUI extends JDialog {
 	private static final long serialVersionUID = 1L;
 	
 	private int tabellenNummer;
 	private JTextField pathField = new JTextField(30);
 
 	// Konstruktor
 	/**
 	 * @param tableNumber Nummer der Tabelle die hinzugefügt werden soll.
 	 */
 	public subImportDialogUI(int tableNumber){
 		/**
 			der konstruktor erwartet als parameter die tabellenNummer,
 			damit bestimmt werden kann, für welche Tabelle (1-5)
 			die eigenschaften festgelegt werden
 		*/
 
 		setTitle("Tabelle " + tableNumber + " f\u00fcr den Import vorbereiten - wedabecha");
 		this.tabellenNummer = tableNumber;
 		this.pack();
 	} // subImportDialogUI()
 	
 	protected int getTableNumber() {
 		return this.tabellenNummer;
 	}
 	
 	/**
 	 * Den Dialog zusammensetzen
 	 */
 	@Override
 	public void pack(){
 		final JPanel topPanel = new JPanel(new GridLayout(3,2));
 		final JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		final JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 
 		final JPanel label1Panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		final JPanel label2Panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		final JPanel label3Panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 
 		final JPanel edit1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		final JPanel edit2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		final JPanel edit3Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
 		final JLabel lokalLabel = new JLabel("lokale Datei");
 		final JLabel datumsFormatLabel = new JLabel("DatumsFormat");
 		final JLabel trennzeichenLabel = new JLabel("Trennzeichen");
 	
 	
		final JComboBox trennzeichenBox = new JComboBox(DataImport.getSeparators());
 	
 		final JButton durchsuchenKnopf = new JButton("Durchsuchen ...");
 		final JButton datumsFormatKnopf = new JButton("Datum"); // die beschriftung muss dynamisch anngepasst werden
 	
 		final JLabel pathLabel = new JLabel("Pfad:");
 		// pathField ist Klassenvariable
 	
 		final JButton okayButton = new JButton("OK");
 		okayButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent event){
 				//System.out.println( event.getActionCommand());
 
 				DataImportUI.setPath(
					DataImportUI.tabellen[getTableNumber() - 1].getImportPath(),
 					getTableNumber()
 				);
 
				DataImportUI.tabellen[getTableNumber() - 1].setSeparatorIndex(
 					trennzeichenBox.getSelectedIndex()
 				);
 
 				wedabecha.getCurve(getTableNumber()).setValues(
 					DataImportUI.tabellen[getTableNumber() - 1].getWerte()
 				);
 
 				wedabecha.getCurve(getTableNumber()).setValues(
 					DataImportUI.tabellen[getTableNumber() - 1].getDaten()
 				);
 
 				wedabecha.getCurve(getTableNumber()).setExists(true);
 				// Button zur jeweiligen eingelesenen Kurve anzeigen
 				MainWindow.getToolBar().kurveWaehlen(getTableNumber(), true);
 				MainWindow.getMainMenuBar().setCurveEditable(getTableNumber(), true);
 
 				if (wedabecha.getCurve(getTableNumber()).isset()){
 					switch(getTableNumber()){
 						case 1:	DataImportUI.oeffneTabelle2.setEnabled(true);
 								DataImportUI.speicherTabelle[1].setEnabled(true);
 								DataImportUI.darstellungsTypButton2.setEnabled(true);
 								break;
 						case 2:	DataImportUI.oeffneTabelle3.setEnabled(true);
 								DataImportUI.speicherTabelle[2].setEnabled(true);
 								DataImportUI.darstellungsTypButton3.setEnabled(true);
 								break;
 						case 3:	DataImportUI.oeffneTabelle4.setEnabled(true);
 								DataImportUI.speicherTabelle[3].setEnabled(true);
 								DataImportUI.darstellungsTypButton4.setEnabled(true);
 								break;
 						case 4:	DataImportUI.oeffneTabelle5.setEnabled(true);
 								DataImportUI.speicherTabelle[4].setEnabled(true);
 								DataImportUI.darstellungsTypButton5.setEnabled(true);
 								break;
 					}
 				}
 				
 				
 				// entkäfern
 //					System.out.println(importiereTabelle.zurZeichenKette());
 
 				setVisible(false);
 			}
 		});
 		bottomPanel.add(okayButton);
 
 		
 		
 		final JButton cancelButton = new JButton("Abbrechen");
 		cancelButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent event){
 				wedabecha.getCurve(getTableNumber()).setExists(false);
 				MainWindow.getMainMenuBar().setCurveEditable(getTableNumber(), false);
 				setVisible(false);
 			}
 		});
 		bottomPanel.add(cancelButton);
 
 		this.getContentPane().setLayout(new FlowLayout());
 		this.getContentPane().add(topPanel);
 		this.getContentPane().add(middlePanel);
 		this.getContentPane().add(bottomPanel);
 
 		topPanel.add(label1Panel);
 			label1Panel.add(lokalLabel);
 		topPanel.add(edit1Panel);
 			edit1Panel.add(durchsuchenKnopf);
 				durchsuchenKnopf.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent event){
 						 chooseFile(getTableNumber());
 					}
 				});
 
 		topPanel.add(label2Panel);
 			label2Panel.add(datumsFormatLabel);
 		topPanel.add(edit2Panel);
 			edit2Panel.add(datumsFormatKnopf);
 				datumsFormatKnopf.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent event){
 						new definiereDatumUI(getTableNumber());
 					}
 				});
 
 		topPanel.add(label3Panel);
 			label3Panel.add(trennzeichenLabel);
 		topPanel.add(edit3Panel);
 			edit3Panel.add(trennzeichenBox);
 				trennzeichenBox.setEditable(false);
 
 		middlePanel.add(pathLabel);
 		middlePanel.add(this.pathField);
 		
 		/*
 			standard zum erzeugen und positionieren des dialogs
 		*/
 		int bildSchirmBreite = getToolkit().getScreenSize().width;
 		int bildSchirmHoehe = getToolkit().getScreenSize().height;
 		int Xposition = (bildSchirmBreite - 600) / 2;
 		int Yposition = (bildSchirmHoehe - 280) / 2;
 		setSize(400,225);
 		setLocation(Xposition,Yposition);
 		setResizable(false);
 		setModal(true);
 		setVisible(true);
 	} // pack()
 
 	/**
 	 * erzeugt einen Dialog zum auswählen der Datei, die importiert werden soll. 
 	 * @param tableNumber Nummer der Tabelle, die gesetzt werden soll.
 	 */
 	public void chooseFile(int tableNumber){
 		JFileChooser auswahlDialog = new JFileChooser();
     	int returnVal = auswahlDialog.showOpenDialog(this);
     	if(returnVal == JFileChooser.APPROVE_OPTION) {
 			this.pathField.setText(auswahlDialog.getSelectedFile().getPath());
			DataImportUI.tabellen[tableNumber - 1].setImportPath(
 				auswahlDialog.getSelectedFile().getPath()
 			);
 
 			DataImportUI.tabellen[tableNumber - 1].setImportName(
 				auswahlDialog.getSelectedFile().getName()
 			);
 		} // fi
 
 	} // chooseFile()
 
 
 	/*public static void main(String args[]){
 		new subImportDialogUI(1);
 	}*/
 
 } // subImportiereTabelleUI
