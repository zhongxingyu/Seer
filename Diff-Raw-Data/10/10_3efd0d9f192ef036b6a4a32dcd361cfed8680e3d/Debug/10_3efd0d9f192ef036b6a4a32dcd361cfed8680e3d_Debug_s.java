 package edu.mit.csail.ada;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.os.Environment;
 import android.util.Log;
 
 public class Debug {
 
 	static final String msgDir = Environment.getExternalStorageDirectory()
 			.getPath() + "/AdaLog/";
 	static File file;
 	static String fileName;
 	static FileWriter filewriter;
 	static BufferedWriter out;
 
 	public static void init() {
 
 		new File(msgDir).mkdir();
 		// log file name
 		SimpleDateFormat dateFormat = new SimpleDateFormat(
 				"yyyy-MM-dd-HH-mm-ss");
 		String fileName = dateFormat.format(new Date());
 		file = new File(msgDir, fileName);
 
 		try {
 			filewriter = new FileWriter(file);
 			out = new BufferedWriter(filewriter);
 		} catch (IOException e) {
 			Log.e("Debug", "Could not write file " + e.getMessage());
 		}
 
 	}
 
 	public static void logPrediction(long time, int groundTruth,
 			int prediction, double[] accel_post, double[] wifi_post,
 			double[] gps_post, double[] softVoting_prob, double[] ewma_prob,
 			double[] accel_feature, double wifi_feature, double gps_feature,
 			int state, int googlePrediction) {
 		String accelPostStr = "";
 		String wifiPostStr = "";
 		String gpsPostStr = "";
 		String softVotingStr = "";
 		String ewmaStr = "";
 		String accelFeatureStr = "";
 
 		DecimalFormat df = new DecimalFormat("#.####");
 		for (int i = 0; i < Global.ACTIVITY_NUM; ++i) {
 			if (i == 0) {
 				accelPostStr += df.format(accel_post[i]);
 				wifiPostStr += df.format(wifi_post[i]);
 				gpsPostStr += df.format(gps_post[i]);
 				softVotingStr += df.format(softVoting_prob[i]);
 				ewmaStr += df.format(ewma_prob[i]);
 			} else {
 				accelPostStr += "," + df.format(accel_post[i]);
 				wifiPostStr += "," + df.format(wifi_post[i]);
 				gpsPostStr += "," + df.format(gps_post[i]);
 				softVotingStr += "," + df.format(softVoting_prob[i]);
 				ewmaStr += "," + df.format(ewma_prob[i]);
 			}
 
 		}
 
 		for (int i = 0; i < Global.ACCEL_FEATURE_NUM; ++i) {
 			if (i == 0) {
 				accelFeatureStr += df.format(accel_feature[i]);
 			} else {
 				accelFeatureStr += "," + df.format(accel_feature[i]);
 			}
 		}
 
 		try {
 
 			String outputString = time + ";" + groundTruth + ";" + prediction
 					+ ";" + ewmaStr + ";" + softVotingStr 
 					+ ";" + accelPostStr + ";" + wifiPostStr + ";" + gpsPostStr
 					+ ";" + accelFeatureStr + ";" + wifi_feature + ";"
 					+ gps_feature + ";" + state + ";" + googlePrediction + "\n";
 
 			if (file.canWrite()) {
 				out.write(outputString);
 			} else {
 				Log.e("Debug", "Cannot write into file.");
 			}
 		} catch (IOException e) {
 
 		}
 	}
 
 	public static void stop() {
 		try {
 			out.flush();
 			filewriter.flush();
 			out.close();
 			filewriter.close();
 		} catch (IOException e) {
 			Log.e("Debug", "Could not close file " + e.getMessage());
 		}
 
 	}
 }
