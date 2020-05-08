 package PatternGame;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import SDK.Edk;
 import SDK.EdkErrorCode;
 import SDK.EmoState;
 
 import com.sun.jna.Pointer;
 import com.sun.jna.ptr.IntByReference;
 
 /*
  * Initializes the connection to the Emotiv device in
  * preparation for raw data collection. The Majority of this code
  * was provided by Emotiv and modified for our needs.
  */
 public class DataCollector extends Thread {
 	
 	private String fileName = null;
 	private Pointer eState = null;
 	private Pointer eEvent = null;
 	private BufferedWriter out = null;
 	public boolean collecting;
 	public boolean writingMatrix;
 	private Matrix matrix;
 	private int sample;
 	
 	/*
 	 * Initializes and starts the thread of execution
 	 */
 	public DataCollector(String threadName, String fileName) {
 		super(threadName);
 		this.fileName = fileName;
 		start();
 	}
 	
 	/*
 	 * The threads main flow of execution. Initializes the Emotiv device and
 	 * then reads data from the sensors. This data is constantly written to the
 	 * file specified by fileName. Also, writes sensor data to the Matrix object
 	 * matrix when set to do so. 
 	 * 
 	 */
 	public void run() {
 		/*Initialization*/
 		eEvent				= Edk.INSTANCE.EE_EmoEngineEventCreate();
     	eState				= Edk.INSTANCE.EE_EmoStateCreate();
     	IntByReference userID 		= null;
 		IntByReference nSamplesTaken= null;
     	int state  					= 0;
     	float secs 					= 60;
     	boolean readytocollect 		= false;
     	collecting = true;
     	writingMatrix = false;
     	userID 			= new IntByReference(0);
 		nSamplesTaken	= new IntByReference(0);
 		
 		/*Initialize the text file we are printing to for the visualization data*/
 		try {
 			out = new BufferedWriter(new FileWriter("VisualizationData/" + fileName + ".txt"));
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 			System.exit(-1);
 		}
 	
 		if (Edk.INSTANCE.EE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
 			System.err.println("Emotiv Engine start up failed.");
 			return;
 		}
     	
 		Pointer hData = Edk.INSTANCE.EE_DataCreate();
 		Edk.INSTANCE.EE_DataSetBufferSizeInSec(secs); 
 		
     	System.out.println("Started receiving EEG Data!");
     	
 		while (collecting) {	
 			
 			state = Edk.INSTANCE.EE_EngineGetNextEvent(eEvent);
 
 			// New event needs to be handled
 			if (state == EdkErrorCode.EDK_OK.ToInt()) {
 				int eventType = Edk.INSTANCE.EE_EmoEngineEventGetType(eEvent);
 				Edk.INSTANCE.EE_EmoEngineEventGetUserId(eEvent, userID);
 
 				// Log the EmoState if it has been updated
 				if (eventType == Edk.EE_Event_t.EE_UserAdded.ToInt()) {
 						if (userID != null) {
 							System.out.println("User added");
 							Edk.INSTANCE.EE_DataAcquisitionEnable(userID.getValue(),true);
 							readytocollect = true;
 						}
 				}
 				if (eventType == Edk.EE_Event_t.EE_EmoStateUpdated.ToInt()) {
 					Edk.INSTANCE.EE_EmoEngineEventGetEmoState(eEvent, eState);
 				}
 			}
 			else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
 				System.err.println("Internal error in Emotiv Engine!");
 				break;
 			}
 			
 			if (readytocollect) {
 				//get the data from device
 				Edk.INSTANCE.EE_DataUpdateHandle(0, hData);
 				Edk.INSTANCE.EE_DataGetNumberOfSample(hData, nSamplesTaken);
 
 				if (nSamplesTaken != null) {
 					if (nSamplesTaken.getValue() != 0) {
 						
 						double[] data = new double[nSamplesTaken.getValue()];
 						
 						for (int sampleIdx=0 ; sampleIdx < nSamplesTaken.getValue(); ++sampleIdx) {
 							try {
 								//write the millisecond time stamp
 								Edk.INSTANCE.EE_DataGet(hData, 19, data, nSamplesTaken.getValue());
 								//The millisecond column
 								out.write(Integer.toString((int) (data[sampleIdx] * 1000)) + " ");
 								
 								//loop through the the data columns
 								for (int i = 0 ; i < 25 ; i++) {
 									
 									Edk.INSTANCE.EE_DataGet(hData, i, data, nSamplesTaken.getValue());
 									
 									if ( writingMatrix && i >= 3 && i <= 16) {
 										try {
 											matrix.matrix[sample][i-3] = data[sampleIdx];
 										} catch (ArrayIndexOutOfBoundsException e) {
 											writingMatrix = false;
 										}
 									}
 									
 									//Write the column data to the file
 									out.write( Double.toString((data[sampleIdx])));
 									out.write(" ");
 								}
 								
 								//increment the sample
 								if(writingMatrix)
 									sample++;
 								
 								//write key indicator column
								out.write("0");
 								
 								//Print the contact quality columns to our file
 								//The ordering is consistent with the ordering of the logical input
 					    		//channels in EE_InputChannels_enum.
 								for (int i = 1; i < 15 ; i++)
 									out.write(" " + EmoState.INSTANCE.ES_GetContactQuality(eState, i) + " ");
 								
 								//next row
 								out.newLine();
 							} catch (IOException e) {
 								System.err.println(e.getMessage());
 								System.exit(-1);
 							}
 						}//END for()
 					}
 				}
 			} //END  if(ready to collect)
 		} //END while
 		cleanUp();
 	}
 
 	/*
 	 * Creates a Matrix object of size seconds and
 	 * sets the thread to fill it with data until
 	 * full.
 	 */
 	public void setMatrix(int seconds) {
 		this.matrix = new Matrix(seconds);
 		this.sample = 0;
 		this.writingMatrix = true;
 	}
 	
 	/*
 	 * returns the matrix object
 	 */
 	public Matrix getMatrix() {
 		return this.matrix;
 	}
 	
 	/*
 	 * Shuts down the Emotiv connection
 	 * Frees the eState and eEvent memory
 	 */
 	public void cleanUp() {
 		//close all connections;
 		try {
 			out.close();
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 		}
 		Edk.INSTANCE.EE_EngineDisconnect();
 		Edk.INSTANCE.EE_EmoStateFree(eState);
 		Edk.INSTANCE.EE_EmoEngineEventFree(eEvent);
 	}
 }
