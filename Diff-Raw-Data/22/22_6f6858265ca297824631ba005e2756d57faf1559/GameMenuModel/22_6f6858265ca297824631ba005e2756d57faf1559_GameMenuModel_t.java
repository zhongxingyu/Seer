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
 package org.jrebirth.demo.masteringtables.ui.menu;
 
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 
 import org.jrebirth.core.ui.DefaultModel;
 import org.jrebirth.core.wave.JRebirthWaves;
 import org.jrebirth.core.wave.Wave;
import org.jrebirth.core.wave.Wave.Status;
 import org.jrebirth.core.wave.WaveData;
 import org.jrebirth.demo.masteringtables.beans.GameSettings;
 import org.jrebirth.demo.masteringtables.service.ExpressionBuilderService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class GameMenuModel.
  */
 public class GameMenuModel extends DefaultModel<GameMenuModel, GameMenuView> {
 
     /** The Constant LOGGER. */
     private static final Logger LOGGER = LoggerFactory.getLogger(GameMenuModel.class);
 
     /** The service used to build quiz data. */
     private ExpressionBuilderService expressionBuilderService;
 
     /** The game settings bean. */
     private final GameSettings gameSettings = new GameSettings();
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void initModel() {
 
         // Be notified when tables are ready
         listen(ExpressionBuilderService.RE_TABLES_BUILT);
 
         // Store an hard link to avoid garbage collection of the service
         this.expressionBuilderService = getService(ExpressionBuilderService.class);
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void showView() {
         super.showView();
 
        // getView().getLoadingBar().setVisible(true);
         // Generate all tables
         final Wave wave = returnData(ExpressionBuilderService.class, ExpressionBuilderService.DO_BUILD_TABLES,
                 WaveData.build(JRebirthWaves.PROGRESS_BAR, getView().getLoadingBar()));
 
         wave.statusProperty().addListener(new ChangeListener<Wave.Status>() {
 
             @Override
             public void changed(final ObservableValue<? extends Wave.Status> arg0, final Wave.Status arg1, final Wave.Status arg2) {
                if (arg1 == Status.Consumed) {
                    getView().getLoadingBar().setVisible(false);
                }
 
             }
         });
     }
 
     /**
      * Tables built.
      * 
      * @param wave the wave
      */
     public void doTablesBuilt(final Wave wave) {
 
         final int nbExpression = this.expressionBuilderService.getAdditionTable().size()
                 + this.expressionBuilderService.getDivisionTable().size()
                 + this.expressionBuilderService.getSubtractionTable().size()
                 + this.expressionBuilderService.getMultiplicationTable().size();
 
         LOGGER.info("Tables are available and contains {} expressions.", nbExpression);
     }
 
     /**
      * Gets the game settings.
      * 
      * @return the game settings
      */
     public GameSettings getGameSettings() {
         return this.gameSettings;
     }
 }
