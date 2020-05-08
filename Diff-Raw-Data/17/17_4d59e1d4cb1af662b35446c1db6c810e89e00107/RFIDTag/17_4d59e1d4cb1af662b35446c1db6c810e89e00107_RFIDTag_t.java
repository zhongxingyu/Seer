 package ltg.hg.model;
 
 public class RFIDTag {
 
 	// Assigned attributes
 	public final String id;
 	public final String rfid_tag;
 	public final String color;
 	public final String color_label;
 	
 	// Instantaneous attributes
 	private String current_location_id = null;
 	private boolean is_alive = true;
 	private double remaining_penalyt_time = 0;
 	
 	// Aggregate attributes
 	private double harvest = 0.0d;
 	// per-move attributes
 	private int total_moves = 0;
 	private int arbitrage = 0;
 	// weighted averages
 	private double avg_quality = 0.0d;
 	private double avg_competition = 0.0d;
 	private double avg_risk = 0.0d; 
 	
 	
 	
 	public RFIDTag(String id, String rfid_tag, String color, String color_label) {
 		this.id = id;
 		this.rfid_tag = rfid_tag;
 		this.color = color;
 		this.color_label = color_label;
 	}
 	
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof RFIDTag 
 				&& ((RFIDTag) obj).id.equals(id)
 				&& ((RFIDTag) obj).rfid_tag.equals(rfid_tag)
 			)
 			return true;
 		return false;
 	}
 
 
 	public synchronized String getCurrentLocation() {
 		return current_location_id;
 	}
 	
 	
 	public synchronized void setCurrentLocation(String patch) {
 		this.current_location_id = patch;
 	}
 	
 	public synchronized void resetCurrentLocation() {
 		this.current_location_id = null;
 	}
 	
 	
 	public synchronized void increaseTotalMovesCounter() {
 		this.total_moves++;
 	}
 
 
 	public synchronized void updateArbitrage(double my_current_yield, double my_displayed_future_yield, double my_actual_future_yield) {
 		double perceivedGain = my_displayed_future_yield - my_current_yield;
 		double realGain = my_actual_future_yield - my_current_yield;
 		if (perceivedGain < realGain )
 			System.err.println("This can't be possible unless there is something very wrong in the code");
 		
 		if ( perceivedGain < 0 && realGain < 0 )
 			arbitrage--;
 		else if ( perceivedGain == 0 && realGain < 0 )
 			arbitrage--;
 		else if ( perceivedGain > 0 && realGain == 0 )
 			; // Arbitrage stays the same
 		else if ( perceivedGain > 0 && realGain < 0 )
 			; // Arbitrage stays the same
 		else if ( perceivedGain == 0 && realGain == 0 )
 			; // Arbitrage stays the same
 		else if ( perceivedGain > 0 && realGain > 0 )
 			arbitrage++;
 		else
 			System.err.println("Unconsidered option. Perceived gain: " + perceivedGain + " Real gain: " + realGain);
 	}
 	
 	
 	public synchronized void updateHarvest(double currentYield) {
 		harvest += currentYield;
 	}
 	
 	
 	public synchronized void updateAverageQuality(double totalTime, double step, double currentQuality) {
		avg_quality = movingAverage(avg_quality, currentQuality, totalTime, step);
 	}
 	
 	
 	public synchronized void updateAverageCompetition(double totalTime, double step, double currentCompetition) {
 		avg_competition = movingAverage(avg_competition, currentCompetition, totalTime, step);
 	}
 	
 	
 	public synchronized void updateAverageRisk(double totalTime, double step, double currentRisk) {
 		avg_risk = movingAverage(avg_risk, currentRisk, totalTime, step);
 	}
 	
 	
 	private double movingAverage (double pastValue, double currentVaue, double totalTime, double step) {
 		return ( pastValue * (totalTime - step) + currentVaue * step ) / totalTime;
 	}
 	
 	
 	public synchronized double getHarvest() {
 		return harvest; 
 	}
 
 
	public double getAvgQualityPerSecond() {
 		return avg_quality;
 	}
	
	public double getAvgQualityPerMinute() {
		return avg_quality * 60.0d;
	}
 
 
 	public double getAvgCompetition() {
 		return avg_competition;
 	}
 
 
 	public int getTotalMoves() {
 		return total_moves;
 	}
 
 
 	public int getArbitrage() {
 		return arbitrage;
 	}
 
 
 	public double getAvgRisk() {
 		return avg_risk;
 	}
 	
 	
 	public synchronized void killTag(double penaltyTime) {
 		this.is_alive = false;
 		this.remaining_penalyt_time = penaltyTime; 
 	}
 	
 	
 	public synchronized boolean isAlive() {
 		return this.is_alive;
 	}
 	
 	// Returns true when the tag becomes alive again
 	public synchronized boolean updatePenaltyTime(double step) {
 		if (!is_alive)
 			remaining_penalyt_time -= step;
 		if (remaining_penalyt_time < 0) {
 			resurrectTag();
 			return true;
 		}
 		return false;
 	}
 	
 	private synchronized void resurrectTag() {
 		this.is_alive = true;
 		this.remaining_penalyt_time = 0;
 	}
 	
 }
