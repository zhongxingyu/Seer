 /*
 Part of the NETLab Hub, which is part of the NETLab Toolkit project - http://netlabtoolkit.org
 
 Copyright (c) 2006-2013 Ewan Branda
 
 NETLab Hub is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 NETLab Hub is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with NETLab Hub.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package netlab.hub.plugins.arduino;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import netlab.hub.core.Service;
 import netlab.hub.core.ServiceException;
 import netlab.hub.core.ServiceMessage;
 import netlab.hub.core.ServiceResponse;
 import netlab.hub.serial.MacSerialFixer;
 import netlab.hub.serial.SerialException;
 import netlab.hub.serial.SerialPort;
 import netlab.hub.serial.SerialPortClient;
 import netlab.hub.serial.SerialPortClientRegistry;
 import netlab.hub.util.Logger;
 import netlab.hub.util.ThreadUtil;
 
 /**
  * <p>Implementation of Service for Arduino access. Communicates with an 
  * Arduino board running StandardFirmata.</p>
  *
  */
 public class ArduinoService extends Service implements SerialPortClient {
 	
 	protected HashMap<String, Arduino> boards = new HashMap<String, Arduino>(); // Keyed by original port pattern
 					
 	/**
 	 * Create an Arduino service instance.
 	 */
 	public ArduinoService() {
 		super();
 	}
 	
 	/* (non-Javadoc)
 	 * @see netlab.hub.core.Service#process(netlab.hub.core.ServiceMessage, netlab.hub.core.ServiceResponse)
 	 */
 	public void process(ServiceMessage request, ServiceResponse response) throws ServiceException {
 		String command = request.getPath().getLast();
 		if ("connect".equalsIgnoreCase(command)) {
 			commandConnect(request, response);
 		}
 		else 	
 		if ("pinmode".equalsIgnoreCase(command)) {
 			commandPinMode(request, response, getBoard(request.getPathElement(0)));
 		} 
 		else {
 			commandReadWrite(request, response, getBoard(request.getPathElement(0)));
 		}
 	}
 	
 	/**
 	 * Mainly for unit testing.
 	 * @param key
 	 * @param board
 	 */
 	public void setBoard(String key, Arduino board) {
 		boards.put(key, board);
 	}
 	
 	/**
 	 * @param namePattern
 	 * @return
 	 * @throws ServiceException
 	 */
 	public Arduino getBoard(String namePattern) throws ServiceException {
 		Arduino arduino = boards.get(namePattern);
 		if (arduino == null) {
 			throw new ServiceException("No board connected for ["+namePattern+"]. Send connect command first.");
 		}
 		return arduino;
 	}
 	
 	/**
 	 * Read the serial port params from the command, create a new board instance,
 	 * which will open the serial port itself, and store it in a lookup table for 
 	 * access by later commands.
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws ServiceException
 	 */
 	public void commandConnect(ServiceMessage request, ServiceResponse response) throws ServiceException {
 		try {
 			String portNamePattern = request.getArgument(0);
 			Arduino arduino = boards.get(portNamePattern);
 			if (arduino == null) {
 				MacSerialFixer.check();
 				String[] availablePorts = SerialPort.list(portNamePattern);
 				if (availablePorts.length == 0) {
 					throw new SerialException("Could not find a USB device matching the name pattern ["
 												+portNamePattern+"]. Is your Arduino connected?");
 				}
 				String portName = availablePorts[0]; // Take the first matching name by default
 				int baud = request.argInt(1, 57600);
				arduino = new Arduino(portName, baud);
 				SerialPortClientRegistry.register(portName, this);
 				if (boards.isEmpty()) { // If this is the first board, store the board reference under the default name pattern
 					boards.put("*", arduino);
 				}
 				boards.put(portName, arduino); // Store the board reference under the full port name
 				if (!portName.equals(portNamePattern)) {
 					boards.put(portNamePattern, arduino); // Also store the board reference under the port name pattern
 				}
 			}
			response.write(new String[] {"OK", arduino.getPortName()});
 		} catch (Exception e) {
 			response.write(new String[] {"FAIL", e.toString()});
 		}
 	}
 	
 	/**
 	 * @param request
 	 * @param response
 	 * @throws ServiceException
 	 */
 	public void commandPinMode(ServiceMessage request, ServiceResponse response, Arduino arduino) throws ServiceException {
 		if (request.getArguments().size() != 2) {
 			Logger.warn("Incorrect arguments to pinmode command (should be [pinnumber, mode])");
 			return;
 		}
 		int pin = request.argInt(0, -1);
 		String mode = request.getArgument(1);
 		if ("input".equals(mode)) {
 			arduino.pinMode(pin, Arduino.INPUT);
 		} else 
 		if ("output".equals(mode)) {
 			arduino.pinMode(pin, Arduino.OUTPUT);
 		} else
 		if ("analog".equals(mode)) {
 			arduino.pinMode(pin, Arduino.ANALOG);
 		} else
 		if ("pwm".equals(mode)) {
 			arduino.pinMode(pin, Arduino.PWM);
 		} else
 		if ("servo".equals(mode)) {
 			arduino.pinMode(pin, Arduino.SERVO);
 		} else {
 			Logger.warn("Unsupported pinmode constant ["+mode+"]");
 		}
 	}
 	
 	/**
 	 * @param request
 	 * @param response
 	 * @throws ServiceException
 	 */
 	public void commandReadWrite(ServiceMessage request, ServiceResponse response, Arduino arduino) throws ServiceException {
 		String pinType = request.getPathElement(1);
 		int pin = Integer.parseInt(request.getPathElement(2));
 		String arduinoCommand = request.getPathElement(3);
 		if ("analogin".equalsIgnoreCase(pinType)) {
 			response.write(arduino.analogRead(pin));
 		} else
 		if ("digitalin".equalsIgnoreCase(pinType)) {
 			arduino.pinMode(pin, Arduino.INPUT); // Is this doing anything? We are only reading from a buffer.
 			response.write(arduino.digitalRead(pin));
 		} else
 		if ("analogout".equalsIgnoreCase(pinType)) {
 			arduino.pinMode(pin, Arduino.PWM);
 			arduino.analogWrite(pin, request.argInt(0, 0));
 		} else
 		if ("digitalout".equalsIgnoreCase(pinType)) {
 			arduino.pinMode(pin, Arduino.OUTPUT);
 			arduino.digitalWrite(pin, request.argInt(0, 0) == 0 ? Arduino.LOW : Arduino.HIGH);
 		} else
 		if ("servo".equalsIgnoreCase(pinType) && "config".equalsIgnoreCase(arduinoCommand)) {
 			arduino.servoConfig(pin, request.argInt(0, 0), request.argInt(1, 0), request.argInt(2, 0));
 		} else
 		if ("servo".equalsIgnoreCase(pinType) && "angle".equalsIgnoreCase(arduinoCommand)) {
 			arduino.setServoAngle(pin, request.argInt(0, 0));
 		} else
 		if ("servo".equalsIgnoreCase(pinType) && "anglerelative".equalsIgnoreCase(arduinoCommand)) {
 			arduino.setServoAngleRelative(pin, request.argInt(0, 0));
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see netlab.hub.core.Service#dispose()
 	 */
 	public void dispose() throws ServiceException {
 		for (Iterator<Arduino> it=boards.values().iterator(); it.hasNext();) {
 			it.next().dispose();
 		}
 		boards.clear();
 	}
 	
 	/* (non-Javadoc)
 	 * @see netlab.hub.serial.SerialPortClient#releasePorts()
 	 */
 	public synchronized void releasePorts() {
 		try {
 			dispose();
 			ThreadUtil.pause(1000);
 		} catch (ServiceException e) {
 			Logger.error("Error releasing serial ports", e);
 		}
 	}
 	
 }
