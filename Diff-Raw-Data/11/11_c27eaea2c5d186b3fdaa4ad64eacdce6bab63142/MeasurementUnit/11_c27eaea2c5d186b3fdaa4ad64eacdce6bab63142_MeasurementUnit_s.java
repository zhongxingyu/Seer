 package eu.play_project.dcep.distributedetalis.measurement;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.play_project.dcep.api.measurement.NodeMeasuringResult;
 import eu.play_project.dcep.distributedetalis.DistributedEtalis;
 import eu.play_project.dcep.distributedetalis.JtalisInputProvider;
 import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;
 import eu.play_project.dcep.distributedetalis.PrologSemWebLib;
 import eu.play_project.dcep.distributedetalis.measurement.fsm.MeasureProcessingTime;
 import eu.play_project.dcep.distributedetalis.measurement.fsm.MeasurementState;
 import eu.play_project.dcep.distributedetalis.measurement.fsm.MeasuremnetFinished;
 import eu.play_project.dcep.distributedetalis.measurement.fsm.Ready;
 import eu.play_project.dcep.distributedetalis.measurement.fsm.WaitForComplexMeasurementEvent;
 import eu.play_project.dcep.distributedetalis.measurement.fsm.WaitForMeasuredData;
 import eu.play_project.play_platformservices.api.EpSparqlQuery;
 import eu.play_project.play_platformservices.api.QueryDetails;
 import fr.inria.eventcloud.api.CompoundEvent;
 
 /**
  * Measure performance for a dEtalis node.
  * @author sobermeier
  */
 public class MeasurementUnit implements MeasurementState{
 	private final Logger logger;
 	private MeasurementState state; // State for the measurement.
 	private final PrologSemWebLib semWebLib;
 	private final DistributedEtalis cepEngine;
 	
 	PlayJplEngineWrapper  etalis;
 
 	private int numberOfInputEvents = 0;
 	private int numberOfOutputEvents = 0;
 	private long totalInputEvents = 0;
 	private long totalOutputEvents = 0;
 	private boolean inMeasurementMode = false;
 	private List<Long> singleEventTime; // Time for one event.
 	private NodeMeasuringResult measuredValues; //Statistics data from cepEngine.
 	
 	//Config
 	public static int mEvents = 10; //Defines the number of events used to measure the performance. Default 10
 	public static int eventsPeriod = 5; // Defines the number of events send in one measuring period. Default 5
 
 	public void sendMeasureEvents(){
 		state.sendMeasuringEvent();
 	}
 
 	public MeasurementUnit(DistributedEtalis cepEngine, PlayJplEngineWrapper etalis, PrologSemWebLib semWebLib){
 		this.logger = LoggerFactory.getLogger(MeasurementUnit.class);
 		this.cepEngine = cepEngine;
 		this.etalis = etalis;
 		this.semWebLib = semWebLib;
 		this.state = create("Start");
 		
 		this.singleEventTime = new ArrayList<Long>();
 		
 		//Register measurement pattern.
 		EpSparqlQuery eq = new EpSparqlQuery();
		// eq.setEleQuery("complexN(MeasureCEID,'measurement-pattern') do (generateConstructResult(['http://play-project.eu/measurement/event'],['http://play-project.eu/timeOneEvent'],[Vtime],MeasureCEID))<-('measurementEvent'(ViD1) 'WHERE' (rdf(Vs, 'http://play-project.eu/startTime', Vtime, ViD1), incrementReferenceCounter(ViD1), incrementReferenceCounter(ViD1), incrementReferenceCounter(ViD1), incrementReferenceCounter(ViD1), incrementReferenceCounter(ViD1), incrementReferenceCounter(ViD1), incrementReferenceCounter(ViD1), incrementReferenceCounter(ViD1), incrementReferenceCounter(ViD1), incrementReferenceCounter(ViD1),random(1000000, 9000000, MeasureCEID)))");
		eq.setEleQuery("complex(MeasureCEID) <- 'measurementEvent'(ViD1)");
 
 		QueryDetails qd = new QueryDetails();
 		qd.setQueryId("'measurement-pattern'");
 		eq.setQueryDetails(qd);
 		cepEngine.registerEventPattern(eq);
 	}
 
 	@Override
 	public void startMeasurement(int period) {
 		logger.debug("Measurement request: " + period + "ms.");
 		
 		//Clear counters
 		numberOfInputEvents =0;
 		numberOfOutputEvents = 0;
 		singleEventTime = new ArrayList<Long>();
 		this.setInMeasurementMode(true);
 		
 		state.startMeasurement(period);
 	}
 	
 	@Override
 	public void eventReceived() {
 		totalInputEvents++;
 		state.eventReceived();
 	}
 
 	@Override
 	public void eventProduced(CompoundEvent event, String patternId) {
 		totalOutputEvents++;
 		state.eventProduced(event, patternId);
 	}
 	
 	public MeasurementState create(String name){
 		MeasurementState state = null;
 		
 		if(name.equals("Start")){
 			state = new Ready(etalis, this);
 		}else if (name.equals("MeasureProcessingTime")){
 			state = new MeasureProcessingTime(this, cepEngine, semWebLib);
 		}else if (name.equals("WaitForComplexMeasurementEvents")){
 			state = new WaitForComplexMeasurementEvent(this, 0);
 		}else if (name.equals("WaitForMeasuredData")){
 			state = new WaitForMeasuredData(this);
 			logger.info("Create WaitForMeasuredData");
 		}else if(name.equals("MeasurementFinished")){
 			state = new MeasuremnetFinished(this);
 		}
 		
 		if(state==null){
 			throw new RuntimeException("It is not possible to generate a objet for the name: " + name);
 		}
 		
 		return state;
 	}
 
 	public boolean inMeasurementMode() {
 		return inMeasurementMode;
 	}
 
 	public long getTotalEventsProduced() {
 		return totalOutputEvents;
 	}
 
 
 	public MeasurementState getState() {
 		return state;
 	}
 
 	public void setState(MeasurementState state) {
 		this.state = state;
 	}
 
 	
 	@Override
 	public NodeMeasuringResult getMeasuringResults() {
 		logger.debug("Request measured data.");
 		logger.debug("State: " + state.getName());
 		NodeMeasuringResult n = state.getMeasuringResults();
 		return n;
 	}
 
 	@Override
 	public void setMeasuredData(NodeMeasuringResult measuredValues) {
 		state.setMeasuredData(measuredValues);
 	}
 	
 	/**
 	 * Return average singele event time.
 	 * @return
 	 */
 	public long getSingleEventTime() {
 		long singelEventTimeResult =0;
 		for (Long time : singleEventTime ) {
 			singelEventTimeResult += time;
 		}
 		
 		singelEventTimeResult /=singleEventTime.size();
 		singleEventTime= new ArrayList<Long>();
 		return singelEventTimeResult;
 	}
 
 	public void addSingleEventTime(long singleEventTime) {
 		logger.info(" Single event time: " + singleEventTime);
 		this.singleEventTime.add(singleEventTime);
 	}
 
 	public int getNumberOfInputEvents() {
 		return numberOfInputEvents;
 	}
 
 	public void setNumberOfInputEvents(int numberOfInputEvents) {
 		this.numberOfInputEvents = numberOfInputEvents;
 	}
 
 	public int getNumberOfOutputEvents() {
 		return numberOfOutputEvents;
 	}
 
 	public void setNumberOfOutputEvents(int numberOfOutputEvents) {
 		this.numberOfOutputEvents = numberOfOutputEvents;
 	}
 	public synchronized void setMeasuredValues(NodeMeasuringResult measuredValues) {
 		state.setMeasuredData(measuredValues);
 		this.measuredValues = measuredValues;
 	}
 
 
 	public void setInMeasurementMode(boolean inMeasurementMode) {
 		this.inMeasurementMode = inMeasurementMode;
 	}
 	
 	public  NodeMeasuringResult getMeasurementData() {
 
 		// If the measurement finished, publish the result. Else null.
 		if (measuredValues != null) {
 			measuredValues.setNumberOfComponentInputEvetns(numberOfInputEvents);
 			measuredValues.setNumberOfOutputEvents(numberOfOutputEvents);
 			measuredValues.setProcessingTimeForOneEvent(this.getSingleEventTime());
 			measuredValues.setNumberOfEventsProcessedSinceStartUp(totalInputEvents);
 			measuredValues.setNumberOfEtalisInputEvents(JtalisInputProvider.getEventCounter());
 			measuredValues.setCompontenQueue(cepEngine.getService().getRequestCount());
 			measuredValues.setEtalisInputQueue(cepEngine.getEventInputProvider().getInputQueueSize());
 			
 			
 			return measuredValues;
 		} else {
 			logger.debug("No measured data.");
 
 			return null;
 		}
 	}
 	@Override
 	public String getName() {
 		return "MeasurementUnit";
 	}
 
 	@Override
 	public void sendMeasuringEvent() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void measuringPeriodIsUp() {
 		state.measuringPeriodIsUp();
 	}
 }
