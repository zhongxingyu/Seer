 package gov.usgs.cida.coastalhazards.uncy;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.FeatureWriter;
 import org.geotools.data.Transaction;
 import org.geotools.data.shapefile.ShapefileDataStore;
 import org.geotools.data.shapefile.ShapefileDataStoreFactory;
 import org.geotools.data.shapefile.ShpFileType;
 import org.geotools.data.shapefile.ShpFiles;
 import org.geotools.data.shapefile.dbf.DbaseFileHeader;
 import org.geotools.data.shapefile.dbf.DbaseFileReader;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.geometry.jts.JTSFactoryFinder;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.feature.type.AttributeType;
 import org.opengis.feature.type.GeometryType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jtsexample.geom.ExtendedCoordinate;
 
 
 /** Write a copy of the input shapefile, with lines exploded to their constituent points
  * and with the M columns used to look up the point-by-point uncertainty (if available).
  * 
  * @author rhayes
  *
  */
 public class Xploder {
 
 	public static final String PTS_SUFFIX = "_pts";
 
 	private static Logger logger = LoggerFactory.getLogger(Xploder.class);
 	
 	private int geomIdx = -1;
 	private FeatureWriter<SimpleFeatureType, SimpleFeature> featureWriter;
 	private Map<UncyKey,Double>  uncyMap;
 	private int dfltUncyIdx = -1;
 	private DbaseFileHeader dbfHdr;
 	private Transaction tx;
     private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
 
 	private int surveyIDIdx;
 
 	private static int locateField(DbaseFileHeader hdr, String nm, Class<?> expected) {
 		int idx = -1;
 		
 		for (int x = 0; x < hdr.getNumFields(); x++) {
 			String fnm = hdr.getFieldName(x);
 			if (nm.equalsIgnoreCase(fnm)) {
 				idx = x;
 			}
 		}
 		if (idx < 0) {
 			throw new RuntimeException("did not find column named " + nm);
 		}
 		
 		Class<?> idClass = hdr.getFieldClass(idx);		
 		if ( ! expected.isAssignableFrom(idClass)) {
 			throw new RuntimeException("Actual class " + idClass + " is not assignable to expected " + expected);
 		}
 
 		return idx;
 	}
 	
 	/** Key to point-by-point uncertainty. Must be hashable and ordered (used as lookup key).
 	 * 
 	 * @author rhayes
 	 *
 	 */
 	public static class UncyKey implements Comparable<UncyKey>{
 		private final int idx;
 		private final String surveyID;
 		
 		public UncyKey(int idx, String surveyID) {
 			this.idx = idx;
 			this.surveyID = surveyID;
 		}
 
 		public int getIdx() {
 			return idx;
 		}
 
 		public String getSurveyID() {
 			return surveyID;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + idx;
 			result = prime * result
 					+ ((surveyID == null) ? 0 : surveyID.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			UncyKey other = (UncyKey) obj;
 			if (idx != other.idx)
 				return false;
 			if (surveyID == null) {
 				if (other.surveyID != null)
 					return false;
 			} else if (!surveyID.equals(other.surveyID))
 				return false;
 			return true;
 		}
 
 		@Override
 		public int compareTo(UncyKey o) {
 			int v;
 			
 			v = Integer.compare(idx, o.idx);
 			if (v != 0) {
 				return v;
 			}
 			if (surveyID == null) {
 				if (o.surveyID == null) {
 					return 0;
 				}
 			}
 			v = surveyID.compareTo(o.surveyID);
 			return v;
 		}
 		
 	}
 	
 	private static Map<UncyKey,Double> readUncyFromDBF(String fn) throws Exception {
 		
 		ShpFiles shpFile = new ShpFiles(fn);
 		Charset charset = Charset.defaultCharset();
 		
 		DbaseFileReader rdr = new DbaseFileReader(shpFile, false, charset);
 		
 		DbaseFileHeader hdr = rdr.getHeader();
 		// System.out.println("Header: " + hdr);
 		
 		int uncyIdx = locateField(hdr, "uncy", Double.class);
 		int idIdx = locateField(hdr, "id", Number.class);
 		int surveyIdx = locateField(hdr,  "surveyID", String.class);
 		
 		Map<UncyKey,Double> value = new HashMap<UncyKey,Double>();
 		
 		while (rdr.hasNext()) {
 			Object[] ff = rdr.readEntry();
 			
 			Integer i = ((Number)ff[idIdx]).intValue();
 			Double d = (Double)ff[uncyIdx];
 			String surveyID = (String)ff[surveyIdx];
 			
 			UncyKey key = new UncyKey(i, surveyID);
 			value.put(key, d);
 		}
 		
 		rdr.close();
 		
 		logger.info("Read uncertainty map, size {}", value.size());
 		
 		return value;
 	}
 	
 	public int processShape(ShapeAndAttributes sap) throws Exception {
 
 		Double defaultUncertainty = (Double)sap.row.read(dfltUncyIdx);
 		String surveyID = (String)sap.row.read(surveyIDIdx);
 		
 		int ptCt = 0;
 		
 		for (Point p : sap) {
 			ExtendedCoordinate ec = (ExtendedCoordinate)p.getCoordinate();
 			
 			double uncy = defaultUncertainty;
 			
 			double md = ec.getM();
 			if ( ! Double.isNaN(md)) {
 				int mi = (int)md;
 				
 				UncyKey key = new UncyKey(mi, surveyID);
 				Double uv = uncyMap.get(key);
 				if (uv != null) {
 					uncy = uv;
 				}
 			}
 			
 			// write new point-thing-with-uncertainty
			writePoint(p, sap.row, uncy);
 			
 			ptCt ++;
 		}
 		
 		return ptCt;
 		
 	}
 
	public void writePoint(Point p, DbaseFileReader.Row row, double uncy) throws Exception {
 		
 		SimpleFeature writeFeature = featureWriter.next();
 		
 		// geometry field is first, otherwise we lose.
 		Point np = geometryFactory.createPoint(p.getCoordinate());
 		writeFeature.setAttribute(0, np);
 
 		// copy them other attributes over, replacing uncy
		for (int i = 0; i < dbfHdr.getNumFields(); i++) {
 			Object value;
 			if (i == dfltUncyIdx) {
 				value = uncy;
 			} else {
 				value = row.read(i);
 			}
 			writeFeature.setAttribute(i+1, value);
 		}
 		
 		featureWriter.write();
 	}
 	
 	private File initWriter(String fn) throws Exception {
 		// read input to get attributes
 		SimpleFeatureType sourceSchema = readSourceSchema(fn);
 		
 		// duplicate input schema, except replace geometry with Point
 		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
 		typeBuilder.setName(sourceSchema.getName() + PTS_SUFFIX);
 		typeBuilder.setCRS(sourceSchema.getCoordinateReferenceSystem());
 		
 		geomIdx = -1;
 		// dfltUncyIdx = -1;
 		int idx = 0;
 		for (AttributeDescriptor ad : sourceSchema.getAttributeDescriptors()) {
 			AttributeType at = ad.getType();
 			if (at instanceof GeometryType) {
 				typeBuilder.add(ad.getLocalName(), Point.class);
 				geomIdx = idx;
 			} else {
 				typeBuilder.add(ad.getLocalName(), ad.getType().getBinding());				
 			}
 			idx++;
 		}
 		SimpleFeatureType outputFeatureType = typeBuilder.buildFeatureType();
 
 		logger.debug("Output feature type is {}", outputFeatureType);
 		
 		File fout = new File(fn + PTS_SUFFIX + ".shp");
 		
 		Map<String, Serializable> connect = new HashMap<String, Serializable> ();
 		connect.put("url", fout.toURI().toURL());
         connect.put("create spatial index", Boolean.TRUE);
         
         ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
         ShapefileDataStore outputStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(connect);
         
 		outputStore.createSchema(outputFeatureType);
 
         featureWriter = outputStore.getFeatureWriterAppend(tx);
         
         logger.info("Will write {}", fout.getAbsolutePath());
         
         return fout;
 	}
 
 	private static SimpleFeatureType readSourceSchema(String fn)
 			throws MalformedURLException, IOException
 	{
 		File fin = new File(fn+".shp");
 		logger.debug("Reading source schema from {}", fin);
 		
 		Map<String, Serializable> connect = new HashMap<String, Serializable> ();
 		connect.put("url", fin.toURI().toURL());
 
 		DataStore inputStore = DataStoreFinder.getDataStore(connect);
 
 		String[] typeNames = inputStore.getTypeNames();
 		String typeName = typeNames[0];
 
 		SimpleFeatureSource featureSource = inputStore.getFeatureSource(typeName);
 		SimpleFeatureType sourceSchema = featureSource.getSchema();
 		
 		// this might kill the source schema.
 		inputStore.dispose();
 		
 		logger.debug("Source schema is {}", sourceSchema);
 		
 		return sourceSchema;
 	}
 	
 	public File explode(String fn) throws Exception {
 		MyShapefileReader rdr = initReader(fn);
 		
 		logger.debug("Input files from {}\n{}", fn, shapefileNames(rdr.getShpFiles()));
 		
 		tx = new DefaultTransaction("create");
 		File ptFile = initWriter(fn);
 		
 		// Too bad that the reader classes don't expose the ShpFiles.
 		
 		int shpCt = 0;
 		int ptTotal = 0;
 		
 		if (geomIdx != 0) {
 			throw new RuntimeException("This program only supports input that has the geometry as attribute 0");
 		}
 		for (ShapeAndAttributes saa : rdr) {
 			int ptCt = processShape(saa);
 			logger.debug("Wrote {} points for shape {}", ptCt, saa.record.toString());
 			
 			ptTotal += ptCt;
 			shpCt++;
 		}
 		
 		tx.commit();
 		
 		logger.info("Wrote {} points in {} shapes", ptTotal, shpCt);
 		
 		return ptFile;
 	}
 
 	private static String shapefileNames(ShpFiles shp) {
 		StringBuilder sb = new StringBuilder();
 		
 		Map<ShpFileType, String> m = shp.getFileNames();
 		for (Map.Entry<ShpFileType, String> me : m.entrySet()) {
 			sb.append(me.getKey()).append("\t").append(me.getValue()).append("\n");
 		}
 		
 		return sb.toString();
 	}
 	
 	private MyShapefileReader initReader(String fn) throws Exception {
 		MyShapefileReader rdr = new MyShapefileReader(fn);
 		
 		dbfHdr = rdr.getDbfHeader();
 		dfltUncyIdx = locateField(dbfHdr, "uncy", Double.class);
 		surveyIDIdx = locateField(dbfHdr, "surveyID", String.class);
 
 		uncyMap = readUncyFromDBF(fn + "_uncertainty.dbf");
 		return rdr;
 	}
 
 	public static void main(String[] args) throws Exception {
 		for (String fn : args) {
 			Xploder ego = new Xploder();
 
 			ego.explode(fn);
 			
 		}
 	}
 
 }
