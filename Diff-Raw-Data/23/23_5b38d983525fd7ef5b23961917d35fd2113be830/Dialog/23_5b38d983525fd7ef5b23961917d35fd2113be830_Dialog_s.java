 /** 
  * Copyright [2013] Antonio J. Iniesta
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  * 
  * File created: 11/03/2013 at 23:14:04 by antonio
  */
 package com.iniesta.layerfx;
 
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.geometry.Pos;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.PasswordField;
 import javafx.scene.control.TextField;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.VBox;
 import javafx.stage.Stage;
 
 /**
  * @author antonio
  *
  */
 public class Dialog extends Stage {
 
 	public enum Option { 
 		YES("Yes"), NO("No"), OK("Ok"), CANCEL("Cancel");
 		private String name;
 		Option(String name){
 			this.name = name;
 		}
 		public String getName(){
 			return this.name;
 		}
 	}
 	
 	public enum Type{
 		INFO("/com/iniesta/layerfx/images/info.png"), 
 		WARNING("/com/iniesta/layerfx/images/warning.png"), 
 		ERROR("/com/iniesta/layerfx/images/error.png"), 
 		QUESTION("/com/iniesta/layerfx/images/question.png");
 		private String path;
 		Type(String path){
 			this.path = path;
 		}
 		public Image getImage(){
 			return new Image(path);
 		}
 	}
 
 	public enum Input{
 		TEXTFIELD, PASSWORDFIELD
 	}
 	
 	private Option optionPressed;
 	private String inputPrompt;
 	
 	/**
 	 * Constructor for alert
 	 * @param type
 	 * @param msg
 	 */
 	private Dialog(Type type, String msg){
 		VBox vbox = new VBox(10);
 		vbox.setAlignment(Pos.CENTER);
 		
 		Label empty = new Label(), empty2 = new Label();
 		
 		HBox hbox = new HBox(10);
 		hbox.setAlignment(Pos.CENTER);
 		ImageView iview = new ImageView(type.getImage());
 		iview.setFitWidth(32.);
 		iview.setFitHeight(32.);
 		Label label = new Label(msg);
 		hbox.getChildren().addAll(iview, label);
 		
 		HBox buttons = new HBox(10);
 		buttons.setAlignment(Pos.CENTER);
 		Button button = new Button("OK");
 		button.setOnAction(new EventHandler<ActionEvent>() {			
 			public void handle(ActionEvent arg0) {
 				close();
 			}
 		});
 		buttons.getChildren().addAll(button);
 		
 		vbox.getChildren().addAll(empty, hbox, buttons, empty2);
 		
 		AnchorPane pane = new AnchorPane();
 		AnchorPane.setRightAnchor(vbox, 0.0);
 		AnchorPane.setLeftAnchor(vbox, 0.0);
 		AnchorPane.setTopAnchor(vbox, 0.0);
 		AnchorPane.setBottomAnchor(vbox, 0.0);
 		pane.getChildren().add(vbox);
 		Scene scene = new Scene(pane);
 		setScene(scene);
 	}
 	
 	/**
 	 * Constructor for question parameters
 	 * @param question
 	 * @param msg
 	 * @param options
 	 */
 	public Dialog(Type question, String msg, Option ... options) {
 		VBox vbox = new VBox(10);
 		vbox.setAlignment(Pos.CENTER);
 		
 		Label empty = new Label(), empty2 = new Label();
 		
 		HBox hbox = new HBox(10);
 		hbox.setAlignment(Pos.CENTER);
 		ImageView iview = new ImageView(Type.QUESTION.getImage());
 		iview.setFitWidth(32.);
 		iview.setFitHeight(32.);
 		Label label = new Label(msg);
 		hbox.getChildren().addAll(iview, label);
 		
 		HBox buttons = new HBox(10);
 		buttons.setAlignment(Pos.CENTER);
 		if(options!=null){
 			for (final Option option : options) {
 				if(option!=null){
 					Button button = new Button(option.getName());
 					button.setOnAction(new EventHandler<ActionEvent>() {			
 						public void handle(ActionEvent arg0) {
 							optionPressed = option;
 							close();
 						}
 					});
 					buttons.getChildren().add(button);
 				}
 			}
 		}
 		
 		vbox.getChildren().addAll(empty, hbox, buttons, empty2);
 		
 		AnchorPane pane = new AnchorPane();
 		AnchorPane.setRightAnchor(vbox, 0.0);
 		AnchorPane.setLeftAnchor(vbox, 0.0);
 		AnchorPane.setTopAnchor(vbox, 0.0);
 		AnchorPane.setBottomAnchor(vbox, 0.0);
 		pane.getChildren().add(vbox);
 		Scene scene = new Scene(pane);
 		setScene(scene);
 	}
 
 	/**
 	 * Constructor for input prompt
 	 * @param question
 	 * @param input
 	 * @param prompt
 	 * @param cancel
 	 * @param ok
 	 */
 	public Dialog(Type question, final TextField input, String prompt, Option cancel, Option ok) {
 		VBox vbox = new VBox(10);
 		vbox.setAlignment(Pos.CENTER);
 		
 		Label empty = new Label(), empty2 = new Label();
 		
 		HBox hbox = new HBox(10);
 		hbox.setAlignment(Pos.CENTER);
 		ImageView iview = new ImageView(Type.QUESTION.getImage());
 		iview.setFitWidth(32.);
 		iview.setFitHeight(32.);
 		Label label = new Label(prompt);
 		hbox.getChildren().addAll(iview, label);
 		
 		HBox inputHbox = new HBox(10);
 		inputHbox.setAlignment(Pos.CENTER);
 		
 		
 		inputHbox.getChildren().addAll(input);
 		
 		HBox buttons = new HBox(10);
 		buttons.setAlignment(Pos.CENTER);
 		Button button = new Button(cancel.getName());
 		button.setOnAction(new EventHandler<ActionEvent>() {			
 			public void handle(ActionEvent arg0) {
 				inputPrompt = null;
 				close();
 			}
 		});
 		buttons.getChildren().add(button);
 		
 		Button button2 = new Button(ok.getName());
 		button2.setOnAction(new EventHandler<ActionEvent>() {			
 			public void handle(ActionEvent arg0) {
 				inputPrompt = input.getText();
 				close();
 			}
 		});
 		buttons.getChildren().add(button2);		
 		
 		vbox.getChildren().addAll(empty, hbox, inputHbox, buttons, empty2);
 		
 		AnchorPane pane = new AnchorPane();
 		AnchorPane.setRightAnchor(vbox, 0.0);
 		AnchorPane.setLeftAnchor(vbox, 0.0);
 		AnchorPane.setTopAnchor(vbox, 0.0);
 		AnchorPane.setBottomAnchor(vbox, 0.0);
 		pane.getChildren().add(vbox);
 		Scene scene = new Scene(pane);
 		setScene(scene);
 
 		
 	}
 
 	/**
 	 * Show the alert window with several icons represented by the type
 	 * @param type
 	 * @param msg
 	 */
 	public static void showAlert(Type type, String msg){
 		Dialog dialog = new Dialog(type, msg);
 		dialog.show();
 	}
 	
 	/**
 	 * Show a confirmation message
 	 * @param msg
 	 * @return
 	 */
 	public static boolean showConfirm(String msg){
 		boolean confirm = false;
 		Dialog dialog = new Dialog(Type.QUESTION, msg, Option.NO, Option.YES);
 		dialog.showAndWait();
 		Option pressed = dialog.getOptionPressed();
 		confirm = (pressed!=null && pressed.equals(Option.YES));
 		return confirm;
 	}
 
 	/**
 	 * Show Input dialog
 	 * @param prompt
 	 * @return
 	 */
 	public static String showInputDialog(Input input, String prompt){
 		String str = null;
 		TextField inputNode = null;
		if(Input.TEXTFIELD.equals(inputNode)){
 			inputNode = new TextField();
 		}else{
 			inputNode = new PasswordField();
 		}
 		Dialog dialog = new Dialog(Type.QUESTION, inputNode, prompt, Option.CANCEL, Option.OK);
 		dialog.showAndWait();
 		str = dialog.getInputPrompt();
 		return str;
 	}
 	
 	private String getInputPrompt() {
 		return inputPrompt;
 	}
 
 	private Option getOptionPressed() {
 		return optionPressed;
 	}
 }
