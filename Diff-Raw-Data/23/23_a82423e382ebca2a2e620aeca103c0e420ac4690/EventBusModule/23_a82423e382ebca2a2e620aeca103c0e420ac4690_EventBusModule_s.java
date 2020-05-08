 package com.praeses.androidstack.di;
 
 import com.squareup.otto.Bus;
 
 import javax.inject.Singleton;
 
 import dagger.Module;
 import dagger.Provides;
 
@Module
 public class EventBusModule {
     @Provides @Singleton
     public Bus provideBus() {
         return new Bus();
     }
 }
