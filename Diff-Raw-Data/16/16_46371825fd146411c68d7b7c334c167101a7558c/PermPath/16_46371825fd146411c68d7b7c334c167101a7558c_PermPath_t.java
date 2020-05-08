 package kniemkiewicz.jqblocks.ingame.controller.ai.paths;
 
 import kniemkiewicz.jqblocks.ingame.controller.CollisionController;
 import kniemkiewicz.jqblocks.util.GeometryUtils;
 import kniemkiewicz.jqblocks.util.SerializableBeanProxy;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 import java.io.Serializable;
 
 /**
  * User: krzysiek
  * Date: 25.10.12
  */
 public final class PermPath implements Serializable {
 
   Path path;
   Position start; // if start != path.getStart(), it means that path could not be computed.
   Position endPosition;
 
   SerializableBeanProxy<PathGraph> pathGraph;
   SerializableBeanProxy<CollisionController> collisionController;
 
   public PermPath(Path path, Position endPosition, PathGraph pathGraph, CollisionController collisionController) {
     this.path = path;
     this.endPosition = endPosition;
     this.start = path.getStart();
     this.pathGraph = SerializableBeanProxy.getInstance(pathGraph);
     this.collisionController = SerializableBeanProxy.getInstance(collisionController);
   }
 
   public Path safeGetPath() {
     boolean pathDirty = false;
     if (start.getEdge().deleted()) {
       Edge edge = start.getEdge();
       float pos = start.getPosition();
       Rectangle r = GeometryUtils.getRectangleCenteredOn(edge.getXFor(pos), edge.getYFor(pos), 10);
       start = pathGraph.get().getClosestPoint(r);
       if (start == null) {
         return null;
       }
       boolean sameLine = GeometryUtils.lineEquals(edge.getShape(), start.getEdge().getShape());
       if (sameLine) {
         start.setPosition(pos);
       }
       pathDirty = true;
     }
     if (endPosition != null && endPosition.getEdge() != null && endPosition.getEdge().deleted()) {
       Edge edge = endPosition.getEdge();
       float pos = endPosition.getPosition();
       Rectangle r = GeometryUtils.getRectangleCenteredOn(edge.getXFor(pos), edge.getYFor(pos), 10);
       endPosition = pathGraph.get().getClosestPoint(r);
       if (endPosition == null) {
         return null;
       }
       boolean sameLine = GeometryUtils.lineEquals(edge.getShape(), endPosition.getEdge().getShape());
       if (sameLine) {
         endPosition.setPosition(pos);
       }
       pathDirty = true;
     }
    if (endPosition == null) {
      path = null;
      return null;
    }
     // Maybe add some change data about last change of graph so that we know when it is worth to search again if
     // path == null
     if (pathDirty || path == null || (start.getEdge() != path.getStart().getEdge())) {
       Path newPath = pathGraph.get().getPath(start, endPosition);
       if (newPath == null) return null;
       path = newPath;
     }
     return path;
   }
 
   // Should be called only after safeGetPath()
   public boolean outsideGraph() {
     if (start == null) return true;
     return start.getEdge().deleted();
   }
 
   public void switchDestinationTo(Position destination) {
     this.endPosition = destination;
     path = pathGraph.get().getPath(start, endPosition);
   }
 
   public Path fastGetPath() {
     return path;
   }
 
   public Vector2f getStartPoint() {
     return path.getStart().getPoint();
   }
 }
