 package com.vaadin.toolkitdraw;
 
 import java.awt.Color;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.event.ItemClickEvent.ItemClickListener;
 import com.vaadin.toolkitdraw.components.paintcanvas.Layer;
 import com.vaadin.toolkitdraw.components.paintcanvas.PaintCanvas;
 import com.vaadin.toolkitdraw.util.IconFactory;
 import com.vaadin.toolkitdraw.util.IconFactory.Icons;
 import com.vaadin.ui.Accordion;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Layout;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 
 public class RightPanel extends VerticalLayout implements Property.ValueChangeListener, ClickListener, ItemClickListener{
 
 	private static final long serialVersionUID = 1L;
 
 	private PaintCanvas canvas;	
 	
 	private GridLayout tab1;
 	private VerticalLayout tab2;
 	
 	private TextField paperHeight;
 	private TextField paperWidth;
 
 	private Table layerTable;
 	private Button addLayer;
 	private Button removeLayer;
 	private Button upLayer;
 	private Button downLayer;
 	
 	private Accordion tabs;
 	
 	private TabSheet histograms;
 	private Map<String, PaintCanvas> histogramCanvases = new HashMap<String, PaintCanvas>();
 	
 	public RightPanel(PaintCanvas canvas){
 		
 		super();
 		setStyleName("rightpanel");
 		
 		this.canvas = canvas;
 		
 		//Add the histograms
 		addComponent(createHistogramsTab());
 		setExpandRatio(histograms, 1);
 		
 		//Add the tabs
 		this.tabs = new Accordion();
 		this.tabs.setSizeFull();
 		addComponent(this.tabs);
 		setExpandRatio(this.tabs, 2);
 		
 		//Create the layers tab
 		this.tabs.addComponent(createLayersTab());
 				
 		//Create the paper options
 		this.tabs.addComponent(createPaperOptionsTab());
 	}
 	
 	private void createHistograms()
 	{
 		//Create RGB channel histogram
 		PaintCanvas rgb = new PaintCanvas("300px","250px", new Color(Integer.parseInt("44",16),
 																	 Integer.parseInt("44",16),
 																	 Integer.parseInt("44",16) ));
 		;	
 		
 		String caption = "Histogram";
 		histogramCanvases.put(caption, rgb);		
 	}
 	
 	private Component createHistogramsTab()
 	{
 		createHistograms();		
 		
 		histograms = new TabSheet();
 		histograms.setSizeFull();		
 		
 		for(String caption : histogramCanvases.keySet()){			
 			PaintCanvas c = histogramCanvases.get(caption);
 			c.setVisible(false);
 						
 			VerticalLayout layout = new VerticalLayout();
 			layout.setSizeFull();
 			layout.addComponent(c);
 			layout.setCaption(caption);
 			
 			histograms.addTab(layout);
 		}
 		
 		return histograms;
 	}
 	
 	private Component createPaperOptionsTab(){
 		GridLayout grid = new GridLayout(4,1);
 		grid.setCaption("Paper Options");	
 		
 		grid.addComponent(new Label("Paper dimensions "),0,0);	
 		
 		paperWidth = new TextField();
 		paperWidth.setValue(300);		
 		if(this.canvas != null) this.canvas.getInteractive().setPaperWidth(300);
 		paperWidth.setWidth("50px");
 		paperWidth.addListener((Property.ValueChangeListener)this);
 		paperWidth.setImmediate(true);
 		grid.addComponent(paperWidth,1,0);
 		
 		grid.addComponent(new Label("x"),2,0);
 		
 		paperHeight = new TextField();
 		paperHeight.setValue(400);
 		if(this.canvas != null) this.canvas.getInteractive().setPaperHeight(400);
 		paperHeight.setWidth("50px");
 		paperHeight.addListener((Property.ValueChangeListener)this);
 		paperHeight.setImmediate(true);
 		grid.addComponent(paperHeight,3,0);
 		
 		tab1 = grid;
 		return tab1;
 	}
 	
 	private Component createLayersTab()
 	{
 		tab2 = new VerticalLayout();		
 		tab2.setCaption("Layers");	
 		tab2.setSizeFull();
 			
 		HorizontalLayout layerTools = new HorizontalLayout();
 		
 		addLayer = new Button();
 		addLayer.setDescription("Add layer");
 		addLayer.setIcon(IconFactory.getIcon(Icons.PLUS));
 		addLayer.setStyleName(Button.STYLE_LINK);
 		addLayer.addListener((ClickListener)this);
 		layerTools.addComponent(addLayer);
 		
 		removeLayer = new Button();
 		removeLayer.setDescription("Remove layer");
 		removeLayer.setIcon(IconFactory.getIcon(Icons.MINUS));
 		removeLayer.setStyleName(Button.STYLE_LINK);
 		removeLayer.addListener((ClickListener)this);
 		layerTools.addComponent(removeLayer);	
 		
 		upLayer = new Button();
 		upLayer.setDescription("Move layer up");
 		upLayer.setIcon(IconFactory.getIcon(Icons.UP_ARROW));
 		upLayer.setStyleName(Button.STYLE_LINK);
 		upLayer.addListener((ClickListener)this);
 		layerTools.addComponent(upLayer);
 		
 		downLayer = new Button();
 		downLayer.setDescription("Move layer down");
 		downLayer.setIcon(IconFactory.getIcon(Icons.DOWN_ARROW));
 		downLayer.setStyleName(Button.STYLE_LINK);
 		downLayer.addListener((ClickListener)this);
 		layerTools.addComponent(downLayer);	
 		
 		HorizontalLayout layerToolsContainer = new HorizontalLayout();
 		layerToolsContainer.setSizeFull();
 		layerToolsContainer.addComponent(layerTools);
 		layerToolsContainer.setComponentAlignment(layerTools, Alignment.MIDDLE_LEFT);
 		tab2.addComponent(layerToolsContainer);
 		tab2.setExpandRatio(layerToolsContainer, 1);
 		
 		layerTable = new Table();
 		layerTable.setStyleName("layer-table");
 		layerTable.setSizeFull();
 		layerTable.setSelectable(true);
 		layerTable.addListener((ItemClickListener)this);
 		layerTable.addContainerProperty("Visible", CheckBox.class, null);		
 		layerTable.setColumnWidth("Visible", 30);
 		layerTable.setColumnHeader("Visible", "");
 		layerTable.addContainerProperty("Name",String.class, "");
		layerTable.setNullSelectionAllowed(false);
 						
 		tab2.addComponent(layerTable);	
 		tab2.setExpandRatio(layerTable, 20);
 		
 		return tab2;
 	}
 	
 	private void refreshLayers(){
 		
 		if(this.canvas == null) return;
 		
 		//Show the histogram
 		for(String caption : histogramCanvases.keySet()){			
 			PaintCanvas c = histogramCanvases.get(caption);
 			c.setVisible(true);
 		}
 		
 		List<Layer> layers = this.canvas.getLayers().getLayers();
 		
  		layerTable.removeAllItems();
 		
 		for(Layer layer : layers){			
 			Item item = layerTable.addItem(layer);
 					
 			//A failue occured
 			if(item == null){
 				System.out.println("ERROR: Could not add layer"+layer.getName()+" to layer table");
 				return;
 			}
 							
 			item.getItemProperty("Name").setValue(layer.getName());
 			
 			final CheckBox visible = new CheckBox();
 			visible.setData(layer);
 			visible.setImmediate(true);
 			visible.setValue(layer.getVisible());
 			visible.addListener(new Button.ClickListener(){
 				public void buttonClick(ClickEvent event) {
 					if(event.getButton().getValue() == null) return;
 					
 					boolean state = Boolean.valueOf(event.getButton().getValue().toString());
 					Layer layer = (Layer) visible.getData();
 					layer.setVisible(state);
 				}				
 			});
 						
 			item.getItemProperty("Visible").setValue(visible);			
 		}
 				
		layerTable.select(canvas.getLayers().getActiveLayer());
 		layerTable.requestRepaint();
 	}
 
 	@Override
 	public void valueChange(ValueChangeEvent event) {
 		
 		if(event.getProperty() == paperWidth){
 			canvas.getInteractive().setPaperWidth(Integer.parseInt(event.getProperty().getValue().toString()));
 		}
 		
 		if(event.getProperty() == paperHeight){
 			canvas.getInteractive().setPaperHeight(Integer.parseInt(event.getProperty().getValue().toString()));
 		}
 		
 	}
 
 	@Override
 	public void buttonClick(ClickEvent event) {
 		if(this.canvas == null) return;
 		
 		if(event.getButton() == addLayer){			
 		
 			List<Layer> layers = this.canvas.getLayers().getLayers();
 												
 			//The layer is added automatically to the canvas
 			Layer newLayer = new Layer("Layer #"+(layers.size()+1), this.canvas);						
 			this.canvas.getLayers().addLayer(newLayer);						
 			
 			refreshLayers();
 			
 			layerTable.select(newLayer);
 			
 		}else if(event.getButton() == removeLayer && layerTable.getValue() != null){
 			
 			
 			
 			
 		}else if(event.getButton() == upLayer && layerTable.getValue() != null ){
 			Layer selected = (Layer)layerTable.getValue();
 			List<Layer> layers = this.canvas.getLayers().getLayers();			
 			
 			//Do not move an element behind the Background element
 			int idx = layers.indexOf(selected);
 			if(idx <= 1) return;
 			
 			//Swap the layers in both the table and the image component
 			this.canvas.getLayers().moveLayerUp(selected.getName());
 			Collections.swap(layers, idx, idx-1);
 			
 			refreshLayers();			
 			layerTable.select(selected);
 			
 		}else if(event.getButton() == downLayer && layerTable.getValue() != null){
 			Layer selected = (Layer)layerTable.getValue();
 			List<Layer> layers = this.canvas.getLayers().getLayers();
 			
 			//Do not move the Background element or the last element
 			int idx = layers.indexOf(selected);
 			if(idx == layers.size()-1 || idx == 0) return;
 			
 			//Swap the layers in both the table and the image component
 			this.canvas.getLayers().moveLayerDown(selected.getName());
 			Collections.swap(layers, idx, idx+1);
 			
 			refreshLayers();
 			layerTable.select(selected);
 		}		
 	}
 	
 	@Override
 	public void itemClick(ItemClickEvent event) {
 		if(event.getItemId() == null) return;
 		
 		Layer layer = (Layer)event.getItemId();
 		layer.getCanvas().getLayers().setActiveLayer(layer);
 	}
 	
 	public PaintCanvas getCanvas() {
 		return canvas;
 	}
 
 	public void setCanvas(PaintCanvas canvas) {
 		
 		if(canvas == null) return;
 		
 		System.out.println("RightPanel: Setting new canvas");		
 		
 		this.canvas = canvas;
 		refreshLayers();
		
 	}
 	
 	public void updateHistogram(){
 		
 		
 		
 	}
 
 
 	
 
 	
 }
