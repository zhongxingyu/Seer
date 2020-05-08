 package com.lastcrusade.soundstream.net.message;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 
 import android.util.Log;
 
 /**
  * This class prepares message data to be sent or received.  It implements the following simple protocol:
  *  4 byte length of the message, not including the length (using java.nio.ByteBuffer)
  *  String canonical class name
  *  \n character
  *  Message data
  *  ...
  *  
  * This protocol supports stacking multiple messages (in that same format) into the same stream.  Each message will
  * have a length that describes that message.
  * 
  * @author Jesse Rosalia
  *
  */
 public class Messenger {
 
     private static final char END_OF_CLASS_CHAR = '\n';
 
     private static final String TAG = Messenger.class.getName();
 
     //package protected, so they can be accessed from the 
     
     static final int SIZE_LEN = 4;
     static final int VERSION_LEN = 4;
 
     static final int MESSENGER_VERSION = 1;
 
 
     private IMessage receivedMessage;
 
     private int messageLength;
 
     private ByteArrayOutputStream inputBuffer = new ByteArrayOutputStream();
 
     private ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
 
     private byte[] inBytes = new byte[16384];
     /**
      * Serialize a message into the output buffer.  This will append to the output
      * buffer, to stack multiple messages next to each other.  See clearOutputBuffer
      * to clear this buffer.
      * 
      * @param message
      * @throws IOException
      */
     public void serializeMessage(IMessage message) throws IOException {
         int start = outputBuffer.size();
         outputBuffer.write(new byte[SIZE_LEN]);
         outputBuffer.write(new byte[VERSION_LEN]);
         outputBuffer.write(message.getClass().getCanonicalName().getBytes());
         outputBuffer.write(END_OF_CLASS_CHAR);
 
         message.serialize(outputBuffer);
 
         //write the length to the first 4 bytes of this message
         byte[] bytes = outputBuffer.toByteArray();
         ByteBuffer bb = ByteBuffer.wrap(bytes, start, SIZE_LEN + VERSION_LEN);
         //the message length includes everything but SIZE_LEN bytes of the message
         int len = outputBuffer.size() - start - SIZE_LEN;
         bb.putInt(len);
         bb.putInt(MESSENGER_VERSION);
 
         outputBuffer.reset();
         outputBuffer.write(bytes);
     }
 
     /**
      * Clear the output buffer of all messages.
      * 
      */
     public void clearOutputBytes() {
         outputBuffer.reset();
     }
 
     /**
      * Get the output bytes for this messenger.  This should contain all of the serialized messages
      * since the last time the messenger was cleared.
      * 
      * NOTE: this method will not clear the messenger itself.
      * 
      * @return
      */
     public byte[] getOutputBytes() {
         return outputBuffer.toByteArray();
     }
 
     /**
      * Deserialize a message in the input stream, and store the result in the receivedMessage field.
      * This may be called multiple times with partial messages (in case the message is not all here yet).
      * 
      * Only one received message may be held at a time, so be prepared to call getReceivedMessage if this
      * method returns true.
      * 
      * This is designed to block until a full message is received, and will throw an exception if the
      * socket is closed unexpectedly.
      * 
      * @param input
      * @return
     * @throws Exception If the message class does not exist, or is not defined properly, or
     * if the stream closes prematurely.
      */
     public boolean deserializeMessage(InputStream input) throws Exception {
         
         boolean processed = false;
         do {
             //read a chunk at a time...the buffer size was determined through trial and error, and
             // could be optimized more.
             //NOTE: this is so input.read can block, and will throw an exception when the connection
             // goes down.  this is the only way we'll get a notification of a downed client
             int read = input.read(inBytes);
             if (read > 0) {
                 inputBuffer.write(inBytes, 0, read);
             } else {
                 inputBuffer.write(inBytes);
             }
 
             //if we need to, consume the message length (to make sure we read until we have a complete message)
             if (this.messageLength <= 0 && inputBuffer.size() >= SIZE_LEN) {
                 byte[] bytes = inputBuffer.toByteArray();
                 ByteBuffer bb = ByteBuffer.wrap(bytes, 0, SIZE_LEN);
                 //this actually consumes the first 4 bytes (removes it from the stream)
                 inputBuffer.reset();
                 inputBuffer.write(bytes, SIZE_LEN, bytes.length - SIZE_LEN);
                 this.messageLength = bb.getInt();
             }
             //check to see if we can process this message
             if (this.messageLength > 0 && inputBuffer.size() >= this.messageLength) {
                 processed = processAndConsumeMessage();
             }
             //loop back around if we havent processed a message yet
         } while (!processed);
         return processed;
     }
     
     /**
      * Process and consume one message contained in the input buffer.  This will modify the contents of the
      * input buffer when successful and when an error is occurred (the message in error is thrown away).
      * 
      * TODO: this needs better error handling.
      * 
      * @return True if a message was processed, false if not
      * @throws ClassNotFoundException 
      * @throws IllegalAccessException 
      * @throws InstantiationException 
      * @throws IOException 
      */
     private boolean processAndConsumeMessage() throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
         boolean processed = false;
         //REVIEW: character encoding issues may arise, but since we're controlling the class names
         // we should be able to decide how to handle these
         byte[] bytes = inputBuffer.toByteArray();
         try {
             int start = 0;
             ByteBuffer bb = ByteBuffer.wrap(bytes, 0, VERSION_LEN);
             int messengerVersion = bb.getInt();
             //TODO: actually do something with the messengerVersion
             start += VERSION_LEN;
             int nameEnd = start;
             for (; nameEnd < bytes.length && bytes[nameEnd] != END_OF_CLASS_CHAR; nameEnd++) {}
             String messageName = new String(bytes, start, nameEnd - start);
             ByteArrayInputStream bais = new ByteArrayInputStream(bytes, nameEnd + 1, this.messageLength - (nameEnd + 1));
 
             Object obj = Class.forName(messageName).newInstance();
             if (obj instanceof IMessage) {
                 //we've created the message...deserialize it and store it
                 ((IMessage)obj).deserialize(bais);
                 this.receivedMessage = ((IMessage)obj);
                 processed = true;
             } else {
                 //otherwise, it's a WTF
                 Log.wtf(TAG, "Received message '" + messageName + "', but it does not implement IMessage");
             }
         } finally {
             //consume this message either way
             int bufferLen = inputBuffer.size();
             inputBuffer.reset();
             inputBuffer.write(bytes, this.messageLength, bufferLen - this.messageLength);
             this.messageLength = 0;
         }
         return processed;
     }
 
     /**
      * Get the last received message processed by this messenger.
      * 
      * @return
      */
     public IMessage getReceivedMessage() {
         return receivedMessage;
     }
 }
