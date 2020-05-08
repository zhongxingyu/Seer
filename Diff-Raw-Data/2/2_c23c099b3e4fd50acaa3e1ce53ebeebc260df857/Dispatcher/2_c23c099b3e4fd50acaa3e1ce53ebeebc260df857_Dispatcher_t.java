 package gov.usgs.cida.nude.out;
 
 import gov.usgs.cida.nude.out.mapping.ColumnToXmlMapping;
 import gov.usgs.cida.nude.out.mapping.XmlNodeAttribute;
 import gov.usgs.webservices.framework.basic.MimeType;
 import gov.usgs.webservices.framework.formatter.JSONFormatter;
 import gov.usgs.webservices.framework.formatter.NudeDataFlatteningFormatter;
 import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
 import gov.usgs.webservices.framework.transformer.ElementToAttributeTransformer;
 import java.io.IOException;
 import java.sql.SQLException;
 import javax.xml.stream.XMLStreamException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Dispatcher {
 	private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);
 	
 	/**
 	 * Returns a reader and a formatter ready to be dispatched.
 	 * @param outputType
 	 * @param tableResponse 
 	 * @return non-null StreamResponse
 	 * @throws SQLException
 	 * @throws XMLStreamException
 	 * @throws IOException
 	 */
 	public static StreamResponse buildFormattedResponse(
 			MimeType outputType, 
 			TableResponse tableResponse) throws SQLException, XMLStreamException, IOException {
 		
 		StreamResponse sr = new StreamResponse();
 		sr.setReader(tableResponse.makeXMLReader());
 
 		String litResp = null;
 		
 		switch (outputType) {
 		case CSV: // fall through
 		case EXCEL: // fall through
 		case TAB:
 
 			// Configure the formatter
 			NudeDataFlatteningFormatter df = new NudeDataFlatteningFormatter(outputType, false);
 			ElementToAttributeTransformer transformer = new ElementToAttributeTransformer();
 
 			df.setRowElementName(tableResponse.getRowTag());
 			// use column map to add content-defined elements
 			for (ColumnToXmlMapping col : tableResponse.getColumns()) {
 				XmlNodeAttribute[] attributes = col.getAttributes();
 				if (attributes != null) {
 					for (int attributeIndex = 0; attributeIndex < attributes.length; attributeIndex++) {
 						XmlNodeAttribute attribute = attributes[attributeIndex];
 						if (attribute.isContentDefinedElement) {
 							df.addContentDefinedElement(col
 									.getXmlElementString(),
 									attribute.name);
 						}
 					}
 				}
 			}
 			sr.setFormatter(df);
 			sr.setReader(transformer.transform(sr.getReader()));
 			
 
 			break;
 		case JSON:
 			JSONFormatter json = new JSONFormatter();
 			json.identifyRepeatedTagElement(tableResponse.getDocTag(), tableResponse.getRowTag());
 			sr.setFormatter(json);
 			if (!sr.getReader().hasNext()) {
				litResp = "{\"" + tableResponse.getDocTag() + "\": {\"@rowCount\": \"0\", \"" + tableResponse.getRowTag() + "\":[]}}";
 				log.debug("Writing JSON empty result:" + litResp);
 			}
 			break;
 		case XML:
 		default:
 			sr.setFormatter(new XMLPassThroughFormatter());
 			if (!sr.getReader().hasNext()) {
 				litResp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<"+ tableResponse.getDocTag()+" rowCount=\"0\"></"+ tableResponse.getDocTag()+">";
 				log.debug("Writing XML based empty result:" + litResp);
 			}
 			break;
 		}
 		
 		sr.setLiteralResponse(litResp);
 		
 		return sr;
 	}
 }
