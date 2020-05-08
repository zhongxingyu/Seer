 package lasp.tss.writer;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import lasp.tss.TSSException;
 import lasp.tss.variable.CompositeVariable;
 import lasp.tss.variable.SequenceVariable;
 import lasp.tss.variable.TSSVariable;
 import lasp.tss.variable.TimeSeries;
 import lasp.tss.variable.TimeVariable;
 
 /**
  * Write a TimeSeriesDataset represented in JSON format.
  * 
  * Model the data as an array of JSON objects, one per time sample.
  * [{"time":123,"value":456},...]
  * 
  * Seems like a lot of redundancy but better than XML. 
  * And new browsers have native support.
  */
 public class JsonWriter extends TextDataWriter {
     
     // Initialize a logger.
     private static final Logger _logger = Logger.getLogger(JsonWriter.class);
     
     //keep track of whether we have printed the first sample for delimiter management
     private boolean _first = true;
     
     public String getContentType() { return "application/json"; }
     
     /**
      * Start the JSON response as an array of time samples.
      */
     public void writeHeader() {
         println("[");
     }
     
     public void writeTimeSample(int timeIndex) { 
         try {
             //Get the variabels from time series 
             TimeSeries ts = getDataset().getTimeSeries();
             List<TSSVariable> vars = ts.getVariables();
             
             //Make a JSON representation of this time sample
             String vs = makeVariables(vars, timeIndex);
             if (vs == null) return; //skip sample
             
             //prepend delimiter if not the first
             if (_first == false) vs = "," + vs;
             else _first = false;
             
             //print this time sample on its own line
             println(vs);
             
         } catch (Exception e) {
             String msg = "Unable to write the data for timeIndex: " + timeIndex;
             _logger.error(msg, e);
             throw new TSSException(msg, e);
         }
     }
     
     private String makeVariables(List<TSSVariable> variables, int timeIndex) { 
         //Create a list a variables in a JSON Object ({}).
         StringBuilder sb = new StringBuilder();
         
         //start JSON object
         sb.append("{");
         
         //make each variable
         int nvar = variables.size();
         for (int ivar=0; ivar<nvar; ivar++) {
             TSSVariable variable = variables.get(ivar);
             String v = makeVariable(variable, timeIndex); //potentially recursive
             if (v == null) return null; //skip sample
             if (ivar > 0) sb.append(",");
             sb.append(v);
         }
  
         //end JSON object
         sb.append("}"); 
         
         return sb.toString();
     }
     
     private String makeVariable(TSSVariable variable, int timeIndex) { 
         StringBuilder sb = new StringBuilder();
         
         //Add the variable name, in quotes
         sb.append("\"");
         sb.append(variable.getName());
         sb.append("\":");
         
         //Construct the JSON representation based on variable type.
         if (variable.isStructure()) { 
             //create a JSON object to contain the Structure components
             List<TSSVariable> vars = ((CompositeVariable) variable).getVariables();
             String s = makeVariables(vars, timeIndex);
             sb.append(s);
         } else if (variable.isSequence()) {
             //create a JSON array of samples
             SequenceVariable seq = (SequenceVariable) variable;
             int n = seq.getLength(); //number of samples in the sequence
             
             //Get list of projected variables
             List<String> names = seq.getVariableNames();
             int nvar = names.size(); //number of variables in the sequence
             
             //Get data values for all projected components
             double[] values = seq.getValues(timeIndex);  //variable dim varies fastest
 
             //start sequence as a JSON array
             sb.append("[");
             
             //Loop over sequence samples
             for (int i=0; i<n; i++) {
                 if (i > 0) sb.append(","); //prepend delimiter if not the first sample
                 //start collection of variables as a JSON object
                 sb.append("{");
                 for (int ivar=0; ivar<nvar; ivar++) {
                     if (ivar > 0) sb.append(","); //prepend delimiter if not the first variable
 
                     //Add the variable name, in quotes
                     sb.append("\"");
                     sb.append(names.get(ivar));
                     sb.append("\":");
                     //Add data value
                     double d = values[i*nvar + ivar];
                     String s = ""+d;
                     if (Double.isNaN(d)) s = "null"; //replace missing values with "null"
                     sb.append(s); //assume no formatting in inner sequence
                 }
                 //end sequence sample
                 sb.append("}");
             }
             
             //end sequence
             sb.append("]");
             
         } else {
             //get the data value as a string, this should be a leaf data set so only one value
             String s = null;
             
             String[] values = variable.getStringValues(timeIndex); 
             //TODO: test for indep var == NaN, skip entire time step
             if (values == null) return null;
             s = values[0];
             if (s.equals("NaN")) s = "null";
             //String values need to be quoted
             if (variable.isString()) s = "\""+s+"\"";
                         
             sb.append(s);
         }
 
         return sb.toString();
     }
     
     /**
      * Write the JSON ending array "]" after writing the time samples.
      */
     public void writeFooter() {
         println("]");
     }
 
 }
