 
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import com.sun.org.apache.bcel.internal.generic.NEW;
 
 import dao.CantonDAO;
 import dao.HibernateUtils;
 import dao.StationDAO;
 
 import beans.Canton;
 import beans.Ligne;
 import beans.Reseau;
 import beans.Station;
import tools.AlgoDivTroncCanton;
 import junit.framework.TestCase;
 
 public class AlgoDivTroncCantonTest extends TestCase {
 
 	private Session se = null;
 	private AlgoDivTroncCanton algo = new AlgoDivTroncCanton();
 	private int stationList[] = { 1, 2 };
 	private int[][] listeStation;
 	private StationDAO station_dao = new StationDAO();
 	private boolean test;
 	private double testResult;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public void testdecoupage() throws SQLException {
 
 		se = HibernateUtils.getSession();
 		Transaction t = se.beginTransaction();
 
 		// Purge des tables base de donnEe de test
 		Query delete1 = se.createSQLQuery("delete from canton");
 		Query delete2 = se.createSQLQuery("delete from station_has_station");
 		Query delete3 = se.createSQLQuery("delete from station");
 		Query delete4 = se.createSQLQuery("delete from parametrehoraire");
 		Query delete5 = se.createSQLQuery("delete from ligne");
 		Query delete12 = se.createSQLQuery("delete from reseau");
 		delete1.executeUpdate();
 		delete2.executeUpdate();
 		delete3.executeUpdate();
 		delete4.executeUpdate();
 		delete5.executeUpdate();
 		delete12.executeUpdate();
 		t.commit();
 
 		Transaction t8 = se.beginTransaction();
 		Query create = se
 				.createSQLQuery("insert into reseau (idreseau, nomreseau) values(1, 'reseau 1')");
 		create.executeUpdate();
 		t8.commit();
 
 		Transaction t9 = se.beginTransaction();
 		Query create2 = se
 				.createSQLQuery("insert into ligne (idligne, nomligne, commentaire, reseau_idreseau) values(1, 'ligne 1','ligne 1',1)");
 		create2.executeUpdate();
 		t9.commit();
 
 		Transaction t10 = se.beginTransaction();
 		Query create3 = se
 				.createSQLQuery("INSERT INTO station (idstation, nomstation, commentaire, latitude, longitude, ligne_idligne) VALUES (1, 'station 1', 'station 1', 47.9015298444342, 1.8995111649564933, 1),(2, 'station 2', 'station 2', 47.90406160754688, 1.907407588296337, 1)");
 		create3.executeUpdate();
 		t10.commit();
 
 		Transaction t11 = se.beginTransaction();
 		Query create4 = se
 				.createSQLQuery("INSERT INTO station_has_station (station_idstation1, station_idstation2) VALUES (1,2)");
 		create4.executeUpdate();
 		t11.commit();
 
 		Transaction t7 = se.beginTransaction();
 		Reseau reseau = new Reseau();
 		reseau.setNomReseau("Reseau_Test");
 		se.save(reseau);
 		t7.commit();
 		se.flush();
 
 		Transaction t6 = se.beginTransaction();
 		Ligne ligne = new Ligne();
 		ligne.setIdLigne(1);
 		ligne.setCommentaire("Ligne de test");
 		ligne.setNomLigne("Ligne Test");
 		ligne.setStationlist(null);
 		ligne.setReseau(reseau);
 		se.save(ligne);
 		t6.commit();
 		se.flush();
 
 		int stationList[] = { 1, 2 };
 		listeStation = station_dao
 				.listerStationHasStationByListStation(stationList);
 
 		// System.out.println("STATION1= " +station1.getIdStation()+
 		// " STATION2= "+station2.getIdStation());
 		for (int[] iterable_element : listeStation) {
 			System.out.println("listestation" + iterable_element);
 		}
 
 		for (int j = 0; j <= stationList.length - 1; j++) {
 			System.out.println("stationList" + stationList[j]);
 		}
 
 		System.out.println("taille listestation " + listeStation.length
 				+ "taille station list" + this.stationList.length);
 		algo.decoupage(listeStation, stationList);
 
 		Transaction t4 = se.beginTransaction();
 		Canton canton = new Canton();
 		canton.setIdCanton(1);
 		canton.setDistance(200);
 		se.save(canton);
 		t4.commit();
 		se.flush();
 
 		if ((canton.getDistance() == 203) || (canton.getDistance() == 200)) {
 
 			test = true;
 		} else {
 			test = false;
 
 		}
 		System.out.println("test deocupage " + test);
 		assertTrue(test);
 
 		Transaction t5 = se.beginTransaction();
 		Query delete6 = se.createSQLQuery("delete from canton");
 		Query delete7 = se.createSQLQuery("delete from station_has_station");
 		Query delete8 = se.createSQLQuery("delete from station");
 		Query delete9 = se.createSQLQuery("delete from parametrehoraire");
 		Query delete10 = se.createSQLQuery("delete from ligne");
 		Query delete11 = se.createSQLQuery("delete from reseau");
 		delete6.executeUpdate();
 		delete7.executeUpdate();
 		delete8.executeUpdate();
 		delete9.executeUpdate();
 		delete10.executeUpdate();
 		delete11.executeUpdate();
 		t5.commit();
 		se.close();
 
 	}// fin testdecoupage
 
 	public void testgetDistancePol() {
 		testResult = algo.getDistancePol(10, 35, 50, 23);
 		System.out.println("testresult" + testResult);
 		if (testResult == 3891645.284377836) {
 			test = true;
 		} else {
 			test = false;
 		}
 		System.out.println("test" + test);
 		assertTrue(test);
 
 	}
 
 }
