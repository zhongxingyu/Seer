 package persistence;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import petrinet.INode;
 import petrinet.Petrinet;
 import transformation.Rule;
 import engine.attribute.NodeLayoutAttribute;
 import engine.ihandler.IPetrinetPersistence;
 import engine.ihandler.IRulePersistence;
 import gui.PopUp;
 
 /**
  * The Persistence class is the interface between persistence component and
  * engine component. It has high-level methods for saving and loading petrinets
  * and rules
  * 
  */
 public class Persistence {
 	static {
 		try {
 			context = JAXBContext.newInstance(persistence.Pnml.class,
 					Arc.class, Converter.class, Graphics.class,
 					InitialMarking.class, Name.class, Net.class, Page.class,
 					Place.class, PlaceName.class, Position.class,
 					Transition.class, TransitionLabel.class,
 					TransitionName.class, TransitionRenew.class, Color.class);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	static JAXBContext context;
 
 	/**
 	 * Saves a petrinet into a file
 	 * 
 	 * @param pathAndFilename
 	 *            String that identifies the file
 	 * @param petrinet
 	 *            logical petrinet to be safed
 	 * @param nodeMap
 	 *            layout information: color and position of nodes
 	 * @param nodeSize
 	 *            size of nodes. This is depending on "zoom" and is important
 	 *            when loading the file
 	 * @return <code>true</code> when successful, <code>false</code> when any
 	 *         exception was thrown
 	 */
 	public static boolean savePetrinet(String pathAndFilename,
 			Petrinet petrinet, Map<INode, NodeLayoutAttribute> nodeMap,
 			double nodeSize) {
 
 		Map<String, String[]> coordinates = new HashMap<String, String[]>();
 
 		for (Entry<INode, NodeLayoutAttribute> e : nodeMap.entrySet()) {
 			String[] coords = {
 					String.valueOf(e.getValue().getCoordinate().getX()),
 					String.valueOf(e.getValue().getCoordinate().getY()),
 					String.valueOf(e.getValue().getColor().getRed()),
 					String.valueOf(e.getValue().getColor().getGreen()),
 					String.valueOf(e.getValue().getColor().getBlue()) };
 			coordinates.put(String.valueOf(e.getKey().getId()), coords);
 		}
 
 		Pnml pnml = Converter.convertPetrinetToPnml(petrinet, coordinates,
 				nodeSize);
 		File file = new File(pathAndFilename);
 
 		try {
 
 			Marshaller m = context.createMarshaller();
 
 			m.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
 			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 
 			FileWriter fw = new FileWriter(file);
 			m.marshal(pnml, fw);
 			fw.flush();
 			fw.close();
 
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * Loads a petrinet from a file
 	 * 
 	 * @param pathAndFilename
 	 *            String that identifies the file
 	 * @param handler
 	 *            The engine handler to create and modify the petrinet
 	 * @return the id of the created petrinet. <code>-1</code> if any exception
 	 *         was thrown
 	 */
 	public static int loadPetrinet(String pathAndFilename,
 			IPetrinetPersistence handler) {
 		Pnml pnml = new Pnml();
 		try {
 			Unmarshaller m = context.createUnmarshaller();
 
 			m.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
 
 			pnml = (Pnml) m.unmarshal(new File(pathAndFilename));
 
 			return Converter.convertPnmlToPetrinet(pnml, handler);
 		} catch (JAXBException e) {
 			e.printStackTrace();
 			PopUp.popError(e);
 		}
 
 		return -1;
 	}
 
 	/**
 	 * Saves a {@link Rule rule} into a file
 	 * 
 	 * @param pathAndFilename
 	 *            String that identifies the file
 	 * @param rule
 	 *            the logical rule to be saved
 	 * @param nodeMapL
 	 *            layout information for nodes in L
 	 * @param nodeMapK
 	 *            layout information for nodes in K
 	 * @param nodeMapR
 	 *            layout information for nodes in R
 	 * @param nodeSize
 	 *            Size of nodes. This is depending on "zoom" and is important
 	 *            when loading the file
 	 * @return <code>true</code> when successful, <code>false</code> when any
 	 *         exception was thrown
 	 */
 	public static boolean saveRule(String pathAndFilename, Rule rule,
 			Map<INode, NodeLayoutAttribute> nodeMapL,
 			Map<INode, NodeLayoutAttribute> nodeMapK,
 			Map<INode, NodeLayoutAttribute> nodeMapR, double nodeSize) {
 		boolean success = false;
 
 		try {
 
 			Map<INode, NodeLayoutAttribute> nodeMapMerged = new HashMap<INode, NodeLayoutAttribute>();
 			nodeMapMerged.putAll(nodeMapL);
 			nodeMapMerged.putAll(nodeMapK);
 			nodeMapMerged.putAll(nodeMapR);
 
 			Marshaller m = context.createMarshaller();
 			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 			Pnml pnml = Converter.convertRuleToPnml(rule, nodeMapMerged,
 					nodeSize);
 
 			m.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
 
 			FileWriter fw = new FileWriter(pathAndFilename);
 			m.marshal(pnml, fw);
 			fw.flush();
 			fw.close();
 			success = true;
 		} catch (IOException e) {
 			PopUp.popError(e);
 		} catch (JAXBException e) {
 			PopUp.popError(e);
 		}
 
 		return success;
 	}
 
 	/**
 	 * Loads a {@linkplain Rule} from a file
 	 * 
 	 * @param pathAndFilename
 	 *            String that identifies the file
 	 * @param handler
 	 *            The engine handler to create and modify the rule
 	 * @return the id of the created rule. <code>-1</code> if any exception was
 	 *         thrown
 	 */
 	public static int loadRule(String pathAndFilename, IRulePersistence handler) {
 		Pnml pnml;
 		try {
 			Unmarshaller m = context.createUnmarshaller();
 
 			m.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
 
 			pnml = (Pnml) m.unmarshal(new File(pathAndFilename));
 
 			return Converter.convertPnmlToRule(pnml, handler);
		} catch (JAXBException e) {
 			e.printStackTrace();
			PopUp.popError(e);
 		}
 
 		return 0;
 	}
 
 }
