 package se.chalmers.touchdeck.test.gui;
 
 import se.chalmers.touchdeck.R;
 import se.chalmers.touchdeck.gui.GuiController;
 import se.chalmers.touchdeck.gui.TableView;
 import se.chalmers.touchdeck.models.Card;
 import se.chalmers.touchdeck.models.Pile;
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.KeyEvent;
 
 import com.jayway.android.robotium.solo.Solo;
 
 public class PileViewTest extends ActivityInstrumentationTestCase2<TableView> {
 
 	private static final String	FLIP_CARD_OPTION	= "Flip card";
 	private static final String	MOVE_CARD_OPTION	= "Move card";
 	private static final String	DECK				= "deck";
 
 	private TableView			tableView;
 	private GuiController		gc;
 	private int					pilePos;
 
 	public PileViewTest() {
 		super(TableView.class);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		setActivityInitialTouchMode(false); // Turn off touch mode in the emulator.
 		tableView = getActivity(); // Get the Activity under test, starting it if necessary.
 		gc = GuiController.getInstance();
 		pilePos = tableView.getResources().getInteger(R.integer.initial_pile_id);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		setActivityInitialTouchMode(true); // Turn on touch mode in the emulator.
 	}
 
 	public void testFlipCard() {
 		Solo solo = new Solo(getInstrumentation(), tableView);
 		solo.clickOnButton(DECK);
 
 		Pile deck = gc.getPile(pilePos);
 
 		String startImage = deck.getCard(0).getImageName();
 
 		solo.clickOnButton(0);
 		solo.clickOnText(FLIP_CARD_OPTION);
 
 		String firstImage = deck.getCard(0).getImageName();
 
 		assertNotSame(startImage, firstImage);
 
 		solo.clickOnButton(0);
 		solo.clickOnText(FLIP_CARD_OPTION);
 
 		String secondImage = deck.getCard(0).getImageName();
 		assertEquals(startImage, secondImage);
 
 		String startImageCard2 = deck.getCard(1).getImageName();
 		assertEquals(startImage, startImageCard2);
 
 		solo.clickOnButton(1);
 		solo.clickOnText(FLIP_CARD_OPTION);
 
 		String firstImageCard2 = deck.getCard(1).getImageName();
 		assertNotSame(firstImageCard2, startImageCard2);
 		assertNotSame(firstImage, firstImageCard2);
 
 		clickBack(solo);
 
 	}
 
 	public void testSendCard() {
 
 		String secondPileName = "MyPile";
		int secondPilePos = pilePos + 1;
 
 		Solo solo = new Solo(getInstrumentation(), tableView);
 
 		solo.clickOnView(tableView.findViewById(secondPilePos));
 		solo.enterText(0, secondPileName);
 		solo.clickOnButton("OK");
 
 		solo.clickOnButton(DECK);
 
 		Pile deck = gc.getPile(pilePos);
 		Card cardToBeMoved = deck.getCard(3);
 
 		solo.clickOnButton(3);
 		solo.clickOnText(MOVE_CARD_OPTION);
 		solo.clickOnText(secondPileName);
 
 		clickBack(solo);
 		solo.clickOnButton(secondPilePos);
 
 		Pile secondPile = gc.getPile(secondPilePos);
 		assertTrue(secondPile.getSize() == 1);
 		Card movedCard = secondPile.getCard(0);
 
 		assertEquals(movedCard, cardToBeMoved);
 
 		clickBack(solo);
 		solo.clickOnButton(DECK);
 
 		Card secondCardToBeMoved = deck.getCard(2);
 
 		solo.clickOnButton(2);
 		solo.clickOnText(FLIP_CARD_OPTION);
 
 		solo.clickOnButton(2);
 		solo.clickOnText(MOVE_CARD_OPTION);
 		solo.clickOnText(secondPileName);
 		clickBack(solo);
 
 		solo.clickOnButton(secondPilePos);
 		Card secondMovedCard = secondPile.getCard(0);
 		assertEquals(secondMovedCard, secondCardToBeMoved);
 
 	}
 
 	private void clickBack(Solo solo) {
 		solo.sendKey(KeyEvent.KEYCODE_BACK);
 	}
 }
