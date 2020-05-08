 package edu.kpi.asu.rduboveckij.cam.utils;
 
 import static edu.kpi.asu.rduboveckij.cam.utils.CommonUtils.randomBool;
 
 import java.io.File;
 
 import android.util.Log;
 
 import edu.kpi.asu.rduboveckij.cam.db.LogTime;
 import edu.kpi.asu.rduboveckij.cam.db.LogTimeDA;
 import edu.kpi.asu.rduboveckij.cam.db.Precedent;
 import edu.kpi.asu.rduboveckij.cam.db.PrecedentDA;
 
 public class GenTestData {
 	private PrecedentDA prDa;
 	private LogTimeDA ltDa;
 
 	public GenTestData(File path) {
 		DatabaseConfig.init(path);
 		prDa = new PrecedentDA();
 		ltDa = new LogTimeDA();
 	}
 	/**
 	 * Generation random [0.5, 1.5] time
 	 * @param count of LogTime size
 	 */
 	public void genLogTime(int count) {
 		Log.i("CAM->GenTestData", "Start LogTime");
 		for (int i : Range.range(0, count))
 			ltDa.save(new LogTime(Math.random() + 0.5));
 		Log.i("CAM->GenTestData size=", count + "");
 	}
 	/**
 	 * Generation random Precedent
 	 * rule generation this rule for test data generation 
 	 * if all rule >=0? 1: 0 
 	 * @param stepC CPU load step in interval [0,100]
 	 * rule generation: [25, 75]; 2, random, -2
 	 * @param stepM Memory load step in interval [50,100]
 	 * rule generation: [70, 90]; 1, random * 2, -2
 	 * @param stepE Energy level step in interval [0,100]
 	 * rule generation: [25, 75]; -3, random, 1
 	 */
 	public void genPrecedent(int stepC, int stepM, int stepE) {
 		Log.i("CAM->GenTestData", "Start Precedent");
 		int i = 0;
 		for (int ic : Range.range(0, 100, stepC)) {
 			int resultC = checkRule(25, 75, ic, 2, randomBool(), -2);
 			for (int im : Range.range(50, 100, stepM)) {
 				int resultM = checkRule(70, 90, im, 1, randomBool() * 2, -2);
 				for (int ie : Range.range(0, 100, stepE)) {
 					int result = resultM + resultC
 							+ checkRule(25, 75, ie, -3, randomBool(), 1);
 					Precedent pr = new Precedent(result >= 0 ? 1.0 : 0.0,
 							ic / 100.0, im / 100.0, ie / 100.0);
 					prDa.save(pr);
 					Log.i("CAM->GenTestData", pr.toString());
 					i++;
 				}
 			}
 		}
 		Log.i("CAM->GenTestData size=", i + "");
 	}
 	
 	/**
 	 * Rule generation
 	 * @param min limit
 	 * @param max limit
 	 * @param i current index
 	 * @param r1 if in min limit
 	 * @param r2 if in min and max limit
 	 * @param r3 if in max limit
 	 * @return r1 or r2 or r3
 	 */
 	public static int checkRule(int min, int max, int i, int r1, int r2, int r3) {
		if (i <= min)
			return r1;
		else if (i > min && i < max)
			return r3;
		else if (i >= max)
			return r3;
 		return 0;
 	}
 
 }
