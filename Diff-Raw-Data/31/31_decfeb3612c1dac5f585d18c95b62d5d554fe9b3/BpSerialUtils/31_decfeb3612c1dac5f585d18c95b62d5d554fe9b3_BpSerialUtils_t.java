 package com.janrain.backplane.common;
 
 
 import com.janrain.backplane.config.BpServerConfig;
 import com.janrain.backplane.server1.BusConfig1;
 import com.janrain.backplane.server2.*;
 import com.janrain.backplane.server2.oauth2.AuthorizationDecisionKey;
 import com.janrain.backplane.server2.oauth2.AuthorizationRequest;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Copied from apache SerializationUtils for customization.
  */
 public class BpSerialUtils {
 
     /**
      * <p>Serializes an <code>Object</code> to the specified stream.</p>
      *
      * <p>The stream will be closed once the object is written.
      * This avoids the need for a finally clause, and maybe also exception
      * handling, in the application code.</p>
      *
      * <p>The stream passed in is not buffered internally within this method.
      * This is the responsibility of your application if desired.</p>
      *
      * @param obj  the object to serialize to bytes, may be null
      * @param outputStream  the stream to write to, must not be null
      * @throws IllegalArgumentException if <code>outputStream</code> is <code>null</code>
      * @throws SerializationException (runtime) if the serialization fails
      */
     public static void serialize(Serializable obj, OutputStream outputStream) {
         if (outputStream == null) {
             throw new IllegalArgumentException("The OutputStream must not be null");
         }
         ObjectOutputStream out = null;
         try {
             // stream closed in the finally
             out = new ObjectOutputStream(outputStream);
             out.writeObject(obj);
 
         } catch (IOException ex) {
             throw new BpSerializationException(ex);
         } finally {
             try {
                 if (out != null) {
                     out.close();
                 }
             } catch (IOException ex) {
                 // ignore close exception
             }
         }
     }
 
     /**
      * <p>Serializes an <code>Object</code> to a byte array for
      * storage/serialization.</p>
      *
      * @param obj  the object to serialize to bytes
      * @return a byte[] with the converted Serializable
      * @throws SerializationException (runtime) if the serialization fails
      */
     public static byte[] serialize(Serializable obj) {
         ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
         serialize(obj, baos);
         return baos.toByteArray();
     }
 
     /**
      * <p>Deserializes an <code>Object</code> from the specified stream.</p>
      *
      * <p>The stream will be closed once the object is written. This
      * avoids the need for a finally clause, and maybe also exception
      * handling, in the application code.</p>
      *
      * <p>The stream passed in is not buffered internally within this method.
      * This is the responsibility of your application if desired.</p>
      *
      * @param inputStream  the serialized object input stream, must not be null
      * @return the deserialized object
      * @throws IllegalArgumentException if <code>inputStream</code> is <code>null</code>
      * @throws SerializationException (runtime) if the serialization fails
      */
     public static <T> T deserialize(InputStream inputStream) {
         if (inputStream == null) {
             throw new IllegalArgumentException("The InputStream must not be null");
         }
         ObjectInputStream in = null;
         try {
             // stream closed in the finally
             in = new BpCompatObjectInputStream(inputStream);
             return (T) in.readObject();
 
         } catch (ClassNotFoundException ex) {
             throw new BpSerializationException(ex);
         } catch (IOException ex) {
             throw new BpSerializationException(ex);
         } finally {
             try {
                 if (in != null) {
                     in.close();
                 }
             } catch (IOException ex) {
                 // ignore close exception
             }
         }
     }
 
     /**
      * <p>Deserializes a single <code>Object</code> from an array of bytes.</p>
      *
      * @param objectData  the serialized object, must not be null
      * @return the deserialized object
      * @throws IllegalArgumentException if <code>objectData</code> is <code>null</code>
      * @throws SerializationException (runtime) if the serialization fails
      */
     public static <T> T deserialize(byte[] objectData) {
         if (objectData == null) {
             throw new IllegalArgumentException("The byte[] must not be null");
         }
         ByteArrayInputStream bais = new ByteArrayInputStream(objectData);
         return BpSerialUtils.<T>deserialize(bais);
     }
 
     private static class BpCompatObjectInputStream extends ObjectInputStream {
 
         public BpCompatObjectInputStream(InputStream in) throws IOException {
             super(in);
         }
 
         @Override
         protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
             ObjectStreamClass classDesc = super.readClassDescriptor();
             String className = classDesc.getName();
            return renamedClasses.containsKey(className) ? renamedClasses.get(className) : classDesc;
         }
 
         // - PRIVATE
 
        private static Map<String,ObjectStreamClass> renamedClasses = new HashMap<String, ObjectStreamClass>() {{
            put("com.janrain.backplane.server.config.BpServerConfig", ObjectStreamClass.lookup(BpServerConfig.class));
            put("com.janrain.backplane.server.BackplaneMessage", ObjectStreamClass.lookup(com.janrain.backplane.server1.BackplaneMessage.class));
            put("com.janrain.backplane.server.BusConfig1", ObjectStreamClass.lookup(BusConfig1.class));
            put("com.janrain.backplane2.server.config.Client", ObjectStreamClass.lookup(Client.class));
            put("com.janrain.backplane2.server.Token", ObjectStreamClass.lookup(Token.class));
            put("com.janrain.backplane2.server.config.BusConfig2", ObjectStreamClass.lookup(BusConfig2.class));
            put("com.janrain.backplane2.server.config.User", ObjectStreamClass.lookup(User.class));
            put("com.janrain.backplane2.server.AuthSession", ObjectStreamClass.lookup(AuthSession.class));
            put("com.janrain.backplane2.server.BackplaneMessage", ObjectStreamClass.lookup(com.janrain.backplane.server2.BackplaneMessage.class));
            put("com.janrain.backplane2.server.Channel", ObjectStreamClass.lookup(Channel.class));
            put("com.janrain.backplane2.server.Grant", ObjectStreamClass.lookup(Grant.class));
            put("com.janrain.oauth2.AuthorizationRequest", ObjectStreamClass.lookup(AuthorizationRequest.class));
            put("com.janrain.oauth2.AuthorizationDecisionKey", ObjectStreamClass.lookup(AuthorizationDecisionKey.class));
         }};
     }
 
     private BpSerialUtils() { }
 }
 
