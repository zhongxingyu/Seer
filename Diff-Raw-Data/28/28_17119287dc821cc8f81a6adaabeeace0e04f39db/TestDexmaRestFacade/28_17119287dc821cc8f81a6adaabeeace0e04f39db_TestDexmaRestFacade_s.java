 package jvg.dexma;
 
 import static org.junit.Assert.*;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.*;
 
 public class TestDexmaRestFacade {
   static String locationsJsonFile = "./src/test/resources/locations.json";
   static String deploymentsJsonFile = "./src/test/resources/deployments.json";
   static List<Location> locations;
   static List<Deployment> deployments;
   static DexmaRestFacade dexmaRestFacade;
   
   /** 
    * load a file which contains all the locations when calling the REST webservice
    */
   @BeforeClass
   public static void loadLocations() {    
     LocationJsonHelper jsonHelper = new LocationJsonHelper();
     try {
       FileInputStream in = new FileInputStream(locationsJsonFile);
       locations = jsonHelper.readJsonStream(in);      
     } catch (IOException e) {
       fail("Test not properly configured. IOException: "+e);      
     }    
   }
   
   /** 
    * load a file which contains all the deployments when calling the REST webservice
    */
   @BeforeClass
   public static void loadDeployments() {    
     DeploymentJsonHelper jsonHelper = new DeploymentJsonHelper();
     try {
       FileInputStream in = new FileInputStream(deploymentsJsonFile);
       deployments = jsonHelper.readJsonStream(IOUtils.toString(in,"UTF-8"));      
     } catch (IOException e) {
       fail("Test not properly configured. IOException: "+e);      
     }    
   }
   
   /**
    * token initialization. All the tests will be based on the information which
    * this token has access to 
    */
   @BeforeClass
   public static void initDexmaRestFacade() {
     /* example token we have access to*/
     dexmaRestFacade = new DexmaRestFacade("o5v19Rsc7BJ1Crla");
   }
   /** 
    * this token has 3 locations and the local file contains all them
    * @throws IOException
    */
   @Test
   public void testGetAllLocations() throws IOException {
     List<Location> remoteLocations = dexmaRestFacade.getAllLocations();     
     assertEquals(3,remoteLocations.size());
     Iterator<Location> itRemote = remoteLocations.iterator();    
     while (itRemote.hasNext()) {
       Location loc = itRemote.next();
       /*iteration over the local list. This operation is slow (O(N2)) but for test purposes
        * is more than enough
        */      
       Iterator<Location> itLocal = locations.iterator();
       boolean notFound = true;      
       while (itLocal.hasNext() && notFound) {        
         notFound = !loc.equals(itLocal.next());
       }
       assertEquals(notFound,false);
     }
   }
   
   /**
    * 10845 corresponds to SUpermercado Bilbao in this token
    * @throws IOException
    */
   @Test
   public void testGetLocation() throws IOException {
     Location loc = dexmaRestFacade.getLocation(10845L);
     assertNotNull(loc);
     assertEquals(loc.getName(),"Supermercado Bilbao");    
   }
   
   /**
    * 265 deployment is valid for this token
    * Quite similar code to testGetAllLocations() because all the locations belongs to the
    * same deployment (265) 
    * @throws IOException
    */
   @Test
   public void testGetLocationsByDeployment() throws IOException {
     List<Location> remoteLocations = dexmaRestFacade.getLocationsByDeployment(265L);
     assertEquals(3,remoteLocations.size());
     Iterator<Location> itRemote = remoteLocations.iterator();    
     while (itRemote.hasNext()) {
       Location loc = itRemote.next();
       /*iteration over the local list. This operation is slow (O(N2)) but for test purposes
        * is more than enough
        */      
       Iterator<Location> itLocal = locations.iterator();
       boolean notFound = true;      
       while (itLocal.hasNext() && notFound) {        
         notFound = !loc.equals(itLocal.next());
       }
       assertEquals(notFound,false);
     }
   }
   /** 
    * this token has 1 deployment and the local file contains it
    * @throws IOException
    */
   @Test
   public void testGetAllDeployments() throws IOException {
     List<Deployment> remoteDeployments = dexmaRestFacade.getAllDeployments();     
     assertEquals(1,remoteDeployments.size());
     Iterator<Deployment> itRemote = remoteDeployments.iterator();    
     while (itRemote.hasNext()) {
       Deployment dep = itRemote.next();
       /*iteration over the local list. This operation is slow (O(N2)) but for test purposes
        * is more than enough - just 1 deployment!-
        */      
       Iterator<Deployment> itLocal = deployments.iterator();
       boolean notFound = true;     
       
       while (itLocal.hasNext() && notFound) {        
         notFound = !dep.equals(itLocal.next());
       }
       assertEquals(notFound,false);
     }
   }
 }
