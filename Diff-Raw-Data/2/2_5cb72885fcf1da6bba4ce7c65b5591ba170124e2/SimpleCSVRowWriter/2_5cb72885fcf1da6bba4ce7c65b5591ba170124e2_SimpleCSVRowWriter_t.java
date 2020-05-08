 
 package axirassa.util;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 abstract public class SimpleCSVRowWriter<T> implements CSVRowWriter<T> {
 	private int columnId = 0;
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 
 
 	public void writeCell(StringBuilder sb, Date date) {
 		writeCell(sb, dateFormat.format(date));
 	}
 
 
 	public void writeCell(StringBuilder sb, Object object) {
 		if (columnId > 0)
 			sb.append(',');
 
 		sb.append(object);
 		columnId++;
 	}
 
 
 	public void startRow() {
 		columnId = 0;
 	}
 
 
 	public void endRow() {
 		// empty
 	}
 }
