 package com.vaadin.toolkitdraw.demos;
 
 import java.awt.Color;
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.terminal.DownloadStream;
 import com.vaadin.terminal.Resource;
 import com.vaadin.terminal.StreamResource;
 import com.vaadin.toolkitdraw.components.paintcanvas.PaintCanvas;
 import com.vaadin.toolkitdraw.components.paintcanvas.enums.CacheMode;
 import com.vaadin.toolkitdraw.components.paintcanvas.events.ImagePNGRecievedEvent;
import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Button.ClickEvent;
 
 public class SimpleGraphDemo extends Window {
 
 	private static final long serialVersionUID = 1L;
 
 	private PaintCanvas canvas;
 	
 	private Table table;
 	
 	private Button refresh;
 	
 	private String[] titles = new String[]{ "Food", "Drinks", "Clothes", "Stuff" };
 
 	private Button save;
 	
 	public SimpleGraphDemo() {
 		
 		super("Simple Graph Demo");		
 		
 		setWidth("600px");
 		setHeight("400px");		
 		setModal(true);			
 						
 		GridLayout layout = new GridLayout(2,2);	
 		setLayout(layout);	
 		
 		//Create a paintcanvas 
 		canvas = new PaintCanvas("300px","300px",  new Color(51,51,51));	
 		
 		//Disable drawing on the canvas
 		canvas.setInteractive(false);
 		
 		//Disable caching
 		canvas.setCacheMode(CacheMode.NONE);		
 		
 		//Enable batch mode
 		canvas.getGraphics().setBatchMode(true);
 				
 		layout.addComponent(canvas,0,0);
 		
 		//Create a value table
 		table = new Table();
 		table.addContainerProperty("Title", String.class, "");
 		table.addContainerProperty("Value", Integer.class, 0);
 		table.setColumnWidth("Title", 190);
 		table.setColumnWidth("Value", 70);
 		table.setColumnAlignment("Value", Table.ALIGN_CENTER);
		table.setHeight("300px");
		table.setWidth("290px");
 		layout.addComponent(table,1,0);
 		
 		//Create the refresh button
 		refresh = new Button("Refresh");
 		refresh.addListener(new Button.ClickListener(){		
 			private static final long serialVersionUID = 1L;
 			@Override
 			public void buttonClick(ClickEvent event) {
 				randomlyCreateValues();												
 				renderBarGraph();							
 			}			
 		});
 		
 		layout.addComponent(refresh,1,1);
		layout.setComponentAlignment(refresh, Alignment.MIDDLE_RIGHT);
 		
 		save = new Button("Save");
 		final Window parent = this;
 		save.addListener(new Button.ClickListener(){		
 			private static final long serialVersionUID = 1L;
 			public void buttonClick(ClickEvent event) {
 				canvas.addListener(new ValueChangeListener(){		
 					private static final long serialVersionUID = 1L;
 					public void valueChange(ValueChangeEvent event) {						
 						if(event instanceof ImagePNGRecievedEvent){							
 							canvas.removeListener(this);							
 							ImagePNGRecievedEvent evnt = (ImagePNGRecievedEvent)event;						
 							ByteArrayInputStream iStream = new ByteArrayInputStream(evnt.getData());							
 							final DownloadStream stream = new DownloadStream(iStream,"Image/JPG","image"+(new Date()).getTime()+".jpg");
 							
 							Resource res = new StreamResource(new StreamResource.StreamSource(){			
 								public InputStream getStream() {
 									return stream.getStream();
 								}				
 							}, stream.getFileName(), parent.getApplication());
 							
 							parent.open(res,"img");							
 						}									
 					}					
 				});	
 				
 				//Request XML image 
 				canvas.getImagePNG(0); 				
 			}			
 		});
 		
 		layout.addComponent(save,0,1);
		layout.setComponentAlignment(save, Alignment.MIDDLE_LEFT);
 				
 		randomlyCreateValues();
 		
 		renderBarGraph();
 	}
 	
 	private void populateTable(Map<String, Integer> values){
 		
 		if(values == null) return;
 		
 		table.removeAllItems();
 		for(String title : values.keySet())
 		{
 			Item item = table.addItem(title);
 			item.getItemProperty("Title").setValue(title);
 			item.getItemProperty("Value").setValue(values.get(title));			
 		}		
 	}
 	
 	private void randomlyCreateValues(){		
 		
 		Map<String, Integer> values = new HashMap<String, Integer>();
 		
 		for(String title : titles){
 			int val = new Double(Math.random()*100.0).intValue();
 			values.put(title,val);			
 		}
 		
 		populateTable(values);
 	}
 	
 	/**
 	 * Renders a bar graph of the values in the table
 	 */
 	private void renderBarGraph(){			
 						
 		//Get the graphics object
 		PaintCanvas.Graphics gc = canvas.getGraphics();
 					
 		// This clears the whole screen
 		gc.clear();	
 											
 		//Draw the bars to the component in one batch
 		int counter = 0;
 		for(Object id : table.getItemIds()){			
 			Item item = table.getItem(id);
 			int value = Integer.valueOf((item.getItemProperty("Value").getValue().toString()));
 						
 			//Draw the front of the bars
 			gc.drawSquare(counter*70+20, 280-value*2, 50, value*2,"CC0000","FF0000");
 			
 			//Draw the tops of the bars
 			int[] topX = new int[4];
 			int[] topY = new int[4];			
 			
 			topX[0] = counter*70+20;
 			topY[0] = 280-value*2;
 			topX[1] = topX[0]+10;
 			topY[1] = topY[0]-10;
 			topX[2] = topX[1]+50;
 			topY[2] = topY[1];
 			topX[3] = topX[0]+50;
 			topY[3] = topY[0];
 			
 			gc.drawPolygon(topX, topY, "CC0000", "FF0000");
 			
 			//Draw the sides of the bars
 			topX[0] = counter*70+70;
 			topY[0] = 280-value*2;
 			topX[1] = topX[0]+10;
 			topY[1] = topY[0]-10;
 			topX[2] = topX[1];
 			topY[2] = topY[1]+value*2;
 			topX[3] = topX[0];
 			topY[3] = topY[2]+10;
 			
 			gc.drawPolygon(topX, topY, "CC0000", "CC0000");			
 		
 			//Add the values to the top of the bars
 			gc.drawText(id.toString(), counter*70+20, 280-value*2, 100, 100, "FFFFFF", 12, "000000", 0.0);
 			
 			counter++;
 		}		
 							
 				
 		
 		//Send the draw intstructions to the client
 		gc.sendBatch();	
 	
 		
 	}
 	
 
 }
