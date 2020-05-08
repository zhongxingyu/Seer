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
 
 import static java.lang.Math.abs;
 import static java.lang.Math.max;
 import static java.lang.Math.min;
 import static org.oobium.console.ConsoleImages.COPY;
 import static org.oobium.console.ConsoleImages.PASTE;
 import static org.oobium.utils.StringUtils.blank;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.dnd.TransferData;
 import org.eclipse.swt.events.MenuAdapter;
 import org.eclipse.swt.events.MenuEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Slider;
 import org.oobium.console.commands.ClearCommand;
 import org.oobium.console.functions.ClearSelectionFunction;
 import org.oobium.console.functions.CopyFunction;
 import org.oobium.console.functions.PasteFunction;
 import org.oobium.console.functions.SelectAllFunction;
 
 
 public class Console extends Composite {
 
 	public enum CaretType { Line, Block, Underline }
 	
 	private class ConsoleLayout extends Layout {
 		@Override
 		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
 			int w = getColumnCount() * charSize.x;
 			int h = getRowCount() * charSize.y;
 			return new Point(w, h);
 		}
 		@Override
 		protected void layout(Composite composite, boolean flushCache) {
 			Rectangle area = composite.getClientArea();
 
 			Point size = computeSize(composite, -1, -1, false);
 			int hbarH = hbar.computeSize(-1, -1).y;
 			int vbarW = vbar.computeSize(-1, -1).x;
 			
 			Point newSize = new Point(area.width, area.height);
 			if(size.y > area.height) {
 				if(size.x > area.width - vbarW) {
 					hbar.setVisible(true);
 					vbar.setVisible(true);
 					hbar.setBounds(area.x, area.y+area.height-hbarH, area.width-hbarH, hbarH);
 					vbar.setBounds(area.x+area.width-vbarW+1, area.y-2, vbarW+3, area.height-hbarH+3);
 					newSize.x -= vbarW-3;
 					newSize.y -= hbarH;
 				} else {
 					hbar.setVisible(false);
 					vbar.setVisible(true);
 					hbar.setBounds(0, 0, 0, 0);
 					vbar.setBounds(area.x+area.width-vbarW+1, area.y-2, vbarW+3, area.height+4);
 					newSize.x -= vbarW-3;
 				}
 			} else if(size.x > area.width) {
 				if(size.y > area.height - hbarH) {
 					hbar.setVisible(true);
 					vbar.setVisible(true);
 					hbar.setBounds(area.x, area.y+area.height-hbarH, area.width-hbarH, hbarH);
 					vbar.setBounds(area.x+area.width-vbarW+1, area.y-2, vbarW+3, area.height-vbarW+7);
 					newSize.x -= vbarW-3;
 					newSize.y -= hbarH;
 				} else {
 					hbar.setVisible(true);
 					vbar.setVisible(false);
 					hbar.setBounds(area.x, area.y+area.height-hbarH, area.width, hbarH);
 					vbar.setBounds(0, 0, 0, 0);
 					newSize.y -= hbarH;
 				}
 			} else {
 				hbar.setVisible(false);
 				vbar.setVisible(false);
 				hbar.setBounds(0, 0, 0, 0);
 				vbar.setBounds(0, 0, 0, 0);
 			}
 			
 			canvas.setBounds(area.x, area.y, newSize.x, newSize.y);
 			canvas.moveAbove(null);
 		}
 	}
 
 	
 	Canvas canvas;
 	private Slider hbar;
 	private Slider vbar;
 	private volatile boolean scrollLock;
 	private volatile boolean hasFocus;
 
 	private boolean readOnly;
 	
 	private Selection selection;
 	private Point mouseDown;
 	private Segment mouseOver;
 	private boolean wholeWordSel;
 	
 	private StringBuilder command;
 	private Point commandSel;
 	private List<String> commandHistory;
 	private int commandHistoryPos;
 	
 	private CaretType caret;
 	private Thread caretTimer;
 	private boolean paintCaret;
 	private boolean paintSelectionBorder;
 	private volatile boolean updateUI;
 
 	Font fontNormal;
 	Font fontBold;
 	Font fontBoldItalic;
 	Font fontItalic;
 	private Point charSize;
 	Buffer buffer;
 	private Map<Integer, Region> regions;
 
 	public final Region defNormal;
 	public final Region defError;
 	public final Region defLink;
 	
 	private Command rootCommand;
 	private Function[] functions;
 	private long lastAutoCompleteRequest;
 	ContentAssist contentAssist;
 
 	private final Clipboard clipboard;
 	
 	public final ConsoleReader in;
 	public final ConsoleWriter out;
 	public final ConsoleErrorWriter err;
 	
 	private final ExecutorService executor;
 
 	private Future<?> commandRunner;
 	
 	private volatile boolean echoOffFirstPrint;
 	
 	Resources resources;
 
 
 	public Console(Composite parent, int style) {
 		super(parent, style);
 		
 		readOnly = (style & SWT.READ_ONLY) != 0;
 
 		if(readOnly) {
 			executor = null;
 			in = null;
 		} else {
 			executor = Executors.newFixedThreadPool(1);
 			in = new ConsoleReader();
 		}
 		
 		out = new ConsoleWriter(this);
 		err = new ConsoleErrorWriter(this);
 		
 		setLayout(new ConsoleLayout());
 
 		canvas = new Canvas(this, SWT.DOUBLE_BUFFERED);
 		hbar = new Slider(this, SWT.HORIZONTAL);
 		hbar.setVisible(false);
 		hbar.addListener(SWT.MouseUp, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				canvas.forceFocus();
 			}
 		});
 		vbar = new Slider(this, SWT.VERTICAL);
 		vbar.setVisible(false);
 		vbar.addListener(SWT.MouseUp, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				canvas.forceFocus();
 			}
 		});
 		
 		String bundleName = Resources.class.getPackage().getName() + ".console";
 		resources = new Resources();
 		resources.putStrings(bundleName, getClass().getClassLoader());
 		
 		clipboard = new Clipboard(getDisplay());
 		createContextMenu();
 		
 		canvas.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_IBEAM));
 		canvas.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
 		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
 
 		selection = new Selection();
 		commandSel = new Point(0, 0);
 		
 		if(readOnly) {
 			buffer = new Buffer("", 1000);
 		} else {
 			command = new StringBuilder();
 			commandHistory = new ArrayList<String>(500);
 			commandHistoryPos = 0;
 			
 			caret = CaretType.Line;
 			
 			buffer = new Buffer("oobium$ ", 1000);
 		}
 
 		String fontName = "gtk".equals(SWT.getPlatform()) ? "monospace" : "courier new";
 		setFont(fontName, 10);
 
 		regions = new HashMap<Integer, Region>();
 
 		defNormal = new Region(Region.NORMAL | Region.BLACK);
 		defError = new Region(Region.NORMAL | Region.RED);
 		defLink = new Region(Region.NORMAL | Region.DARK_BLUE | Region.UNDERLINE);
 		
 		addFunctions();
 		
 		Listener listener = new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				switch(event.type) {
 				case SWT.Dispose:	dispose();				break;
 				case SWT.FocusIn:	handleFocusIn(event);	break;
 				case SWT.FocusOut:	handleFocusOut(event);	break;
 				case SWT.KeyDown:	handleKeyDown(event);	break;
 				case SWT.MouseDoubleClick: handleMouseDoubleClick(event); break;
 				case SWT.MouseDown:	handleMouseDown(event);	break;
 				case SWT.MouseMove:	handleMouseMove(event);	break;
 				case SWT.MouseUp:	handleMouseUp(event);	break;
 				case SWT.MouseWheel:handleMouseWheel(event);break;
 				case SWT.Paint:		handlePaint(event);		break;
 				case SWT.Resize:	updateScrollBars();		break;
 				case SWT.Traverse:	handleTraverse(event);	break;
 				}
 			}
 		};
 
 		int[] types = new int[] { SWT.Dispose, SWT.FocusIn, SWT.FocusOut, SWT.KeyDown, 
 									SWT.MouseDoubleClick, SWT.MouseDown, SWT.MouseMove, SWT.MouseUp, SWT.MouseWheel, 
 									SWT.Paint, SWT.Resize, SWT.Traverse };
 		for(int type : types) {
 			canvas.addListener(type, listener);
 		}
 		
 		hbar.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				canvas.redraw();
 			}
 		});
 		
 		vbar.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				canvas.redraw();
 			}
 		});
 
 		if(!readOnly) {
 			createAndStartCaretTimer();
 		}
 		
 		createAndStartPaintTimer();
 	}
 	
 	public void addFunction(Function function) {
 		if(functions == null) {
 			functions = new Function[1];
 			functions[0] = function;
 		} else {
 			functions = Arrays.copyOf(functions, functions.length+1);
 			functions[functions.length-1] = function;
 		}
 		function.console = this;
 	}
 	
 	private void addFunctions() {
 		if(readOnly) {
 			functions = new Function[3];
 			functions[0] = new ClearSelectionFunction();
 			functions[1] = new CopyFunction();
 			functions[2] = new SelectAllFunction();
 		} else {
 			functions = new Function[4];
 			functions[0] = new ClearSelectionFunction();
 			functions[1] = new CopyFunction();
 			functions[2] = new PasteFunction();
 			functions[3] = new SelectAllFunction();
 		}
 		for(Function f : functions) {
 			f.console = this;
 		}
 	}
 
 	public void addResourceStrings(ResourceBundle resourceBundle) {
 		resources.putStrings(resourceBundle);
 	}
 	
 	public void addResourceStrings(String bundleName, ClassLoader classLoader) {
 		resources.putStrings(bundleName, classLoader);
 	}
 	
 	public void capture() {
 		captureErr();
 		captureOut();
 	}
 	
 	public void captureErr() {
 		if(!(System.err instanceof ConsolePrintStream)) {
 			System.setErr(new ConsolePrintStream(err));
 		}
 	}
 
 	public void captureOut() {
 		if(!(System.out instanceof ConsolePrintStream)) {
 			System.setOut(new ConsolePrintStream(out));
 		}
 	}
 	
 	private void checkReadOnly() {
 		if(readOnly) {
 			throw new UnsupportedOperationException("operation is not supported when in Read Only mode");
 		}
 	}
 
 	public void clear() {
 		if(!isCommandRunning()) {
 			if(!readOnly) {
 				setCommand(null);
 			}
 			buffer.clear();
 			updateScrollBars();
 			canvas.redraw();
 		}
 	}
 	
 	public void clearCommandSelection() {
 		commandSel.y = 0;
 	}
 
 	public void clearFunctions() {
 		functions = null;
 	}
 	
 	public void clearSelection() {
 		resetLineSelection();
 		clearCommandSelection();
 		canvas.redraw();
 	}
 	
 	private void commandHandleKey(char c) {
 		resetLineSelection();
 		int plen = buffer.getPrompt().length();
 		StringBuilder sb = buffer.getLast().sb;
 		switch(c) {
 		case SWT.BS: {
 			int start = 0, end = 0;
 			if(commandSel.y != 0) {
 				start = min(commandSel.x, commandSel.x+commandSel.y);
 				end = max(commandSel.x, commandSel.x+commandSel.y);
 			} else if(commandSel.x > 0) {
 				start = commandSel.x - 1;
 				end = commandSel.x;
 			}
 			command.delete(start, end);
 			sb.delete(start+plen, end+plen);
 			commandSel.x = start;
 			commandSel.y = 0;
 			break;
 		}
 		case SWT.DEL: {
 			int start = command.length(), end = command.length();
 			if(commandSel.y != 0) {
 				start = min(commandSel.x, commandSel.x+commandSel.y);
 				end = max(commandSel.x, commandSel.x+commandSel.y);
 			} else if(commandSel.x < command.length()) {
 				start = commandSel.x;
 				end = commandSel.x + 1;
 			}
 			command.delete(start, end);
 			sb.delete(start+plen, end+plen);
 			commandSel.x = start;
 			commandSel.y = 0;
 			break;
 		}
 		default: {
 			if(commandSel.y != 0) {
 				int start = min(commandSel.x, commandSel.x + commandSel.y);
 				int end = max(commandSel.x, commandSel.x + commandSel.y);
 				command.delete(start, end);
 				commandSel.x = start;
 				sb.delete(start+plen, end+plen);
 			}
 			command.insert(commandSel.x, c);
 			int pos = commandSel.x+plen;
 			if(pos < sb.length()) {
 				sb.insert(pos, c);
 			} else {
 				sb.append(c);
 			}
 			commandSel.x++;
 			commandSel.y = 0;
 		}
 		}
 		updateScrollBars();
 		scrollCommandToView();
 	}
 
 	private void commandInsert(String data) {
 		int plen = buffer.getPrompt().length();
 		StringBuilder sb = buffer.getLast().sb;
 		if(commandSel.y != 0) {
 			int start = min(commandSel.x, commandSel.x + commandSel.y);
 			int end = max(commandSel.x, commandSel.x + commandSel.y);
 			command.delete(start, end);
 			commandSel.x = start;
 			sb.delete(start+plen, end+plen);
 		}
 		String echo = in.notify(data);
 		command.insert(commandSel.x, echo);
 		sb.insert(commandSel.x+plen, echo);
 		commandSel.x += echo.length();
 		commandSel.y = 0;
 	}
 
 	private Suggestion[] complete() {
 		Suggestion[] suggestions = suggest();
 		if(suggestions.length == 1) {
 			complete(suggestions[0].getName() + " ");
 			return null;
 		} else if(suggestions.length > 0 && command.length() > 0) {
 			String name = null;
 			for(int i = 1; i < suggestions.length; i++) {
 				String name0 = suggestions[i-1].getName();
 				String name1 = suggestions[i].getName();
 				int l = 0;
 				while(l < name0.length() && l < name1.length() && name0.charAt(l) == name1.charAt(l)) {
 					l++;
 				}
 				if(l == 0) {
 					name = "";
 					break;
 				} else if(name == null) {
 					name = suggestions[i].getName().substring(0, l);
 				} else if(l < name.length()) {
 					name = name.substring(0, l);
 				}
 			}
 			if(name.length() > 0) {
 				complete(name);
 			}
 		}
 		return suggestions;
 	}
 	
 	void complete(String word) {
 		int start = (commandSel.x == command.length()) ? command.length() : commandSel.x;
 		while(start > 0) {
 			if(command.charAt(start-1) == ' ') break;
 			start--;
 		}
 		int end = commandSel.x;
 		while(end < command.length()) {
 			if(command.charAt(end) == ' ') break;
 			end++;
 		}
 		int newSel = commandSel.x + word.length() - (end - start);
 		command.replace(start, end, word);
 		setCommand(command.toString());
 		setCommandSelection(newSel);
 		scrollCommandToView();
 	}
 	
 	private boolean contentAssistRequest(Event e) {
 		if(readOnly) {
 			return false;
 		}
 		if(e.stateMask == SWT.CONTROL && e.keyCode == 32) {
 			Suggestion[] suggestions = complete();
 			if(!blank(suggestions)) {
 				contentAssist = new ContentAssist(this);
 				contentAssist.open(suggestions);
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	public void copy() {
 		String text = (commandSel.y != 0) ? getCommandSelectionText() : getSelectionText();
 		if(!blank(text)) {
 			TextTransfer textTransfer = TextTransfer.getInstance();
 			clipboard.setContents(new Object[] { text }, new Transfer[] { textTransfer });
 		}
 	}
 	
 	private void createAndStartCaretTimer() {
 		caretTimer = new Thread() {
 			public void run() {
 				while(!Console.this.isDisposed()) {
 					try {
 						Display display = Display.getDefault();
 						if(!display.isDisposed()) {
 							display.syncExec(new Runnable() {
 								@Override
 								public void run() {
 									if(!Console.this.isDisposed()) {
 										paintCaret(true);
 									}
 								}
 							});
 							sleep(1000);
 						}
 						while(!Console.this.isDisposed()) {
 							if(hasFocus) {
 								if(!display.isDisposed()) {
 									display.syncExec(new Runnable() {
 										@Override
 										public void run() {
 											if(!Console.this.isDisposed()) {
 												paintCaret(!paintCaret);
 											}
 										}
 									});
 									sleep(500);
 								}
 							} else {
 								synchronized(this) {
 									wait();
 								}
 							}
 						}
 					} catch(InterruptedException e) {
 						// start over
 					}
 				}
 			};
 		};
 		caretTimer.start();
 	}
 	
 	private void createAndStartPaintTimer() {
 		new Thread("console paint timer") {
 			public void run() {
 				while(!Console.this.isDisposed()) {
 					try {
 						if(updateUI) {
 							Display display = Display.getDefault();
 							if(!display.isDisposed()) {
 								display.syncExec(new Runnable() {
 									@Override
 									public void run() {
 										if(!Console.this.isDisposed()) {
 											updateScrollBars();
 											scrollCommandToView();
 											canvas.redraw();
 										}
 									}
 								});
 							}
 							updateUI = false;
 						}
 						sleep(100);
 					} catch(InterruptedException e) {
 						// restart
 					}
 				}
 			};
 		}.start();
 	}
 	
 	private void createContextMenu() {
 		Menu menu = new Menu(canvas);
 		canvas.setMenu(menu);
 		
 		final MenuItem copy = new MenuItem(menu, SWT.PUSH);
 		copy.setImage(COPY.getImage());
 		copy.setText("&Copy");
 		copy.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				copy();
 			}
 		});
 
 		if(readOnly) {
 			menu.addMenuListener(new MenuAdapter() {
 				@Override
 				public void menuShown(MenuEvent e) {
 					// copy
 					copy.setEnabled(hasCommandSelection() || hasLineSelection());
 				}
 			});
 		} else {
 			final MenuItem paste = new MenuItem(menu, SWT.PUSH);
 			paste.setImage(PASTE.getImage());
 			paste.setText("&Paste");
 			paste.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					paste();
 				}
 			});
 			
 			menu.addMenuListener(new MenuAdapter() {
 				@Override
 				public void menuShown(MenuEvent e) {
 					// copy
 					copy.setEnabled(hasCommandSelection() || hasLineSelection());
 					// paste
 					TransferData[] available = clipboard.getAvailableTypes();
 					boolean enabled = false;
 					for(int i = 0; i < available.length; i++) {
 						if(TextTransfer.getInstance().isSupportedType(available[i])) {
 							enabled = true;
 							break;
 						}
 					}
 					paste.setEnabled(enabled);
 				}
 			});
 		}
 	}
 	
 	@Override
 	public void dispose() {
 		if(resources != null) {
 			resources.clear();
 		}
 		disposeFonts();
 		super.dispose();
 	}
 	
 	private void disposeFonts() {
 		if(fontNormal != null && !fontNormal.isDisposed()) {
 			fontNormal.dispose();
 		}
 		if(fontBold != null && !fontBold.isDisposed()) {
 			fontBold.dispose();
 		}
 		if(fontBoldItalic != null && !fontBoldItalic.isDisposed()) {
 			fontBoldItalic.dispose();
 		}
 		if(fontItalic != null && !fontItalic.isDisposed()) {
 			fontItalic.dispose();
 		}
 	}
 
 	public void execute() {
 		resetSelection();
 		
 		if(blank(command)) {
 			if(command.length() > 0) {
 				command = new StringBuilder(20);
 			}
 			buffer.addPrompt();
 		} else {
 			buffer.addLine(0);
 			String cmd = getCommand().trim();
 			if(commandHistory.isEmpty() || !cmd.equals(commandHistory.get(commandHistory.size()-1))) {
 				commandHistory.add(cmd);
 			}
 			commandHistoryPos = commandHistory.size();
 			command = new StringBuilder(20);
 			scrollCommandToView();
 			if("clear".equals(cmd)) {
 				buffer.clear();
 			} else if(rootCommand != null) {
 				canvas.redraw();
 				commandRunner = executor.submit(new CommandRunner(rootCommand, cmd));
 				Display.getDefault().syncExec(new Runnable() {
 					@Override
 					public void run() {
 						while(isCommandRunning()) {
 							Display display = Display.getDefault();
 							if(!display.readAndDispatch()) {
 								display.sleep();
 							}
 						}
 					}
 				});
 				commandRunner = null;
 				buffer.setLastToPrompt();
 			}
 		}
 		updateScrollBars();
 		scrollCommandToView();
 		if(!scrollLock) {
 			hbar.setSelection(0);
 		}
 	}
 	
 	public void execute(String command) {
 		setCommand(command);
 		execute();
 	}
 	
 	public void execute(String command, boolean echo) {
 		if(echo) {
 			execute(command);
 			return;
 		}
 		
 		if(!blank(command)) {
 			if("clear".equals(command)) {
 				buffer.clear();
 			} else if(rootCommand != null) {
 	    		echoOffFirstPrint = true;
 				commandRunner = executor.submit(new CommandRunner(rootCommand, command));
 				Display.getDefault().syncExec(new Runnable() {
 					@Override
 					public void run() {
 						while(isCommandRunning()) {
 							Display display = Display.getDefault();
 							if(!display.readAndDispatch()) {
 								display.sleep();
 							}
 						}
 					}
 				});
 				commandRunner = null;
 				if(echoOffFirstPrint) {
 					echoOffFirstPrint = false;
 				} else {
 					buffer.setLastToPrompt();
 				}
 			}
 		}
 	}
 	
 	@Override
 	public boolean forceFocus() {
 		hasFocus = canvas.forceFocus();
 		return hasFocus;
 	}
 	
 	public Canvas getCanvas() {
 		return canvas;
 	}
 
 	/**
 	 * Returns a point describing the receiver's location relative
 	 * to its parent (or its display if its parent is null).
 	 * <p>The location of the caret is returned.</p>
 	 * @return a point, the location of the caret
 	 */
 	public Point getCaretLocation() {
 		int start = isCommandRunning() ? buffer.getLastLength() : buffer.getPrompt().length();
 		int x = charSize.x * (start + commandSel.x - hbar.getSelection());
 		int y = charSize.y * (buffer.size() - 1 - top());
 		return new Point(x, y);
 	}
 	
 	/**
 	 * Returns the character position of the caret.
 	 * <p>Indexing is zero based.</p>
 	 * @return the position of the caret
 	 */
 	public int getCaretPosition() {
 		return commandSel.x;
 	}
 	
 	public int getCharHeight() {
 		return charSize.y;
 	}
 	
 	public Point getCharSize() {
 		return new Point(charSize.x, charSize.y);
 	}
 
 	public int getCharWidth() {
 		return charSize.x;
 	}
 	
 	public int getColumnCount() {
 		return buffer.getMaxLength();
 	}
 
 	public String getCommand() {
 		return command.toString();
 	}
 	
 	public String[] getCommandHistory() {
 		return commandHistory.toArray(new String[commandHistory.size()]);
 	}
 	
 	public Point getCommandSelection() {
 		return new Point(commandSel.x, commandSel.x + commandSel.y);
 	}
 
 	public int getCommandSelectionCount() {
 		return abs(commandSel.y);
 	}
 
 	public String getCommandSelectionText() {
 		if(commandSel.y != 0) {
 			int x1 = commandSel.x;
 			int x2 = commandSel.x + commandSel.y;
 			return command.substring(min(x1, x2), max(x1, x2));
 		}
 		return "";
 	}
 
 	@Override
 	public Font getFont() {
 		return fontNormal;
 	}
 
 	public Locale getLocale() {
 		return resources.getLocale();
 	}
 	
 	public Region getRegion(int style) {
 		Region region = regions.get(style);
 		if(region == null) {
 			synchronized(regions) {
 				if(region == null) {
 					region = new Region(style);
 					regions.put(style, region);
 				}
 			}
 		}
 		return region;
 	}
 	
 	public Command getRootCommand() {
 		return rootCommand;
 	}
 	
 	public int getRowCount() {
 		return buffer.size();
 	}
 	
 	public boolean getScrollLock() {
 		return scrollLock;
 	}
 	
 	public Point getSelection() {
 		if(!selection.isValid()) {
 			return new Point(0,0);
 		}
 		
 		int x = 0, y = 0, len = 0;
 		for(int i = 0; i < buffer.size(); i++) {
 			if(i == selection.y1) {
 				x = len + selection.x1;
 			}
 			if(i == selection.y2) {
 				y = len + selection.x2;
 			}
 			len += buffer.get(i).length() + 1;
 		}
 		
 		return new Point(x, y);
 	}
 
 	public int getSelectionCount () {
 		Point selection = getSelection();
 		return abs(selection.y - selection.x);
 	}
 	
 	public String getSelectionText() {
 		return buffer.toString(selection);
 	}
 
 	private Point getSelPoint(Event e) {
 		int column = (e.x / charSize.x) + hbar.getSelection();
 		int line = (e.y / charSize.y) + top();
 		if(line >= buffer.size()) {
 			line = buffer.size()-1;
 		}
 		if(column >= buffer.get(line).length()) {
 			column = buffer.get(line).length();
 		}
 		return new Point(column, line);
 	}
 
 	public String getText() {
 		return buffer.toString();
 	}
 	
 	private void handleAutoCompleteRequest() {
 		Suggestion[] suggestions = complete();
 		if(suggestions != null && suggestions.length != 0) {
 			long time = System.currentTimeMillis();
 			if((time - lastAutoCompleteRequest) < 300) {
 				int maxWidth = canvas.getSize().x / charSize.x - 2;
 				int colWidth = 0;
 				for(Suggestion suggestion : suggestions) {
 					colWidth = Math.max(colWidth, suggestion.length() + 1);
 				}
 				colWidth += 4;
 				int numCols = maxWidth / colWidth;
 				if(numCols == 0) {
 					numCols = 1;
 				}
 				StringBuilder sb = new StringBuilder(maxWidth);
 				for(int i = 0; i < suggestions.length; i++) {
 					if(i != 0 && i % numCols == 0) {
 						buffer.addLine(sb);
 						sb = new StringBuilder(maxWidth);
 					}
 					Suggestion suggestion = suggestions[i];
 					if(suggestion.isDefault()) {
 						sb.append('*');
 					} else {
 						sb.append(' ');
 					}
 					sb.append(suggestion);
 					for(int j = suggestion.length(); j < colWidth; j++) {
 						sb.append(' ');
 					}
 				}
 				if(sb.length() > 0) {
 					buffer.addLine(sb);
 				}
 				buffer.addPrompt();
 				if(command.length() > 0) {
 					buffer.getLast().sb.append(getCommand());
 				}
 				updateScrollBars();
 				scrollCommandToView();
 				canvas.redraw();
 			}
 		}
 		lastAutoCompleteRequest = System.currentTimeMillis();
 	}
 	
 	private void handleFocusIn(Event e) {
 		if(!hasFocus) {
 			hasFocus = true;
 			if(caretTimer != null) {
 				caretTimer.interrupt();
 			}
 		}
 	}
 
 	private void handleFocusOut(Event e) {
 		hasFocus = false;
 		if(contentAssist != null) {
 			getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					if(contentAssist != null && !contentAssist.hasFocus()) {
 						contentAssist.close();
 					}
 				}
 			});
 		}
 	}
 	
 	private void handleKeyDown(Event e) {
 		switch(e.keyCode) {
 		case SWT.BS:
 			if(!readOnly && !isCommandRunning()) {
 				commandHandleKey(SWT.BS);
 			}
 			break;
 		case SWT.CR:
 			if(!readOnly) {
 				if(!isCommandRunning()) {
 					if(contentAssist != null) {
 						contentAssist.execute();
 					} else {
 						execute();
 					}
 				} else {
 					buffer.addLine();
 					in.notify('\n');
 				}
 			}
 			break;
 		case SWT.DEL:
 			if(!readOnly && !isCommandRunning()) {
 				commandHandleKey(SWT.DEL);
 			}
 			break;
 		case SWT.END:
 			if(contentAssist != null) {
 				contentAssist.handleEvent(SWT.END);
 			} else {
 				if((e.stateMask & SWT.CONTROL) != 0) {
 					vbar.setSelection(vbar.getMaximum());
 					redraw();
 				} else {
 					resetLineSelection();
 					if((e.stateMask & SWT.SHIFT) != 0) {
 						commandSel.y = command.length() - commandSel.x;
 					} else {
 						commandSel.x = command.length();
 						commandSel.y = 0;
 					}
 					hbar.setSelection(buffer.getLastLength() - (getSize().x/charSize.x) + 1);
 					vbar.setSelection(vbar.getMaximum());
 				}
 			}
 			break;
 		case SWT.ESC:
 			if(contentAssist != null && contentAssist.isOpen()) {
 				contentAssist.close();
 			} else {
 				performFunction(e);
 			}
 			break;
 		case SWT.HOME:
 			if(contentAssist != null) {
 				contentAssist.handleEvent(SWT.HOME);
 			} else {
 				if((e.stateMask & SWT.CONTROL) != 0) {
 					vbar.setSelection(vbar.getMinimum());
 					redraw();
 				} else {
 					resetLineSelection();
 					if((e.stateMask & SWT.SHIFT) != 0) {
 						commandSel.y = -commandSel.x;
 					} else {
 						commandSel.x = 0;
 						commandSel.y = 0;
 					}
 					hbar.setSelection(0);
 					vbar.setSelection(vbar.getMaximum());
 				}
 			}
 			break;
 		case SWT.PAGE_DOWN:
 			if(contentAssist != null) {
 				contentAssist.handleEvent(SWT.PAGE_DOWN);
 			} else {
 				vbar.setSelection(vbar.getSelection() + 10);
 				redraw();
 			}
 			break;
 		case SWT.PAGE_UP:
 			if(contentAssist != null) {
 				contentAssist.handleEvent(SWT.PAGE_UP);
 			} else {
 				vbar.setSelection(vbar.getSelection() - 10);
 				redraw();
 			}
 			break;
 		default:
 			if(isCommandRunning()) {
 				if(!performFunction(e) && printable(e)) {
 					e.character = in.notify(e.character);
 					buffer.getLast().sb.append(e.character);
 				}
 			} else {
 				if(!contentAssistRequest(e) && !performFunction(e) && printable(e)) {
 					resetLineSelection();
 					commandHandleKey(e.character);
 					scrollCommandToView();
 				}
 			}
 			break;
 		}
 		if(caretTimer != null) {
 			caretTimer.interrupt();
 		}
 		canvas.redraw();
 	}
 	
 	private void handleMouseDoubleClick(Event e) {
 		if(e.button == 1) {
 			wholeWordSel = true;
 			setSelection(mouseDown, getSelPoint(e));
 			canvas.redraw();
 		}
 	}
 	
 	private void handleMouseDown(Event e) {
 		if(e.button == 1) {
 			mouseDown = getSelPoint(e);
 			wholeWordSel = false;
 			commandSel.y = 0;
 			selection.x1 = selection.y1 = selection.x2 = selection.y2 = -1;
 			if(mouseDown.y == buffer.size() - 1 && mouseDown.x > buffer.getPrompt().length() - 1) {
 				commandSel.x = mouseDown.x - buffer.getPrompt().length();
 				if(caretTimer != null) {
 					caretTimer.interrupt();
 				}
 			}
 			canvas.redraw();
 			Segment seg = buffer.getSegment(mouseDown);
 			if(seg != null) {
 				Region def = seg.getDefintion(defNormal);
 				if(def.hasListeners(SWT.Selection)) {
 					def.notifyListeners(SWT.Selection, e, seg);
 				}
 			}
 		} else if(e.button == 2) {
 			String data = getSelectionText();
 			if(!blank(data)) {
 				commandInsert(data);
 				canvas.redraw();
 			}
 		}
 	}
 
 	private void handleMouseMove(Event e) {
 		if(mouseDown != null) {
 			commandSel.y = 0;
 			setSelection(mouseDown, getSelPoint(e));
 			canvas.redraw();
 		} else {
 			Point point = new Point((e.x / charSize.x) + hbar.getSelection(), (e.y / charSize.y) + top());
 			Segment seg = buffer.getSegment(point);
 			if(seg == null) {
 				canvas.setCursor(null);
 				if(mouseOver != null) {
 					Region mouseOverDef = mouseOver.getDefintion(defNormal);
 					if(mouseOverDef.hasListeners(SWT.MouseExit)) {
 						mouseOverDef.notifyListeners(SWT.MouseExit, e, mouseOver);
 					}
 					mouseOver = null;
 				}
 			} else {
 				Region def = seg.getDefintion(defNormal);
 				if(def.hasListeners(SWT.Selection)) {
 					canvas.setCursor(e.display.getSystemCursor(SWT.CURSOR_HAND));
 				} else {
 					canvas.setCursor(null);
 				}
 				if(seg != mouseOver) {
 					if(mouseOver != null) {
 						Region mouseOverDef = mouseOver.getDefintion(defNormal);
 						if(mouseOverDef.hasListeners(SWT.MouseExit)) {
 							mouseOverDef.notifyListeners(SWT.MouseExit, e, seg);
 						}
 					}
 					if(def.hasListeners(SWT.MouseEnter)) {
 						def.notifyListeners(SWT.MouseEnter, e, seg);
 					}
 					mouseOver = seg;
 				}
 			}
 		}
 	}
 	
 	private void handleMouseUp(Event e) {
 		mouseDown = null;
 	}
 	
 	private void handleMouseWheel(Event e) {
 		if(e.count > 0) {
 			vbar.setSelection(vbar.getSelection() - 1);
 		} else {
 			vbar.setSelection(vbar.getSelection() + 1);
 		}
 		canvas.redraw();
 	}
 	
 	private void handlePaint(Event e) {
 		Point size = getSize();
 
 		// paint the background of the read-only selection area
 		if(hasLineSelection()) {
 			Selection selection = new Selection();
 			selection.x1 = this.selection.x1 - hbar.getSelection();
 			selection.y1 = this.selection.y1 - vbar.getSelection();
 			selection.x2 = this.selection.x2 - hbar.getSelection();
 			selection.y2 = this.selection.y2 - vbar.getSelection();
 //			e.gc.setAlpha(128);
 			e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_LIST_SELECTION));
 			if(selection.y1 == selection.y2) {
 				e.gc.fillRectangle(selection.x1 * charSize.x, selection.y1 * charSize.y, (selection.x2 - selection.x1) * charSize.x, charSize.y);
 			} else {
 				if((selection.y2 - selection.y1) == 1 && selection.x2 < selection.x1) {
 					e.gc.fillRectangle(selection.x1 * charSize.x, selection.y1 * charSize.y, size.x+1, charSize.y);
 					e.gc.fillRectangle(-1, selection.y2 * charSize.y, selection.x2 * charSize.x, charSize.y);
 				} else {
 					e.gc.fillRectangle(selection.x1 * charSize.x, selection.y1 * charSize.y, size.x+1, charSize.y);
 					e.gc.fillRectangle(-1, (selection.y1+1) * charSize.y, size.x+1, (selection.y2-selection.y1-1) * charSize.y);
 					e.gc.fillRectangle(-1, selection.y2 * charSize.y, (selection.x2 * charSize.x) + 1, charSize.y);
 				}
 			}
 			e.gc.setAlpha(255);
 		}
 		
 		// paint all text lines except the last (which is the command line)
 		int xorigin = -(hbar.getSelection() * charSize.x);
 		Point pos = new Point(xorigin, 0);
 		int last = buffer.size() - 1;
 		for(int i = top(); i < last; i++) {
 			for(Segment seg : buffer.lines[i]) {
 				String txt = seg.toString();
 				int width = txt.length() * charSize.x;
 				Region type = seg.getDefintion(defNormal);
 				e.gc.setFont(type.font(this));
 				e.gc.setForeground(type.color(e.display));
 				e.gc.drawString(txt, pos.x, pos.y, true);
 				if(type.underline()) {
 					e.gc.drawLine(pos.x, pos.y + charSize.y - 3, pos.x + width, pos.y + charSize.y - 3);
 				}
 				pos.x += width;
 			}
 			pos.x = xorigin;
 			pos.y += charSize.y;
 		}
 		
 		// paint the command line
 		e.gc.setFont(defNormal.font(this));
 		e.gc.setForeground(defNormal.color(e.display));
 		String txt = buffer.getLast().toString();
 		if(commandSel.y == 0) {
 			e.gc.drawText(txt, pos.x, pos.y, true);
 		} else {
 			int start = min(commandSel.x, commandSel.x + commandSel.y) + buffer.getPrompt().length();
 			int end = max(commandSel.x, commandSel.x + commandSel.y) + buffer.getPrompt().length();
 
 			e.gc.drawText(txt.substring(0, start), pos.x, pos.y, true);
 			
 			e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_BLACK));
 			e.gc.fillRectangle(pos.x + (start * charSize.x), pos.y, abs(commandSel.y) * charSize.x, charSize.y);
 			
 			e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_WHITE));
 			e.gc.drawText(txt.substring(start, end), pos.x + (start * charSize.x), pos.y, true);
 
 			if(end < txt.length()) {
 				e.gc.setForeground(defNormal.color(e.display));
 				e.gc.drawText(txt.substring(end), pos.x + (end * charSize.x), pos.y, true);
 			}
 		}
 		
 		// paint the border of the read-only selection area
 		if(paintSelectionBorder && hasLineSelection()) {
 			Selection selection = new Selection();
 			selection.x1 = this.selection.x1 - hbar.getSelection();
 			selection.y1 = this.selection.y1 - vbar.getSelection();
 			selection.x2 = this.selection.x2 - hbar.getSelection();
 			selection.y2 = this.selection.y2 - vbar.getSelection();
 			e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
 			if(selection.y1 == selection.y2) {
 				e.gc.drawRectangle(selection.x1 * charSize.x, selection.y1 * charSize.y, (selection.x2 - selection.x1) * charSize.x, charSize.y);
 			} else {
 				if((selection.y2 - selection.y1) == 1 && selection.x2 < selection.x1) {
 					e.gc.drawRectangle(selection.x1 * charSize.x, selection.y1 * charSize.y, size.x-1, charSize.y);
 					e.gc.drawRectangle(0, selection.y2 * charSize.y, selection.x2 * charSize.x, charSize.y);
 				} else {
 					e.gc.drawLine(selection.x1 * charSize.x, selection.y1 * charSize.y, size.x, selection.y1 * charSize.y);
 					e.gc.drawLine(selection.x1 * charSize.x, selection.y1 * charSize.y, selection.x1 * charSize.x, (selection.y1 + 1) * charSize.y);
 					e.gc.drawLine(0, (selection.y1 + 1) * charSize.y, selection.x1 * charSize.x, (selection.y1 + 1) * charSize.y);
 					e.gc.drawLine(0, (selection.y1 + 1) * charSize.y, 0, (selection.y2 + 1) * charSize.y);
 					e.gc.drawLine(size.x-1, selection.y1 * charSize.y, size.x-1, selection.y2 * charSize.y);
 					e.gc.drawLine(0, (selection.y2 + 1) * charSize.y, selection.x2 * charSize.x, (selection.y2 + 1) * charSize.y);
 					e.gc.drawLine(selection.x2 * charSize.x, (selection.y2 + 1) * charSize.y, selection.x2 * charSize.x, selection.y2 * charSize.y);
 					e.gc.drawLine(selection.x2 * charSize.x, selection.y2 * charSize.y, size.x, selection.y2 * charSize.y);
 				}
 			}
 		} else {
 			paintCaret(e);
 		}
 	}
 
 	private void handleTraverse(Event e) {
 		resetLineSelection();
 		switch(e.keyCode) {
 		case SWT.ARROW_DOWN:
 			if(contentAssist != null) {
 				contentAssist.handleEvent(SWT.ARROW_DOWN);
 			} else {
 				if((e.stateMask & SWT.CONTROL) != 0) {
 					vbar.setSelection(vbar.getSelection() + 1);
 					redraw();
 				} else {
 					setCommand(1);
 					scrollCommandToView();
 				}
 			}
 			break;
 		case SWT.ARROW_LEFT:
 			if(commandSel.x > 0) {
 				if(commandSel.y != 0 && (e.stateMask & SWT.SHIFT) == 0) {
 					commandSel.x = min(commandSel.x, commandSel.x+commandSel.y);
 					commandSel.y = 0;
 				} else {
 					int inc = incPrev(command, commandSel.x, e.stateMask);
 					commandSel.x -= inc;
 					if((e.stateMask & SWT.SHIFT) != 0) {
 						commandSel.y += inc;
 					} else {
 						commandSel.y = 0;
 					}
 				}
 				scrollCommandToView();
 			} else {
 				if((e.stateMask & SWT.SHIFT) == 0) {
 					commandSel.y = 0;
 				}
 				if(hbar.getSelection() > 0) {
 					hbar.setSelection(hbar.getSelection() - 1);
 				}
 			}
 			break;
 		case SWT.ARROW_RIGHT:
 			if(commandSel.x < command.length()) {
 				if(commandSel.y != 0 && (e.stateMask & SWT.SHIFT) == 0) {
 					commandSel.x = max(commandSel.x, commandSel.x+commandSel.y);
 					commandSel.y = 0;
 				} else {
 					int inc = incNext(command, commandSel.x, e.stateMask);
 					commandSel.x += inc;
 					if((e.stateMask & SWT.SHIFT) != 0) {
 						commandSel.y -= inc;
 					} else {
 						commandSel.y = 0;
 					}
 				}
 			} else if((e.stateMask & SWT.SHIFT) == 0) {
 				commandSel.y = 0;
 			}
 			scrollCommandToView();
 			break;
 		case SWT.ARROW_UP:
 			if(contentAssist != null) {
 				contentAssist.handleEvent(SWT.ARROW_UP);
 			} else {
 				if((e.stateMask & SWT.CONTROL) != 0) {
 					vbar.setSelection(vbar.getSelection() - 1);
 					redraw();
 				} else {
 					setCommand(-1);
 					scrollCommandToView();
 				}
 			}
 			break;
 		case SWT.TAB:
 			handleAutoCompleteRequest();
 			scrollCommandToView();
 			break;
 		}
 	}
 	
 	public boolean hasCommand() {
 		return command.length() > 0;
 	}
 	
 	private boolean hasCommandSelection() {
 		return commandSel.y != 0;
 	}
 	
 	private boolean hasLineSelection() {
 		return selection.x1 != -1 && selection.y1 != -1 && selection.x2 != -1 && selection.y2 != -1;
 	}
 	
 	private int incEnd(CharSequence s, int pos) {
 		int i = pos;
 		while(i >= 0 && i < s.length() && Character.isLetterOrDigit(s.charAt(i))) {
 			i++;
 		}
 		return i - pos;
 	}
 	
 	private int incNext(CharSequence s, int pos, int stateMask) {
 		if((stateMask & SWT.CTRL) != 0) {
 			int i = pos;
 			while(i < s.length() && !Character.isLetterOrDigit(s.charAt(i))) {
 				i++;
 			}
 			while(true) {
 				if(i >= s.length()) {
 					return s.length() - pos;
 				}
 				if(s.charAt(i) == ' ') {
 					while(i < s.length() && s.charAt(i) == ' ') {
 						i++;
 					}
 					return i - pos;
 				}
 				if(!Character.isLetterOrDigit(s.charAt(i))) {
 					return i - pos;
 				}
 				i++;
 			}
 		}
 		return 1;
 	}
 
 	private int incPrev(CharSequence s, int pos, int stateMask) {
 		if((stateMask & SWT.CTRL) != 0) {
 			int i = pos - 1;
 			if(i >= s.length()) {
 				i = s.length() - 1;
 			}
 			while(i > 0 && !Character.isLetterOrDigit(s.charAt(i))) {
 				i--;
 			}
 			while(true) {
 				if(i < 0) {
 					return pos;
 				}
 				if(!Character.isLetterOrDigit(s.charAt(i))) {
 					return pos - i - 1;
 				}
 				i--;
 			}
 		}
 		return 1;
 	}
 	
 	private int incStart(CharSequence s, int pos) {
 		int i = pos;
 		while(i >= 0 && i <= s.length()) {
 			i--;
 			if(i < 0 || !Character.isLetterOrDigit(s.charAt(i))) break;
 		}
 		int val = pos - i - 1;
 		return (val < 0) ? 0 : val;
 	}
 	
 	public boolean isCommandRunning() {
 		return commandRunner != null && !commandRunner.isDone();
 	}
 	
 	private void paintCaret(boolean paintCaret) {
 		this.paintCaret = paintCaret;
 		Point loc = getCaretLocation();
 		canvas.redraw(loc.x, loc.y, loc.x+1, loc.y + charSize.y, false);
 	}
 	
 	private void paintCaret(Event e) {
 		if(caret != null && !hasCommandSelection()) {
 			switch(caret) {
 			case Line:
 				if(hasFocus && paintCaret) {
 					e.gc.setForeground(getForeground());
 				} else {
 					e.gc.setForeground(getBackground());
 				}
 				Point loc = getCaretLocation();
 				e.gc.drawLine(loc.x, loc.y, loc.x, loc.y+charSize.y-1);
 				break;
 			case Block:
 				break;
 			case Underline:
 				break;
 			default:
 				throw new IllegalStateException("unknown caret type " + caret);
 			}
 		}
 	}
 	
     public void paste() {
 		checkReadOnly();
 		TextTransfer transfer = TextTransfer.getInstance();
 		String data = (String) clipboard.getContents(transfer);
 		if(!blank(data)) {
 			commandInsert(data);
 			resetLineSelection();
 			canvas.redraw();
 		}
 	}
 
     private boolean performFunction(Event e) {
 		if(functions != null) {
 			for(Function f : functions) {
 				if(f.stateMask == e.stateMask && f.keyCode == e.keyCode) {
 					f.execute();
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
     protected void print(Object x, Region def, boolean addNewLine) {
     	if(x instanceof Throwable) {
 			try {
 				StringWriter sw = new StringWriter();
 				PrintWriter pw = new PrintWriter(sw);
 				((Throwable) x).printStackTrace(pw);
 				pw.close();
 				String s = sw.toString().replaceAll("at (([\\.\\w\\$]+)\\.\\w+)\\(([\\.\\w]+(:\\d+))\\)", "at $1(<a href=\"open type $2$4\">$3</a>)");
 				print(s, def, addNewLine);
 			} catch(Exception ex) {
 				// discard
 			}
     	} else {
     		print(String.valueOf(x), def, addNewLine);
     	}
     }
 
 	protected synchronized void print(String x, Region def, boolean addNewLine) {
     	StringBuilder sb = buffer.getLast().sb;
     	if(sb == null) {
     		return;
     	}
     	String commandLine =  null;
     	if(!isCommandRunning() && (addNewLine/* || !blank(x)*/)) {
     		commandLine = sb.toString();
     	}
     	if(x != null) {
 	    	int start = 0;
 	    	if(commandLine != null) {
 	    		sb.delete(0, sb.length());
 	    	}
 	    	if(def != null) {
 	    		buffer.startRegion(def);
 	    	}
 	    	char[] ca = x.toCharArray();
 	    	if(echoOffFirstPrint) {
 	    		echoOffFirstPrint = false;
 	    		char[] tmp = new char[ca.length + 1];
 	    		tmp[0] = '\n';
 	    		System.arraycopy(ca, 0, tmp, 1, ca.length);
 	    		ca = tmp;
 	    	}
 	    	for(int i = 0; i < ca.length; i++) {
 	    		if(ca[i] == '\n') {
 	    			if(i != 0) {
 	    				if(i > 1 && ca[i-1] == '\r') {
 	    					sb.append(new String(ca, start, i-start-1));
 	    				} else {
 	    					sb.append(new String(ca, start, i-start));
 	    				}
 	    			}
 	    			start = i+1;
 	    			sb = buffer.addLine(20).sb;
 	    		} else if(ca[i] == '\t') {
     				sb.append(' ').append(' ');
 	    			start = i+1;
 	    		} else if(ca[i] == '<') {
 	    			if((i + 2) < ca.length && ca[i+1] == 'a' && (ca[i+2] == ' ' || ca[i+2] == '>')) {
 	    				String data = null;
 	    				sb.append(ca, start, i-start);
 	    				i += 2;
 	    				if(ca[i] != '>') {
 		    				int j = i;
 	    					while(j < ca.length) {
 	    						if(ca[j] == '"') {
     								j++;
 	    							while(j < ca.length) {
 	    								if(ca[j] == '"') break;
 	    								j++;
 	    							}
     								j++;
     								if(j >= ca.length) break;
 	    						}
 	    						if(ca[j] == '>') break;
 	    						j++;
 	    					}
 	    					if(j < ca.length) {
 		    					String href = new String(ca, i, j-i).trim();
 		    					if(href.startsWith("href=")) {
 		    						data = href.substring(5);
 		    						if(data.charAt(0) == '"') {
 		    							data = data.substring(1);
 		    						}
 		    						if(data.charAt(data.length()-1) == '"') {
 		    							data = data.substring(0, data.length()-1);
 		    						}
 		    						data = new String(data).trim();
 		    					}
 		    					i = j;
 	    					}
 	    				}
 	    				start = i+1;
 	    				buffer.startRegion(defLink).setData(data);
 	    			} else if((i + 3) < ca.length && ca[i+1] == '/' && ca[i+2] == 'a'  && ca[i+3] == '>') {
 	    				sb.append(ca, start, i-start);
 	    				i += 3;
 	    				start = i+1;
 	    				buffer.endRegion();
 	    			}
 	    		} else if(i == ca.length-1) {
     				sb.append(ca, start, ca.length-start);
 	    		}
 	    	}
 	    	buffer.endRegions();
     	}
     	if(commandLine != null) {
     		buffer.addLine(commandLine);
     	} else if(addNewLine) {
     		buffer.addLine(20);
     	}
     	
     	updateUI = true;
     }
 
 	private boolean printable(Event e) {
     	if(readOnly) {
     		return false;
     	}
 		if(e.stateMask != 0 && (e.stateMask != SWT.SHIFT)) {
 			return false;
 		}
 		switch(e.character) {
 		case SWT.BS:
 		case SWT.CR:
 		case SWT.DEL:
 		case SWT.ESC:
 		case SWT.TAB: return false;
 		}
 		switch(e.keyCode) {
 		case SWT.ALT:
 		case SWT.ARROW_DOWN:
 		case SWT.ARROW_LEFT:
 		case SWT.ARROW_RIGHT:
 		case SWT.ARROW_UP:
 		case SWT.BREAK:
 		case SWT.CTRL:
 		case SWT.END:
 		case SWT.HOME:
 		case SWT.INSERT:
 		case SWT.NUM_LOCK:
 		case SWT.PAGE_DOWN:
 		case SWT.PAGE_UP:
 		case SWT.PAUSE:
 		case SWT.PRINT_SCREEN:
 		case SWT.SCROLL_LOCK:
 		case SWT.SHIFT: return false;
 		}
 		return true;
 	}
 	
 	public void release() {
 		releaseErr();
 		releaseOut();
 	}
 	
 	public void releaseErr() {
 		if(System.err instanceof ConsolePrintStream) {
 			System.setErr(((ConsolePrintStream) System.err).getOriginal());
 		}
 	}
 
 	public void releaseOut() {
 		if(System.out instanceof ConsolePrintStream) {
 			System.setOut(((ConsolePrintStream) System.out).getOriginal());
 		}
 	}
 
 	private void resetCommandSelection() {
 		commandSel.x = commandSel.y = 0;
 	}
 	
 	private void resetLineSelection() {
 		wholeWordSel = false;
 		selection.x1 = selection.y1 = selection.x2 = selection.y2 = -1;
 	}
 	
 	private void resetSelection() {
 		resetCommandSelection();
 		resetLineSelection();
 	}
 	
 	public void scrollCommandToView() {
 		if(!scrollLock) {
 			int x = buffer.getPrompt().length() + commandSel.x;
 			if(x < hbar.getSelection()) {
 				hbar.setSelection(x);
 			} else {
 				int w = hbar.getSelection() + (canvas.getSize().x / charSize.x);
 				if(x > w) {
 					hbar.setSelection(hbar.getSelection() + x - w);
 				}
 			}
 			vbar.setSelection(vbar.getMaximum());
 		}
 	}
 
 	public void scrollSelectionToView() {
 		if(!scrollLock) {
 			if(hasLineSelection()) {
 				int x = min(selection.x1, selection.x2);
 				if(x < hbar.getSelection()) {
 					hbar.setSelection(x);
 				} else {
 					int w = hbar.getSelection() + (canvas.getSize().x / charSize.x);
 					if(x > w) {
 						hbar.setSelection(hbar.getSelection() + x - w);
 					}
 				}
 				if(selection.y1 < vbar.getSelection() || selection.y1 > vbar.getSelection() + ((canvas.getSize().y / charSize.y)-1)) {
 					vbar.setSelection(selection.y1);
 				}
 			} else {
 				scrollCommandToView();
 			}
 		}
 	}
 	
 	public void selectAll() {
 		selection.x1 = selection.y1 = 0;
 		selection.x2 = buffer.getLastLength() + command.length();
 		selection.y2 = buffer.size() - 1;
 	}
 	
 	@Override
 	public void setBackground(Color color) {
 		super.setBackground(color);
 		canvas.setBackground(color);
 	}
 	
 	private void setCommand(int offset) {
 		commandHistoryPos += offset;
 		if(commandHistoryPos < 0) {
 			commandHistoryPos = 0;
 		} else if(commandHistoryPos > commandHistory.size()) {
 			commandHistoryPos = commandHistory.size();
 		}
 		if(!commandHistory.isEmpty()) {
 			getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					String text = (commandHistoryPos >= commandHistory.size()) ? "" : commandHistory.get(commandHistoryPos);
 					setCommand(text);
 					commandSel.x = text.length();
 					commandSel.y = 0;
 					canvas.redraw();
 				}
 			});
 		}
 	}
 
 	public void setCommand(String command) {
 		checkReadOnly();
 		int plen = buffer.getPrompt().length();
 		StringBuilder sb = buffer.getLast().sb;
 		if(command == null) {
 			this.command = new StringBuilder();
 			sb.delete(plen, sb.length());
 		} else {
 			this.command = new StringBuilder(command);
 			sb.replace(plen, sb.length(), command);
 		}
 		commandSel.x = commandSel.y = 0;
 	}
 	
 	public void setCommandHistory(String...history) {
 		if(history.length > 0) {
 			if(history.length > 1000) {
 				commandHistory = new ArrayList<String>(Arrays.asList(Arrays.copyOf(history, 1000)));
 			} else {
 				commandHistory = new ArrayList<String>(Arrays.asList(history));
 			}
			commandHistoryPos = commandHistory.size();
 		} else {
 			commandHistory = new ArrayList<String>();
 			commandHistoryPos = 0;
 		}
 	}
 	
 	public void setCommandSelection(int start) {
 		setCommandSelection(start, start);
 	}
 
 	/**
 	 * Sets the selection to the range specified
 	 * by the given start and end indices.
 	 * <p>
 	 * Indexing is zero based.  The range of
 	 * a selection is from 0..N where N is
 	 * the number of characters in the command.
 	 * </p>
 	 * <p>
 	 * Start and End can be less than zero - zero
 	 * will be substituted in for any negative values.
 	 * </p>
 	 *
 	 * @param start the start of the range
 	 * @param end the end of the range
 	 */
 	public void setCommandSelection(int start, int end) {
 		commandSel.x = max(start, 0);
 		commandSel.y = max(end, 0) - commandSel.x;
 	}
 
 	public void setCommandSelection(Point selection) {
 		setCommandSelection(selection.x, selection.y);
 	}
 	
 	@Override
 	public boolean setFocus() {
 		hasFocus = canvas.setFocus();
 		return hasFocus;
 	}
 	
 	/**
 	 * Not supported. Use {@link #setFont(String, int)} instead.
 	 * @throws UnsupportedOperationException always
 	 */
 	@Override
 	public void setFont(Font font) {
 		throw new UnsupportedOperationException("not supported. use setFontSize(int) instead");
 	}
 
 	/**
 	 * Set the font for this console widget.<br/>
 	 * Calls {@link #clear()} so that there is only one font visible
 	 * at a time.
 	 * @param name the name of the font
 	 * @param size the size of the font
 	 * @throws IllegalArgumentException if the font is not fixed width
 	 */
 	public void setFont(String name, int size) {
 		Display display = getDisplay();
 
 		Font font = new Font(display, name, size, SWT.NONE);
 		
 		GC gc = new GC(getDisplay());
 		gc.setFont(font);
 		Point p1 = gc.stringExtent(" ");
 		Point p2 = gc.stringExtent("i");
 		Point p3 = gc.stringExtent("W");
 		gc.dispose();
 
 		if(p1.x != p2.x || p2.x != p3.x) {
 			throw new IllegalArgumentException(name + " is not a fixed width font");
 		}
 
 		disposeFonts();
 
 		fontNormal = font;
 		fontBold = new Font(display, name, size, SWT.BOLD);
 		fontBoldItalic = new Font(display, name, size, SWT.BOLD | SWT.ITALIC);
 		fontItalic = new Font(display, name, size, SWT.ITALIC);
 	
 		charSize = p1;
 		
 		clear();
 	}
 	
 	@Override
 	public void setForeground(Color color) {
 		super.setForeground(color);
 		canvas.setForeground(color);
 	}
 	
 	public void setPrompt(String prompt) {
 		checkReadOnly();
 		if(prompt != null) {
 			canvas.redraw();
 		}
 	}
 	
 	public void setRootCommand(Command rootCommand) {
 		rootCommand.console = this;
 		if(rootCommand.helpCommand != null) {
 			rootCommand.helpCommand.console = this;
 			rootCommand.helpCommand.configure();
 		}
 		rootCommand.configure();
 		rootCommand.add(new ClearCommand());
 		this.rootCommand = rootCommand;
 	}
 	
 	public void setScrollLock(boolean lock) {
 		this.scrollLock = lock;
 	}
 
 	public void setSelection(int start, int end) {
 		resetSelection();
 		int total = 0;
 		for(int i = 0; i < buffer.size(); i++) {
 			int len = buffer.get(i).length();
 			total += len;
 			if(selection.x1 == -1) {
 				if(total >= start) {
 					selection.x1 = start + len - total;
 					selection.y1 = i;
 				}
 			}
 			if(selection.x2 == -1) {
 				if(total >= end) {
 					selection.x2 = end + len - total;
 					selection.y2 = i;
 				}
 			}
 			total++; // line ending
 		}
 		canvas.redraw();
 	}
 
 	public void setSelection(int start, int end, boolean scrollToView) {
 		setSelection(start, end);
 		if(scrollToView) {
 			scrollSelectionToView();
 		}
 	}
 	
 	public void setSelection(Point selection) {
 		setSelection(selection.x, selection.y);
 	}
 
 	public void setSelection(Point selection, boolean scrollToView) {
 		setSelection(selection.x, selection.y);
 		if(scrollToView) {
 			scrollSelectionToView();
 		}
 	}
 	
 	private void setSelection(Point down, Point current) {
 		int bufferLen = buffer.getPrompt().length();
 		if(down.y == current.y && current.y == (buffer.size() - 1) && down.x > bufferLen-1 && current.x > bufferLen-1) {
 			selection.x1 = selection.y1 = selection.x2 = selection.y2 = -1;
 			commandSel.x = down.x - bufferLen;
 			commandSel.y = current.x - down.x;
 			if(wholeWordSel) {
 				String string = buffer.getLast().substring(bufferLen);
 				int cx = current.x - bufferLen;
 				if(commandSel.y >= 0) {
 					commandSel.x -= incStart(string, down.x - bufferLen);
 					commandSel.y = (cx - commandSel.x) + incEnd(string, cx);
 				} else {
 					commandSel.x += incEnd(string, down.x - bufferLen);
 					commandSel.y = -((commandSel.x - cx) + incStart(string, cx));
 				}
 			}
 		} else {
 			commandSel.y = 0;
 			if(down.y < current.y || (down.y == current.y && down.x < current.x)) {
 				selection.x1 = down.x;
 				selection.y1 = down.y;
 				selection.x2 = current.x;
 				selection.y2 = current.y;
 			} else {
 				selection.x1 = current.x;
 				selection.y1 = current.y;
 				selection.x2 = down.x;
 				selection.y2 = down.y;
 			}
 			if(wholeWordSel) {
 				selection.x1 -= incStart(buffer.get(selection.y1).sequence(), selection.x1);
 				selection.x2 += incEnd(buffer.get(selection.y2).sequence(), selection.x2);
 			}
 		}
 	}
 	
 	public void setText(final String text) {
 		if(!isCommandRunning()) {
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					if(!readOnly) {
 						setCommand(null);
 					}
 					buffer.clear(false);
 					try {
 						BufferedReader reader = new BufferedReader(new StringReader(text));
 						String line;
 						while((line = reader.readLine()) != null) {
 							buffer.addLine(line);
 						}
 					} catch(IOException e) {
 						// discard
 					}
 					updateScrollBars();
 					canvas.redraw();
 				}
 			});
 		}
 	}
 	
 	Suggestion[] suggest() {
 		if(command.length() == 0) {
 			return (rootCommand != null) ? rootCommand.getSuggestions() : new Suggestion[0];
 		} else if(rootCommand != null) {
 			boolean inargs = false;
 			Command cmd = rootCommand;
 			int start = 0;
 			for(int end = 0; end < commandSel.x; end++) {
 				if(command.charAt(end) == ' ') {
 					Command c = cmd.getSubCommand(new String(command.substring(start, end)));
 					if(c == null) {
 						inargs = true;
 						break;
 					} else {
 						cmd = c;
 						while(end < commandSel.x && command.charAt(end) == ' ') {
 							end++;
 						}
 						start = end;
 					}
 				}
 			}
 			String s;
 			if(inargs) {
 				s = command.substring(start);
 			} else {
 				int end = commandSel.x;
 				while(end < command.length()) {
 					if(command.charAt(end) == ' ') break;
 					end++;
 				}
 				s = command.substring(start, end);
 			}
 			if(blank(s)) {
 				return cmd.getSuggestions("");
 			} else {
 				List<Suggestion> suggestions = new ArrayList<Suggestion>();
 				for(Suggestion suggestion : cmd.getSuggestions(s)) {
 					if(suggestion.startsWith(s)) {
 						suggestions.add(suggestion);
 					}
 				}
 				return suggestions.toArray(new Suggestion[suggestions.size()]);
 			}
 		} else {
 			return new Suggestion[0];
 		}
 	}
 	
 	private int top() {
 		int top = hbar.getVisible() ? (vbar.getSelection()) : vbar.getSelection();
 		if(top < 0) top = 0;
 		return top;
 	}
 	
 	void updateScrollBars() {
 		Point size = canvas.getSize();
 		size.x = size.x / charSize.x;
 		size.y = size.y / charSize.y;
 		
 		int w = buffer.getMaxLength();
 //		if(hbar.getVisible() && vbar.getVisible()) {
 //			w += ceil((float) vbar.getSize().x / charSize.x);
 //		}
 		if(w > size.x) {
 			hbar.setMaximum(w);
 			hbar.setThumb(size.x);
 		} else {
 			hbar.setMaximum(1);
 		}
 		
 		int h = buffer.size();
 //		if(hbar.getVisible() && vbar.getVisible()) {
 //			h += ceil((float) hbar.getSize().y / charSize.y);
 //		}
 		if(h > size.y) {
 			vbar.setMaximum(h);
 			vbar.setThumb(size.y);
 		} else {
 			vbar.setMaximum(1);
 		}
 		
 		layout(true);
 	}
 	
 }
