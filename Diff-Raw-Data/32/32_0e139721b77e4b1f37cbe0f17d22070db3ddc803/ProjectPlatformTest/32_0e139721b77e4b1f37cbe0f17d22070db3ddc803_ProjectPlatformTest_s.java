 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.adde.webbapp_model;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Eric Ahlberg (eahlberg@gmail.com)
  */
 public class ProjectPlatformTest {
     private ProjectPlatform projectPlatform;
     // Total number of owners
    private final static int NO_OF_OWNERS = 5;
     // Collaborators per project
    private final static int NO_OF_COLLABORATORS = 3;
     
     @Before 
     public void setup() {
         projectPlatform = ProjectPlatformFactory.getProjectPlatform(true);
     }
 
     // TODO: Implement usage testcases
     @Test
     public void getAllUsers() {
        assertTrue(projectPlatform.getUsers().size() == NO_OF_OWNERS * NO_OF_COLLABORATORS);
     }
     
     @Test
     public void getAllProjects() {
        assertTrue(projectPlatform.getProjects().size() == NO_OF_OWNERS);
     }
     
     @Test
     public void oneUserOneProject() {
         User benny = new User("Benny");
         try {
         // Create user and check if he's been added to the platform.
         projectPlatform.addUser(benny);
         assertTrue(projectPlatform.getUsers().contains(benny));
         
         // Create project and check if it's been added to the platform.
         Project project = new Project("Bennys Wiki", benny);
         projectPlatform.addProject(project);
         assertTrue(projectPlatform.getProjects().contains(project));
         
         // Get a project by a specific user
         assertTrue(projectPlatform.getProjectsByUser(benny).get(0).equals(project));
         
         // Get a user by his name
         assertTrue(projectPlatform.getUserByName(benny.getName()).equals(benny));
         
         } catch (NullPointerException e) {
             System.out.println(e.toString());
         } catch (Exception e) {
             System.out.println(e.toString());
         }
     }
     
     // Check that the static data has been initialized correctly.
     @Test
     public void printInitData() {
      for (User u : projectPlatform.getUsers()) {
             System.out.println("In platform: " + u);
         }
         
         for (Project proj : projectPlatform.getProjects()) {
             System.out.println("In platform: " + proj);
             for(User usr : proj.getCollaborators()) {
                 System.out.println("Collaborator in " + proj.toString());
             }
         }
     }
 }
