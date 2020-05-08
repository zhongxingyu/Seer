 package net.brwyatt.badscience;
 
 public class LevelGrid {
 	private final int vanishingY=1000;//Constant as changing this would break the horizontal line spacing (for now).
	private final int squareWidth=100;//Constant for now. This might be something that can be changed if the viewport size changes
 	
 	private int viewWidth;
 	private int viewHeight;
 	
 	public LevelGrid(int viewWidth,int viewHeight){
 		this.viewWidth=viewWidth;
 		this.viewHeight=viewHeight;
 	}
 	public void calculateGrid(){
 		//Find all intersections
 		System.out.println("*******************");
		int centerX=viewWidth/2;
 		for(int offset=50;offset<=650;offset+=100){
 			//(400,-1000)
 			
 			// y = (offset/(vanishingY-height))*x + (vanishingY-((offset/(vanishingY-height))*vanishingX)
			double leftM=((vanishingY-viewHeight)/((double)offset));
			double leftB=(vanishingY-(leftM*centerX));
 			System.out.println("LEFT: y=("+leftM+")*x+("+leftB+")");
 			
 			// y = ((-offset)/(vanishingY-height))*x + (vanishingY-(((-offset)/(vanishingY-height))*vanishingX)
			double rightM=((vanishingY-viewHeight)/(-((double)offset)));
			double rightB=(vanishingY-(rightM*centerX));
 			System.out.println("RIGHT: y=("+rightM+")*x+("+rightB+")");
 			
 
 			int yPos=600;
 			for(int d=-1;yPos>=0;d++){
 				if(d==0){
					yPos=viewHeight;
 				}else{
					yPos=(int) Math.round((viewHeight-(squareWidth*(48/((Math.sqrt(1872)/d)+3)))));
 				}
 				int leftX = (int)Math.round((yPos-leftB)/leftM);
 				System.out.println("LEFT: ("+leftX+","+yPos+")");
				//g.fillRect(leftX-1, yPos-1, 3, 3);
 				int rightX = (int)Math.round((yPos-rightB)/rightM);
 				System.out.println("RIGHT: ("+rightX+","+yPos+")");
				//g.fillRect(rightX-1, yPos-1, 3, 3);
 			}
 		}
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
 }
