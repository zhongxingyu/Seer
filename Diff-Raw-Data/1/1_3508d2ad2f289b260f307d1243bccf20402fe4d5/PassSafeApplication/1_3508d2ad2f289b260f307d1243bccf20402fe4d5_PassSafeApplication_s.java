 package de.iweinzierl.passsafe.android;
 
 import android.app.Application;
 
 import de.iweinzierl.passsafe.android.logging.Logger;
 import de.iweinzierl.passsafe.android.secure.AesPasswordHandler;
 import de.iweinzierl.passsafe.android.secure.PasswordHandler;
 
 public class PassSafeApplication extends Application {
 
     private static final Logger LOGGER = new Logger("PassSafeApplication");
 
     private PasswordHandler passwordHandler;
 
     @Override
     public void onCreate() {
         super.onCreate();
         initializeSingletons();
     }
 
     private void initializeSingletons() {
         LOGGER.info("initializeSingletons()");
        LOGGER.debug(new Test("This is my personal test!").toString());
     }
 
     public void setPassword(String password) {
         passwordHandler = new AesPasswordHandler(password);
     }
 
     public PasswordHandler getPasswordHandler() {
         return passwordHandler;
     }
 }
