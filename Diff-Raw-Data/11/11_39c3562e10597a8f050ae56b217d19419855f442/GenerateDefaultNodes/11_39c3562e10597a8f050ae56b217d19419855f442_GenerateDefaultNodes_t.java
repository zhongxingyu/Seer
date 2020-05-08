 package org.tekkotsu.api;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 
 
 /*Author: Albert Toledo
  * 
  * Description: Creates java objects for default nodeclasses that will be provided
  * to the users.
  * Then generates an xml file which will be able to be read then
  * to generate those objects when needed.
  */
 
 public class GenerateDefaultNodes {
 
 	
 	public static void main(String[] args) throws IOException {
 		
 		//Create ArrayList for all default nodes.
 		ArrayList<NodeClass> defaultNodes = new ArrayList<NodeClass>();
 		
 		//Create components of SoundNode
 		Parameter musicFile = new Parameter("string", "\"barkmed.wav\"");
 		ConstructorCall soundNodeConstructor = new ConstructorCall("SoundNodeConstructor");
 		soundNodeConstructor.addParameter(musicFile);
 		System.out.println("Components of soundNode created.");
 		//Create SoundNode
 		NodeClass soundNode = new NodeClass("SoundNode", soundNodeConstructor);
 		//soundNode.setColor("#0AAF53");
 		soundNode.setDefinition("Plays a sound then signals completion.");
 		System.out.println("SoundNode object created.");
 		defaultNodes.add(soundNode);
 		
 		//Create components of StateNode
 		ConstructorCall stateNodeConstructor = new ConstructorCall("StateNodeConstructor");
 		System.out.println("Components of stateNode created.");
 		//Create StateNode
 		NodeClass stateNode = new NodeClass("StateNode", stateNodeConstructor);
 		//stateNode.setColor("#C0C0C0");
 		stateNode.setDefinition("No action. Parent for all other node classes.");
 		System.out.println("StateNode object created.");
 		defaultNodes.add(stateNode);
 		
 		//Create components of SpeechNode
 		Parameter speechName = new Parameter("string", "speechnode");
 		Parameter speechText = new Parameter("string", "Hello World");
 		ConstructorCall speechNodeConstructor = new ConstructorCall("SpeechNodeConstructor");
 		speechNodeConstructor.addParameter(speechName);
 		speechNodeConstructor.addParameter(speechText);
 		System.out.println("Components of speechNode created.");
 		//Create SpeechNode
 		NodeClass speechNode = new NodeClass("SpeechNode", speechNodeConstructor);
 		//speechNode.setColor("#FFC200");
 		speechNode.setDefinition("Speaks some text then signals completion.");
 		System.out.println("SpeechNode object created.");
 		defaultNodes.add(speechNode);
 		
 		
 		//Create components of LedNode
 		ConstructorCall ledNodeConstructor = new ConstructorCall("LedNodeConstructor");
 		System.out.println("Components of speechNode created.");
 		//Create LedNode
 		NodeClass ledNode = new NodeClass("LedNode", ledNodeConstructor);
 		//speechNode.setColor("#003DFF");
		ledNode.setDefinition("Flashes the robot's LED's.");
 		System.out.println("SpeechNode object created.");
 		defaultNodes.add(ledNode);
 		
 		
 		
 		//XSTREAM PART to write it to xml file called DefaultNodes.xml
 		XStream xstream = new XStream(new DomDriver());
 		try {
 			//Create file to write
 			FileOutputStream file = new FileOutputStream("DefaultNodes.xml");
 			System.out.println("DefaultNodes.xml created.");
 			
 			//Write xml of soundNode to file.
 			xstream.toXML(defaultNodes, file);
 			System.out.println("defaultNodes written to Nodes.xml");
 			
 			
 			//Close file
 			file.close();
 			System.out.println("DefaultNode.xml closed.");
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 
 	}
 
 }
