 package controllers;
 
 import helpers.FedexServicesHelper;
 
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import play.mvc.Controller;
 
 public class Application extends Controller {
 
 	public static void index(){
 		render();
 	}
 	public static void getShippingRate(String zipCode,String jsoncallback) throws SAXException, IOException, ParserConfigurationException {
     	String rate = FedexServicesHelper.getShippingRateFor(zipCode);
         if(jsoncallback!=null && jsoncallback.trim().length()>0){
         	response.contentType="application/x-javascript";
        	renderTemplate("Application/shippingRate.jsonp",jsoncallback,rate);
         }else{
         	render(rate);
         }
     }
 }
