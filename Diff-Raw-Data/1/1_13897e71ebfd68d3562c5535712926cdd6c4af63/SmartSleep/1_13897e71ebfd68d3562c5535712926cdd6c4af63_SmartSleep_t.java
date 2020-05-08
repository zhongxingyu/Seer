 package robot.engine.relist.ocr;
 
 import java.awt.image.BufferedImage;
 
 import ocr.Ocr;
 
 import org.sikuli.api.ScreenRegion;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import robot.engine.commons.Controller;
 import robot.engine.commons.ImageLogger;
 import robot.region.TradePileRegion;
 
 public class SmartSleep {
 	private static final int DEFAULT_SLEEP_IN_MILLIS = 3600000; // default to 1 hour
 	
 	private TradePileRegion tradePileRegion;
 	private Controller controller;
 	private Ocr ocr;
 	
 	public SmartSleep(Controller controller, TradePileRegion tradePileRegion) {
 		this.controller = controller;
 		this.tradePileRegion = tradePileRegion;
 		ocr = new Ocr();
 	}
 
 	private boolean clickFirstCard() {
 		Logger logger = LoggerFactory.getLogger(this.getClass());
 		ScreenRegion firstCard = tradePileRegion.findFirstCard(1000);
 		boolean successful = false;
 		logger.info("looking for first card...");
 		if(firstCard != null) {
 			logger.info("clicking first card");
 			controller.clickCenterOf(firstCard);
 			successful = true;
 		}
 		
 		return successful;
 	}
 	
 	private int parseRemainingTime(String s) {
 		Logger logger = LoggerFactory.getLogger(this.getClass());
 		logger.info("Parsing Raw Time Ramaining String");
 		return new TimeParser(s).getInMillis();
 	}
 	
 	private int calculateFromRemaining(BufferedImage timeBox) {
 		Logger logger = LoggerFactory.getLogger(this.getClass());
 		logger.info("OCR Time Remaining - start");
 		String s = ocr.recognizeCharacters(timeBox);
 		logger.info("OCR aquired: {}", s);
 		int timeRemaining = parseRemainingTime(s);
 		logger.info("OCR parsed: {} millis", timeRemaining);
 		return timeRemaining;
 	}
 	
 	// ensure full screen refresh first - wait for a second :)
 	private void sleepForASec() throws InterruptedException {
 		Thread.sleep(1000);
 	}
 
 	public int getRequiredSleepTime() throws InterruptedException {
 		int sleepTime = DEFAULT_SLEEP_IN_MILLIS;
 		Logger logger = LoggerFactory.getLogger(this.getClass());
 		sleepForASec();
 		
 		if(clickFirstCard()) {
 			logger.info("looking for time remaining");
 			ScreenRegion timeRemaining = tradePileRegion.findTimeRemaining(2000);
 			if(timeRemaining != null) {
				sleepForASec();
 				BufferedImage timeBox = timeRemaining.capture();
 				ImageLogger.getLogger().logToResource(timeBox);
 				sleepTime = calculateFromRemaining(timeBox);
 			}
 			else {
 				logger.info("could not find time remaining!");
 			}
 		}
 
 		return sleepTime;
 	}
 }
