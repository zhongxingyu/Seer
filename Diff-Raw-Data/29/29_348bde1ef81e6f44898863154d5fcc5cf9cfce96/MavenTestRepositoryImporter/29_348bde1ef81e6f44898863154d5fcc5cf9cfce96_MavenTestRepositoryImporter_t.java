 package com.zenika.dorm.maven.test.importer;
 
 import com.zenika.dorm.maven.importer.MavenRepositoryImporter;
 import org.junit.Test;
 
 /**
  * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
  */
 public class MavenTestRepositoryImporter {
 
//    @Test
     public void testImporter(){
         MavenRepositoryImporter importer = new MavenRepositoryImporter("/home/erouan/.m2/repository", "http://localhost", 8080, "maven");
         importer.start();
     }
 
 }
