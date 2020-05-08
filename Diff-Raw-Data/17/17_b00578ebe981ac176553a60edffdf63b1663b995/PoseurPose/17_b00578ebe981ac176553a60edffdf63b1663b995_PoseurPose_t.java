 package poseur.state;
 
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.util.Iterator;
 import java.util.LinkedList;
 import poseur.shapes.PoseurLine;
 import poseur.shapes.PoseurShape;
 
 /**
  * This class stores all the information for a pose, including
  * its dimensions and all the shapes to be rendered to 
  * represent it.
  * 
  * @author  Richard McKenna
  *          Debugging Enterprises
  * @version 1.0
  */
 public class PoseurPose 
 {
     // HERE'S THIS POSE'S DIMENSIONS
     private int poseWidth;
     private int poseHeight;
     
     // AND THESE ARE ALL THE SHAPES TO RENDER FOR IT
     private LinkedList<PoseurShape> shapesList;
     
     /**
      * This constructor fully sets up a blank pose using the
      * provided dimensions. Note that we only need to construct
      * the shapes list once. From here on out we'll just add
      * things to it and move things around inside.
      * 
      * @param initPoseWidth The width of the image in pixels
      * that the pose is to represent when exported.
      * 
      * @param initPoseHeight The height of the image in pixels
      * that the pose is to represent when exported.
      */
     public PoseurPose(int initPoseWidth, int initPoseHeight)
     {
         // KEEP THE DIMENSIONS
         poseWidth = initPoseWidth;
         poseHeight = initPoseHeight;
         
         // AND GET OUR SHAPES LIST READY
         shapesList = new LinkedList();
     }
     
     // ACCESSOR METHODS
     
     /**
      * Accessor method for getting the pose width.
      * 
      * @return The width of this pose, in pixels.
      */
     public int getPoseWidth() { return poseWidth; }
     
     /**
      * Accessor method for getting the pose height.
      * 
      * @return The height of this pose, in pixels.
      */
     public int getPoseHeight() { return poseHeight; }
     
     /**
      * Accesoor method for getting this pose's shape list.
      * 
      * @return The pose list with all the shapes used
      * to render this pose.
      */
     public LinkedList<PoseurShape> getShapesList()
     {
         return shapesList;
     }
     
     /**
      * Accessor method for getting an iterator for going
      * through the shapes list.
      * 
      * @return An iterator for going through the shapes list.
      */
     public Iterator<PoseurShape> getShapesIterator()
     {
         return shapesList.iterator();
     }    
 
     // MUTATOR METHODS
     
     /**
      * Mutator method for setting the width of this pose. Note that changing
      * this value might put some shapes partially or fully outside of the
      * pose, which means they wouldn't be rendered.
      * 
      * @param initWidth The new width for the pose.
      */
     public void setPoseWidth(int initWidth) { poseWidth = initWidth; }
     
     /**
      * Mutator method for setting the height of this pose. Note that changing
      * this value might put some shapes partially or fully outside of the
      * pose, which means they wouldn't be rendered.
      * 
      * @param initHeight The new height for the pose.
      */
     public void setPoseHeight(int initHeight) { poseHeight = initHeight; }
     
     /**
      * This method adds a shape to the list of shapes to 
      * be rendered as part of the pose.
      * 
      * @param shapeToAdd The shape to add to the pose.
      */
     public void addShape(PoseurShape shapeToAdd)
     {
         shapesList.add(shapeToAdd);
     }  
 
     /**
      * This method removes a shape from the list of shapes to
      * be rendered as part of this pose.
      * 
      * @param shapeToRemove The shape to remove from the pose.
      */
     public void removeShape(PoseurShape shapeToRemove)
     {
         shapesList.remove(shapeToRemove);
     }
 
     /**
      * This method removes all the shapes from the shape
      * list, resetting the pose. It does not change the
      * pose dimensions, however.
      */
     public void reset()
     {
         shapesList.clear();
     }
      
     /**
      * This method loads the poseData argument's data
      * into this pose object. We do this because when
      * we load a pose from a .pose file, we don't want
      * to end up with a partially loaded pose when 
      * an error happens. Plus, other classes might have
      * references to this pose object and we don't want
      * to chase them all down if we reassign one of their
      * pose variables.
      * 
      * @param poseData Pose data to load into this pose.
      */
     public void loadPoseData(PoseurPose poseData)
     {
         // JUST COPY IT ALL IN, CLEARING OUT ALL OLD SHAPES
         poseWidth = poseData.poseWidth;
         poseHeight = poseData.poseHeight;
         shapesList.clear();
         for (PoseurShape shape : poseData.shapesList)
         {
             shapesList.add(shape);
         }
     }   
     
     /**
      * This method looks through the shapes in the pose,
      * starting with the top-most ones, which are at the
      * end of the list, and returns the first one that
      * contains the point x, y. If no shape is found,
      * null is returned.
      * 
      * @param x Coordinate of test point in x-axis.
      * 
      * @param y Coordinate of test point in y-axis.
      * 
      * @return The first shape on top that contains the 
      * test point, null if no shape is found to contain
      * the point.
      */
     public PoseurShape findShapeWithPoint(int x, int y)
     {
         // WE'RE LOOKING FOR A SHAPE THAT CONTAINS THIS POINT
         Point2D.Double testPoint = new Point2D.Double(x, y);
         
         // GO THROUGH ALL THE SHAPES STARTING FROM THE TOP
         for (int i = shapesList.size()-1; i >= 0; i--)
         {
             // TEST IT
             PoseurShape testShape = shapesList.get(i);
             if (testShape.containsPoint(testPoint))
             {
                 // FOUND!
                 return testShape;
                 //need this to work with lines too
             }else if (testShape instanceof PoseurLine){
                 //cast to line so i can use its methods
                 PoseurLine testLine = (PoseurLine) testShape;
                 //return the shape if the mouse is resonably close
                if (testLine.intersects(x-2, y-2, 5, 5)) {
                     return testShape;
                 }
             }
         }
         return null;
     }
 }
