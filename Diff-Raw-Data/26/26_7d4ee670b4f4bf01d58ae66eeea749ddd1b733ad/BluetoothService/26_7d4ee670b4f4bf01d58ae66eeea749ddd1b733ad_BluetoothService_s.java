 package kb.apps.palinkathermobox;
 
 import java.util.UUID;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 
 import kb.apps.palinkathermobox.BluetoothThread;
 
 /**
  * implements the bluetooth service
  * */
 public class BluetoothService implements ServiceEvents {
 
   private BluetoothAdapter adapter;       /** bluetooth adapter object */
   private BluetoothDevice device;         /** bluetooth device object (connected to) */
   private BluetoothThread service;        /** bluetooth service thread object */
   private final ApplicationEvents event;  /** event handler object */
   private UUID uuid;                      /** service id */
   private String command;                 /** received command */
   
   /**
    * constructor
    * @param adapter the bluetooth adapter in the used device
    * @param device the remote device (server) which intend to connect to
    * @param btUuid service id
    * @param event event handler object
    * */
   public BluetoothService(BluetoothAdapter adapter, BluetoothDevice device, UUID btUuid, ApplicationEvents event) {
     this.adapter = adapter;
     this.device = device;
     this.event = event;
     uuid = btUuid;
     command = "";
   }
   
   /**
    * starts the service
    * */
   public synchronized void start() {
     if (service != null) {
       service.cancel();
       service = null;      
     }
     service = new BluetoothThread(adapter, device, uuid, this);
     service.start();
   }
   
   /**
    * stops the service
    * */
   public synchronized void stop() {
     if (service != null) {
       service.cancel();
       service = null;      
     }
   }
   
   /**
    * returns true if service is running otherwise false
    * */
   public synchronized boolean isRunning() {
     boolean ret = false;
     
     if (service != null) {
       ret = service.isAlive();
     }
     return ret;
   }
   
   /**
    * writes buffer to output stream
    * @param buffer buffer to write
    * */
   public void write(byte[] buffer) {
     service.write(buffer);
   }
   
   public void write(int out) {
     service.write(out);
   }
 
   @Override
   public void onDataReceived(byte[] data) {    
     StringBuilder cmd = new StringBuilder();
    StringBuilder value = new StringBuilder();    
 
     for (int i = 0; i < data.length; ++i) {
       command += (char) data[i];
     }
    if (GetCommand(command, cmd, value)) {
       if (event != null) {
         event.onCommand(cmd.toString(), value.toString());
       }
      command = "";
     }
   }
 
   @Override
   public void onErrorMessage(String errorMsg) {
     if (event != null) {
       event.onErrorMessage(errorMsg);
     }
   }
   
   /**
    * check full command receive do not
   * returns with true if full command is available otherwise false
    * @param data raw command string
    * @param cmd received command
    * @param value received value 
    * */
  private boolean GetCommand(String data, StringBuilder cmd, StringBuilder value) {
    boolean ret;
     int index = data.indexOf("\r\n");
     
    ret = index != -1;
    if (ret) {
       int cmdEnd = data.lastIndexOf("_");
       if (cmdEnd != -1) {
         cmd.append(data.substring(0, cmdEnd));
         value.append(data.substring((cmdEnd + 1), (data.length() - 1)));
       }
     }
    return ret;
   }
     
 }
