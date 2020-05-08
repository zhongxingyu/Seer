 /*
  *  (c) 2012 University of Bolton
  *  
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package uk.ac.bolton.spaws.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.util.Date;
 import java.util.Map;
 
 import org.junit.Test;
 
 import com.navnorth.learningregistry.LRActivity;
 
 import uk.ac.bolton.spaws.model.impl.Rating;
 
 public class RatingTest {
 	
 	@Test
 	public void contruct(){
 		Rating rating = new Rating(5);
 		assertEquals(5, rating.getRating());
 		assertEquals(0, rating.getMin());
 		assertEquals(5, rating.getMax());
 		assertEquals("rated", rating.getVerb());
 	}
 	
 	@Test
 	public void update(){
 		Rating rating = new Rating();
 		
 		rating.setMin(0);
 		rating.setMax(3);
 		rating.setRating(2);
 		
 		assertEquals(2, rating.getRating());
 		assertEquals(0, rating.getMin());
 		assertEquals(3, rating.getMax());
 		assertEquals("rated", rating.getVerb());
 	}
 
 	@Test
 	public void activity(){
 		LRActivity activity = new LRActivity("http://opera.com/widgets/bubbles", "SPAWS-TEST", "agent", null, "SPAWS-TEST", "SPAWS-TEST");
 		activity.addVerb(Rating.VERB, new Date(), null, null, null);
 		
 		Rating rating = new Rating(3);
 		rating.addMeasure(activity);
 		@SuppressWarnings("rawtypes")
 		Map map = (Map) ((Map) ((Map) ((Map) activity.getResourceData()).get("activity")).get("verb")).get("measure");
 		assertEquals(1, map.get("sampleSize"));
 		assertEquals(0, map.get("scaleMin"));
 		assertEquals(5, map.get("scaleMax"));
 		assertEquals(3, map.get("value"));
 	}
 	
 	@Test
 	public void content(){
 		Rating rating = new Rating();
		assertNull(rating.getContent());
 	}
 }
