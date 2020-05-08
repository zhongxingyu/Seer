 package com.jontodd.util.protobuf;
 
 import com.jontodd.util.avro.JellyBeanV1;
 import com.jontodd.util.avro.JellyBeanV2;
 import com.jontodd.util.avro.JellyBeanV3;
 import com.jontodd.util.avro.JellyBeanV4;
 import com.jontodd.util.avro.JellyBeanV5;
 import com.jontodd.util.avro.JellyBeanV6;
 import com.jontodd.util.avro.Size;
 import junit.framework.Assert;
 import org.apache.avro.AvroRuntimeException;
import org.apache.avro.AvroTypeException;
 import org.apache.avro.file.DataFileReader;
 import org.apache.avro.file.DataFileWriter;
 import org.apache.avro.file.SeekableByteArrayInput;
 import org.apache.avro.file.SeekableInput;
 import org.apache.avro.generic.GenericContainer;
 import org.apache.avro.io.DatumReader;
 import org.apache.avro.io.DatumWriter;
 import org.apache.avro.specific.SpecificDatumReader;
 import org.apache.avro.specific.SpecificDatumWriter;
 import org.apache.avro.util.Utf8;
 import org.testng.annotations.Test;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import static org.testng.Assert.assertEquals;
 
 /**
  * A series of unit tests to test understanding of protobufs with regards to schema evolution using the following set of
  * schemas:
  * <p/>
  * JellyBeanV1 - Base object with required fields name and id
  * JellyBeanV2 - Introduce new required color field
  * JellyBeanV3 - Add explicit default to an existing color property
  * JellyBeanV4 - Add an optional size w/o default to V1
  * JellyBeanV5 - Add a default the optional texture property in V4
  * JellyBeanV6 - Add a optional string without a default to V1
  *
  * @author Jon Todd
  */
 public class AvroTest {
 
     /*
      * Basic serialize / de-serialize tests
      */
 
     @Test
     public void testHappyPathSerializeDeserialize() throws IllegalStateException, IOException {
         JellyBeanV1 v1 = JellyBeanV1.newBuilder()
                 .setName("Liquorish")
                 .setId(1)
                 .build();
 
         byte[] serializedV1 = toByteArray(v1);
 
         JellyBeanV1 result = parseFrom(serializedV1, JellyBeanV1.class);
         assertEquals(result.getId(), new Integer(1));
         assertEquals(result.getName(), new Utf8("Liquorish"));
     }
 
     @Test(expectedExceptions = AvroRuntimeException.class)
     public void missingRequiredStringWithoutDefaultThrowsAtBuildTime() {
         JellyBeanV1 v1 = JellyBeanV1.newBuilder()
                 .setId(1)
                 .build();
     }
 
     @Test(expectedExceptions = AvroRuntimeException.class)
     public void nullRequiredStringWithoutDefaultThrowsAtBuildTime() {
         JellyBeanV1 v1 = JellyBeanV1.newBuilder()
                 .setId(1)
                 .setName(null)
                 .build();
     }
 
     @Test(expectedExceptions = AvroRuntimeException.class)
     public void missingRequiredIntWithoutDefaultThrowsAtBuildTime() throws Exception {
         JellyBeanV1 v1 = JellyBeanV1.newBuilder()
                 .setName("Liquorish")
                 .build();
     }
 
     // TODO: Difference - Avro doesn't for a required with a default to be set
     public void missingRequiredStringWithDefaultThrows() throws Exception {
         JellyBeanV3 v3 = JellyBeanV3.newBuilder()
                 .setName("Liquorish")
                 .setId(1)
                 // purposely leave out color
                 .build();
 
         assertEquals(v3.getColor(), new Utf8("Default"));
     }
 
     // TODO: Difference - Avro requires optional (union) types to have a default if the value isn't provided
     @Test(expectedExceptions = AvroRuntimeException.class)
     public void missingOptionalEnumWithoutDefaultChoosesFirst() throws Exception {
         byte[] serializedV4 = toByteArray(JellyBeanV4.newBuilder()
                 .setName("Liquorish")
                 .setId(3)
                         // Purposely leave out size
                 .build());
 
         JellyBeanV4 v4 = parseFrom(serializedV4, JellyBeanV4.class);
 
         assertEquals(v4.getSize(), Size.UNKNOWN);
     }
 
     // TODO: Difference - Avro requires optional (union) types to have a default if the value isn't provided
     @Test(expectedExceptions = AvroRuntimeException.class)
     public void missingOptionalStringWithoutDefaultChoosesEmptyString() throws Exception {
         byte[] serializedV6 = toByteArray(JellyBeanV6.newBuilder()
                 .setName("Liquorish")
                 .setId(3)
                         // Purposely leave out size
                 .build());
 
         JellyBeanV6 v6 = parseFrom(serializedV6, JellyBeanV6.class);
 
         assertEquals(v6.getTexture(), new Utf8(""));
     }
 
     /*
      * Schema evolution
      */
 
    @Test(expectedExceptions = AvroTypeException.class)
     public void introducingNewRequiredFieldWithoutDefault() throws IllegalStateException, IOException {
         byte[] serializedV1 = toByteArray(JellyBeanV1.newBuilder()
                 .setName("Liquorish")
                 .setId(1)
                 .build());
 
         byte[] serializedV2 = toByteArray(JellyBeanV2.newBuilder()
                 .setName("Liquorish")
                 .setId(2)
                 .setColor("Red")
                 .build());
 
         // Old parser new message should work
         parseFrom(serializedV2, JellyBeanV1.class);
 
         // New parser old message should fail
         try {
             parseFrom(serializedV1, JellyBeanV2.class);
             Assert.assertTrue("New parser on old message without required field should fail", false);
         } catch (IllegalStateException ex) {
             Assert.assertTrue(true); // Expect to get here
         }
     }
 
     /*
      * In this case V3 has a default set for color but it still fails parsing a V1 without a color set.
      */
    // TODO: Difference - removing a required field breaks protobuf, it doesn't break avro. It's odd that avro would allow this contract to be broken
     @Test
     public void introducingNewRequiredFieldWithDefault() throws IllegalStateException, IOException {
         byte[] serializedV1 = toByteArray(JellyBeanV1.newBuilder()
                 .setName("Liquorish")
                 .setId(1)
                 .build());
 
         byte[] serializedV3 = toByteArray(JellyBeanV3.newBuilder()
                 .setName("Liquorish")
                 .setId(2)
                 .setColor("Red")
                 .build());
 
         // Old parser new message should work
         parseFrom(serializedV3, JellyBeanV1.class);
 
         // New parser old message should fail
        parseFrom(serializedV1, JellyBeanV3.class);
     }
 
    // TODO: Difference - Avro doesn't allow optional fields to not have defaults
    @Test(expectedExceptions = AvroRuntimeException.class)
     public void introducingNewOptionalFieldWithoutDefault() throws IllegalStateException, IOException {
         byte[] serializedV1 = toByteArray(JellyBeanV1.newBuilder()
                 .setName("Liquorish")
                 .setId(1)
                 .build());
 
         byte[] serializedV4 = toByteArray(JellyBeanV4.newBuilder()
                 .setName("Liquorish")
                 .setId(3)
                         // Purposely leave out size
                 .build());
 
         // Old parser new message should work
         parseFrom(serializedV4, JellyBeanV1.class);
 
         // New parser old message should work
         parseFrom(serializedV1, JellyBeanV4.class);
     }
 
     @Test
     public void introducingNewOptionalFieldWithDefault() throws IllegalStateException, IOException {
         byte[] serializedV1 = toByteArray(JellyBeanV1.newBuilder()
                 .setName("Liquorish")
                 .setId(1)
                 .build());
 
         byte[] serializedV5 = toByteArray(JellyBeanV5.newBuilder()
                 .setName("Liquorish")
                 .setId(3)
                         // Purposely leave out size
                 .build());
 
         // Old parser new message should work
         parseFrom(serializedV5, JellyBeanV1.class);
 
         // New parser old message should work
         parseFrom(serializedV1, JellyBeanV5.class);
     }
 
 
     private <U extends GenericContainer> byte[] toByteArray(U bean) throws IOException {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         DatumWriter<U> userDatumWriter = new SpecificDatumWriter<U>(bean.getSchema());
         DataFileWriter<U> dataFileWriter = new DataFileWriter<U>(userDatumWriter);
         dataFileWriter.create(bean.getSchema(), outputStream);
         dataFileWriter.append(bean);
         dataFileWriter.close();
         System.out.println(outputStream.toString());
         return outputStream.toByteArray();
     }
 
     private <U extends GenericContainer> U parseFrom(byte[] bytes, Class<U> clazz) throws IOException {
         DatumReader<U> reader = new SpecificDatumReader<U>(clazz);
         SeekableInput input = new SeekableByteArrayInput(bytes);
         DataFileReader<U> dataFileReader = new DataFileReader<U>(input, reader);
         return dataFileReader.next();
     }
 
 }
