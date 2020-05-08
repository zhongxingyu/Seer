 package fi.helsinki.cs.nero.db;
 
 import fi.helsinki.cs.nero.NeroApplication;
 import fi.helsinki.cs.nero.data.Contract;
 import fi.helsinki.cs.nero.data.Kannykka;
 import fi.helsinki.cs.nero.data.Person;
 import fi.helsinki.cs.nero.data.PhoneNumber;
 import fi.helsinki.cs.nero.data.Post;
 import fi.helsinki.cs.nero.data.Project;
 import fi.helsinki.cs.nero.data.Reservation;
 import fi.helsinki.cs.nero.data.Room;
 import fi.helsinki.cs.nero.data.RoomKeyReservation;
 import fi.helsinki.cs.nero.data.TimeSlice;
 import fi.helsinki.cs.nero.event.NeroObserver;
 import fi.helsinki.cs.nero.event.NeroObserverTypes;
 import fi.helsinki.cs.nero.logic.Session;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Jyrki Muukkonen
  */
 public class NeroDatabase implements NeroObserver {
 
     /**
      * Tietokantayhteys
      */
     private Connection connection;
     /**
      * Session johon t�m� tietokantayhteys liittyy.
      */
     private Session session;
     /**
      * Kaikki j�rjestelm�n tuntemat huoneet ty�pisteineen ja
      * puhelinnumeroineen. Hajautusrakenne, jossa avaimena on huoneen id ja
      * arvona huoneolio.
      */
     private Map rooms;
     /**
      * Kaikki j�rjestelm�n tuntemat projektit. Hajautusrakenne, jossa
      * avaimena on projektin id ja arvona projektiolio.
      */
     private Map projects;
     /**
      * Kaikki j�rjestelm�n tuntemat ty�pisteet. Hajautusrakenne, jossa
      * avaimena on ty�pisteen id ja arvona ty�pisteolio.
      */
     private Map posts;
     /**
      * Kaikki j�rjestelm�n tuntemat puhelinnumerot. Hajautusrakenne, jossa
      * avaimena on ty�pisteen id (tai "free") ja arvona vektori
      * puhelinnumeroista
      */
     private Map phoneNumbers;
     /**
      * J�rjestelm�n tuntemia henkil�it�, jotka on jo ladattu
      * tietokannasta. Hajautusrakenne, jossa avaimena on henkil�n tunniste
      * (htunnus) ja arvona henkil�olio.
      */
     private Map people = new Hashtable();
     /**
      * Kaikki jrjestelmn tuntemat avainvaraukset
      */
     private ArrayList<RoomKeyReservation> roomKeyReservations;
     /**
      * Tietojen esilataamisessa k�ytetyt preparedStatementit
      */
     private PreparedStatement prepAllRooms;
     private PreparedStatement prepAllPosts;
     private PreparedStatement prepAllPhoneNumbers;
     /**
      * getReservations(Person person, TimeSlice timeslice)-metodin
      * k�ytt�m�t PreparedStatementit.
      */
     private PreparedStatement prepPersonReservations;
     /**
      * getRerservations(Post post, TimeSlice timeslice)-metodin k�ytt�m�t
      * PreparedStatementit.
      */
     private PreparedStatement prepPostReservations;
     /**
      * getPersons()-metodin k�ytt�m�t PreparedStatementit.
      */
     private PreparedStatement prepPersonContracts;
     /**
      * henkiltietohakuun kytettv PreparedStatement.
      */
     private PreparedStatement prepPersonInfo;
     /**
      * getRooms(Project project, TimeSlice timescale)-metodin k�ytt�m�t
      * PreparedStatementit.
      */
     private PreparedStatement prepProjectRooms;
     /**
      * getRooms(String roomName, int maxPosts)-metodin k�ytt�m�t
      * PreparedStatementit.
      */
     private PreparedStatement prepNamedRoomsNormal, prepNamedRoomsWithMaxPosts;
     /**
      * Varausten k�sittelyss� tarvittavat PreparedStatementit
      */
     private PreparedStatement prepNextReservationID;
     private PreparedStatement prepAddReservation;
     private PreparedStatement prepUpdateReservation;
     private PreparedStatement prepDeleteReservation;
     /**
      * getPeople()-metodissa k�ytetyt PreparedStatementit talletetaan
      * hajautukseen, jossa avaimena on kyselyn SQL-koodi Stringin� ja arvona
      * PreparedStatement
      */
     private Map prepFilteredPeople = new Hashtable();
     /**
      * Ty�pisteiden k�sittelyss� tarvittavat PreparedStatementit
      */
     private PreparedStatement prepNextPostID;
     private PreparedStatement prepCreatePost;
     private PreparedStatement prepDeletePost;
     private PreparedStatement prepRoomKeyReservations;
     private PreparedStatement prepRoomKeyReservationName;
     /**
      * Puhelinnumeroihin liittyv�t PreparedStatementit
      */
     private PreparedStatement prepPostPhoneNumbers;
     private PreparedStatement prepUpdatePhoneNumber;
     private HashMap<String, String> henkiloHash;
     private HashMap<String, String> varausHash;
 
     /**
      * Konstruktori. Luo yhteyden tietokantaan ja esilataa tiedot huoneista,
      * puhelinnumeroista sek� projekteista.
      *
      * @param session Sessio, johon t�m� tietokantaolio liittyy
      * @param className Tietokanta-ajurin luokan nimi
      * @param connectionString Tietokantayhteyden nimi
      * @param username K�ytt�j�tunnus
      * @param password Salasana
      */
     public NeroDatabase(Session session, String className,
             String connectionString, String username, String password) {
         this.session = session;
         // kuunnellaan aikajakson ja huonedatan muutoksia, jotta voidaan varmistua, etteiv�t
         // talletetut henkil�oliot sis�ll� vanhentunutta tietoa
         session.registerObserver(NeroObserverTypes.TIMESCALE, this);
         session.registerObserver(NeroObserverTypes.ROOMS, this);
 
         session.waitState(true);
         try {
             this.connection = this.createConnection(className,
                     connectionString, username, password);
             this.loadRooms();
             this.loadPhoneNumbers();
             //this.loadProjects();
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe 1: " + e.getMessage());
         } catch (ClassNotFoundException e) {
             System.err.println("Tietokanta-ajuria ei l�ydy.");
         }
         session.waitState(false);
     }
 
     /**
      * Luo tietokantayhteyden.
      *
      * @param className Tietokanta-ajurin luokan nimi
      * @param connectionString Tietokantayhteyden nimi
      * @param username K�ytt�j�tunnus
      * @param password Salasana
      * @return Luotu tietokantayhteys
      * @throws SQLException Jos yhteyden luonti ep�onnistuu
      * @throws ClassNotFoundException Jos tietokanta-ajuria ei l�ydy
      */
     private Connection createConnection(String className,
             String connectionString, String username, String password)
             throws SQLException, ClassNotFoundException {
         Connection conn = null;
         Class.forName(className);
         conn = DriverManager.getConnection(connectionString, username, password);
         return conn;
     }
 
     /**
      * Lataa j�rjestelm�n tuntemat projektit projects-hajautukseen
      * my�hemp�� k�ytt�� varten.
      *
      * @throws SQLException
      */
 //	private void loadProjects() throws SQLException {
 //		this.projects = new Hashtable();
 //
 //		session.setStatusMessage("Ladataan projekteja...");
 //		if(this.prepAllProjects == null) {
 //			this.prepAllProjects = this.connection.prepareStatement(
 //					"SELECT koodi, nimi, vastuuhenkilo, alkupvm,"
 //					/* loppupvm voi olla null, k�ytet��n 2099-12-31 */
 //					+ " NVL(loppupvm, TO_DATE('2099-12-31', 'YYYY-MM-DD')) AS loppupvm"
 //					+ " FROM PROJEKTI"
 //					+ " ORDER BY nimi, alkupvm"
 //			);
 //		}
 //		ResultSet rs = prepAllProjects.executeQuery();
 //
 //		while (rs.next()) {
 //			TimeSlice slice = null;
 //			Date start = rs.getDate("alkupvm");
 //			Date end = rs.getDate("loppupvm");
 //
 //			Project p = new Project(this.session, rs.getString("koodi"),
 //								rs.getString("nimi"),
 //								rs.getString("vastuuhenkilo"), slice);
 //			this.projects.put(rs.getString("koodi"), p);
 //		}
 //		rs.close();
 //        session.setStatusMessage("Ladattu tiedot " + this.projects.size() + " projektista.");
 //	}
     /**
      * Lataa j�rjestelm�n tuntemat huoneet ty�pisteineen
      * rooms-hajautukseen my�hemp�� k�ytt�� varten.
      *
      * @throws SQLException
      */
     private void loadRooms() throws SQLException {
         this.rooms = new Hashtable();
         this.posts = new Hashtable();
         this.roomKeyReservations = new ArrayList();
         Collection roomPosts = new LinkedList();
         Collection phoneNumbers = new LinkedList();
         session.setStatusMessage("Ladataan huoneita...");
         if (this.prepAllRooms == null) {
             this.prepAllRooms = this.connection.prepareStatement(
                     "SELECT rh.id, rak.nimi AS rakenn_nimi, rh.kerros_numero, rh.numero,"
                     + " rh.nimi, rh.pinta_ala, rh.kuvaus"
                     + " FROM RHUONE rh, RAKENNUS rak"
                     + " WHERE rh.rakenn_tunnus = rak.tunnus");
         }
         if (this.prepAllPosts == null) {
             this.prepAllPosts = this.connection.prepareStatement(
                     /*
                      "SELECT tp.id as tp_id, puh.id as puh_id, puh.puhelinnumero"
                      + "FROM TYOPISTE tp, PUHELINNUMERO puh"
                      + " WHERE rhuone_id = ? AND puh.tp_id = tp.id"
                      + " ORDER BY tp.id, puh.puhelinnumero");
                      */
                     "SELECT id, lisayspvm FROM TYOPISTE WHERE rhuone_id = ? ORDER BY id");
         }
         if (this.prepPostPhoneNumbers == null) {
             this.prepPostPhoneNumbers = this.connection.prepareStatement(
                     "SELECT id, puhelinnumero, h_tunnus FROM PUHELINNUMERO WHERE tp_id = ?");
         }
         if (this.prepRoomKeyReservations == null) {
             this.prepRoomKeyReservations = this.connection.prepareStatement(
                     "SELECT id, htunnus, rhuone_id, alkupvm, loppupvm FROM HUONEVARAUS where RHUONE_ID=?");
         }
         if (this.prepRoomKeyReservationName == null) {
             this.prepRoomKeyReservationName = this.connection.prepareStatement(
                     "SELECT sukunimi, etunimet FROM HENKILO WHERE htunnus=?");
         }
 
         ResultSet rs = this.prepAllRooms.executeQuery();
         /* NOTE rhuone-taulussa on sek "numero" ett "huone_nro" kentt */
         int numbercount = 0;
         while (rs.next()) {
 
 
             Room room = new Room(this.session, rs.getString("id"),
                     rs.getString("rakenn_nimi"),
                     rs.getString("kerros_numero"),
                     rs.getString("numero"), rs.getString("nimi"),
                     rs.getDouble("pinta_ala"), rs.getString("kuvaus"));
 
             this.prepAllPosts.setString(1, rs.getString("id"));
             ResultSet prs = this.prepAllPosts.executeQuery();
             while (prs.next()) {
                 Post post = new Post(this.session, prs.getString("id"), room, roomPosts.size() + 1, prs.getDate("lisayspvm"));
 
                 /* haetaan puhelinnumerot (hidas, kolme sis�kk�ist� prepared statementia
                  * mutta olkoot. */
                 Collection numbers = new Vector();
                 this.prepPostPhoneNumbers.setString(1, post.getPostID());
                 ResultSet pnrs = this.prepPostPhoneNumbers.executeQuery();
                 while (pnrs.next()) {
                     PhoneNumber pn = new PhoneNumber(this.session,
                             pnrs.getString("id"), post,
                             pnrs.getString("puhelinnumero"), pnrs.getString("h_tunnus"));
                     numbers.add(pn);
                     numbercount++;
 
                 }
                 pnrs.close();
                 post.setPhoneNumbers((PhoneNumber[]) numbers.toArray(new PhoneNumber[0]));
                 roomPosts.add(post);
                 this.posts.put(prs.getString("id"), post);
             }
             prs.close();
             room.setPosts((Post[]) roomPosts.toArray(new Post[0]));
             prepRoomKeyReservations.setString(1, rs.getString("id"));
             ResultSet roomKeysResult = this.prepRoomKeyReservations.executeQuery();
             while (roomKeysResult.next()) {
                 prepRoomKeyReservationName.setString(1, roomKeysResult.getString("HTUNNUS"));
                 ResultSet nameResults = this.prepRoomKeyReservationName.executeQuery();
                 if (roomKeysResult.getString("RHUONE_ID").equalsIgnoreCase(room.getRoomID())) {
                     TimeSlice timeslice = new TimeSlice(roomKeysResult.getTimestamp("ALKUPVM"), roomKeysResult.getTimestamp("LOPPUPVM"));
                     nameResults.next();
                     RoomKeyReservation keyReservation = new RoomKeyReservation(
                             roomKeysResult.getInt("ID"), room, roomKeysResult.getString("htunnus"),
                             nameResults.getString("SUKUNIMI") + " " + nameResults.getString("ETUNIMET"), timeslice, this.session);
                     room.addRoomKeyReservation(keyReservation);
                     this.roomKeyReservations.add(keyReservation);
                 }
 
             }
       
             //roomKeysResult;
             roomPosts.clear();
             this.rooms.put(rs.getString("id"), room);
         }
 		
 		rs.close();
 		session.setStatusMessage("Ladattu tiedot " + this.rooms.size() + " huoneesta.");
     }
 
 	/**
 	 * Lataa j�rjestelm�n tuntemat puhelinnumerot.
 	 * Saa kutsua vasta loadRooms() j�lkeen.
 	 * @throws SQLException
 	 */
 	private void loadPhoneNumbers() throws SQLException {
 		this.phoneNumbers = new Hashtable();
 
 		session.setStatusMessage("Ladataan puhelinnumeroita...");
 		if(this.prepAllPhoneNumbers == null) {
 			this.prepAllPhoneNumbers = this.connection.prepareStatement(
 					"SELECT id, puhelinnumero, tp_id, h_tunnus FROM PUHELINNUMERO"
 			);
                         
 		}             
                 
 		ResultSet rs = prepAllPhoneNumbers.executeQuery();
 		
 		int count = 0;
 		while (rs.next()) {
 			PhoneNumber pn;
 			String tpid = rs.getString("tp_id");
 			String pnid = rs.getString("id");
 			String number = rs.getString("puhelinnumero");
                         String htunnus = rs.getString("h_tunnus");
                         
                         
 			if(tpid == null) {
 				pn = new PhoneNumber(this.session, pnid, null, number, htunnus);
 				tpid = "free";
 			} else {
 				pn = new PhoneNumber(this.session, pnid, 
 						(Post)this.posts.get(tpid), number, htunnus);
 			}
 			Collection tpn = (Collection)this.phoneNumbers.get(tpid);
 			if(tpn == null) {
 				tpn = new Vector();
 				this.phoneNumbers.put(tpid, tpn);
 			}
 			tpn.add(pn);
 			count++;
 		}
 		rs.close();
 		session.setStatusMessage("Ladattu tiedot " + count + " puhelinnumerosta.");
 	}
 	
 
 	/* --- Ty�pistevarauksiin liittyv�t metodit alkaa --- */ 
 	
 	/**
 	 * Palauttaa parametrina annetun henkil�n ty�pistevaraukset, jotka
 	 * leikkaavat parametrina annettua aikav�li�. Varaukset palautetaan
      * j�rjestettyn� ensisijaisesti alkuajankohdan, toissijaisesti loppuajankohdan
      * mukaan.
 	 * 
 	 * @param person
 	 *            Henkil�, jonka ty�pistevarauksia haetaan.
 	 * @param timeslice
 	 *            Aikav�li, jonka aikana varauksen tulee olla ainakin osittain
 	 *            voimassa.
 	 * @return varaukset <code>Reservation[]</code> oliona.
 	 */
 
 	public Reservation[] getReservations(Person person, TimeSlice timeslice) {
 		Collection reservations = new Vector();
 		this.session.waitState(true);
 		try {
 			if(this.prepPersonReservations ==  null) {
 				this.prepPersonReservations = this.connection.prepareStatement(
 						"SELECT DISTINCT tpv.id, tpv.alkupvm, tpv.loppupvm,"
 						+ " tpv.viikkotunnit, tpv.selite, tpv.tpiste_id, tp.rhuone_id"
 						+ " FROM TYOPISTEVARAUS tpv, TYOPISTE tp"
 						+ " WHERE tpv.henklo_htunnus = ?"
 						+ " AND tpv.tpiste_id = tp.id"
 						+ " AND ? <= tpv.loppupvm AND ? >= tpv.alkupvm"
 						+ " ORDER BY tpv.alkupvm, tpv.loppupvm"
 				);
 			}
 			this.prepPersonReservations.setString(1, person.getPersonID());
 			this.prepPersonReservations.setDate(2, timeslice.getSQLStartDate());
 			this.prepPersonReservations.setDate(3, timeslice.getSQLEndDate());
 			
 			ResultSet rs = this.prepPersonReservations.executeQuery();
 			while(rs.next()) {
 				Date start = new Date(rs.getDate("alkupvm").getTime());
 				Date end = new Date(rs.getDate("loppupvm").getTime());
 				TimeSlice ts = new TimeSlice(start, end);
 				Post post = (Post) this.posts.get(rs.getString("tpiste_id"));
 				
 				Reservation r = new Reservation(this.session,
 						rs.getString("id"), post, person, ts,
 						rs.getDouble("viikkotunnit"), rs.getString("selite"));
 				reservations.add(r);
 			}
 			rs.close();
 		} catch (SQLException e) {
 			System.err.println("Tietokantavirhe: " + e.getMessage());
 		}
 		this.session.waitState(false);
 		return (Reservation[])reservations.toArray(new Reservation[0]);
 	}
         
 //        public ArrayList<HashMap<String, String>> getKannykat() {
 //            HashMap hashMap;
 //            ArrayList arry = new ArrayList<HashMap<String, String>>();
 //            
 //            try {
 //            
 //            }
 //            roomPosts.clear();
 //            this.rooms.put(rs.getString("id"), room);
 //        
 //        rs.close();
 //        session.setStatusMessage("Ladattu tiedot " + this.rooms.size() + " huoneesta.");
 //    }
 
     /**
      * Lataa j�rjestelm�n tuntemat puhelinnumerot. Saa kutsua vasta
      * loadRooms() j�lkeen.
      *
      * @throws SQLException
      */
     
 
 
     /* --- Ty�pistevarauksiin liittyv�t metodit alkaa --- */
     /**
      * Palauttaa parametrina annetun henkil�n ty�pistevaraukset, jotka
      * leikkaavat parametrina annettua aikav�li�. Varaukset palautetaan
      * j�rjestettyn� ensisijaisesti alkuajankohdan, toissijaisesti
      * loppuajankohdan mukaan.
      *
      * @param person Henkil�, jonka ty�pistevarauksia haetaan.
      * @param timeslice Aikav�li, jonka aikana varauksen tulee olla ainakin
      * osittain voimassa.
      * @return varaukset <code>Reservation[]</code> oliona.
      */
    
 
     public ArrayList<HashMap<String, String>> getKannykat() {
         HashMap hashMap;
         ArrayList arry = new ArrayList<HashMap<String, String>>();
 
         try {
 
             PreparedStatement prepKannykat = this.connection.prepareStatement("SELECT * FROM KANNYKKA");
             ResultSet rs = prepKannykat.executeQuery();
             while (rs.next()) {
                 hashMap = new HashMap<String, String>();
                 hashMap.put("puh_id", rs.getString("puh_id"));
                 hashMap.put("numero", rs.getString("kannukka_numero"));
                 hashMap.put("omistaja", rs.getString("omistaja"));
                 arry.add(hashMap);
             }
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
         return arry;
     }
     /**
      * Knnykn lisminen tietokantaan
      * @param kannykka 
      */
     public void addKannykka(Kannykka kannykka) {
 
         PreparedStatement prepAddKannykka;
         PreparedStatement prepID;
         PreparedStatement prep;
         
         String test = "SELECT * FROM KANNYKKA";       
         
         String x = "INSERT INTO KANNYKKA (PUH_ID, KANNYKKA_NUMERO, HTUNNUS, OMISTAJA, LISAUSPVM) VALUES ((SELECT MAX(PUH_ID) FROM KANNYKKA)+1, ?, ?, ?, CURRENT_TIMESTAMP)";
 
         String ensimmainen = "INSERT INTO KANNYKKA (PUH_ID, KANNYKKA_NUMERO, HTUNNUS, OMISTAJA, LISAUSPVM) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
 
         try {
             
             prepAddKannykka = this.connection.prepareStatement(ensimmainen);
             prepID = this.connection.prepareStatement(test);
             prep = this.connection.prepareStatement(x);
             
             ResultSet rs = prepID.executeQuery();
             
             if (!rs.next()) {
                 prepAddKannykka.setInt(1, 1);
                 prepAddKannykka.setString(2, kannykka.getPhonenumber());
                 prepAddKannykka.setString(3, kannykka.getHtunnus());
                 prepAddKannykka.setString(4, kannykka.getOmistaja());
                 prepAddKannykka.executeUpdate();
             } else {
                 prep.setString(1, kannykka.getPhonenumber());
                 prep.setString(2, kannykka.getHtunnus());
                 prep.setString(3, kannykka.getOmistaja());
                 prep.executeUpdate();
             }
 
             
             
 
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
 
     }
 
     /**
      * Palauttaa parametrina annetun ty�pisteen varaukset, jotka ovat ainakin
      * osittain p��llek�in annetun aikav�li kanssa.
      *
      * @param post Ty�piste, jonka varauksia haetaan.
      * @param timeslice Aikav�li, jolla varausten tulee olla ainakin osittain
      * voimassa.
      * @return Palautaa varaukset <code>Reservation[]</code> oliona.
      */
     public Reservation[] getReservations(Post post, TimeSlice timeslice) {
         Collection reservations = new Vector();
         this.session.waitState(true);
         try {
             if (this.prepPostReservations == null) {
                 this.prepPostReservations = this.connection.prepareStatement(
                         "SELECT DISTINCT tpv.id, tpv.alkupvm, tpv.loppupvm,"
                         + " tpv.viikkotunnit, tpv.selite,"
                         + " h.htunnus, h.etunimet, h.sukunimi, h.huone_nro,"
                         + " h.kutsumanimi,"
                         + " h.aktiivisuus, h.hetu, h.oppiarvo, h.titteli,"
                         + " h.puhelin_tyo, h.puhelin_koti, h.katuosoite, h.katuosoite,"
                         + " h.postinro, h.postitoimipaikka, h.valvontasaldo, h.sahkopostiosoite,"
                         + " h.hallinnollinen_kommentti, h.opiskelija_kommentti, h.ktunnus,"
                         + " h.kannykka, h.postilokerohuone, h.hy_tyosuhde, h.hy_puhelinluettelossa"
                         + " FROM TYOPISTEVARAUS tpv, HENKILO h"
                         + " WHERE tpv.tpiste_id = ?"
                         + " AND tpv.henklo_htunnus = h.htunnus"
                         + " AND ? <= tpv.loppupvm AND ? >= tpv.alkupvm");
             }
             this.prepPostReservations.setString(1, post.getPostID());
             this.prepPostReservations.setDate(2, timeslice.getSQLStartDate());
             this.prepPostReservations.setDate(3, timeslice.getSQLEndDate());
 
             ResultSet rs = this.prepPostReservations.executeQuery();
             while (rs.next()) {
                 Date start = new Date(rs.getDate("alkupvm").getTime());
                 Date end = new Date(rs.getDate("loppupvm").getTime());
                 TimeSlice ts = new TimeSlice(start, end);
 
                 varausHash = new HashMap();
                 varausHash.put("htunnus", rs.getString("htunnus"));
                 varausHash.put("kokonimi", (rs.getString("sukunimi") + " " + rs.getString("etunimet")));
                 varausHash.put("etunimet", rs.getString("etunimet"));
                 varausHash.put("sukunimi", rs.getString("sukunimi"));
                 varausHash.put("huone_nro", rs.getString("huone_nro"));
                 varausHash.put("kutsumanimi", rs.getString("kutsumanimi"));
                 varausHash.put("aktiivisuus", rs.getString("aktiivisuus"));
                 varausHash.put("hetu", rs.getString("hetu"));
                 varausHash.put("oppiarvo", rs.getString("oppiarvo"));
                 varausHash.put("titteli", rs.getString("titteli"));
                 varausHash.put("puhelin_tyo", rs.getString("puhelin_tyo"));
                 varausHash.put("puhelin_koti", rs.getString("puhelin_koti"));
                 varausHash.put("katuosoite", rs.getString("katuosoite"));
                 varausHash.put("postinro", rs.getString("postinro"));
                 varausHash.put("postitoimipaikka", rs.getString("postitoimipaikka"));
                 varausHash.put("valvontasaldo", rs.getString("valvontasaldo"));
                 varausHash.put("sahkopostiosoite", rs.getString("sahkopostiosoite"));
                 varausHash.put("hallinnollinen_kommentti", rs.getString("hallinnollinen_kommentti"));
                 varausHash.put("opiskelija_kommentti", rs.getString("opiskelija_kommentti"));
                 varausHash.put("ktunnus", rs.getString("ktunnus"));
                 varausHash.put("kannykka", rs.getString("kannykka"));
                 varausHash.put("postilokerohuone", rs.getString("postilokerohuone"));
                 varausHash.put("hy_tyosuhde", rs.getString("hy_tyosuhde"));
                 varausHash.put("hy_puhelinluettelossa", rs.getString("hy_puhelinluettelossa"));
 
 
                 Person person = new Person(this.session, varausHash, null, null);
 
 
 //				Person person = new Person(this.session, rs.getString("htunnus"),
 //                                        rs.getString("sukunimi")+" "+rs.getString("etunimet"),
 //                                        rs.getString("etunimet"), rs.getString("sukunimi"),
 //                                        null, null, rs.getString("huone_nro"), rs.getString("kutsumanimi"),
 //                                        rs.getString("aktiivisuus"), rs.getString("hetu"), rs.getString("oppiarvo"),
 //                                        rs.getString("titteli"), rs.getString("puhelin_tyo"), rs.getString("puhelin_koti"),
 //                                        rs.getString("katuosoite"), rs.getString("postinro"), rs.getString("postitoimipaikka"),
 //                                        rs.getString("valvontasaldo"), rs.getString("sahkopostiosoite"),
 //                                        rs.getString("hallinnollinen_kommentti"), rs.getString("opiskelija_kommentti"),
 //                                        rs.getString("ktunnus"), rs.getString("kannykka"), rs.getString("postilokerohuone"),
 //                                        rs.getString("hy_tyosuhde"), rs.getString("hy_puhelinluettelossa"));
 
                 Reservation r = new Reservation(this.session,
                         rs.getString("id"), post, person, ts,
                         rs.getDouble("viikkotunnit"), rs.getString("selite"));
                 reservations.add(r);
             }
             rs.close();
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
         this.session.waitState(false);
         return (Reservation[]) reservations.toArray(new Reservation[0]);
     }
 
     /**
      * Lis�� kantaan parametrin� annetun varauksen.
      *
      * @param reservation Uusi varaus, joka lis�t��n kantaan.
      * @return Onnistuiko lis�ys.
      */
     public boolean createReservation(Reservation reservation) {
         boolean success = false;
         this.session.waitState(true);
         try {
             /* seuraava vapaa ID _t�ytyy_ hakea ensin
              * j�lkipolville: "SELECT * FROM USER_SEQUENCES" */
             if (this.prepNextReservationID == null) {
                 this.prepNextReservationID = this.connection.prepareStatement(
                         "SELECT seq_tpvara_id.NEXTVAL FROM dual");
             }
             ResultSet rsid = this.prepNextReservationID.executeQuery();
             rsid.next();
             int nextID = rsid.getInt(1);
             rsid.close();
             // voisi asettaa annetulle Reservationille ID:n jos olisi tarve 
 
             if (this.prepAddReservation == null) {
                 this.prepAddReservation = this.connection.prepareStatement(
                         " INSERT INTO tyopistevaraus"
                         + " (id, tpiste_id, henklo_htunnus, viikkotunnit,"
                         + "  selite, alkupvm, loppupvm, lisayspvm)"
                         /* TRUNC lyhent�� ajan pelk�ksi p�iv�m��r�ksi */
                         + " VALUES (?, ?, ?, ?, ?, TRUNC(?), TRUNC(?), SYSDATE)");
             }
             this.prepAddReservation.setInt(1, nextID);
             this.prepAddReservation.setString(2, reservation.getTargetPost().getPostID());
             this.prepAddReservation.setString(3, reservation.getReservingPerson().getPersonID());
             this.prepAddReservation.setInt(4, (int) (reservation.getWeeklyHours()));
             this.prepAddReservation.setString(5, reservation.getDescription());
             this.prepAddReservation.setDate(6, reservation.getTimeSlice().getSQLStartDate());
             this.prepAddReservation.setDate(7, reservation.getTimeSlice().getSQLEndDate());
 
             if (this.prepAddReservation.executeUpdate() > 0) {
                 success = true;
             }
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
 
         // poistetaan henkil�n tiedot jotka ovat nyt vanhentuneet
         people.remove(reservation.getReservingPerson().getPersonID());
         this.session.waitState(false);
         return success;
     }
 
     /**
      * Poistaa kannasta parametrina annetun varauksen.
      *
      * @param reservation
      * @return Onnistuiko poisto.
      */
     public boolean deleteReservation(Reservation reservation) {
         boolean success = false;
         this.session.waitState(true);
         try {
             if (this.prepDeleteReservation == null) {
                 this.prepDeleteReservation = this.connection.prepareStatement(
                         "DELETE FROM TYOPISTEVARAUS WHERE id = ?");
             }
             this.prepDeleteReservation.setString(1, reservation.getReservationID());
             int deletedRows = this.prepDeleteReservation.executeUpdate();
             if (deletedRows > 0) {
                 success = true;
             }
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
 
         // poistetaan henkiln tiedot jotka ovat nyt vanhentuneet
         people.remove(reservation.getReservingPerson().getPersonID());
         this.session.waitState(false);
         return success;
     }
 
     /**
      * P�ivitt�� parametrina annetun varauksen kantaan.
      *
      * @param reservation muokattu varaus, joka halutaan tallettaa
      * @return Onnistuiko p�ivitys.
      */
     public boolean updateReservation(Reservation reservation) {
         boolean success = false;
         this.session.waitState(true);
         try {
             if (this.prepUpdateReservation == null) {
                 this.prepUpdateReservation = this.connection.prepareStatement(
                         "UPDATE TYOPISTEVARAUS"
                         + " SET tpiste_id  = ?, henklo_htunnus = ?,"
                         + " viikkotunnit = ?, selite = ?,"
                         + " alkupvm = TRUNC(?), loppupvm = TRUNC(?)"
                         + " WHERE id = ?");
             }
             this.prepUpdateReservation.setString(1, reservation.getTargetPost().getPostID());
             this.prepUpdateReservation.setString(2, reservation.getReservingPerson().getPersonID());
             this.prepUpdateReservation.setInt(3, (int) (reservation.getWeeklyHours()));
             this.prepUpdateReservation.setString(4, reservation.getDescription());
             this.prepUpdateReservation.setDate(5, reservation.getTimeSlice().getSQLStartDate());
             this.prepUpdateReservation.setDate(6, reservation.getTimeSlice().getSQLEndDate());
             this.prepUpdateReservation.setString(7, reservation.getReservationID());
 
             int updatedRows = this.prepUpdateReservation.executeUpdate();
             if (updatedRows > 0) {
                 success = true;
             }
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
 
         // poistetaan henkil�n tiedot jotka ovat nyt vanhentuneet
         people.remove(reservation.getReservingPerson().getPersonID());
         // pivitetn henkiliden huonetiedot
         this.updateRooms();
         this.session.waitState(false);
         return success;
     }
 
     /* --- Ty�pistevarauksiin liittyv�t metodit loppuu --- */
 
     /* --- Sopimuksiin liittyv�t metodit alkaa --- */
     /**
      * Palauttaa parametrin� annetun henkil�n ty�sopimusjaksot tietylt�
      * aikav�lilt�.
      *
      * @param person Henkil�, jonka sopimuksista ollaan kiinnostuneita.
      * @param timeslice Aikav�li, jonka kannsa sopimukset ovat ainakin
      * osittain p��llekk�in.
      * @return Sopimusjaksot <code>Contract[]</code> oliona.
      */
     public Contract[] getContracts(Person person, TimeSlice timeslice) {
         Collection contracts = new Vector();
         this.session.waitState(true);
         try {
             if (this.prepPersonContracts == null) {
                 /* NOTE pelk�st��n sopimusnumero ei ole yksiselitteinen
                  * Taulun primary key on kolmikko
                  * {sopimusnumero, henkloh_tunnus, alkupvm_jakso}
                  */
                 this.prepPersonContracts = this.connection.prepareStatement(
                         "SELECT tsj.sopimusnumero, tsj.alkupvm_jakso,"
                         + " tsj.loppupvm_jakso, tsj.prjkti_koodi,"
                         + " tsj.nimike, tsj.vv_hoitoprosentti"
                         + " FROM TYOSOPIMUSJAKSO tsj"
                         + " WHERE tsj.henklo_htunnus = ?"
                         + " AND ? <= tsj.loppupvm_jakso"
                         + " AND ? >= tsj.alkupvm_jakso");
             }
 
             this.prepPersonContracts.setString(1, person.getPersonID());
             this.prepPersonContracts.setDate(2, timeslice.getSQLStartDate());
             this.prepPersonContracts.setDate(3, timeslice.getSQLEndDate());
             ResultSet rs = this.prepPersonContracts.executeQuery();
 
             while (rs.next()) {
                 Project project = null;
                 String projectID = rs.getString("prjkti_koodi");
                 if (projectID != null) {
                     project = (Project) this.projects.get(projectID);
                 }
                 Date start = new Date(rs.getDate("alkupvm_jakso").getTime());
                 Date end = new Date(rs.getDate("loppupvm_jakso").getTime());
                 TimeSlice ts = new TimeSlice(start, end);
                 int workingPercentage = rs.getInt("vv_hoitoprosentti");
                 // kannassa vv_hoitoprosentti-kent�n NULL merkitsee normaalia ty�skentely�
                 if (rs.wasNull()) {
                     workingPercentage = 100;
                 }
 
                 Contract contract = new Contract(this.session,
                         rs.getString("sopimusnumero"), project, person,
                         rs.getString("nimike"), workingPercentage,
                         ts);
                 contracts.add(contract);
             }
             rs.close();
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
         this.session.waitState(false);
         return (Contract[]) contracts.toArray(new Contract[0]);
     }
 
     /* --- Sopimuksiin liittyv�t metodit loppuu --- */
 
     /* --- Henkil�ihin liittyv�t metodit alkaa --- */
     /**
      * Palauttaa henkilt jotka tyttvt parametreina annetut hakuehdot.
      * Hakuehtoja voi yhdist, ja oliotyyppiset hakuehdot voivat olla null,
      * jolloin ne eivt rajaa tulosta.
      *
      * @param timescale Aikavli, jota henkiliden tysopimusjaksojen tule
      * leikata
      * @param personName Merkkijono, johon henkiln (suku?)nime� verrataan.
      * @param partTimeTeachersOnly jos tosi, nytetn vain sivutoimiset
      * tuntiopettajat
      * @param withoutPost jos tosi, nytetn vain ne, joilla on
      * ty�sopimusjakso ilman samanaikaista ty�pistevarausta
      * @param showEndingContracts jos tosi, n�ytet��n vain ne, joiden
      * viimeinen ty�sopimusjakso on aikav�lill�
      * @param project projekti, jonka henkil�t n�ytet��n
      * @return henkil�t <code>Person[]</code> oliona.
      */
     public Person[] getPeople(TimeSlice timescale, String personName,
             Project project, boolean showEndingContracts, boolean withoutPost,
             boolean partTimeTeachersOnly) {
         Collection filteredPeople = new LinkedList();
         this.session.waitState(true);
 
         session.setStatusMessage("Haetaan henkilit..." /*
                  + " (aikav�li: " + timescale
                  + ", nimi: " + personName
                  + ", projekti: " + project
                  + ", loppuvat: " + showEndingContracts
                  + ", surulliset: " + withoutPost
                  + ", osa-aikaiset: " + partTimeTeachersOnly
                  + ")"
                  */);
         // Kootaan SQL-kysely paloista
         // Yhteinen alkuosa
         String sqlQuery = "SELECT DISTINCT h.htunnus, h.sukunimi,"
                 + " h.etunimet, h.huone_nro, h.kutsumanimi,"
                 + " h.aktiivisuus, h.hetu, h.oppiarvo, h.titteli,"
                 + " h.puhelin_tyo, h.puhelin_koti, h.katuosoite, h.katuosoite,"
                 + " h.postinro, h.postitoimipaikka, h.valvontasaldo, h.sahkopostiosoite,"
                 + " h.hallinnollinen_kommentti, h.opiskelija_kommentti, h.ktunnus,"
                 + " h.kannykka, h.postilokerohuone, h.hy_tyosuhde, h.hy_puhelinluettelossa,"
                 + " max(tsj.loppupvm_jakso) as loppupvm"
                 + " FROM TYOSOPIMUSJAKSO tsj, HENKILO h"
                 + " WHERE (UPPER(h.sukunimi) LIKE UPPER(?)"
                 + " OR UPPER(h.etunimet) LIKE UPPER(?))";
 
         // t�sm�llisemmiss� hauissa t�ytyy katsoa ty�sopimusjaksoa
         if (showEndingContracts || withoutPost
                 || project != null || partTimeTeachersOnly) {
             sqlQuery += " AND tsj.henklo_htunnus = h.htunnus"
                     + " AND tsj.sopimustyyppi LIKE ?"
                     /* Oraclessa like-vertailu nulliin ei toimi,
                      * joten k�ytet��n NVL()-funktiota */
                     + " AND NVL(tsj.prjkti_koodi, 'oraclesucks') LIKE ?"
                     + " AND ? <= tsj.loppupvm_jakso AND ? >= tsj.alkupvm_jakso";
         } else // tehd��n ulkoliitos eli saadaan my�s henkil�t ilman sopimusjaksoja
         {
             sqlQuery += " AND tsj.henklo_htunnus(+) = h.htunnus";
         }
 
         // Jos pyydetty ty�pisteett�m�t mutta ei p��ttyvi� sopimuksia,
         // tarkistetaan helpoin tapaus t�ss� (yksi v�hint��n
         // koko sopimusjakson peitt�v� varaus) ja loput tarkistetaan koodissa
         if (withoutPost && !showEndingContracts) {
             sqlQuery += " AND NOT EXISTS ("
                     + " SELECT id"
                     + " FROM tyopistevaraus"
                     + " WHERE henklo_htunnus = h.htunnus"
                     + " AND alkupvm <= greatest(tsj.alkupvm_jakso, ?)"
                     + " AND loppupvm >= least(tsj.loppupvm_jakso, ?)"
                     + ")";
         }
 
         // Yhteinen GROUP BY -osa
         sqlQuery += " GROUP BY h.htunnus, h.sukunimi, h.etunimet, h.huone_nro, h.kutsumanimi,"
                 + " h.aktiivisuus, h.hetu, h.oppiarvo, h.titteli,"
                 + " h.puhelin_tyo, h.puhelin_koti, h.katuosoite, h.katuosoite,"
                 + " h.postinro, h.postitoimipaikka, h.valvontasaldo, h.sahkopostiosoite,"
                 + " h.hallinnollinen_kommentti, h.opiskelija_kommentti, h.ktunnus,"
                 + " h.kannykka, h.postilokerohuone, h.hy_tyosuhde, h.hy_puhelinluettelossa";
 
         // jos n�ytet��n vain p��ttyv�t sopimukset, niin voidaan rajata jo nyt,
         // mutta jos mukana ovat my�s ty�pisteett�m�t, niin t�ytyy rajaus tehd� my�hemmin
         if (showEndingContracts && !withoutPost) {
             sqlQuery += " HAVING MAX(loppupvm_jakso) BETWEEN ? AND ?";
         }
 
         // Yhteinen ORDER BY -osa
         sqlQuery += " ORDER BY h.sukunimi, h.etunimet";
 
         try {
             // katsotaan olisiko sqlQuerya vastaava PreparedStatement jo valmiina
             PreparedStatement prep = (PreparedStatement) prepFilteredPeople.get(sqlQuery);
             // jos ei ole, tehd��n ja pannaan talteen
             if (prep == null) {
                 prep = this.connection.prepareStatement(sqlQuery);
                 prepFilteredPeople.put(sqlQuery, prep);
             }
             
             long prepTime = System.currentTimeMillis();
             //System.err.println("Valmisteluun meni aikaa ms: " + (prepTime - startTime));
 
             // Asetetaan parametrit; k�ytet��n laskuria apuna
             // Laskuria kasvatetaan jokaisen k�ytt�kerran yhteydess� ++ -operaattorilla
             int paramNo = 1;
             // p�iv�m��ri� on turha muodostaa joka kerta, joten otetaan talteen
             java.sql.Date start = timescale.getSQLStartDate();
             java.sql.Date end = timescale.getSQLEndDate();
 
             /* henkil�n nimi, jos annettu */
             if (personName == null) {
                 prep.setString(paramNo++, "%");
                 prep.setString(paramNo++, "%");
             } else {
                 prep.setString(paramNo++, "%" + personName + "%");
                 prep.setString(paramNo++, "%" + personName + "%");
             }
 
             if (showEndingContracts || withoutPost
                     || project != null || partTimeTeachersOnly) {
 
                 /* sopimustyyppi:
                  * S sivutoiminen, P p��toiminen, L laitosteht�v�, D dosentuuri */
                 if (partTimeTeachersOnly) {
                     prep.setString(paramNo++, "S");
                 } else {
                     prep.setString(paramNo++, "%");
                 }
                 /* projektia peliin */
                 if (project == null) {
                     prep.setString(paramNo++, "%");
                 } else {
                     prep.setString(paramNo++, project.getProjectID());
                 }
                 /* aikav�li*/
                 prep.setDate(paramNo++, start);
                 prep.setDate(paramNo++, end);
             }
 
             /* ty�pisteett�m�t */
             if (withoutPost && !showEndingContracts) {
                 prep.setDate(paramNo++, start);
                 prep.setDate(paramNo++, end);
             }
 
             /* p��ttyv�t sopimukset */
             if (showEndingContracts && !withoutPost) {
                 prep.setDate(paramNo++, start);
                 prep.setDate(paramNo++, end);
             }
 
             ResultSet rs = prep.executeQuery();
             while (rs.next()) {
                 // Yritet��n k�ytt�� tallessa olevaa henkil��
                 Person person = (Person) people.get(rs.getString("htunnus"));
                 if (person == null) { // ei l�ytynyt, t�ytyy luoda uusi
                     /* contracts ja reservations saavat olla null uudella henkil�ll� */
                     Contract[] contracts = null;
                     Date lastContractEnd = rs.getDate("loppupvm");
                     if (lastContractEnd == null || lastContractEnd.before(start)) {
                         // haa, tiedet��n ettei sopimusjaksoja ole aikav�lill�
                         // kerrotaan se Personille ettei se turhaan kysele
                         contracts = new Contract[0];
                     }
                     henkiloHash = new HashMap();
                     henkiloHash.put("htunnus", rs.getString("htunnus"));
                     henkiloHash.put("kokonimi", (rs.getString("sukunimi") + " " + rs.getString("etunimet")));
                     henkiloHash.put("etunimet", rs.getString("etunimet"));
                     henkiloHash.put("sukunimi", rs.getString("sukunimi"));
                     henkiloHash.put("huone_nro", rs.getString("huone_nro"));
                     henkiloHash.put("kutsumanimi", rs.getString("kutsumanimi"));
                     henkiloHash.put("aktiivisuus", rs.getString("aktiivisuus"));
                     henkiloHash.put("hetu", rs.getString("hetu"));
                     henkiloHash.put("oppiarvo", rs.getString("oppiarvo"));
                     henkiloHash.put("titteli", rs.getString("titteli"));
                     henkiloHash.put("puhelin_tyo", rs.getString("puhelin_tyo"));
                     henkiloHash.put("puhelin_koti", rs.getString("puhelin_koti"));
                     henkiloHash.put("katuosoite", rs.getString("katuosoite"));
                     henkiloHash.put("postinro", rs.getString("postinro"));
                     henkiloHash.put("postitoimipaikka", rs.getString("postitoimipaikka"));
                     henkiloHash.put("valvontasaldo", rs.getString("valvontasaldo"));
                     henkiloHash.put("sahkopostiosoite", rs.getString("sahkopostiosoite"));
                     henkiloHash.put("hallinnollinen_kommentti", rs.getString("hallinnollinen_kommentti"));
                     henkiloHash.put("opiskelija_kommentti", rs.getString("opiskelija_kommentti"));
                     henkiloHash.put("ktunnus", rs.getString("ktunnus"));
                     henkiloHash.put("kannykka", rs.getString("kannykka"));
                     henkiloHash.put("postilokerohuone", rs.getString("postilokerohuone"));
                     henkiloHash.put("hy_tyosuhde", rs.getString("hy_tyosuhde"));
                     henkiloHash.put("hy_puhelinluettelossa", rs.getString("hy_puhelinluettelossa"));
                     
                     
                     person = new Person(this.session, henkiloHash, contracts, null);
                     
                     for (RoomKeyReservation rkreservation : this.roomKeyReservations) {
                         if(rkreservation.getReserverID().equals(rs.getString("htunnus")))
                             person.addRoomKeyReservation(rkreservation);
                     }
                     people.put(rs.getString("htunnus"), person);
                 }
 
                 if (filterPerson(person, timescale, rs.getDate("loppupvm"), showEndingContracts, withoutPost, session.getFilterActiveEmployees(), session.getFilterContract())) {
                     filteredPeople.add(person);
                 }
             }
             rs.close();
             //System.err.println("Kyselyn suorittamiseen meni aikaa ms: " + (System.currentTimeMillis() - prepTime));
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
         this.session.waitState(false);
         session.setStatusMessage("Lytyi " + filteredPeople.size() + " henkil.");
         return (Person[]) filteredPeople.toArray(new Person[0]);
     }
     public void getPersonInfo(Person person) {
         
         String sqlQuery = "SELECT htunnus, sukunimi,"
                 + " etunimet, huone_nro, kutsumanimi,"
                 + " aktiivisuus, hetu, oppiarvo, titteli,"
                 + " puhelin_tyo, puhelin_koti, katuosoite, katuosoite,"
                 + " postinro, postitoimipaikka, valvontasaldo, sahkopostiosoite,"
                 + " hallinnollinen_kommentti, opiskelija_kommentti, ktunnus,"
                 + " kannykka, postilokerohuone, hy_tyosuhde, hy_puhelinluettelossa"
                 + " FROM HENKILO WHERE h_tunnus = ?";
     }
 
     public void updatePersonInfo(Person person) throws SQLException {
         this.session.waitState(true);
 
         PreparedStatement prepModifyperson = this.connection.prepareStatement(
                 " UPDATE henkilo"
                 + " SET htunnus = ?, etunimet = ?, sukunimi = ?, kutsumanimi = ?, aktiivisuus = ?, huone_nro = ?,"
                 + " hetu = ?, oppiarvo = ?, titteli = ?, puhelin_koti = ?, katuosoite = ?,"
                 + " postinro = ?, postitoimipaikka = ?, sahkopostiosoite = ?, hallinnollinen_kommentti = ?,"
                 + " ktunnus = ?, kannykka = ?, postilokerohuone = ?, hy_tyosuhde = ?, hy_puhelinluettelossa = ?"
                 + " WHERE htunnus = ? AND etunimet = ? AND sukunimi = ?");
 
         prepModifyperson.setString(1, person.getPersonID());
         prepModifyperson.setString(2, person.getEtunimi());
         prepModifyperson.setString(3, person.getSukunimi());
         prepModifyperson.setString(4, person.getCallName());
         prepModifyperson.setString(5, person.getActivity());
         prepModifyperson.setString(6, person.getRoom());
         prepModifyperson.setString(7, person.getHetu());
         prepModifyperson.setString(8, person.getOppiarvo());
         prepModifyperson.setString(9, person.getTitteli());
         prepModifyperson.setString(10, person.getHomePhone());
         prepModifyperson.setString(11, person.getAddress());
         prepModifyperson.setString(12, person.getPostnumber());
         prepModifyperson.setString(13, person.getPostitoimiPaikka());
         prepModifyperson.setString(14, person.getSahkoposti());
         prepModifyperson.setString(15, person.getHallinnollinenKommentti());
         prepModifyperson.setString(16, person.getkTunnus());
         prepModifyperson.setString(17, person.getKannykka());
         prepModifyperson.setString(18, person.getPostilokeroHuone());
         prepModifyperson.setString(19, person.getHyTyosuhde());
         prepModifyperson.setString(20, person.getHyPuhelinluettelossa());
         prepModifyperson.setString(21, person.getPersonID());
         prepModifyperson.setString(22, person.getEtunimi());
         prepModifyperson.setString(23, person.getSukunimi());
 
         prepModifyperson.executeUpdate();
 
 
         this.session.waitState(false);
     }
 
     public void createPerson(Person person) throws SQLException {
         this.session.waitState(true);
 
         PreparedStatement prepCreateperson = this.connection.prepareStatement(
                 " INSERT INTO henkilo (htunnus, etunimet, sukunimi, kutsumanimi, aktiivisuus, huone_nro,"
                 + " hetu, oppiarvo, titteli, puhelin_koti, katuosoite,"
                 + " postinro, postitoimipaikka, sahkopostiosoite, hallinnollinen_kommentti,"
                 + " ktunnus, kannykka, postilokerohuone, hy_tyosuhde, hy_puhelinluettelossa)"
                 + " VALUES (?, ?, ?, ?, ?,"
                 + " ?, ?, ?, ?, ?, ?,"
                 + " ?, ?, ?, ?, ?, ?,"
                 + " ?, ?, ?)");
 
         prepCreateperson.setString(1, person.getPersonID());
         prepCreateperson.setString(2, person.getEtunimi());
         prepCreateperson.setString(3, person.getSukunimi());
         prepCreateperson.setString(4, person.getCallName());
         prepCreateperson.setString(5, person.getActivity());
         prepCreateperson.setString(6, person.getRoom());
         prepCreateperson.setString(7, person.getHetu());
         prepCreateperson.setString(8, person.getOppiarvo());
         prepCreateperson.setString(9, person.getTitteli());
 //        prepCreateperson.setString(10, person.getWorkPhone());
         prepCreateperson.setString(10, person.getHomePhone());
         prepCreateperson.setString(11, person.getAddress());
         prepCreateperson.setString(12, person.getPostnumber());
         prepCreateperson.setString(13, person.getPostitoimiPaikka());
         prepCreateperson.setString(14, person.getSahkoposti());
         prepCreateperson.setString(15, person.getHallinnollinenKommentti());
         prepCreateperson.setString(16, person.getkTunnus());
         prepCreateperson.setString(17, person.getKannykka());
         prepCreateperson.setString(18, person.getPostilokeroHuone());
         prepCreateperson.setString(19, person.getHyTyosuhde());
         prepCreateperson.setString(20, person.getHyPuhelinluettelossa());
 
         prepCreateperson.executeUpdate();
 
 
         this.session.waitState(false);
     }
 
     /**
      * Tarkista kuuluuko henkil� hakuehtojen mukaiseen listaan. Tarkistus
      * tehd��n, koska vastaavan seikan tarkistaminen SQL-kannassa on hyvin
      * vaikeaa ja hidasta.
      *
      * @param person henkil�
      * @param timescale tarkasteltava aikav�li
      * @param contractEndDate henkil�
      * @param showEndingContracts halutaanko p��ttyv�t ty�sopimukset
      * @param withoutPost halutaanko ty�pisteett�m�t
      * @return Sopivatko annetut hakuehdot henkil��n
      */
     private boolean filterPerson(Person person, TimeSlice timescale, java.sql.Date contractEndDate, boolean showEndingContracts, boolean withoutPost, boolean activeOnly, boolean contractsOnly) {
         // Tarkistetaan tarvitseeko henkil filtterid aktiivisuuden perusteella
         if (activeOnly && person.getActivity().equalsIgnoreCase("E")) {
             return false;
         }
         // Tarkistetaan, ett tarvitseeko henkil filtterid tysopimuksen perusteella
         if (contractsOnly && person.getHyTyosuhde().equalsIgnoreCase("E")) {
             return false;
         }
         // jos ei pyydetty ty�pisteett�mi�, ei filtter�inti� tarvita 
         if (!withoutPost) {
             return true;
         }
         // jos pyydettiin p��ttyvi� sopimuksia, tarkistetaan ensin ne
         if (showEndingContracts && timescale.contains(contractEndDate)) {
             return true;
         }
         // muussa tapauksessa t�ytyy tutkia tarkemmin henkil�n varaukset
         return person.getStatus();
     }
 
     /**
      * Nerompi
      * Lis tietokannan henkilo-taulun huone_nro-kenttn annettu arvo.
      *
      * @param person Henkil, jolle huone listn
      * @param room Listtvn huoneen nimi
      * @return Onnistuiko lisminen
      */
     public boolean addRoomToPerson(Person person, Room room) {
         if (person.getRoom() != null) {
             this.removeRoomFromPerson(person);
         }
         String sqlQuery = "update henkilo"
                 + " set HUONE_NRO=?"
                 + " where HTUNNUS=?";
         try {
             PreparedStatement prep = this.connection.prepareStatement(sqlQuery);
             prep.setString(1, room.getRoomName());
             prep.setString(2, person.getPersonID());
             prep.executeQuery();
             person.setRoom(room.getRoomName());
             return true;
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
             return false;
         }
     }
 
     /**
      * Nerompi
      * Korvaa tietokannan henkilo-taulun huone_nro-kentn arvo
      * null-arvolla.
      *
      * @param person Henkil, jolta huone poistetaan.
      * @return Onnistuiko poistaminen.
      */
     public boolean removeRoomFromPerson(Person person) {
         if (person.getRoom() == null) {
             return false;
         }
         String sqlQuery = "update henkilo"
                 + " set huone_nro=null"
                 + " where henkilo.htunnus=?";
         try {
             PreparedStatement prep = this.connection.prepareStatement(sqlQuery);
             prep.setString(1, person.getPersonID());
             prep.executeQuery();
             person.setRoom(null);
             return true;
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
             return false;
         }
     }
 
     /**
      * Nerompi
      * Pivitt henkilo-taulun tyopuhelinnumero-kentn
      *
      * @param name Pivitettvn henkiln nimi
      * @param number Henkillle asetettava typuhelinnumero
      * @return Onnistuiko pivittminen
      */
     public boolean updateWorkPhone(String personID, String number) {
         String updatePhoneQuery = "update henkilo"
                 + " set puhelin_tyo=?"
                 + " where htunnus=?";
         try {
             PreparedStatement prep = this.connection.prepareStatement(updatePhoneQuery);
             prep.setString(1, number);
             prep.setString(2, personID);
             prep.executeQuery();
             return true;
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
             return false;
         }
     }
 
     /** Nerompi
      * Hakee tietokannasta umpeutuneet huonevaraukset ja alkaneet
      * huonevaraukset ja pivitt huoneen nille ihmisille.
      */
     public void updateRooms() {
 
         // Hakee typistevaraukset joiden varaus on mennyt umpeen
         String selectPersonQuery = "select henklo_htunnus"
                 + " from tyopistevaraus"
                 + " where loppupvm<CURRENT_TIMESTAMP";
 
         // Poistaa huoneen annetulta henkillt
         String removeRoomQuery = "update henkilo"
                 + " set huone_nro=null"
                 + " where htunnus=?";
 
         // Hakee typistevaraukset joiden varaus on alkanut, mutta ei ole viel loppunut
         String selectPersonQuery2 = "select henklo_htunnus"
                 + " from tyopistevaraus"
                 + " where alkupvm<CURRENT_TIMESTAMP AND loppupvm>CURRENT_TIMESTAMP";
 
         // Hakee annetun henkiln typistevaraukseen liittyvn huoneen numeron
         String selectRoomQuery = "select huone_nro"
                 + " from huone"
                 + " where huone_nro="
                 + " (select huone_nro"
                 + " from rhuone"
                 + " where id="
                 + " (select rhuone_id"
                 + " from tyopiste"
                 + " where id="
                 + " (select tpiste_id"
                 + " from tyopistevaraus"
                 + " where henklo_htunnus=?"
                 + " and alkupvm<CURRENT_TIMESTAMP AND loppupvm>CURRENT_TIMESTAMP)))";
 
         // Laittaa annetulle henkillle annetun huoneen
         String setRoomQuery = "update henkilo"
                 + " set huone_nro=?"
                 + " where htunnus=?";
 
         PreparedStatement prep, prep2;
         try {
             ResultSet selectPersonResult = this.connection.prepareStatement(selectPersonQuery).executeQuery();
             while (selectPersonResult.next()) { // Poistaa huoneen niilt, joiden varaus on mennyt umpeen
                 prep = this.connection.prepareStatement(removeRoomQuery);
                 prep.setString(1, selectPersonResult.getString("henklo_htunnus"));
                 prep.executeQuery();
             }
 
             ResultSet selectPersonResult2 = this.connection.prepareStatement(selectPersonQuery2).executeQuery();
 
             while (selectPersonResult2.next()) { // Pivitt tyhuonenumeron niille, joiden varaus on alkanut, mutta ei viel loppunut
                 prep2 = this.connection.prepareStatement(selectRoomQuery);
                 prep2.setString(1, selectPersonResult2.getString("henklo_htunnus"));
                 ResultSet selectRoomResult = prep2.executeQuery();
                 selectRoomResult.next();
 
                 prep = this.connection.prepareStatement(setRoomQuery);
                 prep.setString(1, selectRoomResult.getString("huone_nro"));
                 prep.setString(2, selectPersonResult2.getString("henklo_htunnus"));
                 prep.executeQuery();
             }
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
     }
     
     public void deleteRoomFromPerson(Person person) {
         String updateQuery="update henkilo set huone_nro=null where htunnus=?";
         
         try {
             PreparedStatement prep = this.connection.prepareStatement(updateQuery);
             prep.setString(1, person.getPersonID());
             prep.executeUpdate();
         } catch(SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
     }
 
     /* --- Henkil�ihin liittyv�t metodit loppuu --- */
 
     /* --- Ty�pisteisiin liittyv�t metodit alkaa --- */
     /**
      * Poistaa ty�pisteen.
      *
      * @param post Poistettava ty�piste
      * @return Onnistuiko poisto.
      */
     public boolean deletePost(Post post) {
         boolean success = false;
         this.session.waitState(true);
         try {
             if (this.prepDeletePost == null) {
                 this.prepDeletePost = this.connection.prepareStatement(
                         "DELETE FROM TYOPISTE WHERE id = ?");
             }
             this.prepDeletePost.setString(1, post.getPostID());
             int deletedRows = this.prepDeletePost.executeUpdate();
             if (deletedRows > 0) {
                 success = true;
                 // XXX Raskas operaatio
                 loadRooms();
             }
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
         this.session.waitState(false);
         return success;
     }
         
 
     /**
      * Lis typisteen huoneeseen
      *
      * @param post Typiste joka listn.
      * @return Onnistuiko lisys
      */
     public boolean createPost(Post post) {
         boolean success = false;
         this.session.waitState(true);
         try {
             /* seuraava vapaa ID _t�ytyy_ hakea ensin */
             if (this.prepNextPostID == null) {
                 this.prepNextPostID = this.connection.prepareStatement(
                         "SELECT seq_tpiste_id.NEXTVAL FROM dual");
             }
             ResultSet rsid = this.prepNextPostID.executeQuery();
             rsid.next();
             int nextID = rsid.getInt(1);
             rsid.close();
 
             if (this.prepCreatePost == null) {
                 this.prepCreatePost = this.connection.prepareStatement(
                         " INSERT INTO tyopiste (id, rhuone_id, lisayspvm)"
                         + " VALUES (?, ?, SYSDATE)");
             }
             this.prepCreatePost.setInt(1, nextID);
             this.prepCreatePost.setString(2, post.getRoom().getRoomID());
 
             if (this.prepCreatePost.executeUpdate() > 0) {
                 success = true;
                 // XXX Raskas operaatio
                 this.loadRooms();
 
             }
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
         this.session.waitState(false);
         return success;
     }
 
     /* --- Ty�pisteisiin liittyv�t metodit loppuu --- */
 
     /* --- Tyhuoneisiin liittyvt metodit alkaa --- */
     /**
      * Palauttaa kaikki j�rjestelm�n tuntemat huoneet.
      *
      * @return huoneet Room[]-oliona
      */
     public Room[] getRooms() {
         return (Room[]) rooms.values().toArray(new Room[0]);
     }
 
     /**
      * Hakee tietyn projektin henkil�ille varatut huoneet tietyll�
      * aikav�lill�. Jos projekti on null, palautetaan tyhj� huonetaulukko.
      *
      * @param project Projekti, jonka ty�huoneita haetaan. Jos null,
      * palautetaan tyhj� lista.
      * @param timescale Aikav�li, jota varauksien tulee leikata.
      * @return taulukko projektin huoneista
      */
     public Room[] getRooms(Project project, TimeSlice timescale) {
         if (project == null) {
             return new Room[0];
         }
         Collection rooms = new Vector();
         this.session.waitState(true);
         try {
             if (this.prepProjectRooms == null) {
                 this.prepProjectRooms = this.connection.prepareStatement(
                         "SELECT h.ID AS huone FROM RHUONE h,"
                         + " TYOPISTE tp, TYOPISTEVARAUS tpv, TYOSOPIMUSJAKSO tsj"
                         + " WHERE h.ID = tp.RHUONE_ID AND tp.ID = tpv.TPISTE_ID"
                         + " AND tpv.HENKLO_HTUNNUS = tsj.HENKLO_HTUNNUS"
                         + " AND tsj.PRJKTI_KOODI = ?"
                         /* sek� ty�pistevarauksen ett� ty�sopimusjakson p�iv�m��r�t
                          * pit�� olla oikein */
                         + " AND ? <= tsj.LOPPUPVM_JAKSO AND ? >= tsj.ALKUPVM_JAKSO"
                         + " AND ? <= tpv.LOPPUPVM AND ? >= tpv.ALKUPVM");
             }
             this.prepProjectRooms.setString(1, project.getProjectID());
 
             java.sql.Date start = timescale.getSQLStartDate();
             java.sql.Date end = timescale.getSQLEndDate();
             this.prepProjectRooms.setDate(2, start);
             this.prepProjectRooms.setDate(3, end);
             this.prepProjectRooms.setDate(4, start);
             this.prepProjectRooms.setDate(5, end);
 
             ResultSet rs = this.prepProjectRooms.executeQuery();
             while (rs.next()) {
                 rooms.add(this.rooms.get(rs.getString("huone")));
             }
             rs.close();
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
         this.session.waitState(false);
         return (Room[]) rooms.toArray(new Room[0]);
     }
 
     /**
      * Palauttaa ne huoneet, jotka tyttvt annetut hakuehdot.
      *
      * @param roomFilter String jota etsitn huoneen nimest ja
      * numerosta.
      * @param maxPosts Huoneen ty�pisteiden maksimilukum��r�. Jos
      * pienempi kuin 0, ei rajaa tulosta.
      * @return Hakuehdot t�ytt�v�t huoneet Room[]-oliona
      */
     public Room[] getRooms(String roomFilter, int maxPosts) {
         String queryNormal, queryWithMaxPosts;
         Collection temprooms = new Vector();
         this.session.waitState(true);
         /* SQL-kyselyn normaali muoto */
         queryNormal = "SELECT rhuone.id FROM rhuone"
                 + " WHERE (UPPER(rhuone.numero) LIKE UPPER(?)"
                 + " OR UPPER(rhuone.nimi) LIKE UPPER(?))";
 
         queryWithMaxPosts = "SELECT rhuone.id FROM rhuone, tyopiste"
                 + " WHERE (UPPER(rhuone.numero) LIKE UPPER(?)"
                 + " OR UPPER(rhuone.nimi) LIKE UPPER(?))"
                 + " AND rhuone.id = tyopiste.rhuone_id"
                 + " HAVING COUNT(tyopiste.id) <= ?"
                 + " GROUP BY rhuone.id";
 
         try {
             if (this.prepNamedRoomsNormal == null) {
                 this.prepNamedRoomsNormal =
                         this.connection.prepareStatement(queryNormal);
             }
             if (this.prepNamedRoomsWithMaxPosts == null) {
                 this.prepNamedRoomsWithMaxPosts =
                         this.connection.prepareStatement(queryWithMaxPosts);
             }
 
             ResultSet rs;
             if (maxPosts < 0) {
                 this.prepNamedRoomsNormal.setString(1, "%" + roomFilter + "%");
                 this.prepNamedRoomsNormal.setString(2, "%" + roomFilter + "%");
                 rs = this.prepNamedRoomsNormal.executeQuery();
             } else {
                 this.prepNamedRoomsWithMaxPosts.setString(1, "%" + roomFilter + "%");
                 this.prepNamedRoomsWithMaxPosts.setString(2, "%" + roomFilter + "%");
                 this.prepNamedRoomsWithMaxPosts.setInt(3, maxPosts);
                 rs = this.prepNamedRoomsWithMaxPosts.executeQuery();
             }
             while (rs.next()) {
                 Room r = (Room) this.rooms.get(rs.getString("id"));
                 temprooms.add(r);
             }
             rs.close();
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
         this.session.waitState(false);
         return (Room[]) temprooms.toArray(new Room[0]);
     }
 
     /**
      * Palauttaa huoneolion huone-id:n perusteella.
      *
      * @param roomID huoneen id
      * @return huoneolio tai null, jos kyseist huonetta ei lydy
      */
     public Room getRoom(String roomID) {
         return (Room) rooms.get(roomID);
     }
 
     /**
      * Nerompi Palauttaa varaukset annettuun huoneeseen
      *
      * @param room huone, jonka varaukset halutaan hakea
      * @return Taulu varauksista
      */
     public RoomKeyReservation[] getRoomKeyReservations(Room room) {
         String sqlquery = "SELECT *"
                 + " FROM HUONEVARAUS"
                 + " WHERE RHUONE_ID=?";
 
         try {
             PreparedStatement prep = this.connection.prepareStatement(sqlquery);
             prep.setString(1, room.getRoomID());
             ResultSet rs = prep.executeQuery();
             ArrayList<RoomKeyReservation> arrayList = new ArrayList();
             while(rs.next()) {
                 TimeSlice timeslice = new TimeSlice(rs.getDate("ALKUPVM"), rs.getDate("LOPPUPVM"));
                 Person person = (Person) people.get(rs.getString("HTUNNUS"));
                 arrayList.add(new RoomKeyReservation(rs.getInt("ID"), (Room) rooms.get(rs.getString("RHUONE_ID")), person.getPersonID(), person.getName(), timeslice, this.session));
             }
             RoomKeyReservation[] temp = new RoomKeyReservation[0];
             return arrayList.toArray(temp);
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
             return null;
         }
     }
 
 	/* --- Huoneisiin liittyvt metodit loppuu --- */ 
 
 	/* --- Puhelinnumeroihin liittyvt metodit alkaa --- */ 
 
 	/**
 	 * Palauttaa kaikki jrjestelmn tuntemat puhelinnumerot jrjestettyn
 	 * PhoneNumber[] -taulukkona.
 	 * @return PhoneNumber[] -taulukko.
 	 */
 	public PhoneNumber[] getAllPhoneNumbers() {
 		Collection all = new Vector();
 		Iterator iter = this.phoneNumbers.values().iterator();
 		while(iter.hasNext()) {
 			all.addAll((Collection)iter.next());
 		}
 		PhoneNumber[] numbers = (PhoneNumber[]) all.toArray(new PhoneNumber[0]);
 		Arrays.sort(numbers);
 		return numbers;
 	}
 
 	/**
 	 * Pivitt tietokannassa olevan puhelinnumero-olion annetun mallin
 	 * mukaiseksi ja pivitt henkillle puhelinnumeron
 	 * 
 	 * @param phone Uusi versio puhelinnumerosta (uusi typiste id).
 	 * @return Onnistuiko pivitys.
 	 */
 	public boolean updatePhoneNumber(PhoneNumber phone) {
             
             String getpersons = "select HENKLO_HTUNNUS"
                                     + " from TYOPISTEVARAUS"
                                     + " where ALKUPVM<CURRENT_TIMESTAMP"
                                     + " AND LOPPUPVM>CURRENT_TIMESTAMP"
                                     + " AND TPISTE_ID=?";
                       
             boolean success = false;
             this.session.waitState(true);
             PreparedStatement prep;
             Post post = phone.getPost();
             String personID = phone.getPersonID();
             
             
             //tarkistus, jos typistenumero kytss, niin voi list yhden henkilnumeron
             try {
 		if(this.prepUpdatePhoneNumber == null) {
                     this.prepUpdatePhoneNumber = this.connection.prepareStatement("UPDATE PUHELINNUMERO SET tp_id  = ?, h_tunnus = ? WHERE id = ?");
 		}
 		if(post == null) {
                     this.prepUpdatePhoneNumber.setString(1, "");
 		} else {
                     this.prepUpdatePhoneNumber.setString(1, post.getPostID());
 		}
                 this.prepUpdatePhoneNumber.setString(2, personID);
 		this.prepUpdatePhoneNumber.setString(3, phone.getPhoneNumberID());
                 
                 int updatedRows = this.prepUpdatePhoneNumber.executeUpdate();
                 
 		if(updatedRows > 0) { // TODO tehdn jotenkin erilailla kun poistetaan puhelinnumero typisteest
                     success = true;
                    //Kun typisteelle varataan numero, jos knnykk ei lydet typisteen varanneilta henkililt, niin tyisteen numero pivitetn henkilille
                     if (post != null)    {
 
                         prep = this.connection.prepareStatement(getpersons);
                         prep.setString (1, post.getPostID());
                         ResultSet rs = prep.executeQuery();
                         while (rs.next()) {
                            if (this.findKannykka("HENKLO_HTUNNUS") == false) {
                                if(rs.getString("HENKLO_HTUNNUS")!=null) {
                                    this.updateWorkPhone(rs.getString("HENKLO_HTUNNUS"), phone.getPhoneNumber());
                                }                      
                             }
                         }
                     //Kun henkillle varataan numero
                      } else {
                         this.updateWorkPhone(personID, phone.getPhoneNumber());
                     }
                         /* XXX Raskas operaatio */    
                         loadRooms();
 
                         loadPhoneNumbers();
                 }         
             } catch (SQLException e) {
             	System.err.println("Tietokantavirhe: " + e.getMessage());
             }
             this.session.waitState(false);
             return success;
         }
     /* --- Puhelinnumeroihin liittyvt metodit loppuu --- */
         
     /* --- Avainvarauksiin liittyvt metodit alkaa --- */
         
     /**
      * Nerompi Lis Huonevaraus -tauluun uuden huonevarauksen
      *
      * @param reservation listtv huonevaraus
      */
     public void addRoomKeyReservation(Room room, Person person, TimeSlice timeslice) {
         String selectQuery = "SELECT * FROM huonevaraus";
         
         String updateQuery1 = "INSERT INTO HUONEVARAUS (ID, HTUNNUS, RHUONE_ID, ALKUPVM, LOPPUPVM) VALUES (?, ?, ?, ?, ?)"; // (SELECT MAX(ID) FROM HUONEVARAUS)+1
 
         String updateQuery2 = "INSERT INTO HUONEVARAUS (ID, HTUNNUS, RHUONE_ID, ALKUPVM, LOPPUPVM) VALUES ((SELECT MAX(ID) FROM HUONEVARAUS)+1, ?, ?, ?, ?)"; 
         
         PreparedStatement prep;
         try {
             ResultSet rs = this.connection.prepareStatement(selectQuery).executeQuery();
             if(!rs.next()) {
                 prep = this.connection.prepareStatement(updateQuery1);
                 prep.setInt(1, 1);
                 prep = this.connection.prepareStatement(updateQuery2);
                 prep.setString(2, person.getPersonID());
                 prep.setString(3, room.getRoomID());
                 prep.setDate(4, timeslice.getSQLStartDate());
                 prep.setDate(5, timeslice.getSQLEndDate());
                 prep.executeUpdate();
             }
             else {
                 prep = this.connection.prepareStatement(updateQuery2);
                 prep.setString(1, person.getPersonID());
                 prep.setString(2, room.getRoomID());
                 prep.setDate(3, timeslice.getSQLStartDate());
                 prep.setDate(4, timeslice.getSQLEndDate());
                 prep.executeUpdate();
             }
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
     }
     /**
      * Poistaa annetun avainvarauksen tietokannasta
      * @param id poistettavan avainvarauksen id
      */
     public void deleteRoomKeyReservation(int id) {
         String deleteQuery = "DELETE FROM huonevaraus where id=?";
 
         PreparedStatement prep;
 
         try {
             prep = this.connection.prepareStatement(deleteQuery);
             prep.setInt(1, id);
             prep.executeUpdate();
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
     }
     /**
      * Pivitt annetun avainvarauksen alku- ja loppupivmrn
      * @param roomKeyReservation muutettava avainvaraus
      * @return onnistuiko pivitys
      */
     public boolean modifyRoomKeyReservation(RoomKeyReservation roomKeyReservation) {
         String updateQuery = "UPDATE huonevaraus SET alkupvm=?, loppupvm=? where id=?";
         
         PreparedStatement prep;
         
         try {
             prep = this.connection.prepareStatement(updateQuery);
             prep.setDate(1, roomKeyReservation.getTimeSlice().getSQLStartDate());
             prep.setDate(2, roomKeyReservation.getTimeSlice().getSQLEndDate());
             prep.setInt(3, roomKeyReservation.getReservationID());
             prep.executeUpdate();
             return true;
         } catch(SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
             return false;
         }
     }
 
     /* --- Avainvarauksiin liittyvt metodit loppuu --- */
 
     /* --- Projekteihin liittyv�t metodit alkaa --- */ 
 
 	/**
 	 * Palauttaa kaikki j�rjestelm�n tuntemat projektit j�rjestettyn�
 	 * Project[] -taulukkona.
 	 * 
 	 * @return projektit <code>Project[]</code> oliona
 	 */
 	public Project[] getProjects() {
 		Project[] projs = (Project[]) projects.values().toArray(new Project[0]);
 		/* HashTable hukkaa j�rjestyksen, joten sortataan */
 		Arrays.sort(projs);
 		return projs;
 	}
 
 	/* --- Projekteihin liittyv�t metodit loppuu --- */ 
 
 	/* --- Muut metodit alkaa --- */ 
 
 	/**
 	 * Palauttaa tietokantayhteyden. K�yt� varovaisesti, tarkoitettu l�hinn�
 	 * testej� varten.
 	 * @return Yhteys <code>Connection</code> oliona.
 	 */
 	public Connection getConnection() {
 		return connection;
 	}
 
 	/**
 	 * NeroObserver-kuuntelija. K�yt�nn�ss� kuuntelee vain TIMESCALE ja ROOMS
 	 * tyyppej�, mutta ei tarkista mik� tyyppi vastaanotettiin.
 	 * @param type Kuuntelijatyyppi, ei vaikutusta. 
 	 * @see fi.helsinki.cs.nero.event.NeroObserver#updateObserved(int)
 	 */
 	public void updateObserved(int type) {
 		// Aikajakso tai huonetiedot ovat muuttuneet. Tiedot henkil�ist� eiv�t en��
 		// ole ajan tasalla.
 		//System.err.println("DB: heitet��n pois tiedot henkil�ist�");
 		people.clear();
 	}
         
 	/**
 	 * Main-metodi pienimuotoista testailua varten.
 	 * @param args Komentoriviparametrit.
 	 * @throws SQLException
 	 */
     public static void main(String[] args) throws SQLException {
 	NeroApplication.readIni(NeroApplication.DEFAULT_INI);
 	NeroDatabase ndb = new NeroDatabase(new Session(),
         	NeroApplication.getProperty("db_class"),
         	NeroApplication.getProperty("db_connection"),
 		NeroApplication.getProperty("db_username"),
 		NeroApplication.getProperty("db_password"));
 	/* tahtoo katsoa versiot, jotkut toimii ja jotkut ei. */
 	System.out.println(ndb.connection.getMetaData().getDriverVersion());
 	System.out.println(ndb.connection.getMetaData().getDatabaseProductVersion());
 	// testailus�l� poistettu, riippuvaista kannan vanhasta sis�ll�st�.
     	System.out.println("done.");
     }
     /**
      * palauttaa htunnukseen liittyvn knnykknumeron
      * @param htunnus
      * @return 
      */
     public String getKannykka(String htunnus) {
         
         String prep = "SELECT omistaja FROM KANNYKKA WHERE htunnus = ?";
         try {        
             PreparedStatement p = this.connection.prepareStatement(prep);
             p.setString(1, htunnus);
             ResultSet rs = p.executeQuery();
             
             rs.next();
             return rs.getString("omistaja");
             
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
         return null;
     
     }  
     public boolean findKannykka(String henklo_tunnus) {
         
         String getKannykka = "SELECT kannykka_numero FROM KANNYKKA WHERE htunnus = ?";
         try {
             PreparedStatement p = this.connection.prepareStatement(getKannykka);
             p.setString(1, henklo_tunnus);
             ResultSet rs = p.executeQuery();
            
            while (rs.next()) {
                if (rs.getString("kannykka_numero") != null)
                        return true;
             }
             
         } catch (SQLException e) {
            System.err.println("Tietokantavirhe: " + e.getMessage());
         }
         return false;
     }
     /**
      * Poistaa typisteelt puhelinnumeron
      *
      * @param phone Puhelinnumero, jolta poistetaan typiste
      * @return Onnistuiko pivitys
      */
     public boolean removePhoneNumberFromPost(PhoneNumber phone) {
         if (phone.getPost() == null) {
             throw new IllegalArgumentException();
         }
 
         String updatePhoneNumber = "UPDATE PUHELINNUMERO SET tp_id='' WHERE id=?";
        
 
         String getpersons = "select HENKLO_HTUNNUS"
                 + " from TYOPISTEVARAUS"
                 + " where ALKUPVM<CURRENT_TIMESTAMP"
                 + " AND LOPPUPVM>CURRENT_TIMESTAMP"
                 + " AND TPISTE_ID=?";
 
         boolean success = false;
         this.session.waitState(true);
         PreparedStatement prep;
 
         try {
             prep = this.connection.prepareStatement(updatePhoneNumber);
             prep.setString(1, phone.getPhoneNumberID());
             int updatedRows = prep.executeUpdate();
             if (updatedRows > 0) {
                 success = true;
                 prep = this.connection.prepareStatement(getpersons);
                 prep.setString(1, phone.getPost().getPostID());
                 ResultSet rs = prep.executeQuery();
                 while (rs.next()) {
                     //tarkistus thn, onko tunnuksella knnykk, jos ei niin pivit numero tyhjksi
                     if (!this.findKannykka(rs.getString("HENKLO_HTUNNUS"))) {
                         this.updateWorkPhone(rs.getString("HENKLO_HTUNNUS"), "");
                     }
 //                    if (phone.getPersonID() != null) {
 //                        this.updateWorkPhone(phone.getPersonID(), "");
 //                    }
                 }
                 /* XXX Raskas operaatio */
                 loadRooms();
                 loadPhoneNumbers();
             }
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
         this.session.waitState(false);
         return success;
     }
     public boolean removePhoneNumberFromPerson(PhoneNumber phone) {
         if (phone.getPersonID() == null) {
             System.out.println("henkil ei saa olla null");
             return false;
         }       
         
         PreparedStatement prep;
         boolean success = false;
         phone.getPersonID();
         
         
         String updatePhoneNumber = "UPDATE PUHELINNUMERO SET h_tunnus='' WHERE id=?";
         
         try {
             prep = this.connection.prepareStatement(updatePhoneNumber);
             prep.setString(1, phone.getPhoneNumberID());
             int updatedRows = prep.executeUpdate();
             if (updatedRows > 0) {
                 success = true;
                 this.updateWorkPhone(phone.getPersonID(), "");
                 /* XXX Raskas operaatio */
                 loadRooms();
                 loadPhoneNumbers();
             }
         } catch (SQLException e) {
             System.err.println("Tietokantavirhe: " + e.getMessage());
         }
         this.session.waitState(false);
         return success;
         
     }
     /**
      * Palauttaa annetun tyhuoneen puhelinnumerot.
      *
      * @param post Tyhuone <code>Post</code> oliona.
      * @return Puhelinnumerot <code>PhoneNumber[]</code> oliona.
      */
     public PhoneNumber[] getPhoneNumbers(Post post) {
         /*
          // Menisi kutakuinkin n�in jos k�ytett�isiin kantaa eik� omaa tietorakennetta
          Collection numbers = new Vector();
 
          try {
          if(this.prepPostPhoneNumbers == null) {
          this.prepPostPhoneNumbers = this.connection.prepareStatement(
          "SELECT id, puhelinnumero FROM PUHELINNUMERO WHERE tp_id = ?"
          );
          }
          this.prepPostPhoneNumbers.setString(1, post.getPostID());
          ResultSet rs = this.prepPostPhoneNumbers.executeQuery();
          while(rs.next()) {
          PhoneNumber pn = new PhoneNumber(this.session,
          rs.getString("id"), post,
          rs.getString("puhelinnumero"));
          numbers.add(pn);
          }
          rs.close();
          } catch (SQLException e) {
          System.err.println("Tietokantavirhe: " + e.getMessage());
          }
 		
          System.out.println("humpappaa " + numbers.size());
          return (PhoneNumber[]) numbers.toArray(new PhoneNumber[0]);
          */
         String key = "free";
         if (post != null) {
             key = post.getPostID();
         }
         Collection c = (Collection) this.phoneNumbers.get(key);
         if (c == null) {
             return new PhoneNumber[0];
         }
         //yritt tehd null collectionista arrayn
         PhoneNumber[] numbers = (PhoneNumber[]) c.toArray(new PhoneNumber[0]);
         Arrays.sort(numbers);
         return numbers;
     }
 
     /* --- Muut metodit loppuu --- */
 
 }
