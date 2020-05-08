 package com.phdroid.smsb.activity;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 import com.phdroid.test.blackjack.Solo;
 import com.phdroid.smsb.R;
 import com.phdroid.smsb.storage.IMessageProvider;
 import com.phdroid.smsb.storage.MessageProviderHelper;
 import com.phdroid.smsb.storage.TestMessageProvider2;
 
 public class BlockedSmsListActivityTest extends ActivityInstrumentationTestCase2<BlockedSmsListActivity> {
 	private BlockedSmsListActivity mActivity;
 	private ListView mList;
 	private SmsPojoArrayAdapter mAdapter;
 	private static final int ADAPTER_COUNT = 10;
 	private Solo mSolo;
 
 	public BlockedSmsListActivityTest(){
 		super("com.phdroid.smsb.activity.BlockedSmsListActivity", BlockedSmsListActivity.class);
 	}
 
 	public BlockedSmsListActivityTest(String pkg, Class activityClass) {
 		super(pkg, activityClass);
 	}
 
 	public BlockedSmsListActivityTest(Class activityClass) {
 		super(activityClass);
 	}
 
 	public void setUp() throws Exception {
 		MessageProviderHelper.setMessageProvider(new TestMessageProvider2());
 		super.setUp();
 		setActivityInitialTouchMode(false);
 		mActivity = getActivity();
 		mList = (ListView)mActivity.findViewById(R.id.messagesListView);
 		mAdapter = (SmsPojoArrayAdapter) mList.getAdapter();
 		mSolo = new Solo(getInstrumentation(), getActivity());
 	}
 
 	public void testPreConditions() {
 		assertTrue(mAdapter != null);
 		assertEquals(ADAPTER_COUNT, mAdapter.getCount());
 	}
 
 	public void test_undo_button_is_invisible_at_startup(){
 		View v = mActivity.findViewById(R.id.buttonLayout);
 		assertEquals(View.GONE, v.getVisibility());
 	}
 
 	public void test_pressing_DeleteAll_deletes_all_items(){
 		pressDeleteAllMenuItem();
 		assertEquals(0, mAdapter.getCount());
 	}
 
 	public void test_undo_button_is_visible_after_pressing_DeleteAll(){
 		pressDeleteAllMenuItem();
 		View v = mActivity.findViewById(R.id.buttonLayout);
 		assertEquals(View.VISIBLE, v.getVisibility());
 	}
 
 	public void test_undo_button_has_correct_text_after_pressing_DeleteAll(){
 		pressDeleteAllMenuItem();
 		Button b = (Button)mActivity.findViewById(R.id.undoButton);
 		assertEquals("10 messages were deleted. (Undo)", b.getText());
 	}
 
 	public void test_pressing_undo_button_after_deleting_all_returns_all_items_back(){
 		int originalCount = mAdapter.getCount();
 		pressDeleteAllMenuItem();
 		pressUndoButton();
 		assertEquals(originalCount, mAdapter.getCount());
 	}
 
 	public void test_pressing_select_many_button_opens_select_many_activity(){
 		pressSelectManyMenuItem();
 		assertEquals(SelectManyActivity.class, mSolo.getCurrentActivity().getClass());
 	}
 
 	public void test_pressing_settings_button_opens_settings_activity(){
 		pressSettingsMenuItem();
 		assertEquals(SettingsActivity.class, mSolo.getCurrentActivity().getClass());
 	}
 
 	public void test_pressing_message_opens_view_message_activity(){
 		pressMessage(3);
 		assertEquals(ViewMessageActivity.class, mSolo.getCurrentActivity().getClass());
 	}
 
 	public void test_actions_are_performed_when_navigating_to_select_many_activity(){
 		deleteFirstMessage();
 		pressSelectManyMenuItem();
 		sendKeys(KeyEvent.KEYCODE_BACK);
 		View v = mActivity.findViewById(R.id.buttonLayout);
 		assertEquals(View.GONE, v.getVisibility());
 	}
 
 	public void test_actions_are_performed_when_navigating_to_settings_activity(){
 		deleteFirstMessage();
 		pressSettingsMenuItem();
 		sendKeys(KeyEvent.KEYCODE_BACK);
 		View v = mActivity.findViewById(R.id.buttonLayout);
 		assertEquals(View.GONE, v.getVisibility());
 	}
 
 	public void test_actions_are_performed_when_navigating_to_view_message_activity(){
 		deleteFirstMessage();
 		pressMessage(2);
 		sendKeys(KeyEvent.KEYCODE_BACK);
 		View v = mActivity.findViewById(R.id.buttonLayout);
 		assertEquals(View.GONE, v.getVisibility());
 	}
 
 	private void deleteFirstMessage() {
		final IMessageProvider provider = MessageProviderHelper.getMessageProvider(this.getActivity(), this.getActivity(), this.getActivity().getContentResolver());
 		mActivity.runOnUiThread(new Runnable() {
 			public void run() {
 				provider.delete(0);
 				mAdapter.notifyDataSetChanged();
 			}
 		});
 		getInstrumentation().waitForIdleSync();
 	}
 
 	private void pressMessage(int messageIndex) {
 		for(int i = 0; i<messageIndex; i++){
 			sendKeys(KeyEvent.KEYCODE_DPAD_DOWN); // selecting first menu item (delete all)
 		}
 		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER); // pressing selected item
 		getInstrumentation().waitForIdleSync();
 	}
 
 	private void pressUndoButton() {
 		final Button b = (Button)mActivity.findViewById(R.id.undoButton);
 		mActivity.runOnUiThread(new Runnable() {
 			public void run() {
 				b.performClick();
 			}
 		});
 		getInstrumentation().waitForIdleSync();
 	}
 
 	private void pressDeleteAllMenuItem() {
 		getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
 		getInstrumentation().invokeMenuActionSync(mActivity, R.id.delete_all_item, 0);
 
 	}
 
 	private void pressSelectManyMenuItem() {
 		getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
 		getInstrumentation().invokeMenuActionSync(mActivity, R.id.select_many_item, 0);
 
 	}
 
 	private void pressSettingsMenuItem() {
 		getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
 		getInstrumentation().invokeMenuActionSync(mActivity, R.id.settings_item, 0);
 
 	}
 }
