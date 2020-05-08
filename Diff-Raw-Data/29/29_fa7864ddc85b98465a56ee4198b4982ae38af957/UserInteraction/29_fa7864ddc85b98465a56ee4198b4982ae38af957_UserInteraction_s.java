 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.Scanner;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.xml.sax.SAXException;
 
 
 public class UserInteraction {
	private ArrayList<MTGCard> searchResults;
 	public static String language;
 	public static String country;
 	private XMLParser parser;
 	private Deck currentDeck;
 	public static Locale currentLocale;
 	public static ResourceBundle messages;
 	public static Locale aLocale = new Locale("en", "US");
 	public static Locale deLocale = new Locale("de", "DE");
 	public static Locale frLocale = new Locale("fr", "FR");
 	
 
 	private int UserCommandsSize = 6;
 	private enum UserCommands{
 		SEARCH, DISPLAY, DECK, EXIT
 	}
 	private int SearchCommandsSize = 8;
 	private enum SearchEnumeration{
 		NAME, TYPE, POWER, TOUGHNESS, RULES, COST, DISPLAY 
 	}
 	private int DeckCommandsSize = 9;
 	private enum DeckEnum{
		DISPLAY, NEW, ADD, REMOVE, EXIT, LOAD, SAVE
 	}
 	private int RuleCommandsSize = 2;
 	private enum Rules{
 		BASIC
 	}
 	
 	public UserInteraction() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
 		this.parser = new XMLParser();
 		this.searchResults= new ArrayList<MTGCard>();
 		this.currentDeck = new Deck();
 	}
 	
 	
 	public void userInteraction() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
 		Scanner input = new Scanner(System.in);
 		String lang = getLanguage(input);
 		setLocale(lang);
 		System.out.println(getprintable("welcome"));
 		printUICommands();
 		while(input.hasNext()){
 			
 			String command = input.next().toUpperCase();
 			UserCommands u;
 			try{
 				u = UserCommands.valueOf(command);
 			}
 			catch(IllegalArgumentException e){
 				System.out.println(getprintable("unknowncerror") + command);
 				printUICommands();
 				continue;
 			}
 			
 			switch(u){
 			case SEARCH:
 				this.searchCommand(input);
 				break;
 			case DISPLAY:
 				String display = this.displayCommand();
 				System.out.println(display);
 				break;
 			case DECK:
 				this.deckCommands(input);
 				break;
 			case EXIT:
 				return;
 				
 			}
 			printUICommands();
 			
 			
 		}
 			
 	}
 	
 	public String getprintable(String line){
 		return messages.getString(line);
 	}
 
 
 	
 
 
 	private void printUICommands() {
 		for(int i = 1; i < UserCommandsSize; i++){
 			System.out.println(getprintable("standart" + i));
 		}
 		
 	}
 	
 	private void printDeckCommands() {
 		for(int i = 1; i < DeckCommandsSize; i++){
 			System.out.println(getprintable("deckcommands" + i));
 		}
 	}
 	
 	
 	private void deckCommands(Scanner input) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
 		printDeckCommands();
 		String result = "";
 		while(input.hasNext()){
 			String command = input.next();
 			command = command.toUpperCase();
 			DeckEnum c;
 			try {
 				c = DeckEnum.valueOf(command);
 			}
 			catch(IllegalArgumentException e){
 				System.out.println(getprintable("unknowncerror") + command);
 				printDeckCommands();
 				continue;
 			}
 			
 			switch(c){
 			case DISPLAY:
 				System.out.println(this.currentDeck.displayDeck());
 				break;
 			case NEW:
 				result = newDeck(input);
 				break;
 			case ADD:
 				addCard(input);
 				break;
 			case REMOVE:
 				removeCard(input);
 				break;
			case EXIT:
				return;
 			case SAVE:
 				String fileName = "";
 				System.out.println("If you have an existing file you would like to save to enter '1', otherwise enter '0'");
 				String fileChooser = input.next();
 				if(fileChooser.equals("1")){
 					System.out.println("What is the exact name of the file:");
 					fileName = input.next();
 					saveDeck(this.currentDeck, fileName);
 				}else if (fileChooser.equals("0")){
 					fileName = createNewFile(input);
 					saveDeck(this.currentDeck, fileName);
 				}else{
 					System.out.println("You input the wrong value");
 				}
 				
 				break;
 			case LOAD:
 				String fileName1 = "";
 				System.out.println("What file do you want to load?");
 				fileName1 = input.next();
 				loadDeck(fileName1);
 			}
 			
 			printDeckCommands();
 		}
 		System.out.println(result);
 	}
 
 private void loadDeck(String fileName) throws XPathExpressionException, ParserConfigurationException, SAXException {
 		try {
 			FileInputStream loadFile = new FileInputStream(fileName);
 			ObjectInputStream load = new ObjectInputStream(loadFile);
 			Integer numberCardsInFile = (Integer) load.readObject();
 			String rules = (String) load.readObject();
 			this.currentDeck = new Deck(rules);
 			int i = 0;
 			while(i<numberCardsInFile){
 				String cardName = (String) load.readObject();
 				MTGCard card = this.parser.searchForCardName(cardName);
 				this.currentDeck.addCardToDeck(card);
 			}
 		
 			
 		} catch (FileNotFoundException e) {
 			
 			return;
 		} catch (IOException e) {
 			
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			
 			e.printStackTrace();
 		}
 		
 	}
 
 
 private String newDeck(Scanner input){
 	String result = "";
 	System.out.println(getprintable("ruleprompt"));
 	for(Rules value: Rules.values()){
 		System.out.println("    "+value);
 	}
 	
 	String command = input.next();
 	command = command.toUpperCase();
 	this.currentDeck = new Deck(command);
 	result = this.currentDeck.toString();
 	
 	return result;
 }
 
 	private void addCard(Scanner input) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
 		System.out.println(getprintable("addprompt"));
 		System.out.println(getprintable("example") + " 4 Rancor");
 		int numberToAdd=0;
 		while(input.hasNext()){
 			String numberInput = input.next();
 			try{
 				numberToAdd = Integer.parseInt(numberInput);
 			}
 			catch(NumberFormatException e){
 				System.out.println(getprintable("addprompterror"));
 				continue;
 			}
 			if(numberToAdd == 0){
 				break;
 			}
 			input.skip(" ");
 			String cardName = input.nextLine();
 			int numberAdded = addCards(numberToAdd, cardName);
 			System.out.println(getprintable("addcompletion1")+ " " + numberAdded);
 			System.out.println(getprintable("addprompt"));
 			System.out.println(getprintable("example") + " 4 Rancor");
 		}
 	}
 
 
 	public int addCards(int numberToAdd, String cardName)
 			throws XPathExpressionException, ParserConfigurationException,
 			SAXException, IOException {
 		MTGCard card = parser.searchForCardName(cardName);
 		for(int i=0; i < numberToAdd; i++){
 			boolean added = this.currentDeck.addCardToDeck(card);
 			if(added == false){
 				return i;
 			}
 		}
 		return numberToAdd;
 		
 	}
 	
 
 	private void removeCard(Scanner input) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
 
 		System.out.println(getprintable("removeprompt"));
 		System.out.println(getprintable("example") + " 4 Rancor");
 		int numberToRemove=0;
 		while(input.hasNext()){
 			String numberInput = input.next();
 			try{
 				numberToRemove = Integer.parseInt(numberInput);
 			}
 			catch(NumberFormatException e){
 				System.out.println(getprintable("removeprompterror"));
 				continue;
 			}
 			if(numberToRemove == 0){
 				break;
 			}
 			input.skip(" ");
 			String cardName = input.nextLine();
 			MTGCard card = parser.searchForCardName(cardName);
 			for(int i=0; i < numberToRemove; i++){
 				this.currentDeck.removeCardFromDeck(card);
 			}
 			System.out.println(getprintable("removecompletion1"));
 			System.out.println(getprintable("removeprompt"));
 			System.out.println(getprintable("example") + " 4 Rancor");
 		}
 	}
 	public void saveDeck(Deck currentDeck, String fileName){
 		try {
 			FileOutputStream saveFile = new FileOutputStream(fileName);
 			@SuppressWarnings("resource")
 			ObjectOutputStream save = new ObjectOutputStream(saveFile);
 			save.writeObject((Integer) currentDeck.cards.size());
 			save.writeObject((String) currentDeck.rules.toString());
 			for(int i = 0; i < currentDeck.cards.size(); i++){
 				save.writeObject(currentDeck.cards.get(i).name);
 			}
 			save.close();
 		
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String createNewFile(Scanner input){
 		System.out.println("Enter the desired name of the file:");
 		String fileName = input.next();
 		try{
 			File file = new File(fileName);
 			
 			if (file.createNewFile()){
 				System.out.println("You have created a new file");
 			}else{
 				System.out.println("File already exist");
 			}
 		} catch (IOException e){
 			
 			e.printStackTrace();
 		}
 		return fileName;
 		
 		
 	}
 
 public void printSearchCommands(){
 	for(int i = 1; i < SearchCommandsSize ; i++){
 		System.out.println(getprintable("searchcommand" + i));
 	}
 }
 
 
 
 private void searchCommand(Scanner input) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
 		
 		String searchType = "";
 		String searchTerm = "";
 		printSearchCommands();
 		System.out.print("> ");
 		while(input.hasNext()){
 			
 			
 			String command = input.next();
 			command = command.toUpperCase();
 			SearchEnumeration c;
 			try{
 				c = SearchEnumeration.valueOf(command);
 			}
 			catch(IllegalArgumentException e){
 				System.out.println(getprintable("unknowncerror") + " ");
 				printSearchCommands();
 				System.out.print("> ");
 				continue;
 			}
 		
 			switch(c){
 			case NAME:
 				searchType = "name";
 				break;
 			case TYPE:
 				searchType = "type";
 				break;
 			case POWER:
 				searchType = "power";
 				break;
 			case TOUGHNESS:
 				searchType = "toughness";
 				break;
 			case RULES:
 				searchType = "rules";
 				break;
 			case COST:
 				searchType = "cost";
 				break;
 			}
 			break;
 		}
 		input.skip(" ");
 		searchTerm = input.nextLine();
 		
 		this.searchResults = search(this.parser, searchType, searchTerm);
 		System.out.println(this.searchResults.size() + " " + getprintable("searchresults"));
 		
 	}
 	public String displayCommand() {
 		StringBuilder sb = new StringBuilder();
 		
 		if(this.searchResults.isEmpty()){
 			sb.append(getprintable("searchresultsnull"));
 		}
 		else{
 			int searchResultIndex = 1;
 			for(MTGCard card : this.searchResults){
 				sb.append(getprintable("searchresults") + " " + searchResultIndex +"\n");
 				sb.append(card.toString() + "\n");
 				sb.append("\n");
 				searchResultIndex++;
 			}
 		}
 		return sb.toString();
 	
 	}
 
 
 	
 
 
 	public static ArrayList<MTGCard> search(XMLParser parser,
 			String searchType, String searchTerm)
 			throws ParserConfigurationException, SAXException, IOException,
 			XPathExpressionException {
 		String searchQuery = parser.buildXPathQuery(searchType, searchTerm);
 		
 		ArrayList<MTGCard> searchResults = parser.searchXML(searchQuery);
 		return searchResults;
 	}
 	
 	public static void setLocale(String locale) {
 		if(locale == ("English")){
 			language = "en";
 			country = "US";
 			currentLocale = aLocale;
 			messages = ResourceBundle.getBundle("MessagesBundle", currentLocale);
 		}
 		else{
 			language = "de";
 			country = "DE";
 			currentLocale = deLocale;
 			messages = ResourceBundle.getBundle("MessagesBundle", currentLocale);
 		}
 		
 	}
 
 	private static String getLanguage(Scanner scanInput) {
 		System.out.println("Please select your language");
 		System.out.println("Bitte w\u00E4hlen Sie Ihren Sprache");
 		System.out.println("1. English");
 		System.out.println("2. Deutsch");
 		String num = scanInput.nextLine();
 		if (num.equals("1") || num.equals("English")){
 			return "English";
 		}
 		else if (num.equals("2") || num.equals("Deutsch")){
 			return "German";
 		}
 		return "English";
 	}
 	
 	
 //getters
 	public ArrayList<MTGCard> getSearchResults() {
 		return this.searchResults;
 	}
 	
 	public void setSearchResults(ArrayList<MTGCard> searchResults){
 		this.searchResults = searchResults;
 	}
 
 	public XMLParser getParser() {
 		return parser;
 	}
 
 }
