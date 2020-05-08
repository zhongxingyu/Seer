 package gui;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 
 public class LoadImages {
 /**
  *
  * @author Andreas J
  */
   private final String[] urls = new String[]{
       "win2.jpg","win3.jpg","win4.jpg","win5.jpg",
       
       "win6.jpg","win7.jpg","win8.jpeg","win9.jpeg","win10.jpg"};
   private ArrayList<BufferedImage> images;
  // Used for testing of GUI slideshow.
  // The arraylist is now in this class, with no copy in the GUI.
  // GUI can request 1 image which will be drawn from a list
  // if List is empty. Refill
   public LoadImages(){
  images=  new ArrayList<BufferedImage>();
   }
   public void load(){
   URL url;
     BufferedImage tmp=null;
       for (String url1 : urls) {
           System.out.println(url1);
           url = this.getClass().getResource("/img/"+url1);
           
           try {
              
               tmp= ImageIO.read(url);
               
               
           } catch (IOException ex) {
               Logger.getLogger(LoadImages.class.getName()).log(Level.SEVERE, null, ex);
           }   
         if(tmp!=null)images.add(tmp);
       }
   }
  public BufferedImage getImage
        (){
            if(images.isEmpty())load();
            BufferedImage tmp=images.get(0);
            images.remove(tmp);
            return tmp;
  
        }
 }
