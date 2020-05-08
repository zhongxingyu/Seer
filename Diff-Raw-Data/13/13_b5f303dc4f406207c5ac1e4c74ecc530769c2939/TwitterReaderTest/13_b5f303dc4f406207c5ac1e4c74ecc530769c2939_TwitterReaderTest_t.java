 package unit;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 import glare.ClassFactory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import dal.ConfigurationReader;
 import dal.PictureData;
 import dal.TwitterReader;
 
 public class TwitterReaderTest {
 
 		private List<PictureData> pictures;
 		private TwitterReader tr;
 		
 		@Before
 		public void setUp() throws Exception {
 			pictures = new ArrayList<PictureData>();
 			tr = (TwitterReader) ClassFactory.getBeanByName("twitterReader");
 			
			pictures = tr.getPictures("twittermotjavatesting");
 		}
 		@After
 		public void tearDown() {
 			pictures = null;
 		}
 		
 		@Test
 		public void ReturnsListWithPictures(){
 			assertThat(pictures.size(), is(not(nullValue())));
 		}
 		@Test
 		public void FirstPictureHasUrlStandardRes(){
 			assertThat(pictures.get(0).getUrlStd(), is(not(nullValue())));
 		}
 		@Test
 		public void FirstPictureHasUrlThumbnailRes(){
 			assertThat(pictures.get(0).getUrlThumb(), is(not(nullValue())));
 		}
 		@Test
 		public void FirstPictureHasTime(){
 			assertThat(pictures.get(0).getTime(), is(not(nullValue())));
 		}
 		@Test
 		public void FirstPictureHasID(){
 			assertThat(pictures.get(0).getId(), is(not(nullValue())));
 		}
 		@Test
 		public void FirstPictureHasRemoveFlag(){
 			assertThat(pictures.get(0).isRemoveFlag(), is(false));
 		}
 		@Test
 		public void FirstPictureHasHashtag(){
 			assertThat(pictures.get(0).getHashtag(), is(not(nullValue())));
 		}
 }
