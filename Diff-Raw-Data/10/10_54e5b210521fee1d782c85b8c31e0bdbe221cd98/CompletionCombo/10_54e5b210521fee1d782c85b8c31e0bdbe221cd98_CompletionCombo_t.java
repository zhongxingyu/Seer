 /*******************************************************************************
  * Copyright (c) 2000, 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation (CCombo)
  *     compeople AG    - adjustments for autocompletion
  *******************************************************************************/
 package org.eclipse.riena.ui.swt;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Widget;
 
import org.eclipse.riena.core.util.RAPDetector;
 import org.eclipse.riena.ui.swt.facades.ClipboardFacade;
 import org.eclipse.riena.ui.swt.facades.SWTFacade;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * TODO [ev] docs
  * 
  * The CompletionCombo class represents a selectable user interface object that
  * combines a text field and a list and issues notification when an item is
  * selected from the list. The list will automatically pop-up when the text
  * widget is focused and the user is typing.
  * <p>
  * CompletionCombo was written to work around certain limitations in the native
  * combo box. There is no is no strict requirement that CompletionCombo look or
  * behave the same as the native combo box.
  * <p>
  * Note that although this class is a subclass of <code>Composite</code>, it
  * does not make sense to add children to it, or set a layout on it.
  * <dl>
  * <dt><b>Styles:</b>
  * <dd>BORDER, READ_ONLY, FLAT</dd>
  * <dt><b>Events:</b>
  * <dd>DefaultSelection, Modify, Selection, Verify</dd>
  * </dl>
  * 
  * @since 2.0
  */
 public abstract class CompletionCombo extends Composite {
 
 	private static final SWTFacade SWT_FACADE = SWTFacade.getDefault();
 	/**
 	 * Stores all allowed input characters that are not letters or digits. This
 	 * data is computed as items are added to the CompletionCombo and used in
 	 * {@link #isInputChar(char)}.
 	 */
 	private final Set<Character> inputChars = new HashSet<Character>();
 
 	private Label label;
 	private Text text;
 	private Control list;
 	private int visibleItemCount = 5;
 	private Shell popup;
 	private Button arrow;
 	private boolean hasFocus;
 	private Listener listener, filter;
 	private Color listForeground, listBackground;
 	private Font font;
 	private Shell _shell;
 	private boolean autoCompletion;
 	private AutoCompletionMode autoCompletionMode;
 	private IFlashDelegate flashDelegate;
 
 	/**
 	 * This enumeration is used to configure the the way the autocompletion
 	 * works.
 	 */
 	public enum AutoCompletionMode {
 		/**
 		 * The Combo accepts all typed words and and just stops tracking the
 		 * list entries if no match is found.
 		 */
 		ALLOW_MISSMATCH,
 		/**
 		 * The Combo rejects typed characters that would make the String in the
 		 * textfield not match any of the entries in the list.
 		 */
 		NO_MISSMATCH
 	}
 
 	/**
 	 * Constructs a new instance of this class given its parent and a style
 	 * value describing its behavior and appearance.
 	 * <p>
 	 * The style value is either one of the style constants defined in class
 	 * <code>SWT</code> which is applicable to instances of this class, or must
 	 * be built by <em>bitwise OR</em>'ing together (that is, using the
 	 * <code>int</code> "|" operator) two or more of those <code>SWT</code>
 	 * style constants. The class description lists the style constants that are
 	 * applicable to the class. Style bits are also inherited from superclasses.
 	 * </p>
 	 * 
 	 * @param parent
 	 *            a widget which will be the parent of the new instance (cannot
 	 *            be null)
 	 * @param style
 	 *            the style of widget to construct
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the parent</li>
 	 *                </ul>
 	 * 
 	 * @see SWT#BORDER
 	 * @see SWT#READ_ONLY
 	 * @see SWT#FLAT
 	 * @see Widget#getStyle()
 	 */
 	protected CompletionCombo(final Composite parent, int style) {
 		super(parent, style = checkStyle(style));
 		_shell = super.getShell();
 
 		int textStyle = SWT.SINGLE;
 		if ((style & SWT.READ_ONLY) != 0) {
 			textStyle |= SWT.READ_ONLY;
 		}
 		if ((style & SWT.FLAT) != 0) {
 			textStyle |= SWT.FLAT;
 		}
 		label = createLabel(this);
 		text = new Text(this, textStyle);
 		text.setBackground(getBackground());
 		int arrowStyle = SWT.ARROW | SWT.DOWN;
 		if ((style & SWT.FLAT) != 0) {
 			arrowStyle |= SWT.FLAT;
 		}
 		arrow = new Button(this, arrowStyle);
 
 		listener = new Listener() {
 			public void handleEvent(final Event event) {
 				if (isDisposed()) {
 					return;
 				}
 				if (popup == event.widget) {
 					popupEvent(event);
 					return;
 				}
 				if (text == event.widget) {
 					textEvent(event);
 					return;
 				}
 				if (list == event.widget) {
 					listEvent(event);
 					return;
 				}
 				if (arrow == event.widget) {
 					arrowEvent(event);
 					return;
 				}
 				if (CompletionCombo.this == event.widget) {
 					comboEvent(event);
 					return;
 				}
 				if (getShell() == event.widget) {
 					getDisplay().asyncExec(new Runnable() {
 						public void run() {
 							if (isDisposed()) {
 								return;
 							}
 							handleFocus(SWT.FocusOut);
 						}
 					});
 				}
 			}
 		};
 		filter = new Listener() {
 			public void handleEvent(final Event event) {
 				if (isDisposed()) {
 					return;
 				}
 				final Shell shell = ((Control) event.widget).getShell();
 				if (shell == CompletionCombo.this.getShell()) {
 					if (event.type == SWT.MouseDown && !isClickedInCombo()) {
 						dropDown(false);
 						selectAll();
 					} else {
 						handleFocus(SWT.FocusOut);
 					}
 				}
 			}
 
 			private boolean isClickedInCombo() {
 				final Point point = toControl(getDisplay().getCursorLocation());
 				final Point size = getSize();
 				final Rectangle rect = new Rectangle(0, 0, size.x, size.y);
 				return rect.contains(point);
 			}
 		};
 
 		final int[] comboEvents = { SWT.Dispose, SWT.FocusIn, SWT.FocusOut, SWT.Move, SWT.Resize };
 		for (final int comboEvent : comboEvents) {
 			this.addListener(comboEvent, listener);
 		}
 
 		final int[] textEvents = { SWT.DefaultSelection, SWT.DragDetect, SWT.KeyDown, SWT.KeyUp, SWT.MenuDetect,
 				SWT.Modify, SWT.MouseDown, SWT.MouseUp, SWT.MouseDoubleClick, SWTFacade.MouseEnter,
 				SWTFacade.MouseExit, SWTFacade.MouseHover, SWTFacade.MouseMove, SWTFacade.MouseWheel, SWT.Traverse,
 				SWT.FocusIn, SWT.Verify };
 		for (final int textEvent : textEvents) {
 			text.addListener(textEvent, listener);
 		}
 
 		final int[] arrowEvents = { SWT.DragDetect, SWT.MouseDown, SWTFacade.MouseEnter, SWTFacade.MouseExit,
 				SWTFacade.MouseHover, SWTFacade.MouseMove, SWT.MouseUp, SWTFacade.MouseWheel, SWT.Selection,
 				SWT.FocusIn };
 		for (final int arrowEvent : arrowEvents) {
 			arrow.addListener(arrowEvent, listener);
 		}
 
 		createPopup(null, null, -1);
 		autoCompletion = true;
 		autoCompletionMode = AutoCompletionMode.NO_MISSMATCH;
 	}
 
 	/**
 	 * TODO [ev] docs
 	 * 
 	 * @since 3.0
 	 */
 	protected abstract Label createLabel(final Composite parent);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract Control createList(final Composite parent);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract void deselectAll(Control list);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract Image getImage(Control list, int index);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract String getItem(Control list, int index);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract int getItemHeight(Control list);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract Image[] getImages(Control list);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract String[] getItems(Control list);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract int getItemCount(Control list);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract int getSelectionIndex(Control list);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract int indexOf(Control list, String string, int start);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract void removeAll(final Control list);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract void setItems(Control list, String[] items, Image[] images);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract void setSelection(Control list, int index);
 
 	/**
 	 * @since 3.0
 	 */
 	protected abstract void setTopIndex(Control list, int index);
 
 	/**
 	 * @since 3.0
 	 */
 	protected Button getButtonControl() {
 		return arrow;
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected Control getListControl() {
 		return list;
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected Text getTextControl() {
 		return text;
 	}
 
 	static int checkStyle(final int style) {
 		final int mask = SWT.BORDER | SWT.READ_ONLY | SWT.FLAT | SWT.LEFT_TO_RIGHT | SWTFacade.RIGHT_TO_LEFT;
 		return SWT.NO_FOCUS | (style & mask);
 	}
 
 	/**
 	 * Adds the listener to the collection of listeners who will be notified
 	 * when the receiver's text is modified, by sending it one of the messages
 	 * defined in the <code>ModifyListener</code> interface.
 	 * 
 	 * @param listener
 	 *            the listener which should be notified
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 * 
 	 * @see ModifyListener
 	 * @see #removeModifyListener
 	 */
 	public abstract void addModifyListener(final ModifyListener listener);
 
 	/**
 	 * Adds the listener to the collection of listeners who will be notified
 	 * when the user changes the receiver's selection, by sending it one of the
 	 * messages defined in the <code>SelectionListener</code> interface.
 	 * <p>
 	 * <code>widgetSelected</code> is called when the combo's list selection
 	 * changes. <code>widgetDefaultSelected</code> is typically called when
 	 * ENTER is pressed the combo's text area.
 	 * </p>
 	 * 
 	 * @param listener
 	 *            the listener which should be notified when the user changes
 	 *            the receiver's selection
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 * 
 	 * @see SelectionListener
 	 * @see #removeSelectionListener
 	 * @see SelectionEvent
 	 */
 	public abstract void addSelectionListener(final SelectionListener listener);
 
 	/**
 	 * Adds the listener to the collection of listeners who will be notified
 	 * when the receiver's text is verified, by sending it one of the messages
 	 * defined in the <code>VerifyListener</code> interface.
 	 * 
 	 * @param listener
 	 *            the listener which should be notified
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 * 
 	 * @see VerifyListener
 	 * @see #removeVerifyListener
 	 */
 	public abstract void addVerifyListener(final VerifyListener listener);
 
 	void arrowEvent(final Event event) {
 		switch (event.type) {
 		case SWT.FocusIn:
 			handleFocus(SWT.FocusIn);
 			break;
 		case SWT.DragDetect:
 		case SWT.MouseDown:
 		case SWT.MouseUp:
 		case SWTFacade.MouseMove:
 		case SWTFacade.MouseEnter:
 		case SWTFacade.MouseExit:
 		case SWTFacade.MouseHover:
 			Point pt = getDisplay().map(arrow, this, event.x, event.y);
 			event.x = pt.x;
 			event.y = pt.y;
 			notifyListeners(event.type, event);
 			event.type = SWT.None;
 			break;
 		case SWTFacade.MouseWheel:
 			pt = getDisplay().map(arrow, this, event.x, event.y);
 			event.x = pt.x;
 			event.y = pt.y;
 			notifyListeners(SWTFacade.MouseWheel, event);
 			event.type = SWT.None;
 			if (isDisposed()) {
 				break;
 			}
 			if (!event.doit) {
 				break;
 			}
 			if (event.count != 0) {
 				event.doit = false;
 				final int oldIndex = getSelectionIndex();
 				if (event.count > 0) {
 					select(Math.max(oldIndex - 1, 0));
 				} else {
 					select(Math.min(oldIndex + 1, getItemCount() - 1));
 				}
 				if (oldIndex != getSelectionIndex()) {
 					final Event e = new Event();
 					e.time = event.time;
 					e.stateMask = event.stateMask;
 					notifyListeners(SWT.Selection, e);
 				}
 				if (isDisposed()) {
 					break;
 				}
 			}
 			break;
 		case SWT.Selection:
 			text.setFocus();
 			dropDown(!isDropped());
 			break;
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * Sets the selection in the receiver's text field to an empty selection
 	 * starting just before the first character. If the text field is editable,
 	 * this has the effect of placing the i-beam at the start of the text.
 	 * <p>
 	 * Note: To clear the selected items in the receiver's list, use
 	 * <code>deselectAll()</code>.
 	 * </p>
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 * 
 	 * @see #deselectAll
 	 */
 	public void clearSelection() {
 		checkWidget();
 		text.clearSelection();
 		deselectAll(list);
 	}
 
 	void comboEvent(final Event event) {
 		switch (event.type) {
 		case SWT.Dispose:
 			removeListener(SWT.Dispose, listener);

			// FIXME: Workaround for RAP bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=328043
			// RAP throws a ClassCastException, when notifyListeners is called
			if (!RAPDetector.isRAPavailable()) {
				notifyListeners(SWT.Dispose, event);
			}

 			event.type = SWT.None;
 
 			if (popup != null && !popup.isDisposed()) {
 				list.removeListener(SWT.Dispose, listener);
 				popup.dispose();
 			}
 			final Shell shell = getShell();
 			shell.removeListener(SWT.Deactivate, listener);
 			final Display display = getDisplay();
 			display.removeFilter(SWT.FocusIn, filter);
 			popup = null;
 			text = null;
 			list = null;
 			arrow = null;
 			_shell = null;
 			break;
 		case SWT.FocusIn:
 			final Control focusControl = getDisplay().getFocusControl();
 			if (focusControl == arrow || focusControl == list) {
 				return;
 			}
 			if (isAutoCompletion()) {
 				text.setFocus();
 			} else {
 				if (isDropped()) {
 					list.setFocus();
 				} else {
 					text.setFocus();
 				}
 			}
 			break;
 		case SWT.FocusOut:
 			if (isDropped()) {
 				dropDown(false);
 			}
 			break;
 		case SWT.Move:
 			dropDown(false);
 			break;
 		case SWT.Resize:
 			internalLayout(false);
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
 		checkWidget();
 		int width = 0, height = 0;
 		final String[] items = getItems(list);
 		final GC gc = new GC(text);
 		final int spacer = gc.stringExtent(" ").x; //$NON-NLS-1$
 		int textWidth = gc.stringExtent(text.getText()).x;
 		for (final String item : items) {
 			textWidth = Math.max(gc.stringExtent(item).x, textWidth);
 		}
 		gc.dispose();
 		final Point labelSize = label != null ? label.computeSize(SWT.DEFAULT, SWT.DEFAULT) : new Point(0, 0);
 		final Point textSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
 		final Point arrowSize = arrow.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
 		final Point listSize = list.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
 		final int borderWidth = getBorderWidth();
 
 		height = Math.max(textSize.y, arrowSize.y);
 		height = Math.max(height, labelSize.y);
 		width = Math.max(textWidth + 2 * spacer + arrowSize.x + 2 * borderWidth, listSize.x);
 		if (label != null) {
 			width += labelSize.x + (2 * spacer);
 		}
 		if (wHint != SWT.DEFAULT) {
 			width = wHint;
 		}
 		if (hHint != SWT.DEFAULT) {
 			height = hHint;
 		}
 		return new Point(width + 2 * borderWidth, height + 2 * borderWidth);
 	}
 
 	// TODO [ev] removed - document in migration guide
 	//	/**
 	//	 * Copies the selected text.
 	//	 * <p>
 	//	 * The current selection is copied to the clipboard.
 	//	 * </p>
 	//	 * 
 	//	 * @exception SWTException
 	//	 *                <ul>
 	//	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	//	 *                disposed</li>
 	//	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	//	 *                thread that created the receiver</li>
 	//	 *                </ul>
 	//	 */
 	//	public void copy() {
 	//		checkWidget();
 	//		text.copy();
 	//	}
 
 	void createPopup(final String[] items, final Image[] images, final int selectionIndex) {
 		// create shell and list
 		popup = new Shell(getShell(), SWT.NO_TRIM | SWT.ON_TOP);
 		list = createList(popup);
 		if (font != null) {
 			list.setFont(font);
 		}
 		if (listForeground != null) {
 			list.setForeground(listForeground);
 		}
 		if (listBackground != null) {
 			list.setBackground(listBackground);
 		}
 
 		final int[] popupEvents = { SWT.Close, SWTFacade.Paint, SWT.Deactivate };
 		for (final int popupEvent : popupEvents) {
 			popup.addListener(popupEvent, listener);
 		}
 		final int[] listEvents = { SWT.MouseUp, SWT.Selection, SWT.Traverse, SWT.KeyDown, SWT.KeyUp, SWT.FocusIn,
 				SWT.Dispose };
 		for (final int listEvent : listEvents) {
 			list.addListener(listEvent, listener);
 		}
 
 		if (items != null) {
 			setItems(list, items, images);
 		}
 		if (selectionIndex != -1) {
 			setSelection(list, selectionIndex);
 		}
 	}
 
 	/**
 	 * Deselects the item at the given zero-relative index in the receiver's
 	 * list. If the item at the index was already deselected, it remains
 	 * deselected. Indices that are out of range are ignored.
 	 * 
 	 * @param index
 	 *            the index of the item to deselect
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public void deselect(final int index) {
 		checkWidget();
 		if (0 <= index && index < getItemCount(list) && index == getSelectionIndex(list)
 				&& text.getText().equals(getItem(list, index))) {
 			clearImage();
 			text.setText(""); //$NON-NLS-1$
 			deselectAll(list);
 		}
 	}
 
 	/**
 	 * Deselects all selected items in the receiver's list.
 	 * <p>
 	 * Note: To clear the selection in the receiver's text field, use
 	 * <code>clearSelection()</code>.
 	 * </p>
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 * 
 	 * @see #clearSelection
 	 */
 	public void deselectAll() {
 		checkWidget();
 		clearImage();
 		text.setText(""); //$NON-NLS-1$
 		deselectAll(list);
 	}
 
 	void dropDown(final boolean drop) {
 		if (drop == isDropped()) {
 			return;
 		}
 		if (!drop) {
 			popup.setVisible(false);
 			if (!isDisposed() && isFocusControl()) {
 				text.setFocus();
 			}
 			return;
 		}
 		if (!isVisible()) {
 			return;
 		}
 		if (getShell() != popup.getParent()) {
 			final String[] items = getItems(list);
 			final Image[] images = getImages(list);
 			final int selectionIndex = getSelectionIndex(list);
 			list.removeListener(SWT.Dispose, listener);
 			popup.dispose();
 			popup = null;
 			list = null;
 			createPopup(items, images, selectionIndex);
 		}
 
 		final Point size = getSize();
 		int itemCount = getItemCount(list);
 		itemCount = (itemCount == 0) ? visibleItemCount : Math.min(visibleItemCount, itemCount);
 		final int itemHeight = getItemHeight(list) * itemCount;
 		final Point listSize = list.computeSize(SWT.DEFAULT, itemHeight, false);
 		list.setBounds(1, 1, Math.max(size.x - 2, listSize.x), listSize.y);
 
 		final int index = getSelectionIndex(list);
 		if (index != -1) {
 			setTopIndex(list, index);
 		}
 		final Display display = getDisplay();
 		final Rectangle listRect = list.getBounds();
 		final Rectangle parentRect = display.map(getParent(), null, getBounds());
 		final Point comboSize = getSize();
 		final Rectangle displayRect = getMonitor().getClientArea();
 		final int width = Math.max(comboSize.x, listRect.width + 2);
 		final int height = listRect.height + 2;
 		int x = parentRect.x;
 		int y = parentRect.y + comboSize.y;
 		if (y + height > displayRect.y + displayRect.height) {
 			y = parentRect.y - height;
 		}
 		if (x + width > displayRect.x + displayRect.width) {
 			x = displayRect.x + displayRect.width - listRect.width;
 		}
 		popup.setBounds(x, y, width, height);
 		popup.setVisible(true);
 		if (isFocusControl()) {
 			if (isAutoCompletion()) {
 				text.setFocus();
 			} else {
 				list.setFocus();
 			}
 		}
 	}
 
 	/**
 	 * Returns the background color of the Combo's List widget.
 	 * 
 	 * @return a Color instance
 	 * @since 3.0
 	 */
 	public Color getListBackground() {
 		return listBackground != null ? listBackground : getBackground();
 	}
 
 	/**
 	 * Returns the background color of the Combo's Text widget.
 	 * 
 	 * @return a Color instance
 	 * @since 3.0
 	 */
 	public Color getTextBackground() {
 		return text.getBackground();
 	}
 
 	/**
 	 * Gets the editable state.
 	 * 
 	 * @return whether or not the receiver is editable
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public boolean getEditable() {
 		checkWidget();
 		return text.getEditable();
 	}
 
 	/**
 	 * Returns the item at the given, zero-relative index in the receiver's
 	 * list. Throws an exception if the index is out of range.
 	 * 
 	 * @param index
 	 *            the index of the item to return
 	 * @return the item at the given index
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_INVALID_RANGE - if the index is not between 0
 	 *                and the number of elements in the list minus 1 (inclusive)
 	 *                </li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public String getItem(final int index) {
 		checkWidget();
 		return getItem(list, index);
 	}
 
 	/**
 	 * Returns the number of items contained in the receiver's list.
 	 * 
 	 * @return the number of items
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public int getItemCount() {
 		checkWidget();
 		return getItemCount(list);
 	}
 
 	/**
 	 * Returns the height of the area which would be used to display
 	 * <em>one</em> of the items in the receiver's list.
 	 * 
 	 * @return the height of one item
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public int getItemHeight() {
 		checkWidget();
 		return getItemHeight(list);
 	}
 
 	/**
 	 * Returns an array of <code>String</code>s which are the items in the
 	 * receiver's list.
 	 * <p>
 	 * Note: This is not the actual structure used by the receiver to maintain
 	 * its list of items, so modifying the array will not affect the receiver.
 	 * </p>
 	 * 
 	 * @return the items in the receiver's list
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public String[] getItems() {
 		checkWidget();
 		return getItems(list);
 	}
 
 	/**
 	 * Returns <code>true</code> if the receiver's list is visible, and
 	 * <code>false</code> otherwise.
 	 * <p>
 	 * If one of the receiver's ancestors is not visible or some other condition
 	 * makes the receiver not visible, this method may still indicate that it is
 	 * considered visible even though it may not actually be showing.
 	 * </p>
 	 * 
 	 * @return the receiver's list's visibility state
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public boolean getListVisible() {
 		checkWidget();
 		return isDropped();
 	}
 
 	@Override
 	public Menu getMenu() {
 		return text.getMenu();
 	}
 
 	/**
 	 * Returns a <code>Point</code> whose x coordinate is the start of the
 	 * selection in the receiver's text field, and whose y coordinate is the end
 	 * of the selection. The returned values are zero-relative. An "empty"
 	 * selection as indicated by the the x and y coordinates having the same
 	 * value.
 	 * 
 	 * @return a point representing the selection start and end
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public Point getSelection() {
 		checkWidget();
 		return text.getSelection();
 	}
 
 	/**
 	 * Returns the zero-relative index of the item which is currently selected
 	 * in the receiver's list, or -1 if no item is selected.
 	 * 
 	 * @return the index of the selected item
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public int getSelectionIndex() {
 		checkWidget();
 		return getSelectionIndex(list);
 	}
 
 	@Override
 	public Shell getShell() {
 		checkWidget();
 		final Shell shell = super.getShell();
 		if (shell != _shell) {
 			if (_shell != null && !_shell.isDisposed()) {
 				_shell.removeListener(SWT.Deactivate, listener);
 			}
 			_shell = shell;
 		}
 		return _shell;
 	}
 
 	@Override
 	public int getStyle() {
 		int style = super.getStyle();
 		style &= ~SWT.READ_ONLY;
 		if (!text.getEditable()) {
 			style |= SWT.READ_ONLY;
 		}
 		return style;
 	}
 
 	/**
 	 * Returns a string containing a copy of the contents of the receiver's text
 	 * field.
 	 * 
 	 * @return the receiver's text
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public String getText() {
 		checkWidget();
 		return text.getText();
 	}
 
 	/**
 	 * Returns the height of the receivers's text field.
 	 * 
 	 * @return the text height
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public int getTextHeight() {
 		checkWidget();
 		return text.getLineHeight();
 	}
 
 	/**
 	 * Returns the maximum number of characters that the receiver's text field
 	 * is capable of holding. If this has not been changed by
 	 * <code>setTextLimit()</code>, it will be the constant
 	 * <code>Combo.LIMIT</code>.
 	 * 
 	 * @return the text limit
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public int getTextLimit() {
 		checkWidget();
 		return text.getTextLimit();
 	}
 
 	/**
 	 * Gets the number of items that are visible in the drop down portion of the
 	 * receiver's list.
 	 * 
 	 * @return the number of items that are visible
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public int getVisibleItemCount() {
 		checkWidget();
 		return visibleItemCount;
 	}
 
 	void handleFocus(final int type) {
 		switch (type) {
 		case SWT.FocusIn:
 			if (hasFocus) {
 				return;
 			}
 			if (getEditable()) {
 				text.selectAll();
 			}
 			hasFocus = true;
 			Shell shell = getShell();
 			shell.removeListener(SWT.Deactivate, listener);
 			shell.addListener(SWT.Deactivate, listener);
 			Display display = getDisplay();
 			display.removeFilter(SWT.FocusIn, filter);
 			display.addFilter(SWT.FocusIn, filter);
 			display.removeFilter(SWT.MouseDown, filter);
 			display.addFilter(SWT.MouseDown, filter);
 			Event e = new Event();
 			notifyListeners(SWT.FocusIn, e);
 			break;
 		case SWT.FocusOut:
 			if (!hasFocus) {
 				return;
 			}
 			final Control focusControl = getDisplay().getFocusControl();
 			if (focusControl == arrow || focusControl == list || focusControl == text) {
 				return;
 			}
 			hasFocus = false;
 			shell = getShell();
 			shell.removeListener(SWT.Deactivate, listener);
 			display = getDisplay();
 			display.removeFilter(SWT.FocusIn, filter);
 			display.removeFilter(SWT.MouseDown, filter);
 			e = new Event();
 			notifyListeners(SWT.FocusOut, e);
 			break;
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * Searches the receiver's list starting at the first item (index 0) until
 	 * an item is found that is equal to the argument, and returns the index of
 	 * that item. If no item is found, returns -1.
 	 * 
 	 * @param string
 	 *            the search item
 	 * @return the index of the item
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public int indexOf(final String string) {
 		checkWidget();
 		if (string == null) {
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		}
 		return indexOf(list, string, 0);
 	}
 
 	/**
 	 * Searches the receiver's list starting at the given, zero-relative index
 	 * until an item is found that is equal to the argument, and returns the
 	 * index of that item. If no item is found or the starting index is out of
 	 * range, returns -1.
 	 * 
 	 * @param string
 	 *            the search item
 	 * @param start
 	 *            the zero-relative index at which to begin the search
 	 * @return the index of the item
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public int indexOf(final String string, final int start) {
 		checkWidget();
 		if (string == null) {
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		}
 		return indexOf(list, string, start);
 	}
 
 	/**
 	 * Returns true, when autocompletion is on. The default value is true.
 	 * 
 	 * @return true if autocompletion is on, false otherwise
 	 */
 	public boolean isAutoCompletion() {
 		return autoCompletion;
 	}
 
 	/**
 	 * TODO [ev] docs
 	 * 
 	 * @return
 	 * @since 3.0
 	 */
 	protected boolean isDropped() {
 		return !SwtUtilities.isDisposed(popup) && popup.getVisible();
 	}
 
 	@Override
 	public boolean isFocusControl() {
 		checkWidget();
 		if (text.isFocusControl() || arrow.isFocusControl() || list.isFocusControl() || popup.isFocusControl()) {
 			return true;
 		}
 		return super.isFocusControl();
 	}
 
 	void internalLayout(final boolean changed) {
 		if (isDropped()) {
 			dropDown(false);
 		}
 		final Rectangle rect = getClientArea();
 		final int width = rect.width;
 		final int height = rect.height;
 		final Point arrowSize = arrow.computeSize(SWT.DEFAULT, height, changed);
 		if (label != null) {
 			final Point labelSize = arrow.computeSize(16, height, changed);
 			labelSize.x += 3;
 			label.setBounds(3, 0, labelSize.x - 3, height);
 			text.setBounds(labelSize.x, 0, width - arrowSize.x - labelSize.x, height);
 			arrow.setBounds(width - arrowSize.x, 0, arrowSize.x, arrowSize.y);
 		} else {
 			text.setBounds(0, 0, width - arrowSize.x, height);
 			arrow.setBounds(width - arrowSize.x, 0, arrowSize.x, arrowSize.y);
 		}
 	}
 
 	void listEvent(final Event event) {
 		switch (event.type) {
 		case SWT.Dispose:
 			if (getShell() != popup.getParent()) {
 				final String[] items = getItems(list);
 				final Image[] images = getImages(list);
 				final int selectionIndex = getSelectionIndex(list);
 				popup = null;
 				list = null;
 				createPopup(items, images, selectionIndex);
 			}
 			break;
 		case SWT.FocusIn:
 			handleFocus(SWT.FocusIn);
 			break;
 		case SWT.MouseUp:
 			if (event.button != 1) {
 				return;
 			}
 			dropDown(false);
 			break;
 		case SWT.Selection:
 			final int index = getSelectionIndex(list);
 			if (index == -1) {
 				return;
 			}
 			setImage(index);
 			text.setText(getItem(list, index));
 			text.selectAll();
 			setSelection(list, index);
 			Event e = new Event();
 			e.time = event.time;
 			e.stateMask = event.stateMask;
 			e.doit = event.doit;
 			notifyListeners(SWT.Selection, e);
 			event.doit = e.doit;
 			break;
 		case SWT.Traverse:
 			switch (event.detail) {
 			case SWT.TRAVERSE_RETURN:
 			case SWT.TRAVERSE_ESCAPE:
 			case SWTFacade.TRAVERSE_ARROW_PREVIOUS:
 			case SWTFacade.TRAVERSE_ARROW_NEXT:
 				event.doit = false;
 				break;
 			case SWT.TRAVERSE_TAB_NEXT:
 			case SWT.TRAVERSE_TAB_PREVIOUS:
 				event.doit = SWT_FACADE.traverse(text, event.detail);
 				event.detail = SWT.TRAVERSE_NONE;
 				if (event.doit) {
 					dropDown(false);
 				}
 				return;
 			default:
 				break;
 			}
 			e = new Event();
 			e.time = event.time;
 			e.detail = event.detail;
 			e.doit = event.doit;
 			e.character = event.character;
 			e.keyCode = event.keyCode;
 			SWT_FACADE.copyEventKeyLocation(event, e);
 			notifyListeners(SWT.Traverse, e);
 			event.doit = e.doit;
 			event.detail = e.detail;
 			break;
 		case SWT.KeyUp:
 			e = new Event();
 			e.time = event.time;
 			e.character = event.character;
 			e.keyCode = event.keyCode;
 			SWT_FACADE.copyEventKeyLocation(event, e);
 			e.stateMask = event.stateMask;
 			notifyListeners(SWT.KeyUp, e);
 			event.doit = e.doit;
 			break;
 		case SWT.KeyDown:
 			if (event.character == SWT.ESC) {
 				// Escape key cancels popup list
 				dropDown(false);
 			}
 			if ((event.stateMask & SWT.ALT) != 0 && (event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN)) {
 				dropDown(false);
 			}
 			if (event.character == SWT.CR) {
 				// Enter causes default selection
 				dropDown(false);
 				e = new Event();
 				e.time = event.time;
 				e.stateMask = event.stateMask;
 				notifyListeners(SWT.DefaultSelection, e);
 			}
 			// At this point the widget may have been disposed.
 			// If so, do not continue.
 			if (isDisposed()) {
 				break;
 			}
 			e = new Event();
 			e.time = event.time;
 			e.character = event.character;
 			e.keyCode = event.keyCode;
 			SWT_FACADE.copyEventKeyLocation(event, e);
 			e.stateMask = event.stateMask;
 			notifyListeners(SWT.KeyDown, e);
 			event.doit = e.doit;
 			break;
 		default:
 			break;
 		}
 	}
 
 	void popupEvent(final Event event) {
 		switch (event.type) {
 		case SWTFacade.Paint:
 			// draw black rectangle around list
 			final Rectangle listRect = list.getBounds();
 			final Color black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
 			event.gc.setForeground(black);
 			event.gc.drawRectangle(0, 0, listRect.width + 1, listRect.height + 1);
 			break;
 		case SWT.Close:
 			event.doit = false;
 			dropDown(false);
 			break;
 		case SWT.Deactivate:
 			/*
 			 * Bug in GTK. When the arrow button is pressed the popup control
 			 * receives a deactivate event and then the arrow button receives a
 			 * selection event. If we hide the popup in the deactivate event,
 			 * the selection event will show it again. To prevent the popup from
 			 * showing again, we will let the selection event of the arrow
 			 * button hide the popup. In Windows, hiding the popup during the
 			 * deactivate causes the deactivate to be called twice and the
 			 * selection event to be disappear.
 			 */
 			if (!"carbon".equals(SWT.getPlatform())) { //$NON-NLS-1$
 				final Point point = arrow.toControl(getDisplay().getCursorLocation());
 				final Point size = arrow.getSize();
 				final Rectangle rect = new Rectangle(0, 0, size.x, size.y);
 				if (!rect.contains(point)) {
 					dropDown(false);
 				}
 			} else {
 				dropDown(false);
 			}
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void redraw() {
 		super.redraw();
 		text.redraw();
 		arrow.redraw();
 
 		if (!SwtUtilities.isDisposed(popup) && popup.isVisible()) {
 			list.redraw();
 		}
 	}
 
 	@Override
 	public void redraw(final int x, final int y, final int width, final int height, final boolean all) {
 		super.redraw(x, y, width, height, true);
 	}
 
 	/**
 	 * Removes all of the items from the receiver's list and clear the contents
 	 * of receiver's text field.
 	 * <p>
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
 	 *                called from the thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public void removeAll() {
 		checkWidget();
 		clearImage();
 		text.setText(""); //$NON-NLS-1$
 		removeAll(list);
 	}
 
 	/**
 	 * Removes the listener from the collection of listeners who will be
 	 * notified when the receiver's text is modified.
 	 * 
 	 * @param listener
 	 *            the listener which should no longer be notified
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 * 
 	 * @see ModifyListener
 	 * @see #addModifyListener
 	 */
 	public abstract void removeModifyListener(final ModifyListener listener);
 
 	/**
 	 * Removes the listener from the collection of listeners who will be
 	 * notified when the user changes the receiver's selection.
 	 * 
 	 * @param listener
 	 *            the listener which should no longer be notified
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 * 
 	 * @see SelectionListener
 	 * @see #addSelectionListener
 	 */
 	public abstract void removeSelectionListener(final SelectionListener listener);
 
 	/**
 	 * Removes the listener from the collection of listeners who will be
 	 * notified when the control is verified.
 	 * 
 	 * @param listener
 	 *            the listener which should no longer be notified
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 * 
 	 * @see VerifyListener
 	 * @see #addVerifyListener
 	 */
 	public abstract void removeVerifyListener(final VerifyListener listener);
 
 	/**
 	 * Selects the item at the given zero-relative index in the receiver's list.
 	 * If the item at the index was already selected, it remains selected.
 	 * Indices that are out of range are ignored.
 	 * 
 	 * @param index
 	 *            the index of the item to select
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public void select(final int index) {
 		checkWidget();
 		if (index == -1) {
 			deselectAll(list);
 			clearImage();
 			text.setText(""); //$NON-NLS-1$
 			return;
 		}
 		if (0 <= index && index < getItemCount(list)) {
 			if (index != getSelectionIndex()) {
 				setImage(index);
 				text.setText(getItem(list, index));
 				text.selectAll();
 				setSelection(list, index);
 			}
 		}
 	}
 
 	/**
 	 * Sets a flag which indicates that the CompletionCombo should autocomplete
 	 * entries in the Textfield. This autocompletion matches a prefix to the
 	 * dropdown list and focuses on the list entry matching this prefix.
 	 * <p>
 	 * The default value is true.
 	 * 
 	 * @param autoCompletion
 	 *            true if autocompletion should be turned on, false is default
 	 */
 	public void setAutoCompletion(final boolean autoCompletion) {
 		this.autoCompletion = autoCompletion;
 	}
 
 	/**
 	 * Set's the strategy for autocompletion. See {@link AutoCompletionMode} for
 	 * details.
 	 * <p>
 	 * The default value is {@link AutoCompletionMode#NO_MISSMATCH}.
 	 * 
 	 * @param autoCompletionMode
 	 *            an {@link AutoCompletionMode} instance; never null
 	 */
 	public void setAutoCompletionMode(final AutoCompletionMode autoCompletionMode) {
 		Assert.isNotNull(autoCompletionMode);
 		this.autoCompletionMode = autoCompletionMode;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Note: this will change the background of both the Text and List widgets
 	 * maintained by this control.
 	 */
 	@Override
 	public void setBackground(final Color color) {
 		super.setBackground(color);
 		setTextBackground(color);
 		setListBackground(color);
 	}
 
 	/**
 	 * Set the background of this Combo's List widget.
 	 * 
 	 * @param color
 	 *            the new color (or null to set to the default system color)
 	 * @since 3.0
 	 */
 	public void setListBackground(final Color color) {
 		listBackground = color;
 		if (list != null && !list.isDisposed()) {
 			list.setBackground(color);
 		}
 	}
 
 	/**
 	 * Set the background of this Combo's Text widget.
 	 * 
 	 * @param color
 	 *            the new color (or null to set to the default system color)
 	 * @since 3.0
 	 */
 	public void setTextBackground(final Color color) {
 		if (label != null && !label.isDisposed()) {
 			label.setBackground(color);
 		}
 		if (text != null && !text.isDisposed()) {
 			text.setBackground(color);
 		}
 		if (arrow != null && !arrow.isDisposed()) {
 			arrow.setBackground(color);
 		}
 	}
 
 	/**
 	 * Sets the editable state.
 	 * 
 	 * @param editable
 	 *            the new editable state
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public void setEditable(final boolean editable) {
 		checkWidget();
 		text.setEditable(editable);
 		arrow.setEnabled(isEnabled() && editable);
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public void setFlashDelegate(final IFlashDelegate delegate) {
 		this.flashDelegate = delegate;
 	}
 
 	@Override
 	public void setEnabled(final boolean enabled) {
 		super.setEnabled(enabled);
 		text.setEnabled(enabled);
 		final boolean editable = enabled && text.getEditable();
 		arrow.setEnabled(editable);
 		if (!editable && !SwtUtilities.isDisposed(popup)) {
 			popup.setVisible(editable);
 		}
 	}
 
 	@Override
 	public boolean setFocus() {
 		checkWidget();
 		if (!isEnabled() || !isVisible()) {
 			return false;
 		}
 		if (isFocusControl()) {
 			return true;
 		}
 		return text.setFocus();
 	}
 
 	@Override
 	public void setFont(final Font font) {
 		super.setFont(font);
 		this.font = font;
 		text.setFont(font);
 		list.setFont(font);
 		internalLayout(true);
 	}
 
 	@Override
 	public void setForeground(final Color color) {
 		super.setForeground(color);
 		listForeground = color;
 		// fix for 304869
 		if (text != null && !text.isDisposed()) {
 			text.setForeground(color);
 		}
 		if (list != null && !list.isDisposed()) {
 			list.setForeground(color);
 		}
 		if (arrow != null && !arrow.isDisposed()) {
 			arrow.setForeground(color);
 		}
 	}
 
 	/**
 	 * Sets the receiver's list to be the given array of items.
 	 * 
 	 * @param items
 	 *            the array of items
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_NULL_ARGUMENT - if the items array is null</li>
 	 *                <li>ERROR_INVALID_ARGUMENT - if an item in the items array
 	 *                is null</li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public void setItems(final String[] items) {
 		setItems(items, null);
 	}
 
 	/**
 	 * TODO [ev] docs
 	 * 
 	 * @param items
 	 * @param images
 	 * @since 3.0
 	 */
 	public void setItems(final String[] items, final Image[] images) {
 		checkWidget();
 		if (images != null) {
 			Assert.isLegal(items.length == images.length, "Number of items and images does not match"); //$NON-NLS-1$
 		}
 		setItems(list, items, images);
 		for (final String item : items) {
 			updateInputChars(item);
 		}
 		if (!text.getEditable()) {
 			clearImage();
 			text.setText(""); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Sets the layout which is associated with the receiver to be the argument
 	 * which may be null.
 	 * <p>
 	 * Note: No Layout can be set on this Control because it already manages the
 	 * size and position of its children.
 	 * </p>
 	 * 
 	 * @param layout
 	 *            the receiver's new layout or null
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	@Override
 	public void setLayout(final Layout layout) {
 		checkWidget();
 		return;
 	}
 
 	/**
 	 * Marks the receiver's list as visible if the argument is <code>true</code>
 	 * , and marks it invisible otherwise.
 	 * <p>
 	 * If one of the receiver's ancestors is not visible or some other condition
 	 * makes the receiver not visible, marking it visible may not actually cause
 	 * it to be displayed.
 	 * </p>
 	 * 
 	 * @param visible
 	 *            the new visibility state
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public void setListVisible(final boolean visible) {
 		checkWidget();
 		dropDown(visible);
 	}
 
 	@Override
 	public void setMenu(final Menu menu) {
 		text.setMenu(menu);
 	}
 
 	/**
 	 * Sets the selection in the receiver's text field to the range specified by
 	 * the argument whose x coordinate is the start of the selection and whose y
 	 * coordinate is the end of the selection.
 	 * 
 	 * @param selection
 	 *            a point representing the new selection start and end
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public void setSelection(final Point selection) {
 		checkWidget();
 		if (selection == null) {
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		}
 		text.setSelection(selection.x, selection.y);
 	}
 
 	/**
 	 * Sets the contents of the receiver's text field to the given string.
 	 * <p>
 	 * Note: The text field in a <code>Combo</code> is typically only capable of
 	 * displaying a single line of text. Thus, setting the text to a string
 	 * containing line breaks or other special characters will probably cause it
 	 * to display incorrectly.
 	 * </p>
 	 * 
 	 * @param string
 	 *            the new text
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public void setText(final String string) {
 		checkWidget();
 		if (string == null) {
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		}
 		final int index = indexOf(list, string, 0);
 		if (index == -1) {
 			deselectAll(list);
 			clearImage();
 			text.setText(string);
 			return;
 		}
 		setImage(index);
 		text.setText(string);
 		text.selectAll();
 		setSelection(list, index);
 	}
 
 	/**
 	 * Sets the maximum number of characters that the receiver's text field is
 	 * capable of holding to be the argument.
 	 * 
 	 * @param limit
 	 *            new text limit
 	 * 
 	 * @exception IllegalArgumentException
 	 *                <ul>
 	 *                <li>ERROR_CANNOT_BE_ZERO - if the limit is zero</li>
 	 *                </ul>
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public void setTextLimit(final int limit) {
 		checkWidget();
 		text.setTextLimit(limit);
 	}
 
 	@Override
 	public void setToolTipText(final String string) {
 		checkWidget();
 		super.setToolTipText(string);
 		arrow.setToolTipText(string);
 		text.setToolTipText(string);
 	}
 
 	@Override
 	public void setVisible(final boolean visible) {
 		super.setVisible(visible);
 		/*
 		 * At this point the widget may have been disposed in a FocusOut event.
 		 * If so then do not continue.
 		 */
 		if (isDisposed()) {
 			return;
 		}
 		// TEMPORARY CODE
 		if (popup == null || popup.isDisposed()) {
 			return;
 		}
 		if (!visible) {
 			popup.setVisible(false);
 		}
 	}
 
 	/**
 	 * Sets the number of items that are visible in the drop down portion of the
 	 * receiver's list.
 	 * 
 	 * @param count
 	 *            the new number of items to be visible
 	 * 
 	 * @exception SWTException
 	 *                <ul>
 	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
 	 *                disposed</li>
 	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
 	 *                thread that created the receiver</li>
 	 *                </ul>
 	 */
 	public void setVisibleItemCount(final int count) {
 		checkWidget();
 		if (count < 0) {
 			return;
 		}
 		visibleItemCount = count;
 	}
 
 	void textEvent(final Event event) {
 		switch (event.type) {
 		case SWT.FocusIn:
 			handleFocus(SWT.FocusIn);
 			break;
 		case SWT.DefaultSelection:
 			dropDown(false);
 			if (isAutoCompletion()) {
 				selectAll();
 			}
 			Event e = new Event();
 			e.time = event.time;
 			e.stateMask = event.stateMask;
 			notifyListeners(SWT.DefaultSelection, e);
 			break;
 		case SWT.DragDetect:
 		case SWT.MouseDoubleClick:
 		case SWTFacade.MouseMove:
 		case SWTFacade.MouseEnter:
 		case SWTFacade.MouseExit:
 		case SWTFacade.MouseHover:
 			Point pt = getDisplay().map(text, this, event.x, event.y);
 			event.x = pt.x;
 			event.y = pt.y;
 			notifyListeners(event.type, event);
 			event.type = SWT.None;
 			break;
 		case SWT.KeyDown:
 			final Event keyEvent = new Event();
 			keyEvent.time = event.time;
 			keyEvent.character = event.character;
 			keyEvent.keyCode = event.keyCode;
 			SWT_FACADE.copyEventKeyLocation(event, keyEvent);
 			keyEvent.stateMask = event.stateMask;
 			notifyListeners(SWT.KeyDown, keyEvent);
 			if (isDisposed() || !getEditable()) {
 				break;
 			}
 			if (isAutoCompletion()) {
 				handleAutoCompletion(event);
 			} else {
 				event.doit = keyEvent.doit;
 			}
 			if (!event.doit) {
 				break;
 			}
 			if (event.character == SWT.ESC) {
 				// Escape key cancels popup list
 				dropDown(false);
 				break;
 			}
 			if (event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN) {
 				event.doit = false;
 				if ((event.stateMask & SWT.ALT) != 0) {
 					final boolean dropped = isDropped();
 					text.selectAll();
 					if (!dropped) {
 						setFocus();
 					}
 					dropDown(!dropped);
 					break;
 				}
 
 				final int oldIndex = getSelectionIndex();
 				if (event.keyCode == SWT.ARROW_UP) {
 					select(Math.max(oldIndex - 1, 0));
 				} else {
 					select(Math.min(oldIndex + 1, getItemCount() - 1));
 				}
 				if (oldIndex != getSelectionIndex()) {
 					e = new Event();
 					e.time = event.time;
 					e.stateMask = event.stateMask;
 					notifyListeners(SWT.Selection, e);
 				}
 				if (isDisposed()) {
 					break;
 				}
 			}
 			// Further work : Need to add support for incremental search in 
 			// pop up list as characters typed in text widget
 			break;
 		case SWT.KeyUp:
 			e = new Event();
 			e.time = event.time;
 			e.character = event.character;
 			e.keyCode = event.keyCode;
 			SWT_FACADE.copyEventKeyLocation(event, e);
 			e.stateMask = event.stateMask;
 			notifyListeners(SWT.KeyUp, e);
 			event.doit = e.doit;
 			break;
 		case SWT.MenuDetect:
 			e = new Event();
 			e.time = event.time;
 			notifyListeners(SWT.MenuDetect, e);
 			break;
 		case SWT.Modify:
 			deselectAll(list);
 			e = new Event();
 			e.time = event.time;
 			notifyListeners(SWT.Modify, e);
 			break;
 		case SWT.MouseDown:
 			pt = getDisplay().map(text, this, event.x, event.y);
 			Event mouseEvent = new Event();
 			mouseEvent.button = event.button;
 			mouseEvent.count = event.count;
 			mouseEvent.stateMask = event.stateMask;
 			mouseEvent.time = event.time;
 			mouseEvent.x = pt.x;
 			mouseEvent.y = pt.y;
 			notifyListeners(SWT.MouseDown, mouseEvent);
 			if (isDisposed()) {
 				break;
 			}
 			event.doit = mouseEvent.doit;
 			if (!event.doit) {
 				break;
 			}
 			if (isAutoCompletion()) {
 				if (!isDropped()) {
 					dropDown(true);
 				}
 				text.setFocus();
 			}
 			if (event.button != 1) {
 				return;
 			}
 			if (text.getEditable()) {
 				return;
 			}
 			final boolean dropped = isDropped();
 			text.selectAll();
 			if (!dropped) {
 				setFocus();
 			}
 			dropDown(!dropped);
 			break;
 		case SWT.MouseUp:
 			pt = getDisplay().map(text, this, event.x, event.y);
 			mouseEvent = new Event();
 			mouseEvent.button = event.button;
 			mouseEvent.count = event.count;
 			mouseEvent.stateMask = event.stateMask;
 			mouseEvent.time = event.time;
 			mouseEvent.x = pt.x;
 			mouseEvent.y = pt.y;
 			notifyListeners(SWT.MouseUp, mouseEvent);
 			if (isDisposed()) {
 				break;
 			}
 			event.doit = mouseEvent.doit;
 			if (!event.doit) {
 				break;
 			}
 			if (event.button != 1) {
 				return;
 			}
 			if (text.getEditable()) {
 				return;
 			}
 			text.selectAll();
 			break;
 		case SWTFacade.MouseWheel:
 			notifyListeners(SWTFacade.MouseWheel, event);
 			event.type = SWT.None;
 			if (isDisposed()) {
 				break;
 			}
 			if (!event.doit) {
 				break;
 			}
 			if (event.count != 0) {
 				event.doit = false;
 				final int oldIndex = getSelectionIndex();
 				if (event.count > 0) {
 					select(Math.max(oldIndex - 1, 0));
 				} else {
 					select(Math.min(oldIndex + 1, getItemCount() - 1));
 				}
 				if (oldIndex != getSelectionIndex()) {
 					e = new Event();
 					e.time = event.time;
 					e.stateMask = event.stateMask;
 					notifyListeners(SWT.Selection, e);
 				}
 				if (isDisposed()) {
 					break;
 				}
 			}
 			break;
 		case SWT.Traverse:
 			switch (event.detail) {
 			case SWTFacade.TRAVERSE_ARROW_PREVIOUS:
 			case SWTFacade.TRAVERSE_ARROW_NEXT:
 				// The enter causes default selection and
 				// the arrow keys are used to manipulate the list contents so
 				// do not use them for traversal.
 				event.doit = false;
 				break;
 			case SWT.TRAVERSE_TAB_PREVIOUS:
 				event.doit = SWT_FACADE.traverse(this, SWT.TRAVERSE_TAB_PREVIOUS);
 				event.detail = SWT.TRAVERSE_NONE;
 				return;
 			default:
 				break;
 			}
 			e = new Event();
 			e.time = event.time;
 			e.detail = event.detail;
 			e.doit = event.doit;
 			e.character = event.character;
 			e.keyCode = event.keyCode;
 			SWT_FACADE.copyEventKeyLocation(event, e);
 			notifyListeners(SWT.Traverse, e);
 			event.doit = e.doit;
 			event.detail = e.detail;
 			break;
 		case SWT.Verify:
 			e = new Event();
 			e.text = event.text;
 			e.start = event.start;
 			e.end = event.end;
 			e.character = event.character;
 			e.keyCode = event.keyCode;
 			SWT_FACADE.copyEventKeyLocation(event, e);
 			e.stateMask = event.stateMask;
 			notifyListeners(SWT.Verify, e);
 			event.text = e.text;
 			event.doit = e.doit;
 			break;
 		default:
 			break;
 		}
 	}
 
 	private void clearImage() {
 		if (label != null) {
 			label.setImage(null);
 		}
 	}
 
 	private void setImage(final int index) {
 		if (label != null) {
 			label.setImage(getImage(list, index));
 		}
 	}
 
 	private void selectAll() {
 		setSelection(new Point(0, text.getText().length()));
 	}
 
 	/**
 	 * Handles the autocompletion process.
 	 * 
 	 * @param event
 	 *            the key event that triggered the method
 	 * @return if this method handled the event
 	 */
 	private void handleAutoCompletion(final Event event) {
 		event.doit = false;
 		// System.out.println(String.format("ch:%c, kc:%d, mask:%d", event.character, event.keyCode, event.stateMask));
 		if (event.stateMask == SWT.ALT || event.stateMask == SWT.CONTROL) {
 			//  Must use equality instead of (stateMask & const > 0) here!
 			// This allows ALT + x or CONTROL + x combos. However 
 			// ALT + CONTROL + x will not go in here and will be handled
 			// by isInputChar(x) instead.
 			event.doit = handleClipboardOperations(event);
 		} else if (event.character == SWT.DEL && isAllowMissmatch()) {
 			event.doit = true;
 		} else if (isControlChar(event)) {
 			// System.out.println("isControlChar: " + event.character);
 			event.doit = true;
 		} else if (isInputChar(event.character)) {
 			// System.out.println("isInputChar: " + event.character);
 			if (!isDropped()) {
 				dropDown(true);
 				text.setFocus();
 			}
 
 			final Point selection = getSelection();
 			final String oldPrefix = text.getText().substring(0, selection.x);
 			final String newPrefix;
 			if (event.character == SWT.BS) {
 				newPrefix = buildPrefixOnBackSpace(selection);
 			} else if (isRepeatOfFirstChar(event, oldPrefix)) {
 				if (jumpToNextItem(oldPrefix)) {
 					return;
 				}
 				newPrefix = buildPrefixForInput(event.character, selection);
 			} else {
 				newPrefix = buildPrefixForInput(event.character, selection);
 			}
 
 			final boolean matched = matchPrefixWithList(newPrefix);
 			if (!matched) {
 				if (isAllowMissmatch()) { // TODO [ev] ridget sel
 					clearImage();
 					event.doit = true;
 				} else {
 					if (flashDelegate != null) {
 						flashDelegate.flash();
 					}
 				}
 			}
 		}
 	}
 
 	private boolean isRepeatOfFirstChar(final Event event, final String oldPrefix) {
 		return !isAllowMissmatch() && oldPrefix.length() == 1
 				&& oldPrefix.equalsIgnoreCase(String.valueOf(event.character)) && getSelectionIndex(list) != -1;
 	}
 
 	private boolean jumpToNextItem(final String oldPrefix) {
 		boolean result = false;
 		final int newIndex = getSelectionIndex(list) + 1;
 		if (newIndex < getItemCount(list)) {
 			final String newSelection = getItem(list, newIndex);
 			if (newSelection.length() > 0 && oldPrefix.equalsIgnoreCase(newSelection.substring(0, 1))) {
 				setImage(newIndex);
 				text.setText(newSelection);
 				setSelection(list, newIndex);
 				text.setSelection(1, newSelection.length());
 				result = true;
 			}
 		}
 		return result;
 	}
 
 	private boolean handleClipboardOperations(final Event event) {
 		boolean result = true;
 		if (event.stateMask == SWT.CONTROL) {
 			if (event.keyCode == 118) { // Ctrl + V
 				handlePaste();
 				result = false;
 			} else if (event.keyCode == 120) { // Ctrl + X
 				handleCut();
 				result = false;
 			}
 		}
 		return result;
 	}
 
 	private void handleCut() {
 		final Point selection = text.getSelection();
 		final String oldText = text.getText();
 		final String newText = oldText.substring(0, selection.x) + oldText.substring(selection.y, oldText.length());
 		final int index = indexOf(newText);
 		if (index != -1 || newText.length() == 0) {
 			ClipboardFacade.getDefault().cut(text);
 			if (index == -1) { // TODO [ev] ridget sel
 				clearImage();
 			} else {
 				setImage(index);
 			}
 			text.setText(newText);
 			setSelection(list, index);
 			text.setSelection(newText.length());
 		} else if (index == -1 && isAllowMissmatch()) {
 			clearImage();
 			text.setText(newText);
 			text.setSelection(newText.length());
 		}
 	}
 
 	private void handlePaste() {
 		final String data = ClipboardFacade.getDefault().getTextFromClipboard(getDisplay());
 		if (data != null) {
 			final Point selection = text.getSelection();
 			final String oldText = text.getText();
 			final String newText = oldText.substring(0, selection.x) + data
 					+ oldText.substring(selection.y, oldText.length());
 			final int index = indexOf(newText);
 			if (index != -1) { // TODO [ev] ridget sel
 				setImage(index);
 				text.setText(newText);
 				setSelection(list, index);
 				text.setSelection(newText.length());
 			} else if (index == -1 && isAllowMissmatch()) {
 				clearImage();
 				text.setText(newText);
 				text.setSelection(newText.length());
 			}
 		}
 	}
 
 	private void updateInputChars(final String string) {
 		for (int i = 0; i < string.length(); i++) {
 			final char ch = string.charAt(i);
 			if (!Character.isLetterOrDigit(ch)) {
 				inputChars.add(Character.valueOf(ch));
 			}
 		}
 	}
 
 	private boolean isAllowMissmatch() {
 		return autoCompletionMode == AutoCompletionMode.ALLOW_MISSMATCH;
 	}
 
 	private boolean isInputChar(final char ch) {
 		if (Character.isLetterOrDigit(ch) || ch == SWT.BS) {
 			return true;
 		}
 		final Character character = Character.valueOf(ch);
 		if (inputChars.contains(character)) {
 			return true;
 		}
 		if (isAllowMissmatch()) {
 			// Special character: dash, punktuation, currency, quotes, brakets, ...
 			final int type = Character.getType(ch);
 			if (type == Character.LETTER_NUMBER || type == Character.OTHER_NUMBER || type >= Character.DASH_PUNCTUATION
 					&& type < Character.FINAL_QUOTE_PUNCTUATION) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isControlChar(final Event event) {
 		final char[] chars = { SWT.ESC, SWT.CR };
 		for (final int ch : chars) {
 			if (ch == event.character) {
 				return true;
 			}
 		}
 		final int[] keyCodes = { SWT.ARROW_UP, SWT.ARROW_DOWN, SWT.ARROW_LEFT, SWT.ARROW_RIGHT, SWT.HOME, SWT.END };
 		for (final int keycode : keyCodes) {
 			if (keycode == event.keyCode) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean matchPrefixWithList(final String prefix) {
 		boolean result = false;
 		if (prefix != null) {
 			if (prefix.length() == 0) {
 
 				clearImage();
 				text.setText(""); //$NON-NLS-1$
 				result = true;
 			} else {
 
 				int prefixLength = prefix.length();
 				for (final String item : getItems(list)) {
 
 					if (item.equals(text.getText())) {
 						continue;
 					}
 					if (matchesWord(prefix, item)) {
 						setMatchingTextAndSelection(prefixLength, item);
 						result = true;
 						break;
 					}
 				}
 
 				// when nothing was found and the prefix consists of more than 1 character of the same type e.g. "aa"
 				// jump to the next match of "a"
 				if (!result && !isAllowMissmatch() && isMultipleInputOfSameChar(prefix)) {
 
 					final String singleCharPrefix = prefix.substring(0, 1);
 					prefixLength = singleCharPrefix.length();
 
 					final String[] items = getItems();
 
 					for (int i = getSelectionIndex() + 1; i < items.length; i++) {
 
 						final String item = items[i];
 
 						if (matchesWord(singleCharPrefix, item)) {
 							setMatchingTextAndSelection(prefixLength, item);
 							result = true;
 							break;
 						}
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	private boolean isMultipleInputOfSameChar(final String prefix) {
 		if (prefix.length() < 2) {
 			return false;
 		}
 
 		final String lowerPrefix = prefix.toLowerCase();
 
 		final char first = lowerPrefix.charAt(0);
 		for (int i = 1; i < lowerPrefix.length(); i++) {
 			final char current = lowerPrefix.charAt(i);
 			if (current != first) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private void setMatchingTextAndSelection(final int selectionStart, final String item) {
 		final int index = indexOf(item);
 		Assert.isLegal(index > -1);
 		setImage(index);
 		text.setText(item);
 		setSelection(new Point(selectionStart, item.length()));
 		setSelection(list, index);
 	}
 
 	private String buildPrefixOnBackSpace(final Point selection) {
 		String result;
 		final String theText = text.getText();
 		if (isAllowMissmatch()) {
 			final int end = Math.max(0, selection.x - 1);
 			result = theText.substring(0, end) + theText.substring(selection.y);
 		} else {
 			final String prefix = theText.substring(0, selection.x);
 			final int end = Math.max(0, prefix.length() - 1);
 			result = prefix.substring(0, end);
 		}
 		return result;
 	}
 
 	private String buildPrefixForInput(final char typedChar, final Point selection) {
 		String prefix = text.getText().substring(0, selection.x);
 		if (isAllowMissmatch()) {
 			prefix = text.getText().substring(0, selection.x) + typedChar + text.getText().substring(selection.y);
 		} else {
 			prefix += typedChar;
 		}
 		return prefix;
 	}
 
 	private boolean matchesWord(final String prefix, final String word) {
 		if (prefix == null || word == null) {
 			return false;
 		}
 
 		if (word.toLowerCase().startsWith(prefix.toLowerCase())) {
 			return true;
 		}
 
 		return false;
 	}
 
 }
