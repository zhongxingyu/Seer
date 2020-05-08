 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Set;
 
 
 public class ScannerGenerator {
   public static void main(String[] args) {
     if (args.length < 1) {
       System.out.println("Usage: java ScannerGenerator <spec-file>");
       System.exit(1);
     }
 
 
     try {
       BufferedReader in = new BufferedReader(new FileReader(new File(args[0])));
       StringBuffer classSpec = new StringBuffer();
       StringBuffer spec = new StringBuffer();
      String line;
      while ((line = in.readLine()) != null && !line.equals("")) {
         classSpec.append(line);
         classSpec.append('\n');
       }
 
      while ((line = in.readLine()) != null) {
         spec.append(line);
         spec.append('\n');
       }
 
       Map<String, Set<Character>> classes = CharClasses.buildMapFromSpec(classSpec.toString());
 
       //List<NFA> classNfa = new ArrayList<NFA>();
       for (String cls : classes.keySet()) {
         System.out.println(cls);
         //classNFA.add()
       }
 
 
       in.close();
 
     } catch (FileNotFoundException e) {
       System.err.println("Can't Find that file");
       e.printStackTrace();
     } catch (IOException e) {
       System.err.println("Error Reading from the file");
       e.printStackTrace();
     } catch (BadSpecException e) {
       System.err.println("Spec is messed up");
       e.printStackTrace();
     }
   }
 }
