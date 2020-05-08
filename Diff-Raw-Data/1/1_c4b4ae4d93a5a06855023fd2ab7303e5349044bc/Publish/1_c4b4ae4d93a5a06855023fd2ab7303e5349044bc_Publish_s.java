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
 package uk.ac.bolton.spaws.integration;
 
 import static org.junit.Assert.assertEquals;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import uk.ac.bolton.spaws.ParadataManager;
 import uk.ac.bolton.spaws.filter.NormalizingFilter;
 import uk.ac.bolton.spaws.model.INode;
 import uk.ac.bolton.spaws.model.IRating;
 import uk.ac.bolton.spaws.model.IReview;
 import uk.ac.bolton.spaws.model.ISubmission;
 import uk.ac.bolton.spaws.model.ISubmitter;
 import uk.ac.bolton.spaws.model.impl.Actor;
 import uk.ac.bolton.spaws.model.impl.Node;
 import uk.ac.bolton.spaws.model.impl.Rating;
 import uk.ac.bolton.spaws.model.impl.Review;
 import uk.ac.bolton.spaws.model.impl.Submission;
 import uk.ac.bolton.spaws.model.impl.Submitter;
 
 public class Publish {
 	
 	private static final String MIMAS_TEST_NODE_URL = "http://alpha.mimas.ac.uk";
 	private static final String WIDGET_URI = "http://wookie.apache.org/widgets/youtube";
 	private static INode node;
 		
 	@BeforeClass
 	public static void setup() throws Exception{
 		
 		node = new Node(new URL(MIMAS_TEST_NODE_URL), "fred", "flintstone");
 	
 		ParadataManager manager = new ParadataManager(new Submitter(), node);
 
 		List<ISubmission> submissions = new ArrayList<ISubmission>();
 		submissions.add(new Submission(new Actor("Bill"), new Rating(1), WIDGET_URI));
 		submissions.add(new Submission(new Actor("Amy"), new Rating(1), WIDGET_URI));
 		submissions.add(new Submission(new Actor("Chloe"), new Rating(1), WIDGET_URI));
 		
 		submissions.add(new Submission(new Actor("Dave"), new Review("Great"), WIDGET_URI));
 		
 		manager.publishSubmissions(submissions);
 		
 		Thread.sleep(20000);
 	}
 	
 	@Test
 	public void UpdateRatings() throws Exception{
 		
 		Submitter submitter = new Submitter();
 		ParadataManager manager = new ParadataManager(submitter, node);
 
 		List<ISubmission> submissions = new ArrayList<ISubmission>();
 		submissions.add(new Submission(new Actor("Bill"), new Rating(5), WIDGET_URI));
 		submissions.add(new Submission(new Actor("Amy"), new Rating(5), WIDGET_URI));
 		submissions.add(new Submission(new Actor("Chloe"), new Rating(5), WIDGET_URI));
 		
 		manager.publishSubmissions(submissions);
 		
 		Thread.sleep(20000);
 		
 		submissions = manager.getSubmissionsForSubmitter(submitter, WIDGET_URI, IRating.VERB);
 		
 		System.out.println("\nRESULTS");		
 		for (int i=0;i<submissions.size();i++){
 
 			ISubmission s = submissions.get(i);
 			System.out.println("Rating: "+((IRating)s.getAction()).getRating() + " From: " + s.getActor().getName() + " Date:"+s.getUpdated().toString());			
 		}
 		
 		assertEquals(5, ((IRating)submissions.get(0).getAction()).getRating());
 		assertEquals(5, ((IRating)submissions.get(1).getAction()).getRating());
 		assertEquals(5, ((IRating)submissions.get(2).getAction()).getRating());		
 
 	}
 	
 	@Test
 	public void filterBySubmitter() throws Exception{
 		ISubmitter submitter = new Submitter();
 		submitter.setSubmitter("SPAWS-TEST");
 		submitter.setSubmitterType("agent");
 		
 		ParadataManager manager = new ParadataManager(submitter, node);
 		
 		List<ISubmission> submissions = manager.getExternalRatingSubmissions(WIDGET_URI);
 
 		assertEquals(0, submissions.size());
 	}
 	
 	@Test
 	public void filterBySubmitter2() throws Exception{
 		ISubmitter submitter = new Submitter();
 		submitter.setSubmitter("NOBODY");
 		submitter.setSubmitterType("agent");
 		
 		ParadataManager manager = new ParadataManager(submitter, node);
 		List<ISubmission> submissions = manager.getExternalRatingSubmissions(WIDGET_URI);
 		
 		assertEquals(3, submissions.size());
 	}
 	
 	@Test
 	public void filterReviews() throws Exception{
 		ParadataManager manager = new ParadataManager(new Submitter(), node);
 		List<ISubmission> submissions = manager.getSubmissions(WIDGET_URI);
 		submissions = new NormalizingFilter(IReview.VERB).filter(submissions);
 		assertEquals(1, submissions.size());
 		assertEquals("Dave", submissions.get(0).getActor().getName());
 		assertEquals("Great", submissions.get(0).getAction().getContent());
		assertEquals(4, ((IRating)submissions.get(0).getAction()).getRating());
 	}
 }
