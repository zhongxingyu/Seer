 package org.wyona.yarep.tests;
 
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryFactory;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import junit.framework.TestCase;
 
 /**
  * 
  */
 public class YarepTest extends TestCase {
 
     /**
      * 
      */
     public static void testYarep() throws Exception {
 
         RepositoryFactory repoFactory;
         repoFactory = new RepositoryFactory();
         //repoFactory = new RepositoryFactory("my-yarep.properties");
 
         System.out.println(repoFactory);
 
         Repository repoA = repoFactory.newRepository("example1");
         Repository repoC = repoFactory.newRepository("hugo");
 
         // Add more repositories to repository factory
         Repository repoB;
         Repository repoD;
        repoB = repoFactory.newRepository("vanya", new File("orm-example/repository-config.xml"));
         repoD = repoFactory.newRepository("vfs-example", new File("vfs-example/repository.xml"));
 
         System.out.println(repoFactory);
 
         // Test YarepUtil ...
         Path path = new Path("/example2/hello.txt");
         org.wyona.yarep.util.RepoPath rp = new org.wyona.yarep.util.YarepUtil().getRepositoryPath(path, repoFactory);
         System.out.println("YarepUtil: " + rp.getRepo() + " " + rp.getPath());
         path = new Path("/pele/hello/");
         rp = new org.wyona.yarep.util.YarepUtil().getRepositoryPath(path, repoFactory);
         System.out.println("YarepUtil: " + rp.getRepo() + " " + rp.getPath());
 
         Path worldPath = new Path("/hello/world.txt");
 
         // Write content to repository
         System.out.println("\nWrite content to repository " + repoA.getName() + " (repoA) ...");
         Writer writerA = repoA.getWriter(worldPath);
         String testContent = "Hello World!\n...\n";
         writerA.write(testContent);
         writerA.close();
 
         System.out.println("\nWrite content to repository " + repoB.getName() + " (repoB) ...");
         Writer writerB = repoB.getWriter(worldPath);
 
 // TODO: See TODO.txt re VFS implementation
 /*
             System.out.println("\nWrite content to repository " + repoD.getName() + "...");
             Writer writerD = repoD.getWriter(new Path("/hello/vfs-example.txt"));
             writerD.write("Hello VFS example!\n...");
             writerD.close();
 */
 
         // Read content from repository
         System.out.println("\nRead content from repository " + repoA.getName() + " (repoA) ...");
         Reader readerA = repoA.getReader(worldPath);
         BufferedReader br = new BufferedReader(readerA);
         String line = br.readLine();
         StringWriter strWriter = new StringWriter();
         while (line != null) {
             strWriter.write(line + "\n");
             //System.out.println(line);
             line = br.readLine();
         }
         System.out.println(strWriter.toString());
         strWriter.close();
         br.close();
         readerA.close();
         assertEquals("Repository content did not match expected result.", testContent, strWriter.toString());
 
         System.out.println("\nRead content from repository " + repoD.getName() + " (repoD) ...");
         Reader readerD = repoD.getReader(new Path("/hello/vfs-example.txt"));
         br = new BufferedReader(readerD);
         String firstLine = br.readLine();
         System.out.println("Very first line: " + firstLine);
         readerD.close();
         assertEquals("Repository content did not match expected result.", "Hello VFS!", firstLine);
 
         System.out.println("\nRead content from node without a UID:");
         readerA = repoA.getReader(new Path("/no/uid/example.txt"));
         br = new BufferedReader(readerA);
         firstLine = br.readLine();
         System.out.println("Very first line: " + firstLine);
         readerA.close();
         assertEquals("Repository content did not match expected result.", "No UID example!", firstLine);
 
         // List children
         System.out.println("\nList children of path /hello from repository " + repoA.getName() + " ...");
         Path helloPath = new Path("/hello");
 
         Path[] children = repoA.getChildren(helloPath);
         for (int i = 0; i < children.length; i++) {
             System.out.println(children[i]);
             // assert what?
         }
 
         // test size:
         long size = repoD.getSize(new Path("/hello/vfs-example.txt"));
         assertEquals("Size of file /hello/vfs-example.txt did not match expected size.", 11, size);
 
         
         assertFalse("Deleting '" + helloPath + "' should not be possible because it has children.", 
                 repoA.delete(helloPath));
         assertTrue("Deleting '" + worldPath + "' should be possible.", repoA.delete(worldPath));
     }
 }
