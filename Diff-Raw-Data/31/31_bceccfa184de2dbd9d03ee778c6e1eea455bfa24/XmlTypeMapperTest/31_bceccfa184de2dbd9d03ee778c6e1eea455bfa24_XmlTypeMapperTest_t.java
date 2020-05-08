 package org.analogweb.core;
 
 import static org.hamcrest.core.IsNull.nullValue;
 import static org.hamcrest.core.Is.is;
 import static org.mockito.Mockito.*;
 import static org.junit.Assert.assertThat;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.io.StringReader;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.analogweb.RequestAttributes;
 import org.analogweb.RequestContext;
 import org.junit.Before;
 import org.junit.Test;
 
 public class XmlTypeMapperTest {
 
     private XmlTypeMapper mapper;
     private RequestContext context;
     private RequestAttributes attributes;
     private HttpServletRequest request;
 
     @Before
     public void setUp() throws Exception {
         mapper = new XmlTypeMapper();
         context = mock(RequestContext.class);
         attributes = mock(RequestAttributes.class);
         request = mock(HttpServletRequest.class);
     }
 
     @Test
     public void testMapToTypeByStream() {
         when(context.getRequest()).thenReturn(request);
         when(request.getContentType()).thenReturn("text/xml");
         byte[] xmlBytes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><hello><world>snowgoose</world></hello>"
                 .getBytes();
         InputStream xmlBody = new ByteArrayInputStream(xmlBytes);
         Hello actual = (Hello) mapper
                 .mapToType(context, attributes, xmlBody, Hello.class, new String[0]);
         assertThat(actual.getWorld(), is("snowgoose"));
         verify(request).getContentType();
     }
 
     @Test
     public void testMapToTypeByReader() {
         when(context.getRequest()).thenReturn(request);
         when(request.getContentType()).thenReturn("application/xml");
         StringReader xmlBody = new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><hello><world>snowgoose</world></hello>");
         Hello actual = (Hello) mapper
                 .mapToType(context, attributes, xmlBody, Hello.class, new String[0]);
         assertThat(actual.getWorld(), is("snowgoose"));
         verify(request).getContentType();
     }
 
     @Test
     public void testMapToTypeBytes() {
         when(context.getRequest()).thenReturn(request);
         when(request.getContentType()).thenReturn("text/xml");
         byte[] xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><hello><world>snowgoose</world></hello>"
                 .getBytes();
         Hello actual = (Hello) mapper
                 .mapToType(context, attributes, xmlBody, Hello.class, new String[0]);
         assertThat(actual, is(nullValue()));
         verify(request).getContentType();
     }
 
     @Test
     public void testMapToTypeIllegalClassType() {
         when(context.getRequest()).thenReturn(request);
         when(request.getContentType()).thenReturn("text/xml");
         byte[] xmlBytes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><hello><world>snowgoose</world></hello>"
                 .getBytes();
         InputStream xmlBody = new ByteArrayInputStream(xmlBytes);
         Hello actual = (Hello) mapper
                 .mapToType(context, attributes, xmlBody, UnHello.class, new String[0]);
         assertThat(actual, is(nullValue()));
         verify(request).getContentType();
     }
 
     @Test
     public void testMapToTypeIllegalContentType() {
         when(context.getRequest()).thenReturn(request);
         when(request.getContentType()).thenReturn("application/json");
         byte[] xmlBytes = "{\"hello\":\"world\"}".getBytes();
         InputStream xmlBody = new ByteArrayInputStream(xmlBytes);
         Hello actual = (Hello) mapper
                 .mapToType(context, attributes, xmlBody, UnHello.class, new String[0]);
         assertThat(actual, is(nullValue()));
         verify(request).getContentType();
     }
 
    @Test
    public void testMapToTypeWithoutContentType() {
        when(context.getRequest()).thenReturn(request);
        when(request.getContentType()).thenReturn(null);
        byte[] xmlBytes = "plain text".getBytes();
        InputStream xmlBody = new ByteArrayInputStream(xmlBytes);
        Hello actual = (Hello) mapper
                .mapToType(context, attributes, xmlBody, UnHello.class, new String[0]);
        assertThat(actual, is(nullValue()));
        verify(request).getContentType();
    }

     @XmlRootElement
     static class Hello implements Serializable {
         private static final long serialVersionUID = 1L;
         @XmlElement
         private String world;
 
         public String getWorld() {
             return this.world;
         }
     }
 
     @XmlRootElement
     static class UnHello implements Serializable {
         private static final long serialVersionUID = 1L;
         @XmlElement
         private String world;
 
         public String getWorld() {
             return this.world;
         }
     }
 
 }
