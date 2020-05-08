 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.xeneo.db;
 
 import java.util.*;
 import org.junit.After;
 import static org.junit.Assert.*;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.xeneo.core.activity.Activity;
 import org.xeneo.core.activity.ActivityProvider;
 import org.xeneo.core.activity.Actor;
 import org.xeneo.core.activity.Object;
 import org.xeneo.core.task.Case;
 import org.xeneo.core.task.CaseType;
 import org.xeneo.core.task.Task;
 
 /**
  *
  * @author Stefan Huber
  */
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "/test-config.xml")
 public class JdbcActivityTest {
 
     Logger logger = LoggerFactory.getLogger(JdbcActivityTest.class);
     // amount of activities to test
     private final int n = 5;
     @Autowired
     private JdbcCaseEngine engine;
     @Autowired
     private JdbcActivityStream stream;
     @Autowired
     private JdbcActivityManager manager;
     private String testURI = "http://test.xeneo.org/";
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
     
     @Test
     public void testActivityCreation() {
         List<Activity> acts = this.createRandomActivities("ctest", n);
         
         Iterator<Activity> it = acts.iterator();
         while (it.hasNext()) {
             Activity a = it.next();
 
             if (!manager.isExistingActivity(a.getActivityURI())) {
                 manager.addActivity(a);
             }
         }      
     }
     
     public String trim(String in) {
         return in.trim().replace(' ', '-').toLowerCase();
     }
 
     public List<Activity> createRandomActivities(String uniquePath, int n) {
         List<Activity> acts = new ArrayList<Activity>();
 
         for (int i = 0; i < n; i++) {
             Activity a = new Activity();
             a.setActivityURI(testURI + uniquePath + "/activity/" + i);
             a.setActionURI(testURI + "action/" + i);
             //a.setActorURI("http://stefanhuber.at/user/stefan");
             a.setCreationDate(Calendar.getInstance().getTime());
             a.setSummary("Summary of activity for: " + i);
             a.setDescription("Content of activity for: " + i);
             
             ActivityProvider ap = new ActivityProvider();
             ap.setActivityProviderName("ActivityProviderName: "+i);
             ap.setActivityProviderType("ActivityProviderType: "+i);
             ap.setActivityProviderURI(testURI + uniquePath+"/ActivityProviderURI/"+i);
             
             Actor acto = new Actor();
             acto.setActivityProviderURI(testURI+uniquePath+"/ActivityProviderURI/"+i);
             acto.setActorName("ActorName: "+i);
             acto.setActorURI("http://xeneo.org/user/markus/"+trim(acto.getActorName()));
             
             a.setActor((acto));
             a.setActivityProvider(ap);
             
             Object obj = new Object();
             obj.setObjectURI(testURI + "object/" + i);
             obj.setObjectName("object name: " + i);
             obj.setObjectTypeURI(testURI + "objectType/" + i);
 
             Object tar = new Object();
             tar.setObjectURI(testURI + "target/" + i);
             tar.setObjectName("target name: " + i);
             tar.setObjectTypeURI(testURI + "objectType/" + i);
 
             a.setObject(obj);
             a.setTarget(tar);
 
             acts.add(a);
         }
 
         return acts;
     }
 
     protected void createActivities(Map<String, Collection<String>> context) {
 
         List<Activity> acts = this.createRandomActivities("storytest"+ Calendar.getInstance().getTimeInMillis(), n);
         for (int i = 0; i < n; i++) {
             
             manager.addActivity(acts.get(i), context);
         }
 /*
         Set<String> cases = context.keySet();
 
         Iterator<String> ci = cases.iterator();
         while (ci.hasNext()) {
             String c = ci.next();
             List<Activity> as = stream.getActivities(c, n);
             
 
             for (int j = 0; j < n; j++) {
                 Activity a = as.get(j);
                 assertEquals(a.getActor().getActorURI(), "http://xeneo.org/user/markus/ActivityProviderName: "+j+"/ActorName: "+j);
 
             }
         }
         */
     }
     
     @Test
     public void testDBActivities(){
         
     }
     
     @Test
     public void testActivities(){
         List<Activity> activityList = createRandomActivities("blalblalba", n);
         for (int j = 0; j<n;j++){
             Activity a = activityList.get(j);
             assertEquals(a.getActivityURI(),"http://test.xeneo.org/blalblalba/activity/"+j);
             // more tests ...
         }
     }
     @Test
     public void testTaskContext() {
 
         List<Activity> acts = this.createRandomActivities("tcntxt", n);
 
         for (int i = 0; i < n; i++) {
             if(!manager.isExistingActivity(acts.get(i).getActivityURI()))
                 manager.addActivity(acts.get(i));
         }
 
         CaseType ct = engine.createCaseType("Case Type X",
                 "Case Type Description X");
         Case cs1 = engine.createCase(ct.getCaseTypeURI(), "My Case",
                 "My Case Description");
 
         Task t1 = engine.createTask("Task N", "Task N Description");
         Task t2 = engine.createTask("Task M", "Task M Description");
 
         Collection<String> tasks = new ArrayList<String>();
         tasks.add(t1.getTaskURI());
         tasks.add(t2.getTaskURI());
 
         List<Activity> l2 = stream.getActivities(cs1.getCaseURI(), n);
         assertTrue(l2.size() < 1);
         
         for (int i = 0; i < n; i++) {
             manager.addTaskContexts(acts.get(i).getActivityURI(), cs1.getCaseURI(), tasks);
         }
         //System.out.println(stream.getActivities(cs1.getCaseURI(), n).size());
         assertTrue(stream.getActivities(cs1.getCaseURI(), n).size() == n);
         //assertTrue(l3.size() == n);
         
         assertTrue(stream.getActivities(cs1.getCaseURI(), t1.getTaskURI(), n).size() == n);
         //assertTrue(l3.size() == n);
         
         assertTrue(stream.getActivities(cs1.getCaseURI(), t2.getTaskURI(), n).size() == n);
         //assertTrue(l3.size() == n);
         
     }
 
     @Test
     public void testActivityStory() {
         CaseType ct = engine.createCaseType("Some Case Type",
                 "Case Type Description");
         Case cs1 = engine.createCase(ct.getCaseTypeURI(), "A Case",
                 "My Case Description");
         Case cs2 = engine.createCase(ct.getCaseTypeURI(), "B Case",
                 "My 2. Case Description");
 
         Task t1 = engine.createTask("Task 1", "Task 1 Description");
         Task t2 = engine.createTask("Task 2", "Task 2 Description");
         Task t3 = engine.createTask("Task 3", "Task 3 Description");
         Task t4 = engine.createTask("Task 4", "Task 4 Description");
         Task t5 = engine.createTask("Task 5", "Task 5 Description");
 
         List<String> taskList1 = new ArrayList<String>();
         taskList1.add(t1.getTaskURI());
         taskList1.add(t2.getTaskURI());
         taskList1.add(t3.getTaskURI());
         taskList1.add(t5.getTaskURI());
 
         List<String> taskList2 = new ArrayList<String>();
         taskList2.add(t1.getTaskURI());
         taskList2.add(t5.getTaskURI());
         taskList2.add(t4.getTaskURI());
 
         Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
         map.put(cs1.getCaseURI(), taskList1);
         map.put(cs2.getCaseURI(), taskList2);
 
         this.createActivities(map);
 
         List<Activity> l1 = stream.getActivities(cs1.getCaseURI(), t1.getTaskURI(), n);
 
         // should be the same
         assertEquals(l1.size(), n);
         
         manager.removeTaskContexts(l1.get(0).getActivityURI(), map);
 
         List<Activity> l2 = stream.getActivities(cs1.getCaseURI(),
                 t1.getTaskURI(), n);
 
         assertEquals(l2.size(), n - 1);
 
         // TODO: really test remove and get counts okay and data okay, add
         // something remove and get with all different variants (in depht)
 
     }
 }
