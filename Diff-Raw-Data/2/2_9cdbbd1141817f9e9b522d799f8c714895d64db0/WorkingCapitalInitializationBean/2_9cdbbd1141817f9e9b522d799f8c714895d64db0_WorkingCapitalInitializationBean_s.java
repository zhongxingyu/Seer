 package module.workingCapital.domain.util;
 
 import java.io.Serializable;
 import java.util.Calendar;
 
 import module.organization.domain.Person;
 import module.workingCapital.domain.WorkingCapitalInitialization;
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 import myorg.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class WorkingCapitalInitializationBean implements Serializable {
 
     private Unit unit;
     private Person person = initPerson();
     private Integer year = Integer.valueOf(Calendar.getInstance().get(Calendar.YEAR));
 
     private Money requestedMonthlyValue;
     private String fiscalId;
     private String internationalBankAccountNumber;
 
     private Person initPerson() {
 	final User user = UserView.getCurrentUser();
 	return user == null ? null : user.getPerson();
     }
 
     public Unit getUnit() {
 	return unit;
     }
 
     public void setUnit(Unit unit) {
 	this.unit = unit;
     }
 
     public Person getPerson() {
 	return person;
     }
 
     public void setPerson(Person person) {
 	this.person = person;
     }
 
     public Integer getYear() {
 	return year;
     }
 
     public void setYear(Integer year) {
 	this.year = year;
     }
 
     public Money getRequestedMonthlyValue() {
 	return requestedMonthlyValue;
     }
 
     public void setRequestedMonthlyValue(Money requestedMonthlyValue) {
 	this.requestedMonthlyValue = requestedMonthlyValue;
     }
 
     public String getFiscalId() {
 	return fiscalId;
     }
 
     public void setFiscalId(String fiscalId) {
 	this.fiscalId = fiscalId;
     }
 
     public String getInternationalBankAccountNumber() {
 	return internationalBankAccountNumber;
     }
 
     public void setInternationalBankAccountNumber(String internationalBankAccountNumber) {
 	this.internationalBankAccountNumber = internationalBankAccountNumber;
     }
 
     @Service
     public WorkingCapitalInitialization create() {
 	String iban = internationalBankAccountNumber == null || internationalBankAccountNumber.isEmpty()
 		|| !Character.isDigit(internationalBankAccountNumber.charAt(0)) ? internationalBankAccountNumber : "PT50"
 		+ internationalBankAccountNumber;
	return new WorkingCapitalInitialization(year, unit, person, requestedMonthlyValue.multiply(12), fiscalId, iban);
     }
 
 }
