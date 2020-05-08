 /*
  * This file is part of CBCJVM.
  * CBCJVM is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * CBCJVM is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with CBCJVM.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cbccore;
 
 import cbccore.low.*;
 import cbccore.low.CBCSimulator;
 
 /**
  * 
  * @author Braden McDorman / Benjamin Woodruff
  *
  */
 
 public class Device {
 	private static CBCSimulator simulator;
 	private static Motor lowMotors;
 	private static Display lowDisplay;
 	private static Create lowCreate;
 	private static Servo lowServos;
 	private static Camera lowCamera;
 	private static cbccore.low.Device lowDevice;
 	private static Input lowInput;
 	private static Sound lowSound;
 	private static Sensor lowSensors;
 	
 	static {
 		try {
			if(System.getenv().get("ON_CBC") != null) {
 				System.load("/mnt/user/jvm/cbc/CBC.so");
 				lowSound = new Sound();
 				lowSensors = new Sensor();
 				lowDevice = new cbccore.low.Device();
 				lowDisplay = new Display();
 				lowInput = new Input();
 				lowServos = new Servo();
 				lowMotors = new Motor();
 				lowCamera = new Camera();
 				lowCreate = new Create();
 			} else {
 				simulator = new CBCSimulator();
 				lowSound = simulator.sound;
 				lowSensors = simulator.sensor;
 				lowDevice = simulator.device;
 				lowDisplay = simulator.display;
 				lowInput = simulator.input;
 				lowServos = simulator.servo;
 				lowMotors = simulator.motor;
 				lowCamera = simulator.camera;
 				lowCreate = simulator.create;
 			}
 		}
 		catch(UnsatisfiedLinkError e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static CBCSimulator getSimulatorController() {
 		return simulator;
 	}
 	
 	public static Motor getLowMotorController() {
 		return lowMotors;
 	}
 	
 	public static Create getLowCreateController() {
 		return lowCreate;
 	}
 	
 	public static Display getLowDisplayController() {
 		return lowDisplay;
 	}
 	
 	public static Servo getLowServoController() {
 		return lowServos;
 	}
 	
 	public static Camera getLowCameraController() {
 		return lowCamera;
 	}
 	
 	public static cbccore.low.Device getLowDeviceController() {
 		return lowDevice;
 	}
 	
 	public static Input getLowInputController() {
 		return lowInput;
 	}
 	
 	public static Sound getLowSoundController() {
 		return lowSound;
 	}
 	
 	public static Sensor getLowSensorController() {
 		return lowSensors;
 	}
 }
