 package com.san.twilio.callbackservice;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("/*")
 public class TwilioController {
 	@RequestMapping(value="/getResponse", method = RequestMethod.GET)
 	public @ResponseBody Response getResponseInXML() {
 		Response response = new Response();
 		TwilioSayMessage twilioSayMessage = new TwilioSayMessage();
 		response.setTwilioSayMessage(twilioSayMessage);
 		return response;
 	}
 	
	@RequestMapping(value="/getResponseWithPost", method = RequestMethod.POST)
	public @ResponseBody Response getResponseInXMLWithPost() {
		Response response = new Response();
		TwilioSayMessage twilioSayMessage = new TwilioSayMessage();
		response.setTwilioSayMessage(twilioSayMessage);
		return response;
	}
	
 	public static void main(String[] args) {
 		TwilioController controller = new TwilioController();
 		controller.getResponseInXML();
 	}
 }
