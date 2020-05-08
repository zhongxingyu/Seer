 import org.junit.*;
 import static org.junit.Assert.*;
 import java.util.Calendar;
 import java.util.Set;
 import java.util.TreeSet;
 
 
 
public class MeetingTest{
 	
 	private int testSize;
	private Meeting[] testMeeting;
 	private Set<Contact> testContacts;
 	private Calendar[] calArray;
 	private String[] testNotes;
 	
 	@Before
 	public void initialise(){
 		testSize = 100;
 		testMeeting = new PastMeetingImpl[testSize];
 		//create an array of notes
 		testNotes = new String[testSize];
 		//Create a set of contacts
 		testContacts = new TreeSet<Contact>();
 		for(int c = 0;c < 10;c++){
 			testContacts.add(new ContactImpl(c,"" + c));
 		}
 		//Add test dates to the meetings. Store the dates in an
 		//array.
 		calArray = new Calendar[testSize];
 		for(int c = 0;c<testSize;c++){
 			
 			//Create a random date
 			int year = (int)(Math.random() * 5)+2013;
 			int month = (int)(Math.random() * 12)+ 1;
 			int day = (int)(Math.random() * 28)+ 1;
 			calArray[c] = Calendar.getInstance();
 			calArray[c].set(year,month,day);
			testNotes[c]=Math.random().toString();
			testMeeting[c] = new MeetingImpl(c,(Calendar)(calArray[c].clone()),testContacts,testNotes[c]);
 			
 		}
 	}
 	
 	
 	@Test
 	public void testsGetNotes(){
 		for(int c = 0;c<testSize;c++){
 			assertEquals(testNotes[c],testMeeting[c].getNotes());
 		}
		
 	
 	
 	@After
 	public void cleanUp(){
 		testSize = 0;
 		testMeeting = null;
 		testContacts = null;
 		calArray = null;
 	}
 }
 	
 	
 	
