 package com.pasadya.shop;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.PageableListView;
 import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.request.resource.PackageResourceReference;
 
 import com.pasadya.PasadyaBasePage;
 import com.pasadya.assets.AssetReference;
 import com.pasadya.data.ItemVO;
 import com.pasadya.data.ShopFactory;
 import com.pasadya.data.util.PageParametersConstants;
 
 public class Shop extends PasadyaBasePage implements Serializable {
 
 	private final int MODAL_WINDOW_WIDTH = 800;
 	private final int MODAL_WINDOW_HEIGHT = 600;
 	private static final long serialVersionUID = 1L;
 	private final int ITEMS_PER_PAGE = 6;
 	private String shopSelection;
 	public static final String JAVASCRIPT_ATTRIBUTES;
 
 	public List<ItemVO> allShopItems;
 
 	public IModel<ItemVO> modelObject;
 
 	// JavaScript to be appended to the item modal window
 	static {
 		StringBuilder javascriptBuilder = new StringBuilder();
 
 		javascriptBuilder.append("var thisWindow = Wicket.Window.get();\n");
 		javascriptBuilder.append("thisWindow.center();\n");
 		javascriptBuilder.append("console.log(\"test\")");
 		JAVASCRIPT_ATTRIBUTES = javascriptBuilder.toString();
 	}
 
 	// Shop Constructor - fetch the selected category and call the
 	// displayShopItemList with the parameters
 
 	public Shop(final PageParameters parms) {
 
 		shopSelection = parms.get(PageParametersConstants.SHOP_CATEGORY)
 				.toString();
 
 		allShopItems = ShopFactory.getInstance().getAllShopItems();
 
 		// Load the LDM with the artItems in it
 		LoadableDetachableModel<List<ItemVO>> shopModel = new LoadableDetachableModel<List<ItemVO>>() {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected List<ItemVO> load() {
				if (shopSelection == null) {
					shopSelection = "";
				}
 				return sortItemList(shopSelection, allShopItems);
 			}
 		};
 
 		// Load the page-able list view for the shopping items.
 		PageableListView<ItemVO> shoppingItems = new PageableListView<ItemVO>(
 				"thumbGalleryContainer", shopModel, ITEMS_PER_PAGE) {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(final ListItem<ItemVO> item) {
 				// The modal Window to be displayed
 				final ModalWindow shopModalWindow;
 
 				// This model object will be used to populate the clicked item's
 				// modal page
 				shopModalWindow = new ModalWindow("shopModalWindow",
 						item.getModel());
 				shopModalWindow.setContent(new ShopItemPanel(shopModalWindow
 						.getContentId(), item.getModel()));
 				item.add(shopModalWindow);
 
 				// Add the thumb image to the item.
 				Image itemThumbImage = new Image("itemThumbImage",
 						new PackageResourceReference(AssetReference.class,
 								"/images/shopsmall/NomNomLoveYouThumb.png"));
 
 				// Ajax Link for the modal Window
 				AjaxLink<Void> modalLink;
 				modalLink = new AjaxLink<Void>("modalLink") {
 
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void onClick(AjaxRequestTarget target) {
 
 						target.appendJavaScript(JAVASCRIPT_ATTRIBUTES);
 						shopModalWindow.show(target);
 					}
 				};
 
 				// Modal Window Properties
 
 				shopModalWindow.setInitialWidth(MODAL_WINDOW_WIDTH);
 				shopModalWindow.setInitialHeight(MODAL_WINDOW_HEIGHT);
 				shopModalWindow.setResizable(false);
 				shopModalWindow.setInitialWidth(0);
 				// shopModalWindow.setCssClassName("itemModal");
 
 				// Add the Elements to the page
 				item.add(modalLink);
 				modalLink.add(new Label("itemName", item.getModel().getObject()
 						.getItemName()));
 				modalLink.add(itemThumbImage);
 			}
 		};
 
 		// Add the page-able list view
 		add(shoppingItems);
 
 		// Add the navigator for going between pages
 		add(new PagingNavigator("navigator", shoppingItems));
 	}
 
 	// Go through the Store Items and fetch the ones in the category the user
 	// selected.
 	public List<ItemVO> sortItemList(String selection, List<ItemVO> unsortedList) {
 		List<ItemVO> sortedlist = new ArrayList<ItemVO>();
		if (!selection.equals("")) {
			for (ItemVO item : unsortedList) {
				if (item.itemCategory.equals(selection)) {
					sortedlist.add(item);
				}
			}
		}
		// If there is no category selected, add all of the items.
		else {
			for (ItemVO item : unsortedList) {
 				sortedlist.add(item);
 			}
 		}

 		return sortedlist;
 
 	}
 }
