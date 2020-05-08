 /**
  * 
  */
 package at.ac.tuwien.dslab2.presentation.managementClient;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.Iterator;
 import java.util.Scanner;
 import java.util.Set;
 
 import at.ac.tuwien.dslab2.domain.Bill;
 import at.ac.tuwien.dslab2.domain.Event;
 import at.ac.tuwien.dslab2.domain.PriceSteps;
 import at.ac.tuwien.dslab2.service.managementClient.AlreadyLoggedInException;
 import at.ac.tuwien.dslab2.service.managementClient.LoggedOutException;
 import at.ac.tuwien.dslab2.service.managementClient.ManagementClientService;
 import at.ac.tuwien.dslab2.service.managementClient.ManagementClientServiceFactory;
 
 /**
  * @author klaus
  * 
  */
 public class ManagementClient {
 	private static ManagementClientService mcs;
 	private static String userName;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			try {
 				initialize(args);
 				readInput();
 			} finally {
 				close();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void initialize(String[] args) {
 		userName = null;
 
 		if (args.length != 2)
 			usage();
 
 		String analyticsServerName = args[0];
 		String billingServerName = args[1];
 
 		try {
 			mcs = ManagementClientServiceFactory.newManagementClientService(
 					analyticsServerName, billingServerName);
 			mcs.setSubscriptionListener(new SubscriptionListenerImpl());
 		} catch (IOException e) {
 			error("Error while connecting:", e);
 		}
 
 	}
 
 	private static void usage() {
 		System.err.println("usage: java ManagementClient analyticsServerName"
 				+ " billingServerName\n\n"
 				+ "\tanalyticsServerName: the binding name of the "
 				+ "analytics server in the RMI registry"
 				+ "\tbillingServerName: the binding name of the "
 				+ "billing server in the RMI registry\n");
 
 		close();
 		System.exit(0);
 	}
 
 	private static void readInput() {
 		Scanner sc = new Scanner(System.in);
 		String cmd;
 		boolean end;
 
 		System.out.print(getPrompt());
 		end = false;
 		while (!end && sc.hasNextLine()) {
 
 			cmd = sc.nextLine();
 
 			if (!cmd.trim().isEmpty()) {
 				try {
 					String reply = submitCommand(cmd);
 
 					if (reply != null) {
 						System.out.println(reply);
 					} else {
 						end = true;
 					}
 				} catch (Exception e) {
 					System.err.println("ERROR: " + e.getMessage());
 				}
 			}
 			if (!end)
 				System.out.print(getPrompt());
 		}
 	}
 
 	private static String submitCommand(String command) throws RemoteException {
 		if (command == null)
 			throw new IllegalArgumentException("The command is null");
 		if (mcs == null)
 			throw new IllegalStateException(
 					"The ManagementClientService is null");
 
 		// Commands:
 		// !login <username> <password>
 		// !steps
 		// !addStep <min> <max> <fee-fixed> <fee-variable>
 		// !removeStep <min> <max>
 		// !bill <username>
 		// !logout
 		// !subscribe '<regex>'
 		// !unsubscribe <id>
 		// !auto
 		// !hide
 		// !print
		// !end
 
 		final String commands = "!login <username> <password>\n" + "!steps\n"
 				+ "!addStep <min> <max> <fee-fixed> <fee-variable>\n"
 				+ "!removeStep <min> <max>\n" + "!bill <username>\n"
 				+ "!logout\n" + "!subscribe '<regex>'\n"
 				+ "!unsubscribe <id>\n" + "!auto\n" + "!hide\n" + "!print\n"
				+ "!end";
 		final String invalidCommand = "ERROR: Invalid command '" + command
 				+ "'\n\nCommands are:\n" + commands;
 		final String cmdRegex = "![a-zA-Z-]+";
 		String tmp;
 
 		Scanner sc = new Scanner(command);
 		sc.useDelimiter("\\s+");
 		sc.skip("\\s*");
 
 		if (!sc.hasNext(cmdRegex))
 			return invalidCommand;
 
 		tmp = sc.next();
 		if (tmp.equalsIgnoreCase("!login")) {
 			if (!sc.hasNext())
 				return invalidCommand;
 			String userName = sc.next();
 			if (!sc.hasNext())
 				return invalidCommand;
 			String password = sc.next();
 
 			try {
 				mcs.login(userName, password);
 			} catch (AlreadyLoggedInException e) {
 				return "ERROR: You first have to log out!";
 			}
 			ManagementClient.userName = userName;
 
 			return "Successfully logged in as " + userName + "!";
 
 		} else if (tmp.equalsIgnoreCase("!steps")) {
 			PriceSteps pss = null;
 			try {
 				pss = mcs.steps();
 			} catch (LoggedOutException e) {
 				return "ERROR: You have to log in first!";
 			}
 
 			if (pss.isEmpty())
 				return "There are currently no price steps configurated";
 
 			return pss.toString();
 
 		} else if (tmp.equalsIgnoreCase("!addStep")) {
 			if (!sc.hasNextDouble())
 				return invalidCommand;
 			double min = sc.nextDouble();
 			if (!sc.hasNextDouble())
 				return invalidCommand;
 			double max = sc.nextDouble();
 			if (!sc.hasNextDouble())
 				return invalidCommand;
 			double fixedFee = sc.nextDouble();
 			if (!sc.hasNextDouble())
 				return invalidCommand;
 			double variableFee = sc.nextDouble();
 
 			if (max == 0)
 				max = Double.POSITIVE_INFINITY;
 
 			try {
 				mcs.addStep(min, max, fixedFee, variableFee);
 			} catch (LoggedOutException e) {
 				return "ERROR: You have to log in first!";
 			}
 
 			return "Step (" + min + " " + max + "] successfully added";
 
 		} else if (tmp.equalsIgnoreCase("!removeStep")) {
 			if (!sc.hasNextDouble())
 				return invalidCommand;
 			double min = sc.nextDouble();
 			if (!sc.hasNextDouble())
 				return invalidCommand;
 			double max = sc.nextDouble();
 
 			if (max == 0)
 				max = Double.POSITIVE_INFINITY;
 
 			try {
 				mcs.removeStep(min, max);
 			} catch (LoggedOutException e) {
 				return "ERROR: You have to log in first!";
 			}
 
 			return "Price step (" + min + " " + max + "] successfully removed";
 
 		} else if (tmp.equalsIgnoreCase("!bill")) {
 			if (!sc.hasNext())
 				return invalidCommand;
 			String userName = sc.next();
 
 			Bill bill = null;
 			try {
 				bill = mcs.bill(userName);
 			} catch (LoggedOutException e) {
 				return "ERROR: You have to log in first!";
 			}
 
 			if (bill == null)
 				return "No bill for user " + userName;
 
 			final int PADDING_SIZE = 15;
 			final String format = "%-" + PADDING_SIZE;
 
 			StringBuilder builder = new StringBuilder();
 
 			builder.append(String.format(format + "s", "auction_ID"));
 			builder.append(String.format(format + "s", "strike_price"));
 			builder.append(String.format(format + "s", "fee_fixed"));
 			builder.append(String.format(format + "s", "fee_variable"));
 			builder.append(String.format(format + "s", "fee_total"));
 			builder.append("\n");
 			for (Iterator<Bill.Auction> iterator = bill.iterator(); iterator
 					.hasNext();) {
 				Bill.Auction a = iterator.next();
 
 				builder.append(String.format(format + "d", a.getAuctionId()));
 				builder.append(String.format(format + ".2f", a.getPrice()));
 				builder.append(String.format(format + ".2f",
 						a.getCalculatedFixedFee()));
 				builder.append(String.format(format + ".2f",
 						a.getCalculatedVariableFee()));
 				builder.append(String.format(
 						format + ".2f",
 						a.getCalculatedFixedFee()
 								+ a.getCalculatedVariableFee()));
 
 				if (iterator.hasNext())
 					builder.append("\n");
 			}
 			return builder.toString();
 
 		} else if (tmp.equalsIgnoreCase("!logout")) {
 			String userName = ManagementClient.userName;
 			ManagementClient.userName = null;
 			try {
 				mcs.logout();
 			} catch (LoggedOutException e) {
 				return "ERROR: You have to log in first!";
 			}
 
 			return "Successfully logged out as " + userName + "!";
 
 		} else if (tmp.equalsIgnoreCase("!subscribe")) {
 			if (!sc.hasNext())
 				return invalidCommand;
 			String regex = sc.skip("\\s*").nextLine().trim();
 			// Remove starting and trailing " or '
 			regex = regex.replaceFirst("^[\"']", "");
 			regex = regex.replaceFirst("[\"']$", "");
 
 			long id = mcs.subscribe(regex);
 
 			return "Created subscription with ID " + id
 					+ " for events using filter '" + regex + "'";
 
 		} else if (tmp.equalsIgnoreCase("!unsubscribe")) {
 			if (!sc.hasNextLong())
 				return invalidCommand;
 			long id = sc.nextLong();
 
 			mcs.unsubscribe(id);
 
 			return "Subscription " + id + " terminated";
 
 		} else if (tmp.equalsIgnoreCase("!auto")) {
 
 			mcs.auto();
 
 			return "Automatic mode on";
 
 		} else if (tmp.equalsIgnoreCase("!hide")) {
 
 			mcs.hide();
 
 			return "On-demand mode on";
 
 		} else if (tmp.equalsIgnoreCase("!print")) {
 
 			return printEvents(mcs.print());
 
		} else if (tmp.equalsIgnoreCase("!end")) {
 
 			return null;
 		}
 
 		return invalidCommand;
 	}
 
 	static String getPrompt() {
 		if (userName == null)
 			return "> ";
 		return userName + "> ";
 	}
 
 	static String printEvents(Set<Event> events) {
 		if (events == null)
 			return "";
 
 		StringBuilder builder = new StringBuilder();
 		for (Iterator<Event> iterator = events.iterator(); iterator.hasNext();) {
 			Event event = iterator.next();
 			builder.append(event.toString());
 
 			if (iterator.hasNext())
 				builder.append("\n");
 		}
 		return builder.toString();
 	}
 
 	private static void error(String msg, Throwable e) {
 		System.err.println(msg);
 		if (e != null)
 			e.printStackTrace();
 		close();
 		System.exit(1);
 	}
 
 	private static void close() {
 		if (mcs != null) {
 			try {
 				mcs.close();
 			} catch (Exception e) {
 				System.err.println("Something went wrong while closing:");
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
