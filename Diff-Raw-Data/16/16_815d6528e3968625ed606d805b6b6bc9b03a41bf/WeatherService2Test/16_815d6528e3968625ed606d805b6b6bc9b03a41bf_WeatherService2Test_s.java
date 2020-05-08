 package org.codehaus.xfire.xmlbeans;
 
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.soap.SoapConstants;
import org.codehaus.xfire.wsdl11.builder.AbstractWSDL;
 import org.jdom.Document;
 
 /**
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class WeatherService2Test
         extends AbstractXmlBeansTest
 {
     private Service endpoint;
 
     public void setUp()
             throws Exception
     {
         super.setUp();
 
         endpoint = getServiceFactory().create(WeatherService2.class,
                                               "WeatherService",
                                               "urn:WeatherService",
                                               null);
         endpoint.setProperty(XmlBeansType.XMLBEANS_NAMESPACE_HACK, "true");
         getServiceRegistry().register(endpoint);
     }
 
     public void testInvoke() throws Exception
     {
         Document response = invokeService("WeatherService", "GetWeatherByZip.xml");
 
         addNamespace("w", "http://www.webservicex.net");
         assertValid("//w:GetWeatherByZipCodeResponse", response);
         
         response = invokeService("WeatherService", "GetForecasts.xml");
 
         addNamespace("u", "urn:WeatherService");
         assertValid("//u:GetForecastsout", response);
         assertValid("//u:GetForecastsout/w:Latitude", response);
         assertValid("//u:GetForecastsout/w:Longitude", response);
     }
     
     public void testWSDL() throws Exception
     {
         Document wsdl = getWSDLDocument("WeatherService");
 
         addNamespace("xsd", SoapConstants.XSD);
         addNamespace("w", AbstractWSDL.WSDL11_NS);
         
         assertValid("//w:message[@name='GetForecastsResponse']/w:part[@element='tns:GetForecastsout']", wsdl);
     }
 }
