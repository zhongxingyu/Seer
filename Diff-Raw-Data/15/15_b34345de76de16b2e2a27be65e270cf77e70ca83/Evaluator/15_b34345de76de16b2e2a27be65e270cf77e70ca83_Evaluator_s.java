 package interpreter;
 import java.io.*;
 import java.nio.channels.*;
 import java.nio.charset.Charset;
 import java.nio.*;
 import java.util.*;
 
 import tokenizer.NFA;
 import tokenizer.Tokenizer;
 
 /**
  * 
  * Currently performs find and replace operations on text files. Needs to be
  * expanded to walk the AST tree
  *
  */
 public class Evaluator {
 
     HashMap<String, Object> symbolTable;
 
     public Evaluator() {
         symbolTable = new HashMap<String, Object>();
     }
 
     /**
      * Evaluates the AST.
      * @param head the head
      * @return the result of the evaluation
      */
     public Object eval(SyntaxTreeNode head) {
 
         String nodeType = head.nodeType;
         String nodeId = head.id;
         ArrayList<SyntaxTreeNode> children = head.children;
         int count = 0;
 
         if (nodeType == null) {
             return head.value;
         }
         if (nodeType.equals("MiniRE-program")) {
             eval(children.get(1));
         } else if (nodeType.equals("STATEMENT-LIST")) {
             eval(children.get(0));
             eval(children.get(1));
         } else if (nodeType.equals("STATEMENT-LIST-TAIL")) {
             for (SyntaxTreeNode child : children) {
                 eval(child);
             }
         } else if (nodeType.equals("STATEMENT")) {
             if (children.get(0).id.equals("$REPLACE")) {
                 String regex = children.get(1).value;
                 String asciiStr = children.get(3).value;
                 ArrayList<String> fileNames = (ArrayList<String>) eval(children.get(5));
                 return replace(regex, asciiStr, fileNames.get(0), fileNames.get(1));
             } else if (children.get(0).id.equals("$RECREP")) {
                 String regex = children.get(1).value;
                 String asciiStr = children.get(3).value;
                 ArrayList<String> fileNames = (ArrayList<String>) eval(children.get(5));
                 recursivereplace(regex, asciiStr, fileNames.get(0), fileNames.get(1));
             } else if (children.get(0).id.equals("$ID")) {
                 String idName = children.get(0).value;
                 Object idValue = eval(children.get(2));
                 symbolTable.put(idName, idValue);
             } else if (children.get(0).id.equals("$PRINT")) {
                 SyntaxTreeNode exp_list = children.get(2);
                 try {
                     StringList exp = (StringList) eval(exp_list.children.get(0));
                     if (exp.length() > 0)
                         System.out.println(exp.toString());
                     else
                         System.out.println("[]");
                 } catch (ClassCastException cce) {
                     Integer exp  = (Integer) eval(exp_list.children.get(0));
                     System.out.println(exp);
                 }
                 SyntaxTreeNode exp_list_tail = exp_list.children.get(1);
                while (!exp_list_tail.children.get(0).nodeType.equals("EPSILON")) {
                     try {
                        StringList exp = (StringList) eval(exp_list.children.get(0));
                         if (exp.length() > 0)
                             System.out.println(exp.toString());
                         else
                             System.out.println("[]");
                     } catch (ClassCastException cce) {
                        Integer exp  = (Integer) eval(exp_list.children.get(0));
                         System.out.println(exp);
                     }
                     exp_list_tail = exp_list_tail.children.get(2);
                 }
             }
         } else if (nodeType.equals("STATEMENT-RIGHTHAND")) {
             if (children.get(0).nodeType != null && children.get(0).nodeType.equals("EXP")) {
                 return eval(children.get(0));
             } else if (children.get(0).id.equals("$HASH")) {
                 return ((StringList) eval(children.get(1))).length();
             } else if (children.get(0).id.equals("$MAXFREQSTRING")) {
                 return ((StringList) symbolTable.get(children.get(2).value)).maxfreqstring();
             }
         } else if (nodeType.equals("FILE-NAMES")) {
             ArrayList<String> fileNames = new ArrayList<String>();
             fileNames.add((String) eval(children.get(0)));
             fileNames.add((String) eval(children.get(2)));
             return fileNames;
         } else if (nodeType.equals("SOURCE-FILE")) {
             return children.get(0).value;
         } else if (nodeType.equals("DESTINATION-FILE")) {
             return children.get(0).value;
         } else if (nodeType.equals("EXP-LIST-TAIL")) {
             if (children.get(0).nodeType.equals("EPSILON")) {
                 return null;
             } else {
                 return StringList.union((StringList) eval(children.get(1)), (StringList) eval(children.get(2)));
             }
         } else if (nodeType.equals("EXP")) {
             if (children.get(0).id != null && children.get(0).id.equals("$ID")) {
                 if (symbolTable.containsKey(children.get(0).value)) {
                     return symbolTable.get(children.get(0).value);
                 } else {
                     return children.get(0).value;
                 }
             } else if (children.get(0).id != null && children.get(0).id.equals("$LPAREN")) {
                 return eval(children.get(2));
             } else if (children.get(0).nodeType != null && children.get(0).nodeType.equals("TERM")) {
                 if (children.get(1).children.get(0).nodeType.equals("EPSILON")) {
                     return eval(children.get(0));
                 } else {
                     StringList result = (StringList) eval(children.get(0));
                     SyntaxTreeNode exp_list = children.get(1);
                     while (!exp_list.children.get(0).nodeType.equals("EPSILON")) {
                         if (exp_list.children.get(0).children.get(0).id.equals("$DIFF")) {
                             result = StringList.diff(result, (StringList) eval(exp_list.children.get(1)));
                         } else if (exp_list.children.get(0).children.get(0).id.equals("$UNION")) {
                             result = StringList.union(result, (StringList) eval(exp_list.children.get(1)));
                         } else if (exp_list.children.get(0).children.get(0).id.equals("$INTERS")) {
                             result = StringList.inters(result, (StringList) eval(exp_list.children.get(1)));
                         }
                         exp_list = exp_list.children.get(2);
                     }
                     return result;
                 }
             }
         } else if (nodeType.equals("TERM")) {
             String fileName = (String) eval(children.get(3));
             String regex = (String) eval(children.get(1));
             return find(regex, fileName);
         } else if (nodeType.equals("FILE-NAME")) {
             return (String) children.get(0).value;
         } else {
             System.out.println("Finished evaluation.");
         }
         return null;
     }
 
     // TODO
     public static String maxFreqString(StringList list) {
         String mostFrequentString = "";
         return mostFrequentString;
     }
 
 	/**
      * Finds all strings in the given file that match the given regex, and
      * returns them in a StringList
      */
     public static StringList find(String regex, String filename) {
         regex = regex.replaceAll("'", "");
 		Tokenizer d = new Tokenizer("id", regex, filename);
 		d.generateTokens();
 		return StringList.toStringList(d.getTokens());
 	}
 
 	/**
      * Replaces all strings in the given file1 that match the given regex with
      * the replacement string and prints the final output to file2. Returns true
      * if matches were found.
      */
     public static boolean replace(String regex, String replacement,
         String file1, String file2) {
         regex = regex.replaceAll("'", "");
         replacement = replacement.replaceAll("\"", "");
         file1 = file1.replaceAll("\"", "");
         file2 = file2.replaceAll("\"", "");
 
 		String[] matches = find(regex, file1).strings();
 
 		if (matches.length < 1) {
 			return false;
 		}
 
 		String in = "";
 		regex = regex.substring(1,regex.length()-1);
         FileInputStream stream = null;
         try {
             stream = new FileInputStream(new File(file1));
             FileChannel fc = stream.getChannel();
             MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
             in = Charset.defaultCharset().decode(mbb).toString();
             stream.close();
         } catch (FileNotFoundException fnfe) {
             System.out.println("Runtime error: " + file1 + " not found.");
             System.exit(0);
         } catch (IOException io) {
             System.out.println("Runtime error: can't get size of FileChannel");
             System.exit(0);
         }
 
 		for (int i=0; i < matches.length; i++){
 			in = in.replace(matches[i], replacement);
 		}
 
         try {
             FileWriter fstream = new FileWriter(file2);
             BufferedWriter out = new BufferedWriter(fstream);
             out.write(in);
             out.close();
 
         } catch (IOException ie) {
             System.out.println("Runtime error: error in file " + file2);
         }
         return true;
 	}
 	
 	/**
      * Will recursively replace all strings in the given file1 that match the
      * given regex with the replacement string and print the final output to
      * file2.
      */
     public static void recursivereplace(String regex, String replacement,
         String file1, String file2) {
         
         NFA nfa = new NFA(regex);
         String sub = replacement;
         
         while (sub.length() > 0) {
             if (nfa.testString(nfa.thisNFA, sub)) {
                 // throw some kind of error - the recursion is infinite
                 System.out.println("Error: infinite loop inevitable at:\n" +
                     "recursivereplace '" + regex + "' with \"" + replacement +
                     "\" in " + file1 + " >! " + file2);
                 return;
             }
             sub = sub.substring(0, sub.length()-1);
         }
 
 		// Now Recursively replace until no more changes can be made
 
 	}
 }
