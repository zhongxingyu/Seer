 package com.tranhoangdai.korengui.client.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.github.gwtbootstrap.client.ui.TabPane;
 import com.github.gwtbootstrap.client.ui.TabPanel;
 import com.github.gwtbootstrap.client.ui.TabPanel.ShownEvent;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import com.tranhoangdai.korengui.client.Korengui;
 import com.tranhoangdai.korengui.client.model.Host;
 import com.tranhoangdai.korengui.client.model.Link;
 import com.tranhoangdai.korengui.client.model.Switch;
 
 public class TablePanel extends Composite implements TabPanel.ShownEvent.Handler {
 
 	private static InfoPanelUiBinder uiBinder = GWT.create(InfoPanelUiBinder.class);
 
 	interface InfoPanelUiBinder extends UiBinder<Widget, TablePanel> {
 	}
 
 	List<TabPane> tabs = new ArrayList<TabPane>();
 
 	@UiField
 	InfoPane globalInfoPane;
 
 	@UiField
 	TabPanel panel;
 	@UiField
 	TabPane firstTabPane;
 
 	public TablePanel() {
 		initWidget(uiBinder.createAndBindUi(this));
 		tabs.add(firstTabPane);
 		panel.addShownHandler(this);
 	}
 
 	public void showGlobalTopology() {
 		globalInfoPane.showGlobalTopology();
 	}
 
 	public void showZoomTopology(Switch zoomModel, Map<String, Host> childHosts, Map<Integer, Link> childLinks) {
 
 		for (TabPane tp : tabs) {
 			if (tp.getHeading().equals("Zoom:" + zoomModel.getId())) {
 				panel.selectTab(tabs.indexOf(tp));
 				return;
 			}
 		}
 
 		// should add the pane before drawing, since the drawing uses location of parent object
 		TabPane zoomPane = new TabPane("Zoom:" + zoomModel.getDpid());
 		panel.add(zoomPane);
 		tabs.add(zoomPane);
 		panel.selectTab(tabs.indexOf(zoomPane));
 
 		InfoPane zoomInfoPane = new InfoPane();
 		zoomPane.add(zoomInfoPane);
 		zoomInfoPane.showZoomTopology(zoomModel, childHosts, childLinks);
 
 	}
 
 	public int hasTab(TabPane tab) {
 
 		return tabs.indexOf(tab);
 	}
 
 	public TabPanel getPanel() {
 		return panel;
 	}
 
 	public void setPanel(TabPanel panel) {
 		this.panel = panel;
 	}
 
 	@Override
 	public void onShow(ShownEvent shownEvent) {
		Korengui.INSTANCE.getEventBus().deliverEventUserSwitchPanelTab(shownEvent.getTarget().getTabPane());
 
 	}
 
 }
