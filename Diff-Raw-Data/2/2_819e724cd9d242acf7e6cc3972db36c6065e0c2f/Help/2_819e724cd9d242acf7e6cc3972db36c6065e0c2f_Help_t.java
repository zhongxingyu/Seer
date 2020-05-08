 package org.resources;
 
 import java.awt.Image;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 
 import org.publicmain.common.LogEngine;
 import org.publicmain.sql.DatabaseEngine;
 
 public class Help {
 	
 	public static ImageIcon getIcon(String filename) {
 		return getIcon(filename, 16);
 	}
 	public static ImageIcon getIcon(String filename,int size) {
 		return getIcon(filename, size,size);
 	}
 	
 	public static InputStream getInputStream(String filename) throws IOException {
 		return Help.class.getResource(filename).openStream();
 	}
 	
 	public static File getFile(String filename) throws IOException {
 
 		File tmp = null;
 
 		InputStream inputStream = getInputStream(filename);
 
 		try {
			tmp = File.createTempFile("publicMain", "script");
 
 			try (BufferedOutputStream bos = new BufferedOutputStream(
 					new FileOutputStream(tmp));
 					BufferedInputStream bin = new BufferedInputStream(
 							inputStream);) {
 				byte[] cup = new byte[512];
 				int len = -1;
 				while ((len = bin.read(cup)) != -1) {
 					bos.write(cup, 0, len);
 				}
 			} catch (IOException e1) {
 				LogEngine.log("Resources", e1 );
 			}
 		} catch (Exception e) {
 			LogEngine.log("Resources", e );
 		}
 
 		return tmp;
 	}
 	
 	
 	
 	
 	/**
 	 * Gibt des Bild in der angeforderten Gre zurck.
 	 * 
 	 * @param filename, Anzupassendes Bild
 	 * @param size, geforderte Gre
 	 * @return, fertiges Icon
 	 */
 	public static ImageIcon getIcon(String filename, int size_x, int size_y) {
 		URL resource = Help.class.getResource(filename);
 		if (resource == null) {
 			resource = Help.class.getResource("g4174.png");
 		}
 		ImageIcon imageIcon = new ImageIcon(resource);
 		Image img = imageIcon.getImage();
 		Image newimg = img.getScaledInstance(size_x, size_y,java.awt.Image.SCALE_SMOOTH);
 		ImageIcon newIcon = new ImageIcon(newimg);
 		return newIcon;
 	}
 	
 }
