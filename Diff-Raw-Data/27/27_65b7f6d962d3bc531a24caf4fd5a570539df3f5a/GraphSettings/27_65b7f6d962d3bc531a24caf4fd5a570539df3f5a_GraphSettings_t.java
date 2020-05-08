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
 
 	boolean started = false;
 
 	public static int MAX_COLLECTIONS = 10;
 
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
 
 		xaxis.setRange(xmin, xmax-xmin);
 		yaxis.setRange(ymin, ymax-ymin);		
 	}
 
 	public void setXValues(float min, float max)
 	{
 		xmin = min;
 		xmax = max;
 		if(xaxis != null) xaxis.setRange(xmin, xmax-xmin);
 	}
 
 	public void setYValues(float min, float max)
 	{
 		ymin = min;
 		ymax = max;
 		if(yaxis != null) yaxis.setRange(ymin, ymax-ymin);
 	}
 
 	public void setXLabel(String label)
 	{
 		xLabel = label;
 		if(xaxis != null) xaxis.setAxisLabel(xLabel, xUnit);
 	}
 
 	public void setYLabel(String label)
 	{
 		yLabel = label;
 		if(yaxis != null) yaxis.setAxisLabel(yLabel, yUnit);
 	}
 
 	public void setYUnit(CCUnit unit)
 	{
 		// Probably want to check if this a valid switch
 		yUnit = unit;
 		if(yaxis != null) yaxis.setAxisLabel(yLabel, yUnit);
 		if(curBin != null) curBin.setUnit(yUnit);
 	}
 
 	public void setXUnit(CCUnit unit)
 	{
 		xUnit = unit;
 		if(xaxis != null) xaxis.setAxisLabel(xLabel, xUnit);
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
 		if(bins.getCount() < MAX_COLLECTIONS && curBin != null){
 			started = true;
 			bins.add(curBin);
 			curBin.time = new Time();
 
 			// Don't quite know what to do here
 			// this should be taken care of by DataSources
 			curBin.description = "";
 
 			if(gv != null) gv.startGraph(gvCookie, curBin);
 		}
 	}
 
 	public void stopGraph()
 	{
 		if(gv != null && started){
 			started = false;
 			Bin newBin = gv.stopGraph(gvCookie, curBin, 
 									  bins.getCount() < MAX_COLLECTIONS,
 									  xaxis);
 			if(newBin == null) return;
			curBin = newBin;
 			curBin.setUnit(yUnit);
 			curBin.label = "";
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
 
     public void dataReceived(DataEvent dataEvent)
     {
 		if(curBin == null || !started) return;
 		if(!curBin.dataReceived(dataEvent)){
 			stopGraph();
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
     
 	float maxVisY = 0f;
 	float minVisY = 0f;
 
     public boolean calcVisibleRange()
     {
 		int i,j,k;
 		int lastOffset;
 		int [] binPoints;
 		int curX, curMinY, curMaxY;
 		int minY, maxY;
 		float minYf, maxYf;
 		Bin bin;
 		Axis xa;
 		boolean setRanges = false;
 
 	    maxVisY = (float)-(0x7FFFFFF);
 		minVisY = (float)(0x7FFFFFF);
 
 		for(k=0; k<bins.getCount(); k++){
 			bin = (Bin)bins.get(k);
 			xa = bin.xaxis;
 
 			if(xa.drawnX == -1 || bin.numPoints <= 1) continue;
 	    			
 			binPoints = bin.points;
 			lastOffset = bin.numPoints*3;
 	    
 			minY = (0x7FFFFFF);
 			maxY = -(0x7FFFFFF);
 
 			int xOffset = (int)((xa.dispMin - bin.refX) * xa.scale);		
 			for(i=0; i<lastOffset;){
 				curX = binPoints[i++];
 				curMinY = binPoints[i++] - (binPoints[i] & 0xFFFF);					
 				curMaxY = binPoints[i-1] + (binPoints[i] >> 16);
 				i++;
 		
 				if(curX > (xOffset - 1) && curX <= (xOffset + xa.dispLen)){
 					if(curMaxY > maxY) maxY = curMaxY;
 					if(curMinY < minY) minY = curMinY;
 				}		
 			}	    
 			
 			minYf = ((float)minY / yaxis.scale + bin.refY);
 			maxYf = ((float)maxY / yaxis.scale + bin.refY);
 			float temp;
 			if(minYf > maxYf){
 				temp = minYf;
 				minYf = maxYf;
 				maxYf = temp;
 			}
 
 			if(minYf < minVisY) minVisY = minYf;
 			if(maxYf > maxVisY) maxVisY = maxYf;
 
 			setRanges = true;
 		}		
 
 		
 		return setRanges;
     }
 
 	public void clear()
 	{
 		started = false;
 		if(gv != null && curBin != null) gv.clear(gvCookie, curBin);
 		for(int i=0 ; i < bins.getCount(); i++){
 			Bin bin = (Bin)bins.get(i);
 			if(bin != null) bin.free();
 		}
 		bins = new Vector();	
		if(curBin != null) curBin.reset();
 	}
 	
 	public String toString()
 	{
 		return super.toString() + " xmin: " + xmin + " xmax: " + xmax + " xLabel: " + xLabel +
 			" ymin: " + ymin + " ymax: " + ymax + " yLabel: " + yLabel;		
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
