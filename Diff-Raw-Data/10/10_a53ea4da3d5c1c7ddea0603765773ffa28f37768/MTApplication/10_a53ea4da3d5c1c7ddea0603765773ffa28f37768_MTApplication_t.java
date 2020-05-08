 /**
  * Get more info at : www.jrebirth.org .
  * Copyright JRebirth.org © 2011-2013
  * Contact : sebastien.bordes@jrebirth.org
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.jrebirth.demo.masteringtables;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import javafx.scene.Scene;
 import javafx.scene.layout.StackPane;
 import javafx.stage.Stage;
 
 import org.jrebirth.core.application.DefaultApplication;
 import org.jrebirth.core.resource.font.FontItem;
 import org.jrebirth.core.ui.Model;
 import org.jrebirth.core.wave.Wave;
 import org.jrebirth.demo.masteringtables.resources.MTFonts;
 import org.jrebirth.demo.masteringtables.resources.MTStyles;
 import org.jrebirth.demo.masteringtables.ui.page.PageModel;
 
 /**
  * The MTApplication is the main class used to start the application in standalone mode.
  */
 public final class MTApplication extends DefaultApplication<StackPane> {
 
     /**
      * The main method triggered by Java Runtime.
      * 
      * @param args the arguments
      */
     public static void main(final String... args) {
        preloadAndLaunch(args);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void preInit() {
         super.preInit();
 
         Locale.setDefault(Locale.FRANCE);
         // JRebirthParameters.LOG_RESOLUTION.define(false);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Class<? extends Model> getFirstModelClass() {
         return PageModel.class;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void customizeStage(final Stage stage) {
         stage.setFullScreen(false);
         stage.setResizable(false);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void customizeScene(final Scene scene) {
         addCSS(scene, MTStyles.MAIN);
         addCSS(scene, MTStyles.MENU);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FontItem> getFontToPreload() {
         // Define fonts that will be available from CSS
         return Arrays.asList(new FontItem[] {
                 MTFonts.EXPRESSION,
                 MTFonts.COUNTER,
         });
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Wave> getPreBootWaveList() {
         return Collections.emptyList();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Wave> getPostBootWaveList() {
         return Collections.emptyList();
     }
 
 }
