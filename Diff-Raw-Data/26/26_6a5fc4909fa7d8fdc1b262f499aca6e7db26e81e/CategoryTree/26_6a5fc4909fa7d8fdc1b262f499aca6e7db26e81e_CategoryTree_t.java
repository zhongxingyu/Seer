 package org.smartsnip.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.smartsnip.shared.ICategory;
 import org.smartsnip.shared.NoAccessException;
 import org.smartsnip.shared.NotFoundException;
 import org.smartsnip.shared.XCategory;
 
 import com.google.gwt.event.logical.shared.OpenEvent;
 import com.google.gwt.event.logical.shared.OpenHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.MenuItem;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.Tree;
 import com.google.gwt.user.client.ui.TreeItem;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * Tree for manipulating the categories
  * 
  * @author Felix Niederwanger
  * 
  */
 public class CategoryTree extends Composite {
 
 	/* Components */
 	private final VerticalPanel pnlVertCategories;
 	private final Label lblCategories;
 	private final Tree treeCategories;
 	/* End of components */
 
 	/** Root items or null, if not yet fetched */
 	private List<CategoryTreeItem> rootItems = null;
 
 	/** Class providing access to a popupmenu for the categories */
 	private static class PopupMenuCategory {
 
 		/** Associated tree item */
 		private final CategoryTreeItem item;
 
 		private PopupMenuCategory(CategoryTreeItem item) {
 			this.item = item;
 
			popupPanel.setAutoHideEnabled(true);

 			popupMenu.addItem(popupAdd);
 			popupMenu.addItem(popupEdit);
 			popupMenu.addItem(popupDelete);
 			popupMenu.setVisible(true);
 
 			popupPanel.add(popupMenu);
 
 			applyStyle();
 		}
 
 		/** Apply style theme */
 		private void applyStyle() {
 			popupAdd.setStyleName("popup-item");
 			popupEdit.setStyleName("popup-item");
 			popupDelete.setStyleName("popup-item");
 		}
 
 		/* Popup menu */
 		private final PopupPanel popupPanel = new PopupPanel();
 
 		private final MenuBar popupMenu = new MenuBar(true);
 		private final MenuItem popupAdd = new MenuItem("Add new category", false, new Command() {
 
 			@Override
 			public void execute() {
 				close();
 
 				Category.showCreateCategoryPopup();
 			}
 		});
 		private final MenuItem popupEdit = new MenuItem("Edit", false, new Command() {
 
 			@Override
 			public void execute() {
 				close();
 				item.edit();
 			}
 		});
 		private final MenuItem popupDelete = new MenuItem("Delete", false, new Command() {
 
 			@Override
 			public void execute() {
 				close();
 
 				Control.myGUI.showConfirmPopup("Really delete the category " + item.category.name + "?", new ConfirmCallback() {
 
 					@Override
 					public void onYes() {
 						item.delete();
 					}
 
 					@Override
 					public void onNo() {
 					}
 				});
 			}
 		});
 
 		/* End of popup menu */
 
 		/**
 		 * Shows the popup menu for a {@link CategoryTreeItem}
 		 * 
 		 * @param item
 		 *            for the popup menu
 		 * @param x
 		 *            x-coordinate the menu will be shown
 		 * @param y
 		 *            y-coordinate the menu will be shown
 		 */
 		public static void showPopup(CategoryTreeItem item, int x, int y) {
 			if (item == null)
 				return;
 			PopupMenuCategory popup = new PopupMenuCategory(item);
 			popup.show(x, y);
 		}
 
 		/**
 		 * Show pupop menu
 		 * 
 		 * @param x
 		 *            x-coordinate the menu will be shown
 		 * @param y
 		 *            y-coordinate the menu will be shown
 		 * */
 		private void show(int x, int y) {
 			popupPanel.setPopupPosition(x, y);
 			popupPanel.setAutoHideEnabled(true);
 			popupPanel.setModal(false);
 			popupPanel.show();
 
 		}
 
 		/** Close the popup menu */
 		private void close() {
 			popupPanel.hide();
 		}
 
 	}
 
 	/** Extended tree item for categories */
 	private static class CategoryTreeItem extends TreeItem {
 		/** Associated category */
 		private final XCategory category;
 		/** If all subchildren are fetched or not */
 		private boolean fetched = false;
 
 		/** If currently fetching */
 		private boolean fetching = false;
 
 		/** Child categories or null, if not yet fetched */
 		private List<CategoryTreeItem> subCategories = null;
 
 		/** Subitem is needed so that a item is shown as expandable */
 		private final TreeItem fetchItem = new TreeItem("Fetching ... ");
 
 		/** Initialises a extended tree item for the category */
 		private CategoryTreeItem(XCategory category) {
 			this.category = category;
 
 			if (category == null)
 				super.setText("Root");
 			else
 				super.setText(category.name);
 			super.addItem(fetchItem);
 		}
 
 		/** Fetches the subchildren */
 		public void fetchChildren() {
 			if (fetching)
 				return;
 			if (fetched)
 				return;
 
 			setStatus("Fetching children ... ");
 			this.removeItem(fetchItem);
 			fetching = true;
 			ICategory.Util.getInstance().getCategories((category == null ? null : category.name), new AsyncCallback<List<XCategory>>() {
 
 				@Override
 				public void onSuccess(List<XCategory> result) {
 					fetching = false;
 					if (result == null) {
 						onFailure(new IllegalArgumentException("Null returned"));
 						return;
 					}
 					setStatus("");
 
 					CategoryTreeItem parent = CategoryTreeItem.this;
 					subCategories = new ArrayList<CategoryTreeItem>();
 					for (XCategory childCategory : result) {
 						CategoryTreeItem child = new CategoryTreeItem(childCategory);
 						parent.addItem(child);
 						subCategories.add(child);
 					}
 				}
 
 				@Override
 				public void onFailure(Throwable caught) {
 					fetching = false;
 					String message;
 					if (caught == null)
 						message = "Unknown error";
 					else if (caught instanceof NoAccessException)
 						message = "Access denial";
 					else if (caught instanceof NotFoundException)
 						message = "Not found";
 					else
 						message = caught.getMessage();
 
 					setStatus(message);
 					fetchItem.setText("Error: " + message);
 					CategoryTreeItem.this.addItem(fetchItem);
 					fetching = false;
 					fetched = false;
 				}
 			});
 
 			fetched = true;
 		}
 
 		/**
 		 * Sets a status message for this tree item
 		 * 
 		 * @param text
 		 *            status message to be set
 		 */
 		private void setStatus(String text) {
 			if (text == null || text.isEmpty()) {
 				super.setText(category.name);
 				fetchItem.setText("");
 			} else {
 				super.setText(category.name + "(" + text + ")");
 				fetchItem.setText(text);
 			}
 		}
 
 		/**
 		 * @return if subtree is fetched
 		 */
 		public boolean isFetched() {
 			return fetched;
 		}
 
 		/** Shows the edit dialog for a category */
 		public void edit() {
 			Category.showEditCategoryPopup(category);
 		}
 
 		/**
 		 * Deletes this category item with all child categories
 		 */
 		public void delete() {
 			setStatus("Deleting ...");
 			fetching = true;
 
 			ICategory.Util.getInstance().delete(category.name, new AsyncCallback<Void>() {
 
 				@Override
 				public void onFailure(Throwable caught) {
 					fetching = false;
 				}
 
 				@Override
 				public void onSuccess(Void result) {
 					setStatus("Deleted");
 					removeItem(CategoryTreeItem.this);
 				}
 			});
 		}
 	}
 
 	public CategoryTree() {
 		pnlVertCategories = new VerticalPanel();
 		pnlVertCategories.setSize("5cm", "3cm");
 
 		lblCategories = new Label("Categories: 0");
 		pnlVertCategories.add(lblCategories);
 
 		treeCategories = new Tree();
 		pnlVertCategories.add(treeCategories);
 
 		treeCategories.addOpenHandler(new OpenHandler<TreeItem>() {
 
 			@Override
 			public void onOpen(OpenEvent<TreeItem> event) {
 				TreeItem item = event.getTarget();
 				if (item instanceof CategoryTreeItem) {
 					CategoryTreeItem categoryItem = (CategoryTreeItem) item;
 					if (!categoryItem.isFetched())
 						categoryItem.fetchChildren();
 				}
 			}
 		});
 		sinkEvents(Event.ONMOUSEUP | Event.ONDBLCLICK | Event.ONCONTEXTMENU);
 
 		pnlVertCategories.add(lblCategories);
 		pnlVertCategories.add(treeCategories);
 
 		treeCategories.addItem(new CategoryTreeItem(null));
 
 		initWidget(pnlVertCategories);
 	}
 
 	/**
 	 * Catch right mous button, this is a browser event
 	 */
 	@Override
 	public void onBrowserEvent(Event event) {
 		event.cancelBubble(true); // Stop the event, do not propagate any more
 		event.preventDefault();
 
 		switch (DOM.eventGetType(event)) {
 		case Event.ONMOUSEUP:
 			if (DOM.eventGetButton(event) == Event.BUTTON_LEFT) {
 				// Left button - Ignore
 			}
 
 			if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
 				// Right button - Also ignore, because we use
 				// Event.ONCONTEXTMENU
 			}
 			break;
 		case Event.ONDBLCLICK:
 			break;
 
 		case Event.ONCONTEXTMENU:
 			// Show context menu
 			TreeItem item = treeCategories.getSelectedItem();
 			if (item != null && (item instanceof CategoryTreeItem)) {
 				CategoryTreeItem catItem = (CategoryTreeItem) item;
 				int x = DOM.eventGetClientX(event);
 				int y = DOM.eventGetClientY(event);
 				PopupMenuCategory.showPopup(catItem, x, y);
 			}
 
 		default:
 			break; // Do nothing
 		}// end switch
 	}
 
 	/**
 	 * Removes a {@link CategoryTreeItem} from the tree
 	 * 
 	 * @param item
 	 *            to be removed
 	 */
 	private void removeItem(CategoryTreeItem item) {
 		if (item == null)
 			return;
 		treeCategories.removeItem(item);
 	}
 
 	/**
 	 * Updates the component
 	 */
 	public void update() {
 		// Get root categories
 		lblCategories.setText("Getting categories ... ");
 		treeCategories.clear();
 		ICategory.Util.getInstance().getCategories(null, new AsyncCallback<List<XCategory>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				if (caught == null)
 					lblCategories.setText("Unknown error while updating");
 				else if (caught instanceof NoAccessException)
 					lblCategories.setText("Update failed: Access denied");
 				else
 					lblCategories.setText("Update failed: " + caught.getMessage());
 			}
 
 			@Override
 			public void onSuccess(List<XCategory> result) {
 				if (result == null) {
 					onFailure(new IllegalArgumentException("Null returned"));
 					return;
 				}
 
 				rootItems = new ArrayList<CategoryTree.CategoryTreeItem>();
 				for (XCategory category : result) {
 					CategoryTreeItem item = createTreeItem(category);
 					treeCategories.addItem(item);
 					rootItems.add(item);
 				}
 
 				int size = result.size();
 				lblCategories.setText((size == 1 ? "1 category fetched" : size + " categories fetched"));
 			}
 
 			/**
 			 * Creates a tree item for a category
 			 * 
 			 * @param category
 			 *            the tree item should be created for
 			 * @return the created tree item
 			 */
 			private CategoryTreeItem createTreeItem(final XCategory category) {
 				final CategoryTreeItem result = new CategoryTreeItem(category);
 				return result;
 			}
 		});
 	}
 }
