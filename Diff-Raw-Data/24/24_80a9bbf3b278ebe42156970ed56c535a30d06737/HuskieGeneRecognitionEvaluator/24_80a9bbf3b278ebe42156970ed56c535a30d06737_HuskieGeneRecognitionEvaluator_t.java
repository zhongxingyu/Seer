 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
 import java.util.HashMap;
 import org.apache.uima.cas.CAS;
 import org.apache.uima.collection.CasConsumer_ImplBase;
 import org.apache.uima.resource.ResourceProcessException;
 
 /**
  * CasConsumer in charge of evaluating the performance of an annotation result
  * against a reference file. Prints out the precision, recall and F1 score
  * to standard output. 
  * 
  * @author yuchenz
  *
  */
 public class HuskieGeneRecognitionEvaluator extends CasConsumer_ImplBase {
 
   public static final String PARAM_REFERENCE_PATH = "referencePath";
 
   public static final String PARAM_RESULT_PATH = "resultPath";
 
   @Override
   public void processCas(CAS aCAS) throws ResourceProcessException {
     try {
       System.err.println("HuskieGeneRecognitionEvaluator ... ");
 
       String referenceFilename = ((String) getConfigParameterValue(PARAM_REFERENCE_PATH)).trim();
       String resultFilename = ((String) getConfigParameterValue(PARAM_RESULT_PATH)).trim();
 
       System.err.printf("Using reference file %s ... \n", referenceFilename);
       System.err.printf("Using result file %s ... \n", resultFilename);
 
       BufferedReader fileReader = null;
      InputStream referenceInputStream = this.getClass().getResourceAsStream(referenceFilename);
      // fileReader = new BufferedReader(new FileReader(referenceFilename));
      fileReader = new BufferedReader(new InputStreamReader(referenceInputStream));
 
       String line = "";
       HashMap<String, Integer> referenceList = new HashMap<String, Integer>();
       try {
         while ((line = fileReader.readLine()) != null) {
           line = line.replaceAll("\n", "");
           referenceList.put(line, 0);
         }
       } catch (IOException e) {
         System.err.printf("Unable to read from reference file %s ... \n", referenceFilename);
         try {
           fileReader.close();
         } catch (IOException e1) {
         }
         return;
       }
       try {
         fileReader.close();
       } catch (IOException e) {
         return;
       }
 
       int relevantCount = referenceList.size();
       int retrievedCount = 0;
       int retrievedRelaventCount = 0;
 
       // go through every line in the result file, see if it appear in the 
       // reference file (exact match)
       BufferedReader resultReader = null;
       try {
         resultReader = new BufferedReader(new FileReader(resultFilename));
       } catch (FileNotFoundException e) {
         System.err.printf("Unable to read result file %s ... \n", resultFilename);
         return;
       }
 
       try {
         while ((line = resultReader.readLine()) != null) {
           line = line.replaceAll("\n", "");
           if (referenceList.containsKey(line)) {
             retrievedRelaventCount++;
           }
           retrievedCount++;
         }
       } catch (IOException e) {
         System.err.printf("Unable to read from result file %s ... \n", referenceFilename);
         try {
           resultReader.close();
         } catch (IOException e1) {
         }
         return;
       }
       try {
         resultReader.close();
       } catch (IOException e) {
       }
 
       // prints out the stats. 
       double precision = ((double) retrievedRelaventCount) / ((double) retrievedCount);
       double recall = ((double) retrievedRelaventCount) / ((double) relevantCount);
       double fscore = 2 * precision * recall / (precision + recall);
       System.out.printf("Precision=%.6f\n", precision);
       System.out.printf("Recall=%.6f\n", recall);
       System.out.printf("F1 score=%.6f\n", fscore);
     } catch (Exception e) {
       System.err.printf("Unable to calculate stats.\n");
     }
   }
 
   /**
    * Unit testing 
    * @param args
    * @throws ResourceProcessException
    */
   public static void main(String args[]) throws ResourceProcessException {
     HuskieGeneRecognitionEvaluator eval = new HuskieGeneRecognitionEvaluator();
     ((HuskieGeneRecognitionEvaluator) eval).processCas((CAS) null);
   }
 
 }
