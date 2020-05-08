 package dk.dma.aiscoverage.calculator;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
 import dk.dma.aiscoverage.data.BaseStation;
 import dk.dma.aiscoverage.data.Cell;
 import dk.dma.aiscoverage.data.CustomMessage;
 import dk.dma.aiscoverage.project.AisCoverageProject;
 
 
 /** This calculator maintains a buffer for each Ship instance. 
  * 
  * If more than one base station receives messages from a single real world ship
  * a ship instance will be created and associated with each corresponding base station.
  * This is because ship instances holds a message buffer, and this buffer can't be mixed up between base stations.
  * 
  * Rotation is determined based on difference between course over ground (cog) from first and last message in buffer.
  * If rotation is ignored, missing points will only be calculated for ships that are NOT rotating.
  */
 public class CoverageCalculator extends AbstractCalculator {
 
 	private static final long serialVersionUID = 1L;
 	private int bufferInSeconds = 20;
 	private int degreesPerMinute = 20;
 	private boolean ignoreRotation;
 	private double highThreshold = .8;
 	private double lowThreshold = .3;
 	
 
 	public CoverageCalculator(AisCoverageProject project, boolean ignoreRotation){
 		super(project);
 		this.ignoreRotation = ignoreRotation;
 	}
 
 	/**
 	 * This is called whenever a message is received
 	 */
 	public void calculate(CustomMessage message) {
 
 		//put message in ships' buffer
 		message.getShip().addToBuffer(message);
 		
 		// If this message is filtered, we empty the ships' buffer and returns
 		if( filterMessage(message) ){
 			message.getShip().emptyBuffer();
 			return;
 		}
 		
 		//Time difference between first and last message in buffer
 		CustomMessage firstMessage = message.getShip().getFirstMessageInBuffer();
 		CustomMessage lastMessage = message.getShip().getLastMessageInBuffer();
 		double timeDifference = this.getTimeDifference(firstMessage, lastMessage);
 		
 		// Check if it is time to process the buffer
 		if(timeDifference >= bufferInSeconds){
 			
 			if(timeDifference < 1800){
 				List<CustomMessage> buffer = message.getShip().getMessages();
				double rotation = Math.abs( angleDiff((double)firstMessage.getCog(), (double)lastMessage.getCog()) );
 				
 				//Ship is rotating
 				if(rotation > ((double)degreesPerMinute/60)*timeDifference){
 					if(!ignoreRotation){
 						for (int i = 0; i < message.getShip().getMessages().size()-1; i++) {
 							calculateMissingPoints(buffer.get(i), buffer.get(i+1), true);
 						}
 					}
 				}
 				else{
 					for (int i = 0; i < message.getShip().getMessages().size()-1; i++) {
 						calculateMissingPoints(buffer.get(i), buffer.get(i+1), false);
 					}
 				}
 			}
 			
 			//empty buffer
 			message.getShip().emptyBuffer();
 		}
 		
 	}
 	
 	/**
 	 * Calculates missing points between two messages and add them to corresponding cells
 	 */
 	private void calculateMissingPoints(CustomMessage m1, CustomMessage m2, boolean rotating){
 		
 		//Get cell from first message and increment message count
 		Cell cell = m1.getGrid().getCell(m1.getLatitude(), m1.getLongitude());
 		if(cell == null){
 			cell = m1.getGrid().createCell(m1.getLatitude(), m1.getLongitude());
 		}
 		cell.getShips().put(m1.getShip().getMmsi(), m1.getShip());
 		m1.getGrid().incrementMessageCount();
 		cell.incrementNOofReceivedSignals();
 		
 		Long p1Time = m1.getTimestamp().getTime();
 		Long p2Time = m2.getTimestamp().getTime();
 		double p1Lat = m1.getLatitude();
 		double p1Lon = m1.getLongitude();
 		double p2Lat = m2.getLatitude();
 		double p2Lon = m2.getLongitude();
 		double p1X = projection.lon2x(p1Lon, p1Lat);
 		double p1Y = projection.lat2y(p1Lon, p1Lat);
 		double p2X = projection.lon2x(p2Lon, p2Lat);
 		double p2Y = projection.lat2y(p2Lon, p2Lat);
 		
 		double timeSinceLastMessage = getTimeDifference(p1Time, p2Time);
 		int sog = (int) m2.getSog();
 		double expectedTransmittingFrequency = getExpectedTransmittingFrequency(sog, rotating, m1.getShip().getShipClass());
 		
 		// Calculate missing messages and increment missing signal to corresponding cell.
 		// Lat-lon points are calculated to metric x-y coordinates before missing points are calculated.
 		// In order to find corresponding cell, x-y coords are converted back to lat-lon.
 		int missingMessages; 
 		if(timeSinceLastMessage > expectedTransmittingFrequency) {
 
 			// Number of missing points between the two points
			missingMessages = (int) (Math.round((double)timeSinceLastMessage/(double)expectedTransmittingFrequency)-1);
 
 			// Finds lat/lon of each missing point and adds "missing signal" to corresponding cell
 			for (int i = 1; i <= missingMessages; i++) {
 
 				double xMissing = getX((i*expectedTransmittingFrequency),p1Time, p2Time, p1X, p2X);
 				double yMissing = getY(i*expectedTransmittingFrequency,p1Time, p2Time, p1Y, p2Y);
 
 				//Add number of missing messages to cell
 				Cell c = m2.getGrid().getCell(projection.y2Lat(xMissing, yMissing), projection.x2Lon(xMissing, yMissing));
 				if(c == null){
 					c = m2.getGrid().createCell(projection.y2Lat(xMissing, yMissing), projection.x2Lon(xMissing, yMissing));
 				}
 				c.getShips().put(m1.getShip().getMmsi(), m1.getShip());
 				c.incrementNOofMissingSignals();
 			}
 
 		}
 		
 		
 	}
 	
 	/**Calculates the signed difference between angle A and angle B
     *
      * @param a Angle1 in degrees
     * @param b Angle2 in degrees
     * @return The difference in degrees
     */
     private double angleDiff(double a, double b) {
         double difference = b - a;
         while (difference < -180.0)
             difference+=360.0;
                
         while (difference > 180.0)
             difference-=360.0;
        
         return difference;
     }
 
 	
 	
 	
 	
 	/**
 	 * @return 
 	 * A combined coverage of cells from selected base stations.
 	 * If two base stations cover same area, the best coverage is chosen.
 	 * 
 	 * Consider optimizing?
 	 */
 	public Collection<Cell> getCoverage() {
 		HashMap<String, Cell> cells = new HashMap<String, Cell>();
 		//For each base station
 		Collection<BaseStation> basestations = gridHandler.getBaseStations().values();
 		for (BaseStation basestation : basestations) {
 
 			if(basestation.isVisible()){
 				//For each cell
 				Collection<Cell> bscells = basestation.getGrid().values();
 				for (Cell cell : bscells) {
 					Cell existing = cells.get(cell.getId());
 					if(existing == null)
 						cells.put(cell.getId(), cell);
 					else
 						if(cell.getCoverage() > existing.getCoverage())
 							cells.put(cell.getId(), cell);
 				}
 			}
 			
 		}
 		return cells.values();
 	}
 	
 	
 	//Getters and setters
 	private double getY(double seconds, Long p1Time, Long p2Time, double p1y, double p2y){
 		double distanceInMeters = p2y-p1y;
 		double timeDiff = getTimeDifference(p1Time, p2Time);
 		double metersPerSec = distanceInMeters/timeDiff;
 		return p1y + (metersPerSec * seconds);
 	}
 	private double getX(double seconds, Long p1Time, Long p2Time, double p1x, double p2x){
 		double distanceInMeters = p2x-p1x;
 		double timeDiff = getTimeDifference(p1Time, p2Time);
 		double metersPerSec = distanceInMeters/timeDiff;
 		return p1x + (metersPerSec * seconds);
 	}
 	public int getBufferInSeconds() {
 		return bufferInSeconds;
 	}
 	public void setBufferInSeconds(int bufferInSeconds) {
 		this.bufferInSeconds = bufferInSeconds;
 	}
 	public int getDegreesPerMinute() {
 		return degreesPerMinute;
 	}
 	public void setDegreesPerMinute(int degreesPerMinute) {
 		this.degreesPerMinute = degreesPerMinute;
 	}
 	public boolean isIgnoreRotation() {
 		return ignoreRotation;
 	}
 	public void setIgnoreRotation(boolean ignoreRotation) {
 		this.ignoreRotation = ignoreRotation;
 	}
 	public double getHighThreshold() {
 		return highThreshold;
 	}
 
 	public void setHighThreshold(double highThreshold) {
 		this.highThreshold = highThreshold;
 	}
 
 	public double getLowThreshold() {
 		return lowThreshold;
 	}
 	public void setLowThreshold(double lowTHreshold) {
 		this.lowThreshold = lowTHreshold;
 	}
 
 }
