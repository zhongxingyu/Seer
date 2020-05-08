 package de.thatsich.bachelor.prediction.intern.service;
 
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.opencv.core.Core;
 import org.opencv.core.CvType;
 import org.opencv.core.Mat;
 import org.opencv.highgui.Highgui;
 
 import de.thatsich.bachelor.prediction.api.entities.BinaryPrediction;
 
 public class BinaryPredictionFileStorageService {
 	public void save(BinaryPrediction prediction) {
 		// Preprare each information
 		final String file = prediction.getFilePathProperty().get().toAbsolutePath().toString();
 		final Mat firstLayer = prediction.getWithErrorProperty().get();
 		final Mat secondLayer = prediction.getErrorIndicationProperty().get();
 		final Mat thirdLayer = prediction.getErrorPredictionProperty().get();
 		
 		// Merge every layer into a new Mat with 8UC3
 		final List<Mat> listMat = Arrays.asList(firstLayer, secondLayer, thirdLayer);
 		final Mat mergedMat = new Mat(firstLayer.size(), CvType.CV_8UC3);
 		Core.merge(listMat, mergedMat);
 		
 		// Write to FS
 		Highgui.imwrite(file, mergedMat);
 	}
 	
	public BinaryPrediction load(Path filePath) {
 		final Mat layeredImage = Highgui.imread(filePath.toAbsolutePath().toString());
 		
 		// split channels to extract GL and Error Mat
 		final List<Mat> layeredImageChannelMats = new ArrayList<Mat>(); 
 		Core.split(layeredImage, layeredImageChannelMats);
 		
 		final Mat firstLayer = layeredImageChannelMats.get(0);
 		final Mat secondLayer = layeredImageChannelMats.get(1);
 		final Mat thirdLayer = layeredImageChannelMats.get(2);
 		
		// TODO temp fix
		return new BinaryPrediction(filePath, firstLayer, secondLayer, thirdLayer, "", "", 0, "", "");
 	}
 }
