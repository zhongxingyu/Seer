 package ch.bli.mez.model.doa;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Prüft ob die MissionDAO Klasse korrekt funktioniert
  * 
  * @author dave
  * @version draft
  */
 public class MissionDAOTest {
 	
 	private MissionDAO instance;
	private Mission mission
 
 	@Before
 	public void setUp() throws Exception {
 		this.instance = new MissionDAO();
 		this.mission = new Mission("Orgel1", "small comment", true);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		this.instance = null;
 		this.mission = null;
 	}
 
 	/*
 	 * Prüft ob die Instanz erstellt wurde
 	 */
 	@Test
 	public void checkInstance() {
 		assertNotNull(instance);
 		assertNotNull(mission);
 	}
 	
 	/*
 	 * Prüft ob eine Mission im Model abgespeichert werden kann
 	 */
 	@Test
 	public void addMission(){
 		instance.addMission(mission);
 	}
 	
 	/* @@@ (internal comment) muss getestet werden, ob überhaupt eine Exception geworfen wird!!!
 	 * Prüft ob eine Exception geworfen wird, wenn keine Misson mitgegeben wird
 	 */
 	@Test(expected=Exception.class)
 	public void addNullMission(){
 		instance.addMission(null);
 	}
 
 }
