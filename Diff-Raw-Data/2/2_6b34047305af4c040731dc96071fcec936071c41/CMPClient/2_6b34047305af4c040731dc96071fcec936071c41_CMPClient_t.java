 package ru.direvius.cmp;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.security.GeneralSecurityException;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author direvius
  */
 public class CMPClient {
 
     private static final Logger logger = LoggerFactory.getLogger(CMPClient.class);
     
     private final InputStream is;
     private final OutputStream os;
     private static final int ENQ_RESP_LEN = 8;
     private static final byte[] PROTO_VERSION = {0x1, 0x0};
     private static final byte STX = 0x02;
     private static final byte ETX = 0x03;
     private static final byte EOT = 0x04;
     private static final byte ENQ = 0x05;
     private static final byte ACK = 0x06;
     private static final byte BEL = 0x07;
     private static final byte NAK = 0x15;
     private static final String TDES_TRANSFORMATION = "DESede/ECB/NoPadding";
     private static final String DES_TRANSFORMATION = "DES/ECB/NoPadding";
     private static final byte[] TERMINAL_KEY = hexStringToByteArray("C3-5F-ED-30-8F-9C-34-F4-E7-E4-83-3C-67-67-13-EC-24-08-0C-3E-34-C8-F7-BD");
     private static final byte[] TRANSPORT_KEY = hexStringToByteArray("EF-B1-CF-18-5A-9E-DB-B7-0E-C7-F8-0A-E7-28-9A-EA-24-45-1A-EB-4E-09-E2-FE");
     private static final SecretKey terminalKey = new SecretKeySpec(TERMINAL_KEY, "DESede");
     private static final SecretKey transportKey = new SecretKeySpec(TRANSPORT_KEY, "DESede");
     private static final Encrypter tdesTerminalEncrypter;
     private static final Encrypter tdesTransportEncrypter;
     private Encrypter sessionEncrypter;
     private byte[] sessionKeyArray;
     private enum State {
 
         DOWN, ESTABLISHED
     }
     private State state = State.DOWN;
     private final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
     private final ScheduledFuture<?> keepAliver;
     private SecretKey sessionKey;
     static {
         try {
             tdesTerminalEncrypter = new Encrypter(TDES_TRANSFORMATION,terminalKey);
             tdesTransportEncrypter = new Encrypter(TDES_TRANSFORMATION,transportKey);
         } catch (GeneralSecurityException ex) {
             logger.error("Could not initialize encrypter for CMPClient", ex);
             throw new RuntimeException(ex);
         } 
     }
     public CMPClient(InputStream is, OutputStream os){
         this.is = new BufferedInputStream(is);
         this.os = new BufferedOutputStream(os);
         if(logger.isDebugEnabled())logger.debug("Transport key: {}", byteArrayToString(TRANSPORT_KEY));
         if(logger.isDebugEnabled())logger.debug("Terminal key: {}", byteArrayToString(TERMINAL_KEY));
         logger.debug("Scheduling a keep-aliver...");
         keepAliver = ses.scheduleAtFixedRate(new KeepAliver(), 3, 3, TimeUnit.SECONDS);
     }
     private class KeepAliver implements Runnable {
         public void run() {
             try {
                 keepAlive();
             } catch (IOException ex) {
                 logger.error("IOException while trying to automatically keep the connection alive.", ex);
             }
         }
     }
 
     public synchronized void keepAlive() throws IOException {
         if(state == State.ESTABLISHED){
             os.write(BEL);
             os.flush();
             int respCode = is.read();
             if(logger.isDebugEnabled())logger.debug("Sent keep-alive, received {}", String.format("0x%02X",respCode));
         }
     }
     public void sendEncrypt(byte[] message) throws IOException, GeneralSecurityException {
         if(logger.isDebugEnabled()) logger.debug("Encrypting a message: {}", byteArrayToString(message));
         byte[] encryptedMessage;
         if(message.length%8 == 0){
             encryptedMessage = sessionEncrypter.encrypt(message);
         } else {
             int ordinalPartLength = (message.length / 8) * 8;
             ByteBuffer ordinalPart = ByteBuffer.allocate(ordinalPartLength);
             ByteBuffer lastBytes = ByteBuffer.allocate(message.length - ordinalPartLength);
             ordinalPart.put(message, 0, ordinalPartLength);
             lastBytes.put(message, ordinalPartLength, message.length - ordinalPartLength);
             ByteBuffer encryptedMessageBuffer = ByteBuffer.allocate(message.length);
            encryptedMessageBuffer.put(sessionEncrypter.encrypt(ordinalPart.array())).put(symmetric(lastBytes.array()));
             encryptedMessage=encryptedMessageBuffer.array();
         }
         send(encryptedMessage);
     }
     public synchronized void send(byte[] message) throws IOException {
         if(state!=State.ESTABLISHED) throw new IOException("send operation while connection is down");
         ByteBuffer bb = ByteBuffer.allocate(message.length + 3).putShort((short) message.length).put(message).put(ETX);
         ByteBuffer bb2 = ByteBuffer.allocate(bb.capacity() + 3);
         bb2.put(STX).put(bb.array()).put(crc16(bb.array()));
         byte [] msgWithCRC = bb2.array();
         if(logger.isDebugEnabled()) logger.debug("Message dump with CRC (STX(0x02)|MLEN|MSG|ETX(0x03)|CRC16: {}", byteArrayToString(msgWithCRC));
         os.write(msgWithCRC);
         os.flush();
         if(logger.isDebugEnabled())logger.debug("Sent a message: {}", byteArrayToString(message));
         int respCode = is.read();
         if(logger.isDebugEnabled())logger.debug("Response code: {}", String.format("0x%02X",respCode));
         switch(respCode){
             case ACK:
                 logger.debug("Server responded with ACK");
                 break;
             case NAK:
                 throw new IOException("Server responded with NAK");
             default:
                 throw new IOException("Unknown response code");
         }
     }
     public synchronized void open() throws IOException {
         if(state==State.ESTABLISHED) return; // already opened
         logger.debug("Opening connection...");
         os.write(ENQ);
         os.write(PROTO_VERSION);
         os.flush();
         logger.debug("Sent ENQ byte and protocol version ({})", PROTO_VERSION);
         if(is.read() == ACK){
             try {
                 byte[] buff = new byte[ENQ_RESP_LEN];
                 is.read(buff);
                 if(logger.isDebugEnabled())logger.debug("Received a crypted session key: {}", byteArrayToString(buff));
                 byte [] decryptedKey = tdesTransportEncrypter.decrypt(buff);
                 if(logger.isDebugEnabled())logger.debug("Decrypted a key: {}", byteArrayToString(decryptedKey));
                 decryptedKey[7] = 0x7;
                 if(logger.isDebugEnabled())logger.debug("Changed last byte to 0x07: {}", byteArrayToString(decryptedKey));
                 sessionKeyArray = tdesTerminalEncrypter.encrypt(decryptedKey);
                 if(logger.isDebugEnabled())logger.debug("Encrypted a key (session key): {}", byteArrayToString(sessionKeyArray));
                 sessionKey = new SecretKeySpec(sessionKeyArray, "DES");
                 //byte[] iv ={0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
                 //IvParameterSpec ips = new IvParameterSpec(iv);
                 sessionEncrypter = new Encrypter(DES_TRANSFORMATION, sessionKey);
                 state = State.ESTABLISHED;
                 logger.debug("Opened connection");
             } catch (GeneralSecurityException ex) {
                 logger.error("Could not make session key cryptography.", ex);
             }
         } else {
             throw new IOException("Could not establish connection");
         }
     }
     public byte[] receiveDecrypt() throws IOException, GeneralSecurityException {
         byte[] message = receive();
         byte[] decryptedMessage;
         if(message.length%8 == 0){
             decryptedMessage = sessionEncrypter.decrypt(message);
         } else {
             int ordinalPartLength = (message.length / 8) * 8;
             ByteBuffer ordinalPart = ByteBuffer.allocate(ordinalPartLength);
             ByteBuffer lastBytes = ByteBuffer.allocate(message.length - ordinalPartLength);
             ordinalPart.put(message, 0, ordinalPartLength);
             lastBytes.put(message, ordinalPartLength, message.length - ordinalPartLength);
             ByteBuffer decryptedMessageBuffer = ByteBuffer.allocate(message.length);
             decryptedMessageBuffer.put(sessionEncrypter.decrypt(ordinalPart.array())).put(symmetric(lastBytes.array()));
             decryptedMessage = decryptedMessageBuffer.array();
         }
         if(logger.isDebugEnabled()) logger.debug("Decrypted a message: {}", byteArrayToString(decryptedMessage));
         return decryptedMessage;
     }
     public synchronized byte[] receive() throws IOException { 
         logger.debug("Receiving message...");
         if(state!=State.ESTABLISHED) throw new IOException("receive operation while connection is down");
         //TODO: add STX, ETX, message length, crc.
         int stx = is.read();
         if(stx!=STX) throw new IOException("STX excepted but received: "+String.format("%02X", stx));
         int messageLength = (is.read()<<16) | is.read();
         if(logger.isDebugEnabled())logger.debug("Message length: {}", String.format("%02X", messageLength));
         byte[] buff = new byte[messageLength];
         int bytesRead = is.read(buff);
         if(bytesRead < messageLength) throw new IOException("Received message length is letter then it's been told: "+String.format("%04X vs %04X", bytesRead, messageLength));
         int etx = is.read();
         if(etx!=ETX) throw new IOException("ETX excepted but received: "+String.format("%02X", etx));
         int crc = (is.read()<<8) | is.read();
         if(logger.isDebugEnabled())logger.debug("Received a message: {}, crc: {}", byteArrayToString(buff), String.format("%02X", crc));
         os.write(ACK);
         return buff;
     }
     public synchronized void close() throws IOException {
         if(state!=State.ESTABLISHED) throw new IOException("close operation while connection is down");
         os.write(EOT);
         os.flush();
         keepAliver.cancel(true);
         is.close();
         os.close();
         state = State.DOWN;
         logger.debug("Closed connection");
     }
 
 
     public static byte[] crc16(byte[] buff) {
         int nCRC16 = 0;
         for (byte b : buff) {
             int dt = (int)b & 0x00FF;
             //logger.debug(String.format("b: %08X", dt));
             int a1 = (dt ^ nCRC16) & 0xFFFF;
             //logger.debug(String.format("a1: %08X", a1));
             int a2 = (a1 << 4) & 0xFFFF;
             //logger.debug(String.format("a2: %08X", a2));
             int a3 = (a1 ^ a2) & 0xFFFF;
             //logger.debug(String.format("a3: %08X", a3));
             nCRC16 = ((nCRC16 >> 8) ^ (a3 >> 12) ^ (a3 >> 5) ^ a3) & 0xFFFF;
             //logger.debug(String.format("crc: %08X", nCRC16));
         }
         ByteBuffer bb = ByteBuffer.allocate(2);
         bb.put((byte)((nCRC16 & 0xFF00) >> 8)).put((byte)(nCRC16 & 0xFF));
         if(logger.isDebugEnabled())logger.debug("CRC for {}: {}", byteArrayToString(buff), byteArrayToString(bb.array()));
         
         return bb.array();
     }
     public static String byteArrayToString(byte[] bytes){
         StringWriter sw = new StringWriter();
         sw.append(String.format("%02X", bytes[0]));
         for (int i=1; i<bytes.length; i++){
            sw.append(String.format("-%02X", bytes[i]));
         }
         return sw.toString();
     }
     public static byte[] hexStringToByteArray(String s){
         String[] bytes = s.split("-");
         ByteBuffer byteBuff = ByteBuffer.allocate(bytes.length);
         for(String b : bytes){
             byteBuff.put((byte)(Integer.parseInt(b, 16) - 0x100));
         }
         //logger.debug("Converted a hex string: {}", byteArrayToString(byteBuff.array()));
         return byteBuff.array();
     }
     private byte[] symmetric(byte []src){
         byte [] dst = new byte[src.length];
         for (int i = 0; i < src.length; i++){
             dst[i] = (byte)(src[i] ^ i ^ sessionKeyArray[i % 8]);
         }
         return dst;
     }
 }
