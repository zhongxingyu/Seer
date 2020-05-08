 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.StringTokenizer;
 
 public class ServerCommRX {
 	
 	//Data Members
     BufferedReader[] mDataIn;  
     InputStream[] mSocketIn;
     int mId, mN;    
     ServersConfig mSC;
     static Thread[] mListenerThreads;
     String mWorkerBaseFilePath;
     String mWorkerFullFilePath;
     ServerWorker mSW;
     ServerCoordinator mSCoord;
     
 	//Constructor	
 	public ServerCommRX( BufferedReader[] inDataIn, InputStream[] inSocketIn, ServerWorker inSW,
 						 ServerCoordinator inCoord){
 		mSC = ServersConfig.getConfig();
 		mId = mSC.mMyID;
 		mN = mSC.mNumServers;
 		mDataIn = inDataIn;
 		mSocketIn = inSocketIn;
 		mSW = inSW;
 		mSCoord = inCoord;
 	      		
 		//Create listener threads on each channel
 		mListenerThreads = new Thread[mN]; //Although we have an extra thread object, we don't run the thread at index == mId 
 		for(int n=0;n<mN-1;n++){
 			if(n!=mId){
 				mListenerThreads[n] = new Thread(new ListenerThread(n));
 				mListenerThreads[n].run();
 			}
 		}
 		
 		threadMessage("ServerCommRX created mID: "+ mId);
 		
 	}
 	
     static void threadMessage(String message)
     {
         String threadName =
             Thread.currentThread().getName();
         System.out.format("%s: %s%n",
                           threadName,
                           message);
     }
 	
     //Receive Msg
     public ServerMsg receiveMsg(int fromId) throws IOException  {
         String getline = mDataIn[fromId].readLine();      
         StringTokenizer st = new StringTokenizer(getline);
         int srcId = Integer.parseInt(st.nextToken());
         int destId = Integer.parseInt(st.nextToken());
         String tag = st.nextToken("#");
         String msg = st.nextToken();
         
         threadMessage("ServerCommRX received message " + srcId + " " + destId + " " + tag + " " + msg);
         return new ServerMsg(srcId, destId, tag, msg);
     }
     
     //Receive File
     public void receiveFile(int inSrcID, String inName){
 
     	mWorkerFullFilePath =  mWorkerBaseFilePath + inName;
     	
     	 threadMessage("ServerCommRX receiving file srcID: " + inSrcID + " Name:  " +  inName);
     	
     	byte[] filebuffer = new byte[65536];
         try {           
             FileOutputStream fos = new FileOutputStream(mWorkerFullFilePath);
 
             int count;
             while ((count = mSocketIn[inSrcID].read(filebuffer)) >= 0) {
                 fos.write(filebuffer, 0, count);
             }
         }catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
     //Process Msg
    public void processMsg(ServerMsg m) throws IOException{
     	String tag = m.getTag();
     	tag = tag.trim();
     	
     	//Messages received as worker
     	if(tag.equals("job_init")){
     		//set server worker object's coordinator ID field
     		mSW.InitJob(m.srcID);
     	}    	
     	else if(tag.equals("file_transfer_start")){
     		receiveFile(m.srcID,m.getMessage());
     	}
     	else if(tag.equals("file_transfer_done")){
     		//send  message "image_received_ack" to coordinator
     		//set file path for worker object
     		mSW.ImageReceived(mWorkerFullFilePath);
     	}
     	else if(tag.equals("job_start")){    		
     		String payload = m.getMessage();
             StringTokenizer st = new StringTokenizer(payload);
             int XCoord = Integer.parseInt(st.nextToken());
             int YCoord = Integer.parseInt(st.nextToken());
             int JobItemID = Integer.parseInt(st.nextToken());
     		
     		mSW.StartJob(XCoord, YCoord, JobItemID);
     	}
     	else if(tag.equals("job_stop")){
     		//call mSW.stopProcessing
     		//\todo add a message queue in ServerWorker
     	}
     	
     	//Message received as coordinator
     	else if(tag.equals("job_done")){
     		String payload = m.getMessage();
             StringTokenizer st = new StringTokenizer(payload);
             int jobid = Integer.parseInt(st.nextToken());            
             int featMatched = Integer.parseInt(st.nextToken());
             
             int wid = m.getSrcId();
     		mSCoord.JobDone(jobid, wid, featMatched);
     		
     	}
     	else if(tag.equals("image_received_ack")){
     		//add to coordinator queue
     		int wid = m.getSrcId();
     		mSCoord.ImageRcvAck(wid);
     	}
     }
     
     //Listener Thread
     public class ListenerThread extends Thread {
         int channel;     
         public ListenerThread(int channel) {
             this.channel = channel;           
         }
         public void run() {
         	threadMessage("ServerCommRX starting listener thread channel: "+ channel);
             while (true) {
                 try {
                     ServerMsg m = receiveMsg(channel);
                     processMsg(m);
                 } catch (IOException e) {
                     System.err.println(e);
                 }
             }
         }
     }
     
 }
