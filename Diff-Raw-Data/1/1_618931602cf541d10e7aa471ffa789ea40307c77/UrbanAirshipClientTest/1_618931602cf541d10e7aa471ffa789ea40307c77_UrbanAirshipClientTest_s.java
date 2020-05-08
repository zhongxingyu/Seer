 
 package urbanairship;
 
 import java.util.Calendar;
 
 import org.testng.Assert;
 import org.testng.annotations.*;
 
 import com.google.gson.Gson;
 
 public class UrbanAirshipClientTest
 {
 	private String username = "username";
 	private String password = "password";
 	
 	@BeforeTest
 	public void setUp()
 	{
 		String usernameProperty = (String) System.getProperties().get("urbanairship.java.username");
 		if (usernameProperty != null)
 		{
 			username = usernameProperty;
 		}
 		
 		String passwordProperty = (String) System.getProperties().get("urbanairship.java.password");
 		if (passwordProperty != null)
 		{
 			password = passwordProperty;
 		}
 	}
 	
 	@Test(enabled=false)
 	public void client()
 	{
 		UrbanAirshipClient client = new UrbanAirshipClient(username, password);
 		
 		Device dev = new Device();
 		dev.setiOSDeviceToken("todo");
 		
 		client.register(dev);
 
 	}
 	
 	
 	@Test
 	public void jsonPush()
 	{
 		APS aps = new APS();
 		aps.setAlert("Hello world");
 		aps.setAutoBadge("+1");
 		
 		Push p = new Push();
 		p.setAps(aps);
 		
 		Gson gson = GsonFactory.create();
 		
 		String json = gson.toJson(p);
 		
 		Assert.assertNotNull(json);
 
 		System.out.println("Push: " + json);
 		
 	}
 	
 	@Test
 	public void jsonCalendar()
 	{
 		APS aps = new APS();
 		
 		aps.setAlert("You've got mail!");
 		aps.setBadge(1);
 		
 		Calendar c = Calendar.getInstance();
 
 		c.add(Calendar.DAY_OF_YEAR, 1);
 		
 		Broadcast bcast = new Broadcast();
 		bcast.setScheduleFor(c);
 
 		Assert.assertNotNull(bcast.getScheduleFor());
 		
 		
 		bcast.setAps(aps);
 		
 		Gson gson = GsonFactory.create();
 		
 		String json = gson.toJson(bcast);
 		
 		Assert.assertNotNull(json);
 
 		System.out.println("Broadcast: " + json);
 		
 		Broadcast bcast2 = gson.fromJson(json, Broadcast.class);
 		
 		Assert.assertNotNull(bcast2);
 		
 		Assert.assertNotNull(bcast2.getScheduleFor());
 		
 		Assert.assertEquals(bcast.getScheduleFor().getTimeInMillis(), 
 							bcast2.getScheduleFor().getTimeInMillis());
 		
 		
 	}
 	
 }
