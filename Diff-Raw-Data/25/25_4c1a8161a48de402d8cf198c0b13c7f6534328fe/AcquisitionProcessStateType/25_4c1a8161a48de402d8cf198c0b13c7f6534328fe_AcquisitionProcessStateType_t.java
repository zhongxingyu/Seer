 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.util.ResourceBundle;
 
 import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;
 import pt.utl.ist.fenix.tools.util.i18n.Language;
 
 public enum AcquisitionProcessStateType implements IPresentableEnum {
 
     IN_GENESIS {
 
 	@Override
 	public boolean isCompleted(final AcquisitionProcessStateType currentStateType) {
 	    return currentStateType.ordinal() > ordinal();
 	}
 
     },
 
     SUBMITTED_FOR_APPROVAL,
 
     SUBMITTED_FOR_FUNDS_ALLOCATION,
 
     FUNDS_ALLOCATED_TO_SERVICE_PROVIDER,
 
     FUNDS_ALLOCATED,
 
     AUTHORIZED,
 
    INVITES_SENT,

     IN_NEGOTIATION,

     NEGOTIATION_ENDED,

     SELECTED_SERVICE_PROVIDER,

     DOCUMENTATION_INSERTED,
 
     ACQUISITION_PROCESSED,

     INVOICE_RECEIVED,
 
     SUBMITTED_FOR_CONFIRM_INVOICE,
 
     INVOICE_CONFIRMED,
 
     FUNDS_ALLOCATED_PERMANENTLY,
 
     ACQUISITION_PAYED {
 
 	@Override
 	public boolean hasNextState() {
 	    return false;
 	}
     },
     REJECTED {
 
 	@Override
 	public boolean showFor(final AcquisitionProcessStateType currentStateType) {
 	    return currentStateType == this;
 	}
 
 	@Override
 	public boolean hasNextState() {
 	    return false;
 	}
 
 	@Override
 	public boolean isBlocked(final AcquisitionProcessStateType currentStateType) {
 	    return true;
 	}
 
     },
     CANCELED {
 
 	@Override
 	public boolean showFor(final AcquisitionProcessStateType currentStateType) {
 	    return currentStateType == this;
 	}
 
 	@Override
 	public boolean hasNextState() {
 	    return false;
 	}
 
 	@Override
 	public boolean isBlocked(final AcquisitionProcessStateType currentStateType) {
 	    return true;
 	}
 
     };
 
     private AcquisitionProcessStateType() {
     }
 
     public String getLocalizedName() {
	final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources.ExpenditureEnumerationResources", Language
		.getLocale());
 	return resourceBundle.getString(AcquisitionProcessStateType.class.getSimpleName() + "." + name());
     }
 
     public boolean showFor(final AcquisitionProcessStateType currentStateType) {
 	return currentStateType.isActive();
     }
 
     public boolean hasNextState() {
 	return true;
     }
 
     public boolean isCompleted(final AcquisitionProcessStateType currentStateType) {
 	return currentStateType.ordinal() >= ordinal();
     }
 
     public boolean isBlocked(final AcquisitionProcessStateType currentStateType) {
 	return false;
     }
 
     public boolean isActive() {
 	return (this != REJECTED && this != CANCELED);
     }
 
     public boolean isInOrPastState(final AcquisitionProcessStateType acquisitionProcessStateType) {
 	return ordinal() >= acquisitionProcessStateType.ordinal();
     }
 
    public String getDescription() {
	return getLocalizedName();
    }

 }
