 package main.java.org.elasticsearch.sorting.nativescript.script;
 
 import java.util.Date;
 import java.util.Map;
 
 import org.elasticsearch.common.Nullable;
 import org.elasticsearch.common.logging.ESLogger;
 import org.elasticsearch.common.logging.Loggers;
 import org.elasticsearch.script.AbstractDoubleSearchScript;
 import org.elasticsearch.script.ExecutableScript;
 import org.elasticsearch.script.NativeScriptFactory;
 
 public class ActivitySortScript implements NativeScriptFactory {
 	@Override
 	public ExecutableScript newScript(@Nullable Map<String, Object> params) {
 		return new SortScript();
 	}
 
 	public static class SortScript extends AbstractDoubleSearchScript {
 		
 		private final ESLogger logger = Loggers.getLogger(ActivitySortScript.class);
 		private final long one_hour = 3600000;
 		private final Date to_day = new Date();
 		
 		public SortScript() {
 		}		
 		
 		@Override
 		public double runAsDouble() {
 			long total = (Long.parseLong(getFieldValue("like")) * 5)
 					+ (Long.parseLong(getFieldValue("participate")) * 100)
 					+ Long.parseLong(getFieldValue("status"));
 
 			Date start_time = BaseModule
 					.parse_date(getFieldValue("start_time"));
 			Date end_time = BaseModule.parse_date(getFieldValue("end_time"));
 			double sum = 0;
 			if (start_time.after(to_day)) {
 				return total
 						/ ((start_time.getTime() - to_day.getTime()) / one_hour);
 				
 			} else if (start_time.before(to_day) && end_time.before(to_day)) {
 				return total
 						/ ((to_day.getTime() - end_time.getTime()) / one_hour);
 				
 			} else {
 				return (total + (end_time.getTime() - to_day.getTime())) 
 						/ ((to_day.getTime() - start_time.getTime()) / one_hour);				
			}		
 		}
 		
 		private String getFieldValue(String field) {
 			return source().get(field).toString();
 		}	
 	}
 }
