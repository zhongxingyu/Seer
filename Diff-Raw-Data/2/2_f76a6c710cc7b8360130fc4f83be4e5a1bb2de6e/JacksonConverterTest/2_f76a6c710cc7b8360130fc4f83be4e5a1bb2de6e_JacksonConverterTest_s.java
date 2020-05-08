 package com.othelle.jtuples.serialize;
 
 import com.othelle.jtuples.*;
 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.Test;
 
 import static com.othelle.jtuples.Tuples.tuple;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 /**
  * author: v.vlasov
  */
 public class JacksonConverterTest {
 
     private JacksonConverter converter;
 
     @Before
     public void setup() {
         converter = new JacksonConverter();
     }
 
     @Test
     public void testSerialization() {
         assertSerializeDeserialize(tuple(1), Tuple1.class);
         assertSerializeDeserialize(tuple(1, 2), Tuple2.class);
         assertSerializeDeserialize(tuple(1, 2, 3), Tuple3.class);
         assertSerializeDeserialize(tuple(1, 2, 3, 4), Tuple4.class);
         assertSerializeDeserialize(tuple(1, 2, 3, 4, 5), Tuple5.class);
         assertSerializeDeserialize(tuple(1, 2, 3, 4, 5, 6), Tuple6.class);
         assertSerializeDeserialize(tuple(1, 2, 3, 4, 5, 6, 7), Tuple7.class);
         assertSerializeDeserialize(tuple(1, 2, 3, 4, 5, 6, 7, 8), Tuple8.class);
         assertSerializeDeserialize(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9), Tuple9.class);
         assertSerializeDeserialize(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), Tuple10.class);
         assertSerializeDeserialize(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), Tuple11.class);
         assertSerializeDeserialize(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), Tuple12.class);
         assertSerializeDeserialize(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13), Tuple13.class);
         assertSerializeDeserialize(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14), Tuple14.class);
         assertSerializeDeserialize(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), Tuple15.class);
         assertSerializeDeserialize(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16), Tuple16.class);
     }
 
     @Test
     public void testWorkWithDifferentTypes() {
         assertSerializeDeserialize(tuple(1, "2", 3d), Tuple3.class);
     }
 
     @Test
     public void testSerializeLowerArity() {
         String serialized = converter.writeValueAsString(tuple(1, 2, 3));
         assertThat(converter.readValue(serialized.getBytes(), Tuple2.class), Matchers.equalTo((Tuple2) tuple(1, 2)));
     }
 
     @Test
     public void serializeUniProduct() {
         String serialized = converter.writeValueAsString(Tuples.convert("1", "2", "3"));
         Tuple4 deserialized = converter.readValue(serialized.getBytes(), Tuple4.class);
        assertThat(deserialized, Matchers.equalTo(tuple("1", "2", "3", null)));
     }
 
     private void assertSerializeDeserialize(Tuple tuple, Class<? extends Tuple> clazz) {
         String serialized = converter.writeValueAsString(tuple);
         assertThat(converter.readValue(serialized.getBytes(), clazz), Matchers.equalTo(tuple));
     }
 }
