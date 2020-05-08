 
 package com.openfeint.qa.ggp.step_definitions;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.fail;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import net.gree.asdk.api.FriendCode;
 import net.gree.asdk.api.FriendCode.Code;
 import net.gree.asdk.api.FriendCode.CodeListener;
 import net.gree.asdk.api.FriendCode.SuccessListener;
 
 import org.apache.http.HeaderIterator;
 
 import android.R.integer;
 import android.util.Log;
 
 import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
 import com.openfeint.qa.core.command.And;
 import com.openfeint.qa.core.command.Given;
 import com.openfeint.qa.core.command.Then;
 import com.openfeint.qa.core.command.When;
 
 public class FriendCodeStepDefinitions extends BasicStepDefinition {
     private static final String TAG = "FriendCode_Steps";
 
     private static final String FRIEND_CODE = "friendCode";
 
     @Given("I make sure my friend code is (\\w+)")
     public void updateCodeAsCondition(String isExistMark) {
         if ("NOTEXIST".equals(isExistMark)) {
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
         } else if ("EXIST".equals(isExistMark)) {
             // TODO add code for EXIST
         } else
             fail("Unknown isExistMark!");
     }
 
     @When("I request friend code with expire time (.+)")
     public void requestFriendCode(String expireTime) {
         CodeListener listener = new CodeListener() {
             @Override
             public void onSuccess(Code code) {
                 Log.d(TAG, "Add new friend code success: " + code.getCode());
                 getBlockRepo().put(FRIEND_CODE, code);
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Add friend code failed, " + response);
                 notifyStepPass();
             }
         };
 
         getBlockRepo().put(FRIEND_CODE, "");
         notifyStepWait();
        if ("NONE".equals(expireTime)) {
            FriendCode.requestCode("", listener);
        } else {
            FriendCode.requestCode(expireTime, listener);
        }
     }
 
     @Then("I should get my friend code")
     public void verifyFriendCode() {
         assertEquals("friend code length", ((Code) getBlockRepo().get(FRIEND_CODE)).getCode()
                 .length(), 7);
     }
 
     @And("my friend code expire time should be (.+)")
     public void verifyExpireTime(String expectTime) {
         String expireTime = ((Code) getBlockRepo().get(FRIEND_CODE)).getExpireTime();
         if (expectTime.endsWith("days")) {
             int length = Integer.parseInt(expectTime.split(" ")[0]);
            
             // Calculate the expire date
             SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
             Calendar cal = Calendar.getInstance();
             cal.add(Calendar.DAY_OF_MONTH, length);
             cal.add(Calendar.HOUR, -8); // reduce 8 hours to UTC time zone
             String expireDate = formater.format(cal.getTime());
            
             Log.d(TAG, "Checking the expire date...");
             assertEquals("Expire date", expireDate, expireTime.substring(0, 10));
         } else {
             assertEquals("Expire date", expectTime, expireTime);
         }
     }
 }
