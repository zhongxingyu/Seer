 import javax.swing.*; // JPanel
 import java.awt.*; // Graphics, Graphics2D
 import java.awt.geom.*; // GeneralPath
 import java.awt.event.*;
  
 /**
    A JPanel subclass that draws a function to the screen.
    @author Ryan Halbrook
    @version CS56, Spring 2013
  */
 public class Graph2DPanel extends JPanel implements ActionListener {
     private FunctionR1R1 function = null;
     private Bounds2DFloat bounds;
 
     // Used to only redraw the graph if necessary.
     private boolean graphIsValid = false;
     // A path of the graph that can be drawn
     private GeneralPath graph = null;
 
     private float xScale = 10.0f;
     private float yScale = 10.0f;
 
     private boolean debug = false;
 
     /**
        Constructs a new object that will graph the supplied function
        within the given bounds on the function. When the function is
        redrawn it will use this same object.
        @param function the function to graph.
        @param b the rectangular boundary that determines how much of the graph will be shown.
      */
     public Graph2DPanel(FunctionR1R1 function, Bounds2DFloat b) {
 	super();
 	this.function = function;
 	this.bounds = b;
 	if(this.bounds == null) {
 	    this.bounds = new Bounds2DFloat();
 	}
 	this.bounds.addActionListener(this);
     }
 
     /**
        Tell the graph to redraw itself, usually because the
        rendering information, such as the bounds has changed.
      */
     public void refresh() {
 	graphIsValid = false;
 	repaint();
     }
 
     /**
        Paints the component onscreen. Only re-renders from scratch if
        graphIsValid is false. This should not be called directly. 
        Use repaint().
        @param g the graphics object which will perform onscreen drawing.
      */
     public void paintComponent(Graphics g) {
 	Graphics2D g2 = (Graphics2D)g; // Cast for more features.
 	g2.setColor(Color.WHITE);
	g2.fillRect(0, 0, ((int)this.getSize().getWidth()-1),
		    (int)(this.getSize().getHeight()-1));
 	g2.setColor(Color.BLACK);
 	drawAxes(g2);
 
 	if (graph == null || !graphIsValid) updateGraph();
 	double height = this.getSize().getHeight();
 	AffineTransform at = new AffineTransform();
 	at.translate(0, height / 2);
 	
 	g2.draw(graph.createTransformedShape(at));
     }
 
     /**
        Draws a set of axes with the the graphics object g.
        @param g the destination for drawing.
      */
     private void drawAxes(Graphics g) {
         float width = (float)this.getSize().getWidth();
 	float height = (float)this.getSize().getHeight();
 
 	// Draw the x axis
 	g.drawLine(0, (int)(height / 2), (int)width, (int)(height / 2));
 
 	// Draw the y axis
 	g.drawLine(0, 0, 0, (int)width);
     }
 
     /**
        Created the path for the graph.
      */
     private void updateGraph() {
 	GeneralPath gp = new GeneralPath();
 
 	// store old points to "connect" from.
 	double pX = 0;
 	double pY = 0;
 	
 	int ptsCount = 0;
 
 	
 	
 	this.xScale = (float)(this.getSize().getWidth()) / (bounds.getXMax() - bounds.getXMin());
 	this.yScale = (float)(this.getSize().getHeight()) / (bounds.getYMax() - bounds.getYMin());
 	float lastX = (float)(this.getSize().getWidth() / xScale);
 
 	pX = 0;
 	pY = function.evaluate(pX) * yScale;
 	ptsCount++;
 	gp.moveTo(pX, pY);
 	for (float i = (1/xScale); i < lastX; i+=(1 / xScale)) {
 	    pX = i * xScale;
 	    pY =  -(function.evaluate(i) * yScale);
 	    ptsCount++;
 	    gp.lineTo(pX, pY);
 	}
 	if (debug) System.out.println("Points rendered: " + ptsCount);
 	graph = gp;
     }
 
     /**
        Call back for the event that the bounds are changed.
        @param e the ActionEvent object.
      */
     public void actionPerformed(ActionEvent e) {
 	this.refresh();
     }
 
 }
