 package com.stationmillenium.rdsmanager.services.rs232;
 
 import java.io.IOException;
 import java.util.Calendar;
 
 import javax.annotation.PostConstruct;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.support.AbstractApplicationContext;
 import org.springframework.stereotype.Service;
 
 import com.stationmillenium.rdsmanager.beans.rs232.RDSDisplayManagerProperties;
 import com.stationmillenium.rdsmanager.beans.rs232.RS232Properties;
 import com.stationmillenium.rdsmanager.dto.documents.RDSDisplay;
 import com.stationmillenium.rdsmanager.dto.documents.subs.RS232Commands;
 import com.stationmillenium.rdsmanager.dto.title.BroadcastableTitle;
 import com.stationmillenium.rdsmanager.exceptions.rs232.RS232RDSCoderException;
 import com.stationmillenium.rdsmanager.repositories.MongoDBRepository;
 import com.stationmillenium.rdsmanager.services.alertmails.AlertMailService;
 
 /**
  * Service to manager text displayed on RDS
  * @author vincent
  *
  */
 @Service
 public class RDSDisplayManagerService implements ApplicationContextAware {
 
 	/**
 	 * Available command type
 	 * @author vincent
 	 *
 	 */
 	private enum CommandType {
 		INIT,
 		PS,
 		RADIOTEXT;
 	}
 	
 	//logger
 	private static final Logger LOGGER = LoggerFactory.getLogger(RDSDisplayManagerService.class);
 	
 	//config for rs232
 	@Autowired
 	private RS232Properties rs232Config;
 		
 	//configuration
 	@Autowired
 	private RDSDisplayManagerProperties rdsDisplayManagerProperties;
 	
 	//db repository to log commands
 	@Autowired
 	private MongoDBRepository mongoDBRepository;
 	
 	//rs232 wire service to send command
 	@Autowired
 	private RS232WireService rs232WireService;
 	
 	//alerts mail service
 	@Autowired
 	private AlertMailService alertMailService;
 		
 	//app context to shutdown app if com port init error
 	private ApplicationContext context;
 		
 	/**
 	 * Display a title on PS and RadioText
 	 * @param titleToDisplay the title to display
 	 */
 	public void displayTitle(BroadcastableTitle titleToDisplay) {
 		LOGGER.debug("Display title on RDS : " + titleToDisplay);
 		String psCommandToSend = preparePSTitleCommand(titleToDisplay);		
 		String rtCommandToSend = prepareRTTitleCommand(titleToDisplay);
 		
 		//send ps command
 		sendCommandsToRDS(psCommandToSend, rtCommandToSend, titleToDisplay);
 	}
 
 	/**
 	 * Prepapre the RT title command to display title on RDS
 	 * @param titleToDisplay the title
 	 * @return the command as string
 	 */
 	private String prepareRTTitleCommand(BroadcastableTitle titleToDisplay) {
 		String rtText = titleToDisplay.getArtist() + " " + rdsDisplayManagerProperties.getRtSeparator() + " " + titleToDisplay.getTitle();
 		if (rtText.length() > rdsDisplayManagerProperties.getMaxLength())
 			rtText = rtText.substring(0, rdsDisplayManagerProperties.getMaxLength() - 1);
 		String rtCommandToSend = rdsDisplayManagerProperties.getRtCommandPrefix() + rtText + rdsDisplayManagerProperties.getCommandTerminaison();
 		return rtCommandToSend;
 	}
 
 	/**
 	 * Prepare the PS command to display title on RDS
 	 * @param titleToDisplay the title 
 	 * @return the command as string
 	 */
 	private String preparePSTitleCommand(BroadcastableTitle titleToDisplay) {
 		String psText = titleToDisplay.getArtist() + " " + titleToDisplay.getTitle();
 		if (psText.length() > rdsDisplayManagerProperties.getMaxLength())
 			psText = psText.substring(0, rdsDisplayManagerProperties.getMaxLength() - 1);
 		String psCommandToSend = rdsDisplayManagerProperties.getPsCommandPrefix() + psText + rdsDisplayManagerProperties.getCommandTerminaison();
 		return psCommandToSend;
 	}
 	
 	/**
 	 * Display the idle text
 	 */
 	public void displayIdleText() {
 		LOGGER.debug("Display the idle text");
 		String psCommandToSend = rdsDisplayManagerProperties.getPsCommandPrefix() + rdsDisplayManagerProperties.getPsIdle() + rdsDisplayManagerProperties.getCommandTerminaison();
 		String rtCommandToSend = rdsDisplayManagerProperties.getRtCommandPrefix() + rdsDisplayManagerProperties.getRtIdle() + rdsDisplayManagerProperties.getCommandTerminaison();
 		
 		//send command
 		sendCommandsToRDS(psCommandToSend, rtCommandToSend, null);
 	}
 	
 	/**
 	 * Send command to the RDS
 	 * @param psCommand the PS command as string
 	 * @param rtCommand the RT command as String
 	 * @param title title to log
 	 */
 	private void sendCommandsToRDS(String psCommand, String rtCommand, BroadcastableTitle title) {
 		LOGGER.debug("Command to send for PS : " + psCommand);
 		LOGGER.debug("Command to send for RT : " + rtCommand);
 		
 		//send PS command
 		String psCommandReturn = null;
 		try {
 			psCommandReturn = rs232WireService.sendCommand(psCommand);
 			psCommandReturn = processCommandReturnAndVirtualMode(CommandType.PS, psCommandReturn);
 		} catch (IOException | RS232RDSCoderException e) {
 			LOGGER.error("Error while sending PS command", e);
 			alertMailService.sendRDSCoderErrorAlert(e);
 		}
 		
 		//send RT command
 		String rtCommandReturn = null;
 		try {
 			rtCommandReturn = rs232WireService.sendCommand(rtCommand);
 			rtCommandReturn = processCommandReturnAndVirtualMode(CommandType.RADIOTEXT, rtCommandReturn);
 		} catch (IOException | RS232RDSCoderException e) {
 			LOGGER.error("Error while sending RT command", e);
 			alertMailService.sendRDSCoderErrorAlert(e);
 		}
 		
 		//db log
 		logCommandsIntoDB(psCommand, psCommandReturn, rtCommand, rtCommandReturn, title);
 	}
 
 	/**
 	 * Log the commands into db
 	 * @param psCommand the PS command sent
 	 * @param psCommandReturn the PS command return
 	 * @param rtCommand the RT command sent
 	 * @param rtCommandReturn the RT command return
 	 * @param title title to display
 	 */
 	private void logCommandsIntoDB(String psCommand, String psCommandReturn, String rtCommand, String rtCommandReturn, BroadcastableTitle title) {
 		//log into db
 		RDSDisplay rdsDisplayDocument = new RDSDisplay();
 		rdsDisplayDocument.setBroadcastableTitle(title);
 		rdsDisplayDocument.setDate(Calendar.getInstance().getTime());
 		rdsDisplayDocument.setRs232Commands(new RS232Commands());
 		rdsDisplayDocument.getRs232Commands().setPsCommand(psCommand);
 		rdsDisplayDocument.getRs232Commands().setPsCommandReturn(psCommandReturn);
 		rdsDisplayDocument.getRs232Commands().setRtCommand(rtCommand);
 		rdsDisplayDocument.getRs232Commands().setRtCommandReturn(rtCommandReturn);
 		
 		mongoDBRepository.insertRDSDisplay(rdsDisplayDocument);
 	}
 	
 	/**
 	 * Process the return text
 	 * @param commandType the sent command type
 	 * @param returnedText the returned text
 	 * @return the returned text if OK
 	 * @throws RS232RDSCoderException if not the expected text
 	 */
 	private String processCommandReturnAndVirtualMode(CommandType commandType, String returnedText) throws RS232RDSCoderException {
 		if (!rs232Config.isVirtualMode()) {
 			String expectedText = null;
 			switch (commandType) {
 			case INIT:
 				expectedText = rs232Config.getInitCommandReturn();
 				break;
 				
 			case PS:
 				expectedText = rdsDisplayManagerProperties.getPsCommandReturn();
 				break;
 
 			case RADIOTEXT:
 				expectedText = rdsDisplayManagerProperties.getRtCommandReturn();
 				break;
 			}
 			if (expectedText.equals(returnedText))
 				return returnedText;
 			else {
 				String message = "Bad return text on " + commandType.toString() + " : " + returnedText;
 				LOGGER.error(message);
 				throw new RS232RDSCoderException(message);
 			}
 			
 		} else { //we are in virtual mode
 			LOGGER.debug("Virtual mode enabled - using defaut return text");
 			return rdsDisplayManagerProperties.getVirtualModeReturnText();
 		}
 	}
 	
 	@Override
 	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
 		context = applicationContext;
 	}
 	
 	/**
 	 * Send the init commands to RDS
 	 */
 	@PostConstruct
 	public void initRDS() {
		String initCommand = rs232Config.getInitCommand();
 		String psCommand = rdsDisplayManagerProperties.getPsCommandPrefix() + rdsDisplayManagerProperties.getPsInitCommand() + rdsDisplayManagerProperties.getCommandTerminaison();
 		String rtCommand = rdsDisplayManagerProperties.getRtCommandPrefix() + rdsDisplayManagerProperties.getRtInitCommand() + rdsDisplayManagerProperties.getCommandTerminaison();
 		LOGGER.debug("Command to send for RDS init : " + initCommand);
 		LOGGER.debug("Command to send for PS : " + psCommand);
 		LOGGER.debug("Command to send for RT : " + rtCommand);
 		
 		//send PS command
 		String initCommandReturn = null;
 		try {
 			initCommandReturn = rs232WireService.sendCommand(initCommand);
 			initCommandReturn = processCommandReturnAndVirtualMode(CommandType.INIT, initCommandReturn);
 		} catch (IOException | RS232RDSCoderException e) {
 			LOGGER.error("Error while sending RDS port init command - stop context", e);
 			alertMailService.sendCOMPortErrorAlert(e);
 			((AbstractApplicationContext) context).close(); //unload context (stop starting)
 		}
 				
 		//send PS command
 		String psCommandReturn = null;
 		try {
 			psCommandReturn = rs232WireService.sendCommand(psCommand);
 			psCommandReturn = processCommandReturnAndVirtualMode(CommandType.PS, psCommandReturn);
 		} catch (IOException | RS232RDSCoderException e) {
 			LOGGER.error("Error while sending PS init command - stop context", e);
 			alertMailService.sendCOMPortErrorAlert(e);
 			((AbstractApplicationContext) context).close(); //unload context (stop starting)
 		}
 		
 		//send RT command
 		String rtCommandReturn = null;
 		try {
 			rtCommandReturn = rs232WireService.sendCommand(rtCommand);
 			rtCommandReturn = processCommandReturnAndVirtualMode(CommandType.RADIOTEXT, rtCommandReturn);
 		} catch (IOException | RS232RDSCoderException e) {
 			LOGGER.error("Error while sending RT init command - stop context", e);
 			alertMailService.sendCOMPortErrorAlert(e);
 			((AbstractApplicationContext) context).close(); //unload context (stop starting)
 		}
 		
 		//db log
 		logCommandsIntoDB(psCommand, psCommandReturn, rtCommand, rtCommandReturn, null);
 	}
 	
 }
