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
 		Strategy compare = f.makeNullStrategy();
 		int result = 0;
 		//one scanner that is to be passed throughout the menu when
 		//input is needed, prevents memory leak problems from too many scanners
 		Scanner s = new Scanner(System.in);
 		boolean infLoop = true;
 		//loops indefinitely until exitProgram function is run
 		while (infLoop) {
 			int choice = menu(CSV, strat, compare, s);
 			switch (choice) {
 			case 1: 
 				CSV = selectDataFile(s, f);
 				break;
 			case 2: 
 				strat = selectStrategy(s, f, strat);
 				break;
 			case 3: 
 				result = runSimulation(CSV, strat, f);
 				break;
 			case 4:
 				compare = selectComparison(s,f);
 				break;
 			case 5:
				runComparison(CSV, compare, strat, f, result);
				break;
 			case 6: 
 				exitProgram(s);
 				break;
 			}
 		}
 	}
 
 
 	private static int menu (Reader CSV, Strategy strat, Strategy compare, Scanner s) {
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
 
 			System.out.println();
 			System.out.print("Current selected Comparison Strategy:\t");
 
 			System.out.print(compare.getStrategyName());
 			System.out.printf("\n\n");
 		}
 		System.out.println("Please select an option below");
 		System.out.println("1 - Select Data File");
 		System.out.println("2 - Select Strategy");
 		System.out.println("3 - Run Simulation");
 		System.out.println("4 - Select Comparison Strategy");
 		System.out.println("5 - Run Comparison");
 		System.out.println("6 - Exit Program");
 		System.out.println("Please select a number (1 - 6)");
 
 		int choice = 0;
 		boolean validInput = false;
 		while (validInput == false) {// loop to repeat until there is a valid input
 			try {
 				choice = s.nextInt();
 				if (choice < 1 || choice > 6) {
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
 
 
 	private static Strategy selectComparison(Scanner s, Factory f) {
 		System.out.println("Select a strategy to compare from the list");
 		System.out.println("1 - No Strategy");
 		System.out.println("2 - Dumb Strategy");
 		System.out.println("3 - Mean Reversion");
 		System.out.println("4 - Momentum Strategy");
 		Strategy compare = null;
 		try {
 			int choice = s.nextInt();
 			if (choice < 1 || choice > 4) {
 				throw new InputMismatchException();
 			}
 			switch (choice) {
 			case 1:
 				compare = f.makeNullStrategy();
 				break;
 			case 2:
 				compare = f.makeDumbStrategy();
 				break;
 			case 3:
 				compare = f.makeMeanReversionStrategy();
 				break;
 			case 4:
 				compare = f.makeMomentumStrategy();
 				break;
 			}
 		} catch (InputMismatchException e) {
 			System.out.println("Wrong input, returning to menu\n\n");
 		}
 		return compare;
 	}
 
 
 	private static int runSimulation(Reader CSV, Strategy strat, Factory f) {
 		int profit = 0;
 		
 		long initTime = System.currentTimeMillis();
 		//cannot run simulation if there is no CSV chosen
 		if (CSV == null) {
 			System.out.println("A CSV file has not been selected, cannot run simulation"); 
 			return profit;//exit function early
 		}
 
 		SignalGenerator signalGenerator = null;
 		try {
 			CSV = f.makeReader(CSV.getFilePath());
 			System.out.println("Loading " + CSV.getFilePath());
 			signalGenerator = new SignalGenerator(CSV, strat, f);
 		} catch (IOException e) {
 			System.out.println("Error in reading CSV file, exiting simulation");
 			return profit;
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
 			profit = eval.calculateProfit(strategyTrades);
 			System.out.println("Profit: $("+profit+")!");
 		}
 		long finalTime = System.currentTimeMillis();
 		double millisTaken = (double)finalTime-(double)initTime;
 		System.out.printf("\nTime Taken: %.2f seconds\n",millisTaken/1000);
 		//System.out.println("Time Taken: "+(System.currentTimeMillis()-initTime)/1000+" seconds");
 		System.out.println("\n###########################################");
 		return profit;
 	}
 	
	private static void runComparison(Reader CSV, Strategy compare, Strategy strat, Factory f, int result) {
 		int profit = 0;
 		//cannot run simulation if there is no CSV chosen
 		if (CSV == null) {
 			System.out.println("A CSV file has not been selected, cannot run simulation"); 
 			return;//exit function early
 		}
 		
 		SignalGenerator signalGenerator = null;
 		try {
 			CSV = f.makeReader(CSV.getFilePath());
 			System.out.println("Loading " + CSV.getFilePath());
 			signalGenerator = new SignalGenerator(CSV, compare, f);
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
 		compare.reset();
 
 		System.out.println("Running simulation ");
 
 		Order o;
 		while ((o = signalGenerator.advance()) != null) {
 			//one iteration equals one order being processed and traded
 			orderBooks.processOrder(o);
 			tradeEngine.trade();
 			displayProgress(CSV, orderBooks);
 
 
 		}
 		
 		Evaluator eval = new Evaluator(compare, tradeEngine, orderBooks);
 		System.out.println("\nFinished Simulation, now evaluating...\n");
 		List<Trade> strategyTrades = eval.evaluate();
 		if (strategyTrades.size() == 0) {
 			System.out.println("STRATEGY CREATED NO TRADES");
 			//return;
 		} else {
 			//displayEvaluation(strategyTrades);
 			//System.out.println("\nSumming up the buys and sells\n");
 			profit = eval.calculateProfit(strategyTrades);
			System.out.print(compare.getStrategyName());
			System.out.println(" Strategy's Profit: $("+profit+")!");
 		}
		int comparedProfit = 0;
 		if (profit > result) {
			comparedProfit = profit - result;			
			System.out.print (compare.getStrategyName() );
			System.out.format (" resulted in more profit by $%d%n", comparedProfit);
 		} else if (result > profit){
			comparedProfit = result - profit;
			System.out.print (strat.getStrategyName());
			System.out.format (" resulted in more profit by $%d%n", comparedProfit);
 		} else {
			System.out.print("Both ");
			System.out.print(compare.getStrategyName());
			System.out.print(" and");
			System.out.print(strat.getStrategyName());
			System.out.println(" are equal in profit");
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
