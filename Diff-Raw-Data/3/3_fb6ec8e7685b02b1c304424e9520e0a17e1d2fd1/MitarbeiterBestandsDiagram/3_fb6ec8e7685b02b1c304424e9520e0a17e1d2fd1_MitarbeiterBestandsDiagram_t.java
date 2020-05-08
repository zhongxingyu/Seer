 package eshop.local.ui.gui.comp.mitarbeiterMenue.bestandsDiagram;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Giacomo
  * Date: 19.06.13
  * Time: 16:06
  * To change this template use File | Settings | File Templates.
  */
 public class MitarbeiterBestandsDiagram extends JPanel {
 
     // OffsetWert fuer die X Achse
     private int offsetx;
     // OffsetWert fuer die Y Achse
     private int offsety;
     // Vector in dem die Positionen auf der X Achse eingefuegt werden
     private Vector<Integer> xAbstandswerte;
     // Vector in dem die Bestandsdaten eines Artikel gespeichert werden
     private Vector<Integer> yBestandswerte;
 
     /**
      * Konstruktor für das MitarbeiterBestandsDiagram
      */
     public MitarbeiterBestandsDiagram() {
 
 
         // erzeugt einen Vector fuer die Abstandswerte
         xAbstandswerte = new Vector<Integer>();
         // erzeugt den Vector fuer die Bestandwerte
         yBestandswerte = new Vector<Integer>();
 
     }
 
     /**
      * Methode zum zeichnen eines Bestandsgraphen
      *
      * @param g
      */
     protected void paintGraph(Graphics g) {
         super.paintComponent(g);
         // Werte der Bedingungen fuer die do while Schleife
         int arrayGroesse = yBestandswerte.size();
         int aktuellePosition = 0;
 
         do {
             if ((yBestandswerte.get(aktuellePosition) >= (this.getHeight() - 30)) || (yBestandswerte.get((aktuellePosition) + 1) >= (this.getHeight() - 30))) {
 
                 if ((yBestandswerte.get(aktuellePosition) < (this.getHeight() - 30)) && (yBestandswerte.get((aktuellePosition) + 1) >= (this.getHeight() - 30))) {
 
                     //  zeichnet die Linie zwischen dem jeweiligen Start & Endpunkt
                     g.drawLine(xAbstandswerte.get(aktuellePosition) + offsetx, offsety - yBestandswerte.get(aktuellePosition), xAbstandswerte.get((aktuellePosition) + 1) + offsetx, 10);
                     // jedes mal nach dem die Line gezeichnet wurde wird der Bestand als String an der aktuellen Position ausgegeben
                     g.drawString(yBestandswerte.get(aktuellePosition).toString(), xAbstandswerte.get(aktuellePosition) + offsetx, 10);
 
                 } else if ((yBestandswerte.get(aktuellePosition) >= (this.getHeight() - 30)) && (yBestandswerte.get((aktuellePosition) + 1) < (this.getHeight() - 30))) {
 
                     //  zeichnet die Linie zwischen dem jeweiligen Start & Endpunkt
                     g.drawLine(xAbstandswerte.get(aktuellePosition) + offsetx, 10, xAbstandswerte.get((aktuellePosition) + 1) + offsetx, offsety - yBestandswerte.get((aktuellePosition) + 1));
                     // jedes mal nach dem die Line gezeichnet wurde wird der Bestand als String an der aktuellen Position ausgegeben
                     g.drawString(yBestandswerte.get(aktuellePosition).toString(), xAbstandswerte.get(aktuellePosition) + offsetx,offsety - yBestandswerte.get(aktuellePosition));
 
                 } else if ((yBestandswerte.get(aktuellePosition) >= (this.getHeight() - 30)) && (yBestandswerte.get((aktuellePosition) + 1) >= (this.getHeight() - 30))) {
 
                     //  zeichnet die Linie zwischen dem jeweiligen Start & Endpunkt
                     g.drawLine(xAbstandswerte.get(aktuellePosition) + offsetx, 10, xAbstandswerte.get((aktuellePosition) + 1) + offsetx, 10);
                     // jedes mal nach dem die Line gezeichnet wurde wird der Bestand als String an der aktuellen Position ausgegeben
                     g.drawString(yBestandswerte.get(aktuellePosition).toString(), xAbstandswerte.get(aktuellePosition) + offsetx,10);
                 }
 
             }
 
             //  zeichnet die Linie zwischen dem jeweiligen Start & Endpunkt
             g.drawLine(xAbstandswerte.get(aktuellePosition) + offsetx, offsety - yBestandswerte.get(aktuellePosition), xAbstandswerte.get((aktuellePosition) + 1) + offsetx, offsety - yBestandswerte.get((aktuellePosition) + 1));
             // jedes mal nach dem die Line gezeichnet wurde wird der Bestand als String an der aktuellen Position ausgegeben
             g.drawString(yBestandswerte.get(aktuellePosition).toString(), xAbstandswerte.get(aktuellePosition) + offsetx, offsety - yBestandswerte.get(aktuellePosition));
 
             aktuellePosition++;
 
         } while (aktuellePosition < arrayGroesse - 1);
 
         // zeichnet den letzen Bestand in das Diagram
         g.drawString(yBestandswerte.get(aktuellePosition).toString(), xAbstandswerte.get(aktuellePosition) + offsetx, offsety - yBestandswerte.get(aktuellePosition));
        this.yBestandswerte.clear();
        this.xAbstandswerte.clear();
     }
 
     /**
      * Methode zum anzeigen eines Strings
      *
      * @param g
      */
     protected void paintBestand(Graphics g) {
         super.paintComponent(g);
         g.drawString("Ein Bestandsgraph kann nicht angezeigt werden da im gewählten Zeitraum nicht genügend Werte exestieren !", 50, 50);
 
 
     }
 
 
     /**
      * Methode zum zeichnen eines ArtikelBestandsgraphen
      *
      * @param yBestandVector
      */
     public void artikelBestandGraphenzeichnen(Vector<Integer> yBestandVector) {
         // Wenn ein Bestandsgraph angezeigt werden soll
         if (yBestandVector.size() >= 2) {
             // speicher die gesamtgroesse des Panels in der Variable d
             Dimension d = this.getSize();
             // Versatz auf der X Achse
             offsetx = 20;
             // Versatz auf der Y Achse
             offsety = (int) (d.getHeight() - 20);
             // speichert den uebergebenden yBestandsvektor in den lokalen Vektor yBestandswerte
             this.yBestandswerte = yBestandVector;
             // definiert je nach Anzahl der Werte den Abstand zwischen den einzelnen Punkten
             int abstand = (((int) d.getWidth() - 20) / yBestandVector.size());
             // fuellt den xAbstandswerte Vector
             for (int i = 0; i < yBestandswerte.size(); i++) {
                 this.xAbstandswerte.add(abstand * i);
             }
             paintGraph(this.getGraphics());
 
             // Wenn nicht genügend Bestände vorliegen um einen Bestandsgraphen anzuzeigen
         } else if (yBestandVector.size() <= 1) {
             paintBestand(this.getGraphics());
         }
     }
 
 
 }
