 package org.vxp7755_nxz3937.freeforall;
 
 import android.os.Message;
 import android.util.Log;
 
 public abstract class PieceThread extends Thread {
 	protected int _id;
 	protected int _prevX;
 	protected int _prevY;
 	protected int _x;
 	protected int _y;
 	protected int _team;
 	private boolean _alive;
 	private ControllerThread _ctrlr;
 	
 	PieceThread( int id, int x, int y, int team, ControllerThread ctrlr )
 	{
 		this._id    = id;
 		this._x     = x;
 		this._y     = y;
 		this._prevX = -1;
 		this._prevY = -1;
 		this._team  = team;
 		this._alive = true;
 		this._ctrlr = ctrlr;
 	}
 	
 	/**
 	 * Set the _alive boolean to false, which will cause run()'s while loop to end and the thread finish
 	 */
 	public void eaten()
 	{
 		_alive = false;
 	}
 	
 	/**
 	 * Retrieve the team number of this piece
 	 * 
 	 * @return team number (1-4)
 	 */
 	public int getTeam()
 	{
 		return _team;
 	}
 	
 	/**
 	 * Retrieve the x position of this piece
 	 * 
 	 * @return x position
 	 */
 	public int getX()
 	{
 		return _x;
 	}
 	
 	/**
 	 * Retrieve the y position of this piece
 	 * 
 	 * @return y position
 	 */
 	public int getY()
 	{
 		return _y;
 	}
 	
 	/**
 	 * Retrieve the previous x position of this piece
 	 * 
 	 * @return x position
 	 */
 	public int getPrevX()
 	{
 		return _prevX;
 	}
 	
 	/**
 	 * Retrieve the previous y position of this piece
 	 * 
 	 * @return y position
 	 */
 	public int getPrevY()
 	{
 		return _prevY;
 	}
 	
 	@Override
 	public void run()
 	{
 		Message boardMsg;
 		while( _alive )
 		{
 			// Enter PieceQ, this should be blocking
 			_ctrlr.enqueueMe(this);
 			
 			Log.i("PieceThread", String.format("Piece %d has left the pieceQ", _id));
 			
			while (_ctrlr.isDrawing());
 			
 			if (!_alive)
 				break;
 			
 			Log.i("PieceThread", String.format("Piece %d (team %d) is moving", _id, _team));
 			
 			getNextMove();
 			
 			// Tell the controller I moved
 			boardMsg      = _ctrlr.boardHandler.obtainMessage();
 			MoveObject mv = new MoveObject( _x, _y, this );
 			
 			boardMsg.what = _ctrlr.MSGTYPE_MOVER;
 			boardMsg.obj  = mv;
 			_ctrlr.boardHandler.sendMessage( boardMsg );
 			
 			try {
 				Log.i("PieceThread", String.format("Piece %d (team %d) sleeping", _id, _team));
 				sleep( _ctrlr.getMoveDelay() );
 				Log.i("PieceThread", String.format("Piece %d (team %d) waking up", _id, _team));
 			} catch (InterruptedException e) {
 				Log.e( "PieceThread", "sleep interrupted" );
 				e.printStackTrace();
 			}
 		}
 		
 		Log.i("PieceThread", String.format("Piece %d (team %d) dying", _id, _team));
 	}
 	
 	/**
 	 * Retrieve the id of this PieceThread
 	 * 
 	 * @return the id
 	 */
 	public int getID(){ return _id; }
 	
 	/**
 	 * Perform a specified move (only modifies this piece's internal position)
 	 */
 	protected abstract void getNextMove();
 	
 	/**
 	 * If the move pushed us off the board, loop around to the other side of the board, else do nothing
 	 */
 	protected void ensureMoveOnBoard()
 	{
 		if( _ctrlr == null )
 			Log.e("PieceThread", "Controller is null");
 		else if( _ctrlr.getBoard() == null )
 			Log.e("PieceThread", "Board is null");
 		
 		int boardSize[] = _ctrlr.getBoard().getSize(); 
 		
 		if( _x < 0 )
 			_x = boardSize[1] - 1;
 		else if( _x >= boardSize[1] )
 			_x = 0;
 		
 		if( _y < 0 )
 			_y = boardSize[0] - 1;
 		else if( _y >= boardSize[0] )
 			_y = 0;
 	}
 }
