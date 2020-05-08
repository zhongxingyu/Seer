 package de.thatsich.bachelor.opencv.extractor;
 
 import org.opencv.core.CvType;
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfFloat;
 import org.opencv.imgproc.Imgproc;
 
 import de.thatsich.core.opencv.AFeatureExtractor;
 
 
 /**
  * Applies <a href="http://en.wikipedia.org/wiki/Sobel_operator">Sobel Operator</a> 
  * on the Matrix 
  * using the <a href="http://docs.opencv.org/modules/imgproc/doc/filtering.html?highlight=sobel#sobel">OpenCV implementation</a>
  * 
  * @author Tran Minh Do
  *
  */
 public class Gradient extends AFeatureExtractor {
 
 	@Override
 	public MatOfFloat extractFeature(Mat image) {
 		if (image == null) throw new IllegalArgumentException("Image is null.");
 		if (image.type() != CvType.CV_8U) throw new IllegalArgumentException("Image is not grayscale.");
 		if (image.empty()) throw new IllegalArgumentException("Image is empty.");
 		
		MatOfFloat result = new MatOfFloat();

		Imgproc.Sobel(image, result, image.depth(), 1, 1);
 		
		return result;
 	}
 }
