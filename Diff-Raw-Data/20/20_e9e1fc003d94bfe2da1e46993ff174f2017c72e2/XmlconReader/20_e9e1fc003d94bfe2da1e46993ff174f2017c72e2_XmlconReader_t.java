 package strategy;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import model.SensorInfo;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 
 import util.ConnectDB;
 import util.HexReader;
 import util.RunSeabird;
 
 public class XmlconReader {
 
 	private static LinkedHashMap<Integer, SensorInfo> sensorsMap;
 	private static ArrayList<SensorInfo> orderedSensors;
 
 	private Connection con;
 	private Statement statement;
 	private static String userPoly;
 	private static final String DIRECTORY = "\\\\pearl\\temp\\adc-jcu2012";
 
 	private IPsaWriter writerType;
 
 	// static {
 	// sensorsMap = new LinkedHashMap<>();
 	// orderedSensors = new ArrayList<>();
 	// }
 
 	public XmlconReader(IPsaWriter psaWriter) {
 		this.writerType = psaWriter;
 		sensorsMap = new LinkedHashMap<>();
 		orderedSensors = new ArrayList<>();
 	}
 
 	public static LinkedHashMap<Integer, SensorInfo> getSensorsMap() {
 		return sensorsMap;
 	}
 
 	public IPsaWriter getWriterType() {
 		return writerType;
 	}
 
 	/**
 	 * takes @param file which should be a xmlcon file and reads it, gets the
 	 * sensorArray Element, to pass the children to sensorsInXmlcon which is a
 	 * list of the sensors in the xmlcon.
 	 * 
 	 * After this is done it will loop through the children and will try to find
 	 * the 'UserPolynomialSensor' and collects it's name value so it can be used
 	 * in parsing the psa xml files.
 	 * 
 	 * after this is all done it will return the @return sensorsInXmlcon
 	 * 
 	 * @return
 	 * @throws IOException
 	 * @throws JDOMException
 	 */
 	public List<Element> readXmlcon(File file) throws IOException,
 			JDOMException {
 		SAXBuilder builder = new SAXBuilder();
 		Document readDoc = builder.build(file);
 		Element rootEle = readDoc.getRootElement();
 		Element sensorArrayEle = rootEle.getChild("Instrument").getChild(
 				"SensorArray");
 		List<Element> sensorsInXmlcon = sensorArrayEle.getChildren();
 
 		for (Element e : sensorsInXmlcon) {
 			Element child = e.getChildren().get(0);
 			if (child.getName().equals("UserPolynomialSensor")) {
 				Element sensorName = child.getChild("SensorName");
 				userPoly = sensorName.getValue();
 			}
 		}
 
 		return sensorsInXmlcon;
 	}
 
 	// private String getFileName(String file) {
 	// File xmlFile = new File(file);
 	// String fileName = xmlFile.getName();
 	// return null;
 	// }
 
 	/**
 	 * connects to the database, and statement is initalized with the connection
 	 * to the database. The database is opened in this method as opposed to the
 	 * 'getAllAttributes' method as it needs to be closed here.
 	 * 
 	 * getAllAttributes returns a query result set from the query 'SELECT * FROM
 	 * sensor_info'.
 	 * 
 	 * The while loop sorts through each row of the results and inserts them as
 	 * new sensorInfo items in the sensorrsMa, with the the index of the
 	 * sensorID.
 	 * 
 	 * with any exception is caused they are caught, and a error message is
 	 * printed to console. Program may not function in the event of a exception
 	 * raising.
 	 */
 	public void populateSensorsMap() {
 		ConnectDB db = new ConnectDB();
 		con = db.getDdConnection();
 		try {
 			statement = con.createStatement();
 			ResultSet results = getAllAttributes();
 			while (results.next()) {
 				int sensorID = results.getInt("sensor_ID");
 				int calcID = results.getInt("calc_ID");
 				int unitID = results.getInt("unit_ID");
 				int ordinal = results.getInt("ordinal");
 				String name = results.getString("full_name");
 				sensorsMap.put(sensorID, new SensorInfo(unitID, sensorID,
 						calcID, ordinal, name));
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				con.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/**
 	 * is intended to return a result set containing all the information on the
 	 * sensor from the query 'SELECT * FROM sensor_info'. in the event of an
 	 * exception rising it will return null.
 	 * 
 	 * @return rs
 	 */
 	public ResultSet getAllAttributes() {
 		try {
 			String sql = "SELECT * FROM sensor_info";
 			ResultSet rs = statement.executeQuery(sql);
 			return rs;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * adds Pressure, Temperature, Conductivity, Fluorescence, OBS, & Upoly to
 	 * the orderedSensors ArrayList.
 	 * 
 	 * At the moment the current method is not the best method and could be
 	 * potentially optimized
 	 * 
 	 * @param sensorsInXmlcon
 	 */
 	public void sortSensors(List<Element> sensorsInXmlcon) {
 		for (Element sensor : sensorsInXmlcon) {
 			insertSensor("Pressure", sensor);
 		}
 		for (Element sensor : sensorsInXmlcon) {
 			insertSensor("Temperature", sensor);
 		}
 		for (Element sensor : sensorsInXmlcon) {
 			insertSensor("Conductivity", sensor);
 		}
 		for (Element sensor : sensorsInXmlcon) {
 			insertSensor("Fluorescence", sensor);
 		}
 		for (Element sensor : sensorsInXmlcon) {
 			insertSensor("OBS", sensor);
 		}
 		for (Element sensor : sensorsInXmlcon) {
 			insertSensor("Upoly", sensor);
 		}
 		// Other
 
 		SensorInfo freq = sensorsMap.get(-5);
 		SensorInfo desRate = sensorsMap.get(-3);
 		SensorInfo density = sensorsMap.get(-4);
 
 		orderedSensors.add(freq);
 		orderedSensors.add(desRate);
 		orderedSensors.add(density);
 
 		for (Element sensor : sensorsInXmlcon) {
 			insertSensor("Oxygen", sensor);
 		}
 
 	}
 
 	/**
 	 * inserts sensor into orderedSensors if it's fullname starts with
 	 * sensorName.
 	 * 
 	 * @param sensorName
 	 * @param sensor
 	 */
 	private void insertSensor(String sensorName, Element sensor) {
 		SensorInfo info = sensorsMap.get(Integer.parseInt(sensor
 				.getAttributeValue("SensorID")));
 		if (info != null) {
 			if (info.getFullName().startsWith(sensorName)) {
 				orderedSensors.add(info);
 			}
 		}
 	}
 
 	public static void main(String args[]) {
 		String inputLocation = "";
 		String inputHexLocation = "";
 		String outputFileLocation = "";
 
 		try {
 			inputLocation = args[0];
 		} catch (Exception e) {
 			System.out.println("No con file location: going to default");
 			inputLocation = "\\\\pearl\\temp\\adc-jcu2012\\xmlcons";
 		}
 		try {
 			inputHexLocation = args[1];
 		} catch (Exception e) {
 			System.out.println("No hex file location: going to default");
 			inputHexLocation = "\\\\pearl\\temp\\adc-jcu2012\\hex";
 		}
 		try {
 			outputFileLocation = args[2];
 		} catch (Exception e) {
 			System.out.println("No output file location: going to default");
 			outputFileLocation = "\\\\pearl\\temp\\adc-jcu2012\\config";
 		}
 
 		// Tries to make the output file location if it does not exist
 		if (!new File(outputFileLocation).isDirectory()) {
 			new File(outputFileLocation).mkdir();
 		}
 
 		if (new File(inputLocation).isDirectory()
 				&& new File(inputHexLocation).isDirectory()
 				&& new File(outputFileLocation).isDirectory()) {
 
 			ArrayList<XmlconReader> writers = new ArrayList<>();
 
 			// create 6 XMLcon Readers with each with a different psa Writer
 
 			XmlconReader datCnvWriter = new XmlconReader(new DatCnvWriter());
 			XmlconReader alignWriter = new XmlconReader(new AlignWriter());
 			XmlconReader filterWriter = new XmlconReader(new FilterWriter());
 			XmlconReader binAvgWriter = new XmlconReader(new BinAvgWriter());
 			XmlconReader deriveWriter = new XmlconReader(new DeriveWriter());
 			XmlconReader loopEditWriter = new XmlconReader(new LoopEditWriter());
 
 			// Comment in when you want sea bird to run
 			RunSeabird runSeabird = new RunSeabird(inputLocation, ".xmlcon",
 					"xmlProcessSeabirds.bat");
 
 			// fills the sensorsMap of all the XmlconReader
 			datCnvWriter.populateSensorsMap();
 			// dir is the folder //pearl/temp/adc-jcu2012/xmlcon
 			ArrayList<File> files = new ArrayList<>();
 
 			files = findAllFiles(inputLocation, files);
 
 
 			// loops for every file in dir
 			for (File xml : files) {
 
 				/*
 				 * if the file ends with '.xmlcon' then proceed to parse the 6
 				 * psa files from it, otherwise continue to next file.
 				 */
 
 				String xmlName = xml.getName().replaceFirst("[.][^.]+$", "");
 				if (xml.getName().toLowerCase().endsWith(".xmlcon")) {
 					try {
 
 						/*
 						 * sets sensorsInXmlcon to the return of readXmlcon(xml)
 						 * and sets userpoly
 						 */
 
 						List<Element> sensorsInXmlcon = datCnvWriter
 								.readXmlcon(xml);
 
 						// sort sensors
 
 						datCnvWriter.sortSensors(sensorsInXmlcon);
 						// String FileName =
 						// getFileName("xmlcons/NRS2_6390_01102011_O2andNTU.xmlcon");
 
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 
 					/*
 					 * add the different psa-writters to the writters ArrayList
 					 * defined at the start of the main.
 					 */
 
 					writers.add(datCnvWriter);
 					writers.add(alignWriter);
 					writers.add(filterWriter);
 					writers.add(binAvgWriter);
 					writers.add(deriveWriter);
 					writers.add(loopEditWriter);
 
 					/*
 					 * outputDirName as it suggests is the directory in which
 					 * the everything will go.
 					 * 
 					 * '[.][^.]+$' is what's required for the replaceFirst
 					 * method to recongize '.' in a String
 					 * 
 					 * xmlLocation is defined by outputDirName + the xmlcons
 					 * file name
 					 */
 
 					String outputDirName = outputFileLocation + "\\"
 							+ xml.getName().replaceFirst("[.][^.]+$", "");
 
 					createDirectory(outputDirName);
 
 					// Where the batch, final and raw files are located
 					String workingDirectory = outputDirName + "\\data\\";
 
 					// Where the xmlcon is
 					String xmlLocation = outputDirName + "\\" + xml.getName();
 
 					/*
 					 * loop through the writers with the strategy pattern.
 					 * 
 					 * for each loop the following methods will be called 1)
 					 * setup() prepares writer for parsing 2) readTemplate
 					 * imports the appropiate templates 3) writeUppperSection
 					 * writes the section about the CalcArray. here things like
 					 * output/input directory are stored, and various other
 					 * attributes 4) writeCalcArray writes the CalcArray, here
 					 * the instrument sensor attributes and values are stored
 					 * and defined for the psa file 5) writeLowerSection writes
 					 * the section below the CalcArray 6) writeToNewPsaFile
 					 * writes psafile to disk/server
 					 * 
 					 * steps 3 to 5 can be radically different for some of the
 					 * writers and some don't even need to use those methods.
 					 */
 
 					for (XmlconReader writer : writers) {
 						try {
 							writer.getWriterType().setup(orderedSensors);
 							writer.getWriterType().readTemplate(
 									DIRECTORY + "\\utilities\\psa_templates");
 							writer.getWriterType().writeUpperSection(
 									workingDirectory, xmlLocation);
 							writer.getWriterType().writeCalcArray(userPoly);
 							writer.getWriterType().writeLowerSection();
 							writer.getWriterType().writeToNewPsaFile(
 									outputDirName);
 						} catch (Exception e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					orderedSensors.clear();
 
 					// Creates the bat files for the seabird processing
 					createRunBat(outputDirName);
 					createProcessSbbat(outputDirName, xml.getName());
 
 					// Moves the file
 					moveCon(xml, outputDirName);
 
 					// Need there to be a file IDIOT
 					runSeabird.setBatch(outputDirName, xmlName);
 
 				} else if (xml.getName().toLowerCase().endsWith(".con")) {
 					String outputDirName = outputFileLocation + "\\"
 							+ xml.getName().replaceFirst("[.][^.]+$", "");
 
 					createDirectory(outputDirName);
 
 					// Creates the bat files for the seabird processing
 					createRunBat(outputDirName);
 					createProcessSbbat(outputDirName, xml.getName());
 
 					// Moves the file
 					moveCon(xml, outputDirName);
 				} // end of if
 			}
 
 			// Adding comments is fun
 			runSeabird.writeBatch();
 
//			if (!files.isEmpty()) {
 				findHex(new File(inputHexLocation), outputFileLocation);
//				System.out.println("n files");
//			}
 
 		} else {
 			if (!new File(inputLocation).isDirectory()) {
 				System.out.println("Input file location invalid");
 			}
 			if (!new File(inputHexLocation).isDirectory()) {
 				System.out.println("Input hex file location invalid");
 			}
 			if (!new File(outputFileLocation).isDirectory()) {
 				System.out.println("Output file location invalid");
 			}
 		}
 	}
 
 	private static ArrayList<File> findAllFiles(String inputLocation,
 			ArrayList<File> files) {
 		File dir = new File(inputLocation);
 
 		// loops for every file in dir
 		for (File xml : dir.listFiles()) {
 			if (xml.isDirectory()) {
 				files.addAll(findAllFiles(xml.getAbsolutePath(),
 						new ArrayList<File>()));
 			} else if (xml.getName().toLowerCase().endsWith(".con")
 					|| xml.getName().toLowerCase().endsWith(".xmlcon")) {
 				files.add(xml);
 			}
 		}
 		return files;
 	}
 
 	/**
 	 * lacates
 	 * 
 	 * @param hexDir
 	 */
 	private static void findHex(File hexDir, String outputFileLocation) {
 		for (File hex : hexDir.listFiles()) {
 			if (hex.isDirectory()) {
 				findHex(hex, outputFileLocation);
 			} else if (hex.getName().endsWith(".hex")) {
 				HexReader reader = new HexReader(hex, outputFileLocation);
 				reader.run();
 			}
 		}
 	}
 
 	/**
 	 * with createDirectory it creates a directory with @param outputDirName
 	 * 
 	 * after that directory is made, along with sub directories such as -
 	 * '/data' (also know as workingDirectory) - '/data/raw' - '/data/batch' -
 	 * '/data/final'
 	 */
 	private static void createDirectory(String outputDirName) {
 		new File(outputDirName).mkdir();
 
 		// Makes the data stuff
 		new File(outputDirName + "/data").mkdir();
 		new File(outputDirName + "/data/raw").mkdir();
 		new File(outputDirName + "/data/batch").mkdir();
 		new File(outputDirName + "/data/final").mkdir();
 
 	}
 
 	private static void moveCon(File xml, String outputDirName) {
 
 		InputStream inStream = null;
 		OutputStream outStream = null;
 
 		try {
 			inStream = new FileInputStream(xml);
 			outStream = new FileOutputStream(new File(outputDirName + "/"
 					+ xml.getName()));
 
 			byte[] buffer = new byte[1024];
 			int length;
 
 			// copy the file content in bytes
 			while ((length = inStream.read(buffer)) > 0) {
 				outStream.write(buffer, 0, length);
 			}
 
 			inStream.close();
 			outStream.close();
 
 			// delete the original file
 			xml.delete();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Creates the process batch file for each directory
 	 * 
 	 * @param outputDirName
 	 * @param name
 	 */
 	private static void createProcessSbbat(String outputDirName, String name) {
 		File file = new File(outputDirName + "\\process.sbbat");
 		File location = new File(outputDirName);
 		PrintWriter fout = null;
 		try {
 			fout = new PrintWriter(file.getAbsolutePath());
 			// Write the stuff
 
 			// /datcnv
 			fout.println("@Use: sbebatch AIMS-IMOS_CTD_batch.bat");
 			fout.println("datcnv /i\"" + location.getAbsolutePath()
 					+ "\\data\\raw\\%1.hex\" /c\"" + location.getAbsolutePath()
 					+ "\\" + name + "\" /p\"" + location.getAbsolutePath()
 					+ "\\DatCnvIMOS.psa" + "\" /o\""
 					+ location.getAbsolutePath() + "\\data\\batch\" /aC");
 
 			// filter
 			fout.println("filter /i\"" + location.getAbsolutePath()
 					+ "\\data\\batch\\%1C.cnv\" /p\""
 					+ location.getAbsolutePath() + "\\FilterIMOS.psa\" /o\""
 					+ location.getAbsolutePath() + "\\data\\batch\" /aF");
 
 			// alignctd
 			fout.println("alignctd /i\"" + location.getAbsolutePath()
 					+ "\\data\\batch\\%1CF.cnv\" /p\""
 					+ location.getAbsolutePath() + "\\AlignIMOS.psa\" /o\""
 					+ location.getAbsolutePath() + "\\data\\batch\" /aA");
 
 			// loopedit
 			fout.println("loopedit /i\"" + location.getAbsolutePath()
 					+ "\\data\\batch\\%1CFA.cnv\" /p\""
 					+ location.getAbsolutePath() + "\\LoopEditIMOS.psa\" /o\""
 					+ location.getAbsolutePath() + "\\data\\batch\" /aL");
 
 			// derive
 			fout.println("derive /i\"" + location.getAbsolutePath()
 					+ "\\data\\batch\\%1CFAL.cnv\" /c\""
 					+ location.getAbsolutePath() + "\\" + name + "\" /p\""
 					+ location.getAbsolutePath() + "\\DeriveIMOS.psa"
 					+ "\" /o\"" + location.getAbsolutePath()
 					+ "\\data\\batch\" /aD");
 
 			// binavg
 			fout.println("binavg /i\"" + location.getAbsolutePath()
 					+ "\\data\\batch\\%1CFALD.cnv\" /p\""
 					+ location.getAbsolutePath() + "\\BinAvgIMOS.psa\" /o\""
 					+ location.getAbsolutePath() + "\\data\\final\" /aB");
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} finally {
 			fout.close();
 		}
 
 	}
 
 	/**
 	 * creates run.bat at @param outputDirName
 	 */
 	private static void createRunBat(String outputDirName) {
 		File file = new File(outputDirName + "\\run.bat");
 
 		// Creates a new Print Writer
 		PrintWriter fout = null;
 		try {
 			fout = new PrintWriter(file.getAbsolutePath());
 			fout.println("sbebatch "
 					+ new File(outputDirName).getAbsolutePath()
 					+ "\\process.sbbat *\nEXIT [/B] [exitCode] ");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} finally {
 			fout.close();
 		}
 	}
 }
