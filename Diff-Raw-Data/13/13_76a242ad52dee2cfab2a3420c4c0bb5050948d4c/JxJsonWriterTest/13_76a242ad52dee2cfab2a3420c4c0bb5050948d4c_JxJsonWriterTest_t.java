 package net.jps.jx.jackson;
 
 import com.rackspace.papi.components.limits.schema.Limits;
 import com.rackspace.papi.components.limits.schema.ObjectFactory;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Unmarshaller;
 import net.jps.jx.JsonWriter;
import net.jps.jx.mapping.reflection.StaticFieldMapper;
 import org.codehaus.jackson.JsonFactory;
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 import org.junit.runner.RunWith;
 
 /**
  *
  * @author zinic
  */
 @RunWith(Enclosed.class)
 public class JxJsonWriterTest {
 
    public static class WhenSerializingJxJaxbObjects {
 
       @Test
       public void should() throws Exception {
          final InputStream inputStream = JxJsonWriterTest.class.getResourceAsStream("/META-INF/xml/limits.xml");
 
          final JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
          final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
 
          final Limits limits = unwrap(unmarshaller.unmarshal(inputStream));
 
          final ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
          final JsonFactory jsonFactory = new JsonFactory();
          final JsonWriter jsonWriter = new JacksonJsonWriter(jsonFactory, StaticFieldMapper.getInstance());
 
          try {
             jsonWriter.write(limits, baos);
          } catch (Throwable ex) {
             ex.printStackTrace(System.out);
          }
 
          System.out.println("JSON Output\n" + new String(baos.toByteArray()) + "\n");
       }
 
       public Limits unwrap(Object o) {
          if (o instanceof JAXBElement) {
             return ((JAXBElement<Limits>) o).getValue();
          }
 
          return (Limits) o;
       }
    }
 }
