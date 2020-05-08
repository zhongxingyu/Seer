 /**
  * 
  */
 package fishlamp.application.rs232;
 
 import fishlamp.application.protocol.IFrameParser;
 
 /**
  * @author balazs.grill
  *
  */
 public class TimeFrameParser implements IFrameParser<TimeFrame> {
 
 	private final TimeFrame frame = new TimeFrame();
 	private int state = 0;
 	
 	@Override
 	public void newByte(short d) {
 		switch(state){
 		case 0:
 			state++;
			frame.hour = d;
 			break;
 		case 1:
 			state++;
			frame.min = d;
 			break;
 		case 2:
 			state++;
			frame.sec = d;
 			break;
 		}
 		
 	}
 
 	@Override
 	public boolean done() {
		return state == 3;
 	}
 
 	@Override
 	public TimeFrame getFrame() {
 		return frame;
 	}
 
 }
