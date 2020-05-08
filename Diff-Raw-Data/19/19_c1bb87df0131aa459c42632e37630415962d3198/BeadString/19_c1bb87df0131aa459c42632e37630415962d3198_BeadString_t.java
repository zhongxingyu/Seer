 /*
  * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
  *
  * Licensed under the Academic Free License v. 3.0.
  * http://www.opensource.org/licenses/afl-3.0.php
  */
 
 /**
  * A BeadString contains a small set of tokens, called a "string" in Gente, and
  * allows clients to perform some basic operations upon each string.  For use
  * in the Gente rule sets.
  * 
  * @author awaterma@ecosur.mx
  */
 
 package mx.ecosur.multigame.grid.util;
 
 import java.io.Serializable;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
import mx.ecosur.multigame.grid.Color;
 import mx.ecosur.multigame.grid.comparator.CellComparator;
 import mx.ecosur.multigame.grid.enums.Direction;
 import mx.ecosur.multigame.grid.enums.Vertice;
 import mx.ecosur.multigame.grid.model.GridCell;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 import javax.persistence.*;
 
 public class BeadString implements Serializable, Cloneable {
         
     private static final long serialVersionUID = -5360218565926616845L;
 
     private SortedSet<GridCell> beads;
 
     public BeadString () {
         super();
     }
 
     public BeadString (GridCell... cells) {
         this();
         for (GridCell cell : cells) {
             add(cell);
         }
     }
 
    public Color singleColor() {
        Color master = Color.UNKNOWN;
        for (GridCell cell : getBeads()) {
            if (master == Color.UNKNOWN) {
                master = cell.getColor();
                continue;
            }
            if (!cell.getColor().equals(master)) {
                master = Color.UNKNOWN;
                break;
            }
        }

        return master;
    }


     public SortedSet<GridCell> getBeads () {
         if (beads == null)
             beads = new TreeSet<GridCell>(new CellComparator());
         return beads;
     }
 
     public void setBeads(SortedSet<GridCell> new_beads){
         beads = new_beads;
     }
 
     public void add (GridCell cell) {
         if (beads == null)
             beads = new TreeSet<GridCell>(new CellComparator());
         beads.add(cell);
     }
 
     public boolean remove (GridCell cell) {
         return beads.remove(cell);
     }
 
     public int size () {
             return beads.size();
     }
 
     public boolean contains (GridCell cell) {
             return beads.contains(cell);
     }
 
     public boolean isTerminator (GridCell cell) {
             return (beads.first() == cell || beads.last() == cell);
     }
 
     public boolean contains (BeadString string) {
             boolean ret = false;
             int count = 0;
 
             for (GridCell cell : beads) {
                 if (string.contains(cell))
                     count++;
                 if (count > 1) {
                     ret = true;
                     break;
                 }
             }
 
         return ret;
     }
 
     /**
      * Returns the Direction to which these beads point.
      * @return
      */
     public Direction findDirection() {
         Direction ret = Direction.UNKNOWN;
 
         /* Calculate the slope */
         int x = beads.first().getColumn() - beads.last().getColumn();
         int y = beads.first().getRow() - beads.last().getRow();
 
         /** TODO: Determine NE,SE,NW,SW directions */
         if (x == 0 && y == 0) {
             if (beads.first().getRow() > beads.last().getRow())
                     ret = Direction.NORTH;
             else
                     ret = Direction.SOUTH;
         } else {
             float slope = (float) x / y;
             if (slope == 0) {
                 if (beads.first().getColumn() > beads.last().getColumn())
                         ret = Direction.EAST;
                 else
                         ret = Direction.WEST;
             }
         }
 
         return ret;
     }
 
     /**
      * Verifies that a given beadstring is contiguous on a given Vertice.
      * @return boolean
      */
     public boolean isContiguous(Vertice v) {
         boolean ret = true;
 
         int horizontal = 0, vertical = 0;
 
         switch (v) {
             case HORIZONTAL:
                     horizontal = 1;
                     vertical   = 0;
                     break;
             case VERTICAL:
                     horizontal = 0;
                     vertical   = 1;
                     break;
             case FORWARD:
                     horizontal = 1;
                     vertical   = 1;
                     break;
             case REVERSE:
                     horizontal = 1;
                     vertical   = -1;
                     break;
             default:
                 throw new RuntimeException ("Vertice not set!");
         }
 
         GridCell lastCell = beads.first();
         for (GridCell cell : beads.tailSet(beads.first())) {
             if (cell.equals(lastCell))
                     continue;
             if (cell.getColumn() == lastCell.getColumn() + vertical &&
                     cell.getRow() == lastCell.getRow () + horizontal) {
                 lastCell = cell;
             } else {
                 ret = false;
                 break;
             }
         }
 
         return ret;
     }
 
     public BeadString trim(GridCell destination, int stringlength) {
         BeadString ret = new BeadString();
         if (beads.first() == destination) {
                 ret.setBeads(new TreeSet<GridCell> (beads.tailSet(destination)));
         } else if (beads.last() == destination) {
                 ret.setBeads(new TreeSet<GridCell>(beads.headSet(destination)));
         }
 
         if (!ret.contains(destination))
             ret.add(destination);
         return ret;
     }
 
     @Override
     public Object clone() throws CloneNotSupportedException {
         super.clone();
         BeadString ret = new BeadString ();
         for (GridCell cell : beads) {
             ret.add((GridCell) cell.clone());
         }
 
         return ret;
     }
 
     @Override
     public String toString() {
         StringBuffer buf = new StringBuffer ("BeadString [");
         for (GridCell cell : beads) {
             buf.append(cell.toString());
             buf.append (" ");
         }
         buf.append (" ]");
         return buf.toString();
     }
 
     @Override
     public boolean equals(Object obj) {
         boolean ret;
         if (obj instanceof BeadString){
             BeadString comparison = (BeadString) obj;
             ret = beads.equals(comparison.beads);
         } else
             ret = super.equals(obj);
         return ret;
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder().append(beads).hashCode();
     }
 }
