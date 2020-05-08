 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Set;
 
 public class Parser {
     private List<TokenClass> charClasses;
     private List<TokenClass> tokenClasses;
     private List<TokenClass> charsAndTokens;
     NFA bigNFA;
     DFA bigDFA;
     static boolean debug = false;
     private static boolean dfa = false;
     private static boolean output = false;
     private static String dfaOutputFilename;
     private static String outputFilename;
 
     public static void main(String[] args) throws IOException{
     	Parser parser = new Parser();
     	
         PrintStream out = new PrintStream(System.out, true, "UTF-8");
         System.setOut(out);
         if(args.length > 1 && args.length < 10 && args.length%2 == 0){
 //            System.out.println("HERE!");
             if(args[0].equals("-s") && args.length >= 2){
 //                System.out.println("Found -s");
                 if(argsContains(args, "-d") != -1 && args.length >= 4){
 //                    System.out.println("Found -d");
                     dfa = true;
 //                    System.out.println(argsContains(args, "-d") + 1);
                     dfaOutputFilename = args[argsContains(args, "-d")+1];
                 }
                 
                 parser.buildFromFile(args[1]);
                 
                 if(!dfa)
                     System.out.println(parser.bigDFA.toTableString(true));
                 else{
                     File dfaFile = new File(dfaOutputFilename);
                     
                     FileWriter fw = new FileWriter(dfaFile);
                     BufferedWriter bw = new BufferedWriter(fw);
                     bw.write(parser.bigDFA.toTableString(true));
                     bw.close();                    
                 }
                 
                 if(argsContains(args, "-o") != -1 && argsContains(args, "-i") != -1
                         && args.length >= 6){
 //                    System.out.println("Found -o && -i");
                     output = true;
                     outputFilename = args[argsContains(args, "-o")+1];
                     int ind = argsContains(args, "-i");
                     parser.scanAndOutput(args[ind+1]);
                 }
                 else if(argsContains(args, "-i") != -1 && args.length >= 4){
 //                    System.out.println("Found -i");
                     int ind = argsContains(args, "-i");
                     parser.scanAndOutput(args[ind+1]);
                 }
                 else if(argsContains(args, "-o") != -1 && args.length >= 4){
                     throw new RuntimeException("\nIncorrect arguments. Arguments must at least have: \n" +
                     "-s <input_spec.txt> -i <input.txt> and -o <output.txt>\n if -o is used");
                 }
 //                if(argsContains(args, "--debug") != -1 && args.length >= 3){
 //                    System.out.println("Found --debug");
 //                    debug = true;
 //                }
             }
         }
         else{
             throw new RuntimeException("\nIncorrect arguments. Arguments must be: \n" +
             "-s <input_spec.txt> [-i <input.txt>] [-d <dfa.txt>] [-o <output.txt>] [--debug]");
         }
     }
     
     public void scanAndOutput(String filename) throws IOException {
         if(debug)
             System.out.println("Scanning input file with big dfa...");
         
         List<Token> tokens = scanFromFile(filename);
         
         String outputStr = "";
         for (Token token : tokens) {
         	outputStr += token.getKlass().getName() + " " + token.getString() + "\n";
         }
         
         if(output){
             File dfaFile = new File(outputFilename);
             
             FileWriter fw = new FileWriter(dfaFile);
             BufferedWriter bw = new BufferedWriter(fw);
             bw.write(outputStr);
             bw.close();
         } else {
         	System.out.println(outputStr);
         }
     }
     
     public static int argsContains(String[] args, String str){
         for(int i=0; i<args.length; i++){
             if(args[i].equals(str)){
                 return i;
             }
         }
         return -1;
     }
 
 //    // Old multi-DFA based scanner
 //    public static LinkedList<Token> scan(Scanner input) {
 //        LinkedList<Token> tokens = new LinkedList<Token>();
 //    	
 //        while(input.hasNextLine()){
 //            String line = input.nextLine();
 //            while(!line.isEmpty()) {
 //                line = line.trim();
 //
 //                ArrayList<ScanResult> results = new ArrayList<ScanResult>();
 //
 //                // Run all the DFAs on the current string
 //                // See which one matches the farthest
 //                // First defined token takes precedence
 //                int maxPointer = 0;
 //                TokenClass maxKlass = null;
 //                for (TokenClass klass : tokenDefs) {
 //                    ScanResult result = klass.getDFA().walk(line);
 //                    results.add(result);
 //                    if (result.lastPointer > maxPointer) {
 //                        maxPointer = result.lastPointer;
 //                        maxKlass = klass;
 //                    }
 //                }
 //
 //                // If nothing matched, invalid input!
 //                if (maxPointer == 0) {
 //                    if(debug)
 //                        System.out.println("INVALID INPUT!");
 //                    break;
 //                }
 //
 //                // Something matched!
 //                String token = line.substring(0, maxPointer);
 //
 //                if(!output){
 //                    System.out.print(maxKlass.getName());
 //                    System.out.println(" "+token);
 //                }
 //                else{
 //                	tokens.add(new Token(maxKlass, token));
 //                }
 //
 //                // Consume and start over!
 //                line = line.substring(maxPointer);
 //            }
 //        }
 //        
 //        return tokens;
 //    }
     
     public List<Token> scanFromFile(String filename) throws FileNotFoundException {
     	return scan(new Scanner(new File(filename)));
     }
     
     // Find matching token class
     public TokenClass identifyToken(String token) {
     	ScanResult result = bigDFA.walk(token);
     	
     	if (!result.accepts) return null;
     	
     	return result.lastAccept.klass;
     }
     
     // Find one token
     public Token scanToken(String line) {
     	ScanResult result = bigDFA.walk(line);
     	
     	if (result.lastPointer == 0) {
     		return null;
     	} else {
     		return new Token(
     				result.lastAccept.klass,
     				line.substring(0, result.lastPointer));
     	}
     }
     
     public List<Token> scan(Scanner input) {
         List<Token> tokens = new LinkedList<Token>();
         
         while(input.hasNextLine()){
             String line = input.nextLine();
            String fullLine = new String(line);
             while(!line.isEmpty()) {
                 line = line.trim();
 
                 ScanResult result = bigDFA.walk(line);
 
                 // If nothing matched, invalid input!
                 if (result.lastPointer == 0) {
                    System.out.println("INVALID INPUT!");
                    System.out.println(fullLine);
                    
                    String s = "";
                    for (int i = 0; i < fullLine.length()-line.length(); i++)
                    	s += " ";
                    s += "^";
                    System.out.println(s);
                    System.exit(0);
                 }
 
                 // Something matched!
                 String token = line.substring(0, result.lastPointer);
 
                 // Note: lastAccept.klass should NEVER be null!
                 tokens.add(new Token(result.lastAccept.klass, token));
 
                 // Consume and start over!
                 line = line.substring(result.lastPointer);
             }
         }
         
         return tokens;
     }
 
     public void buildFromFile(String filename) throws FileNotFoundException {
     	build(new Scanner(new File(filename)));
     }
     
     public void build(Scanner input) {
         if(debug)
             System.out.println("Parsing input spec...");
 
         TokenClass currClass = null;
         charClasses = new ArrayList<TokenClass>();
         tokenClasses = new ArrayList<TokenClass>();
         charsAndTokens = new ArrayList<TokenClass>();
 
         // Which array are we filling?
         List<TokenClass> classes = charClasses;
         while(input.hasNextLine()){
             String line = input.nextLine();
 
             // EOF or switch arrays?
             if (line.trim().isEmpty()) {
                 // Time for a switch?
                 if (classes == charClasses)
                     classes = tokenClasses;
                 continue;
             }
 
             Scanner lineScan = new Scanner(line);
 
             String token = lineScan.next();
 
             TokenClass newClass = new TokenClass();
             currClass = newClass;
             classes.add(currClass);
             charsAndTokens.add(currClass);
             currClass.setName(token.substring(1,token.length()));
             if(debug)
                 System.out.println(currClass.getName());
 
             token = line.substring(token.length());
 
             //token = token.replaceAll("\\s","");
             // Strip out leading space only
             token = token.replaceAll("^\\s*", "");
             if(debug)
                 System.out.println(token);
             RegExpFunc func = new RegExpFunc(token, this);
             NFA nfa = func.origRegExp(currClass.getName());
             currClass.setNFA(nfa);
             currClass.setDFA(nfa.toDFA(this));
             if(debug)
                 System.out.println("Parsing class "+currClass);
             nfa.setKlass(currClass);
 
             //System.out.println(currClass.getNFA());
             //System.out.println(currClass.getDFA());
         }
         State newStart = new State();
         //newStart.setLabel("Start");
         NFA newNfa = new NFA(newStart);
         //newNfa.setAccepts(newStart, false);
         for(TokenClass each : tokenClasses){
             newNfa.addEpsilonTransition(newNfa.getStart(), each.getNFA().getStart());
         }
         bigNFA = newNfa;
         bigDFA = newNfa.toDFA(this);
     }
     //  }
     /**
      * @return the classes
      */
     public List<TokenClass> getClasses() {
         return charsAndTokens;
     }
 
     public List<TokenClass> getTokenClasses() {
 		return tokenClasses;
 	}
 
 	public static List<Character> getIntervalOfChars(String inside){
         List<Character> list = new ArrayList<Character>();
         int index=0;
         char previousChar = inside.charAt(0);
         while(index<inside.length()){
             char c = inside.charAt(index);
             if(c == '-'){
                 previousChar++;
                 char currentChar = previousChar;
                 char endChar = inside.charAt(index+1);
                 while (currentChar < endChar){
                     list.add(currentChar);
                     currentChar++;
                 }
             }
             else{
                 list.add(c);
             }
             previousChar = c;
             index++;
         }
         return list;
     }
 
     public Set<Character> getClass(String className){
         for(int i=0; i<charsAndTokens.size(); i++){
             if(charsAndTokens.get(i).getName().equals(className))
                 return charsAndTokens.get(i).getChars();
         }
         return null;
     }
 
     public void setClass(String className, Set<Character> exclude) {
         for(int i=0; i<charsAndTokens.size(); i++){
             if(charsAndTokens.get(i).getName().equals(className))
                 charsAndTokens.get(i).setChars(exclude);
         }
     }
     
     public TokenClass getTokenClass(String name) {
     	for (TokenClass klass : tokenClasses) {
     		if (klass.getName().equals(name)) return klass;
     	}
     	
     	return null;
     }
     
     // Try to identify token class in start of string
     public TokenClass findTokenClassIn(String string) {
     	for (TokenClass klass : tokenClasses) {
     		String name = klass.getName();
     		
     		if (string.substring(0, Math.min(name.length(), string.length())).equals(name))
     			return klass;
     	}
     	
     	return null;
     }
 }
