 package strategy;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.jdom2.Document;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 import model.SensorInfo;
 
 public class DeriveWriter implements IPsaWriter{

	
	private ArrayList<SensorInfo> sensors;
 	private Document doc;
 	private static boolean DEBUG = false;
 	
 	@Override
 	public void setup(ArrayList<SensorInfo> orderedSensors) {
		sensors = orderedSensors;
 		System.out.println();
 		System.out.println("5 strategy");
 		System.out.println(orderedSensors);
 		
 	}
 	
 	@Override
 	public void readTemplate() throws JDOMException, IOException{
 		SAXBuilder builder = new SAXBuilder();
 		doc = builder.build(new File("psa_templates/DeriveTemplate.xml"));
 
 		if (DEBUG) {
 			System.out.println("I Read The File");
 		}
 		
 	}
 
 	@Override
 	public void writeUpperSection() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void writeCalcArray(String userPoly) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void writeLowerSection() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void writeToNewPsaFile(String newDirName) throws FileNotFoundException, IOException {
 		
 		XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
 		xmlOutput.output(doc, new FileOutputStream(new File(
 				"output/" + newDirName + "/DeriveIMOS.psa")));
 		System.out.println("Wrote to file");
 		
 	}
 
 }
