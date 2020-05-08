
 import org.neuroph.core.NeuralNetwork;
 import org.neuroph.core.learning.DataSet;
 import org.neuroph.imgrec.ColorMode;
 import org.neuroph.imgrec.FractionRgbData;
 import org.neuroph.imgrec.ImageRecognitionHelper;
 import org.neuroph.imgrec.ImageRecognitionPlugin;
 import org.neuroph.imgrec.ImageUtilities;
 import org.neuroph.imgrec.image.Dimension;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.imageio.ImageIO;
 import org.neuroph.util.TransferFunctionType;
 
 
 public class FacialRecognizer {
 
 	/*
 	 * @param dir1 - directory of all cropped images to be trained e.g. "C://images/"
 	 * @param dir2 - location of image to be recognized e.g. "C://images/recognize/jack.png"
 	 * @param imageName - name of image to be recongnized e.g. "jack"
 	 * 
 	 * OUTPUT: String of filename of that file in training directory
 	 */
 	public static String callNetwork(String dir, String dir2,String imageName) throws FileNotFoundException, IOException {
 		//dir = "/Users/bradg0620/Dropbox/Projects/Java/Facial-Recognition/Users"; // directory of images for training
 		//dir2 = "/Users/bradg0620/Dropbox/Projects/Java/Facial-Recognition/Users/Brad.png";		//location of file to be identified
 		//imageName = "Brad";
 		String answer = "";
 
         answer = identify(dir, dir2, imageName);
 		
 		return answer;
 		
 	}
 	
 	public static String identify(String dir1, String dir2, String imageName){
 		List<Node> ary = new ArrayList<Node>();
 		Node aNode;
 		int i = 0;
 		String key = "";
 		String fileName = "";
 		
 		final File dir = new File(dir1);
 		for(File imgFile : dir.listFiles()) {
 			if(!imgFile.isHidden()){
 				String path = imgFile.getAbsolutePath();
 				String newPath = path.replace("\\", "/");
 			//	System.out.println(newPath);
 				fileName = imgFile.getName();
 				System.out.println("FileName: " + fileName);
 				System.out.println("New Path: " + newPath);
 				System.out.println("Dir 2 " + dir2);
				if(!newPath.contains("png"))
					break;
				aNode = runNN(newPath, dir2, fileName);
				ary.add(aNode);
 			}
 		}
 		
 		double lowest = ary.get(0).getFieldTwo();
 		int indexOfLowest = 0;
		for(i = 0; i<ary.size()-1; i++) {
 			if(ary.get(i).getFieldTwo() < lowest) {
 				lowest = ary.get(i).getFieldTwo();
 				indexOfLowest = i;
 			}			
 		}
 	//	System.out.println("index: "+indexOfLowest);
 		key = ary.get(indexOfLowest).getFieldOne();
 		
 		return key;
 	}
 	
 	
 	/*
 	 * @param dir1 - directory of all cropped images to be trained e.g. "C://images/"
 	 * @param dir2 - location of image to be recognized e.g. "C://images/recognize/jack.png"
 	 * @param imageName - name of image to be recongized e.g. "jack"
 	 */
 	public static Node runNN(String dir1, String dir2, String imageName){
 		
 		HashMap<String, BufferedImage> imageInput = new HashMap<String, BufferedImage>();
 		List<String> ImageLabels = new ArrayList<String>();
 		String label = "first";
 		Dimension samplingResolution = new Dimension(1,1);
 		ColorMode colorMode = ColorMode.BLACK_AND_WHITE;
 		List<Integer> layersNeuronsCount = new ArrayList<Integer>();
 		TransferFunctionType transferFunctionType = TransferFunctionType.SIGMOID;
 		Node aNode = null;
 
 	    BufferedImage img = null;
 	     try {
 	         File imagefile = new File(dir1);
 	         img = ImageIO.read(imagefile);   
 	     } catch (IOException e) {
 	     	System.out.println("IOException");  
 	     }
 
 		samplingResolution.setHeight(img.getHeight());
 		samplingResolution.setWidth(img.getWidth());
         imageInput.put(imageName, img);
         ImageLabels.add(imageName);
            
         ImageUtilities image = new ImageUtilities();
         
         Map<String, FractionRgbData> imageMap = ImageUtilities.getFractionRgbDataForImages(imageInput);
         
         ImageRecognitionHelper imageRecognitionHelper = new ImageRecognitionHelper();
         
         DataSet trainingSet = ImageRecognitionHelper.createBlackAndWhiteTrainingSet(ImageLabels, imageMap);
         
         NeuralNetwork neuralNetwork = ImageRecognitionHelper.createNewNeuralNetwork(label, samplingResolution, colorMode, ImageLabels, layersNeuronsCount, transferFunctionType);
         
         neuralNetwork.learn(trainingSet);
         neuralNetwork.save("testNN");
         NeuralNetwork nnet = NeuralNetwork.load("testNN");
         
         ImageRecognitionPlugin imageRecognition =  (ImageRecognitionPlugin)nnet.getPlugin(ImageRecognitionPlugin.class);
         
         try {
             // image recognition is done here (specify some existing image file)
            HashMap<String, Double> output = imageRecognition.recognizeImage(new File(dir2));
            Object[] anArray = output.keySet().toArray();
            String key = (String)anArray[0];
            Double num = output.get(key);
            aNode = new Node(key, num);
          //  System.out.println(output.toString());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }		
 				
 		return aNode;
 		
 		
 	}		
 }
 
 
