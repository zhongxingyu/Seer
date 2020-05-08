 /*
  * Copyright 2013 Emanuele Tamponi
  *
  * This file is part of object-graph.
  *
  * object-graph is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * object-graph is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.objectgraph.gui;
 
 
 import com.google.common.base.Strings;
 import com.objectgraph.core.*;
 import com.objectgraph.core.Error;
 import com.objectgraph.core.triggers.Assignment;
 import com.objectgraph.gui.editors.ImplementationChooserPropertyEditor;
 import com.objectgraph.gui.editors.StringPropertyEditor;
 import com.objectgraph.pluginsystem.PluginConfiguration;
 import com.objectgraph.pluginsystem.PluginManager;
 import javafx.application.Application;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.VBox;
 import javafx.stage.Stage;
 import javafx.stage.WindowEvent;
 
 import java.lang.ref.WeakReference;
 
 public class Prova {
     private static ProvaNode node = new ProvaNode();
 
     public static class ProvaNode extends ObjectNode {
         @Property String s;
         @Property int i;
         @Property double d;
         @Property float f;
         @Property short sh;
         @Property boolean b;
         @Property Node node;
 
         public ProvaNode() {
             addConstraint(new Constraint<ProvaNode, Node>("node") {
                 @Override
                 public Error getError(Node element) {
                     if (Strings.nullToEmpty(getNode().s).equals("Automatic message")) {
                         if (element instanceof ProvaNode)
                             return null;
                         else
                             return new Error(Error.ErrorLevel.SEVERE, "should be an instance of ProvaNode");
                     } else
                         return null;
                 }
             });
             addErrorCheck(new ErrorCheck<Node, Object>("i") {
                 @Override
                 public Error getError(Object value) {
                     if (value == 12)
                         return null;
                     else
                         return new Error(Error.ErrorLevel.WARNING, "value should be 12");
                 }
             });
         }
     }
 
     public static class ProvaParent extends ObjectNode {
         @Property
         protected String t = "Ciao";
         @Property
         protected ProvaNode child;
 
         public ProvaParent() {
             addTrigger(new Assignment("t", "child.s"));
         }
     }
 
     public static class MyApp extends Application {
         private static Scene scene;
         private static VBox box;
 
         @Override
         public void start(Stage stage) throws Exception {
             AnchorPane pane = new AnchorPane();
             Button button = new Button("Open");
             button.setOnAction(new EventHandler<ActionEvent>() {
                 @Override
                 public void handle(ActionEvent actionEvent) {
                     openStage();
                 }
             });
             pane.getChildren().add(button);
             stage.setScene(new Scene(pane));
             stage.show();
         }
 
         private void openStage() {
             Stage stage = new Stage();
             PropertyEditor editor1 = new StringPropertyEditor().attach(node.getRootedProperty("s"));
             PropertyEditor[] editors = {
                     EditorManager.getBestEditor(node.getRootedProperty("i"), false, true),
                     EditorManager.getBestEditor(node.getRootedProperty("d"), false, true),
                     EditorManager.getBestEditor(node.getRootedProperty("f"), false, true),
                     EditorManager.getBestEditor(node.getRootedProperty("sh"), false, true),
                     EditorManager.getBestEditor(node.getRootedProperty("b"), false, true)
             };
             PropertyEditor editor3 = new ImplementationChooserPropertyEditor().attach(node.getRootedProperty("node"));
 
             box = new VBox();
             Button test = new Button("Automatic");
             test.setOnAction(new EventHandler<ActionEvent>() {
                 @Override
                 public void handle(ActionEvent event) {
                     node.set("s", "Automatic message");
                 }
             });
             box.getChildren().addAll(editor1, editor3, test);
             box.getChildren().addAll(editors);
 
             scene = new Scene(box);
             scene.getStylesheets().add("com/objectgraph/gui/objectgraphgui.css");
 
             stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                 @Override
                 public void handle(WindowEvent windowEvent) {
                     box.getChildren().clear();
                     box = null;
                 }
             });
 
             stage.setScene(scene);
             stage.showAndWait();
         }
     }
 
     public static void provaParent(ProvaNode node) {
         new ProvaParent().set("child", node);
     }
 
     public static void main(String[] args) throws Exception {
         PluginManager.initialise(new PluginConfiguration("com.objectgraph"));

         Application.launch(MyApp.class);
         WeakReference<Scene> scene = new WeakReference<Scene>(MyApp.scene);
         MyApp.scene = null;
         System.out.println(node.getErrors());
         System.out.println(node.b);
         int i = 1;
         ProvaNode n = new ProvaNode();
         provaParent(n);
         while (!n.getControlledProperties().isEmpty() || !node.getParentPaths().isEmpty()) {
             System.out.println((i++) + " " + node.getParentPaths());
             if (i > 1000)
                 System.gc();
             if (i > 1100)
                 EditorManager.detachAllEditors(node);
 
             System.out.println(scene.get());
         }
         System.out.println(node.getParentPaths());
 
         System.out.println("Finished!");
     }
 
 }
