 package com.zen.droidparts.module;
 
 import android.content.Context;
 
 import com.zen.droidparts.BaseApplicationWithInjector;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 
 import javax.inject.Qualifier;
 import javax.inject.Singleton;
 
 import dagger.Module;
 import dagger.ObjectGraph;
 import dagger.Provides;
 
 import static java.lang.annotation.ElementType.FIELD;
 import static java.lang.annotation.ElementType.METHOD;
 import static java.lang.annotation.ElementType.PARAMETER;
 import static java.lang.annotation.RetentionPolicy.RUNTIME;
 
@Module(library = true)
 public class InjectingApplicationModule {
     @Provides
     @Singleton
     @Application
     Context provideContext(BaseApplicationWithInjector baseApplicationWithInjector) {
         return baseApplicationWithInjector.getApplicationContext();
     }
 
     @Provides
     @Singleton
     @Application
     ObjectGraph provideObjectGraph(BaseApplicationWithInjector baseApplicationWithInjector) {
         return baseApplicationWithInjector.getObjectGraph();
     }
 
     @Provides
     @Singleton
     @Application
     Injector provideInjector(BaseApplicationWithInjector baseApplicationWithInjector) {
         return baseApplicationWithInjector;
     }
 
     @Qualifier
     @Target({FIELD, PARAMETER, METHOD})
     @Documented
     @Retention(RUNTIME)
     public @interface Application {
 
     }
 }
