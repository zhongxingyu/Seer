 package unit;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 import org.junit.*;
 
 import resources.DatabaseManagerDummy;
 
 import glare.ClassFactory;
 
 import java.util.*;
 
 import bll.*;
 import dal.*;
 
 public class GetPictureDataAndSaveToDbTest {
 
 	
 	@Before
 	public void setUp() {		
 	}
 	
 
 	@After
 	public void tearDown() {
 	}
 
 	@Test
 	public void SearchPictureData_Instagram_And_Twitter_WhenCalled_SavePictureDataToDb() {
 
 		// DUMMY CHECK
 		DatabaseHandler dbHandler  = new DatabaseHandler();
 		DatabaseManagerDummy dbMan = new DatabaseManagerDummy(dbHandler);
 		PictureController picCtrl  = new PictureController(dbMan);
 
 		// Do some dummy stuff
 		List<String> sourcesDummy;
 		Set<String> hashtagsDummy;
 
 		// Set dummy sources
 		sourcesDummy = new ArrayList<String>();
 		sourcesDummy.add("instagram");
 		sourcesDummy.add("twitter");
 
 		// Set test data for source and hashtag
 		hashtagsDummy = new HashSet<String>();
 		hashtagsDummy.add("twittermotjavatesting");
 		hashtagsDummy.add("raskebriller");
 
 		// Set dummy sources and hashtag
 		dbMan.setSources(sourcesDummy);
 		dbMan.setHashtags(hashtagsDummy);		
 		
 //		PictureController picCtrl = (PictureController) ClassFactory.getBeanByName("pictureController");
 //		DatabaseManager dbMan     = (DatabaseManager) ClassFactory.getBeanByName("databaseManager");
 
 		// Set test data for source and hashtag
 //		Set<String> hashtagsDummy = new HashSet<String>();
 //		hashtagsDummy.add("twittermotjavatesting");
 //		hashtagsDummy.add("raskebriller");
 //		dbMan.addHashtags(hashtagsDummy);
 		
		picCtrl.getNewPictureData();
 		
 		List<PictureData> pictureData = dbMan.getPictureDataFromDb();
 
 		// Additional output
 		System.out.println("PictureData from Database");
 		for ( PictureData pd : pictureData ) {
 			System.out.println(pd.getId());
 			System.out.println(pd.getUrlStd());
 		}
 		System.out.println("");
 		
 		assertThat(pictureData.isEmpty(),is(false));
 	}
 }
