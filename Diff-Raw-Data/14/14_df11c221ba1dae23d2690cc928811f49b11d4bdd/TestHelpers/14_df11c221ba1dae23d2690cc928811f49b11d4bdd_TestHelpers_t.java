 package com.example.androidApp.support;
 
 import android.app.Application;
 import com.google.inject.Module;
 import org.robolectric.Robolectric;
 import roboguice.RoboGuice;
 import roboguice.inject.RoboInjector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pivotal
  * Date: 3/22/13
  * Time: 10:25 AM
  * To change this template use File | Settings | File Templates.
  */
 public class TestHelpers {
     public static RoboInjector updateApplicationInjectorWithModule(Module module) {
         Application application = Robolectric.application;
 
        RoboGuice.setBaseApplicationInjector(
                application,
                RoboGuice.DEFAULT_STAGE,
                RoboGuice.newDefaultRoboModule(application),
                module
        );
 
         return RoboGuice.getInjector(application);
     }
 }
