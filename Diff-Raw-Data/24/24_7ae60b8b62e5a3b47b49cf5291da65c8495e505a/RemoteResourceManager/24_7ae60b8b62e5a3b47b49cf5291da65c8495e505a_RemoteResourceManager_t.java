 package org.geworkbench.builtin.projects.remoteresources;
 
 import java.io.*;
 import java.util.ArrayList;
 import org.apache.axis.client.Service;
 import java.net.URL;
 import org.apache.axis.client.Call;
 import javax.xml.namespace.QName;
 import edu.columbia.stubs.CaARRAYIndexService.service.*;
 import edu.columbia.stubs.CaARRAYIndexService.CaARRAYIndexPortType;
 
 /**
  * <p>Title: </p>
  *
  * <p>Description: </p>
  *
  * <p>Copyright: Copyright (c) 2005</p>
  *
  * <p>Company: </p>
  *
  * @author not attributable
  * @version 1.0
  */
 public class RemoteResourceManager {
     private ArrayList<RemoteResource> existedResources;
     private static final String DEFAULTRESOURCEFILE = "defaultResources.csv";
     private String filename;
     private String cloumnseparator = ",";
 
     public RemoteResourceManager() {
         existedResources = new ArrayList<RemoteResource>();
         String propertyfilename = System.getProperty("remotepropertyfile");
         propertyfilename = System.getProperty("temporary.files.directory") +
                            File.separator + DEFAULTRESOURCEFILE;
         filename = new File(propertyfilename).getAbsolutePath();
         if (filename != null && new File(filename).canRead()) {
 
             init(new File(filename));
 
             //changed for bug 395
 //            deleteRemoteResource("caARRAY");
 //            init();
         } else {
             init();
         }
 
     }
 
     /**
      * A default setup when no property file is found.
      * init
      */
     protected void init() {
         RemoteResource rr = new RemoteResource("caARRAY",
                                                "caarray-mageom-server.nci.nih.gov",
                                                "8080", "http:",
                                                "PUBLIC", "");
 
 //        # caARRAY username/password
 //caarray.mage.user=KustagiM
 //caarray.mage.password=Tbf38!a
 
         existedResources.add(rr);
     }
 
     /**
      * Init the existed resources from a file.
      * @param propertyfilename File
      */
     private void init(File propertyfilename) {
         try {
 
             InputStream input = new FileInputStream(propertyfilename);
             BufferedReader br = new BufferedReader(new InputStreamReader(input));
             String line = null;
 
             while ((line = br.readLine()) != null) {
 
                 String[] cols = line.split(",");
                 if (cols.length > 0) {
                     RemoteResource rr = RemoteResource.createNewInstance(cols);
                     if (rr != null) {
                         existedResources.add(rr);
 
                     }
                 }
             }
             br.close();
             input.close();
         } catch (Exception ex) {
             ex.printStackTrace();
 
         }
 
     }
 
     /**
      * Init the existed resources from a Index service.
      * @param url name
      */
     protected boolean init(String urlname) {
         try {
 //            urlname =  "http://adparacel.cu-genome.org/axis/servlet/AxisServlet";
 //            Service service = new Service();
 //
 //            Call call = (Call) service.createCall();
 //
 //            call.setTargetEndpointAddress(new URL(urlname));
 //            call.setOperationName(new QName("urn:downloadfileService",
 //                                            "getServerInfo")); //This is the target services method to invoke.
 //
 //            call.addParameter("testParam", org.apache.axis.Constants.XSD_STRING,
 //                              javax.xml.rpc.ParameterMode.IN);
 //
 //            call.setReturnType(org.apache.axis.Constants.XSD_STRING);
             URL GSH = new java.net.URL(urlname);
 
             String cmd = "caARRAY";
             //Object result = call.invoke(new Object[] {cmd});
            CaARRAYIndexServiceGridLocator caArrayServiceLocator = new
                    CaARRAYIndexServiceGridLocator();
 
            CaARRAYIndexPortType caARRAYPortType = caArrayServiceLocator.
                    getCaARRAYIndexPort(GSH); //getsequenceAlignmentPort(GSH);// getSequenceAlignmentPort(GSH);
 
            String test = (String) caARRAYPortType.getServer(cmd);
 
             if (test == null) {
                 return false;
             }
             removeIndexResources();
 
             String[] lists = test.split("!");
             if (lists != null) {
                 for (String s : lists) {
                     String[] cols = s.split(",");
                     if (cols != null && cols.length > 0) {
                         RemoteResource rr = RemoteResource.createNewInstance(
                                 cols);
                         if (rr != null) {
                             rr.setEditable(false);
                             existedResources.add(rr);
 
                         }
                     }
 
                 }
             }
             return true;
 
         } catch (Exception e) {
              System.out.println(e + "RemoteResourceManager.init" + urlname);
              e.printStackTrace();
         }
         return false;
 
     }
 
     public void removeIndexResources() {
         int size = existedResources.size();
         ArrayList<RemoteResource>
                 newExistedResources = new ArrayList<RemoteResource>();
         boolean[] removeIndex = new boolean[size];
         for (int i = 0; i < existedResources.size(); i++) {
             RemoteResource rr = existedResources.get(i);
             removeIndex[i] = rr.isEditable();
             if (rr.isEditable()) {
                 newExistedResources.add(rr);
             }
 
         }
         existedResources = newExistedResources;
     }
 
     /**
      * getFristItem
      */
     public String getFristItemName() {
         if (existedResources != null && existedResources.size() > 0) {
             return existedResources.get(0).getShortname();
         }
         return null;
 
     }
 
     public String[] getItems() {
         int size = existedResources.size();
         String[] shortnames = new String[size];
         for (int i = 0; i < size; i++) {
             shortnames[i] = existedResources.get(i).getShortname();
         }
         return shortnames;
     }
 
     public RemoteResource getSelectedResouceByName(String name) {
         for (RemoteResource rr : existedResources) {
             if (rr.getShortname().equals(name)) {
                 return rr;
             }
         }
         return null;
 
     }
 
     /**
      * Edit the properties of a romoteResource
      */
     public void editRemoteResource(int i, RemoteResource rResource) {
         //existedResources.remove(i);
         RemoteResource rr = existedResources.get(i);
         rr.update(rResource);
     }
 
     /**
      * Delete  one resource object
      * @param rResource RemoteResource
      * @return boolean
      */
     public boolean deleteRemoteResource(RemoteResource rResource) {
         if(existedResources.remove(rResource)){
             saveToFile();
             return true;
         }
         return false;
     }
 
     /**
      * Delete one resource based on its index position.
      * @param rResourceIndex int
      * @return boolean
      */
     public boolean deleteRemoteResource(int rResourceIndex) {
         if (existedResources.remove(rResourceIndex) != null) {
             saveToFile();
             return true;
         }
         return false;
     }
 
     /**
      * Delete one resource based on its shortname.
      * @param name String
      * @return boolean
      */
     public boolean deleteRemoteResource(String name) {
         if (getSelectedResouceByName(name) != null) {
             existedResources.remove(getSelectedResouceByName(name));
         }
         return false;
     }
 
 
     /**
      * Add one new resource.
      * @param newResource RemoteResource
      * @return boolean
      */
     public boolean addRemoteResource(RemoteResource newResource) {
         if (existedResources.contains(newResource)) {
             deleteRemoteResource(newResource);
         }
         return existedResources.add(newResource);
     }
 
     /**
      * saveToFile
      */
     public void saveToFile() {
         try {
             BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
                     filename)));
             String line = null;
             if (existedResources.size() == 0) {
 
                 return;
             }
 
             for (RemoteResource s : existedResources) {
 
                 writer.write(s.getShortname()
                              + cloumnseparator + s.getUri()
                              + cloumnseparator + s.getPortnumber()
                              + cloumnseparator + s.getConnectProtocal()
                              + cloumnseparator + s.getUsername()
                              + cloumnseparator + s.getPassword()
                              + cloumnseparator + s.isEditable());
                 writer.newLine();
             }
 
             writer.flush();
             writer.close();
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
 
     }
 
 }
