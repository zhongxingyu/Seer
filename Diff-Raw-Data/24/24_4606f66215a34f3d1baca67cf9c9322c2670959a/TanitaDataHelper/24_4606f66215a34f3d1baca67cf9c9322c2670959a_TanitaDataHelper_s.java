 package ca.charland.tanita.db;
 
 import java.util.List;
 
 import ca.charland.tanita.base.db.Data;
 
 public class TanitaDataHelper {
 
 	public static double getPrevious(List<Data> tanitaData, TanitaDataTable.Column column) {
 		double prev = 0;
		if (!tanitaData.isEmpty()) {
			TanitaData td = (TanitaData) tanitaData.get(tanitaData.size() - 2);
 			prev = td.get(column);
 		}
 		return prev;
 	}
 
 	public static double getAverage(List<Data> tanitaData, TanitaDataTable.Column column) {
		double all = 0;
		for (Data d : tanitaData) {
			TanitaData td = (TanitaData) d;
			all += td.get(column);
		}
		double avg = 0;
		if (all != 0) {
			avg = all / tanitaData.size() ;
 		}
 		return avg;
 	}
 
 }
