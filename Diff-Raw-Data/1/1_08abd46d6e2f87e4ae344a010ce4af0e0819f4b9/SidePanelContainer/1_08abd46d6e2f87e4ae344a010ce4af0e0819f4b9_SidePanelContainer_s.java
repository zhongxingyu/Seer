 package org.cotrix.web.manage.client.codelist.common.side;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.cotrix.web.common.client.widgets.ToggleButtonGroup;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.DeckLayoutPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.Widget;
 
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class SidePanelContainer extends ResizeComposite {
 
 	interface Binder extends UiBinder<Widget, SidePanelContainer> {}
 	
 	private static Binder uiBinder = GWT.create(Binder.class);
 	
 	interface Style extends CssResource {
 		String button();
 	}
 	
 	@UiField Style style;
 	
 	@UiField HorizontalPanel buttonBar;
 	
 	@UiField DeckLayoutPanel panels;
 	
 	@UiField EmptyPanel emptyPanel;
 	
 	
 	private ToggleButtonGroup buttonGroup = new ToggleButtonGroup();
 	private Map<Widget, ToggleButton> panelsIndex = new HashMap<Widget, ToggleButton>();
 	
 	private Widget lastVisualized;
 
 	public SidePanelContainer() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 	
 	public void addPanel(ImageResource tabButton, ImageResource unselectedTabButton, String tabTitle, final Widget panel) {
 		ToggleButton button = createButton(tabButton, unselectedTabButton, tabTitle);
 		buttonBar.add(button);
 		
 		button.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				panels.showWidget(panel);
 			}
 		});
 		
 		panels.add(panel);
 		panels.showWidget(panel);
 		panelsIndex.put(panel, button);
 	}
 	
 	private ToggleButton createButton(ImageResource tabButton, ImageResource unselectedTabButton, String tabTitle) {
 		ToggleButton pushButton = new ToggleButton(new Image(unselectedTabButton), new Image(tabButton));
 		pushButton.getUpHoveringFace().setImage(new Image(tabButton));
 		pushButton.setTitle(tabTitle);
 		pushButton.setStyleName(style.button());
 		
 		buttonGroup.addButton(pushButton);
 		
 		return pushButton;
 	}
 
 	public void showEmptyPanel(boolean visible) {
 		
 		if (visible) {
 			lastVisualized = panels.getVisibleWidget();
 			panels.showWidget(emptyPanel);
 			buttonGroup.setAllDown(false);
 			buttonGroup.setEnabled(false);
 		} else {
 			showPanel(lastVisualized);
 			buttonGroup.setEnabled(true);
 		}
 	}
 	
 	public void showPanel(Widget panel) {
 		panels.showWidget(panel);
 		buttonGroup.setDown(panelsIndex.get(panel));
 	}
 	
 	public void setEmptyPanelMessage(String message) {
 		emptyPanel.setMessage(message);
 	}
 }
