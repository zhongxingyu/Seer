 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.uima.jcas.JCas;
 
 /**
  * The regular expression model for gene name recognition
  * @author yuchenz
  *
  */
 public class RegexModel extends BaseModel {
 
   protected ArrayList<Pattern> regexList = null;
 
   /**
    * Constructor with model path as parameter. 
    * @param regexModelPath path to the model file. 
    * @throws IOException can't find or read the model file. 
    */
   public RegexModel(String regexModelPath) throws IOException {
     regexList = new ArrayList<Pattern>();
     loadModelFromFile(regexModelPath);
   }
   
   /**
    * Never used plain constructor. 
    */
   private RegexModel() {
 
   }
 
   /**
    * Loads the model from file. 
    */
   public void loadModelFromFile(String regexModelPath) throws IOException {
    BufferedReader fileReader = new BufferedReader(new FileReader(regexModelPath));
     String line = null;
 
     while ((line = fileReader.readLine()) != null) {
       line = line.replaceAll("\n", "");
       Pattern ptn = Pattern.compile(line);
       regexList.add(ptn);
     }
 
     fileReader.close();
   }
 
   @Override
   public GeneAnnotation[] annotateLine(String line, JCas aJCas) {
     ArrayList<GeneAnnotation> annotationList = new ArrayList<GeneAnnotation>();
 
     for (int i = 0; i < regexList.size(); i++) {
       Pattern regex = regexList.get(i);
       Matcher matcher = regex.matcher(line);
 
       while (matcher.find()) {
         int begin = matcher.start();
         int end = matcher.end() - 1;
         String gene = line.substring(begin, end + 1);
         
         // subtract number of white spaces from begin and end
         int spaceNumBegin = 0;
         for (int idx = 0; idx <= begin; idx++) {
           if (Character.isWhitespace(line.charAt(idx))) {
             spaceNumBegin++;
           }
         }
         
         int spaceNumEnd = 0;
         for (int idx = 0; idx <= end; idx++) {
           if (Character.isWhitespace(line.charAt(idx))) {
             spaceNumEnd++;
           }
         }
         begin -= spaceNumBegin;
         end -= spaceNumEnd;
         
         GeneAnnotation geneAnnot = new GeneAnnotation(aJCas, begin, end);
         geneAnnot.setGene(gene);
 
         // System.err.printf("\n\t%s located %s from %d to %d ... ", regex.pattern(), gene, begin, end);
         
         annotationList.add(geneAnnot);
       }
     }
 
     GeneAnnotation[] annotations = new GeneAnnotation[annotationList.size()];
     annotationList.toArray(annotations);
 
     return annotations;
   }
   
   /**
    * Unit testing
    * @param args
    * @throws IOException
    */
   public static void main(String[] args) throws IOException {
     String modelPath = "src/main/resources/data/regex_1.model";
     RegexModel rm = new RegexModel(modelPath);
     // rm.annotateLine(line, aJCas)
   }
 }
