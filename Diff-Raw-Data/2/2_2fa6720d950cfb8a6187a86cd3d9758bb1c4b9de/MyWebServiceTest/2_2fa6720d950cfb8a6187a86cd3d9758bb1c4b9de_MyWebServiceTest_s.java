 package com.example.domain;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import javax.inject.Inject;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(Arquillian.class)
 public class MyWebServiceTest {
     @Inject private MyWebService mywebservice;
     
     @Deployment public static WebArchive createDeployment() {
         return ShrinkWrap.create(WebArchive.class, "test.war")
                 .addClasses(MyWebService.class)
                 .addClasses(User.class)
                 .addClasses(Player.class)
                 .addClasses(Link.class)
                 .addClasses(TestUtils.class)
                 				//Add the Gson Jar to the deployments with the following line:
                 				.addAsLibrary(new File("lib/gson-2.1.jar"))
                                 .addAsResource("META-INF/persistence.xml")
                                 .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
     }
 
     @Before
     public void setup() throws Exception{
     	//Put all the sample data into our database
     	
     	ArrayList<String> friendIDList = new ArrayList<String>();
     	friendIDList.add("\"newArray\"");
     	friendIDList.add("\"newArray\"");
     	TestUtils.createPlayerWithFriends(1000, "Ihave%20ZeroFriends", friendIDList);
        	
     	friendIDList.clear();
     	friendIDList.add("\"newArray\"");
     	friendIDList.add("\"67890\"");
     	friendIDList.add("\"newArray\"");
     	friendIDList.add("\"One Friend\"");
     	TestUtils.createPlayerWithFriends(1001, "Ihave%20OneFriends", friendIDList);
 
     	friendIDList.clear();
     	friendIDList.add("\"newArray\"");
     	friendIDList.add("\"67890\"");
     	friendIDList.add("\"76543\"");
     	friendIDList.add("\"89012\"");
     	friendIDList.add("\"21098\"");
     	friendIDList.add("\"newArray\"");
     	friendIDList.add("\"One Friend\"");
 		friendIDList.add("\"Two Friend\"");
 		friendIDList.add("\"Three Friend\"");
 		friendIDList.add("\"Four Friend\"");
     	TestUtils.createPlayerWithFriends(1004, "Ihave%20FourFriends", friendIDList);
 
     	friendIDList.clear();
     	friendIDList.add("\"newArray\"");
     	friendIDList.add("\"67890\"");
     	friendIDList.add("\"76543\"");
     	friendIDList.add("\"89012\"");
     	friendIDList.add("\"21098\"");
     	friendIDList.add("\"91234\"");
     	friendIDList.add("\"newArray\"");
     	friendIDList.add("\"One Friend\"");
 		friendIDList.add("\"Two Friend\"");
 		friendIDList.add("\"Three Friend\"");
 		friendIDList.add("\"Four Friend\"");
 		friendIDList.add("\"Five Friend\"");
     	TestUtils.createPlayerWithFriends(1005, "Ihave%20FiveFriends", friendIDList);
 
     	friendIDList.clear();
     	friendIDList.add("\"newArray\"");
     	friendIDList.add("\"67890\"");
     	friendIDList.add("\"76543\"");
     	friendIDList.add("\"89012\"");
     	friendIDList.add("\"21098\"");
     	friendIDList.add("\"91234\"");
     	friendIDList.add("\"77441\"");
     	friendIDList.add("\"88552\"");
     	friendIDList.add("\"99663\"");
     	friendIDList.add("\"11223\"");
     	friendIDList.add("\"44556\"");
     	friendIDList.add("\"newArray\"");
     	friendIDList.add("\"One Friend\"");
     	friendIDList.add("\"Two Friend\"");
     	friendIDList.add("\"Three Friend\"");
     	friendIDList.add("\"Four Friend\"");
     	friendIDList.add("\"Five Friend\"");
     	friendIDList.add("\"Six Friend\"");
 		friendIDList.add("\"Seven Friend\"");
 		friendIDList.add("\"Eight Friend\"");
 		friendIDList.add("\"Nine Friend\"");
 		friendIDList.add("\"Ten Friend\"");
     	TestUtils.createPlayerWithFriends(1010, "Ihave%20TenFriends", friendIDList);
     }
     
     @After
     public void tearDown() throws Exception{
     	//Clear all the sample data out of our database now that we're done running all the tests.
 
     	String response = TestUtils.removePlayer(1000);
     	Assert.assertTrue(response.equals("Player removed with FacbookID: 1000"));
     	response = TestUtils.removePlayer(1001);
     	Assert.assertTrue(response.equals("Player removed with FacbookID: 1001"));
     	response = TestUtils.removePlayer(1004);
     	Assert.assertTrue(response.equals("Player removed with FacbookID: 1004"));
     	response = TestUtils.removePlayer(1005);
        	Assert.assertTrue(response.equals("Player removed with FacbookID: 1005"));
        	response = TestUtils.removePlayer(1010);
        	Assert.assertTrue(response.equals("Player removed with FacbookID: 1010"));
     }
 
     //@Test
     public void testSetupData(){
 	//Retrieve the sample data from our database and make sure it's what we expect
 		
     	Player playerWith0Friends = TestUtils.getPlayer(1000);
     	Player playerWith1Friends = TestUtils.getPlayer(1001);
     	Player playerWith4Friends = TestUtils.getPlayer(1004);
     	Player playerWith5Friends = TestUtils.getPlayer(1005);
     	Player playerWith10Friends = TestUtils.getPlayer(1010);
     	
     	Assert.assertTrue(playerWith0Friends.getFriendList().size()==0);
     	Assert.assertTrue(playerWith1Friends.getFriendList().size()==1);
     	Assert.assertTrue(playerWith4Friends.getFriendList().size()==4);
     	Assert.assertTrue(playerWith5Friends.getFriendList().size()==5);
     	Assert.assertTrue(playerWith10Friends.getFriendList().size()==10);
     	
     	Assert.assertTrue(playerWith10Friends.getFriendList().get(0)==67890);
     	User twoFriend = TestUtils.getUser(playerWith5Friends.getFriendList().get(1));
     	Assert.assertTrue(twoFriend.getName().equals("Two Friend"));
     }
     
     //@Test
     public void testIsDeployed() {
         Assert.assertNotNull(mywebservice);
     }
     
     //@Test
 	public void testGameLinkForNotEnoughFriends(){
     	Player playerWith0Friends = TestUtils.getPlayer(1000);
 		
 		//Test that the Player has 0 friends and has the correct link.
 		String expectedOnCLickMethod = 
 			"(function (){alert('You do not have enough friends to play the game.');return false;});";
 		Assert.assertTrue(playerWith0Friends.getFriendList().size()==0);
 		Assert.assertTrue(playerWith0Friends.getGameLink(). getOnClickMethod().equals(expectedOnCLickMethod));
 		Assert.assertTrue(playerWith0Friends.getGameLink(). getHref().equals("index.html"));
 		
 		Player playerWith4Friends = TestUtils.getPlayer(1004);
 		
 		//Test that the Player with 4 friends also has the correct link.
 		Assert.assertTrue(playerWith4Friends.getFriendList().size()==4);
 	    Assert.assertTrue(playerWith4Friends.getGameLink(). getOnClickMethod().equals(expectedOnCLickMethod));
 		Assert.assertTrue(playerWith4Friends.getGameLink() .getHref().equals("index.html"));
 	}
     
     //@Test
 	public void testGameLinkForValidNumOfFriends(){
     	Player playerWith5Friends = TestUtils.getPlayer(1005);
 		
 		//Test that the Player has 5 friends and has the correct link.
 		String expectedHrefBeginning = "playGame.html?playerID="
 				+ playerWith5Friends.getPlayerInfo().getFacebookID() + "&playerName="
 				+ playerWith5Friends.getPlayerInfo().getName() + "&playerPoints="
 				+ playerWith5Friends.getPoints() + "&friendIDList=";
 		Assert.assertTrue(playerWith5Friends.getFriendList().size()==5);
 		Assert.assertTrue(playerWith5Friends.getGameLink().getOnClickMethod().equals(""));
 		Assert.assertTrue(playerWith5Friends.getGameLink().getHref().startsWith(expectedHrefBeginning));
 		
 		//Since the friendIDList and friendNameList passed in the GameLink.Href are random,
 		//we can't validate them in any simple manner.  However, we can verify that our
 		//requirements are still in working order.  We need to make sure that the names
 		//that go with all 3 friendIDList entries are among the 5 friendNameList entries
 		Assert.assertTrue(isValidGameLinkLists(expectedHrefBeginning, playerWith5Friends));
 		
 		
 		Player playerWith10Friends = TestUtils.getPlayer(1010);
 		expectedHrefBeginning = "playGame.html?playerID="
 				+ playerWith10Friends.getPlayerInfo().getFacebookID() + "&playerName="
 				+ playerWith10Friends.getPlayerInfo().getName() + "&playerPoints="
 				+ playerWith10Friends.getPoints() + "&friendIDList=";
 		
 		//Test that the Player with 10 friends also has a correct link.
 		Assert.assertTrue(playerWith10Friends.getFriendList().size()==10);
 		Assert.assertTrue(playerWith10Friends.getGameLink().getOnClickMethod().equals(""));
 		Assert.assertTrue(playerWith10Friends.getGameLink().getHref().startsWith(expectedHrefBeginning));
 		Assert.assertTrue(isValidGameLinkLists(expectedHrefBeginning, playerWith10Friends));
 	}
     
 	private boolean isValidGameLinkLists(String HrefBeginning, Player player){
 		//Parse the player's GameLink to get the friendIDList and friendNameList
 		int friendIDStart = HrefBeginning.length();
 		int friendIDEnd = player.getGameLink().getHref().indexOf("&friendNameList=");
 		String friendIDs = player.getGameLink().getHref().substring(friendIDStart, friendIDEnd);
 		int friendNameStart = player.getGameLink().getHref().indexOf("friendNameList=") + 15; //we need to add len("friendNameList=")
 		String friendNames = player.getGameLink().getHref().substring(friendNameStart);
 
 		ArrayList<String> friendIDList = getListFromString(friendIDs);
 		ArrayList<String> friendNamesList = getListFromString(friendNames);
 		for (String friendID : friendIDList) {
 			//Check that each friendID matches to a name in the friendNameList
 			
 			User curUser = TestUtils.getUser(Long.valueOf(friendID));
 			if(curUser==null){
 				System.out.println("No User found in DB for ID: " + friendID);
 				return false;
 			}
 			else {
 				if(userNameInList(curUser, friendNamesList) == false){
 					System.out.println(curUser.getName() + ", ID [" + friendID + 
 							"was not  in the friendNamesList");
 					return false;
 				}
 			}
 			
 			//Ensure each friendID is a member of the player's friendList
 			if(player.getFriendList().contains(Long.valueOf(friendID)) == false){
 				System.out.println("ID [" + friendID + "] is not among the players friendlist"
 						+ player.getFriendList());
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public boolean userNameInList(User user, ArrayList<String> friendNamesList){
 		String curName = user.getName();
 		for (String friendName : friendNamesList) {
 			if(curName.equals(friendName)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public ArrayList<String> getListFromString(String text){
 		 ArrayList<String> list = new ArrayList<String>();
 	     StringTokenizer tokens = new StringTokenizer(text,",");
 	     while(tokens.hasMoreTokens()){
 	    	 list.add((String) tokens.nextElement());
 	     }
 	     return list;
 	}
 	
 	@Test
 	public void testSubmitAllWrongAnswers(){
 		Player playerWith5Friends = TestUtils.getPlayer(1005);
 		
 		//Take note of the player's points before they submit the wrong answers
 		long playerPointsOriginal = playerWith5Friends.getPoints();
 		
 		//Submit 3 incorrect answers to our WebService as a POST request
 		String targetURL = "http://localhost:8080/MyApp/rest/webService/GameAnswers/" + 
 				"1005/67890/76543/89012";
 		String JSONInput = "[\"" + "Four Friend" + "\",\"" + "Five Friend" + "\",\"" + "One Friend" + "\"]";
 		String response = TestUtils.doPOST(targetURL, JSONInput);
 		
 		//Test that we get the correct String back from the incorrect answers and our points were deducted
 		String expectedResponse = "First entry was INCORRECT "
 				+ "Second entry was INCORRECT "
 				+ "Third entry was INCORRECT "
 				+ "You will have a total of [" + 30
 				+ "] points deducted.";
 		
 		//Re-GET the player now that the score should be updated
 		playerWith5Friends = TestUtils.getPlayer(1005);
 		Assert.assertTrue(response.equals(expectedResponse));
 		Assert.assertTrue(playerWith5Friends.getPoints()==(playerPointsOriginal - 30));
 	}
 	
 
 	@Test
 	public void testSubmitAllCorrectAnswers(){
 		Player playerWith5Friends = TestUtils.getPlayer(1005);
 		
 		//Take note of the player's points before they submit the correct answers
 		long playerPointsOriginal = playerWith5Friends.getPoints();
 		
 		//Submit 3 correct answers to our WebService as a POST request
 		String targetURL = "http://localhost:8080/MyApp/rest/webService/GameAnswers/" +
 				"1005/67890/76543/89012";
 		String JSONInput = "[\"" + "One Friend" + "\",\"" + "Two Friend" + "\",\"" + "Three Friend" + "\"]";
 		String response = TestUtils.doPOST(targetURL, JSONInput);
 		
 		//Test that we get the correct String back from the incorrect answers and our points were deducted
 		String expectedResponse = "First entry was correct "
 				+ "Second entry was correct "
 				+ "Thrid entry was correct "
 				+ "You will have a total of [" + 30
 				+ "] points added!";
 		
 		//Re-GET the player now that the score should be updated
 		playerWith5Friends = TestUtils.getPlayer(1005);
 		Assert.assertTrue(response.equals(expectedResponse));
 		Assert.assertTrue(playerWith5Friends.getPoints()==(playerPointsOriginal + 30));
 	}
 	
 	@Test
 	public void testSubmitAllBlankAnswers(){
 		Player playerWith5Friends = TestUtils.getPlayer(1005);
 		
 		//Take note of the player's points before they submit the blank answers
 		long playerPointsOriginal = playerWith5Friends.getPoints();
 		
		//Submit 3 incorrect answers to our WebService as a POST request
 		String targetURL = "http://localhost:8080/MyApp/rest/webService/GameAnswers/" + 
 				"1005/67890/76543/89012";
 		String JSONInput = "[\"" + "\",\"" + "\",\"" + "\"]";
 		String response = TestUtils.doPOST(targetURL, JSONInput);
 		
 		//Test that we get the correct String back from the blank answers and our points were deducted
 		String expectedResponse = "First entry was INCORRECT "
 				+ "Second entry was INCORRECT "
 				+ "Third entry was INCORRECT "
 				+ "You will have a total of [" + 30
 				+ "] points deducted.";
 		
 		System.out.println("\n\n\n\n\n\n response: " + response);
 		System.out.println("\n\n\n\n\n\n");
 
 		//Re-GET the player now that the score should be updated
 		playerWith5Friends = TestUtils.getPlayer(1005);
 		Assert.assertTrue(response.equals(expectedResponse));
 		Assert.assertTrue(playerWith5Friends.getPoints()==(playerPointsOriginal - 30));
 	}
 }
