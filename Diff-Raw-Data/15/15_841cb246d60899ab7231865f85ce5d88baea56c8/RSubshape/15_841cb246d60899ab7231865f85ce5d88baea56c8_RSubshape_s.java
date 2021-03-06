 /**
     Copyright 2004-2008 Ricard Marxer  <email@ricardmarxer.com>
 
     This file is part of Geomerative.
 
     Geomerative is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Geomerative is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
     
     You should have received a copy of the GNU General Public License
     along with Geomerative.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package geomerative ;
 import processing.core.*;
 
 /**
  * RSubshape is a reduced interface for creating, holding and drawing contours. Subshapes are ordered lists of commands (RCommand) which define the outlines of Shapes.  Subshapes can be self-intersecting.
  * @eexample RSubshape
  * @usage Geometry
  * @related RCommand
  * @related RPolygon
  */
 public class RSubshape extends RGeomElem
 {
   /**
    * @invisible
    */
   public int type = RGeomElem.SUBSHAPE;
   
   /**
    * Array of RCommand objects holding the commands of the subshape.
    * @eexample commands
    * @related RCommand
    * @related countCommands ( )
    */
   public RCommand[] commands;
   
   /**
    * Last point from where to add the next command.  Initialized to (0, 0).
    * @eexample lastPoint
    * @related RPoint
    * @invisible
    */
   public RPoint lastPoint;
 
   boolean closed = false;
   
   /**
    * Use this method to create a new subshape.
    * @eexample RSubshape
    * @param x float, the x coordinate of the first point of the subshape
    * @param y float, the y coordinate of the first point of the subshape
    * @param RSubshape s, the object from which to make the copy
    * @param RPoint p, the first point of the subshape
    */
   public RSubshape(){
     this.lastPoint = new RPoint(0,0);
   }
   
   public RSubshape(float x, float y){
     this.lastPoint = new RPoint(x,y);
   }
   
   public RSubshape(RPoint p){
     this.lastPoint = p;
   }
   
   public RSubshape(RSubshape s){
     int numCommands = s.countCommands();
     if(numCommands!=0){
       lastPoint = new RPoint(s.commands[0].startPoint);
       for(int i=0;i<numCommands;i++){
         this.append(new RCommand(s.commands[i], lastPoint));
         lastPoint = commands[i].endPoint;
       }
     }
     
     closed = s.closed;
     setStyle(s);
   }
   
   public RSubshape(RCommand c){
     this();
     this.addCommand(c);
   }
 
   /**
    * Use this method to count the number of commands in the contour. 
    * @eexample countCommands
    * @return int, the number commands in the contour
    */
   public int countCommands(){
     if(this.commands==null){
       return 0;
     }
     
     return this.commands.length;
   }
   
   /**
    * Use this to return the start, control and end points of the subshape.  It returns the points in the way of an array of RPoint.
    * @eexample getHandles
    * @return RPoint[], the start, control and end points returned in an array.
    * */
   public RPoint[] getHandles(){
     int numCommands = countCommands();
 
     RPoint[] result = null;
     RPoint[] newresult = null;
     for( int i = 0; i < numCommands ; i++ ){
       RPoint[] newPoints = commands[i].getHandles();
       if(newPoints != null){
         if(result == null){
           result = new RPoint[newPoints.length];
           System.arraycopy(newPoints,0,result,0,newPoints.length);
         }else{
           int overlap = 0;
           if(newPoints[0] == result[result.length-1]){
             overlap = 1;
           }
           newresult = new RPoint[result.length + newPoints.length - overlap];
           System.arraycopy(result,0,newresult,0,result.length);
           System.arraycopy(newPoints,overlap,newresult,result.length,newPoints.length - overlap);
           result = newresult;
         }
       }
     }
     return result;
   }
   
   /**
    * Use this to return the points on the curve.  It returns the points in the way of an array of RPoint.
    * @eexample getPoints
    * @return RPoint[], the vertices returned in an array.
    * */
   public RPoint[] getPoints(){
     int numCommands = countCommands();
     if(numCommands == 0){
       return null;
     }
     
     // Add the curve points of each command
     
     // First set the accumulated offset to the value of the inital offset
     RCommand.segmentAccOffset = RCommand.segmentOffset;
     RPoint[] result=null;
     RPoint[] newresult=null;
     for(int i=0;i<numCommands;i++){
       RPoint[] newPoints = commands[i].getPoints(false);
       if(newPoints!=null){
         if(result==null){
           result = new RPoint[newPoints.length];
           System.arraycopy(newPoints,0,result,0,newPoints.length);
         }else{
           // Check for overlapping
           // Overlapping happens when the last point of the last command 
           // is the same as the first point of the current command
           RPoint lastp = result[result.length-1];
           RPoint firstp = newPoints[0];
           int overlap = 0;
           if((lastp.x == firstp.x) && (lastp.y == firstp.y))
             {
               overlap = 1;
             }
           newresult = new RPoint[result.length + newPoints.length - overlap];
           System.arraycopy(result,0,newresult,0,result.length);
           System.arraycopy(newPoints,overlap,newresult,result.length,newPoints.length - overlap);
           result = newresult;
         }
       }
     }
     
     // Always add last point
     newresult = new RPoint[result.length + 1];
     System.arraycopy(result,0,newresult,0,result.length);
     newresult[newresult.length - 1] = new RPoint(commands[numCommands-1].endPoint);
     
     return newresult;
   }
   
   protected void calculateCurveLengths(){
     lenCurves = new float[countCommands()];
     lenCurve = 0F;
     for(int i=0;i<countCommands();i++){
       lenCurves[i] = commands[i].getCurveLength();
       lenCurve += lenCurves[i];
     }
   }
   
   /**
    * Use this to return the tangents on the curve.  It returns the vectors in the way of an array of RPoint.
    * @eexample getTangents
    * @param segments int, the number of segments in which to divide each command.
    * @return RPoint[], the tangent vectors returned in an array.
    * */
   public RPoint[] getTangents(){
     int numCommands = countCommands();
     if(numCommands == 0){
       return null;
     }
     
     RPoint[] result=null;
     RPoint[] newresult=null;
     for(int i=0;i<numCommands;i++){
       RPoint[] newPoints = commands[i].getTangents();
       if(newPoints!=null){
         if(newPoints.length!=1){
           int overlap = 1;
           if(result==null){
             result = new RPoint[newPoints.length];
             System.arraycopy(newPoints,0,result,0,newPoints.length);
           }else{
             newresult = new RPoint[result.length + newPoints.length - overlap];
             System.arraycopy(result,0,newresult,0,result.length);
             System.arraycopy(newPoints,overlap,newresult,result.length,newPoints.length - overlap);
             result = newresult;
           }
         }
       }
     }
     return result;
   }
   
   /**
    * Use this to return a specific point on the curve.  It returns the RPoint for a given advancement parameter t on the curve.
    * @eexample getPoint
    * @param t float, the parameter of advancement on the curve. t must have values between 0 and 1.
    * @return RPoint, the vertice returned.
    * */
   public RPoint getPoint(float t){
     int numCommands = countCommands();
     if(numCommands == 0){
       return new RPoint();
     }
     
     if(t==0.0F){ return commands[0].getPoint(0F); }
     if(t==1.0F){ return commands[numCommands-1].getPoint(1F); }
 
     float[] indAndAdv = indAndAdvAt(t);
     int indOfElement = (int)(indAndAdv[0]);
     float advOfElement = indAndAdv[1];
     
     return commands[indOfElement].getPoint(advOfElement);
   }
   
   /**
    * Use this to return a specific tangent on the curve.  It returns the RPoint tangent for a given advancement parameter t on the curve.
    * @eexample getPoint
    * @param t float, the parameter of advancement on the curve. t must have values between 0 and 1.
    * @return RPoint, the vertice returned.
    * */
   public RPoint getTangent(float t){
     int numCommands = countCommands();
     if(numCommands == 0){
       return new RPoint();
     }
     
     if(t==0.0F){ return commands[0].getTangent(0F); }
     if(t==1.0F){ return commands[numCommands-1].getTangent(1F); }
     
     float[] indAndAdv = indAndAdvAt(t);
     int indOfElement = (int)(indAndAdv[0]);
     float advOfElement = indAndAdv[1];    
     
     /* This takes the medium between two intersecting commands, sometimes this is not wanted
        if(advOfElement==1.0F){
        int indNextCommand = (indOfElement + 1) % numCommands;
        result = commands[indOfElement].getTangent(advOfElement);
        RPoint tngNext = commands[indNextCommand].getTangent(0.0F);
        result.add(tngNext);
        result.scale(0.5F);
        }else if (advOfElement==0.0F){
        int indPrevCommand = (indOfElement - 1 + numCommands) % numCommands;
        result = commands[indOfElement].getTangent(advOfElement);
        RPoint tngPrev = commands[indPrevCommand].getTangent(1.0F);
        result.add(tngPrev);
        result.scale(0.5F);
        }else{
        result = commands[indOfElement].getTangent(advOfElement);
        }
     */
     
     return commands[indOfElement].getTangent(advOfElement);
   }
 
   /**
    * Use this to insert a split point into the subshape.
    * @eexample insertHandle
    * @param t float, the parameter of advancement on the curve. t must have values between 0 and 1.
    * */
   public void insertHandle(float t){
     if((t == 0F) || (t == 1F)){
       return;
     }
 
     float[] indAndAdv = indAndAdvAt(t);
     int indOfElement = (int)(indAndAdv[0]);
     float advOfElement = indAndAdv[1];
     
     // Split the affected command and reconstruct each of the shapes
     RCommand[] splittedCommands = commands[indOfElement].split(advOfElement);
 
     if(splittedCommands[0] == null || splittedCommands[1] == null) {
       return;
     }
 
     // Extract the splitted command
     extract( indOfElement );
 
     // Insert the splittedCommands
     insert( splittedCommands[1], indOfElement );
     insert( splittedCommands[0], indOfElement );
 
     // Clear the cache
     lenCurves = null;
    lenCurve = 0F;
 
     return;
   }
 
   /**
    * Use this to insert a split point into each command of the subshape.
    * @eexample insertHandleAll
    * @param t float, the parameter of advancement on the curve. t must have values between 0 and 1.
    * */
   public void insertHandleAll(float t){
     if((t == 0F) || (t == 1F)){
       return;
     }
 
     int numCommands = countCommands();
     
     for ( int i = 0; i<numCommands*2; i+=2 ) {
       // Split the affected command and reconstruct each of the shapes
       RCommand[] splittedCommands = commands[i].split(t);
       
       if(splittedCommands[0] == null || splittedCommands[1] == null) {
         return;
       }
       
       // Extract the splitted command
       extract( i );
       
       // Insert the splittedCommands
       insert( splittedCommands[1], i );
       insert( splittedCommands[0], i );
     }
 
     // Clear the cache
     lenCurves = null;
    lenCurve = 0F;
     
     return;
   }
 
   /**
    * Use this to split a subshape into two separate new subshapes.
    * @eexample split
    * @param t float, the parameter of advancement on the curve. t must have values between 0 and 1.
    * @return RSubshape[], an array of two RSubshape.
    * */
   public RSubshape[] split(float t){
     RSubshape[] result = new RSubshape[2];
     
     int numCommands = countCommands();
     if(numCommands == 0){
       return null;
     }
     
     if(t==0.0F){ 
       result[0] = new RSubshape();
       result[1] = new RSubshape(this);
       result[0].setStyle(this);
       result[1].setStyle(this);
 
       return result;
     }
     
     if(t==1.0F){
       result[0] = new RSubshape(this);
       result[1] = new RSubshape();
       result[0].setStyle(this);
       result[1].setStyle(this);
 
       return result;
     }
     
     float[] indAndAdv = indAndAdvAt(t);
     int indOfElement = (int)(indAndAdv[0]);
     float advOfElement = indAndAdv[1];
     
     
     // Split the affected command and reconstruct each of the shapes
     RCommand[] splittedCommands = commands[indOfElement].split(advOfElement);
 
     result[0] = new RSubshape();
     for(int i = 0; i<indOfElement; i++){
       result[0].addCommand(new RCommand(commands[i]));
     }
     result[0].addCommand(new RCommand(splittedCommands[0]));
     result[0].setStyle(this);
     
     result[1] = new RSubshape();
     for(int i = indOfElement + 1; i < countCommands(); i++){
       result[1].addCommand(new RCommand(commands[i]));
     }
     result[1].addCommand(new RCommand(splittedCommands[1]));
     result[1].setStyle(this);
 
     return result;
   }
   
   /**
    * Use this method to draw the subshape. 
    * @eexample drawSubshape
    * @param g PGraphics, the graphics object on which to draw the subshape
    */
   public void draw(PGraphics g){
     int numCommands = countCommands();
     
     // By default always draw with an adaptative segmentator
     int lastSegmentator = RCommand.segmentType;
     RCommand.setSegmentator(RCommand.ADAPTATIVE);
     
     RPoint[] points = getPoints();
     
     if(points == null){
       return;
     }
 
     g.beginShape();
     for(int i=0;i<points.length;i++){
       g.vertex(points[i].x,points[i].y);
     }
     g.endShape(closed ? RGeomerative.parent().CLOSE : RGeomerative.parent().OPEN);
     
     // Restore the user set segmentator
     RCommand.setSegmentator(lastSegmentator);
   }
   
   public void draw(PApplet g){
     int numCommands = countCommands();
     
     // By default always draw with an adaptative segmentator
     int lastSegmentator = RCommand.segmentType;
     RCommand.setSegmentator(RCommand.ADAPTATIVE);
     
     RPoint[] points = getPoints();
     RCommand.setSegmentator(lastSegmentator);
     if(points == null){
       return;
     }
     g.beginShape();
     for(int i=0;i<points.length;i++){
       g.vertex(points[i].x,points[i].y);
     }
     g.endShape(closed ? RGeomerative.parent().CLOSE : RGeomerative.parent().OPEN);
     
     // Restore the user set segmentator
     RCommand.setSegmentator(lastSegmentator);
   }
   
   /**
    * Use this method to add new commands to the contour.
    * @eexample addCommand
    * @invisible
    */
   public void addCommand(RCommand p){
     this.append(p);
     
     lastPoint = commands[commands.length-1].endPoint;
   }
   
   /**
    * Use this method to add a new cubic bezier to the subshape. The first point of the bezier will be the last point added to the subshape.
    * @eexample addBezierTo
    * @param float cp1x, the x coordinate of the first control point
    * @param float cp1y, the y coordinate of the first control point
    * @param float cp2x, the x coordinate of the second control point
    * @param float cp2y, the y coordinate of the second control point
    * @param float endx, the x coordinate of the ending point
    * @param float endy, the y coordinate of the ending point
    */
   public void addBezierTo(RPoint cp1, RPoint cp2, RPoint end){
     this.addCommand(RCommand.createBezier4(lastPoint, cp1, cp2, end));
   }
   
   public void addBezierTo(float cp1x, float cp1y, float cp2x, float cp2y, float endx, float endy){
     RPoint cp1 = new RPoint(cp1x, cp1y);
     RPoint cp2 = new RPoint(cp2x, cp2y);
     RPoint end = new RPoint(endx, endy);
     
     addBezierTo(cp1,cp2,end);
   }
   
   /**
    * Use this method to add a new quadratic bezier to the subshape. The first point of the bezier will be the last point added to the subshape.
    * @eexample addQuadTo
    * @param float cp1x, the x coordinate of the first control point
    * @param float cp1y, the y coordinate of the first control point
    * @param float endx, the x coordinate of the ending point
    * @param float endy, the y coordinate of the ending point
    */
   public void addQuadTo(RPoint cp1, RPoint end){
     this.addCommand(RCommand.createBezier3(lastPoint, cp1, end));
   }
   
   public void addQuadTo(float cp1x, float cp1y, float endx, float endy){
     RPoint cp1 = new RPoint(cp1x, cp1y);
     RPoint end = new RPoint(endx, endy);
     
     addQuadTo(cp1,end);
   }
   
   /**
    * Use this method to add a new line to the subshape. The first point of the line will be the last point added to the subshape.
    * @eexample addLineTo
    * @param float endx, the x coordinate of the ending point
    * @param float endy, the y coordinate of the ending point
    */
   public void addLineTo(RPoint end){
     this.addCommand(RCommand.createLine(lastPoint, end));
   }
   
   public void addLineTo(float endx, float endy){
     RPoint end = new RPoint(endx, endy);
     addLineTo(end);
   }
   
   
   public void addClose(){
     if(commands == null){
       return;
     }
     
     if((commands[commands.length-1].endPoint.x == commands[0].startPoint.x) && (commands[commands.length-1].endPoint.y == commands[0].startPoint.y))
       {
         commands[commands.length-1].endPoint = new RPoint(commands[0].startPoint.x, commands[0].startPoint.y);
       }else{
       addLineTo(new RPoint(commands[0].startPoint.x,commands[0].startPoint.y));
     }
     
     lastPoint = commands[commands.length-1].endPoint;
 
     closed = true;
   }
   
   /**
    * @invisible
    */
   public RPolygon toPolygon(){
     return this.toShape().toPolygon();
   }
   
   /**
    * @invisible
    */
   public RShape toShape(){
     return new RShape(this);
   }
   
   /**
    * @invisible
    */
   public RMesh toMesh(){
     return this.toPolygon().toMesh();
   }
   
   /**
    * Use this method to get the type of element this is.
    * @eexample RPolygon_getType
    * @return int, will allways return RGeomElem.POLYGON
    */
   public int getType(){
     return type;
   }
   
   
   public void print(){
     for(int i=0;i<countCommands();i++){
       String commandType = "";
       switch(commands[i].commandType)
         {
         case RCommand.LINETO:
           commandType = "LINETO";
           break;
           
         case RCommand.CUBICBEZIERTO:
           commandType = "BEZIERTO";
           break;
           
         case RCommand.QUADBEZIERTO:
           commandType = "QUADBEZIERTO";
           break;
         }
       
       System.out.println("cmd type: " + commandType);
       System.out.print("start point: ");
       commands[i].startPoint.print();
       System.out.print("\n");
       System.out.print("end point: ");
       commands[i].endPoint.print();
       System.out.print("\n");
       if(commands[i].controlPoints != null)
         {
           System.out.println("control points: ");
           for(int j=0;j<commands[i].controlPoints.length;j++){
             commands[i].controlPoints[j].print();
             System.out.print(" ");
             System.out.print("\n");
           }
         }
       System.out.print("\n");
     }
   }
   
   /**
    * Use this method to transform the shape. 
    * @eexample RSubshape_transform
    * @param m RMatrix, the matrix defining the affine transformation
    * @related draw ( )
    */
   // OPT: not transform the EndPoint since it's equal to the next StartPoint
   /*
     public void transform(RMatrix m){
     RPoint[] ps = getHandles();
     if(ps!=null){
     for(int i=0;i<ps.length;i++){
     ps[i].transform(m);
     }
     }
     
     int numCommands = countCommands();
     if(numCommands!=0){
     commands[0].startPoint.transform(m);
     for(int i=0;i<numCommands-1;i++){
     for(int j=0;j<commands[i].countControlPoints();j++){
     commands[i].controlPoints[j].transform(m);
     }
     commands[i].endPoint.transform(m);
     }
     }
     
     }
   */
   
   private float[] indAndAdvAt(float t){
     int indOfElement = 0;
     float[] lengthsCurves = getCurveLengths();
     float lengthCurve = getCurveLength();
 
     /* Calculate the amount of advancement t mapped to each command */
     /* We use a simple algorithm where we give to each command the same amount of advancement */
     /* A more useful way would be to give to each command an advancement proportional to the length of the command */
     /* Old method with uniform advancement per command
        float advPerCommand;
        advPerCommand = 1F / numSubshapes;
        indOfElement = (int)(Math.floor(t / advPerCommand)) % numSubshapes;
        advOfElement = (t*numSubshapes - indOfElement);
     */
     
     float accumulatedAdvancement = lengthsCurves[indOfElement] / lengthCurve;
     float prevAccumulatedAdvancement = 0F;
     
     /* Find in what command the advancement point is  */
     while(t > accumulatedAdvancement){
       indOfElement++;
       prevAccumulatedAdvancement = accumulatedAdvancement;
       accumulatedAdvancement += (lengthsCurves[indOfElement] / lengthCurve);
     }
     
     float advOfElement = (t-prevAccumulatedAdvancement) / (lengthsCurves[indOfElement] / lengthCurve);
 
     float[] indAndAdv = new float[2];
 
     indAndAdv[0] = indOfElement;
     indAndAdv[1] = advOfElement;
     
     return indAndAdv;
   }
 
   private void append(RCommand nextcommand)
   {
     RCommand[] newcommands;
     if(commands==null){
       newcommands = new RCommand[1];
       newcommands[0] = nextcommand;
     }else{
       newcommands = new RCommand[this.commands.length+1];
       System.arraycopy(this.commands,0,newcommands,0,this.commands.length);
       newcommands[this.commands.length]=nextcommand;
     }
     this.commands=newcommands; 
   }
 
   private void insert(RCommand newcommand, int i) throws RuntimeException{
     if( i < 0 ){
       throw new RuntimeException("Negative values for indexes are not valid.");
     }
 
     RCommand[] newcommands;
     if( commands == null ){
       newcommands = new RCommand[1];
       newcommands[0] = newcommand;
     }else{
       if( i > commands.length ){
         throw new RuntimeException("Index out of the bounds.  You are trying to insert an element with an index higher than the number of commands in the group.");        
       }
 
       newcommands = new RCommand[this.commands.length + 1];
       System.arraycopy( this.commands , 0 , newcommands , 0 , i );
       newcommands[i] = newcommand;
       System.arraycopy( this.commands , i , newcommands , i + 1 , this.commands.length - i);
     }
     this.commands = newcommands;    
   }
 
   private void extract(int i) throws RuntimeException{
     RCommand[] newcommands;
     if(commands==null){
       throw new RuntimeException("The group is empty. No commands to remove.");
     }else{
       if(i<0){
         throw new RuntimeException("Negative values for indexes are not valid.");
       }
       if(i>commands.length-1){
         throw new RuntimeException("Index out of the bounds of the group.  You are trying to erase an element with an index higher than the number of commands in the group.");
       }
       if(commands.length==1){
         newcommands = null;
       }else if(i==0){
         newcommands = new RCommand[this.commands.length-1];
         System.arraycopy(this.commands,1,newcommands,0,this.commands.length-1);
       }else if(i==commands.length-1){
         newcommands = new RCommand[this.commands.length-1];
         System.arraycopy(this.commands,0,newcommands,0,this.commands.length-1);
       }else{
         newcommands = new RCommand[this.commands.length-1];
         System.arraycopy(this.commands,0,newcommands,0,i);
         System.arraycopy(this.commands,i+1,newcommands,i,this.commands.length-i-1);
       }
     }
     this.commands=newcommands;
   }
 }
