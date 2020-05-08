 package org.visico.neighborhoodpss.gwt.client;
 
 
 import java.text.ParseException;
 
 import org.visico.neighborhoodpss.gwt.shared.patterns.ScenarioEditMediator;
 import org.visico.neighborhoodpss.domain.project.GeoNetworkDTO;
 import org.visico.neighborhoodpss.domain.project.NetworkDTO;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DoubleBox;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class EditMapPanel extends Composite implements ClickHandler, ChangeHandler
 {
 	//private ScenarioPanel scenarioPanel;
 	
 	private final VerticalPanel mainPanel = new VerticalPanel();
 	private final ToggleButton addBuildingBtn = new ToggleButton("Add Building");
 	private final Button addNetworkBtn = new Button("Add Network");
 	private final NetworkTable network_tbl;
 	private final Button deleteNetworkBtn = new Button("Delete Network");;
 	private final DoubleBox inflow_tb = new DoubleBox();
 	private final DoubleBox outflow_tb = new DoubleBox();
 	private final ToggleButton addNode_btn = new ToggleButton("Add Node");
 	private final DoubleBox capacity_tb = new DoubleBox();
 	private final ToggleButton addEdge_btn = new ToggleButton("Add Edge");
 	private Label selectedLbl = new Label("Selected: 0 nodes; 0 edges; 0 buildings;");
 	private final Button changeSelectedBtn = new Button("Change Selected Elements");
 	private final Button deleteSelectedBtn = new Button("Delete Selected Elements");
 	private final Button deleteHangingNodesBtn = new Button("Delete Hanging Nodes");
 	private final Button editAddElementData = new Button ("Edit Data");
 	
 	
 	
 	ScenarioEditMediator med;
 	
 	public EditMapPanel(ScenarioPanel scenarioPanel, ScenarioEditMediator med)
 	{
 		this.med = med;
 		med.registerEditNetworkPanel(this);
 		network_tbl = new NetworkTable(med);
 		draw();
 	}
 	
 	
 	
 	public void draw()
 	{
 		VerticalPanel addBuildingPanel = new VerticalPanel();
 		addBuildingPanel.setStyleName("boundedVPanel");
 		addBuildingPanel.add(addBuildingBtn);
 		addBuildingBtn.addClickHandler(this);
 		addBuildingBtn.addStyleName("normalButton");
 		mainPanel.add(addBuildingPanel);
 		
 		VerticalPanel addNetwPanel = new VerticalPanel();
 		addNetwPanel.setStyleName("boundedVPanel");
 		addNetwPanel.add(addNetworkBtn);
 		addNetworkBtn.setStyleName("normalButton");
 		addNetworkBtn.addClickHandler(this);
 		mainPanel.add(addNetwPanel);
 		
 		VerticalPanel addTblPanel = new VerticalPanel();
 		Label netwCaption = new Label("Networks:");
 		netwCaption.setStyleName("captionLabel");
 		addTblPanel.add(netwCaption);
 		addTblPanel.setStyleName("boundedVPanel");
 		addTblPanel.add(network_tbl);
 		addTblPanel.add(deleteNetworkBtn);
 		deleteNetworkBtn.addClickHandler(this);
 		mainPanel.add(addTblPanel);
 		
 		VerticalPanel addBtnPanel = new VerticalPanel();
 		addBtnPanel.setStyleName("boundedVPanel");
 		addBtnPanel.add(addNode_btn);
 		addNode_btn.addClickHandler(this);
 		addBtnPanel.add(addEdge_btn);
 		addEdge_btn.addClickHandler(this);
 		mainPanel.add(addBtnPanel);
 		
 		VerticalPanel netwkPropCompPanel = new VerticalPanel();
 		netwkPropCompPanel.setStyleName("boundedVPanel");
 		Label caption = new Label("Element properties:");
 		caption.setStyleName("captionLabel");
 		netwkPropCompPanel.add(caption);
 		netwkPropCompPanel.add(new Label("inflow node"));
 		netwkPropCompPanel.add(inflow_tb);
 		inflow_tb.addChangeHandler(this);
 		netwkPropCompPanel.add(new Label("outflow node"));
 		netwkPropCompPanel.add(outflow_tb);
 		outflow_tb.addChangeHandler(this);
 		netwkPropCompPanel.add(new Label("capacity edge"));
 		netwkPropCompPanel.add(capacity_tb);
 		capacity_tb.addChangeHandler(this);
 		mainPanel.add(netwkPropCompPanel);
 		
 		VerticalPanel editNetworkPanel = new VerticalPanel();
 		editNetworkPanel.setStyleName("boundedVPanel");
 		Label captionEdit = new Label("Edit components");
 		captionEdit.setStyleName("captionLabel");
 		editNetworkPanel.add(captionEdit);
 		selectedLbl.setStyleName("informLabel");
 		editNetworkPanel.add(selectedLbl);
 		changeSelectedBtn.setStyleName("normalButton");
 		changeSelectedBtn.addClickHandler(this);
 		editNetworkPanel.add(changeSelectedBtn);
 		changeSelectedBtn.addClickHandler(this);
 		deleteSelectedBtn.setStyleName("normalButton");
 		deleteSelectedBtn.addClickHandler(this);
 		editNetworkPanel.add(deleteSelectedBtn);
 		deleteHangingNodesBtn.setStyleName("normalButton");
 		deleteHangingNodesBtn.addClickHandler(this);
 		editNetworkPanel.add(deleteHangingNodesBtn);
 		editAddElementData.setStyleName("normalButton");
 		editAddElementData.addClickHandler(this);
 		editNetworkPanel.add(editAddElementData);
 
 		mainPanel.add(editNetworkPanel);
 		
 		this.initWidget(mainPanel);	
 	}
 
 	@Override
 	public void onClick(ClickEvent event) {
 		if (event.getSource() == addNetworkBtn)
 		{
 			AddNetworkDlg nwdlg = new AddNetworkDlg(med);
 			nwdlg.show();
 		}
 		else if (event.getSource() == addBuildingBtn)
 		{
 			if (addBuildingBtn.isDown())
 			{
 				med.addBuildingMode();
 				addEdge_btn.setDown(false);
 				addNode_btn.setDown(false);
 			}
 			else
 			{
				med.selectionMode();
 			}
 		}
 		else if (event.getSource() == addNode_btn)
 		{
 			if (med.getSelectedNetwork() == null)
 			{
 				med.noMode();
 				addEdge_btn.setDown(false);
 				addNode_btn.setDown(false);
 				addBuildingBtn.setDown(false);
 			}
 			else if (addNode_btn.isDown() )
 			{
 				med.addNodeMode();
 				addEdge_btn.setDown(false);
 				addBuildingBtn.setDown(false);
 			}
 			else
 			{
 				med.selectionMode();
 			}
 		}
 		else if (event.getSource() == addEdge_btn )
 		{
 			if (med.getSelectedNetwork() == null)
 			{
 				med.noMode();
 				addEdge_btn.setDown(false);
 				addNode_btn.setDown(false);
 				addBuildingBtn.setDown(false);
 			}
 			else if (addEdge_btn.isDown())
 			{
 				med.addEdgeMode();
 				addNode_btn.setDown(false);
 				addBuildingBtn.setDown(false);
 			}
 			else
 			{
 				med.selectionMode();
 			}
 		}
 		else if (event.getSource() == deleteNetworkBtn)
 		{
 			//TODO: warning box
 			NetworkDTO networkToDelete = network_tbl.getSelectedNetwork();
 			if (Window.confirm("Really delete network "+ networkToDelete.getName()))
 			{
 				med.deleteGeoNetwork((GeoNetworkDTO) networkToDelete);
 				//else if (networkToDelete instanceof BuildingNetworkDTO)
 				//	scenarioPanel.scenario().deleteBuildingNetwork((BuildingNetworkDTO) networkToDelete);
 			}
 			
 		}
 		else if (event.getSource() == changeSelectedBtn)
 		{
 			if (Window.confirm("Really change all selected network elements?"))
 			{
 				med.changeSelected();
 			}
 		}
 		else if (event.getSource() == deleteSelectedBtn)
 		{
 			if (Window.confirm("Really delete all selected buildings and network elements?"))
 			{
 				med.deleteSelected();
 			}
 		}
 		else if (event.getSource() == deleteHangingNodesBtn)
 		{
 			if (Window.confirm("Really delete all not connected nodes of this network?"))
 			{
 				med.deleteHangingNodes();
 			}
 		}
 		else if (event.getSource() == editAddElementData)
 		{
 			med.editAddElementData();
 		}
 	}
 
 	public void cleanEditModes() {
 		addNode_btn.setDown(false);
 		addEdge_btn.setDown(false);
 	}
 	
 	@Override
 	public void onChange(ChangeEvent event) {
 		try
 		{
 			if (event.getSource() == inflow_tb)
 			{
 				med.setInflow(inflow_tb.getValueOrThrow());
 			}
 			else if (event.getSource() == outflow_tb)
 			{
 				med.setOutflow(outflow_tb.getValueOrThrow());
 			}
 			else if (event.getSource() == capacity_tb)
 			{
 				med.setCapacity(capacity_tb.getValueOrThrow());
 			}
 		}
 	 	catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			Window.alert("Please enter only numerical values!");
 		}
 		
 	}
 
 	public void changeSelectedLabel(int selectedNodes, int selectedEdges, int selectedBuildings)
 	{
 		String newText = "Selected: " + selectedNodes + " nodes; " 
 				+ selectedEdges + " edges; " 
 				+ selectedBuildings + " buildings;";
 		selectedLbl.setText(newText);
 	}
 
 
 }
