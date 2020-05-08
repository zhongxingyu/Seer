 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 /**
    A widget for changing a bounding rectangle.
    @author Ryan Halbrook
    @version CS56, S13, Project
  */
 
 public class Grapher2DBoundsPanel extends JPanel implements ActionListener {
     private JTextField xMinField, yMinField, xMaxField, yMaxField;
     private JLabel xLabel = new JLabel(" <= x <= ");
     private JLabel yLabel = new JLabel(" <= y <= ");
     private Bounds2DFloat bounds;
 
     /**
        Creates a new object with b as the rectangular boundary
        to synchronize with.
        @param b the rectangular boundary.
      */
     public Grapher2DBoundsPanel(Bounds2DFloat b) {
 	super();
 	this.bounds = b;
 	bounds.addActionListener(this);
 	this.setLayout(new GridLayout(2,0));
 	TextChangeListener t = new TextChangeListener();
 	xMinField = new JTextField("" + bounds.getXMin());
 	yMinField = new JTextField("" + bounds.getYMin());
 	xMaxField = new JTextField("" + bounds.getXMax());
 	yMaxField = new JTextField("" + bounds.getYMax());
 	xMinField.addActionListener(t);
 	yMinField.addActionListener(t);
 	xMaxField.addActionListener(t);
 	yMaxField.addActionListener(t);
 	
 	xLabel.setHorizontalAlignment(SwingConstants.CENTER);
 	yLabel.setHorizontalAlignment(SwingConstants.CENTER);
 	
 	this.add(xMinField);
 	this.add(xLabel);
 	this.add(xMaxField);
 	this.add(yMinField);
 	this.add(yLabel);
 	this.add(yMaxField);
     }
 
     /**
        Call back for the event that the bounds have changed.
      */
     public void actionPerformed(ActionEvent e) {
 	xMaxField.setText(""+bounds.getXMax());
 	yMaxField.setText(""+bounds.getYMax());
 	xMinField.setText(""+bounds.getXMin());
 	yMinField.setText(""+bounds.getYMin());
     }
 
     /**
        Inner class for a call back when the text inside of a text
        field gets changed.
      */
     public class TextChangeListener implements ActionListener {
 	/** Call back for when a value in a text field is changed. 
 	 @param e the event object */
 	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == xMinField) {
		bounds.setXMin(new Float(xMinField.getText()));
	    } else if (e.getSource() == yMinField) {
		bounds.setYMin(new Float(yMinField.getText()));
	    } else if (e.getSource() == xMaxField) {
		bounds.setXMax(new Float(xMaxField.getText()));
	    } else {
		bounds.setYMax(new Float(yMaxField.getText()));
	    }
 	}
     }
     
 }
