 package members;
 
 /**
  * @author Andrée & Victor
  * 
  * Data structure for handling laps.
  *
  */
 public class Lap {
 	private final Time start, end;
 	
 	/**
 	 * @param s Start time
 	 * @param e End time or lap moment
 	 */
 	public Lap(Time s, Time e) {
 		start = s;
 		end = e;
 	}
 
 	/**
 	 * @return Start time
 	 */
 	public Time getStart() {
 		return start;
 	}
 
 	/**
 	 * @return End time
 	 * @see #getLapMoment()
 	 */
 	public Time getEnd() {
 		return end;
 	}
 	
 	/**
 	 * @return Lap moment
 	 * @see #getEnd()
 	 */
 	public Time getLapMoment() {
 		return end;
 	}
 
 	/**
 	 * @return Lap time
 	 */
 	public Time getTotal() {
 		return start.difference(end);
 	}
 	
 	@Override
 	public String toString() {
 		return getTotal().toString();
 	}
 }
