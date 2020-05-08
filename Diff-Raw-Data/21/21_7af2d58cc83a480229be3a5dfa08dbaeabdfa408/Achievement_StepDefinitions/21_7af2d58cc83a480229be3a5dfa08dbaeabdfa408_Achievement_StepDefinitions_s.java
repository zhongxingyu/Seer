 
 package com.openfeint.qa.ggp.step_definitions;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.fail;
 
 import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
 import com.openfeint.qa.core.command.After;
 import com.openfeint.qa.core.command.Given;
 import com.openfeint.qa.core.command.Then;
 import com.openfeint.qa.core.command.When;
 
 import net.gree.asdk.api.Achievement;
 import net.gree.asdk.api.Achievement.AchievementChangeListener;
 import net.gree.asdk.api.Achievement.AchievementListUpdateListener;
 
 import org.apache.http.HeaderIterator;
 
 import util.Consts;
 
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class Achievement_StepDefinitions extends BasicStepDefinition {
     private static final String TAG = "Achievement_Steps";
 
     private static String ACHIEVEMENT_LIST = "achievementlist";
 
    private int transLockStatus(String statusMark) {
         if ("LOCK".equals(statusMark))
            return Achievement.LOCKED;
         else if ("UNLOCK".equals(statusMark))
            return Achievement.UNLOCKED;
         else {
            return -1;
         }
     }
 
     private AchievementChangeListener lockListener = new AchievementChangeListener() {
 
         @Override
         public void onSuccess() {
             Log.d(TAG, "Change lock status success");
             notifyStepPass();
         }
 
         @Override
         public void onFailure(int responseCode, HeaderIterator headers, String response) {
             Log.e(TAG, "Change lock status failed " + responseCode + "||||||" + response);
 
             notifyStepPass();
         }
 
     };
 
     @When("I load list of achievement")
     @Given("I load list of achievement")
     public void getAchievements() {
         Achievement.loadAchievements(Consts.startIndex_1, Consts.pageSize,
                 new AchievementListUpdateListener() {
                     @Override
                     public void onSuccess(int index, int totalListSize,
                             Achievement[] requestedElements) {
                         Log.d(TAG, "Get achievement list success!");
                         getBlockRepo().put(ACHIEVEMENT_LIST, new ArrayList<Achievement>());
                         if (requestedElements != null) {
                             Log.i(TAG, "Adding achievement datas");
                             ((ArrayList<Achievement>) getBlockRepo().get(ACHIEVEMENT_LIST))
                                     .addAll(Arrays.asList(requestedElements));
                         }
                         notifyStepPass();
                     }
 
                     @Override
                     public void onFailure(int responseCode, HeaderIterator headers, String response) {
                         Log.e(TAG, "Get achievement list failed!");
                         getBlockRepo().put(ACHIEVEMENT_LIST, new ArrayList<Achievement>());
                         notifyStepPass();
                     }
                 });
     }
 
     @Then("I should have total achievements (\\d+)")
     public void verifyAchievementCount(int size) {
         ArrayList<Achievement> a = (ArrayList<Achievement>) getBlockRepo().get(ACHIEVEMENT_LIST);
         if (a == null)
             fail("No achievement found!");
 
         Log.i(TAG, "Verifing the achievement count...");
         assertEquals("achievement count", size, a.size());
         notifyStepPass();
     }
 
     @Then("I should have achievement of name (.+) with status (\\w+) and score (\\d+)")
     public void verifyAchievementInfo(String achiName, String statusMark, int score) {
         ArrayList<Achievement> a = (ArrayList<Achievement>) getBlockRepo().get(ACHIEVEMENT_LIST);
         if (a == null)
             fail("No achievement in the list!");
 
         for (Achievement achi : a) {
             if (achiName.equals(achi.getName())) {
                 Log.d(TAG, "Found the achievement " + achiName);

                 assertEquals("lock status of achievement " + achiName, transLockStatus(statusMark),
                         achi.isUnlocked());
                 assertEquals("score of achievement " + achiName, score, achi.getScore());
                 notifyStepPass();
                 return;
             }
         }
         fail("Could not find the achievement named " + achiName);
     }
 
     @Given("I make sure status of achievement (.+) is (\\w+)")
     @After("I make sure status of achievement (.+) is (\\w+)")
     @When("I update status of achievement (.+) to (\\w+)")
     public void updateLockStatusByName(String achiName, String statusMark) {
         ArrayList<Achievement> a = (ArrayList<Achievement>) getBlockRepo().get(ACHIEVEMENT_LIST);
         if (a == null)
             fail("No achievement in the list!");
 
         for (Achievement achi : a) {
             if (achiName.equals(achi.getName())) {
                 Log.d(TAG, "Found the achievement " + achiName);
 
                if (transLockStatus(statusMark) == Achievement.UNLOCKED) {
                     Log.i(TAG, "Unlocking the achievement " + achi.getName());
                     achi.unlock(lockListener);
                } else if (transLockStatus(statusMark) == Achievement.LOCKED) {
                     Log.i(TAG, "Locking the achievement " + achi.getName());
                     achi.lock(lockListener);
                 }
                 return;
             }
         }
         fail("Could not find the achievement named " + achiName);
     }
 
     @Then("status of achievement (.+) should be (\\w+)")
     public void verifyLockStatusByName(String achiName, String statusMark) {
         ArrayList<Achievement> a = (ArrayList<Achievement>) getBlockRepo().get(ACHIEVEMENT_LIST);
         if (a == null)
             fail("No achievement in the list!");
 
         for (Achievement achi : a) {
             if (achiName.equals(achi.getName())) {
                 Log.d(TAG, "Found the achievement " + achiName);
                 Log.i(TAG, "Verifying the lock status of achievement " + achiName);
                 assertEquals("lock status of achievement " + achiName, transLockStatus(statusMark),
                         achi.isUnlocked());
                 notifyStepPass();
                 return;
             }
         }
         fail("Could not find the achievement named " + achiName);
     }
 
     @Then("my score should be (\\w+) by (\\d+)")
     public void verifyScoreUpdated(String operator, int changeValue) {
         // TODO So far, sdk is not support to get the total score of user, will
         // add this verification after sdk to support it
         notifyStepPass();
     }
 
 }
