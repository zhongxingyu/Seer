 /*
  *  Copyright 2008, 2009, 2010, 2011:
  *   Tobias Fleig (tfg[AT]online[DOT]de),
  *   Michael Haas (mekhar[AT]gmx[DOT]de),
  *   Johannes Kattinger (johanneskattinger[AT]gmx[DOT]de)
  *
  *  - All rights reserved -
  *
  *
  *  This file is part of Centuries of Rage.
  *
  *  Centuries of Rage is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Centuries of Rage is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Centuries of Rage.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package de._13ducks.cor.game.server.movement;
 
 import de._13ducks.cor.game.Moveable;
 import de._13ducks.cor.game.SimplePosition;
 import de._13ducks.cor.game.Unit;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import org.newdawn.slick.Color;
 
 /**
  * Ein freies Vieleck. Ein Teil des Movement-Map-Graphen
  */
 public class FreePolygon {
 
     /**
      * Eine Liste mit allen Nodes, die auf einer Kante dieses Polygons liegen oder die eine Ecke darstellen.
      */
     private LinkedList<Node> myNodes;
     /**
      * Die Farbe dieses Polygons, nur für Debug-Output
      */
     private Color color;
     /**
      * Die bekannten Nachbarn dieses Polygons
      */
     private List<FreePolygon> neighbors;
     /**
      * Diese Liste enthält immer alle Einheiten,
      * die sich gerade auf dem Feld befinden.
      */
     private List<Moveable> residents;
 
     /**
      * Erzeugt einen neues Vieleck mit den angegebenen Knoten als Eckpunkten.
      * Testet NICHT, ob das Vieleck auch konvex ist (muss es normalerweise sein)
      * Wirft eine Exception, wenn Parameter null sind oder weniger als 3 geliefert werden.
      * Registriert sich NICHT als Nachbar! (auch nicht, wenn registerNodes true ist!)
      * @param registerNodes Ob dieses neue Polygon bei seinen Nodes registriert werden soll.
      * @param nodes beliebig viele Nodes, mindestens 3
      */
     public FreePolygon(boolean registerNodes, Node... nodes) {
         if (nodes == null || nodes.length < 3) {
             throw new IllegalArgumentException("At least three nodes requried!");
         }
         myNodes = new LinkedList<Node>();
         neighbors = new ArrayList<FreePolygon>();
         residents = new ArrayList<Moveable>();
         myNodes.addAll(Arrays.asList(nodes));
         if (registerNodes) {
             registerNodes();
         }
 
         color = new Color((int) (Math.random() * 265.0), (int) (Math.random() * 265.0), (int) (Math.random() * 265.0), 100);
     }
 
     /**
      * Liefert einen Temporären Polygon, der die Verbindung dieses mit dem gegebenen Polygon darstellt.
      * Funktioniert nur, wenn beiden beiden Polygone genau eine gemeinsame Kante (aufgespannt von mindestens 2 gemeinsamen Nodes)
      * haben. Die Resultate in Fällen von mehreren gemeinsamen , aber nicht direkt zusammenhängenden Kanten sind undefiniert.
      * @param poly1 Polygon 1
      * @param poly2 Polygon 2
      * @return einen tempörären Polygon, der gemerged ist.
      */
     public static FreePolygon getMergedCopy(FreePolygon poly1, FreePolygon poly2) {
         // Alternativer Algorithmus mit Kanten:
         // Kanten-Netz bauen
         ArrayList<Edge> edges = new ArrayList<Edge>();
 
         for (int i = 0; i < poly1.myNodes.size(); i++) {
             edges.add(new Edge(poly1.myNodes.get(i), poly1.myNodes.get(i + 1 < poly1.myNodes.size() ? i + 1 : 0)));
         }
 
         // Bei Adden alle doppelten Kanten löschen
         for (int i = 0; i < poly2.myNodes.size(); i++) {
             Edge edge = (new Edge(poly2.myNodes.get(i), poly2.myNodes.get(i + 1 < poly2.myNodes.size() ? i + 1 : 0)));
             if (edges.contains(edge)) {
                 edges.remove(edge);
             } else {
                 edges.add(edge);
             }
         }
 
         // Jetzt nur noch einen neuen "Weg" bauen. Einfach irgendwo anfangen, es ist eindeutig
         Edge current = edges.get(0);
         Node start = current.getStart();
         ArrayList<Node> path = new ArrayList<Node>();
         path.add(start);
         while (!start.equals(current.getEnd())) {
             // Suchen
             for (Edge edge : edges) {
                 if (edge.getStart().equals(current.getEnd())) {
                     // Neues Wegsegment
                     current = edge;
                     path.add(current.getStart());
                 }
             }
         }
 
         // Fertig, einen neuen Polygon draus machen und returnen
         return new FreePolygon(false, path.toArray(new Node[0]));
     }
 
     /**
      * Liefert eine (unveränderbare) Liste mit allen Knoten dieses Polygons
      * @return eine (unveränderbare) Liste mit allen Knoten dieses Polygons
      */
     public List<Node> getNodes() {
         return Collections.unmodifiableList(myNodes);
     }
 
     /**
      * Registriert den Polygon bei seinen Nodes.
      * Normalerweise macht dies der Konstruktor automatisch (wenn mit true aufgerufen)
      * Sonst kann man es hier nachholen, z.B. wenn man den temporären Polygon behalten möchte.
      */
     public final void registerNodes() {
         for (Node node : myNodes) {
             node.addPolygon(this);
         }
     }
 
     /**
      * Die Farbe diese Polygons (debug only)
      * @return the color
      */
     public Color getColor() {
         return color;
     }
 
     /**
      * Überprüft, ob der gefragte Polygon ein Nachbar dieses Feldes ist.
      * Erkennt auch Nachbarn, die nicht als Nachbarn registiert sind (z.B. zur Erstellung der Liste, Vergleich mit temporären etc.)
      * Die Nodes müssen aber wissen, dass sie beide Polygone beinhalten!
      * Sucht nach echten Nachbarn mit geteilter Kante, nicht nur übers Ecke.
      * @param poly der zu Untersuchende Polygon
      * @return true, wenn Nachbar, false wenn nicht.
      */
     public boolean isNeighbor(FreePolygon poly) {
         if (neighbors.contains(poly)) {
             return true;
         } else {
             // Manuelle Suche
             int number = 0;
             for (Node node : this.myNodes) {
                 if (poly.myNodes.contains(node)) {
                     number++;
                 }
             }
             // Fertig mit der Suche.
             if (number >= 2) {
                 return true;
             } else {
                 return false;
             }
         }
     }
 
     /**
      * Returns a List containing all neighbors of this Polygon
      * @return a List containing all neighbors of this Polygon
      */
     public List<FreePolygon> getNeighbors() {
         return Collections.unmodifiableList(neighbors);
     }
 
     /**
      * Registriert einen Polygon als Nachbar, falls noch nicht registriert
      * @param poly der neue Nachbar
      */
     public void registerNeighbor(FreePolygon poly) {
         if (!neighbors.contains(poly)) {
             neighbors.add(poly);
         }
     }
 
     /**
      * Deregistriert einen Polygon als Nachbar, fall er registriert war
      * @param poly der alte Nachbar.
      */
     public void removeNeighbor(FreePolygon poly) {
         neighbors.remove(poly);
     }
 
     @Override
     public String toString() {
         String ret = "Poly: [";
         for (Node node : myNodes) {
             ret += " " + node;
         }
         return ret + "]";
     }
 
     @Override
     public boolean equals(Object o) {
         if (o instanceof FreePolygon) {
             FreePolygon p = (FreePolygon) o;
             for (Node node : p.getNodes()) {
                 if (!myNodes.contains(node)) {
                     return false;
                 }
             }
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 89 * hash + (this.myNodes != null ? this.myNodes.hashCode() : 0);
         return hash;
     }
 
     /**
      * Findet heraus, ob der gegebene Punkt innerhalb dieses Polygons liegt.
      * Wenn der Punkt genau auf einer Kante oder auf einem Knoten liegt, ist das Resultat undefiniert.
      * @param x X
      * @param y Y
      * @return true, wenn innen, sonst false
      */
     public boolean contains(double x, double y) {
         // Verwendet die übliche Strahlmethode. Dabei wird ein Strahl vom zu untersuchenden Punkt in eine beliebige Richtung
         // augesandt. Dann werden die Anzahl der Schnittpunkte mit Kanten des Polygons gezählt.
         // Ist diese Anzahl ungerade, so befindet sich der Punkt innerhalb. Sonst außen.
 
         // Liste mit Kanten:
         ArrayList<Edge> edges = new ArrayList<Edge>();
         for (int i = 0; i < myNodes.size(); i++) {
             edges.add(new Edge(myNodes.get(i), myNodes.get(i + 1 < myNodes.size() ? i + 1 : 0)));
         }
 
         // Linie bauen:
         Edge edge = new Edge(new Node(x, y), new Node(10000, 10000));
 
         int intersections = 0;
         for (int i = 0; i < edges.size(); i++) {
             // Schnitte suchen
             if (edges.get(i).intersectsWithEndsAllowed(edge)) {
                 intersections++;
             }
         }
 
         // Wenn Anzahl ungerade ist es innen.
         return intersections % 2 == 1;
     }
 
     /**
      * Prüft, ob dieses Polygon konvex oder konkav ist.
      * True heißt konvex.
      * @return true, wenn konvex.
      */
     public boolean isConvex() {
         if (this.equals(new FreePolygon(false, new Node(421, 401), new Node(403, 301), new Node(403, 221), new Node(403, 115), new Node(338, 42), new Node(350, 30), new Node(421, 0)))) {
             System.out.println();
         }
         boolean rechts = false;
         boolean links = false;
         for (int i = 0; i < myNodes.size(); i++) {
             // Die 3 Nodes holen
             Node n1 = myNodes.get(i > 0 ? i - 1 : myNodes.size() - 1);
             Node n2 = myNodes.get(i);
             Node n3 = myNodes.get(i < myNodes.size() - 1 ? i + 1 : 0);
             // Vorüberprüfungen:
             // Sonderfall:
             // Alle in x oder y-Richtung auf einer Linie:
             if ((n1.x() == n2.x() && n2.x() == n3.x()) || (n1.y() == n2.y() && n2.y() == n3.y())) {
                 continue; // Weder links noch rechts
             }
             // Sonderfall: n1.x() == n2.x(). Das erzeugt sonst immer "rechts" was zu schlimmen Polygonen führen kann!
             if (n1.x() == n2.x()) {
                 // Nach oben oder unten?
                 if (n1.y() < n2.y()) {
                     // Zeigt nach oben
                     if (n3.x() < n1.x()) {
                         links |= true;
                     } else if (n3.x() > n1.x()) {
                         rechts |= true;
                     }
                 } else {
                     // Nach unten
                     if (n3.x() < n1.x()) {
                         rechts |= true;
                     } else if (n3.x() > n1.x()) {
                         links |= true;
                     }
                 }
             }
             // Rechts oder Links abbiegen?
             // XY Richtung suchen:
             double vecX = n2.getX() - n1.getX();
             double vecY = n2.getY() - n1.getY();
             // y = mx + c
             double m = vecY / vecX;
             double c = n2.getY() - m * n2.getX();
             double checkY = n3.getX() * m + c;
             if (checkY > n3.getY()) {
                 // Drüber
                 if (vecX >= 0) {
                     links |= true;
                 } else {
                     rechts |= true;
                 }
             } else {
                 // Drunter
                 if (vecX >= 0) {
                     rechts |= true;
                 } else {
                     links |= true;
                 }
             }
         }
 
         return rechts ^ links;
     }
 
     /**
      * Fügt einen Mover zu diesem Polygon hinzu.
      * Es passiert nichts, wenn der Mover schon bekannt ist.
      * @param unit 
      */
     public void addMoveable(Moveable mover) {
         if (!residents.contains(mover)) {
             residents.add(mover);
         }
     }
     
     /**
      * Löscht ein Mover wieder aus diesem Polygon.
      * Es passiert nichts, wenn der Mover gar nicht bekannt war.
      * @param mover 
      */
     public void removeMoveable(Moveable mover) {
         residents.remove(mover);
     }
 
     /**
      * Liefert eine (unveränderliche) Liste aller derzeitiger Movers auf diesem Sektor zurück.
      * @return eine Liste mit allen movers auf diesem sektor.
      */
     public List<Moveable> getResidents() {
         return Collections.unmodifiableList(residents);
     }
 
     public org.newdawn.slick.geom.Polygon toSlickPoly() {
         org.newdawn.slick.geom.Polygon poly = new org.newdawn.slick.geom.Polygon();
         for (Node node : myNodes) {
             poly.addPoint((float) node.getX(), (float) node.getY());
         }
        poly.addPoint((float) myNodes.get(0).getX(), (float) myNodes.get(0).getX());
         return poly;
     }
 
     /**
      * Liefert einen zu dieser Position möglichst nahe liegenden Knoten dieses Polygons.
      * @param pos die Position  
      * @return der nächste Knoten
      */
     public Node closestNode(SimplePosition pos) {
         Node nearest = null;
         double dist = Double.MAX_VALUE;
         for (int i = 0; i < myNodes.size(); i++) {
             Node n = myNodes.get(i);
             double newdist = Math.sqrt((pos.x() - n.x()) * (pos.x() - n.x()) + ((pos.y() - n.y()) * (pos.y() - n.y())));
             if (newdist < dist) {
                 dist = newdist;
                 nearest = n;
             }
         }
         return nearest;
     }
 
     public List<Edge> calcEdges() {
         LinkedList list = new LinkedList<Edge>();
         for (int i = 0; i < myNodes.size(); i++) {
             list.add(new Edge(myNodes.get(i), myNodes.get(i + 1 < myNodes.size() ? i + 1 : 0)));
         }
         return list;
     }
 }
