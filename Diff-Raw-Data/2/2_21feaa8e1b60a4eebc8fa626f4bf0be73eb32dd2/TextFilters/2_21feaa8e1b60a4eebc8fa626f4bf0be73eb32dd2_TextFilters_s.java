 /*
  * TextFilters.java
  * 
  * Author: Michael Lese
  * 		   Yuxin David Huang
  * 		   Colgate University
  */
 
 import java.io.*;
 import java.util.*;
 
 public class TextFilters
 {
 
     private static void cat(String[] args)
     {
         InputStream in = null;
     	boolean file = false;
 		String filename = "stdin";
 
 		if (args.length>0) {
 			for(int i=0; i<args.length; i++) {
 				filename=args[i];
 				try {
 					in = new FileInputStream(filename);
 					file=true;
 				} catch (FileNotFoundException e) {
 					System.err.println("cat error: file " + filename + " was not found. Exiting.");
 					
 					return;
 				} catch (SecurityException e) {
 					System.err.println("cat error: file " + filename + " could not be opened. Access was denied.");
 					return;
 				}
 				Scanner scan = new Scanner(in);
 
 				while(scan.hasNext()) 
 					System.out.println(scan.nextLine());
 			}
 		}
 
 		else { 
 			System.out.println("stdin");
 			//I'm not 100% sure what I'm supposed to do when args is empty.
 		}
 
 
 		if (file) {
 			try {
 				in.close();
 			} catch (IOException e) {
				System.err.println("sort: error closing file " + filename);
 			}
 		}
 
         
     }
     
 
     private static void diff(String[] args)
     {
 
         // FILL IN CODE HERE
     
     }
     
     
     private static void grep(String[] args) {
     
         InputStream in = null;     // input stream
         
         boolean file = false;       // used for error-msg printing
         String filename = null;     // default - no filename
         
         String target = null;       // default - no target to search for 
         boolean countLine = false;  // default - do not count line numbers
         
         if (args.length > 0) {
             
             // check if the first argument is "-l" or the string to search for
             if ("-l".equals(args[0])) {
                 countLine = true;
                 if (args.length < 2) throw new IllegalArgumentException("grep: Not enough arguments"); 
             }
             
             if (countLine) {
                 
                 if (args.length > 1) {
                     target = args[1];
                 }
                 
                 // use file instead of stdin if filename is provided
                 if (args.length > 2) {
                     filename = args[2];
                     try {
                         in = new FileInputStream(filename);
                         file = true;
                     } catch (FileNotFoundException e) {
                         System.err.print("sort: error, file ");
                         System.err.print(filename);
                         System.err.println(" not found or could not be opened");
                         return;
                     } catch (SecurityException e) {
                         System.err.print("sort: error, access to file ");
                         System.err.print(filename);
                         System.err.println(" was denied");
                         return;
                     }
                 }
                 
                 if (args.length > 3) throw new IllegalArgumentException("grep: Too many arguments");
                 
             } else {
                 target = args[0];
                 
                 // use file instead of stdin if filename is provided
                 if (args.length > 1) {
                     filename = args[1];
                     try {
                         in = new FileInputStream(filename);
                         file = true;
                     } catch (FileNotFoundException e) {
                         System.err.print("sort: error, file ");
                         System.err.print(filename);
                         System.err.println(" not found or could not be opened");
                         return;
                     } catch (SecurityException e) {
                         System.err.print("sort: error, access to file ");
                         System.err.print(filename);
                         System.err.println(" was denied");
                         return;
                     }
                 }
                 
                 if (args.length > 2) throw new IllegalArgumentException("grep: Too many arguments");
             }
             
             Scanner sc;
             if (file) {
                 sc = new Scanner(in);
             } else {
                 sc = new Scanner(System.in);
             }
             
             int lineNum = 0;
             while (sc.hasNextLine()) {
                 
                 String currLine = sc.nextLine();
                 if (currLine.contains(target)) {
                     if (countLine) {
                         System.out.println("{" + lineNum + "} " + currLine);
                     } else {
                         System.out.println(currLine);
                     }
                 }
                 
                 lineNum++;
             }
             
             // close the input stream
             if (file) {
                 try {
                     in.close();
                 } catch (IOException e) {
                     System.err.print("sort: error closing file ");
                     System.err.println(filename);
                 }
             }
             
         } else throw new IllegalArgumentException("grep: Not enough arguments");
     
     } // end of grep(String[])
     
     
     private static void tr(String[] args)
     {
     
         // FILL IN CODE HERE
     
     }
     
     
     private static void uniq(String[] args)
     {
     
         // FILL IN CODE HERE
     
     }
     
     /***********************
     
      YOU SHOULD NOT HAVE TO MODIFY CODE BELOW THIS POINT
      Feel free to look, though!
      
      ***********************/
     
     
     /* This is an example of a filter that is already implemented.
        Feel free to model your own code on it.
      */
 
     private static void sort(String[] args)
     {
     
         // This code was taken from Sort.java
     
         InputStream in = null;          // the input stream
         boolean file = false;           // used for error-msg printing
         String filename = "stdin";      // default - no filename
         
         if (args.length > 0) {
             // there's a filename, so try to open a file
             filename = args[0];
             try {
                 in = new FileInputStream(filename);
                 file = true;
             } catch (FileNotFoundException e) {
                 System.err.print("sort: error, file ");
                 System.err.print(filename);
                 System.err.println(" not found or could not be opened");
                 return;
             } catch (SecurityException e) {
                 System.err.print("sort: error, access to file ");
                 System.err.print(filename);
                 System.err.println(" was denied");
                 return;
             }
         } else {
             // no filename -- use stdin instead
             in = System.in;
         }
         
         Scanner scan = new Scanner(in);    // open scanner on input stream
         
         /* unlike some of the other filters to implement above,
            sort requires storing all the lines of the file
            in order to print them in order.
          
            do not use unnecessary storage in your own implementations!
         */
         ArrayList<String> list = new ArrayList<String>();
         
         // add lines to the list
         while (scan.hasNextLine())
             list.add(scan.nextLine());
         
         // sort the list (by default -- alphabetically
         Collections.sort(list);
         
         // print each line in the sorted list
         for (String s : list)
             System.out.println(s);
         
         // close the input stream
         if (file) {
             try {
                 in.close();
             } catch (IOException e) {
                 System.err.print("sort: error closing file ");
                 System.err.println(filename);
             }
         }
     
     }
     
     
     /*************
      This code provides the main() program that simulates
      piping among the filters so you can invoke multiple
      filters from the Dr. Java Interactions pane
      *************/
     
     private static enum Filter {
         CAT, DIFF, GREP, SORT, TR, UNIQ
     }
             
     public static void main(String[] args)
     {
     
         PrintStream stdout = System.out;
         InputStream stdin = System.in;
     
         ArrayList<ArrayList<String>> commands = new ArrayList<ArrayList<String>>();
         
         ArrayList<String> group = new ArrayList<String>();
 
         for (int pos = 0; pos < args.length; pos++) {
             if (args[pos].equals("---")) {
                 commands.add(group);
                 group = new ArrayList<String>();
             }
             else
                 group.add(args[pos]);
         }
 
         commands.add(group);
         
         ByteArrayOutputStream baos = null;
         
         for (int pos = 0; pos < commands.size(); pos++) {
             ArrayList<String> g = commands.get(pos);
             if (g.size() <= 0) {
                 System.err.println("TextFilters: error, missing command in pipe");
                 return;
             }
                 
             Filter f;
             String cmd = g.get(0);
             
             if (cmd.equals("cat"))
                 f = Filter.CAT;
             else if (cmd.equals("diff"))
                 f = Filter.DIFF;
             else if (cmd.equals("grep"))
                 f = Filter.GREP;
             else if (cmd.equals("sort"))
                 f = Filter.SORT;
             else if (cmd.equals("tr"))
                 f = Filter.TR;
             else if (cmd.equals("uniq"))
                 f = Filter.UNIQ;
             else {
                 System.err.print("TextFilters: error, cannot recognize command ");
                 System.err.println(cmd);
                 return;
             }
                 
             String[] arr = new String[g.size()-1];
             for (int i = 1; i < g.size(); i++)
                 arr[i-1] = g.get(i);
                 
             if (pos > 0)
                 System.setIn(new ByteArrayInputStream(baos.toByteArray()));
             else
                 System.setIn(stdin);
                 
             if (pos == commands.size()-1)
                 System.setOut(stdout);
             else {
                 baos = new ByteArrayOutputStream();
                 System.setOut(new PrintStream(baos));
             }
             
             switch(f) {
             
                 case CAT:
                     cat(arr);
                     break;
             
                 case DIFF:
                     diff(arr);
                     break;
                     
                 case GREP:
                     grep(arr);
                     break;
                     
                 case SORT:
                     sort(arr);
                     break;
                     
                 case TR:
                     tr(arr);
                     break;
                     
                 case UNIQ:
                     uniq(arr);
                     break;
                     
                 default:
                     System.err.println("TextFilters: unexpected error");
                     return;
             }
         }
         System.setIn(stdin);
     }
 }
