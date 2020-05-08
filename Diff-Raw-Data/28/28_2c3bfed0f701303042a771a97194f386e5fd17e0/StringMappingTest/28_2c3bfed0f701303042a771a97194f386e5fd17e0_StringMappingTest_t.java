 package q.serialize.mapping;
 
 import java.io.StringWriter;
 import java.sql.Timestamp;
 import java.util.Date;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import q.serialize.convert.Convert;
 import q.serialize.convert.JSONConvert;
 import q.serialize.mapping.MappingException;
 import q.serialize.mapping.OperationCodeException;
 import q.serialize.mapping.StringMapping;
 
 
 public class StringMappingTest {
 
 	@Test
 	public void testEscape() throws MappingException, OperationCodeException {
 		String source = "\\'1234\\'";
 
 		System.out.println(source);
 
 		StringMapping mapping = new StringMapping();
 		mapping.mapping(source, null, false);
 
 		StringWriter writer = new StringWriter();
 		Convert convert = new JSONConvert(writer);
 		mapping.write(convert, source, null, "str", false);
 		//mapping.write(writer, source, null, "json", "str", false);
 
 		System.out.println(writer.toString());
		Assert.assertEquals("\"str\":\"\\\\'1234\\\\'\"", writer.toString());
 	}
 
 	@Test
 	public void testEmpty() throws MappingException, OperationCodeException {
 		StringMapping mapping = new StringMapping();
 		Assert.assertNull(mapping.mapping(null, null, false));
 		Assert.assertEquals("", mapping.mapping("", null, false));
 	}
 	
 	@Test
 	public void testTimestamp() throws MappingException, OperationCodeException {
 		StringMapping mapping = new StringMapping();
 		Timestamp b = new Timestamp(1265703555000l);
 		Date c = b;
 		Assert.assertEquals("2010-02-09 16:19:15", mapping.mapping(c, null, false));
 	}
 }
