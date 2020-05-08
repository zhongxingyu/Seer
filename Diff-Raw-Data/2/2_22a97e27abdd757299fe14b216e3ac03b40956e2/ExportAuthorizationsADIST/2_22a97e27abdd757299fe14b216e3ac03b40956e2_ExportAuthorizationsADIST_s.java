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
	throw new Error("dot.adist.ist.utl.pt");
     }
 }
