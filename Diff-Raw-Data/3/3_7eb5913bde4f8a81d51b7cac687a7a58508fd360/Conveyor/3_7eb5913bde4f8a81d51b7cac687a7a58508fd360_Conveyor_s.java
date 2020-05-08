 package engine.agent.Dongyoung;
 
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import shared.Glass;
 import transducer.*;
 
 public class Conveyor extends Component implements TReceiver{
 
 	// DATA
 	protected CopyOnWriteArrayList<Glass> glasses = new CopyOnWriteArrayList<Glass>();
 	private Integer[] conveyorNum = new Integer[1];
 	private int frontSensorNum, backSensorNum, sensorNum;
 	protected boolean glassLeaveFront = false;
 	
 	/* Constructor */
 	protected Conveyor(String name, int num, int frontSensorNum, int backSensorNum) {
 		super(name);
 		conveyorNum[0] = num;
 		this.frontSensorNum = frontSensorNum;
 		this.backSensorNum = backSensorNum;
 	}
 	
 	// MESSAGE - Directly from Transducer. Refer to function 'eventFired'
 	
 	// SCHEDULER
 	@Override
 	protected boolean pickAndExecuteAnAction(){
 		if( newGlass ){
 			newGlassAction();
 			return true;
 		}
 		
 		if( glassLeaveFront ){
 			glassLeaveFrontAction();
 			return true;
 		}
 		
 		if( checkPass ){
 			checkPassAction();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	// ACTION
 	private void checkPassAction(){
 		transducer.fireEvent( TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conveyorNum );
 		if( nextCompFree ){
 			nextCompFree = false;
 			passGlassAction();
 			transducer.fireEvent( TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conveyorNum );
 			checkDone = true;
 		}
 		checkPass = false;
 	}
 	
 	private void newGlassAction(){
 		transducer.fireEvent( TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conveyorNum );
 		glasses.add( tempGlass );
 		tempGlass = null;
 		newGlass = false;
 		conveyorCheck();
 	}
 	
 	private void glassLeaveFrontAction(){
 		glassLeaveFront = false;
 		notifyIAmFreeAction();
 	}
 	
 	// EXTRA
 	/* From Transducer */
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		
 		sensorNum = (Integer)args[0];
 		if( channel == TChannel.SENSOR ){
 			if( event == TEvent.SENSOR_GUI_PRESSED ){
 				if( sensorNum == frontSensorNum ){
 					if( debug ){
 						System.out.println( "channel : " + channel.toString() + ", event : " + event.toString() + ", num : " + sensorNum );
 					}
 					newGlass = true;
 				}
 				else if( sensorNum == backSensorNum ){
 					if( debug ){
 						System.out.println( "channel : " + channel.toString() + ", event : " + event.toString() + ", num : " + sensorNum );
 					}
 					checkPass = true;
 				}
 				stateChanged();
 			}
 			else if( event == TEvent.SENSOR_GUI_RELEASED ){
 				if( sensorNum == frontSensorNum ){
 					if( debug ){
 						System.out.println( "channel : " + channel.toString() + ", event : " + event.toString() + ", num : " + sensorNum );
 					}
 					glassLeaveFront = true;
 					stateChanged();
 				}
 				else if( sensorNum == backSensorNum ){
 					if( debug ){
 						System.out.println( "channel : " + channel.toString() + ", event : " + event.toString() + ", num : " + sensorNum );
 					}
 					// Nothing
 				}
 			}
 		}	
 	}
 	
 	private void conveyorCheck(){
 		// Glass on Front Sensor or on Conveyor, but no Glass on Back Sensor
 		if( ( newGlass || !glasses.isEmpty() ) && !checkPass ){
 			transducer.fireEvent( TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conveyorNum );
 		}
 		else{
 			transducer.fireEvent( TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conveyorNum );
 		}
 	}
 	
 	/* Setter */
 	public void setTransducer(Transducer transducer){
 		this.transducer = transducer;
 		transducer.register(this, TChannel.SENSOR);
 	}
 	
 	protected void passGlassAction(){}
 	protected void notifyIAmFreeAction(){}
 }
