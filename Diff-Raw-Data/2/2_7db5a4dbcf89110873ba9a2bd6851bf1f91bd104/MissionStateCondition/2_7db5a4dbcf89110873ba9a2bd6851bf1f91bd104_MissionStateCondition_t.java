 package com.qeevee.gq.rules.cond;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.dom4j.Element;
 
 import edu.bonn.mobilegaming.geoquest.Globals;
 import edu.bonn.mobilegaming.geoquest.Variables;
 
 public class MissionStateCondition extends Condition { 
 
 	private static Map<String, Double> stateMap = new HashMap<String, Double>();
 	static {
 		stateMap.put("new", Double.valueOf(Globals.STATUS_NEW));
 		stateMap.put("succeeded", Double.valueOf(Globals.STATUS_SUCCEEDED));
 		stateMap.put("failed", Double.valueOf(Globals.STATUS_FAIL));
 		stateMap.put("running", Double.valueOf(Globals.STATUS_RUNNING));
 	}
 
 	private String id = null;
 	private Double state = null;
 
 	@Override
 	protected boolean init(Element xmlCondition) {
 		id = xmlCondition.attributeValue("id");
 		if (id == null)
 			return false;
 
 		state = stateMap.get(xmlCondition.attributeValue("state"));
 		if (state == null)
 			return false;
 
 		return super.init(xmlCondition);
 	}
 
 	@Override
 	public boolean isFulfilled() {
		return (((Double)Variables.getValue(Variables.SYSTEM_PREFIX + id + Variables.STATUS_SUFFIX))
 				.equals(state));
 	}
 
 }
