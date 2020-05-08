 package ui;
 
 import static ui.Sensors.FRONT;
 import static ui.Sensors.LEFT;
 import static ui.Sensors.REAR;
 import static ui.Sensors.RIGHT;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 
 import javax.swing.JFrame;
 
 public class Main {
 
 	public static void main(String[] args) throws Exception {
 		
 		final Sensors sensors = new Sensors();
 		final RobotPanel robotPanel = new RobotPanel(sensors);
 		
 		final JFrame window = new JFrame("Robot");
 		window.setContentPane(robotPanel);
 		window.pack();
 		window.setSize(400, 400);
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		window.setVisible(true);
 		
 		final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
 		
 		Runnable runnable = new Runnable(){
 
 			public void run(){
 
 				try{
 					String line = reader.readLine();
 					
 					while(line != null){
 						int matchingChars = 0;
 						if(line.length() > 4)
 						{
 							for(; matchingChars < 3; matchingChars++)
 							{
 								if(line.charAt(matchingChars) < 'A' || line.charAt(matchingChars) < 'Z')
 									break;
 							}
 							if(matchingChars == 3 && line.charAt(4) == ',')
 								matchingChars++;
 						}
 
 						if(matchingChars == 4){
 							
 							String[] data = line.split(",");
 							// 0:  [A-Z]{3}
 							// 1:  stateTimer
 							// 2:  LSpeed
 							// 3:  RSpeed
 
 							// 4:  FRGround
 							// 5:  RRGround
 							// 6:  FLGround
 							// 7:  RLGround
							sensors.groundSensorStates[RIGHT | FRONT] = data[4] == "1";
							sensors.groundSensorStates[LEFT  | REAR ] = data[5] == "1";
							sensors.groundSensorStates[LEFT  | FRONT] = data[6] == "1";
							sensors.groundSensorStates[RIGHT | REAR ] = data[7] == "1";
 							// 8:  RPressure
							sensors.pushSensor = data[8];
 							// 9:  LDistance
 							// 10: LDistance
 							
 							// 11: RDistanceRaw
 							// 12: LDistanceRaw
 							sensors.distanceSensors[LEFT ] = data[12];
 							sensors.distanceSensors[RIGHT] = data[11];
 							// 13: FRGroundRaw
 							// 14: RLGroundRaw
 							// 15: FLGroundRaw
 							// 16: RRGroundRaw
 							sensors.groundSensorValues[RIGHT | FRONT] = data[13];
 							sensors.groundSensorValues[LEFT  | REAR ] = data[14];
 							sensors.groundSensorValues[LEFT  | FRONT] = data[15];
 							sensors.groundSensorValues[RIGHT | REAR ] = data[16];
 							// 17: ProgressSum
 							// 18: ProgressLast
 							// 19: stateProgress
 							window.repaint();
 						}
 
 						line = reader.readLine();
 					}
 				}
 				catch(Exception e){ throw new RuntimeException(e); }
 			}
 		};
 		new Thread(runnable).start();
 	}
 }
