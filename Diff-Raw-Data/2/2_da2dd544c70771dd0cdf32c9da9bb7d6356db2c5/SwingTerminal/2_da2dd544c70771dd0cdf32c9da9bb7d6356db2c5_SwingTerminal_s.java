 /**
  *
  * consoles - Java based console terminals
  * Copyright (c) 2013, Sandeep Gupta
  * 
  * http://www.sangupta/projects/consoles
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package com.sangupta.consoles.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.HeadlessException;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.swing.JFrame;
 import javax.swing.JScrollBar;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import com.sangupta.consoles.ConsolesConstants;
 import com.sangupta.consoles.core.InputKey;
 import com.sangupta.consoles.core.KeyTrapHandler;
 import com.sangupta.consoles.core.ScreenPosition;
 
 /**
  * Java Swing based terminal that uses {@link JFrame} to render the terminal.
  * 
  * @author sangupta
  *
  */
 public class SwingTerminal {
 	
 	/**
 	 * Number of default columns in a terminal
 	 */
 	private static final int DEFAULT_COLUMNS = 80;
 	
 	/**
 	 * Number of maximum columns in the buffer in a terminal
 	 */
 	private static final int MAX_DEFAULT_COLUMNS = 80;
 	
 	/**
 	 * Number of default rows in a terminal
 	 */
 	private static final int DEFAULT_ROWS = 25;
 	
 	/**
 	 * Number of maximum rows in the buffer in a terminal
 	 */
 	private static final int MAX_DEFAULT_ROWS = 200;
 	
 	/**
 	 * Default background color for a terminal
 	 */
 	private static final Color BACKGROUND_COLOR = new Color(0, 0, 0);
 	
 	/**
 	 * Default foreground color for a terminal
 	 */
 	private static final Color FOREGROUND_COLOR = new Color(192, 192, 192);
 	
 	/**
 	 * Default tab stops, 4 chars per tab
 	 */
 	private static final int TAB_STOP = 4;
 	
 	/**
 	 * Reference to the internal {@link JFrame} instance.
 	 */
 	private final JFrame hostFrame;
 	
 	/**
 	 * Reference to the internal {@link Renderer} instance. The renderer
 	 * picks up the screen view and displays in the current frame instance.
 	 * The renderer is responsible to display one screen-size of information.
 	 * This screen-view is provided by this {@link SwingTerminal} instance.
 	 * 
 	 */
 	private final Renderer renderer;
 	
 	/**
 	 * An instance of the empty character represented by a <b>SPACE</b> character
 	 * in the currently set foreground/background color.
 	 * 
 	 */
 	private final TerminalCharacter emptyCharacter;
 	
 	/**
 	 * Holds one screen-view of information for this console.
 	 * 
 	 */
 	private TerminalCharacter screenView[][];
 	
 	/**
 	 * Holds the current location of visible area in the screen
 	 */
 	private AtomicInteger screenLocationRow = new AtomicInteger(0);
 	
 	/**
 	 * Holds the current position of the cursor
 	 */
 	private final ScreenPosition cursorPosition;
 	
 	/**
 	 * Holds the list of key-strokes as they keep coming in
 	 */
 	protected final Queue<InputKey> inputKeys;
 	
 	/**
 	 * Mutex lock to make sure that only one thread changes the screen
 	 * display at one time. Used when we are writing to the terminal, 
 	 * clearing the terminal, or resizing the terminal. 
 	 */
 	private final Object CHANGE_MUTEX = new Object();
 	
 	/**
 	 * Mutex lock to make sure that only thread/one resize-event is taken
 	 * care of first. That is once, the first resize event is complete, only
 	 * then we start processing the second resize event.
 	 * 
 	 */
 	private final Object RESIZE_MUTEX = new Object();
 	
 	/**
 	 * Signals the keyboard input thread to break immediately as 
 	 * we are closing down.
 	 */
 	private boolean closingTerminal = false;
 	
 	/**
 	 * Number of rows that should be present on screen.
 	 */
 	private int numScreenRows;
 	
 	/**
 	 * Number of rows that are can be held in buffer.
 	 */
 	private int numBufferRows;
 	
 	/**
 	 * Number of columns that should be present on screen.
 	 */
 	private int numScreenColumns;
 	
 	/**
 	 * Number of columns that can be held in buffer
 	 */
 	private int numBufferColumns;
 	
 	/**
 	 * Holds the list of all shutdown hooks that have been added to this terminal.
 	 * 
 	 */
 	protected List<Runnable> shutDownHooks;
 	
 	/**
 	 * Boolean value indicating if we are done initialising and creating the
 	 * instance of this object.
 	 */
 	private boolean initialized = false;
 	
 	/**
 	 * Handles mouse interaction in this terminal instance
 	 */
 	private final MouseHandler mouseHandler;
 	
 	/**
 	 * Store all key trap handlers here
 	 */
 	private final ConcurrentMap<InputKey, List<KeyTrapHandler>> keyTrapHandlers;
 	
 	/**
 	 * Indicates whether we have added any keytraps or not
 	 */
 	private boolean hasKeyTraps = false;
 	
 	private final JScrollBar verticalScrollBar;
 	
 	private final JScrollBar horizontalScrollBar;
 	
 	/**
 	 * Default constructor - uses the default number of rows and columns
 	 * to construct and instance.
 	 * 
 	 */
 	public SwingTerminal() {
 		this(DEFAULT_COLUMNS, DEFAULT_ROWS);
 	}
 	
 	public SwingTerminal(int columns, int rows) {
 		this(columns, rows, MAX_DEFAULT_COLUMNS, MAX_DEFAULT_ROWS);
 	}
 	
 	/**
 	 * Construct an instance of Swing based terminal instance with the given
 	 * number of rows and columns. The pixel-size of the instance is obtained
 	 * by the height/width of a character in the font associated with the
 	 * renderer instance.
 	 * 
 	 * @param columns
 	 * @param rows
 	 */
 	public SwingTerminal(int columns, int rows, final int maxColumns, final int maxRows) {
 		// set system look and feel
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (InstantiationException e1) {
 			e1.printStackTrace();
 		} catch (IllegalAccessException e1) {
 			e1.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e1) {
 			e1.printStackTrace();
 		}
 		
 		// validate number of rows and columns
 		if(columns <= 0) {
 			columns = DEFAULT_COLUMNS;
 		}
 		if(rows <= 0) {
 			rows = DEFAULT_ROWS;
 		}
 		
 		this.keyTrapHandlers = new ConcurrentHashMap<InputKey, List<KeyTrapHandler>>();
 		
 		this.numScreenRows = rows;
 		this.numScreenColumns = columns;
 		this.numBufferColumns = maxColumns;
 		this.numBufferRows = maxRows;
 		
 		this.hostFrame = new JFrame();
 		BorderLayout bl = new BorderLayout();
 		this.hostFrame.setLayout(bl);
 		
 		this.emptyCharacter = new TerminalCharacter((char) 0, FOREGROUND_COLOR, BACKGROUND_COLOR);
 		
 		this.screenView = new TerminalCharacter[this.numBufferRows][this.numBufferColumns];
 
 		// initialize screen view and the buffer view
 		for(int row = 0; row < this.numScreenRows; row++) {
 			clearRow(this.screenView[row]);
 		}
 		
 		this.cursorPosition = new ScreenPosition();
 		this.renderer = new Renderer(this.numScreenColumns, this.numScreenRows, this.screenView, this.cursorPosition, this.screenLocationRow);
 		
 		this.inputKeys = new ConcurrentLinkedQueue<InputKey>();
 		
 		// initialize
 		this.hostFrame.getContentPane().add(this.renderer);
 		
 		// add scroll bars
 		this.horizontalScrollBar = new JScrollBar();
 		this.horizontalScrollBar.setOrientation(JScrollBar.HORIZONTAL);
 	    this.hostFrame.getContentPane().add(horizontalScrollBar, BorderLayout.SOUTH);
 		
 	    this.verticalScrollBar = new JScrollBar();
 	    this.verticalScrollBar.addAdjustmentListener(new AdjustmentListener() {
 			
 			@Override
 			public void adjustmentValueChanged(AdjustmentEvent event) {
 				// the user has scrolled
 				// the current start row of the display
 				// now should be the one
 				// that is event
 				final int startRow = event.getValue();
 				if(startRow != SwingTerminal.this.screenLocationRow.get()) {
 					SwingTerminal.this.renderer.scrollToPosition(startRow);
 				}
 				
 //				System.err.println("We need to scroll to another area on screen: " + startRow + "; current: " + SwingTerminal.this.screenLocationRow.get());
 //				SwingTerminal.this.screenLocationRow.set(startRow);
 			}
 			
 		});
 	    
 	    this.hostFrame.getContentPane().add(verticalScrollBar, BorderLayout.EAST);
 	    
 	    this.hostFrame.validate();
 		
 		// pack up - this is required so that
 		// rendered can compute its preferred size
 		this.hostFrame.pack();
 		
 		this.hostFrame.addKeyListener(new InputKeyListener(this.inputKeys));
 		
 		this.hostFrame.setLocationByPlatform(true);
 
 		this.hostFrame.setResizable(true);
 		this.hostFrame.setSize(this.renderer.getPreferredSize());
 		
 		this.hostFrame.pack();
 
 		// add the closing handler for the terminal
 		this.hostFrame.addWindowListener(new WindowAdapter() {
 			
 			@Override
 			public void windowClosing(WindowEvent e) {
 				closeTerminal();
 			}
 			
 		});
 		
 		this.hostFrame.setVisible(true);
 		this.hostFrame.pack();
 		
 		// compute the height of the horizontal scroll bar
 		final int hScrollHeight = this.horizontalScrollBar.getHeight();
 		final int vScrollWidth = this.verticalScrollBar.getWidth();
 		
 		// add the resize handler
 		hostFrame.addComponentListener(new ComponentAdapter() {
 			
 			@Override
 			public void componentResized(ComponentEvent e) {
 				if(!e.getComponent().isShowing()) {
 					return;
 				}
 
 				// this is the dimension of the jframe without the borders
 				Dimension dimension = ((JFrame) e.getSource()).getContentPane().getSize();
 				
 				// subtract the height and width of scroll bars as applicable
 				dimension.height -= hScrollHeight;
 				dimension.width -= vScrollWidth;
 
 				// now we need to set the size of the renderer
 				int[] size = renderer.getSizeInCharacterBlocks(dimension);
 				
 				// set the renderer to the correct size
 				resize(size[0], size[1]);
 				
 				SwingUtilities.invokeLater(new Runnable() {
 					
 					@Override
 					public void run() {
 						hostFrame.pack();
 						hostFrame.repaint();
 					}
 				});
 			}
 			
 		});
 
 		// set it only in the last
 		// a resize event may be fired before this method completes 
 		// which should be allowed to resize the JFrame as that is what
 		// not the user has asked for
 		this.initialized = true;
 		
 		// add mouse event handlers
 		this.mouseHandler = new MouseHandler(this);
 		this.renderer.addMouseListener(this.mouseHandler);
 		this.renderer.addMouseMotionListener(this.mouseHandler);
 
 		resetScrollBars();
 	}
 	
 	/**
 	 * Close this terminal and dispose of all associated resources.
 	 * 
 	 */
 	public void closeTerminal() {
 		if(this.closingTerminal) {
 			// already closed
 			return;
 		}
 		
 		// start closing
 		this.closingTerminal = true;
 
 		// call shutdown hooks
 		if(this.shutDownHooks != null && !this.shutDownHooks.isEmpty()) {
 			for(Runnable hook : this.shutDownHooks) {
 				hook.run();
 			}
 			
 			this.shutDownHooks.clear();
 		}
 		
 		// clean up objects
 		this.renderer.dispose();
 		this.hostFrame.setVisible(false);
 		this.hostFrame.dispose();
 	}
 	
 	/**
 	 * Set the title of the window being used.
 	 * 
 	 * @param title
 	 */
 	public void setTitle(String title) {
 		this.hostFrame.setTitle(title);
 	}
 	
 	/**
 	 * 
 	 * @param inputKey
 	 * @param keyTrapHandler
 	 */
 	public void addKeyTrap(InputKey inputKey, KeyTrapHandler keyTrapHandler) {
 		List<KeyTrapHandler> handlers;
 		if(this.keyTrapHandlers.containsKey(inputKey)) {
 			handlers = this.keyTrapHandlers.get(inputKey);
 		} else {
 			handlers = new ArrayList<KeyTrapHandler>();
 			this.keyTrapHandlers.put(inputKey, handlers);
 		}
 		
 		handlers.add(keyTrapHandler);
 		
 		this.hasKeyTraps = true;
 	}
 	
 	/**
 	 * Method that reads a string from the terminal and sends it back.
 	 * 
 	 */
 	public String readString(boolean echo, char mask) {
 		final StringBuilder builder = new StringBuilder();
 		
 		InputKey key;
 		while(true) {
 			key = this.getKey(false);
 			
 			if(this.closingTerminal) {
 				break;
 			}
 			
 			// ENTER
 			if(key.ch == '\n' && !key.altPressed && !key.ctrlPressed) {
 				// TODO: fix this to just move one row
 				this.write("\n");
 				break;
 			}
 			
 			// ESCAPE
 			if((int) key.ch == 27) {
 				int length = builder.length();
 				builder.setLength(0);
 				
 				// TODO: optimize this call for performance
 				for(int index = 0; index < length; index++) {
 					setRelativeChar(0, -1, ' ');
 				}
 				continue;
 			}
 			
 			// BACKSPACE
 			if(key.ch == 8) {
 				if(builder.length() > 0) {
 					builder.deleteCharAt(builder.length() - 1);
 				} else {
 					// we have deleted all chars already
 					// skip this key
 					continue;
 				}
 				
 				setRelativeChar(0, -1, ' ');
 				continue;
 			}
 			
 			// SPECIAL KEYS
 			if(key.specialKey != null) {
 				
 				switch(key.specialKey) {
 					
 					case LeftArrow:
 						this.setRelativeCursorPosition(0, -1);
 						continue;
 
 					default:
 						// do nothing
 						break;
 				}
 			}
 			
 			// all well
 			// add the character to string
 			builder.append(key.ch);
 			
 			// display on screen as needed
 			if(echo) {
 				if(mask == 0) {
 					writeChar(key.ch);
 				} else {
 					writeChar(mask);
 				}
 			}
 		}
 		
 		return builder.toString();
 	}
 	
 	/**
 	 * Set the char at position relative to current position to the given
 	 * char. This also sets the current cursor position to the given element.
 	 * 
 	 * @param rows
 	 * @param columns
 	 * @param ch
 	 */
 	void setRelativeChar(int rows, int columns, char ch) {
 		this.setRelativeCursorPosition(rows, columns);
 		this.writeChar(ch);
 		this.setRelativeCursorPosition(0, -1);
 	}
 	
 	/**
 	 * Set relative cursor position
 	 * 
 	 * @param rows
 	 * @param columns
 	 */
 	void setRelativeCursorPosition(int rows, int columns) {
 		int row = this.cursorPosition.getRow() + rows;
 		int col = this.cursorPosition.getColumn() + columns;
 		
 		while(col < 0) {
 			col = col + this.numScreenColumns;
 			row--;
 		}
 		
 		if(row < 0) {
 			return;
 		}
 		
 		this.cursorPosition.setPosition(row, col);
 		this.refresh();
 	}
 	
 	/**
 	 * Write a string to the terminal and repaint.
 	 * 
 	 * @param string
 	 */
 	public void writeString(String string) {
 		if(string == null) {
 			return;
 		}
 
 		this.write(string);
 		this.refresh();
 	}
 	
 	/**
 	 * Write a character to the terminal and repaint.
 	 * 
 	 * @param ch
 	 */
 	public void writeChar(char ch) {
 		if(ch == 0) {
 			return;
 		}
 		
 		this.write(ch);
 		this.refresh();
 	}
 	
 	/**
 	 * Write a string to the renderer. This will not repaint the
 	 * renderer. Method {@link SwingTerminal#refresh()} must be called
 	 * to explicitly make the string visible.
 	 * 
 	 * External callees should use the public methods as exposed than
 	 * using this method. 
 	 * 
 	 * @param string
 	 */
 	void write(String string) {
 		if(string == null) {
 			return;
 		}
 		
 		int length = string.length();
 		char[] chars = string.toCharArray();
 		write(chars, 0, length);
 	}
 	
 	/**
 	 * Write a set of characters to the renderer.
 	 * 
 	 * @param chars
 	 * @param offset
 	 * @param length
 	 */
 	void write(final char[] chars, final int offset, final int length) {
 		if(chars == null) {
 			return;
 		}
 		
 		char charToWrite;
 		
 		synchronized (CHANGE_MUTEX) {
 			// write in the actual row of the buffer queue
 			int row = this.cursorPosition.getRow();
 			int col = this.cursorPosition.getColumn();
 			
 			for(int index = offset; index < length; index++) {
 				charToWrite = chars[index];
 				
 				switch(charToWrite) {
 					case '\n':
 					case '\r':
 						row++;
 						col = 0;
 						break;
 						
 					case '\t':
 						// compute the number of tab stops that we need to add
 						int spaces = TAB_STOP - (col % TAB_STOP);
 						if(spaces == 1) {
 							this.screenView[row + this.screenLocationRow.get()][col++] = new TerminalCharacter(' ', FOREGROUND_COLOR, BACKGROUND_COLOR);
 						} else {
 							// TODO: optimize this to prevent recursive call
 							this.cursorPosition.setPosition(row, col);
 							String spaced = "";
 							for(int i = 0; i < spaces; i++) {
 								spaced += " ";
 							}
 							this.write(spaced);
 							
 							col = this.cursorPosition.getColumn();
 							row = this.cursorPosition.getRow() + this.screenLocationRow.get();
 						}
 						break;
 						
 					default:
 						this.screenView[row + this.screenLocationRow.get()][col++] = new TerminalCharacter(charToWrite, FOREGROUND_COLOR, BACKGROUND_COLOR);
 				}
 				
 				// check for next line
 				int[] vals = updateRowAndColumn(row, col);
 				row = vals[0];
 				col = vals[1];
 			}
 			
 			this.cursorPosition.setPosition(row, col);
 		}
 	}
 	
 	private int[] updateRowAndColumn(int row, int col) {
 		if(col == this.numScreenColumns) {
 			col = 0;
 			row++;
 		}
 		
 		// check if we need to move to next line
 		if(row == this.numScreenRows) {
 			// scroll up
 			if(col == 0) {
 				scrollUp(row);
 			}
 			
 			row--;
 		}
 		
 		return new int[] { row, col };
 	}
 
 	/**
 	 * 
 	 * @param ch
 	 */
 	void write(char ch) {
 		synchronized (CHANGE_MUTEX) {
 			// ask renderer to switch back to writing mode
 			this.renderer.switchToWritingMode();
 			
 			int col = this.cursorPosition.getColumn();
 			int row = this.cursorPosition.getRow();
 
 			this.screenView[row  + this.screenLocationRow.get()][col++] = new TerminalCharacter(ch, FOREGROUND_COLOR, BACKGROUND_COLOR);
 
 			int[] vals = updateRowAndColumn(row, col);
 			row = vals[0];
 			col = vals[1];
 			
 			this.cursorPosition.setPosition(row, col);
 		}
 	}
 	
 	/**
 	 * Method that will scroll the window up by one row. This needs to make sure
 	 * that it increments the {@link #screenLocationRow} correctly so that renderer
 	 * can render the right thing, if buffer is not filled.
 	 * 
 	 */
 	void scrollUp(int currentRow) {
 		// detect buffer overflow
 		final boolean overflow = (this.screenLocationRow.get() + this.numScreenRows) == this.numBufferRows;
 		
 		if(!overflow) {
 			
 			// buffer has not overflow
 			// we render the right place and show the cursor at the right place
 			final int scrollPosition = this.screenLocationRow.incrementAndGet();
 			this.verticalScrollBar.setValue(scrollPosition);
 			this.verticalScrollBar.validate();
 			return;
 		}
 		
 		// we have gone overboard
 		// we need to scroll all lines up
 		synchronized (CHANGE_MUTEX) {
 			for(int row = 1; row < this.numBufferRows; row++) {
 				this.screenView[row - 1] = this.screenView[row];
 			}
 
 			int row = this.numBufferRows - 1;
 			this.screenView[row] = new TerminalCharacter[this.numScreenColumns];
 			for(int index = 0; index < this.screenView[row].length; index++) {
 				this.screenView[row][index] = this.emptyCharacter.clone();
 			}
 		}
 	}
 	
 	/**
 	 * Clear this terminal. This basically means that we need to render the empty
 	 * character in the entire screen space.
 	 * 
 	 */
 	public void clearTerminal() {
 		synchronized (CHANGE_MUTEX) {
 			for(int row = 0; row < this.screenView.length; row++) {
 				clearRow(this.screenView[row]);
 			}
 			
 			this.moveCursor(0, 0);
 		}
 	}
 	
 	private void clearRow(TerminalCharacter[] row) {
 		for(int index = 0; index < row.length; index++) {
 			row[index] = this.emptyCharacter.clone();
 		}
 	}
 	
 	/**
 	 * Move the cursor to the designated position on screen.
 	 * 
 	 * @param row
 	 * @param column
 	 */
 	public void moveCursor(int row, int column) {
 		this.cursorPosition.setPosition(row, column);
 		this.refresh();
 	}
 	
 	/**
 	 * Refresh the internal renderer
 	 */
 	public void refresh() {
 		this.renderer.repaint();
 	}
 	
 	/**
 	 * Read a key in a non-blocking fashion. If no key is 
 	 * available, returns <code>null</code>.
 	 * 
 	 * @return
 	 */
 	public InputKey readKey() {
 		return this.inputKeys.poll();
 	}
 	
 	/**
 	 * Read a key blockingly. If no key is available, the thread
 	 * will wait till one is available.
 	 * 
 	 * This method will never return a <code>null</code>
 	 * 
 	 * @return
 	 */
 	public InputKey getKey() {
 		return getKey(true);
 	}
 	
 	public InputKey getKey(boolean echo) {
 		InputKey key = null;
 		while(key == null) {
 			if(this.closingTerminal) {
 				break;
 			}
 			
 			key = this.inputKeys.poll();
 		}
 		
 		// check if we have a key trap handler over this key
		if(this.hasKeyTraps) {
 			boolean hasTrap = this.keyTrapHandlers.containsKey(key);
 			if(hasTrap) {
 				List<KeyTrapHandler> handlers = this.keyTrapHandlers.get(key);
 		
 				boolean bubbleEvent = true;
 				for(KeyTrapHandler handler : handlers) {
 					bubbleEvent = handler.handleKeyInvocation(key);
 					if(!bubbleEvent) {
 						continue;
 					}
 				}
 			}
 		}
 		
 		if(echo) {
 			write(key.ch);
 		}
 		
 		return key;
 	}
 	
 	/**
 	 * Return the current screen position in terms of row and column
 	 * for the given point on screen.
 	 * 
 	 * @param point
 	 * @return
 	 */
 	public ScreenPosition getScreenPosition(Point point) {
 		return this.renderer.getScreenPosition(point);
 	}
 	
 	public void highlightBox(ScreenPosition pos) {
 		if(pos == null) {
 			return;
 		}
 		
 		this.unHighlight();
 		this.highlightBox(pos.getColumn(), pos.getRow(), pos.getColumn(), pos.getRow());
 	}
 
 	/**
 	 * Highlight the entire area on the screen as a mouse selection.
 	 * 
 	 * @param x1
 	 * @param y1
 	 * @param x2
 	 * @param y2
 	 */
 	public void highlightBox(int x1, int y1, int x2, int y2) {
 		for(int row = 0; row < y1; row++) {
 			for(int col = 0; col < this.numScreenColumns; col++) {
 				this.screenView[row][col].highlighted = false;
 			}
 		}
 		for(int row = y2 + 1; row < this.numScreenRows; row++) {
 			for(int col = 0; col < this.numScreenColumns; col++) {
 				this.screenView[row][col].highlighted = false;
 			}
 		}
 		for(int col = 0; col < x1; col++) {
 			for(int row = 0; row < this.numScreenRows; row++) {
 				this.screenView[row][col].highlighted = false;
 			}
 		}
 		for(int col = x2 + 1; col < this.numScreenColumns; col++) {
 			for(int row = 0; row < this.numScreenRows; row++) {
 				this.screenView[row][col].highlighted = false;
 			}
 		}
 		
 		// now highlight the box
 		for(int row = y1; row <= y2; row++) {
 			for(int col = x1; col <= x2; col++) {
 				this.screenView[row][col].highlighted = true;
 			}
 		}
 	}
 
 	/**
 	 * Un-highlight the entire screen area of any previous mouse selection.
 	 * 
 	 */
 	public void unHighlight() {
 		for(int row = 0; row < this.numScreenRows; row++) {
 			for(int col = 0; col < this.numScreenColumns; col++) {
 				this.screenView[row][col].highlighted = false;
 			}
 		}
 	}
 
 	/**
 	 * Resize the JFrame to a new size so that users can alter the size based on their
 	 * needs.
 	 * 
 	 * @param newRows
 	 * @param newColumns
 	 */
 	public void resize(final int newRows, final int newColumns) {
 		if(!this.initialized) {
 			return;
 		}
 		
 		if(newRows <= 0) {
 			throw new IllegalArgumentException("Number of rows cannot be less than or equal to zero.");
 		}
 		
 		if(newColumns <= 0) {
 			throw new IllegalArgumentException("Number of rows cannot be less than or equal to zero.");
 		}
 		
 		synchronized (RESIZE_MUTEX) {
 			if(newRows == this.numScreenRows && newColumns == this.numScreenColumns) {
 				// there is nothing to do - we are the same size
 				return;
 			}
 
 			if(newRows < this.numScreenRows || newColumns < this.numScreenColumns) {
 				// TODO: we currently do not support shrinking of the frame
 				// TODO: this needs to be fixed for scroll bars
 				
 				this.hostFrame.setSize(this.renderer.getPreferredSize());
 				return;
 			}
 			
 			// initialize the new screen view
 			// create a new screen view only if needed
 			boolean bufferChanged = false;
 			
 			TerminalCharacter newScreenView[][] = null;
 			int rows = Math.max(newRows, this.numBufferRows);
 			int cols = Math.max(newColumns, this.numBufferColumns);
 			if(rows > this.numBufferRows || cols > this.numBufferColumns) {
 				newScreenView = new TerminalCharacter[rows][cols];
 				for(int row = 0; row < newRows; row++) {
 					clearRow(newScreenView[row]);
 				}
 				
 				bufferChanged = true;
 			}
 			
 			synchronized (CHANGE_MUTEX) {
 				if(bufferChanged) {
 					// fill this array with the current data
 					for(int row = 0; row < this.numBufferRows; row++) {
 						for(int col = 0; col < this.numScreenColumns; col++) {
 							newScreenView[row][col] = this.screenView[row][col];
 						}
 					}
 					
 					// set the properties
 					this.screenView = newScreenView;
 					
 					this.numBufferRows = rows;
 					this.numScreenColumns = cols;
 				}
 				
 				this.numScreenRows = newRows;
 				this.numScreenColumns = newColumns;
 				
 				// update the scroll bars
 				resetScrollBars();
 
 				// rebuild the renderer
 				this.renderer.resizeRenderer(newScreenView, newRows, newColumns);
 			}
 			
 			// start the re-rendering process
 			this.refresh();
 		}
 	}
 
 	private void resetScrollBars() {
 		// add scroll bar handlers
 		// suppose we can display a max of 1000 rows
 		this.horizontalScrollBar.setValues(0, this.numScreenColumns, 0, this.numBufferColumns);
 		this.verticalScrollBar.setValues(0, this.numScreenRows, 0, this.numBufferRows);
 
 		this.verticalScrollBar.setValue(this.screenLocationRow.get());
 	}
 
 	/**
 	 * Add a shutdown hook to this terminal instance.
 	 * 
 	 * @param runnable
 	 */
 	public void addShutdownHook(Runnable runnable) {
 		if(this.closingTerminal) {
 			throw new IllegalStateException("We are already closing this terminal");
 		}
 		
 		if(this.shutDownHooks == null) {
 			this.shutDownHooks = new ArrayList<Runnable>(); 
 		}
 		
 		this.shutDownHooks.add(runnable);
 	}
 	
 	/**
 	 * Handle paste action from clipboard
 	 */
 	protected void processPasteAction() {
 		String data = null;
 		try {
 			data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
 		} catch (HeadlessException e) {
 			// eat up
 		} catch (UnsupportedFlavorException e) {
 			// eat up
 		} catch (IOException e) {
 			// eat up
 		} 
 		
 		if(data == null) {
 			return;
 		}
 		
 		// paste it at current location
 		char[] chars = data.toCharArray();
 		for(char ch : chars) {
 			this.inputKeys.add(new InputKey(ch));
 		}
 	}
 
 	/**
 	 * Read the text inside the given bounding box.
 	 * 
 	 * @param x1
 	 * @param y1
 	 * @param x2
 	 * @param y2
 	 * @return
 	 */
 	public void copyTextToClipboard(int x1, int y1, int x2, int y2) {
 		StringBuilder builder = new StringBuilder();
 		
 		for(int row = y1; row <= y2; row++) {
 			for(int col = x1; col <= x2; col++) {
 				builder.append(this.screenView[row][col].character);
 			}
 			
 			if(y1 != y2) {
 				builder.append(ConsolesConstants.NEW_LINE);
 			}
 		}
 		
 		String text = builder.toString();
 		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
 	}
 
 }
