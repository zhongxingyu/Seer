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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javafx.beans.binding.Bindings;
 import javafx.beans.binding.BooleanBinding;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.geometry.HPos;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.geometry.VPos;
 import javafx.scene.Node;
 import javafx.scene.control.Button;
 import javafx.scene.control.ButtonBuilder;
 import javafx.scene.control.ProgressBar;
 import javafx.scene.control.Toggle;
 import javafx.scene.control.ToggleButton;
 import javafx.scene.control.ToggleButtonBuilder;
 import javafx.scene.control.ToggleGroup;
 import javafx.scene.control.ToggleGroupBuilder;
 import javafx.scene.layout.BorderPane;
 import javafx.scene.layout.FlowPane;
 import javafx.scene.layout.GridPane;
 import javafx.scene.layout.GridPaneBuilder;
 import javafx.scene.layout.RowConstraints;
 
 import org.jrebirth.core.exception.CoreException;
 import org.jrebirth.core.ui.DefaultView;
 import org.jrebirth.core.ui.annotation.OnAction;
 import org.jrebirth.core.ui.annotation.RootNodeId;
 import org.jrebirth.demo.masteringtables.beans.Operator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class GameMenuView.
  */
 @RootNodeId("GameMenuPanel")
 public class GameMenuView extends DefaultView<GameMenuModel, BorderPane, GameMenuController> {
 
     /** The Constant LOGGER. */
     private static final Logger LOGGER = LoggerFactory.getLogger(GameMenuView.class);
 
     /** The addition. */
     private ToggleButton addition;
 
     /** The subtraction. */
     private ToggleButton subtraction;
 
     /** The multiplication. */
     private ToggleButton multiplication;
 
     /** The division. */
     private ToggleButton division;
 
     /** The start. */
     @OnAction(name = "Play")
     private Button playButton;
 
     private ToggleGroup lengthGroup;
 
     private ProgressBar loadingBar;
 
     /**
      * Instantiates a new start view.
      * 
      * @param model the model
      * @throws CoreException the core exception
      */
     public GameMenuView(final GameMenuModel model) throws CoreException {
         super(model);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void initView() {
 
         // final FlowPane fp = FlowPaneBuilder.create()
         // .children(new ImageView(MTImages.MT_TITLE.get()))
         // .build();
         // fp.setAlignment(Pos.CENTER);
         // getRootNode().setTop(fp);
 
         getRootNode().setCenter(buildGameConfigPanel());
 
         // getRootNode().setBottom(buildStartGamePanel());
 
         // Ui binding
         final BooleanBinding bb1 = Bindings.or(this.addition.selectedProperty(), this.subtraction.selectedProperty());
         final BooleanBinding bb2 = Bindings.or(this.multiplication.selectedProperty(), this.division.selectedProperty());
         final BooleanBinding bb = Bindings.or(bb1, bb2);
 
         this.playButton.disableProperty().bind(bb.not());
 
        this.loadingBar = new ProgressBar();
         this.loadingBar.visibleProperty().bind(this.loadingBar.progressProperty().lessThan(1));
         getRootNode().setBottom(this.loadingBar);
     }
 
     /**
      * Builds the start game panel.
      * 
      * @return the node
      */
     private Node buildStartGamePanel() {
         final FlowPane fp = new FlowPane();
 
         this.playButton = ButtonBuilder.create()
                 .styleClass("play")
                 .minHeight(130)
                 .minWidth(180)
                 // .text("Start Game")
                 .build();
 
         fp.getChildren().add(this.playButton);
         fp.setAlignment(Pos.TOP_CENTER);
 
         return fp;
     }
 
     /**
      * Builds the game config panel.
      * 
      * @return the node
      */
     private Node buildGameConfigPanel() {
 
         final GridPane pane = GridPaneBuilder.create()
                 .hgap(10)
                 // .vgap(10)
                 // .gridLinesVisible(true)
                 .build();
 
         pane.getRowConstraints().addAll(new RowConstraints(150), new RowConstraints(100), new RowConstraints(100));
 
         this.addition = buildChoiceButton(Operator.addition.toString());
         this.addition.getStyleClass().add("addition");
         // this.addition.setSkin(new ArcadeButtonSkin(this.addition));
 
         this.subtraction = buildChoiceButton(Operator.subtraction.toString());
         this.subtraction.getStyleClass().add("subtraction");
         // this.subtraction.setSkin(new ArcadeButtonSkin(this.subtraction));
 
         this.multiplication = buildChoiceButton(Operator.multiplication.toString());
         this.multiplication.getStyleClass().add("multiplication");
         // this.multiplication.setSkin(new ArcadeButtonSkin(this.multiplication));
 
         this.division = buildChoiceButton(Operator.division.toString());
         this.division.getStyleClass().add("division");
         // this.division.setSkin(new ArcadeButtonSkin(this.division));
 
         GridPane.setConstraints(this.addition, 0, 0, 1, 1, HPos.CENTER, VPos.CENTER);
         GridPane.setConstraints(this.subtraction, 1, 0, 1, 1, HPos.CENTER, VPos.CENTER);
         GridPane.setConstraints(this.multiplication, 2, 0, 1, 1, HPos.CENTER, VPos.CENTER);
         GridPane.setConstraints(this.division, 3, 0, 1, 1, HPos.CENTER, VPos.CENTER);
 
         pane.getChildren().addAll(this.addition, this.subtraction, this.multiplication, this.division);
         pane.setAlignment(Pos.CENTER);
 
         // Bound widget to properties Binding SHOULD BE grouped
         getModel().getGameSettings().containsAdditionProperty().bind(this.addition.selectedProperty());
         getModel().getGameSettings().containsSubtractionProperty().bind(this.subtraction.selectedProperty());
         getModel().getGameSettings().containsMultiplicationProperty().bind(this.multiplication.selectedProperty());
         getModel().getGameSettings().containsDivisionProperty().bind(this.division.selectedProperty());
 
         // Manage Question count
 
         final List<ToggleButton> toggleList = new ArrayList<>();
         final FlowPane fp = new FlowPane();
         fp.setAlignment(Pos.CENTER);
         fp.setHgap(10);
 
         final int[] list = { 5, 10, 20, 30, 50, 100 };
         // final int[] list = { 2, 5, 10, 20, 30, 50 };
 
         for (final int i : list) {
             toggleList.add(buildMiniButton(i));
         }
         fp.getChildren().addAll(toggleList);
 
         this.lengthGroup = ToggleGroupBuilder.create()
                 .toggles(toggleList)
                 .build();
 
         GridPane.setConstraints(fp, 0, 1, 4, 1, HPos.CENTER, VPos.CENTER);
         pane.getChildren().add(fp);
 
         this.lengthGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
 
             @Override
             public void changed(final ObservableValue<? extends Toggle> toggleProperty, final Toggle previous, final Toggle next) {
                 if (next != null) {
                     getModel().getGameSettings().setQuestionNumber((int) next.getUserData());
                 }
             }
         });
 
         // Select the first item
         this.lengthGroup.selectToggle(toggleList.get(0));
 
         // Third row START
 
         final Node start = buildStartGamePanel();
         GridPane.setConstraints(start, 0, 2, 4, 1, HPos.CENTER, VPos.CENTER);
         pane.getChildren().add(start);
 
         BorderPane.setMargin(pane, new Insets(140, 0, 0, 0));
 
         return pane;
     }
 
     /**
      * Builds the choice button for tables.
      * 
      * @param name the toggle button name
      * 
      * @return the toggle button
      */
     private ToggleButton buildChoiceButton(final String name) {
         return ToggleButtonBuilder.create()
                 .styleClass("ChoiceButton")// , "toggle-button")
                 .alignment(Pos.BASELINE_CENTER)
                 .minWidth(150)
                 .minHeight(150)
                 .maxWidth(150)
                 .maxHeight(150)
                 // .text(name)
                 .build();
 
     }
 
     /**
      * Builds the choice button for game length.
      * 
      * @param name the name
      * 
      * @return the toggle button
      */
     private ToggleButton buildMiniButton(final int value) {
         return ToggleButtonBuilder.create()
                 .styleClass("MiniChoiceButton", "expr" + value)// , "toggle-button")
                 .alignment(Pos.BASELINE_CENTER)
                 .minWidth(70)
                 .minHeight(70)
                 .maxWidth(70)
                 .maxHeight(70)
                 .userData(value)
                 // .text(Integer.toString(value))
                 .build();
 
     }
 
     /**
      * @return Returns the loadingBar.
      */
     public ProgressBar getLoadingBar() {
         return this.loadingBar;
     }
 
 }
