 import java.lang.*;
 import java.util.*;
 
 class Value {
 	private Node	node=null;
 	private Datum	datum=null;
 	private int reserved = -1;
 	private Schedule schedule = null;
 			
 	public Value() {
 	}
 		
 	public Value(Node n, Datum d, String time) {
 		node = n;
 		if (time != null && time.trim().length() > 0) {	// don't set to default if no time specified
 			n.setTimeStamp(time);
 		}
 		init(n.getLocalName(),d);
 	}
 		
 	public Value(String s, Datum d) {
 		// no associated node - so a temporary variable
 		init(s,d);
 	}
 		
 	public Value(String s, Datum d, int reserved, Schedule schedule) {
 		this.reserved = reserved;
 		this.schedule = schedule;
 		init(s,d);
 	}
 		
 	public Node getNode() { return node; }
 		
 	public void setDatum(Datum d, String time) { 
 		if (node != null)	// do set default time, even if no time string specified
 			node.setTimeStamp(time);
 				
 		if (reserved >= 0 && schedule != null) {
 			schedule.setReserved(reserved,d.stringVal());
 		}
 		init(datum.getName(),d);
 	}
 	public Datum getDatum() { return datum; }
 	
 	private void init(String name, Datum d) {
		datum = new Datum(d);
		datum.setName(name);
 	}
 }
