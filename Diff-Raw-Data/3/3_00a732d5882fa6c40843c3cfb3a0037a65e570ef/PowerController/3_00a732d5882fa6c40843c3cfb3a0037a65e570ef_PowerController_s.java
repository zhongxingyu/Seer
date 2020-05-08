 package automation.api.devices;
 
 import automation.api.interfaces.PowerControllerIface;
 
 import javax.annotation.Resource;
 import javax.xml.ws.WebServiceContext;
 import com.pi4j.io.gpio.GpioController;
 import com.pi4j.io.gpio.GpioFactory;
 import com.pi4j.io.gpio.GpioPinDigitalOutput;
 import com.pi4j.io.gpio.PinState;
 import com.pi4j.io.gpio.RaspiPin;
 
 public abstract class PowerController implements PowerControllerIface {
 	
 	@Resource
     WebServiceContext wsctx;
     
     private GpioController gpio;
     protected GpioPinDigitalOutput powerController;
     
     public PowerController() { 
     	gpio  = GpioFactory.getInstance();
         
         //   GPIO PIN #1 == POWER CONTROLLER
         powerController = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "PowerController");
         
         // force power controller to OFF if the program is shutdown
         powerController.setShutdownOptions(true,PinState.LOW);
         
         // default to off
         powerController.low();
     }
   
     @Override
     abstract public void turnOn();
     
     @Override
     abstract public void turnOff();
 }
         
