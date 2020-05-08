 package org.bodytrack.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
 
 /**
  * A widget that allows the user to switch between, add,
  * and modify views.
  */
 // TODO: Make a new SavableView class that represents all pertinent
 // information about a view.  This should be a JavaScript overlay
 // type, which will allow server reading and writing with ease.
 
 // TODO: Keep track of current view, probably in ChannelManager
 
 // TODO: Add a way to change views to the ChannelManager class
 
 // TODO: Add writing capabilities to ViewSavePopupWidget
 
 // TODO: Implement ViewRestorePopupWidget
 
 // TODO: Put the dropdown box of current view names below the text
 // box and button on the save popup
 
 // TODO: On save popup, get the highlighting to work correctly when
 // there is a current view
 
 // TODO: Consistent styling of buttons (use PushButton instead of
 // Button for the popup windows) and lists (drop the scroll bar
 
 // TODO: Comment new code
 
 public class ViewSwitchWidget extends HorizontalPanel {
 	/**
 	 * The ID attribute of this element.  Note that this only
 	 * works properly if there is only one <tt>ViewSwitchWidget</tt>
 	 * per page.
 	 */
 	private static final String WIDGET_ID = "viewSwitchWidget";
 
 	private final int userId;
 	private final ChannelManager channels;
 	private final ViewSwitchClickHandler clickHandler;
 	private final PushButton saveView;
 	private final PushButton restoreView;
 
 	/**
 	 * Creates a new <tt>ViewSwitchWidget</tt>.
 	 *
 	 * @param userId
 	 * 		the ID of the current user
 	 * @param mgr
 	 * 		the {@link org.bodytrack.client.ChannelManager ChannelManager}
 	 * 		that keeps the current set of channels
 	 * @throws NullPointerException
 	 * 		if mgr is <tt>null</tt>
 	 */
 	public ViewSwitchWidget(int userId, ChannelManager mgr) {
 		if (mgr == null)
 			throw new NullPointerException(
 				"Cannot use null ChannelManager");
 
 		this.userId = userId;
 		channels = mgr;
 		clickHandler = new ViewSwitchClickHandler();
 		saveView = new PushButton("Save", clickHandler);
 		restoreView = new PushButton("Restore", clickHandler);
 
 		getElement().setId(WIDGET_ID);
 
 		add(saveView);
 		add(restoreView);
 	}
 
 	/**
 	 * The class that handles clicks for the saveView and restoreView
 	 * buttons.
 	 */
 	private class ViewSwitchClickHandler implements ClickHandler {
 		/**
 		 * Handles clicks for the saveView and restoreView buttons.
 		 *
 		 * <p>If event is <tt>null</tt> or came from a button that
 		 * is not part of this widget, does nothing.  Otherwise,
 		 * opens the correct popup to save or restore the current
 		 * view.</p>
 		 *
 		 * @param event
 		 * 		the click event containing information about the
 		 * 		user's click on the saveView or restoreView button
 		 */
 		@Override
 		public void onClick(ClickEvent event) {
 			if (event == null)
 				return;
 
 			Object sourceObject = event.getSource();
 			if (sourceObject == saveView) {
 				final ViewSavePopup popup = new ViewSavePopup(null);
 				// TODO: Keep track of the current view
 
 				popup.setPopupPositionAndShow(new PositionCallback() {
 					/**
 					 * Called to place popup.
 					 *
 					 * @param offsetWidth
 					 * 		the width of popup
 					 * @param offsetHeight
 					 * 		the height of popup
 					 */
 					@Override
 					public void setPosition(int offsetWidth,
 							int offsetHeight) {
 						int left = saveView.getAbsoluteLeft();
 						int top = saveView.getAbsoluteTop()
 							+ saveView.getOffsetHeight();
 						popup.setPopupPosition(left, top);
 					}
 				});
 			} else if (sourceObject == restoreView) {
 				// TODO: Some restore action here
 			}
 		}
 	}
 
 	/**
 	 * A popup window that lets the user save the current view to
 	 * the server.
 	 *
 	 * <p>This window is designed to pop up once, save the current
 	 * view, and then be garbage collected.  If the user clicks the
 	 * save button multiple times, a new <tt>ViewSavePopup</tt>
 	 * should be created every time.</p>
 	 */
 	private class ViewSavePopup extends PopupPanel {
 		/**
		 * The name of the CSS class on this widget.
		 */
		public static final String CLASS_NAME = "viewSavePopup";

		/**
 		 * The maximum number of view names that will be visible to
 		 * the user at any time.
 		 */
 		private static final int MAX_VISIBLE_VIEW_NAMES = 10;
 
 		private final String currentView;
 		private final List<String> viewNames;
 
 		private final FlowPanel content;
 		private final TextBox saveName;
		private final PushButton save;
 		private final ListBox viewNamesList;
 		// TODO: better variable name
 		// TODO: drop the viewNames variable as a list and just
 		// use the widget
 
 		public ViewSavePopup(String currentView) {
 			super(true, true);
 
 			this.currentView = currentView;
 			viewNames = new ArrayList<String>();
 
 			saveName = new TextBox();
 
			save = new PushButton("Save", new ClickHandler() {
 				/**
 				 * Called whenever save is clicked.
 				 */
 				@Override
 				public void onClick(ClickEvent event) {
 					if (event.getSource() != save)
 						// The above condition should never hold
 						return;
 
 					String viewName = saveName.getText();
 					if (viewName.isEmpty())
 						return;
 
 					// TODO: Write the data to the server here
 				}
 			});
 
 			viewNamesList = new ListBox();
 
 			// Add all the content to the panel
 			content = new FlowPanel();
 			content.add(saveName);
 			content.add(save);
 			setWidget(content);
			addStyleName(CLASS_NAME);
 
 			// Funny ordering because saveName.selectAll() only
 			// works when saveName is attached to the document
 			// and not hidden
 			if (this.currentView != null) {
 				saveName.setText(currentView);
 				saveName.selectAll();
 			}
 
 			loadViewNamesAndShow();
 		}
 
 		/**
 		 * Fills the viewNames private variable with the data loaded
 		 * from the URL pointed to by the url private variable, then
 		 * adds those view names to this popup window.
 		 */
 		private void loadViewNamesAndShow() {
 			// Send request to server and catch any errors.
 			RequestBuilder builder =
 				new RequestBuilder(RequestBuilder.GET, getViewNamesUrl());
 
 			try {
 				builder.sendRequest(null, new RequestCallback() {
 					@Override
 					public void onError(Request request,
 							Throwable exception) {
 						// Nothing to do in this case
 					}
 
 					@Override
 					public void onResponseReceived(Request request,
 							Response response) {
 						if (GrapherTile.isSuccessful(response)) {
 							fillViewNames(response.getText());
 							showViewNames();
 						}
 					}
 				});
 			} catch (RequestException e) {
 				// Nothing to do here
 			}
 		}
 
 		private String getViewNamesUrl() {
 			return "/users/" + userId + "/views.json";
 		}
 
 		private String getViewWriteUrl() {
 			return "/users/" + userId + "/views/set.json";
 		}
 
 		private void showViewNames() {
 			int numViews = viewNames.size();
 			if (numViews == 0)
 				return;
 
 			for (String name: viewNames)
 				viewNamesList.addItem(name);
 
 			viewNamesList.setVisibleItemCount(
 				Math.min(MAX_VISIBLE_VIEW_NAMES, numViews));
 			content.add(viewNamesList);
 		}
 
 		/**
 		 * Fills the viewNames private variable with the list of
 		 * view names.
 		 *
 		 * @param responseBody
 		 * 		the body of the response to our request for view
 		 * 		names
 		 */
 		private void fillViewNames(String responseBody) {
 			JSONValue response = JSONParser.parse(responseBody);
 			JSONArray views = response.isArray();
 			if (views == null)
 				return;
 
 			for (int i = 0; i < views.size(); i++) {
 				JSONValue viewValue = views.get(i);
 				JSONObject viewObject = viewValue.isObject();
 				if (viewObject != null) {
 					// Success - this should always happen
 					JSONValue nameValue = viewObject.get("name");
 					if (nameValue == null)
 						continue;
 					JSONString nameString = nameValue.isString();
 					if (nameString == null)
 						continue;
 					viewNames.add(nameString.toString());
 				} else {
 					// Also accept an array of strings coming from
 					// the server, even though this isn't part of
 					// the API
 					JSONString viewString = viewValue.isString();
 					if (viewString != null)
 						viewNames.add(viewString.toString());
 				}
 			}
 		}
 	}
 
 	private class ViewRestorePopup extends PopupPanel {
 		// TODO: Implement
 	}
 }
