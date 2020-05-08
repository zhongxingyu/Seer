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
 
/**
	@author
		Martin Müller (mrtnmueller at users.berlios.de)
*/
 
 
 //Diese Klasse dient zum Aufruf des "Speichern"-Dialoges
 
 
 
 import java.io.File;
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 
import de.codeforum.wedabecha.wedabecha.system.toWeda;

 
 public class DataExport {
 	public DataExport () {
 
 			//Tabellennummer abfragen
 			String input = JOptionPane.showInputDialog("Welche Tabelle exportieren (1-5) ?" );
 
 			try{
 				//wandelt den Eingabestring in eine int-Zahl um
 				int tablenumber = Integer.parseInt( input );
 
 				//Abfrage, ob eine gültige Tabellennummer eingegeben wurde
 				if (tablenumber > 0 & tablenumber < 6) {
 
 					JFileChooser fc = new JFileChooser();
 
 					//Dialog-Typ und Titel setzen
 					fc.setDialogTitle("Tabellendaten exportieren");
 					fc.setDialogType(JFileChooser.SAVE_DIALOG);
 
 					//erzeugt einen Filefilter, der dazu dient, nur
 					//Dateien mit der Endung .weda und .csv anzuzeogen
 					fc.setFileFilter( new FileFilter() {
 
 						public boolean accept(File f) {
 							return f.isDirectory() ||
 							f.getName().toLowerCase().endsWith(".weda") ||
 							f.getName().toLowerCase().endsWith(".csv");
 						}//accept
 
 						//die Beschreibung für die Dropdown-Liste im Dialog
 						public String getDescription() {
 							return "Tabellendateien (*.weda, *.csv)";
 						}
 					} );
 
 				int returnVal = fc.showSaveDialog( null );
 
 				if ( returnVal == JFileChooser.APPROVE_OPTION ) {
 					// Rückgabe der gewählten Datei als "file"
 					File file = fc.getSelectedFile();
 					System.out.println( file.getName() );
 
 					//ruft writeFile in toWeda mit dem ausgewählten
 					//Dateinamen- u. Pfad sowie der abgefragten
 					//Tabellennummer auf
					toWeda.writeFile(file.getAbsolutePath(), tablenumber);
 				}
 
 				//falls auf Abbrechen geklickt wurde...
 				else {
 					System.out.println( "Auswahl abgebrochen" );
 				} // fi
 
 				fc.setVisible(false); // is klar, ne
 			}
 
 			//falls keine gültige Tabellennummer eingegeben wurde
 			else {
 				JOptionPane.showMessageDialog( null, "Die Tabellennummer war falsch !" );
 			}//if
 
		}catch
 				//fängt die Exeption ab, wenn auf abbrechen gedrückt wurde
 				(NumberFormatException except){
 				JOptionPane.showMessageDialog
 					(null,"Auswahl abgebrochen !","Fehler",JOptionPane.ERROR_MESSAGE );
 			}//catch
 
 	}//exportiereTabelleUI()
 
 }//class exportiereTabelleUI
