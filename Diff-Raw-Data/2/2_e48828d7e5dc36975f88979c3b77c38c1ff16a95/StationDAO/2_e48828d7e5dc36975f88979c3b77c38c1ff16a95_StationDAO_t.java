 package dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.Query;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import com.mysql.jdbc.PreparedStatement;
 
 import beans.Ligne;
 import beans.Station;
 
 public class StationDAO {
 
 	private Session se = null;
 	private List<Station> listeStation;
 	private Station station = new Station();
     private Set<Ligne> listLigne=new HashSet<Ligne>();
 
     
     public  void createStation(String nomStation , String commentaireStation, double latitude, double longitude, Ligne ligne) {
 	      
 		 
 		 se = HibernateUtils.getSession();
 	     Transaction t = se.beginTransaction();
 	    
 	     
 	     station.setNomStation(nomStation);
 	     station.setCommentaireStation(commentaireStation);
 	     station.setLatitude(latitude);
 	     station.setLongitude(longitude);
 	     //listLigne.add(ligne);
 	     station.setLigne(ligne);
     
 		 se.save(station);		
 	     t.commit();
 	     se.close();
 	}
     
     public  int createStationReturnId(String nomStation , String commentaireStation, double latitude, double longitude, Ligne ligne) {
 	      
 		 
 		 se = HibernateUtils.getSession();
 	     Transaction t = se.beginTransaction();
 	     
 	     station.setNomStation(nomStation);
 	     station.setCommentaireStation(commentaireStation);
 	     station.setLatitude(latitude);
 	     station.setLongitude(longitude);
 	     //listLigne.add(ligne);
 	     station.setLigne(ligne);
 	     int idStation = (Integer) se.save(station);		
 	     t.commit();
 	     se.close();
 	     return idStation;
 	}
     
     public  void createStationToStation(Station station1, Station station2) {
 	      
      
 	  
 	 	 se = HibernateUtils.getSession();       
     	 Transaction tr=se.beginTransaction(); 
     	 
 		 List<Station> listStation = station1.getStationAller();
        	     	
 		 listStation.add(station2);		
 		 List<Station> listStation2 = station2.getStationAller();
 		 
 		 listStation2.add(station1);
 	
 	
          station1.setStationAller(listStation);
     	 station2.setStationAller(listStation2);
     	 //station1.setStationRetour(listStation);
     	// station2.setStationRetour(listStation2);
 
 	     se.update(station1);
 	     se.update(station2);
 	
 
     	 tr.commit();
     	 se.close();
 	    
 	}
     
     
     public void supprimerStation(int id) {
     	
     	se = HibernateUtils.getSession();
     	se.beginTransaction();
         station = (Station) se.load(Station.class, id);
         se.delete(station);
         se.beginTransaction().commit();
         se.close();
     }
     
 	public Station getStationByID(int id) {
     	se = HibernateUtils.getSession();
     	se.beginTransaction(); 
     	
     	//station = (Station) se.createQuery("from station where id=?").setParameter(0, id).uniqueResult();
 
     	
     	//station = (Station) se.get(Station.class, id);
     	//se.getTransaction().commit();
    	station = (Station) se.createQuery("from Station where idStation="+id).uniqueResult();
     	se.close();
     	
         //return station;
         return station;
     }
 	
     public void modifierStation(Station station) {
   	  
     	se = HibernateUtils.getSession();       
     	Transaction tr=se.beginTransaction();
     	se.update(station);
     	tr.commit();
     	se.close();
     	 
     }
     
 	public List<Station> listerStation() {
     	se = HibernateUtils.getSession();
     	se.beginTransaction();  	 	
     	listeStation = se.createQuery("from Station").list();
     	
     	
     	
         return listeStation;
     }
 	 public List<Station> listerStationHasStationByListStation(int[] stationList) throws SQLException {
 	 se = HibernateUtils.getSession();
 	 Transaction t = se.beginTransaction();
 	 String listeStationStr="";
 	 
 	 
 		for(int i=0 ; i<=stationList.length-1 ; i++) {
 		
 				listeStationStr = listeStationStr+stationList[i] +",";
 				System.out.println("listerStationHasStationByListStation=" +stationList[i]);
 				
 		}//fin i
 		listeStationStr = listeStationStr.substring(0, listeStationStr.length()-1);
 		System.out.println("Rsultat ==>"+	listeStationStr);
 
 	String sql ="select * from station_has_station where station_idstation1 in ("+listeStationStr+") or station_idstation2 in ("+listeStationStr+")";	
 	System.out.println("SQL==>" +sql);
 
 	listeStation=se.createSQLQuery(sql).list();
 
 	 t.commit();
 	 return listeStation;
 	 }
     
 }
