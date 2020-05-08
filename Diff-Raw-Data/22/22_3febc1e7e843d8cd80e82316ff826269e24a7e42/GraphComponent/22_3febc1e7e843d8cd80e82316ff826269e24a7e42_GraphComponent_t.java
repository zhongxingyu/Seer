 package it.wolfed.swing;
 
 import com.mxgraph.model.mxGeometry;
 import com.mxgraph.model.mxIGraphModel;
 import com.mxgraph.swing.handler.mxConnectionHandler;
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.util.mxEvent;
 import com.mxgraph.util.mxPoint;
 import it.wolfed.event.AutoUpdateStyleListener;
import it.wolfed.model.PetriNetGraph;
import it.wolfed.model.PlaceVertex;
import it.wolfed.model.TransitionVertex;
import it.wolfed.model.Vertex;
 import java.awt.Color;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 public class GraphComponent extends mxGraphComponent
 {
     /**
      * @param graph
      */
     public GraphComponent(PetriNetGraph graph)
     {
         super(graph);
 
         // Background
         getViewport().setOpaque(true);
         getViewport().setBackground(Color.white);
 
         // Style
         setGridVisible(true);
         setToolTips(true);
         setEnterStopsCellEditing(true);
 
         // Add listener for mouseClick
         getGraphControl().addMouseListener(createCustomMouseAdapter());
         
         // Add rightClick Menu
         setComponentPopupMenu(createCustomRightClickMenu());
         
         // Autocreate on mouseRelease
         getConnectionHandler().setCreateTarget(true);
         
         // Autoupdate Style cells on Change
         graph.getModel().addListener(mxEvent.CHANGE, new AutoUpdateStyleListener(graph));
     }
     
     @Override
     public PetriNetGraph getGraph()
     {
         return (PetriNetGraph) super.getGraph();
     }
 
    /*
     * Custom create connection handler
     */
    @Override
    protected mxConnectionHandler createConnectionHandler()
    {
         return new mxConnectionHandler(this)
         {
             /**
              * Ensure alternance on CREATE ON DROP (PlaceVertex -> TransitionVertex -> PlaceVertex)
              */
             @Override
             public Object createTargetVertex(MouseEvent e, Object source)
             {
                 Object clone = null;
 
                 // Ensure alternance
                 if (source instanceof PlaceVertex)
                 {
                     PlaceVertex place = (PlaceVertex) source;
                     String id = getGraph().nextIndexTransition();
                     clone = new TransitionVertex(place.getParent(), id, id);
                 } 
                 else if (source instanceof TransitionVertex)
                 {
                     TransitionVertex transition = (TransitionVertex) source;
                     String id = getGraph().nextIndexPlaces();
                     clone = new PlaceVertex(transition.getParent(), id, id);
                 }
 
                 mxIGraphModel model = graph.getModel();
                 mxGeometry geo = model.getGeometry(clone);
 
                 if (geo != null)
                 {
                     mxPoint point = graphComponent.getPointForEvent(e);
                     geo.setX(graph.snap(point.getX() - geo.getWidth() / 2));
                     geo.setY(graph.snap(point.getY() - geo.getHeight() / 2));
                 }
 
                 return clone;
             }
 
             /**
              * Avoid linkable edge from same-type class (es: TransitionVertex -> TransitionVertex )
              */
             @Override
             public String validateConnection(Object source, Object target)
             {
                 if (target == null && createTarget)
                 {
                     return null;
                 }
 
                 if (!isValidTarget(target))
                 {
                     return "";
                 }
 
                 // Disable same-class link
                 if (source.getClass().equals(target.getClass()))
                 {
                     return "Alternance for Places and Transitions failed!";
                 }
 
                 // Disable link to a pre-set
                 //TODO
 
                 return graphComponent.getGraph().getEdgeValidationError(
                         connectPreview.getPreviewState().getCell(), source, target);
             }
         };
    }
    
    /*
     * Custom Handler
     */
     private MouseAdapter createCustomMouseAdapter()
     {
         return new MouseAdapter()
         {
             @Override
             public void mouseClicked(MouseEvent e)
             {
                 if (e.getButton() == MouseEvent.BUTTON3)
                 {
                     Object cell = getCellAt(e.getX(), e.getY());
 
                     if (cell != null)
                     {
                         // Se è una Piazza o una Transizione rimuove tutti gli archi
                         if(cell instanceof Vertex)
                         {
                             for(Object arc : graph.getEdges(cell))
                             {
                                 graph.getModel().remove(arc);
                             }
                         }
                         
                         graph.getModel().remove(cell);
                     }
                     else
                     {
                         getComponentPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                     }
                 }
             }
         };
     }
 
     private JPopupMenu createCustomRightClickMenu()
     {
         // ADD PLACE
         JPopupMenu menu = new JPopupMenu();
              
         JMenuItem placeItem = new JMenuItem("Add Place");
         placeItem.addMouseListener(new MouseAdapter() 
         {
             @Override
             public void mousePressed(MouseEvent e) 
             {                
                 String id = getGraph().nextIndexPlaces();
                 PlaceVertex place = new PlaceVertex(graph.getDefaultParent(), id, id);
                 
                 // Set location (-xy for center on mouseclick)
                 place.getGeometry().setX(e.getLocationOnScreen().getX() - 20);
                 place.getGeometry().setY(e.getLocationOnScreen().getY() - 100);
                 
                 graph.addCell(place);
             }
         });
         menu.add(placeItem);
         
         // ADD TRANSITION
         JMenuItem transItem = new JMenuItem("Add Transition");
         transItem.addMouseListener(new MouseAdapter() 
         {
             @Override
             public void mousePressed(MouseEvent e) 
             {                
                 String id = getGraph().nextIndexTransition();
                 TransitionVertex transition = new TransitionVertex(graph.getDefaultParent(), id, id);
                 
                 // Set location (-xy for center on mouseclick)
                 transition.getGeometry().setX(e.getLocationOnScreen().getX() - 20);
                 transition.getGeometry().setY(e.getLocationOnScreen().getY() - 100);
                 
                 graph.addCell(transition);
             }
         });
         
         menu.add(transItem);
         
         return menu;
     }
 }
