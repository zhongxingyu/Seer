 import java.net.InetSocketAddress;
 import java.util.Calendar;
 import java.util.Date;
 
 
 public class AhmetScratch
 {
 	public static void main(String[] args) throws InterruptedException
 	{
 //		Date d = new Date();
 //		MyUtils.dPrintLine(d.toString());
 //		Calendar c = Calendar.getInstance();
 //		c.setTime(d);
 //		c.add(Calendar.HOUR, -24);
 //		Date e = c.getTime();
 //		MyUtils.dPrintLine(e.toString());
 
 		
 		
 		
 //	
 	User u = new User("ahmet");
 	Session s1 = new Session(u, new InetSocketAddress("localhost",6789),MyUtils.nowPlusSeconds(-20),false);
 	u.putSession(s1);
 	Session s2 = new Session(u, new InetSocketAddress("127.0.0.1",1789),MyUtils.nowPlusSeconds(-10),true);
 	u.putSession(s2);
 	Session s3 = new Session(u, new InetSocketAddress("127.0.0.1",179),MyUtils.nowPlusSeconds(0),false);
 	u.putSession(s3);
 	System.out.println(s1.compareTo(s2));
 	System.out.println(s1.compareTo(s1));
 	System.out.println(s2.compareTo(s1));
 	u.pruneSessions(0,MyUtils.nowPlusSeconds(-5),5);
	u.pruneSessions(0,MyUtils.nowPlusSeconds(-5),5);
 
 	
 	}
 }
