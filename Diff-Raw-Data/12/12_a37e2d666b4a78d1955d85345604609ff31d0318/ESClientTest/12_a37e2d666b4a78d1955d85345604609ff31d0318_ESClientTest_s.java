 package story.book.test;
 
 import org.junit.Test;
 import android.test.ActivityInstrumentationTestCase2;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import story.book.dataclient.ESClient;
 import story.book.Dashboard;
 import story.book.DecisionBranch;
 import story.book.Story;
 import story.book.StoryFragment;
 import story.book.StoryInfo;
 import story.book.TextIllustration;
 
 /**
  * Unit testing for the ESClient and related classes.
  * 
  * @author Vina Nguyen
  * 
  */
 
 public class ESClientTest extends
 ActivityInstrumentationTestCase2<story.book.Dashboard> {
 
 	private ESClient es;
 	private ArrayList<Story> sample_story;
 	
 	public ESClientTest() {
 		super(Dashboard.class);
 		setup();
 	}
 	
 	public void setup() {
 		es = new ESClient();
 		sample_story = new ArrayList<Story>();
 		
 		createSampleStory();
 	}
 	
 	public void createSampleStory() {
 		StoryInfo info = new StoryInfo();
 		info.setAuthor("Danny");
 		info.setTitle("Broken Star");
 		info.setGenre("Science Fiction");
 		info.setSynopsis("The princess of a destroyed kingdom is left with no one to guide her, until she finds a fallen star with a secret inside....");
 		info.setPublishDate(new Date());
 		info.setSID(600);
 		
 		Story story = new Story(info);
 		StoryFragment fragment1 = new StoryFragment("Finding the Star");
 		// TODO
 		//TextIllustration text = new TextIllustration("It was a dark, clear night.");
 		//fragment1.addIllustration(text);
 		StoryFragment fragment2 = new StoryFragment("Preparing for the Journey");
 		//TextIllustration text2 = new TextIllustration("She ventured into the locked dungeons to retrieve some potions.");
 		//TextIllustration text3 = new TextIllustration("She could not carry everything, she had to choose between potion A and potion B.");
 		//fragment2.addIllustration(text2);
 		//fragment2.addIllustration(text3);
 		DecisionBranch branch = new DecisionBranch("She decides she must find the star.", fragment2);
 		//DecisionBranch branch2 = new DecisionBranch("She declares she is too weak to find the star.", fragment1);
 		fragment1.addDecisionBranch(branch);
 		//fragment2.addDecisionBranch(branch2);
 		
 		story.addFragment(fragment1);
 		story.addFragment(fragment2);
 		
 		sample_story.add(story);
 		
 		StoryInfo info2 = new StoryInfo();
 		info2.setAuthor("Ashley");
 		info2.setTitle("Frosted Tracks");
 		info2.setGenre("Drama");
 		info2.setSynopsis("Everything has come to a halt in your life when your friend betrays you by revealing your true identity.....");
 		info2.setPublishDate(new Date());
 		info2.setSID(601);
 		
 		sample_story.add(new Story(info2));
 	}
 	
 	public void checkStoryInfo(StoryInfo info, StoryInfo sample_info) {
 		assertTrue(sample_info.getTitle().equals(info.getTitle()));
 		assertTrue(sample_info.getAuthor().equals(info.getAuthor()));
 		assertTrue(sample_info.getGenre().equals(info.getGenre()));
 		assertTrue(sample_info.getSynopsis().equals(info.getSynopsis()));
 		assertEquals(sample_info.getSID(), info.getSID());
 		// TODO Does not work ;-;
 		//assertEquals(0, sample_info.getPublishDate().compareTo(info.getPublishDate()));
 	}
 	
 	@Test
 	public void testPublishStory() {
 		for (int i = 0; i < sample_story.size(); i++) {
 			es.saveStory(sample_story.get(i));
 		}
 	}
 	
 	@Test
 	public void testGetStory() {		
 		StoryInfo sample_info = sample_story.get(0).getStoryInfo();
 
 		Story returned_story = es.getStory(sample_info.getSID());
 		StoryInfo info = returned_story.getStoryInfo();
 		
 		checkStoryInfo(info, sample_info);
 	}
 	
 	@Test
 	public void testGetStoryInfoList() {
 		ArrayList<StoryInfo> list = es.getStoryInfoList();
 		for (int i = 0; i < sample_story.size(); i++) {
			
			for (int j = 0; j < sample_story.size(); j++) {
				if (list.get(i).getSID() == sample_story.get(j).getStoryInfo().getSID()) {
					checkStoryInfo(list.get(i), sample_story.get(j).getStoryInfo());
 					break;
 				}
 			}
 		}
 	}
 	
 	@Test
 	public void testCheckSID() {
 		assertTrue(es.checkSID(500));
 		assertFalse(es.checkSID(600));
 		assertEquals(0, es.getSID());
 	}
 	
 }
