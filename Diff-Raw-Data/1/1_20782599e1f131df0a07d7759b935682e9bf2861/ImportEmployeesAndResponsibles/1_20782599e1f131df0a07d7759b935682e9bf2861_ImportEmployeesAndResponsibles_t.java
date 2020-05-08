 package pt.ist.expenditureTrackingSystem.domain.task;
 
import module.organizationIst.domain.task.ImportEmployeesAndResponsiblesAux;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class ImportEmployeesAndResponsibles extends ImportEmployeesAndResponsibles_Base {
 
     public ImportEmployeesAndResponsibles() {
 	super();
     }
 
     @Override
     @Service
     public void executeTask() {
 	ImportEmployeesAndResponsiblesAux.executeTask();
     }
 
     @Override
     public String getLocalizedName() {
 	return getClass().getName();
     }
 
 }
