 package icp;
 
import icp.algorithm.mp.DetectionAtom;
 import icp.application.OnlineDetection;
 import icp.application.classification.ERPClassifierAdapter;
 import icp.application.classification.IERPClassifier;
 import icp.application.classification.MLPClassifier;
 import icp.online.app.OnLineDataProvider;
 
 /**
  * Hlavn� spou�t�c� t��da aplikace.
  * 
  */
 public class Main {
 
 	public static void main(String[] args) {
 		//new SessionManager().startGui();
 		IERPClassifier classifier = new MLPClassifier();
 		classifier.load("data/classifier.txt");
 		OnlineDetection detection = new OnlineDetection(classifier);
		
		OnLineDataProvider odp = new OnLineDataProvider("147.228.64.220", 51244, detection);
 	}
 }
