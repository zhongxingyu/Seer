 
 package com.openfeint.qa.ggp.step_definitions;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.fail;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import net.gree.asdk.api.FriendCode;
 import net.gree.asdk.api.FriendCode.Code;
 import net.gree.asdk.api.FriendCode.CodeListener;
 import net.gree.asdk.api.FriendCode.Data;
 import net.gree.asdk.api.FriendCode.OwnerGetListener;
 import net.gree.asdk.api.FriendCode.SuccessListener;
 
 import org.apache.http.HeaderIterator;
 
 import android.util.Log;
 
 import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
 import com.openfeint.qa.core.command.And;
 import com.openfeint.qa.core.command.Then;
 import com.openfeint.qa.core.command.When;
 
 public class FriendCodeStepDefinitions extends BasicStepDefinition {
     private static final String TAG = "FriendCode_Steps";
 
     private static final String FRIEND_CODE = "friendCode";
 
     private static final String OWNER_ID = "ownerId";
 
     private static final String EMPTY_CODE = "";
 
     @And("I make sure my friend code is NOTEXIST")
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
     public void requestFriendCode(String expireTime) {
         CodeListener listener = new CodeListener() {
             @Override
             public void onSuccess(Code code) {
                 Log.d(TAG, "Add new friend code success: " + code.getCode());
                 Log.d(TAG, "Expire time is: " + code.getExpireTime());
                 getBlockRepo().put(FRIEND_CODE, code);
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
        if ("".equals(getBlockRepo().get(FRIEND_CODE)))
             fail("Friend code have not return yet!");
         assertEquals("friend code length", length, ((Code) getBlockRepo().get(FRIEND_CODE))
                 .getCode().length());
     }
 
     private String getExpireDate(int days) {
         // Calculate the expire date
         SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.DAY_OF_MONTH, days);
         cal.add(Calendar.HOUR, -8); // reduce 8 hours to UTC time zone
         return formater.format(cal.getTime());
     }
 
     @And("my friend code expire time should be (.+)")
     public void verifyExpireTime(String expectTime) {
         String realExpireTime = ((Code) getBlockRepo().get(FRIEND_CODE)).getExpireTime();
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
     public void loadFriendCode() {
         CodeListener listener = new CodeListener() {
             @Override
             public void onSuccess(Code code) {
                 Log.d(TAG, "Load friend code success: " + code.getCode());
                 Log.d(TAG, "Expire time is: " + code.getExpireTime());
                 getBlockRepo().put(FRIEND_CODE, code);
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
 
     // TODO I just add this method to prepare the data for load owner, need to
     // change step title when really be used
     // @When("I verify the friend code (\\w+)")
     // public void verifyFriendCode(String code) {
     // notifyStepWait();
     // FriendCode.verifyCode(code, new SuccessListener() {
     // @Override
     // public void onSuccess() {
     // Log.d(TAG, "Verify friend code success!");
     // notifyStepPass();
     // }
     //
     // @Override
     // public void onFailure(int responseCode, HeaderIterator headers, String
     // response) {
     // Log.e(TAG, "Verify friend code failed, " + response);
     // notifyStepPass();
     // }
     // });
     // }
 
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
 }
