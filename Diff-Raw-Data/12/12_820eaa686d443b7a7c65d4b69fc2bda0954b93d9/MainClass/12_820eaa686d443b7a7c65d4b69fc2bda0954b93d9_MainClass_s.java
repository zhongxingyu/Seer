 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 import java.awt.Robot;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 public class MainClass extends JPanel{
 	
 	int x1, y1, x2, y2, tmp1, tmp2, tmp3, tmp4;   //tmp variable are used when dragging
 	BufferedImage image, tmpImage;   //which wants to get, tmpImage is for alpha picture
 	Robot robot;
 	Dimension allSize;
 	
 	public MainClass(){   //constructor
 		allSize = Toolkit.getDefaultToolkit().getScreenSize();
 		try {
 			Thread.sleep(3000);   //3 second for prepare picture which want to catch
 			robot = new Robot();
 			image = robot.createScreenCapture(new Rectangle(allSize));   //catch screen robot
 			
 		} catch (AWTException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		addMouseListener(new MouseAdapter(){   //mouse listener
 			public void mousePressed(MouseEvent e){   //get x, y
 				x1 = e.getX();
 				y1 = e.getY();
 				tmp3 = x1;   //use temporary variable let picture display normally
 				tmp1 = y1;
 				tmpImage = robot.createScreenCapture(new Rectangle(allSize));
 			}
 			public void mouseReleased(MouseEvent e){   //get x, y
 				try{
 					File imageFile = new File("test.jpg");
 					image = image.getSubimage(tmp3, tmp1, tmp4-tmp3, tmp2-tmp1);// = robot.createScreenCapture(new Rectangle(x1, y1, x2-x1, y2-y1));   //measure
 					if(ImageIO.write(image, "jpg", imageFile)){   //write into file
 						System.out.println("Success");
 						System.exit(0);
 					}
 					else{
 						System.out.println("Error");
 						System.exit(0);
 					}
 				}
 				catch(FileNotFoundException ex){
 					ex.printStackTrace();
 				}
 				catch (IOException ex) {
 					ex.printStackTrace();
 				}
 			}
 		});
 		addMouseMotionListener(new MouseMotionAdapter(){   //display normal alpha 
 			public void mouseDragged(MouseEvent e){			
 				x2 = e.getX();
 				y2 = e.getY();
 				
 				tmp2 = y2;   //use temporary variable let picture display normally
 				tmp4 = x2;
 				
 				if(x1<x2 && y1>y2){   //left down to right up
 					tmp1 = y2;
 					tmp2 = y1;
 				}
 				else if(x1>x2 && y1>y2){   //right down to left up 
 					tmp3 = x2;
 					tmp4 = x1;
 					tmp1 = y2;
 					tmp2 = y1;
 				}
 				else if(x1>x2 && y1<y2){   //right up to left down
 					tmp3 = x2;
 					tmp4 = x1;
 				}
 				Graphics2D g2D = (Graphics2D)getGraphics().create();   //create graphics for selected block
 				g2D.setColor(Color.RED);   //set color to red
				g2D.drawImage(tmpImage, 0, 0, null);   //draw background alpha=0.3 picture
 				g2D.drawImage(image, tmp3, tmp1, tmp4, tmp2, tmp3, tmp1, tmp4, tmp2, null);   //selected block
 				g2D.drawRect(tmp3, tmp1, tmp4-tmp3, tmp2-tmp1);   //selected block
 				g2D.dispose();   //create and dispose
 			}
 		});
 		
 		this.setDoubleBuffered(true);   //memory associated
 	}
 
 	public void paint(Graphics g){   //repaint frame
 		Graphics2D g2D = (Graphics2D)g.create();   //create it and don't forget to dispose it
 		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
 		g2D.setComposite(ac);
 		g2D.drawImage(image, 0, 0, null);
 		g2D.dispose();   //so dispose it
 	}
 	
 }
