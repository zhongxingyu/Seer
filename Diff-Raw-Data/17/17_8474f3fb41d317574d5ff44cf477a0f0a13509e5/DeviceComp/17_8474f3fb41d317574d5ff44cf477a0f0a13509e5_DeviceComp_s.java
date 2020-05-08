 package knx;
 
 import esir.dom11.nsoc.model.Action;
 import esir.dom11.nsoc.model.Data;
 import esir.dom11.nsoc.model.DataType;
 import esir.dom11.nsoc.model.device.Actuator;
 import esir.dom11.nsoc.model.device.Sensor;
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.framework.MessagePort;
 import tuwien.auto.calimero.CloseEvent;
 import tuwien.auto.calimero.FrameEvent;
 import tuwien.auto.calimero.link.event.NetworkLinkListener;
 
 import java.util.Date;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: michel
  * Date: 09/01/12
  * Time: 12:07
  * To change this template use File | Settings | File Templates.
  */
 
 @Provides({
         @ProvidedPort(name = "ActionReceive", type = PortType.MESSAGE)
 })
 
 @Requires({
         @RequiredPort(name = "DataSend", type = PortType.MESSAGE, optional = true),
         @RequiredPort(name = "commandKNX", type = PortType.SERVICE, className = IntToConnect.class, optional = true, needCheckDependency = true)
 })
 
 @DictionaryType({
         @DictionaryAttribute(name = "Device", defaultValue = "Actuator", optional = true,
                 vals = {"Sensor", "Actuator"}),
         @DictionaryAttribute(name = "Location", defaultValue = "/bat7/salle930/0", optional = true),
         @DictionaryAttribute(name = "AddressDevice", optional = true),
         @DictionaryAttribute(name = "TypeOfSensor", defaultValue = "Data", optional = true,
                 vals = {"Data", "Switch"}),
         @DictionaryAttribute(name = "DataType", defaultValue = "TEMPERATURE", optional = true,
                 vals = {"UNKNOWN", "TEMPERATURE", "BRIGHTNESS", "HUMIDITY", "POWER", "PRESENCE", "SWITCH"})
 })
 
 @Library(name = "NSOC_2011")
 @ComponentType
 public class DeviceComp extends AbstractComponentType {
     private IntToConnect connectionManager;
     private Actuator mActuator;
     private Sensor mSensor;
     private String mDevice;
     private String mLocation;
     private String mAddressDevice;
     private String mTypeOfSensor;
     private String mDataType;
     private Boolean stop = false;
 
     @Start
     public void startComponent() {
         System.out.println("DeviceComp: Start");
         mDevice = (String) this.getDictionary().get("Device");
         mLocation = (String) this.getDictionary().get("Location");
         mAddressDevice = (String) this.getDictionary().get("AddressDevice");
         mTypeOfSensor = (String) this.getDictionary().get("TypeOfSensor");
         mDataType = (String) this.getDictionary().get("DataType");
         connectionManager = getPortByName("commandKNX", IntToConnect.class);
 
         if (mDevice.equals("Actuator")) {
             System.out.println("DeviceComp: Create Actuator");
             mActuator = new Actuator(DataType.valueOf(mDataType), mLocation);
         } else if (mDevice.equals("Sensor")) {
             mSensor = new Sensor(DataType.valueOf(mDataType), mLocation);
             if (connectionManager.getProtocol().equals("knx")) {
                 System.out.println("DeviceComp: Create Sensor");
                 if (mTypeOfSensor.equals("Data")) {
                     sendBackgroundMessage();
                 } else if (mTypeOfSensor.equals("Switch")) {
                     listener(mAddressDevice);
                 }
             }
         }
     }
 
     @Stop
     public void stopComponent() {
         System.out.println("DeviceComp: Stop");
         stop = true;
     }
 
     @Update
     public void updateComponent() {
         System.out.println("DeviceComp: Update");
         stopComponent();
         startComponent();
     }
 
     @Port(name = "ActionReceive")
     public void ActionReceive(Object o) {
         Action action = (Action) o;
         Boolean value = false;
         if (action.getActuator().getLocation().equals(mLocation)) {
             System.out.println("\nDeviceComp: Received: " + action.toString() + "\n\n");
             System.out.println("GetProtocol: " + connectionManager.getProtocol());
             if (connectionManager.getProtocol().equals("knx")) {
                 if (mDevice.equals("Actuator")) {
                     if (action.getValue().equals("ON")) {
                         value = true;
                     } else if (action.getValue().equals("OFF")) {
                         value = false;
                     }
                     System.out.println("DeviceComp: Receive Action");
                     connectionManager.write(mAddressDevice, value);
                 }
             }
         }
     }
 
     public void sendMessage(Object message) {
         MessagePort prodPort = getPortByName("DataSend", MessagePort.class);
         if (prodPort != null) {
             prodPort.process(message);
         }
     }
 
     public void sendBackgroundMessage() {
         new Thread() {
             @Override
             public void run() {
                 String valueRead = connectionManager.read(mAddressDevice);
                 Data data = new Data(mSensor, valueRead, new Date());
                 while (!stop) {
                     try {
                         if (!(valueRead.equals(connectionManager.read(mAddressDevice)))) {
                             System.out.println("ValueRead changed");
                             valueRead = connectionManager.read(mAddressDevice);
                             System.out.println(mSensor.toString() + " Value: " + valueRead);
                             data = new Data(mSensor, valueRead, new Date());
                             sendMessage(data);
                         }
                         Thread.sleep(3000);
                     } catch (InterruptedException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 }
             }
         }.start();
     }
 
     public void listener(final String adresse) {
 
         connectionManager.getNetLink().addLinkListener(new NetworkLinkListener() {
 
             @Override
             public void confirmation(FrameEvent arg0) {
                 // TODO Auto-generated method stub
             }
 
             @Override
             public void indication(FrameEvent arg0) {
                 // TODO Auto-generated method stub
                 String adresseEnvoyee = ((tuwien.auto.calimero.cemi.CEMILData) arg0.getFrame()).getDestination().toString();
                 if (adresseEnvoyee.equals(adresse)) {
                     String valueRead = "";//connectionManager.read(mAddressDevice);
                     System.out.println("Switch: \n" + mSensor.toString() + " Value: " + valueRead);
                     Data data = new Data(mSensor, valueRead, new Date());
                     sendMessage(data);
                 }
             }
 
             @Override
             public void linkClosed(CloseEvent arg0) {
                 // TODO Auto-generated method stub
 
             }
         });
     }
 }
