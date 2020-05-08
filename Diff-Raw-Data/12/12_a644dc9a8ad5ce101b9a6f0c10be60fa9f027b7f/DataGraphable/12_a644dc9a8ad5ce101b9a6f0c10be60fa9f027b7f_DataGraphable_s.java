 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
  * $Revision: 1.59 $
  * $Date: 2007-09-24 18:36:49 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.datagraph.engine;
 
 /**
  * DataGraphable
  * This class is an object that takes data from a 
  * data store and draws this data in a graph (it's a Graphable)
  * as a countinuous set of points that can be connected or not
  * (connected by default).
  * It is itself a DataStore with 2 channels (x,y), so it can easily
  * be added to a data table.
  *
  * Date created: June 18, 2004
  *
  * @author Scott Cytacki<p>
  * @author Ingrid Moncada<p>
  *
  */
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Shape;
 import java.awt.Stroke;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Point2D;
 import java.util.Vector;
 
 import javax.swing.event.ChangeEvent;
 
 import org.concord.data.stream.ProducerDataStore;
 import org.concord.framework.data.stream.AutoIncrementDataStore;
 import org.concord.framework.data.stream.DataChannelDescription;
 import org.concord.framework.data.stream.DataProducer;
 import org.concord.framework.data.stream.DataStore;
 import org.concord.framework.data.stream.DataStoreEvent;
 import org.concord.framework.data.stream.DataStoreListener;
 import org.concord.framework.data.stream.WritableDataStore;
 import org.concord.graph.engine.CoordinateSystem;
 import org.concord.graph.engine.DefaultGraphable;
 import org.concord.graph.engine.Graphable;
 import org.concord.graph.engine.MathUtil;
 import org.concord.swing.SelectableAction;
 
 public class DataGraphable extends DefaultGraphable
 	implements DataStoreListener, DataStore
 {
 	protected DataStore dataStore;
 	
 	protected int showSampleLimit = -1;
 	protected boolean showAllChannels = false;
 
 	//By default, it graphs the dt (x axis) and the first channel (y axis) 
 	protected boolean chartByChannelY = false;	// true to use channel number for X axis
 	protected int channelX = -1;
 	protected int channelY = 0;
 	
 	protected Color lineColor = Color.black;
 	protected float lineWidth = 2;
 	
 	protected Stroke stroke;
 	
 	protected GeneralPath path;
 	
 	/**
 	 * This is the path used to draw all the markers on the
 	 * graph.
 	 */
 	protected GeneralPath markerListPath;
 
 	/**
 	 * This is shape of each marker.  The path is transformed
 	 * to the correct position and then added to the markerListPath. 
 	 */
 	protected GeneralPath markerPath = null;
 	
 	/**
 	 * This variable determines if the markers should be filled
 	 */
 	protected boolean fillMarkers = false;
 
 	/**
 	 * This is the color of the markers if it is null then 
 	 * they will be a little darker than the line color
 	 */
 	protected Color markerColor = null;
 	
 	
 	protected Vector dataStoreListeners;
 	
 	protected boolean connectPoints = true; 
 
 	protected static int crossSize = 3;	
 	public final static GeneralPath CROSS_MARKER_PATH = new GeneralPath();
 	static {
 		CROSS_MARKER_PATH.moveTo(-crossSize, -crossSize);
 		CROSS_MARKER_PATH.lineTo(crossSize, crossSize);
 		CROSS_MARKER_PATH.moveTo(-crossSize, crossSize);
 		CROSS_MARKER_PATH.lineTo(crossSize, -crossSize);
 	}
 	
 	private int lastValueCalculated = -1;
 	protected boolean locked = false;
 
 	/*
 	 * validPrevPoint indicates that last point "processed" 
 	 * was a valid point.  This is used to figure out if points
 	 * should be connected.  If there is an invalid point then there
 	 * will be a break in the line drawn.
 	 */
 	private boolean validPrevPoint = false;		
 
 	protected boolean needUpdate = true;
 	
 	// This is used to indicate when a full recalculation is
 	// needed.  It should only be modified in synchronized regions
 	// this replaces the needUpdateDataReceived boolean.  This is basically
 	// the inverse.
 	protected boolean needRecalculate = true;
 	
 	protected boolean autoRepaintData = true;
 	
 	private float minXValue;
 	private float maxXValue;
 	private float minYValue;
 	private float maxYValue;
 	private boolean validMinMax;
 	
 	// If this is true then we have internally created a ProducerDataStore
 	// this would be done if someone adds a dataProducer to us.
 	// This is useful for state saving 
 	private boolean internalProducerDataStore = false;
 
     private Point2D tmpDataPoint = new Point2D.Double();
 
     private boolean useVirtualChannels = false;
 
 	protected SelectableAction action;
     
 	/**
      * Default constructor.
      */
 	public DataGraphable()
 	{
 		path = new GeneralPath();
 		markerListPath = new GeneralPath();
 		dataStoreListeners = new Vector();
 		updateStroke();
 		
 		minXValue = Float.MAX_VALUE;
 		maxXValue = -Float.MAX_VALUE;
 		minYValue = Float.MAX_VALUE;
 		maxYValue = -Float.MAX_VALUE;
 		validMinMax = false;
 	}
 
 	
 	public void connect(DataProducer dataProducer)
 	{
 		if(internalProducerDataStore) {
 			ProducerDataStore pDataStore = (ProducerDataStore) dataStore;
 			if (pDataStore.getDataProducer() == null)
 				pDataStore.setDataProducer(dataProducer);
 		}
 	}
 	
 	public void disconnect(DataProducer dataProducer)
 	{
 		if(internalProducerDataStore) {
 			ProducerDataStore pDataStore = (ProducerDataStore) dataStore;
 			if (pDataStore.getDataProducer() == dataProducer)
 				pDataStore.setDataProducer(null);
 		}
 	}
 		
 	/**
 	 * Sets the data producer of this graphable.
 	 * By default it will graph dt vs channel 0
      * If the data will be shared by more components, this method is not recommended.
      * Create a ProducerDataStore with the data producer and share the data store with other
      * components. Use setDataStore() instead
 	 */
 	public void setDataProducer(DataProducer dataProducer)
 	{
 	    setDataProducer(dataProducer, -1, 0);
 	}
 	
 	/**
      * If the data will be shared by more components, this method is not recommended.
      * Create a ProducerDataStore with the data producer and share the data store with other
      * components. Use setDataStore() instead
 	 * @param dataProducer
 	 * @param channelXAxis
 	 * @param channelYAxis
 	 */
 	public void setDataProducer(DataProducer dataProducer, int channelXAxis, int channelYAxis)
 	{
 		// Create a default data store for this data producer
 		ProducerDataStore pDataStore = new ProducerDataStore(dataProducer);
 		
 		// This sets the internal producer data store to false
 		setDataStore(pDataStore);
 		
 		// We just created an internal datastore so we need to remember it 
 		internalProducerDataStore = true;
 	    
 		setChannelX(channelXAxis);
 		setChannelY(channelYAxis);
 	}
 	
 	/**
 	 * Sets the data store that this graphable will be using 
 	 * to get its data
 	 * By default it will graph dt vs channel 0
 	 * @param dataStore
 	 */
 	public void setDataStore(DataStore dataStore)
 	{
 		if (this.dataStore != null){
 			this.dataStore.removeDataStoreListener(this);
 		}
 		this.dataStore = dataStore;
 		// Assume someone set this from the outside with their own dataStore
 		internalProducerDataStore = false;
 
 		forceRecalculate();
 		
 		if (this.dataStore != null){
 			this.dataStore.addDataStoreListener(this);
 		}
 	}
 	
 	/**
 	 * Sets the data store that this graphable will be using 
 	 * to get its data
 	 * It will graph channelXAxis vs channelYAxis 
 	 * @param dataStore
 	 */
 	public void setDataStore(DataStore dataStore, int channelXAxis, int channelYAxis)
 	{
 		setDataStore(dataStore);
 		setChannelX(channelXAxis);
 		setChannelY(channelYAxis);
 	}	
 	
 	/*
 	 * Resets the data received
 	 */
 	public void reset()
 	{
 		if(locked) return;
 		
 		dataStore.clearValues();
 		
 		forceRecalculate();
 		
 		minXValue = Float.MAX_VALUE;
 		maxXValue = -Float.MAX_VALUE;
 		minYValue = Float.MAX_VALUE;
 		maxYValue = -Float.MAX_VALUE;
 		validMinMax = false;
 		
 		notifyChange();
 	}
 
 	/*
 	 * If set, graphable will show data from all channels, rather
 	 * than just one selected channel.
 	 */
 	public void setShowAllChannels(boolean flag)
 	{
 		showAllChannels = flag;
 		chartByChannelY = flag;
 	}
 	public boolean getShowAllChannels()
 	{
 		return showAllChannels;
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
 		notifyChange();
 	}
 
 	public Color getColor()
 	{
 		return lineColor;
 	}
 	
 	public void setStroke(Stroke stroke){
 		this.stroke = stroke;
 		if (stroke instanceof BasicStroke){
 			lineWidth = ((BasicStroke)stroke).getLineWidth();
 		}
 		notifyChange();
 	}
 	
 	public Stroke getStroke(){
 		return stroke;
 	}
 	
 	/*
 	 * Sets the line width of the path drawn on the graph
 	 */
 	public void setLineWidth(float width)
 	{
 		lineWidth = width;
 		updateStroke();
 	}
 
     /**
 	 * 
 	 */
 	protected void updateStroke()
 	{
 		stroke = new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
 	}
 
 	/**
      *  Draws this object on Graphics g 
      **/
     public void draw(Graphics2D g)
 	{
 		//long b = System.currentTimeMillis();
 		if (needUpdate){
 			update();
 		}
 		
 		Shape oldClip = g.getClip();
 		Color oldColor = g.getColor();
 		Stroke oldStroke = g.getStroke();
 		
 		graphArea.clipGraphics(g);
 		g.setColor(lineColor);
 		g.setStroke(stroke);
 		
 		if (connectPoints){
 			g.draw(path);
 		}
 		
 		if (markerPath != null){
 			if(markerColor != null) {
 				g.setColor(markerColor);
 			} else {
 				g.setColor(lineColor.darker());
 			}
 			
 		    if(fillMarkers) {
 		    	g.fill(markerListPath);
 		    } else {
 		    	g.draw(markerListPath);
 		    }
 		}
 		
 		extendedDraw(g);
 		
 		g.setColor(oldColor);
 		g.setStroke(oldStroke);
 		g.setClip(oldClip);
 
 		//long a = System.currentTimeMillis();		
 		//System.out.println(a-b);
 	}
 
     /**
      * This is added so the DataGraphable class can be extended 
      * to have addition markers around the data.  For example the
      * DataGraphableRegresssion class uses this to put regression lines
      * @param g
      */
     protected void extendedDraw(Graphics2D g)
     {    	
     }
     
     public float handleValue(Object objVal)
     {    	
 		if (objVal instanceof Float){
 			Float xFloat = (Float)objVal;			
 			// Note: the floatValue could be a NaN here but that
 			// will get taken care of automatically later.
 			return xFloat.floatValue();
 		}
 		
 		// Try to parse string. If unsuccessful, don't do anything and
 		// it will return NaN below.
 		if (objVal instanceof String){
 			try {
 				float xFloat = Float.parseFloat((String) objVal);
 				return xFloat;
 			} catch (java.lang.NumberFormatException e){
 			}
 		}
 
 		//Handling non null objects different than Floats
 		if (objVal != null && !(objVal instanceof Float)){
 			System.err.println("Warning! The value is not a Float object: "+objVal);
 		}
 
 		// objVal is either null or something other than a float  
 		return Float.NaN;
     }
     
     public Point2D getRowPoint(int rowIndex, CoordinateSystem coord, 
             Point2D pathPoint)
     {
 		float px, py;
 		Object objVal;
 
 		if (!chartByChannelY) {
 			objVal = dataStore.getValueAt(rowIndex, getDataStoreChannelX());
 			px = handleValue(objVal);
 		}
 		else
 			px = getDataStoreChannelY();
 		
 		objVal = dataStore.getValueAt(rowIndex, getDataStoreChannelY());
 		py = handleValue(objVal);
 		
 		if(Float.isNaN(px) || Float.isNaN(py)) {
 			//We have found an invalid point.  If there was a valid undrawn point 
 			//before this one, then we need to draw it.  
 			//There can only be an undrawn point if we are connecting points and not
 			//drawing crosses
 			// This could be caused if the data store was being updated at the same
 			// as we are looking at it.  This should make this point the last point
 			// in the data store.
 		    return null;
 		}
 		
 		//Always keep the min and max value available
 		if (px < minXValue){
 			minXValue = px;
 		}
 		if (px > maxXValue){
 			maxXValue = px;
 		}
 		if (py < minYValue){
 			minYValue = py;
 		}
 		if (py > maxYValue){
 			maxYValue = py;
 		}
 		
 		// record the fact that the min and maxes are now valid
 		// we could check this variable first but it is fast to just
 		// set it each time
 		validMinMax = true;
 		
 		tmpDataPoint.setLocation(px, py);
 		coord.transformToDisplay(tmpDataPoint, pathPoint);
 				
 		return pathPoint;
     }
     
 	/**
 	 * Handler of the changes in the graph area
 	 */
 	public void stateChanged(ChangeEvent e)
 	{		
 		forceRecalculate();
 		notifyChange();
 	}
     
     protected void resetPaths()
     {
         path.reset();
         markerListPath.reset();
     }
     
     /**
      * 
      */
     public void update()
 	{		
 		int initialI;
 				
 		if (dataStore == null) return;
 		
 		Point2D.Double pathPointHolder = new Point2D.Double();
 		
 		CoordinateSystem coord = getGraphArea().getCoordinateSystem();
 		
 		// if new data was not received or no values have been
 		// calculated then all the points need to be recalculated
 		synchronized(this) {
 		    if (needRecalculate || lastValueCalculated < 0) {
 		        lastValueCalculated = -1;
 		        needRecalculate = false;
 		    }
 		    initialI = lastValueCalculated + 1;
             
             // We set this to false here so that if someone sets it to 
             // true while we are updating then we will be sure to update
             // again.
             needUpdate = false;
 		}
    		
    		// This is a bit a hack to handle cases that aren't handled correctly
    		// if the points are added atomically.  Then there should never be
    		// any invalid points.  So this code shouldn't be run, however
    		// some data stores don't add points atomically so this case might
    		// happen sometimes.
 	    if(initialI != 0 && !validPrevPoint) {
 	        System.err.println("last drawn point was invalid");
 	        
 	        // verify that the previous point really is invalid
 	        // it can happen that is it invalid for a little while while
 	        // it is being added to the DataStore.
 	        // if there was an atomic DataStore store add row this wouldn't
 	        // be a problem.  
 	        Point2D invalidPoint = getRowPoint(initialI-1, coord, pathPointHolder);
 	        if(invalidPoint != null) {
 	            // it wasn't really invalid
 	            initialI--;
 	            if(initialI != 0) {
 	                validPrevPoint = true;
 	            }
 	        }
 	    }
 
 	    if(initialI == 0) {
 	        resetPaths();
 	        
 	        // the previous point is invalid because there is no
 	        // previous point
 	        validPrevPoint = false;
 	        
 	        minXValue = Float.MAX_VALUE;
 	        maxXValue = -Float.MAX_VALUE;
 	        minYValue = Float.MAX_VALUE;
 	        maxYValue = -Float.MAX_VALUE;
 	        validMinMax = false;
 	    }
 	    
 	    if (!showAllChannels)
 	    	updateOneChannel(initialI, pathPointHolder, coord);
 	    else {
 	    	int yChannelBase = channelsNeedAdjusting() ? 1 : 0;
 	    	for (int nth = 0; nth < dataStore.getTotalNumChannels(); nth++) {
 	    		setChannelY(nth+yChannelBase);
 	    		updateOneChannel(initialI, pathPointHolder, coord);
 	    	}
 	    }
 	}
 
     public void updateOneChannel(int initialI, Point2D.Double pathPointHolder, CoordinateSystem coord) {
 		//undrawnPoint stores the last point that was "processed" 
 		//which was valid but it wasn't drawn.  If crosses are
 		//being drawn or if points aren't connected then all points are drawn as they
 		//are processed.  Points are not drawn when they are processed, 
 		//if they might be the first point of a line segment.
 		//When the undrawnPoint is recorded the path is movedTo to this undrawn spot
 		Point2D undrawnPoint = null;	
 		
 		Point2D currentPathPoint;
 
    		int totalNumSamples = dataStore.getTotalNumSamples();
 		float thresholdPointTheSame = (float)(lineWidth/2 - 0.01);
		int channelColumnN = getDataStoreChannelY()+1;
 		
		boolean isFirstPoint = true;
 		int nthSample = (showSampleLimit!=1) ? initialI :
 						((totalNumSamples>0) ? (totalNumSamples-1)
 											 : 0);
 		if(connectPoints){
 		    float lastPathX = Float.NaN;
 		    float lastPathY = Float.NaN;
 			currentPathPoint = path.getCurrentPoint();
 			if(currentPathPoint != null) {
 			    lastPathX = (float)currentPathPoint.getX();
 			    lastPathY = (float)currentPathPoint.getY();
 			}
 			
 			Point2D pathPoint = null;
 			for(; nthSample < totalNumSamples; nthSample++){
 			    pathPoint = getRowPoint(nthSample, coord, pathPointHolder);
 
 			    if(pathPoint == null) {
 					//We have found an invalid point.  If there was a valid undrawn point 
 					//before this one, then we need to draw it.  
 					//There can only be an undrawn point if we are connecting points and not
 					//drawing crosses
 					if (undrawnPoint != null){
 						drawPoint(undrawnPoint);
 						undrawnPoint = null;
 					}
 					// This could be caused if the data store was being updated at the same
 					// as we are looking at it.  This should make this point the last point
 					// in the data store.
 					validPrevPoint = false;
 					continue;								
 				}
 				
 				float ppx, ppy;
 				ppx = (float)pathPoint.getX();
 				ppy = (float)pathPoint.getY();
 				
 				if (MathUtil.equalsFloat(ppx, lastPathX, thresholdPointTheSame) && 
 					MathUtil.equalsFloat(ppy, lastPathY, thresholdPointTheSame)){
 					continue;
 				}
 
				if (validPrevPoint && (!isFirstPoint || showAllChannels)){
 				    path.lineTo(ppx, ppy);
 				    undrawnPoint = null;					
 				}
 				else{
					isFirstPoint = false;
 				    path.moveTo(ppx, ppy);
 				    
 				    if (undrawnPoint != null){
 				        //if this statement is reach then there is an error in this
 				        //algorythm
 				        throw new RuntimeException("Assert: We have an undrawn point that will be forgotten");
 				    }
 				    
 				    //We aren't going to draw this point so we need to 
 				    //remember it so we can draw it later.
 				    undrawnPoint = path.getCurrentPoint();
 				}				
 			
 				lastPathX = ppx;
 				lastPathY = ppy;
 				
 				// If we made it here then the current point (soon to be the prev point)
 				// is a valid point, so set the flag
 				// technically we only care about this if we are connecting points
 				// but it seemed easier to understand if this is done out here
 				validPrevPoint = true;
 				drawPointMarker(ppx, ppy, nthSample);
 			}
 		    
 		} else {
 			Point2D pathPoint = null;
 			for(nthSample=initialI; nthSample < totalNumSamples; nthSample++){
 			    pathPoint = getRowPoint(nthSample, coord, pathPointHolder);
 
 			    if(pathPoint == null) {
 					//We have found an invalid point.  If there was a valid undrawn point 
 					//before this one, then we need to draw it.  
 					//There can only be an undrawn point if we are connecting points and not
 					//drawing crosses
 					if (undrawnPoint != null){
 						drawPoint(undrawnPoint);
 						undrawnPoint = null;
 					}
 					// This could be caused if the data store was being updated at the same
 					// as we are looking at it.  This should make this point the last point
 					// in the data store.
 					validPrevPoint = false;
 					continue;								
 				}
 				
 				float ppx, ppy;
 				ppx = (float)pathPoint.getX();
 				ppy = (float)pathPoint.getY();
 
 				currentPathPoint = path.getCurrentPoint();			
 				
 				float dy = 1;//TODO dy is 1 because of MAC OS X
 				
 				if (currentPathPoint != null && 
 						MathUtil.equalsDouble(ppx, currentPathPoint.getX(), thresholdPointTheSame) && 
 						MathUtil.equalsDouble(ppy, currentPathPoint.getY() - dy, thresholdPointTheSame)){
 					//System.out.println("Not adding this point:"+ppx+","+ppy+" "+lastPathPoint);
 					continue;
 				}
 
 				drawPoint(ppx, ppy);
 				
 				drawPointMarker(ppx, ppy, nthSample);
 				
 				// If we made it here then the current point (soon to be the prev point)
 				// is a valid point, so set the flag
 				// technically we only care about this if we are connecting points
 				// but it seemed easier to understand if this is done out here
 				validPrevPoint = true;
 			}
 		    
 		}
 		
 		// This is to handle a threading issue
 		// the lastValueCalculated could be set by forceRecalcutate
 		// to -1.  Then this statement is reached where we want to set it to i-1
 		synchronized(this) {
 			if(needRecalculate) {
 				// This means the forceRecalculate method was called while we were 
 				// updating the paths.  That means we need to update the paths again
 				// a check for this could be put in the inner loop so we didn't waste time
 			} else {
 				// we we don't need to recalculate then we can remember the last point we
 				// did calculate and start from there next time.
 				lastValueCalculated = nthSample-1;				
 			}
 		}
 		
 		//There is a point that hasn't been drawn yet
 		//we will draw it here, however this might cause a display glitch.  
 		//If several points
 		//are sent and the second to the last is an invalid point then this
 		//will draw the last point, but in the next "update" a valid point could
 		//be added.  In this case there will be an extra point drawn.  In this case 
 		//there should just be a line.  This could be fixed by removing the extra point 
 		//on the next draw.
 		if (undrawnPoint != null){
 			drawPoint(undrawnPoint);
 			undrawnPoint = null;
 		}
 		
 		//System.out.println("size:"+yValues.size());		
 	}
 
 	/**
 	 * @param ppx
 	 * @param ppy
 	 */
 	protected void drawPoint(float ppx, float ppy)
 	{
 		// If we are not connecting points and there is a marker
 		// then we don't need to draw these one pixel points
 		// It is not clear when these points are drawn
 		// in generall they won't be seen because the line will be
 		// be on top of them.  I believe they are to handle
 		// the case when there is a discontinutity before and
 		// after the current point.  In that case this needs 
 		// be used.
 		if(!isConnectPoints() || markerPath != null) return;
 		
 		//Make a vertical "dot" of 1 pixel
 		path.moveTo(ppx, ppy);
 		path.lineTo(ppx, ppy + 1);//TODO Is 1 because of MAC OS X
 	}
 
 	/**
 	 * @param ppx
 	 * @param ppy
 	 */
 	private void drawPoint(Point2D p)
 	{
 		//Make a vertical "dot" of 1 pixel
 		drawPoint((float)p.getX(), (float)p.getY());
 	}
 	
 	protected void drawPointMarker(float ppx, float ppy, int sample)
 	{
 		if(markerPath != null){
 			GeneralPath newMarker = (GeneralPath)markerPath.clone();
 			java.awt.Rectangle bounds = newMarker.getBounds();
 			float needDX = (ppx - (bounds.x + bounds.width/2));
 			float needDY = (ppy - (bounds.y + bounds.height/2));
 			newMarker.transform(AffineTransform.getTranslateInstance(needDX, needDY));
 			markerListPath.append(newMarker, false);
 		}
 	}
     
 	/** 
 	 * Returns a copy of itself 
 	 */
 	public Graphable getCopy()
 	{
 		DataGraphable g = new DataGraphable();
 		g.setColor(lineColor);
 		g.setLineWidth(lineWidth);
 		g.setDataStore(dataStore);
 		
 		//FIXME Add values to the vector... is that enough?
 		//for(int i=0; i<yValues.size(); i++){
 		//	g.yValues.add(yValues.elementAt(i));
 		//}
 		
 		return g;
 	}
 	
     /** 
      * Set this to true if you want the channel numbers presented
      * to the outside to always start at 0.  
      * This is to abstract the case where the datastore has a dt.
      * In that case the dt (usually x) channel of the datastore is -1
      * 
      * So if useVirtualChannels is true then channel 0 will really 
      * be -1 of the datastore if it has a dt or 0 if it doesn't.
      * The other channels will be shifted too.
      * 
      * This is useful when a single datagraphable can have different
      * datastores plugged into it.  Some of which have dts and some
      * don't.  
      */ 
 	public void setUseVirtualChannels(boolean flag)
 	{
 	    useVirtualChannels = flag;
 	}
 	
 	public boolean useVirtualChannels()
 	{
 	    return useVirtualChannels;
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
 	
 	protected boolean channelsNeedAdjusting()
 	{
 		// If this graphable is not using virtual channels then adjusting is
 		// not needed.  There might still be some virtualChannels being used by the 
 		// dataStore itself.
 	    if(!useVirtualChannels) {
 	        return false;
 	    }
 
 	    // Need to check if the data store is using virtual channels itself
 	    // if it is, then we don't need to adjust them twice.
 	    DataStore dStore = getDataStore();	    
 	    if((dStore instanceof ProducerDataStore) &&
 	    		((ProducerDataStore)dStore).useVirtualChannels()) {
 	    	return false;
 	    }
 	    
 	    // If we have a dt channel then the channels need to be adjusted.
 	    return hasDtChannel();
 	}
 	
 	protected int getDataStoreChannelX()
 	{
 		if(channelsNeedAdjusting()){
 			return channelX - 1;
 		} 
 		
 		return channelX;
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
 
 	protected int getDataStoreChannelY()
 	{
 		if(channelsNeedAdjusting()){
 			return channelY - 1;
 		} 
 		
 		return channelY;
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
 		if (this.connectPoints == connectPoints) return;
 		this.connectPoints = connectPoints;
 		forceRecalculate();
 		notifyChange();
 	}
 	
 /*	//Debugging purposes
 	public void setData(Vector xValues, Vector yValues)
 	{
 		this.xValues = xValues;
 		this.yValues = yValues;
 		
 		needUpdate = true;
 		needUpdateDataReceived = false;
 	}
 */
 	
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
 		if (needUpdate){
 			super.notifyChange();
 		}
 	}
 	
 	/**
 	 * This method is used to set the style of marker.
 	 *  If it is null then no marker is drawn.  A marker
 	 *  is a shape drawn at each data point on the graph.
 	 * 
 	 * @param userPath
 	 */
     public void setMarkerPath(GeneralPath userPath){
         markerPath = userPath;
 
 		// this should probably all be put into the 
 		// the setMarkerPath method.
 		forceRecalculate();
 		notifyChange();
     }
 	
     /**
      * Set this to true if you want the markers to be filled in
      * @param fillMarkers
      */
 	public void setFillMarkers(boolean fillMarkers)
 	{
 		this.fillMarkers = fillMarkers;
 	}
     
 	
 	public boolean isFillMarkers()
 	{
 		return fillMarkers;
 	}
 		
 	public void setMarkerColor(Color markerColor)
 	{
 		this.markerColor = markerColor;
 	}
 	
 	
 	public Color getMarkerColor()
 	{
 		return markerColor;
 	}
 	
 	/**
 	 * @return Returns the showCrossPoint.
 	 */
 	public boolean isShowCrossPoint()
 	{
 		return markerPath == CROSS_MARKER_PATH;
 	}
 	
 	/**
 	 * @param showCrossPoint The showCrossPoint to set.
 	 */
 	public void setShowCrossPoint(boolean showCrossPoint)
 	{
 		if (showCrossPoint) {
     		// if we are already showing the same marker path then
     		// we don't need to update it 
 		    if(markerPath == CROSS_MARKER_PATH) return;
 		   
 			setMarkerPath(CROSS_MARKER_PATH);
 		} else {
 			setMarkerPath(null);
 		} 
 	}
 	
 	/**
 	 * This returns the data producer of this graphable only if the data producer was
 	 * added with setDataProducer.  If a dataproducer is linked to this graphable through
 	 * a custom data store then null will be returned. 
 	 * 
 	 * @return Returns the data producer.
 	 */
 	public DataProducer getDataProducer()
 	{
 		if(internalProducerDataStore) {
 			ProducerDataStore pDataStore = (ProducerDataStore)dataStore;
 			return pDataStore.getDataProducer();
 		}
 		return null;
 	}
 	
 	/**
 	 * This will return a data producer related to this graphable.
 	 * If setDataProducer was called then that prodcuer will be returned.
 	 * If setDataStore was called with a ProducerDataStore, then the
 	 * producer of that datastore will be returned.
 	 * 
 	 * @return
 	 */
 	public DataProducer findDataProducer()
 	{
 	    // This class internally creates a producer data store when
 	    // the setDataProducer method is called.  So we just need
 	    // to check for those types of dataStores	    
 	    if(dataStore instanceof ProducerDataStore) {
 	        return ((ProducerDataStore)dataStore).getDataProducer();
 	    }
 	    return null;
 	}
 	
 	public float getMinXValue()
 	{
 		if(!validMinMax) {
 			return Float.NaN;
 		}
 		return minXValue;
 	}
 
 	public float getMaxXValue()
 	{
 		if(!validMinMax) {
 			return Float.NaN;
 		}
 		return maxXValue;
 	}
 	
 	public float getMinYValue()
 	{
 		if(!validMinMax) {
 			return Float.NaN;
 		}
 		return minYValue;
 	}
 
 	public float getMaxYValue()
 	{
 		if(!validMinMax) {
 			return Float.NaN;
 		}
 		return maxYValue;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataStoreListener#dataAdded(org.concord.framework.data.stream.DataStoreEvent)
 	 */
 	public void dataAdded(DataStoreEvent evt)
 	{
 		needUpdate = true;
 		
 		notifyChange();
 	}
 
 	protected synchronized void forceRecalculate()
 	{
 		needUpdate = true;
 		needRecalculate = true;
 		
 		// You can't count on this value staying -1 
 		// because the update function might be in the middle
 		// of running.  At the end of the update function this value
 		// is changed. 
 		lastValueCalculated = -1;	    
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataStoreListener#dataRemoved(org.concord.framework.data.stream.DataStoreEvent)
 	 */
 	public void dataRemoved(DataStoreEvent evt)
 	{
 	    forceRecalculate();
 	    
 		notifyChange();
 	}
 
 	/**
 	 * @see org.concord.framework.data.stream.DataStoreListener#dataChanged(org.concord.framework.data.stream.DataStoreEvent)
 	 */
 	public void dataChanged(DataStoreEvent evt)
 	{
 	    forceRecalculate();
 	    	    
 		notifyChange();
 	}
 
 	protected boolean hasDtChannel()
 	{
 	    DataStore dStore = getDataStore();
 	    
 	    if((!(dStore instanceof AutoIncrementDataStore))) {
 	        return false;
 	    }
 	    
 	    return ((AutoIncrementDataStore)dStore).isAutoIncrementing();
 	}	
 	
 	/**
 	 * @see org.concord.framework.data.stream.DataStoreListener#dataChannelDescChanged(org.concord.framework.data.stream.DataStoreEvent)
 	 */
 	public void dataChannelDescChanged(DataStoreEvent evt)
 	{
 	    notifyChange();
 	}
 	
 	/**
 	 * Even if setDataStore has not been call this might return
 	 * a non null value.  If setDataProducer was called then an
 	 * internal ProducerDataStore was created, and that would be
 	 * returned here.  
 	 * 
 	 * @return Returns the dataStore.
 	 */
 	public DataStore getDataStore()
 	{
 		return dataStore;
 	}
 
 	/**
 	 * @see org.concord.framework.data.stream.DataStore#getTotalNumSamples()
 	 */
 	public int getTotalNumSamples()
 	{
 		if (dataStore == null) return 0;
 		return dataStore.getTotalNumSamples();
 	}
 
 	/**
 	 * @see org.concord.framework.data.stream.DataStore#getTotalNumChannels()
 	 */
 	public int getTotalNumChannels()
 	{
 		return 2;
 	}
 
 	/**
 	 * @see org.concord.framework.data.stream.DataStore#getValueAt(int, int)
 	 */
 	public Object getValueAt(int numSample, int numChannel)
 	{
 		if (numChannel == 0){
 			return dataStore.getValueAt(numSample, getDataStoreChannelX());
 		}
 		else if (numChannel == 1){
 			return dataStore.getValueAt(numSample, getDataStoreChannelY());
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Only works with a Writable Data Store!
 	 * @param numSample
 	 * @param numChannel
 	 * @param value
 	 */
 	public void setValueAt(int numSample, int numChannel, Object value)
 	{
 		//Only works with a Writable Data Store!
 		if (!(dataStore instanceof WritableDataStore)) {
 			throw new UnsupportedOperationException("The Data Store "+dataStore+" is not Writable!");
 		}
 		
 		if (numChannel == 0){
 			((WritableDataStore)dataStore).setValueAt(numSample, getDataStoreChannelX(), value);
 		}
 		else if (numChannel == 1){
 			((WritableDataStore)dataStore).setValueAt(numSample, getDataStoreChannelY(), value);
 		}
 	}
 
 	/**
 	 * Only works with a Writable Data Store!
 	 * @param numSample
 	 */
 	public void removeSampleAt(int numSample)
 	{
 		//Only works with a Writable Data Store!
 		if (!(dataStore instanceof WritableDataStore)) {
 			throw new UnsupportedOperationException("The Data Store "+dataStore+" is not Writable!");
 		}
 		
 		((WritableDataStore)dataStore).removeSampleAt(numSample);
 	}
 	
 	/**
 	 * Only works with a Writable Data Store!
 	 * @param numSample
 	 */
 	public void insertSampleAt(int numSample)
 	{
 		//Only works with a Writable Data Store!
 		if (!(dataStore instanceof WritableDataStore)) {
 			throw new UnsupportedOperationException("The Data Store "+dataStore+" is not Writable!");
 		}
 		
 		((WritableDataStore)dataStore).insertSampleAt(numSample);
 	}
 	
 	public void addPoint(double x, double y)
 	{
 		//Only works with a Writable Data Store!
 		if (!(dataStore instanceof WritableDataStore)) {
 			throw new UnsupportedOperationException("The Data Store "+dataStore+" is not Writable!");
 		}
 		
 		int newPointIndex = getTotalNumSamples();
 		setValueAt(newPointIndex, 0, new Float(x));
 		setValueAt(newPointIndex, 1, new Float(y));
 	}
 	
 	/**
 	 * @see org.concord.framework.data.stream.DataStore#addDataStoreListener(org.concord.framework.data.stream.DataStoreListener)
 	 */
 	public void addDataStoreListener(DataStoreListener l)
 	{
 		if (dataStore == null) return;
 		dataStore.addDataStoreListener(l);
 	}
 
 	/**
 	 * @see org.concord.framework.data.stream.DataStore#removeDataStoreListener(org.concord.framework.data.stream.DataStoreListener)
 	 */
 	public void removeDataStoreListener(DataStoreListener l)
 	{
 		if (dataStore == null) return;
 		dataStore.removeDataStoreListener(l);
 	}
 
 	/**
 	 * Channel 0 is the x channel of the graphable and
 	 * Channel 1 is the y channel of the graphable
 	 * 
 	 * @see org.concord.framework.data.stream.DataStore#getDataChannelDescription(int)
 	 */
 	public DataChannelDescription getDataChannelDescription(int numChannel)
 	{
 		if (dataStore == null) return null;
 		if (numChannel == 0){
 			return dataStore.getDataChannelDescription(getDataStoreChannelX());
 		}
 		else if (numChannel == 1){
 			return dataStore.getDataChannelDescription(getDataStoreChannelY());
 		}
 				
 		throw new ArrayIndexOutOfBoundsException("requested channel: " + numChannel + 
 				" is not valid for DataGraphable");
 	}
 
 	/**
 	 * @see org.concord.framework.data.stream.DataStore#clearValues()
 	 */
 	public void clearValues()
 	{
 		reset();
 	}
 	
 	/**
 	 * @param p
 	 * @return
 	 */
 	public int getIndexValueAtDisplay(Point p, int threshold)
 	{
 		Point2D pW, pD;
 		float x, y;
 		Object objVal;
 		
 		CoordinateSystem cs = getGraphArea().getCoordinateSystem();
 		
 		for (int i=0; i<getTotalNumSamples(); i++){
 			
 			objVal = getValueAt(i, 0);
 			if (!(objVal instanceof Float)) continue;
 			x = ((Float)objVal).floatValue();
 			
 			objVal = getValueAt(i, 1);
 			if (!(objVal instanceof Float)) continue;
 			y = ((Float)objVal).floatValue();
 			
 			pW = new Point2D.Double(x,y);
 			pD = cs.transformToDisplay(pW);
 			
 			//Threshold
 			if (Math.abs(pD.getX() - p.getX()) <= threshold &&
 					Math.abs(pD.getY() - p.getY()) <= threshold){
 				return i;
 			}
 		}
 		
 		return -1;
 	}
 	
 	/*
 	//It would be nice to have something like this:
 	
 	public float[] getYValue(float xValue)
 	{
 	}
 
 	public float[] getYValue(float xValue)
 	{
 	}
 	*/
 	
 	public void releaseAll()
 	{
 		if (dataStore != null){
 			dataStore.removeDataStoreListener(this);
 		}
 		if (graphArea != null){
 			graphArea.removeChangeListener(this);
 		}
 		remove();
 	}
 	
 	public void setLocked(boolean locked) {
 		this.locked = locked;
 	}
 	public boolean isLocked() {
 		return locked;
 	}
 	
 	/**
 	 * @return Returns the lineWidth.
 	 */
 	public float getLineWidth()
 	{
 		return lineWidth;
 	}
 
 	public boolean isInternalProducerDataStore()
     {
 	    return internalProducerDataStore;
     }
 	
 	public void setAction(SelectableAction action){
 		this.action = action;
 	}
 }
