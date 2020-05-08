 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.middleware.manager.test;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.generationcp.middleware.exceptions.QueryException;
 import org.generationcp.middleware.manager.Operation;
 import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
 import org.generationcp.middleware.manager.api.WorkbenchDataManager;
 import org.generationcp.middleware.pojos.Person;
 import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.CropType;
 import org.generationcp.middleware.pojos.workbench.Project;
 import org.generationcp.middleware.pojos.workbench.Tool;
 import org.generationcp.middleware.pojos.workbench.WorkbenchDataset;
 import org.generationcp.middleware.pojos.workbench.WorkflowTemplate;
 import org.generationcp.middleware.util.HibernateUtil;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TestWorkbenchDataManagerImpl{
 
     private static WorkbenchDataManager manager;
     private static HibernateUtil hibernateUtil;
 
     public static void main(String[] args) throws Exception {
         TestWorkbenchDataManagerImpl t = new TestWorkbenchDataManagerImpl();
         TestWorkbenchDataManagerImpl.setUp();
         
         t.testAddPerson();
         
         TestWorkbenchDataManagerImpl.tearDown();
     }
     
     @BeforeClass
     public static void setUp() throws Exception {
     	
    	hibernateUtil = new HibernateUtil("localhost", "3306", "workbench", "root", "");
         manager = new WorkbenchDataManagerImpl(hibernateUtil);
     }
 
     @Test
     public void testSaveProject() throws QueryException{
         Project project1 = new Project();
         project1.setProjectName("Test Project 1");
        project1.setUserId(1);
        project1.setCropType(CropType.CHICKPEA);
         project1.setTargetDueDate(new GregorianCalendar().getTime());
 
         Project project2 = new Project();
         project2.setProjectName("Test Project 2");
        project2.setCropType(CropType.CHICKPEA);
         project2.setTargetDueDate(new GregorianCalendar().getTime());
 
         WorkflowTemplate marsTemplate = new WorkflowTemplate();
         marsTemplate.setTemplateId(1L);
 
         project1.setTemplate(marsTemplate);
         project2.setTemplate(marsTemplate);
 
         manager.saveOrUpdateProject(project1);
         manager.saveOrUpdateProject(project2);
     }
 
     @Test
     public void testGetProjects()  throws QueryException{
         List<Project> projects = manager.getProjects();
 
         System.out.println("testGetProjects");
         for (Project project : projects) {
             System.out.println(project);
         }
     }
 
     /**
      * @Test public void testDeleteProject() { List<Project> projects =
      *       manager.getProjects(); manager.deleteProject(projects.get(0)); }
      **/
 
     @Test
     public void testFindTool() throws QueryException {
         Tool tool = manager.getToolWithName("fieldbook");
         System.out.println(tool);
     }
     
     @Test
     public void testAddUser() throws QueryException {
         User user = new User();
         user.setUserid(1000);
         user.setInstalid(-1);
         user.setStatus(-1);
         user.setAccess(-1);
         user.setUserid(-1);
         user.setType(-1);
         user.setName("user_test");
         user.setPassword("user_password");
         user.setPersonid(1000);
         user.setAdate(20120101);
         user.setCdate(20120101);
         
         manager.addUser(user);
     }
     
     @Test
     public void testAddPerson() throws QueryException {
         Person person = new Person();
 //        person.setId(1000);
         person.setInstituteId(1);
         person.setFirstName("Lich");
         person.setMiddleName("Frozen");
         person.setLastName("King");
         person.setPositionName("King of Icewind Dale");
         person.setTitle("His Highness");
         person.setExtension("1");
         person.setFax("2");
         person.setEmail("lichking@blizzard.com");
         person.setNotes("notes");
         person.setContact("3");
         person.setLanguage(-1);
         person.setPhone("4");
         
         // add the person
         manager.addPerson(person);
     }
     
     
     @Test
     public void testGetProjectById()  throws QueryException{
         Project project = manager.getProjectById(Long.valueOf(1));
         System.out.println(project);
     }
     
     @Test
     public void testGetUserByName() throws QueryException {
         User user = manager.getUserByName("jeff", 0, 1, Operation.EQUAL).get(0);
         System.out.println(user);
     }
     
     @Test
     public void testAddDataset() throws QueryException {
         WorkbenchDataset dataset = new WorkbenchDataset();
         dataset.setName("Test Dataset");
         dataset.setDescription("Test Dataset Description");
         dataset.setCreationDate(new Date(System.currentTimeMillis()));
         dataset.setProject(manager.getProjectById(Long.valueOf(1)));
         WorkbenchDataset result = null;
         try {
             result = manager.addDataset(dataset);
         } catch (QueryException e) {
             e.printStackTrace();
         }
         System.out.println("TestAddDataset: " + result);
     }
 
     @AfterClass
     public static void tearDown() throws Exception {
         hibernateUtil.shutdown();
     }
 }
