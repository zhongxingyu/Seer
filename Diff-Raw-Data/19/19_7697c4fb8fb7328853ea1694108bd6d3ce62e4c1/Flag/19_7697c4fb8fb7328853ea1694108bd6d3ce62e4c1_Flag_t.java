 import java.awt.*;
 import javax.swing.*;
 
 public class Flag extends JFrame {
 
     //frame has a vertical inset that extends above the frame
     public static int verticalInset = 22;
     
     public static int flagHeight = 500;
     public static int flagWidth = (int) (1.9*flagHeight);
 
     public static int blueHeight = (int) (0.5385*flagHeight);
     public static int blueWidth = (int) (0.76*flagHeight);
 
     public static double starWidth = 0.0616;
 
     public static int stripeHeight = (int) (0.0769*flagHeight);
     public static int stripeWidth = flagWidth;
 
     public static int starFromTop = (int) (0.054*flagHeight);
     public static int starFromSide = (int) (0.063*flagHeight);
     public static int starFromStarVert = starFromTop;
     public static int starFromStarHoriz = starFromSide;
 
     public Flag() {
 	setBackground(Color.WHITE);
	setSize(flagWidth, flagHeight-14);
 	repaint();
     }
 
     public void paint(Graphics g) {
 	Dimension size = getSize();
	flagHeight = (int) size.getHeight();
	flagWidth = (int) (1.9*flagHeight);
	blueHeight = (int) (0.5385*flagHeight);
	blueWidth = (int) (0.76*flagHeight);
	starWidth = 0.0616;
	stripeHeight = (int) (0.0769*flagHeight);
	stripeWidth = flagWidth;
	starFromTop = (int) (0.054*flagHeight);
	starFromSide = (int) (0.063*flagHeight);
	starFromStarVert = starFromTop;
	starFromStarHoriz = starFromSide;

	g.setColor(Color.WHITE);
	g.fillRect(0, 0, (int) size.getWidth(), (int) size.getHeight());
 
 	stripeLoop(g, Color.RED);
 	paintBlue(g, 0, 10);
 	starLoop(g, starWidth);
     }
 
     public void starLoop(Graphics g, double width) {
 	g.setColor(Color.WHITE);
 	for (int i = 0; i < 9; i++) {
 	    if (i%2 == 0) {
 		// odd row
 		for (int j = 0; j < 6; j++) {
 		    paintStar(g, width, starFromSide + 2*j*starFromStarHoriz, starFromTop + i*starFromStarVert);
 		}
 	    }
 	    else {
 		// even row
 		for (int j = 0; j < 5; j++) {
 		    paintStar(g, width, 2*starFromSide + 2*j*starFromStarHoriz, starFromTop + i*starFromStarVert);
 		}
 	    }
 	}
     }
 
     public void stripeLoop(Graphics g, Color color) {
 	for (int i = 1; i < 8; i++)
 	    paintStripe(g, color, 0, (flagHeight/7) * (i-1)+verticalInset);
     }
 
     public void paintStripe(Graphics g, Color color, int x, int y) {
 	g.setColor(color);
 	g.fillRect(x, y, stripeWidth, stripeHeight);
     }
 
     public Star scaleStar(Star s, double t) {
 	for (int i = 0; i < s.x.length; i++) {
 	    s.x[i] = (int) (t*s.x[i]);
 	    s.y[i] = (int) (t*s.y[i]);
 	}
 	return s;
     }
 
     public Star translateStar(Star s, int x, int y) {
 	for (int i = 0; i < s.x.length; i++) {
 	    s.x[i] = s.x[i]+x;
 	    s.y[i] = s.y[i]+y;
 	}
 	return s;
     }
 
     public void paintBlue(Graphics g, int x, int y) {
 	g.setColor(Color.BLUE);
 	g.fillRect(x,y,blueWidth,blueHeight);
     }
 
     public void paintStar(Graphics g, double scale, int x, int y) {
 	//base coords for Stars
 	int[] xcoords = {0, 162, 212, 262, 424, 293, 343, 212, 81, 131};
 	int[] ycoords = {154, 154, 0, 154, 154, 249, 403, 308, 403, 249};
 
 	//create Star from coordinates
 	Star s = new Star(xcoords, ycoords);
 
 	//transform to 1/10 of size
 	s = scaleStar(s, scale);
 
 	//translate based on given coordinates
 	s = translateStar(s, x, y);
 
 	g.setColor(Color.WHITE);
 	g.fillPolygon(s.x, s.y, s.x.length);
     }
 }
