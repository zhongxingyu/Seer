 package org.iucn.sis.shared.api.utils.clipboard;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
 import com.extjs.gxt.ui.client.widget.tree.Tree;
 import com.extjs.gxt.ui.client.widget.tree.TreeItem;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.StyledHTML;
 
 /**
  * Clipboard.java
  * 
  * Holds multiple instances of simple text/html content, provides UI for
  * viewing, removing, and pasting clipboard items.
  * 
  * @author carl.scott
  * 
  */
 @SuppressWarnings("deprecation")
 public class Clipboard {
 
 	/**
 	 * ClipboardItem.java
 	 * 
 	 * Representation of an item on the clipboard.
 	 * 
 	 * @author carl.scott
 	 * 
 	 */
 	private class ClipboardItem extends LayoutContainer {
 
 		private String text;
 		private String source;
 
 		private LayoutContainer headerInstance;
 
 		/**
 		 * Creates a new clipboard item object
 		 * 
 		 * @param text
 		 *            the text to paste
 		 * @param source
 		 *            the source/group the item is in
 		 */
 		public ClipboardItem(String text, String source) {
 			super();
 
 			this.text = text;
 			this.source = source;
 
 			VerticalPanel left = new VerticalPanel();
 			left.add(new StyledHTML("<b>" + text + "</b>", "fontSize60"));
 
 			Image remove = new Image("tango/places/user-trash.png");
 			remove.setTitle("Remove from clipboard");
 			remove.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					removeItem();
 				}
 			});
 
 			FlexTable table = new FlexTable();
 			table.setCellSpacing(2);
 			table.setWidth("100%");
 			table.setWidget(0, 0, left);
 			table.setWidget(0, 1, remove);
 			table.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT,
 					HasVerticalAlignment.ALIGN_BOTTOM);
 			table.getCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT,
 					HasVerticalAlignment.ALIGN_BOTTOM);
 
 			add(table);
 
 			sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
 		}
 
 		public String getClipboardText() {
 			return text;
 		}
 
 		@Override
 		public void onBrowserEvent(Event evt) {
 			switch (DOM.eventGetType(evt)) {
 			case Event.ONMOUSEOUT: {
 				removeStyleName("clipboardSpacer");
 				break;
 			}
 			case Event.ONMOUSEOVER: {
 				addStyleName("clipboardSpacer");
 				break;
 			}
 			default:
 				super.onBrowserEvent(evt);
 			}
 		}
 
 		/**
 		 * Removes an item from the clipboard.
 		 * 
 		 */
 		public void removeFromClipboard() {
 			ArrayList<ClipboardItem> list = clipboard.get(source);
 			list.remove(this);
 			if (list.isEmpty())
 				clipboard.remove(source);
 		}
 
 		/**
 		 * Removes an item from the clipboard and from the UI
 		 * 
 		 */
 		public void removeItem() {
 			removeFromClipboard();
 
 			LayoutContainer parent = (LayoutContainer) getParent();
 			parent.remove(this);
 
			if ((clipboard.get(source)).isEmpty() && headerInstance != null)
				headerInstance.remove(headerInstance);
 
 			if (clipboard.isEmpty()) {
 				removeAll();
 				add(new StyledHTML("The clipboard is empty.", "fontSize80"));
 			}
 		}
 
 		public void setHeader(LayoutContainer header) {
 			this.headerInstance = header;
 		}
 	}
 
 	/**
 	 * ClipboardPasteCallback
 	 * 
 	 * Callback that allows paste() function to callback and give the
 	 * appropriate items to paste.
 	 * 
 	 * @author carl.scott
 	 * 
 	 */
 	public static abstract class ClipboardPasteCallback {
 		public abstract void onPaste(ArrayList<Object> items);
 	}
 
 	private static final String PASTE_TEXT_KEY = "PASTE_TEXT";
 	private static final String CLIPBOARD_ITEM_KEY = "CLIP";
 
 	private static final int FULL_POPUP_WIDTH = 300;
 
 	private static final int FULL_POPUP_HEIGHT = 400;
 	private static Clipboard instance;
 
 	/**
 	 * Retrieves the instance of the clipboard
 	 * 
 	 * @return the clipboard object
 	 */
 	public static Clipboard getInstance() {
 		if (instance == null)
 			instance = new Clipboard();
 		return instance;
 	}
 
 	private HashMap<String, ArrayList<ClipboardItem>> clipboard;
 
	private AccordionLayout expand;

 	/**
 	 * Creates a new clipboard object
 	 * 
 	 */
 	private Clipboard() {
 		clipboard = new HashMap<String, ArrayList<ClipboardItem>>();
 	}
 
 	/**
 	 * Adds an item to the clipboard, given text and an unique source, or group.
 	 * The clipboard will create a new group and add the text to it or, if it
 	 * exists already, add the text to the given group.
 	 * 
 	 * @param text
 	 *            the clipboard text
 	 * @param source
 	 *            the unique group or label.
 	 */
 	public void add(String text, String source) {
 		ArrayList<ClipboardItem> list;
 		if (!clipboard.containsKey(source)) {
 			list = new ArrayList<ClipboardItem>();
 			clipboard.put(source, list);
 		}
 		clipboard.get(source).add(new ClipboardItem(text, source));
 	}
 
 	/**
 	 * Returns the clipboard in HashMap form.
 	 * 
 	 * @return <String, ArrayList<ClipboardItem>> clipboard
 	 */
 	public HashMap<String, ArrayList<ClipboardItem>> getClipboard() {
 		return clipboard;
 	}
 
 	/**
 	 * Determines if this clipboard is empty
 	 * 
 	 * @return true if empty, false otherwise
 	 */
 	public boolean isEmpty() {
 		return clipboard.isEmpty();
 	}
 
 	/**
 	 * Determines what needs to be pasted from the clipboard and callsback the
 	 * list of items.
 	 * 
 	 * TODO: use TreePanel
 	 * 
 	 * @param callback
 	 *            the callback to notify that pasting is complete
 	 */
 	public void pasteConditions(final ClipboardPasteCallback callback) {
 		final Window popup = WindowUtils.getWindow(false, false, "");
 
 		final Tree tree = new Tree();
 		tree.setCheckable(true);
 
 		Iterator<String> iterator = Clipboard.getInstance().getClipboard().keySet().iterator();
 		while (iterator.hasNext()) {
 			String source = iterator.next();
 
 			TreeItem curItem = new TreeItem(source);
 			tree.getRootItem().add(curItem);
 
 			ArrayList<ClipboardItem> textList = clipboard.get(source);
 			for (int i = 0; i < textList.size(); i++) {
 				ClipboardItem cItem = (ClipboardItem) textList.get(i);
 
 				TreeItem item = new TreeItem(cItem.getClipboardText());
 				item.setData(PASTE_TEXT_KEY, cItem.getClipboardText());
 				item.setData(CLIPBOARD_ITEM_KEY, cItem);
 
 				curItem.add(item);
 			}
 
 		}
 
 		LayoutContainer container = popup;
 		container.setSize(FULL_POPUP_WIDTH, FULL_POPUP_HEIGHT);
 		container.setLayout(new BorderLayout());
 
 		final CheckBox checkbox = new CheckBox("Keep contents on clipboard after paste.");
 		checkbox.setValue(true);
 
 		FlowLayout topLayout = new FlowLayout();
 		// topLayout.setSpacing(4);
 		LayoutContainer topContainer = new LayoutContainer();
 		topContainer.setLayout(topLayout);
 		topContainer.add(new StyledHTML("You have multiple items on the "
 				+ "clipboard.  <br/>Please select which ones you'd like to paste.", "fontSize60"));
 		topContainer.add(checkbox);
 
 		container.add(topContainer, new BorderLayoutData(LayoutRegion.NORTH, 100, 100, 100));
 
 		LayoutContainer treePanel = new LayoutContainer();
 		treePanel.setSize(FULL_POPUP_WIDTH - 30, 210);
 		treePanel.setScrollMode(Scroll.AUTO);
 		treePanel.add(tree);
 		container.add(treePanel, new BorderLayoutData(LayoutRegion.CENTER, 210, 210, 210));
 
 		final Button paste = new Button("Paste Selection", new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				final List<TreeItem> checked = tree.getChecked();
 				if (checked.size() == 0) {
 					WindowUtils.errorAlert("Error", "Please select at least one entry");
 				} else {
 					popup.hide();
 					ArrayList<Object> list = new ArrayList<Object>();
 					for (Iterator<TreeItem> iter = checked.listIterator(); iter.hasNext();) {
 						TreeItem cur = iter.next();
 						Object text = cur.getData(PASTE_TEXT_KEY);
 						if (text != null) {
 							list.add(text);
 							if (!checkbox.getValue())
 								((ClipboardItem) cur.getData(CLIPBOARD_ITEM_KEY)).removeFromClipboard();
 						}
 					}
 					callback.onPaste(list);
 				}
 			}
 		});
 		final Button cancel = new Button("Cancel", new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				popup.hide();
 			}
 		});
 
 		FlexTable buttonPanel = new FlexTable();
 		buttonPanel.setWidget(0, 0, paste);
 		buttonPanel.setWidget(0, 1, cancel);
 		buttonPanel.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER,
 				HasVerticalAlignment.ALIGN_MIDDLE);
 		buttonPanel.getCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER,
 				HasVerticalAlignment.ALIGN_MIDDLE);
 
 		container.add(buttonPanel, new BorderLayoutData(LayoutRegion.SOUTH, 30, 30, 30));
 
 		popup.setHeading("Paste From Clipboard");
 		popup.show();
 	}
 
 	/**
 	 * Opens the clipboard UI
 	 * 
 	 */
 	public void show() {
 		final Window popup = WindowUtils.getWindow(false, false, "Clipboard");
 
		LayoutContainer itemPanel = new LayoutContainer();
		expand = new AccordionLayout();
		itemPanel.setLayout(expand);
 
 		Iterator<String> iterator = clipboard.keySet().iterator();
 		while (iterator.hasNext()) {
 			String source = (String) iterator.next();
 			ArrayList<ClipboardItem> textList = clipboard.get(source);
 
 			ContentPanel expandItem = new ContentPanel();
 			expandItem.setHeading(source);
 
			for (int i = 0; i < textList.size(); i++) {
				ClipboardItem item = (ClipboardItem) textList.get(i);
 				// if (i % 2 == 0)
 				// item.addStyleName("clipboardSpacer");
 				item.setHeader(itemPanel);
 				expandItem.add(item);
 			}
 
 			expandItem.setExpanded(true);
 			itemPanel.add(expandItem);
 		}
 
 		LayoutContainer content = new LayoutContainer();
 		content.setScrollMode(Scroll.AUTO);
 		content.add(itemPanel);
 		content.setSize(FULL_POPUP_WIDTH - 15, FULL_POPUP_HEIGHT - 40);
 
 		LayoutContainer fullPanel = popup;
 
 		if (clipboard.isEmpty())
 			fullPanel.add(itemPanel);
 		else {
 			fullPanel.setLayoutOnChange(true);
 			fullPanel.add(content);
 		}
 
 		popup.setSize(FULL_POPUP_WIDTH, FULL_POPUP_HEIGHT);
 		popup.show();
 	}
 
 	/**
 	 * Returns the aggregate size of the clipboard, that is, the total number of
 	 * items in the board, as opposed to the number of groups (keyset size)
 	 * 
 	 * @return the size
 	 */
 	public int size() {
 		int size = 0;
 		Iterator<ArrayList<ClipboardItem>> iterator = clipboard.values().iterator();
 		while (iterator.hasNext()) {
 			size += iterator.next().size();
 		}
 		return size;
 	}
 }
