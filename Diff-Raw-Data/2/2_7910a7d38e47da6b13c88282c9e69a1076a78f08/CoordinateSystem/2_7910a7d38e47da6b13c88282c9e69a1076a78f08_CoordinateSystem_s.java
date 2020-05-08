 package de.codeforum.wedabecha.system;
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
 		Martin Müller (mrtnmueller at users.berlios.de),
 		Dominic Hopf (dmaphy at users.berlios.de),
 		Robert Exner (ashrak at users.berlios.de)
 
 */
 
 
 import java.awt.*;
 import javax.swing.*;
 
 import de.codeforum.wedabecha.wedabecha;
 
 
 import java.util.ArrayList;
 
 public class CoordinateSystem extends JComponent {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/*
 		auf geht's. heute zeichnen wir ein koordinatensystem.
 		dazu brauchen wir erstmal die start- und endpunkte
 		für die beiden achsen...
 	*/
 	private int startX, endX, startY, endY;
 
 	/*
 		für die einteilung der achsen müssen wir die maximalen werte kennen.
 		maxWert für die Y-Achse
 		maxDate für die x-Achse
 	*/
 	private double maxWert;
 //	private double maxDate;
 
 	private int dx, dy; // Länge der Achsen in px
 //	private int ddx, ddy; // Abstand der Achsen von Nullpunkt bis Maximalwert in px
 	private int abstandx = 25; // Abstand der einzelnen Achseneinteilungsstriche
 //	private int abstandy = 25; // Abstand der einzelnen Achseneinteilungsstriche
 
 	private int yBeschriftung;
 	private int startDateIndex = 0; // Startwert für den horizontalen Zeichenbereich
 	private int endDateIndex = 299; // Endwert für den horizontalen Zeichenbereich
 
 	// Konstruktor
 	public CoordinateSystem(){
 	
 	}
 	
 	
 	/**
 	 * setGroesse legt die Grösse für das Koordinatensystem
 	 * fest. Diese Methode wird von verschiedenen Punkten aufgerufen.
 	 * Einmal vom Konstruktor, beim erzeugen,
 	 * und einmal von dem ActionListener der hauptFensterUI,
 	 * welcher für die Grössenänderung des Fensters verantwortlich ist.
 	 * Die Werte, die wir für das System benötigen, können wir erst anhand
 	 * der Fenstergrösse berechnen...
 	 */
 	public void setGroesse(int breite, int hoehe){
 		
 		
 		// Start- und Endpunkt der Y-Achse in vertikaler Richtung
 		this.startY = hoehe - 25; // 25 px vom unteren Rand
 		this.endY = 60; // 60 px vom oberen Rand des layeredPane (35 px für die toolBar, 25 px ab toolBar)
 		// Start- und Endpunkt der X-Achse in horizontaler Richtung
 		this.startX = 25; // 25px vom linken Rand
 		this.endX = breite - 25; // 25 px vom rechten Rand
 
 		this.dx = this.endX - this.startX; // Breite
    		this.dy = this.endY - this.startY; // Hoehe
 		this.setSize(breite, hoehe);
 	} // setGroesse()
 
 
 	public void setStartDateIndex(int index){
 		this.startDateIndex = index;
 	}
 
 
 	public int getStartDateIndex(){
 		return this.startDateIndex;
 	}
 
 
 	public void setEndDateIndex(int index){
 		this.endDateIndex = index;
 	}
 
 
 	public int getEndDateIndex(){
 		return this.endDateIndex;
 	}
 
 
	protected void zeichnen() {
 		/**
 			die Methode zeichnen() rufen wir vom ActionListener für den
 			[OK] - Button in importiereTabelleUI auf.
 			das Koordinatensystem kann ja erst gezeichnet werden,
 			wenn wir die Werte aus den Tabellen kennen und maximalWerte etc.
 			berechnen können
 		*/
 
 		// Grösse der Zeichnungsfläche einstellen
 		this.berechneMaxima();
 
 		this.yBeschriftung = (int)((Math.round(this.maxWert / 10)) * 10);
 		this.setGroesse(690,452);
 		this.setVisible(true); // sichtbar machen
 	} // zeichneKoordinatensystem
 
 
 	protected void paintComponent(Graphics g){
 		/**
 			Mit paintComponent() zeichnen wir das letztendliche Koordinatensystem.
 			Erst Achsen, dann Pfeilspitzen für die Achsen,
 			dann noch die Einteilung...
 		*/
 
 		g.drawLine(startX, startY, endX, startY); // X-Achse
 		g.drawLine(startX, startY, startX, endY); // Y-Achse
 		// Pfeilspitze Y-Achse
 		g.drawLine(21, (endY + 8), 25, endY);
 		g.drawLine(29, (endY + 8), 25, endY);
 
 		// Pfeilspitze X-Achse
 		g.drawLine((endX - 8), (startY + 4), endX, startY);
 		g.drawLine((endX - 8), (startY - 4), endX, startY);
 
 		// Achseneinteilungsstriche und Beschriftung zeichnen
 
 		int tempBeschriftung = 0;
 
 		// auf der Y-Achse
 
 		for (int j = startY; j >= 75; j -= (startY - 75) / 10){
 			g.drawString(""+tempBeschriftung, 0, j + 5);
 			g.drawLine( (startX - 4) , j ,  (startX + 4) , j );
 			tempBeschriftung += yBeschriftung / 10;
 		} // for
 
 		// Striche auf der X-Achse
 		for (int i = startX; i < endX - 25; i+=abstandx ){
 			if ( (i % 100) == 0 ){
 				g.setColor(Color.BLUE);
 				g.drawLine( (startX + i) , (startY - 8) , (startX + i) , (startY + 4) );
 			} else {
 				g.setColor(Color.BLACK);
 				g.drawLine( (startX + i) , (startY - 4) , (startX + i) , (startY + 4) );
 			} // if
 
 		} // for
 
 		/**
 			Beschriftung der X-Achse
 			Diese Beschriftung orientiert sich immer an der zuerst eingelesenen Tabelle,
 			also der Tabelle bzw. Kurve mit der Nummer 1.
 		*/
 
 		ArrayList daten; // mehrzahl von datum
 		int xTextPos = 0; // horizontale position der beschriftung
 		daten = wedabecha.getCurve(1).getDaten();
 		
 		for (int i = startDateIndex; i < endDateIndex; i+= abstandx){
 			g.setColor(Color.BLACK);
 			String text[] = (String[])daten.get(i);
 			g.drawString(text[0] + "-" + text[1] + "-" + text[2],xTextPos,startY + 12);
 			xTextPos += 100;
 		} // for
 
 	} // paintComponent()
 
 
 
 	private void berechneMaxima(){
 		/**
 			hier berechnen wir die Maximalwerte,
 			die brauchen wir für die Einteilung der Achsen.
 		*/
 
 		ArrayList werte;
 		double wertZeile[];
 		double maxZeilenWerte[];
 		double maxKurvenWerte[] = new double[5];
 
 		/*
 			Maximalwerte der Kurven bestimmen
 			for(i) geht die Kurven nacheinander durch,
 			for(j) geht die Zeilen einer Kurve nacheinander durch
 		*/
 
 		for (int i = 1; i < 6;i++){
 			if (wedabecha.getCurve(i).isset()){
 				werte = wedabecha.getCurve(i).getWerte();
 				maxZeilenWerte = new double[werte.size()];
 
 				for (int j = 0; j < werte.size(); j++){
 					wertZeile = (double[])werte.get(j);
 					java.util.Arrays.sort(wertZeile);
 					// der letzte wert der aufsteigend sortierten liste, ist der maximale
 					maxZeilenWerte[j] = wertZeile[wertZeile.length - 1];
 				} // for(j)
 
 				java.util.Arrays.sort(maxZeilenWerte);
 				maxKurvenWerte[i] = maxZeilenWerte[maxZeilenWerte.length - 1];
 			} // if
 		} // for(i)
 
 		java.util.Arrays.sort(maxKurvenWerte);
 		this.maxWert = maxKurvenWerte[maxKurvenWerte.length - 1];
 
 	} // berechneMaxima()
 
 
 	public String toString() {
 		return	" Zeichenbereich:\n" +
 				" X-Achse: |" + this.startX + " - " + this.endX + "| = " + this.dx + "\n" +
 				" Y-Achse: |" + this.startY + " - " + this.endY + "| = " + this.dy + "\n";
 	}
 } // Koordinatensystem
