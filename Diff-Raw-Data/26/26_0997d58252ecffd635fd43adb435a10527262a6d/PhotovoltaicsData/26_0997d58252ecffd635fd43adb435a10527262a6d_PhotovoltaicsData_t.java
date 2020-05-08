 package edu.hawaii.ihale.backend.restserver.resource.photovoltaics;
 
 import java.io.IOException;
 import java.util.List;
 import javax.xml.parsers.ParserConfigurationException;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.EmptyRepresentation;
 import org.restlet.representation.Representation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleState;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 import edu.hawaii.ihale.api.repository.TimestampIntegerPair;
 import edu.hawaii.ihale.backend.restserver.resource.SystemData;
 
 /**
  * Creates the Photovoltatics data resource.
  * 
  * @author Bret K. Ikehara
  */
 public class PhotovoltaicsData extends SystemData {
   
   /**
    * Creates the DomRepresentation of photovoltaics data.
    * 
    * @return Representation
    * @throws ParserConfigurationException Thrown when a problem occurs when creating the XML.
    * @throws IOException Thrown when a problem occurs creating the DomRepresentation object.
    */
   public static Representation toXml() throws IOException, ParserConfigurationException {
         
     TimestampIntegerPair energy = repository.getPhotovoltaicEnergy();
     // There is no state data for Photovolatic Power (omit this)
     // TimestampIntegerPair power = repository.getPhotovoltaicPower();
 
     Document doc = createDocument();
     
     Node rootNode = appendStateDataNode(doc, doc, IHaleSystem.PHOTOVOLTAIC, energy.getTimestamp());
     
     appendStateNode(doc, rootNode, IHaleState.ENERGY, energy.getValue());
     // There is no state data for Photovolatic Power (omit this)
     // appendStateNode(doc, rootNode, IHaleState.POWER, power.getValue());
     
     DomRepresentation result = new DomRepresentation();
     result.setDocument(doc);
     return result; 
   }
   
   /**
    * Returns the data as an XML Document instance.
    * 
    * @param timestamp Long
    * @return The data as XML.
    * 
    * @throws ParserConfigurationException Thrown when a problem occurs when creating the XML.
    * @throws IOException Thrown when a problem occurs creating the DomRepresentation object.
    */
   public static Representation toXmlSince(Long timestamp)
       throws ParserConfigurationException, IOException {
     
     if (timestamp == null) {
       return new EmptyRepresentation();
     }
 
     List<TimestampIntegerPair> energyList = repository.getPhotovoltaicEnergySince(timestamp);
     // There is no state data for Photovolatic Power (omit this)
     // List<TimestampIntegerPair> powerList = repository.getPhotovoltaicPowerSince(timestamp);
     
     Document doc = createDocument();
 
     // Creates the state-history root node.
     Node rootNode = appendStateHistoryNode(doc);
 
     Node stateDataNode;
 
     // Appends the state-data for each entry.
     int size = energyList.size();
     for (int i = 0; i < size; i++) {
       long time = energyList.get(i).getTimestamp();
       int energy = energyList.get(i).getValue();
       // There is no state data for Photovolatic Power (omit this)
       // int power = powerList.get(i).getValue();
       stateDataNode = 
           appendStateDataNode(doc, rootNode, IHaleSystem.PHOTOVOLTAIC, time);
       appendStateNode(doc, stateDataNode, IHaleState.ENERGY, energy);
       // There is no state data for Photovolatic Power (omit this)
       // appendStateNode(doc, stateDataNode, IHaleState.POWER, power);
     }
     
     // Convert Document to DomRepresentation.
     DomRepresentation result = new DomRepresentation();
     result.setDocument(doc);
 
     // Return the XML in DomRepresentation form.
     return result;
   }
 }
