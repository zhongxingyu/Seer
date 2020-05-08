 
 package com.openfeint.qa.ggp.step_definitions;
 
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.fail;
 
 import java.lang.reflect.Field;
 
 import net.gree.asdk.api.ui.NotificationButton;
 import net.gree.asdk.api.ui.StatusBar;
 import net.gree.asdk.core.Injector;
 import net.gree.asdk.core.notifications.NotificationCounts;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 
 import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
 import com.openfeint.qa.core.command.After;
 import com.openfeint.qa.core.command.And;
 import com.openfeint.qa.core.command.Then;
 import com.openfeint.qa.core.command.When;
 import com.openfeint.qa.ggp.MainActivity;
 import com.openfeint.qa.ggp.R;
 
 public class UIPackageStepDefinitions extends BasicStepDefinition {
 
     private final static String TAG = "UIPackage_Step";
 
     private final static String CURRENT_BAR = "current_bar";
 
     @And("I active widget with position (.+) and expandable (\\w+)")
     public void activateStatusBar(String position, String isExpandable) {
         final StatusBar bar;
         if ("YES".equals(isExpandable)) {
             bar = getExpandableStatusBar();
         } else {
             bar = getNormalStatusBar();
         }
         getBlockRepo().put(CURRENT_BAR, bar);
         MainActivity.getInstance().runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
                 bar.setVisibility(View.VISIBLE);
             }
         });
     }
 
     @And("I active default widget")
     public void activityDefaultStatusBar() {
         final StatusBar bar = getNormalStatusBar();
         getBlockRepo().put(CURRENT_BAR, bar);
         MainActivity.getInstance().runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
                 bar.setVisibility(View.VISIBLE);
             }
         });
     }
 
     private StatusBar getExpandableStatusBar() {
         return (StatusBar) MainActivity.getInstance().findViewById(R.id.statusBarExpandable);
     }
 
     private StatusBar getNormalStatusBar() {
         return (StatusBar) MainActivity.getInstance().findViewById(R.id.statusBarNormal);
     }
 
    @When("I set SNS notification to (\\d+) and GAME notification to (\\d+)")
     public void setNotificationBarForTest(int snsCount, int appCount) {
         try {
             NotificationCounts mCounts = Injector.getInstance(NotificationCounts.class);
             // set notification count
            Field mSnsCount_field = NotificationCounts.class.getDeclaredField("mSnsCount");
            mSnsCount_field.setAccessible(true);
            mSnsCount_field.set(mCounts, snsCount);
             Field mAppCount_field = NotificationCounts.class.getDeclaredField("mAppCount");
             mAppCount_field.setAccessible(true);
             mAppCount_field.set(mCounts, appCount);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @And("I update the (\\w+) notification count badge")
     public void updateNotificationCountbadge(String type) {
         StatusBar bar = (StatusBar) getBlockRepo().get(CURRENT_BAR);
         if ("all".equals(type)) {
             updateAllNotificationCount(bar);
        } else if ("SNS".equals(type)) {
            NotificationButton button = (NotificationButton) bar
                    .findViewById(R.id.gree_user_notification_button);
            updateNotificationButtonCount(button);
         } else if ("GAME".equals(type)) {
             NotificationButton button = (NotificationButton) bar
                     .findViewById(R.id.gree_game_notification_button);
             updateNotificationButtonCount(button);
         } else {
             Log.e(TAG, "Unknown Notification Count Type: " + type);
         }
 
     }
 
     private void updateAllNotificationCount(final StatusBar bar) {
         MainActivity.getInstance().runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
                 bar.onUpdate();
             }
         });
     }
 
     private void updateNotificationButtonCount(final NotificationButton button) {
         MainActivity.getInstance().runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
                 button.onUpdate();
             }
         });
     }
 
     @Then("(\\w+) notification count badge should be (\\w+)")
     public void verifyAllNotificationCount(String type, String count) {
         // wait UI thread to update badge
         try {
             Thread.sleep(3000);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
         final TextView badgeCountView = getBadgeCountView(type);
         try {
             assertTrue("badge count", badgeCountView != null
                     && badgeCountView.getText().toString().equals(count));
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             // clear the test data to avoid influence next time run
             MainActivity.getInstance().runOnUiThread(new Runnable() {
 
                 @Override
                 public void run() {
                     badgeCountView.setText("");
                 }
             });
         }
     }
 
     private TextView getBadgeCountView(String type) {
         StatusBar bar = (StatusBar) getBlockRepo().get(CURRENT_BAR);
         TextView badgeCountView = null;
         if ("all".equals(type)) {
             badgeCountView = getAllBadgeCountView(bar);
        } else if ("SNS".equals(type)) {
            NotificationButton button = (NotificationButton) bar
                    .findViewById(R.id.gree_user_notification_button);
            badgeCountView = getNotificationButtonView(button);
         } else if ("GAME".equals(type)) {
             NotificationButton button = (NotificationButton) bar
                     .findViewById(R.id.gree_game_notification_button);
             badgeCountView = getNotificationButtonView(button);
         } else {
             fail("Unknown type of notification badge view!");
         }
         return badgeCountView;
     }
 
     private TextView getAllBadgeCountView(StatusBar bar) {
         // all count badge is only enabled on expandable status bar
         Field mBadgeCount_field;
         TextView badgeCountView = null;
         try {
             mBadgeCount_field = StatusBar.class.getDeclaredField("mBadgeCount");
             mBadgeCount_field.setAccessible(true);
             badgeCountView = (TextView) mBadgeCount_field.get(bar);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return badgeCountView;
     }
 
     private TextView getNotificationButtonView(NotificationButton button) {
         TextView textView = (TextView) button.findViewById(R.id.gree_notificationCount);
         return textView;
     }
 
     @After("I hide widget")
     public void hiddenStatusBar() {
         final StatusBar bar = (StatusBar) getBlockRepo().get(CURRENT_BAR);
         if (bar == null)
             return;
         MainActivity.getInstance().runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
                 bar.setVisibility(View.GONE);
             }
         });
     }
 }
