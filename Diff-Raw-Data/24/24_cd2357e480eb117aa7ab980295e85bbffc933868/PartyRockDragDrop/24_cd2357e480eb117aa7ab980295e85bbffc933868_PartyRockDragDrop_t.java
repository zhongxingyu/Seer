 package com.partyrock.temp;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.graphics.Region;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 /**
  * Drag and drop demo without rewritten code
  * @author Matthew
  * 
  */
 public class PartyRockDragDrop implements KeyListener {
 
 	private final Display display;
 	private Shell shell;
 	private int CHANNEL_WIDTH = 700;
 	static TableItem itemBeingDragged;
 
 	public PartyRockDragDrop() {
 		this.display = new Display();
 		this.shell = new Shell(display);
 		shell.setText("Sample Party Rock GUI");
 		// may need to change this
 		shell.setLayout(new FillLayout());
 
 		final Table table = new Table(shell, SWT.MULTI | SWT.FULL_SELECTION);
 		table.setHeaderVisible(false);
 		table.setLinesVisible(false);
 		table.setToolTipText("");
 		table.addKeyListener(this);
 
 		int columnCount = 2;
 		for (int a = 0; a < columnCount; a++) {
 			TableColumn column = new TableColumn(table, SWT.NONE);
 			column.setText("Column");
 		}
 
 		int rowCount = 20;
 		for (int i = 0; i < rowCount; i++) {
 			TableItem item = new TableItem(table, SWT.NONE);
 			item.setText(new String[] { (i < rowCount / 2) ? "Lights " + i
 					: ("Laser " + (i - rowCount / 2)) });
 		}
 
 		table.addListener(SWT.MeasureItem, new Listener() {
 			public void handleEvent(Event event) {
 				// Custom height
 				event.height = 50;
 				if (event.index == 1) {
 					// render column
 					event.width = CHANNEL_WIDTH;
 				}
 			}
 		});
 
 		table.addListener(SWT.EraseItem, new Listener() {
 			public void handleEvent(Event event) {
 
 				event.detail &= ~SWT.HOT;
 
 				// get the color based on if it's selected
 				Color channelColor = ((event.detail & SWT.SELECTED) != 0) ? display.getSystemColor(SWT.COLOR_BLUE)
 						: display.getSystemColor(SWT.COLOR_RED);
 
 				GC gc = event.gc;
 				// Area of the entire visible table
 				Rectangle area = table.getClientArea();
 
 				// Clipping
 				int width = area.x + area.width - event.x;
 
 				if (width < 0) {
 					// fix for drag and drop, where this can become < 0
 					width = 0;
 				}
 				Region region = new Region();
 				gc.getClipping(region);
				region.add(event.x, event.y, width, event.height + 1);
 				gc.setClipping(region);
 				region.dispose();
 
 				Rectangle rect = event.getBounds();
 				Color origBack = gc.getBackground();
 
 				// start rendering
 
 				if (event.index == 1) {
 					gc.setBackground(channelColor);
 					gc.fillRectangle(event.x, rect.y, CHANNEL_WIDTH, rect.height);
 				}
 
 				gc.setForeground(display.getSystemColor(event.index == 0 ? SWT.COLOR_BLACK
 						: SWT.COLOR_GREEN));
 				gc.drawLine(event.x, rect.y + rect.height - 1, event.x
 						+ CHANNEL_WIDTH, rect.y + rect.height - 1);
 
 				gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
 
 				// end rendering
 
 				gc.setBackground(origBack);
 
 				event.detail &= ~SWT.SELECTED;
 			}
 		});
 
 		for (int i = 0; i < table.getColumnCount(); i++) {
 			table.getColumn(i).pack();
 		}
 
 		table.setSelection(table.getItem(0));
 		shell.setSize(700, 500);
 
 		/* Start DnD */
 
 		table.addListener(SWT.DragDetect, new Listener() {
 			public void handleEvent(Event event) {
 				TableItem item = table.getItem(new Point(event.x, event.y));
 				if (item == null)
 					return;
 				itemBeingDragged = item;
 			}
 		});
 		table.addListener(SWT.MouseMove, new Listener() {
 			public void handleEvent(Event event) {
 				if (itemBeingDragged == null)
 					return;
 //				TableItem item = table.getItem(new Point(event.x, event.y));
 //				table.setInsertMark(item, true);
 			}
 		});
 		table.addListener(SWT.MouseUp, new Listener() {
 			public void handleEvent(Event event) {
 				if (itemBeingDragged == null)
 					return;
 				TableItem item = table.getItem(new Point(event.x, event.y));
 				if (item != null && item != itemBeingDragged) {
 					TableItem[] items = table.getItems();
 
 					int index = -1;
 					for (int i = 0; i < items.length; i++) {
 						if (items[i] == item) {
 							index = i;
 						}
 					}
 
 					// index is now the insertion index
 					if (index != -1) {
 						// This shouldn't fail ever but ya never know
 						TableItem[] selected = table.getSelection();
						TableItem[] newSelected = new TableItem[selected.length];
 						for (int x = 0; x < selected.length; x++) {
 							TableItem sel = selected[x];
							// Make the new TableItem
 							TableItem newSel = new TableItem(table, SWT.NONE, index
 									+ x);
							newSelected[x] = newSel;
 							// Copy columns
 							for (int i = 0; i < table.getColumnCount(); i++) {
 								newSel.setText(i, sel.getText(i));
 							}
						}
						// Remove the old selected items
						for (TableItem sel : selected) {
 							sel.dispose();
 						}
						// Select the new items
						table.setSelection(newSelected);
 					}

 				}
 //				table.setInsertMark(null, false);
 				itemBeingDragged = null;
 			}
 		});
 
 		/* End DnD */
 
 		shell.open();
 
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 
 		display.dispose();
 
 	}
 
 	public static void main(String... args) {
 		new PartyRockDragDrop();
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		switch (e.keyCode) {
 		case SWT.ESC:
 			exit();
 			break;
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void exit() {
 		shell.dispose();
 	}
 
 }
