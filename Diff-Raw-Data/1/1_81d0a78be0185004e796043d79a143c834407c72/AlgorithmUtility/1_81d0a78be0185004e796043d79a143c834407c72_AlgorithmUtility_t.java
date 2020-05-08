 package dhbw.LWBS.CA5_KB1.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.log4j.Logger;
 
 import dhbw.LWBS.CA5_KB1.model.AgeClass;
 import dhbw.LWBS.CA5_KB1.model.Book;
 import dhbw.LWBS.CA5_KB1.model.Children;
 import dhbw.LWBS.CA5_KB1.model.Concept;
 import dhbw.LWBS.CA5_KB1.model.Degree;
 import dhbw.LWBS.CA5_KB1.model.Gender;
 import dhbw.LWBS.CA5_KB1.model.Income;
 import dhbw.LWBS.CA5_KB1.model.Married;
 import dhbw.LWBS.CA5_KB1.model.Person;
 import dhbw.LWBS.CA5_KB1.model.Profession;
 import dhbw.LWBS.CA5_KB1.model.Star;
 
 /**
  * TODO class doc
  * 
  * 
  */
 public class AlgorithmUtility
 {
 	private static final Logger log = Logger.getLogger(AlgorithmUtility.class);
 
 	/**
 	 * The Algorithm starts with a new <code>Set</code> of <code>Concepts</code>
 	 * (also called Hypothesis). After that it starts the iteration over the
 	 * list of <code>positiveExamples</code>. It passes one positive Example
 	 * which is a <code>Person</code> and the list of
 	 * <code>negativeExamples</code> to the method <code>versionSpaceAlgo</code>
 	 * which returns a <code>Star</code>. The generated star together with the
 	 * list of positiveExamples is passed to the method
 	 * <code>bestGeneralization</code> which returns the
 	 * <code>bestConcept</code>. After it is checked whether the
 	 * <code>bestConcept</code> is already contained in the <code>Set</code> of
 	 * <code>Concepts</code> this is returned to the caller of the method.
 	 * 
 	 * @param positiveExamples
 	 *            a list of positive examples of type <code>Person</code>
 	 * @param negativeExamples
 	 *            a list of negative examples of type <code>Person</code>
 	 * @return <code>Set<Concept></code> containing the bestConcept evaluated by
 	 *         the two given lists
 	 */
 	public static Set<Concept> aqAlgo(ArrayList<Person> positiveExamples,
 			ArrayList<Person> negativeExamples)
 	{
 		Set<Concept> k = new HashSet<Concept>();
 		for (Person person : positiveExamples)
 		{
 			// Retrieve a "Star" from the VersionSpace Algorithm
 			log.debug("[BEGIN] VERSION SPACE ALGORITHM \n");
 			Star s = versionSpaceAlgo(person, negativeExamples);
 			log.debug("[ END ] VERSION SPACE ALGORITHM \n");
 			log.info("\nStar: \n" + s);
 
 			// Calculate the "best" concept contained in the "Star"
 			log.debug("[BEGIN] BEST CONCEPT");
 			Concept bestConcept = bestGeneralization(s, positiveExamples);
 			log.debug("[ END ] BEST CONCEPT");
 
 			// Add the Concept to K if there is no Concept in K that 
 			//  already covers the current Concept
 			log.debug("[BEGIN] ADD IFNOTALREADYCOVERED");
 			addBestConceptIfNotAlreadyCovered(k, bestConcept);
 			log.debug("[ END ] ADD IFNOTALREADYCOVERED");
 		}
 
 		return k;
 	}
 
 	/**
 	 * Checks if a given <code>Concept</code> evaluated by the method
 	 * <code>bestGeneralization</code> is already contained in the given
 	 * <code>Set</code> of <code>Concepts</code>. If it is not contained it is
 	 * added to this <code>Set</code> otherwise nothing happens.
 	 * 
 	 * @param k
 	 *            the given <code>Set</code> of <code>Concepts</code> where to
 	 *            look for and add the <code>bestConcept</code>
 	 * @param bestConcept
 	 *            <code>Concept</code> to be evaluated if contained or not
 	 */
 	private static void addBestConceptIfNotAlreadyCovered(Set<Concept> k,
 			Concept bestConcept)
 	{
 		boolean already_covered = false;
 
 		for (Concept c : k)
 		{
 			if (c.covers(bestConcept))
 				already_covered = true;
 		}
 
 		if (!already_covered)
 			k.add(bestConcept);
 	}
 
 	/**
 	 * Initialized two lists S and G. S contains underscores meaning no
 	 * attribute of an example is wanted, G contains stars meaning each
 	 * attribute of an example is wanted. As long as the given list of positive
 	 * examples of type <code>Person</code> contains examples one example after
 	 * another is picked out and list s is generalized. After s is finished it
 	 * iterates over the list of negative examples and every example that is
 	 * contained in S will be deleted from S. If S is empty the algorithm fails,
 	 * otherwise the method goes on. Now G is specialized
 	 * <ol>
 	 * <li>Check if the current <code>Concept</code> of g covers the current
 	 * negative example</li>
 	 * <ul>
 	 * <li>If yes, out of each attribute in S a more specialized concept in G
 	 * must be generated which replaces the old concept</li>
 	 * <li>If not, nothing happens</li>
 	 * </ul>
 	 * <li>Check if G gone empty</li>
 	 * <ul>
 	 * <li>If yes, the algorithm has failed</li>
 	 * <li>If not, program goes on</li>
 	 * </ul>
 	 * <li>Check if S and G are already the same</li>
 	 * <ul>
 	 * <li>If yes, a <code>Star></code> containing S and G is returned</li>
 	 * <li>If not, specializing of G goes on with the next negative example</li>
 	 * </ul>
 	 * 
 	 * If there are no more negative examples the method returns a Star which
 	 * contains S and G <code>Concepts</code>.
 	 * 
 	 * @param positiveExamples
 	 *            a list of positive examples of type <code>Person</code>
 	 * @param negativeExamples
 	 *            a list of negative examples of type <code>Person</code>
 	 * @return <code>Star</code> containing specialized and generalized
 	 *         <code>Concepts</code>, <code>null</code> if list of most
 	 *         specified or most generalized examples runs empty
 	 */
 	public static Star versionSpaceAlgo(ArrayList<Person> positiveExamples,
 			ArrayList<Person> negativeExamples)
 	{
 		// Declarations
 		List<Concept> s = new ArrayList<Concept>(); // Menge der speziellsten Konzepte
 		List<Concept> g = new ArrayList<Concept>(); // Menge der generellsten Konzepte
 		// Menge der noch nicht vorgelegten Beispiele entspricht positiveExamples & negative Examples
 		/*
 		 * Menge der bereits vorgelegten Beispiele muss nicht gefuehrt werden,
 		 * da die for each Schleife jedes Beispiel nur einmal verwendet
 		 */
 
 		// Algorithm
 
 		s.add(Concept.getMostSpecializedConcept()); // s = {(_,...,_)}
 		g.add(Concept.getMostGeneralizedConcept()); // g = {(*,...,*)}
 
 		log.debug("[BEGIN] POSITIVE EXAMPLES");
 
 		// Positive Examples
 		for (Person a : positiveExamples) // equivalent zu "solange bR != leere Menge" & "waehle a aus bR"
 		{
 			log.debug("> VS <positive examples>: Person " + a.toConceptString());
 
 			for (Concept c : s) // ersetze all H aus G mit H(a) = 0 usw.
 			{
 				log.trace(">> VS <positive examples>: Concept " + c + " -> ");
 				generalize(a, c);
 				log.trace(c);
 			}
 		}
 
 		log.debug("[END] POSITIVE EXAMPLES\n");
 		log.debug("[BEGIN] NEGATIVE EXAMPLES");
 
 		// Negative Examples
 		for (Person a : negativeExamples) // equivalent zu "solange bR != leere Menge" & "waehle a aus bR"
 		{
 			log.debug("> VS <negative examples>: Person " + a.toConceptString());
 
 			// Loesche alle Konzepte aus S, die gleich a sind
 			deleteEqualConcepts(s, a);
 
 			// Wenn s leer ist, dann war das letzte negative Beispiel gleich dem letzten Konzept aus s
 			// und der Algorithmus ist fehlgeschlagen
 			if (s.isEmpty())
 			{
 				log.info("> VS algorithm terminated without success because of drained \"S\"");
 				return null;
 			}
 
 			// Ersetze alle H e G, die a als Beispiel haben, durch die allgemeinste
 			// Spezialisierung von H, die a nicht enhaelt (jedoch alle bisher vorgelegten
 			// positiven Beispiele!)
 
 			log.trace(">> [BEGIN] specializing");
 
 			Set<Concept> toBeInserted_g = new TreeSet<Concept>();
 			Set<Concept> toBeDeleted_g = new TreeSet<Concept>();
 
 			log.trace(">>> Iterating over concepts of \"G\"");
 
 			for (Concept cG : g)
 			{
 				log.trace(">>>> VS <concept of g>: " + cG);
 
 				// Wenn das Beispiel a Teilmenge des aktuellen Konzepts cG ist
 				if (cG.covers(a))
 				{
 					log.debug(">>>> " + cG + " covers " + a.toConceptString());
 
 					// von jedem Attribut aus s muss ein neues Konzept in g (toBeInserted) erstellt werden
 					// bsp.: s={(a,b)} g={(a,*),(*,b)}
 
 					for (String key : a.getAttributes().keySet())
 					{
 						int a_Attribute = a.getAttributes().get(key);
 						int s_Attribute = s.get(0).getAttributes().get(key);
 
 						if ((a_Attribute != s_Attribute)
 								|| (s_Attribute != 100)) //ALL (*) = 100
 						{
 							Concept c = cG.copy();
 							c.setAttribute(key, s_Attribute);
 							toBeInserted_g.add(c);
 
 							log.trace(">>>>> created new concept-to-be-inserted: "
 									+ c);
 						}
 					}
 
 					// bisherige Konzepte aus g müssen gelöscht werden
 					log.trace(">>>>> " + cG + " has been marked for deletion");
 					toBeDeleted_g.add(cG);
 				}
 				else
 				{
 					log.debug(">>>> " + cG + " does not cover "
 							+ a.toConceptString());
 				}
 
 				log.trace(">>>> concepts marked for deletion: " + toBeDeleted_g);
 				log.trace(">>>> concepts created: " + toBeInserted_g);
 
 			}
 
 			log.trace(">>>> [BEGIN] deletion of marked concepts from \"G\": "
 					+ g);
 			for (Concept rG : toBeDeleted_g)
 			{
 				g.remove(rG);
 			}
 			log.trace(">>>> [END] deletion of marked concepts from \"G\": " + g
 					+ "\n");
 
 			log.trace(">>>> [BEGIN] inserting of new concepts to \"G\": " + g);
 
 			//TODO schöner machen :)
 			nextConcept: for (Concept iC : toBeInserted_g)
 			{
 				for (Concept iG : g)
 				{
 					if (iC.covers(iG))
 					{
 						log.trace(">>>>> " + iC + " covers " + iG
 								+ " -> new concept is not added to \"G\"");
 						continue nextConcept;
 					}
 					else
 					{
 						log.trace(">>>>> " + iC + " does not cover " + iG);
 					}
 				}
 
 				log.trace(">>>>> added " + iC + " to \"G\"");
 				g.add(iC);
 			}
 			log.trace(">>>> [END] inserting of new concepts to \"G\": " + g
 					+ "\n");
 
 			// Wenn g leer ist, dann ist der Algorithmus fehlgeschlagen
 			if (g.isEmpty())
 			{
 				log.info("> VS algorithm terminated without success because of drained \"G\"");
 				return null;
 			}
 
 			if (s.equals(g))
 			{
 				log.info("> VS algorithm is terminating successfully because of equal \"S\" and \"G\" (concept learned)");
 				return new Star(s, g);
 			}
 
 		}
 		log.trace(">> [END] specializing");
 
 		log.info("> VS algorithm is terminating successfully (no more examples to learn)");
 		return new Star(s, g);
 	}
 
 	/**
 	 * Matches every Concept in the List with the given Person example and
 	 * removes it if they match
 	 * 
 	 * @param s
 	 *            the List of Concepts which should be cleared from the given
 	 *            Person example
 	 * @param a
 	 *            the given Person example
 	 */
 	private static void deleteEqualConcepts(List<Concept> s, Person a)
 	{
 		log.trace(">> [BEGIN] deleting equal concepts");
 		for (Concept cS : s)
 		{
 			if (cS.equals(a))
 			{
 				s.remove(cS);
 				log.trace(">>> removing Concept " + cS);
 			}
 		}
 		log.trace(">> [END] deleting equal concepts");
 	}
 
 	/**
 	 * One positive example of type <code>Person</code> which is added to a list
 	 * and a list of negative examples are passed on to this overloaded method.
 	 * It then returns a Star which contains specialized and generalized
 	 * <code>Concepts</code>.
 	 * 
 	 * @param posExample
 	 *            a list of positive examples of type <code>Person</code>
 	 * @param negExamples
 	 *            a list of negative examples of type <code>Person</code>
 	 * @return <code>Star</code> containing specialized and generalized
 	 *         <code>Concepts</code>, <code>null</code> if list of most
 	 *         specified or most generalized examples runs empty
 	 */
 	private static Star versionSpaceAlgo(Person posExample,
 			ArrayList<Person> negExamples)
 	{
 		ArrayList<Person> posExamples = new ArrayList<Person>();
 		posExamples.add(posExample);
 		return versionSpaceAlgo(posExamples, negExamples);
 	}
 
 	/**
 	 * First proofs if the given Star contains generalized <code>Concepts</code>
 	 * if not no best <code>Concept</code> can be found and <code>null</code> is
 	 * returned. If this is not the case it is evaluated if each
 	 * <code>Concept</code> of the given <code>Star</code> coveres each positive
 	 * example. A local variable counts the covered examples for one
 	 * <code>Concept</code>. If this number is higher than the one of the
 	 * previous <code>Concept</code> this is now the best <code>Concept</code>.
 	 * The last found <code>Concept</code> with the highest number of covered
 	 * positive examples is returned as best <code>Concept</code>.
 	 * 
 	 * 
 	 * @param s
 	 *            the <code>Star</code> which should be evalutated for the best
 	 *            generalization
 	 * @param positiveExamples
 	 *            the given list with positive examples of type
 	 *            <code>Person</code>
 	 * @return <code>Concept</code> which covers the most positive examples,
 	 *         <code>null</code> if the given Star has no generalized concepts
 	 */
 	public static Concept bestGeneralization(Star s,
 			ArrayList<Person> positiveExamples)
 	{
 		// check for empty generalized concept set
 		if ((s.getGeneralizedConcepts() == null)
 				|| (s.getGeneralizedConcepts().isEmpty()))
 		{
 			log.info("The given Star does not contain any generalized Concepts so there is no best concept.");
 			return null;
 		}
 
 		// find the concept that matches most of the given positive examples
 		int found = -1; // number of found matches (has to be initialized with -1 because
 		//  even the first concept that matches 0 examples is the best here
 		Concept best = null; // currently best concept
 
 		for (Concept c : s.getGeneralizedConcepts())
 		{
 			int found_local = 0; // the matches for this Concept
 
 			// try to match the current concept with every given example
 			for (Person p : positiveExamples)
 			{
 				if (c.covers(p))
 					found_local++;
 			}
 
 			// the concept is better than the last best if the number of found matches is higher
 			if (found_local > found)
 			{
 				found = found_local;
 				best = c;
 			}
 		}
 
 		log.info("Best concept of current Star:" + best);
 
 		return best;
 	}
 
 	/**
 	 * Calculates the possibility which <code>Book</code> the given
 	 * <code>Person</code> will pick according to the learned
 	 * <code>Concepts</code>.
 	 * 
 	 * @param p
 	 *            <code>Person</code> to be analysed
 	 * @param booksConcepts
 	 *            all books and their learned <code>Concepts</code>
 	 * @return a list of possible books
 	 */
 	public static List<Book> guessTheBook(Person p,
 			HashMap<Book, Set<Concept>> booksConcepts)
 	{
 		log.info("Guessing Book for Person: " + p.toConceptString());
 
 		int conceptsTotal = 0; // holds the total number of all concepts for all books
 		HashMap<Book, Double> matches = new HashMap<Book, Double>(); // holds the number of matches for each book
 		List<Book> possibleBooks = new ArrayList<Book>(); // holds all books that match more than 0.0%
 
 		// calculate the possibilities
 		for (Book b : booksConcepts.keySet())
 		{
 			// add the number of concepts of the current book to the 
 			//  total number of concepts
 			conceptsTotal += booksConcepts.get(b).size();
 
 			// retrieve the number of matches for the current book
 			double matchesForBook = getPossibleMatches(p, booksConcepts.get(b));
 
 			// calculate the LOCAL percental match
 			double matchPercent = calculatePercentage(matchesForBook, booksConcepts.get(b).size());
 			log.debug("Book \"" + b + "\": " + matchPercent + "%");
 
 			if (matchPercent > 0.0)
 			{
 				// add the matching book to the return list
 				possibleBooks.add(b);
 			}
 
 			matches.put(b, matchPercent);
 		}
 
 		double maxMatches = -1; // holds the maximum number of matches found in the map
 		Book currentMostPossibleBook = null; // the book to be returned 
 
 		// find the highest match
 		for (Book b : matches.keySet())
 		{
 			// if the number of matches for the current book is larger
 			//  than the current number of matches, it is the currently 
 			//  most possible book to be taken
 			if (matches.get(b) > maxMatches)
 			{
 				log.trace("max " + maxMatches + " < " + matches.get(b)
 						+ " (was " + currentMostPossibleBook + ")");
 				maxMatches = matches.get(b);
 				currentMostPossibleBook = b;
 				log.trace("max changed to :" + maxMatches + "(is now "
 						+ currentMostPossibleBook + ")");
 			}
 		}
 
 		double matchPercent = calculatePercentage(maxMatches, conceptsTotal);
 		log.info("Book \"" + currentMostPossibleBook + "\" is the most "
 				+ "possible choice for Person " + p
 				+ " with an overall match of " + matchPercent + "%.");
 
 		return possibleBooks;
 	}
 
 	/**
 	 * Returns the number of matches between the given <code>Person</code> and
 	 * the given book <code>Concepts</code>
 	 * 
 	 * @param p
 	 *            example of type <code>Person</code> that has to be matched
 	 * @param bookConcepts
 	 *            containing all concepts of one book
 	 * @return number of matches
 	 */
 	private static int getPossibleMatches(Person p, Set<Concept> bookConcepts)
 	{
 		int matches = 0;
 		for (Concept c : bookConcepts)
 		{
 			if (c.covers(p))
 				matches++;
 		}
 		return matches;
 	}
 
 	// HELPER METHODS
 	/**
 	 * Compares all attributes of the given <code>Concept</code> to their
 	 * according enum types. If an attribute wasn't spezialised before the
 	 * compatible attribute of the given <code>Person</code> is set in the
 	 * <code>Concept</code>. If a <code>Person</code>'s attribute is unequal to
 	 * the compatible <code>Concept</code>'s attribute this is set to a star
 	 * meaning that it is generalized.
 	 * 
 	 * @param p
 	 *            <code>Person</code> which is compared to the
 	 *            <code>Concept</code>
 	 * @param c
 	 *            <code>Concept</code> where to set values
 	 */
 	private static void generalize(Person p, Concept c)
 	{
 		// generalize ageclass
 		if (c.getAgeClass() == AgeClass.NONE)
 			c.setAgeClass(p.getAgeClass());
 		else if (p.getAgeClass() != c.getAgeClass())
 			c.setAgeClass(AgeClass.ALL);
 
 		// generalize gender
 		if (c.getGender() == Gender.NONE)
 			c.setGender(p.getGender());
 		else if (p.getGender() != c.getGender())
 			c.setGender(Gender.ALL);
 
 		// generalize married
 		if (c.getMarried() == Married.NONE)
 			c.setMarried(p.getMarried());
 		else if (p.getMarried() != c.getMarried())
 			c.setMarried(Married.ALL);
 
 		// generalize children
 		if (c.getChildren() == Children.NONE)
 			c.setChildren(p.getChildren());
 		else if (p.getChildren() != c.getChildren())
 			c.setChildren(Children.ALL);
 
 		// generalize degree
 		if (c.getDegree() == Degree.NONE)
 			c.setDegree(p.getDegree());
 		else if (p.getDegree() != c.getDegree())
 			c.setDegree(Degree.ALL);
 
 		// generalize profession
 		if (c.getProfession() == Profession.NONE)
 			c.setProfession(p.getProfession());
 		else if (p.getProfession() != c.getProfession())
 			c.setProfession(Profession.ALL);
 
 		// generalize income
 		if (c.getIncome() == Income.NONE)
 			c.setIncome(p.getIncome());
 		else if (p.getIncome() != c.getIncome())
 			c.setIncome(Income.ALL);
 	}
 
 	/**
 	 * Calculated the percentage of matches with the calculated number of
 	 * <code>getPossibleMatches</code> and the total number of
 	 * <code>Concepts</code>
 	 * 
 	 * @param matchesForBook
 	 *            number of matches for a book
 	 * @param total
 	 *            number of <code>Concepts</code>
 	 * @return matches for one book in percentage
 	 */
 	private static double calculatePercentage(double matchesForBook, int total)
 	{
 		if (total == 0)
 			return 0;
 		else
 			return matchesForBook / (total / 100.0);
 	}
 }
