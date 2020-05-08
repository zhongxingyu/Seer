 package qp.main.gui;
 
 import java.awt.Image;
 
import javax.swing.ImageIcon;
 
 public class ImageLoader{
 	public static Image getImageFrom(String s){
		Image img = new ImageIcon(s).getImage();
		return img;
 	}
 }
