 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
import javax.swing.JOptionPane;

 import com.googlecode.javacv.CanvasFrame;
 import com.googlecode.javacv.OpenCVFrameGrabber;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 import static com.googlecode.javacv.cpp.opencv_highgui.*;
 
 import org.neuroph.imgrec.ImageRecognitionHelper;
 
 public class FaceScanner{
     
 	IplImage image;
 	static boolean displayRects;
 	static String filename;
 	static String recFilename;
 	static int width, height, x, y;
 	static boolean recognize;
 	ImageRecognitionHelper helper;
 	Dimension canvasDim;
 	FaceDetector faceDetect;
 	
 	public FaceScanner() {
 		filename = "Users/temp.png";
 		recFilename = "UserAttempts/temp.png";
 		image = new IplImage();
 		width = 0;
 		height = 0;
 		x = 0;
 		y = 0;
 		recognize = false;
 		displayRects = true;
 		canvasDim = new Dimension();
 		faceDetect = new FaceDetector();
 	}
 	
 	/* scanFrame
 	 * Scans each frame through the webcam outputting video feedback.
 	 */
 	public static IplImage scanFrame(UserInterface ui, CanvasFrame canvas) throws Exception {
 	    final OpenCVFrameGrabber frameGrabber = new OpenCVFrameGrabber(0);
 	    try {
 	        canvas.setSize(640, 480);
 	        frameGrabber.start();
 	        IplImage img = frameGrabber.grab();
 	        if (img != null) {
 	            canvas.showImage(img);
 	            return img;
 	        }
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 		return frameGrabber.grab();
 	}
 	
 	/* runScan
 	 * Runs the face scanner and based on user input adjusts the number of frames.
 	 */
 	public void runScan(UserInterface ui) throws Exception {
 		CanvasFrame canvas = ui.canvas;
 		BufferedImage croppedImage;
 		canvasDim = new Dimension(640, 480);
 		faceDetect = new FaceDetector();
 		
 		while(true){
 			while(canvas.isEnabled()){
 	        	image = scanFrame(ui, canvas);
 	        	Thread.sleep(10);
 	    	}
 			if(!recognize && !(canvas.isEnabled())){	//RUNS WHEN SCAN BUTTON IS PRESSED
 				try{
 					faceDetect.DetectFaces(image);		//Used primarily to populate the width, height, and location of the face to be detected.
 					
 					if(displayRects){
 						/*Uncomment to display the detection lines and uncomment the rectangle drawing in FaceDetector class*/
 						canvas.showImage(image);
 						Thread.sleep(1000);		//Delay time to see the faces that are detected before choosing the closest.
 						//image = cvLoadImage(filename, 1); 	//Reload image to remove rectangle
 						/*----------------------------------------*/
 					}	
 					
 					croppedImage = cropImage(image.getBufferedImage());
 					croppedImage = resizeImage(croppedImage, 125, 150, false);		//Resize the image for the recognizer
 		
 					if(displayRects){
 						/*Uncomment to zoom and show the closest face*/
 						//canvas.showImage(croppedImage);
 						/*-------------------------------------------*/
 					}
 					cvSaveImage(filename, IplImage.createFrom(croppedImage));
 					
 					ui.status.setText("User " + filename.replace(".png", "").replace("Users/", "") + " was added to the system.");
 					//JOptionPane.showMessageDialog(null, "User " + filename.replace(".png", "").replace("Users/", "") + " was added to the system.");
 					filename = "Users/temp.png";
 				} catch (Exception e) {
 					ui.status.setText("There are no faces detected, please rescan to try again.");
 					//JOptionPane.showMessageDialog(null, "There are no faces detected, please rescan to try again.");
 				}
 				canvas.setEnabled(true);
 			}else if(recognize && !(canvas.isEnabled())){	//RUNS WHEN RECOGNIZE BUTTON PRESSED
 				String user = "";
 				try{
 					ui.status.setText("Scanning image...");
 					faceDetect.DetectFaces(image);		//Used primarily to populate the width, height, and location of the face to be detected.
 					canvas.showImage(image);
 					Thread.sleep(1000);		//Delay time to see the faces that are detected before choosing the closest.
 					
 					croppedImage = cropImage(image.getBufferedImage());
 					croppedImage = resizeImage(croppedImage, 125, 150, false);		//Resize the image for the recognizer
 					
 					cvSaveImage(recFilename, IplImage.createFrom(croppedImage));
 					
 					// Train the neural network with the images saved in Users and then 
 					// recognize the face by finding the user with the highest accuracy
 					// in comparing the two images.
 					ui.status.setText("Processesing recognition algorithm...");
 					user = FacialRecognizer.callNetwork("Users/", recFilename, "temp");
 					
 					ui.status.setText("User " + user.replace(".png", "") + " was detected.");
 					//JOptionPane.showMessageDialog(null, "User " + user.replace(".png", "") + " was detected with a " + 10 +  "% accuracy level.");
 					recognize = false;
 				} catch (Exception e) {
 					ui.status.setText("Unknown user.");
 					//JOptionPane.showMessageDialog(null, "Unknown user.");
 				}
 				canvas.setEnabled(true);
 			}
 				
 			Thread.sleep(100);	//Pause so the user can see the scanned image.
 		}
 	}
 	
 	/* cropImage
 	 * Crops the passed image (detected face) for image processing through the recognition section.
 	 */
 	private BufferedImage cropImage(BufferedImage src) {
 	      return src.getSubimage(x, y, width, height);
 	}
 	
 	/* changeFilename
 	 * Changes the filename to the name inputed by the user.
 	 */
 	public static void changeFilename(String name){
 		filename = filename.replace("temp.png", name + ".png");
 	}
 	
 	/* resizeImage
 	 * Resizes the BufferedImage to the specified height and width
 	 */
 	BufferedImage resizeImage(BufferedImage originalImage, int newWidth, int newHeight, boolean preserveAlpha) {
 		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
     	BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, imageType);
     	Graphics2D g = resizedImage.createGraphics();
     	g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
     	g.dispose();
     	
     	return resizedImage;
     }
 }
