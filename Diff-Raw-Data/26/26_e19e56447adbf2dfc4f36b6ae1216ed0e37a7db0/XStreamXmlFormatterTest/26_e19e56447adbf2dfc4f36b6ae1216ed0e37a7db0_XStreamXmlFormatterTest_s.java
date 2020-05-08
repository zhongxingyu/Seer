 package org.analogweb.xstream;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 
 import org.analogweb.RequestContext;
 import org.analogweb.exception.FormatFailureException;
 import org.analogweb.xstream.model.Foo;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 /**
  * @author snowgoose
  */
 public class XStreamXmlFormatterTest {
 
     private XStreamXmlFormatter formatter;
     private RequestContext context;
     private HttpServletResponse response;
 
     @Rule
     public ExpectedException thrown = ExpectedException.none();
 
     @Before
     public void setUp() throws Exception {
         formatter = new XStreamXmlFormatter();
         context = mock(RequestContext.class);
         response = mock(HttpServletResponse.class);
         when(context.getResponse()).thenReturn(response);
     }
 
     @Test
     public void testFormatAndWriteInto() throws Exception {
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
             @Override
             public void write(int arg0) throws IOException {
                 out.write(arg0);
             }
         });
         Foo f = new Foo();
        f.setBirthDay(new SimpleDateFormat("yyyyMMdd").parse("19780420"));
         formatter.formatAndWriteInto(context, "UTF-8", f);
         String actual = new String(out.toByteArray());
         assertThat(
                 actual,
                is("<?xml version=\"1.0\" ?><org.analogweb.xstream.model.Foo><name>foo</name><age>34</age><birthDay>1978-04-20 00:00:00.0 JST</birthDay></org.analogweb.xstream.model.Foo>"));
     }
 
     @Test
     public void testFormatAndWriteIntoRaiseIOException() throws Exception {
         thrown.expect(FormatFailureException.class);
         when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
             @Override
             public void write(int arg0) throws IOException {
                 throw new IOException();
             }
         });
         Foo f = new Foo();
         f.setBirthDay(new SimpleDateFormat("yyyyMMdd").parse("19780420"));
         formatter.formatAndWriteInto(context, "UTF-8", f);
     }
 
     @Test
     public void testFormatAndWriteIntoRaiseIOException2() throws Exception {
         thrown.expect(FormatFailureException.class);
         when(response.getOutputStream()).thenThrow(new IOException());
         Foo f = new Foo();
         f.setBirthDay(new SimpleDateFormat("yyyyMMdd").parse("19780420"));
         formatter.formatAndWriteInto(context, "UTF-8", f);
     }
 
 }
