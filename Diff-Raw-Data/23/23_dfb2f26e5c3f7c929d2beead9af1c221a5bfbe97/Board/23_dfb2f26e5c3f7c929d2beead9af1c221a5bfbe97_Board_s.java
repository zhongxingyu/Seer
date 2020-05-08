 package com.dolson.connectfour;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Toast;
 
 public class Board extends View implements OnClickListener
 {
 	private int[][] board;
 	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	private int placeChecker;
 	private float diameter;
 	private int space;
 	private int oldSpace;
 	private final float dropSpeed = (float)0.25;
 	private float yValue;
 	private boolean dropToken;
 	private boolean isWinner;
 	private int playerTurn;
 	float spacing;
 	int smallestWidth;
     int orientedHeight;
 	Bitmap boardImage = BitmapFactory.decodeResource(getResources(), R.drawable.board);
 		
 	private Player p0;
 	private Player p1;
 	
 	public Board(Context context)
 	{
 		super(context);
 		
 		this.resetBoard();
 			
 		p0 = new Player("test1", 0, 0, this);
 		p1 = new Player("test2", 1, 0, this);
 		this.notifyPlayerTurn();
 	}
 
 	@Override
	protected void onDraw(Canvas canvas) {
 	    super.onDraw(canvas);
 	    
 
 	    
 	    if (this.getWidth() < this.getHeight())
 	    {
 	    	smallestWidth = this.getWidth();
 	    	System.out.println("test"+smallestWidth);
 	    	orientedHeight = this.getHeight();
 	    }
 	    else
 	    {
 	    	smallestWidth = this.getHeight();
 	    	orientedHeight = this.getWidth();
 	    }
 
 	    
 	    spacing = 4*((float)smallestWidth/(float)728);
 	    System.out.println("spacing"+spacing);
 	    diameter = 96*((float)smallestWidth/(float)728);
 	    System.out.println("diameter"+diameter);
 	    
 	    float radius = diameter/2;
 	   
 	    System.out.println("total  " + (spacing*14 + diameter*7));
 	   
 	    
 	    for (int row = 1; row <= 6; row++)
 	    {
 	    	for (int i = 0; i < 7; i++)
 	    	{
 	    		// if board = 1 > red
 	    		//if board = 2 > black
 	    		//if board = 0 > white
 	    		if (board[6-row][i] == -1)
 	    			paint.setColor(Color.WHITE);
 	    		else if (board[6-row][i] == 0)
 	    			paint.setColor(Color.RED);
 	    		else if (board[6-row][i] == 1)
 	    			paint.setColor(Color.BLACK);
 	    		
	    		canvas.drawCircle((i*diameter)+(radius)+((i+1)*spacing)+(i*spacing), (orientedHeight-(diameter*row))+radius, radius, paint);	
 	    	}
 	    	//canvas.drawBitmap(boardImage, null, new Rect(0, (int)(orientedHeight-(616*((float)smallestWidth/(float)720))), smallestWidth, orientedHeight), paint);
 	    }
 	    
 	    //this will draw the checker that hasnt been dropped but will be when button released
 	    if (playerTurn == 0)
     		paint.setColor(Color.RED);
     	else
     		paint.setColor(Color.BLACK);
 	    
 	    if (placeChecker != -1)
 	    {
	    	canvas.drawCircle((placeChecker*diameter)+(radius)+(placeChecker+1)*spacing+placeChecker*spacing, (orientedHeight-(diameter*7))+radius, radius, paint);
 	    }
 	    
 	    if (dropToken)
 	    {
	    	
	    	canvas.drawCircle((space*diameter)+(radius)+(space+1)*spacing+space*spacing, (orientedHeight-(diameter*yValue))+radius, radius, paint);
 	    	
 	    	
 	    	if (yValue <= (board.length-findYValue(space)))
 	    	{
 	    		insert(space, playerTurn);
 	    		dropToken = false;
 	    		if (!isWinner)
 	    		{
 	    			playerTurn = (playerTurn+1)%2;
 	    			this.notifyPlayerTurn();
 	    		}
 	    		
 	    	}
 	    	yValue = yValue - dropSpeed;
 	    	//canvas.drawBitmap(boardImage, 0, orientedHeight - boardImage.getHeight(), paint);
 	    	this.invalidate();
 	    }
 	    
 	    
 	    
	    //canvas.drawBitmap(boardImage, null, new Rect(0, (int)(orientedHeight-(616*((float)smallestWidth/(float)728))), smallestWidth, orientedHeight), paint);
 	    
 	}
 	
 
 	private void insert(int col, int player)
 	{
 		boolean placedToken = false;
 		int row = -1;
 		for (int i = board.length-1; i >= 0; i--)
 		{
 			if (board[i][col] == -1) //then empty space
 			{
 				board[i][col] = player;
 				row = i;
 				placedToken = true;
 				break;
 			}
 		}
 		//if it wasnt placed, then the slot was full so need to try another slot
 		if (!placedToken)
 		{
 			//do a toast message
 		}
 		//otherwise, check to see if anybody won
 		else
 		{
 			checkWinner(row, col);
 		}
 			
 	}
 	
 	/*
 	 * 0 - Nobody won yet
 	 * 1 - Player one won
 	 * 2 - Player 2 won
 	 */
 	protected boolean checkWinner(int row, int col)
 	{
 		int tempRow = row;
 		int tempCol = col;
 		
 		//check horizontal
 		int p = -1;
 		int count = 0;
 		for (int j = 0; j < board[row].length; j++)
 		{
 			if (board[row][j] != -1)
 			{
 				if (board[row][j] == p)
 					count++;
 				else
 				{
 					p = board[row][j];
 					count = 1;
 				}
 			}
 			else
 			{
 				p = -1;
 				count = 0;
 			}
 			//we have a winner
 			if (count == 4)
 			{
 				CharSequence text = "Winner! Winner! Winner!   " + this.getPlayer(playerTurn).getName();
 				int duration = Toast.LENGTH_SHORT;
 				Toast toast = Toast.makeText(super.getContext(), text, duration);
 				toast.show();
 				isWinner = true;
 			}
 		}
 		
 		
 		//check vertical
 		count = 0;
 		p = -1;
 		for (int j = board.length-1; j >= 0; j--)
 		{
 			if (board[j][col] == -1)
 				break;
 			else
 			{
 				if (board[j][col] == p)
 					count++;
 				else
 				{
 					p = board[j][col];
 					count = 1;
 				}
 			}
 			
 			//check to see if we have 4 in a row
 			if (count == 4)
 			{
 				CharSequence text = "Winner! Winner! Winner!   " + this.getPlayer(playerTurn).getName();
 				int duration = Toast.LENGTH_SHORT;
 				Toast toast = Toast.makeText(super.getContext(), text, duration);
 				toast.show();
 				isWinner = true;
 			}
 		}
 		
 		
 		//check forwardslash
 		count = 0;
 		p = -1;
 		while (tempRow < board.length && tempCol >= 0)
 		{
 			tempRow++;
 			tempCol--;
 		}
 		tempRow--;
 		tempCol++;
 
 		//now we are at the most bottom left place as possible with that piece placement
 		
 		while (tempRow >= 0 && tempCol < board[0].length)
 		{
 			if (board[tempRow][tempCol] == -1)
 			{
 				count = 0;
 				p = -1;
 			}
 			else
 			{
 				if (board[tempRow][tempCol] == p)
 					count++;
 				else
 				{
 					p = board[tempRow][tempCol];
 					count = 1;
 				}
 			}
 			
 			//check to see if we have a winner
 			if (count == 4)
 			{
 				CharSequence text = "Winner! Winner! Winner!   " + this.getPlayer(playerTurn).getName();
 				int duration = Toast.LENGTH_SHORT;
 				Toast toast = Toast.makeText(super.getContext(), text, duration);
 				toast.show();
 				isWinner = true;
 			}
 			
 			tempRow--;
 			tempCol++;
 		}
 		
 		//check backslash
 		count = 0;
 		p = -1;
 		tempRow = row;
 		tempCol = col;
 		
 		while (tempRow > 0 && tempCol > 0)
 		{
 			tempRow--;
 			tempCol--;
 		}
 
 		while (tempRow < board.length && tempCol < board[0].length)
 		{
 			if (board[tempRow][tempCol] == -1)
 			{
 				count = 0;
 				p = -1;
 			}
 			else
 			{
 				if (board[tempRow][tempCol] == p)
 					count++;
 				else
 				{
 					p = board[tempRow][tempCol];
 					count = 1;
 				}
 			}
 			
 			//check to see if we have a winner
 			if (count == 4)
 			{
 				CharSequence text = "Winner! Winner! Winner!   " + this.getPlayer(playerTurn).getName();
 				int duration = Toast.LENGTH_SHORT;
 				Toast toast = Toast.makeText(super.getContext(), text, duration);
 				toast.show();
 				isWinner = true;
 			}
 			
 			tempRow++;
 			tempCol++;
 		}
 		
 		
 		return isWinner;
 	}
 	
 	@Override
 	public String toString()
 	{
 		String s = "";
 		for (int row = 0; row < board.length; row++)
 		{
 			for (int col = 0; col < board[0].length; col++)
 			{
 				s += "|" + board[row][col];
 			}
 			
 			s+="|\n";
 		}
 		return s;
 	}
 
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent me)
 	{
 		//if a token is already being dropped, dont let the person change while its dropping
 		//also if there is a winner, don't let there be any more input
 		//also if the computer is playing, we dont get any input
 		if (!dropToken && !isWinner && this.getPlayer(playerTurn).isHuman())
 		{
 			float xloc = me.getX();
 			System.out.println(me.getX());
 			space = (int)((xloc-1)/((float)smallestWidth/(float)7));	
 			
 			int eventaction = me.getAction();
 			switch (eventaction)
 			{
 			case MotionEvent.ACTION_DOWN:
 				placeChecker = space;
 				break;
 			case MotionEvent.ACTION_MOVE:
 				if (oldSpace != space)
 					placeChecker = space;
 				break;
 			case MotionEvent.ACTION_UP:
 				placeChecker = -1;
 				dropToken = true;
 				yValue = 7;
 				break;
 			}
 			
 			oldSpace = space;
 			this.invalidate();
 		}
 		return true;
 	}
 	
 	private int findYValue(int col)
 	{
 		for (int i = board.length-1; i >= 0; i--)
 		{
 			if (board[i][col] == -1) //then empty space
 			{
 				return i;
 			}
 		}
 		return -1;
 	}
 	protected Player getPlayer(int id)
 	{
 		if (p0.getID() == id)
 			return p0;
 		else if (p1.getID() == id)
 			return p1;
 		else
 			return null;
 	}
 
 	protected int setSpace(int i)
 	{
 		//then we have tried to put something where its full already
 		if (!isWinner)
 		{
 			if (board[0][i] != -1)
 			{
 				// illegal move
 				return 1;
 				//this.notifyPlayerTurn();
 			}
 			else
 			{
 				oldSpace = i;
 				space = i;
 				dropToken = true;
 				yValue = (float)7.25;
 			}
 		}
 		return 0;
 	}
 
 	protected void notifyPlayerTurn()
 	{
 		this.getPlayer(playerTurn).notifyTurn();
 	}
 	
 	@Override
 	public void onClick(View v)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void resetBoard()
 	{
 		board = new int[6][7];
 		for (int i = 0; i < board.length; i++)
 			for (int j = 0; j < board[0].length; j++)
 				board[i][j] = -1;
 		
 		isWinner = false;
 		placeChecker = -1;
 		space = -1;
 		dropToken = false;
 		isWinner = false;
 		playerTurn = 0;
 	}
 }
