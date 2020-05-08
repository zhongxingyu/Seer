 
 import java.util.*;
 import java.util.regex.*;
 import java.text.*;
 import java.io.*;
 import cs224n.util.Counter;
 
 //This library might be useful to convert your test newgroup document into MessageFeatures
 // You could use the newsgroup labelling to later compare it with your classifier output
 // and calculate the accuracy
 
 // This class represents the information contained in an individual 
 // newsgroup message
 public class MessageFeatures implements Serializable {
 	String fileName;
 	int newsgroupNumber;
 
 	//The tokens inside subject, it provides the count of this token inside the message
 	Counter<String> subject;
 
 	//The tokens inside body, it provides the count of this token inside the message
 	Counter<String> body;
 
   //Creates a new, empty, MessageFeatures object
 	public MessageFeatures(int newsgroup, String fileName) {
 		this.newsgroupNumber = newsgroup;
 		this.fileName = fileName;
 		subject = new Counter<String>();
 		body = new Counter<String>();
 	}
 
 
   public static final String subjectTag = "Subject: ";
   
   /**
     parses a single message pointed to by fileName.  uses the stemmer and
     stopwords list passed in as parameters.  updates the subject and body
     word counters with the counts all of the words in the subject and body of the message
   **/ 
 	public void parse(Stemmer stemmer, HashSet<String> stopWords)	throws FileNotFoundException, IOException {
 		BufferedReader in = new BufferedReader(new FileReader(fileName));
 		String line;
 
 		// extract headers first
 		while ((line = in.readLine()) != null) {
 			if (line.startsWith(subjectTag))
 				parseLine(line.substring(subjectTag.length()), subject, stemmer, stopWords);
 			else if (line.equals(""))
 				break;
 		}
 
 		// now the body
 		while ((line = in.readLine()) != null)
 			parseLine(line, body, stemmer, stopWords);
 		
 		in.close();
 	}
 	
 
 	protected static final int maxTokenLength = 20;
 	protected static final int minWordLength = 3;
 	protected static final Pattern numberRE = Pattern.compile("[0-9]+");
 	protected static final Pattern wordRE = Pattern.compile("[a-zA-Z'\\-]+");
 	protected static final Pattern alphanum = Pattern.compile("\\w+");
 	protected static final Pattern hyperlink = Pattern
 			.compile("http\\:\\/\\/(\\w+\\.)+\\w+");
 	protected static final Pattern email = Pattern
 			.compile("[\\w\\-\\.]+@[\\w\\-\\.]+");
 	protected static final String delims = " \t\n\r\f.()\"',-:;/\\?!@";
 
 	protected static void parseLine(String line, Counter<String> counter, Stemmer stemmer, HashSet<String> stopWords) {
		line = line.toLowerCase();
 
 		// first find hyperlinks
 		Matcher mLink = hyperlink.matcher(line);
 		while (mLink.find())
 			counter.incrementCount(mLink.group());
 
 		// now email addresses
 		Matcher mEmail = email.matcher(line);
 		while (mEmail.find())
 			counter.incrementCount(mEmail.group());
 
 		// everything else
 		StringTokenizer st = new StringTokenizer(line, delims);
 		while (st.hasMoreTokens()) {
 			String word = st.nextToken();
 
 			if (word.length() < maxTokenLength) {
 				if (numberRE.matcher(word).matches())
 					counter.incrementCount(word);
 				else {
 					word = stemmer.doStemming(word);
 					if (wordRE.matcher(word).matches()
 							&& word.length() >= minWordLength
 							&& !stopWords.contains(word))
 						counter.incrementCount(word);
 				}
 			}
 		}
 	}
 	
 }
