 package soapproxy.web;
 
 import org.mortbay.jetty.Request;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.PrintWriter;
 
 public class SmdController extends AbstractController {
   private static final String DEFAULT_JAVASCRIPT_TYPE = "text/javascript";
   @Override
   protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
     httpServletResponse.setContentType(DEFAULT_JAVASCRIPT_TYPE);
 
     // get the request params
     String wsdlUri = httpServletRequest.getParameter("wsdl");
     String operationName = httpServletRequest.getParameter("operation");
 
     PrintWriter out = httpServletResponse.getWriter();
     out.write(httpServletRequest.getParameter("callback") + "(");
     out.write("{\n" +
               "    transport:\"JSONP\", // We will use POST as the transport\n" +
               "    envelope:\"JSON-RPC-2.0\", // We will use JSON-RPC\n" +
               "    SMDVersion:\"2.0\",\n" +
               "    services: {\n" +
               "      " + operationName + ": {\n" +
               "        // this defines the URL to connect for the services\n" +
              "        target:   \"" + ((Request)httpServletRequest).getRootURL() + "/proxy?wsdl=" + wsdlUri + "&operation=" + operationName + "\"\n" +
               "      }\n" +
               "    }\n" +
               "  }");
     out.write(");");
     return null;
   }
 }
