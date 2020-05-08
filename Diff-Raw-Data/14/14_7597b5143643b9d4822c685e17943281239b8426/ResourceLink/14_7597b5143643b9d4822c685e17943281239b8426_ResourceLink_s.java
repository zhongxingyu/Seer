 package models;
 
 import java.awt.*;
 import java.io.Serializable;
 import java.net.URI;
 
 /**
  * Created with IntelliJ IDEA.
  * User: laiq
  * Date: 5/28/13
  * Time: 12:45 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ResourceLink extends Model implements Resource, Serializable {
 
 	private static final long serialVersionUID = 305787132332484558L;
 	String uri;
 	
     @Override
     public void open() {
         Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
         if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
             try {
                 desktop.browse(new URI(uri));
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
     
    public ResourceLink(String uri){
        this.uri = uri;
     }
 
     @Override
     public String toString() {
         return uri;
     }
 }
