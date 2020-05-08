 package gui;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 
 import net.coobird.thumbnailator.Thumbnails;
 
 import bll.ViewController;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class ImageShow extends JPanel {
 
 	/**
 	 * @author Andreas J
 	 */
 	private static final long serialVersionUID = 1L;
 	private BufferedImage currImg;
 	private static int SCREEN_W, SCREEN_H, SCALE_FACTOR;
 	private ViewController ctrl;
 
 	/*
 	 * Class to shuffle trough images in the slideshow. Images are drawn to the
 	 * screen in this class Uses LoadImages to extract images from urls.
 	 */
 	public ImageShow(ViewController ctrl,int w, int h) throws IOException {
 		this.ctrl = ctrl;
 		 SCREEN_W=w;
 		 SCREEN_H=h; 
 		 SCALE_FACTOR=2;
 		// currImg = ctrl.getCurrentPicture(false);
 		currImg = null;
 	}
 
 	@Override
 	// Resizes currImage, adds rendering hints, draws and dispose of Graphics
 	// object
 	//
 	public void paint(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;
 		if (currImg != null) {
 			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
 			int w = (SCREEN_W - currImg.getWidth()) / 2, h = (SCREEN_H - currImg
 					.getHeight()) / 2;
			g2.drawImage(currImg, w, h, null);
 			g2.dispose();
 
 		}
 	}
 
 	@Override
 	public void update(Graphics g) {
 		paint(g);
 	}
 
 	/*
 	 * Changes currImage to the next Image in the list. Only if the list
 	 * contains images and that that the
 	 */
 	public void moveNext() throws IOException {
 		BufferedImage bf = ctrl.getCurrentPicture();
 		
 		if (bf != null)
 			currImg = Thumbnails.of(bf).scale( SCALE_FACTOR).asBufferedImage();
 		
 	}
 }
