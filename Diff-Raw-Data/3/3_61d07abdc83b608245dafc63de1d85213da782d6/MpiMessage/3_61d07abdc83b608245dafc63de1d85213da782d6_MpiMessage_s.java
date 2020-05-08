 package mpi;
 
 public class MpiMessage {
 
 	private long timeStamp;
 	private long lookAhead;
 	private int type;
 	
 	public static final int TYPE_EVENT = 0;
 	public static final int TYPE_NULL = 1;
 	
 	private String aircraft = "";
 	private String airport = "";
 	
 	/**
 	 * New aircraft event 
 	 * 
 	 * @param timeStamp
 	 * @param lookAhead
 	 * @param aircraft
 	 * @param airport
 	 */
 	public MpiMessage(long timeStamp, long lookAhead, String aircraft, String airport){
 		setTimeStamp(timeStamp);
 		setLookAhead(lookAhead);
 		setType(MpiMessage.TYPE_EVENT);
 		setAircraft(aircraft);
 		setAirport(airport);		
 	}
 
 	/**
 	 * New null message
 	 * 
 	 * @param timeStamp
 	 * @param lookAhead
 	 */
 	public MpiMessage(long timeStamp, long lookAhead){
 		setTimeStamp(timeStamp);
		setLookAhead(lookAhead);		
 	}
 	
 	public boolean isNullMessage(){
 		if(this.type == MpiMessage.TYPE_NULL){
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean isEvent(){
 		if(!isNullMessage()){
 			return true;
 		}
 		
 		return false;
 	}	
 	
 	public long getTimeStamp() {
 		return timeStamp;
 	}
 
 	public void setTimeStamp(long timeStamp) {
 		this.timeStamp = timeStamp;
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public void setType(int type) {
 		this.type = type;
 	}
 
 	public long getLookAhead() {
 		return lookAhead;
 	}
 
 	public void setLookAhead(long lookAhead) {
 		this.lookAhead = lookAhead;
 	}
 
 	public String getAircraft() {
 		return aircraft;
 	}
 
 	public void setAircraft(String aircraft) {
 		this.aircraft = aircraft;
 	}
 
 	public String getAirport() {
 		return airport;
 	}
 
 	public void setAirport(String airport) {
 		this.airport = airport;
 	}
 	
 }
