 /**
  * GeoTools Example
  * 
  *  (C) 2011 LISAsoft
  *  
  *  This library is free software; you can redistribute it and/or modify it under
  *  the terms of the GNU Lesser General Public License as published by the Free
  *  Software Foundation; version 2.1 of the License.
  *  
  *  This library is distributed in the hope that it will be useful, but WITHOUT
  *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  */
 package com.lisasoft.face;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JToolBar;
 
 import org.geotools.coverage.GridSampleDimension;
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
 import org.geotools.coverage.grid.io.AbstractGridFormat;
 import org.geotools.coverage.grid.io.GridFormatFinder;
 import org.geotools.data.DataStore;
 import org.geotools.data.DefaultRepository;
 import org.geotools.data.FileDataStore;
 import org.geotools.data.FileDataStoreFinder;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.feature.FeatureCollections;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.geometry.jts.JTSFactoryFinder;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.map.DefaultMapContext;
 import org.geotools.map.FeatureLayer;
 import org.geotools.map.GridReaderLayer;
 import org.geotools.map.MapContext;
 import org.geotools.referencing.CRS;
 import org.geotools.renderer.lite.StreamingRenderer;
 import org.geotools.styling.ChannelSelection;
 import org.geotools.styling.ContrastEnhancement;
 import org.geotools.styling.FeatureTypeStyle;
 import org.geotools.styling.Mark;
 import org.geotools.styling.RasterSymbolizer;
 import org.geotools.styling.SLD;
 import org.geotools.styling.SLDParser;
 import org.geotools.styling.SelectedChannelType;
 import org.geotools.styling.Style;
 import org.geotools.styling.StyleFactory;
 import org.geotools.swing.JMapPane;
 import org.geotools.swing.action.InfoAction;
 import org.geotools.swing.action.PanAction;
 import org.geotools.swing.action.ZoomInAction;
 import org.geotools.swing.action.ZoomOutAction;
 import org.geotools.swing.event.MapMouseEvent;
 import org.geotools.swing.table.FeatureCollectionTableModel;
 import org.geotools.swing.tool.CursorTool;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.Filter;
 import org.opengis.filter.FilterFactory2;
 import org.opengis.filter.identity.FeatureId;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.style.ContrastMethod;
 import org.opengis.style.Stroke;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 
 /**
  * This is a prototype application *just* showing how to integrate a MapComponent with an existing
  * application.
  * <p>
  * As such the details of this application are not all that interesting; they do serve to illustrate
  * how to:
  * <ol>
  * <li>set up a MapContent (this is used as the background for the MapComponent)</li>
  * <li>set up a MapComponent (this is actually a simple JPanel consisting of a JMapPane</li>
  * <li>set up a toolbar using some of the actions available for controlling MapComponent</li>
  * </ul> * In all cases this is straight forward application of the components provided by GeoTools.
  * <p>
  * Here is the interesting bit:
  * <ul>
  * <li>set up a MapComponentTable (actually a small JPanel consisting of a JTable working against
  * the MapComponent table model)</li>
  * <li>Custom table model; just backed by the Faces provided to MapComponentTable</li>
  * <li>Custom tool to "select" Faces in the MapComponent; this will update both update an internal
  * *filter* used to display selected faces; and update a list selection model published for use with
  * MapComponentTable.</li>
  * <li>Custom DataStore used to present the Faces provided to MapComponent to the GeoTools rendering
  * system. This is used by *two* layers. One layer to display the sites; and a second to display the
  * selection (These two lays are added to the MapContent).</li>
  * </ul>>
  * 
  * Implementation Notes:
  * <ul>
  * <li>SH: is creating the layout of this form using Eclipse 3.7 window builder tools (this is not
  * really important or interesting; just FYI)</li>
  * </ul>
  * 
  * @author Scott Henderson (LISASoft)
  * @author Jody Garnett (LISASoft)
  */
 public class Prototype extends JFrame {
     /** serialVersionUID */
     private static final long serialVersionUID = -1415741029620524123L;
 
     /**
      * Change this to match the EPSG code for your spatial reference system.
      * 
      * See: http://spatialreference.org/ref/?search=ch1903
      */
     public static String EPSG_CODE = "EPSG:2056";
     /**
      * Used to create GeoTools styles; based on OGC Style Layer Descriptor specification.
      */
     private StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
 
     /**
      * Used to create GeoTools filters; to query data.
      */
     private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
 
     /**
      * Table used to list Face content.
      */
     private JTable table;
     
     /**
      * Scroll pane used by table.
      */
     private JScrollPane scrollpane;
 
     /**
      * Small toolbar configured with both JMapPane tools (for panning and zooming) and custom tools
      * to select Face content.
      */
     private JToolBar toolBar;
     
     /**
      * TBD
      */
     private JMapPane mapPane;
     /**
      * Repository used to hold on to DataStores.
      */
     private DefaultRepository repo;
 
     /** Used to hold on to rasters */
     private Map<String, AbstractGridCoverage2DReader> raster;
 
     /**
      * The MapContext api is deprecated; the replacement MapContent
      * is being integrated with MapComponent as we speak.
      */
     private MapContext map;
 
     private SimpleFeatureCollection faces;
     private FeatureLayer facesLayer;
     private FeatureLayer selectedFaceLayer;
     
     private static final String FEAUTURE_EPSG = "EPSG:2056";
 
     /**
      * Create a Prototype Frame; please call init() to configure.
      * <p>
      * How to use:
      * 
      * <pre>
      * Prototype prototype = new Prototype();
      * // any configuration here
      * init();
      * show();
      * </pre>
      * 
      * Subclasses can override init() or set key methods inorder to control or experiment with how
      * this class functions. These methods are set up to show how to perform common tasks.
      */
     private Prototype() {
         super("AGP Prototype");
         repo = new DefaultRepository();
 
         raster = new LinkedHashMap<String, AbstractGridCoverage2DReader>();
     }
 
     /**
      * Prompts the user for a GeoTIFF file and a Shapefile and passes them to the displayLayers
      * method Usual protected init method called from the constructor(); subclasses can override key
      * methods in order to takepart in configuration.
      * <ul>
      * <li>loadData() - load data into a repository
      * <li>createMap() - create a MapContent
      * <li>loadSites() - load site information
      * <li>initUserInterface() - layout user interface components; this will create the MapComponent
      * and connect it to the required data model etc...
      * </ul>
      */
     protected void init() throws Exception {
         loadData();
         map = createMap(repo, raster);
         loadSites();
         initUserInterface();
     }
 
     /**
      * Used to laod data; any DataStore's laoded should be registered in a repository (so they can
      * be cleaned up).
      */
     private void loadData() {
         //File directory = new File(".");
         File directory = new File("./data");
         if (directory.exists() && directory.isDirectory()) {
             // check for shapefiles
             //
             File[] shapefiles = directory.listFiles(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.toUpperCase().endsWith(".SHP");
                 }
             });
             for (File shp : shapefiles) {
                 try {
                     File prj = checkPRJ(shp);
 
                     FileDataStore dataStore = FileDataStoreFinder.getDataStore(shp);
                     if (dataStore != null) {
                         for (String typeName : dataStore.getTypeNames()) {
                             repo.register(typeName, dataStore);
                         }
                     }
                 } catch (IOException eek) {
                     System.err.println("Unable to load shapefile " + shp + ":" + eek);
                     eek.printStackTrace(System.err);
                 }
             }
             // check for geotiff files
             File[] tiffFiles = directory.listFiles(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.toUpperCase().endsWith(".TIF")
                             || name.toUpperCase().endsWith(".TIFF");
                 }
             });
             for (File tif : tiffFiles) {
                 try {
                     // create PRJ files for .TIF images that do not have one
                     File prj = checkPRJ(tif);
                     
                     AbstractGridFormat format = GridFormatFinder.findFormat(tif);
                     AbstractGridCoverage2DReader reader = format.getReader(tif);
                     if (reader == null) {
                         System.err.println("Unable to load " + tif);
                         continue;
                     }
                     String fileName = tif.getName();
                     String name = fileName.substring(0, fileName.lastIndexOf(".") - 1);
                     raster.put(name, reader);
                 } catch (Throwable eek) {
                     System.err.println("Unable to load " + tif + ":" + eek);
                     eek.printStackTrace(System.err);
                 }
 
             }
         }
     }
 
     private File checkPRJ(File targetFile) {
         // why does Java not have a method to make this easy ...
         String base = targetFile.getName().substring(0, targetFile.getName().lastIndexOf("."));
 
         // this is a world plus image file
         File prj = new File(targetFile.getParentFile(), base + ".prj");
         if (!prj.exists()) {
             FileWriter writer = null;
 
             // prj not provided going to assume EPSG:4326 and write one out
             try {
                 // true is ask for easting / northing order to match the data
                 CoordinateReferenceSystem crs = CRS.decode("EPSG:4326",true);
                 String wkt = crs.toWKT();
 
                 writer = new FileWriter(prj);
                 writer.append(wkt);
             } catch (NoSuchAuthorityCodeException e) {
                 System.out.println("Did you an include an EPSG jar on the CLASSPATH?");
             } catch (FactoryException e) {
                 System.out.println("Did you an include an EPSG jar on the CLASSPATH?");
             } catch (IOException e) {
                 System.out.println("Unable to generate " + prj);
             } finally {
                 if (writer != null) {
                     try {
                         writer.close();
                     } catch (IOException e) {
                         System.out.println("Trouble closing " + prj);
                     }
                 }
             }
         }
         return prj;
     }
 
     /**
      * Displays a GeoTIFF file overlaid with a Shapefile
      * 
      * @param rasterFile the GeoTIFF file
      * @param shpFile the Shapefile
      */
     private MapContext createMap(DefaultRepository repo2,
             Map<String, AbstractGridCoverage2DReader> raster2) {
 
         // Set up a MapContext with the two layers
         final MapContext map = new DefaultMapContext();
 
         // use rasters as "basemap"
         for (Entry<String, AbstractGridCoverage2DReader> entry : raster.entrySet()) {
             // Initially display the raster in greyscale using the
             // data from the first image band
             String name = entry.getKey();
             AbstractGridCoverage2DReader reader = entry.getValue();
 
             Style style = createStyle(reader);
             GridReaderLayer layer = new GridReaderLayer(reader, style);
             if (reader.getInfo() != null && reader.getInfo().getTitle() != null) {
                 layer.setTitle(reader.getInfo().getTitle());
             }
             map.addLayer(layer);
         }
         // add shapefiles on top
         for (DataStore dataStore : repo.getDataStores()) {
 
             try {
                 for (String typeName : dataStore.getTypeNames()) {
                     SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
                     
                     Style style = SLD.createPolygonStyle(Color.RED, null, 0.0f);
                     FeatureLayer layer = new FeatureLayer(featureSource, style);
 
                     if (featureSource.getInfo() != null
                             && featureSource.getInfo().getTitle() != null) {
                         layer.setTitle(featureSource.getInfo().getTitle());
                     }
 
                     map.addLayer(layer);
                 }
             } catch (IOException e) {
                 System.err.print("Could not load " + dataStore);
             }
         }
 
         // configure map
         map.setTitle("Prototype");
 
         return map;
 
     }
     
     protected SimpleFeatureCollection createSiteCollection() {
         File csvFile = new File("data/senario.csv");
         SimpleFeatureCollection collection;
 
         if (csvFile.exists()) {
         	try {
         		return getFeaturesFromFile(csvFile);
         	} catch (Throwable eek) {
         		System.out.println("Could not load faces:" + eek);
         	}
         }
         /*
          * eww
          */
         return null;
     }
 
     /*
      * We create a FeatureCollection into which we will put each Feature created from a record in
      * the input csv data file
      */
     protected void loadSites() {
 
     	/*
     	 * First handle the actual renderable goodness.
     	 */
         Style style;
         try {
         	FileInputStream inputStream = new FileInputStream(new File("./data/rotating_symbol.sld"));
         	SLDParser stylereader = new SLDParser(sf, inputStream);
         	
         	Style styles[] = stylereader.readXML();
 
         	if(styles.length > 0) {
 //        		style = styles[0];
         		style = SLD.createPointStyle("triangle",Color.BLACK,Color.YELLOW,1.0f,16);
         	} else {
         		// Create a basic style with yellow lines and no fill
         		style = SLD.createPointStyle("triangle",Color.BLACK,Color.YELLOW,1.0f,16);
         	}
         } catch(IOException ex) {
         	style = SLD.createPointStyle("triangle",Color.BLACK,Color.YELLOW,1.0f,16);
         }
 
         //FeatureLayer layer = new FeatureLayer( faces, style );
         //map.addLayer( layer );
                 faces = createSiteCollection();
         facesLayer = new FeatureLayer(faces, style);
         map.addLayer(facesLayer);
         
         /*
          * Now let's worry about the selection layer.
          */
 		SimpleFeatureCollection newCollection = createSiteCollection();
 		Style selectionStyle = createSelectedStyle(new HashSet<FeatureId>());
 		selectedFaceLayer = new FeatureLayer(newCollection, style);
		map.addLayer(selectedFaceLayer);
//		map.layers().add(0, selectedFaceLayer);
 		
 		
     }
 
     @SuppressWarnings("deprecation")
     private void initUserInterface() {
         getContentPane().setLayout(new BorderLayout());
 
         JScrollPane scrollPane = new JScrollPane(table);
         getContentPane().add(scrollPane, BorderLayout.SOUTH);
 
         if (faces != null) {
             FeatureCollectionTableModel model = new FeatureCollectionTableModel(faces);
             table = new JTable();
             table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
             table.setPreferredScrollableViewportSize(new Dimension(800, 100));
             table.setModel(model);
             scrollpane = new JScrollPane(table);
         }
         /*
          * mapFrame.setSize(800, 600); mapFrame.enableStatusBar(true);
          * //frame.enableTool(JMapFrame.Tool.ZOOM, JMapFrame.Tool.PAN, JMapFrame.Tool.RESET);
          * mapFrame.enableToolBar(true);
          * 
          * JMenuBar menuBar = new JMenuBar(); mapFrame.setJMenuBar(menuBar); JMenu menu = new
          * JMenu("Raster"); menuBar.add(menu);
          * 
          * menu.add( new SafeAction("Grayscale display") { public void action(ActionEvent e) throws
          * Throwable { Style style = createGreyscaleStyle(); if (style != null) {
          * map.getLayer(0).setStyle(style); mapFrame.repaint(); } } });
          * 
          * menu.add( new SafeAction("RGB display") { public void action(ActionEvent e) throws
          * Throwable { Style style = createRGBStyle(); if (style != null) {
          * map.getLayer(0).setStyle(style); mapFrame.repaint(); } } });
          */
 
         mapPane = new JMapPane();
 
         // set a renderer to use with the map pane
         mapPane.setRenderer(new StreamingRenderer());
 
         // set the map context that contains the layers to be displayed
         mapPane.setMapContext(new MapContext(map));
         mapPane.setSize(800, 500);
 
         toolBar = new JToolBar();
         toolBar.setOrientation(JToolBar.HORIZONTAL);
         toolBar.setFloatable(false);
 
         JButton zoomInBtn = new JButton(new ZoomInAction(mapPane));
         toolBar.add(zoomInBtn);
 
         JButton zoomOutBtn = new JButton(new ZoomOutAction(mapPane));
         toolBar.add(zoomOutBtn);
         
         JButton panBtn = new JButton(new PanAction(mapPane));
         toolBar.add(panBtn);
         
         JButton infoBtn = new JButton(new InfoAction(mapPane));
         toolBar.add(infoBtn);
         
         JButton selectButton = new JButton("Select");
         toolBar.add(selectButton);
         
         selectButton.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		mapPane.setCursorTool(
         				new CursorTool() {
 
         					@Override
         					public void onMouseClicked(MapMouseEvent ev) {
         						selectFeatures(ev);
         					}
         				});
         	}
         });
 
         toolBar.setSize(800, 100);
 
         getContentPane().add(toolBar, BorderLayout.NORTH);
         getContentPane().add(mapPane, BorderLayout.CENTER);
         getContentPane().add(scrollpane, BorderLayout.SOUTH);
         // mapFrame.setVisible(true);
     }
     
     private static int SELECTION_BUFFER_WIDTH = 10;
     void selectFeatures(MapMouseEvent ev) {
     	/*
     	 * Create a small selection region.
     	 */
     	java.awt.Point screenPos = ev.getPoint();
     	Rectangle screenRect = new Rectangle(
     			screenPos.x-(SELECTION_BUFFER_WIDTH/2), 
     			screenPos.y-(SELECTION_BUFFER_WIDTH/2), 
     			SELECTION_BUFFER_WIDTH, SELECTION_BUFFER_WIDTH);
     	
     	/*
     	 * Transform the screen rectangle to map coordinates.
     	 */
     	AffineTransform screenToWorld = mapPane.getScreenToWorldTransform();
     	Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
     	ReferencedEnvelope bbox = new ReferencedEnvelope(
     			worldRect, mapPane.getMapContext().getCoordinateReferenceSystem());
     	
     	try {
     		ReferencedEnvelope filterBox = bbox.transform(faces.getSchema().getCoordinateReferenceSystem(), true);
 
     		/*
     		 * Create a Filter selecting the from the bounding box.
     		 */
     		Filter filter = ff.intersects(ff.property(faces.getSchema().getGeometryDescriptor().getName()), ff.literal(filterBox));
     		SimpleFeatureCollection selectedFeatures = faces.subCollection(filter);
     		SimpleFeatureIterator iter = selectedFeatures.features();
     		Set<FeatureId> ids = new HashSet<FeatureId>();
     		try {
     			while(iter.hasNext()) {
     				SimpleFeature feature = iter.next();
     				ids.add(feature.getIdentifier());
 //    				System.out.println("ID - " + feature.getIdentifier());
     			}
     		} finally {
     			iter.close();
     		}
     		
     		System.out.println("Selected " + ids.size() + " features.");
 
     		Style style = createSelectedStyle(ids);
     		selectedFaceLayer.setStyle(style);
    	        mapPane.repaint();
 
     	} catch(Exception ex) {
     		ex.printStackTrace();
     		return;
     	}
     }
     
     private static Color SELECTED_FILL_COLOR = Color.YELLOW;
    private static double SELECTED_FILL_OPACITY = 0.0;
    private static Color SELECTED_STROKE_COLOR = new Color(255, 0, 255);
    private static double SELECTED_STROKE_WIDTH = 2.5;
    private static double SELECTED_POINT_SIZE = 20.0;
     
     private Style createSelectedStyle(Set<FeatureId> ids) {
     	/*
     	 * First create the normal selection styler.
     	 */
     	org.geotools.styling.Symbolizer symbolizer = null;
     	org.opengis.style.Fill fill = sf.createFill(ff.literal(SELECTED_FILL_COLOR), ff.literal(SELECTED_FILL_OPACITY));
     	Stroke stroke = sf.createStroke(ff.literal(SELECTED_STROKE_COLOR), ff.literal(SELECTED_STROKE_WIDTH));
     	Mark mark = sf.getCircleMark();
     	mark.setFill(fill);
     	mark.setStroke(stroke);
     	
     	org.geotools.styling.Graphic graphic = sf.createDefaultGraphic();
     	graphic.graphicalSymbols().clear();
     	graphic.graphicalSymbols().add(mark);
     	graphic.setSize(ff.literal(SELECTED_POINT_SIZE));
     	
     	symbolizer = sf.createPointSymbolizer(graphic, faces.getSchema().getGeometryDescriptor().getName().toString());
     	
     	
     	org.geotools.styling.Rule selectedRule = sf.createRule();
     	selectedRule.symbolizers().add(symbolizer);
     	if(ids == null || ids.isEmpty()) {
     		System.out.println("Empty id set found.  Creating EXCLUDE filter.");
     		selectedRule.setFilter(Filter.EXCLUDE);
     	} else {
     		System.out.println("" + ids.size() + " ids found.  Creating filter.");
     		selectedRule.setFilter(ff.id(ids));
     	}
     	
     	FeatureTypeStyle fts = sf.createFeatureTypeStyle();
     	fts.rules().add(selectedRule);
     	
     	/*
     	 * Next the other styler.
     	 */
     	/*
     	Mark otherMark = sf.getCircleMark();
     	org.opengis.style.Fill otherFill = sf.createFill(ff.literal(SELECTED_FILL_COLOR), ff.literal(0.0));
     	mark.setStroke(stroke);
     	mark.setFill(otherFill);
     	
     	org.geotools.styling.Graphic otherGraphic = sf.createDefaultGraphic();
     	otherGraphic.graphicalSymbols().clear();
     	otherGraphic.graphicalSymbols().add(otherMark);
     	otherGraphic.setSize(ff.literal(SELECTED_POINT_SIZE));
     	
     	org.geotools.styling.Symbolizer otherSymbolizer = 
     		sf.createPointSymbolizer(graphic, faces.getSchema().getGeometryDescriptor().getName().toString());
     	org.geotools.styling.Rule otherRule = sf.createRule();
     	otherRule.setElseFilter(true);
     	otherRule.symbolizers().add(otherSymbolizer);
     	
     	fts.rules().add(otherRule);
     	*/
     	
     	Style style = sf.createStyle();
     	style.featureTypeStyles().add(fts);
     	return style;
     }
 
     /**
      * This is the opposite of init(); in this case we use it to dispose of all the DataStore we are
      * using. While this is not very exciting for Shapefile; it is important when working with JDBC
      * DataStores that have a real connection.
      */
     public void cleanup() {
         for (DataStore dataStore : repo.getDataStores()) {
             try {
                 dataStore.dispose();
             } catch (Throwable eek) {
                 System.err.print("Error cleaning up " + dataStore + ":" + eek);
             }
         }
     }
 
     // INTERNAL
     //
     // The following private methods are mostly used to set the stage; make them protected
     // if you wish to call them from a subclass
     //
     /**
      * Create a Style to display the specified band of the GeoTIFF image as a greyscale layer.
      * <p>
      * This method is a helper for createGreyScale() and is also called directly by the
      * displayLayers() method when the application first starts.
      * 
      * @param band the image band to use for the greyscale display
      * 
      * @return a new Style instance to render the image in greyscale
      */
     private Style createGreyscaleStyle(int band) {
         ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
         SelectedChannelType sct = sf.createSelectedChannelType(String.valueOf(band), ce);
 
         RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
         ChannelSelection sel = sf.channelSelection(sct);
         sym.setChannelSelection(sel);
 
         return SLD.wrapSymbolizers(sym);
     }
 
     /**
      * This method examines the names of the sample dimensions in the provided coverage looking for
      * "red...", "green..." and "blue..." (case insensitive match). If these names are not found it
      * uses bands 1, 2, and 3 for the red, green and blue channels. It then sets up a raster
      * symbolizer and returns this wrapped in a Style.
      * 
      * @param reader
      * 
      * @return a new Style object containing a raster symbolizer set up for RGB image
      */
     private Style createStyle(AbstractGridCoverage2DReader reader) {
         GridCoverage2D cov = null;
         try {
             cov = reader.read(null);
         } catch (IOException giveUp) {
             throw new RuntimeException(giveUp);
         }
         // We need at least three bands to create an RGB style
         int numBands = cov.getNumSampleDimensions();
         if (numBands < 3) {
             // assume the first brand
             return createGreyscaleStyle(1);
         }
         // Get the names of the bands
         String[] sampleDimensionNames = new String[numBands];
         for (int i = 0; i < numBands; i++) {
             GridSampleDimension dim = cov.getSampleDimension(i);
             sampleDimensionNames[i] = dim.getDescription().toString();
         }
         final int RED = 0, GREEN = 1, BLUE = 2;
         int[] channelNum = { -1, -1, -1 };
         // We examine the band names looking for "red...", "green...", "blue...".
         // Note that the channel numbers we record are indexed from 1, not 0.
         for (int i = 0; i < numBands; i++) {
             String name = sampleDimensionNames[i].toLowerCase();
             if (name != null) {
                 if (name.matches("red.*")) {
                     channelNum[RED] = i + 1;
                 } else if (name.matches("green.*")) {
                     channelNum[GREEN] = i + 1;
                 } else if (name.matches("blue.*")) {
                     channelNum[BLUE] = i + 1;
                 }
             }
         }
         // If we didn't find named bands "red...", "green...", "blue..."
         // we fall back to using the first three bands in order
         if (channelNum[RED] < 0 || channelNum[GREEN] < 0 || channelNum[BLUE] < 0) {
             channelNum[RED] = 1;
             channelNum[GREEN] = 2;
             channelNum[BLUE] = 3;
         }
         // Now we create a RasterSymbolizer using the selected channels
         SelectedChannelType[] sct = new SelectedChannelType[cov.getNumSampleDimensions()];
         ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
         for (int i = 0; i < 3; i++) {
             sct[i] = sf.createSelectedChannelType(String.valueOf(channelNum[i]), ce);
         }
         RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
         ChannelSelection sel = sf.channelSelection(sct[RED], sct[GREEN], sct[BLUE]);
         sym.setChannelSelection(sel);
 
         return SLD.wrapSymbolizers(sym);
     }
 
     /**
      * Here is how you can use a SimpleFeatureType builder to create the schema for your shapefile
      * dynamically.
      * <p>
      * This method is an improvement on the code used in the main method above (where we used
      * DataUtilities.createFeatureType) because we can set a Coordinate Reference System for the
      * FeatureType and a a maximum field length for the 'name' field dddd
      */
     private static SimpleFeatureType createFeatureType() {
 
         SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
         builder.setName("Location");
         
         // from email
         // Martin just got back to me and tells me that we have : ch1903
         // http://spatialreference.org/ref/?search=ch1903
         builder.setSRS(FEAUTURE_EPSG); // from email 
         
         builder.add("Identifier", Integer.class);
         builder.add("Type", String.class);
         builder.add("Face Format", String.class);
         builder.add("Product Face Format", String.class);
         builder.add("Status", String.class);
         builder.add("Installed on", String.class);
         builder.add("Posting Period", String.class);
         builder.add("Area", String.class);
         builder.add("Street", String.class);
         builder.add("House Number", String.class);
         builder.add("Point", Point.class);
         builder.add("Angle", String.class);
         builder.add("Category", String.class);
 
         // build the type
         final SimpleFeatureType LOCATION = builder.buildFeatureType();
 
         return LOCATION;
     }
 
     /**
      * Here is how you can use a SimpleFeatureType builder to create the schema for your shapefile
      * dynamically.
      * <p>
      * This method is an improvement on the code used in the main method above (where we used
      * DataUtilities.createFeatureType) because we can set a Coordinate Reference System for the
      * FeatureType and a a maximum field length for the 'name' field dddd
      */
     private SimpleFeatureCollection getFeaturesFromFile(File csvFile) throws Exception {
 
         final SimpleFeatureType TYPE = createFeatureType();
 
         /*
          * We create a FeatureCollection into which we will put each Feature created from a record
          * in the input csv data file
          */
         SimpleFeatureCollection collection = FeatureCollections.newCollection();
         /*
          * GeometryFactory will be used to create the geometry attribute of each feature (a Point
          * object for the location)
          */
         GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
 
         SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
 
         BufferedReader csvReader = new BufferedReader(new FileReader(csvFile));
         try {
             /* First line of the data file is the header */
             String line = csvReader.readLine();
             System.out.println("Header: " + line);
 
             for (line = csvReader.readLine(); line != null; line = csvReader.readLine()) {
                 if (line.trim().length() > 0) { // skip blank lines
                     String tokens[] = line.split("\\,");
 
                     int identifier = Integer.parseInt(tokens[0].trim());
                     String type = tokens[1].trim();
                     String faceFormat = tokens[2].trim();
                     String productFormat = tokens[3].trim();
                     String status = tokens[4].trim();
                     String installed = tokens[5].trim();
                     String posting = tokens[6].trim();
                     String area = tokens[7].trim();
                     String street = tokens[8].trim();
                     String number = tokens[9].trim();
                     double latitude = Double.parseDouble(tokens[10]);
                     double longitude = Double.parseDouble(tokens[11]);
                     String angle = tokens[12].trim();
                     String category = tokens[13].trim();
 
                     /* Longitude (= x coord) first ! */
                     Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
                     
                     featureBuilder.add(identifier);
                     featureBuilder.add(type);
                     featureBuilder.add(faceFormat);
                     featureBuilder.add(productFormat);
                     featureBuilder.add(status);
                     featureBuilder.add(installed);
                     featureBuilder.add(posting);
                     featureBuilder.add(area);
                     featureBuilder.add(street);
                     featureBuilder.add(number);
                     featureBuilder.add(point);
                     featureBuilder.add(angle);
                     featureBuilder.add(category);
                     
                     System.out.println("face." + identifier);
                     SimpleFeature feature = featureBuilder.buildFeature("face." + identifier);
                     collection.add(feature);
                 }
             }
         } finally {
             csvReader.close();
         }
 
         return collection;
     }
 
     /**
      * Opens the prototype user interface.
      * <p>
      * Please note any shapefiles or raster files in the current directory will be used as a
      * background.
      * 
      * @param args
      * @throws Exception
      */
     public static void main(String[] args) throws Exception {
         // marked final so we can refer to it from a window listener
 
         final Prototype app = new Prototype();
 
         // configuration
         app.init();
 
         // display
         // app.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 
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
