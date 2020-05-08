 package strategy;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import model.SensorInfo;
 import util.ConnectDB;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 
 public class Writer {
 
 	private static LinkedHashMap<Integer, SensorInfo> sensorsMap;
 	private static ArrayList<SensorInfo> orderedSensors;
 	
 	private static Connection con;
 	private static Statement statement;
 	private static String userPoly;
 	
 	
 	private IPsaWriter writerType;
 
 	
 	public Writer(IPsaWriter psaWriter) {
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
 
 
 	public List<Element> readXmlcon(String file) throws IOException, JDOMException {
 		SAXBuilder builder =  new SAXBuilder();
 		Document readDoc =  builder.build(new File(file));
 		
 		Element rootEle = readDoc.getRootElement();
 		Element sensorArrayEle = rootEle.getChild("Instrument").getChild("SensorArray");
 		List<Element> sensorsInXmlcon = sensorArrayEle.getChildren();
 		for (Element e : sensorsInXmlcon){
 			Element child = e.getChildren().get(0);
 			if(child.getName().equals("UserPolynomialSensor")){
 				Element sensorName = child.getChild("SensorName");
 				userPoly = sensorName.getValue();
 			}
 			System.out.println(e.getAttributeValue("SensorID"));
 		}
 		System.out.println();
 		return sensorsInXmlcon;
 	}
 
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
 				getSensorsMap().put(sensorID, new SensorInfo(unitID, sensorID, calcID, ordinal, name));
 			}
 			con.close();
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
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
 			insertSensor("Upoly", sensor);
 		}
 		
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
 		ArrayList<Writer> writers = new ArrayList<>();
 
 		Writer datCnvWriter = new Writer(new DatCnvWriter());
 		Writer alignWriter = new Writer(new AlignWriter());
 		Writer filterWriter = new Writer(new FilterWriter());
 		Writer binAvgWriter = new Writer(new BinAvgWriter());
 		Writer deriveWriter = new Writer(new DeriveWriter());
 		Writer loopEditWriter = new Writer(new LoopEditWriter());
 		// TODO other 3 writers
 
 		datCnvWriter.populateSensorsMap();
 		try {
 			List<Element> sensorsInXmlcon = datCnvWriter.readXmlcon("xmlcons/NRS1_6180_20120917.xmlcon");
 			datCnvWriter.sortSensors(sensorsInXmlcon);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		writers.add(datCnvWriter);
 		writers.add(alignWriter);
 		writers.add(filterWriter);
 		writers.add(binAvgWriter);
 		writers.add(deriveWriter);
 		writers.add(loopEditWriter);
 
 		for (Writer writer : writers) {
 			try {
 				writer.getWriterType().setup(orderedSensors);
 				writer.getWriterType().readTemplate();
 				writer.getWriterType().writeUpperSection();
 				writer.getWriterType().writeCalcArray(userPoly);
 				writer.getWriterType().writeLowerSection();
 				writer.getWriterType().writeToNewPsaFile();
 			} catch (Exception e){
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 //			for (SensorInfo i : orderedSensors) {
 //				System.out.println(i.getFullname());
			
			
 //			}
 		}
 
 	}
 }
