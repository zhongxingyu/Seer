 /*
  Copyright (C) 2013 Thomas Bardoux, Christophe Venevongsos
 
  This file is part of Liquid Wars Android
 
  Liquid Wars Android is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  Liquid Wars Android is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
  */
 
 package fr.umlv.tbcv.liquidwar.logic.pathfinding;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 
 import fr.umlv.tbcv.liquidwar.logic.CellState;
 import fr.umlv.tbcv.liquidwar.logic.Coordinates;
 import fr.umlv.tbcv.liquidwar.logic.Fighter;
 
 /**
  * Nodes for pathfinding
  */
 public class Node implements Comparable {
     private Coordinates coord ;                     // Coordinates on the grid of the node
     protected double g;                             // Exact cost of the path from the starting point to that node
     protected double heuristic ;                    // Approximate cost of the path from that node to the ending point
     protected double f ;                            // g+f
     protected boolean opened, closed ;              // Whether the node is in the open set and/or closed set
     private Node parent = null ;                    // Parent node (JPS algorithm)
     private CellState state = CellState.EMPTY ;     // State of the node : empty, obstacle, or fighter
     private Fighter fighter ;                       // Fighter present on the node, if any
 
     public Node(Coordinates c) {
        g = heuristic = f = 0 ;
         opened = closed = false ;
         coord = new Coordinates(c) ;
     }
 
     public Node(int x, int y) {
         g = heuristic = f = 0 ;
         opened = closed = false ;
         coord = new Coordinates(x,y) ;
     }
 
     public Coordinates getCoord() {
         return coord;
     }
 
     public void setCoord(Coordinates coord) {
         this.coord.copyCoordinates(coord);
     }
 
     public Node getParent() {
         return parent;
     }
 
     public void setParent(Node parent) {
         this.parent = parent;
     }
 
     public CellState getState() {
         return state;
     }
 
     public void setState(CellState state) {
         this.state = state;
     }
 
     public Fighter getFighter() {
         return fighter;
     }
 
     public void setFighter(Fighter f) {
         fighter = f ;
     }
 
     @Override
     public boolean equals(Object o) {
         if(o instanceof Node) {
             Node n = (Node) o ;
             return coord.equals(n.coord) ;
         }
         return false ;
     }
 
     /**
      * Build a list of node by adding parents successively
      * @return A deque consisting of a node and every parent it has
      */
     public Deque<Coordinates> backtrace () {
         Deque<Coordinates> path = new ArrayDeque<>() ;
         Node nParent ;
 
         path.push(this.coord) ;
         nParent = this.parent ;
         while(nParent != null) {
             path.push(nParent.coord) ;
             nParent = nParent.parent ;
         }
         return path ;
     }
 
     /**
      * Reset the node state so that previous pathfinding algorithm call doesn't interfere with a next one.
      */
     public void resetNode() {
        g = heuristic = f = 0 ;
         opened = closed = false ;
         parent = null ;
     }
 
     @Override
     public String toString() {
        return "Node " + coord + " | opened = " + opened + "closed = " + closed ;
     }
 
     /**
      * Compares this object to the specified object to determine their relative
      * order.
      *
      * @param another the object to compare to this instance.
      * @return a negative integer if this instance is less than {@code another};
      *         a positive integer if this instance is greater than
      *         {@code another}; 0 if this instance has the same order as
      *         {@code another}.
      * @throws ClassCastException if {@code another} cannot be converted into something
      *                            comparable to {@code this} instance.
      */
     @Override
     public int compareTo(Object another) {
         if(!(another instanceof Node)) {
             throw new RuntimeException() ;
         }
         Node n = (Node) another ;
         if(f - n.f < 0) {
             return -1 ;
         }
        if(f - n.f < 0) {
             return 1 ;
         }
         return 0;
     }
 }
