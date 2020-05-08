 package ass1;
 
 import javax.media.opengl.GL2;
 
 /**
  * A game object that has a polygonal shape.
  * 
  * This class extend GameObject to draw polygonal shapes.
  *
  * TODO: The methods you need to complete are at the bottom of the class
  *
  * @author malcolmr
  */
 public class PolygonalGameObject extends GameObject {
 
     private double[] myPoints;
     private double[] myFillColour;
     private double[] myLineColour;
 
     /**
      * Create a polygonal game object and add it to the scene tree
      * 
      * The polygon is specified as a list of doubles in the form:
      * 
      * [ x0, y0, x1, y1, x2, y2, ... ]
      * 
      * The line and fill colours can possibly be null, in which case that part of the object
      * should not be drawn.
      *
      * @param parent The parent in the scene tree
      * @param points A list of points defining the polygon
      * @param fillColour The fill colour in [r, g, b, a] form
      * @param lineColour The outlien colour in [r, g, b, a] form
      */
     public PolygonalGameObject(GameObject parent, double points[],
             double[] fillColour, double[] lineColour) {
         super(parent);
 
         myPoints = points;
         myFillColour = fillColour;
         myLineColour = lineColour;
     }
 
     /**
      * Get the polygon
      * 
      * @return
      */
     public double[] getPoints() {        
         return myPoints;
     }
 
     /**
      * Set the polygon
      * 
      * @param points
      */
     public void setPoints(double[] points) {
         myPoints = points;
     }
 
     /**
      * Get the fill colour
      * 
      * @return
      */
     public double[] getFillColour() {
         return myFillColour;
     }
 
     /**
      * Set the fill colour.
      * 
      * Setting the colour to null means the object should not be filled.
      * 
      * @param fillColour The fill colour in [r, g, b, a] form 
      */
     public void setFillColour(double[] fillColour) {
         myFillColour = fillColour;
     }
 
     /**
      * Get the outline colour.
      * 
      * @return
      */
     public double[] getLineColour() {
         return myLineColour;
     }
 
     /**
      * Set the outline colour.
      * 
      * Setting the colour to null means the outline should not be drawn
      * 
      * @param lineColour
      */
     public void setLineColour(double[] lineColour) {
         myLineColour = lineColour;
     }
 
     // ===========================================
     // COMPLETE THE METHODS BELOW
     // ===========================================
     
 
     /**
      * TODO: Draw the polygon
      * 
      * if the fill colour is non-null, fill the polygon with this colour
      * if the line colour is non-null, draw the outline with this colour
      * 
      * @.media.opengl.GL2)
      */
     @Override
     public void drawSelf(GL2 gl) {
         // TODO: Write this method
         if (myFillColour != null) {
             gl.glColor4dv(myFillColour, 0);
             gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
             drawMyPolygon(gl, GL2.GL_POLYGON);
         }
        if (myLineColour != null) {
            gl.glColor4dv(myLineColour, 0);
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
            gl.glPolygonOffset(-1f, -1f);
            gl.glLineWidth(1f);
            drawMyPolygon(gl, GL2.GL_POLYGON);
        }
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
     }
 
     private void drawMyPolygon(GL2 gl, int mode) {
         gl.glBegin(mode); {
             for (int i = 0; i < myPoints.length/2; i ++) {
                 gl.glVertex2dv(myPoints, 2*i);
             }
         } gl.glEnd();
     }
 
     public double[] getGlobalPoints() {
         double[] globalCoordinates = new double[myPoints.length];
         double[][] myGlobalTransformationMatrix = this.getGlobalMatrix();
         for (int pointIndex = 0; pointIndex < myPoints.length/2; pointIndex++) {
             int xIndex = 2 * pointIndex;
             int yIndex = xIndex + 1;
             double[] localVertexPoint = new double[3];
             localVertexPoint[0] = myPoints[xIndex];
             localVertexPoint[1] = myPoints[yIndex];
             localVertexPoint[2] = 1;
             double[] globalVertexPoint = MathUtil.multiply(myGlobalTransformationMatrix, localVertexPoint);
             globalCoordinates[xIndex] = globalVertexPoint[0];
             globalCoordinates[yIndex] = globalVertexPoint[1];
         }
         return globalCoordinates;
     }
 
 }
