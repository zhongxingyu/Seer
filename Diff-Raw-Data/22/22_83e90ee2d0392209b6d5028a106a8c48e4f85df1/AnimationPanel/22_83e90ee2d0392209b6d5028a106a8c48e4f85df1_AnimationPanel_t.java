 package view.window;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.RenderingHints;
 
 import javax.swing.JPanel;
 
 import model.Ball;
 import model.Board;
 import model.RotatablePoint;
 import model.gizmos.Flipper;
 import model.gizmos.IGizmo;
 import model.gizmos.RightFlipper;
 import model.gizmos.TriangleBumper;
 
 public class AnimationPanel extends JPanel {
 	private Board board;
 	private Boolean editMode;
 
 	private Image bufferImage;
 	private int validSquareX;
 	private int validSquareY;
 	private int validSquareW;
 	private int validSquareH;
 	private Color validColor;
 	private boolean locationIndicator;
 	private double xscale;
 	private double yscale;
 
 	public AnimationPanel(Board board) {
 		locationIndicator = false;
 		this.board = board;
 		this.setBackground(Color.black);
 		int width = board.getWidth() * Board.L;
 		int height = board.getHeight() * Board.L;
 		this.setSize(width, height);
 		this.setPreferredSize(new Dimension(width, height));
 		this.setMaximumSize(new Dimension(width, height));
 		this.setMinimumSize(new Dimension(width, height));
 		this.editMode = true;
 	}
 
 	public double getScaleX() {
 		return xscale;
 	}
 
 	public double getScaleY() {
 		return yscale;
 	}
 	
 	@Override
 	public void paint(Graphics g) {
 		if ((bufferImage == null) || (bufferImage.getWidth(null) != getWidth())
 				|| (bufferImage.getHeight(null) != getHeight())) { // start without resize
 			bufferImage = createImage(getWidth(), getHeight());
 		}
 
 		int boardWidth = board.getWidth();
 		int boardHeight = board.getHeight();
 		xscale = Math.round(this.getWidth() / boardWidth);
 		yscale = Math.round(this.getHeight() / boardHeight);
 
 		Graphics buffer = bufferImage.getGraphics();
 		buffer.setColor(Color.black);
 		buffer.fillRect(0, 0, (int)Math.round((boardWidth * xscale)), (int)Math.round((boardHeight * yscale)));
 		((Graphics2D) buffer).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		buffer.setColor(Color.gray);
 
 		if (editMode) {
 			for (int i = 0; i < boardHeight; i++) {
 				buffer.drawLine(0, (int) Math.round((i * yscale)), this.getWidth(), (int)Math.round((i * yscale)));
 			}
 
 			for (int i = 0; i < boardWidth; i++) {
 				buffer.drawLine((int)Math.round((i * xscale)), 0, (int)Math.round((i * xscale)), this.getHeight());
 			}
 		}
 
 		int x, y, w, h;
 
 		for (IGizmo gizmo : board.getGizmos()) {
 			x = (int) (xscale * gizmo.getX());
 			y = (int) (yscale * gizmo.getY());
 			w = (int) (xscale * gizmo.getWidth());
 			h = (int) (yscale * gizmo.getHeight());
 
 			switch (gizmo.getType()) {
 			case SquareBumper:
 				buffer.setColor(Color.RED);
 				buffer.fillRect(x, y, w, h);
 				break;
 
 			case CircleBumper:
 				buffer.setColor(Color.GREEN);
 				buffer.fillOval(x, y, w, h);
 				break;
 
 			case TriangleBumper:
 				buffer.setColor(Color.BLUE);
 				int xTrianglePoints[][] = { { x, x + w, x, x },
 						{ x, x + w, x, x }, { x, x + w, x + w, x },
 						{ x + w, x + w, x, x + w } };
 
 				int yTrianglePoints[][] = { { y, y + h, y + h, y },
 						{ y, y, y + h, y }, { y, y, y + h, y },
 						{ y, y + h, y + h, y } };
 				int orientation = ((TriangleBumper) gizmo).getOrientation();
 				buffer.fillPolygon(xTrianglePoints[orientation],
 						yTrianglePoints[orientation], 4);
 				break;
 
 			case Absorber:
 				buffer.setColor(Color.MAGENTA);
 				buffer.fillRect(x, y, w, h);
 				break;
 
 			case Flipper:
 				flipper((Flipper)gizmo, buffer);
 				break;
 			}
 
 		}
 
 		buffer.setColor(Color.BLUE);
 
 		for (Ball ball : board.getBalls()) {
 			x = (int) ((ball.getX() - ball.getRadius()) * xscale);
 			y = (int) ((ball.getY() - ball.getRadius()) * yscale);
			w = (int) (ball.getRadius() * xscale);
			h = (int) (ball.getRadius() * yscale);
			
			System.out.println("Ball x, y, w, h, xscale, yscale: " + x + ", " + y + ", " + w + ", " + h + ", " + xscale + ", " + yscale);
 
 			buffer.fillOval(x, y, w, h);
 		}
 
 		if (locationIndicator) {
 			buffer.setColor(validColor);
 			buffer.drawRect((int)Math.round(validSquareX * xscale), (int)Math.round(validSquareY * yscale),
 					(int)Math.round(validSquareW * xscale), (int)Math.round(validSquareH * yscale));
 		}
 
 		g.drawImage(bufferImage, 0, 0, null);
 	}
 
 	private void flipper(Flipper flipper, Graphics g)
 	{
 		int orientation = flipper.getOrientation();
 		int x = flipper.getX(), y = flipper.getY();
 		double angle = flipper.getAngle();
 		
 		RotatablePoint pivot = new RotatablePoint(x + 0.25, y + 0.25);
 		RotatablePoint centre = new RotatablePoint(x + 1.0, y + 1.0);
 		
 		RotatablePoint pivotcircle = pivot.rotate(centre, orientation);
 		pivotcircle.x -= 0.25;
 		pivotcircle.y -= 0.25;
 				
 		RotatablePoint endcircle = new RotatablePoint(x + 0.25, y + 1.75).rotate(pivot, angle).rotate(centre, orientation);
 		endcircle.x -= 0.25;
 		endcircle.y -= 0.25;
 	
 		RotatablePoint line1s = new RotatablePoint(x, y + 0.25).rotate(pivot, angle).rotate(centre, orientation);
 		RotatablePoint line1e = new RotatablePoint(x, y + 1.75).rotate(pivot, angle).rotate(centre, orientation);
 		RotatablePoint line2s = new RotatablePoint(x + 0.5, y + 0.25).rotate(pivot, angle).rotate(centre, orientation);
 		RotatablePoint line2e = new RotatablePoint(x + 0.5, y + 1.75).rotate(pivot, angle).rotate(centre, orientation);
 		
 		int xPoints[] = { line1s.getScaledX((int)xscale), line1e.getScaledX((int)xscale), line2e.getScaledX((int)xscale), line2s.getScaledX((int)xscale) };
 		int yPoints[] = { line1s.getScaledY((int)yscale), line1e.getScaledY((int)yscale), line2e.getScaledY((int)yscale), line2s.getScaledY((int)yscale) };
 		
 		g.setColor(Color.ORANGE);
 		g.fillPolygon(xPoints, yPoints, 4);
 		g.fillOval(pivotcircle.getScaledX((int)xscale), pivotcircle.getScaledY((int)yscale), (int)Math.round(0.5 * (int)xscale), (int)Math.round(0.5 * (int)yscale));
 		g.fillOval(endcircle.getScaledX((int)xscale), endcircle.getScaledY((int)yscale), (int)Math.round(0.5 * (int)xscale), (int)Math.round(0.5 * (int)yscale));
 	}
 
 	public void setMode() {
 		editMode = !editMode;
 	}
 
 	public void removeLoactionIndicator() {
 		locationIndicator = false;
 		validSquareX = 0;
 		validSquareY = 0;
 		validSquareW = 0;
 		validSquareH = 0;
 		paint(this.getGraphics());
 	}
 
 	public void setLoactionIndicator(int x, int y, int w, int h, Color c) {
 		locationIndicator = true;
 		validSquareX = x;
 		validSquareY = y;
 		validSquareW = w;
 		validSquareH = h;
 		validColor = c;
 		paint(this.getGraphics());
 
 	}
 
 }
