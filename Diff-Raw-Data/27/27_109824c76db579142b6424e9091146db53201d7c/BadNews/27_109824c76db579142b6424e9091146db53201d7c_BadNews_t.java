 import java.util.ArrayList;
 
 
 public class BadNews {
	private ArrayList<String> quotes = new ArrayList();
 	
 
 	
 	public void dontBeSoSerious(){
 		System.out.println("Whenever you feel sad, just remember that somewhere there in this world there's an idiot pulling a door that says 'PUSH'");

 		System.out.println("I always wanted to be a procrastinator, I just never got around to it");
 		
 	}
 	
	
	
	public void addQuotes(){
		quotes.add("Jokes about German sausage are the wurst");
		quotes.add("Be kind to your dentist, He has fillings to");
 	}
 	
	public void someQuotes(){
		System.out.println("Here is a list of some Quotes");
		for (String quote : quotes)
			System.out.println(quote);
	}

 
 	public static void main(String[] args) {
 
 		BadNews justKidding = new BadNews();
 		
 		justKidding.dontBeSoSerious();
		justKidding.addQuotes();

 		justKidding.someQuotes();
 		
 
 	}
 
 }





