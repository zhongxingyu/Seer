 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ch.cern.atlas.apvs.client.Build;
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.widget.HorizontalFlowPanel;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.AttachEvent;
 import com.google.gwt.event.logical.shared.AttachEvent.Handler;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TabPanel;
 
 public class SupervisorView extends DockPanel {
 
 	private ClientFactory clientFactory;
 	private ScrollPanel mainScrollPanel;
 	private int windowWidth;
 	private int windowHeight;
 
 	private List<Button> deleteButtons = new ArrayList<Button>();
 	DockPanel mainPanel = new DockPanel();
 
 	public SupervisorView(final ClientFactory clientFactory) {
 		this.clientFactory = clientFactory;
 		RemoteEventBus remoteEventBus = clientFactory.getEventBus();
 
 		HorizontalFlowPanel header = new HorizontalFlowPanel();
 		add(header, NORTH);
 
 		Image logo = new Image("ATLASLogo2-64x48.jpg");
 		logo.addStyleName("apvs-logo");
 		header.add(logo);
 
		Label title = new Label("Atlas Procedures Visualization System");
 		title.addStyleName("apvs-title");
 		header.add(title);
 
 		Build build = GWT.create(Build.class);
 		Label footer = new Label("Version: " + build.version() + " - " + build.build()); 
 		footer.addStyleName("apvs-version");
 		add(footer, SOUTH);
 
 		TabPanel tabPanel = new TabPanel();
 		add(tabPanel, NORTH);
 
 		createWorker();
 
 		HorizontalFlowPanel buttonPanel = new HorizontalFlowPanel();
 		mainPanel.add(buttonPanel, SOUTH);
 		Button addWorker = new Button("Add Worker");
 		addWorker.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				createWorker();
 			}
 		});
 
 		buttonPanel.add(addWorker);
 
 		mainScrollPanel = new ScrollPanel(mainPanel);
 		tabPanel.add(mainScrollPanel, "Workers");
 
 		tabPanel.add(new ProcedurePanel(remoteEventBus, 800, 600), "Procedures");
 		tabPanel.add(new PtuView(clientFactory), "PTUs");
 		tabPanel.add(new DosimeterView(remoteEventBus), "Dosimeters");
 		tabPanel.add(new SupervisorSettingsView(remoteEventBus),
 				"Supervisor Settings");
 		tabPanel.add(new ServerSettingsView(remoteEventBus), "Server Settings");
 
 		tabPanel.selectTab(0);
 
 		// Save the initial size of the browser.
 		windowWidth = Window.getClientWidth();
 		windowHeight = Window.getClientHeight();
 
 		// Add a listener for browser resize events.
 		Window.addResizeHandler(new ResizeHandler() {
 
 			@Override
 			public void onResize(ResizeEvent event) {
 				// Save the new size of the browser.
 				windowWidth = event.getWidth();
 				windowHeight = event.getHeight();
 				// Reformat everything for the new browser size.
 				resize();
 			}
 		});
 
 		mainScrollPanel.addAttachHandler(new Handler() {
 
 			@Override
 			public void onAttachOrDetach(AttachEvent event) {
 				resize();
 			}
 		});
 
 		resize();
 	}
 
 	private void createWorker() {
 		final Button deleteButton = new Button("Delete");
 		final SupervisorWorkerView workerView = new SupervisorWorkerView(
 				clientFactory, deleteButton);
 
 		deleteButtons.add(deleteButton);
 		deleteButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				String name = workerView.getName() == null ? "PtuId: "
 						+ workerView.getPtuId() : workerView.getName();
 				if ((workerView.getPtuId() == null)
 						|| Window
 								.confirm("Are you sure you want to delete the view of worker: "
 										+ name)) {
 					mainPanel.remove(workerView);
 					deleteButtons.remove(deleteButton);
 					updateDeleteButtons();
 				}
 			}
 		});
 
 		updateDeleteButtons();
 
 		mainPanel.add(workerView, NORTH);
 		resize();
 	}
 
 	private void updateDeleteButtons() {
 		for (Button button : deleteButtons) {
 			button.setEnabled(deleteButtons.size() > 1);
 		}
 	}
 
 	private void resize() {
 		if (mainScrollPanel == null)
 			return;
 		// Set the size of main body scroll panel so that it fills the
 		mainScrollPanel.setSize(
 				Math.max(windowWidth - mainScrollPanel.getAbsoluteLeft(), 0)
 						+ "px",
 				Math.max(windowHeight - mainScrollPanel.getAbsoluteTop() - 25,
 						0) + "px");
 	}
 
 }
