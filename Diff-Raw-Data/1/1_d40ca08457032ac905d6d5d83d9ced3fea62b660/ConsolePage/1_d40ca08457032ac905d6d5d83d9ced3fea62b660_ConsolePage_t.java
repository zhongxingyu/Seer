 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.console;
 
 import static org.oobium.console.ConsoleImages.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.oobium.console.Console;
 import org.oobium.console.Function;
 
 public class ConsolePage extends Composite {
 
 	protected Console console;
 	private ToolBar finder;
 	private Text finderInput;
 	private Button regex;
 	private Button caseSensitive;
 	private ToolItem finderPrev;
 	private ToolItem finderNext;
 	private ToolItem errorItem;
 	private Composite finderError;
 	private Label finderErrorLbl;
 	private String finderText;
 	private Point finderPos;
 	
 	private List<Point> matches;
 
 	private int match;
 	
 	public ConsolePage(Composite parent, int style) {
 		super(parent, style);
 		createContents(style & SWT.READ_ONLY);
 		super.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
 	}
 	
 	private void createContents(int consoleStyle) {
 		GridLayout layout = new GridLayout();
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		layout.verticalSpacing = 1;
 		setLayout(layout);
 
 		console = new Console(this, consoleStyle);
 		console.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		console.addFunction(new Function(Function.COMMAND, 'f') {
 			@Override
 			public void execute() {
 				setFinderVisible(true);
 			}
 		});
 		
 		console.addFunction(new Function(SWT.ESC) {
 			@Override
 			public void execute() {
 				setFinderVisible(false);
 			}
 		});
 		
 		createFinder();
 		setFinderVisible(false);
 	}
 	
 	private void createFinder() {
 		finder = new ToolBar(this, SWT.FLAT | SWT.HORIZONTAL);
		finder.setBackground(getBackground());
 		finder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		
 		ToolItem item = new ToolItem(finder, SWT.PUSH);
 		item.setImage(CLOSE.getImage());
 		item.setToolTipText("Close");
 		item.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				setFinderVisible(false);
 			}
 		});
 
 		item = new ToolItem(finder, SWT.SEPARATOR);
 		Composite inputComp = new Composite(finder, SWT.NONE);
 		GridLayout layout = new GridLayout(4, false);
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		inputComp.setLayout(layout);
 
 		Label lbl = new Label(inputComp, SWT.NONE);
 		lbl.setText("Find:");
 		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
 		
 		finderInput = new Text(inputComp, SWT.BORDER);
 		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
 		data.widthHint = 200;
 		data.verticalIndent = 1;
 		finderInput.setLayoutData(data);
 		finderInput.addListener(SWT.KeyDown, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				if(event.stateMask == 0) {
 					matches = null;
 					if(event.character == SWT.ESC) {
 						setFinderVisible(false);
 						event.doit = false;
 					} else if(event.character == SWT.CR) {
 						findNext();
 						event.doit = false;
 					}
 				} else if(event.stateMask == SWT.SHIFT && event.character == SWT.CR) {
 					matches = null;
 					findPrev();
 					event.doit = false;
 				}
 			}
 		});
 		finderInput.addListener(SWT.Modify, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				matches = null;
 				findAgain();
 			}
 		});
 
 		regex = new Button(inputComp, SWT.CHECK);
 		regex.setText("regex");
 		regex.setToolTipText("Regular Expression");
 		regex.setLayoutData(new GridData());
 		regex.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				matches = null;
 				findAgain();
 			}
 		});
 		
 		caseSensitive = new Button(inputComp, SWT.CHECK);
 		caseSensitive.setText("Aa");
 		caseSensitive.setToolTipText("Case Sensitive");
 		caseSensitive.setLayoutData(new GridData());
 		caseSensitive.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				matches = null;
 				findAgain();
 			}
 		});
 
 		item.setControl(inputComp);
 		item.setWidth(inputComp.computeSize(-1, -1).x);
 
 		finderPrev = new ToolItem(finder, SWT.PUSH);
 		finderPrev.setImage(PREV.getImage());
 		finderPrev.setToolTipText("Previous");
 		finderPrev.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				findPrev();
 			}
 		});
 		
 		finderNext = new ToolItem(finder, SWT.PUSH);
 		finderNext.setImage(NEXT.getImage());
 		finderNext.setToolTipText("Next");
 		finderNext.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				findNext();
 			}
 		});
 		
 		
 		errorItem = new ToolItem(finder, SWT.SEPARATOR);
 		
 		finderError = new Composite(finder, SWT.NONE);
 		finderError.setVisible(false);
 		layout = new GridLayout(2, false);
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		finderError.setLayout(layout);
 		
 		lbl = new Label(finderError, SWT.NONE);
 		lbl.setImage(ERROR.getImage());
 		data = new GridData(SWT.FILL, SWT.FILL, false, true);
 		lbl.setLayoutData(data);
 
 		finderErrorLbl = new Label(finderError, SWT.NONE);
 		finderErrorLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
 		
 		errorItem.setControl(finderError);
 	}
 
 	private void disableFinder() {
 		finderError.setVisible(false);
 		finderPrev.setEnabled(false);
 		finderNext.setEnabled(false);
 		console.clearSelection();
 	}
 	
 	private void findAll(Pattern pattern) {
 		match = 0;
 		matches = new ArrayList<Point>();
 		Matcher m = pattern.matcher(finderText);
 		int start = finderPos.y + 1;
 		if(start >= finderText.length()) start = 0;
 		while(m.find()) {
 			int x = m.start();
 			int y = m.end();
 			if(x == finderPos.x) {
 				match = matches.size();
 			}
 			matches.add(new Point(x, y));
 		}
 	}
 	
 	private void findAgain() {
 		finderPos.y = finderPos.x-1;
 		if(finderPos.y < 0) finderPos.y = 0;
 		findNext();
 		if(regex.getSelection()) {
 			findPrev();
 		}
 	}
 	
 	private void findNext() {
 		String searchText = finderInput.getText();
 		if(searchText == null || searchText.length() == 0) {
 			disableFinder();
 		} else {
 			if(regex.getSelection()) {
 				if(matches == null) {
 					try {
 						Pattern pattern = caseSensitive.getSelection() ? Pattern.compile(searchText) : Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
 						findAll(pattern);
 					} catch(PatternSyntaxException e) {
 						showError("Not a valid regular expression");
 						return;
 					}
 				}
 				findNextRegex();
 			} else {
 				findNext(searchText);
 			}
 		}
 	}
 	
 	private void findNext(String searchText) {
 		boolean caseSensitive = this.caseSensitive.getSelection();
 		char[] ca = (caseSensitive ? searchText : searchText.toLowerCase()).toCharArray();
 		String finderText = caseSensitive ? this.finderText : this.finderText.toLowerCase();
 		int ix = -1;
 		int start = finderPos.y + 1;
 		if(start >= finderText.length()) start = 0;
 		int end = finderPos.y;
 		if(end < 0) end = finderText.length();
 		for(int i = start; i != end; i++) {
 			boolean match = false;
 			for(int j = 0; j < ca.length && (i+j) < finderText.length(); j++) {
 				if(ca[j] != finderText.charAt(i+j)) {
 					break;
 				}
 				if(j == ca.length-1) {
 					match = true;
 				}
 			}
 			if(match) {
 				ix = i;
 				break;
 			}
 			if(i == finderText.length()-1 && end != finderText.length()) {
 				i = -1;
 			}
 		}
 		if(ix == -1) {
 			showError("Phrase not found");
 		} else {
 			finderPos.x = ix;
 			finderPos.y = finderPos.x + searchText.length();
 			showSelection(finderPos);
 		}
 	}
 
 	private void findNextRegex() {
 		if(matches.isEmpty()) {
 			showError("Phrase not found");
 		} else {
 			if(++match >= matches.size()) match = 0;
 			showSelection(matches.get(match));
 		}
 	}
 
 	private void findPrev() {
 		String searchText = finderInput.getText();
 		if(searchText == null || searchText.length() == 0) {
 			disableFinder();
 		} else {
 			if(regex.getSelection()) {
 				if(matches == null) {
 					try {
 						Pattern pattern = caseSensitive.getSelection() ? Pattern.compile(searchText) : Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
 						findAll(pattern);
 					} catch(PatternSyntaxException e) {
 						showError("Not a valid regular expression");
 						return;
 					}
 				}
 				findPrevRegex();
 			} else {
 				findPrev(searchText);
 			}
 		}
 	}
 
 	private void findPrev(String searchText) {
 		boolean caseSensitive = this.caseSensitive.getSelection();
 		char[] ca = (caseSensitive ? searchText : searchText.toLowerCase()).toCharArray();
 		String finderText = caseSensitive ? this.finderText : this.finderText.toLowerCase();
 		int ix = -1;
 		int start = finderPos.x - 1;
 		if(start < 0) start = finderText.length();
 		int end = finderPos.x;
 		if(end < 0) end = 0;
 		for(int i = start; i != end; i--) {
 			boolean match = false;
 			for(int j = 0; j < ca.length && (i+j) < finderText.length(); j++) {
 				if(ca[j] != finderText.charAt(i+j)) {
 					break;
 				}
 				if(j == ca.length-1) {
 					match = true;
 				}
 			}
 			if(match) {
 				ix = i;
 				break;
 			}
 			if(i == 0 && end != 0) {
 				i = finderText.length();
 			}
 		}
 		if(ix == -1) {
 			showError("Phrase not found");
 		} else {
 			finderPos.x = ix;
 			finderPos.y = finderPos.x + searchText.length();
 			showSelection(finderPos);
 		}
 	}
 	
 	private void findPrevRegex() {
 		if(matches.isEmpty()) {
 			showError("Phrase not found");
 		} else {
 			if(--match < 0) match = matches.size()-1;
 			showSelection(matches.get(match));
 		}
 	}
 	
 	public Console getConsole() {
 		return console;
 	}
 	
 	public boolean getScrollLock() {
 		return console.getScrollLock();
 	}
 	
 	@Override
 	public void setBackground(Color color) {
 		console.setBackground(color);
 	}
 
 	private void setFinderVisible(boolean visible) {
 		matches = null;
 		if(visible) {
 			finderText = console.getText();
 			String text = console.getSelectionText();
 			if(text == null || text.length() == 0) {
 				finderPos = new Point(-1, -1);
 				finderInput.setText("");
 				finderPrev.setEnabled(false);
 				finderNext.setEnabled(false);
 			} else {
 				finderPos = console.getSelection();
 				finderInput.setText(text);
 				finderInput.selectAll();
 				finderPrev.setEnabled(true);
 				finderNext.setEnabled(true);
 			}
 			finderError.setVisible(false);
 			((GridData) finder.getLayoutData()).exclude = false;
 			finder.setVisible(true);
 			finderInput.setFocus();
 		} else {
 			finderText = null;
 			finder.setVisible(false);
 			((GridData) finder.getLayoutData()).exclude = true;
 			console.setFocus();
 		}
 		layout();
 	}
 	
 	@Override
 	public boolean setFocus() {
 		return console.setFocus();
 	}
 	
 	@Override
 	public void setForeground(Color color) {
 		super.setForeground(color);
 		console.setForeground(color);
 	}
 	
 	public void setPrompt(String prompt) {
 		console.setPrompt(prompt);
 	}
 
 	public void setScrollLock(boolean lock) {
 		console.setScrollLock(lock);
 	}
 
 	private void showError(String message) {
 		finderErrorLbl.setText(message);
 		finderError.setVisible(true);
 		finderPrev.setEnabled(false);
 		finderNext.setEnabled(false);
 		console.clearSelection();
 		errorItem.setWidth(finderError.computeSize(-1, -1).x);
 		finder.layout(true, true);
 	}
 	
 	private void showSelection(int start, int end) {
 		finderError.setVisible(false);
 		finderPrev.setEnabled(true);
 		finderNext.setEnabled(true);
 		finderPos.x = start;
 		finderPos.y = end;
 		console.setSelection(finderPos.x, finderPos.y, true);
 	}
 	
 	private void showSelection(Point point) {
 		showSelection(point.x, point.y);
 	}
 	
 }
