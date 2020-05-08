 
 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 
 public class ImageProcessor {
 	
 	public static final String newline = System.getProperty("line.separator");
 	
 	public static final String symbols = "0123456789+-";
 	
 	public static final int outputWidth = 10;
 	public static final int outputHeight = 10;
 	
 	BufferedImage crop;
 	BufferedImage gray;
 	BufferedImage start;
 
 	public ImageProcessor() {
 		// Nothing to do currently.
 	}
 
 	public void printCharacters(File file, String outPath, String name) {
 		BufferedImage base = null;
 		try {
 			//File file = new File(imagePath);
 			base = ImageIO.read(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		BufferedImage gray = toGrayscale(base);
 		BufferedImage crop = crop(gray);
 
 		ArrayList<boolean[][]> result = scaleBitmap(crop, outputWidth, outputHeight, findIndices(crop));
 
    System.out.println(findIndices(crop));
     System.out.println(name.length());
     if (result.size() != name.length()) {
       System.out.println("\tmismatch");
      //return;
     }
 
 		writeInputsToFile(result, outPath + "inputs.txt");
 		writeAnswersToFile(outPath + "answers.txt", name);
 		
 		try {
 			ImageIO.write(crop, "jpg", new File(outPath + "crop.jpg"));
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		int count = 0;
 		for(boolean[][] array : result) {
 			try {
 				ImageIO.write(toImage(array), "jpg", new File(outPath + count + ".jpg"));
 				count ++;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/* Writes the given list of boolean arrays to a file in a format that
 	 * MATLAB can understand.
 	 */
 	private void writeInputsToFile(ArrayList<boolean[][]> arrays, String outPath){
 		String result = "";
 		for(boolean[][] img : arrays){
 			result += newline;
 			for(int i = 0; i < img.length; i++){
 				for(int j = 0; j < img[0].length; j++) {
 					if(img[i][j]){
 						result += " 1";
 					} else {
 						result += " 0";
 					}
 				}
 				result += " ..." + newline;
 			}
 			result += ";" + newline;
 		}
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(outPath, true));
 			writer.write(result);
 			writer.flush();
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void writeAnswersToFile(String outPath, String name){
 		String result = "";
 		String temp;
 		for(int i = 0; i < name.length(); i++){
 			temp = "";
 			int answer = symbols.indexOf(name.charAt(i));
 			for(int j = 0; j < symbols.length(); j++){
 				if(j == answer){
 					temp += " 1";
 				} else {
 					temp += " 0";
 				}
 			}
 			temp += "; ..." + newline;
 			result += temp;
 		}
 		
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(outPath, true));
 			writer.write(result);
 			writer.flush();
 			writer.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public BufferedImage toImage(boolean[][] input) {
 		BufferedImage output = new BufferedImage(input[0].length, input.length, BufferedImage.TYPE_INT_RGB);
 		for(int i = 0; i < outputHeight; ++i) 
 			for(int j = 0; j < outputWidth; ++j)
 				if (input[i][j])
 					output.setRGB(j, i, 0);
 				else
 					output.setRGB(j, i, -1);
 
 		return output;
 
 	}
 
 	public BufferedImage toGrayscale(BufferedImage input) {
 		BufferedImage grayScaled = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
 		int avg = 0;
 		int black = 0;
 		int white = -1;
 		int height = input.getHeight();
 		int width = input.getWidth();
 		for(int i = 0; i < height; ++i) {
 			for(int j = 0; j < width; ++j) {
 				// color is really just an int
 				avg = (input.getRGB(j, i) >> 16) & 0xFF;
 				avg += (input.getRGB(j, i) >> 8) & 0xFF;
 				avg += (input.getRGB(j, i) & 0xFF);
 				avg /= 3;
 				//avg = (avg << 16) | (avg << 8) | avg;
 				if (avg  < 100)
 					grayScaled.setRGB(j, i, black);
 				else
 					grayScaled.setRGB(j, i, white);
 			}
 		}
 		return grayScaled;
 	}
 /*
 	public BufferedImage cropVertically(BufferedImage input) {
 		int topIndex = -1;
 		int botIndex = -1;
 		int THRESH = 135;
 
 		int height = input.getHeight();
 		int width = input.getWidth();
 
 		// Find top and bottom indices
 
 		for(int i = 0; i < height; ++i) {
 			boolean found = false;
 			// Check each row
 			for(int j = 0; j < width; ++j) {
 				if( (input.getRGB(j, i) & 0xFF ) < THRESH)
 					found = true;
 			}
 			if(found) {
 				topIndex = i;
 				break;
 			}
 		}
 
 		for(int i = height - 1; i >= 0; --i) {
 			boolean found = false;
 			// Check each row
 			for(int j = 0; j < width; ++j) {
 				if( (input.getRGB(j, i) & 0xFF) < THRESH)
 					found = true;
 			}
 			if(found) {
 				botIndex = i;
 				break;
 			}
 		}
 
 		BufferedImage cropped = new BufferedImage(input.getWidth(), Math.abs(botIndex - topIndex), BufferedImage.TYPE_INT_RGB);
 
 		int diff = botIndex - topIndex;
 		for(int i = 0; i < diff; ++i) {
 			for(int j = 0; j < width; ++j) {
 				cropped.setRGB(j, i, input.getRGB(j,topIndex+i));
 			}
 		}
 
 		return cropped;
 	}
 
 	public BufferedImage cropHorizontally(BufferedImage input) {
 		int leftIndex = -1;
 		int rightIndex = -1;
 		int THRESH = 135;
 
 		// Find top and bottom indices
 
 		for(int i = 0; i < input.getWidth(); ++i) {
 			boolean found = false;
 			// Check each row
 			for(int j = 0; j < input.getHeight(); ++j) {
 				if( (input.getRGB(i, j) & 0xFF ) < THRESH)
 					found = true;
 			}
 			if(found) {
 				leftIndex = i;
 				break;
 			}
 		}
 
 		for(int i = input.getWidth()-1; i >= 0; --i) {
 			boolean found = false;
 			// Check each row
 			for(int j = 0; j < input.getHeight(); ++j) {
 				if( (input.getRGB(i, j) & 0xFF) < THRESH)
 					found = true;
 			}
 			if(found) {
 				rightIndex = i;
 				break;
 			}
 		}
 
 		BufferedImage cropped = new BufferedImage(rightIndex - leftIndex, input.getHeight(), BufferedImage.TYPE_INT_RGB);
 
 		for(int i = 0; i < input.getHeight(); ++i) {
 			for(int j = 0; j < (rightIndex-leftIndex); ++j) {
 				cropped.setRGB(j, i, input.getRGB(leftIndex+j,i));
 			}
 		}
 
 		return cropped;
 	}
 */
 	public BufferedImage crop(BufferedImage input) {
 		int leftIndex = -1;
 		int rightIndex = -1;
 		int topIndex = -1;
 		int botIndex = -1;
 		int THRESH = 135;
 
 		for(int i = 0; i < input.getHeight(); ++i) {
 			boolean found = false;
 			// Check each row
 			for(int j = 0; j < input.getWidth(); ++j) {
 				if( (input.getRGB(j, i) & 0xFF ) < THRESH)
 					found = true;
 			}
 			if(found) {
 				topIndex = i;
 				break;
 			}
 		}
 
 		for(int i = input.getHeight()-1; i >= 0; --i) {
 			boolean found = false;
 			// Check each row
 			for(int j = 0; j < input.getWidth(); ++j) {
 				if( (input.getRGB(j, i) & 0xFF) < THRESH)
 					found = true;
 			}
 			if(found) {
 				botIndex = i;
 				break;
 			}
 		}
 
 
 		for(int i = 0; i < input.getWidth(); ++i) {
 			boolean found = false;
 			// Check each row
 			for(int j = topIndex; j < botIndex; ++j) {
 				if( (input.getRGB(i, j) & 0xFF ) < THRESH)
 					found = true;
 			}
 			if(found) {
 				leftIndex = i;
 				break;
 			}
 		}
 
 		for(int i = input.getWidth()-1; i >= leftIndex; --i) {
 			boolean found = false;
 			// Check each row
 			for(int j = topIndex; j < botIndex; ++j) {
 				if( (input.getRGB(i, j) & 0xFF) < THRESH)
 					found = true;
 			}
 			if(found) {
 				rightIndex = i;
 				break;
 			}
 		}
 
 		BufferedImage cropped = new BufferedImage(rightIndex - leftIndex, Math.abs(botIndex - topIndex), BufferedImage.TYPE_INT_RGB);
 
 
 		for(int i = 0; i < Math.abs(botIndex - topIndex); ++i) {
 			for(int j = 0; j < (rightIndex-leftIndex); ++j) {
 				cropped.setRGB(j, i, input.getRGB(leftIndex+j,topIndex+i));
 			}
 		}
 
 		return cropped;
 	}
 
 	public ArrayList < ArrayList<Integer> > findIndices(BufferedImage input) {
 		int currentStart = 1;
 		int THRESH = 185;
 		ArrayList<ArrayList<Integer>> indices = new ArrayList<ArrayList<Integer>>();
 
 		boolean foundStart = true;
 		for(int i = 1; i < input.getWidth(); ++i) {
 			boolean dark = false;
 			// Check each row
 			for(int j = 0; j < input.getHeight(); ++j) {
 				if( (input.getRGB(i, j) & 0xFF) < THRESH)
 					dark = true;
 			}
 			if(dark && !foundStart) {
 				currentStart = i;
 				foundStart = true;
 			} else if (!dark && foundStart && ( (i - currentStart) > 4)) {
 				ArrayList<Integer> duple = new ArrayList<Integer>(2);
 				duple.add(0, currentStart-1);
 				duple.add(1, i+1);
 				indices.add(duple);
 				//Log.v("ocrdebug","start: " + currentStart + " " + i);
 				foundStart = false;
 			}
 		}
 
 		if (foundStart) {
 			ArrayList<Integer> duple = new ArrayList<Integer>(2);
 			duple.add(0, currentStart-1);
 			duple.add(1, input.getWidth()-1);
 			indices.add(duple);
 		}
 
 		//	for(ArrayList<Integer> duple: indices) {
 		//		int red = (255 << 16);
 		//		int green = (255 << 8);
 		//		for(int j = 0; j < input.getHeight(); ++j) {
 		//			input.setRGB(duple.get(0), j, red); 
 		//		}
 		//		for(int j = 0; j < input.getHeight(); ++j) {
 		//			input.setRGB(duple.get(1), j, green); 
 		//		}
 		//	}
 
 		return indices;
 
 	}
 
 	/* Presumably we want the images to all be the same size so that they
 	 * can be fed into the neural network with ease. However it can't hurt
 	 * to make the function generate boolean 2d arrays that can vary with
 	 * the input.
 	 */
 	public ArrayList<boolean[][]> scaleBitmap(BufferedImage source, int outputWidth, int outputHeight, ArrayList<ArrayList<Integer>> pairs) {
 		final int THRESH = 185;
 		//final double OUTPUTTHRESH = .1;
 		int largestDim;
 		int imageWidth, imageHeight;
 		int topIndex, botIndex;
 
 		BufferedImage character;
 
 		boolean[][] neuralInput;
 		ArrayList<boolean[][]> ret = new ArrayList<boolean[][]>(pairs.size());
 
 
 		// Loop over the pairs in the list.
 		//ArrayList<Integer> pair = pairs.get(1);
 		for( ArrayList<Integer> pair: pairs ){
 			topIndex = 0;
 			botIndex = 0;
 
 			// Subtract the left index from the right index to get the width
 			// of this character.
 			imageWidth = pair.get(1) - pair.get(0);
 
 			// we want to select that area of the bitmap
 			// then we need to determine its largest dimension.
 
 			// Determine the bottom index and top index of this part of the image,
 			// so that we can zoom in as much as possible on this part of the image.
 			for(int i = 0; i < source.getHeight(); i++) {
 				for(int j = pair.get(0); j < pair.get(1); j++){
 					if( ((source.getRGB(j, i) & 0xFF) < THRESH) && (topIndex == 0) )
 						topIndex = i;
 					else if( (source.getRGB(j, i) & 0xFF) < THRESH)
 						botIndex = i;
 				}
 			}
 
 			int temp = 0;
 			if(topIndex > botIndex){
 				temp = botIndex;
 				botIndex = topIndex;
 				topIndex = temp;
 			}
 
 			// Get the image height.
 			imageHeight = Math.abs(botIndex - topIndex);
 
			if(imageWidth * imageHeight < 9570)
 				continue;
 
 			if(imageWidth > imageHeight)
 				largestDim = imageWidth;
 			else
 				largestDim = imageHeight;
 
 			// now create a new largest dimension^2 Bitmap to hold our scaled image.
 			character = new BufferedImage(largestDim, largestDim, BufferedImage.TYPE_INT_RGB);
 
 			int white = -1;
 			int black = 0;
 
 			// White out all the pixels in the new Bitmap. Doing this as a precaution.
 			for(int i = 0; i < largestDim; i++) {
 				for(int j = 0; j < largestDim; j++) {
 					character.setRGB(j, i, white);
 				}
 			}
 
 			// now center the old bitmap image on the new bitmap (presumably the new image is wider, but it could be taller)
 			int startWidth = largestDim/2 - imageWidth/2;
 			int startHeight = largestDim/2 - imageHeight/2;
 			for(int i = 0; i < imageHeight; i++) {
 				for(int j = 0; j < imageWidth; j++){
 					if((source.getRGB(pair.get(0)+j, topIndex+i) & 0xFF) < THRESH)
 						character.setRGB(startWidth + j, startHeight + i, black);
 				}
 			}
 
 			// Fill in the array of booleans according to the criterion that dictates a full pixel.
 			neuralInput = new boolean[outputHeight][outputWidth];
 
 			int count;
 			int width = (int) Math.ceil(((double)largestDim)/outputWidth);
 			int height = (int) Math.ceil(((double)largestDim)/outputHeight);
 
 			//crop.setImageBitmap(character);
 			for(int i = 0; i < outputHeight; i ++){
 				for(int j = 0; j < outputWidth; j++){
 					count = 0;
 					for(int imagei = i * height; imagei < (i+1) * height; imagei++) {
 						for(int imagej = j * width; imagej < (j+1) * width; imagej++){
 							if(imagej >= largestDim || imagei >= largestDim)
 								continue;
 							if((character.getRGB(imagej,imagei) & 0xFF) < THRESH)
 								count++;		
 						}
 					}
 
 					if(count > 0)
 						neuralInput[i][j] = true;
 				}
 			}
 
 			ret.add(neuralInput);
 		}
 
 		return ret;
 	}
 
 	public static void main(String[] args) {
 		ImageProcessor proc = new ImageProcessor();
 		
 		//String name = "1+2";
 		
 		String cwd = System.getProperty("user.dir");
 		File workingDir = new File(cwd + File.separator + "inputs");
 		FilenameFilter nameFilter = new FilenameFilter() {
 			public boolean accept(File file, String name){
 				return name.endsWith(".jpg");
 			}
 		};
 		
 		String outpath = cwd + File.separator + "output";
		//for( File image: workingDir.listFiles(nameFilter) ) {
		{ File image = workingDir.listFiles(nameFilter)[11];
       String name = image.getName().split("\\.")[0];
 			System.out.println(name);
 			proc.printCharacters(image, outpath, name);
 		}
 		
 		//String path = cwd + File.separator + name + ".jpg",
 		//proc.printCharacters(path, outpath, name);
 		
 	}
 }
