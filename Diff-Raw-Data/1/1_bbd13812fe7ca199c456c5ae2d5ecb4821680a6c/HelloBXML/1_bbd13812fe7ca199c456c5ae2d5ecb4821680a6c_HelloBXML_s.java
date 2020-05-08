 package synchronize.test;
 
 import org.apache.pivot.beans.BXMLSerializer;
 import org.apache.pivot.collections.Map;
 import org.apache.pivot.wtk.Application;
 import org.apache.pivot.wtk.DesktopApplicationContext;
 import org.apache.pivot.wtk.Display;
 import org.apache.pivot.wtk.Window;
 
 public class HelloBXML implements Application {
     private Window window = null;
     
     public static void main(String[] args) {
     	DesktopApplicationContext.main(HelloBXML.class, args);
     }
 
     @Override
     public void startup(Display display, Map<String, String> properties)
         throws Exception {
         BXMLSerializer bxmlSerializer = new BXMLSerializer();
         window = (Window)bxmlSerializer.readObject(HelloBXML.class, "hello.bxml");
         window.open(display);
     }
 
     @Override
     public boolean shutdown(boolean optional) {
         if (window != null) {
             window.close();
         }
 
         return false;
     }
 
     @Override
     public void suspend() {
     }
 
     @Override
     public void resume() {
     }
 }
