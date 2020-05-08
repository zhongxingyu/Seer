 package com.anusiewicz.MCForAndroid.model;
 
 import android.util.Log;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Szymon Anusiewicz
  * Date: 28.08.13
  * Time: 23:37
  */
 public class MCResponse {
 
     private MCCommand commandCode;
     private Integer wordValue;
     private Boolean bitValue;
     private Integer completionCode;
 
 
     public MCResponse(MCCommand command, Integer completion) {
         this(command, completion, null, null);
     }
 
     public MCResponse(MCCommand command, Integer completion, Integer word, Boolean bit) {
         this.commandCode = command;
         this.completionCode = completion;
         this.wordValue = word;
         this.bitValue = bit;
     }
 
 
     public static MCResponse parseMCResponseFromString(String responseString){
 
         MCCommand commandCode = null;
         Integer completionCode = null;
         Integer wordValue = null;
         Boolean bitValue = null;
 
         try {
 
         if (responseString.startsWith("80")) {
             commandCode = MCCommand.READ_BIT;
         } else  if (responseString.startsWith("81")) {
             commandCode = MCCommand.READ_WORD;
         } else  if (responseString.startsWith("82")) {
             commandCode = MCCommand.WRITE_BIT;
         } else  if (responseString.startsWith("83")) {
             commandCode = MCCommand.WRITE_WORD;
         }  else  if (responseString.startsWith("84")) {
             commandCode = MCCommand.READ_WORD;
         } else  if (responseString.startsWith("93")) {
             commandCode = MCCommand.PLC_RUN;
         } else  if (responseString.startsWith("94")) {
             commandCode = MCCommand.PLC_STOP;
         } else {
             Log.e("MCResponse","Error while parsing response - unknown command");
            return new MCResponse(null,-1);
         }
 
         String completionString = responseString.substring(2,4);
         completionCode = Integer.getInteger(completionString);
         if (completionCode != 0 ) {
            Log.w("MCResponse","Command " + commandCode + " completed with error: " + completionCode);
            return new MCResponse(commandCode,completionCode);
         }
 
         if (commandCode == MCCommand.READ_BIT) {
 
             String bitSubstring = responseString.substring(4);
             if (bitSubstring.startsWith("00")){
                 bitValue = false;
             } else {
                 bitValue = true;
             }
             return new MCResponse(commandCode,completionCode,null,bitValue);
         }  else if (commandCode == MCCommand.READ_WORD) {
 
             String wordSubstring = responseString.substring(4);
             wordValue = Integer.parseInt(wordSubstring,16);
 
             return new MCResponse(commandCode,completionCode,wordValue,null);
         }
 
         return new MCResponse(commandCode,completionCode);
 
         } catch (Exception e) {
             Log.e("MCResponse","Error while parsing response");
             return null;
         }
     }
 
     public MCCommand getCommandCode(){
         return this.commandCode;
     }
 
     public Integer getWordValue() {
         return wordValue;
     }
 
     public Boolean getBitValue() {
         return bitValue;
     }
 
     public Integer getCompletionCode() {
         return completionCode;
     }
 }
