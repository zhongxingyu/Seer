 import java.awt.image.BufferedImage;
 import java.awt.image.Raster;
 
 /*
  * Job of class Texture Histogram
  * Index image into histogram using texture
  * with edge direction histogram technique
  */
 public class TextureHistogram {
 	
 	static int dim = 64;
 
 	public static double[] getTextureHistogram(BufferedImage image) {
 		int imHeight = image.getHeight();
         int imWidth = image.getWidth();
         double[] bins = new double[24]; //for 8 directions there will be 8x3 bins for 3 colors 
         int step = 256 / dim;
         Raster raster = image.getRaster();
 
         int[][][] input = new int[3][imHeight][imWidth];
         
         for(int i = 0; i < imWidth; i++) {
             for(int j = 0; j < imHeight; j++) {
             	// rgb->ycrcb
             	int r = raster.getSample(i,j,0);
             	int g = raster.getSample(i,j,1);
             	int b = raster.getSample(i,j,2);
             	int y  = (int)( 0.299   * r + 0.587   * g + 0.114   * b);
         		int cb = (int)(-0.16874 * r - 0.33126 * g + 0.50000 * b);
         		int cr = (int)( 0.50000 * r - 0.41869 * g - 0.08131 * b);
         		
         		int ybin = y / step;
         		int cbbin = cb / step;
         		int crbin = cr / step;
         		
         		input[0][j][i] = ybin;
         		input[1][j][i] = cbbin;
         		input[2][j][i] = crbin;     	           	
             }
         }
         
         //Apply filters one by one
         for (int i=0; i<8; i++) {
             int[][][] output = new int[3][imHeight][imWidth];
             
             //Apply to all 3 color channels
             for (int j=0; j<3; j++) {
             	output[j] = filterMatrix(input[j],i);
             }
         	
             //Iterate through all the pixels in the image
             for (int u=0; u<imHeight; u++) {
             	for (int v=0; v<imWidth; v++) {
             		//if there is an edge (pixel value is non negative)
             		if (output[0][u][v] > 0) {
                         bins[i] ++; 
                         //not sure if last one should be multiplied by 8
             		}
             		if (output[1][u][v] > 0) {
                         bins[i+7] ++; 
                         //not sure if last one should be multiplied by 8
             		}
             		if (output[2][u][v] > 0) {
                         bins[i+15] ++; 
                         //not sure if last one should be multiplied by 8
             		}
             	}
             }
         }
 		
 		return bins;	
 	}
 	
 	// Applies a filter to the input image
 	public static int[][] filterMatrix(int[][] input, int filter_type) {		
 		int[][] output = new int[input.length+4][input[0].length+4];
 		
 		//based on filter_type, a different filter number will be assigned: first value is filter_type
 		int[][][] filter  = {
 				//Values taken from: http://www2.dis.ulpgc.es/~maleman/PDF/ScaleSpace.pdf
 				//Modified Newton filters and corresponding orientation
 				{	{1,1,-2},
 					{2,2,-4},
 					{1,1,-2}
 				}, //F0: 0
 				{	{1,-2,-4},
 					{1,2,-2},
 					{2,1,1}
 				}, //F1: pi/4
 				{	{-2,-4,-2},
 					{1,2,1},
 					{1,2,1}
 				}, //F2: pi/2
 				{	{-4,-2,1},
 					{-2,2,1},
 					{1,1,2}
 				}, //F3: 3pi/4
 				{	{-2,1,1},
 					{-4,2,2},
 					{-2,1,1}
 				}, //F4: pi
 				{	{1,1,2},
 					{-2,2,1},
 					{-4,-2,1}
 				}, //F5: 5pi/4
 				{	{1,2,1},
 					{1,2,1},
 					{-2,-4,-2}
 				}, //F6: 3pi/2
 				{	{2,1,1},
 					{1,2,-2},
 					{1,-2,-4}
 				}, //F7: 7pi/4
 			};
 		
 		//sweep through the image matrix
 		for (int i=0; i<input.length; i+= 3) {
 			for (int j=0; i<input[0].length; j+=3) {
 				//check if filter is applied outside matrix
 				if ((i >= input.length) || (j >= input[0].length)) {
 				    break; //if out of bounds break and continue at next 
 				} else { //if not then calculate multiplication
 					for(int u=0; u<3; u++){
 					    for(int t=0; t<3; t++){
 					    	for(int v=0; v<3; v++){
 					    		//apply filter by using multiplication 
 					    		int in = 0;    		
 					    		if (((u+i) < input.length) && ((v+j) < input[0].length)) {
 						    		//System.out.println(u + " " + t + " " + v);
 					    			in = input[u+i][v+j];
 
 					    		}
 					    		output[u+i][t+j] += in * filter[filter_type][v][t];
 					    	}
 					    }
 					} //end of multiplying matrices
 				}
 			}
 		}
 		
 		int[][] crop_output = new int[input.length][input[0].length];
 		for (int i=0; i<input.length; i++) {
 			for (int j=0; j<input[0].length; j++) {
 				//System.out.println(i + " " + j);
 				crop_output[i][j] = output[i][j];
 			}
 		}
 		return crop_output;
 	}
 
 	public static void main(String[] args) {
 		int[][] test = {
 				{1,2,3},
 				{4,5,6},
 				{7,8,9}
 		};
 		// NOTE: Works only with complete matrix (none of the values above can be blank like this:
 		//{1,2,3,1},
 		//{4,5,6},
 		//{7,8,9}
 		System.out.println(test.length);
 		System.out.println(test[0].length);
 		TextureHistogram tx = new TextureHistogram();
 		int[][] result = TextureHistogram.filterMatrix(test,0);
 		for (int i=0; i<result.length; i++) {
 			for (int j=0; j<result[0].length; j++) {
 				System.out.println(i + " " + j + " : " + result[i][j]);
 			}
 		}
 	}
 }
