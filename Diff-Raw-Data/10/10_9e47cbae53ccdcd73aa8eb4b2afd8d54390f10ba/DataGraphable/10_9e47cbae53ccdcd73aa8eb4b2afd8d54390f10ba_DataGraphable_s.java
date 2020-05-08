 /*
  * Last modification information:
 * $Revision: 1.8 $
 * $Date: 2004-09-17 18:04:09 $
  * $Author: imoncada $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.datagraph.engine;
 
 /**
  * DataGraphable
  * This class is an object that listens to data (it's a data listener)
  * from a data producer,
  * and draws this data in a graph (it's a Graphable)
  * as a countinuous CONNECTED set of points.
  *
  * Date created: June 18, 2004
  *
  * @author Scott Cytacki<p>
  * @author Ingrid Moncada<p>
  *
  */
 import java.awt.*;
 import java.awt.geom.*;
 import java.util.*;
 
 import org.concord.graph.engine.*;
 import org.concord.framework.data.stream.*;
 
 public class DataGraphable extends DefaultGraphable
 	implements DataListener, DataStore
 {
 	protected DataProducer dataSource;
 
 	protected Vector xValues;
 	protected Vector yValues;
 	
 	//By default, it graphs the dt (x axis) and the first channel (y axis) 
 	protected int channelX = -1;
 	protected int channelY = 0;
 	
 	protected Color lineColor = Color.black;
 	protected float lineWidth = 2;
 
 	private int dataOffset = 0;
 	private int nextSampleOffset = 1;
 	private float dt = 1;
 
 	protected GeneralPath path;
 		
 	protected DataStreamDescription dataStreamDesc;
 	
 	protected Vector dataStoreListeners;
 	
 	protected boolean connectPoints = true; 
 	
 	private float lastTime;
 	private int lastValueCalculated = -1;
 	protected boolean needUpdate = true;
 	protected boolean needUpdateDataReceived = false;
 	
 	protected boolean autoRepaintData = true;
 	
 	/**
      * Default constructor.
      */
 	public DataGraphable()
 	{
 		xValues = new Vector();
 		yValues = new Vector();
 		path = new GeneralPath();
 		dataStoreListeners = new Vector();
 	}
 	
 	/**
      * Default constructor.
      */
 	public DataGraphable(DataProducer source)
 	{
 		this();
 		setDataProducer(source);
 	}
 	
 	/*
 	 * Sets the data producer of this graphable
 	 */
 	public void setDataProducer(DataProducer source)
 	{
 		if (dataSource != null){
 			dataSource.removeDataListener(this);
 		}
 		dataSource = source;
 		if (dataSource != null){
 			updateDataDescription(dataSource.getDataDescription());
 			dataSource.addDataListener(this);
 		}
 	}
 	
 	public void setDataProducer(DataProducer source, int channelXAxis, int channelYAxis)
 	{
 		setDataProducer(source);
 		setChannelX(channelXAxis);
 		setChannelY(channelYAxis);
 	}
 	
 
 	/*
 	 ** Handler of the data received event
 	 * @see org.concord.framework.data.stream.DataListener#dataReceived(org.concord.framework.data.stream.DataEvent)
 	 */
 	public void dataReceived(DataStreamEvent dataEvent)
 	{
 		DataStreamEvent de;		
 		if (!(dataEvent instanceof DataStreamEvent)) return;
 		de = (DataStreamEvent)dataEvent;
 		
 		float [] data = de.getData();
 		int numberOfSamples = de.getNumSamples();
 		int sampleIndex;
 		
 		sampleIndex = dataOffset;
 		
 		for(int i=0; i<numberOfSamples; i++)
 		{
 			if (channelX != -1){
 				xValues.addElement(new Float(data[sampleIndex + channelX]));
 			}
 			
 			if (channelY != -1){
 				yValues.addElement(new Float(data[sampleIndex + channelY]));
 			}
 			
 			sampleIndex+= nextSampleOffset;
 		}
 
 		needUpdate = true;
 		needUpdateDataReceived = true;
 		
 		notifyChange();
 		notifyDataAdded();
 	}
 
 	/*
 	 * Resets the data received
 	 */
 	public void reset()
 	{
 		yValues.removeAllElements();
 		needUpdate = true;
 		needUpdateDataReceived = false;
 		lastTime = 0;
 		notifyChange();
 	}
 
 	/*
 	 * Sets the color of the path drawn on the graph
 	 */
 	public void setColor(int r, int g, int b)
 	{
 		setColor(new Color(r,g,b));
 	}
 
 	/*
 	 * Sets the color of the path drawn on the graph
 	 */
 	public void setColor(Color c)
 	{
 		lineColor = c;
 	}
 
 	/*
 	 * Sets the line width of the path drawn on the graph
 	 */
 	public void setLineWidth(float width)
 	{
 		lineWidth = width;
 	}
 
 	protected void updateDataDescription(DataStreamDescription desc)
 	{
 		dataStreamDesc = desc;
 		dataOffset = desc.getDataOffset();
 		nextSampleOffset = desc.getNextSampleOffset();
 		dt = desc.getDt();
 	}
 
 	public void dataStreamEvent(DataStreamEvent dataEvent)
 	{
 		DataStreamEvent de;		
 		if (!(dataEvent instanceof DataStreamEvent)) return;
 		de = (DataStreamEvent)dataEvent;
 		
 		updateDataDescription(de.getDataDescription());
 	}
 
     /**
      *  Draws this object on Graphics g 
      **/
     public void draw(Graphics2D g)
 	{
 		long b = System.currentTimeMillis();
     	if (needUpdate){
     		update();
     	}
 		
 		Shape oldClip = g.getClip();
 		Color oldColor = g.getColor();
 		Stroke oldStroke = g.getStroke();
 		
 		graphArea.clipGraphics(g);
 		g.setColor(lineColor);
 		g.setStroke(new BasicStroke(lineWidth));
 		
 		g.draw(path);
 		
 		g.setColor(oldColor);
 		g.setStroke(oldStroke);
 		g.setClip(oldClip);
 
		long a = System.currentTimeMillis();
		
 		//System.out.println(a-b);
 	}
 
     /**
      * 
      */
     public void update()
 	{		
 		float ppx, ppy;
 		float px, py;
 		
 		Point2D lastPathPoint;
 
 		Point2D.Double pathPoint = new Point2D.Double();
 
 		CoordinateSystem coord = getGraphArea().getCoordinateSystem();
 
 		float time;
 		int initialI;
 		
     	if (!needUpdateDataReceived || lastTime == 0){
     		path.reset();
     		time = 0;
     		initialI = 0;
     	}
     	else{
     		time = lastTime;
     		initialI = lastValueCalculated + 1;
     	}
     	
 		for(int i=initialI; i<yValues.size(); i++){
 			
 			if (channelX == -1){
 				px = time;
 			}
 			else{
 				Float xFloat = (Float)xValues.elementAt(i);
 				px = xFloat.floatValue(); 
 			}
 			
 			if (channelY == -1){
 				py = time;
 			}
 			else{
 				Float yFloat = (Float)yValues.elementAt(i);
 				py = yFloat.floatValue();
 			}
 			
 			Point2D.Double dataPoint = new Point2D.Double(px, py);
 			
 			coord.transformToDisplay(dataPoint, pathPoint);
 			
 			ppx = (float)pathPoint.x;
 			ppy = (float)pathPoint.y;
 
 			lastPathPoint = path.getCurrentPoint();
 			
 			double thresholdPointTheSame = lineWidth/2 - 0.01;
 			float dy = 0;
 			if (!connectPoints){
 				dy = 1;//TODO dy is 1 because of MAC OS X
 			}
 			
 			if (lastPathPoint != null && 
 					MathUtil.equalsDouble(ppx, lastPathPoint.getX(), thresholdPointTheSame) && 
 					MathUtil.equalsDouble(ppy, lastPathPoint.getY() - dy, thresholdPointTheSame)){
 				//System.out.println("Not adding this point:"+ppx+","+ppy+" "+lastPathPoint);
 			}
 			else{
 				
 				if (connectPoints){
 					if (i==0){
 						path.moveTo(ppx, ppy);
 					}
 					else{
 						path.lineTo(ppx, ppy);
 					}
 				}
 				else{
 					//Make a vertical "dot" of 1 pixel
 					path.moveTo(ppx, ppy);
 					path.lineTo(ppx, ppy + dy);//TODO dy is 1 because of MAC OS X
 				}
 			}
 			
 			lastTime = time;
 			lastValueCalculated = i;
 			
 			time += dt;
 		}
 			
 		//System.out.println("size:"+yValues.size());
 		
 		needUpdateDataReceived = false;
 		needUpdate = false;
 	}
 
 	/** 
 	 * Returns a copy of itself 
 	 */
 	public Graphable getCopy()
 	{
 		DataGraphable g = new DataGraphable();
 		g.setColor(lineColor);
 		g.setLineWidth(lineWidth);
 		g.setDataProducer(dataSource);
 		
 		//FIXME Add values to the vector... is that enough?
 		for(int i=0; i<yValues.size(); i++){
 			g.yValues.add(yValues.elementAt(i));
 		}
 		
 		return g;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataStore#getTotalNumSamples()
 	 */
 	public int getTotalNumSamples()
 	{
 		return yValues.size();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataStore#getTotalNumChannels()
 	 */
 	public int getTotalNumChannels()
 	{
 		return 2;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataStore#getValueAt(int, int)
 	 */
 	public Object getValueAt(int numSample, int numChannel)
 	{
 		Float val = null; 
 		
 		if (numChannel == 0){
 			if (channelX == -1){
 				val = new Float(dt * numSample);
 			}
 			else{
 				val = (Float)xValues.elementAt(numSample);
 			}
 		}
 		else if (numChannel == 1){
 			if (channelY == -1){
 				val = new Float(dt * numSample);
 			}
 			else{
 				val = (Float)yValues.elementAt(numSample);
 			}
 		}
 		
 		return val;
 	}
 
 	/**
 	 * @see org.concord.framework.data.stream.DataStore#getDataChannelDescription(int)
 	 */
 	public DataChannelDescription getDataChannelDescription(int numChannel)
 	{
 		DataChannelDescription channelDesc = null;
 		if (dataStreamDesc != null){
 			channelDesc = dataStreamDesc.getChannelDescription(numChannel);
 		}
 		if (channelDesc == null){
 			channelDesc = new DataChannelDescription();
 			channelDesc.setPrecision(2);
 			if (numChannel == 0){
 				channelDesc.setName("x value");
 			}
 			else if (numChannel == 1){
 				channelDesc.setName("y value");
 			}
 		}
 		return channelDesc;
 	}
 
 	/**
 	 * @see org.concord.framework.data.stream.DataStore#addDataStoreListener(org.concord.framework.data.stream.DataStoreListener)
 	 */
 	public void addDataStoreListener(DataStoreListener l)
 	{
 		if (!dataStoreListeners.contains(l)){
 			dataStoreListeners.add(l);
 		}
 	}
 
 	/**
 	 * @see org.concord.framework.data.stream.DataStore#removeDataStoreListener(org.concord.framework.data.stream.DataStoreListener)
 	 */
 	public void removeDataStoreListener(DataStoreListener l)
 	{
 		dataStoreListeners.remove(l);		
 	}
 	
 	protected void notifyDataAdded()
 	{
 		DataStoreEvent evt = new DataStoreEvent(this, DataStoreEvent.DATA_ADDED);
 		DataStoreListener l;
 		for (int i=0; i<dataStoreListeners.size(); i++){
 			l = (DataStoreListener)dataStoreListeners.elementAt(i);
 			l.dataAdded(evt);
 		}
 	}
 	
 	protected void notifyDataRemoved()
 	{
 		DataStoreEvent evt = new DataStoreEvent(this, DataStoreEvent.DATA_ADDED);
 		DataStoreListener l;
 		for (int i=0; i<dataStoreListeners.size(); i++){
 			l = (DataStoreListener)dataStoreListeners.elementAt(i);
 			l.dataRemoved(evt);
 		}
 	}
 	
 	protected void notifyDataChanged()
 	{
 		DataStoreEvent evt = new DataStoreEvent(this, DataStoreEvent.DATA_ADDED);
 		DataStoreListener l;
 		for (int i=0; i<dataStoreListeners.size(); i++){
 			l = (DataStoreListener)dataStoreListeners.elementAt(i);
 			l.dataChanged(evt);
 		}
 	}
 	
 	/**
 	 * @return Returns the lineColor.
 	 */
 	public Color getLineColor()
 	{
 		return lineColor;
 	}
 	
 	/**
 	 * @param lineColor The lineColor to set.
 	 */
 	public void setLineColor(Color lineColor)
 	{
 		this.lineColor = lineColor;
 	}
 	/**
 	 * @return Returns the channelX.
 	 */
 	public int getChannelX()
 	{
 		return channelX;
 	}
 	/**
 	 * @param channelX The channelX to set.
 	 */
 	public void setChannelX(int channelX)
 	{
 		if (channelX < -1){
 			channelX = -1;
 		}
 		this.channelX = channelX;
 	}
 	/**
 	 * @return Returns the channelY.
 	 */
 	public int getChannelY()
 	{
 		return channelY;
 	}
 	/**
 	 * @param channelY The channelY to set.
 	 */
 	public void setChannelY(int channelY)
 	{
 		if (channelY < -1){
 			channelY = -1;
 		}
 		this.channelY = channelY;
 	}
 	/**
 	 * @return Returns the connectPoints.
 	 */
 	public boolean isConnectPoints()
 	{
 		return connectPoints;
 	}
 	/**
 	 * @param connectPoints The connectPoints to set.
 	 */
 	public void setConnectPoints(boolean connectPoints)
 	{
 		this.connectPoints = connectPoints;
 	}
 	
 	//Debugging purposes
 	public void setData(Vector xValues, Vector yValues)
 	{
 		this.xValues = xValues;
 		this.yValues = yValues;
 		
 		needUpdate = true;
 		needUpdateDataReceived = false;
 	}
 	/**
 	 * @return Returns the autoRepaintData.
 	 */
 	public boolean isAutoRepaintData()
 	{
 		return autoRepaintData;
 	}
 	/**
 	 * @param autoRepaintData The autoRepaintData to set.
 	 */
 	public void setAutoRepaintData(boolean autoRepaintData)
 	{
 		this.autoRepaintData = autoRepaintData;
 	}
 	
 	/**
 	 * @see org.concord.graph.engine.DefaultGraphable#notifyChange()
 	 */
 	protected void notifyChange()
 	{
 		if (autoRepaintData){
 			super.notifyChange();
 		}
 	}
 
 	/**
 	 * @see org.concord.graph.engine.DefaultGraphable#notifyChange()
 	 */
 	public void repaint()
 	{
 		if (needUpdate || needUpdateDataReceived){
 			super.notifyChange();
 		}
 	}
 }
