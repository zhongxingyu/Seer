 package com.openfeint.qa.ggp.step_definitions;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.gree.asdk.api.Achievement;
 import net.gree.asdk.api.Achievement.AchievementChangeListener;
 import net.gree.asdk.api.Achievement.AchievementListUpdateListener;
 
 import org.apache.http.HeaderIterator;
 
 import util.Consts;
 import android.util.Log;
 
 import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
 import com.openfeint.qa.core.command.After;
 import com.openfeint.qa.core.command.Given;
 import com.openfeint.qa.core.command.Then;
 import com.openfeint.qa.core.command.When;
 
 public class Achievement_StepDefinitions extends BasicStepDefinition {
 	private static final String TAG = "Achievement_Steps";
 
	private static List<Achievement> achievementList = new ArrayList<Achievement>();
 
 	private String status;
 
 	// waiting for the async callback and assert it success
 	private void waitCallback() {
 		while (true) {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			if (status != Consts.UNKNOWN) {
 				assertEquals("server response", Consts.SUCCESS, status);
 				break;
 			}
 		}
 	}
 
 	private AchievementListUpdateListener achiListener = new AchievementListUpdateListener() {
 
 		@Override
 		public void onSuccess(int index, int totalListSize,
 				Achievement[] requestedElements) {
 			Log.d(TAG, "Get achievement list success!");
			achievementList.clear();
 			if (requestedElements != null) {
 				Log.i(TAG, "Adding achievement datas");
 				for (int i = 0; i < requestedElements.length; i++) {
 					achievementList.add(requestedElements[i]);
 				}
 			}
 			status = Consts.SUCCESS;
 		}
 
 		@Override
 		public void onFailure(int responseCode, HeaderIterator headers,
 				String response) {
 			Log.e(TAG, "Get achievement list failed!");
			achievementList.clear();
 			status = Consts.FAILED;
 		}
 	};
 
 	private AchievementChangeListener lockListener = new AchievementChangeListener() {
 
 		@Override
 		public void onSuccess() {
 			Log.d(TAG, "Change lock status success");
 			status = Consts.SUCCESS;
 		}
 
 		@Override
 		public void onFailure(int responseCode, HeaderIterator headers,
 				String response) {
 			Log.e(TAG, "Change lock status failed");
 			status = Consts.FAILED;
 		}
 
 	};
 
 	@When("I load list of achievement")
 	public void getAchievements() {
 		status = Consts.UNKNOWN;
 		Achievement.loadAchievements(Consts.startIndex_1, Consts.pageSize,
 				achiListener);
 		waitCallback();
 	}
 
 	@Then("I should have total achievements (\\d+)")
 	public void verifyAchievementCount(int size) {
 		if (achievementList == null)
 			fail("No achievement found!");
 		Log.i(TAG, "Verifing the achievement count...");
 		assertEquals("achievement count", size, achievementList.size());
 	}
 
 	private int transLockStatus(String statusMark) {
 		if ("LOCK".equals(statusMark))
 			return Achievement.LOCKED;
 		else if ("UNLOCK".equals(statusMark))
 			return Achievement.UNLOCKED;
 		else {
 			fail("Unknown lock status!");
 			return -1;
 		}
 	}
 
 	@Then("I should have achievement of name (.+) with status (\\w+) and score (\\d+)")
 	public void verifyAchievementInfo(String achiName, String statusMark,
 			int score) {
 		Achievement achi = getAchievementByName(achiName);
 		int lockStatus = transLockStatus(statusMark);
 
 		assertEquals("lock status of achievement " + achiName, lockStatus,
 				achi.isUnlocked());
 		assertEquals("score of achievement " + achiName, score, achi.getScore());
 	}
 
 	@Given("I load list of achievement")
 	public void getAchievementsAsCondition() {
 		getAchievements();
 	}
 
 	private void updateLockStatus(Achievement achi, int expectStatus) {
 		status = Consts.UNKNOWN;
 		if (expectStatus == Achievement.UNLOCKED) {
 			Log.i(TAG, "Unlocking the achievement " + achi.getName());
 			achi.unlock(lockListener);
 		} else if (expectStatus == Achievement.LOCKED) {
 			Log.i(TAG, "Locking the achievement " + achi.getName());
 			achi.lock(lockListener);
 		} else {
 			fail("Unknow lock status of the achievement: " + achi.getName());
 		}
 		waitCallback();
 	}
 
 	@Given("I make sure status of achievement (.+) is (\\w+)")
 	public void updateStatusAsCondition(String achiName, String statusMark) {
 		Achievement achi = getAchievementByName(achiName);
 		int expectStatus = transLockStatus(statusMark);
 
 		Log.i(TAG, "Expect status of achievement " + achiName + " is "
 				+ statusMark);
 		if (achi.isUnlocked() != expectStatus) {
 			Log.i(TAG, "But no the status we want!");
 			updateLockStatus(achi, expectStatus);
 		}
 	}
 
 	@When("I update status of achievement (.+) to (\\w+)")
 	public void updateLockStatusByName(String achiName, String statusMark) {
 		Achievement achi = getAchievementByName(achiName);
 		int expectStatus = transLockStatus(statusMark);
 
 		Log.i(TAG, "Got the achievement " + achiName
 				+ ", and update its status to : " + statusMark);
 		updateLockStatus(achi, expectStatus);
 	}
 
 	private Achievement getAchievementByName(String achiName) {
 		if (achievementList == null)
 			fail("No achievement in the list!");
 		for (Achievement achi : achievementList) {
 			if (achiName.equals(achi.getName())) {
 				Log.d(TAG, "Found the achievement " + achiName);
 				return achi;
 			}
 		}
 		fail("Could not find the achievement named " + achiName);
 		return null;
 	}
 
 	@Then("status of achievement (.+) should be (\\w+)")
 	public void verifyLockStatusByName(String achiName, String statusMark) {
 		Achievement achi = getAchievementByName(achiName);
 		int lockStatus = transLockStatus(statusMark);
 
 		Log.i(TAG, "Verifing the lock status of achievement " + achiName);
 		assertEquals("lock status of achievement " + achiName, lockStatus,
 				achi.isUnlocked());
 	}
 
 	@Then("my score should be (\\w+) by (\\d+)")
 	public void verifyScoreUpdated(String operator, int changeValue) {
 		// TODO So far, sdk is not support to get the total score of user, will
 		// add this verification after sdk to support it
 	}
 
 	@After("I make sure status of achievement (.+) is (\\w+)")
 	public void updateStatusToRecoverData(String achiName, String statusMark) {
 		Log.i(TAG, "Recover data after this run...");
 		updateStatusAsCondition(achiName, statusMark);
 	}
 
 }
