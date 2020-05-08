 package teo.isgci.drawing;
 
 import java.awt.event.MouseAdapter;
 
 import com.mxgraph.swing.mxGraphComponent;
 
 /**
  * Dumbed down version of the original, WIP GraphEvent
  * TODO: replace this with the final one
  */
 class GraphEvent implements GraphEventInterface {
 
     private mxGraphComponent graphComponent;
 
     protected GraphEvent(mxGraphComponent graphComponent) {
         this.graphComponent = graphComponent;
     }
 
     /**
      * Register a MouseAdapter to receive events from the graph panel.
      *
      * @param adapter MouseAdapter
      */
     @Override
     public void registerMouseAdapter(MouseAdapter adapter) {
         graphComponent.addMouseListener(adapter);        
         graphComponent.addMouseWheelListener(adapter);
     }
 }
