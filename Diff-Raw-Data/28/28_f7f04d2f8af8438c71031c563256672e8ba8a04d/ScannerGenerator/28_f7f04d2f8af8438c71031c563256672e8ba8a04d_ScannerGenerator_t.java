 package sg;
 
 /***
  * ScannerGenerator
  * 
  * Represents a scanner generator - calls RDP and makes a DFA
  * @author mlfong
  * @version 1.0
  */
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import sg.fa.DFA;
 import sg.fa.NFA;
 import sg.rdp.InitRegex;
 import sg.rdp.RecursiveDescentParserNFA;
 
 public class ScannerGenerator
 {
     private HashMap<String, NFA> classesNFA;
     private HashMap<String, NFA> tokensNFA;
     private DFA bigDFA;
 
     private ScannerGenerator()
     {
         this.classesNFA = new HashMap<String, NFA>();
         this.tokensNFA = new HashMap<String, NFA>();
         this.bigDFA = null;
     }
 
     public HashMap<String, NFA> getClassesNFA()
     {
         return this.classesNFA;
     }
 
     public HashMap<String, NFA> getTokensNFA()
     {
         return this.tokensNFA;
     }
 
     public DFA getDFA()
     {
         if (this.bigDFA != null)
             return this.bigDFA;
         ArrayList<String> keys = new ArrayList<String>(this.getTokensNFA()
                 .keySet());
        if (keys.size() >= 2)
        {
            NFA bigNFA = NFA.union(this.getTokensNFA().get(keys.get(0)), this
                    .getTokensNFA().get(keys.get(1)));
            for (int i = 2; i < keys.size(); i++)
                bigNFA = NFA
                        .union(bigNFA, this.getTokensNFA().get(keys.get(i)));
            this.bigDFA = DFA.convertNFA(bigNFA);
            return this.bigDFA;
        }
        else
        // if(keys.size() == 1)
        {
            this.bigDFA = DFA.convertNFA(this.getTokensNFA().get(keys.get(0)));
            return this.bigDFA;
        }
     }
 
     /*****
      * init
      * 
      * initializes the scanner generator
     * 
      * @param filename
      * @return
      * @throws IOException
      */
    public static ScannerGenerator init(String filename) throws IOException
     {
         File f = new File(filename);
         BufferedReader br = new BufferedReader(new FileReader(f));
         String s;
         boolean inTokens = false;
         ScannerGenerator sg = new ScannerGenerator();
         HashMap<String, NFA> classesNFA = sg.classesNFA;
         HashMap<String, NFA> tokensNFA = sg.tokensNFA;
         while ((s = br.readLine()) != null)
         {
             if (s.length() < 2 && !inTokens)
             {
                 inTokens = true;
                 continue;
             }
             int divider = s.indexOf(" ");
             String classname = s.substring(0, divider);
             String theregex = s.substring(divider + 1);
             String sanitized = InitRegex.initializeRegex(theregex);
             sanitized = sanitized.substring(1, sanitized.length() - 1);
             sanitized = Util.removeSpaces(sanitized);
 
             NFA nfa = RecursiveDescentParserNFA.validateRegex(sanitized,
                     classesNFA);
             if (nfa == null)
             {
                 System.err.println("Error: RDP Failed on line " + s);
                 br.close();
                 System.exit(0);
             }
             if (inTokens) // token generation
             {
                 tokensNFA.put(classname, nfa);
                 nfa.setAcceptToken(classname);
             }
             else
             // class generation
             {
                 classesNFA.put(classname, nfa);
             }
         }
         br.close();
         return sg;
     }
 
 }
