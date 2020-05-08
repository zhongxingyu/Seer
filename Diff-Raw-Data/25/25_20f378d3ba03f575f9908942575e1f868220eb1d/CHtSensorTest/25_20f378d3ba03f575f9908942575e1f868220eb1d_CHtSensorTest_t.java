 package sensor.color_and_pressure;
 
 import lejos.nxt.Button;
 import lejos.nxt.ButtonListener;
 import lejos.nxt.LCD;
 import lejos.nxt.NXT;
 import lejos.nxt.SensorPort;
 import lejos.nxt.addon.ColorHTSensor;
 import lejos.util.Delay;
 
 public class CHtSensorTest {
 	
 	// Motor declaration
 	ColorHTSensor colorHt = new ColorHTSensor(SensorPort.S4);
 	
 
 	public CHtSensorTest() {
 		Delay.msDelay(1000);
 		Button.LEFT.addButtonListener(new ButtonListener() {
 
 			@Override
 			public void buttonReleased(Button b) {
 				NXT.shutDown();
 
 			}
 
 			@Override
 			public void buttonPressed(Button b) {
 				NXT.shutDown();
 
 			}
 		});
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		CHtSensorTest cAP = new CHtSensorTest();
 		cAP.run();
 	}
 
 	public void run() {
 		while (true) {
 			Delay.msDelay(100);
 			if(lejos.nxt.ColorSensor.Color.RED==colorHt.getColorID()){
 				LCD.clear();
 				LCD.drawString("Rot", 0, 0);	
 			}else{
 			LCD.drawInt(colorHt.getColorID(), 0, 0);
 			if(lejos.nxt.ColorSensor.Color.BLUE==colorHt.getColorID()){
 				LCD.clear();
 				LCD.drawString("Blau", 0, 0);	
 			}else{
 			LCD.drawInt(colorHt.getColorID(), 0, 0);
 			if(7==colorHt.getColorID()){
 				LCD.clear();
 				LCD.drawString("undefiniert", 0, 0);	
 			}
 			else{
 				LCD.clear();
 				LCD.drawString("nicht zugeordnet", 0, 0);
 			}}}
 		}
 	}
 }
