 package gui;
 
 import java.awt.*;
 import java.io.File;
 import java.io.Serializable;
 import java.util.*;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.swing.*;
 
 public class Drawing implements Serializable {
 	private static final long serialVersionUID = 43993875620545410L;
 	
 	public int posX, posY;
     public String filename;
     public CopyOnWriteArrayList<Drawing> subDrawings;
     
     //The explicit constructor prevents NullPointerExceptions with the
     //subDrawings list and filename.
     public Drawing(int x, int y, String file) {
     	posX = x;
     	posY = y;
     	filename = file;
     	subDrawings = new CopyOnWriteArrayList<Drawing>();
     }
 
     public void paint(Component c, Graphics2D g, Map<String, ImageIcon> imageMap) {
     	//Perform a basic depth first search to paint the drawing tree.
     	
     	if(!filename.equals("NOPIC")) {
     		ImageIcon img = imageMap.get(filename);
     	
     		//Die noisily if the image can't be found.
     		if(img == null) {
     			System.out.println("Can't find image " + filename + " in gfx folder.");
     			System.exit(1);
     		}
     	
     		//Otherwise, paint the image and see if it has any images under it.
     		img.paintIcon(c, g, posX, posY);
     	}
     		
     	if(subDrawings.size() != 0)
     		for(Drawing d : subDrawings)
     			d.paint(c, g, imageMap);
     }
     
 	public String toString() {
 		String result = new File(filename).getName() + "@" + posX + "," + posY;
 		if(subDrawings.size() > 0)
 			result += " with " + subDrawings.size() + " sub drawings";
 		
 		return result;
 	}
 }
