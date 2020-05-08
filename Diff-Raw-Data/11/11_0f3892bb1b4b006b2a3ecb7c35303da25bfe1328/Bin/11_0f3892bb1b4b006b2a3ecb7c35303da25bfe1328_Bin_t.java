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
 
 public class Bin
 {
     public static int START_DATA_SIZE = 10000;
 
     int [] points;
     float [] values;
     int numPoints;
     int numValues;
     int lastPlottedPoint;
     int c;
     int collection;
     Axis xaxis, yaxis;
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
     LineGraph graph;
     int [] color = {255,0,0};
 
     public Bin(LineGraph g)
     {
 	// We store three ints for each point
 	// (x),(avgY),(maxOff << 16 | -minOff)
 	points = new int [START_DATA_SIZE*3];
 	values = new float [START_DATA_SIZE*2];
 
 	// System.out.println("Creating bin with size:" + START_DATA_SIZE);
 
 	graph = g;
 	reset();
     }
 
     public void recalc()
     {
 	int i;
 	int lastOffset = numValues*2;
 	int newX, newY;
 	int curPtPos = 0;
 	int avgY;
 
 	numPoints = 0;
 	numXs = 0;
 	
	if(numValues < 2){
 	    return;
 	}
 
 	i=0;
 	curX = (int)(values[i++]* xaxis.scale);
 	minPtY = maxPtY = sumY = (int)(values[i++]* yaxis.scale);
 	numXs = 1;
 
 	// Set the last value to some non valid x
 	// This will cause the inner loop to break out
	values[numValues*2] = -100000f;
 	
 	newX = (int)(values[i] * xaxis.scale);
 	i++;
 	newY = (int)(values[i] * yaxis.scale);
 	i++;		
 
 	while(true){
 	    while(newX == curX){
 		sumY += newY;
 		numXs++;
 		if(newY > maxPtY) maxPtY = newY;
 		else if(newY < minPtY) minPtY = newY;
 
 		newX = (int)(values[i] * xaxis.scale);
 		i++;
 		newY = (int)(values[i] * yaxis.scale);
 		i++;		
 	    }
 	    points[curPtPos++] = curX;
 	    avgY = sumY / numXs;
 	    points[curPtPos++] = avgY;
 	    points[curPtPos++] = (maxPtY - avgY) << 16 | (avgY - minPtY);
 	    if(i >= lastOffset) break;
 	    curX = newX;
 	    numXs = 0;
 	    sumY = 0;
 	    maxPtY = minPtY = newY;
 	}
 
 	numPoints = curPtPos / 3;
 	lastCalcValue = i-2;
     }
 
     int lastCalcValue = 0;
 
     public boolean update()
     {
 	int i;
 	int lastOffset = numValues*2;
 	int newX, newY;
 	int curPtPos = (numPoints-1)*3;
 	int avgY;
 
	if(numPoints < 2){
 	    recalc();
 	    return true;
 	}
 
 	if(numValues - lastCalcValue/2 < 1){
 	    return false;
 	}
 
 	i=lastCalcValue;
 
 	// Set the last value to some non valid x
 	// This will cause the inner loop to break out
	values[numValues*2] = -100000f;
 	
 	newX = (int)(values[i] * xaxis.scale);
 	i++;
 	newY = (int)(values[i] * yaxis.scale);
 	i++;		
 
 	while(true){
 	    while(newX == curX){
 		sumY += newY;
 		numXs++;
 		if(newY > maxPtY) maxPtY = newY;
 		else if(newY < minPtY) minPtY = newY;
 
 		newX = (int)(values[i] * xaxis.scale);
 		i++;
 		newY = (int)(values[i] * yaxis.scale);
 		i++;		
 	    }
 	    points[curPtPos++] = curX;
 	    avgY = sumY / numXs;
 	    points[curPtPos++] = avgY;
 	    points[curPtPos++] = (maxPtY - avgY) << 16 | (avgY - minPtY);
 	    if(i >= lastOffset) break;
 	    curX = newX;
 	    numXs = 0;
 	    sumY = 0;
 	    maxPtY = minPtY = newY;
 	}
 
 	numPoints = curPtPos / 3;
 	lastCalcValue = i-2;
 
 	return true;
     }
 
 
     public boolean addPoint(float x, float value)
     {
 	int offset = numValues*2;
 
 	// should check the current config
 	if(offset >= (values.length - 2)){
 	    // x is out of bounds
 	    // **Need to have calling funct do this*** 
 	    // endCollection();
 
 	    return false;
 	}
 	
 	if(offset == 0){
 	    refY = value;
 	}
 
 	if(maxX < x) maxX = x;
 	if(minX > x) minX = x;
 	values[offset] = x - refX;
 	offset++;
 	
 	values[offset] = value - refY;
 	if(maxY < value) maxY = value;
 	if(minY > value) minY = value;
 
 	numValues++;
 
 	return true;
     }
 
     public boolean addPoints(int num, float [] data)
     {
 	int offset = numValues*2;
 	int i;
 	float x, value;
 	int endPos = num*2;
 	
 	if(offset == 0){
 	    refY = data[1];
 	}
 
 	for(i=0; i<endPos; i+=2){
 	    offset= numValues*2;
 
 	    // should check the current config
 	    if(offset >= (values.length - 2)){
 		// x is out of bounds
 		// **Need to have calling funct do this*** 
 		// endCollection();
 		
 		return false;
 	    }
 	
 	    x = data[i];
 	    if(maxX < x) maxX = x;
 	    if(minX > x) minX = x;
 	    values[offset] = x - refX;
 	    offset++;
 	
 	    value = data[i+1];
 	    values[offset] = value - refY;
 	    if(maxY < value) maxY = value;
 	    if(minY > value) minY = value;
 
 	    numValues++;
 	}
 
 	// Get the older values up to date.
 	update();
 
 	return true;
     }
 
     void reset()
     {
 	int i;
 
 	minX = minY = 1;
 	maxX = maxY = -1;
 
 	for(i=0; i<100; i++){
 	    minX *= (float)10;
 	    maxX *= (float)10;
 	    minY *= (float)10;
 	    maxY *= (float)10;
 	}
 
 	numPoints = 0;
 	numValues = 0;
 	lastPlottedPoint = -1;
 	lastCalcValue = 0;
     }
 
     public boolean getValue(float time, float [] value)
     {
 	int i;
 
 	for(i = 0; i < numValues; i++){
 	    if(time - values[i*2] < (float)0.01){
 		value[0] = values[i*2+1] + refY;
 		return true;
 	    }
 	}
 
 	return false;
     }
 }
 
 
