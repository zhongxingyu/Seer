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
 
 import de.codeforum.wedabecha.system.DataImport;
 
 
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * 
  * 	@author
  * 		Dominic Hopf (dmaphy at users.berlios.de),
  * 		Robert Exner (ashrak at users.berlios.de)
  * 
  * Dialog to get the Dateformat from the User
  * TODO: externalize Strings
  */
 class definiereDatumUI extends JDialog {
 	final static long serialVersionUID = 1;
 	
 	// anhand der Tabellennummer wird die Tabelle identifiziert, für welche hier das Datumsformat festgelegt wird.
 	private int tabellenNummer;
 
 	// Konstruktor
 	/**
 	 * @param tableNumber die Tabellennummer
 	 */
 	public definiereDatumUI(int tableNumber){
 		this.tabellenNummer = tableNumber;
 		this.pack();
 	} // definiereDatumUI
 	
 	private int getTableNumber() {
 		return this.tabellenNummer;
 	}
 	
 	
 	/**
 	 * setzt das Dialogfeld zusammen
 	 */
 	@Override
 	public void pack(){
 		/**
 			pack() setzt das Dialogfeld aus den Bestandteilen zusammen
 		*/
 		this.getContentPane().setLayout(new FlowLayout());
 
 		//	Elemente für die erste zeile
 		final JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
 		final JLabel spaltePreLabel = new JLabel("Datum steht in der ");
 		final String spalten[] = {"ersten","letzten"};
 		final JComboBox spalteCombo = new JComboBox(spalten);
 		final JLabel spalteSufLabel = new JLabel(" Spalte der Datei in Form");
 		
 		// zeile1 zusammensetzen
 		panel1.add(spaltePreLabel);
 		panel1.add(spalteCombo);
 		panel1.add(spalteSufLabel);
 		
 		// Elemente für zweite zeile
 		final JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
 		final JRadioButton inkRB = new JRadioButton("einer inkrementierenden Zahl, welche",true);
 		final String inkRepraesentiert[] = {"einen Tag","eine Woche","einen Monat","ein Jahr"};
 		final JComboBox inkZahlCombo = new JComboBox(inkRepraesentiert);
 		final JLabel repraesentLabel = new JLabel("repr\u00e4sentiert.");
 
 		// zeile2 zusammensetzen
 		panel2.add(inkRB);
 		panel2.add(inkZahlCombo);
 			inkZahlCombo.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent event){
 					inkRB.setSelected(true);
 				}
 			});
 		panel2.add(repraesentLabel);
 		
 		// Elemente für Zeile 3
 		final JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		final JLabel startDatumLabel = new JLabel("Das Startdatum ist (YYYY-MM-DD)");
 		final JTextField startYearField = new JTextField(3);
 		final JTextField startMonthField = new JTextField(2);
 		final JTextField startDayField = new JTextField(2);
 		final JLabel startTrennStrich1 = new JLabel("-");
 		final JLabel startTrennStrich2 = new JLabel("-");
 
 		// Zeile 3 zusammensetzen
 		panel3.add(startDatumLabel);
 		panel3.add(startYearField);
 		panel3.add(startTrennStrich1);
 		panel3.add(startMonthField);
 		panel3.add(startTrennStrich2);
 		panel3.add(startDayField);
 		
 		// Elemente für Zeile 4
 		final JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		final JPanel buttonPanel = new JPanel(new FlowLayout());
 		final JRadioButton konkretRB = new JRadioButton("eines anderen konkreten Datumsformates :");
 		final JComboBox datumCombo = new JComboBox(DataImport.getDatenFormate());
 		
 		// Zeile 4 zusammensetzen
 		panel4.add(konkretRB);
 		panel4.add(datumCombo);
 			datumCombo.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent event){
 					konkretRB.setSelected(true);
 				}
 			});
 
 		
 		final JPanel topPanel = new JPanel(new GridLayout(4,1));
 		topPanel.add(panel1);
 		topPanel.add(panel2);
 		topPanel.add(panel3);
 		topPanel.add(panel4);
 		
 		this.getContentPane().add(topPanel);
 
 		// radiobuttons in einer gruppe
 		final ButtonGroup gruppeRB = new ButtonGroup();
 		gruppeRB.add(inkRB);
 		gruppeRB.add(konkretRB);
 			
 		this.getContentPane().add(buttonPanel);		
 
 		// buttons für den dialog
 		
 		JButton okayButton = new JButton("OK");
 		okayButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent event){
 				if (spalteCombo.getSelectedIndex() == 0){
 					// wenn das datum in der ersten spalte der ascii-datei steht
					DataImport.tabellen[getTableNumber() - 1].setDatumsPosFirstColumn(true);
 				} else {
 					//wenn das datum in der letzten spalte der ascii-datei steht
					DataImport.tabellen[getTableNumber() - 1].setDatumsPosFirstColumn(false);
 				} // if() else
 
 				if(inkRB.isSelected()){
					DataImport.tabellen[getTableNumber() - 1].setInkZahlRep(
 						inkZahlCombo.getSelectedItem().toString()
 					);
 				} else if (konkretRB.isSelected())  {
					DataImport.tabellen[getTableNumber() - 1].setDatumsFormatIndex(
 						datumCombo.getSelectedIndex()
 					);
 				} // if() else
 
 				setVisible(false);
 			} // actionPerformed()
 		});
 
 		buttonPanel.add(okayButton);
 		
 		JButton cancelButton = new JButton("Abbrechen");
 		cancelButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent event){
 				setVisible(false);
 			}
 		});
 				
 		buttonPanel.add(cancelButton);
 				
 		/*
 			standard zum erzeugen und positionieren des dialogs
 		*/
 		int bildSchirmBreite = getToolkit().getScreenSize().width;
 		int bildSchirmHoehe = getToolkit().getScreenSize().height;
 		int Xposition = (bildSchirmBreite - 500) / 2;
 		int Yposition = (bildSchirmHoehe - 220) / 2;
 		setSize(500,220);
 		setLocation(Xposition,Yposition);
 		setResizable(false);
 		setModal(true);
 		setTitle("Format des Datums f\u00fcr den Tabellenimport festlegen - wedabecha");
 		setVisible(true);
 	} // pack()
 
 /*
 	//main methode zu debugging-zwecken
 
 	public static void main(String args[]){
 		new definiereDatumUI(1);
 	} // main(String args[])
 */
 
 } // definiereDatumUI
