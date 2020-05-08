 package net.jps.jx.jackson;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 import org.junit.runner.RunWith;
 import net.jps.jx.mapping.reflection.StaticFieldMapper;
 import org.codehaus.jackson.JsonFactory;
 
 import static net.jps.jx.jackson.TestClasses.*;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author zinic
  */
 @RunWith(Enclosed.class)
 public class JacksonJsonWriterTest {
    public static class WhenWritingSimpleObjects {
       @Test
       public void shouldProduceJsonThatMapps1to1Correctly() throws Exception {
          final MultiFieldMixedAnnotations expected = new MultiFieldMixedAnnotations();
          expected.setDefault("default");
          expected.setJsonNumber(4);
          expected.setStringField("field");
          expected.setXmlDouble(2.4);
          
          final JsonFactory jsonFactory = new JsonFactory();
          final JacksonJsonWriter<MultiFieldMixedAnnotations> jsonWriter = new JacksonJsonWriter<MultiFieldMixedAnnotations>(jsonFactory, StaticFieldMapper.getInstance());
          
          final ByteArrayOutputStream baos = new ByteArrayOutputStream();
          jsonWriter.write(expected, baos);
          
          final JacksonJsonReader<MultiFieldMixedAnnotations> jsonReader = new JacksonJsonReader<MultiFieldMixedAnnotations>(jsonFactory, StaticFieldMapper.getInstance(), MultiFieldMixedAnnotations.class);
          
          final MultiFieldMixedAnnotations rendered = jsonReader.read(new ByteArrayInputStream(baos.toByteArray()));
          
          assertEquals("", expected.getDefault(), rendered.getDefault());
          assertEquals("", expected.getJsonNumber(), rendered.getJsonNumber());
          assertEquals("", expected.getStringField(), rendered.getStringField());
         assertEquals("", expected.getXmlDouble(), rendered.getXmlDouble());
       }
    }
 }
