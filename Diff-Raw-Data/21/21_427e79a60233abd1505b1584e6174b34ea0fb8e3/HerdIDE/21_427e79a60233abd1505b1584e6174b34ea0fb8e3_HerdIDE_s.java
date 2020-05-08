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
 package com.github.mjvesa.herd;
 
 import org.vaadin.aceeditor.AceEditor;
 import org.vaadin.aceeditor.AceMode;
 import org.vaadin.aceeditor.AceTheme;
 
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.Component;
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
 public class HerdIDE extends HorizontalSplitPanel implements View {
 
 	/**
      * 
      */
 	private static final long serialVersionUID = -6911850470039819669L;
 
 	private TextArea console;
 	private StringBuffer consoleString;
 	private Panel mainPanel;
 	private ListSelect fileSelect;
 	private TextField fileName;
 	private ListSelect wordListSelect;
 	private AceEditor editor;
 
 	private Interpreter interpreter;
 
 	public HerdIDE() {
 		setSizeFull();
 		addComponent(constructFilesAndStatus());
 		addComponent(constructEditorAndLayoutPanel());
 		setSplitPosition(15);
 
 		interpreter = new Interpreter();
 		interpreter.setView(this);
 		interpreter.setup();
 		fillFileSelect();
 	}
 
 	/**
 	 * Fills the select for files with their filenames
 	 */
 	private void fillFileSelect() {
 		for (String s : interpreter.getFileList()) {
 			fileSelect.addItem(s);
 		}
 	}
 
 	private TabSheet constructFilesAndStatus() {
 		TabSheet tabs = new TabSheet();
 		tabs.setSizeFull();
 		tabs.addTab(constructFilesTab(), "Files", null);
 		tabs.addTab(constructStatusTab(), "Status", null);
 		return tabs;
 	}
 
 	private Component constructStatusTab() {
 		VerticalLayout vl = new VerticalLayout();
 		vl.setSizeFull();
 		vl.addComponent(createPrintStackButton());
 		wordListSelect = createWordListSelect();
 		vl.addComponent(wordListSelect);
 		vl.setExpandRatio(wordListSelect, 1);
 		return vl;
 	}
 
 	private Component constructFilesTab() {
 		VerticalLayout vl = new VerticalLayout();
 		vl.setSpacing(true);
 		vl.setSizeFull();
 		vl.addComponent(constructFileNameAndSaveButton());
 		fileSelect = createFileSelect();
 		vl.addComponent(fileSelect);
 		vl.setExpandRatio(fileSelect, 1);
 		return vl;
 	}
 
 	private Component constructFileNameAndSaveButton() {
 
 		HorizontalLayout hl = new HorizontalLayout();
 		hl.setSpacing(true);
 		fileName = createFileName();
 		hl.addComponent(fileName);
 		hl.addComponent(createSaveFileButton());
 		return hl;
 	}
 
 	private ListSelect createWordListSelect() {
 		ListSelect ls = new ListSelect("Wordlist");
 		ls.setSizeFull();
 		ls.addStyleName("wordlist");
 		return ls;
 	}
 
 	private Component createPrintStackButton() {
 		Button b = new Button("printStack", new Button.ClickListener() {
 
 		/**
 	     * 
 	     */
 			private static final long serialVersionUID = -7954166353532284682L;
 
 			public void buttonClick(ClickEvent event) {
 				interpreter.printStack();
 			}
 		});
 		return b;
 	}
 
 	private ListSelect createFileSelect() {
 
 		final ListSelect ls = new ListSelect("Files");
 		ls.setSizeFull();
 		ls.setNullSelectionAllowed(false);
 		ls.setImmediate(true);
 		ls.addValueChangeListener(new ValueChangeListener() {
 
 			/**
 	     * 
 	     */
 			private static final long serialVersionUID = 2450173366304389892L;
 
 			public void valueChange(ValueChangeEvent event) {
 				String value = (String) ls.getValue();
 				editor.setValue(interpreter.getSource(value));
 				fileName.setValue(value);
 			}
 		});
 		return ls;
 	}
 
 	private Button createSaveFileButton() {
 
 		Button saveFileButton = new Button("Save", new Button.ClickListener() {
 			/**
 		     * 
 		     */
 			private static final long serialVersionUID = -1273939649837923281L;
 
 			public void buttonClick(ClickEvent event) {
 				String name = (String) fileName.getValue();
 				String code = (String) editor.getValue();
 				interpreter.addSource(name, code);
 				fileSelect.addItem(name);
 			}
 		});
 		return saveFileButton;
 	}
 
 	private TextField createFileName() {
 		final TextField tf = new TextField();
 		return tf;
 	}
 
 	private VerticalSplitPanel constructEditorAndLayoutPanel() {
 		VerticalSplitPanel vsp = new VerticalSplitPanel();
 		vsp.setSizeFull();
 		vsp.addComponent(constructEditorAndLayout());
 		vsp.setSplitPosition(80);
 		console = createConsole();
 		vsp.addComponent(console);
 		return vsp;
 	}
 
 	private TextArea createConsole() {
 		consoleString = new StringBuffer();
 		TextArea ta = new TextArea();
 		ta.setSizeFull();
 		return ta;
 	}
 
 	private Component constructEditorAndLayout() {
 		TabSheet ts = new TabSheet();
 		ts.setSizeFull();
 		ts.addTab(constructEditorTab(), "Editor", null);
 		ts.addTab(constructOutputTab(), "Output", null);
 		return ts;
 	}
 
 	private Component constructOutputTab() {
 		VerticalLayout vl = new VerticalLayout();
 		vl.setSizeFull();
 		vl.addComponent(createLayoutClearButton());
 		mainPanel = createMainPanel();
 		vl.addComponent(mainPanel);
 		vl.setExpandRatio(mainPanel, 1);
 		return vl;
 	}
 
 	private Component createLayoutClearButton() {
 		return new Button("Clear layout", new Button.ClickListener() {
 			/**
 	     * 
 	     */
 			private static final long serialVersionUID = -7003358831608079032L;
 
 			public void buttonClick(ClickEvent event) {
 				((VerticalLayout) mainPanel.getContent()).removeAllComponents();
 			}
 		});
 	}
 
 	private Panel createMainPanel() {
 		Panel p = new Panel();
 		VerticalLayout vl = new VerticalLayout();
 		vl.setSpacing(true);
 		p.setContent(vl);
 		p.setSizeFull();
 		return p;
 	}
 
 	private Component constructEditorTab() {
 		VerticalLayout vl = new VerticalLayout();
 		vl.setSpacing(true);
 		vl.setSizeFull();
 		editor = createEditor();
 		vl.addComponent(editor);
 		vl.setExpandRatio(editor, 1);
 		vl.addComponent(constructButtons());
 		return vl;
 	}
 
 	private Component constructButtons() {
 
 		HorizontalLayout hl = new HorizontalLayout();
 		hl.setSpacing(true);
 		hl.addComponent(createRunButton());
 		hl.addComponent(createClearAndExecuteButton());
 		hl.addComponent(createClearConsoleButton());
 		hl.addComponent(createLogExecutedWordsCheckBox());
 		hl.addComponent(createLogAddedWordCheckBox());
 
 		return hl;
 	}
 
 	private Component createLogAddedWordCheckBox() {
 		CheckBox cb = new CheckBox("log added words");
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
 		return cb;
 	}
 
 	private Component createLogExecutedWordsCheckBox() {
 		
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
 		return cb;
 	}
 
 	private Component createClearConsoleButton() {
 		return new Button("Clear console",
 				new Button.ClickListener() {
			/**
		 * 
		 */
 			private static final long serialVersionUID = -795270660017061409L;
 
 			public void buttonClick(ClickEvent event) {
 				consoleString = new StringBuffer();
 				print("");
 			}
 		});
 	}
 
 	private Component createClearAndExecuteButton() {
 		return  new Button("Clear and execute",
 				new Button.ClickListener() {
 
			/**
		 * 
		 */
 			private static final long serialVersionUID = 8556993951867295858L;
 
 			public void buttonClick(ClickEvent event) {
 				String command = (String) editor.getValue();
 				if (!command.isEmpty()) {
 					((VerticalLayout) mainPanel.getContent()).removeAllComponents();
 					interpreter.runFile(command);
 				}
 			}
 		});
 	}
 
 	private Component createRunButton() {
 
 		return new Button("Execute", new Button.ClickListener() {
 
			/**
    	 * 
    	 */
 			private static final long serialVersionUID = -4517134025974358295L;
 
 			public void buttonClick(ClickEvent event) {
 				String command = (String) editor.getValue();
 				if (!command.isEmpty()) {
 					interpreter.runFile(command);
 				}
 			}
 		});
 	}
 
 	public AceEditor createEditor() {
 		AceEditor editor = new AceEditor();
 		editor.setMode(AceMode.text);
 		editor.setTheme(AceTheme.vibrant_ink);
 		editor.setSizeFull();
 		return editor;
 	}
 
 	public void print(String msg) {
 		consoleString.insert(0, "\n");
 		consoleString.insert(0, msg);
 		console.setValue(consoleString.toString());
 	}
 
 	public void addNewWord(String word) {
 		wordListSelect.addItem(word);
 	}
 
 	public ComponentContainer getMainComponentContainer() {
 		return (ComponentContainer) mainPanel.getContent();
 	}
 }
