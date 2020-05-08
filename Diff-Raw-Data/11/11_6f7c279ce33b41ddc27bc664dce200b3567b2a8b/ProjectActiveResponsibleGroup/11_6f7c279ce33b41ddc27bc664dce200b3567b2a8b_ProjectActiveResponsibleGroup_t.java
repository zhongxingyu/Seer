 package pt.ist.expenditureTrackingSystem.domain.organization;
 
import myorg.domain.MyOrg;
 import myorg.domain.groups.PersistentGroup;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class ProjectActiveResponsibleGroup extends ProjectActiveResponsibleGroup_Base {
 
     protected ProjectActiveResponsibleGroup() {
         super();
	setSystemGroupMyOrg(MyOrg.getInstance());
     }
 
     @Override
     protected String getNameLable() {
         return "label.persistent.group.projectActiveResponsible.name";
     }
 
     @Override
     protected boolean isExpectedUnitType(Unit unit) {
         return unit instanceof Project;
     }
 
     @Service
     public static ProjectActiveResponsibleGroup getInstance() {
 	final ProjectActiveResponsibleGroup group = (ProjectActiveResponsibleGroup) PersistentGroup.getSystemGroup(ProjectActiveResponsibleGroup.class);
 	return group == null ? new ProjectActiveResponsibleGroup() : group;
     }
 
 }
