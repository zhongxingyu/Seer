 package org.analogweb.xstream;
 
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsNull.nullValue;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
import java.text.SimpleDateFormat;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.analogweb.RequestAttributes;
 import org.analogweb.RequestContext;
 import org.analogweb.xstream.model.Foo;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author snowgoose
  */
 public class XStreamXmlTypeMapperTest {
 
     private XStreamXmlTypeMapper mapper;
     private RequestContext requestContext;
     private RequestAttributes attributes;
     private HttpServletRequest request;
 
     @Before
     public void setUp() throws Exception {
         mapper = new XStreamXmlTypeMapper();
         requestContext = mock(RequestContext.class);
         request = mock(HttpServletRequest.class);
         when(requestContext.getRequest()).thenReturn(request);
         attributes = mock(RequestAttributes.class);
     }
 
     @Test
     public void testMapToType() throws Exception {
         when(request.getContentType()).thenReturn("application/xml");
         InputStream from = new ByteArrayInputStream(
                "<?xml version=\"1.0\" ?><org.analogweb.xstream.model.Foo><name>foo</name><age>34</age><birthDay>1978-04-20 00:00:00.0 JST</birthDay></org.analogweb.xstream.model.Foo>"
                         .getBytes());
         Foo actual = (Foo) mapper.mapToType(requestContext, attributes, from, Foo.class, null);
         assertThat(actual.getName(), is("foo"));
         assertThat(actual.getAge(), is(34));
        assertThat(actual.getBirthDay(), is(new SimpleDateFormat("yyyyMMdd").parse("19780420")));
     }
 
     @Test
     public void testMapToTypeWithReader() throws Exception {
         when(request.getContentType()).thenReturn("text/xml");
         StringReader from = new StringReader(
                "<?xml version=\"1.0\" ?><org.analogweb.xstream.model.Foo><name>foo</name><age>38</age><birthDay>1978-04-20 00:00:00.0 JST</birthDay></org.analogweb.xstream.model.Foo>");
         Foo actual = (Foo) mapper.mapToType(requestContext, attributes, from, Foo.class, null);
         assertThat(actual.getName(), is("foo"));
         assertThat(actual.getAge(), is(38));
        assertThat(actual.getBirthDay(), is(new SimpleDateFormat("yyyyMMdd").parse("19780420")));
     }
 
     @Test
     public void testMapToTypeWithAnotherType() throws Exception {
         when(request.getContentType()).thenReturn("text/xml");
         String from = "<?xml version=\"1.0\" ?><org.analogweb.xstream.model.Foo><name>foo</name><age>38</age><birthDay>1978-04-20 00:00:00.0 JST</birthDay></org.analogweb.xstream.model.Foo>";
         assertThat(mapper.mapToType(requestContext, attributes, from, Foo.class, null),
                 is(nullValue()));
     }
 
     @Test
     public void testMapToTypeWithoutXmlStream() throws Exception {
         when(request.getContentType()).thenReturn("application/xml");
         InputStream from = new ByteArrayInputStream("XXX".getBytes());
         assertThat(mapper.mapToType(requestContext, attributes, from, Foo.class, null),
                 is(nullValue()));
     }
 
     @Test
     public void testMapToTypeWithoutXmlReader() throws Exception {
         when(request.getContentType()).thenReturn("application/xml");
         StringReader from = new StringReader("{hoge:XXX}");
         assertThat(mapper.mapToType(requestContext, attributes, from, Foo.class, null),
                 is(nullValue()));
     }
 
     @Test
     public void testMapToTypeWithIOError() throws Exception {
         when(request.getContentType()).thenReturn("application/x-d");
         InputStream from = new InputStream() {
             @Override
             public int read() throws IOException {
                 throw new IOException();
             }
         };
         assertThat(mapper.mapToType(requestContext, attributes, from, Foo.class, null),
                 is(nullValue()));
     }
 
     @Test
     public void testMapToTypeAnotherContentType() throws Exception {
         when(request.getContentType()).thenReturn("application/x-d");
         InputStream from = new ByteArrayInputStream("???".getBytes());
         assertThat(mapper.mapToType(requestContext, attributes, from, Foo.class, null),
                 is(nullValue()));
     }
 
 }
