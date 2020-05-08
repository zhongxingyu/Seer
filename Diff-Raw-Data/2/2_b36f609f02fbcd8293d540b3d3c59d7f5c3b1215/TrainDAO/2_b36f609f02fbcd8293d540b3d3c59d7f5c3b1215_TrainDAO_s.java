 package dao;
 
 
 import java.util.*;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import beans.Ligne;
 import beans.Reseau;
 import beans.Train;
 
 public class TrainDAO {
 	
 	private Session se = null;
 	private List<Train> listeTrain;
     private Train train;
    
     public TrainDAO() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
     public void createTrain(String nomTrain, Double latitudeTrain,
 			Double longitudeTrain, int etat, Ligne ligne) {
 
 		se = HibernateUtils.getSession();
 		Transaction t = se.beginTransaction();
 		Train train = new Train();
 		train.setNomTrain(nomTrain);
 		train.setLatitudeTrain(latitudeTrain);
 		train.setLongitudeTrain(longitudeTrain);
 		train.setEtat(etat);
 		train.setLigne(ligne);
 		se.save(train);
 
 		t.commit();
 		se.close();
 	}
 
 	public  int  createTrainReturnId(String nomTrain, Double latitudeTrain,
 			Double longitudeTrain, int etat, Ligne ligne) {
 		 
 		 se = HibernateUtils.getSession();
 		Transaction t = se.beginTransaction();
 		Train train = new Train();
 		train.setNomTrain(nomTrain);
 		train.setLatitudeTrain(latitudeTrain);
 		train.setLongitudeTrain(longitudeTrain);
 		train.setEtat(etat);
 		train.setLigne(ligne);
 		se.save(train);
 
 		int idTrain = (Integer) se.save(train);
 
 		t.commit();
 		se.close();
 
 		return idTrain;
 		
 	}
 	public List<Train> listerTrain() {
     	se = HibernateUtils.getSession();
     	se.beginTransaction();  	 	
     	listeTrain = se.createQuery("from Train").list();
         return listeTrain;
     }
 	
 	public List<Train> listerTrainByLigne(int idLigne) {
 		se = HibernateUtils.getSession();
     	se.beginTransaction();  	 	
     	listeTrain = se.createQuery("from Train where ligne_idligne="+idLigne).list();
         return listeTrain;
     }
 	
 
     public Train getTrainByID(int id) {
     	se = HibernateUtils.getSession();
     	se.beginTransaction(); 
     	
    	train = (Train) se.createQuery("from Train where ligne_idligne="+id).uniqueResult();
     	 se.close();
     	
         return train;
     }
 
 
 }
