 package net.nfrancois.froguino.gae.rest;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 
 /**
  * Test for {@link TemperatureServlet}
  */
 // TODO fest assert
 public class TemperatureServletTest {
 
     private TemperatureServlet temperatureServlet;
 
     @Before
     public void setupGuestBookServlet() {
         temperatureServlet = new TemperatureServlet();
     }
 
     @Test
     public void should_not_give_temperature() throws IOException, ServletException {
         // Given
         HttpServletRequest request = mock(HttpServletRequest.class);
         HttpServletResponse response = mock(HttpServletResponse.class);
 
         StringWriter stringWriter = new StringWriter();
 
         when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
 
         // When
         temperatureServlet.doGet(request, response);
 
 
         // Then
         assertThat(stringWriter.toString()).isEmpty();
     }
 
     @Test
     public void should_give_temperature() throws IOException, ServletException {
         // Given
         HttpServletRequest request = mock(HttpServletRequest.class);
         HttpServletResponse response = mock(HttpServletResponse.class);
 
         StringWriter stringWriter = new StringWriter();
 
 		TemperatureServlet.actualTemperature = 20.1;
 
         when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
 
         temperatureServlet.doGet(request, response);
 
         // Then
         assertThat(stringWriter.toString()).isEqualTo("20.1");
     }
 
     @Test
     public void should_post_temperature_fail_when_no_parameter() throws IOException, ServletException {
         // Given
         HttpServletRequest request = mock(HttpServletRequest.class);
         HttpServletResponse response = mock(HttpServletResponse.class);
        
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         
         Double before = TemperatureServlet.actualTemperature;
         
         // When
         temperatureServlet.doPut(request, response);
         
         // Then
         assertThat(TemperatureServlet.actualTemperature).isEqualTo(before);
     }
     
     @Test
     public void should_post_temperature() throws IOException, ServletException {
         // Given
         HttpServletRequest request = mock(HttpServletRequest.class);
         HttpServletResponse response = mock(HttpServletResponse.class);
         
         when(request.getAttribute("t")).thenReturn(20.1);
         
         // When
         temperatureServlet.doPut(request, response);
 
         // Then
         assertThat(TemperatureServlet.actualTemperature).isEqualTo(20.1);
     }    
 
 }
