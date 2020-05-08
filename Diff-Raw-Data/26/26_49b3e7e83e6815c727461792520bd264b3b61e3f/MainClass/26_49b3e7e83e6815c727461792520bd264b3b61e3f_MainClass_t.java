 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.Robot;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 public class MainClass extends JFrame{
 	
 	JLabel label;
 	int x1, y1, x2, y2;
 	BufferedImage image;
 	public static void main(String[] args){
 		MainClass a = new MainClass();
 		a.setSize(Toolkit.getDefaultToolkit().getScreenSize());
 		a.setVisible(true);		
 	}
 	
 	public MainClass(){
 		final Container c = getContentPane();
 		label = new JLabel();
 		Dimension allSize = Toolkit.getDefaultToolkit().getScreenSize();
 		try {
 			Thread.sleep(3000);   //3 second for prepare picture which want to catch
 			Robot robot = new Robot();
 			image = robot.createScreenCapture(new Rectangle(allSize));   //catch screen robot
 			
 		} catch (AWTException e1) {
 			e1.printStackTrace();
 		} catch (InterruptedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		addMouseListener(new MouseAdapter(){   //mouse listener
 			public void mousePressed(MouseEvent e){   //get x, y
 				x1 = e.getX();
 				y1 = e.getY();
 				//System.out.println(x1 + "," + y1);
 			}
 			public void mouseReleased(MouseEvent e){   //get x, y
 				x2 = e.getX();
 				y2 = e.getY();
 				try{
 					Robot robot = new Robot();
 					File imageFile = new File("test.jpg");
					image = image.getSubimage(x1, y1, x2-x1, y2-y1);// = robot.createScreenCapture(new Rectangle(x1, y1, x2-x1, y2-y1));   //measure
 					if(ImageIO.write(image, "jpg", imageFile)){   //write into file
 						System.out.println("Success");
 						System.exit(0);
 					}
 					else{
 						System.out.println("Error");
 						System.exit(0);
 					}
 				}
 				catch(AWTException ex){   //exception
 					ex.printStackTrace();
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
				int x3 = e.getX();
				int y3 = e.getY();
				Graphics g = getGraphics();
				g.drawImage(image,x1, y1, x3, y3, x1, y1, x3, y3, null);
				g.setColor(Color.RED);
				g.drawRect(x1, y1, x3-x1, y3-y1);
			}
		});
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setUndecorated(true);
 		this.add(label);
 	}
 	
 	public void paint(Graphics g){   //repaint frame
 		Graphics2D g2D = (Graphics2D)g.create();   //create it and don't forget to dispose it
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
 		g2D.setComposite(ac);
 		g2D.drawImage(image, 0, 0, null);
 		g2D.dispose();   //so dispose it
 	}
 	
 }
