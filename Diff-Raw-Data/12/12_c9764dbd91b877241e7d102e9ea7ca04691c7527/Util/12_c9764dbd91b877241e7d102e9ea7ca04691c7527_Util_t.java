 package com.yanchuanli.games.pokr.util;
 
import com.yanchuanli.games.pokr.util.Config;
 import com.yanchuanli.games.pokr.core.Card;
 import org.apache.log4j.Logger;
 import org.apache.mina.core.buffer.IoBuffer;
 import org.apache.mina.core.session.IoSession;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Copyright Candou.com
  * Author: Yanchuan Li
  * Email: mail@yanchuanli.com
  * Date: 12-5-31
  */
 public class Util {
 
     private static Logger log = Logger.getLogger(Util.class);
 
     /*   public static void sendToAll(String message) {
         for (String s : Memory.sessionsOnServer.keySet()) {
             Player player = Memory.sessionsOnServer.get(s);
             Util.sendMessage(player.getSession(), message);
         }
     }
 
     public static void sendMessage(IoSession session, String input) {
 //        synchronized (session) {
             IoBuffer answer = IoBuffer.allocate(toByte(6, input).length, false);
             answer.put(toByte(6, input));
             answer.flip();
             session.write(answer);
             answer.free();
             log.debug("socket sent:" + input);
 //        }
     }*/
 
     public static String cardsToString(List<Card> cardList) {
         String result = "";
         for (Card card : cardList) {
             result += card.toChineseString() + " ";
         }
         return result;
     }
 
     public static String cardsToGIndexes(List<Card> cardList) {
         String result = "";
         for (Card card : cardList) {
             result += card.getIndex() + "_";
         }
         return result;
     }
 
 
     public static List<String> extractStringFromIoBuffer(IoBuffer buffer) {
         List<String> list = new ArrayList<String>();
         try {
             boolean flag = true;
             int part = 0;
             while (flag) {
                 if (buffer.get(part + 0) == Config.START
                         && buffer.get(part + 2) == Config.SPLIT) {
                     byte[] bb = new byte[4];
                     bb[0] = buffer.get(part + 3);
                     bb[1] = buffer.get(part + 4);
                     bb[2] = buffer.get(part + 5);
                     bb[3] = buffer.get(part + 6);
                     int size = bytesToInt(bb);
                     if (buffer.get(part + size + 3 + 4) == Config.START) {
                         byte[] b = new byte[size];
                         for (int index = 0; index < b.length; index++) {
                             b[index] = buffer.get(part + index + 3 + 4);
                         }
                         list.add(new String(b));
                     }
                     part = part + size + 4 + 4;
                 } else {
                     flag = false;
                 }
             }
         } catch (Exception e) {
 //            log.error(e);
         }
         return list;
     }
 
     public static byte[] toByte(int type, String input) {
         byte[] b = null;
         byte[] length = null;
 
         b = new byte[input.getBytes().length + 8];
         length = intToByte(input.getBytes().length);
         b[3] = length[0];
         b[4] = length[1];
         b[5] = length[2];
         b[6] = length[3];
         for (int index = 7; index < b.length - 1; index++) {
             b[index] = input.getBytes()[index - 7];
         }
         b[0] = Config.START;
         b[1] = (byte) type;
         b[2] = Config.SPLIT;
 
         b[b.length - 1] = (byte) Config.START;
         return b;
     }
 
     public static byte[] intToByte(int i) {
         byte[] bytes = new byte[4];
         bytes[0] = (byte) (0xff & i);
         bytes[1] = (byte) ((0xff00 & i) >> 8);
         bytes[2] = (byte) ((0xff0000 & i) >> 16);
         bytes[3] = (byte) ((0xff000000 & i) >> 24);
         return bytes;
     }
 
     public static int bytesToInt(byte[] bytes) {
         int length = bytes[0] & 0xFF;
         length |= ((bytes[1] << 8) & 0xFF00);
         length |= ((bytes[2] << 16) & 0xFF0000);
         length |= ((bytes[3] << 24) & 0xFF000000);
         return length;
     }
 
     // 2012-06-19
     public static byte[] stringToByteArray(int type, String input) {
         return encodeByteArray(type, input.getBytes());
     }
 
     public static List<Map<Integer, String>> ioBufferToString(IoBuffer buffer) {
         List<Map<Integer, String>> list = new ArrayList<Map<Integer, String>>();
 
         List<Map<Integer, byte[]>> byteArrayList = decodeByteArray(buffer);
         for (Map<Integer, byte[]> byteArrayMap : byteArrayList) {
             Map<Integer, String> map = new HashMap<Integer, String>();
             for (Integer key : byteArrayMap.keySet()) {
                 map.put(key, new String(byteArrayMap.get(key)));
             }
             list.add(map);
         }
 
         return list;
     }
 
     /**
      * 给byteArray加上约定好的head信息。
      */
     private static byte[] encodeByteArray(int type, byte[] byteArray) {
         byte[] b = null;
         byte[] length = null;
 
         b = new byte[byteArray.length + 8];
         length = intToByte(byteArray.length);
         b[3] = length[0];
         b[4] = length[1];
         b[5] = length[2];
         b[6] = length[3];
         for (int index = 7; index < b.length - 1; index++) {
             b[index] = byteArray[index - 7];
         }
         b[0] = Config.START;
         b[1] = (byte) type;
         b[2] = Config.SPLIT;
 
         b[b.length - 1] = (byte) Config.START;
 //		log.info("encodeByteArray: " + new String(b));
         return b;
     }
 
     /**
      * 从接收到的数据中解析出内容byteArray的Map。
      */
     private static List<Map<Integer, byte[]>> decodeByteArray(IoBuffer buffer) {
         List<Map<Integer, byte[]>> list = new ArrayList<Map<Integer, byte[]>>();
 
         try {
             boolean flag = true;
             int part = 0;
             while (flag) {
                 if (buffer.get(part + 0) == Config.START
                         && buffer.get(part + 2) == Config.SPLIT) {
                     byte[] bb = new byte[4];
                     bb[0] = buffer.get(part + 3);
                     bb[1] = buffer.get(part + 4);
                     bb[2] = buffer.get(part + 5);
                     bb[3] = buffer.get(part + 6);
                     int size = bytesToInt(bb);
                     if (buffer.get(part + size + 3 + 4) == Config.START) {
                         Map<Integer, byte[]> map = new HashMap<Integer, byte[]>();
                         byte[] b = new byte[size];
                         for (int index = 0; index < b.length; index++) {
                             b[index] = buffer.get(part + index + 3 + 4);
                         }
                         map.put((int) buffer.get(part + 1), b);
                         list.add(map);
                     }
                     part = part + size + 4 + 4;
                 } else {
                     flag = false;
                 }
             }
         } catch (Exception e) {
 //			log.error(e);
         }
 
         return list;
     }
 
     public static void sendMsg(IoSession session, String input, int type) {
        if (!Config.offlineDebug) {
             IoBuffer answer = IoBuffer.allocate(
                     stringToByteArray(type, input).length, false);
             answer.put(stringToByteArray(type, input));
             answer.flip();
             session.write(answer);
             answer.free();
         }
 
     }
 
 }
