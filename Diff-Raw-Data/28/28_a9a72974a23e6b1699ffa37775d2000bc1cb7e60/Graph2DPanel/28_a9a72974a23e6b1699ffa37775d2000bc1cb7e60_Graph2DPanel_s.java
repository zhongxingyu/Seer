 import javax.swing.*; // JPanel
 import java.awt.*; // Graphics, Graphics2D
 import java.awt.geom.*; // GeneralPath
  
 /**
    A JPanel subclass that draws a function to the screen.
    @author Ryan Halbrook
    @version CS56, Spring 2013
  */
 public class Graph2DPanel extends JPanel {
     private FunctionR1R1 function = null;
     
     // Used to only redraw the graph if necessary.
     private boolean graphIsValid = false;
     // A path of the graph that can be drawn
     private GeneralPath graph = null;
 
    private double xScale = 10.0;
    private double yScale = 10.0;
 
     public Graph2DPanel(FunctionR1R1 function) {
 	super();
 	this.function = function;
     }
 
     public void paintComponent(Graphics g) {
 	Graphics2D g2 = (Graphics2D)g; // Cast for more features.
 	if (graph == null) updateGraph();
 	double height = this.getSize().getHeight();
 	AffineTransform at = new AffineTransform();
 	at.translate(0, height / 2);
	at.scale(xScale, yScale);
 	
 	g2.draw(graph.createTransformedShape(at));
     }
 
     private void updateGraph() {
 	GeneralPath gp = new GeneralPath();
 
 	// store old points to "connect" from.
 	double pX = 0;
 	double pY = 0;
 
	int evals = (int)(this.getSize().getWidth() * xScale);
 
	pX = 1;
	pY = function.evaluate(pX);
 	gp.moveTo(pX, pY);
	for (int i = 2; i < evals; i++) {
	    pX = i;
	    pY = function.evaluate(i);
 	    gp.lineTo(pX, pY);
 	}
 	graph = gp;
     }
 
 }
