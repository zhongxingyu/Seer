 package pt.ist.expenditureTrackingSystem.domain.task;
 
 public class ExportAuthorizationsADIST extends ExportAuthorizationsADIST_Base {
 
     public ExportAuthorizationsADIST() {
 	super();
     }
 
     @Override
     protected String getDbPropertyPrefix() {
 	return "db.mgp.adist";
     }
 
     @Override
     protected String getVirtualHost() {
	return "dot.adist.ist.utl.pt";
     }
 }
