 package br.com.cybereagle.androidlibrary.dagger;
 
 import android.content.Context;
 import dagger.Module;
 import dagger.Provides;
 
 import javax.inject.Singleton;
 
 public interface ActivityModule {
 
     /**
      * Allow the activity context to be injected but require that it be annotated with
      * {@link ForActivity @ForActivity} to explicitly differentiate it from application context.
      */
    @Provides
    @Singleton
     @ForActivity
     Context provideActivityContext();
 
 }
