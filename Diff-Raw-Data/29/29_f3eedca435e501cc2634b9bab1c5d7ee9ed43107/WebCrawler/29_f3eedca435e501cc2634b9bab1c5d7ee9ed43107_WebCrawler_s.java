 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /** WebCrawler
  * 
  * Simple webcrawler that starts at a given url and recursively writes all of the
  * outgoing links to a file, whose location is provided.
  * 
  * @author Justin Greet (jgreet)
  *
  */
 public class WebCrawler implements MigratableProcess {
 	// The queue of urls that have yet to be processed.
 	private Queue<URL> linksToCheck;
 	// A hash map to make sure that we're not cyclically checking links
 	// (i.e. site A points to sit B and site B points to site A
 	private HashMap<URL, Boolean> seenLinks;
 	// The file to write to.
 	private TransactionalFileOutputStream outFile;
 	// If the arguments are invalid, don't run.
	private boolean validArgs;
 	
 	private volatile boolean suspending;
 	
 	/** The constructor for the WebCrawler
 	 * 
 	 * @param args An array of the arguments to the function.
 	 * args[0]: The url to start at.
 	 * args[1]: The location of the file to write to.
 	 */
 	public WebCrawler(String[] args) {
 		try {
 			validArgs = true;
 			parseArgs(args);
 		} catch(IllegalArgumentException e) {
 			validArgs = false;
 			System.out.println("ERROR: Incorrect number of arguments.");
 			System.out.println("WebCrawler usage: $startingURL $targetFilename");
 		}
 	}
 	
 	/** parseArgs(args)
 	 * 
 	 * parseArgs makes sure the arguments passed to WebCrawler are correct,
 	 * and throws an exception otherwise.
 	 * 
 	 * @param args An array of the arguments to the function.
 	 * args[0]: The url to start at.
 	 * args[1]: The location of the file to write to.
 	 */
 	private void parseArgs(String[] args) {
 		/* Make sure the arguments are properly formatted and initialize object variables. */
 		if (args.length != 2) throw new IllegalArgumentException("That's silly");
 		try {
 			URL startingURL = new URL(args[0]);
 			String targetFilename = args[1];
 			seenLinks = new HashMap<URL, Boolean>();
 			linksToCheck = new Queue(startingURL);
 			outFile = new TransactionalFileOutputStream(targetFilename, false);
 			suspending = false;
 		} catch (MalformedURLException e) {
 			System.out.println("ERROR: Invalid url");
 			System.out.println("WebCrawler usage: $startingURL $targetFilename");
 			validArgs = false;
 		}
 	}
 	
 	/** run()
 	 * 
 	 * Simply runs the webcrawler.
 	 */
 	public void run() {
 		if (validArgs) {
 			runCrawler();
 		}
 		else {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/** runCrawler()
 	 * 
 	 * Search the links queue for links to check, visit the url, enqueue all of its
 	 * outgoing links, and repeat.
 	 */
 	private void runCrawler() {
 		PrintStream outStream = new PrintStream(outFile);
 		/* The safe state is after completely checking a single url */
 		while (linksToCheck.isEmpty() == false && suspending == false) {
 			URL curURL = linksToCheck.dequeue();
 			// Don't check this url if it's already been seen.
 			if (seenLinks.containsKey(curURL)) continue;
 			else seenLinks.put(curURL, true);
 			outStream.println("LINKS FROM " + curURL);
 			BufferedReader incomingHTML;
 			try {
 				incomingHTML = new BufferedReader(new InputStreamReader(curURL.openStream()));
 		        String curLine;
 		        curLine = incomingHTML.readLine();
 		        //Process the HTML for a single url line by line
 				while (curLine != null) {
 					//Check for the <a> tag 
 					Pattern linkPattern = Pattern.compile("(?i)<a([^>]+)>(.+?)</a>");
 					Pattern urlFromLinkPattern = Pattern.compile("\\s*(?i)href\\s*=\\s*\"([^\"]*\"|'[^']*'|[^'\">\\s]+)");
 					Matcher matchOnLink = linkPattern.matcher(curLine);
 					while (matchOnLink.find()){
 						String href = matchOnLink.group(1);
 						Matcher hrefPart = urlFromLinkPattern.matcher(href);
 						//Now check for the href attribute in the tag
 						while (hrefPart.find()) {
 							String newLink = hrefPart.group(1);
 							//Remove the "" at the end of the url
 							newLink = newLink.replace("\"", "");
 							try {
 								linksToCheck.enqueue(new URL(newLink));
 								outStream.println("       OUTGOING LINK: " + newLink);
 							}
 							catch (MalformedURLException e) {
 							}
 						}
 					}
 					//Update the current line
 				    curLine = incomingHTML.readLine();
 				}
 		        incomingHTML.close();
 			} catch (IOException e) {
 				outStream.println("ERROR: Bad URL on queue.");
 				e.printStackTrace();
 			}
 		}
 		suspending = false;
 	}
 	
 	/** suspend()
 	 * 
 	 * Bring the process to a safe state to make it serializable.
 	 */
 	public void suspend()
 	{
		System.out.println("SUSPEND");
 		suspending = true;
 		while (suspending);
 	}
 }
