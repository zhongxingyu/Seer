 package vafusion.recog;
 
 import java.awt.image.BufferedImage;
 import org.neuroph.contrib.ocr.OcrPlugin;
 import org.neuroph.core.NeuralNetwork;
 
 public class CharacterRecognizer implements Recognizer {
 
 	NeuralNetwork nnet; // = new MultiLayerPerceptron(75, 50);
 	
 	public CharacterRecognizer(String filename) {
 		
 		System.out.println("Loading neural net...");
		nnet = NeuralNetwork.load(filename);
 		System.out.println("Done loading neural net.");
 		
 	}
 	
 	@Override
 	public vafusion.recog.Character match(Object arg) {
 		
 		BufferedImage charimg = (BufferedImage)arg;
 		OcrPlugin plugin = (OcrPlugin)nnet.getPlugin(OcrPlugin.OCR_PLUGIN_NAME);
 		return Character.mapCharacter(plugin.recognizeCharacter(charimg));
 		
 	}
 
 }
