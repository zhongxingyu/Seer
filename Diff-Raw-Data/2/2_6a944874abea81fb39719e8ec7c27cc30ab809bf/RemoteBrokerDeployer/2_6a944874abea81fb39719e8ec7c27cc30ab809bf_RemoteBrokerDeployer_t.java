 package org.wso2.carbon.cep.wihidum.core.broker;
 
 
 import org.apache.axis2.AxisFault;
 import org.apache.axis2.client.Options;
 import org.apache.axis2.client.ServiceClient;
 import org.apache.axis2.context.ServiceContext;
 import org.apache.axis2.transport.http.HTTPConstants;
 import org.apache.log4j.Logger;
 import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
 import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
 import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
 import org.wso2.carbon.brokermanager.core.*;
 import org.wso2.carbon.brokermanager.core.BrokerConfiguration;
 import org.wso2.carbon.brokermanager.stub.BrokerManagerAdminServiceBrokerManagerAdminServiceExceptionException;
 import org.wso2.carbon.brokermanager.stub.BrokerManagerAdminServiceStub;
 import org.wso2.carbon.brokermanager.stub.types.BrokerProperty;
 import org.wso2.carbon.cep.wihidum.core.cluster.ClusterManager;
 import org.wso2.carbon.cep.wihidum.core.cluster.Constants;
 import org.wso2.carbon.cep.wihidum.core.internal.WihidumCoreValueHolder;
 
 import java.rmi.RemoteException;
 
 public class RemoteBrokerDeployer {
 
     private static RemoteBrokerDeployer remoteBrokerDeployer;
     private static final String ADMIN_SERVICE = "AuthenticationAdmin";
     private static Logger logger = Logger.getLogger(RemoteBrokerDeployer.class);
 
     private RemoteBrokerDeployer() {
 
     }
 
     public static RemoteBrokerDeployer getInstance() {
         if (remoteBrokerDeployer == null) {
             remoteBrokerDeployer = new RemoteBrokerDeployer();
             return remoteBrokerDeployer;
         } else {
             return remoteBrokerDeployer;
         }
     }
 
     public void deploy(String brokerName, String ipAddress) {
         String adminCookie;
         String authenticationAdminURL;
         String brokerManagerAdminServiceURL;
         String serviceURL = "https://" + ipAddress + ":" + ProductConstants.HTTPS_PORT + "/services/";
         authenticationAdminURL = serviceURL + ADMIN_SERVICE;
         brokerManagerAdminServiceURL = serviceURL;
         Options option;
         try {
             BrokerProperty brokerProperty[] = getBrokerConfiguration(brokerName);
             if(brokerProperty == null){
                 throw new RuntimeException("could not fetch given broker configuration");
             }
             AuthenticationAdminStub authenticationAdminStub = new AuthenticationAdminStub(authenticationAdminURL);
             ServiceClient client = authenticationAdminStub._getServiceClient();
             Options options = client.getOptions();
             options.setManageSession(true);
 
             System.setProperty(ProductConstants.TRUSTSTORE, ProductConstants.CLIENT_TRUST_STORE_PATH);
             System.setProperty(ProductConstants.TRUSTSTORE_PASSWORD, ProductConstants.KEY_STORE_PASSWORD);
             System.setProperty(ProductConstants.TRUSTSTORE_TYPE, ProductConstants.KEY_STORE_TYPE);
 
             authenticationAdminStub.login(ProductConstants.USER_NAME, ProductConstants.PASSWORD, ipAddress);
             ServiceContext serviceContext = authenticationAdminStub.
                     _getServiceClient().getLastOperationContext().getServiceContext();
             adminCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
             if (adminCookie == null) {
                 throw new RuntimeException("could not login to the back-end server");
             }
             BrokerManagerAdminServiceStub brokerManagerAdminServiceStub = new BrokerManagerAdminServiceStub(brokerManagerAdminServiceURL + "BrokerManagerAdminService");
             ServiceClient serviceClient = brokerManagerAdminServiceStub._getServiceClient();
             option = serviceClient.getOptions();
 
             option.setManageSession(true);
             option.setTimeOutInMilliSeconds(5 * 60 * 1000);
             option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, adminCookie);
 
            brokerManagerAdminServiceStub.addBrokerConfiguration(brokerName, "agent", brokerProperty);
             logger.info("Successfully deployed broker "+brokerName);
 
 
         } catch (AxisFault axisFault) {
             logger.error("Error when deploying remote broker configuration", axisFault);
         } catch (RemoteException e) {
             logger.error("Error when deploying remote broker configuration", e);
         } catch (LoginAuthenticationExceptionException e) {
             logger.error("Error when deploying remote broker configuration", e);
         } catch (BrokerManagerAdminServiceBrokerManagerAdminServiceExceptionException e) {
             logger.error("Error when deploying remote broker configuration", e);
         }
     }
 
     private BrokerProperty[] getBrokerConfiguration(String brokerName) {
         String adminCookie;
         String authenticationAdminURL;
         String brokerManagerAdminServiceURL;
         String serviceURL = "https://" + ClusterManager.getInstant().getLocalMemberAddress() + ":" + ProductConstants.HTTPS_PORT + "/services/";
         authenticationAdminURL = serviceURL + ADMIN_SERVICE;
         brokerManagerAdminServiceURL = serviceURL;
         Options option;
         try {
             AuthenticationAdminStub authenticationAdminStub = new AuthenticationAdminStub(authenticationAdminURL);
             ServiceClient client = authenticationAdminStub._getServiceClient();
             Options options = client.getOptions();
             options.setManageSession(true);
 
             System.setProperty(ProductConstants.TRUSTSTORE, ProductConstants.CLIENT_TRUST_STORE_PATH);
             System.setProperty(ProductConstants.TRUSTSTORE_PASSWORD, ProductConstants.KEY_STORE_PASSWORD);
             System.setProperty(ProductConstants.TRUSTSTORE_TYPE, ProductConstants.KEY_STORE_TYPE);
 
             authenticationAdminStub.login(ProductConstants.USER_NAME, ProductConstants.PASSWORD, ClusterManager.getInstant().getLocalMemberAddress());
             ServiceContext serviceContext = authenticationAdminStub.
                     _getServiceClient().getLastOperationContext().getServiceContext();
             adminCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
             if (adminCookie == null) {
                 throw new RuntimeException("could not login to the back-end server");
             }
             BrokerManagerAdminServiceStub brokerManagerAdminServiceStub = new BrokerManagerAdminServiceStub(brokerManagerAdminServiceURL + "BrokerManagerAdminService");
             ServiceClient serviceClient = brokerManagerAdminServiceStub._getServiceClient();
             option = serviceClient.getOptions();
 
             option.setManageSession(true);
             option.setTimeOutInMilliSeconds(5 * 60 * 1000);
             option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, adminCookie);
 
             BrokerProperty brokerProperty[] = brokerManagerAdminServiceStub.getBrokerConfiguration(brokerName);
             logger.info("Successfully fetched broker "+brokerName);
             authenticationAdminStub.logout();
             return brokerProperty;
         } catch (AxisFault axisFault) {
             logger.error("Error when fetching remote broker configuration", axisFault);
         } catch (RemoteException e) {
             logger.error("Error when fetching remote broker configuration", e);
         } catch (LoginAuthenticationExceptionException e) {
             logger.error("Error when fetching remote broker configuration", e);
         } catch (BrokerManagerAdminServiceBrokerManagerAdminServiceExceptionException e) {
             logger.error("Error when fetching remote broker configuration", e);
         } catch (LogoutAuthenticationExceptionException e) {
             logger.error("Error when loging out of authentication admin stub", e);
         }
 
         return null;
     }
 }
