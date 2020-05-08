 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import Message.GameDataMessageFromClient;
 import Message.GameDataMessageFromClient.DistributionFromClient;
 import Message.GameDataMessageFromClient.HumanResourcesFromClient;
 import Message.GameDataMessageFromClient.ProductionFromClient;
 import Message.GameDataMessageFromClient.PurchaseFromClient;
 import Message.GameDataMessageFromClient.DistributionFromClient.OfferFromClient;
 import Message.GameDataMessageFromClient.HumanResourcesFromClient.BenefitBookingFromClient;
 import Message.GameDataMessageFromClient.ProductionFromClient.ProductionOrderFromClient;
 import Message.GameDataMessageFromClient.PurchaseFromClient.AcceptedSupplierOfferFromClient;
 import Message.GameDataMessageFromClient.PurchaseFromClient.RequestFromClient;
 import Server.Benefit;
 import Server.Company;
 import Server.GameEngine;
 import Server.Location;
 
 public class GameTestConsole {
 
 	private static BufferedReader		console		= new BufferedReader(new InputStreamReader(System.in));
 
 	private static GameEngine			game		= GameEngine.getGameEngine();
 
 	private static ArrayList<String>	playernames	= new ArrayList<String>();
 
 	public static void main(String[] args) throws Exception {
 
 		startGame();
 
 		while (true) {
 			play();
 		}
 
 	}
 
 	public static void startGame() throws Exception {
 		Benefit.initBenefits();
 		Location.initLocations();
 
 		System.out.println("Willkommen beim Planspiel...");
 		System.out.println("Zunchst muessen die Spieler erstellt werden. Keine Eingabe beendet das Erstellen.");
 		System.out.println("Folgende Lnder sind verfgbar");
 
 		String locations = "";
 		for (Location l : Location.getListOfLocations()) {
 			locations += l.getCountry() + ", ";
 		}
 
 		System.out.println(locations.substring(0, locations.length() - 2));
 
 		int counter = 0;
 
 		while (true) {
 			System.out.print("Spielername: ");
 			String name = console.readLine();
 			if (name.equals("")) {
 				System.out.println("Spieler hinzufuegen wurde beendet");
 				break;
 			}
 			System.out.print("Land: ");
 			String location = console.readLine();
			game.addCompany(new Company(Location.getLocationByCountry(location), name));
 			++counter;
 			playernames.add(name);
 		}
 		if (counter < 1) {
 			throw new IllegalArgumentException("Spiel wurde mit nur einem Spieler gestartet!");
 		}
 
 	}
 
 	private static void play() throws Exception {
 		ArrayList<GameDataMessageFromClient> messages = new ArrayList<GameDataMessageFromClient>();
 		for (String player : playernames) {
 			System.out.println();
 			System.out.println();
 			System.out.println("Spieler: " + player);
 			System.out.println("Zunaechst folgen die Bestellungen der Rohstoffe z.B. \"Wafer;20\"");
 			ArrayList<RequestFromClient> requests = new ArrayList<RequestFromClient>();
 
 			while (true) {
 				String input = console.readLine();
 				if (input.equals("")) {
 					break;
 				}
 				try {
 					requests.add(new RequestFromClient(input.split(";")[0], Integer.valueOf(input.split(";")[1])));
 				} catch (IndexOutOfBoundsException e) {
 					System.err.println("Ungueltige Eingabe wurde verworfen!");
 				}
 			}
 
 			ArrayList<AcceptedSupplierOfferFromClient> accepted = new ArrayList<AcceptedSupplierOfferFromClient>();
 
 			ArrayList<ProductionOrderFromClient> proOrder = new ArrayList<ProductionOrderFromClient>();
 
 			ArrayList<OfferFromClient> offerList = new ArrayList<OfferFromClient>();
 
 			ArrayList<BenefitBookingFromClient> bBook = new ArrayList<BenefitBookingFromClient>();
 
 			System.out.println("Bitte gib die Benefits an die du Buchen moechtest und die Dauer z.B. \"Sport;2\"");
 			System.out.println("Folgende Benefits sind verfgbar");
 
 			String benefits = "";
 			for (Benefit b : Benefit.getBookableBenefits()) {
 				benefits += b.getName() + " (" + b.getCostsPerRound() + "), ";
 			}
 
 			System.out.println(benefits.substring(0, benefits.length() - 2));
 			while (true) {
 				String input = console.readLine();
 				if (input.equals("")) {
 					break;
 				}
 				try {
 					bBook.add(new BenefitBookingFromClient(input.split(";")[0], Integer.valueOf(input.split(";")[1])));
 				} catch (IndexOutOfBoundsException e) {
 					System.err.println("Ungueltige Eingabe wurde verworfen!");
 				}
 			}
 
 			System.out.print("Bitte gib den neuen Lohn an: ");
 			int wage = Integer.valueOf(console.readLine());
 
 			System.out.println("Moechtest du in der naechsten Runde Marketin kaufen? [Y/N] ");
 			boolean marketing = (console.readLine().toUpperCase().equals("Y") ? true : false);
 
 			System.out.println("Moechtest du in der naechsten Runde deinen Maschinenparkt erweitern? [Y/N] ");
 			boolean maschine = (console.readLine().toUpperCase().equals("Y") ? true : false);
 
 			messages.add(new GameDataMessageFromClient(player, new PurchaseFromClient(requests, accepted),
 					new ProductionFromClient(proOrder), new DistributionFromClient(offerList), maschine,
 					new HumanResourcesFromClient(bBook), wage, marketing));
 		}
 		
 		Object o = game.startNextRound(messages);
 		int i = 1;
 	}
 
 }
