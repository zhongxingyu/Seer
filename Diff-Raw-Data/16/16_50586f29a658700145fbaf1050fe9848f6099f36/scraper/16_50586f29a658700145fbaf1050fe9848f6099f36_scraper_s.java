 //Written by Jack Jamieson
 //Reddit Front Page Word Analyzer
 //Does all of the backend work.
 //Dependencies: jsoup, apache commons net, apache commons io.
 //https://twitter.com/jamieson_jack
 
 //    This file is part of RedditFPWAnalyzer.
 //
 //    RedditFPWAnalyzer is free software: you can redistribute it and/or modify
 //    it under the terms of the GNU General Public License as published by
 //    the Free Software Foundation, either version 3 of the License, or
 //    (at your option) any later version.
 //
 //    RedditFPWAnalyzer is distributed in the hope that it will be useful,
 //    but WITHOUT ANY WARRANTY; without even the implied warranty of
 //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //    GNU General Public License for more details.
 //
 //    You should have received a copy of the GNU General Public License
 //    along with RedditFPWAnalyzer.  If not, see <http://www.gnu.org/licenses/>.
 
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Scanner;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 import org.jsoup.*;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.net.ftp.FTPClient;
 
 //To use the timer the class must extend TimerTask.
 public class scraper extends TimerTask{
 	
 String FPposts[] = new String[26];//25 posts on the current front page.  There is an extra array location because [0] contains nothing.
 Object[] FPwordsArr;//The array created from FPwords.
 Object[] FPmatched;//The array created from matchingPosts.
 LinkedList<Object> FPwords = new LinkedList<Object>();//Contains all words to hit the front page.
 LinkedList<Object> matchingPosts = new LinkedList<Object>();//Contains all CURRENT matching posts.
 Timer timer;
 
 	public void execute() throws IOException
 	{
 		scrape();
 		outputFront();
 		outputWords();
 		matchPosts();
 		upload("your server", "username", "password");
 		schedule(120000, 300000);//Example time limits.  This will run every 7 minutes. 2 minute delay, 5 minute interval.
 	}
 
 	//Performs the initial parse and normalizes the data.
 	@SuppressWarnings("resource")
 	public void scrape() throws IOException{
 
 		//Using the RSS feed was easier because there is less formatting to sort through.
 		Document site = Jsoup.connect("http://www.reddit.com/.rss")
 				.userAgent("Reddit Front Page Word Analyzer/Trending FP Words on Reddit by worldbit v0.1")
 				.get();//Using Jsoup to connect to and parse Reddit's RSS feed.
 	
 		Elements titles = site.select("title");//Create a list of elements that use the title tag.  Title defines the names of the posts.
 		
 		//Removing junk from the string.
 		String posts = titles.toString();
 		posts = posts.toLowerCase();//Normalize in lower case.
 		posts = posts.substring(107);//Removes the Reddit homepage titles.
 		posts = posts.replaceAll("<title>", "");
 		posts = posts.replaceAll("</title>", "");
 		posts = posts.replaceAll("&quot;", "\"");
 
 		//Split the string by post.
 		Scanner SplitPosts = new Scanner(posts);
 		SplitPosts.useDelimiter("\n");//Every new line is a new post.
 		Scanner ReadWords = new Scanner(posts);
 		
 		int postnum = 0;
 		while(SplitPosts.hasNextLine() && postnum < 26)
 		{		
 			String line = SplitPosts.nextLine();
 			FPposts[postnum] = line;//Add the front page posts to the array.  Using an array because we know it will always be 25.
 
 			postnum++;
 		}
 		
 		FPposts[0] = "";//There is an empty space here...this is redundant? 
 		//TODO: Look into this [0].
 		
 		while(ReadWords.hasNext())//Making a linked list of words.  There is a variable amount (it keeps growing).
 		{		
 			String line = ReadWords.next();
 			line = line.replace(".", "");//Remove periods from ends of sentences, etc.
 			line = line.replace(",", "");//Remove periods from ends of sentences, etc.
 			FPwords.add(line);//Add the words to the LinkedList.
 
 		}
 		
 		//TODO: This probably could be done without this, but that's how I did it originally.  Maybe change this later?
 		FPwordsArr = FPwords.toArray();//Convert the list to an array.
 
 		Arrays.sort(FPwordsArr);//Perform initial lexicographic sort.
 	}
 	
 	//Outputs the posts on the front page to a text file - will be uploaded later.
 	public void outputFront() throws IOException
 	{
 		FileWriter fw = new FileWriter("FrontPageTitles.txt");
 		BufferedWriter bw = new BufferedWriter(fw);
 		
 		for(int start = 1; start < 26; start++)
 		{
 			bw.write(FPposts[start] + '\n');	
 		}
 		
 		bw.flush();
 		bw.close();
 	}
 	
 	//Outputs words to a text file(appends). As long as the program is running. 
 	//TODO: Find a way to keep the data and use it again so it's not overwritten when restarting.
 	public void outputWords() throws IOException
 	{
 		int count = 0;
 		String temp = "";//Compared to string to remove duplicates in the output.
 		File afpw = new File("ALLFrontPagedWords.txt");
 		
 		//This might still be useful? Download the file if it's been longer than a certain time...I'll leave it, might be useful.
 //		if(FileUtils.isFileNewer(afpw, 600000))
 //		{
 //			URL rwa = new URL("http://www.tcnj.edu/~jamiesj1/rwa/ALLFrontPagedWords.txt");
 //			File over = new File("ALLFrontPagedWords.txt");
 //			FileUtils.copyURLToFile(rwa, over);
 //		}
 		
 		FileWriter fw = new FileWriter(afpw);
 		BufferedWriter bw = new BufferedWriter(fw);
 		
 		//String filter when adding initial words.
 		for(int start = 0; start < FPwordsArr.length; start++)
 		{
 			if(FPwordsArr[start].toString().equals("to") || FPwordsArr[start].toString().equals("a") || FPwordsArr[start].equals("the") || FPwordsArr[start].equals("a") || FPwordsArr[start].equals("") || FPwordsArr[start].equals("with") || FPwordsArr[start].equals("in") || FPwordsArr[start].equals("from") || 
 					FPwordsArr[start].equals("this") || FPwordsArr[start].equals("that") || FPwordsArr[start].equals("and") || FPwordsArr[start].equals("of") || FPwordsArr[start].equals("on") || FPwordsArr[start].equals("is") || FPwordsArr[start].equals("i") || FPwordsArr[start].equals("my") || FPwordsArr[start].equals("it")
 					|| FPwordsArr[start].equals("as") || FPwordsArr[start].equals("an") || FPwordsArr[start].equals("so") || FPwordsArr[start].equals("you") || FPwordsArr[start].equals("my") || FPwordsArr[start].equals("me") || FPwordsArr[start].equals("what") || FPwordsArr[start].equals("was") || FPwordsArr[start].equals("at")
 					|| FPwordsArr[start].equals("for") || FPwordsArr[start].equals("we") || FPwordsArr[start].equals("them") || FPwordsArr[start].equals("she") || FPwordsArr[start].equals("he") || FPwordsArr[start].equals("have") || FPwordsArr[start].equals("had") || FPwordsArr[start].equals("-")|| FPwordsArr[start].equals("us")
 					|| FPwordsArr[start].equals("you're") || FPwordsArr[start].equals("your") || FPwordsArr[start].equals("you've") || FPwordsArr[start].equals("by") || FPwordsArr[start].equals("are") || FPwordsArr[start].equals("were") || FPwordsArr[start].equals("|") || FPwordsArr[start].equals("how") || FPwordsArr[start].equals("its")
 					|| FPwordsArr[start].equals("it's") || FPwordsArr[start].equals("has") || FPwordsArr[start].equals("can") || FPwordsArr[start].equals("how") || FPwordsArr[start].equals("his") || FPwordsArr[start].equals("hers") || FPwordsArr[start].equals("be") || FPwordsArr[start].equals("who") || FPwordsArr[start].equals("whom") || FPwordsArr[start].equals("which")
					|| FPwordsArr[start].equals("they") || FPwordsArr[start].equals("when"))
 			{
 			}
 			else
 			{//Removes duplicate words in the array.
 				for(int rest = start;rest < FPwordsArr.length; rest++)
 					if(FPwordsArr[start].toString().equals(FPwordsArr[rest].toString()))
 					{
 						count++;
 
 						if(temp.equals(FPwordsArr[rest]))
 							FPwordsArr[rest] = "";	
 						
 						temp = FPwordsArr[start].toString();
 
 					}//Removes  empty words and spaces.
 				if(FPwordsArr[start].equals(""))
 				{
 				}
 				else//Append "Seen # 'word'".
 				{
 					FPwordsArr[start] = "Seen " + count + " " + FPwordsArr[start].toString();
 				
 				}
 				
 				count = 0;
 			}
 	
 		}	
 		//String filter when writing to file.  Need both because the first one is add filter, this is write filter.
 		for(int write = 0; write < FPwordsArr.length; write++)
 		{
 			if(FPwordsArr[write].toString().equals("to") || FPwordsArr[write].toString().equals("a") || FPwordsArr[write].equals("the") || FPwordsArr[write].equals("a") || FPwordsArr[write].equals("") || FPwordsArr[write].equals("with") || FPwordsArr[write].equals("in") || FPwordsArr[write].equals("from") || 
 					FPwordsArr[write].equals("this") || FPwordsArr[write].equals("that") || FPwordsArr[write].equals("and") || FPwordsArr[write].equals("of") || FPwordsArr[write].equals("on") || FPwordsArr[write].equals("is") || FPwordsArr[write].equals("i") || FPwordsArr[write].equals("my") || FPwordsArr[write].equals("it")
 					|| FPwordsArr[write].equals("as") || FPwordsArr[write].equals("an") || FPwordsArr[write].equals("so") || FPwordsArr[write].equals("you") || FPwordsArr[write].equals("my") || FPwordsArr[write].equals("me") || FPwordsArr[write].equals("what") || FPwordsArr[write].equals("was") || FPwordsArr[write].equals("at")
 					|| FPwordsArr[write].equals("for") || FPwordsArr[write].equals("we") || FPwordsArr[write].equals("them") || FPwordsArr[write].equals("she") || FPwordsArr[write].equals("he") || FPwordsArr[write].equals("have") || FPwordsArr[write].equals("had") || FPwordsArr[write].equals("-") || FPwordsArr[write].equals("us")
 					|| FPwordsArr[write].equals("you're") || FPwordsArr[write].equals("your") || FPwordsArr[write].equals("you've") || FPwordsArr[write].equals("by") || FPwordsArr[write].equals("are") || FPwordsArr[write].equals("were") || FPwordsArr[write].equals("|") || FPwordsArr[write].equals("how") || FPwordsArr[write].equals("its")
 					|| FPwordsArr[write].equals("it's") || FPwordsArr[write].equals("has") || FPwordsArr[write].equals("can") || FPwordsArr[write].equals("how") || FPwordsArr[write].equals("his") || FPwordsArr[write].equals("hers") || FPwordsArr[write].equals("be") || FPwordsArr[write].equals("who") || FPwordsArr[write].equals("whom") || FPwordsArr[write].equals("which")
					|| FPwordsArr[write].equals("they") || FPwordsArr[write].equals("when"))
 					{
 					
 					}
 					else{
 						bw.append(FPwordsArr[write].toString());
 						bw.newLine();
 					}
 			}
 
 		bw.flush();
 		bw.close();	
 	}
 	
 	//Matches the posts on the CURRENT front page with the overall(since program started) top words.
 	@SuppressWarnings("resource")
 	public void matchPosts() throws IOException
 	{
 		URL origin = new URL("http://www.tcnj.edu/~jamiesj1/rwa/posts.php");//Grab php files containing 25 top words.
 		File dest = new File("top25.txt");
 		FileUtils.copyURLToFile(origin, dest);
 		
 		//Place into a string, normalize/remove junk.
 		String posts = FileUtils.readFileToString(dest);
 		posts = posts.substring(169);
 		posts = posts.replace("<pre>", "");
 		posts = posts.replace("</pre>", "");
 		posts = posts.replace("Seen", "");
 		posts = posts.replaceAll("[0-9]", "");
 		posts = posts.trim();
 		posts = posts.replaceAll(" ", "");
 		FileUtils.write(dest, posts);//Output for testing purposes.
 		
 		Scanner findWords = new Scanner(posts);
 		String words[] = new String[25];
 		
 		int add = 0;
 		while(findWords.hasNext())
 		{
 			words[add] = findWords.next();//Add the 25 words to an array, should always be 25.
 			add++;
 		}
 
 		//Goes through each word in the posts and tests if it equals a word in the top 25.  Add to new LinkedList if they do.
 		for(int matching = 0; matching < FPposts.length-1; matching++)
 		{
 			Scanner scan = new Scanner(FPposts[matching]);
 			while(scan.hasNext())
 			{
 				String test = scan.next();
 				for(int moveWord = 0; moveWord < words.length; moveWord++){
 					if(test.equals(words[moveWord]))
 					{
 						matchingPosts.add(FPposts[matching]);
 					}
 				}
 			}
 
 		}
 
 		//Put the front page posts array into a LinkedList - this is needed to use the retainAll method.
 		LinkedList<Object> FPpostsLink = new LinkedList<Object>();
 		for(int ar2link = 0; ar2link < FPposts.length; ar2link++)
 		{
 			FPpostsLink.add(FPposts[ar2link]);
 		}
 
 		matchingPosts.retainAll(FPpostsLink);//Only keeps the the posts in matchingPosts that are in FPposts(and FPpostsLink) - that way only current posts will show up.
 		FPmatched = matchingPosts.toArray();//Throw matchingPosts into an array to print them out.  Might not be needed?
 		
 		//Removes duplicate posts in the output.
 		String temp = "";
 		for(int fix = 0; fix < FPmatched.length; fix++)
 		{
 			for(int fixLine = fix; fixLine < FPmatched.length; fixLine++)
 			{
 				if(temp.equals(FPmatched[fixLine]))
 					FPmatched[fixLine] = "";
 				
 				temp = FPmatched[fix].toString();
 			}
 		}
 		
 		//Finally write all of this to a file to be uploaded.
 		FileWriter fw = new FileWriter("matchedPosts.txt");
 		BufferedWriter bw = new BufferedWriter(fw);
 		
 		for(int writePosts = 0; writePosts < FPmatched.length; writePosts++)
 		{
 			if(FPmatched[writePosts].equals(""))//Don't write if the post is blank.
 			{
 			}
 			else
 			{
 				//Output the string with html in it.  PHP will read this and use it to create the links on the page.
				bw.write("<a href=\"http://www.reddit.com/search?q=" + FPmatched[writePosts].toString() + "\">" + FPmatched[writePosts].toString() + "</a>");
 				bw.newLine();
 			}
 		}
 		
 		bw.flush();
 		bw.close();
 	}
 
 	//A necessary method to override when using a TimerTask.  Tells what to do when the timer is finished.
 	@Override
 	public void run()
 	{
 		try {
 			execute();
 			timer.cancel();//Cancel the thread after it is done. VERY IMPORTANT! You will run out of memory if this is not done.
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.out.println("ERROR: Failed to execute the timer or internet connection lost.\n Will attempt to reconnect at the specified time interval.");
 		}
 	}
 	
 	//Control the time interval to collect data.
 	public void schedule(long delay, long interval)
 	{
 		timer = new Timer();
 		TimerTask t = new scraper();
 		timer.scheduleAtFixedRate(t, delay, interval);//In milliseconds, 120000 is 2 minute delay, 300000 is 5 minute intervals.
 	}
 	
 	//Upload the files through FTP to be read in PHP and displayed.
 	public void upload(String server, String username, String password)
 	{
 		FTPClient ftp = new FTPClient();
 		FileInputStream fis = null;
 		FileInputStream fis2 = null;
 		try{
 			//Connect using supplied info.
 			ftp.connect(server);
 			ftp.login(username, password);
 			fis = new FileInputStream("ALLFrontPagedWords.txt");//Read this file.
 			fis2 = new FileInputStream("matchedPosts.txt");//Read this file.
 			
 			ftp.changeWorkingDirectory("www/rwa");//This is the directory I change to.
 			ftp.storeFile("ALLFrontPagedWords.txt", fis);//Upload.
 			ftp.storeFile("matchedPosts.txt", fis2);//Upload.
 			ftp.logout();//IMPORTANT!
 		}
 		
 		catch(IOException e)
 		{
 			e.printStackTrace();
 			System.out.println("ERROR: FTP error.");
 			
 		}
 		finally{
 			try{
 				if(fis != null){
 					fis.close();
 				}
 				ftp.disconnect();//IMPORTANT!
 			}catch (IOException e){
 				e.printStackTrace();
 			}
 		}
 	}
 }
