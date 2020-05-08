 import java.util.Scanner;
 
 /**********************************************
  * 
  * 25.11.2011
  * 
  * @author miksa
  * 
  */
 public class Bookshelf {
 	/**
 	 * Tietokanta ilmentyma. Tietokanta maaritellaan main metodissa.
 	 */
 	private static Tietokanta tk;
 	/**
 	 * Scanner luokan kayttaanotto
 	 */
 	public static Scanner lukija = new Scanner(System.in);
 	private static Bookshelf kirjahylly;
 	/**
 	 * 
 	 * @param schema
 	 * @param user
 	 * @param pass
 	 */
 	public Bookshelf(String schema, String user, String pass) {
 		try {
 			tk = new Tietokanta();
 			tk.createConnection(schema, user, pass);
 
 			Naytto toosa = new Naytto(kirjahylly);
 			toosa.setVisible(true);
 
 			// start configuration
 			// Should I remove these?
 			// tk.poistaBookTaulu();
 			// tk.luoBookTaulu();
 			// tk.poistaWriterTaulu();
 			// tk.luoWriterTaulu();
 		} catch (Exception e) {
 			System.out.println("Bookshelf: error");
 		}
 	}
 
 	/**
 	 * main metodi sisaltaa kayttaliittyman
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		kirjahylly = new Bookshelf("Bookshelf", "librarian",
 				"salasana");
 		boolean lukitus = true;
 		//
 //		Naytto toosa = new Naytto(kirjahylly);
 //		toosa.setVisible(true);
 
 		// testaus
 
 		// testaus Loppui
 
 		try {
 			//NO consol UI anymore
 			int valinta = 0;// meneeko silmukkaan??
 			while (valinta != 0) {
 				System.out.println("**Bookshelf**");
 				System.out.println("11) Add book");
 
 				System.out.println("12) Print books");
 
 				System.out.println("21) Add writer");
 				System.out.println("22) Print writers");
 
 				System.out.println("51) Print books ordered by writers");
 				// Not implemented
 
 				// System.out.println("3) Find book");
 				// System.out.println("4) Muuta hintoja");
 				// System.out.println("5) Tulosta asiakkaat ja tuotteet");
 				// System.out.println("6) Tulosta tuotteet");
 				// System.out.println("7) Muuta tuotteen hintaa");
 				System.out.println("90) Admin lukitus");
 				System.out.println("91) LuoTaulut(Books, Writers)");
 				System.out.println("92) PudotaTaulut(Books, Writers )");
 				System.out.println("\n0) Lopeta");
 
 				valinta = Integer.parseInt(lukija.nextLine());
 				switch (valinta) {
 				case 11:
 
 					String nimi;
 
					int ano;
 					System.out.println("Anna kirjan nimi! ");
 					nimi = lukija.nextLine();
 					System.out.println("Anna hinta! ");
 					float price = lukija.nextFloat();
 					lukija.nextLine();
 					tk.printWriters();
 					System.out.println("anna kirjailijan numero! ");
 					int wno = Integer.parseInt(lukija.nextLine());
 
 					String isbn1 = " ";
 					System.out.println("anna vuosiluku! ");
 					int year = Integer.parseInt(lukija.nextLine());
 
 					addBook(nimi, isbn1, year, price, wno);
 					break;
 				case 12:
 					tk.printBooks();
 					break;
 				// add writer
 				case 21:
 
 					String enimi;
 					String snimi;
 
 					System.out.println("Anna kirjailijan etunimi! ");
 					enimi = lukija.nextLine();
 
 					System.out.println("Anna kirjailijan sukunimi! ");
 					snimi = lukija.nextLine();
 
 					addWriter(enimi, snimi);
 					break;
 				case 22:
 					tk.printWriters();
 					break;
 
 				case 3:
 					System.out.println("Anna asiakas numero!");
 					int asno = lukija.nextInt();
 					lukija.nextLine();
 					System.out.println(tk.etsiAsiakasNumero(asno));
 					break;
 				case 4:
 					System.out
 							.println("Anna hinnoille muutosprosentti desimaalilukuna ( 0.5 = 50% )");
 					float korotus = lukija.nextFloat();
 					lukija.nextLine();
 					tk.modifyPrices(korotus);
 					break;
 				case 51:
 					tk.printBooksOrderedByWriter();
 					break;
 				case 6:
 					tk.tulostaTuotteet();
 					break;
 				case 7:
 					System.out.println("Anna muutettavan tuotteen numero!");
 					int tid = lukija.nextInt();
 					lukija.nextLine();
 					System.out.println("Anna tuotteelle uusihinta");
 					float uusiHinta = lukija.nextFloat();
 					lukija.nextLine();
 					tk.muutaTuotteenHinta(tid, uusiHinta);
 					break;
 				case 90:
 					System.out.println("Lukituksen poistu K/E");
 					char lukko = lukija.nextLine().charAt(0);
 					if (lukko == 'K') {
 						System.out.println("Lukitus poistettu");
 						lukitus = false;
 					} else if (lukitus) {
 						System.out.println("Lukitus päällä");
 						lukitus = true;
 					}
 
 					break;
 				case 91:
 					tk.luoBookTaulu();
 					tk.luoWriterTaulu();
 					break;
 				case 92:
 					tk.poistaBookTaulu();
 					tk.poistaWriterTaulu();
 					break;
 
 				case 0:
 					break;
 				default:
 					System.out.println("valitse uudelleen! ");
 				}
 			}
 			// tk.tulostaAsiakkaat();
 			// tk .tulostaAsiakkaanTuotteet();
 			// tk.suljeYhteys();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("virhe");
 		}
 	}
 
 	public static void addBook(String nimi, String isbn1, int year,
 			float price, int wno) {
 		try {
 			tk.addBook(nimi, isbn1, year, price, wno);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("addBook:virhe");
 		}
 	}
 
 	public static void addWriter(String ename, String sname) {
 		try {
 
 			tk.addWriter(ename, sname);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("addWriter:virhe");
 		}
 	}
 
 	public static String[] getWriters() {
 		try {
 			return tk.getWriters();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("getWriters:virhe");
 		}
 		return null;
 
 	}
 	public static String[] getBooks() {
 		try {
 			return tk.getBooks();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("Bookshelf.getBooks:error");
 		}
 		return null;
 
 	}
 	public static void suljeYhteys() {
 		System.out.println("");
 		tk.suljeYhteys();
 	}
 }
