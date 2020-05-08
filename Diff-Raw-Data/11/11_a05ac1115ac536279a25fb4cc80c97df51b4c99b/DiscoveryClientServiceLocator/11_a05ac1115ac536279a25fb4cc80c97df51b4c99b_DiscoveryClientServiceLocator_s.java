 
 package edu.duke.cabig.catrip.gui.discovery;
 
 import edu.duke.cabig.catrip.gui.common.ServiceMetaDataBean;
 import edu.duke.cabig.catrip.gui.config.GUIConfigurationBean;
 import edu.duke.cabig.catrip.gui.config.GUIConfigurationLoader;
 import edu.duke.cabig.catrip.gui.util.GUIConstants;
 import edu.duke.cabig.catrip.gui.util.SwingUtils;
import gov.nih.nci.cagrid.discovery.MetadataUtils;
 import gov.nih.nci.cagrid.discovery.client.DiscoveryClient;
 import gov.nih.nci.cagrid.metadata.ServiceMetadata;
 import gov.nih.nci.cagrid.metadata.common.PointOfContact;
 import gov.nih.nci.cagrid.metadata.common.ResearchCenter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.PropertyResourceBundle;
 import org.apache.axis.message.addressing.EndpointReferenceType;
 
 /**
  * This class is a subclass of ServiceLocator which uses Discovery APIs to locate the Service Metadata and the Domain Model.
  *
  * @author Sanjeev Agarwal
  */
 public class DiscoveryClientServiceLocator extends ServiceLocator{
     
     /** Creates a new instance of DiscoveryClientServiceLocator */
     public DiscoveryClientServiceLocator() {
     }
     
     /**
      * Discover all services registered into Index endpointReference. The endpointReference list is generated using Discovery APIs.
      */
     public ArrayList<ServiceMetaDataBean> discoverServices() {
         ArrayList<ServiceMetaDataBean> serviceList = new ArrayList(50);
         
         DiscoveryClient client = null;
         EndpointReferenceType[] allServices = null;
         
         GUIConfigurationBean guiConfiguration = GUIConfigurationLoader.getGUIConfiguration();
         String indexServiceUrl = guiConfiguration.getIndexServiceUrl();
         String indexServiceName = "Default Index Service";
         
         try {
             client = new DiscoveryClient(GUIConstants.DEFAULT_INDEX_SERVICE_URL);
             
             if (indexServiceUrl != null){
                 client = new DiscoveryClient(indexServiceUrl);
                 indexServiceName = guiConfiguration.getIndexServiceName();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         System.out.println("Searching for the services in the: "+indexServiceName+", Url: "+client.getIndexEPR().getAddress());
         try {
             allServices = client.getAllServices(true);
         } catch (Exception e1) {
             System.out.println("Exception in looking up the services in Index Service.");
             e1.printStackTrace();
         }
         
         if (allServices != null) {
             for (int ii = 0; ii < allServices.length; ii++) {
                 EndpointReferenceType endpointReference = allServices[ii];
                 System.out.println("\nService End Point Address :" + endpointReference.getAddress());
                 try {
                     ServiceMetadata serviceMetadata = MetadataUtils.getServiceMetadata(endpointReference);
                     if (serviceMetadata != null) {
                         
                         ServiceMetaDataBean serviceMetaDataBean = new ServiceMetaDataBean();
                         
                         serviceMetaDataBean.setDomainModelEndPointRef(endpointReference);
                         serviceMetaDataBean.setServiceName(serviceMetadata.getServiceDescription().getService().getName());
                         System.out.println("Service Name:"+serviceMetaDataBean.getServiceName()+"\n\n");
                         serviceMetaDataBean.setDescription(serviceMetadata.getServiceDescription().getService().getDescription());
                         
                         serviceMetaDataBean.setServiceUrl(endpointReference.getAddress().toString());
                         serviceMetaDataBean.setIcon(SwingUtils.getTextAsRandomColorImage(serviceMetaDataBean.getServiceName().trim()));
                         
                         try {
                             PointOfContact pointOfContact = serviceMetadata.getServiceDescription().getService().getPointOfContactCollection().getPointOfContact(0);
                             serviceMetaDataBean.setPointOfContact(pointOfContact.getFirstName()+" "+pointOfContact.getLastName()+":"+pointOfContact.getEmail()+":"+pointOfContact.getRole());
                         } catch (NullPointerException np){
                             np.printStackTrace();
                             serviceMetaDataBean.setPointOfContact("No Point of Contact found in the Metadata.");
                         }
                         
                         try {
                             ResearchCenter researchCenter = serviceMetadata.getHostingResearchCenter().getResearchCenter();
                             serviceMetaDataBean.setHostingResearchCenter(researchCenter.getDisplayName() + "("+researchCenter.getShortName()+")"+":"+serviceMetaDataBean.getPointOfContact());
                         } catch (NullPointerException np){
                             np.printStackTrace();
                             serviceMetaDataBean.setHostingResearchCenter("No Hosting Research Center found in the Metadata.");
                         }
                         
                         
                         
                         
                         // impl stuff..
 //
                         // TODO - remove this later.  only for the demo..
                         try {
                             PropertyResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(guiConfiguration.getConfigRootLocation() + File.separator +"metadataMappings.properties"));
                             String needImpl = bundle.getString(serviceMetaDataBean.getServiceName());
 //                            System.out.println("################### Need impl for service: "+serviceMetaDataBean.getServiceName()+":"+needImpl);
 //                            boolean hasCaTissueCore =  endpointReference.getAddress().toString().endsWith("CaTissueCore_Full");
 //                            boolean hasCaTissueCore_ = endpointReference.getAddress().toString().contentEquals("CaTissueCore");
 //                            if (hasCaTissueCore || hasCaTissueCore_){
 //                                serviceMetaDataBean.needImpl(true);
 //                            }
                             if ((needImpl != null) && (Boolean.valueOf(needImpl))){
                                 serviceMetaDataBean.needImpl(true);
                             }
                         } catch (Exception e){
 //                            e.printStackTrace();
                         }
                         // only for the demo..
                         // impl stuff..
                         
                         
                         
                         
                         serviceList.add(serviceMetaDataBean);
                         
                     }
                     
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         } else {
             System.out.println("No services found.");
         }
         
         return serviceList;
     }
     
 }
