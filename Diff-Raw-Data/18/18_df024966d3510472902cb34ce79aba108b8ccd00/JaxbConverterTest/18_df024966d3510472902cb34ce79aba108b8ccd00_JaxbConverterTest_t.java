 package de.openknowledge.extensions.jsf.jaxb;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.Arrays;
 
 import javax.faces.convert.ConverterException;
 
 import org.junit.Test;
 
 public class JaxbConverterTest {
 
   private JaxbConverter converter = new JaxbConverter(JaxbTestObject.class);
   
   @Test
   public void getAsObject() throws ConverterException {
     JaxbTestObject object = (JaxbTestObject)converter.getAsObject(null, null, "{\"testAttribute\":\"testValue\",\"testValues\":[1,2,3]}");
     assertThat(object.getTestAttribute(), is("testValue"));
     assertThat(object.getTestValues().size(), is(3));
     assertThat(object.getTestValues().get(0), is(1));
     assertThat(object.getTestValues().get(1), is(2));
     assertThat(object.getTestValues().get(2), is(3));
   }
   
   @Test(expected = ConverterException.class)
   public void getAsObjectFails() throws ConverterException {
     converter.getAsObject(null, null, "{\"testAttribute\":\"testValue\",\"testValue\":1 2 3}");
   }
 
   @Test
   public void getAsString() throws ConverterException {
     String result = converter.getAsString(null, null, new JaxbTestObject("testValue", Arrays.asList(1, 2, 3)));
     assertThat(result, is("{\"testAttribute\":\"testValue\",\"testValues\":[1,2,3]}"));
   }
 
   @Test(expected = ConverterException.class)
   public void getAsStringFails() throws ConverterException {
	  converter.getAsString(null, null, "{\"testAttribute\":\"testValue\",\"testValue\":1 2 3}");
   }
 }
