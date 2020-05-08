 package pt.ist.expenditureTrackingSystem.domain.requests.activities;
 
 import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
 import pt.ist.expenditureTrackingSystem.domain.requests.RequestForProposalProcess;
 
 public class ChooseSupplierProposal extends GenericRequestForProposalProcessActivity {
 
     @Override
     protected boolean isAccessible(RequestForProposalProcess process) {
 	User user = getUser();
 	return user != null && process.isResponsibleForUnit(user.getPerson());
     }
 
     @Override
     protected boolean isAvailable(RequestForProposalProcess process) {
 	return process.canAdvanceToAcquisition();
     }
 
     @Override
     protected void process(RequestForProposalProcess process, Object... objects) {
     }
 
 }
