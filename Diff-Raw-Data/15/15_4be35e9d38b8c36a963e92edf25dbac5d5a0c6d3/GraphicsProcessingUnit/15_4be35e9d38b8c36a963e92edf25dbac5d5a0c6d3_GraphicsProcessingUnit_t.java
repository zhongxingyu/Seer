 package pl.ijestfajnie.circles.emulator;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 @SuppressWarnings("serial")
 public class GraphicsProcessingUnit extends JFrame implements Runnable {
 	
 	private static int CONSOLE_WIDTH = 800;
 	private static int CONSOLE_HEIGHT = 800;
 	private static int CHARACTER_HEIGHT = 16;
 	private static int CHARACTER_SPACING_VERTICAL = 2;
 	private static int CONSOLE_CHARS_VERTICAL = CONSOLE_HEIGHT / (CHARACTER_HEIGHT + CHARACTER_SPACING_VERTICAL);
 	private static int CONSOLE_CHARS_HORIZONTAL = 40;
 	public int MAX_BYTES = CONSOLE_CHARS_VERTICAL * CONSOLE_CHARS_HORIZONTAL;
 	public char[] video_ram = new char[MAX_BYTES];
 	
 	Font consoleFont;
 	
 	//Start the GPU
 	public GraphicsProcessingUnit() {
 		super("Circles Emulator");
 		this.setPreferredSize(new Dimension(CONSOLE_WIDTH, CONSOLE_HEIGHT));
         this.setMinimumSize(new Dimension(CONSOLE_WIDTH, CONSOLE_HEIGHT));
         this.setMaximumSize(new Dimension(CONSOLE_WIDTH, CONSOLE_HEIGHT));
         this.add(new Screen());
         this.pack();
         
         consoleFont = new Font("Arial", Font.PLAIN, CHARACTER_HEIGHT);
 
 		for (int i = 0; i < video_ram.length; i++) {
 			video_ram[i] = 0;
 		}
 	}
 	
 	//Print the pixel config as specified by the video RAM
 	public class Screen extends JPanel {
 		public void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			g.setColor(Color.BLACK);
 			g.fillRect(0, 0, CONSOLE_WIDTH, CONSOLE_HEIGHT);
 			for (int y = 0; y < CONSOLE_CHARS_VERTICAL; y++) {
 				String totalLine = "";
 				for (int x = 0; x < CONSOLE_CHARS_HORIZONTAL; x++) {
 					int cellId = (y * CONSOLE_CHARS_HORIZONTAL) + x;
 					if (video_ram[cellId] > 0) {
 						g.setFont(consoleFont);
 						g.setColor(Color.white);
 						totalLine += video_ram[cellId];
 						g.drawString(totalLine, 0, y * (CHARACTER_HEIGHT * CHARACTER_SPACING_VERTICAL) + 12);
 					}
 				}
 				//System.out.println();
 			}
 			//System.out.println();
 		}
 	}
 
 	//GPU used to act as an interpreter... NOPE.JPG
 	@Override
 	public void run() {
 		while(true) {
 			repaint();
 			try {
				Thread.sleep(1);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 }
