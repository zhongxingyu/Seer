 package module.workingCapital.domain;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 
 import myorg.applicationTier.Authenticate;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 import myorg.domain.VirtualHost;
 import myorg.domain.groups.PersistentGroup;
 import myorg.domain.groups.SingleUserGroup;
 import myorg.util.BundleUtil;
 
 import org.jfree.data.time.Month;
 import org.joda.time.LocalDate;
 
 import pt.ist.emailNotifier.domain.Email;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.Role;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.fenixframework.plugins.remote.domain.exception.RemoteException;
 import pt.ist.messaging.domain.Message;
 import pt.ist.messaging.domain.Sender;
 import pt.utl.ist.fenix.tools.util.i18n.Language;
 
 public class EmailDigesterUtil {
     
     public static void executeTask() {
 	final VirtualHost virtualHost = VirtualHost.getVirtualHostForThread();
 
 	Language.setLocale(Language.getDefaultLocale());
 	for (Person person : getPeopleToProcess()) {
 
 	    	final User user = person.getUser();
 	    	if (user.hasPerson() && user.hasExpenditurePerson()) {
 	    	    final UserView userView = Authenticate.authenticate(user);
 	    	    pt.ist.fenixWebFramework.security.UserView.setUser(userView);
 
 	    	    try {
 	    		final WorkingCapitalYear workingCapitalYear = WorkingCapitalYear.getCurrentYear();
 	    		final LocalDate today = new LocalDate();
 	    		final WorkingCapitalYear previousYear = today.getMonthOfYear() == Month.JANUARY ? WorkingCapitalYear.findOrCreate(today.getYear() - 1) : null;
 
 	    		final SortedSet<WorkingCapitalProcess> takenByUser = previousYear == null ?
 	    			workingCapitalYear.getTaken() : previousYear.getTaken(workingCapitalYear.getTaken());
 	    		final int takenByUserCount = takenByUser.size();
 	    		final SortedSet<WorkingCapitalProcess> pendingApproval = previousYear == null ?
 	    			workingCapitalYear.getPendingAproval() : previousYear.getPendingAproval(workingCapitalYear.getPendingAproval());
 	    		final int pendingApprovalCount = pendingApproval.size();
 	    		final SortedSet<WorkingCapitalProcess> pendingVerificationn = previousYear == null ?
 	    			workingCapitalYear.getPendingVerification() : previousYear.getPendingVerification(workingCapitalYear.getPendingVerification());
 	    		final int pendingVerificationnCount = pendingVerificationn.size();
 	    		final SortedSet<WorkingCapitalProcess> pendingProcessing = previousYear == null ?
 	    			workingCapitalYear.getPendingProcessing() : previousYear.getPendingProcessing(workingCapitalYear.getPendingVerification());
 	    		final int pendingProcessingCount = pendingProcessing.size();
 	    		final SortedSet<WorkingCapitalProcess> pendingAuthorization = previousYear == null ?
 	    			workingCapitalYear.getPendingAuthorization() : previousYear.getPendingAuthorization(workingCapitalYear.getPendingAuthorization());
 	    		final int pendingAuthorizationCount = pendingAuthorization.size();
 	    		final SortedSet<WorkingCapitalProcess> pendingPayment = previousYear == null ?
 	    			workingCapitalYear.getPendingPayment() : previousYear.getPendingPayment(workingCapitalYear.getPendingPayment());
 	    		final int pendingPaymentCount = pendingPayment.size();
 	    		final int totalPending = takenByUserCount + pendingApprovalCount + pendingVerificationnCount + pendingAuthorizationCount + pendingPaymentCount;
 
 	    		if (totalPending > 0) {
 	    		    try {
 	    			final String email = person.getEmail();
 	    			if (email != null) {
	    			    final StringBuilder body = new StringBuilder("Caro utilizador, possui processos de fundos de maneio pendentes nas ");
	    			    body.append(virtualHost.getApplicationSubTitle().getContent());
	    			    body.append(", em https://");
	    			    body.append(virtualHost.getHostname());
	    			    body.append("/.\n");
 	    			    if (takenByUserCount > 0) {
 	    				body.append("\n\tPendentes de Libertação\t");
 	    				body.append(takenByUser);
 	    			    }
 	    			    if (pendingApprovalCount > 0) {
 	    				body.append("\n\tPendentes de Aprovação\t");
 	    				body.append(pendingApprovalCount);
 	    			    }
 	    			    if (pendingVerificationnCount > 0) {
 	    				body.append("\n\tPendentes de Verificação\t");
 	    				body.append(pendingVerificationnCount);
 	    			    }
 	    			    if (pendingVerificationnCount > 0) {
 	    				body.append("\n\tPendentes de Processamento\t");
 	    				body.append(pendingProcessingCount);
 	    			    }
 	    			    if (pendingAuthorizationCount > 0) {
 	    				body.append("\n\tPendentes de Autorização\t");
 	    				body.append(pendingAuthorizationCount);
 	    			    }
 	    			    if (pendingPaymentCount > 0) {
 	    				body.append("\n\tPendentes de Pagamento\t");
 	    				body.append(pendingPaymentCount);
 	    			    }
 	    			    body.append("\n\n\tTotal de Processos de Fundos de Maneio Pendentes\t");
 	    			    body.append(totalPending);
 
 	    			    body.append("\n\nSegue um resumo detalhado dos processos pendentes.\n");
 	    			    if (takenByUserCount > 0) {
 	    				report(body, "Pendentes de Libertação", takenByUser);
 	    			    }
 	    			    if (pendingApprovalCount > 0) {
 	    				report(body, "Pendentes de Aprovação", pendingApproval);
 	    			    }
 	    			    if (pendingVerificationnCount > 0) {
 	    				report(body, "Pendentes de Verificação", pendingVerificationn);
 	    			    }
 	    			    if (pendingAuthorizationCount > 0) {
 	    				report(body, "Pendentes de Autorização", pendingAuthorization);
 	    			    }
 	    			    if (pendingPaymentCount > 0) {
 	    				report(body, "Pendentes de Pagamento", pendingPayment);
 	    			    }
 
 	    			    final Sender sender = virtualHost.getSystemSender();
 	    			    final PersistentGroup group = SingleUserGroup.getOrCreateGroup(person.getUser());
	    			    new Message(sender, Collections.EMPTY_SET, Collections.singleton(group), Collections.EMPTY_SET, 
	    				    Collections.EMPTY_SET, null, "Processos Pendentes - Fundos de Maneio",
	    				    body.toString(), null);
 	    			}
 	    		    } catch (final RemoteException ex) {
 	    			System.out.println("Unable to lookup email address for: " + person.getUsername());
 	    			// skip this person... keep going to next.
 	    		    }
 	    		}
 	    	    } finally {
 	    		pt.ist.fenixWebFramework.security.UserView.setUser(null);
 	    	    }
 	    	}
 	}
     }
 
     private static void report(final StringBuilder body, final String title, final SortedSet<WorkingCapitalProcess> processes) {
 	body.append("\n\t");
 	body.append(title);
 	body.append(":");
 	for (final WorkingCapitalProcess workingCapitalProcess : processes) {
 	    final WorkingCapital workingCapital = workingCapitalProcess.getWorkingCapital();
 	    body.append("\n\t\t");
 	    body.append(workingCapital.getUnit().getPresentationName());
 	    body.append(" - ");
 	    body.append(BundleUtil.getStringFromResourceBundle("resources.WorkingCapitalResources", "label.module.workingCapital.year"));
 	    body.append(" ");
 	    body.append(workingCapital.getWorkingCapitalYear().getYear());
 	}
     }
 
     protected String getBundle() {
 	return "resources.ContentResources";
     }
 
     protected String getMessage(final String key) {
 	return BundleUtil.getStringFromResourceBundle(getBundle(), key);
     }
 
     private static Collection<Person> getPeopleToProcess() {
 	final Set<Person> people = new HashSet<Person>();
 	final LocalDate today = new LocalDate();
 	final ExpenditureTrackingSystem instance = ExpenditureTrackingSystem.getInstance();
 	for (final Authorization authorization : instance.getAuthorizationsSet()) {
 	    if (authorization.isValidFor(today)) {
 		final Person person = authorization.getPerson();
 		if (person.getOptions().getReceiveNotificationsByEmail()) {
 		    people.add(person);
 		}
 	    }
 	}
 	for (final RoleType roleType : RoleType.values()) {
 	    addPeopleWithRole(people, roleType);
 	}
 	for (final AccountingUnit accountingUnit : instance.getAccountingUnitsSet()) {
 	    addPeople(people, accountingUnit.getPeopleSet());
 	    addPeople(people, accountingUnit.getProjectAccountantsSet());
 	    addPeople(people, accountingUnit.getResponsiblePeopleSet());
 	    addPeople(people, accountingUnit.getResponsibleProjectAccountantsSet());
 	    addPeople(people, accountingUnit.getTreasuryMembersSet());
 	}
 	final WorkingCapitalYear workingCapitalYear = WorkingCapitalYear.getCurrentYear();
 	addMovementResponsibles(people, workingCapitalYear);
 	if (today.getMonthOfYear() == Month.JANUARY) {
 	    final WorkingCapitalYear previousYear = WorkingCapitalYear.findOrCreate(today.getYear() - 1);
 	    addMovementResponsibles(people, previousYear);
 	}
 
 	return people;
     }
 
     private static void addMovementResponsibles(final Set<Person> people, final WorkingCapitalYear workingCapitalYear) {
 	for (final WorkingCapital workingCapital : workingCapitalYear.getWorkingCapitalsSet()) {
 	    final module.organization.domain.Person movementResponsible = workingCapital.getMovementResponsible();
 	    if (movementResponsible.hasUser()) {
 		final User user = movementResponsible.getUser();
 		if (user.hasExpenditurePerson()) {
 		    final Person person = user.getExpenditurePerson();
 		    if (person.getOptions().getReceiveNotificationsByEmail()) {
 			people.add(person);
 		    }
 		}
 	    }
 	}
     }
 
     private static void addPeopleWithRole(final Set<Person> people, final RoleType roleType) {
 	final Role role = Role.getRole(roleType);
 	addPeople(people, role.getPersonSet());
     }
 
     private static void addPeople(final Set<Person> people, Collection<Person> unverified) {
 	for (final Person person : unverified) {
 	    if (person.getOptions().getReceiveNotificationsByEmail()) {
 		people.add(person);
 	    }
 	}
     }
 
 }
