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
 package de._13ducks.cor.game.server;
 
 import de._13ducks.cor.game.SimplePosition;
 import de._13ducks.cor.game.server.movement.Edge;
 import de._13ducks.cor.game.server.movement.FreePolygon;
 import de._13ducks.cor.game.server.movement.MovementMap;
 import java.util.*;
 import org.apache.commons.collections.buffer.PriorityBuffer;
 import de._13ducks.cor.game.server.movement.Node;
 
 /**
  * Der Serverpathfinder.
  * Sucht Wege zwischen Knoten (Nodes)
  * Um einen echten Weg zu bekommen, muss der Weg danach noch überarbeitet werden, aber das ist nicht Aufgabe des
  * Pathfinders.
  * @author tfg
  */
 public final class ServerPathfinder {
 
     /**
      * Niemand kann einen Pathfinder erstellen, dies ist eine Utilityclass
      */
     private ServerPathfinder() {
     }
 
     public static synchronized List<Node> findPath(Node startNode, Node targetNode) {
 
 
 
         if (startNode == null || targetNode == null) {
             System.out.println("FixMe: SPathfinder, irregular call: " + startNode + "-->" + targetNode);
             return null;
         }
 
         if (startNode.equals(targetNode)) {
             return new ArrayList<Node>();
         }
 
         PriorityBuffer open = new PriorityBuffer();      // Liste für entdeckte Knoten
         LinkedHashSet<Node> containopen = new LinkedHashSet<Node>();  // Auch für entdeckte Knoten, hiermit kann viel schneller festgestellt werden, ob ein bestimmter Knoten schon enthalten ist.
         LinkedHashSet<Node> closed = new LinkedHashSet<Node>();    // Liste für fertig bearbeitete Knoten
 
         double cost_t = 0;    //Movement Kosten (gerade 5, diagonal 7, wird später festgelegt)
 
         startNode.setCost(0);   //Kosten für das Startfeld (von dem aus berechnet wird) sind natürlich 0
         open.add(startNode);  //Startfeld in die openlist
         containopen.add(startNode);
         targetNode.setParent(null);    //"Vorgängerfeld" vom Zielfeld noch nicht bekannt
 
         for (int j = 0; j < 40000; j++) {		//Anzahl der maximalen Durchläufe, bis Wegfindung aufgibt
 
             if (open.isEmpty()) {   //Abbruch, wenn openlist leer ist => es gibt keinen Weg
                 return null;
             }
 
             // Sortieren nicht mehr nötig, PriorityBuffer bewahrt die Felder in der Reihenfolge ihrer Priority - also dem F-Wert auf
             Node current = (Node) open.remove();		//der Eintrag aus der openlist mit dem niedrigesten F-Wert rausholen und gleich löschen
             containopen.remove(current);
             if (current.equals(targetNode)) {	//Abbruch, weil Weg von Start nach Ziel gefunden wurde
                 targetNode.setParent(current.getParent());   //"Vorgängerfeld" von Ziel bekannt
                 break;
             }
 
             // Aus der open wurde current bereits gelöscht, jetzt in die closed verschieben
             closed.add(current);
 
             List<Node> neighbors = current.getReachableNodes();
 
             for (Node node : neighbors) {
 
                 if (closed.contains(node)) {
                     continue;
                 }
 
                 // Kosten dort hin berechnen
                 cost_t = current.movementCostTo(node);
 
                 if (containopen.contains(node)) {         //Wenn sich der Knoten in der openlist befindet, muss berechnet werden, ob es einen kürzeren Weg gibt
 
                     if (current.getCost() + cost_t < node.getCost()) {		//kürzerer Weg gefunden?
 
                         node.setCost(current.getCost() + cost_t);         //-> Wegkosten neu berechnen
                         node.setValF(node.getCost() + node.getHeuristic());  //F-Wert, besteht aus Wegkosten vom Start + Luftlinie zum Ziel
                         node.setParent(current); //aktuelles Feld wird zum Vorgängerfeld
                     }
                 } else {
                     node.setCost(current.getCost() + cost_t);
                     node.setHeuristic((Math.abs((targetNode.getX() - node.getX())) + Math.abs((targetNode.getY() - node.getY()))) * 3);	// geschätzte Distanz zum Ziel
                     //Die Zahl am Ende der Berechnung ist der Aufwand der Wegsuche
                     //5 ist schnell, 4 normal, 3 dauert lange
 
                     node.setParent(current);						// Parent ist die RogPosition, von dem der aktuelle entdeckt wurde
                     node.setValF(node.getCost() + node.getHeuristic());  //F-Wert, besteht aus Wegkosten vom Start aus + Luftlinie zum Ziel
                     open.add(node);    // in openlist hinzufügen
                     containopen.add(node);
                 }
 
 
             }
         }
 
         if (targetNode.getParent() == null) {		//kein Weg gefunden
             return null;
         }
 
         ArrayList<Node> pathrev = new ArrayList();   //Pfad aus parents erstellen, von Ziel nach Start
         while (!targetNode.equals(startNode)) {
             pathrev.add(targetNode);
             targetNode = targetNode.getParent();
         }
         pathrev.add(startNode);
 
         ArrayList<Node> path = new ArrayList();	//Pfad umkehren, sodass er von Start nach Ziel ist
         for (int k = pathrev.size() - 1; k >= 0; k--) {
             path.add(pathrev.get(k));
         }
 
         return path;					//Pfad zurückgeben
     }
 
     public static List<SimplePosition> optimizePath(List<Node> path, SimplePosition startPos, SimplePosition endPos, MovementMap moveMap) {
         List<SimplePosition> optiList = new LinkedList<SimplePosition>();
 
         path.add(0, startPos.toNode());
         path.add(endPos.toNode());
 
         FreePolygon currentPoly = moveMap.containingPoly(startPos.x(), startPos.y());
 
         int firstindex = 0;
         for (int i = 1; i < path.size() - 1; i++) {
 
             Node pre = path.get(firstindex);
             Node cur = path.get(i);
             Node nxt = path.get(i + 1);
 
             Edge edge = new Edge(pre, nxt);
 
             LinkedList<Node> extraNodes = new LinkedList<Node>();
 
             while (!nxt.getPolygons().contains(currentPoly)) {
                 // Die Seite mit Schnitt finden
                 List<Edge> edges = currentPoly.calcEdges();
                 Edge intersecting = null;
                 for (Edge testedge : edges) {
                     // Gibts da einen Schnitt?
                    if (edge.intersectsWith(testedge)) {
                         intersecting = testedge;
                         break;
                     }
                 }
                 // Von dieser Kante die Enden suchen
                 FreePolygon nextPoly = getOtherPoly(intersecting.getStart(), intersecting.getEnd(), currentPoly);
                 // Existiert dieser Polygon?
                 if (nextPoly != null) {
                     // JA!
                     // Extra Node einfügen
                     Node extraNode = intersecting.intersectionWith(edge).toNode();
                     extraNode.addPolygon(nextPoly);
                     extraNode.addPolygon(currentPoly);
                     extraNodes.add(extraNode);
                     currentPoly = nextPoly;
                 } else {
                     // Nein. Also ist die direkte Route nicht möglich. Der derzeitge Polygon ist also Pflicht.
                     optiList.add(cur);
                    firstindex++;
                     break;
                 }
             }
 
             // Alle nach pre löschen
             if (optiList.size() > 1) {
                 optiList = optiList.subList(0, i);
             }
 
             // Alle gefundenen neuen Knoten einbauen
             optiList.addAll(extraNodes);
             extraNodes.clear();
 
         }
 
         return optiList;
     }
 
     private static FreePolygon getOtherPoly(Node n1, Node n2, FreePolygon myself) {
         for (FreePolygon poly : n1.getPolygons()) {
             if (poly.equals(myself)) {
                 continue;
             }
             if (n2.getPolygons().contains(poly)) {
                 return poly;
             }
         }
         return null;
     }
 }
