 /**
  * 
  */
 package org.cotrix.web.manage.client.codelist.codes.marker.menu;
 
 import org.cotrix.web.common.client.widgets.menu.AbstractMenuItem;
 import org.cotrix.web.common.client.widgets.menu.CheckMenuGroup;
 import org.cotrix.web.common.client.widgets.menu.FlatMenuBar;
 import org.cotrix.web.manage.client.codelist.codes.marker.MarkerType;
 import org.cotrix.web.manage.client.codelist.codes.marker.style.MarkerStyleProvider;
 import org.cotrix.web.manage.client.resources.CotrixManagerResources;
 import org.cotrix.web.manage.client.resources.CotrixManagerResources.MenuStyle;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.ui.DecoratedPopupPanel;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.UIObject;
 import com.google.inject.Inject;
import com.google.inject.Singleton;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
@Singleton
 public class MarkerMenuImpl extends DecoratedPopupPanel implements MarkerMenu {
 	
 	private Listener listener;
 	
 	private MenuStyle style = CotrixManagerResources.INSTANCE.menuStyle();
 
 	@Inject
 	public MarkerMenuImpl(MarkerStyleProvider styleProvider) {
 		super(true, false);
 		
 		FlatMenuBar menuBar = new FlatMenuBar(true);
 		menuBar.setStyleName(style.menuBar());
 		
 		CheckMenuGroup markerGroup = new CheckMenuGroup();
 		for (MarkerType marker:MarkerType.values()) {
 			MarkerMenuItem menuItem = new MarkerMenuItem(marker.getName(), marker, styleProvider.getStyle(marker));
 			menuItem.setStyleName(style.menuItem());
 			menuItem.setSelectedItemStyleName(style.menuItemSelected());
 			
 			markerGroup.add(menuItem);
 		}
 		
 		markerGroup.addSelectionHandler(new SelectionHandler<AbstractMenuItem>() {
 			
 			@Override
 			public void onSelection(SelectionEvent<AbstractMenuItem> event) {
 				onMarkerSelection(event);
 			}
 		});
 		
 		menuBar.addGroup(markerGroup);
 		
 		setWidget(menuBar);
 		setStyleName(style.menuPopup());
 		
 		addCloseHandler(new CloseHandler<PopupPanel>() {
 			
 			@Override
 			public void onClose(CloseEvent<PopupPanel> event) {
 				if (listener!=null) listener.onHide();
 			}
 		});
 	}
 
 	private void onMarkerSelection(SelectionEvent<AbstractMenuItem> event) {
 		MarkerMenuItem menuItem = (MarkerMenuItem)event.getSelectedItem();
 		Log.trace("selected "+menuItem.getValue());
 
 		fireButtonClick(menuItem.getValue(), menuItem.isSelected());
 		hide();
 	}
 	
 	private void fireButtonClick(MarkerType marker, boolean selected) {
 		if (listener!=null) listener.onButtonClicked(marker, selected);
 	}
 	
 	@Override
 	public void show(UIObject target) {
 		showRelativeTo(target);
 	}
 
 	@Override
 	public void hide() {
 		super.hide();
 	}
 
 	@Override
 	public void setListener(Listener listener) {
 		this.listener = listener;
 	}
 
 }
