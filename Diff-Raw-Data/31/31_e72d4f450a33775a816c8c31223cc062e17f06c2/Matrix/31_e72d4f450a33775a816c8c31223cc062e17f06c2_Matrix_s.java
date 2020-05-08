 package com.anthropomo.digitalrattle.models;
 
 import com.anthropomo.digitalrattle.io.ConfigEvent.IConfigConsumer;
 import com.anthropomo.digitalrattle.io.ICommand;
 import com.anthropomo.digitalrattle.io.IEventPublisher;
 import com.anthropomo.digitalrattle.io.ScreenEvent.IScreenConsumer;
 
 public class Matrix implements IConfigConsumer, IScreenConsumer{
 	
 	public int[][] cells;
 	private float spaceWidth, spaceHeight;
 	private float screenWidth = 0, screenHeight = 0;
 	private int xC = 0, yR = 0;
 	private boolean haveCAndR = false, haveScreen = false;
 	private MatrixConsumer callback;
 	
 	public Matrix(IEventPublisher eventPublisher, MatrixConsumer callback){
 		eventPublisher.registerSubscriber(this);
 		this.callback = callback;
 	}
 	
 	public interface MatrixConsumer {
 		public void onMatrixUpdate();
 	}
 	
 	private void checkAndInit(){
 		if(haveCAndR && haveScreen){
 			initMatrix();
			callback.onMatrixUpdate();
 		}
 	}
 	
 	private void initMatrix(){
 		cells = populateCells(xC,yR);
 		spaceWidth = screenWidth / xC;
 		spaceHeight = screenHeight / yR;
 	}
 		
 	public float getSpaceWidth(){
 		return spaceWidth;
 	}
 	
 	public int getCellNum(float x, float y){
 		return getCell(x,y);
 	}
 	
 	public int getCell(float x, float y){
 		int i = (int)(x / spaceWidth);
 		int j = (int)(y / spaceHeight);
 		if(i > xC-1){
 			i = xC-1;
 		}
 		if(j > yR-1){
 			j = yR-1;
 		}
 		return cells[i][j];
 	}
 	
 	private int[][] populateCells(int xCols, int yRows) {
 		int[][] c = new int[xCols][yRows];
 		int k = 0;
 		for (int i = 0; i < xCols; i++){
 			for (int j = 0; j < yRows; j++){
 			    c[i][j] = k;
 			    k++;
 			}
 		}
 		return c;
 	}
 
 	@Override
 	public void onEvent(ICommand e) {
 		e.execute(this);
 	}
 
 	@Override
 	public void onScreenEvent(float screenHeight, float screenWidth,
 			float screenHeightDp, float screenWidthDp) {
 		this.screenWidth = screenWidth;
 		this.screenHeight = screenHeight;
 		haveScreen = true;
 		checkAndInit();
 	}
 
 	@Override
 	public void onConfigEvent(int baseNote, int numNotes, int noteRows,
 			int noteCols) {
 		xC = noteCols;
 		yR = noteRows;
 		haveCAndR = true;
 		checkAndInit();
 	}
 
 }
