 package com.group2.atm;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.security.PublicKey;
 
 import javax.crypto.SealedObject;
 
 import com.group2.authority.G2Constants;
 import com.group2.crypto.CryptoAES;
 import com.group2.crypto.keyexchange.KeyExchangeSupport;
 import com.group2.crypto.keyexchange.KeyExchangeSupport.AppMode;
 import com.group2.crypto.keyexchange.messages.AuthenticationMessage;
 import com.group2.crypto.keyexchange.messages.CertificateResponseMessage;
 import com.group2.crypto.keyexchange.messages.InitiationMessage;
 import com.group2.crypto.keyexchange.messages.KeyExchangeMessage;
 import com.group2.crypto.keyexchange.messages.SecretExchangeMessage;
 import com.group2.crypto.keyexchange.messages.SecretExchangePayload;
 import com.group2.messaging.BalanceRequest;
 import com.group2.messaging.BalanceResponse;
 import com.group2.messaging.Message;
 import com.group2.messaging.Payload;
 import com.group2.messaging.TerminationResponse;
 import com.group2.messaging.WithdrawRequest;
 import com.group2.messaging.WithdrawResponse;
 import com.group2.util.PropertiesFile;
 import com.group2.util.Protocol;
 import com.group2.util.cin;
 
 
 
 /**
  * An ATMProtocol processes local splitStr[0]s sent to the ATM and writes to or reads
  * from the router as necessary. You can use whatever method you would like to
  * read from and write to the router, but this is an example to get you started.
  */
 
 public class ATMProtocol implements Protocol {
 
 	private ObjectOutputStream writer;
 	private ObjectInputStream reader;
 	private TransactionManager transactionManager;
 	private KeyExchangeSupport keyExchangeSupport;
 
 	public ATMProtocol(Socket socket) throws IOException {
 		writer = new ObjectOutputStream(socket.getOutputStream());
 		reader = new ObjectInputStream(socket.getInputStream());
 		keyExchangeSupport = new KeyExchangeSupport(AppMode.ATM);
 	}
 
 	/* Continue to read input until terminated. */
 	public void processLocalCommands(BufferedReader stdIn, String prompt) throws IOException {
 		String userInput;
 
 		while ((userInput = stdIn.readLine()) != null) {
 			processCommand(userInput);
 			System.out.print(prompt);
 		}
 
 		stdIn.close();
 	}
 
 	private void processCommand(String command) throws IOException {
 		Payload requestPayload = null;
 		Message requestMessage = null;
 
 		// Split the input command on whitespace
 		String[] splitCmdString = command.split("\\s+");
 
 		if (splitCmdString[0].toLowerCase().matches("begin-session")) {
 			if (transactionManager != null && transactionManager.transactionActive()) {
 				System.out.println("Transaction currently in progress.  Please end-session before beginning a new session.");
 				return;
 			}
 			this.transactionManager = new TransactionManager();
 			boolean transactionActive = transactionManager.authenticateSession(splitCmdString);
 			
 			if (PropertiesFile.isDebugMode()) {
 				System.out.println("processCommand: transactionManager=" + transactionManager);
 			}
 			
 			if (!transactionActive) {
 				System.out.println("Cannot process card.");
 				return;
 			}
 			
 			this.writer.writeObject(new InitiationMessage());
 			this.processRemoteCommands();
 			this.processRemoteCommands();
 		}
 		else if (splitCmdString[0].toLowerCase().matches("balance")) {
 			requestPayload = this.generateBalanceRequest();
 		}
 		else if (splitCmdString[0].toLowerCase().matches("withdraw")) {
 			requestPayload = this.generateWithdrawRequest();  	
 		} 
 		else if (splitCmdString[0].toLowerCase().matches("end-session")) {
			transactionManager.endCurrentTransaction();
 			requestPayload = this.generateTerminationRequest();
 		}
 		else {
 			System.out.println("Illegal input entered");
 		}
 
 		if (requestPayload != null) {
 			requestMessage = new Message();
 			requestMessage.setSessionId(this.transactionManager.getSessionId());
 			requestMessage.setSealedPayload(CryptoAES.encrypt(this.transactionManager.getSessionKey(), requestPayload));
 
 			writer.writeObject(requestMessage);
 
 			// After the message is set to bank, prepare to process the response and block
 			processRemoteCommands();
 		}
 
 	} // end processCommand
 
	private Payload generateTerminationRequest() {    	

 		return null;
 	}
 
 	private Payload generateWithdrawRequest() {
		if (!transactionManager.transactionActive()) {
 			System.out.println("No user logged in");
 			return null;
 		}
 
 		double amt = promptForWithdraw();
 		if (amt > 0) {
 			return new WithdrawRequest(transactionManager.getActiveAccountNum(), amt);
 		}
 
 		return null;
 	}
 
 	private Payload generateBalanceRequest() {
		if (!transactionManager.transactionActive()) {
 			System.out.println("No user logged in");
 			return null;
 		}
 
 		return new BalanceRequest(transactionManager.getActiveAccountNum());
 	}
 
 	public void processRemoteCommands() throws IOException {
 		Object msgObject;
 
 		try {
 			msgObject = (Object) reader.readObject();
 			
 			if (PropertiesFile.isDebugMode()) {
 				System.out.println("processRemoteCommands: msgObject=" + msgObject);
 			}
 			if (msgObject instanceof Message) {
 				this.processMessage((Message) msgObject);
 			}
 			else if(msgObject instanceof KeyExchangeMessage) {
 				this.processMessage((KeyExchangeMessage) msgObject);
 			}
 
 		} catch (ClassNotFoundException e) {
 			if (PropertiesFile.isDebugMode()) {
 				e.printStackTrace();
 			}
 		} // end catch
 
 	}
 
 	private void processMessage(KeyExchangeMessage msgObject) {
 
 		if (msgObject.mType == KeyExchangeMessage.MessageType.CertificateResponse) {
 
 			PublicKey bankPublicKey = this.keyExchangeSupport.validateCertificate(((CertificateResponseMessage) msgObject).getBankCert(), G2Constants.BANK_NAME);
 			if (PropertiesFile.isDebugMode()) {
 				System.out.println("processMessage: bankPublicKey=" + bankPublicKey);
 			}
 			if (bankPublicKey != null) {
 				
 				System.out.print("Enter your PIN: ");
                 String pin = cin.readLine();
 				this.transactionManager.setSessionId(((CertificateResponseMessage) msgObject).getSessionId());
 				SecretExchangePayload secret = new SecretExchangePayload(
 						this.transactionManager.getActiveAccountNum(), 
 						this.transactionManager.getSessionId(), 
 						this.transactionManager.getSessionKey(), 
 						pin);
 				SealedObject so = this.keyExchangeSupport.encryptSecret(secret, bankPublicKey);
 				try {
 					this.writer.writeObject(new SecretExchangeMessage(so, this.transactionManager.getSessionId()));
 					if (PropertiesFile.isDebugMode()) {
 						System.out.println("Secure session created.");
 					}
 				} catch (IOException e) {
 					if (PropertiesFile.isDebugMode()) {
 						e.printStackTrace();
 					}
 				}
 			}
 			else {
 				if (PropertiesFile.isDebugMode()) {
 					System.err.println("Bad Bank Public Key!");
 				}
 			}
 		} else if (msgObject.mType == KeyExchangeMessage.MessageType.AuthenticationResponse) {
 			this.transactionManager.authenticationMessage((AuthenticationMessage) msgObject);
 		}
 	}
 
 	private void processMessage(Message msgObject) {
 		// Pull the payload out of the generic message.  The payload is the
 		// specific message type.
 		Payload payload = CryptoAES.decrypt(this.transactionManager.getSessionKey(), msgObject.getSealedPayload());
 
 		if (payload instanceof BalanceResponse) {
 			transactionManager.balanceResponse((BalanceResponse) payload);
 		}
 		else if (payload instanceof WithdrawResponse) {
 			transactionManager.withdrawResponse((WithdrawResponse) payload);
 		}
 		else if (payload instanceof TerminationResponse) {
 			transactionManager.endCurrentTransaction();
 			this.transactionManager = null;
 		}
 	}
 
 	private double promptForWithdraw() {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
 		double amt = 0.0;
 		System.out.print("Enter the amount to withdraw: $");
 
 		// read the amount to withdraw from the command-line
 		try {
 			amt = Double.parseDouble(br.readLine());
 		} catch (IOException ioe) {
 
 		} catch (NumberFormatException e) {
 
 		}
 
 		return amt;
 	}
 
 	/* Clean up all open streams. */
 	public void close() throws IOException {
 		writer.close();
 		reader.close();
 	}
 
 }
