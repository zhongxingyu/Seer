 package edu.ntnu.idi.goldfish;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 
 import edu.ntnu.idi.goldfish.Main.DataSet;
 
 public class EvaluationResults extends ArrayList<Result> {
 	private static final long serialVersionUID = 7410339309503861432L;
 
 	public static enum SortOption {
 		RMSE, AAD, Precision, Recall
 	}
 	
 	public void sortOn(final SortOption option) {
 		Collections.sort(this, new Comparator<Result>() {  
 			public int compare(Result self, Result other) {
 				double a =0, b = 0;
 				switch(option) {
 					case RMSE: a = self.RMSE; b = other.RMSE; break;
 					case AAD: a = self.AAD; b = other.AAD; break; 
 					case Precision: a = self.precision; b = other.precision; break;
 					case Recall: a = self.recall; b = other.recall; break;
 				}
 				return (a > b) ? -1 : (a < b) ? 1 : 0;
 			}
 		});
 	}
 	
 	public void print(SortOption sortOn) {
 		sortOn(sortOn);
 		print();
 	}
 	
 	public void print() {
 //			System.out.format("%-40s | RMSE: %6s | AAD: %6f | Precision: %6.3f | Recall %6.3f | Build time %7.4f | Rec time %7.4f");
 		for (Result res : this) {
 			System.out.println(res);
 		}
 	}
 	
 	public String toCSV(SortOption sortOn) {
 		sortOn(sortOn);
 		return toCSV();
 		
 	}
 	
 	public String toCSV() {
 		String out = "";
		out += "Recommender,Similarity,KTL,TopN,Precision,Recall,Build time,Rec time\n";
 		for (Result res : this) {
 			out += res.toCSV()+"\n";
 		}
 		return out;
 	}
 	
 	public void save(DataSet set) {
 		Writer writer = null;
 		 
         try {
         	String dateTime = String.format("%1$tY-%1$tm-%1$td-%1$tH%1$tM%1$tS", new Date());
         	String output = toCSV();
             String fileName = String.format("results/%s-%s.csv", dateTime, set.toString());
             File file = new File(fileName);
             writer = new BufferedWriter(new FileWriter(file));
             writer.write(output);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 if (writer != null) {
                     writer.close();
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
 }
