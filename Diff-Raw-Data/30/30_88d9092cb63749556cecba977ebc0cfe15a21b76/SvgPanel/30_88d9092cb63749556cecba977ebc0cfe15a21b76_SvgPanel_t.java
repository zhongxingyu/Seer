 package com.tranhoangdai.korengui.client.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.github.gwtbootstrap.client.ui.TabPane;
 import com.github.gwtbootstrap.client.ui.TabPanel;
 import com.github.gwtbootstrap.client.ui.event.ShownEvent;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import com.tranhoangdai.korengui.client.Korengui;
 import com.tranhoangdai.korengui.client.model.Host;
 import com.tranhoangdai.korengui.client.model.Link;
 import com.tranhoangdai.korengui.client.model.Switch;
 
 public class SvgPanel extends Composite implements TabPanel.ShownEvent.Handler {
 
 	private static SvgPanelUiBinder uiBinder = GWT.create(SvgPanelUiBinder.class);
 
 	interface SvgPanelUiBinder extends UiBinder<Widget, SvgPanel> {
 	}
 
 	List<TabPane> tabs = new ArrayList<TabPane>();
 
 	@UiField
 	TabPanel panel;
 	@UiField
 	TabPane firstTabPane;
 	@UiField
 	DrawingPane globalDrawingPane;
 
 	public SvgPanel() {
 		initWidget(uiBinder.createAndBindUi(this));
 		tabs.add(firstTabPane);
 		panel.addShownHandler(this);
 
 	}
 
 	public void showGlobalTopology() {
 		globalDrawingPane.drawGlobalTopology();
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
 
 		DrawingPane zoomDrawingPane = new DrawingPane();
 		zoomPane.add(zoomDrawingPane);
 		zoomDrawingPane.drawZoom(zoomModel, childHosts, childLinks);
 
 	}
 
 	public int hasTab(TabPane tab) {
 		int index = tabs.indexOf(tab);
 		return index;
 	}
 
 	public TabPanel getPanel() {
 		return panel;
 	}
 
 	@Override
 	public void onShow(com.github.gwtbootstrap.client.ui.TabPanel.ShownEvent shownEvent) {
		try {
			if (tabs.contains(shownEvent.getTarget().getTabPane())) {
				Korengui.INSTANCE.getEventBus().deliverEventUserSwitchPanelTab(shownEvent.getTarget().getTabPane());
			}

		} catch (Exception e) {
			//do nothing
		}
 
 	}
 
 }
