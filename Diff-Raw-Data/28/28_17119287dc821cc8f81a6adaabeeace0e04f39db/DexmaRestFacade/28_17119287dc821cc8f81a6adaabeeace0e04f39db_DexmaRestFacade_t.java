 package jvg.dexma;
 
 import java.util.List;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.commons.io.IOUtils;
 
 
 /** Facade implementing all the http/json dexma v2 API
  * http://support.dexmatech.com/customer/portal/articles/713475-http-json-api-v2
  *
  */
 
 public class DexmaRestFacade implements DexmaRestI {
   
   //token for authenticate the API calls
   private String token;
   
   //parsing the JSON for location calls
   private LocationJsonHelper locationJsonHelper;
   private DeploymentJsonHelper deploymentJsonHelper;
   
   private final String dexmaApiURL = "http://api.dexcell.com/";
   
   public DexmaRestFacade (String token) {
     this.token = token;
     locationJsonHelper = new LocationJsonHelper();
     deploymentJsonHelper = new DeploymentJsonHelper();
   }  
   
   public String doGetRequest (String operation) {    
     try {      
       URL url = new URL(dexmaApiURL+operation);
       HttpURLConnection conn = (HttpURLConnection) url.openConnection();
       conn.setRequestMethod("GET");
       conn.setRequestProperty("Accept", "application/json");
       conn.addRequestProperty("x-dexcell-token", token);
    
       if (conn.getResponseCode() != 200) {
         throw new RuntimeException("Failed : HTTP error code : "
             + conn.getResponseCode());
       }     
        
       String response = IOUtils.toString(conn.getInputStream(), "UTF-8");
       //DEBUG
       //System.out.println("Response for operation "+operation+" : "+response);
       conn.disconnect();      
       return response;
    
       } catch (MalformedURLException e) {
         //Shouldn't get here!   
         e.printStackTrace();
         throw new RuntimeException("Review code at operation: '"+operation+"'");
         
       } catch (IOException e) {
         e.printStackTrace();
         throw new RuntimeException("IOException: '"+e.getMessage()+"'");
       }   
   }  
 
   @Override
   public List<Deployment> getAllDeployments() throws IOException {
     String operation = "deployments.json";
     return deploymentJsonHelper.readJsonStream(doGetRequest(operation));
   }
 
   @Override
  public Deployment getDeployment(Long id) throws IOException {
    String operation = "deployments/"+id+".json";
    return deploymentJsonHelper.readDeployment(doGetRequest(operation));    
   }
 
   @Override
   public List<Device> getAllDevices() {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public Device getDevice(Long id) {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public List<Device> getDevicesByLocation(Long locationId) {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public List<Location> getAllLocations() throws IOException {
     String operation = "locations.json";
     return locationJsonHelper.readJsonStream(doGetRequest(operation));    
   }
 
   @Override
   public Location getLocation(Long id) throws IOException {
     String operation = "locations/"+id+".json";
     return locationJsonHelper.readLocation(doGetRequest(operation));    
   }
 
   @Override
   public List<Location> getLocationsByDeployment(Long deploymentId) throws IOException {
     String operation ="deployments/"+deploymentId+"/locations.json";
     // TODO Auto-generated method stub
     return locationJsonHelper.readJsonStream(doGetRequest(operation));    
   }
 
   @Override
   public List<Readings> getReadings(Long deviceId, Long parameterId) {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public Cost getCost(Long deviceId, EnergyType energyType) {
     // TODO Auto-generated method stub
     return null;
   }
 
 }
 ;
