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
 import extra.util.*;
 
 public class Bin
     implements DecoratedValue
 {
     public static int START_DATA_SIZE = 10000;
 
     int [] points = null;
     int numPoints;
     int lastPlottedPoint;
     int c;
     int collection;
     int lastPlottedY;
     int lastPlottedX;
     int curX;
     int sumY;
     int numXs;
     int minPtY;
     int maxPtY;
     float refX = 0f;
     float refY = 0f;
     public float minX, minY, maxX, maxY;
     int [] color = {255,0,0};
     public int xaIndex = 0;
     
     public String label;
     public LargeFloatArray lfArray = new LargeFloatArray();
     public String description = null;
     public Time time = null;
 
     public Bin(int xIndex)
     {
 		xaIndex = xIndex;
 
 		// We store three ints for each point
 		// (x),(avgY),(maxOff << 16 | -minOff)
 		// points = new int [START_DATA_SIZE*3];
 
 		// System.out.println("Creating bin with size:" + START_DATA_SIZE);
 
 		reset();
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
 
     public float getTime()
     {
 		return getCurX();
     }
 
     public Color getColor()
     {
 		return null;
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
 
     public void recalc(float xscale, float yscale)
     {
 		int i;
 		int numValues = lfArray.getCount();
 		int newX, newY;
 		int curPtPos = 0;
 		int avgY;
 		float xMult = dT*xscale;
 		int remainder = (int)(xMult*numValues);
 		int remainderSum = 0;
 
 		numPoints = 0;
 		numXs = 0;
 	
 		if(numValues < 2){
 			return;
 		}
 
 		i=0;
 		curX = 0;
 	
 		minPtY = maxPtY = sumY = (int)(lfArray.getFloat(0)* yscale);
 		i++;
 		numXs = 1;
 
 		remainderSum += remainder;
 		newX = curX + remainderSum/numValues;
 		newY = (int)(lfArray.getFloat(i) * yscale);
 		i++;		
 
 		while(true){
 			while(newX == curX && i < numValues){
 				sumY += newY;
 				numXs++;
 				if(newY > maxPtY) maxPtY = newY;
 				else if(newY < minPtY) minPtY = newY;
 
 				remainderSum += remainder;
 				newX = curX + remainderSum/numValues;
 				newY = (int)(lfArray.getFloat(i) * yscale);
 				i++;		
 
 			}
 			points[curPtPos++] = curX;
 			avgY = sumY / numXs;
 			points[curPtPos++] = avgY;
 			points[curPtPos++] = (maxPtY - avgY) << 16 | (avgY - minPtY);
 			if(i >= numValues) break;
 			curX = newX;
 			remainderSum = remainderSum % numValues;
 			numXs = 0;
 			sumY = 0;
 			maxPtY = minPtY = newY;
 		}
 
 		numPoints = curPtPos / 3;
 		lastCalcValue = i-1;
     }
 
     int lastCalcValue = 0;
 
     public boolean update(float xscale, float yscale)
     {
 		int i;
 		int numValues = lfArray.getCount();
 		int newX, newY;
 		int curPtPos = (numPoints-1)*3;
 		int avgY;
 		float xMult = dT*xscale;
 
 		if(numPoints < 1){
 			recalc(xscale, yscale);
 			return true;
 		}
 
 		if(numValues - lastCalcValue < 1){
 			return false;
 		}
 
 		i=lastCalcValue;
 
 	
 		newX = (int)((float)i * xMult);
 		newY = (int)(lfArray.getFloat(i) * yscale);
 		i++;		
 
 		while(true){
 			while(newX == curX && i < numValues){
 				sumY += newY;
 				numXs++;
 				if(newY > maxPtY) maxPtY = newY;
 				else if(newY < minPtY) minPtY = newY;
 
 				newX = (int)((float)i * xMult);
 				newY = (int)(lfArray.getFloat(i) * yscale);
 				i++;		
 
 			}
 			points[curPtPos++] = curX;
 			avgY = sumY / numXs;
 			points[curPtPos++] = avgY;
 			points[curPtPos++] = (maxPtY - avgY) << 16 | (avgY - minPtY);
 			if(i >= numValues) break;
 			curX = newX;
 			numXs = 0;
 			sumY = 0;
 			maxPtY = minPtY = newY;
 		}
 
 		numPoints = curPtPos / 3;
 		lastCalcValue = i-1;
 
 		return true;
     }
 
 
     public float dT = 0f;
     int sampSize = 1;
 
     public boolean dataReceived(DataEvent dataEvent)
     {
 		if(lfArray.getCount() == 0){
 			dT = dataEvent.getDataDesc().getDt();
 			sampSize = dataEvent.getDataDesc().getChPerSample();
 			refY = dataEvent.data[dataEvent.dataOffset];
 			lfArray.ref = refY;
 			minX = 0;
 			if(points == null){
 				points = new int [START_DATA_SIZE*3];
 			}
 		}
 
		maxX = dT*dataEvent.numbSamples;
 	
 		boolean ret = lfArray.addFloats(dataEvent.data, dataEvent.dataOffset, 
 										sampSize, dataEvent.numbSamples);
 
 		minY = lfArray.min;
 		maxY = lfArray.max;
 		return ret;
     }
 
     void reset()
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
 
 		numPoints = 0;
 		lastPlottedPoint = -1;
 		lastCalcValue = 0;
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
 	    
 }
 
 
