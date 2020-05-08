 package org.openengsb.loom.java;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 
 import org.hamcrest.CoreMatchers;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openengsb.connector.usernamepassword.Password;
 import org.openengsb.core.api.ConnectorManager;
 import org.openengsb.core.api.model.ConnectorDescription;
 import org.openengsb.core.api.security.service.UserDataManager;
 import org.openengsb.domain.example.ExampleDomain;
 import org.openengsb.domain.example.model.ExampleRequestModel;
 import org.openengsb.domain.example.model.ExampleResponseModel;
 import org.openengsb.loom.java.jms.JmsProtocolHandler;
 
 import com.google.common.collect.ImmutableMap;
 
 public class ConnectorManagerUT {
 
     private ProxyConnectorFactory domainFactory;
 
     private static final String baseURL = "failover:(tcp://localhost:6549)?timeout=60000";
 
     private JmsProtocolHandler jmsConfig;
 
     @Before
     public void setUp() throws Exception {
        jmsConfig = new JmsProtocolHandler(baseURL);
         domainFactory = new ProxyConnectorFactory(jmsConfig, "admin", new Password("password"));
     }
 
     @After
     public void tearDown() throws Exception {
         jmsConfig.destroy();
     }
 
     @Test
     public void retrieveServiceProxy() throws Exception {
         UserDataManager userDataManager = domainFactory.getRemoteProxy(UserDataManager.class, null);
         Collection<String> userList = userDataManager.getUserList();
         assertThat(userList, CoreMatchers.hasItems("admin", "user"));
     }
 
     @Test
     public void createConnector() throws Exception {
         ConnectorManager cm = domainFactory.getRemoteProxy(ConnectorManager.class, null);
         ConnectorDescription connectorDescription = new ConnectorDescription("example", "example",
             ImmutableMap.of("prefix", "<><>", "level", "info"),
             new HashMap<String, Object>());
         cm.create(connectorDescription);
     }
 
     @Test
     public void createConnectorProxyWithModel() throws Exception {
         ExampleDomain handler = new ExampleConnector();
         String uuid = domainFactory.createConnector("example");
         domainFactory.registerConnector(uuid, handler);
         final ExampleDomain self = domainFactory.getRemoteProxy(ExampleDomain.class, uuid);
         ExampleRequestModel modelObject = new ExampleRequestModel();
         modelObject.setId(42);
         modelObject.setName("foo");
         ExampleResponseModel result = self.doSomethingWithModel(modelObject);
         domainFactory.unregisterConnector(uuid);
         assertThat(result.getResult(), is("foo"));
         Callable<String> task = new Callable<String>() {
             @Override
             public String call() throws Exception {
                 return self.doSomethingWithMessage("stuff");
             }
         };
         FutureTask<String> futureTask = new FutureTask<String>(task);
         Thread thread = new Thread(futureTask);
         thread.start();
         thread.join();
         try {
             futureTask.get();
             fail("expected Execution Exception, because the connector should have been unregistered");
         } catch (ExecutionException e) {
             // expected
         }
         domainFactory.deleteConnector(uuid);
     }
 
     @Test
     public void createConnectorProxy() throws Exception {
         ExampleDomain handler = new ExampleConnector();
         String uuid = domainFactory.createConnector("example");
         domainFactory.registerConnector(uuid, handler);
         final ExampleDomain self = domainFactory.getRemoteProxy(ExampleDomain.class, uuid);
         assertThat(self.doSomethingWithMessage("asdf"), is("42"));
         domainFactory.unregisterConnector(uuid);
         try {
             self.doSomethingWithMessage("stuff");
             fail("expected Execution Exception, because the connector should have been unregistered");
         } catch (RemoteException e) {
             // expected
         }
         domainFactory.deleteConnector(uuid);
     }
 
     @Test
     public void callNotRegisteredConnectorProxy_shouldFail() throws Exception {
         String uuid = domainFactory.createConnector("example");
         final ExampleDomain self = domainFactory.getRemoteProxy(ExampleDomain.class, uuid);
         try {
             self.doSomethingWithMessage("stuff");
             fail("expected Execution Exception, because the connector should have been unregistered");
         } catch (RemoteException e) {
             // expected
         }
         domainFactory.deleteConnector(uuid);
     }
 
 }
