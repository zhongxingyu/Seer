 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.opensimkit.drivers;
 
 import java.util.ArrayList;
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import org.opensimkit.utilities.ZTESerialPorts;
 
 /**
  *
  * @author ahmedmaawy
  */
 @PluginImplementation
 public class ZTEMF192Driver implements DriverInterface {
 
     int numMessages;
     private ArrayList<String> messages;
     private final String delimiter = "<<.END.>>";
     
     private ZTESerialPorts serialPorts;
     
     // Contructor
     
     public ZTEMF192Driver()
     {
         serialPorts = new ZTESerialPorts();
         messages = new ArrayList<String>();
     }
     
     // Breaks up messages into smaller components (submessages)
     
     private void processMessages(String textToProcess)
     {
        messages.clear();
        
         String [] splitString = textToProcess.split("\n");
         int numElements = splitString.length;
         
         for(int elementLoop = 0; elementLoop < numElements; elementLoop ++)
         {
             if(!splitString[elementLoop].contains("+CMGL") 
                     && !splitString[elementLoop].trim().equals("OK")
                     && !splitString[elementLoop].trim().equals(""))
             {
                 messages.add(splitString[elementLoop].trim());
             }
         }
     }
     
     @Override
     public String getManufacturer() {
         return "ZTE";
     }
 
     @Override
     public String getModel() {
         return "MF192";
     }
 
     @Override
     public String getRevision() {
         return null;
     }
 
     @Override
     public String runCommand(String command) {
         if(!isDeviceConnected())
         {
             throw new DriverException("Device is not connected");
         }
         
         return serialPorts.runCommand(command);
     }
 
     @Override
     public boolean saveMessage(String contact, String message) {
         if(!isDeviceConnected())
         {
             throw new DriverException("Device is not connected");
         }
         
         return serialPorts.saveMessage(contact, message);
     }
 
     @Override
     public boolean setMemoryToSIMMemory() {
         if(!isDeviceConnected())
         {
             throw new DriverException("Device is not connected");
         }
         
         return serialPorts.setMemSIMCard();
     }
 
     @Override
     public boolean setToTextFormat() {
         if(!isDeviceConnected())
         {
             throw new DriverException("Device is not connected");
         }
         
         return serialPorts.setTextFormat();
     }
 
     @Override
     public String getAllMessages() {
         if(!isDeviceConnected())
         {
             throw new DriverException("Device is not connected");
         }
         
         String messagesToProcess = serialPorts.getAllMessages();
         
         // Store messages in array
         processMessages(messagesToProcess);
         
         numMessages = messages.size();
         String messagesString = "";
         
         for(int messageLoop = 0; messageLoop < numMessages; messageLoop ++)
         {
             messagesString = messagesString.concat(messages.get(messageLoop));
             
             // Dont delimit the end message
             if(messageLoop < (numMessages - 1))
                 messagesString = messagesString.concat("\n" + delimiter + "\n");
         }
         
         return messagesString;
     }
 
     @Override
     public String getMessageAt(int messageIndex) {
         if(!isDeviceConnected())
         {
             throw new DriverException("Device is not connected");
         }
         
         if(messageIndex > (messages.size() - 1))
         {
             getAllMessages();
         }
         
         if((messages.size() - 1) < messageIndex)
         {
             throw new DriverException("Message at that index does not exist");
         }
         
         return messages.get(messageIndex);
     }
 
     @Override
     public String getDelimiter() {
         return delimiter;
     }
 
     @Override
     public boolean isGenericConnection() {
         // Is it a generic form of a connection or is it via the serial ports?
         return false;
     }
 
     @Override
     public boolean connectToDevice() {
         // Use only if the connection is a generic one
         return false;
     }
 
     @Override
     public boolean connectToSerialPort(int portIndex) {
         return serialPorts.connectPort(portIndex);
     }   
 
     @Override
     public boolean isDeviceConnected() {
         return serialPorts.isConnected();
     }
 
     @Override
     public String getPortName() {
         if(!isDeviceConnected())
         {
             throw new DriverException("Device is not connected");
         }
         
         return serialPorts.getPortName();
     }
 
     @Override
     public boolean disconnectDevice() {
         if(!isDeviceConnected())
         {
             throw new DriverException("Device is not connected");
         }
         
         return serialPorts.disconnectPort();
     }
 
     @Override
     public boolean clearAllMessages() {
         if(!isDeviceConnected())
         {
             throw new DriverException("Device is not connected");
         }
         
         return serialPorts.clearAllMessages();
     }
 }
