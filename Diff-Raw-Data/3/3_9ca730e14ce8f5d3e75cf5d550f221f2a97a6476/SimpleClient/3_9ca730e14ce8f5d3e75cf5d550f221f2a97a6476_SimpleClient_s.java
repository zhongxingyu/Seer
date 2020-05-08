 package cz.cuni.mff.d3s.adapt.bookstore.client;
 
 import java.util.Random;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 
 import cz.cuni.mff.d3s.adapt.bookstore.services.Book;
 import cz.cuni.mff.d3s.adapt.bookstore.services.Store;
 import cz.cuni.mff.d3s.adapt.bookstore.services.Wallet;
 
 @Component
 public class SimpleClient implements Runnable {
 
 	@Requires
 	private Store store;
 	
 	public SimpleClient() {
 		
 	}
 
 	@Override
 	public void run() {
 		System.err.printf("SimpleClient.run()\n");
 		Random random = new Random(0);
 		Book[] lastSearchResults = new Book[0];
 		Wallet wallet = new MillionareWallet();
 		while (true) {
 			int action = random.nextInt(100);
 			if (action == 0) {
 				if (lastSearchResults.length > 0) {
 					int index = random.nextInt(lastSearchResults.length);
 					store.buy(lastSearchResults[index], wallet);
 				}
 			} else {
 				String randomString = generateRandomString(3, "abcdefghijklmnopqrstuvwxyz", random.nextInt());
 				lastSearchResults = store.fulltextSearch(randomString);
 			}
 		}
 	}
 	
 	private String generateRandomString(int length, String allowedCharacters, int seed) {
 		Random rnd = new Random(seed);
 		StringBuilder result = new StringBuilder();
 		for (int i = 0; i < length; i++) {
 			int randomIndex = rnd.nextInt(allowedCharacters.length());
 			char nextCharacter = allowedCharacters.charAt(randomIndex);
 			result.append(nextCharacter);
 		}
 		return result.toString();
 	}
 	
 	private class MillionareWallet implements Wallet {
 		@Override
 		public boolean pay(int amount) {
 			return true;
 		}
 	}
 	
 	@Validate
 	public void start() {
		run();
 	}
 }
