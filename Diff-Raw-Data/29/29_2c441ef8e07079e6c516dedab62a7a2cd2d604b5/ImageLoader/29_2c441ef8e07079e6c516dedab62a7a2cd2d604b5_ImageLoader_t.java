 /**
  * @author Jenny Zhen
  * @author Grant Kurtz
  * date: 04.27.13
  * language: Java
  * file: ImageLoader.java
  * project: Pac-man
  *          Implementation of the classic Pac-Man game in Java.
  */
 
 package views;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 
 public class ImageLoader {
 
 	private BufferedImage compositeImage;
 
 	private Image[][] pacManImages;
 
 	private static ImageLoader loader;
 
 	private ImageLoader(){
 	}
 
 	public static ImageLoader getImageLoaderInstance(){
 		if(loader == null)
 			loader = new ImageLoader();
 		return loader;
 	}
 
 	public Image[][] getPacManImages(){
 		if(pacManImages == null){
 			pacManImages = new Image[4][2];
			pacManImages[0][0] = new ImageIcon(getClass().getResource("/images/pacman/pacman_west1.png")).getImage();
			pacManImages[0][1] = new ImageIcon(getClass().getResource("/images/pacman/pacman_west2.png")).getImage();
			pacManImages[1][0] = new ImageIcon(getClass().getResource("/images/pacman/pacman_north1.png")).getImage();
			pacManImages[1][1] = new ImageIcon(getClass().getResource("/images/pacman/pacman_north2.png")).getImage();
			pacManImages[2][0] = new ImageIcon(getClass().getResource("/images/pacman/pacman_east1.png")).getImage();
			pacManImages[2][1] = new ImageIcon(getClass().getResource("/images/pacman/pacman_east2.png")).getImage();
			pacManImages[3][0] = new ImageIcon(getClass().getResource("/images/pacman/pacman_south1.png")).getImage();
			pacManImages[3][1] = new ImageIcon(getClass().getResource("/images/pacman/pacman_south2.png")).getImage();
 		}
 		return pacManImages;
 	}
 }
