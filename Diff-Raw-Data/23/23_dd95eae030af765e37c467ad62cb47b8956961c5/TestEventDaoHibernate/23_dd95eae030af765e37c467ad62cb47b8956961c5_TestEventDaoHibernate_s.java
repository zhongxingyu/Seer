 package com.jweekend.dao.hibernate;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.jweekend.config.modules.Module;
 import com.jweekend.data.dao.interfaces.EventDao;
 import com.jweekend.data.dataobjects.Event;
 import com.wideplay.warp.persist.WorkManager;
 
 /**
  * @author Richard Wilkinson - richard.wilkinson@jweekend.com
  *
  */
 public class TestEventDaoHibernate {
 
 	@Inject
 	protected EventDao eventDao;
 	
 	protected Event event;
 	
 	@Inject
 	protected WorkManager workManager;
 	
 	@Before
 	public void startTransaction()
 	{
 		Injector injector = Guice.createInjector(new Module(){
 			@Override
 			protected boolean initData() {
 				return false;
 			}
 		});
 		
 		injector.injectMembers(this);
 		
 		workManager.beginWork();
 		
 		event = new Event();
 		event.setLocation("new Location");
 		event.setTitle("new Title");
 		eventDao.save(event);
 		event.setId(1l);
 	}
 	
 	@After
 	public void endTransaction()
 	{
 		workManager.endWork();
 	}
 	
 	/**
	 * Test method for {@link com.jweekend.data.dao.jpa.EventDaoJPAImp#findAll()}.
 	 */
 	@Test
 	public void testFindAll() {
 		List<Event> events = new ArrayList<Event>();
 		events.add(event);
 		Assert.assertEquals(events, eventDao.findAll());
 	}
 
 	/**
	 * Test method for {@link com.jweekend.data.dao.jpa.EventDaoJPAImp#countAll()}.
 	 */
 	@Test
 	public void testCountAll() {
 		Assert.assertEquals(1, eventDao.countAll());
 	}
 
 	/**
	 * Test method for {@link com.jweekend.data.dao.jpa.AbstractDaoJPAImpl#delete(com.jweekend.data.dataobjects.DomainObject)}.
 	 */
 	@Test
 	public void testDelete() {
 		eventDao.delete(eventDao.load(event.getId()));
 		Assert.assertEquals(0, eventDao.countAll());
 	}
 
 	/**
	 * Test method for {@link com.jweekend.data.dao.jpa.AbstractDaoJPAImpl#load(java.io.Serializable)}.
 	 */
 	@Test
 	public void testLoad() {
 		Event event2 = eventDao.load(event.getId());
 		Assert.assertEquals(event, event2);
 	}
 
 	/**
	 * Test method for {@link com.jweekend.data.dao.jpa.AbstractDaoJPAImpl#save(com.jweekend.data.dataobjects.DomainObject)}.
 	 */
 	@Test
 	public void testSave() {
 		//if we have got this far then save works
 	}
 
 }
