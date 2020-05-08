 package org.jboss.hibernateUniversity.criteria.action;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 
 import org.apache.lucene.search.Query;
 import org.jboss.hibernateUniversity.criteria.domain.Address;
 import org.jboss.hibernateUniversity.criteria.domain.Book;
 import org.jboss.hibernateUniversity.criteria.domain.Gender;
 import org.jboss.hibernateUniversity.criteria.domain.User;
 import org.jboss.hibernateUniversity.criteria.tools.Transactional;
 
 import org.hibernate.search.jpa.FullTextEntityManager;
 import org.hibernate.search.jpa.FullTextQuery;
 import org.hibernate.search.query.dsl.QueryBuilder;
 
 /**
  * @author Emmanuel Bernard
  */
 @Transactional
 public class BookManager {
 
 	private Random random = new Random( );
 
 	@Inject
 	Provider<FullTextEntityManager> lazyEM;
 
	//get all books from 1977
 	/**
 	 * Type-safe query
 	 * range query
 	 * object to string conversion
 	 */
 	public void getAllBooksFrom1977() {
 		FullTextEntityManager em = lazyEM.get();
 		final QueryBuilder builder = em.getSearchFactory()
 				.buildQueryBuilder().forEntity( Book.class ).get();
 		Calendar pubDate = Calendar.getInstance();
 		pubDate.set( 1977, 0, 01  );
 		Date begin = new Date( pubDate.getTimeInMillis() );
 		pubDate.set( 1977, 11, 31  );
 		Date end = new Date( pubDate.getTimeInMillis() );
 
 		final Query luceneQuery = builder.range()
 				.onField( "publicationDate" )
 				.from( begin ).to( end )
 				.createQuery();
 
 		System.out.println(luceneQuery.toString()) ;
 
 		final FullTextQuery query = em.createFullTextQuery( luceneQuery, Book.class );
 		final List<Book> resultList = query
 				.setFirstResult( 0 ).setMaxResults( 5 )
 				.getResultList();
 		displayListOfBooks( resultList, query );
 	}
 
 	//get all books strictly above 1000 pages
 	/**
 	 * Type-safe query
 	 * numeric query
 	 */
 	public void getAllBooksStrictlyAbove1000Pages() {
 		FullTextEntityManager em = lazyEM.get();
 		final QueryBuilder builder = em.getSearchFactory()
 				.buildQueryBuilder().forEntity( Book.class ).get();
 
 		final Query luceneQuery = builder.range()
 				.onField( "numberOfPages" )
 				.above( 1000 ).excludeLimit()
 				.createQuery();
 
 		System.out.println(luceneQuery.toString()) ;
 
 		final FullTextQuery query = em.createFullTextQuery( luceneQuery, Book.class );
 		final List<Book> resultList = query
 				.setFirstResult( 0 ).setMaxResults( 5 )
 				.getResultList();
 		displayListOfBooks( resultList, query );
 	}
 
 	//get all book whose author name Emmanuel (Emma etc)
 	/**
 	 * Type-safe query
 	 * Association
 	 * exact match
 	 * wildcard match (lowere case)
 	 *
 	 */
 	public void getAllBooksWhoseAuthorIsEmmanuel() {
 		FullTextEntityManager em = lazyEM.get();
 		final QueryBuilder builder = em.getSearchFactory()
 				.buildQueryBuilder().forEntity( Book.class ).get();
 
 		final Query luceneQuery = builder
 				.keyword()
 				.onField( "author.firstName" )
 				.matching( "Emmanuel" )
 				.createQuery();
 
 //		final Query luceneQuery = builder
 //				.keyword()
 //					.wildcard()
 //				.onField( "author.firstName" )
 //				.matching( "em*" )
 //				.createQuery();
 
 		System.out.println(luceneQuery.toString()) ;
 
 		final FullTextQuery query = em.createFullTextQuery( luceneQuery, Book.class );
 		final List<Book> resultList = query
 				.setFirstResult( 0 ).setMaxResults( 5 )
 				.getResultList();
 		displayListOfBooks( resultList, query );
 	}
 
 	//get all book whose author not named Emmanuel
 	/**
 	 * all except
 	 *
 	 */
 	public void getAllBooksWhoseAuthorIsNotEmmanuel() {
 		FullTextEntityManager em = lazyEM.get();
 		final QueryBuilder builder = em.getSearchFactory()
 				.buildQueryBuilder().forEntity( Book.class ).get();
 
 		final Query luceneQuery = builder
 				.all().except(
 					builder.keyword()
 					.onField( "author.firstName" )
 					.matching( "Emmanuel" )
 					.createQuery()
 				).createQuery();
 
 		System.out.println(luceneQuery.toString()) ;
 
 		final FullTextQuery query = em.createFullTextQuery( luceneQuery, Book.class );
 		final List<Book> resultList = query
 				.setFirstResult( 0 ).setMaxResults( 5 )
 				.getResultList();
 		displayListOfBooks( resultList, query );
 	}
 
 	//get books about plants and hands (title and description)
 	/**
 	 * multi fields
 	 * multi fields different boost
 	 * boolean query
 	 */
	//HSEARCH-634
 	public void getAllBooksOnPlantsAndHands() {
 		FullTextEntityManager em = lazyEM.get();
 		final QueryBuilder builder = em.getSearchFactory()
 				.buildQueryBuilder().forEntity( Book.class ).get();
 
 		final Query luceneQuery =
 			builder
 				.bool()
 					.should( builder
 						.keyword()
 						.onField( "title" ).boostedTo( 4f )
 						.andField( "description" )
 						.matching( "plants" )
 						.createQuery() )
 					.should( builder
 						.keyword()
 						.onField( "title" ).boostedTo( 4f )
 						.andField( "description" )
 						.matching( "hands" )
 						.createQuery() )
 				.createQuery();
 
 		System.out.println(luceneQuery.toString()) ;
 
 		final FullTextQuery query = em.createFullTextQuery( luceneQuery, Book.class );
 		final List<Book> resultList = query
 				.setFirstResult( 0 ).setMaxResults( 5 )
 				.getResultList();
 		displayListOfBooks( resultList, query );
 	}
 
 	//get books about plents and hends (title and description)
 	/**
 	 * Fuzzy
 	 * N-Gram
 	 */
 	public void getAllBooksOnPlentsAndHends() {
 		FullTextEntityManager em = lazyEM.get();
 		final QueryBuilder builder = em.getSearchFactory()
 				.buildQueryBuilder().forEntity( Book.class ).get();
 
 		final Query luceneQuery =
 			builder
 				.bool()
 					.should( builder
 						.keyword()
 							//.fuzzy().withThreshold( .8f )
 						.onField( "title_ngram" ).boostedTo( 4f )
 						.andField( "description_ngram" )
 						.matching( "plents" )
 						.createQuery() )
 					.should( builder
 						.keyword()
 						.onField( "title_ngram" ).boostedTo( 4f )
 						.andField( "description_ngram" )
 						.matching( "hends" )
 						.createQuery() )
 				.createQuery();
 
 		System.out.println(luceneQuery.toString()) ;
 
 		final FullTextQuery query = em.createFullTextQuery( luceneQuery, Book.class );
 		final List<Book> resultList = query
 				.setFirstResult( 0 ).setMaxResults( 5 )
 				.getResultList();
 		displayListOfBooks( resultList, query );
 	}
 
 	//get books starred 5 in the last 10 years
 	/**
	 * range query + boolean (must)
 	 */
 	public void getAllBooksStarred5InTheLast10Years() {
 		FullTextEntityManager em = lazyEM.get();
 		final QueryBuilder builder = em.getSearchFactory()
 				.buildQueryBuilder().forEntity( Book.class ).get();
 
 		final Calendar cal = Calendar.getInstance();
 		cal.setTimeInMillis( System.currentTimeMillis() );
 		cal.add( Calendar.YEAR, -10 );
 
 		final Query luceneQuery =
 			builder
 				.bool()
 					.must( builder
 						.range()
 						.onField( "starred" )
 						.above( 5 )
 						.createQuery() )
 					.must( builder
 						.range()
 						.onField( "publicationDate" )
 						.above( new Date( cal.getTimeInMillis() ) )
 						.createQuery() )
 				.createQuery();
 
 		System.out.println(luceneQuery.toString()) ;
 
 		final FullTextQuery query = em.createFullTextQuery( luceneQuery, Book.class );
 		final List<Book> resultList = query
 				.setFirstResult( 0 ).setMaxResults( 5 )
 				.getResultList();
 		displayListOfBooks( resultList, query );
 	}
 
 	private void displayListOfBooks(List<Book> resultList, FullTextQuery query) {
 		System.out.println( "Number of results: " + query.getResultSize() );
 		for (Book book : resultList) {
 			System.out.println( book.toString() );
 		}
 	}
 
 	@Transactional
 	public void createNewUsers() {
 		final EntityManager entityManager = lazyEM.get();
 		for (int i = 0 ; i < 1000 ; i++) {
 			entityManager.persist( createNewRandomBook() );
 		}
 	}
 
 	private Book createNewRandomBook() {
 		Calendar cal = Calendar.getInstance();
 		cal.set( 19 * 100 + 50 + random.nextInt( 60 ), random.nextInt(12 ), random.nextInt( 28 ) );
 		User user = new User(
 				firstname.get(  random.nextInt( firstname.size() ) ),
 				"Marie" + random.nextInt(),
 				new Date( cal.getTimeInMillis() ),
 				getGender(),
 				random.nextInt( 30 ),
 				"user" + random.nextInt(),
 				"" + random.nextInt()
 		);
 		new Address(
 				random.nextInt() + " rue de la Porte Numero " + random.nextInt(),
 				city.get( random.nextInt( city.size() ) ) ,
 				new Integer(random.nextInt() % 9999).toString(),
 				user );
 		new Address(
 				random.nextInt() + " avenue des Champs " + random.nextInt(),
 				city.get( random.nextInt( city.size() ) ) ,
 				new Integer(random.nextInt() % 9999).toString(),
 				user );
 		new Address(
 				random.nextInt() + " downing street " + random.nextInt(),
 				city.get( random.nextInt( city.size() ) ) ,
 				new Integer(random.nextInt() % 9999).toString(),
 				user );
 		Book book = new Book();
 		book.setAuthor( user );
 		book.setDescription( description.get( random.nextInt( description.size() ) ) );
 		book.setTitle( title.get( random.nextInt( title.size() ) ) );
 		cal.add( Calendar.YEAR, random.nextInt( 60 ) );
 		book.setPublicationDate( new Date( cal.getTimeInMillis() ) );
 		book.setNumberOfPages( random.nextInt( 1000 ) + 30);
 		book.setStarred( random.nextInt( 5 ) + 1 );
 		return book;
 	}
 
 	private Gender getGender() {
 		int i = random.nextInt(10);
 		if ( i < 4 ) {
 			return Gender.MALE;
 		}
 		else if ( i < 8 ) {
 			return Gender.FEMALE;
 		}
 		else {
 			return Gender.NOT_SURE;
 		}
 	}
 
 	private List<String> city = new ArrayList<String>(10);
 	private List<String> description = new ArrayList<String>(10);
 	private List<String> title = new ArrayList<String>(10);
 	private List<String> firstname = new ArrayList<String>(20);
 
 	public BookManager() {
 		city.add( "Paris" );
 		city.add( "New York" );
 		city.add( "Sidney" );
 		city.add( "Anwerpen" );
 		city.add( "Atlanta" );
 
 		title.add("A New End For 'The King's Threshold'");
 		title.add("Abba Thule's Lament For His Son Prince Le Boo");
 		title.add("Accidental Magic; Or Don't Tell All You Know");
 		title.add("Acorn-Planter: A California Forest Play (1916), The");
 		title.add("Adventure Of Hardress Fitzgerald, A Royalist Captain, An");
 		title.add("Cairnsmill Den--Tune: 'A Roving'");
 		title.add("Calumny And Scandal Great Enemies To Society");
 		title.add("Pair Of Hands; An Old Maid's Ghost-Story, A");
 		title.add("Parody Of A Celebrated Letter");
 		title.add("Popular Fallacies");
 
 		
 		description.add("When writing systems were invented in ancient civilizations, nearly everything that could be written uponstone, clay, tree bark, metal sheetswas used for writing. Alphabetic writing emerged in Egypt about 5,000 years ago. The Ancient Egyptians would often write on papyrus, a plant grown along the Nile River. At first the words were not separated from each other (scriptura continua) and there was no punctuation. Texts were written from right to left, left to right, and even so that alternate lines read in opposite directions. The technical term for this type of writing is 'boustrophedon,' which means literally 'ox-turning' for the way a farmer drives an ox to plough his fields.");
 		description.add( "Papyrus scrolls were still dominant in the 1st century AD, as witnessed by the findings in Pompeii. The first written mention of the codex as a form of book is from Martial, in his Apophoreta CLXXXIV at the end of the century, where he praises its compactness. However the codex never gained much popularity in the pagan Hellenistic world, and only within the Christian community did it gain widespread use.[7] This change happened gradually during the 3rd and 4th centuries, and the reasons for adopting the codex form of the book are several: the format is more economical, as both sides of the writing material can be used; and it is portable, searchable, and easy to conceal. The Christian authors may also have wanted to distinguish their writings from the pagan texts written on scrolls." );
 		description.add("The methods used for the printing and binding of books continued fundamentally unchanged from the 15th century into the early years of the 20th century. While there was of course more mechanization, Gutenberg would have had no difficulty in understanding what was going on if he had visited a book printer in 1900.\n" +
 				"Gutenbergs invention was the use of movable metal types, assembled into words, lines, and pages and then printed by letterpress. In letterpress printing ink is spread onto the tops of raised metal type, and is transferred onto a sheet of paper which is pressed against the type. Sheet-fed letterpress printing is still available but tends to be used for collectors books and is now more of an art form than a commercial technique (see Letterpress).");
 		description.add( "When a book is printed the pages are laid out on the plate so that after the printed sheet is folded the pages will be in the correct sequence. [see Imposition] Books tend to be manufactured nowadays in a few standard sizes. The sizes of books are usually specified as trim size: the size of the page after the sheet has been folded and trimmed. Trimming involves cutting approximately 1/8 off top, bottom and fore-edge (the edge opposite to the spine) as part of the binding process in order to remove the folds so that the pages can be opened. The standard sizes result from sheet sizes (therefore machine sizes) which became popular 200 or 300 years ago, and have come to dominate the industry. The basic standard commercial book sizes in America, always expressed as width ? height in USA; some examples are: 4? ? 7 (rack size paperback), 5? ? 7? (digest size paperback), 5? ? 8?, 5? ? 8?, 6? ? 9?, 7 ? 10, and 8? ? 11. These standard trim sizes will often vary slightly depending on the particular printing presses used, and on the imprecision of the trimming operation. Of course other trim sizes are available, and some publishers favor sizes not listed here which they might nominate as standard as well, such as 6 ? 9, 8 ? 10. In Britain the equivalent standard sizes differ slightly, as well as now being expressed in millimeters, and with height preceding width. Thus the UK equivalent of 6? ? 9? is 234 ? 156 mm. British conventions in this regard prevail throughout the English speaking world, except for USA. The European book manufacturing industry works to a completely different set of standards." );
 		description.add( "The sequence of events can vary slightly, and usually the entire sequence does not occur in one continuous pass through a binding line. What has been described above is unsewn binding, now increasingly common. The signatures of a book can also be held together by Smyth sewing. Needles pass through the spine fold of each signature in succession, from the outside to the center of the fold, sewing the pages of the signature together and each signature to its neighbors. McCain sewing, often used in schoolbook binding, involves drilling holes through the entire book and sewing through all the pages from front to back near the spine edge. Both of these methods mean that the folds in the spine of the book will not be ground off in the binding line. This is true of another technique, notch binding, where gashes about an inch long are made at intervals through the fold in the spine of each signature, parallel to the spine direction. In the binding line glue is forced into these notches right to the center of the signature, so that every pair of pages in the signature is bonded to every other one, just as in the Smyth sewn book. The rest of the binding process is similar in all instances. Sewn and notch bound books can be bound as either hardbacks or paperbacks." );
 		description.add( "Most algae are no longer classified within the Kingdom Plantae.[4][5] The algae comprise several different groups of organisms that produce energy through photosynthesis, each of which arose independently from separate non-photosynthetic ancestors. Most conspicuous among the algae are the seaweeds, multicellular algae that may roughly resemble terrestrial plants, but are classified among the green, red, and brown algae. Each of these algal groups also includes various microscopic and single-celled organisms." );
 		description.add("Plant fossils include roots, wood, leaves, seeds, fruit, pollen, spores, phytoliths, and amber (the fossilized resin produced by some plants). Fossil land plants are recorded in terrestrial, lacustrine, fluvial and nearshore marine sediments. Pollen, spores and algae (dinoflagellates and acritarchs) are used for dating sedimentary rock sequences. The remains of fossil plants are not as common as fossil animals, although plant fossils are locally abundant in many regions worldwide.\n" +
 				"The earliest fossils clearly assignable to Kingdom Plantae are fossil green algae from the Cambrian. These fossils resemble calcified multicellular members of the Dasycladales. Earlier Precambrian fossils are known which resemble single-cell green algae, but definitive identity with that group of algae is uncertain.\n" +
 				"The oldest known fossils of embryophytes date from the Ordovician, though such fossils are fragmentary. By the Silurian, fossils of whole plants are preserved, including the lycophyte Baragwanathia longifolia. From the Devonian, detailed fossils of rhyniophytes have been found. Early fossils of these ancient plants show the individual cells within the plant tissue. The Devonian period also saw the evolution of what many believe to be the first modern tree, Archaeopteris. This fern-like tree combined a woody trunk with the fronds of a fern, but produced no seeds.\n" +
 				"The Coal measures are a major source of Paleozoic plant fossils, with many groups of plants in existence at this time. The spoil heaps of coal mines are the best places to collect; coal itself is the remains of fossilised plants, though structural detail of the plant fossils is rarely visible in coal. In the Fossil Forest at Victoria Park in Glasgow, Scotland, the stumps of Lepidodendron trees are found in their original growth positions.\n" +
 				"The fossilized remains of conifer and angiosperm roots, stems and branches may be locally abundant in lake and inshore sedimentary rocks from the Mesozoic and Cenozoic eras. Sequoia and its allies, magnolia, oak, and palms are often found.\n" +
 				"Petrified wood is common in some parts of the world, and is most frequently found in arid or desert areas where it is more readily exposed by erosion. Petrified wood is often heavily silicified (the organic material replaced by silicon dioxide), and the impregnated tissue is often preserved in fine detail. Such specimens may be cut and polished using lapidary equipment. Fossil forests of petrified wood have been found in all continents.\n" +
 				"Fossils of seed ferns such as Glossopteris are widely distributed throughout several continents of the Southern Hemisphere, a fact that gave support to Alfred Wegener's early ideas regarding Continental drift theory.");
 		description.add( "Numerous animals have coevolved with plants. Many animals pollinate flowers in exchange for food in the form of pollen or nectar. Many animals disperse seeds, often by eating fruit and passing the seeds in their feces. Myrmecophytes are plants that have coevolved with ants. The plant provides a home, and sometimes food, for the ants. In exchange, the ants defend the plant from herbivores and sometimes competing plants. Ant wastes provide organic fertilizer.\n" +
 				"The majority of plant species have various kinds of fungi associated with their root systems in a kind of mutualistic symbiosis known as mycorrhiza. The fungi help the plants gain water and mineral nutrients from the soil, while the plant gives the fungi carbohydrates manufactured in photosynthesis. Some plants serve as homes for endophytic fungi that protect the plant from herbivores by producing toxins. The fungal endophyte, Neotyphodium coenophialum, in tall fescue (Festuca arundinacea) does tremendous economic damage to the cattle industry in the U.S.\n" +
 				"Various forms of parasitism are also fairly common among plants, from the semi-parasitic mistletoe that merely takes some nutrients from its host, but still has photosynthetic leaves, to the fully parasitic broomrape and toothwort that acquire all their nutrients through connections to the roots of other plants, and so have no chlorophyll. Some plants, known as myco-heterotrophs, parasitize mycorrhizal fungi, and hence act as epiparasites on other plants.\n" +
 				"Many plants are epiphytes, meaning they grow on other plants, usually trees, without parasitizing them. Epiphytes may indirectly harm their host plant by intercepting mineral nutrients and light that the host would otherwise receive. The weight of large numbers of epiphytes may break tree limbs. Hemiepiphytes like the strangler fig begin as epiphytes but eventually set their own roots and overpower and kill their host. Many orchids, bromeliads, ferns and mosses often grow as epiphytes. Bromeliad epiphytes accumulate water in leaf axils to form phytotelmata, complex aquatic food webs.[23]\n" +
 				"Approximately 630 plants are carnivorous, such as the Venus Flytrap (Dionaea muscipula) and sundew (Drosera species). They trap small animals and digest them to obtain mineral nutrients, especially nitrogen and phosphorus.[24]" );
 		description.add( "All telephones have a microphone to speak into, an earphone which reproduces the voice of the other person, a ringer which makes a sound to alert the owner when a call is coming in, and a keypad (or in older phones a telephone dial) to enter the telephone number of the telephone being called. The microphone and earphone are usually built into a handset which is held up to the face to talk. The keypad may be in the handset or in a separate part. A landline telephone is connected by a wire to the telephone network, while a mobile phone or cell phone is portable and communicates with the telephone network by radio. A cordless telephone has a portable handset which communicates by radio with a base station connected by wire to the telephone network, and can only be used within a limited range of the base station." );
 		description.add( "A traditional landline telephone system, also known as \"plain old telephone service\" (POTS), commonly handles both signaling and audio information on the same twisted pair (C) of insulated wires: the telephone line. The signaling equipment consists of a bell, beeper, light or other device (A7) to alert the user to incoming calls, and number buttons or a rotary dial (A4) to enter a telephone number for outgoing calls. Although originally designed for voice communication, the system has been adapted for data communication such asTelex, Fax and dial-up Internet communication. Most of the expense of wire-lines are the wires, so sending both received and sent voices on one pair of wires reduces the expense of wire-line service. A twisted pair line rejects electromagnetic interference (EMI) and crosstalk better than a single wire or an untwisted pair. The microphone and speaker signals do not interfere on the twisted pair because a hybrid coil (A3) subtracts the microphone's signal from the signal sent to the local speaker. The junction box (B) arrests lightning (B2) and adjusts the line's resistance (B1) to maximize the signal power for the line's length. Telephones have similar adjustments for inside line lengths (A8). The wire's voltages are negative compared to earth, to reduce galvanic corrosion. Negative voltage attracts positive metal ions toward the wires." );
 
 		firstname.add( "Jacob" );
 		firstname.add( "Ethan" );
 		firstname.add( "Michael" );
 		firstname.add( "Alexander" );
 		firstname.add( "William" );
 		firstname.add( "Joshua" );
 		firstname.add( "Daniel" );
 		firstname.add( "Jayden" );
 		firstname.add( "Noah" );
 		firstname.add( "Anthony" );
 		firstname.add( "Isabella" );
 		firstname.add( "Emma" );
 		firstname.add( "Olivia" );
 		firstname.add( "Sophia" );
 		firstname.add( "Ava" );
 		firstname.add( "Emily" );
 		firstname.add( "Madison" );
 		firstname.add( "Abigail" );
 		firstname.add( "Chloe" );
 		firstname.add( "Mia" );
 		firstname.add( "Emmanuel" );
 
 	}
 }
