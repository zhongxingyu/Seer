 package org.geekosphere.zeitgeist.processor;
 
 import java.util.ArrayList;
 
 import org.geekosphere.zeitgeist.data.ZGTag;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import at.diamonddogs.service.processor.JSONArrayProcessor;
 
public class ZGTagProcessor extends JSONArrayProcessor<ZGTag[]> {
 	private static final Logger LOGGER = LoggerFactory.getLogger(ZGTagProcessor.class);
 	public static final int ID = 23485234;
 
 	@Override
	protected ZGTag[] parse(JSONArray inputObject) {
 		ArrayList<ZGTag> ret = new ArrayList<ZGTag>();
		ZGTag tmp;
		for (int i = 0; i < inputObject.length(); i++) {
			try {
 				tmp = new ZGTag();
				JSONObject o = (JSONObject) inputObject.get(i);
 				tmp.setId(o.getInt("id"));
 				tmp.setItemCount(o.getInt("count"));
 				tmp.setTagName(o.getString("tagname"));
 				ret.add(tmp);
			} catch (Throwable tr) {
				LOGGER.debug("Could not parse tag.", tr);
 			}
 		}
 		return ret.toArray(new ZGTag[ret.size()]);
 	}
 
 	@Override
 	public int getProcessorID() {
 		return ID;
 	}
 
 }
