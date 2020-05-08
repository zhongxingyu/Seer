 package ch.bittailor.bt.ui;
 
 import java.awt.Color;
 import java.awt.FontFormatException;
 import java.awt.Graphics2D;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import ch.bittailor.bt.com.TcpRequestClient;
 
 public class ImageScreenTest {
 
 
 	private static void draw(final ImageScreen iImageScreen) throws FontFormatException, IOException {
 		Graphics2D graphics = iImageScreen.getGraphics();
 
 		
 		graphics.setColor(Color.BLUE);		
 		graphics.drawOval(0, 0, 6, 6);
 		
		for (int i = 0; i < 4; i++) {
 			
 		
 			graphics.setColor(Color.BLACK);		
 			graphics.drawLine(3, 3, 2, 2);
 			graphics.setColor(Color.BLUE);
 			graphics.drawLine(3, 3, 3, 1);
 			iImageScreen.repaint();
 			sleep();
 			
 			graphics.setColor(Color.BLACK);		
 			graphics.drawLine(3, 3, 3, 1);
 			graphics.setColor(Color.BLUE);	
 			graphics.drawLine(3, 3, 4, 2);
 			iImageScreen.repaint();
 			sleep();
 			
 			graphics.setColor(Color.BLACK);		
 			graphics.drawLine(3, 3, 4, 2);
 			graphics.setColor(Color.BLUE);	
 			graphics.drawLine(3, 3, 5, 3);
 			iImageScreen.repaint();
 			sleep();
 			
 			
 			graphics.setColor(Color.BLACK);		
 			graphics.drawLine(3, 3, 5, 3);
 			graphics.setColor(Color.BLUE);	
 			graphics.drawLine(3, 3, 4, 4);
 			iImageScreen.repaint();
 			sleep();
 			
 			graphics.setColor(Color.BLACK);		
 			graphics.drawLine(3, 3, 4, 4);
 			graphics.setColor(Color.BLUE);	
 			graphics.drawLine(3, 3, 3, 5);
 			iImageScreen.repaint();
 			sleep();
 			
 			graphics.setColor(Color.BLACK);		
 			graphics.drawLine(3, 3, 3, 5);
 			graphics.setColor(Color.BLUE);	
 			graphics.drawLine(3, 3, 2, 4);
 			iImageScreen.repaint();
 			sleep();
 			
 			graphics.setColor(Color.BLACK);		
 			graphics.drawLine(3, 3, 2, 4);
 			graphics.setColor(Color.BLUE);	
 			graphics.drawLine(3, 3, 1, 3);
 			iImageScreen.repaint();
 			sleep();
 			
 			graphics.setColor(Color.BLACK);		
 			graphics.drawLine(3, 3, 1, 3);
 			graphics.setColor(Color.BLUE);	
 			graphics.drawLine(3, 3, 2, 2);
 			iImageScreen.repaint();
 			sleep();
 			
 		}
 		
 		
 		/*
 		Font tinyFont = Font.createFont(Font.TRUETYPE_FONT,ImageScreenTest.class.getResourceAsStream("tiny.ttf"));
 		
 		Font oldFont = graphics.getFont();
 		graphics.setFont(tinyFont.deriveFont(6f));
 		graphics.drawString("Hello", 0, 7);
 		iImageScreen.repaint();*/
 		
 		/*
 		graphics.setColor(Color.BLUE);
 		graphics.drawLine(0, 2, 15, 2);
 		graphics.drawLine(0, 5, 15, 5);
 
 		
 		graphics.setColor(Color.GREEN);
 		graphics.fill3DRect(0, 0, 4, 2, false);
 		graphics.fill3DRect(6, 0, 4, 2, false);
 		graphics.fill3DRect(6, 0, 4, 2, false);
 		iImageScreen.repaint();
 		*/
 		
 		/*
 		graphics.setColor(Color.RED);
 		graphics.draw(new Ellipse2D.Double(0,0,15,7));
 		iImageScreen.repaint();
 		
 		graphics.setColor(Color.YELLOW);
 		graphics.draw(new Ellipse2D.Double(4,0,7,7));
 		iImageScreen.repaint();
 		
 		graphics.setColor(Color.BLUE);
 		graphics.draw(new Line2D.Double(0, 0, 15, 7));
 		iImageScreen.repaint();
 		*/
 		/*
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				JFrame frame = new JFrame("Image");
 				JLabel label = new JLabel(new ImageIcon(iImageScreen.getImage()),SwingConstants.CENTER);
 				frame.getContentPane().add(label);
 				frame.pack();
 				frame.setVisible(true);
 				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			}
 		});
 		*/
 		
 				
 	}
 	
 	
 	private static void sleep() {
 		try {
			Thread.sleep(50);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 
 	public static void main(String[] args) {
 
		for (int i = 0; i < 5 ; i++) {
 			System.out.println("Loop " + i);		
 			try {
 				final Socket socket = new Socket("192.168.2.2",2000);			
 				RgbScreenProxy screen = new RgbScreenProxy(new TcpRequestClient(socket));		
 				ImageScreen imageScreen = new ImageScreen(screen);
 				draw(imageScreen);
				socket.close();		
				sleep();
				sleep();
				sleep();
 			} catch (UnknownHostException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (FontFormatException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	
 }
