 package simulator;
 import simulator.strategy.*;
 
 import java.io.IOException;
 import java.util.InputMismatchException;
 import java.util.List;
 import java.util.Scanner;
 
 public class Run {
 	//fix tests structure
 	//make an evaluator
 	//only accepts enter right now
 	//TODO hashmap the orderbooks for speed
 	//TODO fix tradeEngine
 	//factory pattern orders object
 	//internally generate ID for trade orders
 	//extend Trade from Order, Trade holds the 2 orders that are traded
 	//TODO add qualifier features
 	//trading time range
 	//TODO insert on price works, need to insert on time
 
 	public static void main (String[] args) {
 
 		System.out.println("#######--Welcome to the Trade Simulator!--#######");
 		Factory f = new Factory();
 
 		//menu holds CSV file and strategy selected until running time
 		Reader CSV = null;
 		Strategy strat = f.makeNullStrategy();
 		//one scanner that is to be passed throughout the menu when
 		//input is needed, prevents memory leak problems from too many scanners
 		Scanner s = new Scanner(System.in);
 		boolean infLoop = true;
 		//loops indefinitely until exitProgram function is run
 		while (infLoop) {
 			int choice = menu(CSV, strat, s);
 			switch (choice) {
 			case 1: 
 				CSV = selectDataFile(s, f);
 				break;
 			case 2: 
 				strat = selectStrategy(s, f, strat);
 				break;
 			case 3: 
 				runSimulation(CSV, strat, f);
 				break;
 			case 4: 
 				exitProgram(s);
 				break;
 			}
 		}
 	}
 
 	private static int menu (Reader CSV, Strategy strat, Scanner s) {
 		{//displays currently selected options in menu
 			System.out.print("\nCurrent selected CSV:\t\t");
 			if (CSV == null) {
 				System.out.print("None");
 			} else {
 				System.out.print(CSV.getFilePath());
 			}
 			System.out.println();
 			System.out.print("Current selected Strategy:\t");
 
 			System.out.print(strat.getStrategyName());
 			System.out.printf("\n\n");
 		}
 		System.out.println("Please select an option below");
 		System.out.println("1 - Select Data File");
 		System.out.println("2 - Select Strategy");
 		System.out.println("3 - Run Simulation");
 		System.out.println("4 - Exit Program");
 		System.out.println("Please select a number (1 - 4)");
 
 		int choice = 0;
 		boolean validInput = false;
 		while (validInput == false) {// loop to repeat until there is a valid input
 			try {
 				choice = s.nextInt();
 				if (choice < 1 || choice > 4) {
 					throw new InputMismatchException();
 				}
 				validInput = true;
 			} catch (InputMismatchException e) {
 				//exception catches everything except 1-4
 				System.out.println("Wrong input, select a number between 1 and 4");
 				s.nextLine();
 			}
 		}
 		return choice;
 	}
 
 	private static Reader selectDataFile(Scanner s, Factory f) {
 		System.out.println("Type the filepath of the selected CSV file");
 		System.out.println("E.g. C:\\Users\\user\\Desktop\\sircaData.csv");
 		Reader CSV = null;
 		String filepath = s.next();
 		try {
 			CSV = f.makeReader(filepath);
 		} catch (Exception e) {
 			System.out.printf("Invalid filepath, returning to menu\n\n");
 		}
 		return CSV;
 	}
 	private static Strategy selectStrategy(Scanner s, Factory f, Strategy oldStrat) {
 		//must find a way to automate the list menu, currently must
 		//manually add them in
 		System.out.println("Select a strategy from the list");
 		System.out.println("1 - No Strategy");
 		System.out.println("2 - Dumb Strategy");
 		System.out.println("3 - Mean Reversion Strategy");
 		System.out.println("4 - Momentum Strategy");
 
 		Strategy newStrat = null;
 
 		try {
 			int choice = s.nextInt();
 
 
 			if (choice < 1 || choice > 4) {
 				throw new InputMismatchException();
 			}
 			switch (choice) {
 			case 1:
 				newStrat = f.makeNullStrategy();
 				break;
 			case 2:
 				newStrat = f.makeDumbStrategy();
 				break;
 			case 3:
 				newStrat = f.makeMeanReversionStrategy();
 				break;
 			case 4:
 				newStrat = f.makeMomentumStrategy();
 				break;
 			}
 
 		} catch (InputMismatchException e) {
 			System.out.println("Wrong input, returning to menu\n\n");
 			newStrat = oldStrat;
 		}
 		return newStrat;
 	}
 
 	private static Double getMeanReversionArgs(Scanner s) {
 
 		System.out.println("Enter the mean value");
 		Double mean = (s.nextDouble());
 		return mean;
 	}
 
 	private static void runSimulation(Reader CSV, Strategy strat, Factory f) {

 		//cannot run simulation if there is no CSV chosen
 		if (CSV == null) {
 			System.out.println("A CSV file has not been selected, cannot run simulation"); 
 			return;//exit function early
 		}
 
 		SignalGenerator signalGenerator = null;
 		try {
 			CSV = f.makeReader(CSV.getFilePath());
 			System.out.println("Loading " + CSV.getFilePath());
 			signalGenerator = new SignalGenerator(CSV, strat, f);
 		} catch (IOException e) {
 			System.out.println("Error in reading CSV file, exiting simulation");
 			return;
 		}
 
 		OrderBooks  orderBooks  = f.makeOrderBooks();
 		TradeEngine tradeEngine = f.makeTradeEngine();
 		//ensure each simulation run begins with empty orderbooks and trade lists
 		//ensures successive simulations are unaffected
 		orderBooks.resetOrderBooks();
 		tradeEngine.resetTradeEngine();
 		
 		//reset any strategy attributes after each simulation
 		strat.reset();
 
 		System.out.println("Running simulation ");
 
 		Order o;
 		while ((o = signalGenerator.advance()) != null) {
 			//one iteration equals one order being processed and traded
 			orderBooks.processOrder(o);
 			tradeEngine.trade();
 			displayProgress(CSV, orderBooks);
 
 
 		}
 		Evaluator eval = new Evaluator(strat, tradeEngine, orderBooks);
 		System.out.println("\nFinished Simulation, now evaluating...\n");
 		List<Trade> strategyTrades = eval.evaluate();
 		if (strategyTrades.size() == 0) {
 			System.out.println("STRATEGY CREATED NO TRADES");
 			//return;
 		} else {
 			displayEvaluation(strategyTrades);
 			System.out.println("\nSumming up the buys and sells\n");
 			int profit = eval.calculateProfit(strategyTrades);
 			System.out.println("Profit: $("+profit+")!");
 		}
 		System.out.println("\n###########################################");
 	}
 	private static void exitProgram(Scanner s) {
 		System.out.println("Cya!");
 		s.close();
 		System.exit(0);
 	}
 
 	private static void displayProgress(Reader CSV, OrderBooks books) {
 		System.out.printf("\rsimulated time: %s | %.2f percent done",
 				books.getSimulatedTime(),
 				100*((float)CSV.getProgress()/(float)CSV.getFileSize()));
 	}
 
 
 	private static void displayEvaluation(List<Trade> strategyTrades) {
 
 		System.out.println("Printing the trades generated by Strategy\n");
 		//displayBidAskHeader();
 		//for (Trade t:strategyTrades) {
 		//	displayBidAskofTrade(t);
 		//}
 		displayTradeHeader();
 		for (Trade t:strategyTrades) {
 			displayTrade(t);
 		}
 	}
 
 	private static void displayTradeHeader() {
 		System.out.print("Bid ID\t\t\t");
 		System.out.print("Ask ID\t\t\t");
 		System.out.print("Price\t");
 		System.out.print("Volume");
 		System.out.println();
 	}
 
 	private static void displayBidAskHeader() {
 		System.out.print("Bid ID\t\t\t");
 		System.out.print("Price\t");
 		System.out.print("Volume\t|");
 
 		System.out.print("Ask ID\t\t\t");
 		System.out.print("Price\t");
 		System.out.print("Volume");
 		System.out.println();
 	}
 
 	private static void displayBidAskofTrade(Trade t) {
 		System.out.print(String.format("%019d", t.getBid().ID())+"\t");
 		System.out.print(t.getBid().price()+"\t");
 		System.out.print(t.getBid().volume() + "\t|");
 
 		System.out.print(String.format("%019d", t.getAsk().ID())+"\t");
 		System.out.print(t.getAsk().price()+"\t");
 		System.out.print(t.getAsk().volume());
 		System.out.println();
 	}
 
 	private static void displayTrade(Trade t) {
 
 		System.out.print(String.format("%019d", t.getBid().ID())+"\t");
 		System.out.print(String.format("%019d", t.getAsk().ID())+"\t");
 		System.out.print(t.price()+"\t");
 		System.out.print(t.volume());
 
 		System.out.println();
 	}
 
 }
