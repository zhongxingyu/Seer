 package com.vnexit.fingerprint.gaborfilter;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.WritableRaster;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 import com.vnexit.fingerprint.datamodel.ImageProcess;
 import com.vnexit.fingerprint.datamodel.Pixel;
 import com.vnexit.fingerprint.form.FingerImage;
 
 public class GaborFilter extends JPanel{
 	BufferedImage mFinger;
 	Pixel[][] pi;
 	ImageProcess imgProcess;
 	WritableRaster wr;
 
 	public GaborFilter() {
 		try {
			mFinger = ImageIO.read(getClass().getResource("../datatest/16_1.png"));
 			imgProcess =new ImageProcess(mFinger);
 			wr=mFinger.getRaster();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		Graphics g = mFinger.getGraphics();
 		g.drawImage(mFinger, 0, 0, null);
 	}
 	
 	public void rePaintLink(String link){
 		try {
			mFinger = ImageIO.read(getClass().getResource("../datatest/"+link));
 			imgProcess =new ImageProcess(mFinger);
 			wr=mFinger.getRaster();
 			repaint();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void paint(Graphics g) {
 
 		Graphics2D g2d = (Graphics2D) g;
 
 		g2d.drawImage(mFinger, null, 0, 0);
 	}
 
     
     public void tests() {
     	pi= imgProcess.grayExchange();
     	imgProcess.setData(wr, pi);
     	repaint();
     }
     
 }
