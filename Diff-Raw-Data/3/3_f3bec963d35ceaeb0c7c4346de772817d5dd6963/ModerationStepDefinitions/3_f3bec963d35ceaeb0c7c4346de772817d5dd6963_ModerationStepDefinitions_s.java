 
 package com.openfeint.qa.ggp.step_definitions;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Vector;
 
 import net.gree.asdk.api.GreePlatform;
 import net.gree.asdk.api.GreeUser;
 import net.gree.asdk.api.GreeUser.GreeUserListener;
 import net.gree.asdk.api.ModeratedText;
 import net.gree.asdk.api.ModeratedText.ModeratedTextListener;
 import net.gree.asdk.api.ModeratedText.SuccessListener;
 import net.gree.asdk.core.GLog;
 
 import org.apache.http.HeaderIterator;
 
 import util.Consts;
 import android.util.Log;
 
 import com.handmark.pulltorefresh.library.internal.LoadingLayout;
 import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
 import com.openfeint.qa.core.command.After;
 import com.openfeint.qa.core.command.Given;
 import com.openfeint.qa.core.command.Then;
 import com.openfeint.qa.core.command.When;
 
 public class ModerationStepDefinitions extends BasicStepDefinition {
     private static final String TAG = "Moderation_Steps";
 
     private static String MODERATION_LIST = "moderationlist";
 
     @When("I send to moderation server with text (.+)")
     public void sendModeration(String text) {
         getBlockRepo().put(MODERATION_LIST, new ArrayList<ModeratedText>());
         notifyStepWait();
         ModeratedText.create(text, new ModeratedTextListener() {
             @Override
             public void onSuccess(ModeratedText[] textInfo) {
                 Log.d(TAG, "onSuccess");
                 ModeratedText.logTextInfo(textInfo);
                 ((ArrayList<ModeratedText>) getBlockRepo().get(MODERATION_LIST)).addAll(Arrays
                         .asList(textInfo));
                 notifyStepPass();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Get moderated text failed!");
                 notifyStepPass();
             }
         });
     }
 
     private int transStatus(String status) {
         if ("CHECKING".equals(status))
             return ModeratedText.STATUS_BEING_CHECKED;
         else if ("APPROVED".equals(status))
             return ModeratedText.STATUS_RESULT_APPROVED;
         else if ("DELETED".equals(status))
             return ModeratedText.STATUS_DELETED;
         else if ("REJECTED".equals(status))
             return ModeratedText.STATUS_RESULT_REJECTED;
         else if ("UNKNOWN".equals(status))
             return ModeratedText.STATUS_UNKNOWN;
         else
             fail("Unknow status of the moderation text!");
         return -100;
     }
 
     @Then("status of text (.+) should be (\\w+)")
     public void verifyModerationText(String text, String status) {
         ArrayList<ModeratedText> list = (ArrayList<ModeratedText>) getBlockRepo().get(
                 MODERATION_LIST);
         if (list == null || list.size() == 0)
             fail("No moderation text in the list!");
         for (ModeratedText item : list) {
             if (text.equals(item.getContent())) {
                 Log.d(TAG, "Get the text \"" + text + "\" and checking its status...");
                 assertEquals("Text status", ModeratedText.STATUS_BEING_CHECKED, transStatus(status));
             }
            return;
         }
         fail("Could not find the moderation text: " + text);
     }
 
     private boolean transTarget(String target) {
         if ("NATIVE_CACHE".equals(target)) {
             return false;
         } else if ("SERVER".equals(target)) {
             return true;
         } else {
             fail("Unknown target to load moderation text!");
             return false;
         }
     }
 
     @When("I load from (\\w+) with moderation text (.+)")
     public void loadModerationText(String target) {
         boolean isLoadFromServer = transTarget(target);
         getBlockRepo().put(MODERATION_LIST, new ArrayList<ModeratedText>());
         ModeratedText.loadFromLocalCache(isLoadFromServer, new ModeratedTextListener() {
 
             @Override
             public void onSuccess(ModeratedText[] textInfo) {
                 Log.d(TAG, "Get moderated text success!");
                 Log.d(TAG, "Get " + textInfo.length + " text!");
                 ((ArrayList<ModeratedText>) getBlockRepo().get(MODERATION_LIST)).addAll(Arrays
                         .asList(textInfo));
                 notifyAsyncInStep();
             }
 
             @Override
             public void onFailure(int responseCode, HeaderIterator headers, String response) {
                 Log.e(TAG, "Get moderated text failed! " + response);
                 notifyAsyncInStep();
             }
         });
         waitForAsyncInStep();
     }
 
     @After("I make sure moderation server NOTINCLUDES text (.+)")
     public void cleanUpText(String text) {
         loadModerationText("NATIVE_CACHE");
 
         ArrayList<ModeratedText> list = (ArrayList<ModeratedText>) getBlockRepo().get(
                 MODERATION_LIST);
         if (list == null || list.size() == 0) {
             Log.d(TAG, "No moderation text to be cleaned!");
             return;
         }
 
         // Clean up all texts that match the content
         for (ModeratedText item : list) {
             if (text.equals(item.getContent())) {
                 Log.d(TAG, "Get text \"" + text + "\", clean it up...");
                 item.delete(new SuccessListener() {
                     @Override
                     public void onSuccess() {
                         Log.d(TAG, "Clean up success!");
                         notifyAsyncInStep();
                     }
 
                     @Override
                     public void onFailure(int responseCode, HeaderIterator headers, String response) {
                         Log.e(TAG, "Clean up failed, " + response);
                         notifyAsyncInStep();
                     }
                 });
                 waitForAsyncInStep();
             }
         }
     }
 
     
 }
