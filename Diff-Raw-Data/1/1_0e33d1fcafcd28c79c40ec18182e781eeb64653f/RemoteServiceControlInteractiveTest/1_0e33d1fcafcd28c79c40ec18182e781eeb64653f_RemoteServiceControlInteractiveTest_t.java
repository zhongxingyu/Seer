 package org.ccci.windows.management;
 
 import java.io.IOException;
 
 import org.ccci.util.ConsoleUtil;
 import org.jinterop.dcom.common.JIDefaultAuthInfoImpl;
 import org.jinterop.dcom.common.JIException;
 
 
 public class RemoteServiceControlInteractiveTest
 {
 
     RemoteServiceControl remoteServiceControl;
     
     public void restartService() throws JIException, IOException, InterruptedException
     {
         String password = ConsoleUtil.readPasswordFromInput();
 
         String serviceName = "Tomcat - Staff Services - Siebel";
         String hostName = "hart-a321.net.ccci.org";
         String domain = "NET";
         String username = "phillip.drees";
         JIDefaultAuthInfoImpl credential = new JIDefaultAuthInfoImpl(domain, username, password);
         remoteServiceControl = new RemoteServiceControl(hostName, credential, serviceName);
 
         remoteServiceControl.startOrRestartService();
         remoteServiceControl.close();
     }
     
     public static void main(String... args) throws JIException, IOException, InterruptedException
     {
         new RemoteServiceControlInteractiveTest().restartService();
     }
 
 }
