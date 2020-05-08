 package pt.ist.expenditureTrackingSystem.domain;
 
 import java.util.Comparator;
 
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 
 import org.joda.time.DateTime;
 
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.processes.GenericProcess;
 
 public abstract class ProcessState extends ProcessState_Base {
 
     public ProcessState() {
 	super();
 	setOjbConcreteClass(getClass().getName());
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
     }
 
     protected void initFields(final GenericProcess process, final Person person) {
 	setProcess(process);
 	setWho(person);
 	setWhenDateTime(new DateTime());
     }
 
     protected void checkArguments(GenericProcess process, Person person) {
 	if (process == null || person == null) {
 	    throw new DomainException("error.wrong.ProcessState.arguments");
 	}
     }
 
     protected Person getPerson() {
 	return Person.getLoggedPerson();
     }
 
     public static final Comparator<ProcessState> COMPARATOR_BY_WHEN = new Comparator<ProcessState>() {
 	public int compare(ProcessState o1, ProcessState o2) {
	    return o1.getWhenDateTime().compareTo(o2.getWhenDateTime());
 	}
     };
 
 }
