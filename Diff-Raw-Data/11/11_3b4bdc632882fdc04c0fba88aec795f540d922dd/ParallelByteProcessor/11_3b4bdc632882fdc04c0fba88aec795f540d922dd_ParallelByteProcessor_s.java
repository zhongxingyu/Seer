 package ij.process;
 
 import ij.Prefs;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.util.Random;
 
 /**
  * Class designed to implement ByteProcessor filters in parallel using a variety of approaches.
  * 
  * @author Sina Masoud-Ansari
  *
  */
 public class ParallelByteProcessor extends ByteProcessor {
 	
 	public enum Type {SERIAL, SIMPLE, FORK_JOIN, EXECUTOR, PARALLEL_TASK}
 	private Type type;
 
 	public ParallelByteProcessor(Image img, Type t) {
 		super(img);
 		type = t;
 	}
 
 	public ParallelByteProcessor(int width, int height, Type t) {
 		super(width, height);
 		type = t;
 	}
 
 	public ParallelByteProcessor(int width, int height, byte[] pixels, Type t) {
 		super(width, height, pixels);
 		type = t;
 	}
 
 	public ParallelByteProcessor(int width, int height, byte[] pixels, ColorModel cm, Type t) {
 		super(width, height, pixels, cm);
 		type = t;
 	}
 
 	public ParallelByteProcessor(BufferedImage bi, Type t) {
 		super(bi);
 		type = t;
 	}
 
 	public ParallelByteProcessor(ImageProcessor ip, boolean scale, Type t) {
 		super(ip, scale);
 		type = t;
 	}
 	
 	@Override
 	public void noise(double range, String t){
 		switch (type) {
 			case SERIAL:
 				serial_noise(range);
 				break;
 			case SIMPLE:
 				simple_noise(range);
 				break;
 		}
 	}
 	
     public void serial_noise(double range) {
 		Random rnd=new Random();
 		int v, ran;
 		boolean inRange;
 		for (int y=roiY; y<(roiY+roiHeight); y++) {
 			int i = y * width + roiX;
 			for (int x=roiX; x<(roiX+roiWidth); x++) {
 				inRange = false;
 				do {
 					ran = (int)Math.round(rnd.nextGaussian()*range);
 					v = (pixels[i] & 0xff) + ran;
 					inRange = v>=0 && v<=255;
 					if (inRange) pixels[i] = (byte)v;
 				} while (!inRange);
 				i++;
 			}
 			if (y%20==0)
 				showProgress((double)(y-roiY)/roiHeight);
 		}
 		showProgress(1.0);
     }	
 
     // TODO: There are two problems here:
    // 			1. There are linear artificats when the underlying image is not white
     //			2. It is about 2x slower than the serial version
     public void simple_noise(double r) {	
 		final double range = r;	
 		//Divide the number of rows by the number of threads
 		int numThreads = Math.min(roiHeight, Prefs.getThreads());
 		int ratio = roiHeight / numThreads;
 		Thread[] threads = new Thread[numThreads];
 		for (int i = 0; i < numThreads; i++){
 			final int yIndex = i;
 			final int numRowsPerThread;
 			if ( (i == numThreads - 1)){
 				// add an additional row for the last thread if the roiY is not a multiple of numThreads
 				numRowsPerThread = roiY % numThreads == 0 ? ratio : ratio + 1;
 			} else {
 				numRowsPerThread = ratio;
 			}
 			threads[i] = new Thread(new Runnable(){
 				@Override
 				public void run() {
 					int yStart = roiY+yIndex*numRowsPerThread;
 					int yLimit = yStart + numRowsPerThread;
 					int xEnd = roiX + roiWidth;
 					// for each row
 					for (int y = yStart; y < yLimit; y++){
 						// process pixels in ROI
 						for (int x = roiX; x < xEnd; x++){
 							// pixels is a 1D array so need to map to it
 							int p = y * roiWidth + roiX + x;
 							Random rnd = new Random();
 							int v, ran;
							boolean inRange = false;
							while (!inRange){
 								ran = (int)Math.round(rnd.nextGaussian()*range);
 								v = (pixels[p] & 0xff) + ran;
 								inRange = v>=0 && v<=255;
 								if (inRange) pixels[p] = (byte)v;								
							}					
 						} // end x loop
 						if (y%20==0) {
 							showProgress((double)(y-roiY)/roiHeight);
 						}
 					} // end y loop
 				} // end run				
 			}); // end new thread definition
 		}
 		// start threads
 		for (Thread t : threads){
 			t.start();
 		}
 		// wait for threads to finish
 		for (Thread t : threads){
 			try {
 				t.join();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		// indicate processing is finished
 		showProgress(1.0);
     }	
 	
 }
