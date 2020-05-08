 package fr.geobert.radis.robotium;
 
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import com.jayway.android.robotium.solo.Solo;
 import fr.geobert.radis.R;
 import fr.geobert.radis.RadisConfiguration;
 import fr.geobert.radis.tools.Formater;
 import fr.geobert.radis.tools.Tools;
 import fr.geobert.radis.ui.OperationListActivity;
 import fr.geobert.radis.ui.ScheduledOpListActivity;
 import fr.geobert.radis.ui.editor.AccountEditor;
 import fr.geobert.radis.ui.editor.OperationEditor;
 import fr.geobert.radis.ui.editor.ScheduledOperationEditor;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public class RadisTest extends ActivityInstrumentationTestCase2<OperationListActivity> {
     private static final int WAIT_DIALOG_TIME = 2000;
 
     private static final int CUR_ACC_NAME_IDX = 0;
     private static final int CUR_ACC_PROJ_DATE_IDX = 1;
     private static final int CUR_ACC_SUM_IDX = 2;
     public static final int FIRST_OP_SUM_IDX = 9;
     public static final int THIRD_PARTY_FIELD_IDX = 3;
     public static final int SUM_FIELD_IDX = 4;
     public static final int FIRST_SUM_AT_SEL_IDX = 6;
 
     static String TAG = "RadisRobotium";
     static final String ACCOUNT_NAME = "Test";
     static final String ACCOUNT_START_SUM = "1000,50";
     static final String ACCOUNT_START_SUM_FORMATED_IN_EDITOR = "1 000,50";
     static final String ACCOUNT_START_SUM_FORMATED_ON_LIST = "1 000,50 €";
     static final String ACCOUNT_DESC = "Test Description";
     static final String ACCOUNT_NAME_2 = "Test2";
     static final String ACCOUNT_START_SUM_2 = "2000,50";
     static final String ACCOUNT_START_SUM_FORMATED_ON_LIST_2 = "2 000,50 €";
     static final String ACCOUNT_DESC_2 = "Test Description 2";
     static final String ACCOUNT_NAME_3 = "Test3";
     static final String OP_TP = "Operation 1";
     static final String OP_AMOUNT = "10.50";
     static final String OP_AMOUNT_FORMATED = "-10,50";
     static final String OP_TAG = "Tag 1";
     static final String OP_MODE = "Carte bleue";
     static final String OP_DESC = "Robotium Operation 1";
     static final String OP_AMOUNT_2 = "100";
 
     private Solo solo;
     private RoboTools tools;
 
     static {
         OperationListActivity.ROBOTIUM_MODE = true;
     }
 
     public RadisTest() {
         super(OperationListActivity.class);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         Intent i = new Intent();
         i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         setActivityIntent(i);
         solo = new Solo(getInstrumentation());
         getActivity();
         this.tools = new RoboTools(solo, this);
     }
 
     private void backOutToHome() {
         boolean more = true;
         while (more) {
             try {
                 getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
             } catch (SecurityException e) { // Done, at Home.
                 more = false;
             }
         }
     }
 
     @Override
     protected void tearDown() throws Exception {
         try {
             Log.d(TAG, "tearDown !");
 
             solo.finishOpenedActivities();
 //            backOutToHome();
 //            solo.finalize();
         } catch (Throwable e) {
             e.printStackTrace();
         }
     }
 
     /*
      * Operations Tests
 	 */
 
     public void addAccount() {
         assertTrue(solo.waitForActivity(AccountEditor.class));
         solo.enterText(0, ACCOUNT_NAME);
         solo.enterText(1, ACCOUNT_START_SUM);
         solo.enterText(4, ACCOUNT_DESC);
         solo.clickOnActionBarItem(R.id.confirm);
         assertTrue(solo.waitForActivity(OperationListActivity.class));
         assertEquals(1, solo.getView(Spinner.class, 0).getCount());
         assertEquals(ACCOUNT_NAME, solo.getText(CUR_ACC_NAME_IDX).getText().toString());
         assertEquals(ACCOUNT_START_SUM_FORMATED_ON_LIST, solo.getText(CUR_ACC_SUM_IDX).getText().toString());
     }
 
     public void addAccount2() {
         solo.pressMenuItem(0);
         solo.waitForActivity(AccountEditor.class);
         solo.enterText(0, ACCOUNT_NAME_2);
         solo.enterText(1, ACCOUNT_START_SUM_2);
         solo.enterText(4, ACCOUNT_DESC_2);
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForActivity(OperationListActivity.class);
         assertEquals(2, solo.getView(Spinner.class, 0).getCount());
     }
 
     public void addAccount3() {
         solo.pressMenuItem(0);
         assertTrue(solo.waitForActivity(AccountEditor.class));
         solo.enterText(0, ACCOUNT_NAME_3);
         solo.enterText(1, ACCOUNT_START_SUM);
         solo.enterText(4, ACCOUNT_DESC);
         solo.clickOnActionBarItem(R.id.confirm);
         assertTrue(solo.waitForActivity(OperationListActivity.class));
         assertEquals(3, solo.getView(Spinner.class, 0).getCount());
     }
 
     public void editAccount() {
         solo.pressMenuItem(1);
         solo.waitForActivity(AccountEditor.class);
         assertEquals(ACCOUNT_NAME, solo.getEditText(0).getText().toString());
         assertEquals(ACCOUNT_START_SUM_FORMATED_IN_EDITOR, solo.getEditText(1).getText().toString());
         assertEquals(ACCOUNT_DESC, solo.getEditText(4).getText().toString());
         tools.scrollUp();
         solo.clearEditText(0);
         solo.enterText(0, ACCOUNT_NAME_2);
         solo.clearEditText(1);
         solo.enterText(1, ACCOUNT_START_SUM_2);
         solo.clearEditText(4);
         solo.enterText(4, ACCOUNT_DESC_2);
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForActivity(OperationListActivity.class);
         // assertEquals(1, solo.getCurrentListViews().get(0).getCount());
         assertEquals(ACCOUNT_NAME_2, solo.getText(2).getText().toString());
         assertEquals(ACCOUNT_START_SUM_FORMATED_ON_LIST_2, solo.getText(3).getText().toString());
     }
 
     public void deleteAccount(int originalCount) {
         solo.pressMenuItem(2);
         solo.clickOnButton(solo.getString(fr.geobert.radis.R.string.yes));
         solo.waitForDialogToClose(WAIT_DIALOG_TIME);
         if (originalCount > 1) {
             assertEquals(originalCount - 1, solo.getCurrentViews(ListView.class).get(0).getCount());
         }
     }
 
     private void setUpOpTest() {
         addAccount();
     }
 
     public void addOp() {
         solo.clickOnActionBarItem(R.id.create_operation);
         solo.waitForActivity(OperationEditor.class);
         solo.enterText(3, OP_TP);
         for (int i = 0; i < OP_AMOUNT.length(); ++i) {
             solo.enterText(4, String.valueOf(OP_AMOUNT.charAt(i)));
         }
         solo.enterText(5, OP_TAG);
         solo.enterText(6, OP_MODE);
         solo.enterText(7, OP_DESC);
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForActivity(OperationListActivity.class);
         solo.waitForView(ListView.class);
     }
 
     public void addManyOps() {
         setUpOpTest();
         for (int j = 0; j < 10; ++j) {
             solo.clickOnActionBarItem(R.id.create_operation);
             solo.waitForActivity(OperationEditor.class);
             solo.enterText(3, OP_TP + j);
             solo.enterText(4, OP_AMOUNT_2);
             solo.enterText(5, OP_TAG);
             solo.enterText(6, OP_MODE);
             solo.enterText(7, OP_DESC);
             solo.clickOnActionBarItem(R.id.confirm);
             solo.waitForActivity(OperationListActivity.class);
             solo.waitForView(ListView.class);
         }
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(0.5)));
     }
 
     public void EditOp() {
         TAG = "testEditOp";
         addManyOps();
         tools.sleep(1000);
         tools.scrollUp();
         solo.clickInList(5, 0);
         tools.sleep(2000);
         solo.clickOnImageButton(0);
         solo.waitForActivity(OperationEditor.class);
         solo.clearEditText(4);
         solo.enterText(4, "103");
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForActivity(OperationListActivity.class);
         tools.sleep(500);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(-2.5)));
     }
 
     public void testQuickAddFromOpList() {
         TAG = "testQuickAddFromOpList";
         addAccount();
         tools.printCurrentTextViews();
         assertTrue(
                 solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1000.50)));
         tools.printCurrentEditTexts();
         solo.enterText(0, "Toto");
         solo.enterText(1, "-1");
         solo.clickOnImageButton(0);
         tools.sleep(500);
         assertEquals(1, solo.getCurrentViews(ListView.class).get(0).getCount());
         assertTrue(
                 solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(999.50)));
     }
 
     public void testDisableAutoNegate() {
         TAG = "testDisableAutoNegate";
         setUpOpTest();
         solo.clickOnActionBarItem(R.id.create_operation);
         solo.waitForActivity(OperationEditor.class);
         solo.enterText(THIRD_PARTY_FIELD_IDX, OP_TP);
         solo.enterText(SUM_FIELD_IDX, "+");
         for (int i = 0; i < OP_AMOUNT.length(); ++i) {
             solo.enterText(SUM_FIELD_IDX, String.valueOf(OP_AMOUNT.charAt(i)));
         }
         tools.printCurrentEditTexts();
         assertTrue(solo.getEditText(SUM_FIELD_IDX).getText().toString().equals("+10,50"));
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForActivity(OperationListActivity.class);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains("1 011,00"));
         tools.printCurrentTextViews();
         assertTrue(solo.getText(FIRST_OP_SUM_IDX).getText().toString().equals("10,50"));
     }
 
     /**
      * Schedule ops
      */
 
 
     private void setUpSchOp() {
         addAccount();
         solo.clickOnActionBarItem(R.id.go_to_preferences);
         solo.waitForActivity(RadisConfiguration.class);
         solo.clickOnText(solo.getString(R.string.prefs_insertion_date_label));
         solo.clearEditText(0);
         GregorianCalendar today = Tools.createClearedCalendar();
         today.add(Calendar.DAY_OF_MONTH, 1);
         solo.enterText(0, Integer.toString(today.get(Calendar.DAY_OF_MONTH)));
         solo.clickOnButton(solo.getString(R.string.ok));
         solo.goBack();
         solo.clickOnActionBarItem(R.id.go_to_sch_op);
         solo.waitForActivity(ScheduledOpListActivity.class);
         assertTrue(solo.waitForText(solo.getString(R.string.no_operation_sch)));
     }
 
     public void addScheduleOp() {
         setUpSchOp();
         solo.clickOnActionBarItem(R.id.create_operation);
         solo.waitForActivity(ScheduledOperationEditor.class);
         GregorianCalendar today = Tools.createClearedCalendar();
         solo.setDatePicker(0, today.get(Calendar.YEAR),
                 today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
         solo.enterText(3, OP_TP);
         solo.enterText(4, "9,50");
         solo.enterText(5, OP_TAG);
         solo.enterText(6, OP_MODE);
         solo.enterText(7, OP_DESC);
         tools.scrollUp();
         solo.clickOnText(solo.getString(R.string.scheduling));
         solo.pressSpinnerItem(0, 1);
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForView(ListView.class);
         assertEquals(1, solo.getCurrentViews(ListView.class).get(0).getCount());
         solo.goBack();
         solo.waitForActivity(OperationListActivity.class);
         solo.waitForView(ListView.class);
         // -1 is for "get more ops" line
         assertEquals(1, solo.getCurrentViews(ListView.class).get(0).getCount());
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains("991,00"));
     }
 
     public void testEditScheduledOp() {
         TAG = "testEditScheduledOp";
         addScheduleOp();
         solo.clickInList(0);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.edit_op));
         solo.waitForActivity(OperationEditor.class);
         solo.clearEditText(SUM_FIELD_IDX);
         solo.enterText(SUM_FIELD_IDX, "-7,50");
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForText(solo.getString(R.string.update));
         solo.clickOnButton(solo.getString(R.string.update));
         solo.waitForActivity(OperationListActivity.class);
         solo.waitForView(ListView.class);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains("993,00"));
     }
 
     private int setupDelOccFromOps() {
         setUpSchOp();
         solo.clickOnActionBarItem(R.id.create_operation);
         solo.waitForActivity(ScheduledOperationEditor.class);
         GregorianCalendar today = Tools.createClearedCalendar();
         today.add(Calendar.DAY_OF_MONTH, -14);
         solo.setDatePicker(0, today.get(Calendar.YEAR),
                 today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
         solo.enterText(3, OP_TP);
         solo.enterText(4, "1,00");
         solo.enterText(5, OP_TAG);
         solo.enterText(6, OP_MODE);
         solo.enterText(7, OP_DESC);
         solo.clickOnText(solo.getString(R.string.scheduling));
         solo.pressSpinnerItem(1, -1);
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForView(ListView.class);
         solo.goBack();
         solo.waitForActivity(OperationListActivity.class);
         solo.waitForView(ListView.class);
         tools.scrollDown();
         tools.sleep(1000);
         int nbOps = solo.getCurrentViews(ListView.class).get(0).getCount();
         tools.printCurrentTextViews();
         Log.d(TAG, "interface text : " + solo.getText(CUR_ACC_SUM_IDX).getText().toString()
                 + " / " + Formater.getSumFormater().format(1000.5 - nbOps));
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString()
                 .contains(Formater.getSumFormater().format(1000.5 - nbOps)));
         return nbOps;
     }
 
     public void testDelFutureOccurences() {
         TAG = "testDelFutureOccurences";
         int nbOps = setupDelOccFromOps();
         Log.d(TAG, "nbOPS : " + nbOps);
         solo.clickInList(nbOps - (nbOps - 2));
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.delete_op));
         solo.clickOnButton(solo.getString(R.string.del_all_following));
         tools.sleep(1000);
 //        solo.clickInList(solo.getCurrentViews(ListView.class).get(0).getCount());
 //        assertEquals(2, solo.getCurrentViews(ListView.class).get(0).getCount() - 1);
         int newNbOps = solo.getCurrentViews(ListView.class).get(0).getCount();
         assertTrue(newNbOps < nbOps);
         tools.printCurrentTextViews();
         Log.d(TAG, "interface text : " + solo.getText(CUR_ACC_SUM_IDX).getText().toString()
                 + " / " + Formater.getSumFormater().format(1000.5 - newNbOps) + " nbops / newnbops " + nbOps + " / " + newNbOps);
         assertTrue(solo.waitForText(Formater.getSumFormater().format(1000.5 - newNbOps)));
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1000.5 - newNbOps)));
     }
 
     public void testDelAllOccurencesFromOps() {
         TAG = "testDelAllOccurencesFromOps";
         int nbOps = setupDelOccFromOps();
         solo.clickInList(nbOps - (nbOps - 2));
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.delete_op));
         solo.clickOnButton(solo.getString(R.string.del_all_occurrences));
         assertTrue(solo.waitForText(solo.getString(R.string.no_operation)));
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1000.5)));
     }
 
     // issue 112 : it was about when clicking on + or - of date chooser then cancel that does not work
     // since android 3, the date picker has no buttons anymore, removed Picker usage
     public void testCancelSchEdition() {
         TAG = "testCancelSchEdition";
 //        Picker picker = new Picker(solo);
         setupDelOccFromOps();
         solo.clickOnActionBarItem(R.id.go_to_sch_op);
         solo.waitForActivity(ScheduledOpListActivity.class);
         solo.waitForView(ListView.class);
         tools.printCurrentTextViews();
         final CharSequence date = solo.getCurrentViews(TextView.class).get(2).getText();
         solo.clickInList(0);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.edit_op));
         solo.waitForActivity(ScheduledOperationEditor.class);
         solo.waitForDialogToClose(WAIT_DIALOG_TIME);
         GregorianCalendar today = Tools.createClearedCalendar();
         today.add(Calendar.MONTH, -2);
 //        picker.clickOnDatePicker(today.get(Calendar.MONTH) + 1,
 //                today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.YEAR));
         solo.setDatePicker(0, today.get(Calendar.YEAR),
                 today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
         solo.clickOnActionBarItem(R.id.confirm);
         solo.clickOnButton(solo.getString(R.string.cancel));
         solo.clickOnActionBarItem(R.id.cancel);
         solo.waitForActivity(ScheduledOpListActivity.class);
         solo.waitForView(ListView.class);
         tools.printCurrentTextViews();
         Log.d(TAG, "before date : " + date);
         assertEquals(date, solo.getCurrentViews(TextView.class).get(2).getText());
     }
 
     // issue 59 test
     public void testDeleteAllOccurences() {
         TAG = "testDeleteAllOccurences";
         setUpSchOp();
         solo.clickOnActionBarItem(R.id.create_operation);
         GregorianCalendar today = Tools.createClearedCalendar();
         today.add(Calendar.MONTH, -2);
         solo.setDatePicker(0, today.get(Calendar.YEAR),
                 today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
         solo.enterText(3, OP_TP);
         solo.enterText(4, "9,50");
         solo.enterText(5, OP_TAG);
         solo.enterText(6, OP_MODE);
         solo.enterText(7, OP_DESC);
         tools.scrollUp();
         solo.clickOnText(solo.getString(R.string.scheduling));
         solo.pressSpinnerItem(0, 1);
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForView(ListView.class);
         solo.goBack();
         solo.waitForView(ListView.class);
         assertEquals(3, solo.getCurrentViews(ListView.class).get(0).getCount());
 
         solo.clickOnActionBarItem(R.id.go_to_sch_op);
         solo.clickInList(0);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.delete_op));
         solo.clickOnButton(solo.getString(R.string.del_all_occurrences));
         assertTrue(solo.waitForText(solo.getString(R.string.no_operation_sch)));
         solo.goBack();
         solo.waitForActivity(OperationListActivity.class);
         assertTrue(solo.waitForText(solo.getString(R.string.no_operation)));
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString()
                 .contains(Formater.getSumFormater().format(1000.50)));
     }
 
     /**
      * Infos
      */
 
     // test adding info with different casing
     public void testAddExistingInfo() {
         TAG = "testAddExistingInfo";
         setUpOpTest();
         solo.clickOnActionBarItem(R.id.create_operation);
         solo.waitForActivity(OperationEditor.class);
         solo.enterText(3, OP_TP);
         for (int i = 0; i < OP_AMOUNT.length(); ++i) {
             solo.enterText(4, String.valueOf(OP_AMOUNT.charAt(i)));
         }
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.edit_op_third_parties_list));
         solo.clickOnButton(solo.getString(R.string.create));
         solo.enterText(0, "Atest");
         solo.clickOnButton(solo.getString(R.string.ok));
         solo.waitForView(ListView.class);
         assertEquals(1, solo.getCurrentViews(ListView.class).get(0).getCount());
         solo.clickOnButton(solo.getString(R.string.create));
         solo.enterText(0, "ATest");
         solo.clickOnButton(solo.getString(R.string.ok));
         assertNotNull(solo
                 .getText(solo.getString(fr.geobert.radis.R.string.item_exists)));
     }
 
     // issue 50 test
     public void testAddInfoAndCreateOp() {
         TAG = "testAddInfoAndCreateOp";
         setUpOpTest();
         solo.clickOnActionBarItem(R.id.create_operation);
         solo.enterText(3, OP_TP);
         for (int i = 0; i < OP_AMOUNT.length(); ++i) {
             solo.enterText(4, String.valueOf(OP_AMOUNT.charAt(i)));
         }
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.edit_op_third_parties_list));
         solo.waitForText(solo.getString(R.string.create));
         solo.clickOnButton(solo.getString(R.string.create));
         solo.waitForView(EditText.class);
         solo.enterText(0, "Atest");
         solo.clickOnButton(solo.getString(R.string.ok));
         tools.sleep(1000);
         solo.clickInList(0);
         tools.sleep(500);
         solo.clickOnButton(solo.getString(R.string.ok));
         solo.waitForDialogToClose(500);
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForActivity(OperationListActivity.class);
         solo.waitForView(ListView.class);
         solo.clickOnActionBarItem(R.id.create_operation);
         solo.waitForActivity(OperationEditor.class);
         solo.waitForView(ImageButton.class);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.edit_op_third_parties_list));
         solo.waitForView(ListView.class);
         assertEquals(1, solo.getCurrentViews(ListView.class).get(0).getCount());
     }
 
     private void addOpOnDate(GregorianCalendar t, int idx) {
         solo.clickOnActionBarItem(R.id.create_operation);
         assertTrue(solo.waitForActivity(OperationEditor.class));
         assertTrue(solo.waitForView(DatePicker.class));
         solo.setDatePicker(0, t.get(Calendar.YEAR), t.get(Calendar.MONTH),
                 t.get(Calendar.DAY_OF_MONTH));
         solo.enterText(3, OP_TP + "/" + idx);
         solo.enterText(4, "1");
         solo.clickOnActionBarItem(R.id.confirm);
         assertTrue(solo.waitForActivity(OperationListActivity.class));
 //        assertTrue(solo.waitForView(ListView.class));
     }
 
     private void setUpProjTest1() {
         addAccount();
         GregorianCalendar today = Tools.createClearedCalendar();
         today.set(Calendar.DAY_OF_MONTH, Math.min(today.get(Calendar.DAY_OF_MONTH), 28));
         today.add(Calendar.MONTH, -2);
         for (int i = 0; i < 6; ++i) {
             addOpOnDate(today, i);
             today.add(Calendar.MONTH, +1);
         }
     }
 
     public void testProjectionFromOpList() {
         TAG = "testProjectionFromOpList";
         // test mode 0
         setUpProjTest1();
         assertTrue(solo.waitForActivity(OperationListActivity.class));
         assertTrue(solo.waitForView(ListView.class));
         GregorianCalendar today = Tools.createClearedCalendar();
         today.set(Calendar.DAY_OF_MONTH, Math.min(today.get(Calendar.DAY_OF_MONTH), 28));
         today.add(Calendar.MONTH, 3);
         tools.printCurrentTextViews();
         String projSumTxt = solo.getCurrentViews(TextView.class).get(CUR_ACC_SUM_IDX).getText().toString();
         String projDateTxt = solo.getCurrentViews(TextView.class).get(CUR_ACC_PROJ_DATE_IDX).getText().toString();
         Log.d(TAG, "testProjectionFromOpList : " + Tools.getDateStr(today) + " VS " + projDateTxt + "/" + projSumTxt);
         assertTrue(projDateTxt.contains(Tools.getDateStr(today)));
         assertTrue(projSumTxt.contains(Formater.getSumFormater().format(994.50)));
         assertTrue(solo.waitForView(ListView.class));
         tools.scrollUp();
         tools.printCurrentTextViews();
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(994.50)));
 
         // test mode 1
         solo.pressMenuItem(4);
         solo.waitForActivity(AccountEditor.class);
         solo.pressSpinnerItem(1, 1);
         tools.hideKeyboard();
         assertTrue(solo.waitForView(EditText.class));
         assertTrue(solo.getCurrentViews(EditText.class).get(3).isEnabled());
         today = Tools.createClearedCalendar();
         today.set(Calendar.DAY_OF_MONTH, Math.min(today.get(Calendar.DAY_OF_MONTH), 28));
         solo.enterText(3, Integer.toString(Math.min(today.get(Calendar.DAY_OF_MONTH), 28)));
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForActivity(OperationListActivity.class);
         solo.waitForView(ListView.class);
         today.add(Calendar.MONTH, 1);
         Log.d(TAG, "1DATE : " + Tools.getDateStr(today));
         projDateTxt = solo.getCurrentViews(TextView.class).get(CUR_ACC_PROJ_DATE_IDX).getText().toString();
         Log.d(TAG, "1DATE displayed : " + projDateTxt);
         tools.printCurrentTextViews();
         assertTrue(projDateTxt.contains(Tools.getDateStr(today)));
         assertTrue(solo.getCurrentViews(TextView.class).get(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(996.50)));
         solo.clickInList(0);
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(994.50)));
 
         // test mode 2
         solo.pressMenuItem(4);
         solo.waitForActivity(AccountEditor.class);
         solo.pressSpinnerItem(1, 1);
         tools.hideKeyboard();
         assertTrue(solo.waitForView(EditText.class));
         assertTrue(solo.getEditText(3).isEnabled());
         today = Tools.createClearedCalendar();
         today.set(Calendar.DAY_OF_MONTH, 28);
         today.add(Calendar.MONTH, +3);
         solo.enterText(3, Tools.getDateStr(today));
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForActivity(OperationListActivity.class);
         solo.waitForView(ListView.class);
         Log.d(TAG, "2DATE : " + Tools.getDateStr(today));
         projDateTxt = solo.getCurrentViews(TextView.class).get(CUR_ACC_PROJ_DATE_IDX).getText().toString();
         Log.d(TAG, "2DATE displayed : " + projDateTxt);
         tools.printCurrentTextViews();
         assertTrue(projDateTxt.contains(Tools.getDateStr(today)));
         solo.clickInList(0);
 //        assertTrue(solo.getText(1).getText().toString().contains("994,50"));
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(994.50)));
         tools.scrollDown();
         tools.sleep(1000);
         tools.scrollDown();
         tools.sleep(1000);
         tools.scrollDown();
         solo.clickInList(5);
         tools.sleep(1000);
         tools.printCurrentTextViews();
         assertTrue(solo.getCurrentViews(TextView.class).get(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(994.50)));
         assertTrue(solo.getText(25).getText().toString().contains(Formater.getSumFormater().format(998.50)));
 
         // test back to mode 0
         solo.pressMenuItem(4);
         solo.waitForActivity(RadisConfiguration.class);
         solo.pressSpinnerItem(1, -2);
         tools.hideKeyboard();
         assertTrue(solo.waitForView(EditText.class));
         assertFalse(solo.getEditText(3).isEnabled());
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForActivity(OperationListActivity.class);
         solo.waitForView(ListView.class);
         Log.d(TAG, "0DATE : " + Tools.getDateStr(today));
         projDateTxt = solo.getCurrentViews(TextView.class).get(CUR_ACC_PROJ_DATE_IDX).getText().toString();
         projSumTxt = solo.getCurrentViews(TextView.class).get(CUR_ACC_SUM_IDX).getText().toString();
         Log.d(TAG, "0DATE displayed : " + projDateTxt);
         Log.d(TAG, "solo.getButton(0).getText() : " + projSumTxt);
         tools.printCurrentTextViews();
         assertTrue(projSumTxt.contains(Formater.getSumFormater().format(994.50)));
         assertTrue(solo.getText(25).getText().toString().contains(Formater.getSumFormater().format(998.50)));
     }
 
     public void addOpMode1() {
         // add account
         assertTrue(solo.waitForActivity(AccountEditor.class));
         solo.enterText(0, ACCOUNT_NAME);
         solo.enterText(1, ACCOUNT_START_SUM);
         solo.enterText(4, ACCOUNT_DESC);
 
         solo.pressSpinnerItem(1, 1);
         tools.hideKeyboard();
         assertTrue(solo.waitForView(EditText.class));
         assertTrue(solo.getEditText(3).isEnabled());
         GregorianCalendar today = Tools.createClearedCalendar();
         today.add(Calendar.DAY_OF_MONTH, 1);
         solo.enterText(3, Integer.toString(today.get(Calendar.DAY_OF_MONTH)));
         solo.clickOnActionBarItem(R.id.confirm);
 
         assertTrue(solo.waitForActivity(OperationListActivity.class));
         assertTrue(solo.waitForText(solo.getString(R.string.no_operation)));
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1000.50)));
 //        Log.d(TAG, "addOpMode1 before add " + solo.getCurrentViews(ListView.class).get(0).getCount());
         today.add(Calendar.DAY_OF_MONTH, -1);
         addOpOnDate(today, 0);
         tools.printCurrentTextViews();
         solo.pressSpinnerItem(0, -1);
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(999.50)));
         solo.clickInList(0);
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(999.50)));
 //        Log.d(TAG, "addOpMode1 after one add " + solo.getCurrentViews(ListView.class).get(0).getCount());
         // add op after X
         today.add(Calendar.MONTH, +1);
         addOpOnDate(today, 1);
         solo.clickInList(0);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(999.50)));
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(998.50)));
 //        Log.d(TAG, "addOpMode1 after two add " + solo.getCurrentViews(ListView.class).get(0).getCount());
         // add op before X of next month, should update the current sum
         today.add(Calendar.MONTH, -2);
         addOpOnDate(today, 2);
         // Log.d(TAG, "addOpMode1 after three add " +
         // solo.getCurrentListViews().get(0).getCount());
         solo.clickInList(0);
         tools.printCurrentButtons();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(998.50)));
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(997.50)));
     }
 
     public void editOpMode1() {
         addOpMode1();
 
         solo.clickInList(0);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.edit_op));
         assertTrue(solo.waitForActivity(OperationEditor.class));
         solo.clearEditText(SUM_FIELD_IDX);
         solo.enterText(SUM_FIELD_IDX, "+2");
         solo.clickOnActionBarItem(R.id.confirm);
         assertTrue(solo.waitForActivity(OperationListActivity.class));
         solo.clickInList(0);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(998.50)));
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(1000.50)));
         // Log.d(TAG, "editOpMode1 after one edit " + solo.getCurrentListViews().get(0).getCount());
 
         solo.clickInList(3);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.edit_op));
         solo.clearEditText(SUM_FIELD_IDX);
         solo.enterText(SUM_FIELD_IDX, "+2");
         solo.clickOnActionBarItem(R.id.confirm);
         assertTrue(solo.waitForActivity(OperationListActivity.class));
         assertTrue(solo.waitForView(ListView.class));
         // Log.d(TAG, "editOpMode1 after one edit " + solo.getCurrentListViews().get(0).getCount());
         solo.clickInList(0);
         assertEquals(3, solo.getCurrentViews(ListView.class).get(0).getCount());
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1001.50)));
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(1003.50)));
     }
 
     public void addOpMode2() {
         // add account
         assertTrue(solo.waitForActivity(AccountEditor.class));
         solo.enterText(0, ACCOUNT_NAME);
         solo.enterText(1, ACCOUNT_START_SUM);
         solo.enterText(4, ACCOUNT_DESC);
         solo.pressSpinnerItem(1, 2);
         tools.hideKeyboard();
         assertTrue(solo.waitForView(EditText.class));
         assertTrue(solo.getEditText(3).isEnabled());
         GregorianCalendar today = Tools.createClearedCalendar();
         today.add(Calendar.DAY_OF_MONTH, 1);
         solo.enterText(3, Integer.toString(today.get(Calendar.DAY_OF_MONTH)) + "/" +
                 Integer.toString(today.get(Calendar.MONTH) + 1) + "/" + Integer.toString(today.get(Calendar.YEAR)));
         solo.clickOnActionBarItem(R.id.confirm);
         assertTrue(solo.waitForActivity(OperationListActivity.class));
 //        assertTrue(solo.waitForView(ListView.class));
 //        Log.d(TAG, "addOpMode2 before add " + solo.getCurrentViews(ListView.class).get(0).getCount());
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1000.50)));
         today.add(Calendar.DAY_OF_MONTH, -1);
         addOpOnDate(today, 0);
         // Log.d(TAG, "addOpMode2 after one add " + solo.getCurrentListViews().get(0).getCount());
         tools.printCurrentTextViews();
         solo.clickInList(0);
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(999.50)));
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(999.50)));
 
         // add op after X
         today.add(Calendar.MONTH, +1);
         addOpOnDate(today, 1);
         // Log.d(TAG, "addOpMode2 after two add " +
         // solo.getCurrentListViews().get(0).getCount());
         solo.clickInList(0);
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(999.50)));
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(998.50)));
 
         // add op before X of next month, should update the current sum
         today.add(Calendar.MONTH, -2);
         addOpOnDate(today, 2);
         // Log.d(TAG, "addOpMode2 after three add " +
         // solo.getCurrentListViews().get(0).getCount());
         solo.clickInList(0);
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(998.50)));
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(997.50)));
     }
 
     public void editOpMode2() {
         addOpMode2();
 
         solo.clickInList(0);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.edit_op));
         assertTrue(solo.waitForActivity(OperationEditor.class));
         solo.clearEditText(SUM_FIELD_IDX);
         solo.enterText(SUM_FIELD_IDX, "+2");
         solo.clickOnActionBarItem(R.id.confirm);
         assertTrue(solo.waitForActivity(OperationListActivity.class));
         assertTrue(solo.waitForView(ListView.class));
         solo.clickInList(0);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(998.50)));
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(1000.50)));
         // Log.d(TAG, "editOpMode2 after one edit " + solo.getCurrentListViews().get(0).getCount());
         solo.clickInList(3);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.edit_op));
         solo.clearEditText(4);
         solo.enterText(4, "+2");
         solo.clickOnActionBarItem(R.id.confirm);
         assertTrue(solo.waitForActivity(OperationListActivity.class));
         assertTrue(solo.waitForView(ListView.class));
         solo.clickInList(0);
         // Log.d(TAG, "editOpMode2 after two edit " + solo.getCurrentListViews().get(0).getCount());
         assertEquals(3, solo.getCurrentViews(ListView.class).get(0).getCount());
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1001.50)));
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(1003.50)));
     }
 
     private void delOps() {
         solo.clickInList(0);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.delete_op));
         solo.clickOnButton(solo.getString(R.string.yes));
         solo.waitForDialogToClose(WAIT_DIALOG_TIME);
         String sum = Formater.getSumFormater().format(1001.50);
         solo.clickInList(0);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(sum));
         solo.clickInList(2);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.delete_op));
         solo.clickOnButton(solo.getString(R.string.yes));
         solo.waitForDialogToClose(WAIT_DIALOG_TIME);
         solo.clickInList(0);
         String sum2 = Formater.getSumFormater().format(999.50);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(sum2));
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(sum2));
     }
 
     public void testDelOpMode1() {
         TAG = "testDelOpMode1";
         editOpMode1();
         delOps();
     }
 
     public void testDelOpMode2() {
         TAG = "testDelOpMode2";
         editOpMode2();
         delOps();
     }
 
     // test transfert
 
     private void addTransfertOp() {
         solo.clickOnActionBarItem(R.id.create_operation);
         assertTrue(solo.waitForActivity(OperationEditor.class));
         assertTrue(solo.waitForView(CheckBox.class));
         solo.clickOnCheckBox(0);
         solo.pressSpinnerItem(0, -2);
         solo.pressSpinnerItem(0, 1);
         solo.pressSpinnerItem(1, 2);
         for (int i = 0; i < OP_AMOUNT.length(); ++i) {
             solo.enterText(3, String.valueOf(OP_AMOUNT.charAt(i)));
         }
         solo.enterText(4, OP_TAG);
         solo.enterText(5, OP_MODE);
         solo.enterText(6, OP_DESC);
         solo.clickOnActionBarItem(R.id.confirm);
         assertTrue(solo.waitForActivity(OperationListActivity.class));
         assertTrue(solo.waitForView(ListView.class));
         solo.pressSpinnerItem(0, -1);
         tools.printCurrentTextViews();
         solo.clickInList(0);
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(990.00)));
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(990.00)));
         assertTrue(solo.getText(9).getText().toString().equals(OP_AMOUNT_FORMATED));
     }
 
     public void simpleTransfert() {
         addAccount();
         addAccount2();
         assertTrue(solo.waitForActivity(OperationListActivity.class));
         addTransfertOp();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(990.00)));
         solo.pressSpinnerItem(0, 1);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(2011.00)));
         solo.pressSpinnerItem(0, -1);
     }
 
     public void testDelSimpleTransfert() {
         TAG = "testDelSimpleTransfert";
         simpleTransfert();
         solo.clickInList(0);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.delete_op));
         solo.clickOnButton(solo.getString(R.string.yes));
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1000.50)));
         solo.pressSpinnerItem(0, 1);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(2000.50)));
     }
 
     public void testEditTransfertToNoTransfertAnymore() {
         TAG = "testEditTransfertToNoTransfertAnymore";
         simpleTransfert();
         solo.clickInList(0);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.edit_op));
         solo.waitForActivity(OperationEditor.class);
         tools.sleep(1000);
         solo.clickOnCheckBox(0);
         solo.enterText(3, OP_TP);
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForActivity(OperationListActivity.class);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(990.00)));
         solo.pressSpinnerItem(0, 1);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(2000.50)));
     }
 
     public void testEditSimpleTrans3accounts() {
         TAG = "testEditSimpleTrans3accounts";
         simpleTransfert();
         addAccount3();
         solo.clickInList(0);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.edit_op));
         assertTrue(solo.waitForActivity(OperationEditor.class));
         assertTrue(solo.waitForView(Spinner.class));
         solo.pressSpinnerItem(1, 1);
         solo.clickOnActionBarItem(R.id.confirm);
         assertTrue(solo.waitForActivity(OperationListActivity.class));
         tools.printCurrentTextViews();
         solo.pressSpinnerItem(0, -1);
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(990.00)));
         solo.pressSpinnerItem(0, 1);
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(2000.50)));
         solo.pressSpinnerItem(0, 1);
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1011.00)));
     }
 
     private void setUpSchTransOp() {
         addAccount();
         addAccount2();
         solo.clickOnActionBarItem(R.id.go_to_preferences);
         solo.waitForActivity(RadisConfiguration.class);
         assertTrue(solo.waitForText(solo.getString(R.string.prefs_insertion_date_label)));
         solo.clickOnText(solo.getString(R.string.prefs_insertion_date_label));
         solo.clearEditText(0);
         GregorianCalendar today = Tools.createClearedCalendar();
         today.add(Calendar.DAY_OF_MONTH, -1);
         solo.enterText(0, Integer.toString(today.get(Calendar.DAY_OF_MONTH)));
         solo.clickOnButton(solo.getString(R.string.ok));
         solo.goBack();
     }
 
     private void addSchTransfert() {
         solo.clickOnActionBarItem(R.id.create_operation);
         solo.waitForActivity(ScheduledOperationEditor.class);
         GregorianCalendar today = Tools.createClearedCalendar();
         solo.setDatePicker(0, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
         solo.clickOnCheckBox(0);
         solo.waitForView(Spinner.class);
         solo.pressSpinnerItem(1, 2);
         for (int i = 0; i < OP_AMOUNT.length(); ++i) {
             solo.enterText(3, String.valueOf(OP_AMOUNT.charAt(i)));
         }
         solo.enterText(4, OP_TAG);
         solo.enterText(5, OP_MODE);
         solo.enterText(6, OP_DESC);
         solo.clickOnActionBarItem(R.id.confirm);
         assertTrue(solo.waitForActivity(ScheduledOpListActivity.class));
     }
 
     public void makeSchTransfertFromAccList() {
         setUpSchTransOp();
         solo.clickOnActionBarItem(R.id.go_to_sch_op);
         assertTrue(solo.waitForActivity(ScheduledOpListActivity.class));
         addSchTransfert();
         solo.goBack();
         assertTrue(solo.waitForActivity(OperationListActivity.class));
         solo.pressSpinnerItem(0, -1);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(990.00)));
         solo.pressSpinnerItem(0, 1);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(2011.00)));
         solo.pressSpinnerItem(0, -1);
     }
 
     public void testDelSchTransfert() {
         TAG = "testDelSchTransfert";
         makeSchTransfertFromAccList();
         solo.clickOnActionBarItem(R.id.go_to_sch_op);
         solo.waitForActivity(ScheduledOpListActivity.class);
         solo.clickInList(0);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.delete_op));
         solo.clickOnButton(solo.getString(R.string.del_all_occurrences));
         tools.sleep(1000);
         assertTrue(solo.waitForText(solo.getString(R.string.no_operation_sch)));
         solo.goBack();
         solo.waitForActivity(OperationListActivity.class);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1000.50)));
         solo.pressSpinnerItem(0, 1);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(2000.50)));
     }
 
     private void addSchTransfertHebdo() {
         solo.clickOnActionBarItem(R.id.create_operation);
         solo.waitForActivity(ScheduledOperationEditor.class);
         GregorianCalendar today = Tools.createClearedCalendar();
         today.set(Calendar.DAY_OF_MONTH, 12);
         today.add(Calendar.MONTH, -1);
         solo.setDatePicker(0, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
         solo.clickOnCheckBox(0);
         solo.waitForView(Spinner.class);
         solo.pressSpinnerItem(1, 2);
         for (int i = 0; i < OP_AMOUNT.length(); ++i) {
             solo.enterText(3, String.valueOf(OP_AMOUNT.charAt(i)));
         }
         solo.enterText(4, OP_TAG);
         solo.enterText(5, OP_MODE);
         solo.enterText(6, OP_DESC);
         tools.scrollUp();
         solo.clickOnText(solo.getString(R.string.scheduling));
         solo.waitForText(solo.getString(R.string.account));
         solo.pressSpinnerItem(1, -1);
         solo.clickOnActionBarItem(R.id.confirm);
         solo.waitForActivity(ScheduledOpListActivity.class);
         solo.goBack();
         assertTrue(solo.waitForActivity(OperationListActivity.class));
         assertTrue(solo.waitForView(ListView.class));
         tools.printCurrentTextViews();
         double sum = 8 * 10.50;
         solo.pressSpinnerItem(0, -1);
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1000.50 - sum)));
         solo.pressSpinnerItem(0, 1);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(2000.50 + sum)));
         solo.pressSpinnerItem(0, -1);
     }
 
     public void makeSchTransfertHebdoFromAccList() {
         setUpSchTransOp();
         solo.clickOnActionBarItem(R.id.go_to_sch_op);
         assertTrue(solo.waitForActivity(ScheduledOpListActivity.class));
         addSchTransfertHebdo();
     }
 
     public void testDelSchTransfFromOpsList() {
         TAG = "testDelSchTransfFromOpsList";
         makeSchTransfertHebdoFromAccList();
         solo.clickInList(0);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.delete_op));
         solo.clickOnText(solo.getString(R.string.del_only_current));
         tools.sleep(1000);
         tools.printCurrentTextViews();
         double sum = 7 * 10.50;
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1000.50 - sum)));
         solo.pressSpinnerItem(0, 1);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(2000.50 + sum)));
     }
 
     public void testDelAllOccSchTransfFromOpsList() {
         TAG = "testDelAllOccSchTransfFromOpsList";
         makeSchTransfertHebdoFromAccList();
         solo.clickInList(0);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.delete_op));
         solo.clickOnText(solo.getString(R.string.del_all_occurrences));
         tools.sleep(1000);
         tools.printCurrentTextViews();
         String startSum = Formater.getSumFormater().format(1000.50);
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(startSum));
         solo.pressSpinnerItem(0, 1);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(2000.50)));
     }
 
     public void testDelFutureSchTransfFromOpsList() {
         TAG = "testDelFutureSchTransfFromOpsList";
         makeSchTransfertHebdoFromAccList();
         solo.clickInList(3);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.delete_op));
         solo.clickOnText(solo.getString(R.string.del_all_following));
         tools.sleep(1000);
         tools.printCurrentTextViews();
         double sum = 5 * 10.50;
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1000.50 - sum)));
         solo.pressSpinnerItem(0, 1);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(2000.50 + sum)));
     }
 
     public void testDelAllOccSchTransfFromSchList() {
         TAG = "testDelAllOccSchTransfFromSchList";
         makeSchTransfertHebdoFromAccList();
         solo.clickOnActionBarItem(R.id.go_to_sch_op);
         assertTrue(solo.waitForActivity(ScheduledOpListActivity.class));
         solo.clickInList(0);
         solo.clickOnImageButton(tools.findIndexOfImageButton(R.id.delete_op));
         solo.clickOnText(solo.getString(R.string.del_all_occurrences));
         tools.sleep(1000);
         solo.goBack();
         solo.waitForActivity(OperationListActivity.class);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(1000.50)));
         solo.pressSpinnerItem(0, 1);
         tools.printCurrentTextViews();
         assertTrue(solo.getText(CUR_ACC_SUM_IDX).getText().toString().contains(Formater.getSumFormater().format(2000.50)));
     }
 
     // issue #30 on github
     public void testSumAtSelectionOnOthersAccount() {
         addAccount();
         addAccount2();
         solo.pressSpinnerItem(0, 1);
         addOp();
         tools.printCurrentTextViews();
         assertTrue(solo.getText(FIRST_SUM_AT_SEL_IDX).getText().toString().contains(Formater.getSumFormater().format(2000.50 - 10.50)));
     }
 }
