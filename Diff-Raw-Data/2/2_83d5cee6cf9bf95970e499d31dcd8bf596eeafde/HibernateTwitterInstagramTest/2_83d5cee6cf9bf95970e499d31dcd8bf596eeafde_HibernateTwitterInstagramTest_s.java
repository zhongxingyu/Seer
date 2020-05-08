 package unit;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import glare.ClassFactory;
 import static org.hamcrest.CoreMatchers.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import dal.DatabaseHandler;
 import dal.PictureData;
 import dal.TwitterReader;
 
 public class HibernateTwitterInstagramTest {
 	private TwitterReader tr;
 	private List<PictureData> twitterPics;
 	private ArrayList<PictureData> listOfPics;
 
 	@Before
 	public void setUp() throws Exception {
 		tr = (TwitterReader) ClassFactory.getBeanByName("twitterReader");
 		twitterPics = new ArrayList<PictureData>();
 		twitterPics = tr.getPictures("#winter");
 		System.out.println(twitterPics.size());
 		for(PictureData pic1: twitterPics){
			System.out.println(pic1.getId() + " " + pic1.getUrlStd() + ": " + pic1.getUrlThumb() + " "
 					+ pic1.getCreatedTime() + " " + pic1.getHashtag() + " " + pic1.isRemoveFlag());
 		}
 		
 		for(PictureData pd : twitterPics){
 			DatabaseHandler.addPictureToDB(pd);
 		}
 		
 		
 		
 		listOfPics = (ArrayList<PictureData>) DatabaseHandler.listOfPicturesFromDB();
 		for(PictureData pic1: listOfPics){
 			System.out.println(pic1.getId() + " " + pic1.getUrlStd() + ": " + pic1.getUrlThumb() + " "
 					+ pic1.getCreatedTime() + " " + pic1.getHashtag() + " " + pic1.isRemoveFlag());
 		}
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		twitterPics = null;
 	}
 
 	@Test
 	public void InsertPicturesWithGivenHashTagInDB_ReturnListOfPicturesFromDB() {
 		assertThat(twitterPics.size(), is(listOfPics.size()));
 	}
 
 }
