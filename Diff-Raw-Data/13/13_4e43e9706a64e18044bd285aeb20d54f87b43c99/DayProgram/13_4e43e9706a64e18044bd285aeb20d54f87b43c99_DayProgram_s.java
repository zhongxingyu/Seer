 package nl.tuestudent.thermostaat.data;
 
 import android.util.Log;
 import nl.tuestudent.thermostaat.CommunicationClass;
 
 public class DayProgram implements CommunicationClass.SubmitResult {
 	
 	
 	private String name;
 	private ProgramSwitch[] switches;
 
 	
 	public class ProgramSwitch {
 
 		private String type;
 		private String state;
 		private int hour;
 		private int min;
 		
 		public ProgramSwitch(String type, String state, int hour, int min) {
 			this.type = type;
 			this.state = state;
 			this.hour = hour;
 			this.min = min;
 		}
 		
 		public String getType() {
 			return type;
 		}
 		
 		public void setType(String type) {
 			this.type = type;
 		}
 		
 		public String getState() {
 			return state;
 		}
 		
 		public void setState(String state) {
 			this.state = state;
 		}
 		
 		public int getHour() {
 			return hour;
 		}
 		
 		public void setHour(int hour) {
 			this.hour = hour;
 		}
 		
 		public int getMin() {
 			return min;
 		}
 		
 		public void setMin(int min) {
 			this.min = min;
 		}
 		
 		public String toXML() {
 			String time = String.format("%02d:%02d",this.hour, this.min);
 			return "<switch type=\"" + this.type + "\" state=\"" + this.state + "\">" 
 					+ time + "</switch>\n";
 		}
 	}
 	
 	//keep in mind that dayXMLLines[0] == day tag!!
 	public DayProgram(String dayName, String[] dayXMLLines) {
 		this.name = dayName;
 		
 		switches   = new ProgramSwitch[10];
 		
 		parseXMLLines(dayXMLLines);
 	}
 	
 	public ProgramSwitch[] getSwitches(){
 		return switches;
 	}
 	public void setSwitches(ProgramSwitch[] switches){
 		this.switches = switches;
 	}
 
 	//returns switch occuring in hour
 	public ProgramSwitch getSwitchByHour(int hour) {
 		for(ProgramSwitch ps : switches) {
 			if(ps.getHour() == hour) {
 				return ps;
 			} else if(ps.getHour() > hour) {
 				//already past a transition
 				return null;
 			}
 		}
 		return null;
 	}
 	
 	private void parseXMLLines(String[] dayXMLLines) {
 		for(int i=1; i < dayXMLLines.length - 1 ;i++) { //last element of array seems to be empty so omitting that
 			String line = dayXMLLines[i];
 			String curType = "night";
 			String curState = "on";
 			int hour = 0;
 			int min = 0;
 			
 			int parInd = line.indexOf('"');
 			
 			char typeCh = line.charAt(parInd + 1); //find the " in "type="" and then take char after it
 			if(typeCh == 'd') {
 				curType = "day";
 			} 
 			
 			char stateCh = line.charAt(line.indexOf('"', parInd + 1) + 2); // if state="on" then this = 'n' in "on"
 			if(stateCh == 'f') { //state="off"
 				curState = "off";
 			}
 			
 			parInd = line.indexOf('>');
 			hour = Integer.parseInt(line.substring(parInd+1,parInd+3));
 			min = Integer.parseInt(line.substring(parInd+4,parInd+6));
 		
 			ProgramSwitch ps = new ProgramSwitch(curType, curState, hour, min);
 			switches[i-1] = ps;
 		}
 	}
 
 	public String toXML() {
 		String ret = "<day name=\"" + this.name + "\">\n";
 		for(ProgramSwitch ps : switches) {
 			ret += ps.toXML();
 		}
 		ret += "</day>\n";
 		return ret;
 	}
 	@Override
 	public void submitResult(String function, String method, String contents) {
 		// TODO Auto-generated method stub
 		//actually not even sure if it works
 	}
 
 	public String getTypeAtTime(int hour, int min) {
 		for(ProgramSwitch ps : switches ) {
			if(ps.getHour() == hour && ps.getMin() <= min) {
				return ps.getType();
 			}
 		}
 		
		return "night";
 	}
 
 }
