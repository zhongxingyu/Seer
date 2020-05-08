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
 package org.jrebirth.demo.masteringtables.ui.game;
 
 import java.util.List;
 
 import javafx.geometry.Pos;
 import javafx.scene.input.KeyCode;
 import javafx.scene.layout.StackPane;
 
 import org.jrebirth.core.ui.object.DefaultObjectModel;
 import org.jrebirth.core.wave.Wave;
 import org.jrebirth.core.wave.WaveData;
 import org.jrebirth.demo.masteringtables.beans.Expression;
 import org.jrebirth.demo.masteringtables.beans.Game;
 import org.jrebirth.demo.masteringtables.beans.Page;
 import org.jrebirth.demo.masteringtables.command.CreateGameContent;
 import org.jrebirth.demo.masteringtables.ui.MTWaves;
 import org.jrebirth.demo.masteringtables.ui.expression.ExpressionModel;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class GameModel.
  */
 public class GameModel extends DefaultObjectModel<GameModel, GameView, Game> {
 
     /** The Constant LOGGER. */
     private static final Logger LOGGER = LoggerFactory.getLogger(GameModel.class);
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void initModel() {
 
         listen(MTWaves.START_GAME);
 
         listen(MTWaves.REGISTER_SUCCESS);
         listen(MTWaves.REGISTER_FAILURE);
 
     }
 
     @Override
     protected void bind() {
 
         // Bind counter values
         getView().getSuccessCounter().textProperty().bind(getObject().successCountProperty().asString());
         getView().getFailureCounter().textProperty().bind(getObject().failureCountProperty().asString());
 
     }
 
     /**
      * Start game.
      * 
      * @param expressionList the expression list
      * @param wave the wave
      */
     public void doStartGame(final List<Expression> expressionList, final Wave wave) {
 
         // Reset the current index and counters
         getObject().setIndex(0);
         getObject().setSuccessCount(0);
         getObject().setFailureCount(0);
 
         // Add new generated list of expression
         getObject().getGameList().clear();
         getObject().getGameList().addAll(expressionList);
 
         // Clear the current expression panel
         getView().getExpressionHolder().getChildren().clear();
         getView().getExpressionHolder().getChildren().add(getModel(ExpressionModel.class).getRootNode());
 
         // Center the panel
         StackPane.setAlignment(getModel(ExpressionModel.class).getRootNode(), Pos.CENTER);
 
         // Display the current expression with the help of the inner model
         sendWave(MTWaves.DISPLAY_EXPRESSION, WaveData.build(MTWaves.EXPRESSION, getObject().getCurrentExpression()));
     }
 
     /**
      * Register success.
      * 
      * @param expression the expression
      * @param wave the wave
      */
     public void doRegisterSuccess(final Expression expression, final Wave wave) {
 
         getObject().setSuccessCount(getObject().getSuccessCount() + 1);
 
         getObject().setIndex(getObject().getIndex() + 1);
 
         if (getObject().hasMoreExpression()) {
             // continue game
             sendWave(MTWaves.DISPLAY_EXPRESSION, WaveData.build(MTWaves.EXPRESSION, getObject().getCurrentExpression()));
         } else {
 
             // Game is finished
            sendWave(MTWaves.FINISH_GAME);
             sendWave(MTWaves.SHOW_PAGE, WaveData.build(MTWaves.PAGE, Page.Result));
         }
 
     }
 
     /**
      * Register failure.
      * 
      * @param expression the expression
      * @param wave the wave
      */
     public void doRegisterFailure(final Expression expression, final Wave wave) {
         getObject().setFailureCount(getObject().getFailureCount() + 1);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void showView() {
 
         callCommand(CreateGameContent.class);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void hideView() {
         getModel(ExpressionModel.class).reset();
     }
 
     /**
      * Do number.
      * 
      * @param code the code
      */
     public void performNumber(final KeyCode code) {
         getModel(ExpressionModel.class).appendNumber(code.getName());
     }
 
     /**
      * Do Enter.
      */
     public void performEnter() {
         // Nothing to do yet
 
     }
 
     /**
      * Do cancel.
      */
     public void performCancel() {
         // Cancel the game
         sendWave(MTWaves.FINISH_GAME);
 
         // Display the star menu
         sendWave(MTWaves.SHOW_PAGE, WaveData.build(MTWaves.PAGE, Page.GameMenu));
 
     }
 
     /**
      * Do delete.
      */
     public void performDelete() {
         getModel(ExpressionModel.class).deleteLastChar();
 
     }
 
 }
