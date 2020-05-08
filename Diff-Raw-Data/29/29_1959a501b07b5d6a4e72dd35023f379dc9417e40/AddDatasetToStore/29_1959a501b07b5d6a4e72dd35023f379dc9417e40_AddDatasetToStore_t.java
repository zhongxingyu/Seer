 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ooici.eoi.netcdf.test;
 
 import com.rabbitmq.client.AMQP;
 import ion.core.messaging.MsgBrokerClient;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import net.ooici.netcdf.iosp.messaging.AttributeStore;
import net.ooici.netcdf.iosp.*;
 import ucar.ma2.InvalidRangeException;
 import ucar.nc2.dataset.NetcdfDataset;
 
 /**
  *
  * @author cmueller
  * @deprecated This class was for early prototype testing and is no longer functional
  */
 @Deprecated
 public class AddDatasetToStore {
 
     private Properties ooiciProps;
 
     public AddDatasetToStore(String props, String dsLoc, String id) {
         try {
             ooiciProps = new Properties();
             ooiciProps.load(new java.io.FileReader(props));
             String dsName = id;
             if (dsName == null || dsName.isEmpty()) {
                 dsName = java.util.UUID.randomUUID().toString();
             }
             System.out.println("<<<<<< " + dsName + " >>>>>>");
             addDatasetToStore(dsLoc, dsName);
         } catch (InvalidRangeException ex) {
             Logger.getLogger(AddDatasetToStore.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(AddDatasetToStore.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private String addDatasetToStore(String dsLoc, String dsName) throws IOException, InvalidRangeException {
         /* Setup broker */
         MsgBrokerClient brokercl = new MsgBrokerClient(ooiciProps.getProperty("server", "localhost"), AMQP.PROTOCOL.PORT, ooiciProps.getProperty("exchange", "magnet.topic"));
         brokercl.attach();
         /* make attstore */
         AttributeStore as = new AttributeStore(ooiciProps.getProperty("sysname", "localhost"), brokercl);
 
         /* open NetcdfDataset */
         NetcdfDataset ncd = NetcdfDataset.openDataset(dsLoc);
 //        System.out.println(ncd.getDetailInfo());
 
         String key;
         for (ucar.nc2.Dimension d : ncd.getDimensions()) {
             key = dsName + ":dim:" + d.getName();
             System.out.println(">>>>>>Adding dimension: " + key);
             as.put(key, IospUtils.serialize(new SerialDimension(d)));
         }
         for (ucar.nc2.Attribute a : ncd.getGlobalAttributes()) {
             key = dsName + ":gatt:" + a.getName();
             System.out.println(">>>>>>Adding global attribute: " + key);
             as.put(key, IospUtils.serialize(new SerialAttribute(a)));
         }
         for (ucar.nc2.Variable v : ncd.getVariables()) {
             key = dsName + ":var:" + v.getName();
             System.out.println(">>>>>>Adding variable: " + key);
             /* Add the variable to the store */
             as.put(key, IospUtils.serialize(new SerialVariable(v)));
             /* Add the data for the variable */
             double size = v.getElementSize() * v.getSize();
             key = dsName + ":data:" + v.getName();
             if (size <= 5242880) {
                 /* Small-ish variable (<5mb) - add the whole thing */
                 System.out.println(">>>>>>Adding data: " + key);
                 as.put(key, IospUtils.serialize(new SerialData(v.getDataType(), v.read())));
             } else {
                 /* Big variable - chunk it by the outermost dimension (i.e. slowest varying - often time) */
                 String dKey;
                 int[] origin = v.getShape();
                 java.util.Arrays.fill(origin, 0);
                 int[] shape = v.getShape();
                 int nt = shape[0];
                 shape[0] = 1;
                 for (int i = 0; i < nt; i++) {
                     origin[0] = i;
                     dKey = key + ":idx=" + i;
                     System.out.println(">>>>>>Adding data: " + dKey);
                     as.put(dKey, IospUtils.serialize(new SerialData(v.getDataType(), v.read(origin, shape))));
                 }
             }
         }
 
         System.out.println("Successfully added dataset " + dsName + " to store: true");
 
         /* detatch the broker */
         brokercl.detach();
         return dsName;
     }
 
     public static void main(String[] args) {
         String props = null, dataset = null, id = null;
         if (args.length < 1) {
             System.out.println("Not enough args - requires 1 argument:\n\t1 = path to config file (i.e. ooici.properties)\n\toptional argument = url to netcdf dataset (local or remote)");
             System.exit(-1);
         } else if (args.length == 2) {
             props = args[0];
             dataset = args[1];
         } else if (args.length > 2) {
             props = args[0];
             dataset = args[1];
             id = args[2];
         } else {
             props = args[0];
             try {
                 java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
                 System.out.println("Enter the path/url to the dataset to add, then hit \"enter\": ");
                 dataset = reader.readLine();
                 System.out.println("Enter the name for the dataset (leave blank for auto-generated unique ID): ");
                 id = reader.readLine();
             } catch (IOException ex) {
                 System.exit(-1);
             }
         }
         if (props != null & dataset != null) {
             new AddDatasetToStore(props, dataset, id);
         }
     }
 }
