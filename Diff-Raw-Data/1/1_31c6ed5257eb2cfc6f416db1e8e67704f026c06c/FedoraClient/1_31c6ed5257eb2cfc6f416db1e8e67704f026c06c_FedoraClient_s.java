 package org.fcrepo.client;
 
 import org.apache.axis.AxisFault;
 import org.fcrepo.server.access.FedoraAPIA;
 import org.fcrepo.server.management.FedoraAPIM;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import java.io.File;
 import java.io.InputStream;
 import java.rmi.RemoteException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: 10/08/11
  * Time: 15:03
  * To change this template use File | Settings | File Templates.
  */
 public class FedoraClient {
     private FedoraAPIA APIA;
     private FedoraAPIM APIM;
 
     public FedoraClient(String baseURL, String username, String password) throws AxisFault{
         //To change body of created methods use File | Settings | File Templates.
         APIA = new FedoraAPIA();
         APIM = new FedoraAPIM();
     }
 
     public FedoraAPIA getAPIA() {
         return APIA;
     }
 
     public String getResponseAsString(String s, boolean b, boolean b1) {
         throw new UnsupportedOperationException("This fedora client method have not been implemented yet");
     }
 
     public FedoraAPIM getAPIM() {
         return APIM;
     }
 
     public String uploadFile(File file) throws RemoteException{
         throw new UnsupportedOperationException("This fedora client method have not been implemented yet");
     }
 
     public InputStream get(String fedoraUrl, boolean b) throws RemoteException{
         throw new UnsupportedOperationException("This fedora client method have not been implemented yet");
     }
 }
