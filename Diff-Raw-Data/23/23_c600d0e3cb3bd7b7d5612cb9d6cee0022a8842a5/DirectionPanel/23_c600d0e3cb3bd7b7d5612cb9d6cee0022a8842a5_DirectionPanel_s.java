 package framework;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
 
import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 
 import utility.TextFieldWithPlaceHolder;
 
 @SuppressWarnings("serial")
 public class DirectionPanel extends JPanel {

    private BufferedImage arrow;
     private Point mousePosition;
     private float angle = 90;
     private boolean lock = false;
     private float rotation = 0;
     private TextFieldWithPlaceHolder angleIndicatorField;
 
     /**
      * The arrow animation panel for setting velocity angle
      * 
      * @author patrick.lam
      */
 
     public DirectionPanel(TextFieldWithPlaceHolder angleIndicatorField) {
 
	try {
	    // All images must be of size 190x190 and format .png
	    // Use http://www.picresize.com to resize .png
	    arrow = ImageIO.read(new File("res/side/Direction-Arrow.png"));
	} catch (final IOException ex) {
	    System.out.println("Image error.");
	}
 	this.angleIndicatorField = angleIndicatorField;
 	setLayout(new BorderLayout());
 	setBackground(Color.BLACK);
 	setBorder(BorderFactory.createCompoundBorder(new EtchedBorder(
 		EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(10, 10,
 			10, 10)));
 	addListeners();
 
     }
 
     private void addListeners() {
 
 	addMouseMotionListener(new MouseAdapter() {
 	    @Override
 	    public void mouseMoved(final MouseEvent e) {
 		mousePosition = e.getPoint();
 		if (!lock) {
 		    float x = (e.getX() - 95) / (float) 94;
 		    float y = (-e.getY() + 95) / (float) 94;
 		    angle = (float) (Math
 			    .round(Math.toDegrees(Math.atan2(y, x)) * 100.0) / 100.0);
 		    if (angle < 0) {
 			angle += 360;
 		    }
 		    // System.out.println("X: " + x + " Y: " + y);
 		    // System.out.println(lock);
 		    // System.out.println("Angle: " + getAngle() + "");
 		    angleIndicatorField.setText(angle + "");
 		}
 		repaint();
 	    }
 	});
 
 	addMouseListener(new MouseAdapter() {
 	    @Override
 	    // Cartesian lower and upper bounds = X: [-94, 94], Y: [-94, 94]
 	    public void mouseClicked(final MouseEvent e) {
 		lock = lock ? false : true;
 	    }
 	});
 
     }
 
     public void setAngle(float degrees) {
 
 	angle = degrees;
 	lock = true;
 	repaint();
 	lock = true;
     }
 
     public float getAngle() {
 	return angle;
     }
 
     @Override
     public void paintComponent(final Graphics g) {
 
 	super.paintComponent(g);
 	final Graphics2D g2d = (Graphics2D) g.create();
 
 	if (!lock) {
 	    if (mousePosition != null) {
 		final int x = mousePosition.x - 95;
 		final int y = mousePosition.y - 95;
 		rotation = (float) -Math.atan2(x, y);
 		rotation = (float) (Math.toDegrees(rotation) + 180);
 		// System.out.println(x + " " + y);
 	    }
 	    g2d.rotate(Math.toRadians(rotation), 95, 95);
 	    g2d.drawImage(arrow, 5, 5, this);
 	    g2d.dispose();
 	    return;
 	}
 
 	g2d.rotate(Math.toRadians(90 - angle), 95, 95);
 	g2d.drawImage(arrow, 5, 5, this);
 	g2d.dispose();
 	// System.out.println(angle);
     }
 
 }
