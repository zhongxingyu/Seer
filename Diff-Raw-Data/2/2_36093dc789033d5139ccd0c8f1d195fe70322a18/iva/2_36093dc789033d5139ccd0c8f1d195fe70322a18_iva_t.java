 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.io.*;
 import javax.imageio.*;
 import java.math.*;
 
 public class iva {
           
     private BufferedImage img = null;
     private int width, height;
     private double numberOfPixels;
     
     private double mean = 0, std = 0;
     private int[][] pixelScore;
     private int[][] verticalLines, horizontalLines;
     private int numvlines, numhlines;
     private int jump = 5, window = 5;
 
     private boolean[][] gridVerticalNoise,
 						gridHorizontalNoise,
 						gridDiagonalNoise,
 						gridGeneralNoise,
 						gridColorNoise,
 						gridCenterSurroundNoise;
 	private boolean[][] gridVerticalJet,
 						gridHorizontalJet,
 						gridDiagonalJet;
 	
 	// jet
 	double rowsize,columnsize, boxsize;
 	
     private int[][] imageFeaturesOld, imageFeatures;
     
     // some common color code
     private final int WHITE = 0xFFFFFF, BLACK = 0, GREY = 0x808080, BLUE = 0x0000FF, YELLOW = 0xFFFF00, GREEN = 0x008000;
     private final int v0 = 100, v1 = 50, v2 = 25, v3 = 10, threshold = v0;
     // factor
     private final double upper = 1.25, lower = 0.75;
     
     
     public iva(String imgPath){
 		try {
 			img = ImageIO.read(new File(imgPath));
 			getProperties();
 			
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 	}
     
     public iva(BufferedImage img){
 		this.img = img;
 		getProperties();
 	}
 	
 	public iva(iva x){
 		this.img = x.getBufferedImage();
 		
 		this.height = x.getHeight();
 		this.width = x.getWidth();
 		this.numberOfPixels = height * width;
 		
 		this.mean = x.getMean();
 		this.std = x.getStandardDeviation();
 	}
 	
 	public void colorImageComplete(){
 		
 		for (int i = 1; i < width - 3; i++){
 			for (int j = 0; j < height - 3; j++){
 				if (gridDiagonalNoise[i][j]) setColor(i - 1, j, YELLOW);
 				else if (gridHorizontalNoise[i][j]) setColor(i - 1, j, BLUE);
 				else if (gridVerticalNoise[i][j]) setColor(i - 1, j, GREEN);
 				else setColor(i - 1, j, BLACK);
 			}
 		}
 		
 		output("colorcomplete.png");
 	}
 	
 	public void computeMean(){
 		for(int i = 0; i < width; i++)
 			for (int j = 0; j < height; j++)
 				mean += (double)getAlphalessRGB(i, j) / numberOfPixels;
 	}
 	
 	// computeMean() must be called before computing standard deviation
 	public void computeStandardDeviation(){
 		for(int i = 0; i < width; i++)
 			for (int j = 0; j < height; j++)
 				std += Math.pow((double)getAlphalessRGB(i, j) - mean, 2) / numberOfPixels;
 				
 		std = Math.sqrt(std);
 	}
 	
 	public int getAlphalessRGB(int x, int y){
 		// Java ARGB color scheme
 		// A		R		 G		  B
 		// 00000000	00000000 00000000 00000000
 		
 		return img.getRGB(x, y) & 0xFFFFFF;
 	}
 	
 	public BufferedImage getBufferedImage(){
 		return this.img;
 	}
 	
 	public int getHeight(){
 		return height;
 	}
 	
 	public double getMean(){
 		return mean;
 		
 	}
 	
 	public int getRGB(int x, int y){
 		return getAlphalessRGB(x, y);
 	}
 	
 	public int getRGBBlue(int x, int y){
 		return getRGB(x, y) & 0xFF;
 	}
 	
 	public double getStandardDeviation(){
 		return std;
 	}
 	
 	public void getProperties(){
 		width = img.getWidth();
 		height = img.getHeight();
 		
 		numberOfPixels = width * height;
 		
 		pixelScore = new int[width][height];
 		
 		gridVerticalNoise 		= new boolean[width][height];
 		gridHorizontalNoise 	= new boolean[width][height];
 		gridDiagonalNoise 		= new boolean[width][height];
 		gridGeneralNoise 		= new boolean[width][height];
 		gridColorNoise 			= new boolean[width][height];
 		gridCenterSurroundNoise = new boolean[width][height];
 		
 		imageFeaturesOld = new int[3][9];
 		imageFeatures = new int[3][9]; 
 		
 		verticalLines = new int[1000][4];
 		horizontalLines = new int[1000][4];
 		
 		// initialize jet vectors
 		gridVerticalJet 		= new boolean[width][height];
 		gridHorizontalJet 		= new boolean[width][height];
 		gridDiagonalJet 		= new boolean[width][height];
 	}
 	
 	public int getWidth(){
 		return width;
 	}
 	
 	public void gradientSmoother(){
 		// assume mean and std is computed
 		int color;
 		
 		for(int i = 0; i < width; i++){
 			for (int j = 0; j < height; j++){
 				color = getAlphalessRGB(i, j);
 				
 				if ((double)color > mean + (double)upper * std){
 					setColor(i, j, WHITE);
 					//System.out.println("color:\t" + color + ", compared:\t" + (mean + (double)WHITE * std) + ", WHITE");
 				}else if ((double)color < mean - (double)lower * std){
 					setColor(i, j, BLACK);
 					//System.out.println("color:\t" + color + ", compared:\t" + (mean - (double)BLACK * std) + ", black");
 				}else{
 					setColor(i, j, GREY);
 					//System.out.println("color:\t" + color + ", compared:\t" + (mean + (double)black * std) + "," + (mean - (double)black * std) + "  GREY");
 				}
 			}
 		}
 		
 		output("gradientsmoothed.png");
 	}
 	
 	public void imageJet(){
 		int i, j;
 		
 		for (j = 0; j < height - 3; j++){
 			for (i = 0; i < width; i++){
 				if (gridDiagonalNoise[i][j])
 					gridHorizontalJet[i][j] = false;
 				else
 					gridHorizontalJet[i][j] = gridHorizontalNoise[i][j];
 					
 				if (gridDiagonalNoise[i][j] || gridVerticalNoise[i][j])
 					gridVerticalJet[i][j] = false;
 				else
 					gridVerticalJet[i][j] = gridVerticalNoise[i][j];
 					
 				gridDiagonalJet[i][j] = gridDiagonalNoise[i][j];
 			}
 		}
 		
 		for (j = 0; j < height - 3; j++){
 			if (j < height / 3){
 				for(i = 1; i <= width - 3; i++){
 					if (i < width / 3){
 						imageFeaturesOld[0][0] += gridVerticalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[1][0] += gridHorizontalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[2][0] += gridDiagonalNoise[i][j] ? 1 : 0;
 
 						imageFeatures[0][0] += gridVerticalJet[i][j] ? 1 : 0;
 						imageFeatures[1][0] += gridHorizontalJet[i][j] ? 1 : 0;
 						imageFeatures[2][0] += gridDiagonalJet[i][j] ? 1 : 0;
 					}else if (i >= width / 3 && i < 2 * (width / 3)){
 						imageFeaturesOld[0][1] += gridVerticalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[1][1] += gridHorizontalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[2][1] += gridDiagonalNoise[i][j] ? 1 : 0;
 
 						imageFeatures[0][1] += gridVerticalJet[i][j] ? 1 : 0;
 						imageFeatures[1][1] += gridHorizontalJet[i][j] ? 1 : 0;
 						imageFeatures[2][1] += gridDiagonalJet[i][j] ? 1 : 0;
 					}else{
 						imageFeaturesOld[0][2] += gridVerticalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[1][2] += gridHorizontalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[2][2] += gridDiagonalNoise[i][j] ? 1 : 0;
 
 						imageFeatures[0][2] += gridVerticalJet[i][j] ? 1 : 0;
 						imageFeatures[1][2] += gridHorizontalJet[i][j] ? 1 : 0;
 						imageFeatures[2][2] += gridDiagonalJet[i][j] ? 1 : 0;
 					}
 				}
 			}else if (j >= height / 3 && j < 2 * (height / 3)){
 				for(i = 1; i <= width - 3; i++){
 					if (i < width / 3)
 					{
 						imageFeaturesOld[0][3] += gridVerticalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[1][3] += gridHorizontalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[2][3] += gridDiagonalNoise[i][j] ? 1 : 0;
 
 						imageFeatures[0][3] += gridVerticalJet[i][j] ? 1 : 0;
 						imageFeatures[1][3] += gridHorizontalJet[i][j] ? 1 : 0;
 						imageFeatures[2][3] += gridDiagonalJet[i][j] ? 1 : 0;
 					}
 					else if (i >= width / 3 && i < 2 * (width / 3))
 					{
 						imageFeaturesOld[0][4] += gridVerticalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[1][4] += gridHorizontalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[2][4] += gridDiagonalNoise[i][j] ? 1 : 0;
 
 						imageFeatures[0][4] += gridVerticalJet[i][j] ? 1 : 0;
 						imageFeatures[1][4] += gridHorizontalJet[i][j] ? 1 : 0;
 						imageFeatures[2][4] += gridDiagonalJet[i][j] ? 1 : 0;
 					}
 					else
 					{
 						imageFeaturesOld[0][5] += gridVerticalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[1][5] += gridHorizontalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[2][5] += gridDiagonalNoise[i][j] ? 1 : 0;
 
 						imageFeatures[0][5] += gridVerticalJet[i][j] ? 1 : 0;
 						imageFeatures[1][5] += gridHorizontalJet[i][j] ? 1 : 0;
 						imageFeatures[2][5] += gridDiagonalJet[i][j] ? 1 : 0;
 					}
 				}
 			}else{
 				for(i = 1; i <= width - 3; i++){
 					if (i < width / 3)
 					{
 						imageFeaturesOld[0][6] += gridVerticalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[1][6] += gridHorizontalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[2][6] += gridDiagonalNoise[i][j] ? 1 : 0;
 
 						imageFeatures[0][6] += gridVerticalJet[i][j] ? 1 : 0;
 						imageFeatures[1][6] += gridHorizontalJet[i][j] ? 1 : 0;
 						imageFeatures[2][6] += gridDiagonalJet[i][j] ? 1 : 0;
 					}
 
 					else if (i>=width/3 && i<2*(width/3))
 					{
 						imageFeaturesOld[0][7] += gridVerticalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[1][7] += gridHorizontalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[2][7] += gridDiagonalNoise[i][j] ? 1 : 0;
 
 						imageFeatures[0][7] += gridVerticalJet[i][j] ? 1 : 0;
 						imageFeatures[1][7] += gridHorizontalJet[i][j] ? 1 : 0;
 						imageFeatures[2][7] += gridDiagonalJet[i][j] ? 1 : 0;
 					}
 					else
 					{
 						imageFeaturesOld[0][8] += gridVerticalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[1][8] += gridHorizontalNoise[i][j] ? 1 : 0;
 						imageFeaturesOld[2][8] += gridDiagonalNoise[i][j] ? 1 : 0;
 
 						imageFeatures[0][8] += gridVerticalJet[i][j] ? 1 : 0;
 						imageFeatures[1][8] += gridHorizontalJet[i][j] ? 1 : 0;
 						imageFeatures[2][8] += gridDiagonalJet[i][j] ? 1 : 0;
 					}
 				}
 			}
 		}	 
 	}
 	
 	public void imageJetStore(){
 		rowsize = (height / 3) * width;
         columnsize = height * (width / 3);
         boxsize =  (height / 3) * (width / 3);
 		
 		
 		// save jet
 		for (int k = 0; k < 3; k++){
 			
 		}
 		/*
 		// Save Jet
         
 
         for (int k=0; k<3; k++)
         {
                 float sum,value;
 
                 sum = imageFeaturesOld[k][0];
                 sum += imageFeaturesOld[k][3];
                 sum += imageFeaturesOld[k][6];
 
                 MemoJet->Lines->Add(sum/columnsize);
 
                 sum = imageFeaturesOld[k][1];
                 sum += imageFeaturesOld[k][4];
                 sum += imageFeaturesOld[k][7];
 
                 MemoJet->Lines->Add(sum/columnsize);
 
                 sum = imageFeaturesOld[k][2];
                 sum += imageFeaturesOld[k][5];
                 sum += imageFeaturesOld[k][8];
 
                 MemoJet->Lines->Add(sum/columnsize);
 
                 sum = imageFeaturesOld[k][0];
                 sum += imageFeaturesOld[k][1];
                 sum += imageFeaturesOld[k][2];
 
                 MemoJet->Lines->Add(sum/rowsize);
 
                 sum = imageFeaturesOld[k][3];
                 sum += imageFeaturesOld[k][4];
                 sum += imageFeaturesOld[k][5];
 
                 MemoJet->Lines->Add(sum/rowsize);
 
                 sum = imageFeaturesOld[k][6];
                 sum += imageFeaturesOld[k][7];
                 sum += imageFeaturesOld[k][8];
 
                 MemoJet->Lines->Add(sum/rowsize);
 
                 for (int l=0; l<9; l++)
                         MemoJet->Lines->Add(imageFeaturesOld[k][l]/boxsize);
         }
 
         for (int k=0; k<3; k++)
         {
                 float sum,value;
 
                 sum = imageFeatures[k][0];
                 sum += imageFeatures[k][3];
                 sum += imageFeatures[k][6];
 
                 MemoJet->Lines->Add(sum/columnsize);
 
                 sum = imageFeatures[k][1];
                 sum += imageFeatures[k][4];
                 sum += imageFeatures[k][7];
 
                 MemoJet->Lines->Add(sum/columnsize);
 
                 sum = imageFeatures[k][2];
                 sum += imageFeatures[k][5];
                 sum += imageFeatures[k][8];
 
                 MemoJet->Lines->Add(sum/columnsize);
 
                 sum = imageFeatures[k][0];
                 sum += imageFeatures[k][1];
                 sum += imageFeatures[k][2];
 
                 MemoJet->Lines->Add(sum/rowsize);
 
                 sum = imageFeatures[k][3];
                 sum += imageFeatures[k][4];
                 sum += imageFeatures[k][5];
 
                 MemoJet->Lines->Add(sum/rowsize);
 
                 sum = imageFeatures[k][6];
                 sum += imageFeatures[k][7];
                 sum += imageFeatures[k][8];
 
                 MemoJet->Lines->Add(sum/rowsize);
 
                 for (int l=0; l<9; l++)
                         MemoJet->Lines->Add(imageFeatures[k][l]/boxsize);
         }
 
 		 */
 		
 	}
 	
 	
 	public boolean isInHeightRange(int val){
 		if (val >= height || val < 0)
 			return false;
 			
 		return true;
 	}
 	
 	public boolean isInWidthRange(int val){
 		if (val >= width || val < 0)
 			return false;
 			
 		return true;
 	}
 	
 	public void lineComplete(){
 		
 		for (int i = 0; i < width; i++){
 			for(int j = 0; j < height; j++){
 				if (getRGB(i, j) == WHITE){
 					int m ,n,v;
 					
 					pixelScore[i][j] += v0;
 					
 					for(int k = 1; k < 4; k++) {
 						
 						if (k == 1) v = v1;
 						else if (k == 2) v = v2;
 						else v = v3;
 
 						for(m = i - k; m <= i + k; m++) {
 							if(m >= 0 && m < width && j - k >= 0)
 								pixelScore[m][j-k] += v;
 								
 							if(m >= 0 && m < width && j + k < height)
 								pixelScore[m][j+k] += v;
 						}
 						
 						for(n = j - k + 1; n < j + k; n++) {
 							if(i - k >= 0 && n >= 0 && n < height)
 								pixelScore[i-k][n] += v;
 								
 							if(i + k < width && n >= 0 && n < height)
 								pixelScore[i+k][n] += v;
 						}
 					}
 					
 				}	
 			}
 		}
 		
 		for (int i = 0; i < width; i++){
 			for (int j = 0; j < height; j++){
 				if (pixelScore[i][j] > threshold)
 					setColor(i, j, WHITE);
 				else
 					setColor(i, j, BLACK);
 			}
 		}
 		
 		output("after_linecomplete.png");
 	}
 	
 	//! line Draw complete need some clarification !!!!!
 	public void lineDrawComplete(){
 		
 		int color;
 		for (int i = 0; i < height; i++){
 			for (int j = 0; j < width; j++){
 				
 				color = getRGB(j, i);
 				
 				if (color == GREEN){
 					// the pixel's color is green
 					int topx = j, topy = i, bottomx = j, bottomy = i;
 					int k = i,l = j, m, n;
 					
 					for(n = 0; n <= window; n++) {
 						// find approrpiate m (vertically above the current pixel 
 						// and within the jump range but still in green color) 
 						for(m = 1; m <= jump && isInWidthRange(l + n) && isInHeightRange(k - m) && !(getRGB(l + n, k - m) == GREEN); m++);
 
 						if (m <= jump && k >= 0 && isInWidthRange(l + n) && isInHeightRange(k - m)) {
 							topx = l + n;
 							topy = k - m;
 							k = k - m;
 							l = l + n;
 							n = -1;
 							
 							continue;
 						}
 
 						// similarly check horizontally to the left of current pixel
						for(m = 1; m <= jump && isInWidthRange(l - n) && isInHeightRange(k - m) && !(getRGB(l - n, k - m) == GREEN); m++);
 
 						if (m <= jump && k >= 0) {
 							topx = l - n;
 							topy = k - m;
 							k -= m;
 							l -= n;
 							n = -1;
 							
 							continue;
 						}
 					}
 					
 					
 					// reset k, l index
 					k = i;
 					l = j;
 					
 					for(n = 0; n <= window; n++) {
 						// find approrpiate m (vertically above the current pixel 
 						// and within the jump range but still in green color) 
 						for(m = 1; m <= jump && isInHeightRange(k + m) && isInWidthRange(l + n) && !(getRGB(l + n, k + m) == GREEN); m++) ;
 
 						if (m <= jump && k < height) {
 							bottomx = l + n;
 							bottomy = k + m;
 							k += m;
 							l += n;
 							n = -1;
 							
 							continue;
 						}
 
 						// similarly check horizontally to the left of current pixel
 						for(m = 1; m <= jump && isInHeightRange(k + m) && isInWidthRange(l - n) && !(getRGB(l - n, k + m) == GREEN); m++) ;
 
 						if (m <= jump && k < height && isInHeightRange(k + m) && isInWidthRange(l - n)) {
 							bottomx = l - n;
 							bottomy = k + m;
 							k += m;
 							l -= n;
 							n = -1;
 							continue;
 						}
 
 					}
 
 					if (i - topy > threshold || bottomy - i > threshold) {
 						for(k = i; k >= topy; k--)
 							for(l = j - window;l <= j + window; l++)
 								setColor(l, k, BLACK);
 								
 						for(k = i;k <= bottomy; k++)
 							for(l = j - window;l <= j + window; l++)
 								setColor(l, k, BLACK);
 						
 						// set vertical line vector
 						verticalLines[numvlines][0] = topx;
 						verticalLines[numvlines][1] = topy;
 						verticalLines[numvlines][2] = bottomx;
 						verticalLines[numvlines][3] = bottomy;
 
 						numvlines++;
 					}
 					
 				}else if (color == BLUE){
 					int leftx = j, lefty = i, rightx = j, righty = i;
 					int k = i, l = j, m, n;
 
 					for(n = 0; n <= window; n++) {
 						for(m = 1; m <= jump && isInHeightRange(k + n) && isInWidthRange(l - m) && !(getRGB(l - m, k + n) == BLUE); m++);
 
 						if (m <= jump && l >= 0 && isInHeightRange(k + n) && isInWidthRange(l - m)) {
 							leftx = l - m;
 							lefty = k + n;
 							k += n;
 							l -= m;
 							n = -1;
 							continue;
 						}
 
 						for(m = 1; m <= jump && isInWidthRange(l - m) && isInHeightRange(k - n) && !(getRGB(l - m, k - n) == BLUE); m++) ;
 
 						if (m <= jump && l >= 0 && isInWidthRange(l - m) && isInHeightRange(k - n)) {
 							leftx = l - m;
 							lefty = k - n;
 							k -= n;
 							l -= m;
 							n = -1;
 							
 							continue;
 						}
 
 					}
 
 					for(n = 0; n <= window; n++) {
 						for(m = 1; m <= jump && isInHeightRange(k + n) && isInWidthRange(l + m) && !(getRGB(l + m, k + n) == BLUE); m++);
 
 						if (m <= jump && l < width && isInHeightRange(k + n) && isInWidthRange(l + m)) {
 							rightx = l + m;
 							righty = k + n;
 							k += n;
 							l += m;
 							n = -1;
 							
 							continue;
 						}
 
 						for(m = 1; m <= jump && isInHeightRange(k - n) && isInWidthRange(l + m) && !(getRGB(l + m, k - n) == BLUE); m++);
 
 						if (m <= jump && l < width && isInHeightRange(k - n) && isInWidthRange(l + m)) {
 							rightx = l + m;
 							righty = k - n;
 							k -= n;
 							l += m;
 							n = -1;
 							
 							continue;
 						}
 
 					}
 
 					if (j - leftx > threshold || rightx - j > threshold) {
 						for(k = i - window; k <= i + window; k++)
 							for(l = j;l >= leftx; l--)
 								if (isInWidthRange(l) && isInHeightRange(k))
 									setColor(l, k, BLACK);
 								
 						for(k = i - window; k <= i + window; k++)
 							for(l = j; l <= rightx; l++)
 								if (isInWidthRange(l) && isInHeightRange(k))
 									setColor(l, k, BLACK);
 
 						horizontalLines[numhlines][0] = leftx;
 						horizontalLines[numhlines][1] = lefty;
 						horizontalLines[numhlines][2] = rightx;
 						horizontalLines[numhlines][3] = righty;
 
 						numhlines++;
 					}
 				}	
 			}
 		}
 		
 		// first attempt to simulate line drawing
 		// we need a unified method to 
 		// 1. convert BufferedImage object into Graphics2D object
 		Graphics2D g2d = img.createGraphics();
 		
 		// 2. draw the line with specific color from specific starting point to end point
 		// set draw color
 		g2d.setColor(Color.GREEN);
 		
 		int index = 0;
 		
 		for (index = 0; index < numvlines; index++){
 			g2d.drawLine(verticalLines[index][0], verticalLines[index][1], verticalLines[index][2], verticalLines[index][3]);
 		}
 		
 		g2d.setColor(Color.BLUE);
 		for (index = 0; index < numhlines; index++){
 			g2d.drawLine(verticalLines[index][0], verticalLines[index][1], verticalLines[index][2], verticalLines[index][3]);
 		}
 		
 		// 3. release resource
 		g2d.dispose();
 		output("after_drawlinecomplete.png");
 	}
 	
 	public void lineThinningComplete(){
 		// implementation goes here
 	}
 	
 	public boolean noiseCenterSurround(int i, int j){
 		
 		double surroundColorSum, surroundColorAvrg;
         int pixelValue;
 
         // Out of bounds, do not calculate
         if (i + 2 >= width || j + 2 >= height)
 			return true;
 
         // Analysis 3X3 space
         // * * *
         // *   *
         // * * *
         surroundColorSum = getRGB(i, j) + getRGB(i, j + 1) + getRGB(i, j + 2) + getRGB(i + 1, j) +
 						   getRGB(i + 1, j + 2) + getRGB(i + 2, j) + getRGB(i + 2, j + 1) + getRGB(i + 2, j + 2);
 		
         surroundColorAvrg = surroundColorSum / 8;
 
         if ((double)getRGB(i + 1, j + 1) > surroundColorAvrg)  
 			return true;
 		
 		return false;
 	}
 	
 	public boolean noiseColor(int i, int j){
 		
 		int colors[] = new int[16], count[] = new int[16];
         int ttlcolor, theColor;
         int k, l, m, aPixel;
 
         // Initalize arrays
         for(k = 0; k < 16; k++){
 			colors[k] = -1;
             count[k] = 0;
         }
 
         // Analysis 3X3 space
 
 		for(k = 0; k < 3 && (k + i) < width; k++){
 			for(l = 0; l < 3 && (l + j) < height; l++){
 		
 				aPixel = getRGB(i + k, j + l);
 				
 				for(m = 0; m < 16 && colors[m] != -1 && colors[m] != aPixel; m++);
 				
 				if (m < 16){
 						if (colors[m] == -1) colors[m] = aPixel;
 						count[m]++;
 				}
 			}
 		}
 
         // Copy into next Image
 
         for(ttlcolor = 0; ttlcolor < 16 && colors[ttlcolor] != -1; ttlcolor++);
 
         if (ttlcolor !=1)
 			return true;
 
 		return false;
 	}
 	
 	public boolean noiseDiagonal(int i, int j){
 
         double diagSumR, diagAvrgR, diagSumL, diagAvrgL,
 			   remainderSumR, remainderAvrgR, remainderSumL, remainderAvrgL;
 
         // Out of bounds, do not calculate
         if (i + 2 >= width || j + 2 >= height)
 			return true;
 
         // Analysis 3X3 space
         // diagSumR	diagSumL
         // - - *	* - -
         // - * -	- * -
         // * - -	- - *
 		diagSumR = getRGB(i + 2, j) + getRGB(i + 1, j + 1) + getRGB(i, j + 2);
 		diagAvrgR = diagSumR / 3;
 		
 		diagSumL = getRGB(i, j) + getRGB(i + 1, j + 1) + getRGB(i + 2, j + 2);
 		diagAvrgL = diagSumL / 3;
 
 		remainderSumR = getRGB(i + 1, j) + getRGB(i + 2, j + 1) + getRGB(i + 1, j + 2) + getRGB(i, j + 1) + diagSumL - getRGB(i + 1, j + 1);
 		remainderAvrgR = remainderSumR / 6;
 
 		remainderSumL = remainderSumR + diagSumR - diagSumL;
 		remainderAvrgL = remainderSumL / 6;
                         
 
         // Copy into next Image
         if (diagAvrgR > remainderAvrgR || diagAvrgL > remainderAvrgL)
 			return true;
 		
 		return false;
 	}
 	
 	public boolean noiseGeneral(int i, int j){
 
         int points = 0;
 
         // Out of bounds, do not calculate
         if (i + 2 >= width || j + 2 >= height)
 			return true;
 
         // Analysis 3X3 space
 		if (getRGB(i, j) > getRGB(i + 1, j)) points++;
 		if (getRGB(i + 2, j) > getRGB(i + 1, j)) points++;
 		if (getRGB(i + 1, j + 1) > getRGB(i, j + 1)) points++;
 		if (getRGB(i + 1, j + 1) > getRGB(i + 2, j + 1)) points++;
 		if (getRGB(i, j + 2) > getRGB(i + 1, j + 2)) points++;
 		if (getRGB(i + 2, j + 2) > getRGB(i + 1, j + 2)) points++;
                 
 
         // Copy into next Image
 
         if (points > 3)
 			return true;
 		
 		return false;
 	}
 	
 	public boolean noiseHorizontal(int i, int j){
 		
 		double horizontalSumT, horizontalSumB, horizontalAvrgT, horizontalAvrgB,
 			   remainderSumT, remainderSumB, remainderAvrgT, remainderAvrgB;
 
         // Out of bounds, do not calculate
         if (i + 2 >= width || j + 2 >= height)
 			return true;
 
         // Analysis 3X3 space
         // vertical Top *, Bottom #, middle - 
         // * * *
         // - - -
         // # # #
         
         horizontalSumT = getRGB(i, j) + getRGB(i + 1, j) + getRGB(i + 2, j);
         horizontalAvrgT = horizontalSumT / 3;
 
 		horizontalSumB = getRGB(i, j + 2) + getRGB(i + 1, j + 2) + getRGB(i + 2, j + 2);
 		horizontalAvrgB = horizontalSumB / 3;
 
 		remainderSumT = getRGB(i, j + 1) + getRGB(i + 1, j + 1) + getRGB(i + 2, j + 1) + horizontalSumB;
 		remainderAvrgT = remainderSumT / 6;
 
 		// ???
 		remainderSumB = remainderSumT - horizontalSumB + horizontalSumT;
 		remainderAvrgB = remainderSumB / 6;
 
 
 		if (horizontalAvrgT > remainderAvrgT)
 			return true;
 		
 		return false;
 	}
 	
 	// return a boolean flag whether there exists noise within 3 x 3 section of pixels
 	public boolean noiseVertical(int i, int j){
 
 		// Out of bounds, do not calculate
 		if (width <= i + 2 || height <= j + 2)
 			return true;
 		
 		double verticalSumR = 0, verticalSumL = 0, verticalAvrgR, verticalAvrgL,
 			   remainderSumR = 0, remainderSumL = 0, remainderAvrgR, remainderAvrgL;
 			   
         // Analysis 3X3 space
         // vertical Left *, Right #, middle - 
         // * - #
         // * - #
         // * - #
         
 		verticalSumR += getRGB(i + 2, j) + getRGB(i + 2, j + 1) + getRGB(i + 2, j + 2);
 		verticalAvrgR = verticalSumR / 3;
 
 		verticalSumL += getRGB(i, j) + getRGB(i, j + 1) + getRGB(i, j + 2);
 		verticalAvrgL = verticalSumL / 3;
 
 		remainderSumR = verticalSumL + getRGB(i + 1, j) + getRGB(i + 1, j + 1) + getRGB(i + 1, j + 2);
 		
 		
 		// ??? remainderAvrgR is never used
 		remainderAvrgR = remainderSumR / 6;
 
 		remainderSumL = remainderSumR - verticalSumL + verticalSumR ;
 
 		remainderAvrgL = remainderSumL / 6;
 
 
         if (verticalAvrgL > remainderAvrgL)
 			return true;
 			
 		return false;
 		
 	}
 	
 	public void noiseReduction(){
 		noiseReduction(false);
 	}
 	
 	public void noiseReduction(boolean flag){
 		
 		for (int i = 0; i < width; i++){
 			for (int j = 0; j < height; j++){
 				if (noiseVertical(i, j))
 					gridVerticalNoise[i][j] = true;
 				
 				if (noiseHorizontal(i, j))
 					gridHorizontalNoise[i][j] = true;
 					
 				if (noiseDiagonal(i, j))
 					gridDiagonalNoise[i][j] = true;
 					
 				if (noiseGeneral(i, j))
 					gridGeneralNoise[i][j] = true;
 					
 				if (noiseCenterSurround(i, j))
 					gridCenterSurroundNoise[i][j] = true;
 					
 				if (noiseColor(i, j))
 					gridColorNoise[i][j] = true;
 			}
 		}
 		
 		String o;
 		
 		if (flag)
 			o = "post_";
 		else
 			o = "";
 		
 		noiseSuppression(gridVerticalNoise);
 		output(o + "after_vertical.png");
 		noiseSuppression(gridHorizontalNoise);
 		output(o + "after_horizontal.png");
 		noiseSuppression(gridDiagonalNoise);
 		output(o + "after_diagonal.png");
 		//noiseSuppression(gridGeneralNoise);
 		//output("after_general.png");
 		noiseSuppression(gridCenterSurroundNoise);
 		output(o + "after_centersurround.png");
 		noiseSuppression(gridColorNoise);
 		output(o + "after_color.png");
 	}
 	
 	protected void noiseSuppression(boolean[][] grid){
 		for (int j = 0; j < height - 3; j++){
 			for (int i = 1; i <= width - 3; i++){
 				if ( ! grid[i][j])
 					setColor(i - 1, j, BLACK);
 				else
 					setColor(i - 1, j, WHITE);
 			}
 		}
 	}
 	
 	public void output(){
 		outputImage("o_test.png", "PNG");
 	}
 	
 	public void output(String fileName){
 		outputImage(fileName, "PNG");
 	}
 	
 	public void outputImage(String fileName, String type){
 		try {
 			File f = new File(".." + File.separator + "o" + File.separator + fileName);
 			ImageIO.write(img, type, f);
 		} catch (IOException e){
 			e.printStackTrace();
 		} 
 	}
 	
 	public void preComputation(){
 		// pre computation steps
 		computeMean();
 		computeStandardDeviation();
 	}
 	
 	public void setColor(int x, int y, int color){
 		img.setRGB(x, y, 0xFF000000 | color);
 	}
 	
 	public void setImage(BufferedImage img){
 		this.img = img;
 		getProperties();
 	}
 
 }
 
 
