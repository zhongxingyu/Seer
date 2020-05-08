 package pt.ist.expenditureTrackingSystem.domain.organization;
 
 import myorg.domain.groups.PersistentGroup;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class CostCenterActiveResponsiblesGroup extends CostCenterActiveResponsiblesGroup_Base {
 
     protected CostCenterActiveResponsiblesGroup() {
         super();
     }
 
     @Override
     protected String getNameLable() {
         return "label.persistent.group.costCenterActiveResponsible.name";
     }
 
     @Override
     protected boolean isExpectedUnitType(final Unit unit) {
         return unit instanceof CostCenter;
     }
 
     @Service
     public static CostCenterActiveResponsiblesGroup getInstance() {
 	final CostCenterActiveResponsiblesGroup group = (CostCenterActiveResponsiblesGroup) PersistentGroup.getSystemGroup(CostCenterActiveResponsiblesGroup.class);
 	return group == null ? new CostCenterActiveResponsiblesGroup() : group;
     }
 
 }
