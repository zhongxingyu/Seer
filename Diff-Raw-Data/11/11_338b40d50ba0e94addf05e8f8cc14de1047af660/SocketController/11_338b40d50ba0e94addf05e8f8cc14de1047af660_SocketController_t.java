 package com.pi.relay;
 
 import automation.api.devices.PowerController;
 
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
