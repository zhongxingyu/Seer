 package com.kdcloud.client.gwt.mvc;
 
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.kdcloud.server.entity.Dataset;
 
 public class DetailsPanel extends VerticalPanel implements ViewComponent {
 	
 	Model model;
 	Controller controller;
 	
 	Widget defaultView;
 	
 	public DetailsPanel(Model model, Controller controller) {
 		super();
 		this.model = model;
 		this.controller = controller;
 		
 		this.defaultView = new Label("click on a dataset to see the infos");
 		this.defaultView.setStyleName("topPadding");
 		this.add(defaultView);
 		this.setSpacing(10);
 	}
 	
 	private void showDetails(final Dataset dataset) {
 		this.add(new HTML("<h3>"+dataset.getName()+"</h3>"));
 		
 		this.add(new Label("the following user can send data to this table:"));
 		
 	    TextCell textCell = new TextCell();
 	    CellList<String> cellList = new CellList<String>(textCell);
	    cellList.setRowCount(Model.COMMITTERS.size(), true);
	    cellList.setRowData(0, Model.COMMITTERS);
 	    cellList.setStyleName("leftPadding");
 	    this.add(cellList);
 	    
 	    this.add(new Hyperlink("Share with others..", null));
 	    
 	    Button deleteButton = new Button("Delete Dataset");
 	    deleteButton.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				controller.onDatasetDeletion(dataset);
 			}
 		});
 	    this.add(deleteButton);
 	}
 	
 	public void refresh() {
 		this.clear();
 		Dataset dataset = model.selectedDataset;
 		if (dataset != null) {
 			showDetails(dataset);
 		} else {
 			add(defaultView);
 		}
 	}
 
 }
