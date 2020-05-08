 package comp512;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 public class TCPClient {
     public static void main(String[] args) {
         if (args.length != 2) {
             System.err.println("Usage: TCPClient hostname port");
             System.exit(1);
         }
             
         String serverHost = args[0];
         int serverPort = Integer.parseInt(args[1]);
         
         BufferedReader stdin = new BufferedReader(new InputStreamReader(
             System.in));
 
         System.out.println("Connecting to " + serverHost);
 
         while (true) {
             try {
                 System.out.print("> ");
                 System.out.flush();
                 String input = stdin.readLine();
                 ArrayList<String> msg = parse(input);
 
                 // Ignore empty commands
                 if (msg.isEmpty()) {
                     continue;
                 }
                 
                 // Help and quit commands are not passed to the server.
                 else if (msg.get(0).equalsIgnoreCase("help")) {
                     help(msg);
                 }
                 else if (msg.get(0).equalsIgnoreCase("quit")) {
                     System.exit(0);
                 }
                 
                 // For all other commands, a connection to the middleware
                 // is established, the complete command + arguments sent over
                 // the wire and we wait for a result object.
                 else {
                     Socket socket = new Socket(serverHost, serverPort);
                     
                     try {
                         Comm.sendObject(socket, msg);
                         Result b = (Result)Comm.recvObject(socket);
                         if (b.boolResult != null) {
                             System.out.println(b.boolResult ? "OK" : "Failure");
                         }
                         else if (b.intResult != null) {
                             System.out.println(b.intResult);
                         }
                         else if (b.reservationResult != null) {
                             System.out.println("Reservation successful");
                         }
                         else if (b.stringResult != null) {
                             System.out.println(b.stringResult);
                         }
                         else if (b.reservationResult == null) {
                             System.out.println("Reservation failed");
                         }
                     }
                     catch (Exception e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
                     socket.close();
                 }
             }
             catch (NullPointerException e) {
                 // Ctrl+D
                 System.exit(0);
             }
             catch (IOException e) {
                 System.err.println(e.getMessage());
                 System.exit(1);
             }
         }
 
     }
     
   
 
     public static ArrayList<String> parse(String command) {
         ArrayList<String> arguments = new ArrayList<String>();
         StringTokenizer tokenizer = new StringTokenizer(command, ",");
         String argument = "";
         while (tokenizer.hasMoreTokens()) {
             argument = tokenizer.nextToken();
             argument = argument.trim();
             arguments.add(argument);
         }
         return arguments;
     }
 
 
 
     public static void help(ArrayList<String> msg) {
         if (msg.size() == 1) {
             System.out.println("\nWelcome to the client interface provided to test your project.");
             System.out.println("Commands accepted by the interface are:");
             System.out.println("help");
             System.out.println("newflight\nnewcar\nnewroom\nnewcustomer\nnewcusomterid\ndeleteflight\ndeletecar\ndeleteroom");
             System.out.println("deletecustomer\nqueryflight\nquerycar\nqueryroom\nquerycustomer");
             System.out.println("queryflightprice\nquerycarprice\nqueryroomprice");
             System.out.println("reserveflight\nreservecar\nreserveroom\nitinerary");
             System.out.println("nquit");
             System.out.println("\ntype help, <commandname> for detailed info(NOTE the use of comma).");
         }
         else {
             System.out.print("Help on: ");
             switch(indexOfCommand(msg.get(1))) {
             case 1:
                 System.out.println("Help");
                 System.out.println("\nTyping help on the prompt gives a list of all the commands available.");
                 System.out.println("Typing help, <commandname> gives details on how to use the particular command.");
                 break;
 
             case 2:  //new flight
                 System.out.println("Adding a new Flight.");
                 System.out.println("Purpose:");
                 System.out.println("\tAdd information about a new flight.");
                 System.out.println("\nUsage:");
                 System.out.println("\tnewflight,<id>,<flightnumber>,<flightSeats>,<flightprice>");
                 break;
 
             case 3:  //new Car
                 System.out.println("Adding a new Car.");
                 System.out.println("Purpose:");
                 System.out.println("\tAdd information about a new car location.");
                 System.out.println("\nUsage:");
                 System.out.println("\tnewcar,<id>,<location>,<numberofcars>,<pricepercar>");
                 break;
 
             case 4:  //new Room
                 System.out.println("Adding a new Room.");
                 System.out.println("Purpose:");
                 System.out.println("\tAdd information about a new room location.");
                 System.out.println("\nUsage:");
                 System.out.println("\tnewroom,<id>,<location>,<numberofrooms>,<priceperroom>");
                 break;
 
             case 5:  //new Customer
                 System.out.println("Adding a new Customer.");
                 System.out.println("Purpose:");
                 System.out.println("\tGet the system to provide a new customer id. (same as adding a new customer)");
                 System.out.println("\nUsage:");
                 System.out.println("\tnewcustomer,<id>");
                 break;
 
 
             case 6: //delete Flight
                 System.out.println("Deleting a flight");
                 System.out.println("Purpose:");
                 System.out.println("\tDelete a flight's information.");
                 System.out.println("\nUsage:");
                 System.out.println("\tdeleteflight,<id>,<flightnumber>");
                 break;
 
             case 7: //delete Car
                 System.out.println("Deleting a Car");
                 System.out.println("Purpose:");
                 System.out.println("\tDelete all cars from a location.");
                 System.out.println("\nUsage:");
                 System.out.println("\tdeletecar,<id>,<location>,<numCars>");
                 break;
 
             case 8: //delete Room
                 System.out.println("Deleting a Room");
                 System.out.println("\nPurpose:");
                 System.out.println("\tDelete all rooms from a location.");
                 System.out.println("Usage:");
                 System.out.println("\tdeleteroom,<id>,<location>,<numRooms>");
                 break;
 
             case 9: //delete Customer
                 System.out.println("Deleting a Customer");
                 System.out.println("Purpose:");
                 System.out.println("\tRemove a customer from the database.");
                 System.out.println("\nUsage:");
                 System.out.println("\tdeletecustomer,<id>,<customerid>");
                 break;
 
             case 10: //querying a flight
                 System.out.println("Querying flight.");
                 System.out.println("Purpose:");
                 System.out.println("\tObtain Seat information about a certain flight.");
                 System.out.println("\nUsage:");
                 System.out.println("\tqueryflight,<id>,<flightnumber>");
                 break;
 
             case 11: //querying a Car Location
                 System.out.println("Querying a Car location.");
                 System.out.println("Purpose:");
                 System.out.println("\tObtain number of cars at a certain car location.");
                 System.out.println("\nUsage:");
                 System.out.println("\tquerycar,<id>,<location>");
                 break;
 
             case 12: //querying a Room location
                 System.out.println("Querying a Room Location.");
                 System.out.println("Purpose:");
                 System.out.println("\tObtain number of rooms at a certain room location.");
                 System.out.println("\nUsage:");
                 System.out.println("\tqueryroom,<id>,<location>");
                 break;
 
             case 13: //querying Customer Information
                 System.out.println("Querying Customer Information.");
                 System.out.println("Purpose:");
                 System.out.println("\tObtain information about a customer.");
                 System.out.println("\nUsage:");
                 System.out.println("\tquerycustomer,<id>,<customerid>");
                 break;
 
             case 14: //querying a flight for price
                 System.out.println("Querying flight.");
                 System.out.println("Purpose:");
                 System.out.println("\tObtain price information about a certain flight.");
                 System.out.println("\nUsage:");
                 System.out.println("\tqueryflightprice,<id>,<flightnumber>");
                 break;
 
             case 15: //querying a Car Location for price
                 System.out.println("Querying a Car location.");
                 System.out.println("Purpose:");
                 System.out.println("\tObtain price information about a certain car location.");
                 System.out.println("\nUsage:");
                 System.out.println("\tquerycarprice,<id>,<location>");
                 break;
 
             case 16: //querying a Room location for price
                 System.out.println("Querying a Room Location.");
                 System.out.println("Purpose:");
                 System.out.println("\tObtain price information about a certain room location.");
                 System.out.println("\nUsage:");
                 System.out.println("\tqueryroomprice,<id>,<location>");
                 break;
 
             case 17:  //reserve a flight
                 System.out.println("Reserving a flight.");
                 System.out.println("Purpose:");
                 System.out.println("\tReserve a flight for a customer.");
                 System.out.println("\nUsage:");
                 System.out.println("\treserveflight,<id>,<customerid>,<flightnumber>");
                 break;
 
             case 18:  //reserve a car
                 System.out.println("Reserving a Car.");
                 System.out.println("Purpose:");
                 System.out.println("\tReserve a given number of cars for a customer at a particular location.");
                 System.out.println("\nUsage:");
                 System.out.println("\treservecar,<id>,<customerid>,<location>,<nummberofCars>");
                 break;
 
             case 19:  //reserve a room
                 System.out.println("Reserving a Room.");
                 System.out.println("Purpose:");
                 System.out.println("\tReserve a given number of rooms for a customer at a particular location.");
                 System.out.println("\nUsage:");
                 System.out.println("\treserveroom,<id>,<customerid>,<location>,<nummberofRooms>");
                 break;
 
             case 20:  //reserve an Itinerary
                 System.out.println("Reserving an Itinerary.");
                 System.out.println("Purpose:");
                 System.out.println("\tBook one or more flights.Also book zero or more cars/rooms at a location.");
                 System.out.println("\nUsage:");
                 System.out.println("\titinerary,<id>,<customerid>,<flightnumber1>....<flightnumberN>,<LocationToBookCarsOrRooms>,<NumberOfCars>,<NumberOfRoom>");
                 break;
 
 
             case 21:  //quit the client
                 System.out.println("Quitting client.");
                 System.out.println("Purpose:");
                 System.out.println("\tExit the client application.");
                 System.out.println("\nUsage:");
                 System.out.println("\tquit");
                 break;
 
             case 22:  //new customer with id
                 System.out.println("Create new customer providing an id");
                 System.out.println("Purpose:");
                 System.out.println("\tCreates a new customer with the id provided");
                 System.out.println("\nUsage:");
                 System.out.println("\tnewcustomerid, <id>, <customerid>");
                 break;
 
             default:
                 System.out.println("The interface does not support this command.");
                 break;
             }
         }
     }
 
     public static int indexOfCommand(String command) {
         ArrayList<String> commands = new ArrayList<String>() {{
             add("help");
             add("newflight");
             add("newcar");
             add("newroom");
             add("newcustomer");
             add("deleteflight");
             add("deletecar");
             add("deleteroom");
             add("deletecustomer");
             add("queryflight");
             add("querycar");
             add("queryroom");
             add("querycustomer");
             add("queryflightprice");
             add("querycarprice");
             add("queryroomprice");
             add("reserveflight");
             add("reservecar");
             add("reserveroom");
             add("itinerary");
             add("quit");
             add("newcustomerid");
         }};
         return commands.indexOf(command)+1;
     }
 
 }
