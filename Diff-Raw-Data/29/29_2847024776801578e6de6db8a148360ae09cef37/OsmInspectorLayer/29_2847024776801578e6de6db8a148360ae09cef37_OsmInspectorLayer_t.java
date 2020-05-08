 package org.openstreetmap.josm.plugins.osminspector;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.Action;
 import javax.swing.Icon;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.map.DefaultMapContext;
 import org.geotools.map.MapContext;
 import org.geotools.map.MapLayer;
 import org.geotools.referencing.CRS;
 import org.geotools.renderer.lite.StreamingRenderer;
 import org.geotools.styling.FeatureTypeStyle;
 import org.geotools.styling.Fill;
 import org.geotools.styling.Graphic;
 import org.geotools.styling.Mark;
 import org.geotools.styling.Rule;
 import org.geotools.styling.Stroke;
 import org.geotools.styling.Style;
 import org.geotools.styling.StyleFactory;
 import org.geotools.styling.Symbolizer;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.GeometryDescriptor;
 import org.opengis.filter.FilterFactory2;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.data.Bounds;
 import org.openstreetmap.josm.data.coor.EastNorth;
 import org.openstreetmap.josm.data.coor.LatLon;
 import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
 import org.openstreetmap.josm.gui.MapView;
 import org.openstreetmap.josm.gui.layer.Layer;
 import org.openstreetmap.josm.tools.ImageProvider;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.MultiLineString;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.Polygon;
 
 @SuppressWarnings("deprecation")
 public class OsmInspectorLayer extends Layer {
 
 	private StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
 	private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
 	private StreamingRenderer renderer;
 	private CoordinateReferenceSystem crs;
 	private CoordinateReferenceSystem crsOSMI;
 	private GeomType geometryType;
 	private String geometryAttributeName;
 	private SimpleFeatureSource featureSource;
 	private MapContext context;
 	
 	private enum GeomType {
 		POINT, LINE, POLYGON
 	};
 
 	private static final Color LINE_COLOUR = Color.BLUE;
 	private static final Color FILL_COLOUR = Color.CYAN;
 	private static final Color SELECTED_COLOUR = Color.YELLOW;
 	private static final float OPACITY = 1.0f;
 	private static final float LINE_WIDTH = 1.0f;
 	private static final float POINT_SIZE = 10.0f;
 
 	private ArrayList< OSMIFeatureTracker > arrFeatures;
 	
 	public OsmInspectorLayer(GeoFabrikWFSClient wfsClient)
 			throws NoSuchAuthorityCodeException, FactoryException, IOException {
 		super("OsmInspector");
 		
 		arrFeatures 		= new ArrayList();
 		
 		// Step 3 - discovery; enhance to iterate over all types with bounds
 		
 		String typeNames[] 	= wfsClient.getTypeNames();
 		renderer 			= new StreamingRenderer();
 		crs 				= CRS.decode(Main.getProjection().toCode());
         crsOSMI 			= CRS.decode("EPSG:4326");
 		context 			= new DefaultMapContext(crsOSMI);
 		
 		for(int idx=1; idx < typeNames.length; ++idx) 
 		{
 			String typeName = typeNames[idx];
 
 			FeatureCollection<SimpleFeatureType, SimpleFeature> features = wfsClient.getFeatures( typeName );
 			setGeometry( typeName );
 		
 			System.out.println("Osm Inspector Features size: " + features.size());
 			Style style = createDefaultStyle();
 			context.addLayer(features, style);
 			
 			OSMIFeatureTracker tracker = new OSMIFeatureTracker( features );
 			arrFeatures.add( tracker );
 		}
 		
 		context.setTitle("Osm Inspector Errors");
 		renderer.setContext(context);
 	}
 
 	public void loadFeatures( GeoFabrikWFSClient wfsClient )
 			throws NoSuchAuthorityCodeException, FactoryException, IOException
 	{
 		String typeNames[] 	= wfsClient.getTypeNames();
 		int		layerOffset = 1;
 		
 		context.clearLayerList();
 		
 		for(int idx=layerOffset; idx < typeNames.length; ++idx) 
 		{
 			String typeName = typeNames[idx];
 
 			FeatureCollection<SimpleFeatureType, SimpleFeature> features = wfsClient.getFeatures( typeName );
			setGeometry( typeName );
			
 			System.out.println("Osm Inspector Features size: " + features.size());
 			
 			OSMIFeatureTracker tracker = arrFeatures.get( idx - layerOffset );
 			tracker.mergeFeatures( features );
 			
 			Style style = createDefaultStyle();
 			context.addLayer(tracker.getFeatures(), style);
 		}		
 	}
 	
 	private Style createDefaultStyle() {
 		Rule rule = createRule(LINE_COLOUR, FILL_COLOUR);
 
 		FeatureTypeStyle fts = sf.createFeatureTypeStyle();
 		fts.rules().add(rule);
 
 		Style style = sf.createStyle();
 		style.featureTypeStyles().add(fts);
 		return style;
 	}
 
 	private Rule createRule(Color outlineColor, Color fillColor) {
 		Symbolizer symbolizer = null;
 		Fill fill = null;
 		Stroke stroke = sf.createStroke(ff.literal(outlineColor),
 				ff.literal(LINE_WIDTH));
 
 		switch (geometryType) {
 		case POLYGON:
 			fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
 			symbolizer = sf.createPolygonSymbolizer(stroke, fill,
 					geometryAttributeName);
 			break;
 
 		case LINE:
 			symbolizer = sf.createLineSymbolizer(stroke, geometryAttributeName);
 			break;
 
 		case POINT:
 			fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
 
 			Mark mark = sf.getCircleMark();
 			mark.setFill(fill);
 			mark.setStroke(stroke);
 
 			Graphic graphic = sf.createDefaultGraphic();
 			graphic.graphicalSymbols().clear();
 			graphic.graphicalSymbols().add(mark);
 			graphic.setSize(ff.literal(POINT_SIZE));
 
 			symbolizer = sf.createPointSymbolizer(graphic,
 					geometryAttributeName);
 		}
 
 		Rule rule = sf.createRule();
 		rule.symbolizers().add(symbolizer);
 		return rule;
 	}
 
	private void setGeometry( String typename ) 
	{
		System.out.println("Passed type is" + typename);
 		if (typename.compareTo("duplicate_ways") == 0) 
 		{
 			geometryType = GeomType.LINE;
 		} 
 		else
 			geometryType = GeomType.POINT;
 	}
 
 	@Override
 	public Icon getIcon() {
 		return ImageProvider.get("cancel");
 	}
 
 	@Override
 	public Object getInfoComponent() {
 		return getToolTipText();
 	}
 
 	@Override
 	public Action[] getMenuEntries() {
 		return new Action[] {};
 	}
 
 	@Override
 	public String getToolTipText() {
 		return "OsmInspector";
 	}
 
 	@Override
 	public boolean isMergable(Layer other) {
 		return false;
 	}
 
 	@Override
 	public void mergeFrom(Layer from) {
 		return;
 	}
 
 	@Override
 	public void paint(Graphics2D g, MapView mv, Bounds box) 
 	{
 		LatLon min = box.getMin();
 		LatLon max = box.getMax();
 		
 		Coordinate northWest = new Coordinate(max.getX(), min.getY());
 		Coordinate southEast = new Coordinate(min.getX(), max.getY());
 		
         EastNorth center = Main.map.mapView.getCenter();
         EastNorth leftop = Main.map.mapView.getEastNorth(0, 0);
         EastNorth rightbot = Main.map.mapView.getEastNorth(mv.getBounds().width, mv.getBounds().height);
 
 		Envelope envelope = new Envelope( Math.min( leftop.east(), rightbot.east() ),
 											Math.max( leftop.east(), rightbot.east() ),
 											Math.min( rightbot.north(), leftop.north() ),
 											Math.max( rightbot.north(), leftop.north() )
 										);
 
 		ReferencedEnvelope mapArea = new ReferencedEnvelope(envelope, crs);
 
 		renderer.setInteractive( false );
 		renderer.paint(g, mv.getBounds(), mapArea);
 	}
 
 	@Override
 	public void visitBoundingBox(BoundingXYVisitor v) {
 	}
 
 }
