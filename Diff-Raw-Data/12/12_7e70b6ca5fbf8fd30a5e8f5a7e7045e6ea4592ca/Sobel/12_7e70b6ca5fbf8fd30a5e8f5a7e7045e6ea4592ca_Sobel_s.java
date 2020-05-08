 package com.sobelextractor;
 
 import java.awt.image.PixelGrabber;
 import java.lang.Math;
 import java.util.Vector;
 import javax.media.jai.PlanarImage;
 import javax.media.jai.JAI;
 
 public class Sobel {
 
 		int[] input;
 		int[] output;
 		float[] template={-1,0,1,-2,0,2,-1,0,1};
 		int templateSize=3;
 		int width;
 		int height;
 		double[] direction;
 		
 		float Gx, Gy, dir, magn, sum;
 		int i,j,h;
 		int DIM = 50;
 		float [] hist;
 		Vector<Float> ranges;
 
 		public Sobel(String fileName) {
 			
 			PlanarImage src = JAI.create("fileload", fileName);
 			width = src.getWidth();
 			height = src.getHeight();
 			
 			input = new int[width*height];			
 			output = new int[width*height];
 			direction = new double[width*height];
 
 			PixelGrabber grabber = new PixelGrabber(src.getAsBufferedImage(), 0, 0, width, height, input, 0, width);
 			try {
 				grabber.grabPixels();
 			} catch(InterruptedException e2) {
 				System.out.println("error: " + e2);
 			}
 			init();
 			process();
 		}
 
 		
 		private void init() {
 			hist = new float[DIM+1];
 			for (i=0; i<DIM+1; i++) hist[i] = 0;
 			ranges = new Vector<Float>(DIM+1);
 			
 			for (i=0; i<DIM; i++) {
 				ranges.add(new Float((float)i*3.14159265/(float)DIM - 1.57079633));
 			}
 	
 			DIM = DIM + 1;
 			ranges.add(Float.NaN);
 		}
 
 		public void process() {
 			float[] GY = new float[width*height];
 			float[] GX = new float[width*height];
 			
 			//Convolution
 			for(int x=(templateSize-1)/2; x<width-(templateSize+1)/2;x++) {
 				for(int y=(templateSize-1)/2; y<height-(templateSize+1)/2;y++) {
 					sum=0;
 
 					for(int x1=0;x1<templateSize;x1++) {
 						for(int y1=0;y1<templateSize;y1++) {
 							int x2 = (x-(templateSize-1)/2+x1);
 							int y2 = (y-(templateSize-1)/2+y1);
 							float value = (input[y2*width+x2] & 0xff) * (template[y1*templateSize+x1]);
 							sum += value;
 						}
 					}
 					GY[y*width+x] = sum;
 					for(int x1=0;x1<templateSize;x1++) {
 						for(int y1=0;y1<templateSize;y1++) {
 							int x2 = (x-(templateSize-1)/2+x1);
 							int y2 = (y-(templateSize-1)/2+y1);
 							float value = (input[y2*width+x2] & 0xff) * (template[x1*templateSize+y1]);
 							sum += value;
 						}
 					}
 					GX[y*width+x] = sum;
 
 				}
 			}
 			
 			//Sobel
 			sum = 0.0F;
 			for(int x=20; x<width-40;x++) {
 				for(int y=20; y<height-40;y++) {
 					magn =(float)Math.sqrt(GX[y*width+x]*GX[y*width+x]+GY[y*width+x]*GY[y*width+x]);
 					dir = (float)Math.atan(GY[y*width+x]/GX[y*width+x]);
 					if (Float.isNaN(dir)) hist[DIM-1]++;
 					else {
 						for (h=0; h<DIM-1; h++) {
 							if ((ranges.elementAt(h)).floatValue()  <= dir && (ranges.elementAt(h+1)).floatValue()  > dir) {
 								hist[h]+=magn;
 							} 
 						}
 					}
 				}
 			}
 			
 			//Normalization
 			for (h=0; h<DIM-1; h++) hist[h] = (hist[h]*100.0F)/(float)(width*height);
 			
 		}
 
 		public String getString() {
 			String str = "";
 			for (h=0; h<DIM-1; h++) {
				str += " "+hist[h];
 			}
			return str;	
 		}
 
 	
     public static void main(String[] args) {
         Sobel c = new Sobel(System.getProperty("user.dir")+"/uploads/" + args[0]);
 		System.out.println(""+c.getString());
     }
 
 }
