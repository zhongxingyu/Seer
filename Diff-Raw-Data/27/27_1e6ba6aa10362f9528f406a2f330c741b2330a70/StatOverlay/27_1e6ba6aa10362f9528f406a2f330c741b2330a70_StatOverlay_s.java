 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 /**
  * Source: <a href=
  * "http://stackoverflow.com/questions/6596719/jframe-image-display-at-frame-resize"
  * >JFrame image display at frame resize</a>
  */
 public class StatOverlay {
 	/** Stat box holds the Champion's name, title, spells, stats and attributes. */
 	private static final Rectangle statbox = new Rectangle(191, 61, 215, 263);
 	/** The x-coordinate of the start of the stat bars. */
 	private static final int xstart = 220;
 	/** The x-coordinate increment for each unit of stat. */
 	private static final int xinc = 16;
 	/** the y-coordinate of the start of the stat bars. */
 	private static final int ystart = 206;
 	/** The y-coordinate increment for each unit of stat. */
 	private static final int yinc = 18;
 	/** Color of the ruler notch */
 	private static final Color notchColor = Color.YELLOW;
 	/** Modified image */
 	private static BufferedImage bi;
 	private static BufferedImage originalImage;
 	
 	public static void main(String[] args) {
 		try {
 			loadImageFromClipboard();
 
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					createAndShowGUI();
 				}
 			});
 		} catch (IOException e) {
 			// handle exception
 			System.err.println(e.getMessage());
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 		}
 	}
 
 	private static void loadImageFromClipboard() throws Exception {
 		originalImage = getImageFromClipboard();
 		float scaleW = originalImage.getWidth() / 1024.0f;
 		float scaleH = originalImage.getHeight() / 640.0f;
 		bi = originalImage
 				.getSubimage((int) (statbox.x * scaleW),
 						(int) (statbox.y * scaleH),
 						(int) (statbox.width * scaleW),
 						(int) (statbox.height * scaleH));
 		Graphics2D g2 = (Graphics2D) bi.getGraphics();
 		g2.setColor(notchColor);
 		float x1, y1, x2, y2;
 		for (int j = 0; j < 4; j++) {
 			int y = ystart + j * yinc;
 			y1 = (y - statbox.y) * scaleH;
 			y2 = y1 - 3 * scaleH;
 			for (int i = 0; i <= 10; i++) {
 				int x = xstart + i * xinc;
 				x1 = (x - statbox.x) * scaleW;
 				x2 = x1;
 				g2.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
 			}
 		}
 	}
 
 	private static void createAndShowGUI() {
		final JFrame frame = new JFrame("LoL Champ Stats");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable( false );
 
 		final JPanel panel = new JPanel() {
 
 			private static final long serialVersionUID = -6043908996415397733L;
 
 			@Override
 			protected void paintComponent(Graphics g) {
 				super.paintComponent(g);
 				Graphics g2 = g.create();
 				int x = (this.getWidth() - bi.getWidth(null)) / 2;
 				int y = (this.getHeight() - bi.getHeight(null)) / 2;
 				g2.drawImage(bi, x, y, null);
 				g2.dispose();
 			}
 
 			@Override
 			public Dimension getPreferredSize() {
 				return new Dimension(bi.getWidth(), bi.getHeight());
 			}
 		};
 
 		frame.add(panel);
 		frame.pack();
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 
 	public static BufferedImage getImageFromClipboard() throws Exception {
 		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 		Transferable content = clipboard.getContents(null);
 		if (content == null) {
			throw new Exception("error: nothing found in clipboard");
 		}
 		if (!content.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			throw new Exception("error: no image found in clipbaord");
 		}
 		return (BufferedImage) content.getTransferData(DataFlavor.imageFlavor);
 	}
 }
