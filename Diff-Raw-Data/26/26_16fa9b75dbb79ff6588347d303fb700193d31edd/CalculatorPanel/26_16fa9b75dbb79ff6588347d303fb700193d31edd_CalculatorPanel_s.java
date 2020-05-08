 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.examples.aut.adder.javafx.gui;
 
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.TextField;
 import javafx.scene.layout.GridPane;
 
 /**
  * This class represents the Panel that handles the input.
  * 
  */
 public class CalculatorPanel extends GridPane {
 
     /**
      * Label pertaining the first TextField
      */
     private Label m_value1;
 
     /**
      * Label pertaining second TextField
      */
     private Label m_value2;
 
     /**
      * Label pertaining the result TextField
      */
     private Label m_result;
 
     /**
      * Label for the operation symbol
      */
     private Label m_opSymbol;
 
     /**
      * Text field for value1
      */
     private TextField m_fieldValue1;
 
     /**
      * Text field for value2
      */
     private TextField m_fieldValue2;
 
     /**
      * Text field for the result
      */
     private TextField m_fieldResult;
 
     /**
      * Button which triggers the evaluation
      */
     private Button m_buttonResult;
 
     /**
      * Constructor that Initializes the CalculatorPanel
      */
     public CalculatorPanel() {
         m_value1 = new Label("value1");
         m_value2 = new Label("value2");
         m_opSymbol = new Label("+");
         m_result = new Label("result");
 
         m_fieldValue1 = new TextField();
         m_fieldValue2 = new TextField();
         m_fieldResult = new TextField();
 
         m_buttonResult = new Button("=");
 
         initialize();
     }
 
     /**
      * Manages the Layout of the CalculatorPanel
      */
     private void initialize() {
 
         this.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
         this.setHgap(10);
         this.setVgap(10);
 
         this.add(m_fieldValue1, 1, 0);
         this.add(m_value1, 2, 0);
 
         this.add(m_opSymbol, 0, 1);
         this.add(m_fieldValue2, 1, 1);
         this.add(m_value2, 2, 1);
 
         this.add(m_buttonResult, 0, 2);
         m_fieldResult.setDisable(true);
         this.add(m_fieldResult, 1, 2);
         this.add(m_result, 2, 2);
 
     }
 
     /**
      * returns the input of the first TextField
      * 
      * @return The input of the first TextField
      */
     public String getAddend1() {
         return m_fieldValue1.getText();
     }
 
     /**
      * returns the input of the second TextField
      * 
      * @return The input of the second TextField
      */
     public String getAddend2() {
         return m_fieldValue2.getText();
     }
 
     /**
      * returns the input of the result TextField
      * 
      * @return The input of the result TextField
      */
     public String getResult() {
         return m_fieldResult.getText();
     }
 
     /**
      * Set sum
      * 
      * @param sum
      *            Sum
      */
     public void setResult(String sum) {
         m_fieldResult.setText(sum.toString());
     }
 
     /**
      * Result button
      * 
      * @return Result button
      */
     public Button getResultButton() {
         return m_buttonResult;
     }
 
     /**
      * Deletes the Text in all TextFields
      */
     public void reset() {
         m_fieldResult.setText("");
         m_fieldValue1.setText("");
         m_fieldValue2.setText("");
     }
 }
