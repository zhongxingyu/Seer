 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ooici.eoi.netcdf.test;
 
 import com.rabbitmq.client.AMQP;
 import ion.core.messaging.MsgBrokerClient;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import net.ooici.netcdf.iosp.messaging.AttributeStore;
 
 /**
  *
  * @author cmueller
  * @deprecated This class was for early prototype testing and is no longer functional
  */
 @Deprecated
 public class ClearStore {
 
     public ClearStore(String props) {
         try {
             java.util.Properties ooiciProps = new java.util.Properties();
             ooiciProps.load(new java.io.FileReader(props));
 
             /* Setup broker */
             MsgBrokerClient brokercl = new MsgBrokerClient(ooiciProps.getProperty("server", "localhost"), AMQP.PROTOCOL.PORT, ooiciProps.getProperty("exchange", "magnet.topic"));
             brokercl.attach();
             /* make attstore */
             AttributeStore as = new AttributeStore(ooiciProps.getProperty("sysname", "localhost"), brokercl);
             /* clear the store */
             as.clearStore();
 
             /* Detatch the broker */
             brokercl.detach();
 
         } catch (IOException ex) {
             Logger.getLogger(AddDatasetToStore.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public static void main(String[] args) {
         String props = null;
         if (args.length < 1) {
             System.out.println("Not enough args - requires 1 argument:\n\t1 = path to config file (i.e. ooici.properties)\n\toptional argument = url to netcdf dataset (local or remote)");
             System.exit(-1);
         }
         props = args[0];
         if (props != null) {
             new ClearStore(props);
         }
     }
 }
