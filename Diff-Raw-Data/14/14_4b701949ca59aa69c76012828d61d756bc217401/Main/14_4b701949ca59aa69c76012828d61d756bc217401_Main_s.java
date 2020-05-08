 package kr.hs.sshs.JavaPTS;
 
 import static com.googlecode.javacv.cpp.opencv_core.*;
 import static com.googlecode.javacv.cpp.opencv_imgproc.*;
 import static com.googlecode.javacv.cpp.opencv_highgui.*;
 
 import java.awt.event.KeyEvent;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import com.googlecode.javacv.CanvasFrame;
 import com.googlecode.javacv.FFmpegFrameGrabber;
 import com.googlecode.javacv.FFmpegFrameRecorder;
 import com.googlecode.javacv.FrameGrabber;
 import com.googlecode.javacv.FrameGrabber.Exception;
 import com.googlecode.javacv.FrameRecorder;
 import com.googlecode.javacv.cpp.opencv_core.CvPoint;
 import com.googlecode.javacv.cpp.opencv_core.CvSize;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 
 public class Main {
 	/// Path to store resources
 	private static String PATH = "";
 
 	/// Class instances
 	OpticalFlow opflow;
 	ValueChangeDetect vcd;
 	
 	/// OpenCV Canvases
 	static CanvasFrame canvas1; // Canvas for showing result image
 	static CanvasFrame canvas2; // Canvas for showing original(BW) image
 	static CanvasFrame canvas3;
 	static CanvasFrame canvas4;
 	static CanvasFrame canvas5; //Canvas for Sobel
 	static CanvasFrame canvas6; //Catcher Screen
 	//static CanvasFrame canvas7;
 	//static CanvasFrame canvas8;
 	static CanvasFrame canvas9;
 	//static CanvasFrame canvas10;
 	static CanvasFrame canvas11;
 	static CanvasFrame canvas12;
 
 	/// FFmpeg variables
 	static FrameGrabber grabber;
 	static FrameRecorder recorder;
 
 	/// IplImage variables
 	IplImage imgTmpl;	// template image (RGB)
 	IplImage imgBW;		// template blackwhite image
 	IplImage imgBW_prev;
 	IplImage imgBlob;	// Blob detection image
 	IplImage imgCandidate;	// Candidate image
 	//IplImage imgResult;	// result image
 	IplImage imgBall; //Ball Image
 	IplImage imgSobel; //Sobel Image
 	IplImage imgCropped;
 	IplImage imgTemp;
 	//IplImage imgTemp2;
 	IplImage imgPyrA;	// PREV pyramid
 	IplImage imgPyrB;	// CURR pyramid
 	//IplImage imgMorph;
 	//IplImage imgMorphSobel;
 
 	/// Width and height of original frame
 	static CvSize _size;
 	static CvSize _pyrSize;
 	static int width;
 	static int height;
 	static int cropsize=70;
 
 	/// Current frame number
 	static int framecount = 1;
 
 	/// Flags
 	static char flag_BW = 'x';	// whether to display only BW image
 	static boolean flag_OpflowInitiated = false;
 	static boolean flag_D_Pressed = false;
 
 	/// Indicates whether a candidate is found
 	static boolean balldetermined = false;
 
 	/// HSV colorspace threshold
 	static CvScalar min = cvScalar(0, 0, 180, 0);
 	static CvScalar max = cvScalar(255, 64, 255, 0);
 	final static int ballthresh = 9;
 	int[][] binary;
 
 	/// Stores all Candidate objects
 	List<Candidate>	ballCandidates = new ArrayList<Candidate>();// candidate storage
 	Candidate		detectedball;	
 	static BallInfo	ballfinal = new BallInfo(new CvPoint(cropsize,cropsize));
 	static CvRect	ballcrop;
 	
 	/// Final and initial data of the thrown ball
 	IplImage		imgFinalCaught;
 	int				detectedCandidateSize;
 	CvPoint			caughtBallCtr;		// Center point of the finally caught ball	
 	// IplImages of the frame in which the ball starts to fly;
 	// will store probable imgFT2 and 3 at the same time by packing them into 3-element array
 	List<IplImage>				imgFramesBetweenCatches;	// store all frames between ball catches
 	IplImage					imgFirstThrown;
 	IplImage					imgFirstThrown2;	// imgFirstThrown + 1 frame
 	IplImage					imgFirstThrown3;	// imgFirstThrown + 2 frames
 	// Optical flow data
 	double[]		shiftThrowCatch = {-1, -1};		// -1 means not yet processed
 	
 	/*
 	 * Optimized color threshold examples
 	 *  1. (0,0,180,0),(255,64,255,0)
 	 */
 
 	public static void main(String[] args) throws InterruptedException, Exception, com.googlecode.javacv.FrameRecorder.Exception {
 		System.out.println(
 				"< Key Usage >\n" +
 				"'ESC'	: Escape\n" +
 				"'f'	: Fast forward (x2)\n" +
 				"'TAB'	: Fast forward (x20)\n" +
 				"'j'	: Jump to frame\n" +
 				"'p'	: Print current\n" +
 				"'1'	: Print as \"ssA.jpg\"\n" +
 				"'2'	: Print as \"ssB.jpg\"\n" +
 				"'r'	: Record(append) current\n" +
 				"'d'	: Process\n" +
 				"others	: Bypass processing");
 
 		/// Initialization
 		// Initialize instances
 		Main m = new Main();
 		m.opflow = new OpticalFlow();
 		m.vcd = new ValueChangeDetect();
 		
 		// Initialize canvases
 		//canvas1 = new CanvasFrame("result", CV_WINDOW_AUTOSIZE);
 		canvas2 = new CanvasFrame("blackwhite", CV_WINDOW_AUTOSIZE);
 		canvas3 = new CanvasFrame("ball", CV_WINDOW_AUTOSIZE);
 		canvas4 = new CanvasFrame("Candidates", CV_WINDOW_AUTOSIZE);
 		canvas5 = new CanvasFrame("Sobel", CV_WINDOW_AUTOSIZE);
 		canvas6 = new CanvasFrame("Catcher",CV_WINDOW_AUTOSIZE);
 		//canvas7 = new CanvasFrame("Morphology",CV_WINDOW_AUTOSIZE);
 		//canvas8 = new CanvasFrame("MorphSobel", CV_WINDOW_AUTOSIZE);
 		canvas9 = new CanvasFrame("VCD",CV_WINDOW_AUTOSIZE);
 		canvas11 = new CanvasFrame("FirstThrown",CV_WINDOW_AUTOSIZE);
 		canvas12 = new CanvasFrame("FinalCaught",CV_WINDOW_AUTOSIZE);
 
 		// Initialize FrameRecorder/FrameGrabber
 		recorder = new FFmpegFrameRecorder(PATH + "video/trash.mp4", 640, 480);
 		recorder.setFrameRate(30);
 		recorder.start();
 		grabber = new FFmpegFrameGrabber(PATH + "video/cutting.mp4");
 		grabber.start();
 
 		// Get frame size
 		_size = cvGetSize(grab());
 		m.imgTmpl = cvCreateImage(_size, IPL_DEPTH_8U, 3);
 		cvCopy(grab(), m.imgTmpl);
 		m.imgBW = cvCreateImage(_size, IPL_DEPTH_8U, 1);
 		cvCvtColor(m.imgTmpl,m.imgBW,CV_RGB2GRAY);
 		
 		width = _size.width();
 		height = _size.height();
 		_pyrSize = new CvSize(_size.width()+8, _size.height()/3+1);
 
 		// Initialize IplImages
 		// (DO NOT RELEASE THESE --- intialized only 1 time, reused)
 		m.imgBW_prev = cvCreateImage(_size, IPL_DEPTH_8U, 1);
 		cvCopy(m.imgBW,m.imgBW_prev);
 		m.imgBall = cvCreateImage(_size,IPL_DEPTH_8U,1);
 		cvCopy(grab(), m.imgTmpl);
 		cvCvtColor(m.imgTmpl,m.imgBW,CV_RGB2GRAY);
 
 		m.imgSobel = cvCreateImage(_size, IPL_DEPTH_8U,1);
 		m.imgPyrA = cvCreateImage(_size,IPL_DEPTH_32F,1);
 		m.imgPyrB = cvCreateImage(_size,IPL_DEPTH_32F,1);
 		m.imgBlob = cvCreateImage(_size, IPL_DEPTH_8U, 1);
 		m.imgTemp = cvCreateImage(_size,IPL_DEPTH_8U,1);
 		m.imgCandidate = cvCreateImage(_size, IPL_DEPTH_8U, 1);
 		//m.imgMorphSobel = cvCreateImage(_size, IPL_DEPTH_8U,1);
 		//m.imgCropped = cvCreateImage(_size, IPL_DEPTH_8U,1);
 		//m.imgMorph = cvCreateImage(_size,IPL_DEPTH_8U,1);
 		m.imgFinalCaught = cvCreateImage(_size, IPL_DEPTH_8U, 1);
 		m.imgFirstThrown = cvCreateImage(_size, IPL_DEPTH_8U, 1);
 		m.imgFirstThrown2 = cvCreateImage(_size, IPL_DEPTH_8U, 1);
 		m.imgFirstThrown3 = cvCreateImage(_size, IPL_DEPTH_8U, 1);
 		
 		// Initialize variables used in decision
 		m.caughtBallCtr = new CvPoint(-1, -1);	// -1 means no ball was caught yet
 		m.imgFramesBetweenCatches = new ArrayList<IplImage>();
 		//m.imgFirstThrownWannabes = new ArrayList<ArrayList<IplImage>>();
 
 		while (true) {
 			cvCopy(grab(), m.imgTmpl);
 			cvSmooth(m.imgTmpl, m.imgTmpl, CV_GAUSSIAN, 3);
 			cvCvtColor(m.imgTmpl,m.imgBW,CV_RGB2GRAY);
 			
 			IplImage toAdd = cvCreateImage(_size, IPL_DEPTH_8U, 1);
 			cvCopy(m.imgBW, toAdd);
 			m.imgFramesBetweenCatches.add(toAdd);	// Don't forget to release them all later
 			
 			///
 			/// Looks tiny, is huge
 			m.process();
 			///
 			///
 			
 			//Crop Image Around the Final Ball Point
 			ballcrop = new CvRect(Math.max(ballfinal.x()-cropsize,0), Math.max(ballfinal.y()-cropsize,0), Math.min(2*cropsize,2*(width-ballfinal.x())), Math.min(2*cropsize,2*(height-ballfinal.y())));
 			cvSetImageROI(m.imgSobel, ballcrop);
 			m.imgCropped = cvCreateImage(cvGetSize(m.imgSobel),IPL_DEPTH_8U,1);
 			cvCopy(m.imgSobel,m.imgCropped);
 			cvResetImageROI(m.imgSobel);
 			CatcherDetect.main(m.imgCropped);
 			
 			cvCopy(m.imgBW, m.imgBW_prev);
 
 			canvas2.showImage(m.imgBW);
 			canvas3.showImage(m.imgBall);
 			canvas4.showImage(m.imgCandidate);
 			canvas5.showImage(m.imgSobel);
 			canvas6.showImage(m.imgCropped);
 			//canvas7.showImage(m.imgMorph);
 			//canvas8.showImage(m.imgMorphSobel);
 			canvas9.showImage(m.imgTemp);
 			//canvas10.showImage(m.imgTemp2);
 			canvas11.showImage(m.imgFirstThrown);
 			canvas12.showImage(m.imgFinalCaught);
 
 			System.out.println("############## FRAME " + framecount + " ##############");
 
 			flag_BW = 'x';
 			// Read user key input and do the following
 			KeyEvent key = canvas2.waitKey(0);
 			if (key != null) {
 				if ( key.getKeyChar() == 27 ) {
 					m.cvReleaseAllLoop();
 					break;
 				} else if	(key.getKeyCode() == KeyEvent.VK_TAB) { // FFW 20 frames
 					flag_D_Pressed=false;
 					m.cvReleaseAllLoop();	
 					// pass 19 frame
 					for (int i=0; i<19; i++)
 						grab();
 					continue;
 				} else if (key.getKeyCode() == KeyEvent.VK_P ) { // Take screenshot
 					cvSaveImage(PATH + "screenshot.jpg", m.imgBW);
 					System.out.println("Saved screenshot!");
 				} else if (key.getKeyCode() == KeyEvent.VK_1 ) { // Take screenshot
 					cvSaveImage(PATH + "ssA.jpg", m.imgBW);
 					System.out.println("Saved as ssA.jpg");
 				} else if (key.getKeyCode() == KeyEvent.VK_2 ) { // Take screenshot
 					cvSaveImage(PATH + "ssB.jpg", m.imgBW);
 					System.out.println("Saved as ssB.jpg");
 				} else if (key.getKeyCode() == KeyEvent.VK_R) { // Record frames in an .avi file
 					//recorder.record(m.imgResult);
 				} else if (key.getKeyCode() == KeyEvent.VK_F) { // FFW 2 frames
 					flag_D_Pressed=false;
 					m.cvReleaseAllLoop();	
 					grab();
 				} else if (key.getKeyCode() == KeyEvent.VK_C) {
 					flag_BW = 'c';
 				} else if (key.getKeyCode() == KeyEvent.VK_D) {
 					flag_BW = 'd';
 				} else if (key.getKeyCode() == KeyEvent.VK_J) {	// Go to desired frame
 					flag_D_Pressed=false;
 					m.cvReleaseAllLoop();	
 					moveToFrame();
 				}
 			}
 			// Don't forget to do this!!!
 			m.cvReleaseAllLoop();	
 		}
 
 		// Release resources, dispose grabber/canvas and exit
 		m.cvReleaseAllFinal();
 		recorder.stop();
 		recorder.release();
 		grabber.stop();
 		grabber.release();	
 		canvas2.dispose();
 		canvas3.dispose();
 		canvas4.dispose();	
 		canvas5.dispose();
 		canvas6.dispose();
 		//canvas7.dispose();
 		//canvas8.dispose();
 		canvas9.dispose();
 		//canvas10.dispose();
 		canvas11.dispose();
 		canvas12.dispose();
 		
 		System.out.println("(TERMINATED)");
 	}
 
 	public static IplImage grab() throws com.googlecode.javacv.FrameGrabber.Exception {
 		framecount++;
 		return grabber.grab();
 	}
 
 	public static void pause() throws InterruptedException {
 		canvas2.waitKey(0);
 	}
 
 	public static void moveToFrame() throws com.googlecode.javacv.FrameGrabber.Exception {
 		System.out.print("Move to frame(empty:cancel) : ");
 		Scanner s = new Scanner(System.in);
 		String ui = s.nextLine();
 		s.close();
 		
 		if (!ui.isEmpty()) {
 			int temp = framecount;
 			for (int i=1; i<Integer.parseInt(ui)-temp; i++)
 				grab();
 		}
 	}
 
 	public void loadImage() {
 		imgTmpl = cvLoadImage(PATH + "template.jpg");
 
 		// Check if image is present
 		if (imgTmpl == null) {
 			System.out.println("Failed to load template image!");
 			System.exit(0);
 		}
 	}
 
 	/**
 	* Stretch histogram for more precise thresholding
 	*/
 	public void stretch() {
 		IplImage imgBW = cvCreateImage(_size, IPL_DEPTH_8U, 1);
 		cvCvtColor(imgTmpl, imgBW, CV_RGB2GRAY);
 
 		// CvHistogram hist = new CvHistogram();
 		// cvCalcHist(imgBW, hist, 0, null);
 		// (int) cvGetMinMaxHistValue(hist, min_value, 0, null, null);
 		
 		int temp = 0;
 		for (int y=0; y<height; y++) {
 			for (int x=0; x<width; x++) {
 				temp = (int) ( ((int)cvGetReal2D(imgBW, y, x) - 180.0)*255.0/(255.0-180) );
 				cvSetReal2D(imgBW, y, x, (temp>=0)?temp:0);
 			}
 		}
 
 		//cvCopy(imgBW, imgResult);
 
 		cvReleaseImage(imgBW);
 	}
 
 	/**
 	* Process the image!
 	*/
 	public void process() {
 		width = imgTmpl.width();
 		height = imgTmpl.height();
 
 		cvCvtColor(imgTmpl, imgBW, CV_RGB2GRAY);
 		
 		binary = new int[width][height];
 		
 		Blob_Labeling bl;
 		List<BlobInfo> blobs;
 //		IplImage imgRecovery;
 		
 		cvCanny(imgBW,imgSobel,80,200,3);
 
 		switch (flag_BW) {
 		case 'c' :
 			/// DETECTING VALUE CHANGE
 			vcd.initialize(imgBW_prev, imgBW);
 			binary = vcd.detectChange();
 		break;
 		case 'd' :
 			vcd = new ValueChangeDetect();
 			
 			// Memory management
 			// You'll never want to initialize imgPyrA
 			if(flag_D_Pressed) {
 				imgPyrA = imgPyrB;
 			}
			System.out.println("FLAG:" + flag_D_Pressed);
			boolean isRelated = flag_D_Pressed;
			double[] shift = opflow.processOpticalFlow(imgBW_prev, imgBW, imgPyrA, imgPyrB, isRelated);
 			flag_OpflowInitiated = true;
 			ValueChangeDetect.mX=(int)Math.round(shift[0]);
 			ValueChangeDetect.mY=(int)Math.round(shift[1]);
 			System.out.println(ValueChangeDetect.mX + " and " + ValueChangeDetect.mY);
 			
 			//new PyrA is not needed if you process next time
 			flag_D_Pressed = true;
 			
 			/// DETECTING VALUE CHANGE
 			vcd.initialize(imgBW_prev, imgBW);
 			binary = vcd.detectChange();
 			
 			//Printing SCD Result
 			for(int x = 0; x<width; x++){
 				for(int y = 0; y<height; y++){
 					if(binary[x][y]==0) cvSetReal2D(imgTemp,y,x,0);
 					else cvSetReal2D(imgTemp,y,x,255);
 				}
 			}
 
 			/*
 			/// BLOB CROSSING 
 			// (Only when not bypassed)
 			if (false && flag_BW != 'd') {
 				for (int y=0; y<height; y++) {
 					for (int x=0; x<width; x++) {
 						if (cvGetReal2D(imgCD_prev, y, x) > 245 && cvGetReal2D(imgCD, y, x) > 245) {
 							cvSetReal2D(imgCD, y, x, 255);
 						} else {
 							cvSetReal2D(imgCD, y, x, 0);
 						}
 					}
 				}
 			}
 			// BLOB CROSSING END
 			*/
 
 			/// BLOB LABELING
 			bl = new Blob_Labeling();
 			blobs = bl.detectBlob(binary, width, height);// DETECT BLOB
 			binary = bl.print;
 			
 			//Print Blobs After Simple Blob filtering
 			for(int x = 0; x<width; x++){
 				for(int y = 0; y<height; y++){
 					if(binary[x][y]==0) cvSetReal2D(imgTemp,y,x,0);
 					else cvSetReal2D(imgTemp,y,x,255);
 				}
 			}
 			/// BLOB LABELING END
 
 			///
 			/// BLOB FILTERING
 			if(!balldetermined)
 				blobFiltering(blobs, 4);
 			///
 			///
 
 			/// CANDIDATE PROCESS START
 			// Adding new blobs into existing Candidates --
 			// get each Candidate, add a new center at the end of it,
 			// and then put it onto the top of the ballCandidates
 			// 'ballCandidates' AND 'imgFirstThrownWannabies' SHOULD ALWAYS BE INDEX-MATCHED			
 			
 			for (int q = ballCandidates.size()-1; q>=0; q--) {
 				Candidate cc = new Candidate(ballCandidates.get(q));
 				
 				boolean addedBlob = false; // Indicates whether any blob is added to Candidate cc 
 				for (BlobInfo blob : blobs) { // FOUND BLOB
 					
 					if (cc.xROImin() < blob.xcenter() && cc.xROImax() > blob.xcenter() && cc.yROImin()<blob.ycenter() && cc.yROImax() > blob.ycenter()) { //ROI Thresholding
 						//System.out.println("Appending!!!!!!!!!!! in Candidate" + q);
 						if (cc.centers.get(cc.centers.size()-1).pixelcount<40 || cc.countmin() < blob.count && cc.countmax() > blob.count) { //Size Thresholding : if blob is small, no application of size threshold
 							//if(cc.disturbed==2)
 								//cc.disturbed=0;
 							//else if(cc.disturbed==1)
 							// cc.disturbed++;
 							cc.numOfMissingBlobs = 0;
 							addedBlob = true;
 							ballCandidates.add(new Candidate(cc));
 							//System.out.println("THERE ARE " + ballCandidates.size() + " CANDIDATES");
 							ballCandidates.get(ballCandidates.size()-1).add(new BallInfo(new CvPoint(blob.xcenter(),blob.ycenter()),blob.count));
 						}
 					}
 				}
 
 				if (!addedBlob) { // NOT FOUND BLOB					
 					if (balldetermined) { //If this ball is determined
 						if(cc.numOfMissingBlobs > 0){// If ball is considered to be caught
 							if(ballCandidates.size()==1){
 								cc.centers.remove(cc.centers.size() - 1);
 								detectedball = new Candidate(cc);
 								drawBall();
 								System.out.println("BALL WAS CAUGHT /nf");
 								//System.out.println("The Speed of Pitch is " + 1503/detectedball.centers.size() + "km/h");
 								ballfinal=detectedball.centers.get(detectedball.centers.size()-1); // last ball in the "elected" ball candidate
 								detectedCandidateSize = detectedball.centers.size();
 								
 								// Hookup of Strike-Ball
 								preprocessingStrikeBall();
 								
 								ValueChangeDetect.v_thresh=350;
 								ValueChangeDetect.singlethresh=40;
 								balldetermined=false;
 								
 							}
 						}
 						else {
 							// Do nothing, let this blob get removed (not added)
 							ballCandidates.add(new Candidate(cc)); // auto-updated
 							ballCandidates.get(ballCandidates.size()-1).addMissed();
 						}
 						
 					} else {
 						// Non-ball candidate blob jumping : Do nothing, let this blob removed (not added)
 						//System.out.println("Candidate deletion by missing blob");			
 					}
 				}
 
 				ballCandidates.remove(q); // Remove original Candidate
 			}
 			
 			if(ballCandidates.size()==0){
 				balldetermined=false;	// Of course!
 			}
 			
 			for (Candidate cc : ballCandidates) {
 				if(cc.centers.size()>=ballthresh){
 					balldetermined=true;
 					cc.survive=true;
 				}
 			}
 			if(balldetermined){
 				for (int q = ballCandidates.size()-1; q>=0; q--) {
 					if(!ballCandidates.get(q).survive){
 						ballCandidates.remove(q);
 					}
 				}
 				
 				int x1=Math.max(0,ballCandidates.get(0).xROImin());
 				int x2=Math.min(width-1,ballCandidates.get(0).xROImax());
 				int y1=Math.max(0,ballCandidates.get(0).yROImin());
 				int y2=Math.min(height-1,ballCandidates.get(0).yROImax());
 				
 				int avg = ValAverage(new CvPoint(x1,y1), new CvPoint(x2,y2), imgBW);
 				ValueChangeDetect.singlethresh = (255-avg)/8;
 				ValueChangeDetect.v_thresh = (255-avg);
 				
 				//System.out.println("BALL IS DETERMINED");
 			}
 			
 			// Finding the FIRST ball
 			if(!balldetermined){
 				for (BlobInfo blob : blobs) {
 					if (blob.count>=45) {
 						ballCandidates.add(new Candidate(blob)); //New Candidate
 						//System.out.println("NEW CANDIDATE WAS CREATED");
 					}
 				}
 			}
 
 			//candidateLengthCheck();
 			if(balldetermined)
 				ballJumpingCheck();
 			
 			//System.out.println("THERE ARE " + ballCandidates.size() + " CANDIDATES NOW"); //Print Candidate Number
 			drawCandidate(); //Create IplImage for view
 			/// CANDIDATE PROCESS END
 
 			/// BLOB STAMPING
 			// Stamp blobs list onto imgRecovery
 //			// (Doesn't need any IplImage variable)
 //			imgRecovery = cvCreateImage(_size, IPL_DEPTH_8U, 1);
 //
 //			for (int y = 0; y<height; y++) {
 //				for (int x = 0; x<width; x++) {
 //					cvSetReal2D(imgRecovery, y, x, 0);
 //				}
 //			}
 //
 //			/*for (Info i : blobs) {
 //				for (CvPoint p : i.points) {
 //					// System.out.println("Point : " + p.x() + ", " + p.y());
 //					cvSetReal2D(imgRecovery, p.y(), p.x(), 255);
 //				}
 //			}*/
 //
 //			// cvCopy(imgRecovery, imgCD);
 //			cvCopy(imgRecovery, imgResult);
 //			cvReleaseImage(imgRecovery);
 			/// BLOB STAMPING END			
 		break;
 
 		default : // Do nothing
 			//cvCopy(imgBW, imgResult);
 		break;
 		}
 		// Check Blob Detecting -- end
 	}
 	
 	/**
 	* BLOB FILTERING
 	* Search for points in the square box near a blob --
 	* if there is any, that blob is not likely the ball.
 	* @param blobs Blobs list
 	* @param adjBlobNumThreshold Minimum number of found adjacent blobs required to remove current blob.
 	*/
 	public void blobFiltering(List<BlobInfo> blobs, int adjBlobNumThreshold) {
 
 		// Thickness of the searching box, wrapping around each blob
 		// (set 0 for testing)
 		int boxThickness = 20;
 
 		//int currentLabel = 0; // Label of the current searching blob
 		for (int i = blobs.size() - 1; i > 0 ; i--) { // CAUTION -- No element in blobs.get(0) (background)
 			if (blobs.size() > 0) {
 				// System.out.println("Searching blob number " + (i+1) + "...");
 
 				BlobInfo currentBlob = blobs.get(i);
 				int x = currentBlob.xcenter();
 				int y = currentBlob.ycenter();
 
 				int boxwidth = boxThickness*2 + currentBlob.bwidth();
 				int boxheight = boxThickness*2 + currentBlob.bheight();
 
 				/*
 				System.out.println("WIDTH	: " + currentBlob.bwidth() + "\nHEIGHT	: " + currentBlob.bheight());
 				System.out.println("SIZE	: " + currentBlob.count);
 				System.out.println("POS	: (" + x + ", " + y + ")");
 				*/
 
 				// Remove the current blob, to get it out of the way
 				//currentLabel = i;	
 
 				// Searching inside the box
 				List<Integer> detectedLabel = new ArrayList<Integer> (); // Labels detected inside the box
 				for (int yi = y - boxheight/2; yi < y + boxheight/2; yi++) {
 					for (int xi = x - boxwidth/2; xi < x + boxwidth/2; xi++) {
 						int ysearch = (int)((yi<0)?Math.max(yi,0):Math.min(yi,height-1));
 						int xsearch = (int)((xi<0)?Math.max(xi,0):Math.min(xi,width-1));
 
 						// int ysearch = 50;
 						// int xsearch = -1;
 						// System.out.println("Searching (" + xsearch + ", " + ysearch + ")");
 						int label = binary[xsearch][ysearch]; // Lable of the current searching point
 						if (label > 0) {
 							boolean alreadyDetected = false;
 
 							for (Integer l : detectedLabel) {
 								if (label == l) {
 									alreadyDetected = true;
 								}
 							}
 
 							if (!alreadyDetected) {
 								detectedLabel.add(label);
 							}
 						}
 					}
 				}
 
 				if (detectedLabel.size() >= adjBlobNumThreshold) {
 					blobs.remove(i);
 				} else {
 				}
 
 				// Recover the current blob for the next search,
 				// using its own original label (currentLabel)
 			}
 		}
 	}
 
 	/**
 	* Draw centers in all Candidate objects.
 	*/
 	public void drawCandidate() {
 		// Reset to black screen
 		for (int i = 0; i < imgCandidate.width(); i++) {
 			for (int j = 0; j < imgCandidate.height(); j++) {
 				cvSetReal2D(imgCandidate,j,i,0);
 			}
 		}
 		
 		for (int k = 0; k < ballCandidates.size(); k++) {
 			for (BallInfo pt : ballCandidates.get(k).centers) {
 				for (int i = -1; i <= 1; i++) {
 					for (int j = -1; j <= 1; j++) {
 						int ydraw = (int)((pt.ctr.y()+i<0)?Math.max(pt.ctr.y()+i,0):Math.min(pt.ctr.y()+i,height-1));
 						int xdraw = (int)((pt.ctr.x()+j<0)?Math.max(pt.ctr.x()+j,0):Math.min(pt.ctr.x()+j,width-1));
 						
 						cvSetReal2D(imgCandidate, ydraw, xdraw, 255);
 					}
 				}
 			}
 		
 			// Draw ROI box`
 			Candidate cc = ballCandidates.get(k);
 			int xl = (int)((cc.xROImin()<0)?Math.max(cc.xROImin(),0):Math.min(cc.xROImin(),width-1));
 			int xr = (int)((cc.xROImax()<0)?Math.max(cc.xROImax(),0):Math.min(cc.xROImax(),width-1));
 			int yu = (int)((cc.yROImax()<0)?Math.max(cc.yROImax(),0):Math.min(cc.yROImax(),height-1));
 			int yd = (int)((cc.yROImin()<0)?Math.max(cc.yROImin(),0):Math.min(cc.yROImin(),height-1));
 			
 			for(int i = xl;i<=xr;i++){
 				cvSetReal2D(imgCandidate,yu,i,255);
 				cvSetReal2D(imgCandidate,yd,i,255);
 			}
 			for(int i = yd;i<=yu;i++){
 				cvSetReal2D(imgCandidate,i,xl,255);
 				cvSetReal2D(imgCandidate,i,xr,255);
 			}
 			//cvSetReal2D(imgCandidate,penpoint,255);
 		}
 	}
 	
 	public void drawBall() {
 		// Reset to black screen
 		for (int i = 0; i < imgBall.width(); i++) {
 			for (int j = 0; j < imgBall.height(); j++) {
 				cvSetReal2D(imgBall,j,i,0);
 			}
 		}
 		
 		for (BallInfo pt : detectedball.centers) {
 			for (int i = -1; i <= 1; i++) {
 				for (int j = -1; j <= 1; j++) {
 					int ydraw = (int)((pt.ctr.y()+i<0)?Math.max(pt.ctr.y()+i,0):Math.min(pt.ctr.y()+i,height-1));
 					int xdraw = (int)((pt.ctr.x()+j<0)?Math.max(pt.ctr.x()+j,0):Math.min(pt.ctr.x()+j,width-1));
 					
 					cvSetReal2D(imgBall, ydraw, xdraw, 255);
 				}
 			}
 		}
 	}
 	
 	
 	
 	/**
 	* Check each Candidates' length, and remove too short ones
 	*/
 	public void candidateLengthCheck() {
 		for (int i = ballCandidates.size()-1; i>=0; i--) {
 			Candidate cd = ballCandidates.get(i);
 			int frames = cd.centers.size(); //Candidate elapsed frame
 			if (frames>=3 && frames<=6) {
 				int ruler = Math.abs(cd.centers.get(frames-1).ctr.x()-cd.centers.get(frames-2).ctr.x());
 				if (Math.abs(cd.centers.get(0).ctr.x()-cd.centers.get(frames-1).ctr.x()) < (frames-1)*ruler*0.9) { //If Track of Candidate is not long enough along the x axis
 					ballCandidates.remove(i); //Delete the Candidate
 					//System.out.println("SHORT CANDIDATE WAS REMOVED");
 				}
 			}
 		}
 	}
 	
 	public void ballJumpingCheck() {
 
 		for (int i = ballCandidates.size() - 1; i >= 0; i--) {
 			boolean caught = false;
 			Candidate cd = ballCandidates.get(i);
 			int frames = cd.centers.size(); // Candidate elapsed frame
 			int lastxmove = cd.centers.get(frames - 1).ctr.x() - cd.centers.get(frames - 2).ctr.x();
 			int prevxmove = cd.centers.get(frames - 2).ctr.x() - cd.centers.get(frames - 3).ctr.x();
 			int lastymove = cd.centers.get(frames - 1).ctr.y() - cd.centers.get(frames - 2).ctr.y();
 			int prevymove = cd.centers.get(frames - 2).ctr.y() - cd.centers.get(frames - 3).ctr.y();
 			double angmove = Math.abs(Math.atan2(lastymove,lastxmove)-Math.atan2(prevymove,prevxmove));
 			if(angmove>Math.PI)
 				angmove=2*Math.PI-angmove;
 			//if(Math.atan2(prevymove,prevxmove)>Math.PI /*&& cd.disturbed==0*/){
 				if(angmove>Math.PI/6){
 					caught = true;
 				//System.out.println("ang");
 				}
 			//}
 			if(!caught /*&& cd.disturbed==0*/){
 				if (prevxmove * lastxmove < 0) {
 					if (Math.abs(lastxmove) > Math.abs(prevxmove)) {
 						caught = true;
 						//System.out.println("minus");
 					}
 				}
 				if (prevxmove * lastxmove > 0) {
 					if (Math.abs(lastxmove) >= (Math.max(Math.abs(prevxmove)*2 , 4))) {
 						caught = true;
 						//System.out.println("plus");
 					}
 				}
 				else if (prevxmove == 0) {
 					if (Math.abs(lastxmove) >= 4) {
 						caught = true;
 						//System.out.println("zero");
 					}
 				}
 			}
 			
 			if (caught) {
 				if (ballCandidates.size() == 1) {
 					ballCandidates.get(0).centers.remove(ballCandidates.get(0).centers.size() - 1);
 					detectedball = new Candidate(ballCandidates.get(0));
 					drawBall();
 					System.out.println("BALL WAS CAUGHT /j");
 					//System.out.println("The Speed of Pitch is " + 1080/detectedball.centers.size() + "km/h");
 					ballfinal=detectedball.centers.get(detectedball.centers.size()-1);
 					detectedCandidateSize = detectedball.centers.size();
 					
 					/// Another place where ball is caught
 					preprocessingStrikeBall();
 					
 					ballCandidates.remove(0);
 					ValueChangeDetect.v_thresh=350;
 					ValueChangeDetect.singlethresh=40;
 					balldetermined=false;
 					
 				}
 				else {
 					ballCandidates.remove(i);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Pre-process for Strike-Ball decision.<br>
 	 * - Find final ball position<br>
 	 * - Store first and last frame of ball flying<br>
 	 * - Store 2 more frames right after the first frame, for accurate catcher detection
 	 * @return Background shift between the ball-thrown frame and the ball-caught frame
 	 */
 	public double[] preprocessingStrikeBall() {
 		// Store the last state of the ball
 		cvCopy(imgBW, imgFinalCaught);
 		caughtBallCtr = ballfinal.ctr;
 		// Find ball-thrown frame, "+1, "+2
 		int framesago = detectedCandidateSize+1;	// assume there was NOT a ball jumping at the end
 													// (therefore last ball caught was currframe-2
 		int thrownFrameIndex = (imgFramesBetweenCatches.size()-1) - framesago;
 		cvCopy(imgFramesBetweenCatches.get(thrownFrameIndex),imgFirstThrown);
 		cvCopy(imgFramesBetweenCatches.get(thrownFrameIndex+1),imgFirstThrown2);
 		cvCopy(imgFramesBetweenCatches.get(thrownFrameIndex+2),imgFirstThrown3);
 		
 		// Clear frames list
 		for (IplImage il : imgFramesBetweenCatches)	cvReleaseImage(il);
 		imgFramesBetweenCatches.clear();
 		
		IplImage imgPyrA, imgPyrB;	// Use separate pyramid values, to avoid contaminating video optical flow
		imgPyrA = cvCreateImage(_pyrSize, IPL_DEPTH_32F, 1);
		imgPyrB = cvCreateImage(_pyrSize, IPL_DEPTH_32F, 1);
		double[] shift = opflow.processOpticalFlow(imgFirstThrown, imgFinalCaught, imgPyrA, imgPyrB, false);
		cvReleaseImage(imgPyrA);
		cvReleaseImage(imgPyrB);
 		System.out.println("xshift: " + shift[0]);
 		System.out.println("yshift: " + shift[1]);
 		shiftThrowCatch = shift;
 		return shift;
 	}
 
 	/**
 	* Release all redundant resources <b>at the end of the loop.</b>
 	*/
 	public void cvReleaseAllLoop() {
 		// DO NOT RELEASE imgBW (stored in imgFirstThrownWannabies)
 		// Don't release *_prevs!
 		cvReleaseImage(imgCropped);
 	}
 	
 	/**
 	* Release all redundant resources <b>at the termination of the program.</b>
 	*/
 	public void cvReleaseAllFinal() {
 		cvReleaseImage(imgTmpl);
 		cvReleaseImage(imgBW);
 		cvReleaseImage(imgBW_prev);
 		cvReleaseImage(imgBlob);
 		cvReleaseImage(imgCandidate);
 		//cvReleaseImage(imgResult);
 		cvReleaseImage(imgBall);
 		cvReleaseImage(imgSobel);
 		cvReleaseImage(imgCropped);
 		cvReleaseImage(imgTemp);
 		//cvReleaseImage(imgTemp2);
 		cvReleaseImage(imgPyrA);
 		cvReleaseImage(imgPyrB);
 		cvReleaseImage(imgFinalCaught);
 		cvReleaseImage(imgFirstThrown);
 		cvReleaseImage(imgFirstThrown2);
 		cvReleaseImage(imgFirstThrown3);
 	}
 	
 	public String doubleArrayToString(double[] ds) {
 		String result = "";
 		for(int i=0; i<ds.length; i++) {
 			result += ds[i] + " ";
 		}
 		return result;
 	}
 	
 	public int ValAverage(CvPoint a, CvPoint b, IplImage bw){
 		
 		int sum=0;
 		for(int x=a.x(); x<b.x()+1; x++){
 			for(int y=a.y(); y<b.y()+1; y++){
 				sum+=cvGetReal2D(bw, y, x);
 			}
 		}
 		
 		return (int) (sum/((b.x()-a.x()+1)*(b.y()-a.y()+1)));
 	}
 }
