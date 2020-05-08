 /**
  *  A simple chatbot.
  *
  * @author  Zach Arthurs
  * @version 30 Jan 2013
  */
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.Hashtable;
 import java.util.Random;
 import java.util.Scanner;
 
class  ConversationEC  {
 	/** Random responses to user requests */
 	private static final String[] cannedlist = {"Who is best pony?", "How do you think we could make this 20 percent cooler?", "Wouldn't you agree that Friendship is Magic?", "Aren't the Cutie Mark Crusaders cute?", "What's your cutie mark? Sadly, I'm a blank flank.", "Would you like some cider from Sweet Apple Acres?", "I'm gonna love and tolerate till the end of time.", "I'd like to be a tree."};
 	
 	/** Computes the length of the list of strings. */
 	private static final int num_canned = cannedlist.length;
 	
 	/** The string that starts the conversation */
 	private static final String start = "Howdy everypony!";
 	
 	/** The string that ends the conversation */
 	private static final String end = "Goodbye, everypony!";
 	
 	/**
 	 *  Asks the user of the number of rounds of conversation that they wish to have.
 	 *
 	 *  @return  Number of rounds of conversation
 	 */
 	private static int rounds() {
 		int rounds;
 		System.out.println("How many rounds?");
 		Scanner input = new Scanner(System.in);
 		try {
 			rounds = input.nextInt();
 			} catch(Exception e) {
 				System.out.println("Error! That input is not valid.");
 				rounds = 0;
 				System.exit(3);
 			}
 		return rounds;
 	}
 	
 	/**
 	     *  Has a conversation with the user.
 	     *
 	     *  
 	     */		 
 	public static void main(String[] args){
 		
 		// This is a hashtable used later to translate mirror words.
 		Hashtable<String, String> mirrorwords = new Hashtable<String, String>();
 		mirrorwords.put("You", "I");
 		mirrorwords.put("you", "I");
 		mirrorwords.put("I", "you");
 		mirrorwords.put("You're", "I'm");
 		mirrorwords.put("you're", "I'm");
 		mirrorwords.put("I'm", "you're");
 		mirrorwords.put("am", "are");
 		mirrorwords.put("are", "am");
 		mirrorwords.put("my", "your");
 		mirrorwords.put("your", "my");
 		mirrorwords.put("My", "Your");
 		mirrorwords.put("Your", "My");
 		mirrorwords.put("me", "you");
 		mirrorwords.put("you.", "me.");
 		mirrorwords.put("me.", "you.");
 		mirrorwords.put("me?", "you?");
 		mirrorwords.put("you?", "me?");
 		mirrorwords.put("me!", "you!");
 		mirrorwords.put("you!", "me!");
 		
 		int rounds = rounds();
 		String[] conversation = new String [(rounds*2 + 2)];
 		conversation[0] = start;
 		conversation[rounds*2 + 1] = end;
 		System.out.println(conversation[0]);
 		
 		for (int i = 1; i <= rounds*2; i += 2){
 			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
 			System.out.print(">");
 			String user = "foobar";
 			try { 
 				user = input.readLine();
 				} catch (Exception e) {
 					System.out.print("Error on input.");
 				}
 			conversation[i] = user;
 			String delimiter = " ";
 			String[] userwords = user.split(delimiter);
 			String computer = "";
 			boolean mirror_words_found = false;
 			for (int j = 0; j < userwords.length; j++) {
 				String word = userwords[j];
 				if (mirrorwords.containsKey(word)) {
 					mirror_words_found = true;
 					computer = computer + " " + mirrorwords.get(word);
 					} else {
 						computer = computer + " "+ word;
 					}
 			}
 			if (mirror_words_found) {
 				computer = computer.replace(".", "?");
 				computer = computer.replace("!", "?");
 				conversation[i+1] = computer;
 				System.out.println(computer);
 				}  else {
 					int rndnum = new Random().nextInt(num_canned);
 					conversation[i+1] = cannedlist[rndnum];
 					System.out.println(conversation[i+1]);
 				}
 		}
 		
 		System.out.println(conversation[rounds*2 + 1]);
 		System.out.println("\n\n\nTranscript");
 		for (int k = 0; k < conversation.length; k++) {
 			String typed = conversation[k];
 			System.out.println(typed);
 		}
 		
 	}
 }
