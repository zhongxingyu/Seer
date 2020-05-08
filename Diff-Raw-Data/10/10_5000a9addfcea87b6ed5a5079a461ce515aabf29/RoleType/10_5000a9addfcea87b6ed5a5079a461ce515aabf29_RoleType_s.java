 package pt.ist.expenditureTrackingSystem.domain;
 
 import java.util.ResourceBundle;
 
 import myorg.domain.groups.IRoleEnum;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;
 import pt.utl.ist.fenix.tools.util.i18n.Language;
 
public enum RoleType implements IRoleEnum, IPresentableEnum {
 
     ACQUISITION_CENTRAL,
     ACQUISITION_CENTRAL_MANAGER,
     ACCOUNTING_MANAGER,
     PROJECT_ACCOUNTING_MANAGER,
     MANAGER,
     TREASURY_MANAGER,
     SUPPLIER_MANAGER,
     SUPPLIER_FUND_ALLOCATION_MANAGER,
     STATISTICS_VIEWER,
     AQUISITIONS_UNIT_MANAGER,
     ACQUISITION_PROCESS_AUDITOR;
 
    public String getRepresentation() {
	return getClass().getName() + "." + name();
     }
 
     @Override
     public String getLocalizedName() {
 	final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources.ExpenditureEnumerationResources", Language.getLocale());
 	return resourceBundle.getString(RoleType.class.getSimpleName() + "." + name());
     }
 
 }
