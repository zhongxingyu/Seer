 package common.json;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Calendar;
 
 import org.junit.Test;
 
 import common.json.JsonWriter;
 
 public class JsonWriterTest {
 	
 	@Test
 	public void testStringProperty() throws IOException {
 		StringWriter stringWriter = new StringWriter();
 		JsonWriter writer = null;
 		try {
 			writer = new JsonWriter(stringWriter);
 			writer.startObject();
 			writer.name("foo").value("bar");
 			writer.endObject();
 		} finally {
 			if(writer != null) {
 				writer.close();
 			}
 		}
 		
 		String expected = "{\"foo\":\"bar\"}";
 		String actual = stringWriter.toString();
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void testBooleanProperty() throws IOException {
 		StringWriter stringWriter = new StringWriter();
 		JsonWriter writer = null;
 		try {
 			writer = new JsonWriter(stringWriter);
 			writer.startObject();
 			writer.name("closed").value(true);
 			writer.name("open").value(false);
 			writer.endObject();
 		} finally {
 			if(writer != null) {
 				writer.close();
 			}
 		}
 		
 		String expected = "{\"closed\":true,\"open\":false}";
 		String actual = stringWriter.toString();
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void testNumberProperty() throws IOException {
 		StringWriter stringWriter = new StringWriter();
 		JsonWriter writer = null;
 		try {
 			writer = new JsonWriter(stringWriter);
 			writer.startObject();
 			writer.name("count").value(Integer.valueOf(10));
 			writer.endObject();
 		} finally {
 			if(writer != null) {
 				writer.close();
 			}
 		}
 		
 		String expected = "{\"count\":10}";
 		String actual = stringWriter.toString();
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void testNestedObjectProperty() throws IOException {
 		StringWriter stringWriter = new StringWriter();
 		JsonWriter writer = null;
 		try {
 			writer = new JsonWriter(stringWriter);
 			writer.startObject();
 			writer.name("foo").value("bar");
 			writer.name("user");
 			writer.startObject();
 			writer.name("name").value("Jared");
 			writer.endObject();
 			writer.name("fiz").value("biz");
 			writer.endObject();
 		} finally {
 			if(writer != null) {
 				writer.close();
 			}
 		}
 		
 		String expected = "{\"foo\":\"bar\",\"user\":{\"name\":\"Jared\"},\"fiz\":\"biz\"}";
 		String actual = stringWriter.toString();
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void testLongProperty() throws IOException {
 		StringWriter stringWriter = new StringWriter();
 		JsonWriter writer = null;
 		try {
 			writer = new JsonWriter(stringWriter);
 			writer.startObject();
 			writer.name("sum").value(10l);
 			writer.endObject();
 		} finally {
 			if(writer != null) {
 				writer.close();
 			}
 		}
 		
 		String expected = "{\"sum\":10}";
 		String actual = stringWriter.toString();
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void testDateProperty() throws IOException {
 		Calendar calendar = Calendar.getInstance();
 		calendar.clear();
		calendar.set(Calendar.MONTH, 7); //august
 		calendar.set(Calendar.DATE, 10);
 		calendar.set(Calendar.YEAR, 2013);
 		
 		StringWriter stringWriter = new StringWriter();
 		JsonWriter writer = null;
 		try {
 			writer = new JsonWriter(stringWriter);
 			writer.startObject();
 			writer.name("created").value(calendar.getTime());
 			writer.endObject();
 		} finally {
 			if(writer != null) {
 				writer.close();
 			}
 		}
 		
		String expected = "{\"created\":1376118000000}";
 		String actual = stringWriter.toString();
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void testStartArray() throws IOException {
 		StringWriter stringWriter = new StringWriter();
 		JsonWriter writer = null;
 		try {
 			writer = new JsonWriter(stringWriter);
 			writer.startArray();
 			writer.value(1);
 			writer.value(2);
 			writer.value("three");
 			
 			writer.startObject();
 			writer.name("fruit").value("apple");
 			writer.endObject();
 			
 			writer.endArray();
 		} finally {
 			if(writer != null) {
 				writer.close();
 			}
 		}
 		
 		String expected = "[1,2,\"three\",{\"fruit\":\"apple\"}]";
 		String actual = stringWriter.toString();
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void testStartArrayInObject() throws IOException {
 		StringWriter stringWriter = new StringWriter();
 		JsonWriter writer = null;
 		try {
 			writer = new JsonWriter(stringWriter);
 			writer.startObject();
 			writer.name("values");
 			writer.startArray();
 			writer.value(1);
 			writer.value(2);
 			writer.value("three");
 			
 			writer.startObject();
 			writer.name("fruit").value("apple");
 			writer.endObject();
 			
 			writer.endArray();
 			writer.endObject();
 		} finally {
 			if(writer != null) {
 				writer.close();
 			}
 		}
 		
 		String expected = "{\"values\":[1,2,\"three\",{\"fruit\":\"apple\"}]}";
 		String actual = stringWriter.toString();
 		assertEquals(expected, actual);
 	}
 	
 }
