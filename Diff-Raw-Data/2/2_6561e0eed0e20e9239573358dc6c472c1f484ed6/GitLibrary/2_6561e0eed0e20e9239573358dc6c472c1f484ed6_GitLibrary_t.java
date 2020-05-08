 package de.syngenio.lib;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import de.syngenio.lib.dao.BookDao;
 import de.syngenio.lib.domainobject.Book;
 import de.syngenio.lib.io.CharacterReader;
 import de.syngenio.lib.service.BookCreateService;
 import de.syngenio.lib.service.BookDeletionService;
 import de.syngenio.lib.service.BookEditService;
 import de.syngenio.lib.service.BookRentService;
 import de.syngenio.lib.service.BookReturnService;
 import de.syngenio.lib.service.BookShowService;
 import de.syngenio.lib.service.DoNothingService;
 import de.syngenio.lib.service.IMenuChoiceService;
 
 public class GitLibrary {
 
 	BookDao bookDao = new BookDao();
 
 	CharacterReader characterReader = new CharacterReader();
 
 
 	private Map<Integer, IMenuChoiceService> serviceHandlingChoice = new HashMap<Integer, IMenuChoiceService>();
 
 	private BookShowService   bookShowService      = new BookShowService();
 	private BookCreateService bookCreationService  = new BookCreateService();
 	private BookEditService   bookEditService      = new BookEditService();
 
 	private IMenuChoiceService bookDeletionService = new BookDeletionService();
 	private IMenuChoiceService bookRentService     = new BookRentService();
 	private IMenuChoiceService bookReturnService   = new BookReturnService();
 
 	public GitLibrary() {
 		bookShowService.setBookDao(new BookDao());
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new GitLibrary().start();
 	}
 
 	public void start() {
 		createBook("Harry Potter", "9983-78978");
		createBook("Illuminati", "666666-666");
 		printOptions();
 		System.out.println("Goodbye");
 	}
 
 	private void printOptions() {
 		int option = 0;
 		while (option != 7) {
 			printOption(1, "Bücher anzeigen", bookShowService);
 			printOption(2, "Neues Buch anlegen", bookCreationService);
 			printOption(3, "Buch löschen", bookDeletionService);
 			printOption(4, "Buch bearbeiten", bookEditService);
 			printOption(5, "Buch ausleihen", bookRentService);
 			printOption(6, "Buch zurück geben", bookReturnService);
 			printOption(7, "Git lib beenden", new DoNothingService());
 			option = CharacterReader.readIntegerFromConsole();
 			serviceHandlingChoice.get(option).optionSelected();
 		}
 		
 
 	}
 
 	private void printOption(int i, String string, IMenuChoiceService menuChoiceService) {
 		System.out.println("(" + i + ") " + string);
 		serviceHandlingChoice.put(i, menuChoiceService);
 	}
 
 	public void createBook(String nameOfBook, String isbn) {
 		Book bookToSave = new Book();
 		bookToSave.setName(nameOfBook);
 		bookToSave.setIsbn(isbn);
 		bookDao.saveBook(bookToSave);
 	}
 
 }
