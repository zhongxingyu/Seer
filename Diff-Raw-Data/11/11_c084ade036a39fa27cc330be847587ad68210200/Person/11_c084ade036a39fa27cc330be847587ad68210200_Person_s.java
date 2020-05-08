 package pt.ist.expenditureTrackingSystem.domain.organization;
 
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import module.workflow.domain.WorkflowLog;
 import module.workflow.domain.WorkflowProcess;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.MyOrg;
 import myorg.domain.User;
 import myorg.domain.util.Address;
import net.sourceforge.fenixedu.domain.RemotePerson;
 
 import org.apache.commons.collections.Predicate;
 
 import pt.ist.expenditureTrackingSystem.domain.DashBoard;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.Options;
 import pt.ist.expenditureTrackingSystem.domain.Role;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.SavedSearch;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestWithPayment;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.AuthorizationLog;
 import pt.ist.expenditureTrackingSystem.domain.dto.AuthorizationBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreatePersonBean;
 import pt.ist.expenditureTrackingSystem.domain.processes.GenericProcess;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.plugins.luceneIndexing.IndexableField;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.IndexDocument;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.interfaces.Indexable;
 import pt.ist.fenixframework.plugins.luceneIndexing.domain.interfaces.Searchable;
 import pt.utl.ist.fenix.tools.util.StringNormalizer;
 import dml.runtime.RelationAdapter;
 
 public class Person extends Person_Base implements Indexable, Searchable {
 
     public static enum PersonIndexes implements IndexableField {
 
 	NAME_INDEX("name"), USERNAME_INDEX("username");
 
 	private String fieldName;
 
 	private PersonIndexes(String fieldName) {
 	    this.fieldName = fieldName;
 	}
 
 	@Override
 	public String getFieldName() {
 	    return fieldName;
 	}
 
     }
 
     public static final Comparator<Person> COMPARATOR_BY_NAME = new Comparator<Person>() {
 
 	@Override
 	public int compare(Person person1, Person person2) {
 	    final String name1 = person1.getName();
 	    final String name2 = person2.getName();
 	    return Collator.getInstance().compare(name1, name2);
 	}
 
     };
 
     public static class UserMyOrgListener extends RelationAdapter<User, MyOrg> {
 
 	@Override
 	public void afterAdd(final User user, final MyOrg myOrg) {
 	    final String username = user.getUsername();
 	    Person person = Person.findByUsername(username);
 	    if (person == null) {
 		person = new Person(user.getUsername());
 	    }
 	    user.setExpenditurePerson(person);
 	}
 
     }
 
     static {
 	User.MyOrgUser.addListener(new UserMyOrgListener());
     }
 
     protected Person() {
 	super();
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
 	new Options(this);
     }
 
     protected Person(final String username) {
 	this();
 	setUsername(username);
 	setName(username);
     }
 
     @Service
     public static Person createPerson(final CreatePersonBean createPersonBean) {
 	final String username = createPersonBean.getUsername();
 	User user = User.findByUsername(username);
 	if (user == null) {
 	    user = new User(username);
 	}
 	return user.getExpenditurePerson();
     }
 
     @Service
     public void delete() {
 	removeExpenditureTrackingSystem();
 	getRoles().clear();
 	getOptions().delete();
 	deleteDomainObject();
     }
 
     @Service
     public Authorization createAuthorization(final Unit unit, final String justification) {
 	return new Authorization(this, unit, justification);
     }
 
     @Service
     public Authorization createAuthorization(final AuthorizationBean authorizationBean, final String justification) {
 	return new Authorization(authorizationBean, justification);
     }
 
     public static Person findByUsername(final String username) {
 	if (username != null && username.length() > 0) {
 	    for (final Person person : ExpenditureTrackingSystem.getInstance().getPeopleSet()) {
 		if (username.equalsIgnoreCase(person.getUsername())) {
 		    return person;
 		}
 	    }
 	}
 	return null;
     }
 
     public Set<AcquisitionProcess> findAcquisitionProcessesPendingAuthorization() {
 	final Set<AcquisitionProcess> result = new HashSet<AcquisitionProcess>();
 	final Options options = getOptions();
 	final boolean recurseSubUnits = options.getRecurseAuthorizationPendingUnits().booleanValue();
 	for (final Authorization authorization : getAuthorizationsSet()) {
 	    authorization.findAcquisitionProcessesPendingAuthorization(result, recurseSubUnits);
 	}
 	return result;
     }
 
     public boolean hasRoleType(final RoleType type) {
 	for (final Role role : getRolesSet()) {
 	    if (role.getRoleType() == type) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     @Service
     @Override
     public void addRoles(Role role) {
 	if (!hasRoles(role)) {
 	    super.addRoles(role);
 	}
     }
 
     @Service
     @Override
     public void removeRoles(Role roles) {
 	super.removeRoles(roles);
     }
 
     public void createNewDeliveryInfo(String recipient, Address address, String phone, String email) {
 	new DeliveryInfo(this, recipient, address, phone, email);
     }
 
     public DeliveryInfo getDeliveryInfoByRecipientAndAddress(String recipient, Address address) {
 	for (DeliveryInfo deliveryInfo : getDeliveryInfosSet()) {
 	    if (deliveryInfo.getRecipient().equals(recipient) && deliveryInfo.getAddress().equals(address)) {
 		return deliveryInfo;
 	    }
 	}
 	return null;
     }
 
     private <T extends RequestWithPayment> List<T> getRequestsWithClassType(Class<T> clazz) {
 	List<T> requests = new ArrayList<T>();
 	for (RequestWithPayment acquisition : getRequestsWithyPayment()) {
 	    if (acquisition.getClass().equals(clazz)) {
 		requests.add((T) acquisition);
 	    }
 	}
 	return requests;
     }
 
     public List<AcquisitionProcess> getAcquisitionProcesses() {
 	List<AcquisitionProcess> processes = new ArrayList<AcquisitionProcess>();
 	for (AcquisitionRequest request : getRequestsWithClassType(AcquisitionRequest.class)) {
 	    processes.add(request.getAcquisitionProcess());
 	}
 	return processes;
     }
 
     public <T extends GenericProcess> List<T> getAcquisitionProcesses(Class<T> classType) {
 	List<T> processes = new ArrayList<T>();
 	for (RequestWithPayment request : getRequestsWithyPayment()) {
 	    PaymentProcess process = request.getProcess();
 	    if (classType.isAssignableFrom(process.getClass())) {
 		processes.add((T) process);
 	    }
 	}
 	return processes;
     }
 
     public Set<Authorization> getValidAuthorizations() {
 	final Set<Authorization> res = new HashSet<Authorization>();
 	for (Authorization authorization : getAuthorizationsSet()) {
 	    if (authorization.isValid()) {
 		res.add(authorization);
 	    }
 	}
 	return res;
     }
 
     public String getFirstAndLastName() {
 	final String name = super.getName();
 	int s1 = name.indexOf(' ');
 	int s2 = name.lastIndexOf(' ');
 	return s1 < 0 || s1 == s2 ? name : name.subSequence(0, s1) + name.substring(s2);
     }
 
     public List<Unit> getDirectResponsibleUnits() {
 	List<Unit> units = new ArrayList<Unit>();
 	for (Authorization authorization : getAuthorizations()) {
 	    Unit unit = authorization.getUnit();
 	    if (!unit.hasResponsibleInSubUnits()) {
 		units.add(unit);
 	    }
 	}
 	return units;
     }
 
     @Override
     @Service
     public void setDefaultSearch(SavedSearch defaultSearch) {
 	super.setDefaultSearch(defaultSearch);
     }
 
     public static Person getLoggedPerson() {
 	final User user = UserView.getCurrentUser();
 	return getPerson(user);
     }
 
     public static Person getPerson(final User user) {
 	return user == null ? null : user.getExpenditurePerson();
     }
 
     public <T extends WorkflowProcess> List<T> getProcesses(Class<T> classType) {
 	List<T> processes = new ArrayList<T>();
 	for (WorkflowProcess process : getUser().getUserProcesses()) {
 	    if (classType.isAssignableFrom(process.getClass())) {
 		processes.add((T) process);
 	    }
 	}
 	return processes;
     }
 
     @Override
     public DashBoard getDashBoard() {
 	DashBoard dashBoard = super.getDashBoard();
 	if (dashBoard == null) {
 	    dashBoard = DashBoard.newDashBoard(this);
 	    setDashBoard(dashBoard);
 	}
 	return dashBoard;
     }
 
     private <T extends WorkflowProcess> Set<T> filterLogs(Predicate predicate) {
 	Set<T> processes = new HashSet<T>();
 	for (WorkflowLog log : getUser().getUserLogs()) {
 	    WorkflowProcess process = log.getProcess();
 	    if (predicate.evaluate(process)) {
 		processes.add((T) process);
 	    }
 	}
 	return processes;
     }
 
     public <T extends GenericProcess> Set<T> getProcessesWhereUserWasInvolved(final Class<T> processClass) {
 	return filterLogs(new Predicate() {
 	    @Override
 	    public boolean evaluate(Object arg0) {
 		GenericProcess process = (GenericProcess) arg0;
 		return processClass.isAssignableFrom(process.getClass());
 	    }
 
 	});
     }
 
     public <T extends GenericProcess> Set<T> getProcessesWhereUserWasInvolvedWithUnreadComments(final Class<T> processClass) {
 	final Person person = this;
 	return filterLogs(new Predicate() {
 	    @Override
 	    public boolean evaluate(Object arg0) {
 		return processClass.isAssignableFrom(arg0.getClass())
 			&& !((GenericProcess) arg0).getUnreadCommentsForPerson(person).isEmpty();
 	    }
 
 	});
 
     }
 
     public Set<AuthorizationLog> getSortedAuthorizationLogsSet() {
 	final Set<AuthorizationLog> authorizationLogs = new TreeSet<AuthorizationLog>(AuthorizationLog.COMPARATOR_BY_WHEN);
 	authorizationLogs.addAll(getAuthorizationLogsSet());
 	return authorizationLogs;
     }
 
     @Override
     public IndexDocument getDocumentToIndex() {
 	IndexDocument document = new IndexDocument(this);
 	document.indexField(PersonIndexes.NAME_INDEX, StringNormalizer.normalize(getName()));
 	document.indexField(PersonIndexes.USERNAME_INDEX, getUsername());
 	return document;
     }
 
     @Override
     public Set<Indexable> getObjectsToIndex() {
 	Set<Indexable> set = new HashSet<Indexable>();
 	set.add(this);
 	return set;
     }
 
     // @Service
     // private static void setPersonInUser(final User user) {
     // final Person person = Person.findByUsername(user.getUsername());
     // if (person == null) {
     // final CreatePersonBean createPersonBean = new CreatePersonBean();
     // createPersonBean.setName(user.getUsername());
     // createPersonBean.setUsername(user.getUsername());
     // createPerson(createPersonBean);
     // } else {
     // person.setUser(user);
     // }
     // }
 
     // @Override
     // public void setUsername(final String username) {
     // super.setUsername(username);
     // connectToUser(username);
     // }
 
     // private void connectToUser(final String username) {
     // User user = User.findByUsername(username);
     // if (user == null) {
     // user = new User(username);
     // }
     // setUser(user);
     // }
 
     // @Override
     // public User getUser() {
     // final User user = super.getUser();
     // if (user == null) {
     // connectToUser(getUsername());
     // return super.getUser();
     // }
     // return user;
     // }
 
     @Override
     public String getEmail() {
	final User user = getUser();
	final module.organization.domain.Person person = user == null ? null : user.getPerson();
	final RemotePerson remotePerson = person == null ? null : person.getRemotePerson();
	return remotePerson == null ? super.getEmail() : remotePerson.getEmailForSendingEmails();
     }
 
     public boolean hasAnyValidAuthorization() {
 	for (final Authorization authorization : getAuthorizationsSet()) {
 	    if (authorization.isValid()) {
 		return true;
 	    }
 	}
 	return false;
     }
 
 }
