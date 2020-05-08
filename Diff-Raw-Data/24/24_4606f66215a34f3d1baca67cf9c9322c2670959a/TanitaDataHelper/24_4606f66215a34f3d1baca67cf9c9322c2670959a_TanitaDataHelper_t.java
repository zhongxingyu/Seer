 package ca.charland.tanita.db;
 
 import java.util.List;
 
 import ca.charland.tanita.base.db.Data;
 
 public class TanitaDataHelper {
 
 	public static double getPrevious(List<Data> tanitaData, TanitaDataTable.Column column) {
 		double prev = 0;
		int index = tanitaData.size() - 2;
		if (index >= 0) {
			TanitaData td = (TanitaData) tanitaData.get(index);
 			prev = td.get(column);
 		}
 		return prev;
 	}
 
 	public static double getAverage(List<Data> tanitaData, TanitaDataTable.Column column) {
		double avg = 0;		
		if (!tanitaData.isEmpty()) {
			double all = 0;
			for (Data d : tanitaData) {
				TanitaData td = (TanitaData) d;
				all += td.get(column);
			}
			if (all != 0) {
				avg = all / tanitaData.size();
			}
 		}
 		return avg;
 	}
 
 }
