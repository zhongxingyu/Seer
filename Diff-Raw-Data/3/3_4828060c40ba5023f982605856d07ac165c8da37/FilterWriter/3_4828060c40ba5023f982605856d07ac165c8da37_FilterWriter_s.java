 package strategy;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import model.SensorInfo;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 public class FilterWriter implements IPsaWriter{
 
 	ArrayList<SensorInfo> sensors;
 	Document doc;
 	
 	@Override
 	public void setup(ArrayList<SensorInfo> orderedSensors) {
 		sensors = orderedSensors;
 		System.out.println();
 		System.out.println("2 strategy");
 		System.out.println(orderedSensors);
 	}
 
 	@Override
 	public void readTemplate() 
 			throws JDOMException, IOException {
 		SAXBuilder builder =  new SAXBuilder();
 		doc = builder.build
 				(new File("./psa_templates/FilterTemplate.xml"));
 	}
 
 	@Override
 	public void writeUpperSection() {
 		// TODO Auto-generated method stub
 		
 		Element root = doc.getRootElement();
 		Element inputFileArray = root.getChild("InputFileArray");
 		
 		inputFileArray.setAttribute("size", "" + 0);
 		
 	}
 
 	@Override
 	public void writeCalcArray(String userPoly) {
 		//CalcArray Element from
 		
 		Element root = doc.getRootElement();
 		Element calcArray = root.getChild("CalcArray");
 		
 		//sets the size of the calcArray
 		calcArray.setAttribute("Size", "" + sensors.size());
 		
 		//counter
 		int counter = 0;
 		
 		//places sensors in calcArray
 		for (SensorInfo sensor : sensors){
 			
 			//set up CalcArrayItem
 			Element calcArrayItem = new Element("CalcArrayItem");
 			calcArrayItem.setAttribute("index", "" + counter++);
 			calcArrayItem.setAttribute("CalcID", 
 					"" + sensor.getCalcID());
 			
 			//adds calcArrayItem to calcArray
 			calcArray.addContent(calcArrayItem);
 			
 			//set up Calc
 			Element calc = new Element("Calc");
 			calc.setAttribute("UnitID", 
 					"" + sensor.getUnitID());
 			calc.setAttribute("Ordinal", 
 					"" + sensor.getOrdinal());
 			
 			//add calc to calcaArrayItem
 			calcArrayItem.addContent(calc);
 			
 			//set up FullName
 			Element fullName = new Element("FullName");
 			if (sensor.getFullName().startsWith("Upoly")) {
				fullName.setAttribute("value", 
						"" + sensor.getFullName() + ", " + userPoly);
 			}
 			else {
 				fullName.setAttribute("value", 
 						"" + sensor.getFullName() );
 			}
 
 			//add fullname to calc
 			calc.addContent(fullName);
 			
 			//set up elements unqiue to 'Upoly 0, Upoly 0, ISUS V3 Nitrate'
 			if (sensor.getFullName().startsWith("Upoly")) {
 				
 				Element calcName = new Element("CalcName");
 				calcName.setAttribute("value", "Upoly 0, " + userPoly);
 				calc.addContent(calcName);
 				
 			}
 			//set up elements unqiue to 'Oxygen, SBE 43'
 			else if (sensor.getFullName().startsWith("Oxygen")) {
 				
 				//set up windowsize
 				Element windowSize = new Element("WindowSize");
 				windowSize.setAttribute("value", "2.000000");
 
 				//set up applyHysteresisCorrection
 				Element applyHysteresisCorrection = 
 						new Element("ApplyHysteresisCorrection");
 				applyHysteresisCorrection.setAttribute("value", "1");
 
 				//set up applyTauCorrection
 				Element applyTauCorrection = 
 						new Element("ApplyTauCorrection");
 				applyTauCorrection.setAttribute("value", "1");
 
 				calc.addContent(windowSize);
 				calc.addContent(applyHysteresisCorrection);
 				calc.addContent(applyTauCorrection);
 			}
 			//set up elements unqiue to Descent Rate [m/s]
 			else if (sensor.getFullName().startsWith("Descent")){
 				Element windowSize = new Element("WindowSize");
 				windowSize.setAttribute("value", "2.000000");
 				calc.addContent(windowSize);
 			}
 		}
 	}
 
 	@Override
 	public void writeLowerSection() {
 		
 		//FilterTypeArray Element from
 		Element root = doc.getRootElement();
 		Element filterTypeArray = root.getChild("FilterTypeArray");
 		
 		boolean first = true;
 		int counter = 0;
 		for (SensorInfo sensor: sensors){
 			
 			Element arrayItem = new Element("ArrayItem");
 			
 			arrayItem.setAttribute("index", "" + counter++);
 			
 			
 			int value;
 			if (first){
 				value = 2;
 				first = false;
 			} else {
 				value = 1;
 			}
 			
 			arrayItem.setAttribute("value", "" + value);
 			filterTypeArray.addContent(arrayItem);
 		}
 	}
 
 	@Override
 	public void writeToNewPsaFile() throws FileNotFoundException, IOException {
 		// TODO Auto-generated method stub 
 
 		XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
 		xmlOutput.output(doc, new FileOutputStream(new File(
 				"output/FilterIMOS.psa")));
 		System.out.println("Wrote to file");
 	}
 
 }
