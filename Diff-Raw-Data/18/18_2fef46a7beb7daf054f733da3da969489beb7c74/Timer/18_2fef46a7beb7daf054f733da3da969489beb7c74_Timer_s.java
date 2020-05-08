 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public class Timer {
 
 	private Calendar simulationClock;
 	private Calendar maxSimulationTime;
 	private int tickSize_seconds;
 	private int maxSimulationTime_secs;
 	
 	/**
 	 * Constructor for the Timer class. 
 	 * @param _simulation A link back to the simulation that created this Timer
 	 * @param _maxSimulationTime_secs the number of seconds that the simulation may run for
 	 * @param _tickSize_secs the length of each 'tick' in seconds
 	 */
 	public Timer (int _maxSimulationTime_secs, int _tickSize_secs) {
		setMaxSimulationTime_secs(_maxSimulationTime_secs);
		setTickSize(_tickSize_secs);
 		createTimers();
 	}
 
 	private void createTimers() {
 
 		simulationClock = Calendar.getInstance();
 		simulationClock.set(2000, 0, 0, 0, 0, 0); //NB: Arbitrary year *after* the 1970 epoch, to keep the date math simple (ie, positive milliseconds)
 
 		this.maxSimulationTime = Calendar.getInstance();
 		this.maxSimulationTime.set(2000, 0, 0, 0, 0, 0); //NB: Arbitrary year *after* the 1970 epoch, to keep the date math simple (ie, positive milliseconds)
 		this.maxSimulationTime.add(Calendar.SECOND, maxSimulationTime_secs);
 	}
 	
	public void setTickSize(int tickSize_secs){
 		this.tickSize_seconds = tickSize_secs;
 	}
 	
	public int getTickSize(){
 		return tickSize_seconds;
 	}
 	
 	public void setMaxSimulationTime(int maxSimulationTime_secs){
 		this.maxSimulationTime_secs=maxSimulationTime_secs;
 	}
 	
 	public Date getCurrentTime(){
		simulationClock.getTime();
 		//h,m,s
 	}
 
 	
 	public void increaseTick(){
 		
 	}
 	
 	public int getCurrentTick(){
		
 	}
 		
 	
 	@SuppressWarnings("unused")
 	private void createTimers_GregorianCalendar(int _maxSimulationTime_secs, int _tickSize_secs) {
 		
 		this.tickSize_seconds = _tickSize_secs;
 		
 		simulationClock = new GregorianCalendar(0, 0, 0, 0, 0, 0);
 
 		this.maxSimulationTime = new GregorianCalendar(0, 0, 0, 0, 0, 0);
 		this.maxSimulationTime.add(Calendar.SECOND, _maxSimulationTime_secs);
 	}
 	
 
 	public boolean isMaxSimulationTimeExceeded() {
 		if( simulationClock.getTimeInMillis() >= maxSimulationTime.getTimeInMillis() ) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public void advanceByTick() {
 		simulationClock.add(Calendar.SECOND, tickSize_seconds);
 		
 		/*System.out.println(simulationClock.getTimeInMillis() + " >= " + maxSimulationTime.getTimeInMillis() + "\n" +
 							"simulationClock >= maxSimulationTime = " + "\n" +
 							(simulationClock.getTimeInMillis() >= maxSimulationTime.getTimeInMillis()) );*/
 		
 	}
 	
 	
 	
 }
