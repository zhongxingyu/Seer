 /*
   Copyright (C) 2001 Concord Consortium
 
   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 package graph;
 
 import waba.ui.*;
 import waba.fx.*;
 import waba.io.*;
 import waba.sys.*;
 import waba.util.*;
 import org.concord.waba.extra.event.*;
 import org.concord.waba.extra.util.*;
 import extra.util.*;
 
 public class Bin
     implements DecoratedValue, ActionListener
 {
     public static int START_DATA_SIZE = 10000;
 
     public int [] points = null;
     public int numPoints;
     int lastPlottedPoint;
     int c;
     int collection;
     int curX;
     int sumY;
     int numXs;
     int minPtY;
     int maxPtY;
     public float refX = 0f;
     public float refY = 0f;
     public float minX, minY, maxX, maxY;
     int [] color = {255,0,0};
     public Axis xaxis = null;
 	public Axis yaxis = null;
 	boolean needRecalc = false;
 
     public String label;
     public LargeFloatArray lfArray = new LargeFloatArray();
     public String description = null;
     public Time time = null;
 	float curXscale, curYscale;
 	CCUnit unit;
     public Vector annots = new Vector();
 
     public Bin(Axis xAx, Axis yAx)
     {
 		xaxis = xAx;
 		yaxis = yAx;
 
 		xaxis.addActionListener(this);
 		yaxis.addActionListener(this);
 
 		// We store three ints for each point
 		// (x),(avgY),(maxOff << 16 | -minOff)
 		// points = new int [START_DATA_SIZE*3];
 
 		// System.out.println("Creating bin with size:" + START_DATA_SIZE);
 
 		reset();
     }
 
 	public void setXAxis(Axis xAx)
 	{
 		xaxis.removeActionListener(this);
 		xaxis = xAx;
 		xaxis.addActionListener(this);
 	}
 
 	public void free()
 	{
 		xaxis.removeActionListener(this);
 		yaxis.removeActionListener(this);
 		binListeners = null;		
 	}
 
 	public void actionPerformed(ActionEvent e)
 	{
 		if(e.type == Axis.SCALE_CHANGE){
 			needRecalc = true;
 		}
 	}
 
     public String getLabel()
     {
 		return label;
     }
 
     public float getValue()
     {
 		if(lfArray.getCount() == 0) return 0f;
 		return lfArray.getFloat(lfArray.getCount()-1) + lfArray.ref;
     }
 
 	public Annotation getCurAnnot()
 	{
 		if(annots != null &&
 		   annots.getCount() > 0){
 			return (Annotation)annots.get(annots.getCount() - 1);
 		}
 		return null;
 	}
 
 	public Annotation addAnnot(String label, float time)
 	{
 		boolean valid = false;
 		float [] tempVal = new float [1];
 		Annotation a = null;
 
 		valid = getValue(time, tempVal);
 
 		if(valid){
 			a = new Annotation(label, time, tempVal[0], xaxis);
 			annots.add(a);
 		}
 
 		notifyListeners(ANNOT_ADDED);
 
 		return a;
 	}
 
 	ActionEvent annotEvent = new ActionEvent(this, null, null);
 	public final static int ANNOT_ADDED = 4000;
 	public final static int ANNOT_DELETED = 4001;
 	Vector binListeners = new Vector();
 
 	public void addActionListener(ActionListener al)
 	{
 		binListeners.add(al);
 	}
 
 	public void removeActionListener(ActionListener al)
 	{
 		int index = binListeners.find(al);
 		if(index >= 0) binListeners.del(index);
 	}
 
     void notifyListeners(int type)
 	{
 		annotEvent.type = type;
 		for(int i=0; i<binListeners.getCount(); i++){
 			ActionListener al = (ActionListener)binListeners.get(i);
 			al.actionPerformed(annotEvent);
 		}
 	}
 
 	public void drawCrossHairs(Graphics g, int xPos, int yPos, Axis xaxis)
 	{
 		g.setColor(0,0,0);
 		g.drawLine(xPos, yaxis.drawnY, xPos, yaxis.drawnY+yaxis.dispLen);
 		g.drawLine(xaxis.drawnX, yPos, xaxis.drawnX+xaxis.dispLen, yPos);
 	}
 
    public void drawAnnots(Graphics g, int annotTopY, Axis parentXaxis)
     {
 		int i;
 		Annotation a;
 		int pos;
 		int xPos;
 		int valPos;
 
 		for(i=0; i<annots.getCount(); i++){
 			a = (Annotation)annots.get(i);
 			if(xaxis.drawnX != -1){
 				pos = (int)((a.time - xaxis.dispMin) * xaxis.scale);
 				if((pos*xaxis.axisDir >= 0) && 
 				   (pos*xaxis.axisDir < xaxis.axisDir*xaxis.dispLen)){
 					xPos = pos + xaxis.drawnX + xaxis.axisDir;
 					a.draw(g, xPos- a.width/2, annotTopY);
 					if(a.selected){
 						valPos = (int)((a.value - yaxis.min) * yaxis.scale) + yaxis.drawnOffset;
						drawCrossHairs(g, xPos, valPos, parentXaxis);
 					}
 				}
 			}
 		}
     }
 
     /*
      *  We need to check the bounds or only take
      * one direction or something
      */
     Annotation getAnnotAtPoint(int x)
 	{
 		int i;
 		Annotation a;
 	
 		// We draw them forward, so we search backwards
 		//   because they might overlap
 		for(i=annots.getCount()-1; i>=0; i--){
 			a = (Annotation)annots.get(i);
 			if(a.checkPos(x)) return a;
 		}
 	
 		// We didn't find any anotations
 		return null;			
     }
 
     public float getTime()
     {
 		return getCurX();
     }
 
     public Color getColor()
     {
 		return null;
     }
 
 	public CCUnit getUnit()
 	{
 		return unit;
 	}
 
 	public void setUnit(CCUnit u)
 	{
 		unit = u;
 	}
 
     public float getCurX()
     {
 		if(lfArray.getCount() == 0) return 0f;
 		return maxX;
     }
 
     public int getNumVals()
     {
 		return lfArray.getCount();
     }
 
 	int remainder;
 	int remainderSum;
 
     public void recalc()
     {
 		curXscale = xaxis.scale;
 		curYscale = yaxis.scale;
 
 		lastCalcValue = 1;
 		needRecalc = false;
     }
 
     int lastCalcValue = 0;
 
     public boolean update(boolean recalc)
     {
 		int i;
 		int numValues = lfArray.getCount();
 		int newX, newY;
 		int avgY;
 
 		if(recalc && needRecalc) recalc();
 
 		if(numValues < 2 ||
 		   numValues - lastCalcValue < 1){
 			return false;
 		} else if(lastCalcValue == 1){
 			// we've got points for the first time
 			// reset again
 			resetPts();
 		}
 
 		int curPtPos;
 
 		curPtPos = (numPoints-1)*3;
 
 		i=lastCalcValue;
 	
 		remainderSum += remainder;
 		newX = curX + remainderSum/denom;
 		newY = (int)(lfArray.getFloat(i) * curYscale);
 		i++;		
 
 		while(true){
 			while(newX == curX && i < numValues){
 				sumY += newY;
 				numXs++;
 				if(newY > maxPtY) maxPtY = newY;
 				else if(newY < minPtY) minPtY = newY;
 
 				remainderSum += remainder;
 				newX = curX + remainderSum/denom;
 				newY = (int)(lfArray.getFloat(i) * curYscale);
 				i++;		
 
 			}
 			points[curPtPos++] = curX;
 			avgY = sumY / numXs;
 			points[curPtPos++] = avgY;
 			points[curPtPos++] = (maxPtY - avgY) << 16 | (avgY - minPtY);
 			if(i >= numValues) break;
 			curX = newX;
 			remainderSum = remainderSum % denom;
 			numXs = 0;
 			maxPtY = minPtY = newY;
 			sumY = 0;
 		}
 
 		if(curX != newX){
 			// This means we have a newX 
 			// so what is curX
 			// it is the old x.
 			// if we don't change it then we need to 
 			// reparse the newX point
 			// however we should change it 
 			curX = newX;
 			remainderSum = remainderSum % denom;
 			minPtY = maxPtY = sumY = newY;
 			numXs = 1;
 
 			// We increase the i to move the lastCalcVal next
 			i++;
 
 			// This is just an estimate
 			// so we need to set the curPtPos to 
 			// check this point the next time
 			points[curPtPos++] = curX;
 			avgY = sumY / numXs;
 			points[curPtPos++] = avgY;
 			points[curPtPos++] = (maxPtY - avgY) << 16 | (avgY - minPtY);
 		} else {
 			// They match so we should update the sums and xs
 
 			sumY += newY;
 			numXs++;
 			if(newY > maxPtY) maxPtY = newY;
 			else if(newY < minPtY) minPtY = newY;
 
 			i++;
 
 			// update the points.
 			curPtPos-=3;
 			points[curPtPos++] = curX;
 			avgY = sumY / numXs;
 			points[curPtPos++] = avgY;
 			points[curPtPos++] = (maxPtY - avgY) << 16 | (avgY - minPtY);
 
 		}
 		// else the curX == newX so 
 		// we should be ok
 		numPoints = curPtPos / 3;
 		lastCalcValue = i-1;
 
 		return true;
     }
 
     public float dT = 0f;
     int sampSize = 1;
     public boolean dataReceived(DataEvent dataEvent)
     {
 		if(lfArray.getCount() == 0 && dataEvent.numbSamples > 0){
 			dT = dataEvent.getDataDesc().getDt();
 			sampSize = dataEvent.getDataDesc().getChPerSample();
 			refY = dataEvent.data[dataEvent.dataOffset];
 			lfArray.ref = refY;
 			minX = 0;
 			if(points == null){
 				points = new int [START_DATA_SIZE*3];
 			}
 		}
 
 		maxX += dT*dataEvent.numbSamples;
 	
 		boolean ret = lfArray.addFloats(dataEvent.data, dataEvent.dataOffset, 
 										sampSize, dataEvent.numbSamples);
 
 		minY = lfArray.min;
 		maxY = lfArray.max;
 		return ret;
     }
 
 	int denom = 0;
 
 	void resetPts()
 	{
 		if(curXscale != 0f && dT != 0f){
 			denom = (int) (10000f/curXscale/dT);
 		} else {
 			denom = 1;
 		}
 		remainder = (int)(dT*curXscale*denom);
 		remainderSum = 0;
 		numXs = 0;
 		curX = 0;
 		minPtY = maxPtY = sumY = (int)(lfArray.getFloat(0)* curYscale);
 		lastCalcValue = 1;
 		numXs = 1;
 		numPoints = 1;
 	}
 
     public void reset()
     {
 		int i;
 		lfArray.clear();
 		minX = minY = 1;
 		maxY = -1;
 		maxX = 0f;
 		for(i=0; i<100; i++){
 			minX *= (float)10;
 			minY *= (float)10;
 			maxY *= (float)10;
 		}
 		lastPlottedPoint = -1;
 		resetPts();
 
 		// remove annotations
 		annots = new Vector();
 
 		needRecalc = true;
     }
 
     public boolean getValue(float time, float [] value)
     {
 		int i;
 		if(time > 0f && time < maxX){
 			value[0] = lfArray.getFloat((int)(time / dT )) + refY;
 			return true;
 		}
 		return false;
     }
 
     public int numDataChunks(){return 1;}
 
     public DataEvent getDataChunk(int index)
     {
 		DataEvent dEvent;
 		DataDesc dDesc = new DataDesc(dT, 1);
 		float [] data = lfArray.getFloats(0, lfArray.getCount());
 		int numValues = lfArray.getCount();
 
 		for(int i=0; i<numValues; i++){
 			data[i] += refY;
 		}
 
 		dEvent = new DataEvent(DataEvent.DATA_RECEIVED, 
 							   0, data , dDesc);
 		dEvent.dataOffset = 0;
 		dEvent.numbSamples = numValues;
 		return dEvent;
     }
 
	boolean visible = true;
	public void setVisible(boolean val)
	{
		visible = val;
	}

 	public void draw(Graphics g)
 	{
		if(!visible)return;
 		if(xaxis.estimateScale || yaxis.estimateScale){
 			drawEst(g);
 			return;
 		}
 
 		int i,j,k;
 		int lastX, lastY, curX, curY;
 		Axis xa = xaxis;
 
 		if(xa.drawnX == -1) return;
 
 		update(true);
 
 		if(numPoints < 2) return;
 
 		int lastOffset = numPoints*3;	    
 		int xOffset = (int)((xa.dispMin - refX) * xa.scale);
 		int xTrans = xa.drawnX - xOffset + xa.axisDir;
 		int yTrans = (int)((refY - yaxis.dispMin) * yaxis.scale) + yaxis.drawnY + yaxis.axisDir;
 	    
 		g.translate(xTrans, yTrans);
 		g.setColor(color[0], color[1], color[2]);
 
 		if(lastPlottedPoint == -1){
 			i=0;
 		} else {
 			// Start 1 before the lastPlottedPoint, because the lastpp might have
 			// moved
 			i = lastPlottedPoint*3 - 3;
 		}
 				
 		lastX = points[i++];
 		lastY = points[i++];
 		i++;
 		
 		for(; i<lastOffset;){
 			curX = points[i++];
 			curY = points[i++];
 			i++;
 		
 			if(curX > (xOffset - 1) && curX <= (xOffset + xa.dispLen))
 				g.drawLine(lastX, lastY, curX, curY);
 			
 			lastY = curY;
 			lastX = curX;
 		}
 	    
 		g.translate(-xTrans, -yTrans);
 
 		lastPlottedPoint = numPoints - 1;
 	}
 
 	public static int FIXED_PT_DENOM = 1000;
 
 	public void drawEst(Graphics g)
 	{
 		int xNum = (int)(xaxis.scale*(float)FIXED_PT_DENOM/curXscale); 
 		int yNum = (int)(yaxis.scale*(float)FIXED_PT_DENOM/curYscale);
 		int i,j,k;
 		int lastOffset;
 		int lastX, lastY, curX, curY;
 		Axis xa = xaxis;
 
 		if(xa.drawnX == -1) return;
 		update(false);
 
 		if(numPoints < 2) return;
 	    
 		lastOffset = numPoints*3;
 	    
 		int xOffset = (int)((xa.dispMin - refX) * xa.scale);
 		int xTrans = xa.drawnX - xOffset + xa.axisDir;
 		int yTrans = (int)((refY - yaxis.dispMin) * yaxis.scale) + yaxis.drawnY + yaxis.axisDir;
 
 		g.translate(xTrans, yTrans);
 		g.setColor(color[0], color[1], color[2]);
 
 		if(lastPlottedPoint == -1){
 			i=0;
 		} else {
 				i = lastPlottedPoint*3;
 		}
 				
 		lastX = points[i++] * xNum / FIXED_PT_DENOM;
 		lastY = points[i++] * yNum / FIXED_PT_DENOM;
 		i++;
 		
 		if(yaxis.scale == curYscale ){
 			for(; i<lastOffset;){
 				curX = points[i++] * xNum / FIXED_PT_DENOM;
 				curY = points[i++];
 				i++;
 					
 				if(curX > (xOffset - 1) && curX <= (xOffset + xa.dispLen))
 					g.drawLine(lastX, lastY, curX, curY);
 					
 				lastY = curY;
 				lastX = curX;
 			}
 		} else if(xaxis.scale == curXscale){
 			for(; i<lastOffset;){
 				curX = points[i++];
 				curY = points[i++] * yNum / FIXED_PT_DENOM;
 				i++;
 					
 				if(curX > (xOffset - 1) && curX <= (xOffset + xa.dispLen))
 					g.drawLine(lastX, lastY, curX, curY);
 					
 				lastY = curY;
 				lastX = curX;
 			}
 		} else {
 			for(; i<lastOffset;){
 				curX = points[i++] * xNum / FIXED_PT_DENOM;
 				curY = points[i++] * yNum / FIXED_PT_DENOM;
 				i++;
 					
 				if(curX > (xOffset - 1) && curX <= (xOffset + xa.dispLen))
 					g.drawLine(lastX, lastY, curX, curY);
 					
 				lastY = curY;
 				lastX = curX;
 			}
 		}
 	    
 		g.translate(-xTrans, -yTrans);
 
 		lastPlottedPoint = numPoints - 1;
 	}
 }
 
 
