 import javax.imageio.ImageIO;  
 
 import org.openimaj.image.ImageUtilities;
 import org.openimaj.image.MBFImage;
 import org.openimaj.image.colour.ColourSpace;
 import org.openimaj.image.colour.RGBColour;
 import org.openimaj.image.pixel.Pixel;
 import org.openimaj.image.typography.general.GeneralFont;
 import org.openimaj.math.geometry.shape.Polygon;
 
 import java.awt.Font;
 import java.awt.image.BufferedImage;  
 import java.io.*; 
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 
 
 public class ServerCoordinator {
 	private static final int NUM_RESULTS = 3;
 	private final static String WALDO_RESULT_IMAGE = "waldo_search_results.jpg";
 	static //Data members
 	String mLocalImgPath,mLocalImgResPath;
 	String mLocalBasePath;//Base path is $HOME/WaldoFiles/Coordinator
 	int mImgWd, mImgHt, mTmpWd, mTmpHt, mVStride, mHStride,mNumJobItemsRem, 
 		mWorkersAvailable,mCurrentJobIdx;
 	ServerImageTemplate mSit;
 	ImageSenderThread mImgS;
 	static Thread mThImageSender, mThScheduler;
 	static ServerCommTX mSCT;
 	static ArrayList<JobItem> mJIList; // List to track status of job-items
 	Boolean mClientJobDone;
 			
 	// List to track status of each worker
 	//Worker status can be 
 	//SELF(It's coordinator itself)
 	//UNINIT(Worker hasn't received image yet)
 	//INIT_START(Worker init message has been sent, but hasn't received an ack yet)
 	//INIT_DONE(Worker init message has been sent and ack received)
 	//IMAGE_SEND_START
 	//IMAGE_SENT(Worker has received image)
 	//BUSY(Worker is still processing the last request sent by the coordinator)
 	//DONE(Worker is done processing the last request)
 	static ArrayList<String> mWList; 
 	
 	static ServersConfig mSC;
 	
 	//Constructor	
 	//Instantiated on the server start
 	public ServerCoordinator(ServerCommTX inSCT, ServerImageTemplate inSit) throws IOException{
 		mSCT = inSCT;
 		mSit = inSit;
 		mSC = ServersConfig.getConfig();
 		mWorkersAvailable = 0;
 		mNumJobItemsRem = 0;
 		mLocalImgPath = "";
 				
 		mClientJobDone = false;
 		mImgS = new ImageSenderThread();
 		mThImageSender = new Thread(mImgS);
 		mThScheduler = new Thread(new JobScheduler(this));
 	
 		//Create directories
 		
 		File homeDir = new File(System.getProperty("user.home"));
 		File dir = new File(homeDir,"//WaldoFiles//Coordinator//" + "_" + mSC.getMyID());
 		if (!dir.exists() && !dir.mkdirs()) {
 			throw new IOException("Unable to create " + dir.getAbsolutePath());
 		}	
 		else{
 			mLocalBasePath = dir.getPath();
 		}
 		
 		threadMessage("Server Coordinator instantiated");
 	}
 	
     static void threadMessage(String message)
     {
         String threadName =
             Thread.currentThread().getName();
         System.out.format("%s: %s%n",
                           threadName,
                           message);
     }
 	
 	//ProcessJob
 	//Called by ServerClientComm on receiving a request from the client
 	public synchronized void ProcessJob(String inFileName){
 				
 		mLocalImgPath = mLocalBasePath  + "/" + inFileName;
 		
 		threadMessage("ServerCoordinator Process Job: " + mLocalImgPath);
 		
 		//Determine dimension of the image	
 		File file = new File(mLocalImgPath); 
         FileInputStream fis;
         BufferedImage image;
 		try {
 			fis = new FileInputStream(file);
 			image = ImageIO.read(fis);
 			mImgWd = image.getWidth();
 			mImgHt = image.getHeight();
 		} catch (FileNotFoundException e) {			
 			e.printStackTrace();
 		} catch (IOException e) {			
 			e.printStackTrace();
 		}
 		
 		//Determine how many sub-sections of image we need to process
 		//This is equal to number of job items	
 		//\todo should be set at run time from image dimensions
 		mHStride = 5;
 		mVStride = 5;
 				
 		//Create job queue data structure			
 		mJIList = new ArrayList<JobItem>();
 		int xcoord =0, ycoord = 0;
 		for(xcoord=0; xcoord<mImgWd; xcoord+=mHStride){
 			for(ycoord=0; ycoord<mImgHt; ycoord+=mVStride){
 				JobItem ji = new JobItem(xcoord,ycoord);
 				mJIList.add(ji);
 								
 			}				
 		}
 		mNumJobItemsRem = mJIList.size();
 		mCurrentJobIdx = 0;
 			
 		//Create worker status data structure		
 		mWList = new ArrayList<String>();
 		for(int n=0;n<mSC.mNumServers;n++){
 			if(n != mSC.mMyID){
 				mWList.add("UNINIT");
 			}else{
 				mWList.add("SELF");
 			}
 		}
 			
 		
 		//Run Image sender thread
 		mImgS.setFilePath(mLocalImgPath);
 		mThImageSender.start();
 						
 		//Run scheduler thread
 		mThScheduler.start();
 	}
 	
 			
 	//Starts a job on a worker.
 	//Called by the scheduler thread
 	public synchronized void JobStart(){
 		Boolean found=false;
 		
 		//Find free worker
 		int n = 0, wid;
 		while(n<mSC.mNumServers && !found){
 			if(mWList.get(n).equals("DONE") || mWList.get(n).equals("IMAGE_SENT")){
 				found = true;
 			}
 			n++;
 		}
 		
 		wid = n-1;
 		
 		//setWorkerStatus to busy		
 		mWList.set(wid, "BUSY");
 		
 		//Get next job
 		JobItem ji = mJIList.get(mCurrentJobIdx);
 		
 		//send message "job_start" with coordinates to worker server
 		String msg = "job_start#" + ji.x  + " " + ji.y + " " + mCurrentJobIdx;
 		try {
 			mSCT.sendMsg(wid,msg);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		//Reduce number of workers available
 		mWorkersAvailable--;
 		
 		//Increment job id
 		mCurrentJobIdx++;
 		
 		notify();
 	}
 		
 	//Job Done Receive
 	//Called by ServerServerComm processMSG
 	public synchronized void JobDone(int inJobID, int inWID, int inFeatMatched, Boolean timeout){		
 		if(!timeout){		
 			//update job item
 			mJIList.get(inJobID).mFeatMatched = inFeatMatched;
 			mJIList.get(inJobID).mStatus = "P"; 
 			//Decrement number of mNumJobItemsRem
 			mNumJobItemsRem--;
			
			
 		}
 		
 		//After all the jobs are completed, we may receive a few timeout messages from workers
 		//This can happen it may take some time to notify all the workers that the processing 
 		//is done
 		if(mNumJobItemsRem>0){
 			//setWorkerStatus to done
 			mWList.set(inWID, "DONE");
 		
 			//Increment number of workers available
 			mWorkersAvailable++;
 		}
 		
 		notify();
 	}
 	
 	
 	//Image receive ack
 	//Called by ServerServerComm processMSG
 	public synchronized void ImageRcvAck(int inWID){
 		//Set worker status to init
 		mWList.set(inWID, "IMAGE_SENT");
 			
 		mWorkersAvailable++;
 		
 		notify();
 	}
 	
 	public synchronized void UnInitWorkers(){
     	for(int i=0;i<mWList.size() ;i++){
     		if(mWList.get(i) != null){    			
     				String msg = "job_uninit#" + Integer.toString(mSC.mMyID);
     				try {
     					if(i!=mSC.mMyID){
     						mSCT.sendMsg(i, msg);
     					}
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}  
     				mWList.set(i, "UNINIT");
     				       		
     		}
     	}
 	}
 	
 	public Boolean getClientJobDone(){
 		return mClientJobDone;
 	}
 	
 	public String getResultImgPath(){
 		return mLocalImgResPath;
 	}
 	
 	public synchronized int getNumWorkersAvailable(){
 		return mWorkersAvailable;		
 	}
 	
 	public synchronized int getNumJobItemsRem(){
 		return mNumJobItemsRem;		
 	}
 	
	public synchronized Boolean getAllJobsSent(){
		if(mCurrentJobIdx == mJIList.size()){
			return true;
		}
		else{
			return false;
		}
	}
	
 	private static class ImageSender{
 		String mFilePath;
 		
 		public ImageSender(){}
 		
         public void setFilePath(String inFilePath){
         	mFilePath = inFilePath;
         }
 		
 	}
 	
 	//Image sender thread
 	//Separate thread so that jobs on some worker threads can get started before
 	//images are transferred to all the workers
     private static class ImageSenderThread extends ImageSender implements Runnable
     {
     	
     	public ImageSenderThread()
         {    		
         }
     	
         public void run()
         {            
         	for(int i=0;i<mWList.size() ;i++){
         		if(mWList.get(i) != null){
         			if(mWList.get(i).equals("UNINIT")){
         				String msg = "job_init#" + Integer.toString(mSC.mMyID);
         				try {
 							mSCT.sendMsg(i, msg);
 							mSCT.sendFile(i,mFilePath);  
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}    
         				     
         			}
         		}
         	}        	        	
 
         }
         
 
     }
 	
 						
 	//Scheduler thread
     private static class JobScheduler implements Runnable
     {    	
     	ServerCoordinator mSCoord;
     	public JobScheduler(ServerCoordinator inSCoord)    	
         {    		
     		mSCoord = inSCoord;
         }
     	
     	//This keeps on running until there are job items to process.
         public void run()
         {            
    		while(!mSCoord.getAllJobsSent()){
     			if(mSCoord.getNumWorkersAvailable() > 0){
     				mSCoord.JobStart();
     			}    				
     		}
     		
     		//Send uninit message to workers. This will put all the servers back to initialize
     		//state of non-workers and non-coordinators
     		mSCoord.UnInitWorkers();
     		
     		// Load the image and sort the candidates by number of matches
     		MBFImage target = null;
 			try {
 				target = ImageUtilities.readMBF(new File(mLocalImgPath));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
     		List<MatchCandidate> curCandidates = new LinkedList<MatchCandidate>();
     		Collections.sort(mJIList, new Comparator<JobItem>(){
 				@Override
 				public int compare(JobItem job1, JobItem job2) {
 					if (job1.mFeatMatched > job2.mFeatMatched) {
 						return -1;
 					} else if (job1.mFeatMatched < job2.mFeatMatched) {
 						return 1;
 					}
 					return 0;
 				}
     		});
     		
     		// Draw the top 3 results on the image
     		for (int i = 0, j = 0 ; j < NUM_RESULTS ; i++) {
     			JobItem result = mJIList.get(i);
     			MatchCandidate curBest = new MatchCandidate(result.x, result.y, result.mFeatMatched);
     			if (overlappingResult(curBest, curCandidates)) continue;
     			j++;
     			curCandidates.add(curBest);
     			ArrayList<Pixel> rectangle = new ArrayList<Pixel>();
     			rectangle.add(new Pixel(curBest.x, curBest.y));
     			rectangle.add(new Pixel(curBest.x, curBest.y + ServerWorker.SUB_IMAGE_HEIGHT));
     			rectangle.add(new Pixel(curBest.x + ServerWorker.SUB_IMAGE_WIDTH, curBest.y));
     			rectangle.add(new Pixel(curBest.x + ServerWorker.SUB_IMAGE_WIDTH, curBest.y + ServerWorker.SUB_IMAGE_HEIGHT));
     			target.drawShape(new Polygon(rectangle).calculateRegularBoundingBox(), 3,RGBColour.BLACK);
     			target.drawText(Integer.toString(j), curBest.x - 10, curBest.y - 10, new GeneralFont("Courier", Font.BOLD), 26, RGBColour.BLACK);
     		}
     		
     		// Save the image locally
     		int[] imageBytes = target.toPackedARGBPixels();
     		BufferedImage resultImage = new BufferedImage(target.getWidth(), target.getHeight(), BufferedImage.TYPE_INT_RGB);
     		resultImage.setRGB(0, 0, target.getWidth(), target.getHeight(), imageBytes, 0, target.getWidth());
     		try {
     			mLocalImgResPath = mSCoord.mLocalBasePath+ "/" +WALDO_RESULT_IMAGE;
 				ImageIO.write(resultImage, "jpg", new File(mLocalImgResPath));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
     		
     		//Signal job done
     		mSCoord.mClientJobDone = true;
 
         }
 		
 		private static boolean overlappingResult(MatchCandidate curBest, List<MatchCandidate> curCandidates) {
 			for (MatchCandidate curCandidate : curCandidates) {
 				if (curCandidate.overlaps(curBest)) {
 					return true;
 				}
 			}
 			return false;
 		}
     }
 
 		
 	//Job-item class
 	 private class JobItem{
 		//Data members
 		public int mWID; //Worker server to which this item is assigned to.
 		public int mFeatMatched; //Number of features matched
 		public int x, y; //X and Y coordinates of sub-section of the image.
 		
 		//Status of the job-item. Values: NP(notProcessed), P(processing), or D(done)
 		public String mStatus; 
 		
 		public JobItem(int inX, int inY){
 			mFeatMatched = 0;
 			mWID = -1;
 			mStatus = "NP";
 			x = inX;
 			y = inY;
 		}
 		
 	}
 	
 				
 }
 
 
