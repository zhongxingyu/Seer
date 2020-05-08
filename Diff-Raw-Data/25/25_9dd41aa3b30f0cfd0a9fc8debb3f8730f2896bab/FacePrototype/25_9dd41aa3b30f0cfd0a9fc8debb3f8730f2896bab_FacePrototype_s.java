 package com.lisasoft.face;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JFrame;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.styling.Style;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.filter.identity.FeatureId;
 
 import com.lisasoft.face.data.Face;
 import com.lisasoft.face.data.FaceDAO;
 import com.lisasoft.face.map.MapComponent;
 import com.lisasoft.face.map.MapComponentFactory;
 import com.lisasoft.face.map.MapComponentImpl;
 import com.lisasoft.face.table.FaceTable;
 
 /**
  * This is a prototype application *just* showing how to integrate a MapComponent with an existing
  * application.
  * <p>
  * As such the details of this application are not all that interesting; they do serve to illustrate
  * how to:
  * <ol>
  * <li>set up a MapComponent (this is actually a simple JPanel consisting of a JMapPane</li>
  * <li>set up a toolbar using some of the actions available for controlling MapComponent</li>
  * </ul> * In all cases this is straight forward application of the components provided by GeoTools.
  * <p>
  * Here is the interesting bit:
  * <ul>
  * <li>set up a FaceTable (actually a small JPanel consisting of a JTable working against
  * the MapComponentImpl)</li>
  * <li>Custom table model; just backed by the Faces provided to MapComponentTable</li>
  * <li>Custom tool to "select" Faces in the MapComponent; this will update both update an internal
  * *filter* used to display selected faces; and send a FaceTable update event</li>
  * <li>Custom DataStore used to present the Faces provided to MapComponent to the GeoTools rendering
  * system. This is used by *two* layers. One layer to display the sites; and a second to display the
  * selection (These two layerss are added to the MapContent).</li>
  * </ul>>
  * 
  * @author Scott Henderson (LISASoft)
  * @author Jody Garnett (LISASoft)
  */
 public class FacePrototype extends JFrame {
 
     /** serialVersionUID */
     private static final long serialVersionUID = 3738236346475642092L;
     
     /**
      * This is the map component; it handles all "GIS" functionality
      * and is just treated as a display of Face POJOs similar to a table.
      * <p>
      * You can use the static FaceDAO.load( csvFile ) to load a List<Face>
      * to place into the MapCompeonent.
      */
     MapComponentImpl map;
     
     /**
      * Uses a JTableModel and ListSelectionModel.
      */
     FaceTable table;
     
     /**
      * Init method called from main
      * 
      * Scott: most if not all work here, basically need to implement prototype in here
      */
     public void init(){
     	
     	// layout the user interface with
         // a) MapComponent (it will laod the MapContent itself)
         // b) FaceTable (it will listen to the MapComponent itself)
         // c) toolbar (will need to add actions? on the MapContent - perhaps pass int a toolbar?)
     	
     	MapComponentFactory factory = new MapComponentFactory();
     	map = factory.buildMapComponent();
    	
         //table = new FaceTable(map);
     }
     
     public void loadFaces() throws IOException {
         File csvFile = new File("data/senario.csv");
         List<Face> faces = FaceDAO.load(csvFile);
 
         //need to see map componenet with this faces data
         map.setFaces(faces);
 
     }
     
     /**
      * This is the opposite of init(); in this case we use it to dispose of all the DataStore we are
      * using. While this is not very exciting for Shapefile; it is important when working with JDBC
      * DataStores that have a real connection.
      */
     public void cleanup() {
         map.dispose();
     }
     
     public static void main(String[] args) {
         // marked final so we can refer to it from a window listener
 
         final FacePrototype app = new FacePrototype();
 
         // configuration
         app.init();
         
         // load faces - this simulates interaction with external POJO based application
         try {
             app.loadFaces();
         } catch (IOException eek) {
             System.out.println("Could not load Faces :"+eek);
         }
         // display
  
         // use anonymous WindowListener to clean up ..
         app.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 // This is where you prompt to save; commit transactions or rollback as needed
                 System.out.println("Goodbye");
 
                 // window is about to close; we could save out any state here
                 // so the user does not lose their work
 
                 // this is the same as HIDE_ON_CLOSE
                 app.setVisible(false);
                 // (you could leave it open to show a progress bar)
 
                 // this is the same as dispose on close; calls windowClosed()
                 app.dispose();
             }
 
             public void windowClosed(WindowEvent e) {
                 System.out.println("Finished");
                 app.cleanup();
                 System.exit(0);
             }
         });
         app.setSize(900, 900);
         app.setVisible(true);
 
         // even though this is the "end" of the main method the Swing thread was created
         // by setVisible above and will hold the application open (strange design really)
     }
     
 }
