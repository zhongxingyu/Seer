 package com.pi.relay;
 
import javax.jws.WebService;

 import automation.api.devices.PowerController;
 
@WebService(endpointInterface = "automation.api.PowerController")
 public class SocketController extends PowerController {
 
 	@Override
 	public void turnOn() {
 		powerController.high();
 	}
 
 	@Override
 	public void turnOff() {
 		powerController.low();
 	}
 }
