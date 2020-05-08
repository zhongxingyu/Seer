 package ch.unibas.gwt.torrus.client;
 
 import java.util.ArrayList;
 
 import ch.unibas.gwt.torrus.client.config.ConfigHandler;
 import ch.unibas.gwt.torrus.client.helper.TorrusUrl;
 import ch.unibas.gwt.torrus.client.widget.TorrusImage;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.IntegerBox;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Gwt_torrus implements EntryPoint {
 
 	ArrayList<TorrusImage> images;
 	private Grid grid;
 	private ArrayList<String> servers;
 	private ArrayList<String> values;
 	private RootPanel rootPanel;
 
 	public class TitleWidget extends VerticalPanel {
 
 
 		public TitleWidget(final String title, final ArrayList<String> list,
 				String removeKey) {
 			this(title, null, list, removeKey);
 		}
 
 		public TitleWidget(final String title, String link,
 				final ArrayList<String> list, final String removeKey) {
 			super();
 			Anchor remove = new Anchor("Remove");
 			remove.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					list.remove(removeKey);
 					updateGrid();
 				}
 			});
 
 			if (link != null) {
 				Anchor a = new Anchor(title);
 				a.setHref(TorrusUrl.buidlServerUrl(link));
 				add(a);
 				add(remove);
 			} else {
 				Label tbTitle = new Label(title);
 				add(tbTitle);
 				add(remove);
 			}
 		}
 
 	}
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 		images = new ArrayList<TorrusImage>();
 		String configName = Window.Location.getParameter("dataset");
 		updateConfig(configName);
 
 		rootPanel = RootPanel.get("nameFieldContainer");
 		updateGrid();
 
 		final ListBox comboBoxView = new ListBox();
 		comboBoxView.addChangeHandler(new ChangeHandler() {
 			public void onChange(ChangeEvent event) {
 				int selectedIndex = comboBoxView.getSelectedIndex();
				if (selectedIndex > 0) {
 					String v = comboBoxView.getValue(selectedIndex);
 					for (TorrusImage img : images) {
 						img.setView(v);
 					}
 				}
 
 			}
 		});
 
 		comboBoxView.addItem("last24h");
		comboBoxView.addItem("last24h");
 		comboBoxView.addItem("lastweek");
 		comboBoxView.addItem("lastmonth");
 		comboBoxView.addItem("lastyear");
 
 		rootPanel.add(comboBoxView, 48, 0);
 
 		Label lblNewLabel = new Label("View: ");
 		rootPanel.add(lblNewLabel, 10, 4);
 
 		Label lblZoom = new Label("Zoom");
 		rootPanel.add(lblZoom, 150, 4);
 
 		final IntegerBox integerBox = new IntegerBox();
 		integerBox.setValue(50);
 		integerBox.addChangeHandler(new ChangeHandler() {
 			public void onChange(ChangeEvent event) {
 				Integer percent = integerBox.getValue();
 				float p = percent / 100f;
 				for (TorrusImage img : images) {
 					img.scaleImages(p);
 				}
 			}
 		});
 		rootPanel.add(integerBox, 189, 2);
 		integerBox.setSize("47px", "16px");
 
 		Label label = new Label("%");
 		rootPanel.add(label, 246, 4);
 
 		Label lblDataset = new Label("Dataset:");
 		rootPanel.add(lblDataset, 277, 4);
 
 		final ListBox comboBoxDataset = new ListBox();
 		ConfigHandler.setSetAvailableConfigs(comboBoxDataset);
 		comboBoxDataset.addChangeHandler(new ChangeHandler() {
 			public void onChange(ChangeEvent event) {
 				int selectedIndex = comboBoxDataset.getSelectedIndex();
				if (selectedIndex > 0) {
 					String ds = comboBoxDataset.getValue(selectedIndex);
 					updateConfig(ds);
 					updateGrid();
 				}
 			}
 		});
 		rootPanel.add(comboBoxDataset, 332, 0);
 
 	}
 
 	private void updateConfig(String name) {
 		ConfigHandler config = ConfigHandler.getConfig(name);
 		servers = config.getServers();
 		values = config.getValues();
 	}
 
 	private void updateGrid() {
 		if (grid != null) {
 			grid.clear(true);
 		}
 		grid = new Grid(servers.size() + 1, values.size() + 1);
 		grid.setText(0, 0, "server name");
 		for (int i = 0; i < values.size(); i++) {
 			String value = values.get(i);
 			grid.setWidget(0, i + 1, new TitleWidget(value, values, value));
 			// grid.setText(0, i + 1, value);
 		}
 
 		for (int i = 0; i < servers.size(); i++) {
 			String serverName = servers.get(i);
 			String shortServerName = serverName.substring(0,
 					serverName.indexOf("."));
 			grid.setWidget(i + 1, 0, new TitleWidget(shortServerName,
 					serverName, servers, serverName));
 			for (int j = 0; j < values.size(); j++) {
 				grid.setWidget(i + 1, j + 1,
 						getImage(serverName, values.get(j)));
 			}
 		}
 		rootPanel.add(grid, 0, 32);
 	}
 
 	private Widget getImage(String serverName, String value) {
 		TorrusImage image = new TorrusImage(serverName, value, "last24h");
 
 		image.scaleImages(.5f);
 		images.add(image);
 		return image;
 	}
 }
