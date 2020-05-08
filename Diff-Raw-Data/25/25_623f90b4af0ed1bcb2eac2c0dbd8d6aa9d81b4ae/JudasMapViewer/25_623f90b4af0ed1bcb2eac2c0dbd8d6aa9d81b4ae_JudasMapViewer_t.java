 package org.iugonet.www;
 
 import gnu.getopt.Getopt;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FileDataStore;
 import org.geotools.data.FileDataStoreFinder;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.map.DefaultMapContext;
 import org.geotools.map.FeatureLayer;
 import org.geotools.map.Layer;
 import org.geotools.map.MapContent;
 import org.geotools.map.MapContext;
 import org.geotools.styling.SLDParser;
import org.geotools.styling.StyleFactory;
 import org.geotools.swing.JMapFrame;
 import org.geotools.swing.data.JFileDataStoreChooser;
 import org.geotools.swing.dialog.JExceptionReporter;
 import org.geotools.swing.styling.JSimpleStyleDialog;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.style.Style;
 
 public class JudasMapViewer extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private JPanel contentPane;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(final String[] args) {
 		Getopt getopt = new Getopt("JudasMapViewer", args, "u:f:");
 		int c;
 		while ((c = getopt.getopt()) != -1) {
 			switch (c) {
 			case 'u': // url
 				getopt.getOptarg();
 				break;
 			case 'f': // flag
 				Integer.parseInt(getopt.getOptarg());
 				break;
 			}
 		}
 
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					JudasMapViewer frame = new JudasMapViewer(args);
 					frame.setTitle("JudasViewer");
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 * @throws IOException 
 	 */
 	public JudasMapViewer(String[] args) throws IOException {
 
 		Icons icons = new Icons();
 		setIconImage(icons.getImage("icons/favicon.ico"));
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 300);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 
 		File file = JFileDataStoreChooser.showOpenFile("shp", null);
 		if (file == null) {
 			return;
 		}
 
 		FileDataStore store = FileDataStoreFinder.getDataStore(file);
 		SimpleFeatureSource featureSource = store.getFeatureSource();
 
 		// Create a map context and add our shapefile to it
         MapContent map = new MapContent();
         map.setTitle("StyleLab");
  
         // Create a basic Style to render the features
         Style style = createStyle(file, featureSource);
 
         // Add the features and the associated Style object to
         // the MapContent as a new Layer
        Layer layer = new FeatureLayer(featureSource, (org.geotools.styling.Style) style);
         map.addLayer(layer);
 
         // Now display the map
         JMapFrame.showMap(map);
 
 		/*
 		 * ChartPanel(chart);
 		 * 
 		 * getContentPane().add(cpanel); getContentPane().setVisible(true);
 		 */
 	}
 	
     /**
      * Create a Style to display the features. If an SLD file is in the same
      * directory as the shapefile then we will create the Style by processing
      * this. Otherwise we display a JSimpleStyleDialog to prompt the user for
      * preferences.
      */
     private Style createStyle(File file, FeatureSource featureSource) {
         File sld = toSLDFile(file);
         if (sld != null) {
             return createFromSLD(sld);
         }
 
         SimpleFeatureType schema = (SimpleFeatureType)featureSource.getSchema();
         return JSimpleStyleDialog.showDialog(null, schema);
     }
     
     /**
      * Figure out if a valid SLD file is available.
      */
     public File toSLDFile(File file)  {
         String path = file.getAbsolutePath();
         String base = path.substring(0,path.length()-4);
         String newPath = base + ".sld";
         File sld = new File( newPath );
         if( sld.exists() ){
             return sld;
         }
         newPath = base + ".SLD";
         sld = new File( newPath );
         if( sld.exists() ){
             return sld;
         }
         return null;
     }
 
     /**
      * Create a Style object from a definition in a SLD document
      */
     private Style createFromSLD(File sld) {
         try {
            StyleFactory styleFactory = null;
			SLDParser stylereader = new SLDParser(styleFactory, sld.toURI().toURL());
             Style[] style = stylereader.readXML();
             return style[0];
             
         } catch (Exception e) {
             JExceptionReporter.showDialog(e, "Problem creating style");
         }
         return null;
     }
}
