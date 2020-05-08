 /*	GenesisChess, an Android chess application
 	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)
 
 	Licensed under the Apache License, Version 2.0 (the "License");
 	you may not use this file except in compliance with the License.
 	You may obtain a copy of the License at
 
 	http://apache.org/licenses/LICENSE-2.0
 
 	Unless required by applicable law or agreed to in writing, software
 	distributed under the License is distributed on an "AS IS" BASIS,
 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 	See the License for the specific language governing permissions and
 	limitations under the License.
 */
 
 package com.chess.genesis;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.Toast;
 
 class RegGameState extends GameState
 {
 	private final ObjectArray<MoveFlags> flagsHistory;
 
 	private final Handler xhandle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 			switch (msg.what) {
 			case RegEngine.MSG:
 				final Bundle bundle = (Bundle) msg.obj;
 
 				if (bundle.getLong("time") == 0) {
 					cpu.setBoard((RegBoard) board);
 					(new Thread(cpu)).start();
 					return;
 				}
 				currentMove();
 
 				final RegMove tmove = bundle.getParcelable("move");
 				final RegMove move = new RegMove();
 				if (board.validMove(tmove, move))
 					applyMove(move, true, true);
 				break;
 			default:
 				handleOther(msg);
 				break;
 			}
 		}
 	};
 
 	public RegGameState(final Activity _activity, final GameFrag _game, final Bundle _settings)
 	{
 		activity = _activity;
 		game = _game;
 		settings = _settings;
 		handle = xhandle;
 
 		callstack = new IntArray();
 		flagsHistory = new ObjectArray<MoveFlags>();
 		history = new ObjectArray<Move>();
 		board = new RegBoard();
 		progress = new ProgressMsg(activity);
 
 		type = settings.getInt("type", Enums.ONLINE_GAME);
 		switch (type) {
 		case Enums.LOCAL_GAME:
 		default:
 			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
 			cpu = new RegEngine(handle);
 			cpu.setTime(pref.getInt("cputime", cpu.getTime()));
 			oppType = Integer.valueOf(settings.getString("opponent"));
 			net = null;
 			ycol = (oppType == Enums.CPU_WHITE_OPPONENT)? Piece.BLACK : Piece.WHITE;
 			break;
 		case Enums.ONLINE_GAME:
 		case Enums.ARCHIVE_GAME:
 			oppType = Enums.HUMAN_OPPONENT;
 			cpu = null;
 			net = new NetworkClient(activity, handle);
 			ycol = settings.getString("username").equals(settings.getString("white"))? Piece.WHITE : Piece.BLACK;
 			break;
 		}
 
 		final String tmp = settings.getString("history");
 		if (tmp == null || tmp.length() < 3) {
 			check_endgame();
 			return;
 		}
 		final String[] movehistory = tmp.trim().split(" +");
 
 		for (int i = 0; i < movehistory.length; i++) {
 			final RegMove move = new RegMove();
 			move.parse(movehistory[i]);
 
 			if (board.validMove(move) != Move.VALID_MOVE)
 				break;
 			flagsHistory.push(board.getMoveFlags());
 			history.push(move);
 			board.make(move);
 			hindex++;
 		}
 		check_endgame();
 	}
 
 	public void setBoard()
 	{
 		// set dead piece counts
 		setBoard(board.getPieceCounts(Piece.DEAD));
 	}
 
 	protected boolean runCPU()
 	{
 		// Start computer player
 		if (oppType == Enums.HUMAN_OPPONENT)
 			return false;
 		else if (hindex + 1 < history.size())
 			return false;
 		else if (board.getStm() == ycol)
 			return false;
 
 		if (cpu.isActive()) {
 			cpu.stop();
 			return true;
 		}
 		cpu.setBoard((RegBoard) board);
 		(new Thread(cpu)).start();
 		return true;
 	}
 
 	protected void applyRemoteMove(final String hist)
 	{
 		if (hist == null || hist.length() < 3)
 			return;
 
 		final String[] movehistory = hist.trim().split(" +");
 		if (movehistory[movehistory.length - 1].equals(history.top().toString()))
 			return;
 
 		// must be on most current move to apply it
 		currentMove();
 		Toast.makeText(activity, "New move loaded...", Toast.LENGTH_LONG).show();
 
 		final RegMove move = new RegMove();
 		move.parse(movehistory[movehistory.length - 1]);
 		if (board.validMove(move) != Move.VALID_MOVE)
 			return;
 		applyMove(move, true, false);
 	}
 
 	public void reset()
 	{
 		super.reset();
 
 		history.clear();
 		flagsHistory.clear();
 		board.reset();
 	}
 
 	public void backMove()
 	{
 		if (hindex < 0)
 			return;
 		final RegMove move = (RegMove) history.get(hindex);
 		revertMove(move);
 	}
 
 	public void forwardMove()
 	{
 		if (hindex + 1 >= history.size())
 			return;
 		final RegMove move = (RegMove) history.get(hindex + 1);
 		applyMove(move, false, true);
 	}
 
 	public void currentMove()
 	{
 		while (hindex + 1 < history.size()) {
 			final RegMove move = (RegMove) history.get(hindex + 1);
 			applyMove(move, false, true);
 		}
 	}
 
 	public void firstMove()
 	{
 		while (hindex > 0) {
 			final RegMove move = (RegMove) history.get(hindex);
 			revertMove(move);
 		}
 	}
 
 	public void undoMove()
 	{
 		if (hindex < 0)
 			return;
 		final RegMove move = (RegMove) history.get(hindex);
 		revertMove(move);
 		history.pop();
 		flagsHistory.pop();
 	}
 
 	private void handleMove()
 	{
 		if (type == Enums.ONLINE_GAME) {
 			// you can't edit the past in online games
 			if (hindex + 1 < history.size()) {
 				callstack.pop();
 				return;
 			}
 		} else if (type == Enums.ARCHIVE_GAME) {
 			return;
 		}
 
 		final RegMove move = new RegMove();
 
 		move.from = callstack.get(0);
 		move.to = callstack.get(1);
 
 		// return if move isn't valid
 		if (board.validMove(move) != Move.VALID_MOVE) {
 			callstack.pop();
 			return;
 		}
 		callstack.clear();
 		applyMove(move, true, true);
 	}
 
 	private void applyMove(final RegMove move, final boolean erase, final boolean localmove)
 	{
 		if (hindex >= 0) {
 			// undo last move highlight
 			final BoardButton to = (BoardButton) activity.findViewById(history.get(hindex).to);
 			to.setLast(false);
 
 			if (hindex > 1) {
 				// legal move always ends with king not in check
 				final int king = board.kingIndex(board.getStm());
 				final BoardButton kingI = (BoardButton) activity.findViewById(king);
 				kingI.setCheck(false);
 			}
 		}
 
 		final BoardButton from = (BoardButton) activity.findViewById(move.from);
 		final BoardButton to = (BoardButton) activity.findViewById(move.to);
 
 		to.setPiece(from.getPiece());
 		to.setLast(true);
 		from.setPiece(0);
 		from.setHighlight(false);
 
 		if (move.xindex != Piece.NONE) {
 			final PlaceButton piece = (PlaceButton) activity.findViewById(board.PieceType(move.xindex) + 1000);
 			piece.plusPiece();
 		}
 
 		if (move.getCastle() != 0) {
 			final boolean left = (move.getCastle() == Move.CASTLE_QS);
 			final int castleTo = move.to + (left? 1 : -1),
 				castleFrom = (left? 0:7) + ((board.getStm() == Piece.WHITE)? Piece.A1 : Piece.A8);
 
 			BoardButton castle = (BoardButton) activity.findViewById(castleFrom);
 			castle.setPiece(Piece.EMPTY);
 			castle = (BoardButton) activity.findViewById(castleTo);
 			castle.setPiece(Piece.ROOK * board.getStm());
 		} else if (move.getPromote() != 0) {
 			final BoardButton pawn = (BoardButton) activity.findViewById(move.to);
			pawn.setPiece(move.getPromote() * board.getStm());
 		} else if (move.getEnPassant()) {
 			final BoardButton pawn = (BoardButton) activity.findViewById(board.Piece(move.xindex));
 			pawn.setPiece(Piece.EMPTY);
 		}
 		// get copy of board flags
 		final MoveFlags flags = board.getMoveFlags();
 
 		// apply move to board
 		board.make(move);
 
 		// update hindex, history
 		hindex++;
 		if (erase) {
 			if (hindex < history.size()) {
 				history.resize(hindex);
 				flagsHistory.resize(hindex);
 			}
 			history.push(move);
 			flagsHistory.push(flags);
 			if (localmove)
 				save(activity, false);
 		}
 
 		// move caused check
 		if (board.incheck(board.getStm())) {
 			final int king = board.kingIndex(board.getStm());
 			final BoardButton kingI = (BoardButton) activity.findViewById(king);
 			kingI.setCheck(true);
 		}
 		setStm();
 	}
 
 	private void revertMove(final RegMove move)
 	{
 		// legal move always ends with king not in check
 		if (hindex > 1) {
 			final int king = board.kingIndex(board.getStm());
 			final BoardButton kingI = (BoardButton) activity.findViewById(king);
 			kingI.setCheck(false);
 		}
 
 		final BoardButton from = (BoardButton) activity.findViewById(move.from);
 		final BoardButton to = (BoardButton) activity.findViewById(move.to);
 
 		from.setPiece(to.getPiece());
 		to.setLast(false);
 
 		if (move.xindex == Piece.NONE) {
 			to.setPiece(Piece.EMPTY);
 		} else if (move.getEnPassant()) {
 			final int loc = move.to + ((move.to - move.from > 0)? -16 : 16);
 			final BoardButton pawn = (BoardButton) activity.findViewById(loc);
 			pawn.setPiece(Piece.PAWN * board.getStm());
 			to.setPiece(Piece.EMPTY);
 		} else {
 			to.setPiece(board.PieceType(move.xindex));
 		}
 
 		if (move.xindex != Piece.NONE) {
 			final PlaceButton piece = (PlaceButton) activity.findViewById(board.PieceType(move.xindex) + 1000);
 			piece.minusPiece();
 		}
 
 		if (move.getCastle() != 0) {
 			final boolean left = (move.getCastle() == Move.CASTLE_QS);
 			final int castleTo = move.to + (left? 1 : -1),
 				castleFrom = (left? 0:7) + ((board.getStm() == Piece.BLACK)? Piece.A1 : Piece.A8);
 			
 			BoardButton castle = (BoardButton) activity.findViewById(castleFrom);
 			castle.setPiece(Piece.ROOK * -board.getStm());
 			castle = (BoardButton) activity.findViewById(castleTo);
 			castle.setPiece(Piece.EMPTY);
 		} else if (move.getPromote() != 0) {
 			final BoardButton pawn = (BoardButton) activity.findViewById(move.from);
 			pawn.setPiece(Piece.PAWN * -board.getStm());
 		}
 
 		board.unmake(move, flagsHistory.get(hindex));
 		hindex--;
 
 		if (hindex >= 0) {
 			// redo last move highlight
 			final BoardButton hto = (BoardButton) activity.findViewById(history.get(hindex).to);
 			hto.setLast(true);
 		}
 		// move caused check
 		if (board.incheck(board.getStm())) {
 			final int king = board.kingIndex(board.getStm());
 			final BoardButton kingI = (BoardButton) activity.findViewById(king);
 			kingI.setCheck(true);
 		}
 		setStm();
 	}
 
 	public void boardClick(final View v)
 	{
 		final BoardButton to = (BoardButton) v;
 		final int index = to.getIndex();
 		final int col = yourColor();
 
 		if (callstack.size() == 0) {
 		// No active clicks
 			// first click must be non empty and your own
 			if (to.getPiece() * col <= 0)
 				return;
 			callstack.push(index);
 			to.setHighlight(true);
 			return;
 		} else if (callstack.get(0) == index) {
 		// clicking the same square
 			callstack.clear();
 			to.setHighlight(false);
 			return;
 		} else {
 		// piece move action
 			final BoardButton from = (BoardButton) activity.findViewById(callstack.get(0));
 			// capturing your own piece (switch to piece)
 			if (from.getPiece() * to.getPiece() > 0) {
 				from.setHighlight(false);
 				to.setHighlight(true);
 				callstack.set(0, index);
 				return;
 			}
 		}
 		callstack.push(index);
 		handleMove();
 	}
 
 	public void placeClick(final View v)
 	{
 		// Required because GameState calls this function
 	}
 }
