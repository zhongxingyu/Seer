 package net.jorgeherskovic.medrec.client;
 
 import net.jorgeherskovic.medrec.client.event.FinishedLoadingEvent;
 import net.jorgeherskovic.medrec.client.event.RedrawEvent;
 import net.jorgeherskovic.medrec.client.event.FinishedLoadingEventHandler;
 import net.jorgeherskovic.medrec.shared.Reconciliation;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalSplitPanel;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 @SuppressWarnings("deprecation")
 public class MedRec implements EntryPoint {
 	/**
 	 * The message displayed to the user when the server cannot be reached or
 	 * returns an error.
 	 */
 	/**
 	 * This is the entry point method.
 	 */
 	private static final String JSON_URL = "sample.json";
 	private Reconciliation myData;
 	private final SimpleEventBus bus = new SimpleEventBus();
 
 	public void onModuleLoad() {
 		// TODO Auto-generated method stub
 		final RootPanel rootPanel = RootPanel.get("insert_app_here");
 		//rootPanel.setSize("800px", "600px");
 
 		final String[] consolidatedHeadings = new String[] { "&nbsp;",
 				"Origin", "Medication", "Dosage", "Freq.", "Start", "End",
 				"Form", "Relation" };
 		final String[] reconciledHeadings = new String[] { "&nbsp;", "Origin",
 				"Medication", "Dosage", "Freq.", "Start", "End", "Form",
 				"Alerts" };
 
 		rootPanel.getElement().getStyle().setPosition(Position.RELATIVE); // Fix required for drag & drop
 		
 		final AbsolutePanel absolutePanel = new AbsolutePanel();
 		
 		rootPanel.add(absolutePanel);
 		//absolutePanel.setSize("800px", "600px");
 		FlexTableRowDragController row_dc = new FlexTableRowDragController(
 				absolutePanel);
 
 		final VerticalSplitPanel vsPanel = new VerticalSplitPanel();
 		absolutePanel.add(vsPanel);
 		vsPanel.setSize("800px", "600px");
 
 		final AbsolutePanel absolutePanel_1 = new AbsolutePanel();
 		absolutePanel_1.setStyleName("provenance");
 		// absolutePanel.add(absolutePanel_1, 0, 0);
 		absolutePanel_1.setSize("800px", "52px");
 
 		Label lblNewLabel = new Label("List 1 comes from");
 		absolutePanel_1.add(lblNewLabel, 10, 0);
 
 		TextBox list1origin = new TextBox();
 		absolutePanel_1.add(list1origin, 10, 18);
 		list1origin.setSize("291px", "18px");
 
 		Label lblListComes = new Label("List 2 comes from");
 		absolutePanel_1.add(lblListComes, 489, 0);
 		lblListComes.setSize("105px", "18px");
 
 		TextBox list2origin = new TextBox();
 		absolutePanel_1.add(list2origin, 489, 18);
 		list2origin.setSize("291px", "18px");
 
 		vsPanel.setTopWidget(absolutePanel_1);
 		vsPanel.setSplitPosition("52px");
 
 		final VerticalSplitPanel rest = new VerticalSplitPanel();
 		rest.setSize("800px", "548px");
 		vsPanel.setBottomWidget(rest);
 		rest.setSplitPosition("505px");
 
 		final VerticalSplitPanel rest_of_rest = new VerticalSplitPanel();
 		rest_of_rest.setSize("800px", "504px");
 		final AbsolutePanel consolidatedPanel = new AbsolutePanel();
 		// absolutePanel.add(consolidatedPanel, 0, 58);
 		consolidatedPanel.setSize("800px", "252px");
 		rest_of_rest.setTopWidget(consolidatedPanel);
 		rest.setTopWidget(rest_of_rest);
 
 		final DockPanel dockPanel = new DockPanel();
 		dockPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		consolidatedPanel.add(dockPanel, 0, 0);
 		dockPanel.setSize("800px", "252px");
 
		Label lblConsolidatedRecord = new Label("Consolidated Record");
 		lblConsolidatedRecord.setStyleName("big-label");
 		lblConsolidatedRecord
 				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		dockPanel.add(lblConsolidatedRecord, DockPanel.NORTH);
 
 		final DraggableFlexTable consolidatedTable = new DraggableFlexTable(
 				row_dc, null);
 		consolidatedTable.setStyleName("TableDesign");
 		dockPanel.add(consolidatedTable, DockPanel.CENTER);
 		FlexTableRowDropController ct_dc = new FlexTableRowDropController(
 				consolidatedTable, this.bus);
 		@SuppressWarnings("unused")
 		ConsolidatedRenderer conRenderer = new ConsolidatedRenderer(
 				consolidatedTable, consolidatedHeadings, bus);
 
 		final AbsolutePanel bottomPanel = new AbsolutePanel();
 		bottomPanel.setSize("800px", "30px");
 		rest.setBottomWidget(bottomPanel);
 
 		Button btnDone = new Button("Done");
 		bottomPanel.add(btnDone, 737, 0);
 		btnDone.setSize("53px", "30px");
 
 		final AbsolutePanel reconciledPanel = new AbsolutePanel();
 		// absolutePanel.add(reconciledPanel, 0, 316);
 		reconciledPanel.setSize("800px", "252px");
 		rest_of_rest.add(reconciledPanel);
 
 		final DockPanel dockPanel_1 = new DockPanel();
 		dockPanel_1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		reconciledPanel.add(dockPanel_1, 0, 0);
 		dockPanel_1.setSize("800px", "252px");
 
 		Label lblReconciledRecord = new Label("Reconciled Record");
 		lblReconciledRecord.setStyleName("big-label");
 		lblReconciledRecord
 				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		dockPanel_1.add(lblReconciledRecord, DockPanel.NORTH);
 
 		final DraggableFlexTable reconciledTable = new DraggableFlexTable(
 				row_dc, null);
 		reconciledTable.setStyleName("TableDesign");
 		dockPanel_1.add(reconciledTable, DockPanel.CENTER);
 		FlexTableRowDropController rc_dc = new FlexTableRowDropController(
 				reconciledTable, bus);
 
 		@SuppressWarnings("unused")
 		ReconciledRenderer recRenderer = new ReconciledRenderer(
 				reconciledTable, reconciledHeadings, bus);
 
 		/* Resize behavior */
 		Window.addResizeHandler(new ResizeHandler() {
 			public void onResize(ResizeEvent event) {
 				int height=event.getHeight();
 				int width=event.getWidth();
 				String new_width=width+"px";
 				
 				rootPanel.setHeight(height+"px");
 				rootPanel.setWidth(new_width);
 				absolutePanel.setHeight(height+"px");
 				absolutePanel.setWidth(new_width);
 				vsPanel.setHeight(height+"px");
 				vsPanel.setWidth(new_width);
 				absolutePanel_1.setWidth(new_width);
 				rest.setWidth(new_width);
 				rest.setHeight((height-52)+"px");
 				rest_of_rest.setWidth(new_width);
 				rest_of_rest.setHeight((height-52-44)+"px");
 				consolidatedPanel.setWidth(new_width);
 				dockPanel.setWidth(new_width);
 				dockPanel_1.setWidth(new_width);
 				bottomPanel.setWidth(new_width);
 				reconciledPanel.setWidth(new_width);
 			}
 		});
 
 		/* Instantiate drop controllers */
 
 		/* Connect the drop controllers to the drag controller */
 		row_dc.registerDropController(ct_dc);
 		row_dc.registerDropController(rc_dc);
 
 		bus.addHandler(FinishedLoadingEvent.TYPE,
 				new FinishedLoadingEventHandler() {
 
 					public void onFinishedLoading(FinishedLoadingEvent event) {
 						reconciledTable.associateMedList(myData
 								.getReconciledMeds());
 						consolidatedTable.associateMedList(myData
 								.getConsolidatedMeds());
 
 						RootPanel.get("LoadingLabel").setVisible(false);
 
 						bus.fireEvent(new RedrawEvent());
 					}
 
 				});
 
 		/* Read data */
 		this.myData = new MedRecJSONData(JSON_URL, bus);
 		//this.myData = new SampleData();
 		//this.bus.fireEvent(new FinishedLoadingEvent());
 	}
 
 }
