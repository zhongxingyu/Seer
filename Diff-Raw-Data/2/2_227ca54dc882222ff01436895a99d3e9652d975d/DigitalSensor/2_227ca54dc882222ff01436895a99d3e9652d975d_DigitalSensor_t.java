 /*
  * This file is part of LinkJVM.
  *
  * Java Framework for the KIPR Link
  * Copyright (C) 2013 Markus Klein<m@mklein.co.at>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package linkjvm.high.sensors.digital;
 
 import linkjvm.high.sensors.InvalidPortException;
 import linkjvm.low.Digital;
 import linkjvm.low.factory.JNIController;
 
 /**
  * An instance of this class is used to control one digital sensor on the specified sensor port.
  * @author Markus Klein
  * @since 2.0.0
  * @version 2.0.0
  */
 public class DigitalSensor implements IDigitalSensor{
 	private volatile Digital jniSensor;
 
 	private int port;
 
 	/**
 	 * Creates a new digital sensor on the specified port.
      * The invocation {@code DigitalSensor sensor = new DigitalSensor(0)} is equivalent to:
      * {@code DigitalSensor sensor = new DigitalSensor();
      * sensor.setPort(0);}
      * 
 	 * @param port the sensor's port
 	 * @throws InvalidPortException
 	 */
 	public DigitalSensor(int port) throws InvalidPortException{
 		if(port < 8 || port > 15){
 			throw new InvalidPortException();
 		}
 		this.port = port;
 		jniSensor = JNIController.getInstance().getDigitalFactory().getInstance(port);
 	}
 
 	/**
 	 * Returns the sensor port.
 	 * @return sensor port
 	 */
 	public int getPort(){
 		return port;
 	}
 	
 	/**
 	 * Sets the sensor's port.
 	 * @param port sensor's port
 	 */
 	public void setPort(int port){
 		jniSensor = JNIController.getInstance().getDigitalFactory().getInstance(port);
 		this.port = port;
 	}
 	
 
 	/**
 	 * Returns the current sensor value.
 	 * @return sensor value
 	 */
 	@Override
 	public boolean getValue(){
		return !jniSensor.value();
 	}
 }
