 package application;
 
 import java.text.DecimalFormat;
 import java.util.Date;
 
 public class Transport {
 	private Date departure;
 	private String destination, type, line;
 	
 	public Transport(
 		Date departure,
 		String destination,
 		String type,
 		String line
 	) {
 		this.departure = departure;
 		this.destination = destination;
 		this.type = type;
 		this.line = line;
 	}
 	
 	public Transport(
 		String departure,
 		String destination,
 		String typeLine
 	) {
 		Date date = new Date();
 		String[] time = departure.split(":");
 		
 		date.setHours(Integer.parseInt(time[0]));
 		date.setMinutes(Integer.parseInt(time[1]));
 		date.setSeconds(0);
 		
 		this.departure = date;
 		this.destination = destination;
 		this.line = typeLine;
 		
 		// TODO fix line/type parser
 		this.type = null;
 	}
 	
 	public Date getDeparture() {
 		return this.departure;
 	}
 	
 	public String getDestination() {
 		return this.destination;
 	}
 	
 	public String getLine() {
 		return this.line;
 	}
 	
 	public String getEtc() {
 		Long etc = this.calcEtcSec();
 		etc = (etc < 0) ? 0 : (etc / 60);
 		
 		String etcString;
 		
 		if (etc < 60) {
 			etcString = etc.toString();
 		} else {
 			DecimalFormat tt = new DecimalFormat("#00");
 			etcString = (etc / 60) + ":" + tt.format(etc % 60);
 		}
 		return etcString;
 	}
 	
 	private Long calcEtcSec() {
 		Date now = new Date();
 		Long etc = this.departure.getTime() - now.getTime();
 		etc = etc / 1000;
 		
 		return etc;
 	}
 }
