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
  * File created: 12/03/2013 at 00:02:12 by antonio
  */
 package com.iniesta.layerfx;
 
 import com.iniesta.layerfx.Dialog.Input;
 import com.iniesta.layerfx.Dialog.Type;
 
 import javafx.application.Application;
 import javafx.stage.Stage;
 
 /**
  * @author antonio
  *
  */
 public class DialogTest extends Application{
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		launch(args);
 	}
 
 	@Override
 	public void start(Stage arg0) throws Exception {
		Dialog.showAlert(Type.INFO, "This is a test!");
 		
		boolean confirm = Dialog.showConfirm("Confirmation...");
		System.out.println("Confirm..."+confirm);
		
		String input = Dialog.showInputDialog(Input.PASSWORDFIELD, "Enter a password");
		System.out.println("Input: " + input);
 	}
 
 }
