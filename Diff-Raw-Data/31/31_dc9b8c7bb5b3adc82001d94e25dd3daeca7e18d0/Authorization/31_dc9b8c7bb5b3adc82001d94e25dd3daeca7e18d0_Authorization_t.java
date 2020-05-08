 package pt.ist.expenditureTrackingSystem.domain.authorizations;
 
 import java.util.Set;
 
 import org.joda.time.LocalDate;
 
 import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixWebFramework.security.UserView;
 import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.pstm.Transaction;
 
 public class Authorization extends Authorization_Base {
 
     public Authorization() {
 	super();
 	setOjbConcreteClass(getClass().getName());
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
 	setStartDate(new LocalDate());
     }
 
     public Authorization(final Person person, final Unit unit) {
 	this();
 	setPerson(person);
 	setUnit(unit);
 	setCanDelegate(Boolean.FALSE);
     }
 
     @Service
     public void changeUnit(final Unit unit) {
 	setUnit(unit);
     }
 
     public void findAcquisitionProcessesPendingAuthorization(final Set<AcquisitionProcess> result, final boolean recurseSubUnits) {
 	final Unit unit = getUnit();
 	unit.findAcquisitionProcessesPendingAuthorization(result, recurseSubUnits);
     }
 
     public boolean isPersonAbleToRevokeDelegatedAuthorization(Person person) {
 	return getPerson() == person;
     }
 
     @Override
     public Boolean getCanDelegate() {
 	return super.getCanDelegate() && isValid();
     }
 
     @Service
     public void revoke() {
 	if (!isCurrentUserAbleToRevoke()) {
 	    throw new DomainException("error.person.not.authorized.to.revoke");
 	}
 	setEndDate(new LocalDate());
 	for (DelegatedAuthorization authorization : getDelegatedAuthorizations()) {
 	    authorization.revoke();
 	}
     }
 
     @Override
     public void setEndDate(LocalDate endDate) {
 
 	super.setEndDate(endDate);
 
 	if (endDate != null && (super.getEndDate() == null || super.getEndDate().isAfter(endDate))) {
 	    for (Authorization delegatedAuthorization : getDelegatedAuthorizations()) {
 		if (delegatedAuthorization.getEndDate() == null || delegatedAuthorization.getEndDate().isAfter(endDate)) {
 		    delegatedAuthorization.setEndDate(endDate);
 		}
 	    }
 	}
     }
 
     public boolean isValid() {
 	return getEndDate() == null || getEndDate().isAfter(new LocalDate());
     }
 
     public boolean isCurrentUserAbleToRevoke() {
 	User user = UserView.getUser();
 	return user != null && isValid() && isPersonAbleToRevokeDelegatedAuthorization(user.getPerson());
     }
 
    @Service
    public void delete() {
	for (final DelegatedAuthorization delegatedAuthorization : getDelegatedAuthorizationsSet()) {
	    delegatedAuthorization.delete();
	}
	removePerson();
	removeUnit();
	removeExpenditureTrackingSystem();
	Transaction.deleteObject(this);
    }

 }
