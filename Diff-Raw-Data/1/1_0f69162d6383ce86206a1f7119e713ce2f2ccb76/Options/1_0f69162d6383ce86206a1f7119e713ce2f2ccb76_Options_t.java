 package pt.ist.expenditureTrackingSystem.domain;
 
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class Options extends Options_Base {
 
     public Options(final Person person) {
 	super();
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());	
 	setPerson(person);
 	setDisplayAuthorizationPending(Boolean.FALSE);
 	setRecurseAuthorizationPendingUnits(Boolean.FALSE);
	setReceiveNotificationsByEmail(Boolean.FALSE);
     }
 
     @Service
     public void setCascadingStyleSheet(final byte[] bytes, final String filename) {
 	CascadingStyleSheet cascadingStyleSheet = getCascadingStyleSheet();
 	if (cascadingStyleSheet == null) {
 	    cascadingStyleSheet = new CascadingStyleSheet(this);
 	}
 	cascadingStyleSheet.setContent(bytes);
 	cascadingStyleSheet.setContentType("text/css");
 	cascadingStyleSheet.setFilename(filename);
     }
 
     @Service
     public void deleteCascadingStyleSheet() {
 	CascadingStyleSheet cascadingStyleSheet = getCascadingStyleSheet();
 	if (cascadingStyleSheet != null) {
 	    cascadingStyleSheet.delete();
 	}
     }
 
 }
