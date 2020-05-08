 package de.codeforum.wedabecha.system.draw;
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
 
 import java.awt.*;
 import javax.swing.JComponent;
 
 import de.codeforum.wedabecha.ui.MainWindow;
 
 
 import java.util.ArrayList;
 
 /**
  * @author
  * Matthias Tylkowski (micron at users.berlios.de)
  */
 public class LineCurve extends JComponent {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private ArrayList werte;
 	private Color farbe;
 	private int abstand;
 
 	public int dateBeginIndex = 0;
 	public int dateEndIndex = 299;
 
 	// zur Berechnung der neuen Werte bei Grösseenänderung des Fensters
 	private double multiplikator;
 	// höchster Wert in der ArrayList werte
 	private double max;
 
 	private int breite = 700;
 
 	private ArrayList ausgangsWerte; // unveränderte Werte vom Import
 	
 	/**
 	 * 
 	 * @param werte
 	 * @param farbe
 	 * @param ausgangsWerte
 	 */
 	public LineCurve (ArrayList werte, Color farbe, ArrayList ausgangsWerte) {
 		this.farbe = farbe;
 		this.werte = werte;
 		this.setSize(MainWindow.getWindowWidth() - 30, MainWindow.getWindowHeight());
 		this.ausgangsWerte = ausgangsWerte;
 		this.setVisible(false);
 	} // zeichneKurve()
 
 
 	public void setGroesse(int breite, int hoehe){
 		this.breite = breite;
 		this.setSize(breite, hoehe);
 	} // setGroesse()
 
 
	protected void setVisibility(boolean sichtbar){
 		this.setVisible(sichtbar);
 	} // setVisibility()
 
 	/* diese Methode berechnet den höchsten wert in der ArrayList, damit
 	 * Kurve immer die volle Fensterhöhe ausnutzt
 	 */
 
 	protected void getMax(){
 		for(int i = 0; i < this.werte.size(); i++){
 			double tempArray = ((Double)this.werte.get(i)).doubleValue();
 			this.max = Math.max(this.max, tempArray);
  		} // for
 		this.multiplikator =	(MainWindow.layeredPane.getHeight() - 100) /
 								this.max;
 	} // getMax()
 
 
 	public void paintComponent(Graphics kurve){
 		getMax();
 		int zaehler = 25;
 
 		this.abstand = (2 * this.ausgangsWerte.size() * this.breite / 700)  / this.werte.size();
 
 		for(int i = dateBeginIndex / (this.abstand / 2); i < dateEndIndex / (this.abstand / 2); i ++){
 			kurve.setColor(this.farbe);
 			kurve.drawLine(	zaehler,
 							(MainWindow.layeredPane.getHeight() -
 							25) - (int)(this.multiplikator *
 								((Double)this.werte.get(i)).doubleValue()),
 							zaehler += this.abstand,
 							(MainWindow.layeredPane.getHeight() -
 							25) - (int)(this.multiplikator *
 								((Double)this.werte.get(i+1)).doubleValue())
 			); // drawLine
 		} // for
 	} // paintComponent()
 } // zeichneLinienKurve
