 package com.anthavio.httl;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.StringReader;
 import java.lang.reflect.Field;
 
 import org.testng.annotations.Test;
 
 import com.anthavio.httl.util.CodeGeneratingHandler;
 import com.anthavio.httl.util.FakeSender.FakeResponse;
 import com.anthavio.httl.util.JsonBuilder;
 import com.anthavio.httl.util.JsonInputGenerator;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 /**
  * 
  * @author martin.vanek
  *
  */
 public class UtilTest {
 
 	@Test
 	public void jsonBuilder() throws IOException {
 		String message = "Hello čobole";
 
 		String json = new JsonBuilder(true).object().field("message", message).object("request").array("headers").object()
 				.field("name", "xname").field("value", message).end().end().end().end().getJson();
 		ObjectMapper mapper = new ObjectMapper();
 		TestResponse value = mapper.readValue(json, TestResponse.class);
 
 		assertThat(value.getMessage()).isEqualTo(message);
 		assertThat(value.getRequest().getHeaders().get(0).getValue()).isEqualTo(message);
 	}
 
 	@Test
 	public void generator() throws Exception {
 
 		//String json = "{ \"f\" : { \"m\": 1.1, \"n\": 2.2 }, \"x\" : [ { \"x\" : \"a\"} , {\"x\" : \"c\"} ] }";
 		String json = JsonBuilder.OBJECT()//
 				.object("f").field("m", 1.1).field("n", "2012.12.05").end()//
 				.array("x")//
 				.object().field("x", "a").end()//
 				.object().field("x", "c").end()//
 				.end()//
 				.end()//
 				.getJson();
 		System.out.println(json);
 
 		//String json = "[ {\"f\" : { \"m\": 1.1, \"n\": \"2012.11.05\" }, \"x\" : [ { \"x\" : \"a\"} , {\"x\" : \"c\"} ]} ]";
 		//String json = "{ \"f\" :[ null , [2, \"x\" , \"y\"] , [ \"1\" , \"a\" , 2 ] ]}";
 		//String json = " [ {\"x\" : \"z\" } , { \"x\" : \"y\"} ]";
 
 		JsonInputGenerator generator = new JsonInputGenerator();
 		generator.setBaseClass(Object.class);
 		generator.addInterface(Serializable.class);
 		generator.setDateFormat("yyyy.MM.dd");
 
 		String className = "com.example.FromJson";
 		String javaCode = generator.process(className, new StringReader(json));
 		System.out.println(javaCode);
 		generator.compile(className, javaCode);
 
 		Class<?> class1 = Class.forName(className);
 		Field[] fields = class1.getDeclaredFields();
 		for (Field field : fields) {
 			System.out.println(field.getType() + " " + field.getName());
 		}
 
 		CodeGeneratingHandler handler = new CodeGeneratingHandler();
		SenderResponse response = new FakeResponse(200, "application/xml; charset=utf-8", json);
 		handler.onResponse(response);
 	}
 }
