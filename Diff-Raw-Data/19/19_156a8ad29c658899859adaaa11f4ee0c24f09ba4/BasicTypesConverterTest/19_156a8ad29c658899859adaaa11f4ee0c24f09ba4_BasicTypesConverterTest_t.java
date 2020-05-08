 package nl.spelberg.util.converter;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class BasicTypesConverterTest {
 
     @Test
     public void testNulls() throws Exception {
         assertNull(BasicTypesConverter.convert(null, String.class));
         assertNull(BasicTypesConverter.convert(null, Integer.class));
         assertNull(BasicTypesConverter.convert(null, UnknownType.class));
         try {
             BasicTypesConverter.convert("", null);
             fail();
         } catch (IllegalArgumentException e) {
             assertEquals("targetClass is null", e.getMessage());
         }
     }
 
    @SuppressWarnings({"StringEquality", "NumberEquality"})
     @Test
     public void testSameTypes() throws Exception {
         String myString = "My String";
         assertTrue(myString == BasicTypesConverter.convert(myString, String.class));
 
         Integer myInteger = Integer.valueOf(42);
         assertTrue(myInteger == BasicTypesConverter.convert(myInteger, Integer.class));
 
         UnknownType myUnknownType = new UnknownType();
         assertTrue(myUnknownType == BasicTypesConverter.convert(myUnknownType, UnknownType.class));
     }
 
     @Test
     public void testString() {
         assertEquals("42", BasicTypesConverter.convert(42, String.class));
         assertEquals("42", BasicTypesConverter.convert(Integer.valueOf(42), String.class));
     }
 
     @Test
     public void testInteger() {
         assertEquals(42, (int) BasicTypesConverter.convert(Integer.valueOf(42), int.class));
         assertEquals(42, (int) BasicTypesConverter.convert("42", int.class));
         assertEquals(Integer.valueOf(42), BasicTypesConverter.convert(Integer.valueOf(42), Integer.class));
         assertEquals(Integer.valueOf(42), BasicTypesConverter.convert("42", Integer.class));
     }
 
 
 
     @Test
     public void testUnknownConversion() throws Exception {
         UnknownType unknownType = new UnknownType();
         try {
             BasicTypesConverter.convert(unknownType, String.class);
             fail();
         } catch (UnsupportedOperationException e) {
             assertEquals("No converter available to convert from '" + UnknownType.class.getName() + "' to '" + String.class.getName() + "' for value '" + unknownType + "'", e.getMessage());
         }
     }
 
     private static class UnknownType {
 
     }
 }
