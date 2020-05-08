 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 public class MotionIndexer {
 	
     int height = 288;
     int width = 352;
     int numFrames = 720;
 	
 	public static void main(String[]args) {
 		MotionIndexer mi = new MotionIndexer();
 	}
 	
 	public MotionIndexer() {
 
 		
 		String[] filenames = new String[12];
 		filenames[0] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo1/vdo1.rgb";
 		filenames[1] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo2/vdo2.rgb";
 		filenames[2] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo3/vdo3.rgb";
 		filenames[3] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo4/vdo4.rgb";
 		filenames[4] = "C:/Users/Cauchy/Documents/CSCI576/project/vdo5/vdo5.rgb";
 		filenames[5] = "C:/Users/Cauchy/Documents/CSCI576/project/vdo6/vdo6.rgb";
 		filenames[6] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo7/vdo7.rgb";
 		filenames[7] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo8/vdo8.rgb";
 		filenames[8] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo9/vdo9.rgb";
 		filenames[9] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo10/vdo10.rgb";
 		filenames[10] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo11/vdo11.rgb";
 		filenames[11] = "C:/Users/Cauchy/Documents/CSCI576/Project/vdo12/vdo12.rgb";
 		
 		try{
 			FileWriter fw = new FileWriter("motionindex.txt");
 			BufferedWriter bw = new BufferedWriter(fw);
 			
 			for (int j = 0; j < 12; j++){
 				ArrayList<Macroblock> macroBlocks = new ArrayList<Macroblock>();
 				
 				File file = new File(filenames[j]);
 				bw.write("" + j + " ");
 			    InputStream is = new FileInputStream(file);
 			    long len = file.length();
 			    byte[] bytes = new byte[(int)len];
 			    
 			    int offset = 0;
 		        int numRead = 0;
 		        
 		        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
 		            offset += numRead;
 		        }
 			   
 		    	int ind = 0;
 		    	for (int z = 0; z < numFrames; z++) {
 		    		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 					for(int y = 0; y < height; y++){
 				
 						for(int x = 0; x < width; x++){
 					 
 							byte a = 0;
 							byte r = bytes[ind];
 							byte g = bytes[ind+height*width];
 							byte b = bytes[ind+height*width*2]; 
 							
 							//System.out.println(r + " " + g + " " + b);
 							
 							int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
 							//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
 							img.setRGB(x,y,pix);
 							
 							ind++;
 						}
 					}
 					
 					for (int y = 16; y < height; y+=16) {
 						for (int x = 16; x < width; x+=16) {
 							Macroblock mb = new Macroblock(img, x, y);
 							macroBlocks.add(mb);
 						}
 					}
 					//System.out.print(z + " ");
 					ind = width*height*3*z;
 //					System.out.println(ind);
 //					System.exit(1);
 		    	}
 		    	
 		    	
		    	int mbOffset = ((width/16)-1) * ((height/16)-1);
 		    	double maxDiff = 0;
 		    	double diffSum = 0;
 		    	double maxAverage = 0;
 		    	
 		    	System.out.println("num macroBlocks: " + macroBlocks.size());
 		    	
 		    	for (int i = 0; i < macroBlocks.size(); i++) {
 		    		if (mbOffset+i < macroBlocks.size()-1) {
 		    			double compare = macroBlocks.get(i).compareTo(macroBlocks.get(mbOffset+i));
 		    			//System.out.println(compare);
 		    			diffSum += compare;
 		    			
 		    			if (compare > maxDiff) {
 		    				maxDiff = compare;
 		    			}
 		    		}
 		    		if (i % mbOffset == 0) {
 		    			if (diffSum/mbOffset > maxAverage)
 		    				maxAverage = diffSum/mbOffset;
 		    			//System.out.println((int)(diffSum/mbOffset/62));
		    			bw.write(""+ (int)((diffSum / mbOffset)/64) + " ");
 		    			diffSum = 0;
 		    		}
 		    	}
 		    	System.out.println("maxAverage: " + maxAverage);
 		    	//System.out.println("maxDiff: " + maxDiff);
 		    	bw.write("\n");
 		    	System.out.println("Finished " + j + "th video");
 		    	maxAverage = 0;
 			}
 			bw.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
 }
