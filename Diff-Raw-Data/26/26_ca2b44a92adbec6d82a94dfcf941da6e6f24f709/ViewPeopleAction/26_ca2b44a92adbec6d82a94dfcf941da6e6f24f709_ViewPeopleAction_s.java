 package sampleapp.actions;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sampleapp.SampleAppDataManager;
 import sampleapp.SampleAppDbWorker;
 import sampleapp.entities.Person;
 
 public class ViewPeopleAction extends SampleAppAction {
 
 	private static final long serialVersionUID = 20100901L;
 
 	public static final Logger LOG = LoggerFactory.getLogger(ViewPeopleAction.class.getName());
 
 	public static final String VIEW_PERSON_MSG = "view.people.message.key";
 	
 	private List<Person> _people;
 	
 	@Override
 	public String doWork() throws Exception {
		new SampleAppDbWorker() { public void doDbTasks(SampleAppDataManager dataMgr) {
 			_people = dataMgr.getPeople();
 		}}.doWork();
 		
 		return SUCCESS;
 	}
 	
 	public List<Person> getPeople() {
 		return _people;
 	}
 	
 	public String getMessage() {
 		return getMessage(VIEW_PERSON_MSG);
 	}
 }
