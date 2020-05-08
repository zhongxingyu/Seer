 package sampleapp.actions;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sampleapp.SampleAppDataManager;
 import sampleapp.SampleAppDbWorker;
 import sampleapp.entities.Person;
 import edu.upenn.bbl.common.util.EnumUtil;
 import edu.upenn.bbl.common.web.util.ActionOperation;
 
 public class UpdatePersonAction extends SampleAppAction {
 
 	private static final long serialVersionUID = 20100901L;
 
 	private static final Logger LOG = LoggerFactory.getLogger(UpdatePersonAction.class.getName());
 	
 	private ActionOperation _operation;
 	private Person _person;
 	
 	@Override
 	public String doWork() throws Exception {
 		// save or delete person
 		LOG.info("Performing operation " + _operation + " on person: " + _person);
 		new SampleAppDbWorker() {
 			public void doDbTasks(SampleAppDataManager dataMgr) {
 				String displayName = (StringUtils.isEmpty(_person.getFullName()) ? "'No-name' user" : _person.getFullName());
 				switch (_operation) {
 				case CREATE:
 					dataMgr.saveObject(_person);
 					setMessage(ViewPeopleAction.VIEW_PERSON_MSG, displayName + " has been added.");
 					break;
 				case UPDATE:
 					dataMgr.saveObject(_person);
 					setMessage(ViewPeopleAction.VIEW_PERSON_MSG, displayName + " has been updated.");
 					break;
 				case DELETE:
 					dataMgr.deleteObject(_person);
 					setMessage(ViewPeopleAction.VIEW_PERSON_MSG, displayName + " has been deleted.");
 					break;
 				}
 			}
 		}.doWork();
 		
 		return SUCCESS;
 	}
 
 	public void setOperation(String operation) {
 		_operation = EnumUtil.getNullSafeEnum(ActionOperation.class, operation);
 	}
 
 	public Person getPerson() {
 		if (_person == null) {
 			_person = new Person();
 		}
 		return _person;
 	}
 	
 	public String getRequestUrl() {
 		return "ViewPeople";
 	}
 }
