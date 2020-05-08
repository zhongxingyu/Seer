 package com.stfalcon.mtpclient;
 
 import android.util.Log;
 
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.HashMap;
 
 /**
  * Created by user on 7/22/13.
  */
 public class Parser {
 
     public static final String TYPE = "type";
     public static final String AUTH = "auth_key";
     public static final String MESSAGE_ID = "message_id";
     public static final String MESSAGE_LENGTH = "message_length";
     public static final String RES_PQ = "res_pq";
     public static final String NONCE = "nonce";
     public static final String SERVER_NONCE = "server_nonce";
     public static final String NEW_NONCE_HASH1 = "new_nonce_hash1";
     public static final String PQ = "pq";
     public static final String P = "p";
     public static final String Q = "q";
     public static final String VECTOR_LONG = "vector_long";
     public static final String COUNT = "count";
     public static final String FINGER_PRINTS = "finger_prints";
     public static final String ENC_ANSWER = "encrypted_answer";
     public static final int TYPE_RES_PQ = 1663309317;
     public static final int TYPE_RES_DH = 1544022224;
     public static final int TYPE_DH_GEN_OK = 888654651;
 
     public static HashMap<String, Object> parseReqPqResponse(byte[] response) {
 
         try {
             HashMap<String, Object> result = new HashMap<String, Object>();
             ByteBuffer buffer = ByteBuffer.wrap(response, 0, 4);
             buffer.order(ByteOrder.LITTLE_ENDIAN);
             int header_message_length = buffer.getInt();
             byte[] message = new byte[header_message_length];
             ByteBuffer.wrap(response, 0, header_message_length).get(message);
             int header_pack_id = ByteBuffer.wrap(message, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
             Log.v("PARSER", "HEADER: " + header_message_length + " " + header_pack_id);
             long auth_key = ByteBuffer.wrap(message, 8, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
             long message_id = ByteBuffer.wrap(message, 16, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
             int message_length = ByteBuffer.wrap(message, 24, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
             long res_pq = ByteBuffer.wrap(message, 28, 4).order(ByteOrder.BIG_ENDIAN).getInt();
 
            int number_res_pq1 = ByteBuffer.wrap(message, 28, 4).order(ByteOrder.BIG_ENDIAN).getInt();
            int number_res_pq2 = ByteBuffer.wrap(message, 0, 4).order(ByteOrder.BIG_ENDIAN).getInt();
            int type = 0;
            if (number_res_pq1 == TYPE_RES_PQ || number_res_pq1 == TYPE_RES_DH) {
                type = number_res_pq1;
            } else {
                type = number_res_pq2;
            }
 
            switch (type) {
                 case TYPE_RES_PQ:
                     try {
                         byte[] nonce = new byte[16];
                         ByteBuffer.wrap(response, 32, 16).get(nonce);
                         byte[] server_nonce = new byte[16];
                         ByteBuffer.wrap(response, 48, server_nonce.length).get(server_nonce);
                         byte[] pq = new byte[12];
                         ByteBuffer.wrap(response, 64, pq.length).get(pq);
                         result.put(Parser.TYPE, TYPE_RES_PQ);
                         result.put(Parser.AUTH, auth_key);
                         result.put(Parser.MESSAGE_ID, message_id);
                         result.put(Parser.MESSAGE_LENGTH, message_length);
                         result.put(Parser.RES_PQ, res_pq);
                         result.put(Parser.NONCE, nonce);
                         result.put(Parser.SERVER_NONCE, server_nonce);
                         result.put(Parser.PQ, pq);
                         Log.v("PARSER", "AUTH: " + auth_key);
                         Log.v("PARSER", "Message ID: " + message_id);
                         Log.v("PARSER", "message_length: " + message_length);
                         Log.v("PARSER", "RES_PQ: " + res_pq);
                         Log.v("PARSER", "NONCE: " + Utils.byteArrayToHex(nonce));
                         Log.v("PARSER", "Server_NONCE: " + Utils.byteArrayToHex(server_nonce));
                         Log.v("PARSER", "PQ: " + Utils.byteArrayToHex(pq));
                         long vector_long = ByteBuffer.wrap(message, 76, 4).order(ByteOrder.BIG_ENDIAN).getInt();
                         long count = ByteBuffer.wrap(message, 80, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                         byte[] finger_prints = new byte[8];
                         ByteBuffer.wrap(response, 84, finger_prints.length).get(finger_prints);
                         byte[] PQ = new byte[8];
                         ByteBuffer.wrap(response, 65, pq.length).get(PQ);
                         BigInteger bigInteger = new BigInteger(PQ);
                         BigIntegerMath bigIntegerMath = new BigIntegerMath();
                         bigIntegerMath.factor(bigInteger);
                         BigInteger[] pq_result = bigIntegerMath.getfactors();
 
                         ByteBuffer byteBuffer = ByteBuffer.allocate(4);
                         byte[] p_arr = byteBuffer.putInt(pq_result[0].intValue()).array();
                         byteBuffer = ByteBuffer.allocate(8);
                        byteBuffer.put((byte) 0x04);
                         byteBuffer.put(p_arr);
                        byteBuffer.put(new byte[]{0x00, 0x00, 0x00});
                         p_arr = byteBuffer.array();
 
                         byteBuffer = ByteBuffer.allocate(4);
                         byte[] q_arr = byteBuffer.putInt(pq_result[1].intValue()).array();
                         byteBuffer = ByteBuffer.allocate(8);
                        byteBuffer.put((byte) 0x04);
                         byteBuffer.put(q_arr);
                        byteBuffer.put(new byte[]{0x00, 0x00, 0x00});
                         q_arr = byteBuffer.array();
 
                         result.put(Parser.P, p_arr);
                         result.put(Parser.Q, q_arr);
                         result.put(Parser.VECTOR_LONG, vector_long);
                         result.put(Parser.COUNT, count);
                         result.put(Parser.FINGER_PRINTS, finger_prints);
                         Log.v("PARSER", "P: " + pq_result[0]);
                         Log.v("PARSER", "Q: " + pq_result[1]);
                         Log.v("PARSER", "VECTOR_LONG: " + vector_long);
                         Log.v("PARSER", "COUNT: " + count);
                         Log.v("PARSER", "finger_prints: " + Utils.byteArrayToHex(finger_prints));
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                     break;
                 case TYPE_RES_DH:
                     try {
                         byte[] nonce = new byte[16];
                         ByteBuffer.wrap(response, 32, 16).get(nonce);
                         byte[] server_nonce = new byte[16];
                         ByteBuffer.wrap(response, 48, server_nonce.length).get(server_nonce);
                         result.put(Parser.TYPE, TYPE_RES_DH);
                         result.put(Parser.AUTH, auth_key);
                         result.put(Parser.MESSAGE_ID, message_id);
                         result.put(Parser.MESSAGE_LENGTH, message_length);
                         result.put(Parser.RES_PQ, res_pq);
                         result.put(Parser.NONCE, nonce);
                         result.put(Parser.SERVER_NONCE, server_nonce);
                         Log.v("PARSER", "AUTH: " + auth_key);
                         Log.v("PARSER", "Message ID: " + message_id);
                         Log.v("PARSER", "message_length: " + message_length);
                         Log.v("PARSER", "RES_PQ: " + res_pq);
                         Log.v("PARSER", "NONCE: " + Utils.byteArrayToHex(nonce));
                         Log.v("PARSER", "Server_NONCE: " + Utils.byteArrayToHex(server_nonce));
                         byte[] enc_ansver = new byte[596];
                         ByteBuffer.wrap(response, 64, enc_ansver.length).get(enc_ansver);
                         result.put(Parser.ENC_ANSWER, enc_ansver);
                         Log.v("PARSER", "encrypted_answer: " + Utils.byteArrayToHex(enc_ansver));
 
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                     break;
 
                 case TYPE_DH_GEN_OK:
                     try {
                         byte[] nonce = new byte[16];
                         ByteBuffer.wrap(response, 4, 16).get(nonce);
                         byte[] server_nonce = new byte[16];
                         ByteBuffer.wrap(response, 20, server_nonce.length).get(server_nonce);
                         byte[] new_nonce_hash1 = new byte[4];
                         ByteBuffer.wrap(response, 36, new_nonce_hash1.length).get(new_nonce_hash1);
                         result.put(Parser.TYPE, TYPE_RES_DH);
                         result.put(Parser.NONCE, nonce);
                         result.put(Parser.SERVER_NONCE, server_nonce);
                         result.put(Parser.NEW_NONCE_HASH1, new_nonce_hash1);
                         Log.v("PARSER", "NONCE: " + Utils.byteArrayToHex(nonce));
                         Log.v("PARSER", "Server_NONCE: " + Utils.byteArrayToHex(server_nonce));
                         Log.v("PARSER", "New_nonce_hash1: " + Utils.byteArrayToHex(server_nonce));
 
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                     break;
 
                 default:
                     break;
             }
             return result;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
 
     }
 
     public void parseResponse() {
 
     }
 }
