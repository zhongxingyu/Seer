 package mining;
 
 public class UTIL {
 	public static int getPerHour(final int value){
		return (int)(value * 3600000 / VARS.timeRunning);
 	}
 }
