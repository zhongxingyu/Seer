 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package cx.ath.mancel01.dependencyshot.test.junit;
 
 import cx.ath.mancel01.dependencyshot.DependencyShot;
 import cx.ath.mancel01.dependencyshot.api.DSInjector;
 import cx.ath.mancel01.dependencyshot.api.Stage;
 import cx.ath.mancel01.dependencyshot.graph.Binder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.InitializationError;
 
 /**
  *
  * @author Mathieu ANCELIN
  */
 public class DependencyShotRunner extends BlockJUnit4ClassRunner {
 
     private static final Logger logger = Logger.getLogger(DependencyShotRunner.class.getSimpleName());
 
     private Class<?> clazz;
 
     private DSInjector injector;
 
     public DependencyShotRunner(Class<?> clazz) throws InitializationError {     
         super(clazz);
         this.clazz = clazz;
         ConfigureWith config = clazz.getAnnotation(ConfigureWith.class);
         if (config != null) {
             Collection<Binder> binders = new ArrayList<Binder>();
             for (Class cl : config.value()) {
                 try {
                     binders.add((Binder) cl.newInstance());
                 } catch (Exception ex) {
                     logger.log(Level.SEVERE, null, ex);
                 }
             }
             Binder[] bins = new Binder[binders.size()];
            if (config.staging())
                 injector = DependencyShot.getInjector(Stage.TEST, binders.toArray(bins));
            else
                 injector = DependencyShot.getInjector(binders.toArray(bins));
         } else {
             injector = DependencyShot.getInjector();
         }
        if (config.allowCircularDependencies())
             injector.allowCircularDependencies(true);
         logger.info(new StringBuilder()
                 .append("Running test class \"")
                 .append(clazz.getSimpleName())
                 .append(".java\" with Dependency-Shot container ...")
                 .toString());
     }
 
     @Override
 	protected Object createTest() throws Exception {
         return injector.getInstance(getTestClass().getJavaClass());
 	}
 }
