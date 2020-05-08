 package org.concord.CCProbe;
 
 import graph.*;
 import waba.ui.*;
 import waba.fx.*;
 import waba.util.*;
 import extra.util.*;
 import extra.ui.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.event.*;
 import org.concord.waba.extra.probware.probs.*;
 import org.concord.waba.extra.probware.*;
 import org.concord.LabBook.*;
 	
 public class LObjDataCollectorView extends LabObjectView
     implements ActionListener, DialogListener, ViewContainer
 {
     LObjDataCollector dc;
     LObjGraphView gv;
     LObjDictionary dataDict = null;
 
     Label nameLabel = null;
     Edit nameEdit = null;
 
     int gt_height = 40;
 
     Menu menu = new Menu("Probe");
 
     ToggleButton collectButton;
     Button doneB;
 
 	Label title1Label, title2Label;
     String title1 = "";
 	String  title2 = "";
 
     public LObjDataCollectorView(ViewContainer vc, LObjDataCollector dc, 
 							   LObjDictionary curDict)
     {
 		super(vc);
 
 		menu.add("Properties...");
 		menu.add("Save Profile...");
 		menu.addActionListener(this);
 		if(vc != null){
 			vc.getMainView().addMenu(this, menu);
 		}
 
 		this.dc = dc;
 		lObj = dc;
 		dataDict = curDict;
     }
 
 	public void setTitle1(String t1)
 	{
 		if(t1 == null) t1 = "";
 		title1Label.setText(t1);
 	}
 
 	public void setTitle2(String t2)
 	{
 		if(t2 == null) t2 = "";
 		title2Label.setText(t2);
 	}
 
     void stop()
     {
 		dc.stop();
 		collectButton.setSelected(false);    
 		gv.stopGraph();
 
 		repaint();
     }
     
     public void dialogClosed(DialogEvent e)
     {
 		Debug.println("Got closed");	
 		gv.updateProp();
 		setTitle2(graph.title);
		
 		dc.store();
 		repaint();
     }
 
     LObjGraph graph = null;
 
     public void layout(boolean sDone)
     {
 		if(didLayout) return;
 		didLayout = true;
 
 		showDone = sDone;
 
 		collectButton = new ToggleButton("Collect", false);
 		add(collectButton);
 
 		graph = (LObjGraph)dc.getObj(0);
 		gv = (LObjGraphView)graph.getView(this, false, dataDict);
 		gv.showTitle(false);
 		Vector dataSources = dc.getDataSources();
 		for(int i=0; i<dataSources.getCount(); i++){
 			gv.addDataSource((DataSource)dataSources.get(i));
 		}
 		gv.setDC(dc);
 
 		gv.layout(false);
 
 		title1Label = new Label(title1);
 		add(title1Label);
 
 		title2Label = new Label(graph.title);
 		add(title2Label);
 
 		doneB = new Button("Done");
 		add(doneB);
 
 
 		add(gv);
     }
 
 
     public void setRect(int x, int y, int width, int height)
     {
 		super.setRect(x,y,width,height);
 		if(!didLayout) layout(false);
 
 		int curY = 0;
 		int gHeight = height;
 
 		if(gHeight <= 160){
 			gt_height = 22;
 		}
          
 		gv.setRect(0, curY+gt_height, width, gHeight-gt_height);
 	
 		int buttonWidth = gt_height;
 		if(gt_height < 30) buttonWidth = 35;
 
 		int xPos = 0;
         collectButton.setRect(xPos,0,buttonWidth,gt_height);
 		xPos += buttonWidth+2;
 
 		title1Label.setRect(xPos, 0, width-xPos-27, gt_height/2);
 		title2Label.setRect(xPos, gt_height/2, width-xPos, gt_height/2);
 		doneB.setRect(width-27, 0, 27, gt_height/2);
 
 		setTitle1(dc.name);
 
     }
 
     public void actionPerformed(ActionEvent e)
     {
 		String command;
 		Debug.println("Got action: " + e.getActionCommand());
 
 		if(e.getSource() == menu){
 			if(e.getActionCommand().equals("Properties...")){
 				stop();
 
 				gv.updateAv2Graph();
 				if(dc.dataSources == null || dc.dataSources.getCount() < 1 ||
 				   !(dc.dataSources.get(0) instanceof LObjProbeDataSource)){
 					return;
 				}
 
 				LObjProbeDataSource pds = (LObjProbeDataSource)dc.dataSources.get(0);
 				CCProb p = pds.getProbe();
 
 				p.calibrateMe((ExtraMainWindow)(MainWindow.getMainWindow()), this, dc.interfaceId);
 
 				Debug.println("Callllll");
 			} else if(e.getActionCommand().equals("Save Profile...")){
 				LObjDocument dProf = new LObjDocument();
 				String text = "";
 				for(int i=0; i < gv.curPtime; i++){
 					for(int j=0; j < gv.pTimes[i].length; j++){
 						text += gv.pTimes[i][j] + " ";		
 					}
 					text += "\n";
 				}
 				dProf.setText(text);
 				dProf.name = "Profile";
 		
 				if(dataDict != null){
 					dataDict.add(dProf);
 					dataDict.store();
 					dProf.store();
 				} 
 		
 			}
 		} 
     }
 
     public void close()
     {
 		Debug.println("Got close in graph");
 		if(container != null){
 			container.getMainView().delMenu(this,menu);
 		}
 
 		stop();	
 
 		// need to make sure this unregisters data sources
 		gv.close();
 
 		dc.closeSources();
 
 		super.close();
     }
 
     public void onEvent(Event e)
     {
 		if(e.target == gv){
 			if(e.type == 1000){
 				stop();
 			} else if(e.type == 1001){
 				setTitle2(gv.graph.title);
 			}			
 		} else 	if(e.type == ControlEvent.PRESSED){
 			Control target = (Control)e.target;
 			int index;
 			if(target == collectButton && collectButton.isSelected()){
 				// need to tell the GraphView to start
 				gv.startGraph();
 				dc.start();
 			} else if(target == collectButton && ! collectButton.isSelected()){
 				// need to tell the GraphView to stop
 				stop();
 			} else if(target == doneB){
 				// let our parent know we've been done'd
 				if(container != null){
 					container.done(this);
 				}	    
 			}
 		}  
     }
 
 	public MainView getMainView()
 	{
 		if(container != null) return container.getMainView();
 		return null;
 	}
 
     public void reload(LabObjectView source){}
 
     public void done(LabObjectView source) {}
 
 }
