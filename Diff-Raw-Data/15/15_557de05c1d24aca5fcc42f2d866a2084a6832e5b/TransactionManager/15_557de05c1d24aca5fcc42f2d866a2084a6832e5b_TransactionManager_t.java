 package original;
 
 import messaging.AuthenticationRequest;
 import messaging.BalanceRequest;
 import messaging.BalanceResponse;
 import messaging.AuthenticationResponse;
 import messaging.WithdrawResponse;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Group2
  * Date: 5/25/13
  * Time: 9:44 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class TransactionManager {
     private boolean transactionActive = false;
     private AtmCardClass atmCard;
     private File cardFile;
     private int activeAccountNum = 0;
     private int numberOfFailedLoginAttempts;
 
     private AuthenticationRequest authenticationRequest;
     private AuthenticationResponse authenticationResponse;
     private BalanceRequest balanceRequest;
     private BalanceResponse balanceResponse;
 
     public TransactionManager() {
     }
 
     public void endCurrentTransaction() {
         transactionActive = false;
        if(atmCard != null) {
        	System.out.println(atmCard.getName() + " logged out.");
        }
     } // end setTransactionActive
 
     public int getActiveAccountNum() {
         return activeAccountNum;
     } // end getActiveAccountNum()
 
     public boolean transactionActive() {
         return transactionActive;
     } // end transactionActive()
 
     public AuthenticationRequest authenticateSession(String[] splitCmdString) throws IOException {
 
         int enteredPin = 0;
 
         // Check if more than one argument was read from stdin
         if (!(splitCmdString.length > 1)) {
 
             // Not enough command line arguments were given so set the
             // transaction state to inactive and the session request to null
             transactionActive = false;
             activeAccountNum = 0;
             authenticationRequest = null;
 
         } // end if length < 1
 
         else { // splitCmdString.length IS greater than 1 so multiple args were given
 
             // Prepare and read the original.ATM card for the requested username
             cardFile = new File(splitCmdString[1] + ".card");
 
             // if the card doesn't exist with the given user name
             if (!cardFile.isFile()) {
 
                 // User's name does not match the card or the file doesn't exist so set the
                 // transaction state to inactive and the session request to null
                 transactionActive = false;
                 activeAccountNum = 0;
                 authenticationRequest = null;
 
             } // end if not a valid card file
 
             else { // this IS a valid card file
 
                 // Read the original.ATM card into a class variable
                 atmCard = (AtmCardClass) Disk.load(splitCmdString[1] + ".card");
 
                 // Check that the name within the card matches the given name
                 if (!atmCard.getName().matches(splitCmdString[1])) {
                     // User's name does not match the card or the file doesn't exist so set the
                     // transaction state to inactive and the session request to null
                     transactionActive = false;
                     activeAccountNum = 0;
                     authenticationRequest = null;
 
                 } // end if (!atmCard.getName().matches(splitCmdString[1]))
                 // All checks have passed so prompt for the pin
                 else {
                     // Get the pin
                     System.out.print("Enter your PIN: ");
                     enteredPin = cin.readInt();
 
                     // Get the required information out of the card and prepare the message
                     authenticationRequest = new AuthenticationRequest(enteredPin, atmCard.getAccountNumber());
                 }
 
             } // end else this IS a valid card file
 
         } // end else splitCmdString.length IS greater than 1 so multiple args were given
 
         // Return the message to be sent, or null
         return authenticationRequest;
 
     } // end authenticateSession
 
     public void authenticationResponse(AuthenticationResponse authenticationResponse) {
 
         try {
             // Set the transaction state true or false
             if (transactionActive = authenticationResponse.isSessionValid() == true) {
 
                 // Set the active active account number
                 activeAccountNum = authenticationResponse.getAccountNumber();
 
                 System.out.println("User " + atmCard.getName() + " is Authorized");
 
             } // end if transactionActive
             else {
 
                 System.out.println("Unauthorized");
             } // end else
 
         } catch (UnknownError e) {
             e.getMessage();
             e.printStackTrace();
         } // end catch
     } // end sessionResponse
 
     public void balanceResponse(BalanceResponse balanceResponse) {
         System.out.println("balance: $" + balanceResponse.getBalance());
     } // end processBalanceResponse
 
     private void readAtmCard(String[] splitCmdString) throws IOException {
 
         // Prepare and read the original.ATM card for the requested username
         cardFile = new File(splitCmdString[1] + ".card");
 
         if (!cardFile.isFile()) {
 
             // Not a valid card file so set the card class variable to null.
             atmCard = null;
 
         } else { // this IS a valid card file
 
             // Load the contents of the stored card into the card class variable.
             atmCard = (AtmCardClass) Disk.load(splitCmdString[1] + ".card");
         }
 
     } // end readAtmCard
 
 	public void withdrawResponse(WithdrawResponse payload) {
 		double amt = payload.getWithdrawAmount();
 		if (amt != 0) {
 			System.out.println("$" + amt + " dispensed");
 		} else {
 			System.out.println("Insufficient funds.");
 		}
 	}
 
 } // end class original.TransactionManager
