 package de.gzockoll.observation;
 
 import java.util.HashMap;
 import java.util.Map;
 
import javax.measure.Measurable;

 import org.codehaus.jettison.json.JSONObject;
 
 public class Measurement extends Observation {
 	private PhanomenonType type;
	private Measurable quantity;
 
	public Measurement(Subject subject, PhanomenonType type, Measurable quantity) {
 		super(subject);
 		this.type = type;
 		this.quantity = quantity;
 	}
 
 	@Override
	public Measurable getValue() {
 		return quantity;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.gzockoll.prototype.camel.observation.Observation#toJSON()
 	 */
 	@Override
 	public String toJSON() {
 		Map<String, Object> entries = new HashMap<String, Object>();
 
		 entries.put("key", subject.getName() + "." + type);
		 entries.put("value", quantity.);
		 entries.put("timeTaken", timeTaken);
		 entries.put("unit", quantity.);
 
 		return new JSONObject(entries).toString();
 	}
 }
