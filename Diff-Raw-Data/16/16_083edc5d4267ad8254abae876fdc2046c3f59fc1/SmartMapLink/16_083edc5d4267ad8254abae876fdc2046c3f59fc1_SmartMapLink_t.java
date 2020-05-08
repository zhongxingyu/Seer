 /*
  * This file is part of SmartStreets.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ru.jcorp.smartstreets.routing;
 
 import ru.jcorp.smartstreets.map.SmartMapLine;
 import ru.jcorp.smartstreets.map.SmartMapNode;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * <p>$Id$</p>
  *
  * @author Artamonov Yuriy
  */
 public class SmartMapLink implements Comparable<SmartMapLink> {
 
     private SmartMapNode node;
 
     private double cost = 0.0;
 
     private double newCost = 0.0;
 
     private SmartMapLink parent;
 
     public SmartMapLink(SmartMapNode node) {
         this.node = node;
     }
 
     public SmartMapLink(SmartMapNode node, double newCost) {
         this.node = node;
         this.newCost = newCost;
     }
 
     public List<SmartMapLink> getNeighbors() {
         List<SmartMapLink> neighbors = new LinkedList<SmartMapLink>();
         if (node.getStartings() != null)
            for (SmartMapLine line : node.getStartings()) {
                if ((parent == null) || (line.getEndNode() != parent.getNode()))
                    neighbors.add(new SmartMapLink(line.getEndNode(), cost + line.getLength()));
            }
         if (node.getEndings() != null)
            for (SmartMapLine line : node.getEndings()) {
                if ((parent == null) || (line.getStartNode() != parent.getNode()))
                    neighbors.add(new SmartMapLink(line.getStartNode(), cost + line.getLength()));
            }
         return neighbors;
     }
 
     public SmartMapNode getNode() {
         return node;
     }
 
     public double getCost() {
         return cost;
     }
 
     public double getNewCost() {
         return newCost;
     }
 
     public void setCost(double cost) {
         this.cost = cost;
     }
 
     public SmartMapLink getParent() {
         return parent;
     }
 
     public void setParent(SmartMapLink parent) {
         this.parent = parent;
     }
 
     @Override
     public int compareTo(SmartMapLink o) {
         if (o != null) {
            return Double.compare(o.cost, cost);
         }
         return 1;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         SmartMapLink link = (SmartMapLink) o;
 
         return node.equals(link.node);
 
     }
 
     @Override
     public int hashCode() {
         return node.hashCode();
     }
 }
