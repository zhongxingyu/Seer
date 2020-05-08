 package com.chess.genesis;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.widget.Toast;
 import java.util.Date;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 class GameState
 {
 	public static GameState self;
 
 	private int hindex = -1;
 	private int ycol = 5;
 	private boolean isOnline = false;
 
 	private IntArray callstack;
 	private Board board;
 	private ObjectArray<Move> history;
 	private Bundle settings;
 	private NetworkClient net;
 
 	private Context context;
 
 	private Handler handle = new Handler()
 	{
 		public void handleMessage(Message msg)
 		{
 			switch (msg.what) {
 			case NetworkClient.SUBMIT_MOVE:
 				JSONObject json = (JSONObject) msg.obj;
 				try {
 					if (json.getString("result").equals("error")) {
 						Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 						return;
 					}
 					Toast.makeText(context, json.getString("reason"), Toast.LENGTH_LONG).show();
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 				break;
 			}
 		}
 	};
 
 	public GameState(Context _context, Bundle _settings)
 	{
 		self = this;
 		context = _context;
 		settings = _settings;
 
 		callstack = new IntArray();
 		history = new ObjectArray<Move>();
 		board = new Board();
 
 		if (settings.getInt("type", Enums.ONLINE_GAME) == Enums.ONLINE_GAME) {
 			isOnline = true;
 			ycol = settings.getString("username").equals(settings.getString("white"))? 1:-1;
 			net = new NetworkClient(handle);
 		}
 
 		String tmp = settings.getString("history");
		if (tmp == null) {
 			setStm();
 			return;
 		}
 		String[] movehistory = tmp.trim().split(" +");
 
 		for (int i = 0; i < movehistory.length; i++) {
 			Move move = new Move();
 			move.parse(movehistory[i]);
 
 			if (board.validMove(move) != Board.VALID_MOVE)
 				break;
 			board.make(move);
 			history.push(move);
 			hindex++;
 		}
 		setBoard();
 	}
 
 	private void setBoard()
 	{
 		// set place piece counts
 		int[] pieces = board.getPieceCounts();
 		for (int i = -6; i < 0; i++) {
 			PlaceButton button = (PlaceButton) Game.self.findViewById(i + 100);
 			button.setCount(pieces[i + 6]);
 		}
 		for (int i = 1; i < 7; i++) {
 			PlaceButton button = (PlaceButton) Game.self.findViewById(i + 100);
 			button.setCount(pieces[i + 6]);
 		}
 
 		// set board pieces
 		int[] squares = board.getBoardArray();
 		for (int i = 0; i < 64; i++) {
 			BoardButton button = (BoardButton) Game.self.findViewById(i);
 			button.setPiece(squares[i]);
 		}
 		// move caused check
 		if (board.incheck(board.getStm())) {
 			int king = board.kingIndex(board.getStm());
 			BoardButton kingI = (BoardButton) Game.self.findViewById(king);
 			kingI.setCheck(true);
 		}
 		setStm();
 	}
 
 	private void setStm()
 	{
 		String check = " ", stm;
 
 		stm = (board.getStm() > 0)? "White's Turn" : "Black's Turn";
 		switch (board.isMate()) {
 		case Board.NOT_MATE:
 			if (board.incheck(board.getStm()))
 				check = " (check)";
 			break;
 		case Board.CHECK_MATE:
 			check = " (checkmate)";
 			break;
 		case Board.STALE_MATE:
 			check = " (stalemate)";
 			break;
 		}
 		Game.self.stm_txt.setText(stm + check);
 	}
 
 	public void save(Context context, boolean exitgame)
 	{
 		if (isOnline) {
 			if (exitgame)
 				return;
 			String username = settings.getString("username");
 			String gameid = settings.getString("gameid");
 			String move = history.top().toString();
 
 			net.submit_move(username, gameid, move);
 			(new Thread(net)).run();
 		} else {
 			GameDataDB db = new GameDataDB(context);
 			int id = Integer.valueOf(settings.getString("id"));
 
 			if (history.size() < 1) {
 				db.deleteLocalGame(id);
 				db.close();
 				return;
 			}
 			if (exitgame) {
 				db.close();
 				return;
 			}
 			long stime = (new Date()).getTime();
 			String zfen = board.getPosition().printZfen();
 			String hist = history.toString();
 
 			db.saveLocalGame(id, stime, zfen, hist);
 			db.close();
 		}
 	}
 
 	public void reset()
 	{
 		hindex = -1;
 		callstack.clear();
 		history.clear();
 		board.reset();
 		Game.self.reset();
 	}
 
 	public void backMove()
 	{
 		if (hindex < 0)
 			return;
 		Move move = history.get(hindex);
 		revertMove(move);
 	}
 
 	public void forwardMove()
 	{
 		if (hindex + 1 >= history.size())
 			return;
 		Move move = history.get(++hindex);
 		applyMove(move, false);
 	}
 	
 	private void handleMove()
 	{
 		if (isOnline) {
 			// you can't edit the past in online games
 			if (hindex + 1 < history.size()) {
 				callstack.pop();
 				return;
 			}
 		}
 
 		Move move = new Move();
 
 		// create move
 		if (callstack.get(0) > 64) {
 			move.index = Math.abs(callstack.get(0) - 100);
 			move.from = Piece.PLACEABLE;
 		} else {
 			move.from = callstack.get(0);
 		}
 		move.to = callstack.get(1);
 
 		// return if move isn't valid
 		if (board.validMove(move) != Board.VALID_MOVE) {
 			callstack.pop();
 			return;
 		}
 		callstack.clear();
 		applyMove(move, true);
 	}
 
 	private void applyMove(Move move, boolean erase)
 	{
 		// legal move always ends with king not in check
 		if (hindex > 1) {
 			int king = board.kingIndex(board.getStm());
 			BoardButton kingI = (BoardButton) Game.self.findViewById(king);
 			kingI.setCheck(false);
 		}
 
 		if (move.from == Piece.PLACEABLE) {
 			PlaceButton from = (PlaceButton) Game.self.findViewById(Board.pieceType[move.index] + 100);
 			BoardButton to = (BoardButton) Game.self.findViewById(move.to);
 
 			from.setHighlight(false);
 			from.minusPiece();
 			to.setPiece(from.getPiece());
 		} else {
 			BoardButton from = (BoardButton) Game.self.findViewById(move.from);
 			BoardButton to = (BoardButton) Game.self.findViewById(move.to);
 
 			to.setPiece(from.getPiece());
 			from.setPiece(0);
 			from.setHighlight(false);
 		}
 
 		// apply move to board
 		board.make(move);
 		// update hindex, history
 		if (erase) {
 			hindex++;
 			if (hindex < history.size())
 				history.resize(hindex);
 			history.push(move);
 			save(Game.self.game_board.getContext(), false);
 		}
 
 		// move caused check
 		if (board.incheck(board.getStm())) {
 			int king = board.kingIndex(board.getStm());
 			BoardButton kingI = (BoardButton) Game.self.findViewById(king);
 			kingI.setCheck(true);
 		}
 		setStm();
 	}
 
 	private void revertMove(Move move)
 	{
 		// legal move always ends with king not in check
 		if (hindex > 1) {
 			int king = board.kingIndex(board.getStm());
 			BoardButton kingI = (BoardButton) Game.self.findViewById(king);
 			kingI.setCheck(false);
 		}
 
 		if (move.from == Piece.PLACEABLE) {
 			PlaceButton from = (PlaceButton) Game.self.findViewById(Board.pieceType[move.index] + 100);
 			BoardButton to = (BoardButton) Game.self.findViewById(move.to);
 
 			to.setPiece(0);
 			from.plusPiece();
 		} else {
 			BoardButton from = (BoardButton) Game.self.findViewById(move.from);
 			BoardButton to = (BoardButton) Game.self.findViewById(move.to);
 
 			from.setPiece(to.getPiece());
 
 			if (move.xindex == Piece.NONE)
 				to.setPiece(0);
 			else
 				to.setPiece(Board.pieceType[move.xindex]);
 		}
 		hindex--;
 		board.unmake(move);
 
 		// move caused check
 		if (board.incheck(board.getStm())) {
 			int king = board.kingIndex(board.getStm());
 			BoardButton kingI = (BoardButton) Game.self.findViewById(king);
 			kingI.setCheck(true);
 		}
 		setStm();
 	}
 
 	public void boardClick(View v)
 	{
 		BoardButton to = (BoardButton) v;
 		int index = to.getIndex();
 
 		if (callstack.size() == 0) {
 		// No active clicks
 			int col = isOnline? ycol : board.getStm();
 			// first click must be non empty and your own
 			if (to.getPiece() == 0 || to.getPiece() * board.getStm() < 0 || col != board.getStm())
 				return;
 			callstack.push(index);
 			to.setHighlight(true);
 			return;
 		} else if (callstack.get(0) == index) {
 		// clicking the same square
 			callstack.clear();
 			to.setHighlight(false);
 			return;
 		} else if (callstack.get(0) > 64) {
 		// Place piece action
 			// can't place on another piece
 			if (to.getPiece() != 0)
 				return;
 		} else {
 		// piece move action
 			BoardButton from = (BoardButton) Game.self.findViewById(callstack.get(0));
 			// capturing your own piece
 			if (from.getPiece() * to.getPiece() > 0)
 				return;
 		}
 		callstack.push(index);
 		handleMove();
 	}
 
 	public void placeClick(View v)
 	{
 		PlaceButton from = (PlaceButton) v;
 		int col = isOnline? ycol : board.getStm();
 		int type = from.getPiece();
 
 		// only select your own pieces where count > 0
 		if (board.getStm() != col || type * board.getStm() < 0 || from.getCount() <= 0)
 			return;
 		if (callstack.size() == 0) {
 		// No active clicks
 			callstack.push(type + 100);
 		} else if (callstack.get(0) < 64) {
 		// switching from board to place piece
 			BoardButton to = (BoardButton) Game.self.findViewById(callstack.get(0));
 			to.setHighlight(false);
 			callstack.set(0, type + 100);
 		} else if (callstack.get(0) == type + 100) {
 		// clicking the same square
 			callstack.clear();
 			from.setHighlight(false);
 			return;
 		} else {
 		// switching to another place piece
 			PlaceButton fromold = (PlaceButton) Game.self.findViewById(callstack.get(0));
 			fromold.setHighlight(false);
 			callstack.set(0, type + 100);
 			from.setHighlight(true);
 			return;
 		}
 		from.setHighlight(true);
 		Game.self.game_board.flip();
 	}
 }
