 package de.tum.in.tumcampus.test;
 
 import java.util.Date;
 
 import android.test.ActivityInstrumentationTestCase2;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import de.tum.in.tumcampus.TumCampus;
 
 public class LinksTest extends ActivityInstrumentationTestCase2<TumCampus> {
 
 	private Solo solo; // simulates the user of the app
 
 	public LinksTest() {
 		super("de.tum.in.tumcampus", TumCampus.class);
 	}
 
 	public void setUp() {
 		solo = new Solo(getInstrumentation(), getActivity());
		solo.scrollDown();
 	}
 
 	public void testLinksList() {
 		assertTrue(solo.searchText("Links"));
 		solo.clickOnText("Links");
 
 		assertTrue(solo.searchText("Golem"));
 		assertTrue(solo.searchText("Heise"));
 
 		solo.clickOnText("Heise");
 	}
 
 	public void testLinksCreateDelete() {
 		assertTrue(solo.searchText("Links"));
 		solo.clickOnText("Links");
 
 		String name = "some name " + new Date();
 		solo.enterText(0, "http://www.heise.de");
 		solo.enterText(1, name);
 
 		solo.clickOnText("Hinzufgen");
 
 		assertTrue(solo.searchText(name));
 		solo.clickLongOnText(name);
 
 		assertTrue(solo.searchButton("Ja"));
 		solo.clickOnText("Ja");
 
 		assertFalse(solo.searchText(name));
 	}
 }
