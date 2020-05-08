 package interiores.core.presentation;
 
 import interiores.core.Observer;
 import interiores.core.Utils;
 import interiores.core.terminal.Terminal;
 import java.util.Map;
 
 /**
  *
  * @author hector
  */
 public class PresentationController implements Observer
 {
     private ViewLoader vloader;
     private Terminal terminal;
     
     public PresentationController(ViewLoader vloader, Terminal terminal)
     {
         this.vloader = vloader;
         this.terminal = terminal;
     }
     
     public void showView(String action, String subject) throws Exception
     {
         String viewName = getViewName(action, subject);
         
         View view;
         
         if(! vloader.isLoaded(viewName))
         {
             vloader.load(viewName);
             view = vloader.get(viewName);
             
             view.setPresentation(this);
         }
         else
             view = vloader.get(viewName);
         
         view.showView();
     }
     
     public void exec(String command)
     {
         terminal.exec(command);
     }
     
     @Override
     public void notify(String name, Map<String, Object> data)
     {
         
     }
     
     static private String getViewName(String action, String subject)
     {
         return Utils.capitalize(action) + Utils.capitalize(subject);
     }
 }
