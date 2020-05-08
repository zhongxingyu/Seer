 package com.ingenuity.icg.util;
 
 import org.springframework.stereotype.Component;
 
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: pschmidt
  * Date: 10/15/12
  * Time: 4:53 PM
  * <p/>
  * Copyright (C) 2010 Ingenuity Systems, Inc. All rights reserved.
  * <p/>
  * This software is the confidential & proprietary information of Ingenuity Systems, Inc.
  * ("Confidential Information").
  * You shall not disclose such Confidential Information and shall use it only in
  * accordance with the terms of any agreement or agreements you entered into with
  * Ingenuity Systems.
  */
 @Component
 public class DatasetParserHelper {
 
      public Map<String, String> convertStringToMap(String s) {
         Map<String, String> keyValuesMap = new HashMap<String, String>();
 
         System.out.println(s);
 
        String[] lines = s.split("\r\n|\r|\n");
         for(String str : lines) {
             if (str.startsWith("key")) {
                 // skip first header row
                 continue;
             }
 
             String[] keyvalues = str.split("\t");
             if (keyvalues != null && keyvalues.length == 2) {
                keyValuesMap.put(keyvalues[0].trim(), keyvalues[1].trim());
             }
         }
 
         return keyValuesMap;
     }
 
     /**
      * Parse a byte buffer into a map of key/value pairs. Each key/value pair is expected to be separated by a tab(\t)
      * character. Expected format is like below:
      *  key	value
      *  external_experiment_id	7330943
      *  tissue_type	Liver
      *
      * @param bb ByteBuffer to parse
      * @return  A map of key/values
      */
     public Map<String, String> convertByteBufferToMap(ByteBuffer bb) {
         Map<String, String> keyValuesMap = new HashMap<String, String>();
 
         Charset charset = Charset.defaultCharset();
         CharBuffer charBuffer;
         CharsetDecoder decoder = charset.newDecoder();
 
         try {
             charBuffer = decoder.decode(bb);
         } catch (CharacterCodingException ex) {
             return keyValuesMap;
         }
 
         String s = charBuffer.toString();
         System.out.println(s);
 
        String[] lines = s.split("\r\n|\r|\n");
         for(String str : lines) {
             if (str.startsWith("key")) {
                 // skip first header row
                 continue;
             }
 
             String[] keyvalues = str.split("\t");
             if (keyvalues != null && keyvalues.length == 2) {
                keyValuesMap.put(keyvalues[0].trim(), keyvalues[1].trim());
             }
         }
 
         return keyValuesMap;
     }
 }
