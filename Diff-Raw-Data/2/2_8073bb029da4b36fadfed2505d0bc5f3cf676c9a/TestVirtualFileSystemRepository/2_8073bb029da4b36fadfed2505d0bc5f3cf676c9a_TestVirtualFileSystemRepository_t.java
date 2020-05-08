 package org.wyona.yarep.examples;
 
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryFactory;
 import org.wyona.yarep.core.Revision;
 
 import java.io.File;
 
 import org.apache.log4j.Category;
 
 /**
  *
  */
 public class TestVirtualFileSystemRepository {
 
     private static Category log = Category.getInstance(TestVirtualFileSystemRepository.class);
 
     /**
      *
      */
     public static void main(String[] args) {
 
         RepositoryFactory repoFactory;
         try {
             repoFactory = new RepositoryFactory();
         } catch (Exception e) {
             System.err.println(e);
             return;
         }
 
         System.out.println(repoFactory);
         System.out.println("\n\n");
 
         Repository repo;
         try {
             repo = repoFactory.newRepository("vfs-example", new File("new-vfs-example/repository.xml"));
         } catch (Exception e) {
             System.err.println(e);
             return;
         }
 
         System.out.println(repoFactory);
 
         try {
             Node root = repo.getNode("/");
             System.out.println("Root node is collection: " + root.isCollection());
 
             Node child = root.getNode("hello-world.txt");
             System.out.println("Child node is collection: " + child.isCollection());
             System.out.println("Child node is resource: " + child.isResource());
 
             Node anotherChild = root.getNode("another-directory");
             System.out.println("Another child node is collection: " + anotherChild.isCollection());
 
             print(root.getInputStream());
 
             System.out.println("\n");
 
             print(anotherChild.getInputStream());
 
             System.out.println("\n");
 
             print(child.getInputStream());
 
             Revision[] childRevisions = child.getRevisions();
             if (childRevisions != null && childRevisions.length > 0) {
                 for (int i = 0;i < childRevisions.length; i++) {
                     System.out.println("Child revision: " + childRevisions[i]);
                    Revision revision = child.getRevision(childRevisions[i].getRevisionName());
                    System.out.println("The same child revision: " + revision);
                 }
             } else {
                 System.err.println("Child has no revisions!");
             }
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             System.err.println(e);
             return;
         }
     }
 
     /**
      *
      */
     static public void print(java.io.InputStream in) throws java.io.IOException {
         java.io.OutputStream out = System.out;
         byte[] buffer = new byte[8192];
         int bytesRead = -1;
         while ((bytesRead = in.read(buffer)) != -1) {
             out.write(buffer, 0, bytesRead);
         }
     }
 }
