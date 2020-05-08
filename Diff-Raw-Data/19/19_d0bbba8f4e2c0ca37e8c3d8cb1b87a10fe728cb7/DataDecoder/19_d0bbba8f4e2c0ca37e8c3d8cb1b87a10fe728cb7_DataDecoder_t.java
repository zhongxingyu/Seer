 
 package com.test.websocket;
 
 import javax.websocket.DecodeException;
 import javax.websocket.Decoder;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.apache.log4j.Logger;
 
 /**
  * @author Arun Gupta
  */
 public class DataDecoder implements Decoder.Text<Gamedata> {
      Logger logger = Logger.getLogger(com.test.websocket.DataDecoder.class);
     @Override
     public Gamedata decode(String string) throws DecodeException {
        logger.info ("In Data Decode");
         try {
             JSONObject jsonObject = new JSONObject(string);
             return new Gamedata(jsonObject);
         } catch (JSONException ex) {
             throw new DecodeException("Error parsing JSON", ex.getMessage(), ex.fillInStackTrace());
         }
     }
 
     @Override
     public boolean willDecode(String string) {
         logger.info ("Check Decoding Data: " + string);
         try {
             new JSONObject(string);
             return true;
         } catch (JSONException ex) {
             ex.printStackTrace();
             return false;
         }
     }
     
 }
