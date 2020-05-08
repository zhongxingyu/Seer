 package ca.uwaterloo.lkc;
 
 import java.io.FileNotFoundException;
 
 import org.gnome.gdk.Event;
 import org.gnome.glade.Glade;
 import org.gnome.glade.XML;
 import org.gnome.gtk.Alignment;
 import org.gnome.gtk.Button;
 import org.gnome.gtk.Gtk;
 import org.gnome.gtk.IconSize;
 import org.gnome.gtk.Image;
 import org.gnome.gtk.ProgressBar;
 import org.gnome.gtk.Stock;
import org.gnome.gtk.ToggleButton;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;
 
 public class WindowInspect extends Thread {
 
     final XML xmlWndInspect;
     
     public final Window w;
 
     private final int nPartsConfiguration = 2;
     private final int nPartsHardware = 200;
     private final int nParts = nPartsConfiguration + nPartsHardware;
     private final int taskTime = 10;
     
     private final ProgressBar pg;
     
     WindowInspect(String gladeFile) throws FileNotFoundException
     {
         xmlWndInspect = Glade.parse(gladeFile, "wndInspect");
         
         w = (Window) xmlWndInspect.getWidget("wndInspect");
         final Button btnCancel = (Button) xmlWndInspect.getWidget("btnCancel");
         final ToggleButton tbtnInfo = (ToggleButton) xmlWndInspect.getWidget("tbtnInfo");
         final Alignment alignInfo = (Alignment) xmlWndInspect.getWidget("alignInfo");
         pg = (ProgressBar) xmlWndInspect.getWidget("pgInspect");
         
         w.connect(new Window.DeleteEvent() {
             
             @Override
             public boolean onDeleteEvent(Widget arg0, Event arg1) {
                 Gtk.mainQuit();
                 return false;
             }
         });
         
         btnCancel.connect(new Button.Clicked() {
             
             @Override
             public void onClicked(Button arg0) {
                 Gtk.mainQuit();
             }
         });
         
         tbtnInfo.connect(new ToggleButton.Toggled() {
             
             @Override
             public void onToggled(ToggleButton arg0) {
                 if (tbtnInfo.getActive())
                 {
                     alignInfo.show();
                 }
                 else
                 {
                     alignInfo.hide();
                 }
             }
         });
     }
     
     public void run() {
         dumpConfiguration();
         dumpHardware();
     }
     
     void dumpConfiguration()
     {
         work((Image) xmlWndInspect.getWidget("imgConfiguration"));
     }
     
     void dumpHardware()
     {
         work((Image) xmlWndInspect.getWidget("imgHardware"));
     }
     
     void work(Image img)
     {
         img.setImage(Stock.FIND, IconSize.BUTTON);
         
         for (int i = 0; i < nParts; ++i)
         {
             try {
                 sleep(taskTime);
                 pg.setFraction(Math.floor(pg.getFraction() + (1.0 / nParts)));
             } catch (InterruptedException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
         
         img.setImage(Stock.APPLY, IconSize.BUTTON);
     }
 }
