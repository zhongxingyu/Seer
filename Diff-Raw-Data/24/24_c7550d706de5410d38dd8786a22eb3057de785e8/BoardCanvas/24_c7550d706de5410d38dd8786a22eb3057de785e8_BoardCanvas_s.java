 /*
  * Fling! Solver
  * Copyright (C) 2013 - Jos Expsito <jose.exposito89@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>
  */
 package com.eggsoftware.flingsolver.gui;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 import com.eggsoftware.flingsolver.solver.BoardSolver.Direction;
 
 /**
  * Draws the Fling! board. Allows to edit the board if necessary allowing to the users to draw their own Fling! level to
  * solve.
  */
 public class BoardCanvas extends View {
 
 	/**
 	 * Dimensions of the board.
 	 */
 	private static final int BOARD_NUM_COLUMNS = 7;
 	private static final int BOARD_NUM_ROWS    = 8;
 	
 	/**
 	 * Paints to draw the board, the Flings and the row.
 	 */
 	private Paint emptySquarePaint;
 	private Paint usedSquaredPaint;
 	private Paint arrowPaint;
 
 	/**
 	 * The board to draw.
 	 */
 	private boolean[][] board;
 
 	/**
 	 * Some values to draw the board.
 	 */
 	private int squareSize;
 	private int topMargin;
 	private int leftMargin;
 	private boolean acceptEditFlings = false;
 	
 	/**
 	 * To draw an arrow if necessary.
 	 */
 	private boolean drawArrow = false;
 	private int arrowRow;
 	private int arrowCol;
 	private Direction arrowDirection;
 	
 	/**
 	 * Necessary constructors to be able to add this view to a XML activity.
 	 */
 	public BoardCanvas(Context context) {
 		super(context);
 		this.init();
 	}
 
 	public BoardCanvas(Context context, AttributeSet attrs) {
         super(context, attrs);
         this.init();
     }
     public BoardCanvas(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         this.init();
     }
 	
     /**
      * Real constructor.
      */
     private void init() {
     	// Initialize the painters
     	this.emptySquarePaint = new Paint();
     	this.emptySquarePaint.setStyle(Paint.Style.STROKE);
     	this.emptySquarePaint.setStrokeWidth(4);
  		this.emptySquarePaint.setARGB(255, 213, 213, 213);
  		this.emptySquarePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
  		
  		this.usedSquaredPaint = new Paint();
  		this.usedSquaredPaint.setARGB(255, 113, 113, 113);
  		this.usedSquaredPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
  		
  		this.arrowPaint = new Paint();
  		this.arrowPaint.setARGB(255, 113, 113, 113);
  		this.arrowPaint.setStrokeWidth(4);
  		this.arrowPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
  		
  		// Initialize the board
  		this.board = new boolean[BOARD_NUM_ROWS][BOARD_NUM_COLUMNS];
  		for (int row=0; row<BOARD_NUM_ROWS; row++) {
  			for (int col=0; col<BOARD_NUM_COLUMNS; col++) {
  				this.board[row][col] = false;
  			}
  		}
     }
 	
 	/**
 	 * To allow or not to edit the board by touching.
 	 * By default it is false.
 	 */
 	public void setAcceptEditFlings(boolean acceptEditFlings) {
 		this.acceptEditFlings = acceptEditFlings;
 	}
 	
 	/**
 	 * Returns or sets the drawn board.
 	 */
 	public boolean[][] getBoardRepresentation() {
 		return this.board;
 	}
 	
 	public void setBoardRepresentation(boolean[][] board) {
 		this.board = board;
 		this.invalidate();
 	}
 	
 	/**
 	 * Called BEFORE the board is painted. Sets the dimensions of the board. 
 	 */
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		// Calculate the size of the board
		this.squareSize = MeasureSpec.getSize(widthMeasureSpec) / BOARD_NUM_ROWS;
		this.leftMargin = (MeasureSpec.getSize(widthMeasureSpec) - squareSize*BOARD_NUM_COLUMNS) / 2;
		this.topMargin  = this.leftMargin;
 				
 		// Set the size of the board to full width and aspect ratio height
 		int desiredWidth  = MeasureSpec.getSize(widthMeasureSpec);
 		int desiredHeight = this.topMargin + (BOARD_NUM_ROWS * this.squareSize) + this.topMargin;
 		
 		this.setMeasuredDimension(desiredWidth, desiredHeight);
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);		
 		
 		// Draw the board
 		for (int row=0; row<BOARD_NUM_ROWS; row++) {
 			for (int col=0; col<BOARD_NUM_COLUMNS; col++) {
 				if (!(this.drawArrow && this.arrowCol == col && this.arrowRow == row)) {
 					this.drawSquare(canvas, row, col, this.board[row][col]);
 				}
 			}
 		}
 		
 		// Draw the arrow if any
 		if (this.drawArrow) {
 			this.drawArrow(canvas, this.arrowRow, this.arrowCol, this.arrowDirection);
 		}
 	}
 	
 	/**
 	 * Draws a square with or without Fling!
 	 */
 	private void drawSquare(Canvas canvas, int row, int col, boolean haveFling) {
 		float colToX = leftMargin + col*squareSize + squareSize/2;
 		float rowToY = topMargin  + row*squareSize + squareSize/2;
 		
 		if (haveFling)
 			canvas.drawCircle(colToX, rowToY, squareSize/2-5, this.usedSquaredPaint);
 		canvas.drawCircle(colToX, rowToY, squareSize/2-5, this.emptySquarePaint);
 	}
 	
 	/**
 	 * Draws an arrow in the specified position
 	 */
 	private void drawArrow(Canvas canvas, int row, int col, Direction direction) {
 		float firstPointX  = -1;
 		float firstPointY  = -1;
 		float secondPointX = -1;
 		float secondPointY = -1;
 		float thirdPointX  = -1;
 		float thirdPointY  = -1;
 		
 		int squareX = leftMargin + col*squareSize;
 		int squareY = topMargin  + row*squareSize;
 		
 		if (direction == Direction.UP) {
 			firstPointX  = squareX + squareSize/4;
 			firstPointY  = squareY + 3*squareSize/4;
 			secondPointX = squareX + squareSize/2;
 			secondPointY = squareY + squareSize/4;
 			thirdPointX  = squareX + 3*squareSize/4;
 			thirdPointY  = squareY + 3*squareSize/4;
 			
 		} else if (direction == Direction.DOWN) {
 			firstPointX  = squareX + squareSize/4;
 			firstPointY  = squareY + squareSize/4;
 			secondPointX = squareX + squareSize/2;
 			secondPointY = squareY + 3*squareSize/4;
 			thirdPointX  = squareX + 3*squareSize/4;
 			thirdPointY  = squareY + squareSize/4;
 			
 		} else if (direction == Direction.LEFT) {
 			firstPointX  = squareX + 3*squareSize/4;
 			firstPointY  = squareY + squareSize/4;
 			secondPointX = squareX + squareSize/4;
 			secondPointY = squareY + squareSize/2;
 			thirdPointX  = squareX + 3*squareSize/4;
 			thirdPointY  = squareY + 3*squareSize/4;
 			
 		} else if (direction == Direction.RIGHT) {
 			firstPointX  = squareX + squareSize/4;
 			firstPointY  = squareY + squareSize/4;
 			secondPointX = squareX + 3*squareSize/4;
 			secondPointY = squareY + squareSize/2;
 			thirdPointX  = squareX + squareSize/4;
 			thirdPointY  = squareY + 3*squareSize/4;
 		}
 
 		canvas.drawLine(firstPointX, firstPointY, secondPointX, secondPointY, this.arrowPaint);
 		canvas.drawLine(secondPointX, secondPointY, thirdPointX, thirdPointY, this.arrowPaint);
 	}
 	
 	@Override
 	public boolean onTouchEvent (MotionEvent event) {
 		int action = event.getAction() & MotionEvent.ACTION_MASK;
 		if (this.acceptEditFlings && action == MotionEvent.ACTION_DOWN) {
 			int touchedRow = (int)((event.getY() - this.topMargin) / this.squareSize);
 			int touchedCol = (int)((event.getX() - this.leftMargin) / this.squareSize);
 			
 			if (touchedRow >= 0 && touchedCol >= 0 && touchedRow < BOARD_NUM_ROWS && touchedCol < BOARD_NUM_COLUMNS) {
 				this.board[touchedRow][touchedCol] = !this.board[touchedRow][touchedCol];
 				this.invalidate();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Draws an arrow in the specified position and direction.
 	 */
 	public void setArrow(int row, int col, Direction direction) {
 		this.drawArrow = true;
 		this.arrowDirection = direction;
 		
 		if (direction == Direction.UP) {
 			this.arrowRow = row - 1;
 			this.arrowCol = col;
 			
 		} else if (direction == Direction.DOWN) {
 			this.arrowRow = row + 1;
 			this.arrowCol = col;
 			
 		} else if (direction == Direction.LEFT) {
 			this.arrowRow = row;
 			this.arrowCol = col - 1;
 			
 		} else if (direction == Direction.RIGHT) {
 			this.arrowRow = row;
 			this.arrowCol = col + 1;
 		}
 		
 		this.invalidate();
 	}
 	
 }
