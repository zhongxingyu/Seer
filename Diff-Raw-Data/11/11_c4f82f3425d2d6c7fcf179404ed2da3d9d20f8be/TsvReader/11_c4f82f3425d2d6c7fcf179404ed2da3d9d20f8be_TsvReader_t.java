 package edu.kpi.fbp.network.components;
 
import java.io.BufferedInputStream;
import java.net.URL;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import com.jpmorrsn.fbp.engine.OutPort;
 import com.jpmorrsn.fbp.engine.OutputPort;
 
 import edu.kpi.fbp.javafbp.ParameterizedComponent;
 import edu.kpi.fbp.network.datastucts.NamedArray;
 import edu.kpi.fbp.params.ComponentParameter;
 import edu.kpi.fbp.params.ParameterBundle;
 import edu.kpi.fbp.params.ParameterType;
 
 /**
  * TSV files reader.
  *
  * @author Pustovit Michael, pustovitm@gmail.com
  */
 @ComponentParameter(name = TsvReader.PARAM_FILE_URL, type = ParameterType.STRING, defaultValue = "")
 @OutPort(value = TsvReader.PORT_OUT_COLUMNS, arrayPort = true, type = NamedArray.class)
 public class TsvReader extends ParameterizedComponent {
   private static final String SEPARATOR = "[ \\t]+";
   private static final String NULL_ENTRY = "*";
 
   public static final String PARAM_FILE_URL = "fileUrl";
   public static final String PORT_OUT_COLUMNS = "COLUMNS";
 
   private String fileUrl;
   private OutputPort[] outPorts;
 
   @Override
   public void setParameters(final ParameterBundle arg0) {
     fileUrl = arg0.getString(PARAM_FILE_URL);
   }
 
   @Override
   protected void execute() throws Exception {
    final URL tsvFileUrl = new URL(fileUrl);
 
    final Scanner scanner = new Scanner(new BufferedInputStream(tsvFileUrl.openStream()));
     final String[] titles = scanner.nextLine().split(SEPARATOR);
     final Column[] columns = new Column[titles.length];
 
     for (int i = 0; i < titles.length; i++) {
       columns[i] = new Column(titles[i]);
     }
 
     while (scanner.hasNextLine()) {
       final String line = scanner.nextLine();
 
       final String[] entries = line.split(SEPARATOR);
 
       for (int i = 0; i < entries.length && i < titles.length; i++) {
         if (entries[i].equals(NULL_ENTRY)) {
           columns[i].add(null);
         } else {
           columns[i].add(Double.parseDouble(entries[i]));
         }
       }
     }
 
     scanner.close();
 
     for (final OutputPort port : outPorts) {
       for (final Column col : columns) {
         port.send(create(col));
       }
 
       port.close();
     }
   }
 
   @Override
   protected void openPorts() {
     outPorts = openOutputArray(PORT_OUT_COLUMNS);
   }
 
 
   /**
    * TSV file column object model.
    *
    * @author Pustovit Michael, pustovitm@gmail.com
    */
   public static class Column extends ArrayList<Double> implements NamedArray<Double> {
     private static final long serialVersionUID = -2978161943480846484L;
 
     private final String title;
 
     /**
      * @param title the column title
      */
     public Column(final String title) {
       this.title = title;
     }
 
     /**
      * @see edu.kpi.fbp.network.datastucts.NamedArray#getName()
      * @return the list name
      */
     public String getName() {
       return title;
     }
 
     @Override
     public String toString() {
       final StringBuilder strBuilder = new StringBuilder();
 
       strBuilder.append(title);
       strBuilder.append("\n");
 
       for (final Double d : this) {
         strBuilder.append(d);
         strBuilder.append("\n");
       }
 
       return strBuilder.toString();
     }
   }
 }
