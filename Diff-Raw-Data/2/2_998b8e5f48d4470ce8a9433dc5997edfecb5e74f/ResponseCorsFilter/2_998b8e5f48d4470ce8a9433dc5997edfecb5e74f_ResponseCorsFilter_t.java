 package no.sb1.lpt;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 
 import com.sun.jersey.spi.container.ContainerRequest;
 import com.sun.jersey.spi.container.ContainerResponse;
 import com.sun.jersey.spi.container.ContainerResponseFilter;
 
 public class ResponseCorsFilter implements ContainerResponseFilter {
 
     public ContainerResponse filter(ContainerRequest request, ContainerResponse containerResponse) {
 
         ResponseBuilder response = Response.fromResponse(containerResponse.getResponse());
         response.header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
 
         String requestHeaders = request.getHeaderValue("Access-Control-Request-Headers");
 
         if (null != requestHeaders && !requestHeaders.equals(null)) {
             response.header("Access-Control-Allow-Headers", requestHeaders);
         }
 
         containerResponse.setResponse(response.build());
         return containerResponse;
     }
 
 }
