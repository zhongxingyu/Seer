 package gov.nih.nci.cagrid.data.client;
 
 import gov.nih.nci.cagrid.data.common.DataServiceI;
 import gov.nih.nci.cagrid.data.faults.MalformedQueryExceptionType;
 import gov.nih.nci.cagrid.data.faults.QueryProcessingExceptionType;
 import gov.nih.nci.cagrid.data.stubs.DataServicePortType;
 import gov.nih.nci.cagrid.data.stubs.service.DataServiceAddressingLocator;
 import gov.nih.nci.cagrid.introduce.security.client.ServiceSecurityClient;
 
 import java.io.InputStream;
 import java.rmi.RemoteException;
 
 import javax.xml.namespace.QName;
 
 import org.apache.axis.EngineConfiguration;
 import org.apache.axis.client.AxisClient;
 import org.apache.axis.client.Stub;
 import org.apache.axis.configuration.FileProvider;
 import org.apache.axis.message.addressing.EndpointReferenceType;
 import org.apache.axis.types.URI.MalformedURIException;
 import org.cagrid.dataservice.stubs.Cql2DataServicePortType;
 import org.globus.gsi.GlobusCredential;
 import org.oasis.wsrf.properties.GetResourcePropertyResponse;
 
 
 /**
  * This class is autogenerated, DO NOT EDIT GENERATED GRID SERVICE METHODS.
  * 
  * This client is generated automatically by Introduce to provide a clean
  * unwrapped API to the service.
  * 
  * On construction the class instance will contact the remote service and
  * retrieve it's security metadata description which it will use to configure
  * the Stub specifically for each method call.
  * 
  * @created by Introduce Toolkit version 1.0
  */
 public class DataServiceClient extends ServiceSecurityClient implements DataServiceI {
     protected DataServicePortType portType;
     protected Cql2DataServicePortType cql2PortType;
     private Object portTypeMutex;
 
 
     public DataServiceClient(String url) throws MalformedURIException, RemoteException {
         this(url, null);
     }
 
 
     public DataServiceClient(String url, GlobusCredential proxy) throws MalformedURIException, RemoteException {
         super(url, proxy);
         initialize();
     }
 
 
     public DataServiceClient(EndpointReferenceType epr) throws MalformedURIException, RemoteException {
         this(epr, null);
     }
 
 
     public DataServiceClient(EndpointReferenceType epr, GlobusCredential proxy) throws MalformedURIException,
         RemoteException {
         super(epr, proxy);
         initialize();
     }
 
 
     private void initialize() throws RemoteException {
         this.portTypeMutex = new Object();
         this.portType = createPortType();
         this.cql2PortType = createCql2PortType();
     }
 
 
     private DataServicePortType createPortType() throws RemoteException {
 
         DataServiceAddressingLocator locator = new DataServiceAddressingLocator();
         // attempt to load our context sensitive wsdd file
        InputStream resourceAsStream = getClass().getResourceAsStream("client-config.wsdd");
         if (resourceAsStream != null) {
             // we found it, so tell axis to configure an engine to use it
             EngineConfiguration engineConfig = new FileProvider(resourceAsStream);
             // set the engine of the locator
             locator.setEngine(new AxisClient(engineConfig));
         }
         DataServicePortType port = null;
         try {
             port = locator.getDataServicePortTypePort(getEndpointReference());
         } catch (Exception e) {
             throw new RemoteException("Unable to locate portType:" + e.getMessage(), e);
         }
 
         return port;
     }
     
     
     private Cql2DataServicePortType createCql2PortType() throws RemoteException {
         org.cagrid.dataservice.stubs.service.DataServiceAddressingLocator cql2Locator = 
             new org.cagrid.dataservice.stubs.service.DataServiceAddressingLocator();
         // attempt to load our context sensitive wsdd file
        InputStream resourceAsStream = getClass().getResourceAsStream("client-config.wsdd");
         if (resourceAsStream != null) {
             // we found it, so tell axis to configure an engine to use it
             EngineConfiguration engineConfig = new FileProvider(resourceAsStream);
             // set the engine of the locator
             cql2Locator.setEngine(new AxisClient(engineConfig));
         }
         Cql2DataServicePortType port = null;
         try {
             port = cql2Locator.getCql2DataServicePortTypePort(getEndpointReference());
         } catch (Exception ex) {
             throw new RemoteException("Unable to locate portType:" + ex.getMessage(), ex);
         }
         
         return port;
     }
 
 
     public GetResourcePropertyResponse getResourceProperty(QName resourcePropertyQName) throws RemoteException {
         return portType.getResourceProperty(resourcePropertyQName);
     }
 
 
     public static void usage() {
         System.out.println(DataServiceClient.class.getName() + " -url <service url>");
     }
 
 
     public static void main(String[] args) {
         System.out.println("Running the Grid Service Client");
         try {
             if (!(args.length < 2)) {
                 if (args[0].equals("-url")) {
                     DataServiceClient client = new DataServiceClient(args[1]);
                     // place client calls here if you want to use this main as a
                     // test....
                 } else {
                     usage();
                     System.exit(1);
                 }
             } else {
                 usage();
                 System.exit(1);
             }
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(1);
         }
     }
 
 
     /**
      * @deprecated As of caGrid 1.4, CQL 2 is the preferred query language.  http://cagrid.org/display/dataservices/CQL+2
      * Use {@link #executeQuery(org.cagrid.cql2.CQLQuery)}
      */
     @Deprecated
     public gov.nih.nci.cagrid.cqlresultset.CQLQueryResults query(gov.nih.nci.cagrid.cqlquery.CQLQuery cqlQuery)
         throws RemoteException, MalformedQueryExceptionType, QueryProcessingExceptionType {
         synchronized (portTypeMutex) {
             configureStubSecurity((Stub) portType, "query");
             gov.nih.nci.cagrid.data.stubs.QueryRequest params = new gov.nih.nci.cagrid.data.stubs.QueryRequest();
             gov.nih.nci.cagrid.data.stubs.QueryRequestCqlQuery cqlQueryContainer = new gov.nih.nci.cagrid.data.stubs.QueryRequestCqlQuery();
             cqlQueryContainer.setCQLQuery(cqlQuery);
             params.setCqlQuery(cqlQueryContainer);
             gov.nih.nci.cagrid.data.stubs.QueryResponse boxedResult = portType.query(params);
             return boxedResult.getCQLQueryResultCollection();
         }
     }
     
     
     public org.cagrid.cql2.results.CQLQueryResults executeQuery(org.cagrid.cql2.CQLQuery query)
         throws RemoteException, MalformedQueryExceptionType, QueryProcessingExceptionType {
         synchronized (portTypeMutex) {
             configureStubSecurity((Stub) cql2PortType, "executeQuery");
             org.cagrid.dataservice.stubs.ExecuteQueryRequest params = new org.cagrid.dataservice.stubs.ExecuteQueryRequest();
             org.cagrid.dataservice.stubs.ExecuteQueryRequestQuery queryContainer = new org.cagrid.dataservice.stubs.ExecuteQueryRequestQuery();
             queryContainer.setCQLQuery(query);
             params.setQuery(queryContainer);
             org.cagrid.dataservice.stubs.ExecuteQueryResponse boxedResult = cql2PortType.executeQuery(params);
             return boxedResult.getCQLQueryResults();
         }
     }
 
 
     public gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata getServiceSecurityMetadata()
         throws RemoteException {
         synchronized (portTypeMutex) {
             configureStubSecurity((Stub) portType, "getServiceSecurityMetadata");
             gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataRequest params = new gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataRequest();
             gov.nih.nci.cagrid.introduce.security.stubs.GetServiceSecurityMetadataResponse boxedResult = portType
                 .getServiceSecurityMetadata(params);
             return boxedResult.getServiceSecurityMetadata();
         }
     }
 
 }
