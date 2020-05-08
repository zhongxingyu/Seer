 package gov.usgs.gdp.geotools;
 
 
 import static org.junit.Assert.*;
 import gov.usgs.gdp.analysis.GeoToolsFileAnalysis;
 import gov.usgs.gdp.helper.FileHelper;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.geotools.data.FileDataStore;
 import org.geotools.data.shapefile.dbf.DbaseFileHeader;
 import org.geotools.data.shapefile.dbf.DbaseFileReader;
 import org.geotools.data.shapefile.shp.ShapefileException;
 import org.geotools.data.shapefile.shp.ShapefileReader;
 import org.geotools.data.shapefile.shp.ShapefileReader.Record;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 public class GeoToolsFileAnalysisTest {
 	private String tempDir = ""; 
 	private String seperator = "";
 	@Before
 	public void setUp() throws Exception {
 		this.tempDir = System.getProperty("java.io.tmpdir");
 
 		if ( !(this.tempDir.endsWith("/") || this.tempDir.endsWith("\\")) )
 		   this.tempDir = this.tempDir + System.getProperty("file.separator");
 		String systemTempDir = System.getProperty("java.io.tmpdir"); 
 		this.seperator =  java.io.File.separator;
 		String currentTime = Long.toString((new Date()).getTime());
 		this.tempDir = systemTempDir + this.seperator + "GDP-APP-TEMP" + this.seperator + currentTime;
 		(new File(this.tempDir)).mkdir();
 		
 		// Copy example files 
 		ClassLoader cl = Thread.currentThread().getContextClassLoader(); 
 		URL sampleFileLocation = cl.getResource("Sample_Files/");
 		if (sampleFileLocation != null) {
 			File sampleFiles = null;
 			try {
 				sampleFiles = new File(sampleFileLocation.toURI());
 			} catch (URISyntaxException e) {
 				assertTrue("Exception encountered: " + e.getMessage(), false);
 			}
 			FileHelper.copyFileToFile(sampleFiles, this.tempDir + this.seperator);
 		} else {
 			assertTrue("Sample files could not be loaded for test", false);
 		}
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		FileUtils.deleteDirectory((new File(this.tempDir)));
 	}
 
 	@Test
 	public void testGetDBaseFileSummary() {
 		List<String> result =  null;
 		String fileToLoad = this.tempDir 
 			+ this.seperator 
 			+ "Sample_Files" 
 			+ this.seperator
 			+ "Shapefiles" 
 			+ this.seperator
			+ "hru20VSR.DBF";
 		String nullString = null;
 		File loadedFile = new File(fileToLoad);
 		
 		GeoToolsFileAnalysis analyzer = new GeoToolsFileAnalysis();
 		assertNull(result);
 		try {
 			result = analyzer.getDBaseFileSummary();
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 		try {
 			result = GeoToolsFileAnalysis.getDBaseFileSummary("");
 			assertNull(result);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 		try {
 			result = GeoToolsFileAnalysis.getDBaseFileSummary(nullString);
 			assertNull(result);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 		try {
 			result = GeoToolsFileAnalysis.getDBaseFileSummary(loadedFile);
 			assertNotNull(result);
 			assertFalse(result.isEmpty());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 		try {
 			result = GeoToolsFileAnalysis.getDBaseFileSummary(fileToLoad);
 			assertNotNull(result);
 			assertFalse(result.isEmpty());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		try {
 			analyzer.setDbFileReader(GeoToolsFileAnalysis.readInDBaseFile(FileHelper.loadFile(fileToLoad), false, Charset.defaultCharset()));
 			assertNotNull(analyzer.getDbFileReader()); // Ensure we've read in a file		
 			result = analyzer.getDBaseFileSummary();
 			assertNotNull("Analyzer returned a null result object", result);
 			assertFalse("Analyzer returned an empty result object",result.isEmpty());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		loadedFile.delete();
 	}
 	
 	@Test
 	public void testProcessShapeFilePolygons() {
 		String fileToLoad = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
		+ "statesp020.shp";
 		
 		ShapefileReader result = null;
 		try {
 			result = GeoToolsFileAnalysis.getShapeFileReader(fileToLoad);
 			try {
 				while (result.hasNext()) {
 					Record record = result.nextRecord();
 					Geometry shape = (Geometry) record.shape();
 					int numOfGeometries = shape.getNumGeometries();
 					assertTrue(numOfGeometries > 0);
 				}
 			} finally {
 				try { result.close(); } catch (IOException e) {	e.printStackTrace(); }
 			}
 		} catch (ShapefileException e1) {
 			fail(e1.getMessage());
 		} catch (MalformedURLException e1) {
 			fail(e1.getMessage());
 		} catch (IOException e1) {
 			fail(e1.getMessage());
 		}
 		
 			
 	}
 	
 	@Test
 	public void testGetShapeFileReader() {
 		String fileToLoad = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
		+ "hru20VSR.SHP";
 		
 		String nullString= null;
 		ShapefileReader result;
 		try {
 			result = GeoToolsFileAnalysis.getShapeFileReader(nullString);
 			assertNull(result);
 		} catch (ShapefileException e1) {
 			fail(e1.getMessage());
 		} catch (MalformedURLException e1) {
 			fail(e1.getMessage());
 		} catch (IOException e1) {
 			fail(e1.getMessage());
 		}
 		
 		
 		File nullFile = null;
 		try {
 			result = GeoToolsFileAnalysis.getShapeFileReader(nullFile);
 			assertNull(result);
 		} catch (ShapefileException e1) {
 			fail(e1.getMessage());
 		} catch (MalformedURLException e1) {
 			fail(e1.getMessage());
 		} catch (IOException e1) {
 			fail(e1.getMessage());
 		}
 		
 		
 		try {
 			result = GeoToolsFileAnalysis.getShapeFileReader(fileToLoad);
 			assertNotNull(result);
 			result.close();
 		} catch (ShapefileException e1) {
 			fail(e1.getMessage());
 		} catch (MalformedURLException e1) {
 			fail(e1.getMessage());
 		} catch (IOException e1) {
 			fail(e1.getMessage());
 		}
 		
 	
 	}
 	
 	@Test
 	public void testGetShapeFileHeaderSummary() {
 		List<String> result = null;
 		String fileToLoad = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
		+ "hru20VSR.SHP";
 		
 		String nullString = null;
 		ShapefileReader reader;
 		try {
 			reader = GeoToolsFileAnalysis.getShapeFileReader(nullString);
 			assertNull(reader);
 		} catch (ShapefileException e1) {
 			fail(e1.getMessage());
 		} catch (MalformedURLException e1) {
 			fail(e1.getMessage());
 		} catch (IOException e1) {
 			fail(e1.getMessage());
 		}
 		
 		
 		try {
 			reader = GeoToolsFileAnalysis.getShapeFileReader(fileToLoad);
 			assertNotNull(reader);
 			result = GeoToolsFileAnalysis.getShapeFileHeaderSummary(reader);
 			assertNotNull("GeoToolsFileAnalysis.getShapeFileHeaderSummary returned null",result);
 			assertFalse("GeoToolsFileAnalysis.getShapeFileHeaderSummary returned empty List of type String",result.isEmpty());
 			reader.close();
 			
 			result = GeoToolsFileAnalysis.getShapeFileHeaderSummary(reader);
 			assertNull(result);
 			
 			ShapefileReader nullReader = null;
 			result = GeoToolsFileAnalysis.getShapeFileHeaderSummary(nullReader);
 			assertNull(result);
 		} catch (ShapefileException e1) {
 			fail(e1.getMessage());
 		} catch (MalformedURLException e1) {
 			fail(e1.getMessage());
 		} catch (IOException e1) {
 			fail(e1.getMessage());
 		}
 		
 		
 		
 		
 		
 	}
 	
 	@Test
 	public void testGetShapeFileHeaderSummaryUsingFileObject() {
 		List<String> result = null;
 		String fileToLoad = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
		+ "hru20VSR.SHP";
 		
 		try {
 			result = GeoToolsFileAnalysis.getShapeFileHeaderSummary(new File(fileToLoad));
 			assertNotNull(result);
 			fileToLoad = null;
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 	}
 	
 	@Test 
 	public void testGetShapefileSummary() {
 		List<String> result =  null;
 		String fileToLoad = this.tempDir 
 			+ this.seperator 
 			+ "Sample_Files" 
 			+ this.seperator
 			+ "Shapefiles" 
 			+ this.seperator
			+ "hru20VSR.SHP";
 		String nullString = null;
 		
 		GeoToolsFileAnalysis subject = new GeoToolsFileAnalysis();
 		try {
 			result = subject.getShapeFileSummary();
 			assertNull(result);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		try {
 			result = GeoToolsFileAnalysis.getShapeFileSummary("");
 			assertNull(result);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 		try {
 			result = GeoToolsFileAnalysis.getShapeFileSummary(nullString);
 			assertNull(result);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 		ShapefileReader shpFileReader;
 		try {
 			shpFileReader = GeoToolsFileAnalysis.getShapeFileReader(fileToLoad);
 			subject.setShpFileReader(shpFileReader);
 			result = subject.getShapeFileSummary();
 			assertNotNull(result);
 			assertFalse(result.isEmpty());
 		} catch (ShapefileException e) {
 			fail(e.getMessage());
 		} catch (MalformedURLException e) {
 			fail(e.getMessage());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 				
 		try {
 			result = GeoToolsFileAnalysis.getShapeFileSummary(fileToLoad);
 			assertNotNull(result);
 			assertFalse(result.isEmpty());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 		try {
 			result = GeoToolsFileAnalysis.getShapeFileSummary(new File(fileToLoad));
 			assertNotNull(result);
 			assertFalse(result.isEmpty());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 		File nullFile = null;
 		try {
 			result = GeoToolsFileAnalysis.getShapeFileSummary(nullFile);
 			assertNull(result);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 	}
 	
 	@Test
 	public void testGetGeotoolsSummary() {
 		String fileToLoad = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
		+ "hru20VSR.SHP";
 		ShapefileReader shpFileReader;
 		try {
 			shpFileReader = GeoToolsFileAnalysis.getShapeFileReader(fileToLoad);
 			assertNotNull(shpFileReader);
 			List<String> result = GeoToolsFileAnalysis.getShapeFileSummary(shpFileReader);
 			assertNotNull(result);
 			assertFalse(result.isEmpty());
 		} catch (ShapefileException e) {
 			fail(e.getMessage());
 		} catch (MalformedURLException e) {
 			fail(e.getMessage());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 	}
 	
 
 	
 	@Test
 	public void testReadInDBaseFile() {
 		String fileToLoad = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
		+ "hru20VSR.DBF";
 		String fakeFileToLoad = fileToLoad + "xxx";
 		String nonDbaseFileToLoad = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
		+ "hru20VSR.SHP";
 		
 		File file = FileHelper.loadFile(fileToLoad);
 		File fakeFile = FileHelper.loadFile(fakeFileToLoad);
 		File nonDbFile = FileHelper.loadFile(nonDbaseFileToLoad);
 		assertNotNull("File came back null. Cannot continue test.", file);
 		DbaseFileReader result;
 		try {
 			result = GeoToolsFileAnalysis.readInDBaseFile(file, false, Charset.defaultCharset());
 			assertNotNull("DbaseFileReader object came back null", result);
 			assertNotSame("", result.id());		
 			result.close();
 		} catch (IOException e1) {
 			fail(e1.getMessage());
 		}
 		
 		// Test FileNotFound
 		try {
 			result = GeoToolsFileAnalysis.readInDBaseFile(fakeFile, false, Charset.defaultCharset());
 		} catch (IOException e) {
 			assertNotNull(e.getMessage());
 		} 
 		
 		// Test IOException
 		try {
 			result = GeoToolsFileAnalysis.readInDBaseFile(nonDbFile, false, Charset.defaultCharset());
 			assertNull(result);
 		} catch (IOException e) {
 			assertNotNull(e.getMessage());
 		}
 		
 	}
 	
 	@Test
 	public void testGetDbaseFileHeader() {
 		String fileToLoad = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
		+ "hru20VSR.DBF";
 		GeoToolsFileAnalysis subject = new GeoToolsFileAnalysis();
 		DbaseFileHeader result = subject.getDBaseFileHeader();
 		assertNull(result);
 		File file = FileHelper.loadFile(fileToLoad);
 		DbaseFileReader reader;
 		try {
 			reader = GeoToolsFileAnalysis.readInDBaseFile(file, false, Charset.defaultCharset());
 			subject.setDbFileReader(reader);
 			result = subject.getDBaseFileHeader();
 			assertNotNull(result);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 	}
 	
 	@Test
 	public void testGetFileDataStore() {
 		String fileToLoad = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
		+ "hru20VSR.SHP";
 		
 		FileDataStore result = null;
 		try {
 			result = GeoToolsFileAnalysis.getFileDataStore(new File("does/not/exist"));
 			assertNull(result);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 		try {
 			result = GeoToolsFileAnalysis.getFileDataStore(null);
 			assertNull(result);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		
 		try {
 			result = GeoToolsFileAnalysis.getFileDataStore(new File(fileToLoad));
 			assertNotNull(result);
 		} catch (IOException e) {
 			fail(e.getMessage());;
 		}
 		
 	}
 }
