 package com.agileteam.biddingbidding.unittest;
 
 import static com.objogate.wl.android.driver.ViewDriver.enabled;
 import static com.objogate.wl.android.driver.ViewDriver.visible;
 import static com.objogate.wl.android.driver.ViewDriver.forcused;
 import static org.hamcrest.CoreMatchers.not;
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.agileteam.biddingbidding.MainActivity;
 import com.agileteam.biddingbidding.R;
 import com.agileteam.biddingbidding.test.ApplicationRunner;
 import com.agileteam.biddingbidding.test.BiddingBiddingDriver;
 import com.agileteam.biddingbidding.test.FakeAuctionServer;
 import com.jayway.android.robotium.solo.Solo;
 import com.objogate.wl.android.driver.ViewDriver;
 
 public class ActivityUITest extends
 		ActivityInstrumentationTestCase2<MainActivity> {
 	FakeAuctionServer auction = new FakeAuctionServer("item-54321");
 	private Solo solo;
 	private BiddingBiddingDriver driver;
 	private ViewDriver<Button> bidButton;
	private ViewDriver<Button> loginButton;
 	private ViewDriver<EditText> hostEditText;
 	private ViewDriver<EditText> idEditText;
 	private ViewDriver<EditText> passwordEditText;
 
 	public ActivityUITest() {
 		super(MainActivity.class);
 	}
 
 	@Override
 	public void setUp() {
 		solo = new Solo(getInstrumentation(), getActivity());
 		driver = new BiddingBiddingDriver(solo, 2000);
 		bidButton = new ViewDriver<Button>(driver, R.id.button_bid);
		loginButton = new ViewDriver<Button>(driver, R.id.button_login);
 		hostEditText = new ViewDriver<EditText>(driver, R.id.editText_host);
 		idEditText = new ViewDriver<EditText>(driver, R.id.editText_id);
 		passwordEditText = new ViewDriver<EditText>(driver,
 				R.id.editText_password);
 	}
 
 	public void testBidButtonInactiveWhenStateIsNotLosing() throws Exception {
 		auction.startSellingItem();
 		joinAuction();
 
 		auction.hasReceivedJoinRequestFrom(ApplicationRunner.BIDDER_ID);
 		bidButton.is(not(enabled()));
 
 		bidButton.setEnabled(true);
 		auction.reportPrice(2000, 100, ApplicationRunner.BIDDER_ID);
 		bidButton.is(not(enabled()));
 
 		auction.reportPrice(2000, 100, "Other Bidder");
 		bidButton.is(enabled());
 
 		auction.announceClosed();
 		bidButton.is(not(enabled()));
 	}
 
 	public void testBidButtonActiveWhenLosing() throws Exception {
 		auction.startSellingItem();

 		joinAuction();

 		auction.hasReceivedJoinRequestFrom(ApplicationRunner.BIDDER_ID);
 		bidButton.setEnabled(false);
 		auction.reportPrice(2000, 100, "JYP");
 		bidButton.is(enabled());
 	}
 
 	public void testKeepBidButtonActiveWhenDeviceRotate() throws Exception {
 		auction.startSellingItem();
 
 		joinAuction();
 
 		auction.hasReceivedJoinRequestFrom(ApplicationRunner.BIDDER_ID);
 		auction.reportPrice(2000, 100, "JYP");
 
 		bidButton.is(enabled());
 
 		solo.setActivityOrientation(Solo.LANDSCAPE);
 		solo.setActivityOrientation(Solo.PORTRAIT);
 
 		bidButton.is(enabled());
 	}
 
 	public void testInactiveLoginViewAfterJoinClicked() throws Exception {
 		auction.startSellingItem();
 		joinToWrongServer();
 
 		hostEditText.is(not(enabled()));
 		idEditText.is(not(enabled()));
 		passwordEditText.is(not(enabled()));
 	}
 
 	public void testHideLoginViewAfterJoined() throws Exception {
 		auction.startSellingItem();
 		joinAuction();
		new ViewDriver<View>(solo, 2000, R.id.layout_login)
				.is(not(visible()));
 	}
 
 	public void testWrongServerShowsLoginFormAgainWithFlash() throws Exception {
 		ViewDriver<View> loginPanel = new ViewDriver<View>(solo, 2000,
 				R.id.layout_login);
 
 		auction.startSellingItem();
 
 		joinToWrongServer();
		solo.waitForText("Failed To Login");
 
 		loginPanel.is(visible());
 
 		joinAuction();
 		auction.hasReceivedJoinRequestFrom(ApplicationRunner.BIDDER_ID);
 
 		loginPanel.is(not(visible()));
 	}
 
	public void testSingleLineInputAndEnterMakeCurserMoveToNextInputTextView() throws Exception{
 		solo.enterText(0, "singleLine");
 		solo.sendKey(KeyEvent.KEYCODE_ENTER);
 		idEditText.is(forcused());
 		solo.enterText(0, "singleLine");
 		solo.sendKey(KeyEvent.KEYCODE_ENTER);
 		passwordEditText.is(forcused());
 	}
	
 	private void joinToWrongServer() {
 		solo.clearEditText(0);
 		solo.clearEditText(1);
 		solo.clearEditText(2);
 		solo.enterText(0, "not-existing-server.com");
 		solo.enterText(1, ApplicationRunner.BIDDER_ID);
 		solo.enterText(2, ApplicationRunner.BIDDER_PASSWORD);
 		solo.clickOnButton("Login Auction");
 	}
 
 	private void joinAuction() {
 		solo.clearEditText(0);
 		solo.clearEditText(1);
 		solo.clearEditText(2);
 		solo.enterText(0, FakeAuctionServer.XMPP_HOSTNAME);
 		solo.enterText(1, ApplicationRunner.BIDDER_ID);
 		solo.enterText(2, ApplicationRunner.BIDDER_PASSWORD);
 		solo.clickOnButton("Login Auction");
 	}
 
 	@Override
 	public void tearDown() {
 		auction.stop();
 		solo.finishOpenedActivities();
 	}
 }
