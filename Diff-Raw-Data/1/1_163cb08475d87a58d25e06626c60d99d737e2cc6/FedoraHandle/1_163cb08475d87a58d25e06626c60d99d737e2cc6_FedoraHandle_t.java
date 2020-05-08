 /**
  *
  */
 package dk.dbc.opensearch.common.fedora;
 
 import dk.dbc.opensearch.common.config.FedoraConfig;
 
 import fedora.client.FedoraClient;
 import fedora.server.management.FedoraAPIM;
 import fedora.server.access.FedoraAPIA;
 
 import javax.xml.rpc.ServiceException;
 
 /**
  * FedoraStore act as the plugin communication link with the fedora base. The
  * only function of this (abstract) class is to establish the SOAP communication
  * layer.
  */
 public abstract class FedoraHandle
 {
     // private FedoraAPIMServiceLocator m_locator;
     // private FedoraAPIAServiceLocator a_locator;
 
     protected FedoraAPIM fem;
     protected FedoraAPIA fea;
 
 
     /**
      * The constructor handles the initiation of the connection with the
      * fedora base
      *
      * @throws ServiceException
      */
     public FedoraHandle() throws ServiceException, java.net.MalformedURLException, java.io.IOException
     {
         String fedora_base_url;
 
         String host = FedoraConfig.getFedoraHost();
         String port = FedoraConfig.getFedoraPort();
         String user = FedoraConfig.getFedoraUser();
         String pass = FedoraConfig.getFedoraPassPhrase();
 
         fedora_base_url  = String.format( "http://%s:%s/fedora", host, port );
 
         FedoraClient fc = new FedoraClient( fedora_base_url, user, pass );
         fea = fc.getAPIA();
         fem = fc.getAPIM();
 
     }
 }
