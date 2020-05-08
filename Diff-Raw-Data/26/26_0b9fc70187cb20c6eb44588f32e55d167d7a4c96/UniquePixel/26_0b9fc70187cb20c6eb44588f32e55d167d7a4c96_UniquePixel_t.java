 package util;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class UniquePixel {
 	
 	private static final String IMAGE_PATH = "Numbers/";
 	private static final String RED_PATH = "Red/";
 	private static final String BLACK_PATH = "Black/";
 	private static String CURRENT_ADDON_PATH = RED_PATH;
 	private static final String ACE = "A.png";
 	private static final String KING = "K.png";
 	private static final String QUEEN = "Q.png";
 	private static final String JACK = "J.png";
 	private static final String TEN = "10.png";
 	private static final String NINE = "9.png";
 	private static final String EIGHT = "8.png";
 	private static final String SEVEN = "7.png";
 	private static final String SIX = "6.png";
 	private static final String FIVE = "5.png";
 	private static final String FOUR = "4.png";
 	private static final String THREE = "3.png";
 	private static final String TWO = "2.png";
 	
 	private int[][][] imageArray = new int[13][][]; 
 	private int[][] pixelPosition = new int[13][2];
 	
 	public UniquePixel(){
 		loadImages();
 		findPixelPositions();
 	}
 	
 	public static void main(String args[]){
 		UniquePixel up = new UniquePixel();
 		System.out.println("RED");
 		System.out.println(up);
 		CURRENT_ADDON_PATH = BLACK_PATH;
 		UniquePixel up2 = new UniquePixel();
 		System.out.println("BLACK");
 		System.out.println(up2);
 	}
 	
 	@Override
 	public String toString(){
 		String s = "";
 		s+= "ACE: "+pixelPosition[0][0]+","+pixelPosition[0][1]+" Color: "+imageArray[0][pixelPosition[0][0]][pixelPosition[0][1]]+"\n";
		s+= "KING: "+pixelPosition[1][0]+","+pixelPosition[1][1]+" Color: "+imageArray[1][pixelPosition[1][0]][pixelPosition[1][1]]+"\n";
		s+= "QUEEN: "+pixelPosition[2][0]+","+pixelPosition[2][1]+" Color: "+imageArray[2][pixelPosition[2][0]][pixelPosition[2][1]]+"\n";
		s+= "JACK: "+pixelPosition[3][0]+","+pixelPosition[3][1]+" Color: "+imageArray[3][pixelPosition[3][0]][pixelPosition[3][1]]+"\n";
		s+= "TEN: "+pixelPosition[4][0]+","+pixelPosition[4][1]+" Color: "+imageArray[4][pixelPosition[4][0]][pixelPosition[4][1]]+"\n";
		s+= "NINE: "+pixelPosition[5][0]+","+pixelPosition[5][1]+" Color: "+imageArray[5][pixelPosition[5][0]][pixelPosition[5][1]]+"\n";
		s+= "EIGHT: "+pixelPosition[6][0]+","+pixelPosition[6][1]+" Color: "+imageArray[6][pixelPosition[6][0]][pixelPosition[6][1]]+"\n";
		s+= "SEVEN: "+pixelPosition[7][0]+","+pixelPosition[7][1]+" Color: "+imageArray[7][pixelPosition[7][0]][pixelPosition[7][1]]+"\n";
		s+= "SIX: "+pixelPosition[8][0]+","+pixelPosition[8][1]+" Color: "+imageArray[8][pixelPosition[8][0]][pixelPosition[8][1]]+"\n";
		s+= "FIVE: "+pixelPosition[9][0]+","+pixelPosition[9][1]+" Color: "+imageArray[9][pixelPosition[9][0]][pixelPosition[9][1]]+"\n";
		s+= "FOUR: "+pixelPosition[10][0]+","+pixelPosition[10][1]+" Color: "+imageArray[10][pixelPosition[10][0]][pixelPosition[10][1]]+"\n";
		s+= "THREE: "+pixelPosition[11][0]+","+pixelPosition[11][1]+" Color: "+imageArray[11][pixelPosition[11][0]][pixelPosition[11][1]]+"\n";
		s+= "TWO: "+pixelPosition[12][0]+","+pixelPosition[12][1]+" Color: "+imageArray[12][pixelPosition[12][0]][pixelPosition[12][1]]+"\n";
 		return s;
 	}
 	
 	private void loadImages(){
 		imageArray[0] = getSel(ACE);
 		imageArray[1] = getSel(KING);
 		imageArray[2] = getSel(QUEEN);
 		imageArray[3] = getSel(JACK);
 		imageArray[4] = getSel(TEN);
 		imageArray[5] = getSel(NINE);
 		imageArray[6] = getSel(EIGHT);
 		imageArray[7] = getSel(SEVEN);
 		imageArray[8] = getSel(SIX);
 		imageArray[9] = getSel(FIVE);
 		imageArray[10] = getSel(FOUR);
 		imageArray[11] = getSel(THREE);
 		imageArray[12] = getSel(TWO);
 	}
 	
 	public int[][] getSel(String fileName){
 		BufferedImage img = null;
 		try {
 		    img = ImageIO.read(new File(IMAGE_PATH+CURRENT_ADDON_PATH+fileName));
 		} catch (IOException e) {
 			System.out.println("Error");
 			e.printStackTrace();
 		}
 		int[][] sel = new int[img.getWidth()][img.getHeight()];
 		for(int i=0; i<img.getWidth(); i++){
 			for(int j=0; j<img.getHeight(); j++){
 				//Set up to not care about red/black.
 				/*if(img.getRGB(i, j) != Integer.parseInt(String.valueOf(Color.WHITE))){
 					sel[i][j] = Integer.parseInt(String.valueOf(Color.BLACK));
 				}else{
 					sel[i][j] = Integer.parseInt(String.valueOf(Color.WHITE));
 				}*/
 				//-16777216 BLACK, -6546415 RED
 				sel[i][j] = img.getRGB(i, j);
 				/*if(sel[i][j] == -16777216 || sel[i][j] == -6546415 || sel[i][j] == -10592674 || sel[i][j] == -3437690 || sel[i][j] == -6215393 || sel[i][j] == -5421764
 						|| sel[i][j] == -3371898 || sel[i][j] == -3624780){
 					sel[i][j] = -2;
 				}else{
 					sel[i][j] = -1;
 				}*/
 			}
 		}
 		return sel;
 	}
 	
 	private void findPixelPositions(){
 		boolean found = false;
 		for(int i=0; i<13; i++){
 			found = false;
 			for(int j=0; j<imageArray[i].length; j++){
 				if(found){
 					break;
 				}
 				for(int k=0; k<imageArray[i][j].length; k++){
 					if(found){
 						break;
 					}
 					found = true;
 					for(int l=0; l<13; l++){
 						if(i != l){
 							if(imageArray[i][j][k] == imageArray[l][j][k]){
 								found = false;
 							}
 						}
 					}
 					if(found){
 						pixelPosition[i][0] = j;
 						pixelPosition[i][1] = k;
 						for(int p=0; p<i; p++){
 							if(pixelPosition[p][0] == j && pixelPosition[p][1] == k || j < 3 || k < 3){
 								found = false;
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 }
