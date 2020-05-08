 package se.chalmers.touchdeck.test.gui;
 
 import se.chalmers.touchdeck.R;
 import se.chalmers.touchdeck.gui.TableView;
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.KeyEvent;
 
 import com.jayway.android.robotium.solo.Solo;
 
 public class TableViewTest extends ActivityInstrumentationTestCase2<TableView> {
 
 	private TableView	tableView;
 
 	public TableViewTest() {
 		super(TableView.class);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		setActivityInitialTouchMode(false); // Turn off touch mode in the emulator.
 		tableView = getActivity(); // Get the Activity under test, starting it if necessary.
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		setActivityInitialTouchMode(true); // Turn on touch mode in the emulator.
 	}
 
 	public void testCreatePile() {
 		Solo solo = new Solo(getInstrumentation(), tableView);
 		solo.clickOnView(tableView.findViewById(tableView.getResources().getInteger(R.integer.initial_pile_id) + 1));
 		solo.clickOnButton("OK");
 		solo.clickOnButton("Pile 1");
 		clickBack(solo);
 
 		solo.clickOnView(tableView.findViewById(tableView.getResources().getInteger(R.integer.initial_pile_id) - 1));
		solo.enterText(0, "MyPile");
 		solo.clickOnButton("OK");
 
		solo.clickOnButton("Pile 1");
 		clickBack(solo);
 
 	}
 
 	private void clickBack(Solo solo) {
 		solo.sendKey(KeyEvent.KEYCODE_BACK);
 	}
 }
