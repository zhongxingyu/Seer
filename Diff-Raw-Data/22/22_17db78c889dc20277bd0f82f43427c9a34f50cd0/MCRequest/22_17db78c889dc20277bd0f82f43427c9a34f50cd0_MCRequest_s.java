 package com.anusiewicz.MCForAndroid.model;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Szymon Anusiewicz
  * Date: 29.06.13
  * Time: 22:14
  */
 public class MCRequest {
 
    private static final int MAX_INT = 32767;
    private static final int MIN_INT = -32768;
     private MCCommand command;
     private MCDeviceCode deviceType;
     private Integer deviceNumber;
     private Integer wordValue;
     private Boolean bitValue;
     //private  int numberOfDevicePoints;
 
     public MCRequest(MCCommand command) {
         this(command, null, null);
     }
 
     public MCRequest(MCCommand command, MCDeviceCode deviceType, Integer deviceNumber) throws IndexOutOfBoundsException {
         this(command, deviceType, deviceNumber,null,null);
     }
 
     public MCRequest(MCCommand command, MCDeviceCode deviceType, Integer deviceNumber, Integer word, Boolean bit) throws IndexOutOfBoundsException {
         this.command = command;
 
         if (command.equals(MCCommand.PLC_STOP) || command.equals(MCCommand.PLC_RUN)) {
             this.deviceType = null;
             this.deviceNumber = null;
             this.wordValue = null;
             this.bitValue = null;
 
         }  else {
 
             this.deviceType = deviceType;
 
             if (deviceNumber == null) {
                 this.deviceNumber = null;
             } else
             if (deviceNumber >=0 && deviceNumber <=deviceType.getDeviceRange()) {
                 this.deviceNumber = deviceNumber;
             }    else {
                 throw new IndexOutOfBoundsException("Choose " + deviceType + " devices from 0 to " + deviceType.getDeviceRange());
             }
 
             if (command.equals(MCCommand.READ_BIT) || command.equals(MCCommand.READ_WORD)) {
                 this.wordValue = null;
                 this.bitValue = null;
 
             }   else if (command.equals(MCCommand.WRITE_WORD)) {
                 if (word >= MIN_INT && word<=MAX_INT) {
                     this.wordValue = word;
                 } else {
                     throw new IndexOutOfBoundsException("Specified value exceeds the valid range");
                 }
                 this.bitValue = null;
             }   else if (command.equals(MCCommand.WRITE_BIT)) {
                 this.wordValue = null;
                 this.bitValue = bit;
             }
         }
     }
 
     public static String generateStringFromRequest(MCRequest request) {
 
         StringBuilder builder = new StringBuilder(30);
         builder.append(request.getCommand().getCommandCode())
                 .append("FF0000");
         if (request.getDeviceType() != null && request.getDeviceNumber() !=null) {
                 builder.append(request.getDeviceType().getDeviceCode());
 
                 String devNum = Integer.toHexString(request.getDeviceNumber());
 
                 for ( int i = 1; i<= 8-devNum.length(); i++ ) {
                     builder.append("0");
                 }
                 builder.append(devNum)
                         .append("0100");
         }
 
         if (request.getWordValue() != null) {
 
             String word = Integer.toHexString(request.getWordValue());
 
             for ( int i = 1; i<= 4-word.length(); i++ ) {
                 builder.append("0");
             }
             builder.append(word);
         }
 
         if (request.getBitValue() != null) {
             if (request.getBitValue().equals(Boolean.TRUE)) {
                 builder.append("10");
             }   else {
                 builder.append("00");
             }
         }
 
 
          return builder.toString();
     }
 
     public MCCommand getCommand() {
         return command;
     }
 
     public MCDeviceCode getDeviceType() {
         return deviceType;
     }
 
     public Integer getDeviceNumber() {
         return deviceNumber;
     }
     public Integer getWordValue() {
         return wordValue;
     }
 
     public Boolean getBitValue() {
         return bitValue;
     }
 
 }
