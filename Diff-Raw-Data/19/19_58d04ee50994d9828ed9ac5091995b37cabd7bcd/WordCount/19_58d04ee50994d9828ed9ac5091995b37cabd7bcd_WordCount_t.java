 
 /*
    This program will read words from an input file, and count the
    number of occurrences of each word.  The word data is written to
    an output file twice, once with the words in alphabetical order
    and once with the words ordered by number of occurrences.
    The input and output file names must be given as command line
    arguments when the program is run.  The file uses the non-standard
    classes defined in the file TextReader.java.
    
    The program demonstrates several parts of Java's framework for
    generic programming:  TreeMap, List sorting, Comparators, etc.
 */
 
 import java.io.*;
 import java.util.*;
 
 public class WordCount {
 
    static ArrayList<TextReader> in;   // An input stream for reading the input file.
    static PrintWriter out; // Output stream for writing the output file.
    @SuppressWarnings("rawtypes")
    static ArrayList<TreeMap> filesWords;   // An input stream for reading the input file.
    static TreeMap<File,String> files;
    static TreeMap<String, WordData> wordsFreq;
    
    static class WordData { 
          // Represents the data we need about a word:  the word and
          // the number of times it has been encountered.
       String word;
       int count;
       WordData(String w) {
             // Constructor for creating a WordData object when
             // we encounter a new word.
          word = w;
          count = 1;  // The initial value of count is 1.
       }
       
       WordData(String w, int c) {
           // Constructor for creating a WordData object when
           // we encounter a new word.
        word = w;
        count = c;  // The initial value of count is 1.
     }
    } // end class WordData
    
    @SuppressWarnings("rawtypes")
 static class CountCompare implements Comparator {
          // A comparator for comparing objects of type WordData
          // according to their counts.  This is used for
          // sorting the list of words by frequency.
       public int compare(Object obj1, Object obj2) {
          WordData data1 = (WordData)obj1;
          WordData data2 = (WordData)obj2;
          return data2.count - data1.count;
             // The return value is positive if data2.count > data1.count.
             // I.E., data1 comes after data2 in the ordering if there
             // were more occurrences of data2.word than of data1.word.
             // The words are sorted according to decreasing counts.
       }
    } // end class CountCompare
 
 
    @SuppressWarnings({ "rawtypes", "unchecked" })
 public static void main(String[] args) throws IOException{
          // The program opens the input and output files.  It reads
          // words from the input file into a TreeMap, in which 
          // they are sorted by alphabetical order.  The words
          // are copied into a List, where they are sorted according
          // to frequency.  Then the words are copied from the
          // data structures to the output file.
 	   
       openFiles(args);  // Opens input and output streams, using file
                         // names given as command-line parameters.
       
       TreeMap words;  // TreeMap in which keys are words and values
                       // are objects of type WordData.
       TreeMap allWords;  // TreeMap with all words in which keys are words and values
       					// are objects of type WordData.
       allWords = new TreeMap(); 
       filesWords = new ArrayList<TreeMap>();
       
       System.out.println("Read "+files.size()+" files.");
       
       Iterator iterFile = files.keySet().iterator();
 	  Iterator iterCat = files.values().iterator();
       while (iterFile.hasNext()) {
     	 String cat = (String)iterCat.next();
          File f = (File)iterFile.next();
         	words = new TreeMap();
        	  	TextReader t;
        	  	try {
 	   			t = new TextReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
 	   			readWords(t,words);
 	   			filesWords.add(words);
 	   			Iterator iter3 = words.values().iterator();
 	   			while (iter3.hasNext()) {
 	   			    WordData data = (WordData)iter3.next();
 	   			    if(allWords.containsKey(data.word))    	 
 	   			    	 allWords.put(data.word, 
 	   			    			 new WordData(data.word, data.count + ((WordData) allWords.get(data.word)).count));
 	   			     else
 	   			    	 allWords.put(data.word, new WordData(data.word, data.count));
 	   			}
 	   		} catch (UnsupportedEncodingException e) {
 	   			System.err.println("Error trying to read file (Unsupported Encoding).");
 	   		} catch (FileNotFoundException e) {
 	   			System.err.println("File "+f.getName()+" not found.");
 	   		}
          }
       
       List wordsList = new ArrayList(allWords.values());
       wordsFreq = new TreeMap<String, WordData>();
       
       Iterator iter = wordsList.iterator();
       while (iter.hasNext()) {
          WordData data = (WordData)iter.next();
          wordsFreq.put(data.word, new WordData(data.word,0));
       }
       
      
       Iterator iterValues = files.values().iterator();
       for(TreeMap t : filesWords)
       {
 	      iter = wordsList.iterator();
 	      while (iter.hasNext()) {
 	         WordData data = (WordData)iter.next();
 	         if(t.containsKey(data.word)) {
 	        	 WordData data2 = (WordData) t.get(data.word);
 	        	 wordsFreq.put(data2.word, new WordData(data2.word, wordsFreq.get(data2.word).count+1));
 	         }
 	      }
       }
       
       ArrayList<WordData> wordsFreqSorted = new ArrayList<WordData>(wordsFreq.values());
       Collections.sort(wordsFreqSorted, new CountCompare());
       
       double limi = Double.parseDouble(args[2]);
       double lims = Double.parseDouble(args[3]);
        
       for(int i = 0; i < wordsFreqSorted.size(); i++) {
     	  double r = ((double)(wordsFreqSorted.get(i).count))/((double)(filesWords.size()));
     	  if(r >= lims)
     		  allWords.remove(wordsFreqSorted.get(i).word);
     	  else
     		  break;
       }
       
       for(int i = wordsFreqSorted.size()-1; i > 0; i--) {
     	 
     	  double r = ((double)(wordsFreqSorted.get(i).count))/((double)(filesWords.size()));
     	  if(r <= limi)
     		  allWords.remove(wordsFreqSorted.get(i).word);
     	  else
     		  break;
       }
       
       wordsList = new ArrayList(allWords.values());
       
       out.println("@RELATION news\n");
       
 
       
       iter = wordsList.iterator();
       while (iter.hasNext()) {
          WordData data = (WordData)iter.next();
          out.println("@ATTRIBUTE "+ data.word + " REAL");
       }
       
       out.println("\n@ATTRIBUTE class 	{'Politica', 'Desporto', 'Cultura'}\n");
       
       out.println("@DATA");
       
       iterValues = files.values().iterator();
       for(TreeMap t : filesWords)
       {
     	  String categ = (String)iterValues.next();
 	      iter = wordsList.iterator();
 	      while (iter.hasNext()) {
 	         WordData data = (WordData)iter.next();
 	         if(t.containsKey(data.word)) {
 	        	 WordData data2 = (WordData) t.get(data.word);
 	        	 out.print(data2.count + ",");
 	         }
 	         else
 	        	 out.print("0,");
 	      }
 	      out.println("'"+categ+"'");
 	      
       }
       System.out.println(wordsFreq.toString());
       
	  FileWriter fstream = new FileWriter("words.txt");
	  BufferedWriter filewriter = new BufferedWriter(fstream);
	  iter = wordsList.iterator();
      while (iter.hasNext()) {
         WordData data = (WordData)iter.next();
         filewriter.write(data.word);
         if(iter.hasNext())
        	 filewriter.write("\r\n");
         else
        	 break;
       }
      filewriter.close();
       
       if (out.checkError()) {
             // Some error occurred on the output stream.
          System.out.println("An error occurred while writing the data.");
          System.out.println("Output file might be missing or incomplete.");
          System.exit(1);
       }
       
       System.out.println(wordsList.size() + " distinct words were found.");
       
    } // end main()
    
    
 
 
 
 @SuppressWarnings("rawtypes")
 static void printWords(PrintWriter outStream, Collection wordData) {
          // wordData must contain objects of type WordData.  The words
          // and counts in these objects are written to the output stream.
       Iterator iter = wordData.iterator();
       while (iter.hasNext()) {
          WordData data = (WordData)iter.next();
          outStream.println("   " + data.word + " (" + data.count + ")");
       }
    } // end printWords()
    
 
    @SuppressWarnings("rawtypes")
 static void openFiles(String[] args) {
           // Open the global variable "in" as an input file with name args[0].
           // Open the global variable "out" as an output file with name args[1].
           // If args.length != 2, or if an error occurs while trying to open
           // the files, then an error message is printed and the program
           // will be terminated.
       if (args.length != 4) {
          System.out.println("Error: Please specify file names on command line.");
          System.exit(1);
       }
       //try {
     	  File dir = new File(args[0]);
     	  files = new TreeMap<File, String>();
     	  ArrayList<File> categoryFiles; 
     	  File[] categories = dir.listFiles();
     	  if (categories == null) {
     	      // Either dir does not exist or is not a directory
     	  } else {
     	      for (int i=0; i<categories.length; i++) {
     	    	  categoryFiles = new ArrayList<File>();
     	    	  // Get filename of file or directory
     	          String filename = categories[i].getName();
     	          if(categories[i] == null || categories[i].getName().contains(".")){
     	        	  
     	          } else {
     	        	  System.out.println(filename);
     	        	  File[] file = categories[i].listFiles();
     	        	for(int j=0; j<file.length;j++){
     	        		if(file[j] == null || file[j].getName().startsWith(".")){
     	        			
     	        		} else {
     	        			files.put(file[j],categories[i].getName());
     	        		}
     	        	}
     	          }
     	      }
     	  }
     	  
     	  Iterator iter = files.values().iterator();
     	  Iterator iter2 = files.keySet().iterator();
           while (iter2.hasNext()) {
         	 String cat = (String)iter.next();
              File data = (File)iter2.next();
              System.out.println(cat+": "+data.getName());
           }
     	  
     	  
     	  /*in = new ArrayList<TextReader>();
          in.add(new TextReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8")));
          in.add(new TextReader(new InputStreamReader(new FileInputStream(args[1]), "UTF-8")));
          */
 	  /*}
       catch (IOException e) {
          System.out.println("Error: Can't open input file " + args[0]);
          System.exit(1);
       }*/
       try {
          out = new PrintWriter(new FileWriter(args[1]));
       }
       catch (IOException e) {
          System.out.println("Error: Can't open output file " + args[1]);
          System.exit(1);
       }
    } // end openFiles()
    
 
    @SuppressWarnings({ "rawtypes", "unchecked" })
 static void readWords(TextReader inStream, Map words) {
         // Read all words from inStream, and store data about them in words.
         // A word is any sequence of letters.  Words are converted to lower
         // case.  Any non-letters in the input stream are ignored.
         // When a word is encountered for the first time a key/value pair
         // is inserted into words.  The key is the word and the value is
         // a new object of type WordData containing the word.  When a word
         // is encountered again, the frequency count in the corresponding
         // WordData object is just increased by one.  If an error occurs
         // while trying to read the data, an error message is printed and
         // the program is terminated.
 
       try {
          while (true) { 
             while (! inStream.eof() && ! Character.isLetter(inStream.peek()))
                inStream.getAnyChar();  // Skip past non-letters.
             if (inStream.eof())
                break;  // Exit because there is no more data to read.
             String word = inStream.getAlpha();  // Read one word from stream.
             word = word.toLowerCase();
             WordData data = (WordData)words.get(word);
                 // Check whether the word is already in the Map.  If not,
                 // the value of data will be null.  If it is not null, then
                 // it is a WordData object containing the word and the
                 // number of times we have encountered it so far.
             if (data == null) {
                   // We have not encountered word before.  Add it to
                   // the map.  The initial frequency count is
                   // automatically set to 1 by the WordData constructor.
                words.put(word, new WordData(word) );
             }
             else {
                   // The word has already been encountered, and data is
                   // the WordData object that holds data about the word.
                   // Add 1 to the frequency count in the WordData object.
                data.count = data.count + 1;
             }
          }
       }
       catch (TextReader.Error e) {
          System.out.println("An error occurred while reading the data.");
          System.out.println(e.toString());
          System.exit(1);
       }
 
    } // end readWords()
    
    
 } // end class WordCount
 
    
