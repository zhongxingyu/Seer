 package module.organization.domain;
 
 import jvstm.cps.ConsistencyPredicate;
 import module.organization.domain.util.OrganizationConsistencyException;
 import myorg.domain.User;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 public class AccountabilityVersion extends AccountabilityVersion_Base {
 
     private AccountabilityVersion(LocalDate beginDate, LocalDate endDate, Accountability acc, boolean erased) {
 	super();
 	super.setAccountability(acc);
 	super.setErased(erased);
 	super.setBeginDate(beginDate);
 	super.setEndDate(endDate);
 	super.setCreationDate(new DateTime());
 	super.setUserWhoCreated(myorg.applicationTier.Authenticate.UserView.getCurrentUser());
     }
 
     // let's protect all of the methods that could compromise the workings of
     // the Acc. Version
     @Deprecated
     @Override
     public void setAccountability(Accountability accountability) {
 	throw new UnsupportedOperationException("this.slot.shouldn't.be.editable.make.new.object.instead");
     }
 
     @Deprecated
     @Override
     public void setErased(boolean erased) {
 	throw new UnsupportedOperationException("this.slot.shouldn't.be.editable.make.new.object.instead");
     }
 
     @Deprecated
     @Override
     public void setBeginDate(LocalDate beginDate) {
 	throw new UnsupportedOperationException("this.slot.shouldn't.be.editable.make.new.object.instead");
     }
 
     @Deprecated
     @Override
     public void setEndDate(LocalDate endDate) {
 	throw new UnsupportedOperationException("this.slot.shouldn't.be.editable.make.new.object.instead");
     }
 
     @Deprecated
     @Override
     public void setCreationDate(DateTime creationDate) {
 	throw new UnsupportedOperationException("this.slot.shouldn't.be.editable.make.new.object.instead");
     }
 
     @Deprecated
     @Override
     public void setUserWhoCreated(User userWhoCreated) {
 	throw new UnsupportedOperationException("this.slot.shouldn't.be.editable.make.new.object.instead");
     }
 
     @ConsistencyPredicate
     public boolean checkListBehaviour() {
 	if (hasPreviousAccVersion() && hasAccountability())
 	    return false;
 	if (!hasPreviousAccVersion() && !hasAccountability())
 	    return false;
 	return true;
 
     }
 
     @ConsistencyPredicate(OrganizationConsistencyException.class)
     protected boolean checkDateInterval() {
 	return getBeginDate() != null && (getEndDate() == null || !getBeginDate().isAfter(getEndDate()));
     }
 
     /**
      * It creates a new AccountabilityHistory item and pushes the others (if
      * they exist)
      * 
      * @param userWhoCreated
      * @param instantOfCreation
      * @param beginDate
      * @param endDate
      * @param acc
      *            the Accountability which
      * @param active
      *            if true, the new AccountabilityHistory will be marked as
      *            active, if it is false it is equivalent of deleting the new
      *            AccountabilityHistory
      * 
      * 
      */
     protected static void insertAccountabilityVersion(LocalDate beginDate, LocalDate endDate, Accountability acc, boolean erased) {
 	if (acc == null)
 	    throw new IllegalArgumentException("cant.provide.a.null.accountability");
 	// let's check on the first case i.e. when the given acc does not have
 	// an AccountabilityHistory associated
 	AccountabilityVersion firstAccHistory = acc.getAccountabilityVersion();
 	AccountabilityVersion newAccountabilityHistory = new AccountabilityVersion(beginDate, endDate, acc, erased);
 	if (firstAccHistory == null) {
	    // we are the first ones, let's just create ourselves
	    // TODO uncomment FENIX-337 this verification doesn't make sense
	    // untill all of the Accountabilities are migrated
	    // if (erased)
	    // throw new
	    // IllegalArgumentException("creating.a.deleted.acc.does.not.make.sense");
	    // //we shouldn't be creating a deleted accountability to start
	    // with!
 	} else {
 	    // let's push all of the next accHistories into their rightful
 	    // position
 	    firstAccHistory.setPreviousAccVersion(newAccountabilityHistory);
 	    newAccountabilityHistory.setNextAccVersion(firstAccHistory);
 	}
     }
 
 }
