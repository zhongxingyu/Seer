 package dbc.opensearch.tools.tests;
 /** \brief UnitTest for FedoraHandler */
 
 import dbc.opensearch.tools.FedoraHandler;
 import dbc.opensearch.components.datadock.CargoContainer;
 
 import org.junit.*;
 //import static org.easymock.EasyMock.*;
 import static org.easymock.classextension.EasyMock.*;
 
 import org.apache.axis.types.NonNegativeInteger;
 
 import fedora.client.FedoraClient;
 import fedora.common.Constants;
 import fedora.server.access.FedoraAPIA;
 import fedora.server.management.FedoraAPIM;
 import fedora.server.types.gen.DatastreamDef;
 import fedora.server.types.gen.MIMETypedStream;
 
 import java.net.MalformedURLException;
 import javax.xml.rpc.ServiceException;
 import java.io.IOException;
 import org.apache.commons.configuration.ConfigurationException;
 
 public class FedoraHandlerTest {
    
     @Test public void constructorTest() throws ConfigurationException, IOException, MalformedURLException, ServiceException { 
         
         /**1*/
         FedoraClient mockFedoraClient = createMock( FedoraClient.class );
         FedoraAPIA mockFedoraAPIA = createMock( FedoraAPIA.class );
         FedoraAPIM mockFedoraAPIM = createMock( FedoraAPIM.class );
         
         /**2*/
         /** Use dependency injection here
            expect( FedoraClient client = new FedoraClient( isA(String.class), isA(String.class), isA(String.class) ) );*/
         expect( mockFedoraClient.getAPIA() ).andReturn( mockFedoraAPIA );
         expect( mockFedoraClient.getAPIM() ).andReturn( mockFedoraAPIM );
         
         /**3*/
         replay( mockFedoraClient );
         //replay( mockFedoraAPIA );
         //replay( mockFedoraAPIM );
         FedoraHandler fh = new FedoraHandler(mockFedoraClient); 
      
         
         /**4*/  
         verify( mockFedoraClient );
         //verify( mockFedoraAPIA );
         //verify( mockFedoraAPIM );
         
     }
     /*
       public void submitDatastreamTest(){
       
       String[] mockPids = createMock( String[].class );
       expect( mockFedoraAPIM.getNextPID( isA( NonNegativeInteger.class ), isA( String.class ))).andReturn( mockPids );   
       }*/
     
     //public void getDatastreamTest(){}
     
 }
