 package dk.dma.aiscoverage.calculator;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import dk.dma.aiscoverage.calculator.geotools.GeoConverter;
 import dk.dma.aiscoverage.calculator.geotools.SphereProjection;
 import dk.dma.aiscoverage.data.BaseStation;
 import dk.dma.aiscoverage.data.BaseStationHandler;
 import dk.dma.aiscoverage.data.CustomMessage;
 import dk.dma.aiscoverage.data.Ship;
 import dk.dma.aiscoverage.data.BaseStation.ReceiverType;
 import dk.dma.aiscoverage.data.Ship.ShipClass;
 import dk.dma.aiscoverage.event.AisEvent;
 import dk.dma.aiscoverage.project.AisCoverageProject;
 import dk.dma.aiscoverage.project.ProjectHandler;
 import dk.frv.ais.geo.GeoLocation;
 import dk.frv.ais.message.AisMessage;
 import dk.frv.ais.message.AisMessage4;
 import dk.frv.ais.message.AisMessage5;
 import dk.frv.ais.message.IGeneralPositionMessage;
 import dk.frv.ais.message.ShipTypeCargo;
 import dk.frv.ais.message.ShipTypeCargo.ShipType;
 import dk.frv.ais.proprietary.IProprietarySourceTag;
 
 /**
  * See CoverageCalculator and DensityPlotCalculator for examples of how to extend this class.
  * When a calculator is added to an AisCoverageProject instance, the calculator automatically receives
  * CustomMessages via calculate().
  * 
  */
 public abstract class AbstractCalculator implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	transient protected SphereProjection projection = new SphereProjection();
 	protected BaseStationHandler gridHandler = new BaseStationHandler(this);
 	private double latSize = -1;
 	private double longSize = -1;
 	private int cellSize = 2500;
 	protected AisCoverageProject project;
 	protected Map<ShipClass, ShipClass> allowedShipClasses = new ConcurrentHashMap<ShipClass, ShipClass>();
 	protected Map<ShipType, ShipType> allowedShipTypes = new ConcurrentHashMap<ShipType, ShipType>();
 	protected Map<Long, Boolean> allowedShips = new ConcurrentHashMap<Long, Boolean>();
 	protected CustomMessage firstMessage = null;
 	protected CustomMessage currentMessage = null;
 	
 	abstract public void calculate(CustomMessage m);
 	
 	/**
 	 * This is called by message handlers whenever a new message is received.
 	 */
	public synchronized void processMessage(AisMessage aisMessage, String defaultID) {
 		
 		CustomMessage newMessage = aisToCustom(aisMessage, defaultID);
 		if(newMessage != null){
 			calculate(newMessage);
 		}
 		
 	}
 	
 	/**
 	 * Determines the expected transmitting frequency, based on speed over ground(sog),
 	 * whether the ship is rotating and ship class.
 	 * This can be used to calculate coverage.
 	 */
 	public double getExpectedTransmittingFrequency(double sog, boolean rotating, ShipClass shipClass){
 		double expectedTransmittingFrequency;
 		if(shipClass == ShipClass.CLASS_A){		
 			if(rotating){
 				if(sog < .2)
 					expectedTransmittingFrequency = 180;
 				else if(sog < 14)
 					expectedTransmittingFrequency = 3.33;
 				else if(sog < 23)
 					expectedTransmittingFrequency = 2;
 				else 
 					expectedTransmittingFrequency = 2;
 			}else{
 				if(sog < .2)
 					expectedTransmittingFrequency = 180;
 				else if(sog < 14)
 					expectedTransmittingFrequency = 10;
 				else if(sog < 23)
 					expectedTransmittingFrequency = 6;
 				else 
 					expectedTransmittingFrequency = 2;
 			}
 		}
 		else{
 			if(sog <= 2)
 				expectedTransmittingFrequency = 180;
 			else
 				expectedTransmittingFrequency = 30;
 		}
 		
 		return expectedTransmittingFrequency;
 		
 	}
 	
 	/*
 	 * Use this method to filter out unwanted messages. 
 	 * The filtering is based on rules of thumbs. For instance, if a distance 
 	 * between two messages is over 2000m, we filter
 	 */
 	public boolean filterMessage(CustomMessage customMessage){
 
 		if(customMessage.getCog() < 3 || customMessage.getSog() > 50)
 			return true;
 		if(customMessage.getCog() == 360)
 			return true;
 
 		// check distance from last message to new message
 		CustomMessage firstMessage = customMessage.getShip().getFirstMessageInBuffer();
 		CustomMessage lastMessage = customMessage.getShip().getLastMessageInBuffer();
 		if(lastMessage != null){
 			projection.setCentralPoint(firstMessage.getLongitude(), firstMessage.getLatitude());
 			double distance = projection.distBetweenPoints(firstMessage.getLongitude(), firstMessage.getLatitude(), lastMessage.getLongitude(), lastMessage.getLatitude());
 			if(distance > 2000){
 //				System.out.println(distance);
 				return true;
 			}
 		}
 		return false;
 		
 	}
 	
 
 	protected void extractBaseStationPosition(AisMessage4 m){
 		BaseStation b = gridHandler.getBaseStations().get(m.getUserId()+"");
 		if (b != null) {
 			b.setLatitude( m.getPos().getGeoLocation().getLatitude() );
 			b.setLongitude( m.getPos().getGeoLocation().getLongitude() );
 
 			ProjectHandler.getInstance().broadcastEvent(new AisEvent(AisEvent.Event.BS_POSITION_FOUND, this, b));
 		}
 	}
 	protected boolean isShipAllowed(AisMessage aisMessage){
 		if(allowedShipTypes.size() > 0){
 			
 			// Ship type message
 			if(aisMessage instanceof AisMessage5){
 				
 				//if ship type is allowed, we add ship mmsi to allowedShips map
 				AisMessage5 m = (AisMessage5) aisMessage;
 				ShipTypeCargo shipTypeCargo = new ShipTypeCargo(m.getShipType());
 				if(allowedShipTypes.containsKey(shipTypeCargo.getShipType())){
 					allowedShips.put(m.getUserId(), true);
 				}
 				// It's not a position message, so we return false
 				return false;
 
 			}	
 			
 			// if ship isn't in allowedShips we don't process the message
 			if(!allowedShips.containsKey(aisMessage.getUserId()) ){
 				return false;
 			}
 		}
 		return true;
 	}
 	protected ShipClass extractShipClass(AisMessage aisMessage){
 		if (aisMessage.getMsgId() == 18) {
 			// class B
 			return Ship.ShipClass.CLASS_B;
 		} else {
 			// class A
 			return Ship.ShipClass.CLASS_A;
 		}
 	}
 	
 	/**
 	 * Calculates lat/lon sizes based on a meter scale and a lat/lon position
 	 */
 	protected void calculateLatLonSize(double latitude){
 		double cellInMeters= getCellSize(); //cell size in meters
 		setLatSize(GeoConverter.metersToLatDegree(cellInMeters));
 		setLongSize(GeoConverter.metersToLonDegree(latitude, cellInMeters));
 	}
 	
 	/**
 	 * Check if grid exists (If a message with that bsmmsi has been received before)
 	 * Otherwise create a grid for corresponding base station.
 	 */
 	protected BaseStation extractBaseStation(String baseId, ReceiverType receiverType){
 		BaseStation grid = gridHandler.getGrid(baseId);
 		if (grid == null) {
 			grid = gridHandler.createGrid(baseId);
 			grid.setReceiverType(receiverType);
 		}
 		return grid;
 	}
 	
 	/**  Check which ship sent the message.
 	 *	If it's the first message from that ship, create ship and put it in
 	 *	base statino that received message
 	 */
 	protected Ship extractShip(long mmsi, ShipClass shipClass, BaseStation baseStation){
 		Ship ship = baseStation.getShip(mmsi);
 		if (ship == null) {
 			baseStation.createShip(mmsi, shipClass);
 			ship = baseStation.getShip(mmsi);
 		}
 		return ship;
 	}
 	
 	/** The aisToCustom method is used to map AisMessages to CustomMessages. It also takes care of creating base station instances,
 	 * ship instances and to set up references between these. Override it if you want to handle this in a different way.
 	 */
 	public CustomMessage aisToCustom(AisMessage aisMessage, String defaultID){
 		
 		//Stops analysis if project has been running longer than timeout
 		long timeSinceStart = project.getRunningTime();
 		if (project.getTimeout() != -1 && timeSinceStart > project.getTimeout())
 			project.stopAnalysis();
 
 		String baseId = null;
 		ReceiverType receiverType = ReceiverType.NOTDEFINED;
 		IGeneralPositionMessage posMessage = null;
 		GeoLocation pos = null;
 		Date timestamp = null;
 		ShipClass shipClass = null;
 
 
 		// Get source tag properties
 		IProprietarySourceTag sourceTag = aisMessage.getSourceTag();
 		if (sourceTag != null) {
 			Long bsmmsi = sourceTag.getBaseMmsi();
 			timestamp = sourceTag.getTimestamp();
 //			srcCountry = sourceTag.getCountry();
 			String region = sourceTag.getRegion();
 			if(bsmmsi == null){
 				if(!region.equals("")){
 					baseId = region;
 					receiverType = ReceiverType.REGION;
 				}
 			}else{
 				baseId = bsmmsi+"";
 				receiverType = ReceiverType.BASESTATION;
 			}
 		}
 
 		//Checks if its neither a basestation nor a region
 		if (baseId == null){
 			baseId = defaultID;
 		}
 		
 		// If time stamp is not present, we add one
 		if(timestamp == null){
 			timestamp = new Date();
 		}
 		
 
 		// It's a base station positiion message
 		if (aisMessage instanceof AisMessage4) {
 			extractBaseStationPosition((AisMessage4) aisMessage);	
 			return null;
 		}
 		
 		
 		// if no allowed ship types has been set, we process all ship types
 		if(!isShipAllowed(aisMessage))
 			return null;
 
 		// Handle position messages. If it's not a position message 
 		// the calculators can't use them
 		if (aisMessage instanceof IGeneralPositionMessage)
 			posMessage = (IGeneralPositionMessage) aisMessage;
 		else 
 			return null;
 		
 		//Check if ship type is allowed
 		shipClass = extractShipClass(aisMessage);
 		if(!allowedShipClasses.containsKey(shipClass))
 			return null;
 
 		// Check if position is valid
 		if (!posMessage.isPositionValid()) {
 			return null;
 		}
 
 		// Get location
 		pos = posMessage.getPos().getGeoLocation();
 		
 		//calculate lat lon size based on first message
 		if(firstMessage == null){
 			calculateLatLonSize(pos.getLatitude());
 		}
 
 
 		// Extract Base station
 		BaseStation baseStation = extractBaseStation(baseId, receiverType);
 
 
 		// Extract ship
 		Ship ship = extractShip(aisMessage.getUserId(), shipClass, baseStation);
 
 		CustomMessage newMessage = new CustomMessage();
 		newMessage.setCog( (double) posMessage.getCog() / 10 );
 		newMessage.setSog( (double) posMessage.getSog() / 10 );
 		newMessage.setLatitude( posMessage.getPos().getGeoLocation()
 				.getLatitude() );
 		newMessage.setLongitude( posMessage.getPos().getGeoLocation()
 				.getLongitude() );
 		newMessage.setTimestamp( timestamp );
 		newMessage.setGrid( baseStation );
 		newMessage.setShip( ship );
 		
 		// Keep track of current message
 		currentMessage = newMessage;
 		
 		// Keep track of first message
 		if(firstMessage == null){
 			firstMessage = newMessage;
 		}
 
 		return newMessage;
 	}
 	
 	
 	
 	//getters and setters
 	
 	
 	public double getLatSize() {
 		return latSize;
 	}
 	public void setLatSize(double latSize) {
 		this.latSize = latSize;
 		gridHandler.setLatSize(latSize);
 	}
 	public double getLongSize() {
 		return longSize;
 	}
 	public void setLongSize(double longSize) {
 		this.longSize = longSize;
 		gridHandler.setLonSize(longSize);
 	}
 	public BaseStationHandler getBaseStationHandler(){
 		return gridHandler;
 	}
 	public String[] getBaseStationNames(){
 		Set<String> set = gridHandler.getBaseStations().keySet();
 		String[] bssmsis = new String[set.size()];
 		int i = 0;
 		for (String s : set) {
 			bssmsis[i] = s;
 			i++;
 		}
 		
 		return bssmsis;
 	}
 	public int getCellSize() {
 		return cellSize;
 	}
 	public void setCellSize(int cellSize) {
 		this.cellSize = cellSize;
 	}
 	
 	/**
 	 * Time difference between two messages in seconds
 	 */
 	public double getTimeDifference(CustomMessage m1, CustomMessage m2){
 		return (double) ((m2.getTimestamp().getTime() - m1.getTimestamp().getTime()) / 1000);
 	}
 	public double getTimeDifference(Long m1, Long m2){
 		return  ((double)(m2 - m1) / 1000);
 	}
 	public Map<ShipClass, ShipClass> getAllowedShipClasses() {
 		return allowedShipClasses;
 	}
 	public void setAllowedShipClasses(Map<ShipClass, ShipClass> allowedShipClasses) {
 		this.allowedShipClasses = allowedShipClasses;
 	}
 	public Map<ShipType, ShipType> getAllowedShipTypes() {
 		return allowedShipTypes;
 	}
 	public void setAllowedShipTypes(Map<ShipType, ShipType> allowedShipTypes) {
 		this.allowedShipTypes = allowedShipTypes;
 	}
 	public AbstractCalculator(AisCoverageProject project){
 		this.project = project;
 	}
 	public CustomMessage getFirstMessage() {
 		return firstMessage;
 	}
 	public CustomMessage getCurrentMessage() {
 		return currentMessage;
 	}
 
 }
