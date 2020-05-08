 /**
  * 
  */
 package com.stationmillenium.rdsmanager.services.rs232;
 
 import gnu.io.CommPort;
 import gnu.io.CommPortIdentifier;
 import gnu.io.NoSuchPortException;
 import gnu.io.PortInUseException;
 import gnu.io.SerialPort;
 import gnu.io.UnsupportedCommOperationException;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.support.AbstractApplicationContext;
 import org.springframework.stereotype.Service;
 
 import com.stationmillenium.rdsmanager.beans.rs232.RS232Properties;
 import com.stationmillenium.rdsmanager.exceptions.rs232.RS232COMPortException;
 import com.stationmillenium.rdsmanager.services.alertmails.AlertMailService;
 
 /**
  * Service managing RS232 wire
  * @author vincent
  *
  */
 @Service
 public class RS232WireService  implements ApplicationContextAware {
 
 	//logger
 	private static final Logger LOGGER = LoggerFactory.getLogger(RS232WireService.class);
 	
 	//config
 	@Autowired
 	private RS232Properties config;
 	
 	//alerts mail sender
 	@Autowired
 	private AlertMailService alertMailService;
 	
 	//app context to shutdown app if com port init error
 	private ApplicationContext context;
 	
 	//local instances
 	private SerialPort serialPort;
 	private BufferedReader bufferedReader;
 	private BufferedWriter bufferedWriter;
 
 	/**
 	 * Init the COM port connection
 	 */
 	@PostConstruct
 	public void initCOMPort() {
 		if (!config.isVirtualMode()) {
 			try {
 				CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(config.getPortName()); //get com port
 				
 				if (!(portIdentifier.isCurrentlyOwned())) { //test if not already used
 					CommPort comPort = portIdentifier.open("RDSManager", 3000); //try opening during 3s				
 					
 					if (comPort instanceof SerialPort) { //if com port and not other type
 						  	serialPort = (SerialPort) comPort;
 			                serialPort.setSerialPortParams(config.getSpeed(), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
 			                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
 			                LOGGER.info("COM port opened : " + config.getPortName() + " | " + serialPort);
 			                
 			                //plug streams
 			                try { //plugin buffered reader
 								bufferedReader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
 							} catch (IOException e) { //error while opening reader
 								LOGGER.error("Buffered reader opening error", e); 
 								alertMailService.sendCOMPortErrorAlert(e);
 								stopContext();
 							}
 			                
 			                if (bufferedReader != null) { //if buffered reader properly opened
 			                	try {
 									bufferedWriter = new BufferedWriter(new OutputStreamWriter(serialPort.getOutputStream()));
 								} catch (IOException e) { //error while opening writer
 									LOGGER.error("Buffered writer opening error", e);
 									alertMailService.sendCOMPortErrorAlert(e);
 									stopContext();
 								}
 			                }
 		                
 					} else {
 						String message = "COM port not a serial port : " + config.getPortName();
 						LOGGER.error(message);
 						alertMailService.sendCOMPortErrorAlert(new RS232COMPortException(message));
 						stopContext();
 					}
 					
 				} else {
 					String message = "COM port already in use : " + config.getPortName();
 					LOGGER.error(message);
 					alertMailService.sendCOMPortErrorAlert(new RS232COMPortException(message));
 					stopContext();
 				}	
 				
 			} catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException e) {
 				LOGGER.error("COM port error : " + config.getPortName(), e);
 				alertMailService.sendCOMPortErrorAlert(e);
 				stopContext();
 			}
 		} else
 			LOGGER.warn("Virtual mode enabled - COM port not started");
 	}
 	
 	/**
 	 * Stop Spring context
 	 */
 	private void stopContext()  {
 		LOGGER.warn("Context stopped");
 		((AbstractApplicationContext) context).close(); //unload context (stop starting)
 	}
 	
 	@Override
 	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
 		context = applicationContext;
 	}
 	
 	/**
 	 * Close stream and COM port connection
 	 */
 	@PreDestroy
 	public void closeCOMPort() {
 		if (!config.isVirtualMode()) {
 			if (bufferedReader != null) { //close buffered reader
 				try {
 					bufferedReader.close();
 				} catch (IOException e) {
 					LOGGER.warn("Buffered reader closing error", e);
 				}
 			}
 			
 			if (bufferedWriter != null) { //close buffered writer
 				try {
 					bufferedWriter.close();
 				} catch (IOException e) {
 					LOGGER.warn("Buffered writer closing error", e);
 				}
 			}			
 				
 			if (serialPort != null) { //close serial port
 				serialPort.close();
 				LOGGER.info("COM port closed");
 			}
 		} else
 			LOGGER.warn("Virtual mode enabled - COM port not closing");
 	}
 	
 	/**
 	 * Send a command on the wire
 	 * @param commandToSend the command to send
 	 * @return the returned line, or null if virtual mode enabled
 	 * @throws IOException if any error occurs
 	 */
 	public String sendCommand(String commandToSend) throws IOException {
 		if (!config.isVirtualMode()) { //if not in virtual mode
 			bufferedWriter.write(commandToSend);
 			bufferedWriter.flush();
 			LOGGER.debug("Command sent : " + commandToSend + " | Waiting reply...");
 			
 			//get return data
 			String returnLine = null;
 			boolean bufferRead = false;
 			for (int i = 0; i < config.getWaitingTries(); i++){
 				if (bufferedReader.ready()) { //if buffer ready : read
 					LOGGER.debug("Receive buffer is ready !");
 					returnLine = bufferedReader.readLine();
					if ((returnLine != null) && (returnLine.length() > 0)) { //line has something to read
						bufferRead = true;
						break;
					} else { //read line is empty - try again
						LOGGER.debug("Read empty line - try next time...");
						i--; //cancel this try
					}
 				} else { //wait next time
 					LOGGER.debug("Receive buffer not ready - wait...");
 					try {
 						Thread.sleep(config.getWaitingTime());
 					} catch (InterruptedException e) {
 						LOGGER.warn("Error while waiting receive buffer", e);
 					}
 				}
 			}
 			
 			//check if buffer properly read
 			if (!bufferRead) 
 				throw new IOException("Receive buffer not read !");
 			
 			LOGGER.debug("Reply : " + returnLine);
 			return returnLine;
 			
 		} else {
 			LOGGER.warn("Virtual mode enabled - send/receive nothing");
 			return null;
 		}
 	}
 	
 }
