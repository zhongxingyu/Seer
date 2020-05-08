 package simulation.beefs.model;
 
 import manelsim.EventScheduler;
 
 public class DataServer {
 
 	private long freeSpace;
 	
 	private final Machine host;
 	
 	private static final int NUMBER_OF_CHANGES_BEFORE_LOG = 1000;
 	
 	private int numberOfChanges = 0;
 	
 	public DataServer(Machine host, long freeSpace) {
 		this.host = host;
 		this.freeSpace = freeSpace;
 	}
 	
 	public long freeSpace() {
 		return freeSpace;
 	}
 	
 	public void useDisk(long bytes) {
 		freeSpace -= bytes;
 		notifyChange();
 	}
 	
 	public void cleanSpace(long bytes) {
 		freeSpace += bytes;
 		notifyChange();
 	}
 
 	public Machine getHost() {
 		return host;
 	}
 	
 	private void notifyChange() {
 		numberOfChanges++;
 		if(numberOfChanges > NUMBER_OF_CHANGES_BEFORE_LOG) {
			String msg = String.format("%s\t%d\t%d", host.getName(), EventScheduler.now(), freeSpace());
 			System.out.println(msg);
 			numberOfChanges = 0;
 		}
 	}
 }
