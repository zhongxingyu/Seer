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
     implements DataListener, LabObjListener, ActionListener
 {
     String description = null;
     String xLabel = null;
     String yLabel = null;
 	CCUnit xUnit = null;
 	CCUnit yUnit = null;
 
 	SplitAxis xaxis=null;
 	ColorAxis yaxis=null;
 	int linkX = -1;
 	int linkY = -1;
 
 	LObjGraphView gv=null;
 
     Vector bins = new Vector();
 	Bin curBin = null;
 
 	boolean started = false;
 
 	public static int MAX_COLLECTIONS = 10;
 
 	LObjGraph graph = null;
 	int dsIndex = -1;
 	DataSource ds = null;
 	Vector annots = new Vector();
 
 	boolean visible = true;
 
 	public GraphSettings(LObjGraph g, int dataSourceIndex)
 	{
 		graph = g;
 		dsIndex = dataSourceIndex;
 	}
 
 	public GraphSettings(LObjGraph g, int dataSourceIndex, int lX, int lY)
 	{
 		this(g, dataSourceIndex);
 
 		linkX = lX;
 		xaxis = (SplitAxis)graph.getXAxis(linkX);
 		
 		linkY = lY;
 		yaxis = (ColorAxis)graph.getYAxis(linkY);				
 
 		/*
 		if(graph != null && ds == null && dsIndex >= 0){
 			ds = graph.getDataSource(dsIndex);
 		}
 		*/
 	}
 
 	public void init(LObjGraphView gv)
 	{
 		this.gv = gv;
 
 		xaxis = (SplitAxis)getXAxis();
 		yaxis = (ColorAxis)getYAxis();
 
 		if(xaxis == null || yaxis == null){
 			return;
 		}
 
 		yaxis.setMaxDigits(6);
 		yaxis.init();
 		xaxis.init();
 
 		if(graph != null && ds == null && dsIndex >= 0){
 			ds = graph.getDataSource(dsIndex, gv.getSession());
 		}
 
 		if(ds == null) return;
 
 		if(ds instanceof LObjProbeDataSource){
 			LObjProbeDataSource pDS = (LObjProbeDataSource)ds;
 			pDS.addLabObjListener(this);
 		}			
 		
 		ds.addDataListener(this);
 
 		// Is this necessary???
 		setYUnit(ds.getUnit(gv.getSession()));
 		
 		xUnit = xaxis.getUnit();
 
 		// update the yaxis labels just to be safe
 		updateAxis();
 	}
 
 	public String getSummary(LabBookSession session)
 	{
 		if(ds == null && gv != null){
 			ds = graph.getDataSource(dsIndex, session);
 		}
 
 		if(ds != null){
 			return ds.getSummary(session);
 		} 
 		return null;
 	}
 
 	public DataSource getDataSource(LabBookSession session)
 	{
 		return graph.getDataSource(dsIndex, session);
 	}
 
 	public void labObjChanged(LabObjEvent e)
 	{
 		if(e.getObject() == ds &&
 		   ds != null && gv != null){
 			setYUnit(ds.getUnit(gv.getSession()));
 			updateAxis();
 			graph.notifyObjListeners(new LabObjEvent(graph, 0));
 		}
 	}		
 
 	public void close()
 	{
 		// might need to stop the ds first
 		if(ds != null){
 			ds.removeDataListener(this);
 			ds.closeEverything();
 		}
 
 		ds = null;
 
 		if(xaxis != null) xaxis.freeRef();
 		if(yaxis != null) yaxis.freeRef();
 
 		// might also need to remove our selves from 
 		// listening to the axis if we are doing that
 	}
 
 	public Axis getXAxis()
 	{
 		if(xaxis == null){
 			xaxis = (SplitAxis)graph.getXAxis(linkX);
 		}
 		return xaxis;
 	}
 
 	public Axis getYAxis()
 	{
 		if(yaxis == null){
 			yaxis = (ColorAxis)graph.getYAxis(linkY);				
 		}
 		return yaxis;
 	}
 
 	public void updateAxis()
 	{
 		if(yaxis != null && yaxis.autoLabel){
 			// need to set the ylabel to it's auto name
 			if(ds != null){
 				yLabel = ds.getQuantityMeasured(gv.getSession());
 			} else {
 				yLabel = "null DS";
 			}
 			yaxis.setAxisLabel(yLabel, yUnit);
 		}
 		
 		if(xaxis != null && xaxis.autoLabel){
 			// need to set the xlabel to it's auto name
 			xLabel = "Time";
 			xaxis.setAxisLabel(xLabel, xUnit);
 		}
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
 
 	public boolean getVisible()
 	{
 		return visible;
 	}
 
 	public void setVisible(boolean val)
 	{
 		visible = val;
 		if(bins != null){
 			for(int i=0; i<bins.getCount(); i++){
 				Bin b = (Bin)bins.get(i);
 				b.setVisible(visible);
 			}
 		}
 	}
 
 
 	// The last x axis should always be empty
 	// when this function is called
 	public void startDataDelivery()
 	{
 		if(ds == null || gv == null) return;
 
 		// This MAX_COLLECTIONS needs to be centralized somehow
 		if(bins.getCount() < MAX_COLLECTIONS){
 			if(curBin == null || curBin.getNumVals() > 0){
 				// either this is the first time
 				// or the curBin is linked to an old xaxis
 				if(curBin != null){
 					// This should also pass in the old axis
 					xaxis.setAxisEndPoint(curBin.xaxis, curBin.getCurX());
 				}
 				curBin = new Bin(xaxis.lastAxis, yaxis);
 			} else if(curBin.getNumVals() <= 0 && curBin.xaxis != xaxis.lastAxis){
 				curBin.setXAxis(xaxis.lastAxis);
 			}
 			bins.add(curBin);
 			curBin.addActionListener(this);
 			curBin.label = "Probe";
 			curBin.setUnit(yUnit);
 			curBin.time = new Time();
 			// Don't quite know what to do here
 			// this should be taken care of by DataSources
 			curBin.description = "";
 			curBin.setVisible(visible);
 
 			gv.startGraph(curBin);
 
 			started = true;
 			ds.startDataDelivery(gv.getSession());
 		}
 	}
 
 	public void stopDataDelivery()
 	{
 		stopDataDelivery(true);
 	}
 
 	public void stopDataDelivery(boolean needDSStop)
 	{
 		if(ds == null || gv == null || !started) return;
 
 		started = false;
 		if(needDSStop) ds.stopDataDelivery();
 		if(curBin == null) return;
 
 		if(curBin.getCurX() < 0 || curBin.getNumVals() < 3){
 			// Save this bin and xaxis for the next time
 			curBin.reset();
 			// remove this bin because it hasn't been used yet
 			bins.del(bins.getCount() - 1);
 		} 
 		gv.stopGraph(curBin);
 	}
 
 	public void clear()
 	{
 		if(ds == null || gv == null) return;
 
 		if(started){
 			started = false;
 			ds.stopDataDelivery();
 			gv.stopGraph(curBin);
 		}
 
 		// do this for safety incase it isn't done
 		// by the LineGraph
 		xaxis.reset();
 
 		annots = null;
 		if(curBin != null){
 			// This is a hack need to figure out
 			// about reseting the curBin
 			gv.clear(curBin);
 			curBin.reset();
 		} 
 
 		// Don't free the last bin
 		for(int i=0 ; i < bins.getCount()-1; i++){
 			Bin bin = (Bin)bins.get(i);
 			bin.removeActionListener(this);
 			if(bin != null) bin.free();
 		}
 		bins = new Vector();
 		annots = new Vector();
 	}
 	
 
     int numVals = 0;
 
     //    int [] [] pTimes = new int [1000][];
     int [] [] pTimes = null;
 
 	public void dataStreamEvent(DataEvent dataEvent)
 	{
 		switch(dataEvent.type){
 		case DataEvent.DATA_READY_TO_START:
 			//			startGraph();
 			return;
 		case DataEvent.DATA_COLLECTING:
 			if(gv != null) gv.update(dataEvent.getTime());
 			break;
 		case DataEvent.DATA_STOPPED:
 			if(started) stopDataDelivery(true);
 			break;
 		}
 	}
 
     public void dataReceived(DataEvent dataEvent)
     {
 		if(curBin == null || !started) return;
 		if(!curBin.dataReceived(dataEvent)){
 			stopDataDelivery(true);
 			return;		
 		}
     }
 
 	public void delAnnot(Annotation a)
 	{
 		for(int i=0; i<annots.getCount(); i++){
 			LObjAnnotation lObjA = (LObjAnnotation)annots.get(i);
 			if(lObjA.getAnnot() == a){
 				annots.del(i);
 			}
 		}
 	}
 
 	public LObjAnnotation findAnnot(Annotation a)
 	{
 		int index;
 		for(int i=0; i<annots.getCount(); i++){
 			LObjAnnotation lObjA = (LObjAnnotation)annots.get(i);
 			if(lObjA.getAnnot() == a){
 				return lObjA;
 			}
 		}
 		return null;
 	}
 
 	public void actionPerformed(ActionEvent annotEvent)
 	{
 		Object obj = annotEvent.getSource();
 		if(obj instanceof Bin){
 			int bIndex = bins.find(obj);
 			if(bIndex < 0 || annots == null) return;
 			Bin b = (Bin)obj;
 			Annotation a = b.getCurAnnot();
 			int aIndex;
 
 			switch(annotEvent.type){
 			case Bin.ANNOT_ADDED:
 				LObjAnnotation lObjA = DataObjFactory.createAnnotation();
 				// watch out for this
 				lObjA.setup(a, null, bIndex);
 				annots.add(lObjA);		
 				break;
 			case Bin.ANNOT_DELETED:				
 				delAnnot(a);
 				break;
 			case Bin.ANNOTS_CLEARED:
 				Vector binAnnots = b.annots;
 				for(int i=0; i<binAnnots.getCount(); i++){
 					delAnnot((Annotation)binAnnots.get(i));
 				}
 				break;
 			} 
 		}
 	}
 
 	public void saveAnnots(LabBookSession session)
 	{
 		DataSource ds = getDataSource(session);
 		if(ds instanceof LObjDataSet){
 			LObjDataSet dSet = (LObjDataSet)ds;
 			dSet.clearAnnots(session);
 			Vector annots = getAnnots();
 			dSet.addAnnots(annots, session);
 		}
 	}
 
 	public Vector getAnnots()
 	{
 		// Should update the data of each annotation
 		return annots;
 	}
 
 	public void getBinAnnots(Vector annots, int binIndex, LObjDataSet dSet)
 	{
 		Bin b = (Bin)bins.get(binIndex);
 		if(b == null || b.annots == null ||
 		   b.annots.getCount() < 1) return;
 
 		for(int i=0; i<b.annots.getCount(); i++){
 			Annotation a = (Annotation)b.annots.get(i);
 			LObjAnnotation lObjA = DataObjFactory.createAnnotation();
 			lObjA.setup(a, dSet, binIndex);
 			annots.add(lObjA);
 		}
 	}
 
 	public void saveData(LObjDataSet dSet, LabBookSession session)
 	{
 		dSet.setUnit(yUnit);
 		dSet.setLabel(yaxis.getLabel());
 		
 		for(int i=0; i<bins.getCount(); i++){
 			dSet.addBin((Bin)bins.get(i), session);
 		}
 	}
 	
 	//???
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
 
 	public String toString()
 	{
 		return super.toString();		
 	}
 
 	public void readExternal(DataStream ds)
 	{
 		dsIndex = ds.readInt();
 		description = ds.readString();
 		visible = ds.readBoolean();
 		linkX = ds.readInt();
 		linkY = ds.readInt();
 	}
 
     public void writeExternal(DataStream ds)
     {
 		ds.writeInt(dsIndex);
 		ds.writeString(description);
 		ds.writeBoolean(visible);
 		ds.writeInt(linkX);
 		ds.writeInt(linkY);
     }
 
 	// Note: we aren't copying the dsIndex
 	public GraphSettings copy(LObjGraph newGraph)
 	{
 		GraphSettings g = new GraphSettings(newGraph, -1);
 
 		g.description = description.toString();
 		g.xLabel = xLabel;
 		g.yLabel = yLabel;
 		g.xUnit = xUnit;
 		g.yUnit = yUnit;		
 		
 		return g;
 	}
 }
