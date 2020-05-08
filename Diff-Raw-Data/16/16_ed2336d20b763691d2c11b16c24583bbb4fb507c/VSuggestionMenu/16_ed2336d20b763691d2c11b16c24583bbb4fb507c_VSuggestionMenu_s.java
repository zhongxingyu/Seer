 package com.zipsoft.suggestfield.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.aria.client.Id;
 import com.google.gwt.aria.client.Roles;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.MenuItem;
 import com.google.gwt.user.client.ui.MenuItemSeparator;
 import com.google.gwt.user.client.ui.UIObject;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.impl.FocusImpl;
 
 public class VSuggestionMenu extends Widget {
 
 	private static final String STYLENAME_DEFAULT = "z-suggestion-items";
 
 	private Element body;
 
 	private VSuggestionMenuItem selectedItem;
 	
 	//TODO Proveriti ovo
 	private boolean focusOnHover = false;
 
 	/**
 	 * List of all {@link MenuItem}s and {@link MenuItemSeparator}s.
 	 */
 	// private ArrayList<UIObject> allItems = new ArrayList<UIObject>();
 
 	/**
 	 * List of {@link MenuItem}s, not including {@link MenuItemSeparator}s.
 	 */
 	private ArrayList<VSuggestionMenuItem> items = new ArrayList<VSuggestionMenuItem>();
 
 	/**
 	 * Default constructor
 	 * 
 	 */
 	public VSuggestionMenu() {
 		Element table = DOM.createTable();
 		body = DOM.createTBody();
 		DOM.appendChild(table, body);
 
 		Element outer = FocusImpl.getFocusImplForPanel().createFocusable();
 		DOM.appendChild(outer, table);
 		setElement(outer);
 
 		Roles.getMenubarRole().set(getElement());
 
 		sinkEvents(Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT
 				| Event.ONFOCUS | Event.ONKEYDOWN);
 
 		setStyleName(STYLENAME_DEFAULT);
 
 		// Hide focus outline in Mozilla/Webkit/Opera
 		getElement().getStyle().setProperty("outline", "0px");
 //		DOM.setStyleAttribute(getElement(), "outline", "0px");
 
 		// Hide focus outline in IE 6/7
 //		DOM.setElementAttribute(getElement(), "hideFocus", "true");
 		getElement().getStyle().setProperty("hideFocus", "true");
 
 		// Deselect items when blurring without a child menu.
 		addDomHandler(new BlurHandler() {
 			@Override
 			public void onBlur(BlurEvent event) {
 				// if (shownChildMenu == null) {
 				selectItem(null);
 				// }
 			}
 		}, BlurEvent.getType());
 	}
 
 	/**
 	 * Give this MenuBar focus.
 	 */
 	public void focus() {
 		FocusImpl.getFocusImplForPanel().focus(getElement());
 	}
 
 	/**
 	 * Adds a menu item to the bar.
 	 * 
 	 * @param item
 	 *            the item to be added
 	 * @return the {@link MenuItem} object
 	 */
 	public VSuggestionMenuItem addItem(VSuggestionMenuItem item) {
 		// return insertItem(item, allItems.size());
 		return insertItem(item, items.size());
 	}
 
 	/**
 	 * Adds a menu item to the bar at a specific index.
 	 * 
 	 * @param item
 	 *            the item to be inserted
 	 * @param beforeIndex
 	 *            the index where the item should be inserted
 	 * @return the {@link VSuggestionMenuItem} object
 	 * @throws IndexOutOfBoundsException
 	 *             if <code>beforeIndex</code> is out of range
 	 */
 	public VSuggestionMenuItem insertItem(VSuggestionMenuItem item,
 			int beforeIndex) throws IndexOutOfBoundsException {
 		// Check the bounds
 		// if (beforeIndex < 0 || beforeIndex > allItems.size()) {
 		if (beforeIndex < 0 || beforeIndex > items.size()) {
 			throw new IndexOutOfBoundsException();
 		}
 
 		// Add to the list of items
 		// allItems.add(beforeIndex, item);
 		// int itemsIndex = 0;
 		// for (int i = 0; i < beforeIndex; i++) {
 		// if (allItems.get(i) instanceof MenuItem) {
 		// itemsIndex++;
 		// }
 		// }
 		// items.add(itemsIndex, item);
 		items.add(beforeIndex, item);
 
 		// Setup the menu item
 		addItemElement(beforeIndex, item.getElement());
 		item.setParentMenu(this);
 		item.setSelectionStyle(false);
 		return item;
 	}
 
 	/**
 	 * Physically add the td element of a {@link VSuggestionMenuItem} or
 	 * {@link MenuItemSeparator} to this {@link MenuBar}.
 	 * 
 	 * @param beforeIndex
 	 *            the index where the separator should be inserted
 	 * @param tdElem
 	 *            the td element to be added
 	 */
 	private void addItemElement(int beforeIndex, Element tdElem) {
 		Element tr = DOM.createTR();
 		DOM.insertChild(body, tr, beforeIndex);
 		DOM.appendChild(tr, tdElem);
 	}
 
 	/**
 	 * Removes the specified item from the {@link MenuBar} and the physical DOM
 	 * structure.
 	 * 
 	 * @param item
 	 *            the item to be removed
 	 * @return true if the item was removed
 	 */
 	private boolean removeItemElement(UIObject item) {
 		int idx = items.indexOf(item);
 		if (idx == -1) {
 			return false;
 		}
 
 		Element container = getItemContainerElement();
 		container.removeChild(container.getChild(idx));
 //		DOM.removeChild(container, DOM.getChild(container, idx));
 		items.remove(idx);
 		return true;
 	}
 
 	/**
 	 * Removes the specified menu item from the bar.
 	 * 
 	 * @param item
 	 *            the item to be removed
 	 */
 	public void removeItem(VSuggestionMenuItem item) {
 		// Unselect if the item is currently selected
 		if (selectedItem == item) {
 			selectItem(null);
 		}
 
 		if (removeItemElement(item)) {
 			setItemColSpan(item, 1);
 			items.remove(item);
 			item.setParentMenu(null);
 		}
 	}
 
 	/**
 	 * Removes all menu items from this menu bar.
 	 */
 	public void clearItems() {
 		// Deselect the current item
 		selectItem(null);
 
 		Element container = getItemContainerElement();
 		while (DOM.getChildCount(container) > 0) {
 			container.removeChild(container.getChild(0));
 //			DOM.removeChild(container, DOM.getChild(container, 0));
 		}
 
 		for (VSuggestionMenuItem item : items) {
 			setItemColSpan(item, 1);
 			item.setParentMenu(null);
 		}
 
 		items.clear();
 	}
 
 	private VSuggestionMenuItem findItem(Element hItem) {
 		for (VSuggestionMenuItem item : items) {
 			if (item.getElement().isOrHasChild(hItem)) {
 				return item;
 			}
 //			if (DOM.isOrHasChild(item.getElement(), hItem)) {
 //				return item;
 //			}
 		}
 		return null;
 	}
 
 	private Element getItemContainerElement() {
 		return body;
 	}
 
 	/**
 	 * Select the given MenuItem, which must be a direct child of this MenuBar.
 	 * 
 	 * @param item
 	 *            the MenuItem to select, or null to clear selection
 	 */
 	public void selectItem(VSuggestionMenuItem item) {
 		assert item == null || item.getParentMenu() == this;
 
 		if (item == selectedItem) {
 			return;
 		}
 
 		if (selectedItem != null) {
 			selectedItem.setSelectionStyle(false);
 		}
 
 		if (item != null) {
 			item.setSelectionStyle(true);
 
 			Roles.getMenubarRole().setAriaActivedescendantProperty(
 					getElement(), Id.of(item.getElement()));
 		}
 
 		selectedItem = item;
 	}
 	
 	public void selectItemAtIndex(int itemIndex) {
 		if (itemIndex != -1 && itemIndex < items.size()) {
 			if (items.get(itemIndex) != null) {
 				this.selectItem(items.get(itemIndex));				
 			}
 		}
 	}
 
 	/**
 	 * Selects the first item in the menu if no items are currently selected.
 	 * Has no effect if there are no items.
 	 * 
 	 * @return true if no item was previously selected, false otherwise
 	 */
 	private boolean selectFirstItemIfNoneSelected() {
 		if (selectedItem == null) {
 			for (VSuggestionMenuItem nextItem : items) {
 				if (nextItem.isEnabled()) {
 					selectItem(nextItem);
 					break;
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Moves the menu selection down to the next item. If there is no selection,
 	 * selects the first item. If there are no items at all, does nothing.
 	 */
 	public void moveSelectionDown() {
 		if (selectFirstItemIfNoneSelected()) {
 			return;
 		}
 		selectNextItem();
 	}
 
 	/**
 	 * Moves the menu selection up to the previous item. If there is no
 	 * selection, selects the first item. If there are no items at all, does
 	 * nothing.
 	 */
 	public void moveSelectionUp() {
 		if (selectFirstItemIfNoneSelected()) {
 			return;
 		}
 		selectPrevItem();
 	}
 
 	private void selectNextItem() {
 		if (selectedItem == null) {
 			return;
 		}
 
 		int index = items.indexOf(selectedItem);
 		// We know that selectedItem is set to an item that is contained in the
 		// items collection.
 		// Therefore, we know that index can never be -1.
 		assert (index != -1);
 
 		VSuggestionMenuItem itemToBeSelected;
 
 		int firstIndex = index;
 		while (true) {
 			index = index + 1;
 			if (index == items.size()) {
 				// we're at the end, loop around to the start
 				index = 0;
 			}
 			if (index == firstIndex) {
 				itemToBeSelected = items.get(firstIndex);
 				break;
 			} else {
 				itemToBeSelected = items.get(index);
 				if (itemToBeSelected.isEnabled()) {
 					break;
 				}
 			}
 		}
 		selectItem(itemToBeSelected);
 	}
 
 	private void selectPrevItem() {
 		if (selectedItem == null) {
 			return;
 		}
 
 		int index = items.indexOf(selectedItem);
 		// We know that selectedItem is set to an item that is contained in the
 		// items collection.
 		// Therefore, we know that index can never be -1.
 		assert (index != -1);
 
 		VSuggestionMenuItem itemToBeSelected;
 
 		int firstIndex = index;
 		while (true) {
 			index = index - 1;
 			if (index < 0) {
 				// we're at the start, loop around to the end
 				index = items.size() - 1;
 			}
 			if (index == firstIndex) {
 				itemToBeSelected = items.get(firstIndex);
 				break;
 			} else {
 				itemToBeSelected = items.get(index);
 				if (itemToBeSelected.isEnabled()) {
 					break;
 				}
 			}
 		}
 
 		selectItem(itemToBeSelected);
 	}
 
 	void doItemAction(final VSuggestionMenuItem item, boolean fireCommand,
 			boolean focus) {
 		// Should not perform any action if the item is disabled
 		if (!item.isEnabled()) {
 			return;
 		}
 
 		// Ensure that the item is selected.
 		selectItem(item);
 
 		// if the command should be fired and the item has one, fire it
 		if (fireCommand && item.getCommand() != null) {
 			// Close this menu and all of its parents.
 			// closeAllParents();
 
 			// Fire the item's command. The command must be fired in the same
 			// event
 			// loop or popup blockers will prevent popups from opening.
 			final ScheduledCommand cmd = item.getCommand();
 			Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
 				@Override
 				public void execute() {
 					cmd.execute();
 				}
 			});
 			selectItem(null);
 		}
 	}
 
 	public void itemOver(VSuggestionMenuItem item, boolean focus) {
 
 		if (item != null && !item.isEnabled()) {
 			return;
 		}
 
 		// Style the item selected when the mouse enters.
 		selectItem(item);
 		if (focus && focusOnHover) {
 			focus();
 		}
 	}
 
 	@Override
 	public void onBrowserEvent(Event event) {
 		VSuggestionMenuItem item = findItem(DOM.eventGetTarget(event));
 		switch (DOM.eventGetType(event)) {
 		case Event.ONCLICK: {
 			FocusImpl.getFocusImplForPanel().focus(getElement());
 			// Fire an item's command when the user clicks on it.
 			if (item != null) {
 				doItemAction(item, true, true);
 			}
 			break;
 		}
 
 		case Event.ONMOUSEOVER: {
 			if (item != null) {
 				itemOver(item, true);
 			}
 			break;
 		}
 
 		case Event.ONMOUSEOUT: {
 			if (item != null) {
 				itemOver(null, true);
 			}
 			break;
 		}
 
 		case Event.ONFOCUS: {
 			selectFirstItemIfNoneSelected();
 			break;
 		}
 
 		case Event.ONKEYDOWN: {
 //			int keyCode = DOM.eventGetKeyCode(event);
 			int keyCode = event.getKeyCode();
 			switch (keyCode) {
 			case KeyCodes.KEY_UP:
 				moveSelectionUp();
 				eatEvent(event);
 				break;
 			case KeyCodes.KEY_DOWN:
 				moveSelectionDown();
 				eatEvent(event);
 				break;
 			case KeyCodes.KEY_ESCAPE:
 				// closeAllParentsAndChildren();
 				// eatEvent(event);
 				break;
 			case KeyCodes.KEY_TAB:				
 			case KeyCodes.KEY_ENTER:
 				if (!selectFirstItemIfNoneSelected()) {
 					doItemAction(selectedItem, true, true);
 					eatEvent(event);
 				}
 				break;
 			} // end switch(keyCode)
 
 			break;
 		} // end case Event.ONKEYDOWN
 		} // end switch (DOM.eventGetType(event))
 		super.onBrowserEvent(event);
 	}
 
 	private void eatEvent(Event event) {
 //		DOM.eventCancelBubble(event, true);
 		event.stopPropagation();
 		event.preventDefault();
 //		DOM.eventPreventDefault(event);
 	}
 
 	/**
 	 * Get the index of a {@link MenuItem}.
 	 * 
 	 * @return the index of the item, or -1 if it is not contained by this
 	 *         MenuBar
 	 */
 	public int getItemIndex(VSuggestionMenuItem item) {
 		return items.indexOf(item);
 	}
 
 	/**
 	 * Returns the <code>MenuItem</code> that is currently selected
 	 * (highlighted) by the user. If none of the items in the menu are currently
 	 * selected, then <code>null</code> will be returned.
 	 * 
 	 * @return the <code>MenuItem</code> that is currently selected, or
 	 *         <code>null</code> if no items are currently selected
 	 */
 	public VSuggestionMenuItem getSelectedItem() {
 		return this.selectedItem;
 	}
 
 	/**
 	 * Check whether or not this widget will steal keyboard focus when the mouse
 	 * hovers over it.
 	 * 
 	 * @return true if enabled, false if disabled
 	 */
 	public boolean isFocusOnHoverEnabled() {
 		return focusOnHover;
 	}
 
 	/**
 	 * Enable or disable auto focus when the mouse hovers over the MenuBar. This
 	 * allows the MenuBar to respond to keyboard events without the user having
 	 * to click on it, but it will steal focus from other elements on the page.
 	 * Enabled by default.
 	 * 
 	 * @param enabled
 	 *            true to enable, false to disable
 	 */
 	public void setFocusOnHoverEnabled(boolean enabled) {
 		focusOnHover = enabled;
 	}
 
 	/**
 	 * Set the colspan of a {@link MenuItem} or {@link MenuItemSeparator}.
 	 * 
 	 * @param item
 	 *            the {@link MenuItem} or {@link MenuItemSeparator}
 	 * @param colspan
 	 *            the colspan
 	 */
 	private void setItemColSpan(UIObject item, int colspan) {
 //		DOM.setElementPropertyInt(item.getElement(), "colSpan", colspan);
 		item.getElement().getStyle().setPropertyPx("colSpan", colspan);
 	}
 
 	/**
 	 * <b>Affected Elements:</b>
 	 * <ul>
 	 * <li>-item# = the {@link MenuItem} at the specified index.</li>
 	 * </ul>
 	 * 
 	 * @see UIObject#onEnsureDebugId(String)
 	 */
 	@Override
 	protected void onEnsureDebugId(String baseID) {
 		super.onEnsureDebugId(baseID);
 		setMenuItemDebugIds(baseID);
 	}
 
 	/**
 	 * Set the IDs of the menu items.
 	 * 
 	 * @param baseID
 	 *            the base ID
 	 */
 	void setMenuItemDebugIds(String baseID) {
 		int itemCount = 0;
 		for (VSuggestionMenuItem item : items) {
 			item.ensureDebugId(baseID + "-item" + itemCount);
 			itemCount++;
 		}
 	}
 	
 	public List<VSuggestionMenuItem> getItems() {
 		return this.items;
 	}
 
 }
