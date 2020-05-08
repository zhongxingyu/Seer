 import java.util.*;
 import java.awt.image.*;
 import java.io.*;
 import javax.imageio.ImageIO;
 import java.awt.Color;
 
 
 
 
 public class Streak{
 
 
 
 	private static BufferedImage currentImage;
 	private static File imageFolder;
 	private static File productFolder;
 	private static File[] inputArray;
 	private static ArrayList<int[]> bufferColors = new ArrayList<int[]>();
 	//private static HashMap<List<Integer>, Integer> colorHash = new HashMap<List<Integer>, Integer>();
 	private static int[][][] changeBuffer;
 	private static Random randomGen = new Random();
 	private static int delay;
 
 	private static boolean isGreen(Color color){
 
 		if(color.getGreen()>105 && color.getRed()<115 && color.getBlue()<115){
 			return true;
 		}else{
 			return false;
 		}
 
 	}
 
 	private static Color setRandomColor(BufferedImage image, int x, int y){
 
 		int red = randomGen.nextInt(256);
 		int green = randomGen.nextInt(256);
 		int blue = randomGen.nextInt(256); 
 
 		Color randomColor = new Color(red, green, blue);
 
 		image.setRGB(x,y,randomColor.getRGB());
 		return randomColor;
 
 	}
 
 
 
 	/*private static boolean cycleBuffer(BufferedImage subject, int x, int y, int i){
 
 		Color subjectColor = new Color(subject.getRGB(x,y), true);
 
 		for(int j=0; j<bufferColors.size(); j++){
 
 			int[] bufferItem = bufferColors.get(j);
 
 			if((i-bufferItem[0])<delay && bufferItem[1]==x && bufferItem[2]==y){
 
 				imgProduct.setRGB(x,y,bufferItem[3]);
 				return true;
 
 			}
 		}
 
 		return false;
 	}*/
 
 	public static void main(String[] args){
 
 		imageFolder = new File(args[0]);
 		inputArray = imageFolder.listFiles();
 		productFolder = new File(args[1]);
 		delay = Integer.parseInt(args[2]);
 
 
 		productFolder.mkdir();
 
 		BufferedImage templateImage = null;
 
 		try{
 			templateImage = ImageIO.read(inputArray[1]);
 		} catch(IOException e){
 			System.out.println("There was an error in reading the image: " + inputArray[1]);
 		}
 
 		//System.out.println("The image is: " + templateImage.getWidth() + templateImage.getHeight());
 
 		changeBuffer = new int[delay][templateImage.getWidth()][templateImage.getHeight()];
 
 		for(int i=1; i<inputArray.length; i++){
 
 			System.out.println(inputArray[i] + "of " + inputArray.length + " total");
 
 
 			BufferedImage imgSubject = null;
 			try{
 				imgSubject = ImageIO.read(inputArray[i]);
 			} catch(IOException e){
 				System.out.println("There was an error in reading the image: " + inputArray[i]);
 			}
 
 
 			BufferedImage imgProduct = new BufferedImage(imgSubject.getWidth(), imgSubject.getHeight(), BufferedImage.TYPE_INT_ARGB);
 
 
 			for(int x=0; x<imgSubject.getWidth(); x++){
 
 				for(int y=0; y<imgSubject.getHeight(); y++){
 
 					Color subjectColor = new Color(imgSubject.getRGB(x,y), true);
 					boolean pixelSet = false;
 
 
 					for(int j=0; j<delay && !pixelSet; j++ ){
 
 
 							//List<Integer> fileColorMatch = Arrays.asList(i-j,x,y);
 
 							if(changeBuffer[j][x][y]!=0){
 
 								imgProduct.setRGB(x,y,changeBuffer[j][x][y]);
 								pixelSet = true;
 
 
 							}
 
 							//int possibleColor = pixelMap[(i-j)][x][y];
 							/*if(possibleColor!=0){
 								imgProduct.setRGB(x,y,possibleColor);
 								pixelSet = true;
 								break;
 							}*/
 						
 
 
 					}
 
 
					for(int k=0; k < delay-1; k++ ){



						changeBuffer[k][x][y] = changeBuffer[k+1][x][y];



					}
 
 					/*List<Integer> expiredListing = Arrays.asList(i-delay-1,x,y);
 
 					if(colorHash.containsKey(expiredListing)){
 						colorHash.remove(expiredListing);
 					}*/
 
 					/*for(int j=0; j<bufferColors.size(); j++){
 
 						int[] bufferItem = bufferColors.get(j);
 
 						if((i-bufferItem[0])>delay){
 
 							bufferColors.remove(j);
 						}
 
 						if((i-bufferItem[0])<delay && bufferItem[1]==x && bufferItem[2]==y){
 
 							imgProduct.setRGB(x,y,bufferItem[3]);
 							pixelSet = true;
 							break;
 						}
 					}*/
 
 					if(isGreen(subjectColor) && !pixelSet){
 
 						int randomColor = setRandomColor(imgProduct,x,y).getRGB();
 						/*int[] pixelRemember = {i,x,y,randomColor};
 						bufferColors.add(pixelRemember);*/
 						/*pixelMap[i][x][y] = randomColor;*/
 						//List<Integer> fileCoordinateKey = Arrays.asList(i,x,y);
 						//colorHash.put(fileCoordinateKey, randomColor);
 						changeBuffer[delay-1][x][y] = randomColor;
 
 					}else if (!pixelSet){
 						imgProduct.setRGB(x,y,imgSubject.getRGB(x,y));
 					}
 
 
 
 					/*Color subjectColor = new Color(imgSubject.getRGB(x,y), true);
 
 					if(subjectColor.getGreen()>105 && subjectColor.getRed()<115 && subjectColor.getBlue()<115){
 
 						int red = randomGen.nextInt(256);
 						int green = randomGen.nextInt(256);
 						int blue = randomGen.nextInt(256); 
 						
 						Color randomColor = new Color(red, green, blue);
 						imgProduct.setRGB(x,y,randomColor.getRGB());
 
 					} else {
 
 						imgProduct.setRGB(x,y,imgSubject.getRGB(x,y));
 					}*/
 
 
 				}
 
 			}
 
 			try{
 				File fullProductPath = new File(productFolder, ""+i+".png");
 				ImageIO.write(imgProduct, "png", fullProductPath);
 			} catch (IOException e) {
 				System.out.println("There was an error writing the saved file.");
 			}
 
 
 
 
 		}
 
 		
 
 		//System.out.println("Hello world!");
 
 		//System.out.println(Arrays.toString(imageFolder.listFiles()));
 
 
 	}
 
 
 
 }
