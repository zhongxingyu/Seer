 /*
  * Job of class Texture Histogram
  * Index image into histogram using texture
  * with edge direction histogram technique
  */
 public class TextureHistogram {
 
 	public int[][] filterMatrix(int[][] input, int filter_type) {		
		int[][] output = new int[input.length][input[0].length];
 		
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
					    		output[u+i][t+j] += input[u+i][v+j] * filter[filter_type][v][t];
 					    	}
 					    }
 					} //end of multiplying matrices
 				}
 			}
 		}
 		return output;
 	}
 
 	public static void main(String[] args) {
 		int[][] test = {
				{1,2,3},
				{4,5,6},
				{7,8,9}
 		};
 		TextureHistogram tx = new TextureHistogram();
 		int[][] result = tx.filterMatrix(test,0);
 		for (int i=0; i<result.length; i++) {
 			for (int j=0; j<result[0].length; j++) {
 				System.out.println(i + " " + j + " : " + result[i][j]);
 			}
 		}
 	}
 }
