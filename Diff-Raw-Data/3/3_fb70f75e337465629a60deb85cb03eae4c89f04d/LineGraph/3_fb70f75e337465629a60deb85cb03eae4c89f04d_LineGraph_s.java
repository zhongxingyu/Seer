 package graph;
 
 import waba.ui.*;
 import waba.fx.*;
 import waba.io.*;
 import waba.sys.*;
 
 public class LineGraph implements Graph2D
 {
     boolean palm = false;
     public int topPadding = 10;
 
     int xOriginOff, yOriginOff;
     int dwWidth, dwHeight;
     public Axis xaxis = null;
     public Axis yaxis = null;
     int platform = 0;
 
     int numSets = 0;
 
     int width, height;
     float minValue = (float)500.0;
     float range = (float)-200.0;
 
     int [] lineColors = { 255, 0, 0,   // red
 			  0, 255, 0,   // green
 			  0, 0, 255,   // blue
 			  255, 255, 0, // yellow
 			  255, 0, 255, // purple
 			  0, 255, 255,}; // turquois
 
     protected int length = 0;
     int [][] points;
     float [][] values;
     int numPoints;
     int lastPlottedPoint;
 
     public LineGraph(int w, int h)
     {	
 	width = w;
 	height = h;
 
 	xaxis = new Axis((float)0, (float)100, Axis.BOTTOM);
 	xaxis.ticsInside = false;
 	xaxis.drawgrid = true;
 	
 	yaxis = new Axis(minValue, range + minValue, Axis.LEFT);
 	yaxis.maximum = range + minValue;
 	yaxis.minimum = minValue;
 	yaxis.ticsInside = false;
 	yaxis.drawgrid = true;
 
 	length = 0;
 
 	points = new int[100][];
 	values = new float[100][];
 	numPoints = 0;
 	lastPlottedPoint = 0;
     }
 
     public void resize(int w, int h){}
 
     int maxX = 0;
 
     // return the maximum x offset plotted
     public int plot(JGraphics g)
     {
 	int i,j;
 	int [] lastPoint = points[lastPlottedPoint];
 	if(numPoints == 0 || lastPoint == null)
 	    return 0;
 
 	int lastX = lastPoint[0];
 
 	for(i=lastPlottedPoint+1; i<numPoints; i++){
 	    int [] nextPoint = points[i];
 	    int nextX = nextPoint[0];
 	    for(j=1; j<numSets+1; j++){
 		g.setColor(lineColors[(j-1)*3], lineColors[(j-1)*3+1], lineColors[(j-1)*3+2]);
 		g.drawLine(lastX, lastPoint[j], nextX, nextPoint[j]);	    
 	    } 
 	    lastPoint = nextPoint;
 	    lastX = nextPoint[0];
 	}
 	lastPlottedPoint = numPoints-1;
 
 	return maxX;
     }
 
     public boolean removeBin(Object id)
     {
 	return true;
     }
 
 
     public void setRange(float min, float range)
     {
 	int i, j;
 	int [] curPoint;
 	float [] curValue;
 
 	this.range = range;
 	minValue = min;
 
 	yaxis.setRange(minValue, range + minValue);
 
	System.out.println("reseting points for " + numPoints + " points");
 	for(i=0; i<numPoints; i++){
 	    curPoint = points[i];
 	    curValue = values[i];
 	    curPoint[0] = (int)((curValue[0] - xaxis.minimum) * xaxis.xScale) + xaxis.xStart+1;
 
 	    for(j=0; j<numSets; j++){
 		curPoint[j+1] = (int)((curValue[j+1] - yaxis.minimum) * yaxis.yScale) + yaxis.yStart-1;
 	    }
 	}
 
     }  
 
     public int getNextBin()
     {
 	return 1;
     }
 
     // Translate a location from an old configuration to a new one
     public int transLocId(int confId, int locId)
     {
 	return locId;
     }
 
     // return the current configuration
     public Object addBin(int location, String label)
     {
 	// setup points, reset to the begining of the graph
 	numSets++;
 	lastPlottedPoint = 0;
 	numPoints = 0;
 
 	return label;
     }
 
     public void draw(JGraphics g, int x, int y)
     {
 	if(g != null){
 	    g.setColor(255,255,255);
 	    g.fillRect(x,y,width,height);
 	    
 	    g.setColor(0,0,0);
 
 	    // Calculate data window
 	    calcDataWin(g, width, height);
 	    
 	    // DrawAxis
 	    yaxis.setSize(-dwHeight, dwWidth);
 	    yaxis.draw(g,x+xOriginOff,y+yOriginOff-1);
 	    xaxis.setSize(dwWidth, dwHeight);
 	    xaxis.draw(g,x+xOriginOff+1,y+yOriginOff);
 	    
 	    lastPlottedPoint = 0;
 	    plot(g);
 	}
     }
 
     public void calcDataWin(JGraphics g, int w, int h)
     {	
 	// This should be a bit of an iteration
 	// attempting to arrive at the approx
 	int widthSpace = yaxis.getWidth(h);
 	int heightSpace = xaxis.getHeight(w);
 	widthSpace = yaxis.getWidth(h-heightSpace);
 	heightSpace = xaxis.getHeight(w-widthSpace);
 
 	dwWidth = w - widthSpace;
 	dwHeight = h - heightSpace - topPadding;
 
 	xOriginOff = widthSpace;
 	yOriginOff = h - heightSpace;
 
     }
 
 
     public void reset()
     {
 	length = 0;
 	numPoints = 0;
     }
 
     public boolean addPoint(int confId, int x, float newValues[])
     {
 	int i;
 
 	// should check the current config
 	if(numPoints == points.length){
 	    return true;
 	}
 	numPoints++;
 
 	if(points[0] == null ||
 	   points[0].length != numSets+1){
 	    for(i=0; i<100; i++){
 		points[i] = new int [numSets+1];
 		values[i] = new float [numSets+1];
 	    }
 	}
 
 
 	int [] curPoint = points[numPoints-1];
 	float [] curValue = values[numPoints-1];
 	int absoluteX = (int)((x - xaxis.minimum) * xaxis.xScale) ;
 	if(absoluteX > maxX){
 	    maxX = absoluteX;
 	}
 	curPoint[0] = absoluteX + xaxis.xStart+1;
 	curValue[0] = x;
 	for(i=0; i<newValues.length; i++){
 	    curPoint[i+1] = (int)((newValues[i] - yaxis.minimum) * yaxis.yScale) + yaxis.yStart-1;
 	    curValue[i+1] = newValues[i];
 	}
 	return true;
     }
 
     public boolean addPoint(int confId, int locId, int x, float value)
     {
 	// should check the current config
 	if(x <= lastPlottedPoint || x > numPoints || x >= points.length){
 	    // x is out of bounds
 	    return true;
 	}
 	
 	if(x == numPoints && x+1 <= points.length){
 	    numPoints++;
 	}
 
 	points[x][0] = (int)((x - xaxis.minimum) * xaxis.xScale) + xaxis.xStart+1;
 	points[x][locId] = (int)((value - yaxis.minimum) * yaxis.yScale) + yaxis.yStart-1;
 	values[x][0] = x;
 	values[x][locId] = value;
 
 	return true;
     }
 
 }
 
 
 
 
 
 
 
 
 
 
 
