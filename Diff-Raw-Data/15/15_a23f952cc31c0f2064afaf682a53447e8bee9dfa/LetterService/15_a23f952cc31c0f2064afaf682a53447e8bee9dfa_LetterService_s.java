 /**
  * 
  */
 package ru.meowth.services;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import ru.meowth.domain.Letter;
 
 
 /**
  * @author meowth
  *
  */
 public class LetterService {
 	
 	/**
 	 * Saves letter to database
 	 * @param letter
 	 */
 	public void saveLetter(Letter letter) {
 		PersistenceManager pm =  PMF.get().getPersistenceManager();		
 		try{
 			pm.makePersistent(letter);			
 			getStatisticsObject(pm).incrementLettersQueued();
 			pm.flush();
 		}
 		finally {			
 			pm.close();			
 		}		
 	}
 	
 	/**
 	 * Returns list of letters to send, removing them from database
 	 * @return
 	 */
 	public List<Letter> getLettersToSend() {
 		PersistenceManager pm =  PMF.get().getPersistenceManager();
 		try
 		{
 			String query = "select from " + Letter.class.getName() + " where whenToDeliver < :now";
 			Query q = pm.newQuery(query);
 			q.declareImports("import java.util.Date");
 					
 			@SuppressWarnings("unchecked")		
 			List<Letter> letters = (List<Letter>)q.execute(new Date());
			pm.makeTransientAll(letters);		
			q.deletePersistentAll();
			
 			getStatisticsObject(pm).addLettersSent(letters.size());
 			return letters;
 		} 
		finally {
 			pm.close();
 		}		
 	}
 	
 	public int getLettersSentCount(PersistenceManager pm) {
 		return getStatisticsObject(pm).getLettersSent();	
 	}
 	
 	public int getLettersInQueue(PersistenceManager pm) {
 		return getStatisticsObject(pm).getLettersQueued();	
 	}
 	
 	private static Statistics getStatisticsObject(PersistenceManager pm) throws IllegalArgumentException{				
 		String query = "select from " + Statistics.class.getName();
 		Query q = pm.newQuery(query);
 		@SuppressWarnings("unchecked")
 		List<Statistics> stats = (List<Statistics>)q.execute();
 		if(stats.size()> 1) {
 			throw new IllegalArgumentException();
 		}
 		for(Statistics s : stats) {
 			return s;
 		}
 		
 		Statistics s = new Statistics();
 		pm.makePersistent(s);			
 		return s;		
 	}
 }
