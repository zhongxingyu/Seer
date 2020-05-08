 package pl.ijestfajnie.circles.emulator;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 @SuppressWarnings("serial")
 public class GraphicsProcessingUnit extends JFrame implements Runnable {
 	
 	//Quite small, but results in compatibility with older models
 	public int MAX_BYTES = 64;
 	public byte[] video_ram = new byte[MAX_BYTES];
 	
 	//Start the GPU
 	public GraphicsProcessingUnit() {
 		super("Circles Emulator");
 		this.setPreferredSize(new Dimension(800, 800));
         this.setMinimumSize(new Dimension(800, 800));
         this.setMaximumSize(new Dimension(800, 800));
         this.add(new Screen());
         this.pack();
 
 		for (int i = 0; i < video_ram.length; i++) {
 			video_ram[i] = 0;
 		}
 	}
 	
 	//Print the pixel config as specified by the video RAM
 	public class Screen extends JPanel {
 		public void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			for (int x = 0; x < 8; x++) {
 				for (int y = 0; y < 8; y++) {
 					int cellId = x*8 + y;
 					System.out.print(video_ram[cellId]);
 					if (video_ram[cellId] > 0) {
 						g.setColor(Color.black);
 						g.fillRect(x*100, y*100, 100, 100);
 					} else {
 						g.setColor(Color.white);
 						g.fillRect(x*100, y*100, 100, 100);
 					}
 				}
 				System.out.println();
 			}
 			System.out.println();
 		}
 	}
 
 	//GPU used to act as an interpreter... NOPE.JPG
 	@Override
 	public void run() {
 		repaint();
		System.out.println("test");
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
