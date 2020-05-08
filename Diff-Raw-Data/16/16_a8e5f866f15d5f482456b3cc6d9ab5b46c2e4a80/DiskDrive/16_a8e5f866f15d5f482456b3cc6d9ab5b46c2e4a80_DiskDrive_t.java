 public class DiskDrive {
	public static int oldTrack;
 	private int moveTime;
 	private static final int READ_TIME = 1;
 	public static final int DISK_SIZE = 1024;
 	
 	private int userCount;
 		
 	public DiskDrive() {
 		userCount = 0;
 	}
 
	public synchronized void useTheDisk(int newTrack) {
		System.out.println("newTrack: "+newTrack+" oldTrack: "+oldTrack);
		while (userCount > 0) try { wait(); } catch (Exception e) {};  // wait for "notify()"
		userCount++; 		
		moveTime = Math.abs(newTrack - oldTrack);			
		System.out.println("Disk is at track number " + oldTrack + ", and will move " + moveTime + " tracks taking " + (moveTime + 1) + "  milliseconds.");			
		oldTrack = newTrack;
 		try {Thread.sleep(moveTime + READ_TIME);} catch(Exception e) {};
 		userCount--;
 		System.out.println("userCount: " +userCount);
 		notify();
 	}
 }
