 /*My Web Server 
  *Bhojan Anand
  */
 
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 public class WebServer {
 	
 	private Vector<String> inputWords = new Vector<String>();
     private Vector<String> scrambledWords = new Vector<String>();
     private Integer currentWordIndex;
     private Integer numWords = 3;
    private Timer timer;
    private Integer alwaysConstantWordTime = 2 * 60; // default is 2 * 60
    private Integer alwaysConstantGameTime = 6 * 60; // default is 6 * 60
     private DataOutputStream dos;
     private String currentGuessingWord;
     private int guessReply; // 0 = none, 1 = right, 2 = wrong
     private int points; 
     private String wordToGuess;
     private int wordTime = 2 * 60;
     private int gameTime = 6 * 60;
    private int showCountDown = 1; // 0 means dont show, 1 means show
 
     public void WebServer() {
         
         gameTime = alwaysConstantGameTime;
         wordTime = alwaysConstantWordTime;
         guessReply = 0;
         numWords = 1;
         inputWords = new Vector<String>();
         scrambledWords = new Vector<String>();
         currentGuessingWord = "";
         points = 0;
         wordToGuess = "";
 		currentWordIndex = 0;
 
     }
 
  	private void scrambleWords() { // hides at least 80% of the input
         scrambledWords.clear();
 
         for (Integer tempIndex = 0; tempIndex < numWords; tempIndex++) {
             scrambledWords.add(inputWords.elementAt(tempIndex));
             String tempWord = scrambledWords.elementAt(tempIndex);
             Integer lengthOfWord = tempWord.length();
             int eightyPercent = (int) Math.round((lengthOfWord * .8));
             int indexToRemove = 0;
             for (Integer tempRemoveChars = 0; tempRemoveChars < eightyPercent; tempRemoveChars++) {
                 do {
 
                     indexToRemove = (int) (Math.random() * lengthOfWord);
 
                 } while (tempWord.charAt(indexToRemove) == '_');
 
 
                 char[] tempCharArray = tempWord.toCharArray();
                 tempCharArray[indexToRemove] = '_';
                 tempWord = String.valueOf(tempCharArray);
                 scrambledWords.setElementAt(tempWord, tempIndex);
             }
 
         }
     }
 
   
 
     public void processAndSendReply(String contents) throws Exception { // checks for timeup, assigns points for guess
 
     	if(wordTime <= 1) // time was up
     		sendMessageToClient("4" + contents.replace("???ScoreHere???", points + ""));
 
     	else if(gameTime <= 1)	
     	{
     		sendMessageToClient("5" + contents.replace("???ScoreHere???", points + ""));
     	}
     	
     	else if(guessReply == 0) // no guess
     		sendMessageToClient("0" + contents.replace("???ScoreHere???", points + ""));
 
     	else if(guessReply == 1) // right guess
     	{
     		points++;
     		sendMessageToClient("1" + contents.replace("???ScoreHere???", points + ""));
     		
 
     	} 
     	else if(guessReply == 2) // wrong guess
     	{
     		points -= 2;
     		sendMessageToClient("3" + contents.replace("???ScoreHere???", points + ""));
 
     	}
     	
     	guessReply = 0;
     }
 
     public void sendMessageToClient(String messageToSend) throws Exception{ // sends the html file to the client
     	
     	String contents = messageToSend.substring(1);
 
     	if (messageToSend.startsWith("0")) // Just send word
     	{
 			contents = contents.replace("???PutInformationHere???", "");
     			
     	}
     	else if (messageToSend.startsWith("1")) // correct guess
     	{	
 			contents = contents.replace("???PutInformationHere???", "<font color = \"green\">Correct Guess</font>");
     	}
     	else if (messageToSend.startsWith("3")) // wrong guess
     	{
 			
 			contents = contents.replace("???PutInformationHere???", "<font color = \"red\">Wrong Guess</font>");
     	}
     	else if (messageToSend.startsWith("4")) // time was up
     	{
 			
 			contents = contents.replace("???PutInformationHere???", "<font color = \"blue\">Word time was up.</font>");
     	}
     	else if (messageToSend.startsWith("5")) // time was up
     	{
 			
 			contents = contents.replace("???PutInformationHere???", "<font color = \"blue\">Game time was up. Please restart</font>");
     	}
     	else
     	{
     		contents = contents.replace("???PutInformationHere???", "");
     	}
     	
     	contents = contents.replace("???WordHere???", wordToGuess);
     	wordTime = alwaysConstantWordTime;
 
 		dos.writeBytes("HTTP/1.0 200 Okie \r\n");
 		dos.writeBytes("Content-type: text/html\r\n");
 		dos.writeBytes("\r\n");
 		dos.writeBytes(contents);
     }
     public void getMoreWords() throws Exception{ // reads from the file, puts in vector, and calls scramble function
     	String filename = "inputWords.txt";
 
     	File f = new File(filename);
 		      
 		if (f.canRead())
 		{
 			int size = (int)f.length();
 			
 			//Create a File InputStream to read the File
 			FileInputStream fis = new FileInputStream(filename);
 			byte[] buffer = new byte[size];
 			fis.read(buffer);
 		
 			// Now, write buffer to client
 			
 			String contents = new String(buffer, "UTF-8");
 			
 			inputWords = new Vector<String> (Arrays.asList(contents.split("\n")));
 			numWords = inputWords.size();
 			Collections.shuffle(inputWords);
 
 			scrambleWords();
 
 			
 		}
 		else
 		{
 			// No input file
 			System.out.println("No input file found");
 			System.exit(0);
 		}
 
     	currentWordIndex = 0;
     }
     public void resetGame() throws Exception { // starts a new game. typically called when the base URL of the game is called using a browser
     	points = 0;
     	currentWordIndex = 0;
     	getMoreWords();
     	wordToGuess = scrambledWords.elementAt(currentWordIndex);
     	currentGuessingWord = inputWords.elementAt(currentWordIndex);
    		
    		wordTime = alwaysConstantWordTime;
     	gameTime = alwaysConstantGameTime;
 
    	if(timer != null) // if an existing timer is running, stop it
    	{
    		timer.cancel();
    		System.out.println("Cancelled existing timer");
    	}

    	timer = new Timer();
     	timer.schedule(new TimerTask() {
             @Override
             public void run() {
             	wordTime --;
             	gameTime --;
             	if(showCountDown == 1)
             		System.out.println("Game Time is " + gameTime + " word time is " + wordTime);
 
             }
 	        }, 0, 1000); // task is triggered every 1 sec
 	    }
 
     public String getNextWord() throws Exception{ // after a guess, this is called to get next word to send to user. also handles no more words case
 
     	if(++currentWordIndex >= inputWords.size())
     	{
     		getMoreWords();
     	}
     	
     	return scrambledWords.elementAt(currentWordIndex);
     	
     }
 
     public void processParameters(String parameters) throws Exception{ // the actual logic of checking analyst's guess happens here
     	
     	wordToGuess = getNextWord();
 
     	
 
     	if(parameters == null || parameters.length() < 1) // new game
     	{
     		resetGame();
     		currentGuessingWord = inputWords.elementAt(0);
     		return;
     	}
     	if(parameters.indexOf("guess=") != -1) // player has guessed
     	{
 
     		if(parameters.replace("guess=", "").compareToIgnoreCase(currentGuessingWord) == 0)
     		{
     			guessReply = 1; // correct guess
     		}
     		else
     		{
     			guessReply = 2; // wrong guess
 
     			System.out.println("Wrong guess - " + parameters.replace("guess=", ""));
     		}
     		
 			
     	}
     	else
     		guessReply = 0;
     	currentGuessingWord = inputWords.elementAt(currentWordIndex);
     	
     }
 
 
     public void run() throws Exception{ // main driver function
 
     	ServerSocket serverSock = new ServerSocket(7000);
 		System.out.println("SERVER IS WAITING FOR HTTP REQUEST at PORT 7000...");
 		
 		
 		currentGuessingWord = "";
 		wordToGuess = currentGuessingWord;
 		inputWords = new Vector<String>();
 		currentWordIndex = -1;
 		  
 
 
 		while (true) 
 		{
 			//Listen & Accept Connection and Create new CONNECTION SOCKET
 			Socket s = serverSock.accept();
 			
 			
 			// The next 3 lines create a buffer reader that
 			// reads from the socket s.
 			InputStream is = s.getInputStream();
 			InputStreamReader isr = new InputStreamReader(is);
 			BufferedReader br = new BufferedReader(isr);
 
 			// The next 2 lines create a output stream we can
 			// write to.
 			OutputStream os = s.getOutputStream();
 			dos = new DataOutputStream(os);
 			
 			// Read HTTP request (empty line signal end of request)
 			String input = br.readLine();
 			String filename = "";
 			String parameters = "";
 
 			if(input == null || input.length() <= 0) // To prevent crashing on receipt of stray msg
 				continue;
 
 
 			StringTokenizer st = new StringTokenizer(input);
 		
 			if (st.nextToken().equals("GET"))
 			{
 				// This is a GET request.  Parse filename.
 				filename = st.nextToken();
 				
 				if (filename.startsWith("/")) 
 				{
 					filename = filename.substring(1);
 				
 					if(filename.indexOf("?") != -1)
 					{
 						parameters = filename.substring(filename.indexOf("?"));
 						filename = filename.substring(0, filename.indexOf("?"));
 						
 						if (parameters.startsWith("?")) 
 						{
 							parameters = parameters.substring(1);
 						}
 					}
 
 					
 					processParameters(parameters);
 				}
 			    filename = "root/" + filename; //my web folder
 			}
 			// read and throw away the rest of the HTTP request
 			while (input.compareTo("") != 0) 
 			{
 				input = br.readLine();  //Just read and ignore
 			}
 			
 			try{
 			
 			// Open and read the file into buffer
 			File f = new File(filename);
 		      
 			if (f.canRead())
 			{
 				int size = (int)f.length();
 				
 				//Create a File InputStrem to read the File
 				FileInputStream fis = new FileInputStream(filename);
 				byte[] buffer = new byte[size];
 				fis.read(buffer);
 			
 				// Now, write buffer to client
 				// (but, send HTTP response header first)
 				String contents = new String(buffer, "UTF-8");
 				
 
 				processAndSendReply(contents);
 
 				
 			}
 			else
 			{
 				// File cannot be read.  Reply with 404 error.
 				dos.writeBytes("HTTP/1.0 404 Not Found\r\n");
 				dos.writeBytes("\r\n");
 				dos.writeBytes("Cannot find " + filename + " leh");
 			}
 			}
 			catch (Exception ex){
 				System.out.println("Connection over");
 			}
 
 			// Close connection (using HTTP 1.0 which is non-persistent).
 			s.close();
 		}
     }
 
 
     public static void main (String args[]) throws Exception 
 	{
 		WebServer ws = new WebServer();
 		ws.run();
 	}
 }
