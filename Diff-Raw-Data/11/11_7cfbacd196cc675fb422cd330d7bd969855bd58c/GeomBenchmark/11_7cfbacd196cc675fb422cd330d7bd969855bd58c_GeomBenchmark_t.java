 package com.bennight;
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.data.FeatureSource;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureIterator;
 import org.jdom.output.EscapeStrategy;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.CoordinateList;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LinearRing;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Polygon;
 import com.vividsolutions.jts.geom.prep.PreparedPolygon;
 import com.vividsolutions.jts.io.WKTWriter;
 import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
 
 
 public class GeomBenchmark {
 
 	private List<Polygon> polygons;
 	private List<Envelope> boxes;
 
 	private List<Envelope> clip_boxes; // for clips
 	private List<Polygon> ellipses; // for intersections
 	private List<Integer> ids; // for tracing
 
 	private List<PreparedPolygon> ppolygons;
 	
 	private List<com.esri.core.geometry.Polygon> Epolygons;
 	private List<com.esri.core.geometry.Envelope> Eboxes;
 
 	private List<com.esri.core.geometry.Envelope2D> Eclip_boxes; // for clips
 	private List<com.esri.core.geometry.Polygon> Eellipses; // for intersections
 	private List<Integer> Eids; // for tracing
 
 	private List<com.esri.core.geometry.Polygon> Eppolygons;
 
 	com.esri.core.geometry.OperatorFactoryLocal factory = com.esri.core.geometry.OperatorFactoryLocal.getInstance();
 	com.esri.core.geometry.OperatorImportFromWkt operatorImport = (com.esri.core.geometry.OperatorImportFromWkt) factory.getOperator(com.esri.core.geometry.Operator.Type.ImportFromWkt);
 	com.esri.core.geometry.OperatorIntersection operatorIntersection = (com.esri.core.geometry.OperatorIntersection) factory.getOperator(com.esri.core.geometry.Operator.Type.Intersection);
 	com.esri.core.geometry.OperatorConvexHull operatorConvexHull = (com.esri.core.geometry.OperatorConvexHull) factory.getOperator(com.esri.core.geometry.Operator.Type.ConvexHull);
 	com.esri.core.geometry.OperatorClip operatorClip = (com.esri.core.geometry.OperatorClip) factory.getOperator(com.esri.core.geometry.Operator.Type.Clip);
 	com.esri.core.geometry.OperatorSimplify operatorSimplify = (com.esri.core.geometry.OperatorSimplify) factory.getOperator(com.esri.core.geometry.Operator.Type.Simplify);
 	com.esri.core.geometry.OperatorWithin operatorWithin = (com.esri.core.geometry.OperatorWithin) factory.getOperator(com.esri.core.geometry.Operator.Type.Within);
 	com.esri.core.geometry.OperatorContains operatorContains = (com.esri.core.geometry.OperatorContains) factory.getOperator(com.esri.core.geometry.Operator.Type.Contains);
 	com.esri.core.geometry.SpatialReference sr = com.esri.core.geometry.SpatialReference.create(4269);
 	WKTWriter wkt = new WKTWriter();	
 	private GeometryFactory geometryFactory;
 
 	// intersections/unions
 
 	public GeomBenchmark() {
 		super();
 		polygons = new ArrayList<Polygon>();
 		boxes = new ArrayList<Envelope>();
 		clip_boxes = new ArrayList<Envelope>(); // for clips
 		ellipses = new ArrayList<Polygon>(); // for
 		geometryFactory = new GeometryFactory();
 		ids = new ArrayList<Integer>();
 		
 		Epolygons = new ArrayList<com.esri.core.geometry.Polygon>();
 		Eboxes = new ArrayList<com.esri.core.geometry.Envelope>();
 		Eclip_boxes = new ArrayList<com.esri.core.geometry.Envelope2D>(); // for clips
 		Eellipses = new ArrayList<com.esri.core.geometry.Polygon>(); // for
 		Eids = new ArrayList<Integer>();
 
 
 	}
 
 
 	
 	private com.esri.core.geometry.Polygon JTStoESRIPoly(Polygon jtsGeom){
 		String wktP = wkt.write(jtsGeom);
 		return (com.esri.core.geometry.Polygon) operatorImport.execute(0, com.esri.core.geometry.Geometry.Type.Polygon , wktP, null);
 	}
 	
 	public void prepare(File shapefile) {
 		polygons.clear();
 		boxes.clear();
 		clip_boxes.clear();
 		ellipses.clear();
 		ids.clear();
 		
 		Epolygons.clear();
 		Eboxes.clear();
 		Eclip_boxes.clear();
 		Eellipses.clear();
 		Eids.clear();
 
 		geometryFactory = new GeometryFactory();
 
 		try {
 			// load all shapes from shapefile into polygons
 			read_shapefile(shapefile, polygons, ids);
 
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 			System.exit(1);
 		}
 
 		// ////////////////////////////////////////////////////////////////
 		ppolygons = new ArrayList<PreparedPolygon>(polygons.size());
 		
 
 		for (Polygon polygon : polygons) {
 			PreparedPolygon prepoly = new PreparedPolygon(polygon);
 			ppolygons.add(prepoly);
 			if (prepoly.contains(polygon.getCentroid())) {
 				// needed to cache it
 			}
 		}
 		// ////////////////////////////////////////////////////////////////
 
 		// Create envelopes
 		for (Polygon polygon : polygons) {
 			boxes.add(polygon.getEnvelopeInternal());
 			com.esri.core.geometry.Polygon p2 = JTStoESRIPoly(polygon);
 			Epolygons.add(p2);
 			com.esri.core.geometry.Envelope e2 = new com.esri.core.geometry.Envelope();
 			p2.queryEnvelope(e2);
 			Eboxes.add(e2);
 		}
 		
 		
 		// Create the star-ellipses for intersections later on
 		if (Compare.MEASURE_OVERLAY || Compare.MEASURE_CLIP) {
 			int k = 0;
 			for (Envelope box : boxes) {
 				k++;
 
 				double cx = box.centre().x;
 				double cy = box.centre().y;
 
 				double dx = box.getWidth();
 				double dy = box.getHeight();
 
 				if (Compare.MEASURE_OVERLAY) {
 					double a1 = Compare.OVERLAY_ELLIPSE_FACTOR1 * 0.5 * dx;
 					double b1 = Compare.OVERLAY_ELLIPSE_FACTOR1 * 0.5 * dy;
 					double a2 = Compare.OVERLAY_ELLIPSE_FACTOR2 * 0.5 * dx;
 					double b2 = Compare.OVERLAY_ELLIPSE_FACTOR2 * 0.5 * dy;
 
 					// We will use a coordinate list to build the linearring
 					CoordinateList clist = new CoordinateList();
 					// Compare.OVERLAY_ELLIPSE_COUNT);
 					double angle = 0.0; // 45.0 * ggl::math::d2r; //0.0;
 					for (int i = 0; i < Compare.OVERLAY_ELLIPSE_COUNT - 1; i++, angle += Compare.delta) {
 						if (i % 2 == 0) {
 							clist.add(new Coordinate(cx + a1 * Math.sin(angle),
 									cy + b1 * Math.cos(angle)));
 						} else {
 							clist.add(new Coordinate(cx + a2 * Math.sin(angle),
 									cy + b2 * Math.cos(angle)));
 						}
 					}
 
 					clist.add(clist.get(0));
 					LinearRing lr = geometryFactory.createLinearRing(clist.toCoordinateArray());
 					Polygon ellipse = geometryFactory.createPolygon(lr, null);
 					ellipses.add(ellipse);
 					com.esri.core.geometry.Polygon e2 = JTStoESRIPoly(ellipse);
 					Eellipses.add(e2);
 				}
 
 				if (Compare.MEASURE_CLIP) {
 					// note : weird use of sin/cos for a constant angle
 					// effectively this create a box . shrinkBy(
 					// Compare.CLIP_FACTOR * 0.5*sqrt2)?
 					double a = Compare.CLIP_FACTOR * 0.5 * dx;
 					double b = Compare.CLIP_FACTOR * 0.5 * dy;
 
 					double angle1 = Math.toRadians(225.0);
 					double angle2 = Math.toRadians(45.0);
 
 					double x0 = (cx + a * Math.sin(angle1));
 					double y0 = (cy + b * Math.cos(angle1));
 
 					double x1 = (cx + a * Math.sin(angle2));
 					double y1 = (cy + b * Math.cos(angle2));
 
 					Envelope clipbox = new Envelope(x0, x1, y0, y1);
 					clip_boxes.add(clipbox);
 					com.esri.core.geometry.Envelope2D env = new com.esri.core.geometry.Envelope2D(x0, y0, x1, y1);
 					Eclip_boxes.add(env);
 				}
 			}
 		}
 	}
 
 	
 	public void runchecks() {
 		if (Compare.MEASURE_AREA) {
 			System.out.println("");
 			//JTS
 			double area = 0;
 			long t0 = System.nanoTime();
 			for (int i = 0; i < Compare.AREA_COUNT; i++) {
 				for (Polygon polygon : polygons) {
 					area += polygon.getArea();
 				}
 			}
 			long t1 = System.nanoTime();
 			Compare.report_area("JTS", t1 - t0, polygons.size(), area);
 			
 			//ESRI
 			area = 0;
 			t0 = System.nanoTime();
 			for (int i = 0; i < Compare.AREA_COUNT; i++) {
 				for (com.esri.core.geometry.Polygon polygon : Epolygons) {
 					area += polygon.calculateArea2D();
 				}
 			}
 			t1 = System.nanoTime();
 			Compare.report_area("ESRI", t1 - t0, polygons.size(), area);
 		}
 
 		if (Compare.MEASURE_CENTROID) {
 			System.out.println("");
 			//JTS
 			double sum_x = 0, sum_y = 0;
 			long t0 = System.nanoTime();
 			for (int i = 0; i < Compare.CENTROID_COUNT; i++) {
 				for (Polygon polygon : polygons) {
 					Point centroid = polygon.getCentroid();
 					sum_x += centroid.getX();
 					sum_y += centroid.getY();
 				}
 			}
 			long t1 = System.nanoTime();
 			Compare.report_centroid("JTS", t1 - t0, polygons.size(), sum_x, sum_y);
 			
 			//ESRI
 			Compare.report_centroid("ESRI - UNSUPPORTED", -1L, -1, -1D, -1D);
 			
 		}
 
 		if (Compare.MEASURE_CONVEX_HULL) {
 			System.out.println("");
 			//JTS
 			double area = 0.0;
 			long t0 = System.nanoTime();
 			for (Polygon polygon : polygons) {
 				Geometry hull = polygon.convexHull();
 				if (Compare.HULL_AREA) {
 					area += Math.abs(hull.getArea());
 				}
 			}
 			long t1 = System.nanoTime();
 			Compare.report_hull("JTS", t1 - t0, polygons.size(), area);
 			
 			//ESRI
 			area = 0.0;
 			t0 = System.nanoTime();
 			for (com.esri.core.geometry.Polygon polygon : Epolygons) {
 				com.esri.core.geometry.Polygon hull = (com.esri.core.geometry.Polygon) operatorConvexHull.execute(polygon,null);
 				if (Compare.HULL_AREA) {
 					area += Math.abs(hull.calculateArea2D());
 				}
 			}
 			t1 = System.nanoTime();
 			Compare.report_hull("ESRI", t1 - t0, polygons.size(), area);
 		}
 
 		if (Compare.MEASURE_OVERLAY) {
 			System.out.println("");
 			//JTS
 			double area1 = 0.0, area2 = 0.0;
 			long t0 = System.nanoTime();
 			for (int i = 0; i < Compare.OVERLAY_COUNT; i++) {
 				int k = 0;
 				Iterator<Polygon> eit = ellipses.iterator();
 				for (Iterator<Polygon> pit = polygons.iterator(); pit.hasNext()	&& eit.hasNext(); k++) {
 					Polygon poly = pit.next();
 					Polygon ellipse = eit.next();
 					if (Compare.OVERLAY_AREA) {
 						area1 += poly.getArea();
 					}
 					Geometry v = ellipse.intersection(poly);
 					if (Compare.OVERLAY_AREA) {
 						area2 += v.getArea();
 					}
 				}
 			}
 			long t1 = System.nanoTime();
 			Compare.report_overlay("JTS", t1 - t0, polygons.size(), area1, area2);
 			
 			
 			//ESRI
 			t0 = System.nanoTime();
			area1 = 0;
			area2 = 0;
 			for (int i = 0; i < Compare.OVERLAY_COUNT; i++) {
 				int k = 0;
 				Iterator<com.esri.core.geometry.Polygon> eit = Eellipses.iterator();
 				for (Iterator<com.esri.core.geometry.Polygon> pit = Epolygons.iterator(); pit.hasNext()&& eit.hasNext(); k++) {
 					com.esri.core.geometry.Polygon poly = pit.next();
 					com.esri.core.geometry.Polygon ellipse = eit.next();
 					if (Compare.OVERLAY_AREA) {
 						area1 += poly.calculateArea2D();
 					}
 					com.esri.core.geometry.GeometryCursor cursor1 = new com.esri.core.geometry.SimpleGeometryCursor(poly);
 					com.esri.core.geometry.GeometryCursor cursor2 = new com.esri.core.geometry.SimpleGeometryCursor(ellipse);
 					com.esri.core.geometry.GeometryCursor outputGeoms = operatorIntersection.execute(cursor1, cursor2, sr, null);
 					com.esri.core.geometry.Geometry intersect = outputGeoms.next();
 					if (Compare.OVERLAY_AREA) {
 						area2 += intersect.calculateArea2D();
 					}
 				}
 			}
 			t1 = System.nanoTime();
 			Compare.report_overlay("ESRI", t1 - t0, polygons.size(), area1, area2);
 			
 		}
 
 		if (Compare.MEASURE_CLIP) {
 			System.out.println("");
 			//JTS
 			boolean first = true;
 			double area1 = 0.0, area2 = 0.0;
 			long t0 = System.nanoTime();
 			for (int i = 0; i < Compare.CLIP_COUNT; i++) {
 				Iterator<Envelope> bit = clip_boxes.iterator();
 				Iterator<Polygon> pit = polygons.iterator();
 				for (int k = 0; pit.hasNext() && bit.hasNext(); k++) {
 					Polygon poly = pit.next();
 					Envelope clipenv = bit.next();
 					Geometry clipgeom = geometryFactory.toGeometry(clipenv);
 					if (Compare.CLIP_AREA) {
 						area1 += poly.getArea();
 					}
 					Geometry v = clipgeom.intersection(poly);
 					if (Compare.CLIP_AREA) {
 						area2 += v.getArea();
 					}
 				}
 			}
 			long t1 = System.nanoTime();
 			Compare.report_clip("JTS", t1 - t0, polygons.size(), area1, area2);
 			
 			//ESRI
 			first = true;
 			area1 = 0.0; 
 			area2 = 0.0;
 			t0 = System.nanoTime();
 			for (int i = 0; i < Compare.CLIP_COUNT; i++) {
 				Iterator<com.esri.core.geometry.Envelope2D> bit = Eclip_boxes.iterator();
 				Iterator<com.esri.core.geometry.Polygon> pit = Epolygons.iterator();
 				for (int k = 0; pit.hasNext() && bit.hasNext(); k++) {
 					com.esri.core.geometry.Polygon poly = pit.next();
 					com.esri.core.geometry.Envelope2D clipenv = bit.next();
 
 					
 					if (Compare.CLIP_AREA) {
 						area1 += poly.calculateArea2D();
 					}
 					com.esri.core.geometry.Geometry g = operatorClip.execute(poly, clipenv, sr, null);
 					//com.esri.core.geometry.Geometry v = operatorIntersection.execute(poly,g, sr, null);
 					if (Compare.CLIP_AREA) {
 						area2 += g.calculateArea2D();
 					}
 				}
 			}
 			t1 = System.nanoTime();
 			Compare.report_clip("ESRI", t1 - t0, polygons.size(), area1, area2);
 		}
 
 		if (Compare.MEASURE_SIMPLIFY) {
 			System.out.println("");
 			//JTS
 			int count1 = 0, count2 = 0;
 			double length1 = 0.0, length2 = 0.0;
 			long t0 = System.nanoTime();
 			for (Polygon polygon : polygons) {
 				Geometry simplegeom = DouglasPeuckerSimplifier.simplify(
 						polygon, Compare.SIMPLIFY_DISTANCE);
 				count1 += polygon.getNumPoints();
 				count2 += simplegeom.getNumPoints();
 				if (Compare.SIMPLIFY_LENGTH) {
 					length1 += polygon.getLength();
 					length2 += simplegeom.getLength();
 				}
 
 			}
 			long t1 = System.nanoTime();
 			Compare.report_simplify("JTS", t1 - t0, polygons.size(), length1, length2,	count1, count2);
 			
 			//ESRI
 			count1 = 0;
 			count2 = 0;
 			length1 = 0.0;
 			length2 = 0.0;
 			t0 = System.nanoTime();
 			for (com.esri.core.geometry.Polygon polygon : Epolygons) {
 				
 				com.esri.core.geometry.Geometry simplegeom = operatorSimplify.execute(polygon, sr, false,null);
 				count1 += polygon.getPointCount();
 				count2 += ((com.esri.core.geometry.Polygon)simplegeom).getPointCount();
 				if (Compare.SIMPLIFY_LENGTH) {
 					length1 += polygon.calculateLength2D();
 					length2 += simplegeom.calculateLength2D();
 				}
 
 			}
 			t1 = System.nanoTime();
 			Compare.report_simplify("ESRI", t1 - t0, polygons.size(), length1, length2,	count1, count2);
 
 		}
 
 		
 		if (Compare.MEASURE_WITHIN) {
 			System.out.println("");
 			//JTS
 			int count = 0;
 			long t0 = System.nanoTime();
 			for (int e = 0; e < boxes.size(); e++) {
 				Envelope b = boxes.get(e);
 				Coordinate c = b.centre();
 				Point p = geometryFactory.createPoint(c);
 				Iterator<Envelope> bit = boxes.iterator();
 				Iterator<Polygon> pit = polygons.iterator();
 				for (int k = 0; pit.hasNext() && bit.hasNext(); k++) {
 					Polygon poly = pit.next();
 					Envelope box = bit.next();
 					if (box.contains(c) && p.within(poly)) {
 						count++;
 					}
 				}
 			}
 			long t1 = System.nanoTime();
 			Compare.report_within("JTS", t1 - t0, polygons.size(), count, -1);
 			
 			
 			//ESRI
 			count = 0;
 			t0 = System.nanoTime();
 			for (int e = 0; e < Eboxes.size(); e++) {
 				com.esri.core.geometry.Envelope b = Eboxes.get(e);
 				
 				com.esri.core.geometry.Point p = b.getCenter();
 				Iterator<com.esri.core.geometry.Envelope> bit = Eboxes.iterator();
 				Iterator<com.esri.core.geometry.Polygon> pit = Epolygons.iterator();
 				for (int k = 0; pit.hasNext() && bit.hasNext(); k++) {
 					com.esri.core.geometry.Polygon poly = pit.next();
 					com.esri.core.geometry.Envelope box = bit.next();
 					if (box.contains(p) && operatorWithin.execute(p, poly, sr,null)) {
 						count++;
 					}
 				}
 			}
 			t1 = System.nanoTime();
 			Compare.report_within("ESRI", t1 - t0, polygons.size(), count, -1);
 		}
 
 
 		if (Compare.MEASURE_CONTAINS) {
 			System.out.println("");
 			//JTS
 			int count = 0;
 			List<Point> points = new ArrayList<Point>(boxes.size());
 			for (int e = 0; e < boxes.size(); e++) {
 				Envelope b = boxes.get(e);
 				Coordinate c = b.centre();
 				Point p = geometryFactory.createPoint(c);
 				points.add(p);
 			}
 			long t0 = System.nanoTime();
 			Iterator<Point> pointIt = points.iterator();
 			Iterator<Polygon> pit = polygons.iterator();
 			for (int k = 0; pit.hasNext() && pointIt.hasNext(); k++) {
 				Polygon poly = pit.next();
 				Point p = pointIt.next();
 				if (poly.contains(p)) {
 					count++;
 				}
 			}
 			long t1 = System.nanoTime();
 			Compare.report_contains("JTS", t1 - t0, polygons.size(), count, -1);
 			
 			
 			
 			
 			//ESRI
 			count = 0;
 			List<com.esri.core.geometry.Point> Epoints = new ArrayList<com.esri.core.geometry.Point>(boxes.size());
 			for (int e = 0; e < Eboxes.size(); e++) {
 				com.esri.core.geometry.Envelope b = Eboxes.get(e);
 				com.esri.core.geometry.Point p = b.getCenter();
 				Epoints.add(p);
 			}
 			
 			t0 = System.nanoTime();
 			Iterator<com.esri.core.geometry.Point> EpointIt = Epoints.iterator();
 			Iterator<com.esri.core.geometry.Polygon> Epit = Epolygons.iterator();
 			for (int k = 0; Epit.hasNext() && EpointIt.hasNext(); k++) {
 				com.esri.core.geometry.Polygon poly = Epit.next();
 				com.esri.core.geometry.Point p = EpointIt.next();
 				if (operatorContains.execute(poly, p, sr, null)) {
 					count++;
 				}
 			}
 			t1 = System.nanoTime();
			Compare.report_contains("ESRI", t1 - t0, polygons.size(), count, -1);
 
 		}
 	}
 
 	public static void main(String[] args) throws Exception {
 		GeomBenchmark c = new GeomBenchmark();
 		File file = new File("d:/tempshape/c_02ap13.shp");
 		c.prepare(file);
 		// wait
 		c.runchecks();
 	}
 
 	public static void read_shapefile(File file, List<Polygon> polygons,
 			List<Integer> ids) {
 		try {
 			/*
 			 * Attmpt to find a GeoTools DataStore that can handle the shapefile
 			 */
 			Map<String, Serializable> connectParameters = new HashMap<String, Serializable>();
 
 			connectParameters.put("url", file.toURI().toURL());
 			connectParameters.put("create spatial index", false);
 
 			DataStore dataStore = DataStoreFinder
 					.getDataStore(connectParameters);
 			if (dataStore == null) {
 				System.out.println("No DataStore found to handle"
 						+ file.getPath());
 				System.exit(1);
 			}
 
 			/*
 			 * We are now connected to the shapefile. Get the type name of the
 			 * features within it
 			 */
 			String[] typeNames = dataStore.getTypeNames();
 			String typeName = typeNames[0];
 
 			System.out.println("Reading content " + typeName);
 
 			/*
 			 * Iterate through the features, collecting some spatial data (line
 			 * or boundary length) on each one
 			 */
 			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
 			FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
 			FeatureIterator<SimpleFeature> iterator;
 
 			featureSource = dataStore.getFeatureSource(typeName);
 			collection = featureSource.getFeatures();
 			iterator = collection.features();
 
 			int i = 0;
 			double totalArea = 0.0;
 			try {
 				while (iterator.hasNext()) {
 					SimpleFeature feature = iterator.next();
 
 					i++;
 					// System.out.println(i+"  :"+feature.getID());
 					/*
 					 * The spatial portion of the feature is represented by a
 					 * Geometry object
 					 */
 					Geometry geometry = (Geometry) feature.getDefaultGeometry();
 					// TODO validate polygon?
 
 					// Process only polygons, and from them only single-polygons
 					// without holes
 					Polygon polygon = null;
 					if (geometry instanceof Polygon) {
 						polygon = (Polygon) geometry;
 					} else if (geometry instanceof MultiPolygon) {
 						MultiPolygon mp = (MultiPolygon) geometry;
 						if (mp.getNumGeometries() == 1) {
 							polygon = (Polygon) mp.getGeometryN(0);
 						} else {
 							/*System.out
 									.println(i
 											+ "skipped:  not a single polygon multipolygon");
 							*/
 						}
 					} else {
 						System.out.println(i
 								+ " skipped: not a (multi)polygon:"
 								+ geometry.getGeometryType());
 					}
 
 					if (polygon != null) {
 						if (polygon.getNumInteriorRing() == 0) {
 							totalArea += polygon.getArea();
 							polygons.add(polygon);
 							ids.add(i);
 						} else {
 							/*System.out.println(i
 									+ "  not a single ring polygon:"
 									+ geometry.getGeometryType());
 							*/
 						}
 					}
 
 				}
 			} finally {
 				/*
 				 * You MUST explicitly close the feature iterator otherwise
 				 * terrible things will happen !!!
 				 */
 				if (iterator != null) {
 					iterator.close();
 				}
 			}
 
 			System.out.println("Total Area" + totalArea);
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			System.exit(1);
 		}
 	}
 }
