 import waba.ui.*;
 import waba.fx.*;
 import waba.io.*;
 import waba.util.*;
 
 public class DataPage extends Container
 {
     Canvas modelCanvas;
     Canvas dataCanvas;
     GraphView gv;
     PSConnection connection;
     Button discover;
     TabManager tabMan;
     PushButtonGroup selector, netControl;
     Edit addrEdit;
     Control curControl;
     BlockModel bm;
 
     String curFileName = "untitled.dnm";
 
     public DataPage()
     {
 	Container current;
 	Layer layer;
 
 	// add a canvas
 	modelCanvas = new Canvas(236, 129, 2);
 	modelCanvas.live = true;
 	modelCanvas.setPos(2,110);
 	
 	modelCanvas.getLayer(0).gridSpace = 7;
 	modelCanvas.getLayer(0).gridDist = 3;
 
 	modelCanvas.getLayer(1).gridSpace = 3;
 	modelCanvas.getLayer(1).gridDist = 1;
 
 	modelCanvas.addObject(new GarbageObject(), modelCanvas.getLayer(0), 10, 90);
 
 	add(modelCanvas);
 	
 	String names [] = new String [2];
 	names [0] = "Graph";
 	names [1] = "Setup";
 	selector = new PushButtonGroup(names, true, 0, 4, 4, 2, true, PushButtonGroup.NORMAL); 
 	selector.setRect(1,2,34,30);
 	add(selector);
 
 	addrEdit = new Edit();
 	addrEdit.setText("93");
 	addrEdit.setRect(1,38,20,17);
 	add(addrEdit);
 
 	names = new String [2];
 	names [0] = "Open";
 	names [1] = "Close";
 	netControl = new PushButtonGroup(names, true, 1, 4, 4, 2, true, PushButtonGroup.NORMAL); 
 	netControl.setRect(1,60,34,30);
 	add(netControl);
 
 
 	gv = new GraphView(195, 105);
 	gv.setPos(35,2);
 	modelCanvas.gv = gv;
 	add(gv);
 	curControl = gv;
 	
 	// add a canvas
 	dataCanvas = new Canvas(190, 105, 1);
 	dataCanvas.setPos(42,2);
 	dataCanvas.gv = gv;
 	layer = dataCanvas.getLayer(0);
 
 	VProbeObject vProbe = new VProbeObject(MainWindow.defaultFont, "A");
 	vProbe.dragAction = vProbe.EXT_DRAG_COPY;
 	vProbe.targetLayerIndex = 1;
 	// the order is important here that should be fixed
 	vProbe.rotate(-1);
 	dataCanvas.addObject(vProbe, layer);
 
 	vProbe = new VProbeObject(MainWindow.defaultFont, null);
 	vProbe.dragAction = vProbe.EXT_DRAG_COPY;
 	vProbe.targetLayerIndex = 1;
 	vProbe.rotate(-1);
 	dataCanvas.addObject(vProbe, layer);
 
 
 	// setup server connection
 	connection = new PSConnection(dataCanvas);
 	connection.graph = gv;
 	connection.dp = this;
 
     }
 
     /*
      * Write all the objects
      */
     public void writeExt(DataStream ds)
     {
 	ds.writeString("DataPage");
 	connection.writeExt(ds);
 	dataCanvas.writeExt(ds);
 	modelCanvas.writeExt(ds);
     }
 
     public void readExt(DataStream ds)
     {
 	Vector dataObj1, dataObj2;
 
 	// setup server connection
 	connection = new PSConnection();
 	connection.readExt(ds);
 	connection.graph = gv;
 	connection.dp = this;
 		
 	dataCanvas.removeAll();
 	Canvas newDataCanvas = new Canvas();
 	dataObj1 = newDataCanvas.readExt(ds);
 	
 	if(curControl == dataCanvas){
 	    remove(dataCanvas);
 	    dataCanvas.removeAll();
 	    add(newDataCanvas);
 	}
 	dataCanvas = newDataCanvas;
 	
 
 	// set Connections addCanvas
 	connection.newCanvas = dataCanvas;
 	
 	modelCanvas.removeAll();
 	remove(modelCanvas);
 	modelCanvas = new Canvas();
 	dataObj2 = modelCanvas.readExt(ds);
 	add(modelCanvas);
 
 	// setup connection
 	int numDataBlocks = 0;
 	int i;
 
 	Object [] tmpObjs1 = dataObj1.toObjectArray();
 	for(i=0; i<tmpObjs1.length; i++){
 	    if(tmpObjs1[i] instanceof BlockObject){
 		numDataBlocks++;
 	    }
 	}
 	Object [] tmpObjs2 = dataObj2.toObjectArray();
 	for(i=0; i<tmpObjs2.length; i++){
 	    if(tmpObjs2[i] instanceof BlockObject){
 		numDataBlocks++;
 	    }
 	}
 
 	connection.blocks = new BlockObject [numDataBlocks];
 	int curDataBlock = 0;
 	for(i=0; i<tmpObjs1.length; i++){
 	    if(tmpObjs1[i] instanceof BlockObject){
 		connection.blocks[curDataBlock++] = (BlockObject)tmpObjs1[i];
 	    }
 	}
 	for(i=0; i<tmpObjs2.length; i++){
 	    if(tmpObjs2[i] instanceof BlockObject){
 		connection.blocks[curDataBlock++] = (BlockObject)tmpObjs2[i];
 	    }
 	}
     }
 
     public void onEvent(Event e)
     {
 	if(e.type == ControlEvent.PRESSED){
 	    Control target = (Control)e.target;
 	    int index;
 	    if(target == selector){
 		index = selector.getSelected();
 		if(index == 0 && curControl != gv){
 		    remove(curControl);
 		    curControl.setEnabled(false);
 		    add(gv);
 		    curControl = gv;
 		    curControl.setEnabled(true);
 		} else if(index == 1 && curControl != dataCanvas){
 		    remove(curControl);
 		    curControl.setEnabled(false);
 		    add(dataCanvas);
 		    curControl = dataCanvas;
 		    curControl.setEnabled(true);
 		}
 		
 	    } else if(target == netControl){
 		index = netControl.getSelected();
 		if(index == 0){
 		    bm.setStatus("Opening Connection");
 		    bm.status.repaint();
 		    if(!connection.open("4.19.234." + addrEdit.getText())){
 			bm.status.setText("Error Connecting");
 			netControl.setSelected(1);
 			return;
 		    }
 		    bm.status.setText("Connection open");
 		    if(curControl == gv){
 			selector.setSelected(1);
 			remove(curControl);
 			curControl.setEnabled(false);
 			add(dataCanvas);
 			curControl = dataCanvas;
 			curControl.setEnabled(true);
 		    }
 		} else {
 		    bm.status.setText("Closing Connection");
 		    connection.close();
 		    bm.status.setText("Connection Closed");
 		}
 	    }
 	} 
 
     }    
 
     public void forceClose()
     {
 	bm.status.setText("Connection Closed");
 	netControl.setSelected(1);
 
     }
 
 }
 
 
 
 
 
 
 
 
 
 
 
