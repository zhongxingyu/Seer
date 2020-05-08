 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 
 public class TCPServer {
         
         private Integer gameState; // 0 - waiting, 1 - in progress, 2 - completed
         private Integer gameLength = 6 * 60 * 1000; // 6 minutes to milliseconds
         private Integer wordTimeLength = 2 * 60;
         private Integer numWords = 3;
         private Vector<String> inputWords  = new Vector<String>();
         private Vector<String> scrambledWords  = new Vector<String>();
         private Integer currentWordIndex;
         DataOutputStream outToClient;
         BufferedReader inFromClient;
         BufferedReader inFromUser;
         private Integer alwaysConstantWordTime = 2 * 5;
 
         public TCPServer()  {
 
         gameState = 0; // 0 - waiting, 1 - in progress, 2 - completed
         gameLength = 6 * 60 * 1000; // 6 minutes to milliseconds
         wordTimeLength = alwaysConstantWordTime;
 
         numWords = 3;
         inputWords  = new Vector<String>();
         scrambledWords  = new Vector<String>();
            
         }
 
         private void scrambleWords()
         {   
             scrambledWords.clear();
 
             for(Integer tempIndex = 0; tempIndex < numWords; tempIndex++)
             {
                 scrambledWords.add(inputWords.elementAt(tempIndex));
                 String tempWord = scrambledWords.elementAt(tempIndex);
                 Integer lengthOfWord = tempWord.length();
                 int eightyPercent =  (int)Math.round((lengthOfWord * .8));
                 int indexToRemove = 0;
                 for(Integer tempRemoveChars = 0; tempRemoveChars < eightyPercent; tempRemoveChars++)
                 {
                     do{
 
                     indexToRemove = (int)(Math.random() * lengthOfWord);
 
                     }while(tempWord.charAt(indexToRemove) == '_');
 
                     
                     char[] tempCharArray = tempWord.toCharArray();
                     tempCharArray[indexToRemove] = '_';
                     tempWord = String.valueOf(tempCharArray);
                     scrambledWords.setElementAt(tempWord, tempIndex);
                 }
 
             }
         }
 
         public void getMoreWordsFromOracle() 
         {
             try {
                 inputWords.clear();
             System.out.println("Enter " + numWords + " words: ");
             
 
             for(Integer tempIndex = 0; tempIndex < numWords; tempIndex++)
             {
            //     System.out.println("index is " + tempIndex + " numWords is " + numWords + "inputWords is " + inputWords.size());
                 String tempInputString = new String(inFromUser.readLine());
                 inputWords.add(tempInputString);
                 
               //  currentWordIndex = tempIndex;
             }
             //System.out.println("done");
             scrambleWords(); // create the words to send to client
 
 
             System.out.println("These are sent to the analyst -");
             for(Integer tempIndex = 0; tempIndex < numWords; tempIndex++)
             {
                 System.out.println(scrambledWords.elementAt(tempIndex));
                 outToClient.writeBytes(scrambledWords.elementAt(tempIndex));
                 outToClient.writeBytes("\n");
                 currentWordIndex = tempIndex;
             }}
             catch(Exception e)
             {
                 System.out.println("Unable to send more words");
                 System.exit(2);
             }
         }
         public void processClientMessage(String message) throws Exception
         {
             //System.out.println("Got this message from client - " + message);
             switch(message.charAt(0))
             {
                 case '0':   // guess word
                resetWordTimeLength();
                 if(message.substring(1).compareTo(inputWords.elementAt(currentWordIndex)) == 0) // correct guess
                 {
                     --currentWordIndex;
                     if(currentWordIndex < 0) // get more words from oracle
                     {
                         getMoreWordsFromOracle();
                     }
                      
                     sendMessageToClient(new String("1"+scrambledWords.elementAt(currentWordIndex)));
                 }
                 else // wrong guess
                 {
                     System.out.println("Wrong guess - " + message.substring(1));
                     --currentWordIndex;
                    if(currentWordIndex < 0) // get more words from oracle
                    {
                        getMoreWordsFromOracle();
                    }
                     
                    sendMessageToClient(new String("3"+scrambledWords.elementAt(currentWordIndex)));
                    
                 }
                 break;
                 case '1': // ask question
                 System.out.println("Analyst asks the following question (Y/N)- ");
                 System.out.println(message.substring(1));
                 String tempInputString = "N";
                 do {
                     if(!tempInputString.equals(new String("Y")) && !tempInputString.equals(new String("N")))
                         System.out.println("Only Y or N are acceptable. Try again.");
                     tempInputString = inFromUser.readLine();
                  }while(!tempInputString.equals(new String("Y")) && !tempInputString.equals(new String("N")));
 
                 if(tempInputString.charAt(0) == 'Y')
                     sendMessageToClient(new String("5Yes"));
                 else
                     sendMessageToClient(new String("5No"));
 
                 break;
                 default:
                 System.out.println("Got unknown message from client");
             }
         }
         public void sendTimeUpToClient()
         {
             sendMessageToClient("4"+scrambledWords.elementAt(currentWordIndex));
         }
 
         public void sendGameOverToClient()
         {
             sendMessageToClient("2");
 
         }
 
         public void sendMessageToClient(String message)
         {
             try {
                 outToClient.writeBytes(message);
                 outToClient.writeBytes("\n");
             }
             catch(Exception e)
             {
                 System.out.println("Unable to communicate withc client");
                 System.exit(2);
             }
         }
 
         public void resetWordTimeLength()
         {
             wordTimeLength = alwaysConstantWordTime;
             
         }
         void run() throws Exception{
         String clientSentence;
         String capitalizedSentence;
         ServerSocket welcomeSocket = new ServerSocket(6789);
         Timer timer = new Timer();
         inFromUser = new BufferedReader(new InputStreamReader(System.in));
         gameState = 0;
         // Welcome message
         System.out.println("Waiting for client");
 
         // Socket connections - wait for client
         Socket connectionSocket = welcomeSocket.accept();
         inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
         outToClient = new DataOutputStream(connectionSocket.getOutputStream());
 
         // Got client - request N words
         System.out.println("Enter " + numWords + " words: ");
         
         for(Integer tempIndex = 0; tempIndex < numWords; tempIndex++)
         {
             String tempInputString = inFromUser.readLine();
             inputWords.add(tempInputString);
             //inputWords.add("AAA");
         }
 
         scrambleWords(); // create the words to send to client
 
 
         System.out.println("These are sent to the analyst -");
         for(Integer tempIndex = 0; tempIndex < numWords; tempIndex++)
         {
             System.out.println(scrambledWords.elementAt(tempIndex));
             outToClient.writeBytes(scrambledWords.elementAt(tempIndex));
             outToClient.writeBytes("\n");
             currentWordIndex = tempIndex;
         }
 
         timer.schedule(new TimerTask() {
             @Override
             public void run()
             {
                 //System.out.println(" word time " + wordTimeLength);
                 if(--wordTimeLength <= 0)
                 {
                     //System.out.println("1");
                     if(currentWordIndex == 0)
                     {
                         //System.out.println("2");
                        getMoreWordsFromOracle();
                        sendTimeUpToClient();
                         resetWordTimeLength();
 
                     }
                     else
                     {
                        // System.out.println("3");
                         currentWordIndex--;
                         sendTimeUpToClient();
                         resetWordTimeLength();
                     }
                 }
             }
         }, 0, 1000);
         
         
         while(gameState != 2) {
         
 
             clientSentence = new String(inFromClient.readLine());
             processClientMessage(clientSentence);
 
         }
         if(gameState == 2)
         {
             System.out.println("Game over");
         }
         
         
     }
     public static void main(String[] args) {
     try {
     TCPServer s = new TCPServer();
     s.run();
     }
     catch(Exception e)
     {
         System.out.println("Client disconnected abrubtly");
         System.exit(0);
     }
   }
 
 }
