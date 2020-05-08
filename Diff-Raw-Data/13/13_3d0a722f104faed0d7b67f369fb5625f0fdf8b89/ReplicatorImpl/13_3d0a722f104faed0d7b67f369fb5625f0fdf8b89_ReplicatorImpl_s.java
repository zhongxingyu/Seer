 package assignmentImplementation;
 
 import java.util.concurrent.Future;
 
 import keyValueBaseInterfaces.LogRecord;
 import keyValueBaseInterfaces.Replicator;
 
 /**
  * This class extends your MyLogger from assignment 2 to use its
  * logic, and it is a suggestion. You can choose to follow it
  * or lint to your last assignment's implementation in your own way.
  */
 
public class ReplicatorImpl extends MyLogger implements Replicator {
 
 	@Override
 	public Future<?> makeStable(LogRecord record) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
