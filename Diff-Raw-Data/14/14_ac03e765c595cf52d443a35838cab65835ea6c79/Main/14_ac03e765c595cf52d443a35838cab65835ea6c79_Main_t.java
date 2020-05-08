 package depend;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Map.Entry;
import java.util.Properties;
 
 import com.ibm.wala.classLoader.IClass;
 import com.ibm.wala.classLoader.IMethod;
 import com.ibm.wala.types.ClassLoaderReference;
 import com.ibm.wala.types.Selector;
 import com.ibm.wala.types.TypeReference;
 import com.ibm.wala.util.CancelException;
 import com.ibm.wala.util.WalaException;
 import com.ibm.wala.util.io.CommandLine;
 import com.ibm.wala.util.warnings.Warnings;
 import com.ibm.wala.viz.DotUtil;
 
 import depend.util.Util;
 
 public class Main {
 
   private static final String DOT_EXECUTABLE_PATH_PROPERTY_NAME = "dotPath";
 
   private static final String GRAPH_OUTPUT_PATH_PROPERTY_NAME = "graphFileOutputPath";
  private static final String DEFAULT_GRAPH_OUTPUT_PATH = System.getProperty("java.io.tmpdir") +  System.getProperty("file.separator") + "results.pdf"; 
   
   private static final String DOT_OUTPUT_PATH_PROPERTY_NAME = "dotFileOutputPath";
  private static final String DEFAULT_DOT_OUTPUT_PATH = System.getProperty("java.io.tmpdir") +  System.getProperty("file.separator") + "results.dot";
 
   /**
    * example of use for this class
    * 
    * @param args currently hard-coded (modify appJar)
    * 
    * @throws IOException
    * @throws IllegalArgumentException
    * @throws WalaException
    * @throws CancelException
    * @throws InterruptedException 
    */
   public static void main(String[] args) throws IOException, IllegalArgumentException, WalaException, CancelException, InterruptedException {
     
     // reading and saving command-line properties
     Properties p = CommandLine.parse(args);
     Util.setProperties(p);
     
     // clearing warnings from WALA
     Warnings.clear();
     
     // performing dependency analysis
     MethodDependencyAnalysis an = new MethodDependencyAnalysis(p);    
     if (Util.getBooleanProperty("printWalaWarnings")) {
       System.out.println(Warnings.asString());    
     }
     
     // finding **informed** class and method
     String strClass = Util.getStringProperty("targetClass");
     IClass clazz = an.cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application, strClass));
     if (clazz == null) {
       throw new RuntimeException("could not find class!");
     }
     String strMethod = Util.getStringProperty("targetMethod");
     IMethod method = clazz.getMethod(Selector.make(strMethod));
     if (method == null) {
       throw new RuntimeException("could not find class!");
     }
     
     
     // looking for dependencies to method
     Map<IMethod,String> map = an.getDependencies(method, false, false);
     
     String reportType = Util.getStringProperty("reportType").trim();
     
     dumpResults(method, map, reportType);
 
   }
 
   private static void dumpResults(IMethod method, Map<IMethod,String> map,
       String reportType) throws IOException, WalaException {
     if (reportType.equals("list")) {
       
       System.out.printf("data dependencies to method %s\n", method);
 
       // printing dependencies
       for (Entry<IMethod, String> m: map.entrySet()) {
         if (Util.isAppClass(m.getKey().getDeclaringClass())) {
           System.out.printf("  %s\n", m.getKey() + m.getValue());
         }      
       }
 
     } else if (reportType.equals("dot")) {
       //TODO: you may want to print before propagating 
       //data dependencies
       
       /**
        * generate dot
        */
       StringBuffer sb = new StringBuffer();
       sb.append("digraph \"DirectedGraph\" {\n");
       sb.append(" graph [concentrate = true];\n");
       sb.append(" center=true;\n");
       sb.append(" fontsize=6;\n");
       sb.append(" node [ color=blue,shape=\"box\"fontsize=6,fontcolor=black,fontname=Arial];\n");
       sb.append(" edge [ color=black,fontsize=6,fontcolor=black,fontname=Arial];\n");
       
       for (Entry<IMethod, String> m: map.entrySet()) {
         if (Util.isAppClass(m.getKey().getDeclaringClass())) {
           sb.append(m);
           sb.append(" -> ");
           sb.append(m.getKey() + m.getValue());
           sb.append("\n");
         }      
       }
       sb.append("}\n");
       
       /**
        * results.dot
        */
       String dotResultsPath = Util.getStringProperty(DOT_OUTPUT_PATH_PROPERTY_NAME, DEFAULT_DOT_OUTPUT_PATH);
       System.out.println("Outputing dot file to: " + dotResultsPath);
       File dotFile = new File(dotResultsPath);
       FileWriter fw = new FileWriter(dotFile);
       fw.append(sb);
       fw.flush();
       fw.close();
       String graphPdfPath = Util.getStringProperty(GRAPH_OUTPUT_PATH_PROPERTY_NAME, DEFAULT_GRAPH_OUTPUT_PATH);
       System.out.println("Outputing graph file to: " + graphPdfPath);
       DotUtil.spawnDot(Util.getStringProperty(DOT_EXECUTABLE_PATH_PROPERTY_NAME), graphPdfPath, dotFile);
       
     }
   }
 }
