 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de._13ducks.cor.game.server.movement;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  * Ein Knoten des Graphen
  */
 public class SubSectorObstacle {
 
     /**
      * Koordinaten
      */
     private double x;
     /**
      * Koordinaten
      */
     private double y;
     /**
      * Der Radius dieses Hindernisses selbst
      */
     private double radius;
     /**
      * Die Knoten dieses SubSectorObstacles
      */
     private LinkedList<SubSectorNode> nodes;
 
     SubSectorObstacle(double x, double y, double radius) {
         this.x = x;
         this.y = y;
         this.radius = radius;
         nodes = new LinkedList<SubSectorNode>();
     }
 
     /**
      * @return the x
      */
     double getX() {
         return x;
     }
 
     /**
      * @param x the x to set
      */
     void setX(double x) {
         this.x = x;
     }
 
     /**
      * @return the y
      */
     double getY() {
         return y;
     }
 
     /**
      * @param y the y to set
      */
     void setY(double y) {
         this.y = y;
     }
 
     /**
      * Findet heraus, ob sich die Lauflinie dieses Knoten des Graphen mit dem
      * gegebenen schneidet.
      * @param next Der andere Knoten
      * @param moveRadius Der Radius des Objektes, das dazwischen noch durch passen soll
      * @return true, wenn sie sich schneiden
      */
     boolean inColRange(SubSectorObstacle next, double moveRadius) {
         double dist = Math.sqrt((x - next.x) * (x - next.x) + (y - next.y) * (y - next.y));
         if (dist < radius + next.radius + moveRadius + moveRadius) {
             return true;
         }
         return false;
     }
 
     /**
      * @return the radius
      */
     double getRadius() {
         return radius;
     }
 
     /**
      * Berechnet die Schnittpunkte der Lauflinien dieses und des gegebenen
      * Hindernisses
      * Setzt voraus, dass es zwei Schnittpunkte gibt. Das Verhalten dieser
      * Methode ist in anderen Fällen nicht definiert.
      * @param next Das andere Hinderniss
      * @param radius Der Radius (die Größe) des Movers, der noch zwischendurch passen muss.
      * @return SubSectorNode[] mit 2 Einträgen
      */
     SubSectorNode[] calcIntersections(SubSectorObstacle next, double radius) {
         // Zuerst Mittelpunkt der Linie durch beide Schnittpunkte berechnen:
         Vector direct = new Vector(next.x - x, next.y - y);
         Vector z1 = direct.normalize().multiply(this.radius + radius);
         Vector z2 = direct.normalize().multiply(direct.length() - (next.radius + radius));
         Vector mid = direct.normalize().multiply((z1.length() + z2.length()) / 2.0);
         // Senkrechten Vektor und seine Länge berechnen:
         Vector ortho = new Vector(direct.y(), -direct.x());
         ortho = ortho.normalize().multiply(Math.sqrt(((this.radius + radius) * (this.radius + radius)) - (mid.length() * mid.length())));
         // Schnittpunkte ausrechnen:
         SubSectorNode[] intersections = new SubSectorNode[2];
         Vector posMid = new Vector(x + mid.x(), y + mid.y()); // Positionsvektor des Mittelpunkts
         Vector s1 = posMid.add(ortho);
         Vector s2 = posMid.add(ortho.getInverted());
         intersections[0] = new SubSectorNode(s1.x(), s1.y(), this, next);
         intersections[1] = new SubSectorNode(s2.x(), s2.y(), this, next);
         return intersections;
     }
 
     /**
      * Findet heraus, ob der gegebene Punkt innerhalb des Laufkreises liegt,
      * also zu nahe dran ist.
      * @param n2 Der zu untersuchende Punkt
      * @return true, wenn zu nahe dran
      */
     boolean moveCircleContains(SubSectorNode n2, double radius) {
         double dist = Math.sqrt((x - n2.getX()) * (x - n2.getX()) + (y - n2.getY()) * (y - n2.getY()));
         if (dist < radius + this.radius) {
             return true;
         }
         return false;
     }
 
     /**
      * Fügt einen Knoten in die Kreislinie dieses Obstacles ein
      * @param n2 
      */
     void addNode(SubSectorNode n2) {
         if (nodes.contains(n2)) {
             System.out.println("ERROR: Adding same Node again!!");
         } else {
             nodes.add(n2);
         }
     }
 
     /**
      * Löscht alle Knoten, die zu nahe an next dran sind.
      * @param next 
      */
     void removeNearNodes(SubSectorObstacle next, double radius) {
         for (int i = 0; i < nodes.size(); i++) {
             SubSectorNode node = nodes.get(i);
             if (moveCircleContains(node, radius)) {
                 nodes.remove(i--);
             }
         }
     }
 
     /**
      * Alle Knoten ihrem Bogenmaß nach sortieren
      */
     void sortNodes() {
         Collections.sort(nodes, new Comparator<SubSectorNode>() {
 
             @Override
             public int compare(SubSectorNode o1, SubSectorNode o2) {
                 double t1 = Math.atan2(y - o1.getY(), x - o1.getX());
                 double t2 = Math.atan2(y - o2.getY(), x - o2.getX());
                 // IN 2PI-Bogenmaß umwandeln:
                 if (t1 < 0) {
                     t1 = 2 * Math.PI + t1;
                 }
                 if (t2 < 0) {
                     t2 = 2 * Math.PI + t2;
                 }
                 if (t1 > t2) {
                     return 1;
                 } else if (t1 < t2) {
                     return -1;
                 } else {
                     return 0;
                 }
             }
         });
     }
 
     /**
      * Verbindet die Knoten intern.
      * Setzt sortierte Knotenliste voraus.
      */
     void interConnectNodes(double radius) {
         if (nodes.size() >= 2) {
             SubSectorNode start = nodes.peekFirst();
             Iterator<SubSectorNode> iter = nodes.iterator();
             SubSectorNode current = start;
             iter.next(); // Eines übersprigen
             // Immer versuchen, mit dem nächsten zu verbinden.
             while (iter.hasNext() && current != null) {
                 SubSectorNode work = iter.next();
                 // Versuche current mit work zu verbinden.
                 // Geht nur, wenn current in + und work in - Richtung verbunden werden dürfen.
                 // Richtungen bestimmen
                 boolean cDirection = calcDirection(current, radius);
                 boolean wDirection = calcDirection(work, radius);
                 // Verbinden, wenn c + und w - ist.
                 if (cDirection & !wDirection) {
                     // Verbinden
                     buildEdge(current, work);
                 }
                 current = work;
             }
             // Am Ende noch versuchen den letzen mit dem Start zu verbinden:
             if (calcDirection(nodes.getLast(), radius) & !calcDirection(nodes.getFirst(), radius)) {
                 buildEdge(nodes.getLast(), nodes.getFirst());
             }
         }
     }
 
     /**
      * Berechnet, ob ein Knoten in Plus (true) oder Minus-Richtung laufen darf.
      * @param node Der Knoten
      * @return true, wenn in Plus-Richtung.
      */
     boolean calcDirection(SubSectorNode node, double radius) {
         // "Anderes" Hinderniss finden
         SubSectorObstacle other = node.otherObstacle(this);
         // Abstand vom anderen zu Node berechnen:
         double dist = Math.sqrt((node.getX() - other.x) * (node.getX() - other.x) + (node.getY() - other.y) * (node.getY() - other.y));
         // Ein kleines Stück in plus-Richtung weiter gehen:
         double tetha = Math.atan2(y - node.getY(), x - node.getX());
         tetha += 0.1;
         if (tetha > Math.PI) {
             tetha = -Math.PI + 0.1;
         }
         // Punkt hier berechnen:
         Vector newVec = new Vector(Math.cos(tetha), Math.sin(tetha));
         newVec = newVec.normalize().multiply(other.radius + radius);
         // Abstand hier berechnen:
         double dist2 = Math.sqrt((newVec.x() - other.x) * (newVec.x() - other.x) + (newVec.y() - other.y) * (newVec.y() - other.y));
         // Wenn Abstand größer geworden, dann darf man in Plus gehen
         if (dist2 > dist) {
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public boolean equals(Object o) {
         if (o instanceof SubSectorObstacle) {
             SubSectorObstacle obst = (SubSectorObstacle) o;
             if (Math.abs(x - obst.x) < 0.001 && Math.abs(y - obst.y) < 0.001) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 97 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
         hash = 97 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
         return hash;
     }
 
     /**
      * Verbindet current mit work.
      * @param current
      * @param work 
      */
     void buildEdge(SubSectorNode current, SubSectorNode work) {
         double tethaWork = Math.atan2(y - work.getY(), x - work.getX());
         double tethaCurrent = Math.atan2(y - current.getY(), x - current.getX());
         // Beide in 2PI-Darstellung umrechnen:
         if (tethaWork < 0) {
             tethaWork = 2 * Math.PI + tethaWork;
         }
         if (tethaCurrent < 0) {
             tethaCurrent = 2 * Math.PI + tethaCurrent;
         }
         double length = tethaWork - tethaCurrent;
         if (length < 0) {
             length = 2 * Math.PI + length;
         }
         SubSectorEdge edge = new SubSectorEdge(current, work, length, this);
         current.addEdge(edge);
         work.addEdge(edge);
     }
 
     /**
      * @return the nodes
      */
     LinkedList<SubSectorNode> getNodes() {
         return nodes;
     }
 
     /**
      * Fügt einen Knoten ein, nachdem der Graph eigentlich schon aufgebaut wurde.
      * Setzt einen vollständig aufgebauten Graphen voraus.
      * Der neue Knoten muss auf dem Laufkreis liegen UND
      * eine derzeit vorhandene Kante teilen.
      * Es können keine Knoten in leere Bereiche eingeteilt werden.
      * Die derzeit bestehenden Kanten werden umgebogen.
      * @param minNode der neue Knoten, auf dem Laufkreis auf einer Kante
      */
     void lateIntegrateNode(SubSectorNode minNode) {
         if (nodes.size() >= 2) {
             // Kante suchen
             double newTetha = Math.atan2(minNode.getY() - y, minNode.getX() - x);
             if (newTetha < 0) {
                 newTetha = 2 * Math.PI + newTetha;
             }
             SubSectorNode current = null;
             SubSectorNode next = null;
             double currTetha = 0;
             double nextTetha = 0;
             boolean found = false;
             for (int i = 0; i < nodes.size(); i++) {
                 current = nodes.get(i);
                 next = i < nodes.size() - 1 ? nodes.get(i + 1) : nodes.get(0);
                 currTetha = Math.atan2(current.getY() - y, current.getX() - x);
                 if (currTetha < 0) {
                     currTetha = 2 * Math.PI + currTetha;
                 }
                 nextTetha = Math.atan2(next.getY() - y, next.getX() - x);
                 if (nextTetha < 0) {
                     nextTetha = 2 * Math.PI + nextTetha;
                 }
                 if (currTetha < newTetha && nextTetha > newTetha) {
                     // Genau hier einfügen
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 // Dann ist es zwischen dem letzen und dem ersten
                 current = nodes.getLast();
                 next = nodes.getFirst();
             }
             // Hier einfügen
             // Neue Kanten bauen:
             double l1length = newTetha - currTetha;
             if (l1length < 0) {
                 l1length = 2 * Math.PI + l1length;
             }
             SubSectorEdge newEdge1 = new SubSectorEdge(current, minNode, l1length, this);
             current.removeEdgeTo(next);
             current.addEdge(newEdge1);
             minNode.addEdge(newEdge1);
 
             double l2length = nextTetha - newTetha;
             if (l2length < 0) {
                 l2length = 2 * Math.PI + l2length;
             }
             SubSectorEdge newEdge2 = new SubSectorEdge(minNode, next, l2length, this);
             next.removeEdgeTo(current);
             next.addEdge(newEdge2);
             minNode.addEdge(newEdge2);
         } else if (nodes.size() == 1) {
             SubSectorNode old = nodes.get(0); 
             // Nur ein Knoten da. (Wegen eines einzelnen Hindernisses)
             double oldTetha = Math.atan2(old.getY() - y, old.getX() - x);
             if (oldTetha < 0) {
                 oldTetha = 2 * Math.PI + oldTetha;
             }
             double newTetha = Math.atan2(minNode.getY() - y, minNode.getX() - x);
             if (newTetha < 0) {
                newTetha = 2 * Math.PI + oldTetha;
             }
             double maxL = newTetha - oldTetha;
             if (maxL < 0) {
                 maxL = 2 * Math.PI + maxL;
             }
             SubSectorEdge e1 = new SubSectorEdge(old, minNode, maxL, this);
             SubSectorEdge e2 = new SubSectorEdge(minNode, old, Math.PI * 2 - maxL, this);
             minNode.addEdge(e1);
             minNode.addEdge(e2);
             old.addEdge(e1);
             old.addEdge(e2);
         }
         nodes.add(minNode);
         sortNodes();
     }
 }
