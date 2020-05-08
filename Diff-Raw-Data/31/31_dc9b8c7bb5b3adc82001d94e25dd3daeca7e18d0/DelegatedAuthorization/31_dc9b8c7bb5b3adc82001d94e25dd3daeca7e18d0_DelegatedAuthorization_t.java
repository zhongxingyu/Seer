 package pt.ist.expenditureTrackingSystem.domain.authorizations;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class DelegatedAuthorization extends DelegatedAuthorization_Base {
 
     DelegatedAuthorization(Person delegatedPerson, Authorization authorization, Boolean canDelegate, LocalDate endDate) {
 	super();
 	checkParameters(authorization, delegatedPerson, canDelegate, endDate);
 	setPerson(delegatedPerson);
 	setAuthorization(authorization);
 	setCanDelegate(canDelegate);
 	setUnit(authorization.getUnit());
 	setStartDate(new LocalDate());
 	setEndDate(endDate);
     }
 
     private void checkParameters(Authorization authorization, Person delegatedPerson, Boolean canDelegate, LocalDate endDate) {
 	if (authorization == null || delegatedPerson == null || canDelegate == null) {
 	    throw new DomainException("error.authorization.person.and.delegation.are.required");
 	}
 
 	if (authorization.getEndDate() != null
 		&& ((endDate != null && endDate.isAfter(authorization.getEndDate()) || (authorization.getEndDate() != null && endDate == null)))) {
 	    throw new DomainException("error.authorization.cannot.be.delegated.after.your.end.date");
 	}
 
     }
 
     public Person getDelegatingPerson() {
 	return getAuthorization().getPerson();
     }
 
     @Service
     public static void delegate(Authorization authorization, Person person, Boolean canDelegate, LocalDate endDate) {
 	new DelegatedAuthorization(person, authorization, canDelegate, endDate);
     }
 
     @Override
     public boolean isPersonAbleToRevokeDelegatedAuthorization(Person person) {
 	return getDelegatingPerson() == person || getAuthorization().isPersonAbleToRevokeDelegatedAuthorization(person);
     }
 
    @Override
    public void delete() {
	removeAuthorization();
	super.delete();
    }

 }
