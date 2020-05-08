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
 
 import waba.fx.*;
 import waba.ui.*;
 import waba.util.*;
 import waba.sys.*;
 import extra.util.Maths;
 import extra.util.*;    
 import extra.io.*;
 import org.concord.waba.extra.event.*;
 
 public class Axis
 {
     public final static int X_SCREEN_AXIS = 0;
     public final static int Y_SCREEN_AXIS = 1;
 
     public final static int TOP = 0;
     public final static int BOTTOM = 1;
     public final static int LEFT = 2;
     public final static int RIGHT = 3;
 
     int orient = X_SCREEN_AXIS;
 
     TextLine label = null;
 
     /*  The absolute min and max
      *  The Axis is designed to show a portion of its full length
      *  These values are the full length values.
      *  the disp* values are the "portion". 
 	 *
 	 *  Also note the int values are in screen resolution and can 
 	 *  be negative. (think about the yaxis)  So for a regular
 	 *  yaxis dispLen and length will be negative
      */
     float min;
     int length;
 
     // This is for external use it does not affect the drawing
     float max;
 
     // Range to display
     public float dispMin;
     public int dispLen;
 
 	// This is set when readExternal is called
 	// it can be used to make the axis maintain it's dispMax
 	// on different scales
 	public float readDispMax;
     
     // If the absolute min is at 0 in screen coordiates
     // this is the position of dispMin 
     public int dispOffset;
 
     // (screen units)/(input units)
     public float scale;
 
     // 1 or -1 this gives the sign of the scale
     int axisDir;
 
     // This is used to compute the graph grid
     // You can adjust this to compensate for diff screen sizes
     // and resolutions
     int minMajTicSpacing = 20;
 
     // This is computed from the length and the minTicSpacing
     // it should be a relatively nice number (1,2,5,10) in the correct 
     // magnitude of course
     float majTicStep = 1f;
 
     // This is used to compute the minor grid spacing
     // Hopefully this will be picked integently 
     int numMinTics = 1;
 
     // Whether the tics go in the direction of the screen coords
     // or opposite them
     int ticDir = 1;
 
     int gridEndOff = 100;
     int gridDir = -1;
 
     int [] gridMinCol = {205,205,205};
     int [] gridMajCol = {170,170,170};
     int [] axisCol = {0,0,0};
 
     int labelEdge, labelOff;
 
     int minTicSize = 3;
     int majTicSize = 5;
 
 	int maxDigits = 3;
 
     public int drawnX = -1;
     int drawnY = -1;
     int drawnOffset = 1;
 
     boolean nonNegative = true;
 
 	boolean estimateScale = false;
 	float oldScale = 1f;
 
 	TextLine axisLabel = null;
 	String axisLabelStr;
 	CCUnit axisLabelUnit;
 
 	Vector scaleListeners = new Vector();
 
     public Axis(int type)
     {
 		this.min = dispMin = 0f;
 		dispOffset = 0;	
 		needCalcTics = true;
 
 		switch(type){
 		case BOTTOM:
 			length = dispLen = 100;	
 			axisDir = 1;
 			ticDir = 1;
 			orient = X_SCREEN_AXIS;
 			labelOff = 7;
 			labelEdge = TextLine.TOP_EDGE;
 			minMajTicSpacing = 40;
 			break;
 		case LEFT:
 			length = dispLen = -100;	
 			axisDir = -1;
 			ticDir = -1;
 			orient = Y_SCREEN_AXIS;
 			labelOff = -6;
 			labelEdge = TextLine.RIGHT_EDGE;
 			axisDir = -1;
 			gridDir = 1;
 			nonNegative = false;
 			break;
 		}
 
 		scale = 1f*(float)axisDir;
     }
 
 	public void init()
 	{
 		if(label == null){
 			label = new TextLine();
 			label.maxDigits = 2;
 		}
 
 		if(axisLabel == null){
			axisLabelChanged = true;
 			switch(orient){
 			case X_SCREEN_AXIS:
 				axisLabel = new TextLine(TextLine.RIGHT);
 				break;
 			case Y_SCREEN_AXIS:
 				axisLabel = new TextLine(TextLine.UP);
 				break;
 			}
 		}
 	}
 
 	public void setLength(int len)
 	{
 		length = dispLen = len;	
 	}
 
 	public void setMaxDigits(int axisDigits)
 	{
 		maxDigits = axisDigits;
 	}
 
 	ActionEvent changeEvent = new ActionEvent(this, null, null);
 	public final static int SCALE_CHANGE = 3000;
 	public final static int ORIGIN_CHANGE = 3001;
 	public final static int LABEL_CHANGE = 3002;
 
 	public void addActionListener(ActionListener al)
 	{
 		scaleListeners.add(al);
 	}
 
 	public void removeActionListener(ActionListener al)
 	{
 		int index = scaleListeners.find(al);
 		if(index >= 0) scaleListeners.del(index);
 	}
 
     void notifyListeners(int type)
 	{
 		changeEvent.type = type;
 		for(int i=0; i<scaleListeners.getCount(); i++){
 			ActionListener al = (ActionListener)scaleListeners.get(i);
 			al.actionPerformed(changeEvent);
 		}
 	}
 
 	private boolean axisLabelChanged = true;
 	public void setAxisLabel(String label, CCUnit unit)
 	{
 		axisLabelChanged = true;
 		axisLabelStr = label;
 		axisLabelUnit = unit;
 		notifyListeners(LABEL_CHANGE);
 	}
 
 	public String getLabel(){return axisLabelStr;}
 
     public void setRange(float min, float range)
     {
 		setDispMin(min);
 		setRange(range);
     }
 
     public void setRange(float range)
     {		
 		setScale((dispLen - axisDir) / range);
     }
 
 	int drawnLabelExp = -10000;
 	public void drawAxisLabel(Graphics g, int edgePos)
 	{
 		if(axisLabelChanged || labelExp != drawnLabelExp){
 			String unitStr;
 			if(axisLabelUnit != null){
 				unitStr = " (" + axisLabelUnit.abbreviation + ")";
 			} else {
 				unitStr = "";
 			}
 
 			if(labelExp != 0){
 				axisLabel.setText(axisLabelStr + unitStr + "  10^"+ labelExp);
 			} else {
 				axisLabel.setText(axisLabelStr + unitStr);
 			}
 			drawnLabelExp = labelExp;
 			axisLabelChanged = false;
 		}
 
 		if(labelEdge == TextLine.RIGHT_EDGE){
 		   axisLabel.drawCenter(g, edgePos, drawnY + dispLen/2, TextLine.LEFT_EDGE);
 		} else if(labelEdge == TextLine.TOP_EDGE){
 			axisLabel.drawCenter(g, drawnX + dispLen/2, edgePos, TextLine.BOTTOM_EDGE);
 		}
 	}
 
     public void free()
     {
 		int i;
 
 		if(label != null)label.free();
 		label = null;
 
 		if(majTicLabels != null){
 			for(i=0; i<majTicLabels.length; i++){
 				if(majTicLabels[i] != null){
 					majTicLabels[i].free();
 				}				
 			}
 		}
 		majTicLabels = null;
 		ticOffsets = null;
 		numTics = 0;
 		
 
 		if(axisLabel != null)axisLabel.free();
 		axisLabel = null;
     }
 
 	public float getValue(int pos)
 	{
 		if(orient == Axis.Y_SCREEN_AXIS){
 			if(pos*axisDir > drawnY*axisDir &&
 			   pos*axisDir < axisDir*(drawnY + axisDir + dispLen)){
 				return Maths.NaN;
 			}
 
 			return (pos - drawnY) / scale + dispMin;
 		} else {
 			if(pos*axisDir > drawnX*axisDir && 
 			   pos*axisDir < axisDir*(drawnX + axisDir + dispLen)){
 				return Maths.NaN;
 			}
 
 			return (pos - drawnX) / scale + dispMin;
 		}
 	}
 
 	public float getDispMax()
 	{
 		return dispMin+dispLen/scale;
 	}
 
     /**
      * SERC: this comes from the SpecialFunction class
      */
 	final static float log10(float x) {
 		int radix = 0;
 
 		if( x <= (float)0.0 ) return 0;
 	 
 
 		while(x < (float)1.0) {
 			x *= 10f;
 			radix--;
 		}
 
 		while(x >= (float)10.0) {
 			x /= 10f;
 			radix++;
 		}
 
 		return radix;
 	}
 
     final static float floor(float x) {
 		if(x >= 0)
 			return (float)((int)x);
 		else 
 			return (float)((int)(x- (float)1.0));
     }
 
     final static float exp10(float val, int exp)
     {
 		int mult = 1;
 		int i;
 
 		if(exp >= 0){
 			for(i = 0; i < exp; i++) mult *= 10;
 			return val * (float)mult;
 		} else {
 			for(i = 0; i > exp; i--)  mult *= 10;
 			return val / (float)mult;
 		}
     }
 
     /*
      * This should only be called when the scale changes or the 
      * Idealy it should return the same size for the same scale
      *  (given the same tic spacing)
      */
     int labelExp = 0;
 	int labelExpStep = 3;
 	float labelExpStepVal = 1000f;
     float labelTicStep = 1;
 	int labelTicStepExp = 0;
 	int labelTicStepRawExp = 0;
 
     void setStepSize()
     {
 		float rawStep = axisDir * (float)minMajTicSpacing / scale;
 		int stepSign = 1;
 		int exponent;
 		int i;
 
 		if(rawStep < (float)0){
 			stepSign = -1;
 			rawStep = -rawStep;
 		}
 
 		exponent = (int)log10(rawStep);
 
 		rawStep = exp10(rawStep, -exponent);
 		// rawStep should now be in the range of 1 -> 10
 		// seems we can do a trick here to cut out the last special case
 		// but I don't think it will buy us much speed
 
 		if(rawStep < 2) labelTicStep = (float)2;
 		else if(rawStep < 5) labelTicStep = (float)5;
 		else if(rawStep < 10){
 			labelTicStep = (float)1;
 			exponent++;
 		}
 
 		labelTicStepRawExp = exponent;
 
 		majTicStep = exp10(labelTicStep, exponent);
 		if(exponent > 0) labelTicStepExp = exponent / labelExpStep * labelExpStep;
 		else labelTicStepExp = (((exponent + 1) / labelExpStep) - 1) * labelExpStep;
 		labelExp = labelTicStepExp;
 
 		setFirstTic();
     }
 
 	int getRoundExp(float val)
 	{
 		float x = val;
 		if(x < 0) x = -x;
 		if(x == 0f) return 0;
 
 		int radix = 0;
 
 		while(x < (float)1.0) {
 			x *= labelExpStepVal;
 			radix--;
 		}
 		
 		while(x >= labelExpStepVal) {
 			x /= labelExpStepVal;
 			radix++;
 		}
 
 		return radix*labelExpStep;
 	}
 
     float firstTic;
     float firstLabelTic;
 
     void setFirstTic()
     {
 		float rawMinSteps = min / majTicStep;
 		int intFloor = (int)rawMinSteps;
 
 		if(rawMinSteps < (float)0){
 			firstTic = (float)(intFloor - 1) * majTicStep;
 		} else {
 			firstTic = (float)intFloor * majTicStep;
 		}
 
 		int firstTicExp = getRoundExp(firstTic);
 		int approxLastTicExp = getRoundExp(length/scale + firstTic);
 
 		int maxEndExp = firstTicExp;
 		if(maxEndExp < approxLastTicExp) maxEndExp = approxLastTicExp;
 
 		if(maxEndExp > labelTicStepExp){
 			labelExp = maxEndExp;
 		} else {
 			labelExp = labelTicStepExp;
 		}
 		labelTicStep = exp10(majTicStep, -labelExp);
 		firstLabelTic = exp10(firstTic, -labelExp);
 		if(labelExp > labelTicStepRawExp){
 			int decDigits = labelExp - labelTicStepRawExp;
 			if(decDigits > (maxDigits - 1)) decDigits = maxDigits - 1;
 			label.minDigits = label.maxDigits = decDigits;
 		} else {
 			label.minDigits = 0;
 			label.maxDigits = 0;
 		}
 		if(majTicLabels != null){
 			for(int i=0; i<majTicLabels.length; i++){
 				if(majTicLabels[i] != null){
 					majTicLabels[i].minDigits = label.minDigits;
 					majTicLabels[i].maxDigits = label.maxDigits;
 				}
 			}
 		}
 
     }
 	
     int maxLabelOff = 0;
     int minLabelOff = 0;
     float ticStep = (float)1;
     int [] ticOffsets = null;
     TextLine [] majTicLabels = null;
 	int numTics = 0;
 
     int maxLabelSize;
 
     void computeTicArrays()
     {
 		int i;
 
 		ticStep = majTicStep / (float)(numMinTics + 1);
 		float lTicStep = labelTicStep / (float) (numMinTics + 1);
 
 		float range = length / scale;
 
 		if(range < 0){
 			range = (float)0;
 		}
 
 		int maxNumTics = (int)(range / ticStep) + 4;
 
 		// To be super efficient we should not reallocate
 		// if we don't have to
 		if(majTicLabels == null || ticOffsets == null || 
 		   ticOffsets.length < maxNumTics){
 			ticOffsets = new int[maxNumTics];
 			if(majTicLabels != null){
 				TextLine [] newLabels = new TextLine[maxNumTics];
 				Vm.copyArray(majTicLabels, 0, newLabels, 0, majTicLabels.length);
 				for(i=majTicLabels.length; i<newLabels.length; i++){
 					newLabels[i] = null;
 				}
 				majTicLabels = newLabels;
 			} else {
 				majTicLabels = new TextLine[maxNumTics];
 			}
 		}
 		numTics = maxNumTics;
 
 		float curPos = firstTic;
 		float curLabelVal = firstLabelTic;
 		float max = min + range;
 		i = 0;
 
 		TextLine curLabel;
 		int newLabelSize, newLabelMinOff, newLabelMaxOff;
 
 		if(orient == X_SCREEN_AXIS){
 			maxLabelSize = 0;
 			while(i < numTics){
 				ticOffsets[i] = (int)((curPos - min) * scale);
 				if(i % (numMinTics + 1) == 0){
 					// Its a major tic
 					// make TextLine
 					if(majTicLabels[i] == null){
 						majTicLabels[i] = curLabel = 
 							new TextLine();
 						curLabel.minDigits = label.minDigits;
 						curLabel.maxDigits = label.maxDigits;
 						curLabel.setText(curLabelVal);
 					} else {
 						curLabel = majTicLabels[i];
 						curLabel.setText(curLabelVal);
 					}
 					newLabelSize = curLabel.height;
 					newLabelMinOff = curLabel.getXOffset(labelEdge);
 					newLabelMaxOff = newLabelMinOff + newLabelSize;
 					if(newLabelMaxOff > maxLabelOff)
 						maxLabelOff = newLabelMaxOff;
 					if(newLabelMinOff < minLabelOff)
 						minLabelOff = newLabelMinOff;
 				} else {
 					if(majTicLabels[i] != null){
 						// push the label up one space
 						// so it can be used later
 						if(i+1 < majTicLabels.length &&
 						   majTicLabels[i+1] == null){
 							majTicLabels[i+1] = majTicLabels[i];
 						} else {
 							majTicLabels[i].free();
 						}
 						majTicLabels[i] = null;
 					}
 				}
 				i++;
 				curLabelVal += lTicStep;
 				curPos += ticStep;		
 			}
 		} else {
 			maxLabelSize = 0;
 			while(i < numTics){
 				ticOffsets[i] = (int)((curPos - min) * scale);
 				if(i % (numMinTics + 1) == 0){
 					// Its a major tic
 					// make TextLine
 					if(majTicLabels[i] == null){
 						majTicLabels[i] = curLabel = 
 							new TextLine();
 						curLabel.minDigits = label.minDigits;
 						curLabel.maxDigits = label.maxDigits;
 						curLabel.setText(curLabelVal);
 					} else {
 						curLabel = majTicLabels[i];
 						curLabel.setText(curLabelVal);
 					}
 					newLabelSize = curLabel.width;
 					newLabelMinOff = curLabel.getYOffset(labelEdge);
 					newLabelMaxOff = newLabelMinOff + newLabelSize;
 					if(newLabelMaxOff > maxLabelOff)
 						maxLabelOff = newLabelMaxOff;
 					if(newLabelMinOff < minLabelOff)
 						minLabelOff = newLabelMinOff;
 
 				} else {
 					if(majTicLabels[i] != null){
 						// push the label up one space
 						// so it can be used later
 						if(i+1 < majTicLabels.length &&
 						   majTicLabels[i+1] == null){
 							majTicLabels[i+1] = majTicLabels[i];
 						} else {
 							majTicLabels[i].free();
 						}
 						majTicLabels[i] = null;
 					}
 				}
 				i++;
 				curPos += ticStep;
 				curLabelVal += lTicStep;
 			}
 		}
     }
 
 	public void setScale(float s)
 	{
 		setScale(s, false);
 	}
 
     // This holds the dispMin fixed and changes the scale around that
     public void setScale(float newScale, boolean eScale)
     {
 		if(!estimateScale && eScale) oldScale = scale;
 		estimateScale = eScale;
 
 		scale = newScale;
 		axisDir = 1;
 		if(scale < (float)0) axisDir = -1;
 		setDispOffset(dispMin, 0);
 		int ticSpacing = (int)(axisDir*majTicStep*scale);
 		if(!eScale || (ticSpacing < minMajTicSpacing/2) || (ticSpacing > minMajTicSpacing*4)){
 			setStepSize();
 		}
 		needCalcTics = true;
 		notifyListeners(SCALE_CHANGE);
     }
 
 	public void setCacheAbs(int cacheOffset, int cacheLen)
 	{
 		min = cacheOffset/scale;
 		length = cacheLen;
 		if(drawnX != -1){
 			dispOffset = (int)((dispMin - min) * scale);
 		}
 		setFirstTic();
 		needCalcTics = true;							
 	}
 
 	public void setCache(int cacheBefore, int cacheAfter)
 	{
 		min = dispMin - cacheBefore/scale;
 		length = dispLen + cacheAfter + cacheBefore;
 		dispOffset = (int)((dispMin - min) * scale);
 		setFirstTic();
 		needCalcTics = true;					
 	}
 
     public void setDispOffset(float startMin, int newDO, boolean cache)
     {
 		dispMin = startMin + (float)newDO / scale;
 		dispOffset = (int)((dispMin - min) * scale);
 	
 		if(cache ||
 		   (axisDir*(dispOffset + dispLen) > axisDir*length) ||
 		   (dispMin < min)){
 			setCache(dispLen, dispLen);
 		} 
 		
 		notifyListeners(ORIGIN_CHANGE);
     }
 
     public void setDispOffset(float startMin, int newDO)
     {
 		setDispOffset(startMin, newDO, true);
 	}
     public void setDispMin(float newDM)
     {
 		setDispOffset(newDM, 0, true);
     }
 
 	public void init(int x, int y)
 	{
 		drawnX = x;
 		drawnY = y;
 		if(readExternalFlag){
 			setRange(readDispMax - dispMin);
 		}		
 	}
 
     boolean needCalcTics = false;
 
     LineGraph lGraph = null;
 
     // have the two arrays
     // tic offsets and tic labels
     // also have first dispTic
     public void draw(Graphics g, int x, int y)
     {
 		drawnX = x;
 		drawnY = y;
 
 		if(lGraph != null){
 			lGraph.endTime = Vm.getTimeStamp();
 			g.drawText(lGraph.endTime - lGraph.startTime + "", lGraph.xText, lGraph.yText);
 			lGraph.startTime = lGraph.endTime;
 			lGraph.xText += 20;
 		}
 
 		if(axisDir*dispLen*11 < axisDir*length){
 			// we are tracking way too many tics here.
 			length = 3*dispLen;
 			min = dispMin - (dispLen/scale);
 			if(nonNegative && min < (float)0){
 				min = (float)0;
 			}
 			setDispOffset(dispMin, 0);
 			setFirstTic();
 			needCalcTics = true;
 		}
 	
 		if(needCalcTics){
 			computeTicArrays();
 			needCalcTics = false;
 		}
 
 		// Find first valid tic
 		int i = 0;
 		while((i < numTics) && 
 			  (ticOffsets[i]*axisDir < dispOffset*axisDir))
 			i++;
 
 
 
 		int curPos;
 		int firstIndex = i;
 
 		int lastIndex;
 		int endPos = dispOffset + dispLen - axisDir;
 		int majTicEndOff = ticDir * majTicSize;
 		int minTicEndOff = ticDir * minTicSize;
 		int j;
 	
 		if(orient == X_SCREEN_AXIS){
 			drawnOffset = x - dispOffset;
 	    
 			g.translate(x, y);
 
 			// draw axis line
 			g.setColor(axisCol[0],axisCol[1],axisCol[2]);
 			g.drawLine(0,0, dispLen + axisDir, 0);
 			for(j=0; j <= majTicSize; j++){
 				g.drawLine(dispLen + axisDir - axisDir*j,0,
 						   dispLen + axisDir - axisDir*j, majTicEndOff - ticDir);
 			}
 
 			g.translate(axisDir - dispOffset, gridDir);
 		       
 
 
 			// draw tic marks and labels
 			while((i < numTics) &&
 				  ((curPos = ticOffsets[i])*axisDir <= endPos*axisDir)){
 				if(majTicLabels[i] == null){
 					g.drawLine(curPos, ticDir, curPos, minTicEndOff);
 				} else {
 					g.drawLine(curPos, ticDir, curPos, majTicEndOff);
 					majTicLabels[i].drawCenter(g, curPos, labelOff, labelEdge);
 				}
 				i++;
 			}
 
 			lastIndex = i;
 
 			// draw Minor GridLines
 			g.setColor(gridMinCol[0],gridMinCol[1],gridMinCol[2]);	    
 			for(i=firstIndex; i< lastIndex; i++){
 				curPos = ticOffsets[i];
 				if(majTicLabels[i] == null)
 					g.drawLine(curPos, 0, curPos, gridEndOff);
 			}
 
 			// draw Major GridLines
 			g.setColor(gridMajCol[0],gridMajCol[1],gridMajCol[2]);
 			for(i=firstIndex; i< lastIndex; i++){
 				curPos = ticOffsets[i];
 				if(majTicLabels[i] != null){
 					g.drawLine(curPos, 0, curPos, gridEndOff);
 				}
 			}
 
 
 			g.translate(-(x + axisDir - dispOffset), -(y + gridDir));
 		} else {
 			if(lGraph != null){
 				lGraph.endTime = Vm.getTimeStamp();
 				g.drawText(lGraph.endTime - lGraph.startTime + "", lGraph.xText, lGraph.yText);
 				lGraph.startTime = lGraph.endTime;
 				lGraph.xText += 20;
 			}
 
 			drawnOffset = y + axisDir - dispOffset;
 
 			g.translate(x, y);
 
 			// draw axis line
 			g.setColor(axisCol[0],axisCol[1],axisCol[2]);
 			g.drawLine(0,0, 0, dispLen + axisDir);
 			for(j=0; j<= majTicSize; j++){
 				g.drawLine(0, dispLen + axisDir - axisDir*j,
 						   majTicEndOff - ticDir, dispLen + axisDir - axisDir*j);
 			}
 
 			g.translate(gridDir, axisDir - dispOffset);
 
 			// draw tic marks and labels	    
 			while((i < numTics) &&
 				  ((curPos = ticOffsets[i])*axisDir <= endPos*axisDir)){
 				if(majTicLabels[i] == null){
 					g.drawLine(ticDir, curPos, minTicEndOff, curPos);
 				} else {
 					g.drawLine(ticDir, curPos, majTicEndOff, curPos);
 					majTicLabels[i].drawCenter(g, labelOff, curPos, labelEdge);
 				}
 				i++;
 			}
 
 			lastIndex = i;
 
 			if(lGraph != null){
 				g.translate(-(x + gridDir), -(y + axisDir - dispOffset));
 				lGraph.endTime = Vm.getTimeStamp();
 				g.drawText(lGraph.endTime - lGraph.startTime + "", lGraph.xText, lGraph.yText);
 				lGraph.startTime = lGraph.endTime;
 				lGraph.xText += 20;
 				g.translate((x + gridDir), (y + axisDir - dispOffset));
 			}
 
 			// draw Minor GridLines
 			g.setColor(gridMinCol[0],gridMinCol[1],gridMinCol[2]);	    
 			for(i=firstIndex; i< lastIndex; i++){
 				curPos = ticOffsets[i];
 				if(majTicLabels[i] == null)
 					g.drawLine(0, curPos, gridEndOff, curPos);
 			}
 
 			// draw Major GridLines
 			g.setColor(gridMajCol[0],gridMajCol[1],gridMajCol[2]);	    
 			for(i=firstIndex; i< lastIndex; i++){
 				curPos = ticOffsets[i];
 				if(majTicLabels[i] != null)
 					g.drawLine(0, curPos, gridEndOff, curPos);
 			}
 
 
 			g.translate(-(x + gridDir), -(y + axisDir - dispOffset));
 		}	    
 
     }
 
     // Need to have functions to calc width without drawing
     // Do we need a g for this??
     int getOutsideSize()
     {
 		int minOffset, maxOffset;
 
 		// Check if the real size changed
 		/*
 		  if(minLast != min){
 		  minLast = min;
 		  dispMin = min;
 		  setFirstTic();
 		  needCalcTics = true;
 		  }
 		*/	
 
 		if(needCalcTics){
 			computeTicArrays();
 			needCalcTics = false;
 		}
 
 		/* 
 		 * find max or min of all graph 
 		 * element offsets.
 		 * Elements:
 		 *   labels, ticMarks
 		 * The big problem is labels
 		 *  the edge is set and the offset is set
 		 *  so we'd have to switch on each edge to figure out
 		 *  the relevent offset.
 		 *
 		 * This should incorporate tic marks if the label happens
 		 *   to be on the inside
 		 */
 		if(gridEndOff > 0){
 			// find min
 			return labelOff + minLabelOff;
 		} else {
 			// find max
 			return labelOff + maxLabelOff;	    
 		}
     }
 
 	public boolean readExternalFlag = false;
 
 	public void readExternal(DataStream ds)
 	{
 		readExternalFlag = true;
 		dispMin = ds.readFloat();
 		readDispMax = ds.readFloat();
 		String labelStr;
 		if(ds.readBoolean()){
 			labelStr = ds.readString();
 		} else {
 			labelStr = null;
 		}
 		int labelUnitCode = ds.readInt();
 		CCUnit labelUnit = null;
 		if(labelUnitCode >= 0){
 			labelUnit = CCUnit.getUnit(labelUnitCode);
 		}
 		if(labelStr != null){
 			setAxisLabel(labelStr, labelUnit);
 		}
 	}
 
     public void writeExternal(DataStream ds)
     {
 		ds.writeFloat(dispMin);
 		if(drawnX == -1 && !readExternalFlag){
 			ds.writeFloat(getDispMax());
 		} else if(drawnX == -1 && readExternalFlag){
 			ds.writeFloat(readDispMax);
 		} else {
 			ds.writeFloat(getDispMax());
 		}
 		if(axisLabelStr == null){
 			ds.writeBoolean(false);
 		} else {
 			ds.writeBoolean(true);
 			ds.writeString(axisLabelStr);
 		}
 		if(axisLabelUnit == null) ds.writeInt(-1);
 		else ds.writeInt(axisLabelUnit.code);
     }
 
 }
