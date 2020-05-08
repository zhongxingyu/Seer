 package my;
 
 public class Protocol {
 	private static final int WAITING = States.WAITING;
 	private static final int SENTauthANSWER = States.SENTauthANSWER;
 	private static final int GAMEOVER = States.GAMEOVER;
 	private static final int WAITforDATA = States.WAITforDATA;
 	private int state = SENTauthANSWER;
 
 	Parser parser;
 	Serializer serializer;
 	public String oppName = null;
 
 	public Protocol(Parser p, Serializer s) {
 		this.parser = p;
 		this.serializer = s;
 	}
 
 	public String processOppName(String name) {
 		String outputLine = null;
 		if (state == WAITforDATA) {
 			oppName = name;
 			outputLine = serializer.serializeString("Start");
 			state = WAITING;
 		} 
 		return outputLine;
 	}	
 
 	public int getState() {
 		return state;
 	}
 
 	public String processGameOver() {
 		String outputLine = null;
 		state = GAMEOVER;
 		outputLine = serializer.serializeGameOver();
 		return outputLine;
 	}
 
 	public boolean processGOanswer(String answer) {
		if (answer.contains("y")){
 			state = WAITING;
 			return true;
 		}
 		state = SENTauthANSWER;
 		return false;
 	}	
 
 	public void setState(int s) {
 		this.state = s;		
 	}
 
 }
