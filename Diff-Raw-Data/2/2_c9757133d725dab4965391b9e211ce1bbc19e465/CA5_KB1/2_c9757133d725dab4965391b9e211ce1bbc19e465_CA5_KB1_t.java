 package dhbw.LWBS.CA5_KB1.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.collections.ListUtils;
 import org.apache.log4j.PropertyConfigurator;
 
 import dhbw.LWBS.CA5_KB1.model.Book;
 import dhbw.LWBS.CA5_KB1.model.Concept;
 import dhbw.LWBS.CA5_KB1.model.Person;
 
 /**
  * Main class of the program. Contains the program flow.
  */
 public class CA5_KB1
 {
 	/**
 	 * Starts the program
 	 * <ol>
 	 * <li>Read in the training data</li>
 	 * <li>Fill example sets for each book</li>
 	 * <li>Do the AQ-Algorithm for Book A, Book B and Book C</li>
 	 * <li>Show learned <code>Concepts</code> for Book A, Book B and Book C</li>
 	 * <li>Read in proof data</li>
 	 * <ul>
 	 * <li>Either through a .csv file</li>
 	 * <li>If this is not given, program enters interactive mode</li>
 	 * </ul>
 	 * <li>Show the guessed result</li>
 	 * </ol>
 	 * 
 	 * @param args
 	 *            command line arguments which contain the file name of training
 	 *            data as first argument and the file name of proof data (if
 	 *            there are some) as second argument
 	 */
 	public static void main(String[] args)
 	{
 		// configure log4j
 		PropertyConfigurator.configure("log4j.properties");
 
 		String trainingData = "gruppe_ca5_kb1.csv";
 		String proofData = null;
 
 		switch (args.length)
 		{
 		case 2:
 			proofData = args[1];
 		case 1:
 			trainingData = args[0];
 			break;
 		}
 
 		ArrayList<Person> trainingPersons;
 		ArrayList<Person> proofPersons;
 		ArrayList<Person> bookA = new ArrayList<Person>();
 		ArrayList<Person> bookB = new ArrayList<Person>();
 		ArrayList<Person> bookC = new ArrayList<Person>();
 		HashMap<Book, Set<Concept>> booksConcepts = new HashMap<Book, Set<Concept>>();
 
 		System.out.print("Reading data for training from: " + trainingData
 				+ " ..");
 		trainingPersons = importPersons(trainingData);
 		System.out.println(" done\n");
 		System.out.print("Populating example sets for each book ..");
 		populateBookLists(trainingPersons, bookA, bookB, bookC);
 		System.out.println(" done\n");
 
 		System.out.println("[ENTERING AQ ALGORITHM FOR BOOK A]\n");
 		booksConcepts.put(Book.BOOK_A, AlgorithmUtility.aqAlgo(bookA, mergeLists(bookB, bookC)));
 		System.out.println("[FINISHED AQ ALGORITHM FOR BOOK A]\n\n");
 
 		System.out.println("[ENTERING AQ ALGORITHM FOR BOOK B]\n");
 		booksConcepts.put(Book.BOOK_B, AlgorithmUtility.aqAlgo(bookB, mergeLists(bookA, bookC)));
 		System.out.println("[FINISHED AQ ALGORITHM FOR BOOK B]\n\n");
 
 		System.out.println("[ENTERING AQ ALGORITHM FOR BOOK C]\n");
 		booksConcepts.put(Book.BOOK_C, AlgorithmUtility.aqAlgo(bookC, mergeLists(bookA, bookB)));
 		System.out.println("[FINISHED AQ ALGORITHM FOR BOOK C]\n\n");
 
 		System.out.println("[CONCEPTS FOR BOOK A]:\n"
 				+ booksConcepts.get(Book.BOOK_A) + "\n");
 		System.out.println("[CONCEPTS FOR BOOK B]:\n"
 				+ booksConcepts.get(Book.BOOK_B) + "\n");
 		System.out.println("[CONCEPTS FOR BOOK C]:\n"
 				+ booksConcepts.get(Book.BOOK_C) + "\n");
 
 		System.out.println("[ LEARNING COMPLETED ]\n");
 
 		if (proofData != null)
 		{
 			proofPersons = importPersons(proofData);
 			for (Person person : proofPersons)
 			{
 				getResultForTestData(booksConcepts, person);
 			}
 
 			return;
 		}
 		else
 		{
 			System.out.println("No proof data given, continuing to interactive mode");
 
 			ConceptConsole c = new ConceptConsole();
 
 			do
 			{
 				Person person = c.readConcept();
 				getResultForTestData(booksConcepts, person);
 			} while (c.continueTesting());
 		}
 	}
 
 	/**
 	 * Calls the method <code>guessTheBook</code> in order to get the possible
 	 * choices of the given <code>Person</code> for the given bookConcepts.
 	 * Additionally it generates some nice output to represent the result(s).
 	 * 
 	 * @param booksConcepts
 	 *            containing all concepts of one book
 	 * @param person
 	 *            example that is compared
 	 */
 	private static void getResultForTestData(
 			HashMap<Book, Set<Concept>> booksConcepts, Person person)
 	{
 		// guess the book
 		List<Book> results = AlgorithmUtility.guessTheBook(person, booksConcepts);
 
 		// generate some nice output to represent the result(s)
 		System.out.print("Proof " + person.getNumber() + " ("
 				+ person.getBook() + "): ");
 
 		if (results.size() == 0)
			System.out.print("No Book matched.");
 		else
 		{
 			System.out.print(results.get(0));
 
 			for (int i = 1; i < results.size(); i++)
 				System.out.print(" or " + results.get(i));
 		}
 
 		System.out.println();
 	}
 
 	/**
 	 * Appends two given lists and returns this as a new list.
 	 * 
 	 * @param a
 	 *            list containing <code>Persons</code>
 	 * @param b
 	 *            list containing <code>Persons</code>
 	 * @return concatenated list containing <code>Persons</code>
 	 */
 	@SuppressWarnings("unchecked")
 	private static ArrayList<Person> mergeLists(ArrayList<Person> a,
 			ArrayList<Person> b)
 	{
 		return new ArrayList<Person>(ListUtils.union(a, b));
 	}
 
 	/**
 	 * Fills the given lists with the examples of the first list according to
 	 * the book's type. Example: Person example that has chosen Book A. This
 	 * example will be filled into the first given list called bookA.
 	 * 
 	 * @param allPersons
 	 *            list containing all <code>Person</code> examples
 	 * @param bookA
 	 *            list to be filled with examples of type <code>Person</code>
 	 * @param bookB
 	 *            list to be filled with examples of type <code>Person</code>
 	 * @param bookC
 	 *            list to be filled with examples of type <code>Person</code>
 	 */
 	private static void populateBookLists(ArrayList<Person> allPersons,
 			ArrayList<Person> bookA, ArrayList<Person> bookB,
 			ArrayList<Person> bookC)
 	{
 		for (Person person : allPersons)
 		{
 			switch (person.getBook())
 			{
 			case BOOK_A:
 				bookA.add(person);
 				break;
 
 			case BOOK_B:
 				bookB.add(person);
 				break;
 
 			case BOOK_C:
 				bookC.add(person);
 				break;
 			}
 		}
 		System.out.print(" A: " + bookA.size());
 		System.out.print(" B: " + bookB.size());
 		System.out.print(" C: " + bookC.size());
 	}
 
 	/**
 	 * Fills a list with examples of type <code>Person</code> from the specified
 	 * file and returns it afterwards.
 	 * 
 	 * @param fileName
 	 *            name of the file in which the data is found
 	 * @return list containing examples of type <code>Person</code>
 	 */
 	private static ArrayList<Person> importPersons(String fileName)
 	{
 		String[] currentPerson = null;
 		ArrayList<Person> p = new ArrayList<Person>();
 		CSVUtility readFile = new CSVUtility(fileName);
 		while ((currentPerson = readFile.getPerson()) != null)
 		{
 			p.add(new Person(currentPerson));
 		}
 		System.out.println(p.size());
 		return p;
 	}
 
 }
