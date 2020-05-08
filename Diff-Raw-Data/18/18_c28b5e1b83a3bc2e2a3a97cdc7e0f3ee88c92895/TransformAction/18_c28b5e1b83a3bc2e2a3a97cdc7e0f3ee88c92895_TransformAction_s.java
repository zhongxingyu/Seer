 package uk.ac.ed.inf.proj.xmlnormaliser.validator;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 import uk.ac.ed.inf.proj.xmlnormaliser.parser.dtd.DTD;
 
 
 /**
  * A description of an action
  * 
  * @author Tomas Tauber
  * 
  */
 public class TransformAction {
 
 	private static final Logger LOGGER = Logger.getLogger(TransformAction.class.getName());
 	
 	public enum ActionType {
 		MOVE_ATTRIBUTE,
 		ADD_ATTRIBUTE,
 		ADD_NODE,
 		DELETE_NODE,
 		CHANGE_XFD,
 		DELETE_XFD,
 		ADD_XFD
 	}
 	
 	private ActionType type;
 	private Object[] parameters;
 	
 	/**
 	 * @param type
 	 * @param node
 	 */
 	public TransformAction(ActionType type, Object[] parameters) {
 		super();
 		this.type = type;
 		this.parameters = parameters;
 	}
 
 	/**
 	 * @return the type
 	 */
 	public ActionType getType() {
 		return type;
 	}
 
 	/**
 	 * @return the parameters
 	 */
 	public Object[] getParameters() {
 		return parameters;
 	}
 	
 	/**
 	 * Returns a new DTD string
 	 * @param inputDTD - starting DTD String
 	 * @param actions - actions to be applied
 	 * @param transformedDTD - the new DTD object
 	 * @return new DTD String
 	 */
 	public static String applyActions(String inputDTD, List<TransformAction> actions, DTD transformedDTD) {
 		inputDTD = inputDTD.replace("]>", "");
 		for (TransformAction action : actions) {
 			switch (action.type) {
 			case MOVE_ATTRIBUTE:
 				inputDTD = inputDTD.replaceFirst("<!ATTLIST[\\s]+" + (String) action.parameters[0] + "[\\s]+" + (String) action.parameters[2], 
 						"<!ATTLIST " + (String) action.parameters[1] + " " + (String) action.parameters[2]);
 				break;
 			case ADD_ATTRIBUTE:
 				inputDTD += "<!ATTLIST " +  (String) action.parameters[0] + " " + (String) action.parameters[1] + " CDATA #REQUIRED>\n";
 				break;
 			case ADD_NODE:
 				Matcher parent = Pattern.compile("<!ELEMENT\\s+" + (String) action.parameters[0] + "\\s+[^>]+").matcher(inputDTD);
 				if (parent.find()) {
 					inputDTD = parent.replaceFirst("<!ELEMENT " + (String) action.parameters[0] + " " + transformedDTD.getElementTypeDefinition((String) action.parameters[0]));
 				} else {
 					inputDTD += "<!ELEMENT " + (String) action.parameters[0] + " " + transformedDTD.getElementTypeDefinition((String) action.parameters[0]) + ">\n";
 					inputDTD += "<!ELEMENT " + (String) action.parameters[1] + " " + transformedDTD.getElementTypeDefinition((String) action.parameters[1]) + ">\n";
 				}
 				break;
 			case DELETE_NODE:
 				parent = Pattern.compile("<!ELEMENT\\s+" + (String) action.parameters[0] + "\\s+[^>]+").matcher(inputDTD);
 				if (parent.find()) {
 					inputDTD =parent.replaceFirst("<!ELEMENT " + (String) action.parameters[0] + " " + transformedDTD.getElementTypeDefinition((String) action.parameters[0]));
 				} else {
 					inputDTD += "<!ELEMENT " + (String) action.parameters[0] + " " + transformedDTD.getElementTypeDefinition((String) action.parameters[0]) + ">\n";
 					inputDTD += "<!ELEMENT " + (String) action.parameters[1] + " " + transformedDTD.getElementTypeDefinition((String) action.parameters[1]) + ">\n";
 				}
 				break;				
 			default:
 				break;
 			}
 		}
 		if (inputDTD.contains("<!DOCTYPE")) {
 			inputDTD += "]>";
 		}
 		return inputDTD;
 	}
 }
