 package org.concord.CCProbe;
 
 import waba.ui.*;
 import waba.util.*;
 import waba.fx.*;
 import waba.sys.*;
 import extra.util.*;
 import extra.io.*;
 import graph.*;
 import org.concord.waba.extra.event.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.util.*;
 import org.concord.waba.extra.probware.*;
 import org.concord.waba.extra.probware.probs.*;
 import org.concord.LabBook.*;
 
 public class GraphSettings
     implements DataListener
 {
     float xmin = 0f, xmax = 100f;
     float ymin = -20f, ymax = 50f;
 
     String description = null;
     String xLabel = null;
     String yLabel = null;
 	CCUnit xUnit = null;
 	CCUnit yUnit = null;
 
 	SplitAxis xaxis=null;
 	ColorAxis yaxis=null;
 
 	Object gvCookie;
 	LObjGraphView gv=null;
 
     Vector bins = new Vector();
 	Bin curBin = null;
 
 	public void init(LObjGraphView gv, Object cookie, Bin bin, 
 					 SplitAxis xAx, ColorAxis yAx)
 	{
 		curBin = bin;
 		gvCookie = cookie;
 		this.gv = gv;
 		xaxis = xAx;
 		yaxis = yAx;
 
 		if(xaxis == null || yaxis == null){
 			return;
 		}
 		xaxis.setAxisLabel(xLabel, xUnit);
 		yaxis.setAxisLabel(yLabel, yUnit);
 
 		xaxis.setRange(xmin, xmax);
 		yaxis.setRange(ymin, ymax);		
 
 	}
 
 	public void setXValues(float min, float max)
 	{
 		xmin = min;
 		xmax = max;
 	}
 
 	public void setYValues(float min, float max)
 	{
 		ymin = min;
 		ymax = max;
 	}
 
 	public void setXLabel(String label)
 	{
 		xLabel = label;
 	}
 
 	public void setYLabel(String label)
 	{
 		yLabel = label;
 	}
 
 	public void setYUnit(CCUnit unit)
 	{
 		yUnit = unit;
 	}
 
 	public void setXUnit(CCUnit unit)
 	{
 		xUnit = unit;
 	}
 
 
 	public void updateAv()
 	{
 		if(xaxis == null || yaxis == null){
 			return;
 		}
 		xaxis.setAxisLabel(xLabel, xUnit);
 		yaxis.setAxisLabel(yLabel, yUnit);
 
 		xaxis.setRange(xmin, xmax);
 		yaxis.setRange(ymin, ymax);
 
 		if(curBin != null) curBin.setUnit(yUnit);
 	}
 
 	public void updateGS()
 	{
 		if(xaxis == null || yaxis == null){
 			return;
 		}
 		ymin = yaxis.dispMin;
 		ymax = yaxis.getDispMax();
 		xmin = xaxis.dispMin;
 		xmax = xaxis.getDispMax();
 	}
 
 	public void startGraph(){
 		if(bins.getCount() == 0 && curBin != null){
 			bins.add(curBin);
 			curBin.time = new Time();
 
 			// Don't quite know what to do here
 			// this should be taken care of by DataSources
 			curBin.description = "";
 
 			if(gv != null) gv.startGraph(gvCookie, curBin);
 		}
 	}
 
     int numVals = 0;
 
     //    int [] [] pTimes = new int [1000][];
     int [] [] pTimes = null;
 
 	public void dataStreamEvent(DataEvent dataEvent)
 	{
 		switch(dataEvent.type){
 		case DataEvent.DATA_READY_TO_START:
 			startGraph();
 			return;
 		case DataEvent.DATA_COLLECTING:
 			if(gv != null) gv.update(gvCookie, dataEvent.getTime());
 			break;
 		case DataEvent.DATA_STOPPED:
 			stopGraph();
 			break;
 		}
 	}
 
 	public void stopGraph()
 	{
 		if(gv != null){
 			Bin newBin = gv.stopGraph(gvCookie, curBin);
 			if(newBin == null) return;
 			curBin = newBin;
 			curBin.setUnit(yUnit);
 			curBin.label = "";
 		}
 	}
 
     public void dataReceived(DataEvent dataEvent)
     {
		if(curBin == null) System.out.println("GS: null bin why?");
 		if(!curBin.dataReceived(dataEvent)){
 			stopGraph();
 			// av.curView.draw();
 			return;		
 		}
     }
 
 
 	public void saveData(LObjDataSet dSet)
 	{
 		dSet.setUnit(yUnit);
 		dSet.setLabel(yLabel);
 		
 		for(int i=0; i<bins.getCount(); i++){
 			dSet.addBin((Bin)bins.get(i));				   
 		}
 	}
 	
 	public Bin getBin()
 	{
 		if(bins != null ||
 		   bins.getCount() > 0){
 			return ((Bin)bins.get(0));
 		}
 		return null;
 	}
     
 	public void clear()
 	{
 		bins = new Vector();	
 	}
 	
 	public String toString()
 	{
 		return "xLabel: " + xLabel;		
 	}
 
 	public void readExternal(DataStream ds)
 	{
 		xmin = ds.readFloat();
 		xmax = ds.readFloat();
 		ymin = ds.readFloat();
 		ymax = ds.readFloat();
 		description = ds.readString();
 		xLabel = ds.readString();
 		yLabel = ds.readString();		
 		int code;
 		code = ds.readInt();
 		if(code == -1) xUnit = null;
 		else xUnit = CCUnit.getUnit(code);
 		code = ds.readInt();
 		if(code == -1) yUnit = null;
 		else yUnit = CCUnit.getUnit(code);
 	}
 
     public void writeExternal(DataStream ds)
     {
 		ds.writeFloat(xmin);
 		ds.writeFloat(xmax);
 		ds.writeFloat(ymin);
 		ds.writeFloat(ymax);
 		ds.writeString(description);
 		ds.writeString(xLabel);
 		ds.writeString(yLabel);
 		if(xUnit == null) ds.writeInt(-1);
 		else ds.writeInt(xUnit.code);
 		if(yUnit == null) ds.writeInt(-1);
 		else ds.writeInt(yUnit.code);
     }
 
 	public GraphSettings copy()
 	{
 		GraphSettings g = new GraphSettings();
 
 		g.xmin = xmin;
 		g.ymin = ymin;
 		g.xmax = xmax;
 		g.ymax = ymax;
 		g.description = description.toString();
 		g.xLabel = xLabel;
 		g.yLabel = yLabel;
 		g.xUnit = xUnit;
 		g.yUnit = yUnit;		
 
 		return g;
 	}
 }
