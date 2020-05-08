 package com.kii.demo.cloudstorage.api;
 
 import android.app.Activity;
 
 import com.kii.cloud.storage.KiiClient;
 import com.kii.cloud.storage.KiiUser;
 import com.kii.cloud.storage.callback.KiiUserCallBack;
 import com.kii.demo.cloudstorage.activities.ConstantValues;
 import com.kii.demo.cloudstorage.activities.ShowInfo;
 import com.kii.demo.cloudstorage.activities.StartActivity;
 import com.kii.demo.cloudstorage.activities.UserOperationActivity;
 
 public class KiiUserOperation {
     static {
        KiiClient.initialize(AppInfo.APP_ID, AppInfo.APP_KEY,AppInfo.BASE_URL);
     }
 
     // ------------ Sync Methods ----------------
 
     public static void createUser(StartActivity activity, KiiUser user,
             String username, String pwd) {
         try {
             user.register(username, pwd);
             showUserResult(activity, user);
         } catch (Exception e) {
             ShowInfo.showException(activity, e);
         }
     }
 
     public static void userLogin(StartActivity activity, String username,
             String pwd) {
         try {
             KiiUser user = KiiUser.logIn(username, pwd);
             showUserResult(activity, user);
         } catch (Exception e) {
             ShowInfo.showException(activity, e);
         }
 
         activity.updateUserStateTitle();
     }
 
     public static String getResetPasswordUrl() {
         return KiiUser.getResetPasswordUrl();
     }
 
     public static void updateUserPhone(UserOperationActivity activity,
             String phone) {
         KiiUser user = KiiClient.getCurrentUser();
 
         try {
             user.set(ConstantValues.FIELD_USER_PHONE, phone);
             user.update();
             showUserResult(activity, user);
         } catch (Exception e) {
             ShowInfo.showException(activity, e);
         }
     }
     
     public static void updateUserBirthday(UserOperationActivity activity, long time) {
         KiiUser user = KiiClient.getCurrentUser();
 
         try {
             user.set(ConstantValues.FIELD_USER_BOD, time);
             user.update();
             showUserResult(activity, user);
         } catch (Exception e) {
             ShowInfo.showException(activity, e);
         }
 
     }
     
     public static void changePwd(UserOperationActivity activity, String old_pwd, String new_pwd) {
 
         try {
             KiiClient.getCurrentUser().changePassword(new_pwd, old_pwd);
             showUserResult(activity, KiiClient.getCurrentUser());
         } catch (Exception e) {
             ShowInfo.showException(activity, e);
         }
 
     }
 
     // --------------- Async Method -------------------------
     public static int asyncCreateUser(final StartActivity activity,
             KiiUser user, String username, String pwd) {
         int token = user.register(new KiiUserCallBack() {
 
             @Override
             public void onRegisterCompleted(int token, boolean success,
                     KiiUser user, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     showUserResult(activity, user);
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
             }
 
         }, username, pwd);
 
         return token;
 
     }
 
     public static int asyncUserLogin(final StartActivity activity,
             String username, String pwd) {
         int token = KiiUser.logIn(new KiiUserCallBack() {
 
             @Override
             public void onLoginCompleted(int token, boolean success,
                     KiiUser user, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     showUserResult(activity, user);
                     activity.updateUserStateTitle();
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
             }
 
         }, username, pwd);
 
         return token;
 
     }
 
     public static int asyncUpdateUserPhone(final UserOperationActivity activity,
             String phone) {
         KiiUser user = KiiClient.getCurrentUser();
 
         user.set(ConstantValues.FIELD_USER_PHONE, phone);
 
         int token = user.update(new KiiUserCallBack(){
             @Override
             public void onUpdateCompleted(int token, boolean success, KiiUser user,
                     Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     showUserResult(activity, user);
                     activity.updateUserStateTitle();
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
             }
         });
         
         return token;
     }
 
     public static int asyncUpdateUserBirthday(final UserOperationActivity activity, long time) {
         KiiUser user = KiiClient.getCurrentUser();
         
         user.set(ConstantValues.FIELD_USER_BOD, time);
         
         int token = user.update(new KiiUserCallBack(){
 
             @Override
             public void onUpdateCompleted(int token, boolean success,
                     KiiUser user, Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     showUserResult(activity,  user);
                     activity.updateUserStateTitle();
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
             }
             
         });
        
         return token;
     }
 
     public static int asyncchangePwd(final UserOperationActivity activity, String old_pwd, String new_pwd) {
         int token = KiiClient.getCurrentUser().changePassword(new KiiUserCallBack(){
 
             @Override
             public void onChangePasswordCompleted(int token, boolean success,
                     Exception exception) {
                 ShowInfo.closeProgressDialog();
                 if (success) {
                     showUserResult(activity, KiiClient.getCurrentUser());
                 } else {
                     ShowInfo.showException(activity, exception);
                 }
             }
             
         }, new_pwd, old_pwd);
 
         return token;
     }
 
     public static void showUserResult(Activity activity, KiiUser user) {
         StringBuilder show = new StringBuilder();
         show.append("Kii User UUID: " + user.toUri());
         show.append("\n");
 
         show.append("Details: " + user.toString());
         ShowInfo.showSuccess(activity, "user operation successful");
     }
 }
