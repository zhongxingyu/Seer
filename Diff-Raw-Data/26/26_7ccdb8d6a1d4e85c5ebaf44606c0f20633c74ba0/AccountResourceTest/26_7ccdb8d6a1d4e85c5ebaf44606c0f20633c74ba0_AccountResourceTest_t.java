 package n3phele.process;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.net.URI;
 import java.sql.Date;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 //import n3phele.client.model.CostsCollection;
 import n3phele.service.actions.CreateVMAction;
 import n3phele.service.core.NotFoundException;
 import n3phele.service.core.Resource;
 import n3phele.service.lifecycle.ProcessLifecycle;
 import n3phele.service.model.Account;
 import n3phele.service.model.Action;
 import n3phele.service.model.ActionState;
 import n3phele.service.model.Cloud;
 import n3phele.service.model.CloudProcess;
 import n3phele.service.model.Context;
 import n3phele.service.model.CostsCollection;
 import n3phele.service.model.SignalKind;
 import n3phele.service.model.VariableType;
 import n3phele.service.model.core.Collection;
 import n3phele.service.model.core.Credential;
 import n3phele.service.model.core.ExecutionFactoryCreateRequest;
 import n3phele.service.model.core.NameValue;
 import n3phele.service.model.core.User;
 import n3phele.service.model.core.VirtualServer;
 import n3phele.service.model.core.VirtualServerStatus;
 import n3phele.service.rest.impl.AccountResource;
 import n3phele.service.rest.impl.ActionResource;
 import n3phele.service.rest.impl.CloudProcessResource;
 import n3phele.service.rest.impl.CloudResource;
 import n3phele.service.rest.impl.UserResource;
 import n3phele.service.rest.impl.AccountResource.AccountManager;
 import n3phele.service.rest.impl.CloudProcessResource.CloudProcessManager;
 
 import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
 import org.joda.time.MutableDateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.mockito.Matchers;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.reflect.Whitebox;
 
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.annotation.EntitySubclass;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 
 public class AccountResourceTest {
 	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(), new LocalTaskQueueTestConfig().setDisableAutoTaskExecution(false).setCallbackClass(LocalTaskQueueTestConfig.DeferredTaskCallback.class));
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		helper.setUp();
 	}
 
 	@After
 	public void tearDown() throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
 		helper.tearDown();
 						
 		Field field = AccountResource.class.getField("dao");
 		field.setAccessible(true);
 
 		// remove final modifier from field
 		Field modifiersField = Field.class.getDeclaredField("modifiers");
 		modifiersField.setAccessible(true);
 		modifiersField.setInt(field, field.getModifiers() | Modifier.FINAL);
 
 		field.set(null, new AccountManager());
 	}
 
 	@Test
 	public void listCost24Hour() {
 		AccountResource accr = PowerMockito.spy(new AccountResource());
 		AccountManager accm = PowerMockito.mock(AccountManager.class);
 		PowerMockito.mockStatic(AccountManager.class);
 		//DateTimeUtils.setCurrentMillisFixed(1000200000000L);
		
		PowerMockito.when(accr.createMutableTime()).thenReturn(createMutableTime(1000200000000L));
 		
 		ArrayList<CloudProcess> list = new ArrayList<CloudProcess>();
 		CloudProcess p = new CloudProcess();
 		p.setAccount("acc");
 		MutableDateTime c = createMutableTime(1000200000000L);
 		p.setComplete(c.toDate());
 		c.addHours(-10);
 		c.addMinutes(-2);
 		p.setEpoch(c.toDate());
 		p.setStart(c.toDate());
 		p.setCostPerHour(0.5);
 		list.add(p);
 
 		// second process
 		p = new CloudProcess();
 		p.setAccount("acc");
 		c = createMutableTime(1000200000000L);
 		c.addHours(-20);
 		p.setComplete(c.toDate());
 		c.addHours(-5);
 		c.addMinutes(2);
 		p.setEpoch(c.toDate());
 		p.setStart(c.toDate());
 		p.setCostPerHour(0.5);
 		list.add(p);
 
 		// third process
 		p = new CloudProcess();
 		p.setAccount("acc");
 		c = createMutableTime(1000200000000L);
 		c.addHours(-1);
 		c.addMinutes(2);
 		p.setEpoch(c.toDate());
 		p.setStart(c.toDate());
 		p.setCostPerHour(0.5);
 		list.add(p);
 		Collection<CloudProcess> col = new Collection<CloudProcess>("acc", null, list);
 		try {
 			setFinalStatic(AccountResource.class.getField("dao"), accm);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		PowerMockito.when(accm.getAllProcessByDays("acc", 1)).thenReturn(col);
 		CostsCollection costs = accr.listCostPerDays("acc", 1);
 
 		List<Double> listfinal = costs.getElements();
 
 		Assert.assertEquals("Wrong Value", 0.5, listfinal.get(0));
 		Assert.assertEquals("Wrong Value", 0.5, listfinal.get(1));
 		Assert.assertEquals("Wrong Value", 0.5, listfinal.get(2));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(3));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(4));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(5));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(6));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(7));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(8));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(9));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(10));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(11));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(12));
 		Assert.assertEquals("Wrong Value", 0.5, listfinal.get(13));
 		Assert.assertEquals("Wrong Value", 0.5, listfinal.get(14));
 		Assert.assertEquals("Wrong Value", 0.5, listfinal.get(15));
 		Assert.assertEquals("Wrong Value", 0.5, listfinal.get(16));
 		Assert.assertEquals("Wrong Value", 0.5, listfinal.get(17));
 		Assert.assertEquals("Wrong Value", 0.5, listfinal.get(18));
 		Assert.assertEquals("Wrong Value", 0.5, listfinal.get(19));
 		Assert.assertEquals("Wrong Value", 0.5, listfinal.get(20));
 		Assert.assertEquals("Wrong Value", 0.5, listfinal.get(21));
 		Assert.assertEquals("Wrong Value", 1.0, listfinal.get(22));
 		Assert.assertEquals("Wrong Value", 0.5, listfinal.get(23));
 	}
 
 	static void setFinalStatic(Field field, Object newValue) throws Exception {
 		field.setAccessible(true);
 
 		// remove final modifier from field
 		Field modifiersField = Field.class.getDeclaredField("modifiers");
 		modifiersField.setAccessible(true);
 		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
 
 		field.set(null, newValue);
 	}
 
 	@Test
 	public void listCost7days() {
 		AccountResource accr = PowerMockito.spy(new AccountResource());
 		AccountManager accm = PowerMockito.mock(AccountManager.class);
 		PowerMockito.mockStatic(AccountManager.class);
 		ArrayList<CloudProcess> list = new ArrayList<CloudProcess>();
 		//DateTimeUtils.setCurrentMillisFixed(1000200000000L);

		PowerMockito.when(accr.createMutableTime()).thenReturn(createMutableTime(1000200000000L));
 		
 		CloudProcess p = new CloudProcess();
 		p.setAccount("acc");
 		MutableDateTime c = createMutableTime(1000200000000L);
 		c.addDays(-1);
 		p.setComplete(c.toDate());
 		c = createMutableTime(1000200000000L);
 		c.addDays(-3);
 		c.addMinutes(3);
 		p.setEpoch(c.toDate());
 		p.setStart(c.toDate());
 		p.setCostPerHour(0.5);
 		list.add(p);
 		
 		p = new CloudProcess();
 		c = createMutableTime(1000200000000L);
 		c.addDays(-5);
 		p.setComplete(c.toDate());
 		c = createMutableTime(1000200000000L);
 		c.addDays(-8);
 		c.addMinutes(1);
 		p.setEpoch(c.toDate());
 		p.setStart(c.toDate());
 		p.setCostPerHour(0.5);
 		list.add(p);
 		
 		p = new CloudProcess();
 		c = createMutableTime(1000200000000L);
 		c.addDays(-4);
 		p.setComplete(c.toDate());
 		c = createMutableTime(1000200000000L);
 		c.addDays(-5);
 		c.addMinutes(-1);
 		p.setEpoch(c.toDate());
 		p.setStart(c.toDate());
 		p.setCostPerHour(0.5);
 		list.add(p);
 
 		Collection<CloudProcess> col = new Collection<CloudProcess>("acc", null, list);
 		try {
 			setFinalStatic(AccountResource.class.getField("dao"), accm);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		PowerMockito.when(accm.getAllProcessByDays("acc", 7)).thenReturn(col);
 		CostsCollection costs = accr.listCostPerDays("acc", 7);
 
 		List<Double> listfinal = costs.getElements();
 
 		Assert.assertEquals("Wrong Value", 12.0, listfinal.get(0));
 		Assert.assertEquals("Wrong Value", 12.0, listfinal.get(1));
 		Assert.assertEquals("Wrong Value", 3.5, listfinal.get(2));
 		Assert.assertEquals("Wrong Value", 9.0, listfinal.get(3));
 		Assert.assertEquals("Wrong Value", 12.0, listfinal.get(4));
 		Assert.assertEquals("Wrong Value", 3.0, listfinal.get(5));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(6));
 	}
 
 	protected MutableDateTime createMutableTime(long time) {
		MutableDateTime date = new MutableDateTime(time);
		date.setZone(DateTimeZone.forOffsetHours(-3));
		return date;
 	}
 
 	@Test
 	public void listCost30days() {
 		AccountResource accr = PowerMockito.spy(new AccountResource());
 		AccountManager accm = PowerMockito.mock(AccountManager.class);
 		PowerMockito.mockStatic(AccountManager.class);
 		ArrayList<CloudProcess> list = new ArrayList<CloudProcess>();
 		//DateTimeUtils.setCurrentMillisFixed(1000200000000L);
 
		PowerMockito.when(accr.createMutableTime()).thenReturn(createMutableTime(1000200000000L));
		
 		CloudProcess p = new CloudProcess();
 		p.setAccount("acc");
 		MutableDateTime c = createMutableTime(1000200000000L);
 		c.addDays(-1);
 		p.setComplete(c.toDate());
 		c = createMutableTime(1000200000000L);
 		c.addDays(-3);
 		c.addMinutes(3);
 		p.setEpoch(c.toDate());
 		p.setStart(c.toDate());
 		p.setCostPerHour(0.5);
 		list.add(p);
 		
 		p = new CloudProcess();
 		c = createMutableTime(1000200000000L);
 		c.addDays(-5);
 		p.setComplete(c.toDate());
 		c = createMutableTime(1000200000000L);
 		c.addDays(-8);
 		c.addMinutes(1);
 		p.setEpoch(c.toDate());
 		p.setStart(c.toDate());
 		p.setCostPerHour(0.5);
 		list.add(p);
 		
 		p = new CloudProcess();
 		c = createMutableTime(1000200000000L);
 		c.addDays(-28);
 		p.setComplete(c.toDate());
 		c = createMutableTime(1000200000000L);
 		c.addDays(-30);
 		c.addMinutes(-1);
 		p.setEpoch(c.toDate());
 		p.setStart(c.toDate());
 		p.setCostPerHour(0.5);
 		list.add(p);
 		
 		p = new CloudProcess();
 		c = createMutableTime(1000200000000L);
 		c.addDays(-5);
 		p.setComplete(c.toDate());
 		c = createMutableTime(1000200000000L);
 		c.addDays(-4);
 		c.addMinutes(3);
 		p.setEpoch(c.toDate());
 		p.setStart(c.toDate());
 		p.setCostPerHour(0.5);
 		list.add(p);
 
 		p = new CloudProcess();
 		c = createMutableTime(1000200000000L);
 		c.addDays(-10);
 		p.setComplete(c.toDate());
 		c = createMutableTime(1000200000000L);
 		c.addDays(-10);
 		c.addHours(-3);
 		p.setEpoch(c.toDate());
 		p.setStart(c.toDate());
 		p.setCostPerHour(0.5);
 		list.add(p);
 		
 		Collection<CloudProcess> col = new Collection<CloudProcess>("acc", null, list);
 		try {
 			setFinalStatic(AccountResource.class.getField("dao"), accm);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		PowerMockito.when(accm.getAllProcessByDays("acc", 30)).thenReturn(col);
 		CostsCollection costs = accr.listCostPerDays("acc", 30);
 
 		List<Double> listfinal = costs.getElements();
 		Assert.assertEquals("Wrong Value", 12.0, listfinal.get(0));
 		Assert.assertEquals("Wrong Value", 3.5, listfinal.get(1));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(2));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(3));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(4));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(5));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(6));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(7));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(8));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(9));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(10));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(11));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(12));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(13));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(14));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(15));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(16));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(17));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(18));
 		Assert.assertEquals("Wrong Value", 1.5, listfinal.get(19));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(20));
 		Assert.assertEquals("Wrong Value", 9.0, listfinal.get(21));
 		Assert.assertEquals("Wrong Value", 12.0, listfinal.get(22));
 		Assert.assertEquals("Wrong Value", 12.0, listfinal.get(23));
 		Assert.assertEquals("Wrong Value", 6.0, listfinal.get(24));
 		Assert.assertEquals("Wrong Value", 9.0, listfinal.get(25));
 		Assert.assertEquals("Wrong Value", 9.0, listfinal.get(26));
 		Assert.assertEquals("Wrong Value", 12.0, listfinal.get(27));
 		Assert.assertEquals("Wrong Value", 3.0, listfinal.get(28));
 		Assert.assertEquals("Wrong Value", 0.0, listfinal.get(29));
 
 	}
 }
