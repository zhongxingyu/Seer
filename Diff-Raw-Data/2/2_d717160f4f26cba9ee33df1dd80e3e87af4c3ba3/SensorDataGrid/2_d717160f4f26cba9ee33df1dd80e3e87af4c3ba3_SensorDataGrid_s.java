 /*
  * Ext GWT - Ext for GWT
  * Copyright(c) 2007, 2008, Ext JS, LLC.
  * licensing@extjs.com
  *
  * http://extjs.com/license
  */
 package nl.sense_os.testing.client.widgets;
 
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.ModelType;
 import com.extjs.gxt.ui.client.fx.Draggable;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONValue;
 
 import java.util.ArrayList;
 import java.util.List;
 
import nl.sense_os.commonsense.client.common.grid.PaginationGridPanel;
 
 
 public class SensorDataGrid extends LayoutContainer {
 
 	public SensorDataGrid(){
 		// Data store structure.
 		ModelType model = new ModelType();
 		model.setTotalName("total");
 		model.setRoot("data");
         model.addField("d");
         model.addField("s");
         model.addField("t");
 		model.addField("v");
 		
 		// Column model.
 		List<ColumnConfig> colConf = new ArrayList<ColumnConfig>();        
         
         ColumnConfig timeCol = new ColumnConfig();
         timeCol.setId("t");
         timeCol.setHeader("time");
         timeCol.setDataIndex("t");
         timeCol.setWidth(200);           
         colConf.add(timeCol);     
 		
 		ColumnConfig deviceCol = new ColumnConfig();
         deviceCol.setId("d");
         deviceCol.setHeader("device_id");
         deviceCol.setDataIndex("d");
         deviceCol.setWidth(200);           
         colConf.add(deviceCol);
         
         ColumnConfig sensorCol = new ColumnConfig();
         sensorCol.setId("s");
         sensorCol.setHeader("sensor_id");
         sensorCol.setDataIndex("s");       
         sensorCol.setWidth(200);
         colConf.add(sensorCol);  
 
         ColumnConfig valueCol = new ColumnConfig();
         valueCol.setId("v");
         valueCol.setHeader("value");
         valueCol.setDataIndex("v");
         valueCol.setWidth(200);
         valueCol.setRenderer(getValueCellFormat());
         colConf.add(valueCol);
         
         ColumnModel cm = new ColumnModel(colConf);
 		
 		// Grid.
 		PaginationGridPanel gridPanel = new PaginationGridPanel(
 				"http://dev2.almende.com/commonsense/testing/get_sensor_data_paged.php" +
 				"?email=steven@sense-os.nl&password=81dc9bdb52d04dc20036dbd8313ed055&s_id=2",
 				model,
 				cm,
 				5);
 		
 		// Grid config.
 		/*
 		HashMap<Integer, Object> gridConf = new HashMap<Integer, Object>();
 		gridConf.put(grid.WIDTH, 402);
 		gridConf.put(grid.AUTO_HEIGHT, true);
 		grid.loadConf(gridConf);
 		*/
 		gridPanel.setWidth(802);
 		gridPanel.setAutoHeight(true);
 		gridPanel.setTitle("sensor data");
 		gridPanel.setCollapsible(true);
 		gridPanel.setBodyBorder(true);
 		
 		gridPanel.load();
 
 		new Draggable(gridPanel);
 		
 		add(gridPanel);
 	}
 	
 	/**
 	 * 
 	 * @return the value field html format
 	 */
 	private GridCellRenderer<ModelData> getValueCellFormat() {
 		return new GridCellRenderer<ModelData>() {
 			@Override
 			public Object render(
 					ModelData model, 
 					String property,
 					ColumnData config, 
 					int rowIndex, 
 					int colIndex,
 					ListStore<ModelData> store, 
 					Grid<ModelData> grid) {
 				
 				String value = model.<String>get("v");
 			    				
                 JSONValue jsonValue = JSONParser.parseLenient(value);
                 JSONObject jsonObj = jsonValue.isObject();
                 if (null != jsonObj) {
                     return "foo";
                 }
                 
                 // return the normal value for non-JSON input
                 return value;
                 
                 
 				/*
 				// example: accelerometer
 				final int ACCELOMETER = 1;
 				
 				int sensorType = ACCELOMETER; 
 				
 				switch (sensorType) {
 				case ACCELOMETER:
 					String value = (String) model.get("v");
 					String [] values = ((String) value).split(",");
 					return "x-axis: " + values[0] + "<br />y-axis: " + values[1] + "<br />z-axis: " + values[2];
 
 				default:
 					// Returns the value without format.
 					return model.get("v");
 				}
 				*/
 			}
         };
 	}
 }
