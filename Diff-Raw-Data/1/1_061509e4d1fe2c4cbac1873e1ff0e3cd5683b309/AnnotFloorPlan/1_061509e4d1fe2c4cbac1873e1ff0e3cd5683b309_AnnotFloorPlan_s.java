 /**
  * Floor Plan Marker Project
  * Copyright (C) 2013  Vy Thuy Nguyen
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  * 
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
  * Boston, MA  02110-1301, USA.
  */
 
 package entity;
 
 import java.io.Serializable;
 import java.util.*;
 import javax.persistence.*;
 import org.jgrapht.alg.DijkstraShortestPath;
 import org.jgrapht.graph.DefaultWeightedEdge;
 import org.jgrapht.graph.SimpleWeightedGraph;
 
 /**
  * @author              Vy Thuy Nguyen
  * @version             1.0 Jan 16, 2013
  * Last modified:       
  */
 @Entity
 public class AnnotFloorPlan implements Serializable 
 {
     private static final long serialVersionUID = 1L;
     
     @Id
     @GeneratedValue (strategy = GenerationType.AUTO)
     private long id;
     
     @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
     private FloorPlan floorPlan;
     
     @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.PERSIST}, 
                           fetch = FetchType.EAGER, mappedBy = "annotFloorPlan")
     private Set<Cell> deadCells;
     
     
     //Each cell accounts for 1% of the width and the height
     private final int ratio = 2; //percent in respect to actual length
     int unitW;
     int unitH;
     int rowCount;
     int colCount;
     
     @Transient
     private boolean graphInitialized = false;
     
     @Transient
     private SimpleWeightedGraph<Cell, DefaultWeightedEdge> g;
     
     @Transient
     private Cell[][] cellContainer;
         
     @SuppressWarnings({"unchecked", "unchecked"})
     public AnnotFloorPlan()
     {
         floorPlan = new FloorPlan();
         deadCells = new HashSet<Cell>();
         
         int width = 1200;
         int height = 1000;
         unitW = width * ratio / 100;
         unitH = height * ratio / 100;
         rowCount = height / unitH + 1;
         colCount = width / unitW + 1;
         cellContainer = new Cell[rowCount][colCount];
          
         //Init the graph
         g = new SimpleWeightedGraph<Cell, DefaultWeightedEdge>(DefaultWeightedEdge.class);
         
         initGraph();
     }
     
     public AnnotFloorPlan(FloorPlan fp)
     {
         floorPlan = fp;        
         deadCells = new HashSet<Cell>();
         
         unitW = fp.getWidth() * ratio / 100;
         unitH = fp.getHeight() * ratio / 100;
         rowCount = fp.getHeight() / unitH + 1;
         colCount = fp.getWidth() / unitW + 1;
         cellContainer = new Cell[rowCount][colCount];
         
         //init the graph
         g = new SimpleWeightedGraph<Cell, DefaultWeightedEdge>(DefaultWeightedEdge.class);
         
         initGraph();
         //generateVertices();
         //generateEdges();
     }
      
     public int getUnitW()
     {
         return unitW;
     }
     
     public int getUnitH()
     {
         return unitH;
     }
     
     public SimpleWeightedGraph<Cell, DefaultWeightedEdge> getGraph()
     {
         return g;
     }
     
     public boolean needInitGraph()
     {
         return !graphInitialized;
     }
     
     public void initGraph()
     {
         if (!graphInitialized)
         {
             generateVertices();
             generateEdges();
             graphInitialized = true;
         }
     }
     
     /**
      * 
      * @param x1 the actual "from" x coordinate (in respect to the "dense grid")
      * @param y1 the actual "from" y coordinate (ditto)
      * @param x2 the actual "to" x coordinate (ditto)
      * @param y2 the actual "to" y coordinate (ditto)
      * @return the list of edges representing the shortest path from P1 to P2
      */
     public List<DefaultWeightedEdge> getShortestPath(int x1, int y1, int x2, int y2)
     {
         //int x1 = 158, y1 = 109, x2 = 108, y2 = 489;
         int row1 = y1 / unitH, col1 = x1 / unitW, row2 = y2 / unitH, col2 = x2 / unitW;
         DijkstraShortestPath d = new DijkstraShortestPath(g, cellContainer[row1][col1], cellContainer[row2][col2]);
         return d.getPathEdgeList();
     }
     
     /**
      * 
      */
     private void generateVertices()
     {
         for (int row = 0; row < rowCount; ++row)
             for (int col = 0; col < colCount; ++col)
             {
                 cellContainer[row][col] = new Cell(row, col);
                 g.addVertex(cellContainer[row][col]);
             }
     }
     
     private void generateEdges()
     {
         for (int row = 0; row < rowCount; ++row)
             for (int col = 0; col < colCount; ++col)
             {
                //System.out.println("generating edges for node at [" + row + ", " + col+ "]...");
                 addEdges(row, col);
             }
     }
      
     /**
      * Remove all edges touching the cell at the specified row and col.
      * 
      * @param row
      * @param col 
      */
     private void removeEdges(int row, int col)
     {
          //North
         if (row > 0)
         {
             g.removeAllEdges(cellContainer[row][col], cellContainer[row - 1][col]);
             
             //NE
             if (col < colCount - 2)
                 g.removeAllEdges(cellContainer[row][col], cellContainer[row - 1][col + 1]);                
         }
 
         //East
         if (col < colCount - 2)
         {
             g.removeAllEdges(cellContainer[row][col], cellContainer[row][col + 1]);
            
             //SE
             if (row < rowCount - 2)
                 g.removeAllEdges(cellContainer[row][col], cellContainer[row + 1][col + 1]);                
         }
 
         //South
         if (row < rowCount - 2)
         {
             g.removeAllEdges(cellContainer[row][col], cellContainer[row + 1][col]);
             
             //SW
             if (col > 0)
                 g.removeAllEdges(cellContainer[row][col], cellContainer[row + 1][col - 1]);              
         }
 
         //West
         if (col > 0)
         {
             g.removeAllEdges(cellContainer[row][col], cellContainer[row][col - 1]);
             
             //NW
             if (row > 0)
                 g.removeAllEdges(cellContainer[row][col], cellContainer[row - 1][col - 1]);
                 
         }
     }
     
     /**
      * Add edges connecting this cell and its 8 (or less) neighbors
      * 
      * @param row
      * @param col 
      */
     private void addEdges(int row, int col)
     {
         //Add weighted edge
         //North
         if (row > 0)
         {
             g.addEdge(cellContainer[row][col], cellContainer[row - 1][col]);
             g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                       cellContainer[row - 1][col]),
                             1);
             //NE
             if (col < colCount - 2)
             {
                 g.addEdge(cellContainer[row][col], cellContainer[row - 1][col + 1]);
                 g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                           cellContainer[row - 1][col + 1]),
                                 Math.sqrt(2.0));
             }
         }
 
         //East
         if (col < colCount - 2)
         {
             g.addEdge(cellContainer[row][col], cellContainer[row][col + 1]);
             g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                       cellContainer[row][col + 1]),
                             1);
 
 
             //SE
             if (row < rowCount - 2)
             {
                 g.addEdge(cellContainer[row][col], cellContainer[row + 1][col + 1]);
                 g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                           cellContainer[row + 1][col + 1]),
                                 Math.sqrt(2.0));
 
             }
         }
 
         //South
         if (row < rowCount - 2)
         {
             g.addEdge(cellContainer[row][col], cellContainer[row + 1][col]);
             g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                       cellContainer[row + 1][col]),
                             1);
 
             //SW
             if (col > 0)
             {
                 g.addEdge(cellContainer[row][col], cellContainer[row + 1][col - 1]);
                 g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                           cellContainer[row + 1][col - 1]),
                                 Math.sqrt(2.0));
             }
         }
 
         //West
         if (col > 0)
         {
             g.addEdge(cellContainer[row][col], cellContainer[row][col - 1]);
             g.setEdgeWeight(g.getEdge(cellContainer[row][col], cellContainer[row][col - 1]),
                             1);
 
             //NW
             if (row > 0)
             {
                 g.addEdge(cellContainer[row][col], cellContainer[row - 1][col - 1]);
                 g.setEdgeWeight(g.getEdge(cellContainer[row][col],
                                           cellContainer[row - 1][col - 1]),
                                 Math.sqrt(2.0));
             }
         }
     }
     
     /**
      * 
      * @param x the x-coor
      * @param y the y-coor
      * @param dc 
      */
     public void disableCell(int x, int y)
     {
         int col = x / unitW;
         int row = y / unitH;
         Cell c = cellContainer[row][col];
         c.disableCell();
         deadCells.add(c);
         
         //removing all edges touching this cell
         removeEdges(row, col);
         
         //Color all pixels within this cell
         c.setMinX(col * unitW);
         c.setMinY(row * unitH);
     }
     
     public void enabbleCell(int x, int y)
     {
         int col = x / unitW;
         int row = y / unitH;
         Cell c = cellContainer[row][col];
         c.enabbleCell();
         deadCells.remove(c);
         
         //add edges to all cells touching this cell
        addEdges(row, col);
         
         //Set minX and minY
         c.setMinX(col * unitW);
         c.setMinY(row * unitH);
     }
 
     public Set<Cell> getDeadCells()
     {
         return Collections.unmodifiableSet(deadCells); 
     }
     
     /**
      * Remove all edges connecting dead cells with others
      * Call this after loading this entity from db
      */
     public void updateGraph()
     {
         for (Cell c : deadCells)
             removeEdges(c.getRow(), c.getCol());
     }
 }
