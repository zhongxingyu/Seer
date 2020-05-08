 package com.adde.webbapp_model;
 
 /**
  *
  * @author Eric Ahlberg (eahlberg@gmail.com)
  */
 public class ProjectPlatformFactory {
     // Total number of owners
    private final static int OWNERS = 5;
     // Collaborators per project
    private final static int COLLABORATORS_PER_PROJECT = 3;
     
     private ProjectPlatformFactory() {
     }
 
     public static ProjectPlatform getProjectPlatform(boolean initTestData) {
         ProjectPlatform p = new ProjectPlatform();
         if (initTestData) {
             initTestData(p);
         }
         return p;
     }
     
     private static void initTestData(ProjectPlatform p) {
         try {
            for(int i = 0; i < OWNERS; i++) {
                 User owner = new User("Owner " + i);
                System.out.println(owner.toString());
                 p.addUser(owner);
                 Project project = new Project("Test project " + i, owner);
                for (int k = 0; k < COLLABORATORS_PER_PROJECT; k++) {
                     User collaborator = new User("Collaborator " + i);
                    System.out.println(collaborator.toString());
                     p.addUser(collaborator);
                     project.addCollaborator(collaborator);
                 }
                 p.addProject(project);
             }
         } catch (NullPointerException e) {
             System.out.println("Fail in initTestData");
             System.out.println(e.toString());
         } catch (Exception e) {
             System.out.println("Fail in initTestData");
             System.out.println(e.toString());
         }
     }
 }
