 package pt.ist.expenditureTrackingSystem.domain;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 public class SyncProjects extends SyncProjects_Base {
 
     public SyncProjects() {
         super();
     }
 
     @Override
     public void executeTask() {
 	try {
 	    new SyncProjectsIST().syncData();
 	    System.out.println("Done with IST");
 	    new SyncProjectsISTid().syncData();
 	    System.out.println("Done with IST-ID");
	    new SyncProjectsADIST().syncData();
	    System.out.println("Done with ADIST");
 	} catch (final IOException e) {
 	    throw new Error(e);
 	} catch (final SQLException e) {
 	    throw new Error(e);
 	}
     }
 
     @Override
     public String getLocalizedName() {
 	return getClass().getName();
     }
 
 }
