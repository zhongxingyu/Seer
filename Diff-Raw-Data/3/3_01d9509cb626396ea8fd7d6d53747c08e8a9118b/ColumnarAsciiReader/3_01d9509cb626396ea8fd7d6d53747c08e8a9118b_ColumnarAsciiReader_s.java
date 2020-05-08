 package lasp.tss.iosp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 public class ColumnarAsciiReader extends AsciiGranuleReader {
     
     // Initialize a logger.
     private static final Logger _logger = Logger.getLogger(ColumnarAsciiReader.class);
     
     private List<int[]> _varColumns;
     
     
     protected void init() {
         //get the columns for each variable
         _varColumns = new ArrayList<int[]>();
         
         String columns = getProperty("columns");
         //TODO: error if null
         for (String cols : columns.split(";")) {
             String[] cs = cols.split(",");
             int[] icols = new int[cs.length];
             int i = 0;
             for (String c : cs) {
                 icols[i++] = Integer.parseInt(c);
             }
             _varColumns.add(icols);
         }
     }
     
     protected String[] parseRecord(String record) {
         //TODO: performance concern, called for every time sample
         String delim = getDelimiter();
         String[] columns = record.split(delim);
         
         int nvar = _varColumns.size();
         String[] ss = new String[nvar];
 
         try {
             for (int ivar=0; ivar<nvar; ivar++) {
                 for (int i : _varColumns.get(ivar)) {
                     if (ss[ivar] == null) ss[ivar] = columns[i-1]; //columns start at 1
                     else ss[ivar] += " " + columns[i-1]; 
                 }
             }
         } catch (Exception e) {
             String msg = "Unable to parse record: " + record;
            _logger.warn(msg, e);
            e.printStackTrace();
         }
         
         return ss;
     }
     
 }
