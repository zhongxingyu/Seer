 package application;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import exceptions.*;
 import javafx.collections.*;
 import javafx.event.*;
 import javafx.fxml.*;
 import javafx.scene.*;
 import javafx.scene.control.*;
 import javafx.scene.input.*;
 import javafx.scene.layout.*;
 import javafx.stage.*;
 import logic.*;
 import jfxtras.labs.dialogs.*;
 import jfxtras.labs.dialogs.MonologFX.*;
 
 public class Controller {
 
 	@FXML
 	private ResourceBundle resources;
 
 	@FXML
 	private URL location;
 
 	@FXML
 	private MenuItem About;
 
 	@FXML
 	private MenuItem NewAccount;
 
 	@FXML
 	private MenuBar MenuBar;
 
 	@FXML
 	private Menu MenuFile;
 
 	@FXML
 	private Menu MenuHelp;
 
 	@FXML
 	private Button acceptAccount;
 
 	@FXML
 	private Button acceptPin;
 
 	@FXML
 	private Button accountStatement;
 
 	@FXML
 	private Button removeCard;
 
 	@FXML
 	private Button withdraw;
 
 	@FXML
 	private ComboBox<Integer> chooseYourMoney;
 
 	@FXML
 	private RadioButton free;
 
 	@FXML
 	private RadioButton choice;
 
 	@FXML
 	private TextArea infoText;
 
 	@FXML
 	private TextField moneyField;
 
 	@FXML
 	private TextField pinField;
 
 	@FXML
 	private TextField accountField;
 
 	@FXML
 	private AnchorPane accon;
 
 	@FXML
 	private AccountController dialogController;
 	/*
 	 * http://docs.oracle.com/javafx/2/api/javafx/scene/control/RadioButton.html
 	 * "Only one RadioButton can be selected when placed in a ToggleGroup.... A
 	 * RadioButton that is not in a ToggleGroup can be selected and unselected."
 	 */
 	ToggleGroup group = new ToggleGroup();
 
 	/*
 	 * I have borrowed this idea from James_D (Co-Director, Marshall University
 	 * Genomics and Bioinformatics Core Facility) here
 	 * https://forums.oracle.com/message/10746865
 	 * 
 	 * You can either use the List-item-object or ObservableList-item2-object
 	 * 
 	 * List<Integer> items = Arrays.asList(new Integer[] { 100, 200, 500, 700,
 	 * 1000, 1200 });
 	 */
 	ObservableList<Integer> items2 = FXCollections.observableArrayList(100,
 			200, 500, 700, 1000, 1200);
 
 	/*
 	 * Object from CashMachine & CashCard
 	 */
 	CashMachine<Account> maschine = new CashMachine<Account>();
 	CashCard cashCard = new CashCard();
 
 	@FXML
	void initialize() {
 		assert About != null : "fx:id=\"About\" was not injected: check your FXML file 'default.fxml'.";
 		assert MenuBar != null : "fx:id=\"MenuBar\" was not injected: check your FXML file 'default.fxml'.";
 		assert MenuFile != null : "fx:id=\"MenuFile\" was not injected: check your FXML file 'default.fxml'.";
 		assert MenuHelp != null : "fx:id=\"MenuHelp\" was not injected: check your FXML file 'default.fxml'.";
 		assert NewAccount != null : "fx:id=\"NewAccount\" was not injected: check your FXML file 'default.fxml'.";
 		assert acceptAccount != null : "fx:id=\"acceptAccount\" was not injected: check your FXML file 'default.fxml'.";
 		assert acceptPin != null : "fx:id=\"acceptPin\" was not injected: check your FXML file 'default.fxml'.";
 		assert accountField != null : "fx:id=\"accountField\" was not injected: check your FXML file 'default.fxml'.";
 		assert accountStatement != null : "fx:id=\"accountStatement\" was not injected: check your FXML file 'default.fxml'.";
 		assert choice != null : "fx:id=\"choice\" was not injected: check your FXML file 'default.fxml'.";
 		assert chooseYourMoney != null : "fx:id=\"chooseYourMoney\" was not injected: check your FXML file 'default.fxml'.";
 		assert free != null : "fx:id=\"free\" was not injected: check your FXML file 'default.fxml'.";
 		assert infoText != null : "fx:id=\"infoText\" was not injected: check your FXML file 'default.fxml'.";
 		assert moneyField != null : "fx:id=\"moneyField\" was not injected: check your FXML file 'default.fxml'.";
 		assert pinField != null : "fx:id=\"pinField\" was not injected: check your FXML file 'default.fxml'.";
 		assert removeCard != null : "fx:id=\"removeCard\" was not injected: check your FXML file 'default.fxml'.";
 		assert withdraw != null : "fx:id=\"withdraw\" was not injected: check your FXML file 'default.fxml'.";
 
 		chooseYourMoney.setItems(items2);
 
 		chooseYourMoney.setDisable(true);
 		moneyField.setDisable(true);
 		accountStatement.setDisable(true);
 
 		free.setToggleGroup(group);
 		choice.setToggleGroup(group);
 
 		acceptAccount();
 		acceptPin();
 		accountField();
 		accountStatement();
 		withdraw();
 		removeCard();
 		pinField();
 
 	}
 
 	@FXML
 	void acceptAccount() {
 		acceptAccount.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent event) {
 				try {
 					int karteNummer = Integer.parseInt(accountField.getText());
 					cashCard.setAccountNumber(karteNummer);
 					maschine.insertCashCard(cashCard);
 					infoText.appendText("\nYou have inserted a card in ATM");
 				} catch (CardInsertedException e) {
 					System.out.println(e.getMessage());
 				} catch (InvalidCardException e) {
 					infoText.appendText("\nWrong account number");
 				} catch (NumberFormatException e) {
 					infoText.appendText("\nYou can't do this");
 				}
 				accountField.setEditable(false);
 				accountStatement.setDisable(false);
 				event.consume();
 			}
 		});
 	}
 
 	@FXML
 	void accountField() {
 		accountField.addEventFilter(KeyEvent.KEY_PRESSED,
 				new EventHandler<KeyEvent>() {
 
 					@Override
 					public void handle(KeyEvent event) {
 						if (event.getCode() == KeyCode.ENTER) {
 							try {
 								int karteNummer = Integer.parseInt(accountField
 										.getText());
 								cashCard.setAccountNumber(karteNummer);
 								maschine.insertCashCard(cashCard);
 								infoText.appendText("\nYou have inserted a card in ATM");
 							} catch (CardInsertedException e) {
 								System.out.println(e.getMessage());
 							} catch (InvalidCardException e) {
 								infoText.appendText("\nWrong account number");
 							} catch (NumberFormatException e) {
 								infoText.appendText("\nYou can't do this");
 							}
 							accountField.setEditable(false);
 							accountStatement.setDisable(false);
 						}
 						event.consume();
 					}
 				});
 	}
 
 	@FXML
 	void accountStatement() {
 		accountStatement.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent event) {
 				infoText.appendText("\n" + maschine.accountStatementMethod());
 				moneyField.setDisable(false);
 				chooseYourMoney.setDisable(false);
 				event.consume();
 			}
 		});
 	}
 
 	@FXML
 	void acceptPin() {
 		acceptPin.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent event) {
 				try {
 					int pinNummer = Integer.parseInt(pinField.getText());
 					maschine.pinInsert(pinNummer);
 					infoText.appendText("\nYou habe inserted a pin number in ATM");
 				} catch (PinNotCorectException e) {
 					System.out.println(e.getMessage());
 				} catch (CardNotInsertedException e) {
 					System.out.println(e.getMessage());
 				} catch (InvalidCardException e) {
 					System.out.println(e.getMessage());
 				} catch (NumberFormatException e) {
 					infoText.appendText("\nYou can't do this");
 				}
 				pinField.setEditable(false);
 				accountStatement.setDisable(false);
 				// infoText.appendText("\n take your card out, then you can change the number again");
 				event.consume();
 			}
 		});
 	}
 
 	@FXML
 	void pinField() {
 		pinField.addEventFilter(KeyEvent.KEY_PRESSED,
 				new EventHandler<KeyEvent>() {
 
 					@Override
 					public void handle(KeyEvent event) {
 						if (event.getCode() == KeyCode.ENTER) {
 							try {
 								int pinNummer = Integer.parseInt(pinField
 										.getText());
 								maschine.pinInsert(pinNummer);
 								infoText.appendText("\nYou habe inserted a pin in ATM");
 							} catch (PinNotCorectException e) {
 								System.out.println(e.getMessage());
 							} catch (CardNotInsertedException e) {
 								System.out.println(e.getMessage());
 							} catch (InvalidCardException e) {
 								System.out.println(e.getMessage());
 							} catch (NumberFormatException e) {
 								infoText.appendText("\nYou can't do this");
 							}
 							pinField.setEditable(false);
 						}
 						event.consume();
 					}
 				});
 	}
 
 	/**
 	 * for the right docs see JavaFX8 docs:
 	 * xxx/api/javafx/scene/control/TextInputControl.html#deleteText(int,%20int)
 	 * and see this bug {@link} https://javafx-jira.kenai.com/browse/RT-31802
 	 */
 	@FXML
 	void removeCard() {
 		removeCard.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent event) {
 				try {
 					maschine.ejectCashCard();
 					int zahl = accountField.getLength();
 					int zahl2 = pinField.getLength();
 					int zahl3 = infoText.getLength();
 					accountField.deleteText(0, zahl);
 					accountField.setEditable(true);
 					pinField.deleteText(0, zahl2);
 					pinField.setEditable(true);
 					infoText.deleteText(0, zahl3);
 				} catch (CardNotInsertedException e) {
 					System.out.println(e.getMessage());
 				}
 				event.consume();
 			}
 		});
 	}
 
 	@FXML
 	void withdraw() {
 		withdraw.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent event) {
 				try {
 					if (choice.isSelected()) {
 						maschine.withdraw(chooseYourMoney.getValue());
 						infoText.appendText("\n--New--");
 						infoText.appendText("\n"
 								+ maschine.accountStatementMethod());
 					} else if (free.isSelected()) {
 						double zahl2 = Double.parseDouble(moneyField.getText());
 						maschine.withdraw(zahl2);
 						infoText.appendText("\n--New--");
 						infoText.appendText("\n"
 								+ maschine.accountStatementMethod());
 					} else {
 						infoText.appendText("\nYou must choose the ammount to withdraw");
 					}
 				} catch (PinNotCorectException e) {
 					System.out.println(e.getMessage());
 				} catch (NotEnoughMoneyException e) {
 					System.out.println(e.getMessage());
 				} catch (NumberFormatException e) {
 					infoText.appendText("\nYou can't do this");
 				}
 				event.consume();
 			} // here end of the small method
 		});
 	}// here the end of the method
 
 	@FXML
 	void About() throws IOException {
 		About.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent event) {
 				// user must do sth. before click on this
 				// or he must click 2 times
 				MonologFX mf = new MonologFX(Type.INFO);
 				mf.setMessage("Created by @Malcjohn");
 				mf.setTitleText("About this app");
 				mf.showDialog();
 			}
 		});
 
 	}
 
 	@FXML
 	void NewAccount() {
 		NewAccount.setOnAction(new EventHandler<ActionEvent>() {
 
 			@Override
 			public void handle(ActionEvent event) {
 				Parent root;
 				try {
 					root = FXMLLoader.load(getClass().getResource(
 							"/res/account.fxml"));
					Scene as = new Scene(root);
					Stage asd = new Stage();
					asd.setScene(as);
					asd.show();					
 				} catch (IOException e) {
 					System.out.println("error");
 				}
 				event.consume();
 			}
 		});
 	}
 }
