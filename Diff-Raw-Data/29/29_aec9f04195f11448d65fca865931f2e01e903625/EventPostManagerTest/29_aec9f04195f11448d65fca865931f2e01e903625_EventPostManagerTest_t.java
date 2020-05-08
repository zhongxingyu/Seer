 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.chalmers.fannysangles.friendzone.model;
 
 import it.chalmers.fannysangles.friendzone.model.managers.EventPostManager;
 import it.chalmers.fannysangles.friendzone.model.managers.UserFactory;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.embeddable.EJBContainer;
 import javax.naming.NamingException;
 import org.junit.After;
 import org.junit.AfterClass;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 public class EventPostManagerTest {
 
     private static EJBContainer ejbContainer;
     private static EventPostManager eventPostManager;
     
     @BeforeClass
     public static void first() throws Exception {
 //        if(ejbContainer != null)
 //            ejbContainer.close();
 //        Map<String, Object> properties = new HashMap<String, Object>();
 //        properties.put(EJBContainer.MODULES, new File("build/jar"));
         ejbContainer = javax.ejb.embeddable.EJBContainer.createEJBContainer();
         eventPostManager = (EventPostManager)ejbContainer.getContext().lookup("java:global/classes/EventPostManager");
     }
     
     @Before
     public final void setUp() throws Exception {
         
     }
 
     @AfterClass
     public static void last() {
         ejbContainer.close();
     }
     
     @Test
     public synchronized void addSearchRemove() throws Exception{
 //        EJBContainer ejbContainer = javax.ejb.embeddable.EJBContainer.createEJBContainer();
 //        EventPostManager eventPostManager = (EventPostManager)ejbContainer.getContext().lookup("java:global/classes/EventPostManager");
//        eventPostManager.reset();
         EventPost ep;
         ep = new EventPost("title", "description", new ArrayList<Tag>(), UserFactory.getInstance().getUniversalUser(), new ArrayList<FriendzoneUser>(),new ArrayList<Comment>(), new Date());
         eventPostManager.add(ep);
         List<EventPost> list = eventPostManager.getRange(0, 1);
         
         assertTrue("Size should be 1, is "+list.size(),list.size() == 1);
 
         list = eventPostManager.search("title");
         
         assertTrue("Size should be 1, is "+list.size(),list.size() == 1);
 //        EJBContainer ejbContainer = javax.ejb.embeddable.EJBContainer.createEJBContainer();
 //        EventPostManager eventPostManager = (EventPostManager)ejbContainer.getContext().lookup("java:global/classes/EventPostManager");
//        eventPostManager.reset();
         
         list = eventPostManager.getRange(0,1);
         
         eventPostManager.remove(list.get(0).getId());
         
         int count = eventPostManager.getCount();
         
         assertTrue("Count should be 0, is "+count,count == 0);
 //        ejbContainer.close();
     }
     
 }
