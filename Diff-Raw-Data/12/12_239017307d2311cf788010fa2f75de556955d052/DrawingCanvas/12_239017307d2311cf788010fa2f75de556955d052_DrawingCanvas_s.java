 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.Vector;
 
 import javax.swing.JComponent;
 
 /**
  * Main view of the MiniDraw program which serves a both a view and a model.
  * The model component is specified below, and the view component displays the
  * image contents of the model.
  *
  * To prevent screen flicker when drawing or resizing, an ImageBuffer is used.
  * All drawing from the associated tools is executed on imageBuffer, which is
  * drawn to screen on updates which are specified by the tools.
  */
 @SuppressWarnings("serial")
 public class DrawingCanvas extends JComponent {
 
   /* Class member variables */
   public final Color BACKGROUND = Color.white;
   protected DrawingCanvasController DCcontroller;
   protected Image imageBuffer;
   protected Graphics imageBufferGraphics;
   protected int canvasWidth;
   protected int canvasHeight;
   protected Color penColor = Color.black;
   protected Tool currentTool;
   protected Vector<DrawnObject> drawnList;
 
   /****< Constructor >*********************************************************/
   /**
    * Creates a default DrawingCanvas with a white background
    */
   public DrawingCanvas() {
 	drawnList.clear();
     setBackground( BACKGROUND );
     DCcontroller = createDrawingCanvasController();
     addDrawingCanvasListener(DCcontroller);
   }
 
   /****< Factory Methods >*****************************************************/
 
   protected DrawingCanvasController createDrawingCanvasController() {
       return new DrawingCanvasController(this);
   }
 
   /****< Listener Register Methods >*******************************************/
   protected void addDrawingCanvasListener(DrawingCanvasController listener) {
     if( listener != null ) {
       addMouseListener((MouseListener) listener);
       addMouseMotionListener((MouseMotionListener) listener);
       addKeyListener((KeyListener) listener );
     }
   }
 
   /****< Drawing Methods >*****************************************************/
   /* (non-Javadoc)
    * @see javax.swing.JComponent#update(java.awt.Graphics)
    */
   public void update(Graphics g){
      paint(g);
   }
 
   /* (non-Javadoc)
    *
    * Painting the DrawingCanvas is simply displaying the contents of the
    * imageBuffer.
    *
    * @see javax.swing.JComponent#paint(java.awt.Graphics)
    */
   public void paint(Graphics g) {
      g.drawImage(imageBuffer,0, 0, this);
   }
 
 
   /**
    * Paints over the drawing canvas in the background color
    */
   public void clearCanvas() {
 	drawnList.clear();
 	imageBufferGraphics.setColor(BACKGROUND);
 	imageBufferGraphics.fillRect(0, 0, canvasWidth, canvasHeight);
 	imageBufferGraphics.setColor(penColor);
 	repaint();
   }
 
   /****< Accessor and Update Methods >*****************************************/
 
   /**
    * Updates the current drawing color
    *
    * @param c new drawing color
    */
   public void setpenColor(Color c) {
 	  if( c != null ) {
         penColor = c;
         imageBufferGraphics.setColor(c);
 	  }
   }
 
   /**
    * Accessor method for current drawing color
    *
    * @return current drawing color
    */
   public Color getpenColor() {
     return penColor;
   }
 
   /**
    * Updates current drawing tool
    *
    * @param t new drawing tool
    */
   public void setcurrentTool(Tool t)  {
 	if( t != null )
       currentTool = t;
   }
 
   /**
    * Accessor method for current drawing tool
    *
    * @return current drawing tool
    */
   public Tool getcurrentTool() {
     return currentTool;
   }
 
   /**
    * Accessor method for imageBuffer
    *
    * @return current image buffer graphics context
    */
   public Graphics getimageBufferGraphics() {
     return imageBufferGraphics;
   }
 
 
   /* (non-Javadoc)
    *
    * Adjusts the size of the DrawingCanvas as well as the imageBuffer to match
    * the new DrawingCanvas size.
    *
    * @see java.awt.Component#setBounds(int, int, int, int)
    */
   public void setBounds(int x, int y, int width, int height) {
     Image newimageBuffer = createImage(width, height);
     imageBufferGraphics = newimageBuffer.getGraphics();
     if (imageBuffer != null) {
       imageBufferGraphics.drawImage(imageBuffer, 0, 0 ,this);
     }
     imageBuffer = newimageBuffer;
     setpenColor(penColor);
     super.setBounds(x, y, width, height);
     repaint();
     canvasWidth = width;
     canvasHeight = height;
   }
   
   public void addDrawnObject(DrawnObject obj){
	 drawnList.add(obj);
   }
 }// end public class DrawingCanvas extends JComponent
