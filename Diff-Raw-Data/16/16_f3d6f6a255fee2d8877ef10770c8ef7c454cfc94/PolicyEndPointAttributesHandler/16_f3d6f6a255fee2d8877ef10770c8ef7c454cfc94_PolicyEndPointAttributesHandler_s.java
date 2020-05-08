 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 
 	/** A SAX parser handler 
 	 *  
 	 *  
 	 */
 
 	public class PolicyEndPointAttributesHandler extends DefaultHandler {
 		
 		PolicyEndPointAttributesHandler()  {}
 		
 		PolicyEndPointAttributesHandler(String targetPolicyID, String targetEndPointID)  {
 			deploymentPolicy = targetPolicyID;
 			endPoint = targetEndPointID;
 		}
 		
 		private String deploymentPolicy = "";
 		private String endPoint = "";
 		private boolean deployPolicyFlag = false;
 		private boolean printFlag = false;
 	
 	/** 
 	*  
 	*  
 	*/
 	  
 	public void startElement(String namespaceUri, String localName, String qualifiedName,
                 Attributes attributes)  throws SAXException {
 		if ( !this.deployPolicyFlag && !this.printFlag )  {
 			if (qualifiedName.equalsIgnoreCase("deploymentPolicy") )  {
 				if ( attributes.getValue("xmi:id").equalsIgnoreCase(deploymentPolicy) ) {
 					this.deployPolicyFlag = true;
 				}
 			}
 		}
 		else  {
 			if ( qualifiedName.equalsIgnoreCase("serviceend-point") && !this.printFlag )  {
 				if ( attributes.getValue("xmi:id").equalsIgnoreCase(endPoint) )  {
 					System.out.println("Result:");
 					for (int i = 0; i < attributes.getLength(); i++)  {
 						if ( attributes.getLocalName(i).equalsIgnoreCase("xmi:id") )  {
 							System.out.println( new String(attributes.getLocalName(i).substring(4).toUpperCase() +
 									" = " + attributes.getValue(i) ) );
 						}
 						else  {
 						System.out.println( attributes.getLocalName(i) + " = " + attributes.getValue(i) );
 						}
 					}
 					this.printFlag = true;
 					this.deployPolicyFlag = false;
 				}
 			}
 		}
 	}
 	
 	/** 
 	*  
 	*  
 	*/
 		
 	public void endElement(String namespaceUri, String localName, String qualifiedName)
 			throws SAXException  {
 		if ( !this.printFlag )  {
 			System.out.println(endPoint + " not found.");
 		}
	}	
 }
