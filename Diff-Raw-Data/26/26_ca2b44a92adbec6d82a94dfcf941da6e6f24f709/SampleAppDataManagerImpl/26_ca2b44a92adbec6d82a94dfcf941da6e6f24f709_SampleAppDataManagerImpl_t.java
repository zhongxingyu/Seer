 package sampleapp;
 
 import java.util.List;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import sampleapp.entities.Person;
 import edu.upenn.bbl.common.jpa.JpaDataManager;
 
 public class SampleAppDataManagerImpl extends JpaDataManager implements SampleAppDataManager {
 
 	private static final String PERSISTENCE_UNIT_NAME = "sampleapp-persistence";
 	
 	private static EntityManagerFactory _emf =
 		Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
 
 	@Override
 	protected EntityManagerFactory getEmf() {
 		return _emf;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Person> getPeople() {
		return getEm().createNamedQuery(Person.GET_ALL).getResultList();
 	}
 	
 }
