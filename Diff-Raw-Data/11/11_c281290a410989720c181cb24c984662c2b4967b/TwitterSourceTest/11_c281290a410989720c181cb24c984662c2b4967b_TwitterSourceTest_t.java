 package test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import org.junit.Test;
 
 import dal.admin.ITwitterClient;
 import dal.admin.Image;
 import dal.admin.TwitterSource;
 
 
 public class TwitterSourceTest {
 	private class GenericClient implements ITwitterClient {
 		@Override
 		public String search(String keyword, long maxId) {
 			if (maxId == 0) {
 				return ""+
 					"{'statuses': [{ " +
 					"    'id': 1," +
 					"    'text': 'Description for this image',"+
 					"    'created_at': 'Fri Apr 09 12:53:54 +0000 2010',"+
 					"    'entities': {"+
 					"        'media': [{"+
 					"            'type': 'photo',"+
 					"            'media_url': 'http://example.com/image.jpg',"+
 					"            'id': '234252'"+
 					"        }]"+
 					"    }"+
 					"}, {"+
 					"    'id': 2,"+
 					"    'text': 'Another image',"+
 					"    'created_at': 'Fri Apr 09 15:53:54 +0000 2010',"+
 					"    'entities': {"+
 					"        'media': [{"+
 					"            'type': 'photo',"+
 					"            'media_url': 'http://example.com/image2.jpg',"+
 					"            'id': '23421'"+
 					"        }]"+
 					"    }"+
 					"}]}";
 			} else {
 				return "{'statuses': []}";
 			}
 		}
 	}
 	
 	@Test
 	public void GetByKeyword() {
 		TwitterSource source = new TwitterSource(new GenericClient());
 		
 		List<Image> images = source.getByKeyword("Painting", 10);
 		assertEquals(2, images.size());
 		
 		assertEquals("Got the URL from the first object",
 					 "http://example.com/image.jpg", images.get(0).getUrl());
 
 		assertEquals("Got the URL from the second object",
 					 "http://example.com/image2.jpg", images.get(1).getUrl());
 
		assertEquals("Got the ID as an long",
					 1L, images.get(0).getID());
 
 		assertEquals("Got correct parsed date",
 		 			 1270817634000L, images.get(0).getCreatedTime().getTime());
 
 		assertEquals("Got correct description",
 					 "Another image", images.get(1).getDescription());
 	}
 }
