 import java.io.DataInputStream;
 import java.io.IOException;
 
 // Lejos imports
 import lejos.nxt.*;
 import lejos.nxt.comm.*;
 
 // Collect commands, write to screen
 public class MainCtrl {
 
         //Defines the execution mode variable used for turning relivant test functions on/off
         private static int execMode = 0;
 
         //Deifines a boolean to determine wheather the movement motors are currently running
         private static boolean moving = false;
 
         //Defines the variables used for determining the position of each wheel
         private static int steeringangle_left = 0;
 	private static int steeringangle_right = 0;
     
         //Defines the motors used for steering the right and left wheels
 	private static final Motor motor_left = Motor.A;
 	private static final Motor motor_right = Motor.B;
 	
         //Defines the motor used for the kicker
 	private static final Motor motor_kick = Motor.C;
 	
 	//Defines the number of motor turns to wheel turns
 	private static final double rotConstant = 2.375;
 
         //Defines the sensor port used to power the communication light
         private static final SensorPort port_comlight = SensorPort.S1;
 
         //Defines the buttons
         private static final Button button_left = Button.LEFT;
         private static final Button button_right = Button.RIGHT;
         private static final Button button_enter = Button.ENTER;
 
         //Defines the variable used to prevent the motors starting whilst turning
         private static  boolean inturn = false;
 
         // Defines variables used for the managing bluetooth connection
 	private static BTConnection connection;
 	private static DataInputStream inputStream;
 
 	public static void main(String[] args) throws InterruptedException{
 	    motor_left.setSpeed(900);
 	    motor_right.setSpeed(900);
 
 	    executionMenu();
 	}
 	
         //Aims to establish a conection over Bluetooth
         public static void connect(){
 	        writeToScreen("Trying to connect", 7);
 		// Wait until connected
 		connection = Bluetooth.waitForConnection();
 		writeToScreen("Connected", 7); 
 		inputStream = connection.openDataInputStream();
 		writeToScreen("Connection Opened", 7); 
 	}
 
         //Handles collecting the messages from the server over Bluetooth
 	private static void collectMessage() throws InterruptedException{
 	    boolean atend = false;
 	    long numoftwos = 0;
 	    long prevval = 0;
 	    int messageno = 0;
 	    while(atend == false){
 			try {
 				// Parse if there are any messages
 			    long inlen = inputStream.available();
 			    if((inlen>=4) && (inlen%4 == 0)){
 				writeToScreen("Got message no:"+Integer.toString(messageno),7);
 					if (inlen > 4){
 					    inputStream.skip( ((int) (inlen / 4)) * 4);
 					}
 					int message = inputStream.readInt();
 					// Do specific action
 					if (message == 10000){
 					    atend = true;
 					    writeToScreen(Integer.toString(message),7);
 					} else {
 					    writeToScreen(Integer.toString(message),6);
 					    parseMessage(message);
 					}
 					inputStream.close();
 					inputStream = connection.openDataInputStream();
 			    } else {
 				writeToScreen("inlen = "+Long.toString(inlen),7);
 				
 				if (prevval == inlen){
 				    numoftwos++;
 				} else {
 				    numoftwos = 0;
 				}
 				if (numoftwos > 30){
 				    inputStream.close();
 				    inputStream = connection.openDataInputStream();
 				}
 				prevval = inlen;
 				//try{
 				//    Thread.sleep(200);
 				//}catch (InterruptedException e){
 				//}
 			    }	
 			} catch (IOException e) {
 			    writeToScreen("Error",7);
 			    atend = true;
 			}
 		}
 	    writeToScreen("Exit While",7);
 	}
 
     //Parses integer messages
     public static void parseMessage(int message){
 	switch(message){
 	case 100:
 	    reset();
 	    break;
 	case 101:
 	    drive();
 	    break;
 	case 102:
 	    stop();
 	    break;
 	case 103:
 	    startSpinRight();
 	    break;
 	case 104:
 	    startSpinLeft();
 	    break;
 	case 105:
 	    stopSpin();
 	    break;
 	case 466:
 	    kick();
 	    break;
 	case 467:
 	    spinRightShort();
 	    break;
 	case 468:
 	    spinLeftShort();
 	    break;
 	default:
 	     if ((message >= 106)&&(message <=465)){
 		 setRobotDirection(message - 106);
 	     } else if ((message >= 469)&&(message <= 828)){
 		 turnLeftWheelByAmount(message - 469);
 	     } else if ((message >= 829)&(message <= 1188)){
 		 turnRightWheelByAmount(message - 829);
 	     } else if ((message >= 1189)&&(message <= 1548)){
 		 turnLeftWheelTo(message - 1189);
 	     } else if ((message >= 1549)&&(message <= 1908)){
		 turnRightWheelTo(message - 1908);
 	     }
 	}
     }
 
 	// Writes a message to the brick's screen on a particular line if valid
         public static void writeToScreen(String message, int line){	
 	    if ((line >= 0)&&(line <=7)){
 	        LCD.drawString("                ", 0, line);
 		LCD.drawString(message, 0, line);
 		LCD.refresh();
 	    }
 	}
 
         // Defines the function to provide the menu for choosing execution mode of the program
         public static void executionMenu(){
 	   int selectedchoice = 0;
 	   int numchoices = 3;
 	   boolean enterselected = false;
 	   boolean haschanged = false;
 	   
 	   writeToScreen("Select Execution Mode",0);
 
 	   switch (selectedchoice){
 	   case 0:
 	       writeToScreen("1. Standard Exc.", 1);
        	       break;
        	   case 1:
 	       writeToScreen("2. Test +BT", 1);
        	       break;
 	   case 2:
        	       writeToScreen("3. Test -BT", 1);
        	       break;
 	   }
 	   
 	   while (enterselected == false){
 	       //enumerates the list item when the right button is pressed
 	       if (button_right.isPressed()){
 		   if(selectedchoice < (numchoices -1)){
 		       ++selectedchoice;
 		   } else {
 		       selectedchoice = 0;
 		   }
 		   haschanged = true;
 	       }
 	       
 	       //denumerates the list item when the left button is pressed
 	       if (button_left.isPressed()){
 		   if(selectedchoice > 0){
 		       --selectedchoice;
 		   } else {
 		       selectedchoice = (numchoices - 1);
 		   }
 		   haschanged = true;
 	       }
 
 	       //deals with the enter key being pressed
 	       if (button_enter.isPressed()){
 		   enterselected = true;
 	       }
 
 	       //if the menu item has been changed this updates the screen
 	       if (haschanged == true){
 		   switch (selectedchoice){
 		   case 0:
 		       writeToScreen("1. Standard Exc.", 1);
 		       break;
 		   case 1:
 		       writeToScreen("2. Test +BT", 1);
 		       break;
 		   case 2:
 		       writeToScreen("3. Test -BT", 1);
 		       break;
 		   }
 	       }
 	       
 	       haschanged = false;
 	   }
 
 	   //executes the relevant routines based on selection
 	   switch (selectedchoice){
 	   case 0:
 	       writeToScreen("1. Standard Exc.", 0);
 	       writeToScreen("",1);
 	       execMode = 0;
 	       executeStandard();
 	       break;
 	   case 1:
 	       writeToScreen("2. Test +BT", 0);
 	       writeToScreen("",1);
 	       execMode = 1;
 	       executeTestPlusBT();
 	       break;
 	   case 2:
 	       writeToScreen("3. Test -BT", 1);
 	       writeToScreen("",1);
 	       execMode = 2;
 	       executeTestMinBT();
 	       break;
 	   }
        }
 	
         // Standard execution path
         public static void executeStandard(){
 	    connect();
 	    motor_right.resetTachoCount();
 	    motor_left.resetTachoCount();
 
 	    motor_right.regulateSpeed(true);
 	    motor_left.regulateSpeed(true);
 
 	    motor_right.smoothAcceleration(true);
 	    motor_left.smoothAcceleration(true);
 	    try{
 		collectMessage();
 	    } catch (InterruptedException e){
 		writeToScreen("Msg Col Interupt",7);
 	    }
         }
 
        // Test execution with Bluetooth
        public static void executeTestPlusBT(){
 	   
        }
 
        // Test execution without Bluetooth
        public static void executeTestMinBT(){
 	   //Tests the drive and stop commands
 	     
 	     //Drive forward for ten seconds then stop
 	     writeToScreen("Drive Test 1.",1);
 	     writeToScreen("Fwd10,Stp",2);
 	     button_enter.waitForPress();
 	     drive();
 	     try {
 		 Thread.sleep(10000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     writeToScreen("Done!",2);
 	     button_enter.waitForPress();
 
 	     //Drive forward 5sec, stop 2sec, forward 5sec, stop
 	     writeToScreen("Drive Test 2.",1);
 	     writeToScreen("Fwd5,Stp,Fwd5,Stp",2);
 	     button_enter.waitForPress();
 	     drive();
 	     try{
 		 Thread.sleep(5000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     try{
 		 Thread.sleep(2000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     drive();
 	     try {
 		 Thread.sleep(2000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     writeToScreen("Done!",2);
 	     button_enter.waitForPress();
 
 	     //Drive forwards 2sec, drive forwards 2sec, stop
 	     writeToScreen("Drive Test 3.",1);
 	     writeToScreen("Fwd2,Fwd2,Stp",2);
 	     button_enter.waitForPress();
 	     drive();
 	     try {
 		 Thread.sleep(2000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     drive();
 	     try{
 		 Thread.sleep(2000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     writeToScreen("Done!",2);
 	     button_enter.waitForPress();
 
 	     //Drive forwards 2sec, stop, stop
 	     writeToScreen("Drive Test 4.",1);
 	     writeToScreen("Fwd2,Stp,Stp",2);
 	     button_enter.waitForPress();
 	     drive();
 	     try{
 		 Thread.sleep(2000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     stop();
 	     writeToScreen("Done!",2);
 	     button_enter.waitForPress();
 
 	  //Tests startSpin and stopSpin
 
 	     //startSpin, drive 5s, stop, stopSpin
 	     writeToScreen("Spin Test 1.", 1);
 	     writeToScreen("StasR,Fwd5,Stp,Stps",2); 
 	     button_enter.waitForPress();
 	     startSpinRight();
 	     drive();
 	     try{
 		 Thread.sleep(5000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     stopSpin();
 	     writeToScreen("Done!",2);
 	     button_enter.waitForPress();
 
 	     //startSpin, drive 5s, stop, stopSpin
 	     writeToScreen("Spin Test 2.", 1);
 	     writeToScreen("StasL,Fwd5,Stp,Stps",2); 
 	     button_enter.waitForPress();
 	     startSpinLeft();
 	     drive();
 	     try{
 		 Thread.sleep(5000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     stopSpin();
 	     writeToScreen("Done!",2);
 	     button_enter.waitForPress();
 
 
 	     //startSpin, stopSpin
 	     writeToScreen("Spin Test 3.",1);
 	     writeToScreen("StasR,Stps",2);
 	     button_enter.waitForPress();
 	     startSpinRight();
 	     try{
 		 Thread.sleep(1000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stopSpin();
 	     writeToScreen("Done!",2);
 	     button_enter.waitForPress();
 
 	     //startSpin, stopSpin
 	     writeToScreen("Spin Test 4.",1);
 	     writeToScreen("StasL,Stps",2);
 	     button_enter.waitForPress();
 	     startSpinLeft();
 	     try{
 		 Thread.sleep(1000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stopSpin();
 	     writeToScreen("Done!",2);
 	     button_enter.waitForPress();
 
 	     //spinLeftShort
 	     writeToScreen("Spin Test 5.",1);
 	     writeToScreen("spinLeftShort",2);
 	     button_enter.waitForPress();
 	     spinLeftShort();
 	     writeToScreen("Done!",2);
 	     button_enter.waitForPress();
 
 	     //spinRightShort
 	     writeToScreen("Spin Test 5.",1);
 	     writeToScreen("spinRightShort",2);
 	     button_enter.waitForPress();
 	     spinRightShort();
 	     writeToScreen("Done!",2);
 	     button_enter.waitForPress();
 	     
 	  //Tests setRobotDirection
 
 	     //The Square test: setRobotDirection 90Deg, forward 3s, stop, setRobotDirection 180Deg,forward 3s, stop, setRobotDirection 270Deg, forward 3s, stop, setRobotDirection 0Deg, forward3s, stop  
 
 	     writeToScreen("SRDir Test 1.",1);
 	     writeToScreen("The 3s Sqr Tst",2);
 	     button_enter.waitForPress();
 	     setRobotDirection(90);
 	     drive();
 	     try{
 		 Thread.sleep(3000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     setRobotDirection(180);
 	     drive();
 	     try{
 		 Thread.sleep(3000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     setRobotDirection(270);
 	     drive();
 	     try{
 		 Thread.sleep(3000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     setRobotDirection(0);
 	     drive();
 	     try{
 		 Thread.sleep(3000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     writeToScreen("Done!",2);
 	     button_enter.waitForPress();
 
 	     //The Diamond Test: setRobotDirection 315Deg, forward 3sec, stop, setRobotDirection 45Deg, forward 3sec, stop, setRobotDirection 135Deg, forward 3sec, stop, setRobotDirection 225, forward 3, stop, reset
 	     writeToScreen("SDir Test 2.",1);
 	     writeToScreen("The Diamd Tst",2);
 	     button_enter.waitForPress();
 	     setRobotDirection(315);
 	     drive();
 	     try{
 		 Thread.sleep(3000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     setRobotDirection(45);
 	     drive();
 	     try{
 		 Thread.sleep(3000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     setRobotDirection(135);
 	     drive();
 	     try{
 		 Thread.sleep(3000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     setRobotDirection(225);
 	     drive();
 	     try{
 		 Thread.sleep(3000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     reset();
 	     writeToScreen("Done",2);
 	     button_enter.waitForPress();
 
 	   //Tests the Kicker
 	     //Standard kick
 	     writeToScreen("Kick Test 1.",1);
 	     writeToScreen("Std Kick",2);
 	     button_enter.waitForPress();
 	     kick();
 	     writeToScreen("Done!",2);
 	     button_enter.waitForPress();
 
 	     //Moving Kick: reset(), forward 2s + kick()
 	     writeToScreen("Kick Test 2.",1);
 	     writeToScreen("Mving Kick",2);
 	     button_enter.waitForPress();
 	     reset();
 	     drive();
 	     kick();
 	     try{
 		 Thread.sleep(2000);
 	     } catch (InterruptedException e){
 		 writeToScreen("Interrupted!",7);
 	     }
 	     stop();
 	     writeToScreen("Done!",2);
 	     button_enter.waitForPress();
 	     
        }
 
 	// Activate kicker
 	public static void kick(){
 	        writeToScreen("Kick", 7);
 		motor_kick.setSpeed(900);
 		motor_kick.rotate(720);
 	}
 
        //Sets the robot's direction to the input direction in degrees
 	public static void setRobotDirection(int DirectionDEGs){
 	       inturn = true;
 	       boolean hasmoved = false;
 
 	        //Halts the movement motors if they are already running
 	        if (moving == true){
 	            stop();
 		    hasmoved = true;
 	        }
 
 	       // For the left motor
 		if (DirectionDEGs > steeringangle_left){
 			motor_left.rotate((int)(rotConstant * (DirectionDEGs - steeringangle_left)),false);
 			steeringangle_left = DirectionDEGs;
 		} else if (DirectionDEGs < steeringangle_left){
 			motor_left.rotate((int)(-1*(steeringangle_left - DirectionDEGs)*rotConstant),false);
 			steeringangle_left = DirectionDEGs;
 		}
 		
 		// For the right motor
 		if (DirectionDEGs > steeringangle_right){
 			motor_right.rotate((int)(rotConstant * (DirectionDEGs - steeringangle_right)),false);
 			steeringangle_right = DirectionDEGs;
 		} else if (DirectionDEGs < steeringangle_right){
 			motor_right.rotate((int)(-1*(steeringangle_right - DirectionDEGs)*rotConstant),false);
 			steeringangle_right = DirectionDEGs;
 		}
 
 		//Restarts the movement motors if the robot was moving prior to executing the command
 		if (hasmoved == true){
 		    drive();
 		}
 		inturn = false;
 	}
 
     //Defines the function to set the robot to spin around it's own centre in the right direction
     public static void startSpinRight(){
 		
 	        boolean hasmoved = false;
 
 		inturn = true;
 
 	        //Halts the movement motors if they are already running
 	        if (moving == true){
 	            stop();
 		    hasmoved = true;
 	        }
 
 		//For the left (rotate wheels to 135Deg)
 		if ((steeringangle_left % 360) > 315){
 			motor_left.rotate((int) (rotConstant * (135 + (360 - (steeringangle_left % 360)))));
 		} else if((steeringangle_left % 360) < 135){
 			motor_left.rotate((int) (rotConstant * (135 - (steeringangle_left % 360))));
 		} else if ((steeringangle_left % 360) >= 135 && ((steeringangle_left % 360) <= 315)) {
 			motor_left.rotate((int) (rotConstant * -1 * ((steeringangle_left %360) - 135)));
 		}
 		
 		steeringangle_left = 135;
 		
 		//For the right (rotate wheels to 315Deg)
 		if ((steeringangle_right % 360) > 315){
 			motor_right.rotate((int) (rotConstant * -1 *((steeringangle_right % 360)-315)));
 		} else if((steeringangle_right % 360) < 135){
 			motor_right.rotate((int) (rotConstant * -1 *(45 +( steeringangle_right % 360))));
 		} else if ((steeringangle_right % 360) >= 135 && ((steeringangle_right % 360) <= 315)) {
 		    motor_right.rotate((int) (rotConstant * (315 - (steeringangle_right % 360))));
 		}
 		
 		steeringangle_right = 315;
 
 		//Restarts the movement motors if the robot was moving prior to executing the command
 		if (hasmoved == true){
 		    drive();
 		}
 
 		inturn = false;
 	}
 
     //Defines the function to set the robot to spin around it's own centre in the right direction
     public static void startSpinLeft(){
 		
 	        boolean hasmoved = false;
 		inturn = true;
 
 	        //Halts the movement motors if they are already running
 	        if (moving == true){
 	            stop();
 		    hasmoved = true;
 	        }
 
 		//For the left (rotate wheels to 315Deg)
 		if ((steeringangle_left % 360) > 315){
 			motor_left.rotate((int) (rotConstant * -1 *((steeringangle_left % 360)-315)));
 		} else if((steeringangle_left % 360) < 135){
 			motor_left.rotate((int) (rotConstant * -1 *(45 +( steeringangle_left % 360))));
 		} else if ((steeringangle_left % 360) >= 135 && ((steeringangle_left % 360) <= 315)) {
 		    motor_left.rotate((int) (rotConstant * (315 - (steeringangle_left % 360))));
 		}
 		
 		steeringangle_left = 315;
 
      		//For the right (rotate wheels to 135Deg)
 		if ((steeringangle_right % 360) > 315){
 			motor_right.rotate((int) (rotConstant * (135 + (360 - (steeringangle_right % 360)))));
 		} else if((steeringangle_right % 360) < 135){
 			motor_right.rotate((int) (rotConstant * (135 - (steeringangle_right % 360))));
 		} else if ((steeringangle_right % 360) >= 135 && ((steeringangle_right % 360) <= 315)) {
 			motor_right.rotate((int) (rotConstant * -1 * ((steeringangle_right %360) - 135)));
 		}
 		
 		steeringangle_right = 135;
 
 		//Restarts the movement motors if the robot was moving prior to executing the command
 		if (hasmoved == true){
 		    drive();
 		}
 
 		inturn = false;
 	}
 
     //Defines the function used to stop the robot spinning round it's own centre
     public static void stopSpin(){
 	
 	 boolean hasmoved = false;
 	 inturn = true;
 
 	 //Halts the movement motors if they are already running
          if (moving == true){
             stop();
        	    hasmoved = true;
          }
 	
 	//Puts the wheels back to 0Deg
 	reset();
 
 	//Restarts the movement motors if the robot was moving prior to executing the command
 	if (hasmoved == true){
        	    drive();
 	}
 	inturn = false;
     }
 
     //Communicates with the light sensor on the RCX to start the drive motors
     public static void drive(){
 	if((inturn == false) && (motor_left.isMoving() == false) && (motor_right.isMoving()== false)){
 	    port_comlight.setPowerType(port_comlight.POWER_RCX9V);
 	    port_comlight.activate();
 	    moving = true;
 	}
     }
 
     //Communicates with the light sensor on the RCX to stop the drive motors
     public static void stop(){
 	port_comlight.passivate();
 	moving = false;
     }
 
     //Resets both wheels to 0 deg
     public static void reset(){
 
 	inturn = true;
 
 	//rotates the left wheels back to 0 deg
 	if ((steeringangle_left % 360) > 180){
 	    motor_left.rotate((int)(rotConstant * (180 - ((steeringangle_left % 360) - 180))));
 	} else if ((steeringangle_left % 360) <= 180) {
 	    motor_left.rotate((int)(rotConstant * -1 * (steeringangle_left % 360)));
 	}
 
 	steeringangle_left = 0;
 
 	//rotates the right wheels back to 0 deg
 	if ((steeringangle_right % 360) > 180){
 	    motor_right.rotate((int)(rotConstant * (180 - ((steeringangle_right % 360) - 180))));
 	} else if ((steeringangle_right % 360) <= 180){
 	    motor_right.rotate((int)(rotConstant * -1 * (steeringangle_right % 360)));
 	}
 
 	steeringangle_right = 0;
 
 	inturn = false;
     }
 
     //Makes the robot make a slight spin right
     public static void spinRightShort(){
 	startSpinRight();
 	drive();
 	try{
 	    Thread.sleep(600);
 	} catch (InterruptedException e){
 	    writeToScreen("Msg Col Interupt",7);
 	}
 	stop();
 	stopSpin();
     }
 
     //Makes the robot make a slight spin left
     public static void spinLeftShort(){
 	startSpinLeft();
 	drive();
 	try{
 	    Thread.sleep(600);
 	} catch (InterruptedException e){
 	    writeToScreen("Msg Col Interupt",7);
 	}
 	stop();
 	stopSpin();
     }
 
     //Turns the left wheel by a specified ammount
     public static void turnLeftWheelByAmount(int TurnDegs){
 	motor_left.rotate((int) (rotConstant * TurnDegs));
 	steeringangle_left = ((steeringangle_left + TurnDegs) % 360);
     }
 
     //Turns the right wheel by a specified amount
     public static void turnRightWheelByAmount(int TurnDegs){
 	motor_right.rotate((int) (rotConstant * TurnDegs));
 	steeringangle_right = ((steeringangle_right + TurnDegs) % 360);
     }
 
     //Turns the left wheel to a specified angle
     public static void turnLeftWheelTo(int TurnDegs){
 	if ((steeringangle_left % 360) < 180){
 	    if (TurnDegs < 180){
 		motor_left.rotate((int)(rotConstant * (TurnDegs - steeringangle_left)));
 	    } else if (TurnDegs >= 180){
 		if ((TurnDegs - (steeringangle_left % 360)) < 180){
 		    motor_left.rotate((int) (rotConstant * (TurnDegs - steeringangle_left)));
 		} else if(TurnDegs - (steeringangle_left % 360)) >= 180 (){
 			motor_left.rotate((int) ( rotConstant * -1 *((360 - (TurnDegs % 360)) + steeringangle_left)));
 		}
 	    }
 	} else if ((steeringangle_left % 360) >= 180){
 	    if ((TurnDegs % 360) >= 180){
 		motor_left.rotate((int)(rotConstant * ((TurnDegs % 360) - steeringangle_left)));
 	    }else if (TurnDegs < 180){
 		if(((steeringangle_left % 360) - (TurnDegs % 360)) < 180){
 		    motor_left.rotate((int)(rotConstant * ((TurnDegs % 360) - (steeringangle_left % 360))));
 		} else if(((steeringangle_left % 360) - (TurnDegs % 360)) >= 180){
 		    motor_left.rotate((int)(rotConstant * ((360 - (steeringangle_left % 360))+ TurnDegs)));
 		}
 	    }
 	}
 
 	steeringangle_left = (TurnDegs % 360);
     }
 
     //Turns the right wheel to a specified angle
     public static void turnRightWheelTo(int TurnDegs){
 	if ((steeringangle_right % 360) < 180){
 	    if (TurnDegs < 180){
 		motor_right.rotate((int)(rotConstant * (TurnDegs - steeringangle_right)));
 	    } else if (TurnDegs >= 180){
 		if ((TurnDegs - (steeringangle_right % 360)) < 180){
 		    motor_right.rotate((int) (rotConstant * (TurnDegs - steeringangle_right)));
 		} else if(TurnDegs - (steeringangle_right % 360)) >= 180 (){
 			motor_right.rotate((int) ( rotConstant * -1 *((360 - (TurnDegs % 360)) + steeringangle_right)));
 		}
 	    }
 	} else if ((steeringangle_right % 360) >= 180){
 	    if ((TurnDegs % 360) >= 180){
 		motor_right.rotate((int)(rotConstant * ((TurnDegs % 360) - steeringangle_right)));
 	    }else if (TurnDegs < 180){
 		if(((steeringangle_right % 360) - (TurnDegs % 360)) < 180){
 		    motor_right.rotate((int)(rotConstant * ((TurnDegs % 360) - (steeringangle_right % 360))));
 		} else if(((steeringangle_right % 360) - (TurnDegs % 360)) >= 180){
 		    motor_right.rotate((int)(rotConstant * ((360 - (steeringangle_right % 360))+ TurnDegs)));
 		}
 	    }
 	}
 	
 	steeringangle_right = (TurnDegs % 360);
     }
 }
 
