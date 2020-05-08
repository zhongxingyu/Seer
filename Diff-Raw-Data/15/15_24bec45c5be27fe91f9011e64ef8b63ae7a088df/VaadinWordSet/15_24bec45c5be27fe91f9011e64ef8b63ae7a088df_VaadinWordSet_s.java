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
 package com.github.mjvesa.herd.wordset;
 
 import java.util.ArrayList;
 
 import com.github.mjvesa.herd.BaseWord;
 import com.github.mjvesa.herd.CompiledWord;
 import com.github.mjvesa.herd.DefinedWord;
 import com.github.mjvesa.herd.Interpreter;
 import com.github.mjvesa.herd.Util;
 import com.github.mjvesa.herd.Word;
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.event.ItemClickEvent.ItemClickListener;
 import com.vaadin.ui.AbstractSelect;
 import com.vaadin.ui.AbstractSelect.NewItemHandler;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.ComponentContainer;
 import com.vaadin.ui.DateField;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.ListSelect;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 public class VaadinWordSet extends WordSet {
 
 	@Override
 	public Word[] getWords() {
 		return new Word[] {
 
 		new BaseWord("new-button", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = -2492817908731559368L;
 
 			@Override
 			public void execute(final Interpreter interpreter) {
 				
 				Button b = new Button("", new Button.ClickListener() {
 					
 					/**
 					 * 
 					 */
 					private static final long serialVersionUID = -4622489800920283752L;
 
 					@Override
 					public void buttonClick(ClickEvent event) {
 						Button b = event.getButton();
 						DefinedWord command = (DefinedWord) b.getData();
 						if (command != null) {
 							interpreter.execute(command);
 						}
 					}
 				});
 				interpreter.pushData(b);
 			}
 		},
 
 		new BaseWord("set-click-listener", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = 5749856686458297558L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				Object o = interpreter.popData();
 				Button b = (Button) interpreter.popData();
 				b.setData(o);
 				interpreter.pushData(b);
 			}
 		},
 
 		new BaseWord("new-hl", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = 8813556668649386248L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				HorizontalLayout hl = new HorizontalLayout();
 				hl.setSpacing(true);
 				interpreter.pushData(hl);
 			}
 		},
 
 		new BaseWord("new-vl", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = -1848213448504804229L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				VerticalLayout vl = new VerticalLayout();
 				vl.setSpacing(true);
 				interpreter.pushData(vl);
 			}
 		},
 
 		new BaseWord("new-gl", "( x y - gl )", Word.POSTPONED) {
 
 			private static final long serialVersionUID = 4079634885691605257L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				Integer height = (Integer) interpreter.popData();
 				Integer width = (Integer) interpreter.popData();
 				interpreter.pushData(new GridLayout(width, height));
 			}
 		},
 
 		new BaseWord("gl-new-line", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = 975877390052961807L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				((GridLayout) interpreter.peekData()).newLine();
 			}
 		},
 
 		new BaseWord("new-window", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = -6887364362728545090L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				Window w = new Window();
				((VerticalLayout) w.getContent()).setSpacing(true);
 				interpreter.pushData(w);
 			}
 		},
 
 		new BaseWord("main-panel", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = -8622281600566696475L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				interpreter.pushData(interpreter.getMainPanel());
 			}
 		},
 
 		new BaseWord("add-window", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = 7106029415576813922L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				Window w = (Window) interpreter.popData();
 				interpreter.getView().getUI().addWindow(w);
 			}
 		},
 
 		new BaseWord("add-component", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = 5640824046985354091L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				Component comp = (Component) interpreter.popData();
 				ComponentContainer cc = (ComponentContainer) interpreter
 						.popData();
 				cc.addComponent(comp);
 				interpreter.pushData(cc);
 			}
 		},
 
 		new BaseWord("set-caption", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = 5497598050469462487L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				String s = (String) interpreter.popData();
 				Component c = (Component) interpreter.popData();
 				c.setCaption(s);
 				interpreter.pushData(c);
 			}
 		},
 
 		new BaseWord("set-value", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = -1769743552659215058L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				Object o = interpreter.popData();
				Field f = (Field) interpreter.popData();
				f.setValue(o);
				interpreter.pushData(f);
 			}
 		},
 
 		new BaseWord("get-value", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = 8445550546521886374L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				Field f = (Field) interpreter.popData();
 				interpreter.pushData(f);
 				interpreter.pushData(f.getValue());
 
 			}
 		},
 
 		new BaseWord("set-size-full", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = -1206491811133054467L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				Component comp = (Component) interpreter.popData();
 				comp.setSizeFull();
 				interpreter.pushData(comp);
 			}
 		},
 
 		new BaseWord("set-size-undefined", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = -3450618729379622987L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				Component comp = (Component) interpreter.popData();
 				comp.setSizeUndefined();
 				interpreter.pushData(comp);
 			}
 		},
 
 		new BaseWord("set-height", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = -8426734568403715950L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				String str = (String) interpreter.popData();
 				Component comp = (Component) interpreter.popData();
 				comp.setHeight(str);
 				interpreter.pushData(comp);
 			}
 		},
 
 		new BaseWord("set-width", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = -4558264143049463814L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				String str = (String) interpreter.popData();
 				Component comp = (Component) interpreter.popData();
 				comp.setWidth(str);
 				interpreter.pushData(comp);
 			}
 		},
 
 		new BaseWord("clear-container", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = 1070175466682034329L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				ComponentContainer cc = (ComponentContainer) interpreter
 						.popData();
 				cc.removeAllComponents();
 			}
 		},
 
 		new BaseWord("new-check-box", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = 4018632924389912599L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				interpreter.pushData(new CheckBox());
 			}
 		},
 
 		new BaseWord("new-date-field", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = 6313296566085274642L;
 
 			@Override
 			public void execute(final Interpreter interpreter) {
 				interpreter.pushData(new DateField());
 				final String dfCommand = (String) interpreter.popData();
 				DateField df = new DateField();
 				df.setImmediate(true);
 				df.addValueChangeListener(new ValueChangeListener() {
 					/**
 					 * 
 					 */
 					private static final long serialVersionUID = 1472139878970514093L;
 
 					public void valueChange(ValueChangeEvent event) {
 						interpreter.pushData(event.getProperty().getValue());
 						interpreter.interpret(dfCommand);
 					}
 
 				});
 				interpreter.pushData(df);
 			}
 		},
 
 		new BaseWord("new-label", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = -2825285195439247251L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				interpreter.pushData(new Label());
 			}
 		},
 
 		new BaseWord("new-text-field", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = -1064489458253275380L;
 
 			@Override
 			public void execute(final Interpreter interpreter) {
 				final String tfCommand = interpreter.getNextNonNopWord();
 				TextField tf = new TextField();
 				tf.setCaption((String) interpreter.popData());
 				tf.setValue("");
 				tf.setImmediate(true);
 				tf.addValueChangeListener(new ValueChangeListener() {
 					/**
 				 *
 				 */
 					private static final long serialVersionUID = 4325104922208051065L;
 
 					public void valueChange(ValueChangeEvent event) {
 						interpreter.pushData(event.getProperty().getValue());
 						interpreter.interpret(tfCommand);
 					}
 				});
 				interpreter.pushData(tf);
 			}
 		},
 
 		new BaseWord("new-table", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = -5052653341575232035L;
 
 			@Override
 			public void execute(final Interpreter interpreter) {
 				final String tableCommand = interpreter.getParser()
 						.getNextWord();
 				Table table = new Table();
 				table.setCaption((String) interpreter.popData());
 				table.setImmediate(true);
 				table.setSelectable(true);
 				table.addItemClickListener(new ItemClickListener() {
 
 					/**
 				 *
 				 */
 					private static final long serialVersionUID = 3585546076571010729L;
 
 					public void itemClick(ItemClickEvent event) {
 
 						interpreter.pushData(event.getItem());
 						interpreter.execute(interpreter.getDictionary().get(
 								tableCommand));
 					}
 				});
 				interpreter.pushData(table);
 			}
 		},
 
 		new BaseWord("new-combo-box", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = 3881577354424928897L;
 
 			@Override
 			public void execute(final Interpreter interpreter) {
 				final String newItemCommand = interpreter.getParser()
 						.getNextWord();
 				final String itemSelectedCommand = interpreter.getParser()
 						.getNextWord();
 				final ComboBox cb = new ComboBox();
 				String str = (String) interpreter.popData();
 				cb.setNullSelectionAllowed(false);
 				cb.setCaption(str);
 				cb.setItemCaptionMode(AbstractSelect.ItemCaptionMode.ITEM);
 				cb.setNewItemsAllowed(true);
 				cb.setNewItemHandler(new NewItemHandler() {
 
 					/**
 				 *
 				 */
 					private static final long serialVersionUID = 3340658590351611289L;
 
 					public void addNewItem(String newItemCaption) {
 						cb.setImmediate(false);
 						interpreter.pushData(newItemCaption);
 						interpreter.interpret(newItemCommand);
 						cb.setImmediate(true);
 					}
 				});
 
 				cb.addValueChangeListener(new ValueChangeListener() {
 
 					/**
 				 *
 				 */
 					private static final long serialVersionUID = 2706579869793251379L;
 
 					public void valueChange(ValueChangeEvent event) {
 						interpreter.pushData(cb.getContainerDataSource()
 								.getItem(event.getProperty().getValue()));
 						interpreter.interpret(itemSelectedCommand);
 					}
 				});
 				cb.setImmediate(true);
 				interpreter.pushData(cb);
 			}
 		},
 
 		new BaseWord("new-select", "", Word.POSTPONED) {
 
 			private static final long serialVersionUID = -6142351970812196488L;
 
 			@Override
 			public void execute(final Interpreter interpreter) {
 				final String selCommand = interpreter.getParser().getNextWord();
 				final ComboBox sel = new ComboBox();
 				sel.setCaption((String) interpreter.popData());
 				sel.setItemCaptionMode(AbstractSelect.ItemCaptionMode.ITEM);
 				sel.setNullSelectionAllowed(false);
 				sel.setImmediate(true);
 				sel.addValueChangeListener(new ValueChangeListener() {
 					/**
 				 *
 				 */
 					private static final long serialVersionUID = -7705548618092166199L;
 
 					public void valueChange(ValueChangeEvent event) {
 						Item item = sel.getContainerDataSource().getItem(
 								event.getProperty().getValue());
 						interpreter.pushData(item);
 						interpreter.interpret(selCommand);
 					}
 				});
 				interpreter.pushData(sel);
 			}
 		},
 
 		new BaseWord("new-list-select", "", Word.POSTPONED) {
 			private static final long serialVersionUID = 8686093227035249035L;
 
 			@Override
 			public void execute(final Interpreter interpreter) {
 				final String lselCommand = interpreter.getParser()
 						.getNextWord();
 				final ListSelect lsel = new ListSelect();
 				lsel.setCaption((String) interpreter.popData());
 				lsel.setItemCaptionMode(AbstractSelect.ItemCaptionMode.ITEM);
 				lsel.setNullSelectionAllowed(false);
 				lsel.setImmediate(true);
 				lsel.addValueChangeListener(new ValueChangeListener() {
 					/**
 				 *
 				 */
 					private static final long serialVersionUID = -5523488417834167806L;
 
 					public void valueChange(ValueChangeEvent event) {
 						Item item = lsel.getContainerDataSource().getItem(
 								event.getProperty().getValue());
 						interpreter.pushData(item);
 						interpreter.interpret(lselCommand);
 					}
 				});
 				interpreter.pushData(lsel);
 			}
 		},
 
 		new BaseWord("set-container-data-source", "", Word.POSTPONED) {
 			private static final long serialVersionUID = 8644721936358613031L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				Container cont = (Container) interpreter.popData();
 				AbstractSelect as = (AbstractSelect) interpreter.popData();
 				as.setContainerDataSource(cont);
 				interpreter.pushData(as);
 			}
 		},
 
 		new BaseWord("set-column-headers", "", Word.POSTPONED) {
 			private static final long serialVersionUID = -7296881714369214846L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				Table table = (Table) interpreter.popData();
 				table.setColumnHeaders((String[]) 
 						getArrayFromList(interpreter, new String[0]));
 			}
 		},
 
 		new BaseWord("set-visible-columns", "", Word.POSTPONED) {
 			private static final long serialVersionUID = 5674765074478598320L;
 
 			@Override
 			public void execute(Interpreter interpreter) {
 				Table table = (Table) interpreter.popData();
 				table.setVisibleColumns((String[]) 
 						getArrayFromList(interpreter, new String[0]));
 			}
 		}
 
 		};
 	}
 	
 	/**
 	 * Constructs an array from a list on the stack.
 	 * 
 	 * @param array
 	 *            Array whose type will be nicked.
 	 * 
 	 * @return
 	 */
 	public <T> T[] getArrayFromList(Interpreter interpreter, T[] array) {
 
 		ArrayList<Object> list = new ArrayList<Object>();
 
 		// The first word of 'list[' is a literal with the address of our
 		// list as parameter
 		Word[] code = ((DefinedWord) interpreter.getDictionary().get("list[")).getCode();
 		int addr = (Integer) ((CompiledWord) code[0]).getParameter();
 
 		while (interpreter.peekHeap(addr) != Util.LIST_TERMINATOR) {
 			list.add(interpreter.peekHeap(addr));
 			addr++;
 		}
 		return list.toArray(array);
 	}
 
 
 }
