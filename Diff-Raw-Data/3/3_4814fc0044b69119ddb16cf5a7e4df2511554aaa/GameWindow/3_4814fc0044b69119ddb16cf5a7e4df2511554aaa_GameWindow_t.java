 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferStrategy;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JViewport;
 import javax.swing.Timer;
 
 public class GameWindow extends JFrame {
 	private static final long serialVersionUID = 1L; //To suppress warning
 	static JFrame frame;
 	JPanel panel;
 	Graphics graphics;
 	Rectangle rec;
 	int xGridMin = 40;
 	int yGridMin = 70;
 	int maxX;
 	int maxY;
 	int xBoardDim;
 	int yBoardDim;
 	boolean pointSelected = false;
 	int radius;
 	int xLastCoord = -1;
 	int yLastCoord = -1;
 	boolean clicked = false;
 	int xClick;
 	int yClick;
 	
 	private enum selectionStates {
 		None,
 		FirstPiece,
 		SecondCoord;
 	}
 	selectionStates currentSelectState = selectionStates.None;
 	
 	final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
 	
 	GameWindow(int _xBoardDim, int _yBoardDim) {
 		xBoardDim = _xBoardDim;
 		yBoardDim = _yBoardDim;
 		
 		int xSpacing = (maxX - 2*40)/(xBoardDim - 1);
 		int ySpacing = ((maxY-30) - 2*40)/(yBoardDim - 1);
 		if (xSpacing >= ySpacing) {
 			radius = (int) (0.75 * (double) (ySpacing * -1));
 		} else {
 			radius = (int) (0.75 * (double) xSpacing);
 		}
 		createWindow();
 	}
 	
 	public final void createWindow() {
 		setTitle("Fanorona");
 		setResizable(false);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(600, 600);
 		setLocationRelativeTo(null);
 		rec = new Rectangle();
 		if (getParent() instanceof JViewport) {
 	        JViewport vp = (JViewport) getParent();
 	        rec = vp.getViewRect();
 	    } else {
 	        rec = new Rectangle(0, 0, getWidth(), getHeight());
 	    }
 		maxX = (int) rec.getMaxX();
 		maxY = (int) rec.getMaxY();
 		
         ActionListener timerListener = new ActionListener()  
         {  
             public void actionPerformed(ActionEvent e)  
             {  
             	clearTime();
                 updateScreen(false);
             }  
         };  
         Timer timer = new Timer(1000, timerListener);   
         timer.start();
 		
 		addMouseListener(new MouseAdapter() {
 			@Override
             public void mousePressed(MouseEvent event) {
                 if (event.getButton() == MouseEvent.BUTTON1) {
                 	clicked = true;
                 	xClick = event.getX();
                 	yClick = event.getY();
                	updateScreen(true);
                     //processClick(event.getX(), event.getY());
                 }
 
                 if (event.getButton() == MouseEvent.BUTTON3) {
 
                 }
 			}
 		});
 		setVisible(true);
 		graphics = this.getGraphics();
 		/* To clear the window initially, a pause is needed
 		 * otherwise the graphics objects are not usable */
 		try {
 			Thread.sleep(1000);
 			clearWindow();
 			drawGrid();
 		} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		}
 	}
 	
 	private void processClick(int x, int y) {
 		xClick = -1;
 		yClick = -1;
 		int xSpacing = (maxX - 2*40)/(xBoardDim - 1);
 		int ySpacing = ((maxY-30) - 2*40)/(yBoardDim - 1);
 		int xGridMax = xGridMin + ((xBoardDim - 1) * xSpacing);
 		int yGridMax = yGridMin + ((yBoardDim - 1) * ySpacing);
 		int xTemp;
 		int yTemp;
 		if (x <= xGridMin) {
 			xTemp = 0;
 		} else if (x >= xGridMax) {
 			xTemp = xBoardDim - 1;
 		} else {
 			double xDouble = (double) x;
 			xTemp = (int) Math.round(((xDouble -
 					(double)xGridMin) / (double)xSpacing));
 		}
 		if (y <= yGridMin) {
 			yTemp = 0;
 		} else if (y >= yGridMax) {
 			yTemp = yBoardDim - 1;
 		} else {
 			double yDouble = (double) y;
 			yTemp = (int) Math.round(((yDouble -
 					(double)yGridMin) / (double)ySpacing));
 		}
 		drawIndicator(xTemp, yTemp, x, y);
 
 		graphics.drawString("Point nearest click: " +
 				Integer.toString(xTemp), 20, 40);
 		graphics.drawString("Point nearest click: " +
 				Integer.toString(yTemp), 20, 60);
 	}
 
 	private void drawIndicator(int xCoord, int yCoord, int xActual,
 			int yActual) {
 		int xSpacing = (maxX - 2*40)/(xBoardDim - 1);
 		int ySpacing = ((maxY-30) - 2*40)/(yBoardDim - 1);
 		int xTemp = xGridMin + xSpacing * xCoord;
 		int yTemp = yGridMin + ySpacing * yCoord;
 
 		double distance = Math.sqrt(((xTemp - xActual) * (xTemp - xActual)) +
 				((yTemp - yActual) * (yTemp - yActual)));
 		if (distance <= radius) {
 			pointSelected = true;
 			if (currentSelectState == selectionStates.FirstPiece) {
 				if (xCoord == xLastCoord && yCoord == yLastCoord) {
 					/* Currently selected piece is selected again. This is
 					 * interpreted as a deselect action */
 					/* TODO Check to see if the selected coordinate has a
 					 * selectable piece in it, otherwise take no action */
 					currentSelectState = selectionStates.None;
 					return;
 				} else {
 					/* A location different from the first is selected. This
 					 * is interpreted as an attempted move action. */
 					currentSelectState = selectionStates.SecondCoord;
 					drawSelection(xTemp, yTemp);
 				}
 			}
 
 			xLastCoord = xCoord;
 			yLastCoord = yCoord;
 			currentSelectState = selectionStates.FirstPiece;
 			drawSelection(xTemp, yTemp);
 
 		} else {
 			pointSelected = false;
 			currentSelectState = selectionStates.None;
 			xLastCoord = -1;
 			yLastCoord = -1;
 		}
 	}
 	
 	private void drawSelection(int xPoint, int yPoint) {
 		if (currentSelectState == selectionStates.FirstPiece) {
 			graphics.setColor(Color.RED);
 		} else {
 			graphics.setColor(Color.BLUE);
 		}
 		graphics.drawLine(xPoint - radius, yPoint - radius,
 				xPoint + radius, yPoint - radius); //Top
 		graphics.drawLine(xPoint - radius, yPoint - radius,
 				xPoint - radius, yPoint + radius); //Left
 		graphics.drawLine(xPoint + radius, yPoint - radius,
 				xPoint + radius, yPoint + radius); //Right
 		graphics.drawLine(xPoint - radius, yPoint + radius,
 				xPoint + radius, yPoint + radius); //Bottom
 		graphics.setColor(Color.BLACK);
 	}
 	
 	private void clearWindow() {
 		graphics.setColor(Color.GRAY);
         graphics.fillRect(0, 0, maxX, maxY);
         graphics.setColor(Color.BLACK);
 	}
 	
 	private void clearTime() {
 		graphics.setColor(Color.GRAY);
         graphics.fillRect(maxX-250, 0, 240, 45);
         graphics.setColor(Color.BLACK);
 	}
 	
 	public void drawGrid() {
 		/* Do not process bad dimensions */
 		if ((xBoardDim == 1) || (yBoardDim == 1)) {
 			return;
 		}
 		
 		int xSpacing = (maxX - 2*40)/(xBoardDim - 1);
 		int ySpacing = ((maxY-30) - 2*40)/(yBoardDim - 1);
 		int xGridMax = xGridMin + ((xBoardDim - 1) * xSpacing);
 		int yGridMax = yGridMin + ((yBoardDim - 1) * ySpacing);
 		int xCurrent = xGridMin;
 		int yCurrent = yGridMin;
 
 		Graphics2D graphics2D = (Graphics2D) graphics;      
 		 
 	    graphics2D.setStroke(new BasicStroke(7F));  // set stroke width of 10
 		
 		boolean altLeft = true;
 		boolean flipAlt = false;
 		if (xBoardDim % 2 == 0) {
 			flipAlt = true;
 		}
 		while(true) {
 			if (yCurrent != yGridMax) {
 				//Draw down line
 				graphics2D.drawLine(xCurrent, yCurrent, xCurrent,
 						yCurrent + ySpacing);
 			}
 			if ((xCurrent != xGridMin) && (yCurrent != yGridMax) && altLeft) {
 				//Draw left diagonal
 				graphics2D.drawLine(xCurrent, yCurrent, xCurrent - xSpacing,
 						yCurrent + ySpacing);
 			}
 			if ((xCurrent != xGridMax) && (yCurrent != yGridMax) && altLeft) {
 				//Draw right diagonal
 				graphics2D.drawLine(xCurrent, yCurrent, xCurrent + xSpacing,
 						yCurrent + ySpacing);
 			}
 			if (xCurrent != xGridMax) {
 				//Draw right line
 				graphics2D.drawLine(xCurrent, yCurrent, xCurrent + xSpacing,
 						yCurrent);
 			}
 			if ((xCurrent >= xGridMax) && (yCurrent >= yGridMax)) {
 				break;
 			}
 			if (xCurrent == xGridMax) {
 				if (flipAlt) {
 					altLeft = !altLeft;
 				}
 				xCurrent = xGridMin;
 				yCurrent += ySpacing;
 			} else {
 				xCurrent += xSpacing;
 			}
 			altLeft = !altLeft;
 		}
 		graphics2D.setStroke(new BasicStroke(0F));
 	}
 	
 	public void updateScreen(boolean clicked) {
 		Date date = new Date();  
         String time = timeFormat.format(date);
 		/*RenderingHints renderHints = new RenderingHints(
 				RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		graphics.setRenderingHints(renderHints);*/
 		if (clicked) {
 			clearWindow();
 			drawGrid();
 		} else {
 			clearTime();
 		}
 		
 		if (clicked) {
 			clicked = false;
 			processClick(xClick, yClick);
 		}
 		graphics.drawString("Remaining move time: ", maxX-250, 40);
 		graphics.drawString(time + " sec", maxX-100, 40);
 	}
 }
