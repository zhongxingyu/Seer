 package application;
 
 import java.net.*;
 import java.util.*;
 
 import exceptions.*;
 import logic.*;
 import javafx.event.*;
 import javafx.fxml.*;
 import javafx.scene.control.*;
 import javafx.scene.input.*;
import javafx.stage.*;
 
 public class AccountController {
 
 	@FXML
 	private ResourceBundle resources;
 
 	@FXML
 	private URL location;
 
 	@FXML
 	private Button ButtonAccountNumber;
 
 	@FXML
 	private Button ButtonCash;
 
 	@FXML
 	private Button ButtonOverdraft;
 
 	@FXML
 	private Button ButtonPin;
 
 	@FXML
 	private Button printButton;
 
 	@FXML
 	private Button saveAccount;
 
 	@FXML
 	private TextField AccountNumberField;
 
 	@FXML
 	private TextField CashField;
 
 	@FXML
 	private TextField OverdraftField;
 
 	@FXML
 	private PasswordField PinField;
 
 	@FXML
 	private TextArea TextArea;
 
 	/*
 	 * Object Account for inserting all the stuff there
 	 */
 	Account ac = new Account();
 	CashMachine<Account> cm = new CashMachine<Account>();
 
 	@FXML
 	void initialize() {
 		assert AccountNumberField != null : "fx:id=\"AccountNumberField\" was not injected: check your FXML file 'Account.fxml'.";
 		assert ButtonAccountNumber != null : "fx:id=\"ButtonAccountNumber\" was not injected: check your FXML file 'Account.fxml'.";
 		assert ButtonCash != null : "fx:id=\"ButtonCash\" was not injected: check your FXML file 'Account.fxml'.";
 		assert ButtonOverdraft != null : "fx:id=\"ButtonOverdraft\" was not injected: check your FXML file 'Account.fxml'.";
 		assert ButtonPin != null : "fx:id=\"ButtonPin\" was not injected: check your FXML file 'Account.fxml'.";
 		assert CashField != null : "fx:id=\"CashField\" was not injected: check your FXML file 'Account.fxml'.";
 		assert OverdraftField != null : "fx:id=\"OverdraftField\" was not injected: check your FXML file 'Account.fxml'.";
 		assert PinField != null : "fx:id=\"PinField\" was not injected: check your FXML file 'Account.fxml'.";
 		assert TextArea != null : "fx:id=\"TextArea\" was not injected: check your FXML file 'Account.fxml'.";
 		assert printButton != null : "fx:id=\"printButton\" was not injected: check your FXML file 'account.fxml'.";
 
 		AccountNumberMethod();
 		ButtonAccountNumberMethod();
 		ButtonCashMethod();
 		CashFieldMethod();
 		PinFieldMethod();
 		ButtonPinMethod();
 		OverdraftFieldMethod();
 		ButtonOverdraftMethod();
		Save();
		printAllAccounts();
 
 	}
 
 	// account number - button
 	@FXML
 	void AccountNumberMethod() {
 		AccountNumberField.addEventFilter(KeyEvent.KEY_PRESSED,
 				new EventHandler<KeyEvent>() {
 
 					@Override
 					public void handle(KeyEvent event) {
 						if (event.getCode() == KeyCode.ENTER) {
 							int AccountNumber = Integer
 									.parseInt(AccountNumberField.getText());
 							ac.setAccountNumber(AccountNumber);
 							TextArea.appendText("\nEverything allright: "
 									+ AccountNumberField.getText());
							//AccountNumberField.setDisable(true);
 						}
 					}
 				});
 	}
 
 	// account number - textfield
 	@FXML
 	void ButtonAccountNumberMethod() {
 		ButtonAccountNumber.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent event) {
 				int AccountNumber = Integer.parseInt(AccountNumberField
 						.getText());
 				ac.setAccountNumber(AccountNumber);
 				TextArea.appendText("\nEverything allright: "
 						+ AccountNumberField.getText());
 			}
 		});
 	}
 
 	// cash money - button
 	@FXML
 	void ButtonCashMethod() {
 		ButtonCash.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent event) {
 				double zahl = Double.parseDouble(CashField.getText());
 				ac.setBankDeposit(zahl);
 				TextArea.appendText("\nEverything allright: "
 						+ CashField.getText());
 			}
 		});
 	}
 
 	// cash money - field
 	@FXML
 	void CashFieldMethod() {
 		CashField.addEventFilter(KeyEvent.KEY_PRESSED,
 				new EventHandler<KeyEvent>() {
 
 					@Override
 					public void handle(KeyEvent event) {
 						if (event.getCode() == KeyCode.ENTER) {
 							double overdraft = Double.parseDouble(CashField
 									.getText());
 							ac.setBankDeposit(overdraft);
 							TextArea.appendText("\nEverything allright: "
 									+ CashField.getText());
 							CashField.setDisable(true);
 						}
 					}
 				});
 	}
 
 	// overdraft money - button
 	@FXML
 	void ButtonOverdraftMethod() {
 		ButtonOverdraft.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent event) {
 				double overdraft = Double.parseDouble(OverdraftField.getText());
 				ac.setOverdraft(overdraft);
 				TextArea.appendText("\nEverything allright: "
 						+ OverdraftField.getText());
 			}
 		});
 	}
 
 	// overdraft money - field
 	@FXML
 	void OverdraftFieldMethod() {
 		OverdraftField.addEventFilter(KeyEvent.KEY_PRESSED,
 				new EventHandler<KeyEvent>() {
 
 					@Override
 					public void handle(KeyEvent event) {
 						if (event.getCode() == KeyCode.ENTER) {
 							double overdraft = Double
 									.parseDouble(OverdraftField.getText());
 							ac.setOverdraft(overdraft);
 							TextArea.appendText("\nEverything allright: "
 									+ OverdraftField.getText());
 							OverdraftField.setDisable(true);
 						}
 					}
 				});
 	}
 
 	// pin number - button
 	@FXML
 	void ButtonPinMethod() {
 		ButtonPin.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent event) {
 				int pin = Integer.parseInt(PinField.getText());
 				try {
 					ac.setPin(pin);
 					TextArea.appendText("\nEverything allright: "
 							+ PinField.getText());
 				} catch (WrongQuantityOfDigits e) {
 					TextArea.appendText("\nOnly 4 number are allowed");
 				}
 			}
 		});
 	}
 
 	// pin number field
 	@FXML
 	void PinFieldMethod() {
 		PinField.addEventFilter(KeyEvent.KEY_PRESSED,
 				new EventHandler<KeyEvent>() {
 
 					@Override
 					public void handle(KeyEvent event) {
 						if (event.getCode() == KeyCode.ENTER) {
 							int pin = Integer.parseInt(PinField.getText());
 							try {
 								ac.setPin(pin);
 								TextArea.appendText("\nEverything allright: "
 										+ PinField.getText());
 								PinField.setDisable(true);
 							} catch (WrongQuantityOfDigits e) {
 								TextArea.appendText("\nOnly 4 number are allowed");
 							}
 						}
 					}
 				});
 	}
 
 	@FXML
 	void printAllAccounts() {
 		printButton.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent event) {
 				Iterator<Account> s = cm.iterator();
 				TextArea.appendText("-----------");
 				while (s.hasNext()) {
 					TextArea.appendText("\n" + s.next());
 				}
 			}
 		});
 	}
 
 	@FXML
 	void Save() {
 		saveAccount.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent arg0) {
 				Account saveNewOne = new Account(ac.getAccountNumber(), ac
 						.getOverdraft(), ac.getBankDeposit(), ac.getPin());
 				cm.addNewAccount(saveNewOne);
 				TextArea.appendText("\nYou have saved new account");
 			}
 		});
 	}
 }
