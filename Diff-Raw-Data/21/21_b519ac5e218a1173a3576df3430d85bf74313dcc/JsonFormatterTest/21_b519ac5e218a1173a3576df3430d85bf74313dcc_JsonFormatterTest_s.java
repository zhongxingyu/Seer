 package com.wesabe.xmlson.tests;
 
 import static org.junit.Assert.*;
 
 import java.math.BigDecimal;
 
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 import org.junit.runner.RunWith;
 
 import com.wesabe.xmlson.JsonFormatter;
 import com.wesabe.xmlson.XmlsonArray;
 import com.wesabe.xmlson.XmlsonFormatter;
 import com.wesabe.xmlson.XmlsonMember;
 import com.wesabe.xmlson.XmlsonObject;
 
 @RunWith(Enclosed.class)
 public class JsonFormatterTest {
 	private static String format(XmlsonMember member) {
 		final XmlsonFormatter formatter = new JsonFormatter();
 		return formatter.format(member);
 	}
 	
 	public static class An_Empty_Object {
 		@Test
 		public void itIsFormattedAsAnEmptyJsonObject() throws Exception {
 			final XmlsonObject object = new XmlsonObject("object");
 			
 			assertEquals(
 				"{}",
 				format(object)
 			);
 		}
 	}
 	
 	public static class An_Object_With_A_Null_Property {
 		@Test
 		public void itIsFormattedAsAJsonObjectWithAProperty() throws Exception {
 			final XmlsonObject object = new XmlsonObject("object");
 			object.addNullProperty("test");
 			
 			assertEquals(
 				"{" +
 					"\"test\":null" +
 				"}",
 				format(object)
 			);
 		}
 	}
 	
 	public static class An_Object_With_A_String_Property {
 		@Test
 		public void itIsFormattedAsAJsonObjectWithAProperty() throws Exception {
 			final XmlsonObject object = new XmlsonObject("object").addProperty("key", "value");
 			assertEquals(
 				"{" +
 					"\"key\":\"value\"" +
 				"}",
 				format(object)
 			);
 		}
 	}
 	
 	public static class An_Object_With_A_Multiline_String_Property {
 		@Test
 		public void itIsFormattedAsAJsonObjectWithAProperty() throws Exception {
 			final XmlsonObject object = new XmlsonObject("object").addProperty("key", "value\nvalue");
 			assertEquals(
 				"{" +
 					"\"key\":\"value\\nvalue\"" +
 				"}",
 				format(object)
 			);
 		}
 	}
 	
 	public static class An_Object_With_A_Boolean_Property {
 		@Test
 		public void itIsFormattedAsAJsonObjectWithAProperty() throws Exception {
 			final XmlsonObject object = new XmlsonObject("object").addProperty("key", false);
 			assertEquals(
 				"{" +
 					"\"key\":false" +
 				"}",
 				format(object)
 			);
 		}
 	}
 	
 	public static class An_Object_With_An_Integer_Property {
 		@Test
 		public void itIsFormattedAsAJsonObjectWithAProperty() throws Exception {
 			final XmlsonObject object = new XmlsonObject("object").addProperty("key", 100);
 			assertEquals(
 				"{" +
 					"\"key\":100" +
 				"}",
 				format(object)
 			);
 		}
 	}
 	
 	public static class An_Object_With_A_Long_Property {
 		@Test
 		public void itIsFormattedAsAJsonObjectWithAProperty() throws Exception {
 			final XmlsonObject object = new XmlsonObject("object").addProperty("key", 100L);
 			assertEquals(
 				"{" +
 					"\"key\":100" +
 				"}",
 				format(object)
 			);
 		}
 	}
 	
 	public static class An_Object_With_A_Float_Property {
 		@Test
 		public void itIsFormattedAsAJsonObjectWithAProperty() throws Exception {
 			final XmlsonObject object = new XmlsonObject("object").addProperty("key", 20.3F);
 			assertEquals(
 				"{" +
 					"\"key\":20.3" +
 				"}",
 				format(object)
 			);
 		}
 	}
 	
 	public static class An_Object_With_A_Double_Property {
 		@Test
 		public void itIsFormattedAsAJsonObjectWithAProperty() throws Exception {
 			final XmlsonObject object = new XmlsonObject("object").addProperty("key", 20.3);
 			assertEquals(
 				"{" +
 					"\"key\":20.3" +
 				"}",
 				format(object)
 			);
 		}
 	}
 	
 	public static class An_Object_With_A_BigDecimal_Property {
 		@Test
 		public void itIsFormattedAsAJsonObjectWithAProperty() throws Exception {
 			final XmlsonObject object = new XmlsonObject("object").addProperty("key", new BigDecimal("20.3"));
 			assertEquals(
 				"{" +
 					"\"key\":20.3" +
 				"}",
 				format(object)
 			);
 		}
 	}
 	
 	public static class An_Object_With_A_Member_Property {
 		@Test
 		public void itIsFormattedAsAJsonObjectWithAJsonObjectProperty() throws Exception {
 			final XmlsonObject object = new XmlsonObject("object").add(new XmlsonObject("property"));
 			assertEquals(
 				"{" +
 					"\"property\":{}" +
 				"}",
 				format(object)
 			);
 		}
 	}
 	
 	public static class An_Empty_Array {
 		@Test
 		public void itIsFormattedAsAJsonArray() throws Exception {
 			final XmlsonArray array = new XmlsonArray("array");
 			assertEquals(
 				"[]",
 				format(array)
 			);
 		}
 	}
 	
 	public static class An_Array_With_Members {
 		@Test
 		public void itIsFormattedAsAJsonArrayOfObjects() throws Exception {
 			final XmlsonArray array = new XmlsonArray("array");
 			array.add(new XmlsonObject("item").addProperty("name", "Mr. Peez"));
 			assertEquals(
 				"[" +
 					"{" +
 						"\"name\":\"Mr. Peez\"" +
 					"}" +
 				"]",
 				format(array)
 			);
 		}
 	}
 	
 	public static class An_Empty_Object_And_An_Empty_Hash {
 		@Test
 		public void itIsFormattedAsAHashWithAnArrayPropertyAndAnObjectProperty() throws Exception {
 			final XmlsonObject object = new XmlsonObject("root");
 			object.add(new XmlsonObject("object"));
 			object.add(new XmlsonArray("array"));
 			assertEquals(
 				"{" +
 					"\"object\":{}," +
 					"\"array\":[]" +
 				"}",
 				format(object)
 			);
 		}
 	}
 }
