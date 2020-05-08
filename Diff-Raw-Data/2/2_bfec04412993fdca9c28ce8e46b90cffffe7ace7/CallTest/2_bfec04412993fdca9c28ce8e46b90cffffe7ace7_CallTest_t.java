 package test.unit;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.acmetelecom.calls.Call;
 import com.acmetelecom.calls.CallEnd;
 import com.acmetelecom.calls.CallStart;
 import com.acmetelecom.time.TimeStamp;
 
 @SuppressWarnings("deprecation")
 public class CallTest {
 	
 	private String caller = "440000000000";
 	private String callee = "441000000000";
 	
 	TimeStamp time = new TimeStamp(2000, 1, 1, 0, 0, 0);
 	TimeStamp timePlusTen = new TimeStamp(2000, 1, 1, 0, 0, 10);
 	TimeStamp timeOverlappingDay = new TimeStamp(2000, 1, 2, 0, 0, 0);
 	
 	private CallStart start = new CallStart(caller, callee, time);
 	private CallEnd end = new CallEnd(caller, callee, time);
 	private CallEnd endPlusTen = new CallEnd(caller, callee, timePlusTen);
 	private CallEnd endOverlappingDay = new CallEnd(caller, callee, timeOverlappingDay);
 
 	@Test
 	public void testCallee() {
 		Call call = new Call(start, end);
 		Assert.assertEquals(call.callee(), callee);
 	}
 
 	@Test
 	public void testDurationSeconds() {
 		Call call = new Call(start, end);
 		Assert.assertEquals(call.durationSeconds(), 0);
 		
 		call = new Call(start, endPlusTen);
 		Assert.assertEquals(call.durationSeconds(), 10);
 		
 		call = new Call(start, endOverlappingDay);
 		Assert.assertEquals(call.durationSeconds(), 86400);
 	}
 
 	@Test
 	public void testDate() {
 		Call call = new Call(start, end);
		Assert.assertEquals(call.date().toString(), time.getDate().toString());
 	}
 
 	@Test
 	public void testStartTime() {
 		Call call = new Call(start, end);
 		Assert.assertEquals(call.startTime().toString(), time.getTime().toString());
 	}
 
 	@Test
 	public void testEndTime() {
 		Call call = new Call(start, end);
 		Assert.assertEquals(call.endTime().toString(), time.getTime().toString());
 				
 		call = new Call(start, endPlusTen);
 		Assert.assertEquals(call.endTime().toString(), timePlusTen.getTime().toString());
 	}
 
 }
