 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dip.lab3;
 import javax.swing.JOptionPane;
 /**
  *
  * @author gcDataTechnology
  */
 public class MobilePhoneDevice implements Device{
     
      
      
      private DeviceType deviceType=DeviceType.LAPTOP;
      private MessageHandlerTemplate messageHandler;
      private MessageType[] compatibleMessages;
      //private final int EXPANDARRAY=1;
      private final int STARTING_INDEX=0;
      private UserOutput userOut;
      private UserInput userInput;
      private Message message;
      
      private final String WRONG_MESSAGE_TYPE="I cannot receive that message type";
      private String SENDING_STARTED="I am sending a message from a " + deviceType + " to a ";
      private final String ERROR="ERROR...ProcessConflict";
      private final String MESSAGE_RECEIVED="The package has been received";
      private String IPAddress="";
      public static int IPnode=1;
      MessageSource source;
      
      
     
      //constructors
         
      public MobilePhoneDevice(MessageType[] types, UserInput userIn, UserOutput userOut, MessageHandlerTemplate messageHandler){
          if(types!=null && userIn!=null && userOut!=null && messageHandler!=null){
              IPnode++;
              this.messageHandler = messageHandler;
              this.userOut=userOut;
              this.userInput=userIn;
             IPAddress="210.178.5." + IPnode;
 
              //set message types
              setCompatibleMessageTypes(types);
          }else{
              //throws exception
              userOut.writeLine(ERROR);
          }
          
      }
      
      //Destination Methods
      @Override
      public final void receiveMessageFromSource(Message message, MessageSource source){
             this.source=source;
             userOut.writeLine(MESSAGE_RECEIVED);
             replyToSource(source, MESSAGE_RECEIVED);
          //useMessage has exhausive error handling since it was declared public abstract in the interface
              useMessage(message);
             
             
      }
      
     public final boolean checkMessageCompatibility(Message message){
         boolean goodMessage=false;
         if(message!=null){
             for(MessageType m:compatibleMessages){
                 if(message.getMessageType().equals(m)){
                     goodMessage=true;
                 }
             }
         }else{
             //throws exception
             userOut.writeLine(ERROR);
             replyToSource(this.source,ERROR);
         
         }
         return goodMessage;
     }
     
     @Override
     public  final void setCompatibleMessageTypes(MessageType[] types){
        boolean ready=true;
        for(MessageType t:types){
            if(t==null){
                ready=false;
            }
        }
        if(ready){
        compatibleMessages=new MessageType[types.length];
        System.arraycopy(types,STARTING_INDEX, compatibleMessages, STARTING_INDEX, types.length);
        }else{
            //throw an exception
            userOut.writeLine(ERROR);
        }
     }
     
     @Override
     public final MessageType[] getCompatibleMessageTypes(){
         
         return compatibleMessages;
     }
             
     @Override
     public final void useMessage(Message message){
         if(message!=null){
         //checks for compatibility
              if(checkMessageCompatibility(message)){
 
                  //uses the data appropriately
                  for(MessageType m:compatibleMessages){
                     if(message.getMessageType().equals(m)){
                         //use message accordingly
                         userOut.writeLine(message.getMessageEncoding());//using as a substitute reality
                     }
                  }
              }else{
                  //throwsException;
                  replyToSource(this.source,WRONG_MESSAGE_TYPE);
                  userOut.writeLine(WRONG_MESSAGE_TYPE);
              }
         }else{
             //throws exception
             replyToSource(this.source,ERROR);
             userOut.writeLine(ERROR);
         }
             
     }
         
         
     
     
     //Source Methods
     @Override
     public  final void createMessage(MessageType type){
         if(type!=null){
         message=new Message(type);
         }else{
             //throw exception
             userOut.writeLine(ERROR);
             
         }
     }
     
     @Override
     public final Message getMessage(){
         return message;
     }
     
     @Override
     public  final void sendMessageToDestination(Message message, MessageSource source, MessageDestination destination){
         if(message!=null && destination!=null){
             userOut.writeLine(SENDING_STARTED + destination.getDeviceType().toString());
             
             //delegates the work
             messageHandler.deliverMessageToDesination(message, source,destination);
         }else{
             //throw exception
             userOut.writeLine(ERROR);
         }
         
     }
     
     //Getters & Setters
     public final DeviceType getDeviceType() {
         return deviceType;
     }
 
     public final void setDeviceType(DeviceType deviceType) {
         if(deviceType!=null){
         this.deviceType = deviceType;
         }else{
             //throws exception
             userOut.writeLine(ERROR);
         }
     }
 
     public final MessageHandlerTemplate getMessageHandler() {
         return messageHandler;
     }
 
     public final void setMessageHandler(MessageHandlerTemplate messageHandler) {
         if(messageHandler!=null){
         this.messageHandler = messageHandler;
         }else{
             //throws exception
             userOut.writeLine(ERROR);
         }
         
     }
 
     @Override
      public UserOutput getSourceUserOutput(){
       return userOut;   
     }
     
     
     @Override
     public  void replyToSource(MessageSource source, String line){
         source.getSourceUserOutput().writeLine(getIPAddress() + ":: " + line);
     }
     
     public String getIPAddress(){
         return IPAddress;
     }
     
     
 }
