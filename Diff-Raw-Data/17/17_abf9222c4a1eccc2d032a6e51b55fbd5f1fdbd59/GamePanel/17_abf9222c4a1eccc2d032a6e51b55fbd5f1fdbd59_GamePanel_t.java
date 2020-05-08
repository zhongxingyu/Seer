 package cs283.catan;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import java.awt.Dimension;
 import javax.swing.JLabel;
 import java.awt.BasicStroke;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.geom.Ellipse2D;
 import java.awt.image.BufferedImage;
 //import java.io.File;
 import java.io.IOException;
 
 
 public class GamePanel {
 			//http://stackoverflow.com/questions/7081308/jpanel-custom-drawing-using-graphics
 			//http://stackoverflow.com/questions/5797862/draw-a-line-in-a-jpanel-with-button-click-in-java
 		
 		    private BufferedImage board;
 		    //private BufferedImage boardclean;
 		    private JLabel view;
 			Point robberpt;
 			boolean drawrobber;
 			Graphics2D g2d;
 			Point rstart;
 			Point rend;
 			Point settlepoint;
 			boolean drawcity;
 			boolean addsettle;
 			boolean addroad;
 			boolean clear;
 			int pnum;
 			
 			public GamePanel(){
 				try {
 					board = ImageIO.read(Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream("cs283/catan/catanBoard.png"));
 					view = new JLabel( new ImageIcon(board));
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				robberpt = new Point(0,0);
 				settlepoint = new Point(0,0);
 				clear = false;
 				drawrobber = false;
 				drawcity = false;
 				addsettle = false;
 				addroad = false;
 				pnum = 0;
 				g2d = board.createGraphics();
 				view.setPreferredSize(new Dimension(525, 550));
 				view.setMaximumSize(new Dimension(525, 550));
 				g2d.dispose();
 			}
 			
 			/* Repaint 0 args
 			 * automatically called by other controller functions
 			 * Gets the current state of the board and then checks the pnum 
 			 * and assigns color
 			 * Once this is complete, it checks to see what task it needs to run
 			 */
 		    private void repaint(){
 		    	Graphics2D g2d = board.createGraphics();
 		        switch(pnum){
 		        case 0:
 		        	g2d.setColor(Color.orange);
 		        	break;
 		        case 1:
 		        	g2d.setColor(Color.red);
 		        	break;
 		        case 2:
 		        	g2d.setColor(Color.cyan);
 		        	break;
 		        case 3:
 		        	g2d.setColor(Color.pink);
 		        	break;
 		        default:
 		        	g2d.setColor(Color.black);
 		        }
 		        
 		        g2d.setRenderingHint(
 		            RenderingHints.KEY_ANTIALIASING,
 		            RenderingHints.VALUE_ANTIALIAS_ON);
 		        g2d.setStroke(new BasicStroke(8,
 		        BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
 		        
 		        if(clear){
 		        	try {
 						g2d.drawImage(ImageIO.read(Thread.currentThread()
                                 .getContextClassLoader()
                         .getResourceAsStream("cs283/catan/catanBoard.png")),
                                 0, 0, null);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 		        	
 		        	clear = false;
 		        }		        
 		        else if(addsettle){
 					System.out.print("drawing city");
 					Ellipse2D.Double Settlement = new Ellipse2D.Double(settlepoint.x-5, settlepoint.y-5, 10, 10);
 					g2d.draw(Settlement);
 					g2d.fill(Settlement);
 					if(drawcity){
 						Ellipse2D.Double City = new Ellipse2D.Double(settlepoint.x-3, settlepoint.y-3, 6, 6);
 						g2d.setColor(g2d.getColor().darker());
 						g2d.draw(City);
 						g2d.fill(City);
 						drawcity = false;
 
 					}
 					addsettle = false;
 				}
 		        else if(addroad){
 					g2d.drawLine(rstart.x, rstart.y, rend.x, rend.y);
 					addroad = false;
 				}
 		        else if(drawrobber){
 					g2d.setColor(Color.black);
					Rectangle Robber = new Rectangle(robberpt.x-5, robberpt.y-5, 10, 10);
 					g2d.draw(Robber);
 					g2d.fill(Robber);
 					drawrobber=false;
 				}
 				g2d.dispose();
 			}
 		    
 			//returns the internal JLabel display for use in GUIs
 		    public JLabel view(){
 		    	return view;
 		    }
 		    
 			public void drawsettle(Point a, boolean b, int c){
 				settlepoint = a;
 				drawcity = b;
 				pnum = c;
 				addsettle = true;	
 				repaint();
 			}	
 
 			public void addroad(Point a, Point b, int c){
 				rstart = a;
 				rend = b;
 				pnum = c;
 				addroad = true;
 				repaint();
 			}
 			
 			public void drawrobber(Point a){
 				robberpt = a;
 				drawrobber = true;
 				repaint();	
 			}
 
 			public void clearboard(){
 				clear = true;
 				repaint();
 			}
 		}
 		
 		//example
 		//GamePanel gamePanel = new GamePanel();
 		//_____.add(GamePanel.view());
 		//clearboard()
 		//no arg
 		//iterate through roads
 		//point, point, pnum
 		//iterate through cities (send settlement flag as 3rd argument
 		//point, point, cityflag, pnum
 		//robber: (to be fixed)
 		//point 
 	
