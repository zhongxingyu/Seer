 package com.nearinfinity.honeycomb.mysql;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.UUID;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.avro.io.BinaryDecoder;
 import org.apache.avro.io.DatumReader;
 import org.apache.avro.io.DatumWriter;
 import org.apache.avro.io.DecoderFactory;
 import org.apache.avro.io.Encoder;
 import org.apache.avro.io.EncoderFactory;
 import org.apache.avro.specific.SpecificDatumReader;
 import org.apache.avro.specific.SpecificDatumWriter;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.nearinfinity.honeycomb.mysql.gen.TableSchema;
 
 /**
  * Utility class containing helper functions.
  */
 public class Util {
     private static final Logger logger = Logger.getLogger(Util.class);
 
     public static final int UUID_WIDTH = 16;
 
 
     private static BinaryDecoder binaryDecoder;
 
 
     /**
     * Returns a byte wide buffer from a {@link UUID}.
      *
     * @param uuid The {@link UUID} to convert
     * @return A byte array representation that is {@value #UUID_WIDTH} bytes wide
      */
     public static byte[] UUIDToBytes(UUID uuid) {
         checkNotNull(uuid, "uuid must not be null.");
         return ByteBuffer.allocate(UUID_WIDTH)
                 .putLong(uuid.getMostSignificantBits())
                 .putLong(uuid.getLeastSignificantBits())
                 .array();
     }
 
     /**
     * Create a {@link UUID} from the provided byte array.
      *
     * @param bytes A byte array that must be {@value #UUID_WIDTH} bytes wide, not null
     * @return A {@link UUID} representation
      */
     public static UUID BytesToUUID(byte[] bytes) {
         checkNotNull(bytes, "bytes must not be null.");
         checkArgument(bytes.length == UUID_WIDTH, "bytes must be of length " + UUID_WIDTH + ".");
         ByteBuffer buffer = ByteBuffer.wrap(bytes);
         return new UUID(buffer.getLong(), buffer.getLong());
     }
 
     public static byte[] serializeTableSchema(TableSchema schema) throws IOException {
         return serializeAvroObject(schema, TableSchema.class);
     }
 
     public static TableSchema deserializeTableSchema(byte[] schema) throws IOException {
         return deserializeAvroObject(schema, TableSchema.class);
     }
 
     /**
     * Serialize an object to a byte array
     * @param obj The object to serialize
     * @param clazz The type of the object being serialized
      * @return Serialized row
      * @throws IOException when serialization fails
      */
     public static <T> byte[] serializeAvroObject(T obj, Class<T> clazz) throws IOException {
         DatumWriter<T> writer = new SpecificDatumWriter<T>(clazz);
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
         writer.write(obj, encoder);
         encoder.flush();
         return out.toByteArray();
     }
 
     /**
      * Deserialize the provided serialized data into an instance of the specified class type
      * @param serializedData a buffer containing the serialized data
      * @param clazz the class type to instantiate to store the deserialized data
      * @return A new instance of the specified class representing the deserialized data
      * @throws IOException On deserialization reader failure
      */
     public static <T> T deserializeAvroObject(byte[] serializedData, Class<T> clazz) throws IOException {
         final DatumReader<T> userDatumReader = new SpecificDatumReader<T>(clazz);
         final ByteArrayInputStream in = new ByteArrayInputStream(serializedData);
         binaryDecoder = DecoderFactory.get().binaryDecoder(in, binaryDecoder);
 
         return userDatumReader.read(null, binaryDecoder);
     }
 
 
     public static String generateHexString(final byte[] bytes) {
         StringBuilder sb = new StringBuilder();
         for (byte b : bytes) {
             sb.append(String.format("%02X", b));
         }
 
         return sb.toString();
     }
 
     public static Configuration readConfiguration(File source)
             throws IOException, ParserConfigurationException, SAXException {
         Configuration params = new Configuration(false);
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.parse(source);
         NodeList options = doc.getElementsByTagName("adapteroption");
         logger.info(String.format("Number of options %d", options.getLength()));
         for (int i = 0; i < options.getLength(); i++) {
             Element node = (Element) options.item(i);
             String name = node.getAttribute("name");
             String nodeValue = node.getTextContent();
             logger.info(String.format("Node %s = %s", name, nodeValue));
             params.set(name, nodeValue);
         }
 
         return params;
     }
 }
