 package genBot2;
 
 import java.net.MalformedURLException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 
 import serialRMI.SerialRMIException;
 
 public class TestAllowedIngredients {
 
 	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException, SerialRMIException, SQLException, MaxAttemptsToMeetPriceConstraintException {
 		Ingredient[] alleZutaten = IngredientArray.getInstance().getAllIngredients();
 		Ingredient[] erlaubteZutaten1 = {alleZutaten[5], alleZutaten[6]};
 
 		CocktailQueue queue = new CocktailQueue();
 
 		QueueManager queueManager;
 
 		queueManager = new QueueManager(queue, "", "", 250);
 		queueManager.start();
 
 		RemoteOrderImpl remoteOrderImpl = new RemoteOrderImpl(queueManager);
 		
 		remoteOrderImpl.generateEvolutionStack("Test1", erlaubteZutaten1, 20);
 		System.out.println("generated");
 		
 		remoteOrderImpl.setProps("Test1", 15, 0, 2, 0.05, 5, "blub", "0000011");
 		int gen = -1;
 		while (true) {
 			gen++;
 					
 			CocktailWithName[] cg1 = remoteOrderImpl.getNamedPopulation("Test1");
 			for (int i = 0; i < cg1.length; i++) {
 				System.out.println(cg1[i].toString());
 			}
 
 			for (int num = 0; num < 15; num++) {
 				String name = "Test1-" + gen + "-" + num;
 			
 				Cocktail cocktail = remoteOrderImpl.readGenerationManager("Test1").getCocktailByName(name);
 				if (cocktail.getAmount("Orange Juice") != 0) {
 					System.out.println(name + ": " + cocktail);
 				}
 				
 				remoteOrderImpl.queueCocktail("Test1", name);
				remoteOrderImpl.deleteCocktailFromQueue(name);
//				remoteOrderImpl.setCocktailFitness("Test1", name, 10.0);
 			}
//			CocktailWithName[] cg2 = remoteOrderImpl.getNamedPopulation("Test1");
//			for (int i = 0; i < cg2.length; i++) {
//				System.out.println(cg2[i].toString());
//			}			
 		}
 	}
 
 }
