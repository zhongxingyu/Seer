 package soapproxy.web;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import soapproxy.application.mapping.MappingGenerator;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.PrintWriter;
 
 @Controller
 public class MappingController{
   private static final String DEFAULT_XML_TYPE = "text/xml";
 
   @Autowired
   private MappingGenerator mappingGenerator;
 
   @RequestMapping("/mapping")
  protected ModelAndView getMapping(@RequestParam("wsdlUri") String wsdlUri, @RequestParam("operation") String operation, HttpServletRequest request, HttpServletResponse response) throws Exception {
 
     response.setContentType(DEFAULT_XML_TYPE);
 
     PrintWriter out = response.getWriter();
     String jsonSchemaUrl = getJsonSchemaUrl(request, wsdlUri, operation);
 
     out.print(mappingGenerator.getMapping(wsdlUri, operation, jsonSchemaUrl));
     return null;
   }
 
   private String getJsonSchemaUrl(HttpServletRequest request, String wsdlUri, String operation) {
     String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" +
                 request.getServerPort() + request.getContextPath();
     return baseUrl + "/json-schema?wsdl=" + wsdlUri + "&operation=" + operation + "&message=input";
   }
 }
