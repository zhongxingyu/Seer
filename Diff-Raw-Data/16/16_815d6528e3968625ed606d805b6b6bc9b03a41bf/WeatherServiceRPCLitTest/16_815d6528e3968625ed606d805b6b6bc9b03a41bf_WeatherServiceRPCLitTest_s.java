 package org.codehaus.xfire.xmlbeans.rpc;
 
 import net.webservicex.WeatherData;
 
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.soap.SoapConstants;
import org.codehaus.xfire.wsdl11.builder.AbstractWSDL;
 import org.codehaus.xfire.xmlbeans.AbstractXmlBeansTest;
 import org.codehaus.xfire.xmlbeans.XmlBeansServiceFactory;
 import org.codehaus.xfire.xmlbeans.XmlBeansType;
 import org.jdom.Document;
 
 /**
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class WeatherServiceRPCLitTest
         extends AbstractXmlBeansTest
 {
     private Service endpoint;
 
     public void setUp()
             throws Exception
     {
         super.setUp();
 
         XmlBeansServiceFactory xsf = new XmlBeansServiceFactory();
         xsf.setStyle(SoapConstants.STYLE_RPC);
         endpoint = xsf.create(RPCWeatherService.class,
                               "WeatherService",
                               "http://www.webservicex.net",
                               null);
         endpoint.setProperty(XmlBeansType.XMLBEANS_NAMESPACE_HACK, "true");
         getServiceRegistry().register(endpoint);
     }
 
     public void testInvoke() throws Exception
     {
         Document response = invokeService("WeatherService", "SetWeatherData.xml");
 
         addNamespace("w", "http://www.webservicex.net");
         assertValid("//w:setWeatherDataResponse", response);
 
         response = invokeService("WeatherService", "GetWeatherData.xml");
         printNode(response);
         assertValid("//w:getWeatherDataResponse/out", response);
         assertValid("//w:getWeatherDataResponse/out/w:MaxTemperatureC[text()='1']", response);
         assertValid("//w:getWeatherDataResponse/out/w:MaxTemperatureF[text()='1']", response);
        
     }
     
     public void testWSDL() throws Exception
     {
         Document wsdl = getWSDLDocument("WeatherService");
 
         addNamespace("xsd", SoapConstants.XSD);
         addNamespace("w", AbstractWSDL.WSDL11_NS);
         
         assertValid("//w:message[@name='getWeatherDataResponse']/w:part[@type='tns:WeatherData']", wsdl);
     }
     
     public static class RPCWeatherService
     {
         public WeatherData getWeatherData()
         {
             WeatherData data = WeatherData.Factory.newInstance();
             data.setMaxTemperatureC("1");
             data.setMaxTemperatureF("1");
             
             return data;
         }
         
         public void setWeatherData(WeatherData data)
         {
             
         }
     }
 }
