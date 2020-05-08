 
 package com.openfeint.qa.ggp.step_definitions;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.fail;
 import static junit.framework.Assert.assertTrue;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.TimeZone;
 
 import net.gree.asdk.api.FriendCode;
 import net.gree.asdk.api.GreePlatform;
 import net.gree.asdk.api.GreeUser;
 import net.gree.asdk.api.FriendCode.Code;
 import net.gree.asdk.api.FriendCode.CodeListener;
 import net.gree.asdk.api.FriendCode.Data;
 import net.gree.asdk.api.FriendCode.EntryListGetListener;
 import net.gree.asdk.api.FriendCode.OwnerGetListener;
 import net.gree.asdk.api.FriendCode.SuccessListener;
 
 import org.apache.http.HeaderIterator;
 import org.objenesis.instantiator.basic.NewInstanceInstantiator;
 
 import util.Consts;
 import android.util.Log;
 
 import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
 import com.openfeint.qa.core.command.After;
 import com.openfeint.qa.core.command.And;
 import com.openfeint.qa.core.command.Then;
 import com.openfeint.qa.core.command.When;
 
 public class FriendCodeStepDefinitions extends BasicStepDefinition {
     private static final String TAG = "FriendCode_Steps";
 
     private static final String FRIEND_CODE = "friendCode";
 
     private static final String OWNER_ID = "ownerId";
 
     private static final String EMPTY_CODE = "";
 
     private static final String FRIEND_LIST = "friendList";
 
     private static final String ERROR_CODE = "errorCode";
 
     @And("I make sure my friend code is NOTEXIST")
     @After("I make sure my friend code is NOTEXIST")
     public void cleanCodeAsCondition() {
         deleteCode();
     }
 
     @And("I make sure my friend code is EXIST")
     public void addCodeAsCondition() {
         requestNoExpireTimeCode();
     }
 
     @When("I delete my friend code")
     public void deleteCode() {
         notifyStepWait();
         FriendCode.deleteCode(new SuccessListener() {
             @Override
             public void onSuccess() {
                 Log.d(TAG, "Delete friend code success!");
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Delete friend code failed, " + response);
                 notifyStepPass();
             }
         });
     }
 
     @When("I request friend code with no expire time")
     public void requestNoExpireTimeCode() {
         requestFriendCode("");
     }
 
     @When("I request friend code with expire time (.+)")
     @And("I request friend code with expire time (.+)")
     public void requestFriendCode(String expireTime) {
         CodeListener listener = new CodeListener() {
             @Override
             public void onSuccess(Code code) {
                 Log.d(TAG, "Add new friend code success: " + code.getCode());
                 Log.d(TAG, "Expire time is: " + code.getExpireTime());
                 HashMap<String, Code> map = new HashMap<String, Code>();
                 map.put(GreePlatform.getLocalUser().getNickname(), code);
                 getBlockRepo().put(FRIEND_CODE, map);
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Add friend code failed, " + response);
                 notifyStepPass();
             }
         };
 
         getBlockRepo().put(FRIEND_CODE, EMPTY_CODE);
         notifyStepWait();
         FriendCode.requestCode(expireTime, listener);
     }
 
     @Then("my friend code length should be (\\d+)")
     public void verifyFriendCodeGot(int length) {
         if (EMPTY_CODE.equals(getBlockRepo().get(FRIEND_CODE)))
             fail("Friend code have not return yet!");
         assertEquals(
                 "friend code length",
                 length,
                 ((HashMap<String, Code>) getBlockRepo().get(FRIEND_CODE))
                         .get(GreePlatform.getLocalUser().getNickname()).getCode().length());
     }
 
     private String getExpireDate(int days) {
         // Calculate the expire date
         SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
         formater.setTimeZone(TimeZone.getTimeZone("GMT"));
         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.DAY_OF_MONTH, days);
         return formater.format(cal.getTime());
     }
 
     @And("my friend code expire time should be (.+)")
     public void verifyExpireTime(String expectTime) {
         String realExpireTime = ((HashMap<String, Code>) getBlockRepo().get(FRIEND_CODE)).get(
                 GreePlatform.getLocalUser().getNickname()).getExpireTime();
         if (expectTime.endsWith("days")) {
             int days = Integer.parseInt(expectTime.split(" ")[0]);
             String expectExpireDate = getExpireDate(days);
             Log.d(TAG, "Checking the expire date...");
             assertEquals("Expire date", expectExpireDate, realExpireTime.substring(0, 10));
         } else {
             assertEquals("Expire date", expectTime, realExpireTime);
         }
     }
 
     @And("I load my friend code")
     @When("I load my friend code")
     public void loadFriendCode() {
         CodeListener listener = new CodeListener() {
             @Override
             public void onSuccess(Code code) {
                 Log.d(TAG, "Load friend code success: " + code.getCode());
                 Log.d(TAG, "Expire time is: " + code.getExpireTime());
                 HashMap<String, Code> map = new HashMap<String, Code>();
                 map.put(GreePlatform.getLocalUser().getNickname(), code);
                 getBlockRepo().put(FRIEND_CODE, map);
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Load friend code failed, " + response);
                 notifyStepPass();
             }
         };
         getBlockRepo().put(FRIEND_CODE, EMPTY_CODE);
         notifyStepWait();
         FriendCode.loadCode(listener);
     }
 
     @Then("my friend code should be deleted")
     public void verifyCodeDeleted() {
         Log.d(TAG, "Verify the friend code is deleted...");
         assertEquals("code is deleted", EMPTY_CODE, getBlockRepo().get(FRIEND_CODE));
     }
 
     @When("I verify friend code of user (.+)")
     @And("I verify friend code of user (.+)")
     public void verifyFriendCode(String user) {
         notifyStepWait();
         String code = ((HashMap<String, Code>) getBlockRepo().get(FRIEND_CODE)).get(user).getCode();
         FriendCode.verifyCode(code, new SuccessListener() {
             @Override
             public void onSuccess() {
                getBlockRepo().put(ERROR_CODE, null);
                 Log.d(TAG, "Verify friend code success!");
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Verify friend code failed, " + response);
                 getBlockRepo().put(ERROR_CODE, responseCode);
                 notifyStepPass();
             }
         });
     }
 
     @When("I load the owner of friend code I verified")
     public void loadCodeOwner() {
         getBlockRepo().put(OWNER_ID, "");
         notifyStepWait();
         FriendCode.loadOwner(new OwnerGetListener() {
             @Override
             public void onSuccess(Data owner) {
                 Log.d(TAG, "Get friend code owner: " + owner.getUserId());
                 getBlockRepo().put(OWNER_ID, owner.getUserId());
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Get friend code owner failed!");
                 notifyStepPass();
             }
         });
     }
 
     @Then("the owner should be user (\\w+)")
     public void verifyCodeOwner(String ownerId) {
         Log.d(TAG, "Verify the code owner...");
         assertEquals("code owner", ownerId, getBlockRepo().get(OWNER_ID));
     }
 
     @When("I load friends who verifies my code")
     public void loadFriends() {
         notifyStepWait();
         FriendCode.loadFriends(Consts.STARTINDEX_0, Consts.PAGESIZE_ALL,
                 new EntryListGetListener() {
                     @Override
                     public void onSuccess(int startIndex, int itemsPerPage, int totalResults,
                             Data[] entries) {
                         Log.d(TAG, "Get friend list success!");
                         if (entries != null) {
                             ArrayList<String> l = new ArrayList<String>();
                             for (Data data : entries) {
                                 l.add(data.getUserId());
                             }
                             getBlockRepo().put(FRIEND_LIST, l);
                         }
                         notifyStepPass();
                     }
 
                     @Override
                     public void onFailure(int responseCode, HeaderIterator headers, String response) {
                         Log.e(TAG, "Get friend list failed!");
                         getBlockRepo().put(FRIEND_LIST, new ArrayList<String>());
                         notifyStepPass();
                     }
                 });
     }
 
     @Then("friend code verified list should be size of (\\d+)")
     public void verifyFriendNumber(int num) {
         Log.d(TAG, "Checking friend number...");
         ArrayList<String> l = (ArrayList<String>) getBlockRepo().get(FRIEND_LIST);
         if (l == null) {
             fail("Don't get the friend list!");
         }
         assertEquals("friend count", num, l.size());
     }
 
     @And("friend code verified list should have (\\w+)")
     public void verifyFriendExists(String friend_id) {
         ArrayList<String> l = (ArrayList<String>) getBlockRepo().get(FRIEND_LIST);
         if (l == null) {
             fail("Don't get the friend list!");
         }
         for (String id : l) {
             if (friend_id.equals(id)) {
                 assertTrue(true);
                 return;
             }
         }
         fail("Friend " + friend_id + " is not exists!");
     }
 
     @Then("my friend code should be VALID")
     public void checkVerifyResult() {
         String error_code = (String) getBlockRepo().get(ERROR_CODE);
         if (error_code != null) {
             fail("Friend code verified failed!");
         }
     }
 
 }
