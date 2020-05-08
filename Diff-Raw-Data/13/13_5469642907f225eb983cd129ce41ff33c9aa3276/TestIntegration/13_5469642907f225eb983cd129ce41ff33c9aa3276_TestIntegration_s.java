 
 
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
import tools.Algo;
import tools.AlgoCreationReseau;
import tools.AlgoDivTroncCanton;
 
 import dao.CantonDAO;
 import dao.GeolocalisationDAO;
 import dao.HibernateUtils;
 import dao.ReseauDAO;
 import dao.StationDAO;
 import dao.ZoneDAO;
 
 import beans.Canton;
 import beans.Ligne;
 import beans.Reseau;
 import beans.Station;
 import beans.Zone;
 import junit.framework.TestCase;
 
 public class TestIntegration extends TestCase {
 
 	Reseau reseau =new Reseau();
 	ReseauDAO reseau_dao= new ReseauDAO();
 	ZoneDAO zone_dao=new ZoneDAO();
 	Set<Zone> zonelist=new HashSet<Zone>();
 	Set<Ligne> lignelist=new HashSet<Ligne>();
 	StationDAO station_dao=new StationDAO();
 	CantonDAO canton_dao=new CantonDAO();
 	boolean test=false;
 
 	public TestIntegration (){
 	}
 	
 	public void testinter() {
 		System.out.println("dans testintegration");
 		Session se = null;
 		se = HibernateUtils.getSession();
 
 		Transaction t10 = se.beginTransaction();
 		Query create10=se.createSQLQuery("delete from canton");
 		create10.executeUpdate();
 		t10.commit();
 		
 		Transaction t = se.beginTransaction();
 		Query create=se.createSQLQuery("delete from station_has_station");
 		create.executeUpdate();
 		t.commit();
 
 		
 		Transaction t8 = se.beginTransaction();
 		Query create8=se.createSQLQuery("delete from station");
 		create8.executeUpdate();
 		t8.commit();
 		
 		Transaction t9 = se.beginTransaction();
 		Query create9=se.createSQLQuery("delete from parametrehoraire");
 		create9.executeUpdate();
 		t9.commit();
 		
 
 		Transaction t2 = se.beginTransaction();
 		Query create2=se.createSQLQuery("delete from ligne");
 		create2.executeUpdate();
 		t2.commit();
 		
 		Transaction t3 = se.beginTransaction();
 		Query create3=se.createSQLQuery("delete from lieu");
 		create3.executeUpdate();
 		t3.commit();
 		
 		Transaction t4 = se.beginTransaction();
 		Query create4=se.createSQLQuery("delete from geolocalisation");
 		create4.executeUpdate();
 		t4.commit();
 		
 		Transaction t5 = se.beginTransaction();
 		Query create5=se.createSQLQuery("delete from geolocalisation");
 		create5.executeUpdate();
 		t5.commit();
 		
 		Transaction t6 = se.beginTransaction();
 		Query create6=se.createSQLQuery("delete from zone");
 		create6.executeUpdate();
 		t6.commit();
 		
 		Transaction t7 = se.beginTransaction();
 		Query create7=se.createSQLQuery("delete from reseau");
 		create7.executeUpdate();
 		t7.commit();
 		
 		
 
 		System.out.println("fin sup de la base purger");
 		
 	
 
 		//ajout zone
 		int nbrha =1000;
 		int nbr = 3;
 		float surfac=(float) 8247384.6139979595;
 		//creer reseau
 		int  idreseau =reseau_dao.createReseauReturnId("reseau 1");
 		List<Reseau> listeReseau = reseau_dao.listerReseau();
 		System.out.println("idreseau" +idreseau);
 		Double latitude = 47.92278444035086;
 		Double longitude =1.9217491149902344;
 		Double latitudeb = 47.903109595807585;
 		Double longitudeb = 1.8712806701660156;
 
 		List<Zone>  listeZone = zone_dao.listerZone();
 		reseau=reseau_dao.getReseauByID(idreseau);
 		int idzone = zone_dao.createZonereturn(nbrha, nbr, surfac ,reseau);
 		System.out.println("idzone" +idzone);
 		Zone zone = zone_dao.getZoneByID(idzone);
 	
 		GeolocalisationDAO geolocalisation_dao=new GeolocalisationDAO();
 		geolocalisation_dao.createGeolocalisation(latitude, longitude , zone);
 		geolocalisation_dao.createGeolocalisation(latitudeb, longitudeb, zone);
 		listeZone=zone_dao.listerZone();
 		listeReseau= reseau_dao.listerReseau();
 		
 		reseau = reseau_dao.getReseauByID(idreseau);
 		AlgoCreationReseau algo = new AlgoCreationReseau();
 		Algo kruskal = new Algo();
 		algo.creerReseau(reseau);
 		kruskal.stationToStation(algo.creerReseau(reseau));
 		
 		int[] stationList =kruskal.getMatriceNomStation(algo.creerReseau(reseau));
 		System.out.println("taille int[] stationList " +stationList.length);
 
 		int[][]  listeStationAffichage;
 		try {
 			listeStationAffichage = station_dao.listerStationHasStationByListStation(stationList);
 		//.listerStation();
 
 		List<Station> listeStation = station_dao.listerStation();
 		
 		AlgoDivTroncCanton algodiv=new AlgoDivTroncCanton();
 
 		algodiv.decoupage(listeStationAffichage,stationList);
 
 		List<Canton> listeCanton = canton_dao.listerCantonParam(listeStationAffichage);
 		
 		if((listeReseau!=null) && (listeZone!=null) && (listeCanton!=null) && (listeStation!=null)){
 			test=true;
 		}
 		else{
 			test=false;
 		}
 		
 		System.out.println("boolean => " +test);
 		assertTrue(test);
 	
 
 		Transaction t11 = se.beginTransaction();
 		Query create11=se.createSQLQuery("delete from canton");
 		create11.executeUpdate();
 		t11.commit();
 		
 		Transaction t12 = se.beginTransaction();
 		Query create12=se.createSQLQuery("delete from station_has_station");
 		create12.executeUpdate();
 		t12.commit();
 
 		
 		Transaction t13 = se.beginTransaction();
 		Query create13=se.createSQLQuery("delete from station");
 		create13.executeUpdate();
 		t13.commit();
 		
 		Transaction t14 = se.beginTransaction();
 		Query create14=se.createSQLQuery("delete from parametrehoraire");
 		create14.executeUpdate();
 		t14.commit();
 		
 
 		Transaction t15 = se.beginTransaction();
 		Query create15=se.createSQLQuery("delete from ligne");
 		create15.executeUpdate();
 		t15.commit();
 		
 		Transaction t16 = se.beginTransaction();
 		Query create16=se.createSQLQuery("delete from lieu");
 		create16.executeUpdate();
 		t16.commit();
 		
 		Transaction t17 = se.beginTransaction();
 		Query create17=se.createSQLQuery("delete from geolocalisation");
 		create17.executeUpdate();
 		t17.commit();
 		
 		Transaction t18 = se.beginTransaction();
 		Query create18=se.createSQLQuery("delete from geolocalisation");
 		create18.executeUpdate();
 		t18.commit();
 		
 		Transaction t19 = se.beginTransaction();
 		Query create19=se.createSQLQuery("delete from zone");
 		create19.executeUpdate();
 		t19.commit();
 		
 		Transaction t20 = se.beginTransaction();
 		Query create20=se.createSQLQuery("delete from reseau");
 		create20.executeUpdate();
 		t20.commit();
 		
 		se.close();
 		
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 }
