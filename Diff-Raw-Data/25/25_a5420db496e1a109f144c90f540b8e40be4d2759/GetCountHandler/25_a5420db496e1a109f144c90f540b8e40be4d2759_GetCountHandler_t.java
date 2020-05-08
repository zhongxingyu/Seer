 /*
  * Created Mar 19, 2012
  */
 package ltg.es.wallcology.notifier.xml_handlers;
 
 import ltg.es.wallcology.notifier.components.ConfFile;
 import ltg.es.wallcology.notifier.requests.CountRequestData;
 
 import org.dom4j.Element;
 
 import com.github.jsonj.JsonArray;
 import com.github.jsonj.JsonObject;
 import com.github.jsonj.tools.JsonBuilder;
 import com.github.jsonj.tools.JsonSerializer;
 
 /**
  * TODO Description
  *
  * @author Gugo
  */
 public class GetCountHandler extends XMLHandler {
 	
 	private CountRequestData cd = null;
 	//private JsonObject alerts = null; 
 	private JsonArray alerts = null;
 	
 
 	/**
 	 * @param e
 	 */
 	public GetCountHandler(Element e) {
 		super(e);
 	}
 
 	/* (non-Javadoc)
 	 * @see ltg.es.wallcology.notifier.xml_handlers.XMLHandler#handle()
 	 */
 	@Override
 	public void handle() {
 		// Get the stored kids-entered data 
 		cd = (CountRequestData) rm.removeRequest(xml.attributeValue("reqId"));
 		if (cd==null)
 			return;
 		
 		// Compare values...
 		boolean doNotSendNotification = true;
 		alerts = JsonBuilder.array();
 		
 		// ... for environment
		if (!compare(Double.parseDouble(xml.elementTextTrim("temperature")), 2.0, cd.getTemp())) {
			generateAlert("Kids entered the wrong temperature value <br />Entered value = " + cd.getTemp() + " <br />Real value = " + xml.elementTextTrim("temperature"));
 			doNotSendNotification = false;
 		}
		if (!compare(Double.parseDouble(xml.elementTextTrim("humidity")), 3.0, cd.getHumid())) {
			generateAlert("Kids entered the wrong humidity value <br />Entered value = " + cd.getHumid() + " <br />Real value = " + xml.elementTextTrim("humidity"));
 			doNotSendNotification = false;
 		}
		if (!compare(Double.parseDouble(xml.elementTextTrim("light")), 3.0, cd.getLight())) {
			generateAlert("Kids entered the wrong light value <br />Entered value = " + cd.getLight() + " <br />Real value = " + xml.elementTextTrim("light"));
 			doNotSendNotification = false;
 		}
 		// ... for creatures
 		
 		// ... and the math
 		
 		
 		// And send the notification
 		if (!doNotSendNotification) {
 			// Generate alert
 			JsonObject notification = JsonBuilder.object()
					.put("id", cd.getId())
 					.put("title", "Group " + cd.getWall() + " needs help")
 					.put("alerts", alerts)
 					.get();
			//log.debug(JsonSerializer.serialize(notification));
			net.sendTo(ConfFile.getProperty("FRONTEND_USERNAME"), JsonSerializer.serialize(notification));
 		}
 	}
 	
 	
 	// Returns true if the kids' value is correct and false if it is not
 	private boolean compare(int sim, int tolerance, int kids) {
 		if (Math.abs(sim-kids) < tolerance)
 			return true;
 		return false;
 	}
 	
 	
 	// Returns true if the kids' value is correct and false if it is not
 	private boolean compare(double sim, double tolerance, double kids) {
 		if (Math.abs(sim-kids) < tolerance)
 			return true;
 		return false;
 	}
 	
 	
 	private void generateAlert(String text) {
 		alerts.add(JsonBuilder.object().put("text", text).get());
 	}
 
 }
