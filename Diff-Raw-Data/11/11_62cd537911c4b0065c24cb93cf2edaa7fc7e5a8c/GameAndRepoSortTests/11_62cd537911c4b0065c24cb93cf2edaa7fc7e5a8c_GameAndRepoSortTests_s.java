 package com.qeevee.gq.tests.ui;
 
 import static com.qeevee.gq.tests.util.TestUtils.getFieldValue;
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.qeevee.gq.history.History;
 import com.qeevee.gq.tests.robolectric.GQTestRunner;
 import com.qeevee.gq.tests.util.FileUtilities;
 import com.qeevee.gq.tests.util.TestUtils;
 import com.qeevee.util.location.Distance;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import edu.bonn.mobilegaming.geoquest.GameListActivity;
 import edu.bonn.mobilegaming.geoquest.GeoQuestApp;
 import edu.bonn.mobilegaming.geoquest.GeoQuestProgressHandler;
 import edu.bonn.mobilegaming.geoquest.RepoListActivity;
 import edu.bonn.mobilegaming.geoquest.SortingOptionsActivity;
 import edu.bonn.mobilegaming.geoquest.Start;
 import edu.bonn.mobilegaming.geoquest.Variables;
 import edu.bonn.mobilegaming.geoquest.gameaccess.GameDataManager;
 import edu.bonn.mobilegaming.geoquest.gameaccess.GameItem;
 
 @RunWith(GQTestRunner.class)
 public class GameAndRepoSortTests {
 	
 	private RepoListActivity repoList;
 	private GeoQuestApp theApp;
 	private Start start;
 	private File repoPath;
 	private GameListActivity gameListActivity;
 	private SortingOptionsActivity sortingOptionsActivity;
 
     @After
     public void cleanUp() {
 	// get rid of all variables that have been set, e.g. for checking
 	// actions.
 	Variables.clean();
 	History.getInstance().clear();
 	FileUtilities.deleteDirectory(repoPath);
     }
 
     @Before
     public void prepare() {
     	start = new Start();
     	theApp = (GeoQuestApp) start.getApplication();
     	theApp.onCreate();
     	repoPath = GameDataManager.getLocalRepoDir(null);
     }
 
 
     // === TESTS FOLLOW =============================================
 
     @SuppressWarnings("unchecked")
     @Test
     public void testRepolistSortingByName(){
     	
     	// GIVEN: 3 repos
     	copyRepositoryToRightLocation("threeRepo");
     	
 
     	// WHEN: loading repodata
     	ProgressBar downloadRepoDataProgress = (ProgressBar) getFieldValue(start,"downloadRepoDataProgress");
     	TextView gameListProgressDescr = (TextView) getFieldValue(start,"gameListProgressDescr");
     	
     	final GeoQuestProgressHandler loadRepoDataHandler = new GeoQuestProgressHandler(
     		downloadRepoDataProgress, gameListProgressDescr,
     		GeoQuestProgressHandler.LAST_IN_CHAIN);
     	GeoQuestApp.loadRepoData(loadRepoDataHandler);
     	repoList = new RepoListActivity();
     	repoList.onCreate(null);
 
     	// THEN: repos should be sorted by name
     	
     	int noOfRepos = repoList.getListAdapter().getCount();
     	for(int i = 0; i<noOfRepos-1; i++){
     		String first = (String) repoList.getListAdapter().getItem(i);
     		String second = (String) repoList.getListAdapter().getItem(i+1);
     		assertTrue(first.compareToIgnoreCase(second)<=0);
     	} 
     	cleanUp();
     }
     
     @SuppressWarnings("unchecked")
     @Test
     public void testRepolistSortingByNameWithOnlyOneRepo(){
     	
     	// GIVEN: 1 repo
     	copyRepositoryToRightLocation("oneRepoOneGameWithLocation");
     	
 
     	// WHEN: loading repodata
     	ProgressBar downloadRepoDataProgress = (ProgressBar) getFieldValue(start,"downloadRepoDataProgress");
     	TextView gameListProgressDescr = (TextView) getFieldValue(start,"gameListProgressDescr");
     	
     	final GeoQuestProgressHandler loadRepoDataHandler = new GeoQuestProgressHandler(
     		downloadRepoDataProgress, gameListProgressDescr,
     		GeoQuestProgressHandler.LAST_IN_CHAIN);
     	GeoQuestApp.loadRepoData(loadRepoDataHandler);
     	repoList = new RepoListActivity();
     	repoList.onCreate(null);
 
     	// THEN: one repo should be in the repolist
     	String gameName = (String) repoList.getListAdapter().getItem(0);
     	assertTrue(gameName.equals("MyTestGames"));
     	cleanUp();
     }
     
     @SuppressWarnings("unchecked")
     @Test
     public void testGamelistSortingByName(){
     	
     	// GIVEN: repo with 5 games
     	copyRepositoryToRightLocation("oneRepoMultiGames");
         
         
     	//WHEN: Selecting "sort by name" in SortingOptionsActivity
     	excecuteSorting(GameItem.SORT_GAMELIST_BY_NAME);
     	
     	//THEN: gamelist should be sorted by name
     	int noOfGames = gameListActivity.getListAdapter().getCount();
     	for(int i = 0; i<noOfGames-1; i++){
     		String first = (String) gameListActivity.getListAdapter().getItem(i);
     		String second = (String) gameListActivity.getListAdapter().getItem(i+1);
     		assertTrue(first.compareToIgnoreCase(second)<=0);
     	}   
     	cleanUp();
     }
     
     @SuppressWarnings("unchecked")
     @Test
     public void testGamelistSortingByDate(){
     	
     	// GIVEN: repo with 5 games
     	copyRepositoryToRightLocation("oneRepoMultiGames");
         
         
     	//WHEN: Selecting "sort by date" in SortingOptionsActivity
     	excecuteSorting(GameItem.SORT_GAMELIST_BY_DATE);
     	
     	//THEN: gamelist should be sorted by date
     	int noOfGames = gameListActivity.getListAdapter().getCount();
     	
     	for(int i = 0; i<noOfGames-1; i++){
     		
     		String first = (String) gameListActivity.getListAdapter().getItem(i);
     		double firstDateClient = GeoQuestApp.getGameItem("MyTestGames", first).getLastmodifiedClientSide();
     		double firstDateServer = GeoQuestApp.getGameItem("MyTestGames", first).getLastmodifiedServerSide();
     		String second = (String) gameListActivity.getListAdapter().getItem(i+1);
     		double secondDateClient = GeoQuestApp.getGameItem("MyTestGames", second).getLastmodifiedClientSide();
     		double secondDateServer = GeoQuestApp.getGameItem("MyTestGames", second).getLastmodifiedServerSide();
 
     		assertTrue(firstDateServer>=secondDateServer);
     		assertTrue(firstDateClient>=secondDateClient);
     	}  
     	cleanUp();
     }
     
     @SuppressWarnings("unchecked")
     @Test
     public void testGamelistSortingByDistanceWithLocation(){
     	
     	// GIVEN: repo with 5 games
     	copyRepositoryToRightLocation("oneRepoMultiGames");
         
         
     	//WHEN: Selecting "sort by distance" in SortingOptionsActivity
     	excecuteSorting(GameItem.SORT_GAMELIST_BY_DISTANCE);
     	
     	//THEN: gamelist should be sorted by distance 
     	ListView lvWithLoc = (ListView) (getFieldValue(gameListActivity, "lv2"));
     	int noOfGames = lvWithLoc.getAdapter().getCount();
 		double deviceLat = 0;
 		double deviceLong = 0;
 		
     	for(int i = 0; i<noOfGames-1; i++){
     		
     		String first = (String) lvWithLoc.getAdapter().getItem(i);
     		double firstLat = GeoQuestApp.getGameItem("MyTestGames", first).getLatitude();
     		double firstLong = GeoQuestApp.getGameItem("MyTestGames", first).getLongitude();
     		String second = (String) lvWithLoc.getAdapter().getItem(i+1);
     		double secondLat = GeoQuestApp.getGameItem("MyTestGames", second).getLatitude();
     		double secondLong = GeoQuestApp.getGameItem("MyTestGames", second).getLongitude();
     		double distanceFirst = Distance.distance(firstLat, firstLong, deviceLat, deviceLong);
     		double distanceSecond = Distance.distance(secondLat, secondLong, deviceLat, deviceLong);
     		
     		assertTrue(distanceFirst<=distanceSecond && distanceFirst > 0);
     	}   
     	cleanUp();
     }
     
     @SuppressWarnings("unchecked")
     @Test
     public void testGamelistSortingByDistanceWithoutLocation(){
     	
     	// GIVEN: repo with 5 games
     	copyRepositoryToRightLocation("oneRepoMultiGames");
         
         
     	//WHEN: Selecting "sort by distance" in SortingOptionsActivity
     	excecuteSorting(GameItem.SORT_GAMELIST_BY_DISTANCE);
     	
     	//THEN: gamelist should be sorted by distance 
     	int noOfGames = gameListActivity.getListAdapter().getCount();
		double deviceLat = 0;
		double deviceLong = 0;
 		
     	for(int i = 0; i<noOfGames-1; i++){  
     		String gameName = (String) gameListActivity.getListAdapter().getItem(i);
     		assertTrue(GeoQuestApp.getGameItem("MyTestGames", gameName).getLatitude() == 0 && GeoQuestApp.getGameItem("MyTestGames", gameName).getLongitude() == 0);
     	}   
     	cleanUp();
     }
     
     
     @SuppressWarnings("unchecked")
     @Test
     public void testGamelistSortingWithOnlyOneGameWithLocation(){
     	
     	// GIVEN: repo with 1 game
     	copyRepositoryToRightLocation("oneRepoOneGameWithLocation");
        
     	//THEN: sorting should work too
     	excecuteSorting(GameItem.SORT_GAMELIST_BY_DISTANCE);
     	ListView lvWithLoc = (ListView) (getFieldValue(gameListActivity, "lv2"));
     	String gameName = (String) lvWithLoc.getAdapter().getItem(0);
     	assertTrue(gameName.equals("AudioRecordTest")); 
     	assertTrue(gameListActivity.getListAdapter().getCount() == 0);
     	excecuteSorting(GameItem.SORT_GAMELIST_BY_NAME);
     	gameName = (String) gameListActivity.getListAdapter().getItem(0);
     	assertTrue(gameName.equals("AudioRecordTest")); 
     	excecuteSorting(GameItem.SORT_GAMELIST_BY_DATE);
     	gameName = (String) gameListActivity.getListAdapter().getItem(0);
     	assertTrue(gameName.equals("AudioRecordTest")); 
     	cleanUp();
     }
     
     @SuppressWarnings("unchecked")
     @Test
     public void testGamelistSortingWithOnlyOneGameWithoutLocation(){
     	
     	// GIVEN: repo with 1 game
     	copyRepositoryToRightLocation("oneRepoOneGameWithoutLocation");
        
     	//THEN: sorting should work too
     	System.out.println("should have no loc!");
     	excecuteSorting(GameItem.SORT_GAMELIST_BY_DISTANCE);
     	ListView lvWithLoc = (ListView) (getFieldValue(gameListActivity, "lv2"));
     	String gameName = (String) gameListActivity.getListAdapter().getItem(0);
     	assertTrue(gameName.equals("AudioRecordTest"));
     	assertTrue(lvWithLoc.getCount() == 0); 
     	excecuteSorting(GameItem.SORT_GAMELIST_BY_NAME);
     	gameName = (String) gameListActivity.getListAdapter().getItem(0);
     	assertTrue(gameName.equals("AudioRecordTest")); 
     	excecuteSorting(GameItem.SORT_GAMELIST_BY_DATE);
     	gameName = (String) gameListActivity.getListAdapter().getItem(0);
     	assertTrue(gameName.equals("AudioRecordTest")); 
     	cleanUp();
     }
     
     //helper functions---------------------------------------------
     
     
     private void copyRepositoryToRightLocation(String repoName){
     	 File repoSource = new File("../GQ_Android/test/testmetadata/testRepos/" + repoName);
          try {
      		FileUtilities.copyDirectory(repoSource, repoPath);
      	} catch (IOException e) {
      		// TODO Auto-generated catch block
      		e.printStackTrace();
      	}
     }
     
     private void excecuteSorting(int sortingMode){
     	ProgressBar downloadRepoDataProgress = (ProgressBar) getFieldValue(start,"downloadRepoDataProgress");
      	TextView gameListProgressDescr = (TextView) getFieldValue(start,"gameListProgressDescr");
      	
      	final GeoQuestProgressHandler loadRepoDataHandler = new GeoQuestProgressHandler(
      		downloadRepoDataProgress, gameListProgressDescr,
      		GeoQuestProgressHandler.LAST_IN_CHAIN);
      	GeoQuestApp.loadRepoData(loadRepoDataHandler);
      	repoList = new RepoListActivity();
      	repoList.onCreate(null);
      	
      	gameListActivity = new GameListActivity();
     	
      	sortingOptionsActivity = new SortingOptionsActivity();
      	TestUtils.callMethod(sortingOptionsActivity, "onCreate", new Class[] {Bundle.class }, new Object[] {null});
      	TestUtils.callMethod(sortingOptionsActivity, "onListItemClick", new Class[] {ListView.class, View.class, int.class, long.class }, new Object[] {null, null, sortingMode, 0});
 
      	gameListActivity.repoName = "MyTestGames";
      	gameListActivity.onResume();
     }
 }
