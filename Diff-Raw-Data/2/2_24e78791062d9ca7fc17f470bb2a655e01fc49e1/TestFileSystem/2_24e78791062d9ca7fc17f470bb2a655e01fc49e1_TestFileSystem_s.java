 package org.ow2.chameleon.everest.fileSystem.test;
 
 import org.junit.Test;
 import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
 import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ow2.chameleon.everest.client.api.EverestClient;
 import org.ow2.chameleon.everest.services.IllegalActionOnResourceException;
 import org.ow2.chameleon.everest.services.ResourceNotFoundException;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: colin
  * Date: 19/08/13
  * Time: 16:00
  * To change this template use File | Settings | File Templates.
  */
 @ExamReactorStrategy(PerMethod.class)
 public class TestFileSystem extends CommonTest{
 
     String path = System.getProperty("user.dir" );
 
     @Test
     public void testRead() throws ResourceNotFoundException {
         EverestClient testAPI = new EverestClient(everest);
 
         System.out.println("DIR PATH " + path);
         assertThat(testAPI.read("/fs"+path).retrieve("Type")).isEqualTo("DIRECTORY");
     }
 
     @Test
     public void testCreateWithName() throws ResourceNotFoundException, IllegalActionOnResourceException {
         EverestClient testAPI = new EverestClient(everest);
 
         System.out.println("DIR PATH " + path);
         testAPI.read("/fs"+path).create().with("type","FILE").with("name","toto").doIt();
         String filePath = "/fs"+path+"/toto" ;
         assertThat(testAPI.read(filePath).retrieve("type")).isEqualTo("FILE");
         assertThat(testAPI.read(filePath).retrieve("name")).isEqualTo("toto");
 
         testAPI.read(filePath).delete().doIt();
         try{
             testAPI.read(filePath);
             assertThat(true).isEqualTo(false);
         }   catch(ResourceNotFoundException e){
 
         }
 
     }
 
     @Test
     public void testCreateWithDownload() throws ResourceNotFoundException, IllegalActionOnResourceException {
         EverestClient testAPI = new EverestClient(everest);
         testAPI.read("/fs"+path).create().with("url","http://apache.opensourcemirror.com//felix/org.apache.felix.configadmin-1.6.0.jar").doIt();
         String filePath = "/fs"+path+"/org.apache.felix.configadmin-1.6.0.jar" ;
         assertThat(testAPI.read(filePath).retrieve("type")).isEqualTo("FILE");
         assertThat(testAPI.read(filePath).retrieve("name")).isEqualTo("org.apache.felix.configadmin-1.6.0.jar");
 
         testAPI.read(filePath).delete().doIt();
         try{
             testAPI.read(filePath);
             assertThat(true).isEqualTo(false);
         }   catch(ResourceNotFoundException e){
 
         }
     }
 
     @Test
     public void testUpdatePermission() throws ResourceNotFoundException, IllegalActionOnResourceException {
         EverestClient testAPI = new EverestClient(everest);
 
         System.out.println("DIR PATH " + path);
         testAPI.read("/fs"+path).create().with("type","FILE").with("name","toto").doIt();
         String filePath = "/fs"+path+"/toto" ;
         testAPI.read(filePath).update().with("writable","true").doIt();
         assertThat(testAPI.read(filePath).retrieve("writable")).isEqualTo("true");
 
         testAPI.read(filePath).update().with("readable","true").doIt();
         assertThat(testAPI.read(filePath).retrieve("readable")).isEqualTo("true");
 
         testAPI.read(filePath).update().with("writable","false").doIt();
         assertThat(testAPI.read(filePath).retrieve("writable")).isEqualTo("false");
 
         testAPI.read(filePath).update().with("readable","false").doIt();
         assertThat(testAPI.read(filePath).retrieve("readable")).isEqualTo("false");
 
         testAPI.read(filePath).update().with("writable","true").doIt();
         assertThat(testAPI.read(filePath).retrieve("writable")).isEqualTo("true");
 
         testAPI.read(filePath).delete().doIt();
 
 
     }
 
     @Test
     public void testDeleteDirectory() throws ResourceNotFoundException, IllegalActionOnResourceException {
         EverestClient testAPI = new EverestClient(everest);
 
         System.out.println("DIR PATH " + path);
         testAPI.read("/fs"+path).create().with("type","DIRECTORY").with("name","toto").doIt();
         String filePath = "/fs"+path+"/toto" ;
         testAPI.read(filePath).create().with("type","DIRECTORY").with("name","tata").doIt();
         testAPI.read(filePath).create().with("type","FILE").with("name","titi").doIt();
 
 
         testAPI.read(filePath).delete().doIt();
         try{
             testAPI.read(filePath);
             assertThat(true).isEqualTo(false);
         }   catch(ResourceNotFoundException e){
 
         }
 
         try{
             testAPI.read(filePath+"/titi");
             assertThat(true).isEqualTo(false);
         }   catch(ResourceNotFoundException e){
 
         }
 
 
         try{
             testAPI.read(filePath+"/tata");
             assertThat(true).isEqualTo(false);
         }   catch(ResourceNotFoundException e){
 
         }
     }
 
 
 }
 
 
