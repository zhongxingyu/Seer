 package org.gethydrated.hydra.test.cli;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.PrintStream;
 
 import org.gethydrated.hydra.api.HydraException;
 import org.gethydrated.hydra.api.configuration.ConfigurationGetter;
 import org.gethydrated.hydra.api.configuration.ConfigurationSetter;
 import org.gethydrated.hydra.api.platform.Platform;
 import org.gethydrated.hydra.api.service.Service;
 import org.gethydrated.hydra.api.service.ServiceContext;
 import org.gethydrated.hydra.core.config.Configuration;
 
 /**
  * 
  * @author Hanno Sternberg
  *
  */
 public class CLITestContext implements ServiceContext {
         
         /**
          * 
          */
         private PrintStream ps;
         
         /**
          * 
          */
         private ByteArrayOutputStream output;
         
         /**
          * 
          */
         private Configuration testConfig;
         
         /**
          * 
          * @return OutputStream.
          */
         public final String getOutput() {
                 return output.toString();
         }
         
         /**
          * 
          */
         public CLITestContext() {
                 output = new ByteArrayOutputStream();
                 ps = new PrintStream(output);
                 testConfig = new Configuration();
                 testConfig.setString("Name", "Test");
                 testConfig.setInteger("Network.Port", 1337);
                 testConfig.setString("Network.Host", "local");
         }
 
         @Override
        public void registerLocal(String name, Object obj) {
                 // TODO Auto-generated method stub
                 
         }
 
         @Override
        public void registerGlobal(String name, Object obj) {
                 // TODO Auto-generated method stub
                 
         }
 
         @Override
        public void getLocalService(String name) {
                 // TODO Auto-generated method stub
                 
         }
 
         @Override
        public void getGlobalService(String name) {
                 // TODO Auto-generated method stub
                 
         }
 
         @Override
         public void startService() throws HydraException {
                 // TODO Auto-generated method stub
                 
         }
 
         @Override
         public void stopService() throws HydraException {
                 // TODO Auto-generated method stub
                 
         }
 
         @Override
         public final Platform getPlatform() {
                 // TODO Auto-generated method stub
                 return null;
         }
 
         @Override
         public final Service getService() {
                 // TODO Auto-generated method stub
                 return null;
         }
 
         @Override
         public final PrintStream getOutputStream() {
                 return ps;
         }
 
         @Override
         public final InputStream getInputStream() {
                 return System.in;
         }
 
         @Override
         public final ConfigurationGetter getConfigurationGetter() {
                 // TODO Auto-generated method stub
                 return testConfig;
         }
 
         @Override
         public final ConfigurationSetter getConfigurationSetter() {
                 // TODO Auto-generated method stub
                 return testConfig;
         }
 }
