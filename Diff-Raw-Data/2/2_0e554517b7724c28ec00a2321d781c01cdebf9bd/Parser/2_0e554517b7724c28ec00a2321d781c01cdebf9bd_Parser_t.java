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
     public static final String GA = "g_a";
     public static final String GB = "g_B";
     public static final String SERVER_TIME = "server_time";
     public static final String DH_PRIME = "dh_prime";
     public static final String G = "g";
     public static final String TYPE_RES_PQ = Utils.byteArrayToHex(Utils.hexStringToByteArray("05162463"));
     public static final String TYPE_RES_DH = Utils.byteArrayToHex(Utils.hexStringToByteArray("d0e8075c"));
     public static final String TYPE_server_DH_inner_data = Utils.byteArrayToHex(Utils.hexStringToByteArray("b5890dba"));
     public static final String TYPE_DH_GEN_OK = Utils.byteArrayToHex(Utils.hexStringToByteArray("3bcbf734"));
 
     public static HashMap<String, Object> parseResponse(byte[] response) {
 
         try {
 
 
             //Get server_function code
             byte[] code = new byte[4];
             ByteBuffer.wrap(response, 28, code.length).get(code);
             Utils.reverseArray(code);
             String server_function = Utils.byteArrayToHex(code);
 
             if (server_function.equals(TYPE_RES_PQ)) {
                 return parseTYPE_RES_PQ(response);
             }
             if (server_function.equals(TYPE_RES_DH)) {
                 return parseTYPE_RES_DH(response);
             }
             if (server_function.equals(TYPE_DH_GEN_OK)) {
                 return parseTYPE_DH_GEN_OK(response);
             }
 
 
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
         return null;
     }
 
     public static HashMap<String, Object> parseTYPE_RES_PQ(byte[] response) {
 
         //Get message
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
         long res_code = ByteBuffer.wrap(message, 28, 4).order(ByteOrder.BIG_ENDIAN).getInt();
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
         result.put(Parser.RES_PQ, res_code);
         result.put(Parser.NONCE, nonce);
         result.put(Parser.SERVER_NONCE, server_nonce);
         result.put(Parser.PQ, pq);
         Log.v("PARSER", "AUTH: " + auth_key);
         Log.v("PARSER", "Message ID: " + message_id);
         Log.v("PARSER", "message_length: " + message_length);
         Log.v("PARSER", "RES_PQ: " + res_code);
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
         return result;
     }
 
     public static HashMap<String, Object> parseTYPE_RES_DH(byte[] response) {
 
         //Get message
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
         long res_code = ByteBuffer.wrap(message, 28, 4).order(ByteOrder.BIG_ENDIAN).getInt();
         byte[] nonce = new byte[16];
         ByteBuffer.wrap(response, 32, 16).get(nonce);
         byte[] server_nonce = new byte[16];
         ByteBuffer.wrap(response, 48, server_nonce.length).get(server_nonce);
         result.put(Parser.TYPE, TYPE_RES_DH);
         result.put(Parser.AUTH, auth_key);
         result.put(Parser.MESSAGE_ID, message_id);
         result.put(Parser.MESSAGE_LENGTH, message_length);
         result.put(Parser.RES_PQ, res_code);
         result.put(Parser.NONCE, nonce);
         result.put(Parser.SERVER_NONCE, server_nonce);
         Log.v("PARSER", "AUTH: " + auth_key);
         Log.v("PARSER", "Message ID: " + message_id);
         Log.v("PARSER", "message_length: " + message_length);
         Log.v("PARSER", "RES_CODE: " + res_code);
         Log.v("PARSER", "NONCE: " + Utils.byteArrayToHex(nonce));
         Log.v("PARSER", "Server_NONCE: " + Utils.byteArrayToHex(server_nonce));
         byte[] enc_answer = new byte[592];
         ByteBuffer.wrap(response, 68, enc_answer.length).get(enc_answer);
         result.put(Parser.ENC_ANSWER, enc_answer);
         Log.v("PARSER", "EncData: " + Utils.byteArrayToHex(enc_answer));
         return result;
     }
 
     public static HashMap<String, Object> parseTYPE_DH_GEN_OK(byte[] response) {
 
         //Get message
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
         long res_code = ByteBuffer.wrap(message, 28, 4).order(ByteOrder.BIG_ENDIAN).getInt();
         byte[] nonce = new byte[16];
         ByteBuffer.wrap(response, 4, 16).get(nonce);
         byte[] server_nonce = new byte[16];
         ByteBuffer.wrap(response, 20, server_nonce.length).get(server_nonce);
        byte[] new_nonce_hash1 = new byte[16];
         ByteBuffer.wrap(response, 36, new_nonce_hash1.length).get(new_nonce_hash1);
         result.put(Parser.TYPE, TYPE_DH_GEN_OK);
         result.put(Parser.NONCE, nonce);
         result.put(Parser.SERVER_NONCE, server_nonce);
         result.put(Parser.NEW_NONCE_HASH1, new_nonce_hash1);
         Log.v("PARSER", "NONCE: " + Utils.byteArrayToHex(nonce));
         Log.v("PARSER", "Server_NONCE: " + Utils.byteArrayToHex(server_nonce));
         Log.v("PARSER", "New_nonce_hash1: " + Utils.byteArrayToHex(server_nonce));
         return result;
     }
 
     public static HashMap<String, Object> parse_server_DH_inner_data(byte[] response) {
         HashMap<String, Object> result = new HashMap<String, Object>();
         //ByteBuffer buffer = ByteBuffer.wrap(response, 0, 4);
         //buffer.order(ByteOrder.LITTLE_ENDIAN);
         //int header_message_length = buffer.getInt();
         byte[] message = new byte[564];
         ByteBuffer.wrap(response, 0, message.length).get(message);
         byte[] res_code = new byte[4];
         ByteBuffer.wrap(message, 0, res_code.length).get(res_code);
         Utils.reverseArray(res_code);
         byte[] nonce = new byte[16];
         ByteBuffer.wrap(message, 4, 16).get(nonce);
         byte[] server_nonce = new byte[16];
         ByteBuffer.wrap(message, 20, server_nonce.length).get(server_nonce);
         byte[] g = new byte[4];
         ByteBuffer.wrap(message, 36, 4).get(g);
         byte[] dh_prime = new byte[260];
         ByteBuffer.wrap(message, 40, 260).get(dh_prime);
         byte[] g_a = new byte[260];
         ByteBuffer.wrap(message, 300, 260).get(g_a);
         byte[] server_time = new byte[4];
         ByteBuffer.wrap(message, 560, 4).get(server_time);
 
         result.put(Parser.TYPE, TYPE_server_DH_inner_data);
         result.put(Parser.NONCE, nonce);
         result.put(Parser.SERVER_NONCE, server_nonce);
         result.put(Parser.G, g);
         result.put(Parser.DH_PRIME, dh_prime);
         result.put(Parser.GA, g_a);
         result.put(Parser.SERVER_TIME, server_time);
         Log.v("PARSER", "RES_CODE: " + Utils.byteArrayToHex(res_code));
         Log.v("PARSER", "NONCE: " + Utils.byteArrayToHex(nonce));
         Log.v("PARSER", "Server_NONCE: " + Utils.byteArrayToHex(server_nonce));
         Log.v("PARSER", "G: " + Utils.byteArrayToHex(g));
         Log.v("PARSER", "DH_PRIME: " + Utils.byteArrayToHex(dh_prime));
         Log.v("PARSER", "GA: " + Utils.byteArrayToHex(g_a));
         Log.v("PARSER", "SERVER_TIME: " + Utils.byteArrayToHex(server_time));
         return result;
     }
 
 }
