 /*
  * Main class for Hw1. 
  * 
  */
 public class CS440Hw1 {
 
 	/**
 	 * Entry point for the program
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		try
 		{

 			VideoSink dvs = new VideoSink();
 
 			//Initialize VideoSource
 			ExtVideoSource evs = new ExtVideoSource();
 			evs.setup(dvs,500);			
 
 			ImageMomentsGenerator img = new ImageMomentsGenerator();
 			TemporalDifferenceProcessor tdp = new TemporalDifferenceProcessor();
 			ObjectTracker ot = new ObjectTracker();
 
 			dvs.subscribe(tdp);
 			dvs.subscribe(ot.GetFrameReceiver());
 			tdp.subscribe(img);
 			img.subscribe(ot);
 			
 			//start grab
 			evs.run(ot);
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	static {
         System.loadLibrary("ExtVideoSource");
     }
 	
 	
 }
