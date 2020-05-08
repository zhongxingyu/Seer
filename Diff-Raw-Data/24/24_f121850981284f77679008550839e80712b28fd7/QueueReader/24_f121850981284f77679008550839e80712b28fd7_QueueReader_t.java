 package communication;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.servlet.ServletContextEvent;
 
 import datatypes.BLData_t;
 import datatypes.Data3D_t;
 import datatypes.NaviData_t;
 import datatypes.VersionInfo_t;
 import datatypes.Waypoint_t;
 import datatypes.s16;
 import datatypes.u16;
 import datatypes.u8;
 
 //TODO Add description for JAVADOC
 public class QueueReader extends SerialReader {
 	BlockingQueue<ArrayList> writeQueue = new LinkedBlockingQueue<ArrayList>();
 	BlockingQueue<ArrayList> readQueue = new LinkedBlockingQueue<ArrayList>();
 	
 	public QueueReader(ServletContextEvent event, String port, boolean testMode){
 		super();
 		OutputStream outStream;
 
 		ArrayList init = new ArrayList(); 
     	init.add("init");
 		readQueue.add(init);
 		
 		event.getServletContext().setAttribute("readQueue", readQueue);	//add readQueue to the context	
 		writeQueue = (BlockingQueue<ArrayList>) event.getServletContext().getAttribute("writeQueue");
 		
 		try {
 			if (testMode==true){
 				Timer timer;
 				timer = new Timer();
 				ByteArrayOutputStream debugOutStream = new ByteArrayOutputStream();
 				outStream = debugOutStream;
 				//send dummy data at regular interval (interval = 3s)
 			
 				
 				timer.scheduleAtFixedRate(new TimerTask(){
 					public void run(){
 						 ArrayList OSD = new ArrayList(); 
 			                /** CurrentPosition **/
 			                OSD.add("OSD");
			                Waypoint_t wp_target = new Waypoint_t("new target");                          
			                OSD.add((double)510481055/(double)10000000); // in 1E-7 deg
			                OSD.add((double) 36827906/(double) 10000000); // in 1E-7 deg
			                OSD.add((double)5038/(double)1000); // in mm
 			                OSD.add(0x01); // validity of data 
 			                /** TargetPosition **/
			                OSD.add((double)510481055/(double)10000000); // in 1E-7 deg
			                OSD.add((double)36827906/(double)10000000); // in 1E-7 deg
			                OSD.add((double)5038/(double)1000); // in mm
 			                OSD.add(0x01); // validity of data
 			                /** HomePosition **/
			                OSD.add((double)510481055/(double)10000000); // in 1E-7 deg
			                OSD.add((double)36827906/(double)10000000); // in 1E-7 deg
			                OSD.add((double)5038/(double)1000); // in mm
 			                OSD.add(0x01); // validity of data 
 			              
 			                OSD.add(0);  // index of current waypoints running from 0 to WaypointNumber-1
 			                OSD.add(3); // number of stored waypoints
 			                OSD.add(8);   // number of satellites used for position solution
 			                OSD.add(20);	 // hight according to air pressure
 			                OSD.add(10);	 // climb(+) and sink(-) rate
 			                OSD.add(10);	// FlyingTime in seconds
 			                OSD.add(3000);	// Battery Voltage in 0.1 Volts
 			                OSD.add(15); // speed over ground in cm/s (2D)
 			                OSD.add(90); // current flight direction in deg as angle to north
 			                OSD.add(90);  // current compass value in deg
 			                OSD.add(0);// current Nick angle in 1 deg
 			                OSD.add(0);  // current Rick angle in 1deg
 			                OSD.add(0);	// RC_Quality
 			               
 			                	                
 			                // ------- FCStatusFlags -------------------------------
 			                //FC_STATUS_MOTOR_RUN                     0x01
 			                //FC_STATUS_FLY                           0x02
 			                //FC_STATUS_CALIBRATE                     0x04
 			                //FC_STATUS_START                         0x08
 			                //FC_STATUS_EMERGENCY_LANDING             0x10
 			                //FC_STATUS_LOWBAT                        0x20
 			                //FC_STATUS_VARIO_TRIM_UP                 0x40
 			                //FC_STATUS_VARIO_TRIM_DOWN               0x80
 			                
 			                OSD.add(0); // Flags from FC
 			                
 			                // ------- NCFlags -------------------------------------
 			                // NC_FLAG_FREE                            0x01
 			                // NC_FLAG_PH                              0x02
 			                // NC_FLAG_CH                              0x04
 			                // NC_FLAG_RANGE_LIMIT                     0x08
 			                // NC_FLAG_NOSERIALLINK                    0x10
 			                // NC_FLAG_TARGET_REACHED                  0x20
 			                // NC_FLAG_MANUAL                          0x40
 			                // NC_FLAG_GPS_OK                          0x80
 			                
 			                OSD.add(0); // Flags from NC		                
 			                OSD.add(0); // 0 --> okay
 			                OSD.add(0); // current operation radius around the Home Position in m
 			                OSD.add(0);   // velocity in vertical direction in cm/s
 			                OSD.add(0); // time in s to stay at the given target, counts down to 0 if target has 
 			               
 			                // ------- FCStatusFlags2 ------------------------------
 			                //FC_STATUS2_CAREFREE_ACTIVE              0x01
 			                //FC_STATUS2_ALTITUDE_CONTROL_ACTIVE      0x02
 			                //FC_STATUS2_FAILSAFE_ACTIVE              0x04
 			                //FC_STATUS2_OUT1                         0x08
 			                //FC_STATUS2_OUT2                         0x10
 			                //FC_STATUS2_RES1                         0x20
 			                //FC_STATUS2_RES2                         0x40
 			                //FC_STATUS2_RES3                         0x80
 			                
 			                OSD.add(0); // StatusFlags2 (since version 5 added)
 			                OSD.add(0); // setpoint for altitude
 			                OSD.add(0); // actual current in 0.1A steps
 			                OSD.add(0);
 			                OSD.add(0); // used capacity in mAh
 			                OSD.add(0);
 			                readQueue.add(OSD);
 					}
 					
 				}, 3000, 3000);
 				
 				
 				
 			} else {
 				outStream = Listen(port);
 			}
 			
 			//start QueueWriter in a new Thread
 			Thread QueueWriter_t = new Thread(new QueueWriter(outStream, event));
 			QueueWriter_t.start();
 			
 		} catch (Exception e) {
 			System.out.println("Exception while trying to listen");
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 	 /** handleFrame method
      * @param dataFrame encoded dataFrame
      * 
      * main workhorse of the system: differentiate commands and act accordingly
      * **/
 	public void handleFrame(int[] dataFrame) {
 		//TODO implement adding to the queue for each element
 		String data = "New data has arrived";
 		//queue.add(data);
 		//context.getServletContext().setAttribute("serialPortData", queue);	
 		
 		//TODO Some of these values need to be remembered outside of frame handling (motorList, motorCount)
         u8 WPIndex;
         u8 numberOfWP;
         u8 parameterId;
         s16 parameterValue;
         String displayMessage;
         String label;
         
         BLData_t motorList[] = new BLData_t[5]; //6 motors (0->5)
         int motorCount = 0;
         
         /**
 		if (!NewDataRecieved){
 			System.out.println("NewDataRecieved (inside handleFrame): " + NewDataRecieved );
 			return;
 		}
 		
 			**/
 		
 		/** --Protocol-- (http://www.mikrokopter.de/ucwiki/en/SerialProtocol)
 	        * 	Start-Byte: 			'#'
 	        * 	Address Byte: 			'a'+ Addr
 	        * 	ID-Byte:				'V','D' etc'
 	        * 	n Data-Bytes coded: 	"modified-base64"
 	        * 	CRC-Byte1:				variable
 	        * 	CRC-Byte2:				variable
 	        * 	Stop-Byte:				'\r'
 	     */
 		
 		int[] decodedDataFrame = Decode64(dataFrame); //DECODE THE FRAME; 
 		
 		 /**  
          * REMARK!
          * 
          * FC address = 'a' + 1 -> ascii 'b' or decimal 98 
          * NC address = 'a' + 2 -> ascii 'c' or decimal 99
          * 
          **/
 		
 		//TODO Add handler for SystemTime
 		
 		decodedDataFrame[1] = decodedDataFrame[1] - 'a';  //removing 'a'
 		
 		switch (decodedDataFrame[1]){
 			case NC_ADDRESS:		//frame from Navi Ctrl
 				switch (decodedDataFrame[2]){
 					//NAVI-CTRL
 		            case 'Z':   // Serial link test
 		            	u16 echoPattern = new u16("echoPattern");
 		            	echoPattern.loadFromInt(decodedDataFrame, dataPointer);	 
 		            	
 		                System.out.println("<nZ>Serial Link Test");	                                             
 		                System.out.println("u16 (echoPattern): " + echoPattern.value);
 		            
 		                ArrayList serialLinkTest = new ArrayList();                 
 		                serialLinkTest.add("serialTest");	//set ID
 		                serialLinkTest.add(echoPattern.value);
 		                readQueue.add(serialLinkTest);	
 		                
 		                break;    
 		                            
 		            case 'E':   // Feedback from Error Text Request; Error Str from Navi
 		                System.out.println("<nE> Returns: Error Str from NaviCtrl");
 		                //TODO Test this
 		                /** Data is probably just an int[] of which every character has to be cast individually to a char
 		                 * 	Perhaps it is advised to make add a function. 
 		                 * **/
 		                //char errorMessage= (char) decodedDataFrame[dataPointer];
 		                String errorMessage = convertMessage(decodedDataFrame,dataPointer);    
 		                System.out.println(errorMessage);	
 		                
 		                ArrayList errorMsg = new ArrayList();                 
 		                errorMsg.add("errorMsg");	//set ID
 		                errorMsg.add(errorMessage);
 		                readQueue.add(errorMsg);
 		                break;
                   
 		            case 'W':   // Feedback from 'send WP' command
 		                System.out.println("<nW> Feedback from 'send WP' command - # Waypoint Index");
 		                //System.out.println("<W> Returns: # Waypoints in memory");
 		                //http://forum.mikrokopter.de/topic-post441164.html#post441164
 		                //Returns the index of the WP that was added => see http://svn.mikrokopter.de/ : waypoints.c PointList_SetAt method
 		                //Clearly this is a mistake in the documentation
 		                
 		                WPIndex = new u8("WPIndex");
 		                WPIndex.loadFromInt(decodedDataFrame, dataPointer);
 		                System.out.println("WPIndex (u8): " + WPIndex.value); 
 		           
 		                ArrayList sendWP = new ArrayList(); 
 		                sendWP.add("sendWP_ACK");	//set ID
 		                sendWP.add(WPIndex.value); //number of WP
 		                readQueue.add(sendWP);
 		                
 		                break;
 		                
 		            case 'X':   // Feedback from 'request WP' command
 		            	
 		            	
 		                System.out.println("<nX> Returns: # Waypoints in memory; WP index; WP struct");	                
 		                
 		                /** -- Data Structure--
 		                 * 
 		                 * byte 1: u8 number of WP
 		                 * byte 2: u8 WP index
 		                 * from byte 3: Waypoint struct
 		                */
 		          	               
 		                numberOfWP = new u8("numberOfWP");
 		                numberOfWP.loadFromInt(decodedDataFrame, dataPointer);
 		                System.out.println("NymberOfWP (u8): " + numberOfWP.value);
 		                
 		                WPIndex = new u8("WPIndex");
 		                WPIndex.loadFromInt(decodedDataFrame, dataPointer+1);
 		                System.out.println("WPIndex (u8): " + WPIndex.value);
 		                
 		             
 		                Waypoint_t WP= new Waypoint_t("WP");
 		                WP.loadFromInt(decodedDataFrame, dataPointer+2);
 		                System.out.println("Latitude: "+WP.Position.Latitude.value);
 		                System.out.println("Longitude: "+WP.Position.Longitude.value);
 		                System.out.println("Altitude: "+WP.Position.Altitude.value);
 		                System.out.println("Latitude: "+WP.Position.Latitude.value);
 		                System.out.println("Speed: "+WP.Speed.value);
 		                System.out.println("Altituderate: "+WP.AltitudeRate.value);
 		                System.out.println("ToleranceRadius: "+WP.ToleranceRadius.value);
 		                
 		                ArrayList reqWP = new ArrayList(); 	
 	                	
 	                	reqWP.add("reqWP");	//set ID
 	                	reqWP.add(numberOfWP.value); //number of WP
 	                	reqWP.add(WPIndex.value); //WP index
 	                	
 	                	reqWP.add(WP.Position.Latitude.value); 
 	                	reqWP.add(WP.Position.Longitude.value); 
 	                	reqWP.add(WP.Position.Altitude.value); 
 	                	reqWP.add(WP.Position.Status.value); 
 	                	
 	                	reqWP.add(WP.Heading.value);
 	                	reqWP.add(WP.ToleranceRadius.value);
 	                	reqWP.add(WP.HoldTime.value);
 	                	reqWP.add(WP.Event_Flag.value);
 	                	reqWP.add(WP.Index.value);
 	                	reqWP.add(WP.Type.value);
 	                	reqWP.add(WP.WP_EventChannelValue.value);
 	                	reqWP.add(WP.AltitudeRate.value);               	
 	                	reqWP.add(WP.Speed.value);
 	                	reqWP.add(WP.CamAngle.value);
 	                	reqWP.add(WP.name.toString());
 	
 		                readQueue.add(reqWP);
 		                
 		                break;    
 	
 		            case 'O':   // Feedback from 'request OSD Values'
 		            	/** We will only print limited amount of data **/
 		                System.out.println("<nO> Recieving OSD Data");
 		                
 		                NaviData_t navi = new NaviData_t();
 		                navi.loadFromInt(decodedDataFrame,dataPointer); 
 		             
 		                System.out.println("Version: "+navi.Version.value);
 		                System.out.println("CurrentPosition: (lat)"+navi.CurrentPosition.Latitude.value+" (lng) "+navi.CurrentPosition.Longitude.value+" (alt) "+navi.CurrentPosition.Altitude.value+" (status)" +navi.CurrentPosition.Status.value);
 		                System.out.println("TargetPosition: (lat)"+navi.TargetPosition.Latitude.value+" (lng) "+navi.TargetPosition.Longitude.value+" (alt) "+navi.TargetPosition.Altitude.value+" (status)" +navi.TargetPosition.Status.value);
 		                System.out.println("HomePosition: (lat)"+navi.HomePosition.Latitude.value+" (lng) "+navi.HomePosition.Longitude.value+" (alt) "+navi.HomePosition.Altitude.value+" (status)" +navi.HomePosition.Status.value);
 		                System.out.println("SatsInUse: "+navi.SatsInUse.value);
 		                System.out.println("Altimeter: "+navi.Altimeter.value);
 		                System.out.println("Heading: "+navi.Heading.value);
 		                System.out.println("Errorcode: "+navi.Errorcode.value);
 		                
 		                ArrayList OSD = new ArrayList(); 
 		                
 		                /** CurrentPosition **/
 		                OSD.add("OSD");
 		              
 		                OSD.add(navi.CurrentPosition.Latitude.value/10000000); // in 1E-7 deg
 		                OSD.add(navi.CurrentPosition.Longitude.value/10000000); // in 1E-7 deg
 		                OSD.add(navi.CurrentPosition.Altitude.value/1000); // in mm
 		                OSD.add(navi.CurrentPosition.Status.value); // validity of data 
 		                /** TargetPosition **/
 		                OSD.add(navi.TargetPosition.Latitude.value/10000000); // in 1E-7 deg
 		                OSD.add(navi.TargetPosition.Longitude.value/10000000); // in 1E-7 deg
 		                OSD.add(navi.TargetPosition.Altitude.value/1000); // in mm
 		                OSD.add(navi.TargetPosition.Status.value); // validity of data
 		                /** HomePosition **/
 		                OSD.add(navi.HomePosition.Latitude.value/10000000); // in 1E-7 deg
 		                OSD.add(navi.HomePosition.Longitude.value/10000000); // in 1E-7 deg
 		                OSD.add(navi.HomePosition.Altitude.value/1000); // in mm
 		                OSD.add(navi.HomePosition.Status.value); // validity of data 
 		              
 		                OSD.add(navi.WaypointIndex.value);  // index of current waypoints running from 0 to WaypointNumber-1
 		                OSD.add(navi.WaypointNumber.value); // number of stored waypoints
 		                OSD.add(navi.SatsInUse.value);   // number of satellites used for position solution
 		                OSD.add(navi.Altimeter.value);	 // hight according to air pressure
 		                OSD.add(navi.Variometer.value);	 // climb(+) and sink(-) rate
 		                OSD.add(navi.FlyingTime.value);	// FlyingTime in seconds
 		                OSD.add(navi.UBat.value);	// Battery Voltage in 0.1 Volts
 		                OSD.add(navi.GroundSpeed.value); // speed over ground in cm/s (2D)
 		                OSD.add(navi.Heading.value); // current flight direction in deg as angle to north
 		                OSD.add(navi.CompassHeading.value);  // current compass value in deg
 		                OSD.add(navi.AngleNick.value);// current Nick angle in 1 deg
 		                OSD.add(navi.AngleRoll.value);  // current Rick angle in 1deg
 		                OSD.add(navi.RC_Quality.value);	// RC_Quality
 		               
 		                	                
 		                // ------- FCStatusFlags -------------------------------
 		                //FC_STATUS_MOTOR_RUN                     0x01
 		                //FC_STATUS_FLY                           0x02
 		                //FC_STATUS_CALIBRATE                     0x04
 		                //FC_STATUS_START                         0x08
 		                //FC_STATUS_EMERGENCY_LANDING             0x10
 		                //FC_STATUS_LOWBAT                        0x20
 		                //FC_STATUS_VARIO_TRIM_UP                 0x40
 		                //FC_STATUS_VARIO_TRIM_DOWN               0x80
 		                
 		                OSD.add(navi.FCFlags.value); // Flags from FC
 		                
 		                // ------- NCFlags -------------------------------------
 		                // NC_FLAG_FREE                            0x01
 		                // NC_FLAG_PH                              0x02
 		                // NC_FLAG_CH                              0x04
 		                // NC_FLAG_RANGE_LIMIT                     0x08
 		                // NC_FLAG_NOSERIALLINK                    0x10
 		                // NC_FLAG_TARGET_REACHED                  0x20
 		                // NC_FLAG_MANUAL                          0x40
 		                // NC_FLAG_GPS_OK                          0x80
 		                
 		                OSD.add(navi.NCFlags.value); // Flags from NC		                
 		                OSD.add(navi.Errorcode.value); // 0 --> okay
 		                OSD.add(navi.OperatingRadius.value); // current operation radius around the Home Position in m
 		                OSD.add(navi.TopSpeed.value);   // velocity in vertical direction in cm/s
 		                OSD.add(navi.TargetHoldTime.value); // time in s to stay at the given target, counts down to 0 if target has 
 		               
 		                // ------- FCStatusFlags2 ------------------------------
 		                //FC_STATUS2_CAREFREE_ACTIVE              0x01
 		                //FC_STATUS2_ALTITUDE_CONTROL_ACTIVE      0x02
 		                //FC_STATUS2_FAILSAFE_ACTIVE              0x04
 		                //FC_STATUS2_OUT1                         0x08
 		                //FC_STATUS2_OUT2                         0x10
 		                //FC_STATUS2_RES1                         0x20
 		                //FC_STATUS2_RES2                         0x40
 		                //FC_STATUS2_RES3                         0x80
 		                
 		                OSD.add(navi.FCStatusFlags2.value); // StatusFlags2 (since version 5 added)
 		                OSD.add(navi.SetpointAltitude.value); // setpoint for altitude
 		                OSD.add(navi.Current.value); // actual current in 0.1A steps
 		                OSD.add(navi.Gas.value);
 		                OSD.add(navi.UsedCapacity.value); // used capacity in mAh
 		                OSD.add(navi.Version.value);
 		                readQueue.add(OSD);
 		                   
 		                break;
 		                
 		            case 'C':   // Feedback from 'set 3D data interval'
 		                System.out.println("<nC> Recieving 3D Data");
 		                /** We will only print limited amount of data **/
        
 		                Data3D_t data3D = new Data3D_t();
 		                data3D.loadFromInt(decodedDataFrame,dataPointer);
 
 		                System.out.println("AngleNick: "+data3D.AngleNick.value);// AngleNick in 0.1 deg
 		                System.out.println("AngleRoll: "+data3D.AngleRoll.value);// AngleRoll in 0.1 deg
 		                System.out.println("Heading: "+data3D.Heading.value); // Heading in 0.1 deg
 		                
 	                	ArrayList set3DInterval = new ArrayList(); 	
 	                	
 	                	set3DInterval.add("Data3D");	//set ID
 	                	set3DInterval.add(data3D.AngleNick.value); // AngleNick in 0.1 deg
 		                set3DInterval.add(data3D.AngleRoll.value); // AngleRoll in 0.1 deg
 		                set3DInterval.add(data3D.Heading.value); // Heading in 0.1 deg
 		                
 		                readQueue.add(set3DInterval);
 		                
 	
 		                break; 
 		            
 		            case 'J':   // Feedback from 'Set/Get NC-Parameter' (only set when when there is a value
 		                System.out.println("<nJ> Returns: ParameterId,Value");
 		                
 		                /** -- Data Structure--
 		                 * 
 		                 * byte 1: u8 parameterId
 		                 * from byte 2: s16 value
 		                */
 		          	               
 		                parameterId = new u8("parameterId");
 		                parameterId.loadFromInt(decodedDataFrame, dataPointer);
 		                System.out.println("Parameter Id (u8): " + parameterId.value);
 		                
 		                parameterValue = new s16("WPIndex");
 		                parameterValue.loadFromInt(decodedDataFrame, dataPointer+1);
 		                System.out.println("Parameter Value (s16): " + parameterValue.value);
 		                
 		                ArrayList getsetNCParam = new ArrayList(); 	
 	                	
 	                	getsetNCParam.add("getsetNCParam");	//set ID
 	                	getsetNCParam.add(parameterId.value); // parameter ID
 	                	getsetNCParam.add(parameterValue.value); //parameter value
 
 		                readQueue.add(getsetNCParam);
                 
 		                break;   
 		    
 		            case 'K':   // Feedback from 'BL Ctrl Status'
 		            	//TODO Handle different motors
 		                BLData_t motorData = new BLData_t();
 		                motorData.loadFromInt(decodedDataFrame, dataPointer);
 		             
 		                
 		                System.out.println("<nK> Recieving BL Ctrl Data (DC Motor "+motorData.Index.value+" Data)");
 		                int motorID = (int) motorData.Index.value;
 		                motorList[motorID] = motorData;
 		                ArrayList BLCtrlStatus = new ArrayList(); 	
 	                	
 	                	BLCtrlStatus.add("MotorData");	//set ID
 	                	BLCtrlStatus.add(motorData.Index.value); //Index
 	                	BLCtrlStatus.add(motorData.Current.value);//Current
 	                	BLCtrlStatus.add(motorData.Temperature.value);//Temperature
 	                	BLCtrlStatus.add(motorData.MaxPWM.value);//MAXPW
 	                	BLCtrlStatus.add(motorData.State.value);//Status
 		                readQueue.add(BLCtrlStatus);
 		                           
 		               break; 
 		               
 		            default: 
 			        	   System.out.println("Unsupported command recieved"); 
 			        	   System.out.println("CMD Id: "+decodedDataFrame[2]);
 			        	   break;
 					}
 				break;
 			case FC_ADDRESS:		//frame from Flight Ctrl
 				switch (decodedDataFrame[2]){
 					case 'T':   // Feedback from 'BL Ctrl Status'
 						//!!Empty ack frame
 		                System.out.println("<fT>Motor Test (empty frame)");
 		                ArrayList motorTest = new ArrayList(); 		
 		                motorTest.add("motorTest");	//set ID; motorTest frame contains no data    
 		                readQueue.add(motorTest);
 		                break; 
 		                
 		            case 'P':   // Feedback from 'BL Ctrl Status'
 		            	//TODO Test
 		                System.out.println("<fP>Read PPM Channels");
 		                //TODO There should be a constant somewhere containing the number of motors
 		                int numOfMotors=8;
 		                s16[] PPM_Array = new s16[numOfMotors];
 		                String PPM_out = "";
 		                for (int i=0; i<numOfMotors; i++){
 		                	s16 PPM_Data = new s16();
 		                	PPM_Data.loadFromInt(decodedDataFrame, dataPointer+i);
 		                	PPM_Array[i] = PPM_Data;
 		                	PPM_out += i + ": "+ PPM_Data.value + " ";
 		                }
 		                System.out.println(PPM_out);		                
 		                break; 
            
 		           case 'N':   // Feedback from 'Mixer Request'
 		        	   	//TODO Test
 		                System.out.println("<fN>Mixer request");
 		                //revision
 		                u8 MixerRevision = new u8();
 		                MixerRevision.loadFromInt(decodedDataFrame, dataPointer);
 	                	System.out.println("Revision: "+ MixerRevision.value);
 		                
 		                //name
 		                u8[] Name_Array = new u8[12];
 	                	String Mixer_out="";
 		                for (int i=0; i<12; i++){
 		                	u8 Name_Data = new u8();
 		                	Name_Data.loadFromInt(decodedDataFrame, dataPointer+i);
 		                	Name_Array[i] = Name_Data;
 		                	Mixer_out += Name_Data.value;
 		                }
 		                System.out.println("Name: "+Mixer_out);
 		                
 		                //table
 		                u8 MixerTable = new u8();
 		                //TODO Add mixer table [16][4]      
 		                break; 
 		                
 		           default: 
 		        	   System.out.println("<f> Unsupported command recieved"); 
 		        	   System.out.println("CMD Id: "+decodedDataFrame[2]);
 		        	   break;
 				}
 				//break;
 				
 			default: 				//frame from Common Commands
 			
 				switch (decodedDataFrame[2]){
 					
 				    case 'A':   // Feedback from 'get labels of the analog values in debug data struct'		              
 			                /** -- Data Structure--
 			                 * 
 			                 * byte 1: 		u8 Index
 			                 * byte 2-end:	char[16] label text
 			                */
 			                
 			                u8 analogLabelIndex = new u8("AnalogLabelIndex");
 			            	analogLabelIndex.loadFromInt(decodedDataFrame, dataPointer);
 			            	System.out.println("Label Index: "+analogLabelIndex.value);
 			            			            	
 			            	String analogLabelText = convertMessage(decodedDataFrame, dataPointer+1);
 			            	System.out.println("Analog Label: " + analogLabelText);
 			            	
 			            	int convIndex = (int) analogLabelIndex.value;
 			                names[convIndex]=analogLabelText;
 			            
 			                ArrayList analogLabel = new ArrayList(); 		
 			                analogLabel.add("analogLabel");	//set ID; 
 			                analogLabel.add(analogLabelIndex.value);  
 			                analogLabel.add(analogLabelText);  
 			                readQueue.add(analogLabel); //analog label index	
 			                
 			                break;     
 			                
 				    case 'B':   // Feedback from 'External Control'
 			                System.out.println("<B> Frame: unsigned char,echo of externCtrlStruct.Frame");
 			                
 			                /** 
 			                 *  unsigned char Digital[2];
 			                 *  unsigned char RemoteTasten;
 			                 *  signed char   Nick;
 			                 *  signed char   Roll;
 			                 *  signed char   Gier;
 			                 *  unsigned char Gas;
 			                 *  signed char   Hight;
 			                 *  unsigned char free;
 			                 *  unsigned char Frame; => can be used as an ACK ID; this one is send back
 			                 *  unsigned char Config; 
 			                 *  
 			                 * **/
 			              
 			                //TODO Test this conversion; maybe have a system remember the value and check against it
 			                char Ext_frame = (char) decodedDataFrame[dataPointer];
 			                System.out.println("confirmation: " +Ext_frame);
 			                
 			                ArrayList externCtrl = new ArrayList(); 		
 			                externCtrl.add("externCtrl");	//set ID;   
 			                //TODO Implement extern ctrl struct: not documented online; look up source code
 			                readQueue.add(externCtrl); 
 			                
 				    		break;
 			           
 				    case 'H':   // Feedback from 'Request Display/LCD Data'	  1               
 			                System.out.println("<H> Recieving Display Data");
 			               
 			                /** -- Data Structure--
 			                 * 
 			                 * byte 1-end: char[80] DisplayText
 			                */
 			                
 			            
 			                String displayMessage_h = convertMessage(decodedDataFrame, dataPointer);
 			                System.out.println("Display Message: "+displayMessage_h);
 			                
 			                ArrayList reqDisplay = new ArrayList(); 		
 			                reqDisplay.add("reqDisplay");	//set ID; 
 			                reqDisplay.add(displayMessage_h);  		               
 			                readQueue.add(reqDisplay); 
 			                break;
 			                
 				    case 'L':   // Feedback from 'Request Display/LCD Data' 2
 			                System.out.println("<L> Frame: MenuItem,MaxMenuItem,DisplayText");
 		   
 			                /** -- Data Structure--
 			                 * 
 			                 * byte 1: u8 MenuItem
 			                 * byte 2: u8 MaxMenuItem   => WHAT IS THIS??
 			                 * byte 3: char[80] Display Text
 			                */
 			                
 			                u8 menuItem = new u8("MenuItem");
 			                menuItem.loadFromInt(decodedDataFrame, dataPointer);
 			                System.out.println("MenuItem: " +menuItem.value);
 			                
 			                u8 maxMenuItem = new u8("MaxMenuItem");
 			                maxMenuItem.loadFromInt(decodedDataFrame, dataPointer+1);
 			                System.out.println("maxMenuItem: " +maxMenuItem.value);
 			                
 			                String displayMessage_l = convertMessage(decodedDataFrame, dataPointer+2);
 			                System.out.println("Display Message: "+displayMessage_l);
 			                
 			                ArrayList reqMenuDisplay = new ArrayList(); 		
 			                reqMenuDisplay.add("reqMenuDisplay");	//set ID; 
 			                reqMenuDisplay.add(menuItem); //MenuItem
 			                reqMenuDisplay.add(maxMenuItem); //MaxMenuItem
 			                reqMenuDisplay.add(displayMessage_l); //Display Message
 			                
 			                readQueue.add(reqMenuDisplay); //analog label index	
 			                
 			                break;
 	
 				    case 'V':   // Feedback from 'Version Info'
 			                System.out.println("<V> Frame: VersionStruct");
 			                
 			                VersionInfo_t versionStruct = new VersionInfo_t("versioninfo");
 			                versionStruct.loadFromInt(decodedDataFrame, dataPointer);
 		   
 			                System.out.println("SWMajor :"+ versionStruct.SWMajor.value);
 			                System.out.println("SWMinor :"+ versionStruct.SWMinor.value);
 			                System.out.println("ProtoMajor :"+ versionStruct.ProtoMajor.value);
 			                System.out.println("ProtoMinor :"+ versionStruct.ProtoMinor.value);  
 			                System.out.println("SWPatch :"+ versionStruct.SWPatch.value);
 			                System.out.println("HardwareError :"+ versionStruct.HardwareError.toString()); //TODO TEST
 			               
 			                //TODO Document hardwareError codes http://www.mikrokopter.de/ucwiki/en/SerialCommands/VersionStruct
 			                ArrayList versionInfo = new ArrayList(); 		
 			                versionInfo.add("versionInfo");	//set ID; 
 			                versionInfo.add(versionStruct.SWMajor.value); //SWMajor
 			                versionInfo.add(versionStruct.SWMinor.value); //SWMinor
 			                versionInfo.add(versionStruct.ProtoMajor.value); //ProtoMajor
 			                versionInfo.add(versionStruct.ProtoMinor.value); //ProtoMinor
 			                versionInfo.add(versionStruct.SWPatch.value); //SWPatch
 			                //TODO: Test this not sure this works
 			                //TODO Document hardwareError codes http://www.mikrokopter.de/ucwiki/en/SerialCommands/VersionStruct
 			                versionInfo.add(versionStruct.HardwareError.toString()); //HardwareError
 			                readQueue.add(versionInfo); 
 			                
 			                break;
 			                
 				    case 'D':   // Feedback from 'request debug Data'
 			               	System.out.println("<D> Frame: Debug Request");
 			               	//TODO check strange param ADRESS
 			              
 			                /** -- Data Structure--
 			                 * 
 			                 * unsigned char Status [2]
 			                 * signed int Analog[32]
 			                */
 			               	
 			               	String debug_status = "";
 			               	debug_status += (char) decodedDataFrame[dataPointer];
 			               	debug_status += (char) decodedDataFrame[dataPointer+1];
 			               	
 			               	int[] analog = new int[32];
 			               	
 			                //skip status and parse unsigned int
 			                for (int i=2;i<34;i++){                 
 			                	analog[i] = (int) decodedDataFrame[dataPointer+i];
 			                }
 			                
 			                //TODO Better handle this
 			                ArrayList analogData = new ArrayList(); 		
 			                analogData.add("analogData");	//set ID; 
 			                for (int i=0;i<analog.length; i++){
 			                	analogData.add(analog);	
 			                }
 
 			                readQueue.add(analogData); 
 			                
 			                
 			                System.out.println("Debug data recieved");
 			                
 			                break; 
 			                
 				    case 'G':   // Feedback from 'Get External control'	
 			                System.out.println("<G> Returns: ExternControl Struct");
 			                
 			                //So what is the difference between this command and ExternCtrl? Since this is basically and empty frame (ack with externctrl struct)
 			                //we can assume this enables the extern control?
 			                //TODO Test
 			                /** 
 			                 *  unsigned char Digital[2];
 			                 *  unsigned char RemoteTasten;
 			                 *  signed char   Nick;
 			                 *  signed char   Roll;
 			                 *  signed char   Gier;
 			                 *  unsigned char Gas;
 			                 *  signed char   Hight;
 			                 *  unsigned char free;
 			                 *  unsigned char Frame; => can be used as an ACK ID; this one is send back
 			                 *  unsigned char Config; 
 			                 *  
 			                 * **/
 			              
 			                //TODO Test this conversion; maybe have a system remember the value and check against it
 			                //TODO Make a datatype externctrl (not a priority right now)
 			                
 				    		break;
 				}
 				break;
 		}
 		
 	
 	
         dataPointer = 0;
         numBytesDecoded = 0;
        
 		
 	}
 
 	
 }
