 package nzgot.cma.util;
 
 /**
  * Name Space
  * @author Walter Xie
  */
 public class NameParser {
 
     //create an object of SingleObject
     private static NameParser instance = new NameParser();
 
     //make the constructor private so that this class cannot be instantiated
     private NameParser(){}
 
     //Get the only object available
     public static NameParser getInstance(){
         return instance;
     }
 
     public static final String SEPARATOR_COLUMN = "\t"; // TODO customize?
     public static final String SEPARATOR_CSV_COLUMN = ",";
     // eg IDME8NK01ETVXF|DirectSoil|LB1-A
     public static final int READ_INDEX_SAMPLE = 2;
 
     // parse name by mutli-separators in different naming level
     // allow to customize separator according to index
    private String[] separators = new String[]{"\\|", "-", "_"};
 
     public String getSeparator(int index) {
         return separators[index];
     }
 
     public void setSeparator(int index, String separator) {
         this.separators[index] = separator;
     }
 
     /**
      * parse read name into sample location, e.g. 2-C
      * @param readName
      * @return
      */
     public String getSampleFromRead(String readName) {
         // 3 fields in read name
         String[] fields = readName.split(separators[0], -1);
         if (fields.length < 3)
             throw new IllegalArgumentException("Error: invalid read name : " + readName);
 
         return fields[READ_INDEX_SAMPLE];
     }
 
     /**
      * parse sample location in the read name into plot and subplot
      * @param sample
      * @return
      */
     public String[] getPlotFromSample(String sample) {
         // plot_subplot, subplot
         String[] plot_subplot = sample.split(separators[1], -1);
         if (plot_subplot.length != 2)
             throw new IllegalArgumentException("Error: invalid sample location in the read name : " + sample);
 
         return plot_subplot;
     }
 
 
    public String getSampleBy (int samplesBy, String readName) {
        String sample = getSampleFromRead(readName);
         return samplesBy == NameSpace.BY_PLOT ? getPlotFromSample(sample)[0] : sample;
     }
 }
