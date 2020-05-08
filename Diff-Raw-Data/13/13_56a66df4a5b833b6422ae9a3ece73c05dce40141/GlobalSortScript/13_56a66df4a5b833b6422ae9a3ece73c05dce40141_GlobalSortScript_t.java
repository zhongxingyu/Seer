 package main.java.org.elasticsearch.sorting.nativescript.script;
 
 import org.elasticsearch.ElasticSearchIllegalArgumentException;
 import org.elasticsearch.common.Nullable;
 import org.elasticsearch.common.xcontent.support.XContentMapValues;
 import org.elasticsearch.script.AbstractDoubleSearchScript;
 import org.elasticsearch.script.AbstractFloatSearchScript;
 import org.elasticsearch.script.AbstractLongSearchScript;
 import org.elasticsearch.script.ExecutableScript;
 import org.elasticsearch.script.NativeScriptFactory;
 import org.elasticsearch.common.logging.ESLogger;
 import org.elasticsearch.common.logging.Loggers;
 import org.elasticsearch.common.mvel2.ast.ReturnNode;
 import org.elasticsearch.script.AbstractSearchScript;
 import org.elasticsearch.index.fielddata.ScriptDocValues;
 
 import main.java.org.elasticsearch.sorting.nativescript.script.BaseModule;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
import javax.sound.midi.MidiDevice.Info;
 
public class GlobalSortScript implements NativeScriptFactory {
 
 	@Override
 	public ExecutableScript newScript(@Nullable Map<String, Object> params) {	
 		return new SortScript();
 	}
 
 	private static class SortScript extends AbstractDoubleSearchScript {
 
 		private final long one_hour = 3600000;
 		private final Date to_day = new Date();
 		private final Map<String, Integer> map = new HashMap<String, Integer>();
		private final ESLogger logger = Loggers.getLogger(GlobalSortScript.class);
 		
 		public SortScript() {			
 			map.put("activity", 1);
 			map.put("ask_buy", 2);
 			map.put("product", 3);
 			map.put("shop_product", 4);
 		}
 
		public double runAsDouble() {		
			ScriptDocValues.Strings type = (ScriptDocValues.Strings) doc().get("_type");			
			switch (map.get(type.getValue())) {
 			case 1:
 				return activity();				
 			case 2:
 				return ask_buy();				
 			case 3:
 				return product();				
 			case 4:
 				return shop_product();				
 			default:
 				return 0;
 			}
 		}
 
 		private double activity() {
 			long total = (Long.parseLong(getFieldValue("like")) * 5)
 					+ (Long.parseLong(getFieldValue("participate")) * 100)
 					+ Long.parseLong(getFieldValue("status"));
 
 			Date start_time = BaseModule.parse_date(getFieldValue("start_time"));
 			Date end_time = BaseModule.parse_date(getFieldValue("end_time"));
 
 			if (start_time.after(to_day)) {
 				return total
 						/ ((start_time.getTime() - to_day.getTime()) / one_hour);
 			} else if (start_time.before(to_day) && end_time.before(to_day)) {
 				return total
 						/ ((to_day.getTime() - end_time.getTime()) / one_hour);
 			} else {
 				return total
 						/ ((to_day.getTime() - start_time.getTime()) / one_hour);
 			}
 		}
 
 		private double ask_buy() {
 			Date c = BaseModule.parse_date(getFieldValue("end_time"));
 			return 10 / ((to_day.getTime() - c.getTime()) / one_hour);
 		}
 
 		private double product() {
 			return 0.0001;
 		}
 
 		private double shop_product() {
 			return 0.001;
 		}
 
 		private String getFieldValue(String field) {
 			return source().get(field).toString();
 		}		
 	}
 
 }
