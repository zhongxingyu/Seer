 package cz.cvut.indepmod.uc.modelFactory.ucGraphItemModels;
 
 import com.jgraph.components.labels.CellConstants;
 import com.jgraph.components.labels.MultiLineVertexRenderer;
 import org.jgraph.graph.GraphConstants;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.UUID;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Alena Varkockova
  * Date: 2.11.2010
  * Time: 15:00:23
  * To change this template use File | Settings | File Templates.
  */
 public class StepModel extends UCEditableVertex {
     private static final String DEFAULT_LABEL = "Step";// Resources.getResources().getString("uc.vertex.actor");
     private final UUID uuid;
     public static final int DEFAULT_INSET = 6;
 
     public StepModel(UUID uuid) {
         this.uuid = uuid;
         this.setName(DEFAULT_LABEL);
     }
 
      public StepModel(final StepModel stepModel, final String name){
         setName(name);
         uuid = stepModel.getUuid();
         setNote(stepModel.getNote());
     }
 
     @Override
     public String toString(){
         return name;
     }
 
      /**
     /**
      * Initialize new vertex attributes.
      *
      * @param point is the point where new vertex is supposed to be inserted
      * @return a proper attribute map for new vertex
      */
     public static Map installAttributes(final Point2D point) {
         final Map map = new Hashtable();
 
         map.put(CellConstants.VERTEXSHAPE, MultiLineVertexRenderer.SHAPE_ROUNDED);
 
         GraphConstants.setBounds(map, new Rectangle2D.Double(point.getX(), point.getY(), 300, 50)); // velikost 200*20
         GraphConstants.setBorderColor(map, Color.black);
         GraphConstants.setOpaque(map, true);
         GraphConstants.setHorizontalAlignment(map, SwingConstants.LEFT);
         GraphConstants.setVerticalAlignment(map, SwingConstants.CENTER);
         GraphConstants.setMoveable(map, true);
         GraphConstants.setResize(map, false);
         GraphConstants.setEditable(map, true);
         GraphConstants.setConnectable(map, false);
         GraphConstants.setSelectable(map, true);
         GraphConstants.setInset(map, DEFAULT_INSET);
         GraphConstants.setDisconnectable(map, false);
         GraphConstants.setBendable(map, false);
 
         return map;
     }
 
     public UUID getUuid() {
         return this.uuid;
     }
 }
