 package de.konsteirama.jgraphxadapter;
 
 import java.util.HashMap;
 
 import org.jgrapht.Graph;
 import org.jgrapht.ListenableGraph;
 import org.jgrapht.event.GraphEdgeChangeEvent;
 import org.jgrapht.event.GraphListener;
 import org.jgrapht.event.GraphVertexChangeEvent;
 
 import com.mxgraph.model.mxCell;
 import com.mxgraph.model.mxGeometry;
 import com.mxgraph.view.mxGraph;
 
 /**
  * Adapter to draw a JGraphT graph with the JGraphX drawing library.
  * @author JeanYves Tinevez
  *
  * @param <V> Vertex
  * @param <E> Edge
  */
 public class JGraphXAdapter<V, E> extends mxGraph implements
         GraphListener<V, E> {
 
 	/**
 	 * The graph to be drawn.
 	 * Has vertices "V" and edges "E".
 	 */
     private Graph<V, E> graphT;
 
     /**
      * Hashmap, that maps the JGraphT-Vertices onto JGraphX-mxCells.
      * {@link #cellToVertexMap} is for the opposite direction.
      */
     private HashMap<V, mxCell> vertexToCellMap = new HashMap<V, mxCell>();
 
     /**
      * Hashmap, that maps the JGraphT-Edges onto JGraphX-mxCells.
      * {@link #cellToEdgeMap} is for the opposite direction.
      */
     private HashMap<E, mxCell> edgeToCellMap = new HashMap<E, mxCell>();
 
     /**
      * Hashmap, that maps the JGraphX-mxCells onto JGraphT-Edges.
      * {@link #edgeToCellMap} is for the opposite direction.
      */
     private HashMap<mxCell, V> cellToVertexMap = new HashMap<mxCell, V>();
 
     /**
      * Hashmap, that maps the JGraphX-mxCells onto JGraphT-Vertices.
      * {@link #vertexToCellMap} is for the opposite direction.
      */
     private HashMap<mxCell, E> cellToEdgeMap = new HashMap<mxCell, E>();
 
     /*
      * CONSTRUCTOR
      */
 
     /**
      * Constructs and draws a new ListenableGraph.
      * @param graphT casted to graph
      */
     public JGraphXAdapter(final ListenableGraph<V, E> graphT) {
         // call normal constructor with graph class
         this((Graph<V, E>) graphT);
         
         graphT.addGraphListener(this);
     }
     
     /**
      * Constructs and draws a new graph.
      * @param graphT is a graph
      * @throws IllegalArgumentException if {@link #graphT} is null
      */
     public JGraphXAdapter(final Graph<V, E> graphT) {
         super();
         
         // Don't accept null as jgrapht graph
         if (graphT == null) {
             throw new IllegalArgumentException();
         } else {
             this.graphT = graphT;
         }
         
         // generate the drawing
         insertJGraphT(graphT);
         
         setAutoSizeCells(true);
     }
 
     /*
      * METHODS
      */
 
     /**
      * Returns Hashmap which maps the vertices onto their visualization mxCells.
      * @return {@link #vertexToCellMap}
      */
     public HashMap<V, mxCell> getVertexToCellMap() {
         return vertexToCellMap;
     }
 
     /**
      * Returns Hashmap which maps the edges onto their visualization mxCells.
      * @return {@link #edgeToCellMap}
      */
     public HashMap<E, mxCell> getEdgeToCellMap() {
         return edgeToCellMap;
     }
 
     /**
      * Returns Hashmap which maps the visualization mxCells onto their edges.
      * @return {@link #cellToEdgeMap}
      */
     public HashMap<mxCell, E> getCellToEdgeMap() {
         return cellToEdgeMap;
     }
 
     /**
      * Returns Hashmap which maps the visualization mxCells onto their vertices.
      * @return {@link #cellToVertexMap}
      */
     public HashMap<mxCell, V> getCellToVertexMap() {
         return cellToVertexMap;
     }
 
     /*
      * GRAPH LISTENER
      */
 
     @Override
     public final void vertexAdded(GraphVertexChangeEvent<V> e) {
         addJGraphTVertex(e.getVertex());
     }
 
     @Override
     public final void vertexRemoved(GraphVertexChangeEvent<V> e) {
         mxCell cell = vertexToCellMap.remove(e.getVertex());
         removeCells(new Object[] { cell });
     }
 
     @Override
     public final void edgeAdded(GraphEdgeChangeEvent<V, E> e) {
         addJGraphTEdge(e.getEdge());
     }
 
     @Override
     public final void edgeRemoved(GraphEdgeChangeEvent<V, E> e) {
         mxCell cell = edgeToCellMap.remove(e.getEdge());
         removeCells(new Object[] { cell });
     }
 
     /*
      * PRIVATE METHODS
      */
 
     /**
      * Draws a new vertex into the graph.
      * 
      * @param vertex vertex to be added to the graph
      */
     private void addJGraphTVertex(V vertex) {
 
         getModel().beginUpdate();
 
         try {
             // create a new JGraphX vertex at position 0
             mxCell cell = (mxCell) insertVertex(defaultParent, null, vertex, 
                                                 0, 0, 0, 0);
             
            // Set geometry and size
            cell.setGeometry(new mxGeometry());
            cell.getGeometry().setRelative(true);
             updateCellSize(cell);
             
             // Save reference between vertex and cell
             vertexToCellMap.put(vertex, cell);
             cellToVertexMap.put(cell, vertex);
             
         } finally {
             getModel().endUpdate();
         }
     }
 
     /**
      * Draws a new egde into the graph.
      * 
      * @param edge edge to be added to the graph. Source and target
      *  vertices are needed.
      */
     private void addJGraphTEdge(E edge) {
 
         getModel().beginUpdate();
 
         try {
             // find vertices of edge
             V sourceVertex = graphT.getEdgeSource(edge);
             V targetVertex = graphT.getEdgeTarget(edge);
             
             // if the one of the vertices is not drawn, don't draw the edge
             if (!(vertexToCellMap.containsKey(sourceVertex) 
                && vertexToCellMap.containsKey(targetVertex))) {
                 return;
             }
             
             // get mxCells
             Object sourceCell = vertexToCellMap.get(sourceVertex);
             Object targetCell = vertexToCellMap.get(targetVertex);
             
             // add edge between mxcells
             mxCell cell = (mxCell) insertEdge(defaultParent, null, 
                     edge, sourceCell, targetCell);
             
            // set geometry and size
            cell.setGeometry(new mxGeometry());
            cell.getGeometry().setRelative(true);
             updateCellSize(cell);
             
             // Save reference between vertex and cell
             edgeToCellMap.put(edge, cell);
             cellToEdgeMap.put(cell, edge);
             
         } finally {
             getModel().endUpdate();
         }
     }
     
     /**
      * Draws a given graph with all its vertices and edges.
      * 
      * @param graphT the graph to be added to the existing graph.
      */
     private void insertJGraphT(Graph<V, E> graphT) {
         
         for (V vertex : graphT.vertexSet()) {
             addJGraphTVertex(vertex);
         }
 
         for (E edge : graphT.edgeSet()) {
             addJGraphTEdge(edge);
         }
 
     }
 }
