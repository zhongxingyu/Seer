 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Set;
 import java.util.Map;
 import java.util.LinkedHashMap;
 
 public class Chart
 {
     private Map<String, String> data;
    
     public Chart(String[][] contents){
         // LinkedHashMap will retain insertion ordering
         this.data = new LinkedHashMap<String, String>();
         this.setRows(contents);
     }
    
     // produces the chart
     public String toString() {
        String rt = "";
 
       int labelWidth = 0, dataWidth = 0;
        
        // calculate padding for label and data columns
        for (Map.Entry<String, String> i : data.entrySet()){
            labelWidth = Math.max(i.getKey().length(), labelWidth);
            dataWidth = Math.max(i.getValue().length(), dataWidth);
        }
        
        // build header line
        String head = "";
        for (int i = 0; i < labelWidth+dataWidth+7; i++) {
            head += "-";
        }
        head += "\n";
        rt += head;
        
        // actually make the chart now
        for (Map.Entry<String, String> i : data.entrySet()){
            // use the computed padding to align labels and data left with given padding
            rt += String.format(String.format("| %s | %s |", "%-"+labelWidth+"s", "%-"+dataWidth+"s"), i.getKey(), i.getValue())+"\n";
        }
        
        // footer line
        rt += head;
        
        return rt;
    }
    
    // add a row of data
    /**
     * @param row Should be a array with length 2.  The first value will be the 
     * row label, the second the row data.
     */
    public void addRow(String[] row) {
        data.put(row[0], row[1]);
    }
    
    /**
     * Converts the row from the internal data structure to a 2D Array of strings.
     * @return Array of String arrays with length 2.  Each array is a row.  First 
     * value is label, second value is value.
     */
    public String[][] getRows() {
        Set<Map.Entry<String, String>> temp = data.entrySet();
        // finish conversion
        String[][] rows = new String[data.size()][2];
        int i = 0;
        for (Map.Entry<String, String> e: temp) {
           rows[i][0] = e.getKey();
           rows[i][1] = e.getValue();
           i++;
        }
        return rows;
    }
    
    /**
     * @param rows 2D array of form {{label, value}, {label, value}}
     */
    public void setRows(String[][] rows) {
        data.clear();
        for (String[] row: rows) {
            data.put(row[0], row[1]);
        }
    }
 }
