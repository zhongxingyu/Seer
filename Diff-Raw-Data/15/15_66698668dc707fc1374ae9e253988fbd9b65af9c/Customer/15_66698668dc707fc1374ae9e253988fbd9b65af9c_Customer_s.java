 
 package conleyairlines;
 import java.sql.*;
 import java.util.*;
 /**
  *
  * @author Casey Conley
  * Email: cdc214@lehigh.edu
  * Course: CSE 241
  * Title: Airline Semester Project
  * Purpose: The user interface for the customer of a fictional 
  *          airline company. Uses JDBC with a database housing the information 
  *          for the airline.
  * 
  */
 public class Customer {
     Connection con;
     Scanner in;
     
     public Customer(Connection con, Scanner in){
         this.con = con;
         this.in = in;
     }
     
     public void openInterface(){
         int customerID = logIn();
         if (customerID < 0){
             //DON'T HANDLE IT LIKE THIS, FIX LATER
             System.out.println("Exiting Customer Interface.");
             return;
         }
         System.out.println("What action would you like to perform?");
         printOptions();
         boolean done = false;
         while(!done){
             int userChoice;
             try{
                 userChoice = in.nextInt();
                 switch(userChoice){
                     case 1:
                         System.out.println("Manage credit card info option chosen\n");
                         manageCreditCardInfo(customerID);
                         done = false;
                         break;
                     case 2:
                         System.out.println("Make a reservation option chosen\n");
                         makeAReservation(customerID);
                         done = false;
                         break;
                     case 3:
                         System.out.println("View reserved flights option chosen\n");
                         viewReservedFlights(customerID);
                         done = false;
                         break;
                     case 4:
                         System.out.println("Cancel reserved flights option chosen\n");
                         cancelReservedFlight(customerID);
                         done = false;
                         break;
                     case 5:
                         System.out.println("Exiting Customer Interface.\n");
                         done = true;
                         break;
                     default:
                         System.out.println("Error: Please enter a valid number choice.");
                         in.nextLine();
                         done = false;
                         break;
                 }
             } catch (InputMismatchException ex) {
                 System.out.println("Error: Please enter a valid number choice.");
                 
                 done = false;
             }
             catch (IllegalStateException ex) {
                 in = new Scanner(System.in);
                 System.out.println("Error: Internal Error, scanner closed, "
                         + "reinitialized. \nPlease try again.");
                 
                 done = false;
             }
             if(!done){
                 System.out.println("What action would you like to perform?");
                 printOptions();
             }
         }
         //done == true
         //break out of customer interface
     }
     
     private int logIn(){
         System.out.println("Welcome to Conley Airlines! Are you a returning customer?");
         
         boolean done = false;
         int customerID = -1;
         String fname, lname;
         while(!done){
             System.out.println("1: Yes, I have a customer ID.");
             System.out.println("2: No, I would like to create an account."); 
             System.out.println("3: I would like to close the Customer Interface.");
             System.out.print("-->");
             try{
                 int userChoice = in.nextInt();
                 
                 
                 switch(userChoice){
                     case 1:
                         System.out.println("Please enter your customer ID below.");
                         System.out.print("-->");
                         in.nextLine();
                         customerID = in.nextInt();
                         in.nextLine();
                         //validate customerID
                         try{
                             Statement idValidation = con.createStatement();
                             ResultSet validationResult;
                             validationResult = idValidation.executeQuery("select * from customer where id = " + customerID);
                             if (!validationResult.next()){
                                 //id does not exist in database
                                 System.out.println("That id does not exist in our database. "
                                         + "Please try again or create a new account.");
                                 System.out.println("Are you a returning customer?");
                                 done = false;
                             }
                             else {
                                 fname = validationResult.getString(2);
                                 lname = validationResult.getString(3);
                                 done = true;
                                 System.out.println("Welcome back, " + fname + " " + lname+ "!");
                             }
                         } catch (SQLException e){
                             System.out.println("Error: Database error, please try again.");
                         }
                         break;
                     case 2:
                         System.out.println("Please enter your first name.");
                         System.out.print("-->");
                         in.nextLine();
                         fname = in.next();
                         System.out.println("Please enter your last name.");
                         System.out.print("-->");
                         lname = in.next();
                         in.nextLine();
                         boolean valid = false;
                         while (!valid){
                             Random rnd = new Random();
                             customerID = 100000 + rnd.nextInt(900000);
                             try{
                                 Statement stmt;
                                 stmt = con.createStatement();
                                 int result = stmt.executeUpdate("insert into customer values (" 
                                         + customerID + ", '" + fname + "', '" + lname + "')");
                                 System.out.println(result + " customer added"); //comment this out in final submission
                                 stmt.close();
                                 valid = true;
                             } catch (SQLException e){
                                 valid = false; //random number generator failed to create a unique customer ID number
                             }
                         }
                         System.out.println("Congratulations! Your account has been created. "
                                 + "Your customer ID is " + customerID);
                         System.out.println("Use your customer ID to log into this interface "
                                 + "in subsequent visits.");
                         done = true;
                         break;
                     case 3:
                         //get me out of here
                         done = true;
                         break;
                         
                     default:
                         System.out.println("Error: Please enter a valid number choice.");
                         done = false;
                         break;
                 }
             } catch (InputMismatchException ex) {
                     System.out.println("Error: Please enter a valid number choice.");
                     done = false;
             } catch (IllegalStateException ex) {
                     in = new Scanner(System.in);
                     System.out.println("Error: Internal Error, scanner closed, "
                             + "reinitialized. \nPlease try again.");
                     System.out.print("-->");
                     done = false;
             }
         }
         return customerID;
     }
     
     private void manageCreditCardInfo(int customerID){
         
         boolean done = false;
         while(!done){
             System.out.println("What would you like to do?");
             System.out.println("1: Add a credit card");
             System.out.println("2: View my credit card info");
             System.out.println("3: Delete a credit card");
             System.out.println("4: Go back");
             System.out.print("-->");
             int userChoice;
             try{
                 userChoice = in.nextInt();
                 switch(userChoice){
                     case 1:
                         System.out.println("Calling createCreditCard()...\n");
                         addCreditCard(customerID);
                         done = false;
                         break;
                     case 2:
                         System.out.println("Calling viewCreditCards()...\n");
                         viewCreditCards(customerID);
                         done = false;
                         break;
                     case 3:
                         System.out.println("Calling deleteCreditCard()...\n");
                         deleteCreditCard(customerID);
                         done = false;
                         break;
                     case 4:
                         System.out.println("Exiting credit card management.\n");
                         done = true;
                         break;
                     default:
                         System.out.println("Error: Please enter a valid number choice.");
                         in.nextLine();
                         done = false;
                         break;
                 }
             } catch (InputMismatchException ex) {
                 System.out.println("Error: Please enter a valid number choice.");
                 
                 done = false;
             }
             catch (IllegalStateException ex) {
                 in = new Scanner(System.in);
                 System.out.println("Error: Internal Error, scanner closed, "
                         + "reinitialized. \nPlease try again.");
                 
                 done = false;
             }
             if(!done){
                 System.out.println("What action would you like to perform?");
             }
         }
     }
     
     private void addCreditCard(int customerID){
         try{
             System.out.print("Please enter your 16-digit credit card number.\n-->");
             long card_num = in.nextLong();
             in.nextLine();
             System.out.print("Please enter your 3-digit security number.\n-->");
             int sec_code = in.nextInt();
             in.nextLine();
             System.out.print("Please enter the expiration date in the format MMYY "
                     + "(example if it is June 2013, enter 0613.\n-->");
             int exp_date = in.nextInt();
             in.nextLine();
             System.out.print("Please enter your street address (example: 1600 "
                     + "Pennsylvania Avenue).\n-->");
             String address = in.nextLine();
             System.out.print("Please enter your city.\n-->");
             String city = in.nextLine();
             System.out.print("Please enter your 2-letter state (example: "
                     + "CA, TX, NC, etc.).\n-->");
             String state = in.nextLine();
             System.out.print("Please enter your 5-digit zip code.\n-->");
             int zip = in.nextInt();
             in.nextLine();
             String insertCreditCard = "insert into credit_card values (" + card_num + ", " 
                     + sec_code + ", " + exp_date + ", '"+ address + "', '" + city + "', '" + state + "', " + zip + ")";
             Statement stmtCreditCard;
             stmtCreditCard = con.createStatement();
             int resultCreditCard;
             resultCreditCard = stmtCreditCard.executeUpdate(insertCreditCard);
             if (resultCreditCard == 1){
                 //insert into credit_card successful, now insert into billing_info
                 String insertBillingInfo = "insert into billing_info values (" 
                         + card_num + ", " + customerID + ")";
                 Statement stmtBillingInfo;
                 stmtBillingInfo = con.createStatement();
                 int resultBillingInfo;
                 resultBillingInfo = stmtBillingInfo.executeUpdate(insertBillingInfo);
                 if(resultBillingInfo == 1){
                     System.out.println("Credit card successfully added to your account.");
                 }
                 else {
                     System.out.println("A credit card with that card number already exists.");
                 }
                 stmtBillingInfo.close();
             }
             else {
                 System.out.println("A credit card with that card number already exists.");
             }
             stmtCreditCard.close();
             
         } catch (InputMismatchException ex) {
                 System.out.println("Error: Invalid input, please try again.");
         } catch (IllegalStateException ex) {
             in = new Scanner(System.in);
             System.out.println("Error: Internal Error, scanner closed, "
                     + "reinitialized. \nPlease try again.");
         } catch (SQLException ex){
             System.out.println("Please follow the correct format the fields and try again.");
         }
     }
     
     private boolean hasCreditCards(int customerID){
         String q = "select card_num from billing_info where customer_id = " 
                 + customerID + "";
         try {
             Statement stmt = con.createStatement();
             ResultSet result = stmt.executeQuery(q);
             if (result.next()){
                 stmt.close();
                 return true;
             }
             else{
                 stmt.close();
                 return false;
             }
             
         } catch (SQLException ex){
             System.out.println("Database Error, delete aborted.");
             return false;
         }
     }
     
     private void deleteCreditCard(int customerID){
         System.out.println("Listing all of your credit cards on file...");
         if (!hasCreditCards(customerID)){
             System.out.println("You have no credit cards on file. Please add one"
                     + " before trying to delete one.");
         }
         else{
             ArrayList<Long> cards = viewCreditCards(customerID);
             System.out.println("Please enter the number of the credit card "
                     + "you would like to remove, or enter 0 to go back.");
             boolean valid = false;
             int userChoice;
             long card_num;
             card_num = -1;
             while(!valid){
                 System.out.print("-->");
                 userChoice = in.nextInt();
                 in.nextLine();
                 if(userChoice == 0){
                     return;
                 }
                 
                 try {
                     card_num = (cards.get(userChoice-1)).longValue();
                     con.setAutoCommit(false);
                     Statement stmtBilling;
                     stmtBilling = con.createStatement();
                     
                     String deleteBilling = "delete from billing_info where card_num = " 
                                 + card_num + " and customer_id = " + customerID;
                     int resultBilling = stmtBilling.executeUpdate(deleteBilling);
                     if (resultBilling == 1){
                         //delete from credit_card successful, now delete from billing_info
                         String deleteCredit = "delete from credit_card where card_num = " + card_num + "";
                         Statement stmtCredit;
                         stmtCredit = con.createStatement();
                         int resultCredit;
                         resultCredit = stmtCredit.executeUpdate(deleteCredit);
                         if(resultCredit == 1){
                             System.out.println("Credit card successfully deleted from your account.");
                             valid = true;
                             con.commit();
                             con.setAutoCommit(true);
                         }
                         else {
                             System.out.println("Please enter a valid number choice.");
                             valid = false;
                             con.rollback();
                             con.setAutoCommit(true);
                         }
                         stmtCredit.close();
                     }
                     else {
                         System.out.println("Please enter a valid number choice.");
                         con.rollback();
                         con.setAutoCommit(true);
                         valid = false;
                     }
                     stmtBilling.close();
                 } catch (SQLException ex){
                     System.out.println("Error: Database error. Please try again later.");
                     try {
                         con.rollback();
                     } catch (SQLException ex1) { //please don't ever get to here
                         System.out.println("Database rollback failed. Check internet connection. Exiting program.");
                         System.exit(1);
                     }
                     valid = true;
                 } catch (IndexOutOfBoundsException ex){
                     System.out.println("Please enter a valid number choice.");
                     try {
                         con.rollback();
                     } catch (SQLException ex1) { //please don't ever get to here
                         System.out.println("Database rollback failed. Check internet connection. Exiting program.");
                         System.exit(1);
                     }
                     valid = false;
                 }
             }
             if (card_num < 0){
                  System.out.println("Error: Credit card not chosen");
             }
         }
     }
     
     private ArrayList<Long> viewCreditCards(int customerID){
         ArrayList<Long> cards = new ArrayList<>();
         try{
             Statement billing_list;
             billing_list = con.createStatement();
             ResultSet result;
             result = billing_list.executeQuery("select card_num, security_code, "
                     + "expiration_date, street_address, city, state, zip from "
                     + "billing_info natural join credit_card where "
                     + "customer_id = " + customerID);
             if (!result.next()){
                 System.out.println("You have no credit cards on file.");
                 
             }
             else {
                 
                 int i = 1;
                 do{
                     int security_code, zip;
                     long card_num;
                     String exp_date, street_add, city, state;
                     card_num = result.getLong(1);
                     cards.add(card_num);
                     security_code = result.getInt(2);
                     exp_date = result.getString(3);
                     street_add = result.getString(4);
                     city = result.getString(5);
                     state = result.getString(6);
                     zip = result.getInt(7);
                     System.out.println("Card " + i + ":");
                     System.out.println("Card Number: " + card_num);
                     System.out.println("Security Code: " + security_code);
                     System.out.println("Expiration Date: " + exp_date);
                     System.out.println("Street: " + street_add);
                     System.out.println("City: " + city);
                     System.out.println("State: " + state);
                     System.out.println("Zip Code: " + zip);
                     System.out.println("");
                     i++;
                 } while (result.next());
             }
             billing_list.close();
         } catch (SQLException e){
                 System.out.println("Error: Database error, please try again.");
         }
         return cards;
     }
     
     private boolean hasReservations(int customerID){
         String q = "select reservation_id from customer_reserved where "
                 + "customer_id = " + customerID;
         try {
             Statement stmt = con.createStatement();
             ResultSet result = stmt.executeQuery(q);
             if (result.next()){
                 stmt.close();
                 return true;
             }
             else{
                 stmt.close();
                 return false;
             }
             
         } catch (SQLException ex){
             System.out.println("Database Error, delete aborted.");
             return false;
         }
     }
     
     private void makeAReservation(int customerID){
         if(!hasCreditCards(customerID)){
             System.out.println("You don't have any credit cards on file, "
                     + "please add one before making a reservation.");
             return;
         }
         try {
             con.setAutoCommit(false);
         } catch (SQLException ex) {
             System.out.println("Database error disabling auto-commit. "
                     + "Going back to main menu.");
             return;
         }
         boolean tripsFound = false;
         ArrayList<AbstractMap.SimpleEntry<Integer,String>> trips = new ArrayList();
         //find trips
         while (!tripsFound){
             System.out.println("Please tell us where you're flying to.");
             String destination = pickAirportFromOptions();
             if("".equals(destination)){
                 System.out.println("Airport not chosen. Please try again.");
                 try {
                     con.setAutoCommit(true);
                 } catch (SQLException ex) {
                     System.out.println("Database error enabling auto-commit. "
                             + "Check internet connection.");
                     System.exit(1);
                 }
                 return;
             }
 
             System.out.println("Please tell us where you're flying from.");
             String source = pickAirportFromOptions();
             if("".equals(source)){
                 System.out.println("Airport not chosen. Please try again.");
                 try {
                     con.setAutoCommit(true);
                 } catch (SQLException ex) {
                     System.out.println("Database error enabling auto-commit. "
                             + "Check internet connection.");
                     System.exit(1);
                 }
                 return;
             }
             
             trips = findTrips(source, destination);
             if (trips.isEmpty()){
                 System.out.println("Trips not found.");
                 try{
                     System.out.println("Would you like to search for a different"
                             + " trip or quit?");
                     System.out.println("1: Yes");
                     System.out.println("2: No");
                     boolean valid = false;
                     while(!valid){
                         System.out.print("-->");
                         int userChoice = in.nextInt();
                         in.nextLine();
                         switch(userChoice){
                             case 1:
                                 valid = true;
                                 break;
                             case 2:
                                 try {
                                     con.setAutoCommit(true);
                                 } catch (SQLException ex) {
                                     System.out.println("Database error enabling auto-commit. "
                                             + "Check internet connection.");
                                     System.exit(1);
                                 }
                                 return;
                             default:
                                 System.out.println("Please enter a either 1 (yes) or 2 (no).");
                                 valid = false;
                                 break;
                         }
                     }
                 } catch (InputMismatchException e){
                     System.out.println("Please enter a either 1 (yes) or 2 (no).");
                 }
                 tripsFound = false;
             }
             else {
                 System.out.println("Trips found!");
                 tripsFound = true;
             }
         }
         in.nextLine();
         
         //found trips, listing them
         try{
             Statement listTrips = con.createStatement();
             ResultSet listTripsResult;
             int trip_num;
             String trip_date;
             for(int i = 1; i<=trips.size(); i++){
                 trip_num = ((trips.get(i-1)).getKey()).intValue();
                 trip_date = ((trips.get(i-1)).getValue());
                listTripsResult = listTrips.executeQuery("select price from "
                    + "trip where trip_number = " + trip_date 
                        + "and trip_date = '" + trip_num);
                 System.out.println("Trip Option " + i);
                 System.out.println("Trip Number:" + trip_num);
                 System.out.println("Trip Date:" + trip_date);
                 System.out.println("Price:" + listTripsResult.getInt(1));
             }
         } catch (SQLException e){
             System.out.println("Error: Database error while listing trips "
                     + "found. Going back to main menu.");
             try {
                 con.setAutoCommit(true);
             } catch (SQLException ex) {
                 System.out.println("Database error enabling auto-commit. "
                         + "Check internet connection.");
                 System.exit(1);
             }
             return;
         }
         
         //choose a trip
         boolean valid = false;
         int tripToReserve = -1;
         String tripDate = "";
         while (!valid){
             System.out.println("Please pick a trip you would like to reserve.");
             System.out.print("-->");
             int userChoice;
             userChoice = in.nextInt();
             
             try{
                 tripToReserve = ((trips.get(userChoice -1)).getKey()).intValue();
                 tripDate = (trips.get(userChoice -1)).getValue();
                 System.out.println("Trip " + tripToReserve + " on " + tripDate +" selected");
                 valid = true;
             } catch (IndexOutOfBoundsException e){
                 System.out.println("Please choose a valid number option.");
                 System.out.print("-->");
                 valid = false;
             }
             if (tripToReserve < 0 || "".equals(tripDate)){
                 System.out.println("Trip not picked/reserved. Going back to main menu.");
                 try {
                     con.setAutoCommit(true);
                 } catch (SQLException ex) {
                     System.out.println("Database error enabling auto-commit. "
                             + "Check internet connection.");
                     System.exit(1);
                 }
                 return;
             }
         }
         //choose seat class
         valid = false;
         System.out.println("Please choose what class you would like.");
         String seatClass = "";
         while (!valid){
             
             System.out.println("1: Economy");
             System.out.println("2: Business");
             System.out.println("3: First-Class");
             System.out.print("-->");
             
             int userChoice;
             try{
                 userChoice = in.nextInt();
                 in.nextLine();
                 
                 switch (userChoice){
                     case 1:
                         seatClass = "economy";
                         valid = true;
                         break;
                     case 2:
                         seatClass = "business";
                         valid = true;
                         break;
                     case 3:
                         seatClass = "first";
                         valid = true;
                         break;
                     default:
                         valid = false;
                         System.out.println("Please enter a valid number choice.");
                         break;
                 }
             } catch (InputMismatchException e){
                 valid = false;
                 System.out.println("Please enter a valid number choice.");
             }
         }
         
         //get the legs as a resultSet
         //"select leg_id from leg_of_trip where trip_num = " + tripToReserve + ""
         //create random number for reservationID
         //add the customer to each leg's first seat available in their class. 
         /*select leg_id , seat_number
         from leg L natural join plane_seats P
         where seat_class = 'seatClass'
         and leg_id = legsResult.getInt(1)
         and seat_number not in
             (select seat_number 
             from reserved_seat
             where leg_id = L.leg_id)
         order by seat_number asc;
         */
         int reservationID = -1;
         String legsQuery = "select leg_id from leg_of_trip where trip_num = " 
                 + tripToReserve + "";
         int tripPrice = 0;
         try {
             
             Statement legsStatement = con.createStatement();
             ResultSet legsResult = legsStatement.executeQuery(legsQuery);
             Statement legSeatStatement = con.createStatement();
             while (legsResult.next()){
                 String legSeatQuery = "select leg_id , seat_number, price from leg L "
                         + "natural join plane_seats P where seat_class = '" 
                         + seatClass +"' and leg_id = " + legsResult.getInt(1) + " and seat_number "
                         + "not in (select seat_number from reserved_seat where "
                         + "leg_id = L.leg_id) order by seat_number asc";
                 ResultSet legSeatResult = legSeatStatement.executeQuery(legSeatQuery);
                 if (!legSeatResult.next()){
                     System.out.println("We're sorry, one of the legs on that "
                             + "flight is full. Please try another flight or "
                             + "another seat class for the same flight.");
                     try {
                         con.setAutoCommit(true);
                     } catch (SQLException ex) {
                         System.out.println("Database error enabling auto-commit. "
                                 + "Check internet connection.");
                         System.exit(1);
                     }
                     return;
                 }
                 else {
                     //add customer and seat number to reserved_seat
                     int legId = legSeatResult.getInt(1);
                     int seat_number = legSeatResult.getInt(2);
                     int legPrice = legSeatResult.getInt(3);
                     while (!valid){
                         Random randomReservationID = new Random();
                         reservationID = 100000 + randomReservationID.nextInt(900000);
                         try{
                             Statement insertSeatStmt;
                             insertSeatStmt = con.createStatement();
                             int result = insertSeatStmt.executeUpdate("insert into "
                                     + "reserved_seat values (" + 
                                     reservationID + ", " + tripToReserve + 
                                     ", " + seatClass + ", " + customerID + 
                                     ", " + legId + ", " + seat_number + ")");
                             System.out.println(result + " customer added"); //comment this out in final submission
                             insertSeatStmt.close();
                             valid = true;
                             tripPrice += legPrice;
                         } catch (SQLException e){
                             valid = false; //random number generator failed to create a unique customer ID number
                         }
                     }
                 }
                 
             }
             
         } catch (SQLException e){
             System.out.println("Error: Database Error. Going back to main menu.");
             try {
                 con.setAutoCommit(true);
             } catch (SQLException ex) {
                 System.out.println("Database error ensabling auto-commit. "
                         + "Check internet connection.");
                 System.exit(1);
             }
             return;
         }
         
         //pick a credit card
         
         
         //viewcreditcards()
         ArrayList<Long> creditCards = viewCreditCards(customerID);
         //handle user choice
         System.out.println("Please enter the number of the credit card you would"
                 + " like to use to pay for your trip.");
         boolean validCard = false;
         int userChoice;
         long cardNum = -1;
         while(!validCard){
             System.out.print("-->");
             userChoice = in.nextInt();
             in.nextLine();
             try {
                 cardNum = (creditCards.get(userChoice-1)).longValue();
                 validCard = true;
                 
             } catch (IndexOutOfBoundsException ex){
                 System.out.println("Please enter a valid number choice.");
                 validCard = false;
             }
         }
         if(cardNum < 0){
             System.out.println("Error with selecting credit card. Going back to"
                     + " main menu");
             try {
                 con.setAutoCommit(true);
             } catch (SQLException ex) {
                 System.out.println("Database error enabling auto-commit. "
                         + "Check internet connection.");
                 System.exit(1);
             }
             return;
         }
         //are you sure?
         System.out.println("Are you sure you would like to place this reservation?");
         System.out.println("Reservation ID: " + reservationID);
         System.out.println("Flight Number: " + tripToReserve);
         System.out.println("Date of Flight: " + tripDate);
         System.out.println("Seat Class: " + seatClass);
         System.out.println("Price: $" + tripPrice);
         System.out.println("Purchased with Credit Card: " + cardNum);
         System.out.println("");
         System.out.println("1: Yes, make this reservation");
         System.out.println("2: No, cancel reservation");
         boolean validFinal = false;
         int finalChoice;
         while(!validFinal){
             System.out.print("-->");
             finalChoice = in.nextInt();
             in.nextLine();
             switch (finalChoice){
                 case 1: //yes
                     boolean done = false;
                     try {
                         Statement stmtReservation;
                         stmtReservation = con.createStatement();
                         int resultReservation = stmtReservation.executeUpdate("insert "
                                 + "into reservation values ("+reservationID+", "
                                 +tripToReserve+", '"+tripDate+"', '"+seatClass+"', "
                                 +tripPrice+")");
                         if(resultReservation > 0){
                             Statement stmtCustReserved;
                             stmtCustReserved = con.createStatement();
                             int resultCustReserved = stmtCustReserved.executeUpdate("insert "
                                     + "into customer_reserved values ("+customerID
                                     +", "+reservationID+", "+tripToReserve+", '"
                                     +seatClass+"')");
                             if (resultCustReserved > 0){
                                 Statement stmtCredReserved;
                                 stmtCredReserved = con.createStatement();
                                 int resultCredReserved = stmtCredReserved.executeUpdate("insert "
                                         + "into credit_card_reserved values ("+cardNum
                                         +", "+reservationID+", "+tripToReserve+", '"
                                         +seatClass+"')");
                                 if (resultCredReserved > 0){
                                     done = true;
                                 }
                                 else {
                                     done = false;
                                 }
                                 stmtCredReserved.close();
                             }
                             else {
                                 done = false;
                             }
                             stmtCustReserved.close();
                         }
                         else {
                             done = false;
                         }
                         stmtReservation.close();
                         if (done = true){
                             con.commit();
                         }
                         else {
                             con.rollback();
                         }
                         con.setAutoCommit(true);
                     } catch (SQLException ex){
                         System.out.println("Error: Database error. Please try again later.");
                         try {
                             con.rollback();
                             con.setAutoCommit(true);
                         } catch (SQLException ex1) { //please don't ever get to here
                             System.out.println("Database rollback failed. Check internet connection. Exiting program.");
                             System.exit(1);
                         }
                     }
                     validFinal = true;
                     break;
                 case 2: //no
                     try {
                         con.rollback();
                         con.setAutoCommit(true);
                     } catch (SQLException ex) {
                         System.out.println("Database error enabling auto-commit. "
                                 + "Check internet connection.");
                         System.exit(1);
                     }
                     validFinal = true;
                     break;
                 default: //invalid choice
                     validFinal = false;
                     break;
             }
             
         }
     }
     
     private ArrayList<AbstractMap.SimpleEntry<Integer,String>> findTrips(String destination, String source){
         ArrayList<AbstractMap.SimpleEntry<Integer,String>> trips = new ArrayList();
        String q = "select trip_number, trip_date, price "
                 + "from trip where start_airport = '" + source 
                 + "' and end_airport = '" + destination + "' "
                 + "order by trip_date asc";
         try{
             Statement stmt;
             stmt = con.createStatement();
             ResultSet result = stmt.executeQuery(q);
             while (result.next()){
                 AbstractMap.SimpleEntry<Integer, String> entry 
                         = new AbstractMap.SimpleEntry(result.getInt(1), 
                         result.getString(2));
                 trips.add(entry);
                 
             }
             stmt.close();
         } catch(SQLException e){
             System.out.println("Error: Database error fetching trips");
         }
             
         return trips;
     }
     
     private String pickAirportFromOptions(){
         ArrayList<String> airports = new ArrayList();
         String choice = "";
         try{
             Statement stmt;
             stmt = con.createStatement();
             String q = "select callsign, city from airport";
             ResultSet result = stmt.executeQuery(q);
             System.out.printf("%-3s\t%-20s\t%-3s\n", "#", "City", "Callsign");
             System.out.printf("%-3s\t%-20s\t%-3s\n", "-", "----", "--------");
             int i=1;
             do {
                 String callsign = result.getString(1);
                 airports.add(callsign);
                 String city = result.getString(2);
                 System.out.printf("%-3d\t%-20s\t%-3s\n", i, city, callsign);
                 i++;
             } while (result.next());
             System.out.println("Please enter the number of the airport.");
             
             boolean valid = false;
             while (!valid){
                 System.out.print("-->");
                 in.nextLine();
                 int userChoice = in.nextInt();
                 try{
                     choice = airports.get(userChoice - 1);
                     valid = true;
                 } catch (IndexOutOfBoundsException | InputMismatchException e){
                     System.out.println("Please select a valid number choice.");
                     valid = false;
                 }
             }
             System.out.println(choice + " chosen.");
             stmt.close();
         } catch (SQLException e){
             System.out.println("Error: Database error when fetching airports.");
         } 
         return choice;
     } 
     
     private ArrayList<Integer> viewReservedFlights(int customerID){
         ArrayList<Integer> reservationIDs = new ArrayList<>();
         try{
             Statement reservationStmt = con.createStatement();
             ResultSet result;
             result = reservationStmt.executeQuery("select reservation_id, "
                     + "trip_number, trip_date, seat_class, price, card_num "
                     + "from customer_reserved natural join reservation natural "
                     + "join credit_card_reserved natural join trip where "
                     + "customer_id = " + customerID);
             if (!result.next()){
                 System.out.println("You have no reservations on file.");
                 
             }
             else {
                 
                 int i = 1;
                 do{
                     int reservation_id, trip_number, price;
                     long card_num;
                     String trip_date, seat_class;
                     reservation_id = result.getInt(1);
                     trip_number = result.getInt(2);
                     trip_date = result.getString(3);
                     seat_class = result.getString(4);
                     price = result.getInt(5);
                     card_num = result.getLong(6);
                     System.out.println("Reservation " + i + ":");
                     System.out.println("Reservation ID: " + reservation_id);
                     System.out.println("Flight Number: " + trip_number);
                     System.out.println("Date of Flight: " + trip_date);
                     System.out.println("Seat Class: " + seat_class);
                     System.out.println("Price: $" + price);
                     System.out.println("Purchased with Credit Card: " + card_num);
                     System.out.println("");
                     /*
                     
                     
                     */
                     i++;
                 } while (result.next());
             }
         } catch (SQLException e){
                 System.out.println("Error: Database error, please try again.");
         }
         return reservationIDs;
     }
     
     private void cancelReservedFlight(int customerID){
         System.out.println("Listing all of your reservations on file...");
         if (!hasReservations(customerID)){
             System.out.println("You have no reservations on file. Please add one"
                     + " before trying to delete one.");
         }
         ArrayList<Integer> reservations = viewReservedFlights(customerID);
         System.out.println("Please enter the number of the reservation you "
                 + "would like to cancel, or enter 0 to go back.");
 
         boolean valid = false;
         int userChoice;
         int reservation = -1;
         while(!valid){
             System.out.print("-->");
             userChoice = in.nextInt();
             in.nextLine();
             if(userChoice == 0){
                     return;
             }
             try {
                 reservation = (reservations.get(userChoice-1)).intValue();
                 con.setAutoCommit(false);
                 //remove from reserved_seat
                 Statement stmtReservedSeat;
                 stmtReservedSeat = con.createStatement();
 
                 String deleteReservedSeats = "delete from reserved_seat where "
                         + "customer_id = " + customerID + " and reservation_id"
                         + " = " + reservation + "";
                 int resultReservedSeats = stmtReservedSeat.executeUpdate(deleteReservedSeats);
                 if (resultReservedSeats > 0){
                     //remove from credit_card_reserved
                     String deleteCreditCardReserved = "delete from credit_card_reserved "
                             + "where reservation_id = " + reservation + "";
                     Statement stmtCreditCardReserved;
                     stmtCreditCardReserved = con.createStatement();
                     int resultCreditCardReserved;
                     resultCreditCardReserved = stmtCreditCardReserved.executeUpdate(deleteCreditCardReserved);
                     if(resultCreditCardReserved == 1){
                         //remove from customer_reserved
                         String deleteCustomerReserved = "delete from "
                                 + "customer_reserved where customer_id = " 
                                 + customerID + " and reservation_id = " 
                                 + reservation + "";
                         Statement stmtCustomerReserved;
                         stmtCustomerReserved = con.createStatement();
                         int resultCustomerReserved;
                         resultCustomerReserved = stmtCustomerReserved.executeUpdate(deleteCustomerReserved);
                         if(resultCustomerReserved == 1){
                             //remove from reservation
                             String deleteReservation = "delete from reservation "
                                     + "where reservation_id = " + reservation;
                             Statement stmtReservation;
                             stmtReservation = con.createStatement();
                             int resultReservation;
                             resultReservation = stmtReservation.executeUpdate(deleteReservation);
                             if (resultReservation == 1){
                                 System.out.println("Reservation successfully deleted from your account.");
                                 valid = true;
                                 con.commit();
                                 con.setAutoCommit(true);
                             }
                             else {
                                 System.out.println("Update of reservation failed.");
                                 valid = false;
                                 con.rollback();
                                 con.setAutoCommit(true);
                             }
                             stmtReservation.close();
                         }
                         else {
                             System.out.println("Update of credit_card_reserved failed.");
                             valid = false;
                             con.rollback();
                             con.setAutoCommit(true);
                         }
                         stmtCustomerReserved.close();
                         
                     }
                     else {
                         System.out.println("Update of customer_reserved failed.");
                         valid = false;
                         con.rollback();
                         con.setAutoCommit(true);
                     }
                     stmtCreditCardReserved.close();
                 }
                 else {
                     System.out.println("Update of reserved_seat failed.");
                     con.rollback();
                     con.setAutoCommit(true);
                     valid = false;
                 }
                 stmtReservedSeat.close();
             } catch (SQLException ex){
                 System.out.println("Error: Database error. Please try again later.");
                 try {
                     con.rollback();
                     con.setAutoCommit(true);
                 } catch (SQLException ex1) { //please don't ever get to here
                     System.out.println("Database rollback failed. Check internet connection. Exiting program.");
                     System.exit(1);
                 }
                 valid = true;
             } catch (IndexOutOfBoundsException ex){
                 System.out.println("Please enter a valid number choice.");
                 try {
                     con.rollback();
                     con.setAutoCommit(true);
                 } catch (SQLException ex1) { //please don't ever get to here
                     System.out.println("Database rollback failed. Check internet connection. Exiting program.");
                     System.exit(1);
                 }
                 valid = false;
             }
             
         }
         if (reservation < 0){
                 System.out.println("Error: Reservation not chosen");
         }
     }
     
     private void printOptions(){
         System.out.println("1: Manage Credit Card Information");
         System.out.println("2: Make a Reservation");
         System.out.println("3: View Reserved Flights");
         System.out.println("4: Cancel Reserved Flights");
         System.out.println("5: Close Customer Interface");
         System.out.print("-->");
     }
     
     
 }
