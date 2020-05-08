 package gui;
 
 import imgUtil.ImgCommonUtil;
 import imgUtil.FourierProcessingUtil;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import mathUtil.Coordinate2D;
 import model.Orientation;
 import model.SrcImage;
 
 
 import controller.IMyController;
 
 public class SrcImgPanel extends JPanel implements MouseListener, KeyListener{
 	
 	private static final int UP_MARGIN = 0;
 
 	private static final int LEFT_MARGIN = 0;
 	
 	private static final int PANEL_WIDTH = 800;
 	
 	private static final int PANEL_HEIGHT = 800;
 	
 	private static final Color POINT_COLOR = Color.RED;
 	
 	private static final Color FORMER_COLOR = Color.RED;
 	
 	private static final Color LATTER_COLOR = Color.BLUE;
 
 //	private int width;
 //	
 //	private int height;
 	
	private static boolean renderToFile = false;
 	
	
	private static final int NEEDLE_LENGTH = 200;
 	
 	private static final int GRID_GAP = 16;
 	
 	private IMyView mainView;
 	
 //	private SrcImage srcImg;
 	
 	private int patchWidth;
 	
 	private int patchHeight;
 	
 	
 	
 	
 	private Coordinate2D formerCoord;
 	
 	private Coordinate2D latterCoord;
 	
 	private Coordinate2D centerCoord;
 	
 	
 	private boolean leftPicked;
 	
 	private boolean rightPicked;
 	
 	private boolean centerPicked;
 	
 	
 	
 	
 	
 	private static final int FORMER = 1;
 	
 	private static final int LATTER = 2;
 	
 	private static final int CENTER = 3;
 	
 	private int active;
 	
 	
 	private LinkedList<Orientation> estimatedOrientations;
 	
 	private LinkedList<Coordinate2D> estimatedPositions;
 	
 	private LinkedList<Color> needleColor;
 	
 	
 	
 	public SrcImgPanel(){
 		
		
 	}
 
 	
 	public void mousePressed(MouseEvent e) {
 		this.requestFocus();
 		
 		if(javax.swing.SwingUtilities.isLeftMouseButton(e)){
 			
 			formerCoord.setCoordinate(e.getX(), e.getY(), mainView.getSrcImgWidth(), mainView.getSrcImgHeight());
 			
 			leftPicked = true;
 			
 			active = FORMER;
 		}
 		else if(javax.swing.SwingUtilities.isRightMouseButton(e)){
 			
 			latterCoord.setCoordinate(e.getX(), e.getY(), mainView.getSrcImgWidth(), mainView.getSrcImgHeight());
 			
 			rightPicked = true;
 			
 			active = LATTER;
 		}
 		else if(javax.swing.SwingUtilities.isMiddleMouseButton(e)){
 			
 			centerCoord.setCoordinate(e.getX(), e.getY(), mainView.getSrcImgWidth(), mainView.getSrcImgHeight());
 			
 			System.out.println("Center: " + centerCoord.toString());
 			
 			centerPicked = true;
 			
 			active = CENTER;
 		}
 		
 		this.repaint();
 		
 	}
 	
 	
 	
 	public void keyPressed(KeyEvent e) {
 		
 		if(!leftPicked && !rightPicked && !centerPicked)
 			return;
 		
 		/*
 		if(e.getKeyChar() == 'f'){
 			
 			if(leftPicked){
 				System.out.println("Former:");
 				mainView.patchSelected(formerCoord);
 				
 			}if(rightPicked){
 				System.out.println("Latter:");
 				mainView.patchSelected(latterCoord);
 				
 			}
 			
 			
 		}
 		*/
 				
		
 		if(e.getKeyChar() == 'r'){ // estimate the orientation by any two patches selected
 		
 			if(leftPicked && rightPicked){
 			
 				estimateByFormerLatter();
 				
 				this.repaint();
 			}
 		}
 		
 		else if(e.getKeyChar() == 'd'){ // estimate the orientation by two pairs of diagonal patches around a center point
 			
 			if(centerPicked){
 			
 				estimateByCenterDiagonal();
 				
 				this.repaint();
 			}
 			
 			
 		}
 		else if(e.getKeyChar() == 'c'){ // estimate the orientation by two pairs of cross patches around a center point
 			
 			if(centerPicked){
 
 				estimateByCenterCross();
 				
 				this.repaint();
 			}
 			
 			
 		}
 		else if(e.getKeyChar() == 's'){ // estimate the orientation by four pairs of patches around a center point
 			
 			System.out.println("Simple -- Diagonal & Cross");
 			
 			if(centerPicked){
 				
 				
 				estimateSingle(centerCoord.getX(), centerCoord.getY());
 				
 				this.repaint();
 			}
 			
 		}
 		
 		
 		
 		else if(e.getKeyChar() == 'z'){ // estimate the orientation by four pairs of patches around 9 center points
 			
 			System.out.println("Zhankai");
 			if(centerPicked){
 
 				estimateGrid(GRID_GAP);
 				
 				
 			}
 			
 		}
 	
 		else if(e.getKeyChar() == 'p'){ // pre-processing of the target image
 			
 			
 			mainView.updateSrcImg(new SrcImage(FourierProcessingUtil.bandpassFiltering(
 					mainView.getSrcImg().getBufferedImg(), 0.1, 0.45)));
 			
 			this.repaint();
 		}
 		else if(e.getKeyChar() == 'n'){
 //			
 //			int[][] pos = {{-171, 95}, {-115, 84}, {-69, 77}, {-11 ,65}, {41, 53}, 
 //					{-166, 6}, {-106, 4}, {-48, 2}, {3, 0}, {61, -2},
 //					{-170, -72}, {-109, -65}, {-50, -59}, {-2, -55}, {59, -40}};
 //			
 //			int[][] angs = {{30, 43}, {42, 49}, {38, 65}, {50, 54}, {43, 59},
 //					{44, 2}, {50, -5}, {50, 4}, {65, 0}, {59, 3},
 //					{58, -37}, {43, -47}, {49, -50}, {45, -44}, {49, -36}};
 			
 			//floor
 //			int[][] pos = {{-138, -130}, {-9, -137}, {123, -139}, {-128, -59}, {-8, -66}, {121, -66}, {-135, 24}, {-6, 15}, {110, 7}};
 //			
 //			int[][] angs = {{55, 80}, {45, 93}, {49, 87}, {60, 90}, {46, 78}, {55, 88}, {42, 84}, {50, 90}, {60, 95}};
 			
 			//slope
 //			int[][] pos = {{-128, -97}, {-106, -17}, {-81, 42}, {2, 39}, {71, 37}, {8, -25}, {1, -105}, {108, -117}, {101, -26}, {82, 96}, {20, 101}};
 //			int [][] angs = {{80, 79}, {70, 82}, {67, 67}, {45, 54}, {45, 45}, {68, 65}, {70, 75}, {80, 69}, {67, 76}, {20, 40}, {30, 60}};
 			
 			//tower
 //			int[][] pos = {{-146, 422}, {-118, 382}, {-85, 338}, {-49, 289}, {-8, 235}, {40, 172}, {81, 113}, 
 //					 {-215, 415}, {-189, 373}, {-157, 334}, {-126, 282}, {-90, 225}, {-46, 162}, {-3, 95},
 //					 {-283, 388}, {-254, 340}, {-226, 291}, {-196, 240}, {-162, 180}, {-127, 119}, {-88, 51},
 //					 {-352, 354}, {-330, 311}, {-305, 266}, {-279, 213}, {-252, 159}, {-220, 95}, {-184, 25},
 //					 {-404, 307}, {-385, 263}, {-363,213}, {-330, 133}, {-305, 78}, {-287, 38}, {-258, -32}};
 //	
 //			double[][] angs = {{52, 78}, {41, 86}, {44, 71}, {48, 67}, {48, 72}, {41, 90}, {34, 67},
 //						  {45, 97}, {54, 98}, {78, 89}, {55, 87}, {25, 94}, {68, 95}, {73, 110},
 //						  {45, 107}, {54, 107}, {39, 115}, {33, 125}, {50, 118}, {43, 120}, {45, 115},
 //						  {57, 118}, {51, 129}, {37, 124}, {56, 121}, {55, 137}, {34, 135}, {36, 121},
 //						  {46, 139}, {53, 130}, {58, 129}, {49, 142}, {40, 134}, {58, 146}, {45, 140}};
 			
 			
 			//library
 //			int[][] pos = {{408, -128}, {420, -232}, {278, -76}, {290, -197}, {105, 16}, {113, -150}, {-73, 100}, {-72, -94}};
 //			
 //			double[][] angs = {{72, 12.5}, {73.5, 6.5}, {82, 7.25}, {74, 12}, {74, 3}, {68.5, 6.5}, {80, 1}, {77.7, 12.5}};
 			
 			
 			
 			
 //			for(int i = 0; i < pos.length; i++){
 //				
 //				estimatedOrientations.add(Orientation.orientationInDegree(angs[i][0], angs[i][1]));
 //				
 //				estimatedPositions.add(new Coordinate2D(pos[i][0] + 256 , 256 -pos[i][1]));
 //				
 //				needleColor.add(Color.MAGENTA);
 //			}
 			
 			
 			int[] pos = {31, 68};
 			double[] angEstimated = {42.46, 300.29};
 			double[] angTruth = {45, 300};
 			
 			estimatedOrientations.add(Orientation.orientationInDegree(angTruth[0], angTruth[1]));
 			estimatedPositions.add(new Coordinate2D(pos[0] + 256 , 256 -pos[1]));
 			needleColor.add(Color.GREEN);
 			
 			estimatedOrientations.add(Orientation.orientationInDegree(angEstimated[0], angEstimated[1]));
 			estimatedPositions.add(new Coordinate2D(pos[0] + 256 , 256 -pos[1]));
 			needleColor.add(Color.MAGENTA);
 			
 			
 			this.repaint();
 			
 			
 		}
 		
 		else{ // move point
 		
 			Coordinate2D activeCoord = null;
 			
 			if(active == FORMER)
 				activeCoord = formerCoord;
 			else if(active == LATTER)
 				activeCoord = latterCoord;
 			else if(active == CENTER)
 				activeCoord = centerCoord;
 			
 			moveCoordinate(activeCoord, e.getKeyCode(), 1);
 		
 			
 			this.repaint();
 		}
 		
 	}
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	
 	
 	private void estimateByFormerLatter(){
 		
 		
 			
 			centerPicked = true;
 			needleColor.add(Color.GREEN);
 			
 			int centerX = (formerCoord.getX() + latterCoord.getX())/2;
 			int centerY = (formerCoord.getY() + latterCoord.getY())/2;
 			
 			centerCoord.setCoordinate(centerX, centerY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight());
 			
			long startTime = System.currentTimeMillis();
 			
 			Orientation orientation = mainView.shapeEstimate(formerCoord, latterCoord);
 			
			long endTime = System.currentTimeMillis();
			
			System.out.println((endTime - startTime) / 1000);
			
 			estimatedOrientations.add(orientation);
 			
 			estimatedPositions.add(new Coordinate2D(centerX, centerY));
 			
 			needleColor.add(Color.GREEN);
 			
 			
 	}
 	
 	
 	private void estimateByCenterDiagonal(){
 		
 			
 			int topLeftX = centerCoord.getX() - patchWidth/2;
 			int topLeftY = centerCoord.getY() - patchHeight/2;
 			
 			int bottomRightX = centerCoord.getX() + patchWidth/2;
 			int bottomRightY = centerCoord.getY() + patchHeight/2;
 					
 //			needleColor = Color.RED;
 			needleColor.add(Color.GREEN);
 			
 			mainView.shapeEstimate(new Coordinate2D(topLeftX, topLeftY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()), new Coordinate2D(bottomRightX, bottomRightY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()));
 			
 			
 			int topRightX = centerCoord.getX() + patchWidth/2;
 			int topRightY = centerCoord.getY() + patchHeight/2;
 			
 			int bottomLeftX = centerCoord.getX() - patchWidth/2;
 			int bottomLeftY = centerCoord.getY() - patchHeight/2;
 					
 			needleColor.add(Color.GREEN);
 			
 			mainView.shapeEstimate(new Coordinate2D(topRightX, topRightY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()), new Coordinate2D(bottomLeftX, bottomLeftY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()));
 			
 			
 		
 		
 	}
 	
 	
 	private void estimateByCenterCross(){
 		
 
 		int leftX = centerCoord.getX() - patchWidth/2;
 		int leftY = centerCoord.getY();
 		
 		int rightX = centerCoord.getX() + patchWidth/2;
 		int rightY = centerCoord.getY();
 				
 //		needleColor = Color.RED;
 		needleColor.add(Color.GREEN);
 		
 		mainView.shapeEstimate(new Coordinate2D(leftX, leftY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()), new Coordinate2D(rightX, rightY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()));
 		
 		
 		int topX = centerCoord.getX();
 		int topY = centerCoord.getY() + patchHeight/2;
 		
 		int bottomX = centerCoord.getX();
 		int bottomY = centerCoord.getY() - patchHeight/2;
 				
 		needleColor.add(Color.GREEN);
 		
 		mainView.shapeEstimate(new Coordinate2D(topX, topY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()), new Coordinate2D(bottomX, bottomY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()));
 		
 		
 	}
 	
 	private void estimateGridVertical(int gridGap){
 		
 		
 
 		int centerX = centerCoord.getX();
 		int centerY = centerCoord.getY();
 		
 			
 		LinkedList<Orientation> resultList = new LinkedList<Orientation>();
 		
 		System.out.println("#1");
 		resultList.add(estimateVertical(centerX, centerY)); //#1
 		this.repaint();
 		
 		System.out.println("#2");
 		resultList.add(estimateVertical(centerX - gridGap, centerY)); //#2
 		this.repaint();
 		
 		System.out.println("#3");
 		resultList.add(estimateVertical(centerX + gridGap, centerY)); //#3
 		this.repaint();
 		
 		System.out.println("#4");
 		resultList.add(estimateVertical(centerX, centerY - gridGap)); //#4
 		this.repaint();
 		
 		System.out.println("#5");
 		resultList.add(estimateVertical(centerX, centerY + gridGap)); //#5
 		this.repaint();
 		
 		System.out.println("#6");
 		resultList.add(estimateVertical(centerX - gridGap, centerY - gridGap)); //#6
 		this.repaint();
 		
 		System.out.println("#7");
 		resultList.add(estimateVertical(centerX + gridGap, centerY - gridGap)); //#7
 		this.repaint();
 		
 		System.out.println("#8");
 		resultList.add(estimateVertical(centerX - gridGap, centerY + gridGap)); //#8
 		this.repaint();
 		
 		System.out.println("#9");
 		resultList.add(estimateVertical(centerX + gridGap, centerY + gridGap)); //#9
 		this.repaint();
 		
 
 		
 
 		Orientation avgOrient = avgOrientation(resultList, null);
 		
 		System.out.println("Grid Avg: slant = " + avgOrient.getSlantD() + ", tilt = " + avgOrient.getTiltD());
 	}
 	
 	
 	private void estimateGrid(int gridGap){
 		
 		int centerX = centerCoord.getX();
 		int centerY = centerCoord.getY();
 		
 			
 		LinkedList<Orientation> resultList = new LinkedList<Orientation>();
 		
 		System.out.println("#1");
 		resultList.add(estimateSingle(centerX, centerY)); //#1
 		this.repaint();
 		
 		System.out.println("#2");
 		resultList.add(estimateSingle(centerX - gridGap, centerY)); //#2
 		this.repaint();
 		
 		System.out.println("#3");
 		resultList.add(estimateSingle(centerX + gridGap, centerY)); //#3
 		this.repaint();
 		
 		System.out.println("#4");
 		resultList.add(estimateSingle(centerX, centerY - gridGap)); //#4
 		this.repaint();
 		
 		System.out.println("#5");
 		resultList.add(estimateSingle(centerX, centerY + gridGap)); //#5
 		this.repaint();
 		
 		System.out.println("#6");
 		resultList.add(estimateSingle(centerX - gridGap, centerY - gridGap)); //#6
 		this.repaint();
 		
 		System.out.println("#7");
 		resultList.add(estimateSingle(centerX + gridGap, centerY - gridGap)); //#7
 		this.repaint();
 		
 		System.out.println("#8");
 		resultList.add(estimateSingle(centerX - gridGap, centerY + gridGap)); //#8
 		this.repaint();
 		
 		System.out.println("#9");
 		resultList.add(estimateSingle(centerX + gridGap, centerY + gridGap)); //#9
 		this.repaint();
 		
 
 		
 
 		Orientation avgOrient = avgOrientation(resultList, null);
 		
 		System.out.println("Grid Avg: slant = " + avgOrient.getSlantD() + ", tilt = " + avgOrient.getTiltD());
 		
 	}
 	
 	private Orientation estimateVertical(int centerX, int centerY){
 		
 		LinkedList<Orientation> resultOrientationList = new LinkedList<Orientation>();
 		
 		int x1 = centerX;
 		int y1 = centerY - patchHeight/2;
 		
 		int x2 = centerX;
 		int y2 = centerY + patchHeight/2;
 		
 		
 		Orientation avgOrient = mainView.shapeEstimate(new Coordinate2D(x1, y1, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()), new Coordinate2D(x2, y2, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()));
 
 		System.out.println("Single Avg: slant = " + avgOrient.getSlantD() + ", tilt = " + avgOrient.getTiltD());
 		
 
 		
 		estimatedOrientations.add(avgOrient);
 		
 		estimatedPositions.add(new Coordinate2D(centerX, centerY));
 		
 		needleColor.add(Color.GREEN);
 		
 		return avgOrient;
 	}
 	
 	
 	
 	private Orientation estimateSingle(int centerX, int centerY){
 		
 		
 		LinkedList<Orientation> resultOrientationList = new LinkedList<Orientation>();
 		
 
 		//Diagonal
 		int topLeftX = centerX - patchWidth/2;
 		int topLeftY = centerY - patchHeight/2;
 		
 		int bottomRightX = centerX + patchWidth/2;
 		int bottomRightY = centerY + patchHeight/2;
 				
 
 		
 //		needleColor.add(Color.GREEN);
 		
 		
 		
 		
 		int topRightX = centerX + patchWidth/2;
 		int topRightY = centerY + patchHeight/2;
 		
 		int bottomLeftX = centerX - patchWidth/2;
 		int bottomLeftY = centerY - patchHeight/2;
 				
 //		needleColor.add(Color.GREEN);
 		
 		
 		
 		
 		
 		//Cross
 		int leftX = centerX - patchWidth/2;
 		int leftY = centerY;
 		
 		int rightX = centerX + patchWidth/2;
 		int rightY = centerY;
 				
 
 //		needleColor.add(Color.GREEN);
 		
 		
 		
 		
 		int topX = centerX;
 		int topY = centerY + patchHeight/2;
 		
 		int bottomX = centerX;
 		int bottomY = centerY - patchHeight/2;
 				
 //		needleColor.add(Color.GREEN);
 		
 		
 		
 		
 		
 		Orientation result1 = mainView.shapeEstimate(new Coordinate2D(topLeftX, topLeftY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()), new Coordinate2D(bottomRightX, bottomRightY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()));
 		
 		Orientation result2 = mainView.shapeEstimate(new Coordinate2D(topRightX, topRightY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()), new Coordinate2D(bottomLeftX, bottomLeftY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()));
 		
 		Orientation result3 = mainView.shapeEstimate(new Coordinate2D(leftX, leftY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()), new Coordinate2D(rightX, rightY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()));
 				
 		Orientation result4 = mainView.shapeEstimate(new Coordinate2D(topX, topY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()), new Coordinate2D(bottomX, bottomY, mainView.getSrcImgWidth(), mainView.getSrcImgHeight()));
 		
 		
 		
 		resultOrientationList.add(result1);
 		
 		resultOrientationList.add(result2);
 		
 		resultOrientationList.add(result3);
 		
 		resultOrientationList.add(result4);
 		
 		
 		
 		Orientation avgOrient = avgOrientation(resultOrientationList, null);
 		
 //		System.out.println("BeforeWeight: slant = " + avgOrient.getSlantD() + ", tilt = " + avgOrient.getTiltD());
 //		
 //		LinkedList<Double> weightList = new LinkedList<Double>();
 //		
 //		weightList.add(directionWeight(1, avgOrient.getTiltD()));
 //		weightList.add(directionWeight(2, avgOrient.getTiltD()));
 //		weightList.add(directionWeight(3, avgOrient.getTiltD()));
 //		weightList.add(directionWeight(4, avgOrient.getTiltD()));
 //		
 //		
 //		Orientation weightedAvgOrient = avgOrientation(resultOrientationList, weightList);
 		
 		
 		System.out.println("Single Avg: slant = " + avgOrient.getSlantD() + ", tilt = " + avgOrient.getTiltD());
 		
 
 		
 		estimatedOrientations.add(avgOrient);
 		
 		estimatedPositions.add(new Coordinate2D(centerX, centerY));
 		
 		needleColor.add(Color.GREEN);
 		
 		return avgOrient;
 	}
 	
 
 	
 	
 	private double directionWeight(int direction, double tiltD){
 		
 		double degreeDiff;
 		
 		if(direction == 1){
 			
 			degreeDiff = Math.min(Math.abs(-45.0 - tiltD), Math.abs(135.0 - tiltD));
 			
 		}
 		else if(direction == 2){
 			
 			degreeDiff = Math.min(Math.abs(45.0 - tiltD), Math.abs(-135.0 - tiltD));
 		}
 		else if(direction == 3){
 			
 			degreeDiff = Math.min(Math.abs(tiltD), Math.abs(180.0 - tiltD));
 		}
 		else if(direction == 4){
 			
 			degreeDiff = Math.min(Math.abs(90.0 - tiltD), Math.abs(-90.0 - tiltD));
 		}
 		else
 			return 0;
 		
 		if(degreeDiff > 90.0)
 			degreeDiff = 180.0 - degreeDiff;
 		
 		double weight = (Math.abs(degreeDiff - 45.0) / 45.0); 
 		
 //		System.out.println("weight:" + weight);
 		
 		return weight;
 	}
 	
 	
 	
 	
 	protected void paintComponent(Graphics g) {
 		
		
 		g.drawImage(mainView.getSrcImg().getBufferedImg(), 0, 0, this);
         
         
         drawBoundary(g);
         
         drawNeedle(g);
         
         drawGrid(g);
 	}
 	
 	
 	
 	private void drawGrid(Graphics g){
 		
 		if(centerPicked){
 			
 			
 			g.setColor(POINT_COLOR);
 
 			((Graphics2D)g).setStroke(new BasicStroke(2.0f));
 			
         	g.drawRect(centerCoord.getX() - 1, centerCoord.getY() - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() - GRID_GAP - 1, centerCoord.getY() - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() + GRID_GAP - 1, centerCoord.getY() - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() - 1, centerCoord.getY() - GRID_GAP - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() - 1, centerCoord.getY() + GRID_GAP - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() - GRID_GAP - 1, centerCoord.getY() - GRID_GAP - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() + GRID_GAP - 1, centerCoord.getY() - GRID_GAP - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() - GRID_GAP - 1, centerCoord.getY() + GRID_GAP - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() + GRID_GAP - 1, centerCoord.getY() + GRID_GAP - 1, 2, 2);
 			
 		}
 		
 	}
 	
 	
 	
 
 	private void drawNeedle(Graphics g){
 		
 		if(!centerPicked)
 			return;
 		
 		((Graphics2D)g).setStroke(new BasicStroke(5.0f));
 		
 		
 		for(int i = 0; i < estimatedOrientations.size(); i++){
 			
 			Coordinate2D position = estimatedPositions.get(i); 
 		
 			Orientation orient = estimatedOrientations.get(i);
 			
 			
 			int x1 = position.getX();
 			
 			int y1 = position.getY();
 			
 			
 			
 			
 			int x2 = (int) Math.round(x1 + NEEDLE_LENGTH * Math.sin(orient.getSlantR()) * Math.cos(orient.getTiltR()));
 			
 			int y2 = (int) Math.round(y1 - NEEDLE_LENGTH * Math.sin(orient.getSlantR()) * Math.sin(orient.getTiltR()));
 			
 			g.setColor(needleColor.get(i));
 			
 			g.drawLine(x1, y1, x2, y2);
 			
 //			g.setColor(Color.GREEN);
 //			
 //			g.drawLine(x1, y1, x1, y1);
 			
 			
 			
 		}
 		
 	}
 	
 	private void drawBoundary(Graphics g){
 		
 		if(leftPicked){
         	
         	System.out.println("Former" + formerCoord.toString());
         	
         	g.setColor(POINT_COLOR);
         	
         	g.drawRect(formerCoord.getX() - 1, formerCoord.getY() - 1, 2, 2);
 
         	g.setColor(FORMER_COLOR);
         	
         	g.drawRect(formerCoord.getX() - (patchWidth - 1) / 2, formerCoord.getY() - (patchHeight - 1) / 2, patchWidth, patchHeight);
         	
         	
         	
 //        	g.drawLine(formerCoord.getX(), formerCoord.getY(), formerCoord.getX(), formerCoord.getY());
         	
         }
        if(rightPicked){
 
        	System.out.println("Latter" + latterCoord.toString());
        	
        	g.setColor(POINT_COLOR);
        	
        	g.drawRect(latterCoord.getX() - 1, latterCoord.getY() - 1, 2, 2);
        	
        	g.setColor(LATTER_COLOR);
        	
        	g.drawRect(latterCoord.getX() - (patchWidth - 1) / 2, latterCoord.getY() - (patchHeight - 1) / 2, patchWidth, patchHeight);
        	
     	   
         }
        if(centerPicked){
     	   
     	   System.out.println("Center" + centerCoord.toString());
     	   
     	   g.setColor(POINT_COLOR);
     	   
     	   g.drawRect(centerCoord.getX() - 1, centerCoord.getY() - 1, 2, 2);
        }
 		
 	}
 	
 	
 	
 	private Orientation avgOrientation(List<Orientation> orientationList, List<Double> weightList){
 		
 		double sumSlantD = 0.0;
 		double sumTiltD = 0.0;
 		
 		Iterator<Orientation> iter = orientationList.iterator();
 		
 		if(weightList == null){
 		
 			while(iter.hasNext()){
 				Orientation currentOrient = iter.next();
 				
 				sumSlantD += currentOrient.getSlantD();
 				sumTiltD += currentOrient.getTiltD();
 			}
 			
 			return Orientation.orientationInDegree(sumSlantD / orientationList.size(), sumTiltD / orientationList.size());
 		}
 		else{
 			
 			Iterator<Double> weightIter = weightList.iterator();
 			double weightSum = 0.0;
 			
 			while(iter.hasNext()){
 				Orientation currentOrient = iter.next();
 				double weight  = weightIter.next();
 				
 				
 				sumSlantD += weight * currentOrient.getSlantD();
 				sumTiltD += weight * currentOrient.getTiltD();
 				
 				weightSum += weight;
 			}
 			
 			return Orientation.orientationInDegree(sumSlantD / weightSum, sumTiltD / weightSum);
 			
 			
 			
 		}
 //		
 //		double rawSlantD = sumSlantD / orientationList.size();
 //		double rawTiltD = sumTiltD / orientationList.size();
 //		
 //		Iterator<Orientation> newIter = orientationList.iterator();
 //		
 //		
 //		double newSumSlantD = 0.0;
 //		double newSumTiltD = 0.0;
 //		int newSize = 0;
 //		
 //		while(newIter.hasNext()){
 //			
 //			Orientation currOrient = newIter.next();
 //			
 //			if(Math.abs(currOrient.getSlantD() - rawSlantD) + Math.abs(currOrient.getTiltD() - rawTiltD) < 10.0){
 //				
 //				newSumSlantD += currOrient.getSlantD();
 //				newSumTiltD += currOrient.getTiltD();
 //				newSize++;
 //				
 //				System.out.println("Accepted: " + currOrient.getSlantD() + ", " + currOrient.getTiltD());
 //			}
 //			else
 //				System.out.println("Rejected: " + currOrient.getSlantD() + ", " + currOrient.getTiltD());
 //				
 //		}
 //		
 //		
 //		return Orientation.orientationInDegree(newSumSlantD / newSize, newSumTiltD / newSize);
 		
 	}
 	
 	
 	
 	
 	private void moveCoordinate(Coordinate2D activeCoord, int direction, int step){
 		
 		
 		
 		if(direction == KeyEvent.VK_UP){
 			
 			activeCoord.moveUp(step);
 			
 		}
 		else if(direction == KeyEvent.VK_DOWN){
 			
 			activeCoord.moveDown(step);
 			
 		}
 		else if(direction == KeyEvent.VK_LEFT){
 			
 			activeCoord.moveLeft(step);
 			
 		}
 		else if(direction == KeyEvent.VK_RIGHT){
 			
 			activeCoord.moveRight(step);
 			
 		}
 		
 	}
 	
 	
 
 	public void init() {
 		// TODO Auto-generated method stub
 		
 		this.setLayout(null);
 		
 		this.setBounds(LEFT_MARGIN, UP_MARGIN, PANEL_WIDTH, PANEL_HEIGHT);
 		
 		this.setFocusable(true);
 		
 		
 				
 		this.addMouseListener(this);
 		
 		this.addKeyListener(this);
 		
 		
 		formerCoord = new Coordinate2D();
 		latterCoord = new Coordinate2D();
 		centerCoord = new Coordinate2D();
 		
 		estimatedOrientations = new LinkedList<Orientation>();
 		
 		
 		estimatedPositions = new LinkedList<Coordinate2D>();
 		
 		needleColor = new LinkedList<Color>();
 		
 		patchWidth = mainView.getPatchWidth();
 		
 		patchHeight = mainView.getPatchHeight();
 		
 	}
 
 	
 	public void setMainView(IMyView myView){
 		
 		this.mainView = myView;
 		
 	}
 	
 
 	
 	public void keyReleased(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	public void mouseClicked(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 
 	
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 
 }
