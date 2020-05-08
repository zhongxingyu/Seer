 /*
  *  Copyright 2012 Matti Vesa
  *
  *  Licensed under the Apache License, Version 2.0 (the "License"); you
  *  may not use this file except in compliance with the License. You may
  *  obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  *  implied. See the License for the specific language governing
  *  permissions and limitations under the License.
  */
 package com.github.mjvesa.f4v;
 
 import org.vaadin.aceeditor.AceEditor;
 import org.vaadin.aceeditor.AceMode;
 import org.vaadin.aceeditor.AceTheme;
 
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.ComponentContainer;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.HorizontalSplitPanel;
 import com.vaadin.ui.ListSelect;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.VerticalSplitPanel;
 
 /**
  * 
  * @author mjvesa@vaadin.com
  * 
  */
 public class ForthIDE extends HorizontalSplitPanel implements View  {
 
     /**
      * 
      */
     private static final long serialVersionUID = -6911850470039819669L;
 
     private TabSheet editorAndLayout;
 
     private TextArea console;
     private StringBuffer consoleString;
 
     private Panel panel;
 
     private TabSheet buffersAndStatus;
     private ListSelect bufferSelect;
     private TextField bufferName;
     private Button saveBlockButton;
     private Button clearConsoleButton;
 
     private Button printStackButton;
     private ListSelect wordListSelect;
 
     private AceEditor editor;
 
     private Interpreter interpreter;
 
     public ForthIDE() {
         setSizeFull();
         constructBuffersAndStatus();
         constructEditorAndLayout();
         interpreter = new Interpreter();
         interpreter.setView(this);
         interpreter.setup();
         fillBufferSelect();
     }
 
     /**
      * Fills the select for buffers with their filenames
      */
     private void fillBufferSelect() {
         for (String s : interpreter.getBufferList()) {
             bufferSelect.addItem(s);
 
         }
     }
 
     private void constructBuffersAndStatus() {
         buffersAndStatus = new TabSheet();
         buffersAndStatus.setSizeFull();
 
         VerticalLayout vl = new VerticalLayout();
         vl.setSpacing(true);
         vl.setSizeFull();
         HorizontalLayout hl = new HorizontalLayout();
         hl.setSpacing(true);
 
         bufferName = new TextField();
         saveBlockButton = new Button("Save", new Button.ClickListener() {
             /**
 		     * 
 		     */
             private static final long serialVersionUID = -1273939649837923281L;
 
             public void buttonClick(ClickEvent event) {
                 String name = (String) bufferName.getValue();
                 String code = (String) editor.getValue();
                 interpreter.addSource(name, code);
                 bufferSelect.addItem(name);
             }
         });
         hl.addComponent(bufferName);
         hl.addComponent(saveBlockButton);
         vl.addComponent(hl);
 
         bufferSelect = new ListSelect("Blocks");
         bufferSelect.setSizeFull();
         bufferSelect.setNullSelectionAllowed(false);
         bufferSelect.setImmediate(true);
         bufferSelect.addValueChangeListener(new ValueChangeListener() {
 
         /**
 	     * 
 	     */
             private static final long serialVersionUID = 2450173366304389892L;
 
             public void valueChange(ValueChangeEvent event) {
                 String value = (String) bufferSelect.getValue();
                 editor.setValue(interpreter.getSource(value));
                 bufferName.setValue(value);
             }
 
         });
 
         vl.addComponent(bufferSelect);
         vl.setExpandRatio(bufferSelect, 1);
         buffersAndStatus.addTab(vl, "Buffers", null);
 
         vl = new VerticalLayout();
         vl.setSizeFull();
         printStackButton = new Button("printStack", new Button.ClickListener() {
 
             /**
 	     * 
 	     */
             private static final long serialVersionUID = -7954166353532284682L;
 
             public void buttonClick(ClickEvent event) {
                 interpreter.printStack();
             }
 
         });
         vl.addComponent(printStackButton);
         wordListSelect = new ListSelect("Wordlist"); // ,
         // interpreter.getWordList());
         wordListSelect.setSizeFull();
         wordListSelect.addStyleName("wordlist");
         vl.addComponent(wordListSelect);
         vl.setExpandRatio(wordListSelect, 1);
         buffersAndStatus.addTab(vl, "Status", null);
         addComponent(buffersAndStatus);
 
     }
 
     private void constructEditorAndLayout() {
         VerticalLayout vl = new VerticalLayout();
         editorAndLayout = new TabSheet();
         editorAndLayout.setSizeFull();
 
         vl = new VerticalLayout();
         vl.setSpacing(true);
         vl.setSizeFull();
         editor = createEditor();
         vl.addComponent(editor);
         vl.setExpandRatio(editor, 1);
 
         HorizontalLayout hl = new HorizontalLayout();
         hl.setSpacing(true);
         Button b = new Button("Execute");
         b.setImmediate(true);
         b.addClickListener(runListener);
         hl.addComponent(b);
 
         b = new Button("Clear and execute");
         b.setImmediate(true);
         b.addClickListener(clearAndRunListener);
         hl.addComponent(b);
 
         clearConsoleButton = new Button("Clear console");
         clearConsoleButton.addClickListener(clearConsoleListener);
         hl.addComponent(clearConsoleButton);
 
         CheckBox cb = new CheckBox("log executed words");
         cb.addValueChangeListener(new ValueChangeListener() {
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 8229355957367047681L;
 
 			@Override
 			public void valueChange(ValueChangeEvent event) {
                 interpreter.setLogExecutedWords((Boolean) event.getProperty()
                         .getValue());
 			}
 		});
 
         hl.addComponent(cb);
 
         cb = new CheckBox("log added words");
         cb.addValueChangeListener(new ValueChangeListener() {
 			
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 3555018128568837160L;
 
 			@Override
 			public void valueChange(ValueChangeEvent event) {
                 interpreter.setLogNewWords((Boolean) event.getProperty()
                         .getValue());
 			}
 		});
 
         hl.addComponent(cb);
 
         vl.addComponent(hl);
 
         editorAndLayout.addTab(vl, "Editor", null);
 
         vl = new VerticalLayout();
         vl.setSizeFull();
         vl.addComponent(new Button("Clear layout", new Button.ClickListener() {
             /**
 	     * 
 	     */
             private static final long serialVersionUID = -7003358831608079032L;
 
             public void buttonClick(ClickEvent event) {
                 ((VerticalLayout)panel.getContent()).removeAllComponents();
             }
         }));
 
         panel = new Panel();
         VerticalLayout panelContent = new VerticalLayout();
         panelContent.setSpacing(true);
         panel.setContent(panelContent);
         
         panel.setSizeFull();
 
         vl.addComponent(panel);
         vl.setExpandRatio(panel, 1);
 
         editorAndLayout.addTab(vl, "Output", null);
 
         VerticalSplitPanel vsp = new VerticalSplitPanel();
         vsp.setSizeFull();
         vsp.addComponent(editorAndLayout);
         vsp.setSplitPosition(80);
 
         consoleString = new StringBuffer();
         console = new TextArea();
         console.setSizeFull();
         print("Yes. This is Forth. Welcome!");
         vsp.addComponent(console);
         addComponent(vsp);
         setSplitPosition(15);
 
     }
 
     public AceEditor createEditor() {
         AceEditor editor = new AceEditor();
        editor.setMode(AceMode.);
         editor.setTheme(AceTheme.vibrant_ink);
         editor.setSizeFull();
         return editor;
     }
 
     private Button.ClickListener runListener = new Button.ClickListener() {
 
         /**
 	 * 
 	 */
         private static final long serialVersionUID = -4517134025974358295L;
 
         public void buttonClick(ClickEvent event) {
             String command = (String) editor.getValue();
             if (!command.isEmpty()) {
                 interpreter.runBuffer(command);
             }
         }
 
     };
 
     private Button.ClickListener clearConsoleListener = new Button.ClickListener() {
 
         /**
 	 * 
 	 */
         private static final long serialVersionUID = -795270660017061409L;
 
         public void buttonClick(ClickEvent event) {
             consoleString = new StringBuffer();
             print("");
         }
 
     };
 
     private Button.ClickListener clearAndRunListener = new Button.ClickListener() {
 
         /**
 	 * 
 	 */
         private static final long serialVersionUID = 8556993951867295858L;
 
         public void buttonClick(ClickEvent event) {
             String command = (String) editor.getValue();
             if (!command.isEmpty()) {
                 ((VerticalLayout)panel.getContent()).removeAllComponents();
                 interpreter.runBuffer(command);
             }
         }
     };
 
     public void print(String msg) {
         consoleString.insert(0, "\n");
         consoleString.insert(0, msg);
         console.setValue(consoleString.toString());
     }
 
     public void addNewWord(String word) {
         wordListSelect.addItem(word);
     }
 
     public ComponentContainer getMainComponentContainer() {
         return (ComponentContainer)panel.getContent();
     }
 
     public void disableContinueButton() {
         clearConsoleButton.setEnabled(false);
     }
 
     public void enableContinueButton() {
         clearConsoleButton.setEnabled(true);
     }
 
 }
