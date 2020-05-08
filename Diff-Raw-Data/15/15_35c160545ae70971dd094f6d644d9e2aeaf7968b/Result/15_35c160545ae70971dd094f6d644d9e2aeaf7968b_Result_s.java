 package edu.ntnu.idi.goldfish;
 
import java.util.HashMap;
import java.util.LinkedHashMap;
 import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.benchmark.quality.QualityStats.RecallPoint;
import org.apache.mahout.cf.taste.eval.IRStatistics;

 
 public class Result {
 
 	Evaluation evaluation;
 	
 	// RMSE
 	double RMSE;
 	
 	// AAD/MAE 
 	double AAD;
 	
 	// IR stats (precision, recall)
 	double precision;
 	double recall;
 	
 	// model build timing
 	long buildTime;
 	
 	// recommendation timing
 	long recTime;
 	
 	public Result(Evaluation evaluation, double RMSE, double AAD, double precision, double recall, long buildTime, long recTime) {
 		this.evaluation = evaluation;
 
 		this.RMSE = RMSE;
 		this.AAD = AAD;
 		
 		this.precision = precision;
 		this.recall = recall;
 		
 		this.buildTime = buildTime;
 		this.recTime = recTime;
 	}
 	
 	public int getTopN() {
 		return evaluation.getTopN();
 	}
 	
 	public double getKTL() {
 		return evaluation.getKTL();
 	}
 	
 	public String getSimilarity() {
 		return evaluation instanceof MemoryBased ? ((MemoryBased) evaluation).similarity.toString() : "";
 	}
 	
 	public String toString() {
 		
 		Formatter formats = new Formatter();
		formats.put("%-9s", evaluation.toString());
 		formats.put("%19s", getSimilarity());
 		formats.put("K/T/L: %5.2f", getKTL());
 		formats.put("Top-N: %3d", getTopN());
 //		formats.put("RMSE: %6.3f", RMSE);
 //		formats.put("AAD: %6.3f", AAD);
 		formats.put("Precision: %6.3f", precision);
 		formats.put("Recall: %6.3f", recall);
		formats.put("Build time: %3d", buildTime);
		formats.put("Rec time: %3d", recTime);
 
 		return formats.join(" | ");
 	}
 	
 	public String toCSV() {
 		Formatter formats = new Formatter();
 		formats.put("%s", evaluation.toString());
 		formats.put("%s", getSimilarity());
 		formats.put("%.2f", getKTL());
 		formats.put("%d", getTopN());
 //		formats.put("%.3f", RMSE);
 //		formats.put("%.3f", AAD);
 		formats.put("%.3f", precision);
 		formats.put("%.3f", recall);
 		formats.put("%d", buildTime);
 		formats.put("%d", recTime);
 		
 		return formats.join(",");
 	}
 	
 	public static Result getAverage(List<Result> results) {
 		int N = results.size();
 		double totalRMSE = 0, totalAAD = 0, totalPrecision = 0, totalRecall = 0;
 		long totalBuildTime = 0, totalRecTime = 0;  
 		for(Result res : results) {
 			totalRMSE += res.RMSE;
 			totalAAD += res.AAD;
 			totalPrecision += res.precision;
 			totalRecall += res.recall;
 			totalBuildTime += res.buildTime;
 			totalRecTime += res.recTime;
 		}
 		return new Result(results.get(0).evaluation, totalRMSE / N, totalAAD / N, totalPrecision / N, totalRecall / N, totalBuildTime / N, totalRecTime / N);
 	}
 }
