 package jmbs.client.Graphics;
 
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
  
 public class ImagePanel extends JPanel {
 	String filePath;
 	private int w = 0;
 	private int h = 0; /*w: Width ; h : Height */
 	public ImagePanel(String fileImage) {
 		this.filePath = fileImage;
 	}
 	
 	public ImagePanel(String fileImage, int width, int height) {
 		this.filePath = fileImage;
 		this.w = width;
 		this.h = height;
 	}
  
         /**
 	 * 
 	 */
 	private static final long serialVersionUID = 5118025531286392963L;
 
 		public void paintComponent(Graphics g){
                 try {
                         Image img = ImageIO.read(new File(this.filePath));
                         if (this.w == 0 || this.h==0)
                         	g.drawImage(img, 0, 0,img.getWidth(null),img.getHeight(null), this);
                         else 
                         	g.drawImage(img, 0, 0,w,h, this);
                         //Pour une image de fond
                         //g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
                 } catch (IOException e) {
                         // TODO Auto-generated catch block
                        //e.printStackTrace();
                	System.err.println("file of image not found\n"+this.filePath);
                 }
                 
         }
 		
 		
 }
 
