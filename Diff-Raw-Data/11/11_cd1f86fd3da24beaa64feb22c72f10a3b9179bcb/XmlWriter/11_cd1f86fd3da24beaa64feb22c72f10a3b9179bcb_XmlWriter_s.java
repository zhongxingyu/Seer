 package de.hohenheim.controller;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 import de.hohenheim.controller.main.Main;
 import de.hohenheim.modell.timetable.Timetable;
 import de.hohenheim.modell.train.TrainData;
 
 /**
  * This class provides method/s to save trains and time tables (train schedules)
  * using XML-files.
  * 
  * @author Matze
  * 
  */
 public class XmlWriter {
 
 	/**
 	 * Static method to write XML-file that provides the trains and time tables
 	 * (train schedules) we want to save. Is static so you don't have to
 	 * instantiate the XmlWriter as object to use this method.
 	 * 
 	 * (Note: JDOM = Document Object Model)
 	 * 
 	 * @param fileName
 	 *            Name of the saved XML-File as String, importent to distinguish
 	 *            the different trains and time tables.
 	 */
 	public static void writeToXML(String fileName) {
 
 	}
 
 	/**
 	 * Method to save single selected Trains in one XML File
 	 * 
 	 * @param fileName
 	 *            Name of SaveFile user gives
 	 * @param tD
 	 *            The user selected train which will be saved in the XML File
 	 */
 	public static void saveSingleTrain(String fileName, TrainData tD) {
 
 		Element rootSingleTrain = new Element("Train");
 
 		Element id = new Element("ID");
 		rootSingleTrain.addContent(id);
 		id.setText(String.valueOf(tD.getID()));
 
 		Element typeOfTrain = new Element("TypeOfTrain");
 		rootSingleTrain.addContent(typeOfTrain);
 		typeOfTrain.setText(String.valueOf(tD.getTypOfTrain()));
 
 		Element priority = new Element("Priority");
 		rootSingleTrain.addContent(priority);
 		priority.setText(String.valueOf(tD.getPriority()));
 
 		Element speed = new Element("Speed");
 		rootSingleTrain.addContent(speed);
 		speed.setText(String.valueOf(tD.getSpeed()));
 
 		Element ladung = new Element("Ladung");
 		rootSingleTrain.addContent(ladung);
 		ladung.setText(String.valueOf(tD.getLadung()));
 
 		// Document with attached root element
 		Document doc = new Document(rootSingleTrain);
 
 		// FileOutPutStream with fileName given from Method
 		try {
 			FileOutputStream out = new FileOutputStream(fileName);
 			XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
 			serializer.output(doc, out);
 			out.flush();
 			out.close();
 		} catch (IOException e) {
 			System.err.println(e);
 			e.fillInStackTrace();
 
 	    } catch (NullPointerException e){
 	    	
 	    }
 		
 
 		
 
 
 	}
 
 	public static void saveSingleTimeTable(String fileName, Timetable tD) {
 
 		Element rootTimeTable = new Element("Train");
 
 		Element iD = new Element("ID");
 		rootTimeTable.addContent(iD);
 		iD.setText(String.valueOf(tD.getId()));
 
 		Element name = new Element("Name");
 		rootTimeTable.addContent(name);
 		name.setText(String.valueOf(tD.getName()));
 
		Element drivingdays = new Element("DrivingDays");
		rootTimeTable.addContent(drivingdays);
 		for (int i = 0; i < tD.getDrivingdays().size(); i++) {
 			drivingdays.setText(String.valueOf(tD.getDrivingdays().get(i)));
 		}
 
 		Element rootStations = new Element("Stations");
 		rootTimeTable.addContent(rootStations);
 
 		Element startstation = new Element("StartStation");
 		rootStations.addContent(startstation);
 		startstation.setText(String.valueOf(tD.getStartstation()));
 
		Element middlestations = new Element("MiddleStations");
		rootStations.addContent(middlestations);
 		for (int i = 0; i < tD.getMiddlestations().size(); i++) {
 			middlestations.setText(String
 					.valueOf(tD.getMiddlestations().get(i)));
 		}
 
 		Element endstation = new Element("EndStation");
 		rootStations.addContent(endstation);
 		endstation.setText(String.valueOf(tD.getEndstation()));
 
 		Element rootTime = new Element("Time");
 		rootTimeTable.addContent(rootTime);
 
 		Element startHouer = new Element("StartHouer");
 		rootTime.addContent(startHouer);
 		startHouer.setText(String.valueOf(tD.getStartHouer()));
 
 		Element startMinutes = new Element("StarMinutes");
 		rootTime.addContent(startMinutes);
 		startMinutes.setText(String.valueOf(tD.getStartMinutes()));
 
 		// Document with attached root element
 		Document docTimeTable = new Document(rootTimeTable);
 
 		// FileOutPutStream with fileName given from Method
 		try {
 			FileOutputStream out = new FileOutputStream(fileName);
 			XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
 			serializer.output(docTimeTable, out);
 			out.flush();
 			out.close();
 		} catch (IOException e) {
 			System.err.println(e);
 			e.fillInStackTrace();
 		}
 
 	}
 
 }
