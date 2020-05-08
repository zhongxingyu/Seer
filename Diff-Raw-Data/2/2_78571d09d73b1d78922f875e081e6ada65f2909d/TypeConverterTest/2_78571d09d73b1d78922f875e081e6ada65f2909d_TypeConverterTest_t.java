 package net.sf.laja.parser.cdd.state;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 public class TypeConverterTest {
     private TypeConverter converter;
 
     @Before
     public void setUp() {
         converter = new TypeConverter();
     }
 
     @Test
     public void convertString() {
         assertThat(converter.asMutable("String"), equalTo("String"));
     }
 
     @Test
     public void convertState() {
         assertThat(converter.asMutable("AddressState"), equalTo("AddressMutableState"));
     }
 
     @Test
     public void convertImmutableSet() {
         assertThat(converter.asMutable("ImmutableSet"), equalTo("Set"));
     }
 
     @Test
     public void convertImmutableList() {
         assertThat(converter.asMutable("ImmutableList"), equalTo("List"));
     }
 
     @Test
     public void convertImmutableListToMutableString() {
        assertThat(converter.asMutableString("ImmutableList"), equalTo("List"));
     }
 
     @Test
     public void convertImmutableMap() {
         assertThat(converter.asMutable("ImmutableMap"), equalTo("Map"));
     }
 }
