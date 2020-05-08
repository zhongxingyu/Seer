 package net.brwyatt.badscience.levelgrid;
 
 import java.util.ArrayList;
 
 public class LevelGrid {
 	private final int vanishingY=-1000;//Constant as changing this would break the horizontal line spacing (for now).
 	private final int squareWidth=100;//Constant for now. This might be something that can be changed if the viewport size changes
 	
 	private int viewWidth;
 	private int viewHeight;
 	private ArrayList<ArrayList<LevelGridSquare>> gridSquares;
 	
 	public LevelGrid(int viewWidth,int viewHeight){
 		this.viewWidth=viewWidth;
 		this.viewHeight=viewHeight;
 		gridSquares=new ArrayList<ArrayList<LevelGridSquare>>();
 		calculateGrid();
 	}
 	public void calculateGrid(){
 		int verticalCount=0;
 		int horizontalCount=0;
 		ArrayList<Integer> yValues=new ArrayList<Integer>();
 		
 		int prevYPos=0;
 		int yPos=0;
 		//loop over all the horizontal values (only need to calculate these once
 		for(int d=-1;prevYPos>=0;d++){
 			prevYPos=yPos;
 			if(d==0){
 				yPos=viewHeight;
 			}else{
 				yPos=(int) Math.round((viewHeight-(squareWidth*(48/((Math.sqrt(1872)/d)+3)))));
 			}
 			yValues.add(0,yPos);
 			horizontalCount++;
 			
 			//int leftX = (int)Math.round((yPos-leftB)/leftM);
 			//System.out.println("LEFT: ("+leftX+","+yPos+")");
 			
 			//int rightX = (int)Math.round((yPos-rightB)/rightM);
 			//System.out.println("RIGHT: ("+rightX+","+yPos+")");
 		}
 		
 		int centerX=viewWidth/2;
 		//Find all grid points
 		//Loop over vertical lines
 		for(int offset=squareWidth/2;offset<=750;offset+=squareWidth){//Test should be changed to determine if we have have found the second vertical that isn't visible
 			double leftM=((vanishingY-viewHeight)/((double)offset));
 			double leftB=(vanishingY-(leftM*centerX));
 			System.out.println("LEFT: y=("+leftM+")*x+("+leftB+")");
 			verticalCount++;
 			
 			double rightM=((vanishingY-viewHeight)/(-((double)offset)));
 			double rightB=(vanishingY-(rightM*centerX));
 			System.out.println("RIGHT: y=("+rightM+")*x+("+rightB+")");
 			verticalCount++;
 			
 			if(gridSquares.size()==0){//this is our first loop
 				ArrayList<LevelGridSquare> col=new ArrayList<LevelGridSquare>();
 				for(int i=0;i<yValues.size()-1;i++){
 					int top=yValues.get(i);
 					int bottom=yValues.get(i+1);
 					col.add(new LevelGridSquare(
 							new LevelGridPoint((int)Math.round((top-leftB)/leftM),top),
 							new LevelGridPoint((int)Math.round((top-rightB)/rightM),top),
 							new LevelGridPoint((int)Math.round((bottom-leftB)/leftM),bottom),
 							new LevelGridPoint((int)Math.round((bottom-rightB)/rightM),bottom)
 							));
 					if(i>0){
						col.get(i).setBelow(col.get(i+1));
						col.get(i+1).setAbove(col.get(i));
 					}
 				}
 				gridSquares.add(col);
 			}else{//all others
 				ArrayList<LevelGridSquare> leftCol=new ArrayList<LevelGridSquare>();
 				ArrayList<LevelGridSquare> lastLeftCol=gridSquares.get(0);
 				ArrayList<LevelGridSquare> rightCol=new ArrayList<LevelGridSquare>();
 				ArrayList<LevelGridSquare> lastRightCol=gridSquares.get(gridSquares.size()-1);
 				for(int i=0;i<yValues.size()-1;i++){
 					int top=yValues.get(i);
 					int bottom=yValues.get(i+1);
 					LevelGridSquare lastLeftSquare=lastLeftCol.get(i);
 					LevelGridSquare lastRightSquare=lastRightCol.get(i);
 					
 					LevelGridSquare newLeftSquare=new LevelGridSquare(
 							new LevelGridPoint((int)Math.round((top-leftB)/leftM),top),
 							lastLeftSquare.getTopLeft(),
 							new LevelGridPoint((int)Math.round((bottom-leftB)/leftM),bottom),
 							lastLeftSquare.getBottomLeft()
 							);
 					LevelGridSquare newRightSquare=new LevelGridSquare(
 							lastRightSquare.getTopRight(),
 							new LevelGridPoint((int)Math.round((top-rightB)/rightM),top),
 							lastRightSquare.getBottomRight(),
 							new LevelGridPoint((int)Math.round((bottom-rightB)/rightM),bottom)
 							);
 					
 					leftCol.add(newLeftSquare);
 					rightCol.add(newRightSquare);
 					
 					newLeftSquare.setRight(lastLeftSquare);
 					lastLeftSquare.setLeft(newLeftSquare);
 					
 					newRightSquare.setLeft(lastRightSquare);
 					lastRightSquare.setRight(newRightSquare);
 					
 					if(i>0){
						leftCol.get(i).setBelow(leftCol.get(i+1));
						leftCol.get(i+1).setAbove(leftCol.get(i));
						rightCol.get(i).setBelow(rightCol.get(i+1));
						rightCol.get(i+1).setAbove(rightCol.get(i));
 					}
 				}
 				
 				gridSquares.add(0,leftCol);
 				gridSquares.add(rightCol);
 			}
 		}
 		
 		System.out.println("Vertical lines: "+verticalCount);
 		System.out.println("Horizontal lines: "+horizontalCount);
 		System.out.println("Grid: "+gridSquares.size()+"x"+gridSquares.get(0).size());
 	}
 	public int getViewWidth() {
 		return viewWidth;
 	}
 	public int getViewHeight() {
 		return viewHeight;
 	}
 	public void setViewWidth(int viewWidth) {
 		this.viewWidth = viewWidth;
 	}
 	public void setViewHeight(int viewHeight) {
 		this.viewHeight = viewHeight;
 	}
 	public int getGridWidth(){
 		return gridSquares.size();
 	}
 	public int getGridHeight(){
 		return gridSquares.get(0).size();
 	}
 	public LevelGridSquare getGridSquare(int x, int y){
 		return gridSquares.get(x).get(y);
 	}
 }
