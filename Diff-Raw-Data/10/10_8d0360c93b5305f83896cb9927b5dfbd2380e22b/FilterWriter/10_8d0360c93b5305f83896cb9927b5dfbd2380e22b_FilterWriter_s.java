 package strategy;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import model.SensorInfo;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 
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
 			if (sensor.getFullname().startsWith("Upoly")) {
 				calc.setAttribute("value", 
 						"" + sensor.getFullname() + userPoly);
 			}
 			else {
 				calc.setAttribute("value", 
 						"" + sensor.getFullname() );
 			}
 
 			//add fullname to calc
 			calc.addContent(fullName);
 			
 			//set up elements unqiue to 'Upoly 0, Upoly 0, ISUS V3 Nitrate'
 			if (sensor.getFullname().startsWith("Upoly")) {
 				
 				Element calcName = new Element("CalcName");
 				calcName.setAttribute("value", "Upoly 0, " + userPoly);
 				calc.addContent(calcName);
 				
 			}
 			//set up elements unqiue to 'Oxygen, SBE 43'
 			else if (sensor.getFullname().startsWith("Oxygen")) {
 				
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
 			else if (sensor.getFullname().startsWith("Descent")){
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
 			
 			
			
 			if (first){
 				first = false;
 			} else {
				arrayItem.setAttribute("value", "" + 1);
 			}
 			
			arrayItem.setAttribute("value", "" + 2);
 		}
 	}
 
 	@Override
 	public void writeToNewPsaFile() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
