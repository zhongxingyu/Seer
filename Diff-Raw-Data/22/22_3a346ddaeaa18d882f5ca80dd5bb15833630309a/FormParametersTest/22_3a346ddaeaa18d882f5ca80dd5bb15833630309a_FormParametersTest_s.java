 package org.analogweb.core;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.ByteArrayInputStream;
 import java.net.URI;
 import java.util.List;
 import java.util.Map;
 
 import org.analogweb.MediaType;
 import org.analogweb.RequestContext;
 import org.analogweb.RequestPath;
 import org.junit.Before;
 import org.junit.Test;
 
 public class FormParametersTest {
 
     private FormParameters params;
     private RequestContext context;
 
     @Before
     public void setUp() {
         context = mock(RequestContext.class);
         params = new FormParameters(context);
     }
 
     @Test
     public void testGetValues() throws Exception {
         RequestPath path = mock(RequestPath.class);
         when(context.getRequestPath()).thenReturn(path);
         when(context.getContentType()).thenReturn(MediaTypes.APPLICATION_FORM_URLENCODED_TYPE);
         when(path.getRequestURI()).thenReturn(URI.create("http://localhost:80/a"));
         when(context.getRequestBody()).thenReturn(
                 new ByteArrayInputStream("foo=baa&hoge=fuga&hoge=hoge%21".getBytes()));
         List<String> actual = params.getValues("foo");
         assertThat(actual.size(), is(1));
         assertThat(actual.get(0), is("baa"));
 
         actual = params.getValues("hoge");
         assertThat(actual.size(), is(2));
         assertThat(actual.get(0), is("fuga"));
         assertThat(actual.get(1), is("hoge!"));
         // not avairable.
         actual = params.getValues("baz");
         assertThat(actual.isEmpty(), is(true));
     }
 
     @Test
     public void testAsMap() throws Exception {
         RequestPath path = mock(RequestPath.class);
         when(context.getRequestPath()).thenReturn(path);
        when(context.getContentType()).thenReturn(MediaTypes.APPLICATION_FORM_URLENCODED_TYPE);
         when(path.getRequestURI()).thenReturn(URI.create("http://localhost:80/a"));
         when(context.getRequestBody()).thenReturn(
                 new ByteArrayInputStream("%E5%A0%B4%E6%89%80=Tokyo&%E5%90%8D%E5%89%8D=Yukio"
                        .getBytes()));
         Map<String, String[]> map = params.asMap();
         String[] actual = map.get("名前");
         assertThat(actual.length, is(1));
         assertThat(actual[0], is("Yukio"));
 
         actual = map.get("場所");
         assertThat(actual.length, is(1));
         assertThat(actual[0], is("Tokyo"));
 
         actual = map.get("foo");
         assertThat(actual, is(nullValue()));
     }
 
     @Test
     public void testAsMapOtherEncoding() throws Exception {
         RequestPath path = mock(RequestPath.class);
         when(context.getRequestPath()).thenReturn(path);
        when(context.getContentType()).thenReturn(MediaTypes.APPLICATION_FORM_URLENCODED_TYPE);
         when(path.getRequestURI()).thenReturn(URI.create("http://localhost:80/a"));
         when(context.getRequestBody()).thenReturn(
                 new ByteArrayInputStream("%E5%A0%B4%E6%89%80=Tokyo&%E5%90%8D%E5%89%8D=Yukio"
                        .getBytes()));
         Map<String, String[]> map = params.asMap();
         String[] actual = map.get("名前");
         assertThat(actual.length, is(1));
         assertThat(actual[0], is("Yukio"));
 
         actual = map.get("場所");
         assertThat(actual.length, is(1));
         assertThat(actual[0], is("Tokyo"));
 
         actual = map.get("foo");
         assertThat(actual, is(nullValue()));
     }
 
     @Test
     public void testAsMapIsEmpty() {
         RequestPath path = mock(RequestPath.class);
         when(context.getRequestPath()).thenReturn(path);
         MediaType formUrlEncoded = mock(MediaType.class);
         when(context.getContentType()).thenReturn(formUrlEncoded);
         when(formUrlEncoded.getType()).thenReturn("text");
         when(path.getRequestURI()).thenReturn(URI.create("http://localhost:80/a"));
         Map<String, String[]> actual = params.asMap();
         assertThat(actual.isEmpty(), is(true));
     }
 
 }
