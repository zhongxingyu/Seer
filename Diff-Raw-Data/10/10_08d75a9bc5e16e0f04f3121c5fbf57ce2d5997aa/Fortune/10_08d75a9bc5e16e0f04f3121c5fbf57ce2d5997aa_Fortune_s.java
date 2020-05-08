 package gui;
 
 import java.util.Random;
 
 /**
  * gui/Fortune.java 
  * <br><br>
  * Fortunes / Jokes for new terminals.
  * 
  * @author Pavel Čurda
  * 
  * @team <i>OutOfMemory</i> for KIV/OS 2013
  * @teamLeader Radek Petruška radekp25@students.zcu.cz
  * 
  */
 public final class Fortune {
 	
 	/** For choosing random ones */
 	private final Random random;
 	/** How maby jokes are avaiable */
 	private static int jokesCount;
 	
 	/** The jokes */
 	private static final String [] jokes = {  
 	
 	" ___________________________________________"
 	+"\n( Working together towards a common goal... )"
 	+"\n( Until a \"difference of opinion\" gets      )"
 	+"\n( someone dropped into molten lava.         )"
 	+"\n(                                           )"
 	+"\n( -- TEAMWORK                               )"
 	+"\n -------------------------------------------"
 	+"\n   o"
 	+"\n    o"
 	+"\n        .--."
 	+"\n       |o_o |"
 	+"\n       |:_/ |"
 	+"\n      //   \\ \\"
 	+"\n     (|     | )"
 	+"\n    /'\\_   _/`\\"
 	+"\n    \\___)=(___/"
 	+"\n\n",
 	
 	"This joke has been banned.\n\n",
 	
	"Joke not found.\n\n"
 	
 	};
 	
 
 	/**
 	 * Constructor for fortunes.
 	 * 
 	 * @param seed, use 0 for random.
 	 */
 	public Fortune(long seed) {
 
 		if (seed == 0){
 			this.random = new Random();
 		} else {
 			this.random = new Random(seed);
 		}
 		
 		jokesCount = jokes.length;
 
 	}
 	
 	/**
 	 * Gives you random joke.
 	 * 
 	 * @return
 	 */
 	public String getRandomJoke(){
 		
 		int number = (Math.abs(this.random.nextInt())) % jokesCount;
 		
 		return(jokes[number]);
 		
 	}
 	
 	/**
 	 * Gives you specific joke by your number.
 	 * 
 	 * @param number
 	 * @return joke or null.
 	 */
 	public static String getJoke(int number){
 		
 		if (number < 0){
 			return (null);
 		}
 		
 		if (number >= jokesCount){
 			return (null);
 		}
 		
 		return (jokes[number]);
 		
 	}
 
 
 }
