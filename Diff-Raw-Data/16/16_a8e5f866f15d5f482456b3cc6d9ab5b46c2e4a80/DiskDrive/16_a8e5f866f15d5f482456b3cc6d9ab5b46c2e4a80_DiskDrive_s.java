 public class DiskDrive {
	private int track;
 	private int moveTime;
 	private static final int READ_TIME = 1;
 	public static final int DISK_SIZE = 1024;
 	
 	private int userCount;
 		
 	public DiskDrive() {
 		userCount = 0;
 	}
 
	public synchronized void useTheDisk(int t) {
		while (userCount > 0) try {  wait(); } catch (Exception e) {};  // wait for "notify()"
		userCount++; 	
		System.out.println("userCount: " +userCount);	
		moveTime = Math.abs(t - track);	
		System.out.println("Disk is at track number " + track + ", and will move " + " tracks taking " + (t + 1) + "  milliseconds.");			
 		try {Thread.sleep(moveTime + READ_TIME);} catch(Exception e) {};
 		userCount--;
 		System.out.println("userCount: " +userCount);
 		notify();
 	}
 }
