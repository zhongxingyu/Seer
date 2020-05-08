 package org.wyona.security.test;
 
 import java.io.File;
 
 import org.wyona.security.core.IdentityManagerFactory;
 import org.wyona.security.core.api.IdentityManager;
 import org.wyona.security.core.PolicyManagerFactory;
 import org.wyona.security.core.api.Policy;
 import org.wyona.security.core.api.PolicyManager;
 import org.wyona.security.core.api.Role;
 import org.wyona.security.core.api.Usecase;
 import org.wyona.security.core.api.Identity;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.RepositoryFactory;
 
 import org.wyona.commons.io.Path;
 
 /**
  *
  */
 public class HelloWorld {
 
     /**
      *
      */
     public static void main(String[] args) {
         System.out.println("Some security samples ...\n");
 
         RepositoryFactory repoFactory;
         Repository policiesRepo;
         Repository identitiesRepo;
         
         try {
             repoFactory = new RepositoryFactory();
             policiesRepo = repoFactory.newRepository("policies", new File("src/test/repository/repository1/config/repository.xml").getAbsoluteFile());
             identitiesRepo = repoFactory.newRepository("identities", new File("src/test/repository/repository1/config/repository-identities.xml").getAbsoluteFile());
         
             PolicyManagerFactory pmf = PolicyManagerFactory.newInstance();
             PolicyManager pm = pmf.newPolicyManager(policiesRepo);
     
             IdentityManagerFactory imf = IdentityManagerFactory.newInstance();
             IdentityManager im = imf.newIdentityManager(identitiesRepo);
     
             Path path = new Path("/hello/world.html");
 
/*
             Policy policy = pm.getPolicy(path.toString());
             System.out.println(policy);
*/
 
             String[] groupnames = {"hello", "sugus"};
             if (pm.authorize(path, new Identity("lenya", groupnames), new Role("view"))) {
                 System.out.println("Access granted (T1): " + path);
             } else {
                 System.out.println("Access denied (T2): " + path);
             }
     
             try {
                 path = new Path("/");
                 if (pm.authorize(path, null, null)) {
                     System.out.println("Access granted: " + path);
                 } else {
                     System.out.println("Access denied: " + path);
                 }
             } catch (Exception e) {
             //} catch (org.wyona.security.core.AuthenticationException e) {
                 System.err.println("EXCEPTION: " + e);
             }
                             
             path = new Path("/hello");
             if (pm.authorize(path, new Identity("lenya", null), new Role("read"))) {
                 System.out.println("Access granted: " + path);
             } else {
                 System.out.println("Access denied: " + path);
             }
     
             path = new Path("/hello/sugus.txt");
             if (pm.authorize(path, new Identity("alice", null), new Role("touch"))) {
                 System.out.println("Access granted: " + path);
             } else {
                 System.out.println("Access denied: " + path);
             }
             
             path = new Path("/hello/world2.html");
             if (pm.authorize(path, new Identity("lenya",null), new Role("view"))){
         	System.out.println("Access granted: " + path);
             } else {
                 System.out.println("Access denied: " + path);
             }
             
             path = new Path("/hello/world2.html");
             if (pm.authorize(path, new Identity("alice",groupnames), new Role("view"))){
         	System.out.println("Access granted: " + path);
             } else {
                 System.out.println("Access denied: " + path);
             }
             
             path = new Path("/hello/world2.html");
             if (pm.authorize(path, new Identity("alice",null), new Role("view"))){
         	System.out.println("Access granted: " + path);
             } else {
                 System.out.println("Access denied: " + path);
             }
 
             path = new Path("/hello/world2.html");
             if (pm.authorize(path, new Identity("lenya",null), new Role("read"))){
         	System.out.println("Access granted: " + path);
             } else {
                 System.out.println("Access denied: " + path);
             }
             
             path = new Path("/hello/world2.html");
             if (pm.authorize(path, new Identity("lenya",null), new Role("touch"))){
         	System.out.println("Access granted: " + path);
             } else {
                 System.out.println("Access denied: " + path);
             }
                 
             java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
             System.out.println("Please enter a path (e.g. /hello/world.txt):");
             String value = br.readLine();
             if (value.equals("")) {
                 System.out.println("No path has been specified!");
                 return;
             }
             System.out.println("The following value has been entered: " + value);
             path = new Path(value);
 
             System.out.println("Please enter a username (e.g. lenya or alice or wyona):");
             value = br.readLine();
             if (value.equals("")) {
                 System.out.println("No username has been specified!");
                 return;
             }
             System.out.println("The following value has been entered: " + value);
             Identity identity = new Identity(value, null);
 
             System.out.println("Please enter a password (e.g. levi):");
             value = br.readLine();
             if (value.equals("")) {
                 System.out.println("No password has been specified!");
                 return;
             }
             System.out.println("The following value has been entered: " + value);
             if (im.authenticate(identity.getUsername(), value)) {
                 System.out.println("Authentication successful!");
             } else {
                 System.err.println("Authentication failed!");
                 return;
             }
 
             System.out.println("Please enter a usecase (e.g. view):");
             value = br.readLine();
             if (value.equals("")) {
                 System.out.println("No usecase has been specified!");
                 return;
             }
             System.out.println("The following value has been entered: " + value);
             Usecase usecase = new Usecase(value);
 
             if (pm.authorize(path.toString(), identity, usecase)) {
                 System.out.println("Access granted: " + path);
             } else {
                 System.out.println("Access denied: " + path);
             }
         } catch (Exception e) {
             System.err.println(e);
             e.printStackTrace();
         }
     }
 }
