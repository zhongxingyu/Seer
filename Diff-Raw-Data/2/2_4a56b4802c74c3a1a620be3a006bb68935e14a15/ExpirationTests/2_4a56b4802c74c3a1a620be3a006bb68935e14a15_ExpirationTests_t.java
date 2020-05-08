 package com.redhat.qe.sm.cli.tests;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 public class ExpirationTests extends SubscriptionManagerCLITestScript {
 
	@BeforeClass(groups="setup", enabled=false)
 	public void checkTime() throws Exception{
 		//make sure local clock and server clock are synced
 		Date localTime = Calendar.getInstance().getTime();
 		Date remoteTime; 
 		SSHCommandRunner runner = new SSHCommandRunner("jweiss-rhel6-1.usersys.redhat.com", sshUser, sshKeyPrivate, sshkeyPassphrase, null);
 		runner.runCommandAndWait("date");
 		String serverDateStr = runner.getStdout();
 		SimpleDateFormat unixFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
 		remoteTime = unixFormat.parse(serverDateStr);
 		long timeDiffms = Math.abs(localTime.getTime() - remoteTime.getTime());
 		Assert.assertLess(timeDiffms, 60000L, "Time difference with candlepin server is less than 1 minute");
 	}
 	
 	@Test(enabled=false)
 	public void dummyTest(){
 		
 	}
 	
 	public static void main(String... args) throws Exception{
 		ExpirationTests test = new ExpirationTests();
 		test.checkTime();
 	}
 }
