 package sofia.graphics.internal;
 
 import java.util.HashSet;
 import sofia.graphics.ResolvableGeometry;
 import sofia.graphics.Shape;
 import android.graphics.RectF;
 import android.util.Log;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import sofia.internal.HashGraph;
 
 // -------------------------------------------------------------------------
 /**
  * Performs a topological sort of a shape collection based on the dependencies
  * between their bounding boxes. This class is used by the canvas's layout
  * algorithm to ensure that dependent shapes are laid out before the shapes
  * that depend on them.
  *
  * @author  Tony Allevato
  * @version 2011.11.25
  */
 public class ShapeSorter
 {
     //~ Instance/static variables .............................................
 
     private Collection<Shape> collection;
     private HashGraph<Shape> graph;
     private List<Shape> sorted;
 
 
     //~ Constructors ..........................................................
 
     // ----------------------------------------------------------
     /**
      * Creates a new ShapeSorter for the specified collection of shapes.
      *
      * @param collection the collection of shapes to sort
      */
     public ShapeSorter(Collection<Shape> collection)
     {
         this.collection = collection;
         sort();
     }
 
 
     //~ Methods ...............................................................
 
     // ----------------------------------------------------------
     /**
      * Gets the list of shapes in topologically sorted order.
      *
      * @return the list of shapes in topologically sorted order
      */
     public List<Shape> sorted()
     {
         return sorted;
     }
 
 
     // ----------------------------------------------------------
     private void sort()
     {
         sorted = new ArrayList<Shape>(collection.size());
         graph = new HashGraph<Shape>();
 
         LinkedList<Shape> queue = new LinkedList<Shape>();
 
         // Construct a graph that represents the dependencies between shapes.
         // An edge from shape1 to shape2 means that shape1's bounding box is
         // dependent on shape2's bounding box.
 
         for (Shape shape : collection)
         {
             Collection<Shape> dependencies = getLayoutDependencies(shape);
 
             graph.addVertex(shape);
 
             for (Shape dependsOn : dependencies)
             {
                 graph.addEdge(dependsOn, shape);
             }
         }
 
         for (Shape shape : graph.sourceVertexSet())
         {
             queue.add(shape);
         }
 
         while (!queue.isEmpty())
         {
             Shape s = queue.removeFirst();
             sorted.add(s);
 
             for (Shape dependsOn : new HashSet<Shape>(graph.outVertexSet(s)))
             {
                 graph.removeEdge(s, dependsOn);
 
                 if (graph.isSource(dependsOn))
                 {
                     queue.addLast(dependsOn);
                 }
             }
         }
 
         if (sorted.size() < collection.size())
         {
             Log.e("ShapeCanvas", "There was a cycle in the shape layout "
                 + "dependencies that you provided. Some of your shapes may "
                 + "not appear as expected.");
         }
     }
 
 
     // ----------------------------------------------------------
     private Set<Shape> getLayoutDependencies(Shape shape)
     {
         RectF bounds = shape.getBounds();
 
         if (bounds instanceof ResolvableGeometry)
         {
            return ((ResolvableGeometry) bounds).getShapeDependencies();
         }
         else
         {
             return Collections.<Shape>emptySet();
         }
     }
 }
