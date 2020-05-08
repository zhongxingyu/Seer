 package kornell.scorm.client.scorm12;
 
 import com.google.web.bindery.event.shared.EventBus;
 
 public class CMICoreModel extends DataModel implements CMIConstants {
 	//TODO: Enforce read-only, write-only and enumerations
 	
 	static {
 		register(new CMIDataElement("cmi.core.exit", null, new Action() {
 			@Override
 			public void execute(EventBus bus) {
 				//TODO: Is action run on set() or on LMSFinish()?
 				//bus.fireEvent(new LogoutEvent());
 			}
 		}));
 
 		register(new CMIDataElement("cmi.core.lesson_mode","normal"));
 		register(new CMIDataElement("cmi.core.lesson_status", NOT_ATTEMPTED));
		register(new CMIDataElement("cmi.core.lesson_location"));
 		register(new CMIDataElement("cmi.core.session_time"));
 		register(new CMIDataElement("cmi.core.score.raw"));
 		register(new CMIDataElement("cmi.core.score.min"));
 		register(new CMIDataElement("cmi.core.score.max"));
 		register(new CMIDataElement("cmi.core.score.scaled"));
 		register(new CMIDataElement("cmi\\.interactions\\.[\\d]+\\.id"));
 		register(new CMIDataElement("cmi\\.interactions\\.[\\d]+\\.type"));
 		register(new CMIDataElement("cmi\\.interactions\\.[\\d]+\\.correct_responses\\.[\\d]+\\.pattern"));
 		register(new CMIDataElement("cmi\\.interactions\\.[\\d]+\\.student_response"));
 		register(new CMIDataElement("cmi\\.interactions\\.[\\d]+\\.result"));
 	}
 
 }
