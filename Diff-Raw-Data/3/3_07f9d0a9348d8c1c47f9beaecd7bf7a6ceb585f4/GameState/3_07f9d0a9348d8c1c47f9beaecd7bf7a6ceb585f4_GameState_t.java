 package com.chess.genesis;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 import java.util.Date;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 class GameState
 {
 	public static GameState self;
 
 	private final Context context;
 	private final Bundle settings;
 	private final NetworkClient net;
 	private final ProgressMsg progress;
 	private final ObjectArray<Move> history;
 	private final Board board;
 	private final IntArray callstack;
 	private final int ycol;
 	private final int type;
 
 	private int hindex = -1;
 
 	private final Handler handle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 		try {
 			switch (msg.what) {
 			case NetworkClient.SUBMIT_MOVE:
 				JSONObject json = (JSONObject) msg.obj;
 
 				if (json.getString("result").equals("error")) {
 					progress.remove();
 					Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 					return;
 				}
 				progress.setText(json.getString("reason"));
 
 				net.game_status(settings.getString("gameid"));
 				(new Thread(net)).start();
 				break;
 			case ResignConfirm.MSG:
 				progress.setText("Sending resignation");
 
 				net.resign_game(settings.getString("gameid"));
 				(new Thread(net)).start();
 				break;
 			case NetworkClient.RESIGN_GAME:
 				json = (JSONObject) msg.obj;
 
 				if (json.getString("result").equals("error")) {
 					progress.remove();
 					Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 					return;
 				}
 				progress.setText("Resignation sent");
 
 				net.game_status(settings.getString("gameid"));
 				(new Thread(net)).run();
 				break;
 			case NetworkClient.GAME_STATUS:
 				json = (JSONObject) msg.obj;
 
 				if (json.getString("result").equals("error")) {
 					progress.remove();
 					Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 					return;
 				}
 				final String gameid = json.getString("gameid");
 				final String zfen = json.getString("zfen");
 				final String history = json.getString("history");
 				final int status = Enums.GameStatus(json.getString("status"));
 				final long stime = json.getLong("stime");
 
 				settings.putString("status", String.valueOf(status));
 
 				final GameDataDB db = new GameDataDB(context);
 				db.updateOnlineGame(gameid, status, stime, zfen, history);
 				db.close();
 
 				applyRemoteMove(history);
 				if (status != Enums.ACTIVE) {
 					if (Integer.valueOf(settings.getString("eventtype")) == Enums.INVITE) {
 						progress.remove();
 
 						json.put("yourcolor", ycol);
 						json.put("white_name", settings.getString("white"));
 						json.put("black_name", settings.getString("black"));
 						json.put("eventtype", settings.getString("eventtype"));
 						json.put("status", settings.getString("status"));
 						json.put("gametype", Enums.GameType(Integer.valueOf(settings.getString("gametype"))));
 						json.put("gameid", settings.getString("gameid"));
 
 						(new EndGameDialog(context, json)).show();
 						return;
 					}
 					progress.setText("Retrieving score");
 
 					settings.putString("status", String.valueOf(status));
 					net.game_score(settings.getString("gameid"));
 					(new Thread(net)).start();
				} else {
					progress.setText("Status Synced");
					progress.remove();
 				}
 				break;
 			case NetworkClient.GAME_SCORE:
 				json = (JSONObject) msg.obj;
 
 				if (json.getString("result").equals("error")) {
 					progress.remove();
 					Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 					return;
 				}
 				progress.setText("Score loaded");
 				progress.remove();
 
 				json.put("yourcolor", ycol);
 				json.put("white_name", settings.getString("white"));
 				json.put("black_name", settings.getString("black"));
 				json.put("eventtype", settings.getString("eventtype"));
 				json.put("status", settings.getString("status"));
 				json.put("gametype", Enums.GameType(Integer.valueOf(settings.getString("gametype"))));
 				json.put("gameid", settings.getString("gameid"));
 
 				(new EndGameDialog(context, json)).show();
 				break;
 			case RematchConfirm.MSG:
 				Bundle data = (Bundle) msg.obj;
 				progress.setText("Sending newgame request");
 
 				final String opponent = data.getString("opp_name");
 				String color = Enums.ColorType(data.getInt("color"));
 				String gametype = Enums.GameType(data.getInt("gametype"));
 
 				net.new_game(opponent, gametype, color);
 				(new Thread(net)).start();
 				break;
 			case NetworkClient.NEW_GAME:
 				json = (JSONObject) msg.obj;
 				try {
 					if (json.getString("result").equals("error")) {
 						progress.remove();
 						Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 						return;
 					}
 					progress.setText(json.getString("reason"));
 					progress.remove();
 				} catch (JSONException e) {
 					e.printStackTrace();
 					throw new RuntimeException();
 				}
 				break;
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 		}
 	};
 
 	private void check_endgame()
 	{
 		switch (type) {
 		case Enums.LOCAL_GAME:
 			return;
 		case Enums.ONLINE_GAME:
 			if (Integer.valueOf(settings.getString("status")) == Enums.ACTIVE) {
 				return;
 			} else if (Integer.valueOf(settings.getString("eventtype")) == Enums.INVITE) {
 			try {
 				final JSONObject json = new JSONObject();
 				json.put("yourcolor", ycol);
 				json.put("white_name", settings.getString("white"));
 				json.put("black_name", settings.getString("black"));
 				json.put("eventtype", settings.getString("eventtype"));
 				json.put("status", settings.getString("status"));
 				json.put("gametype", Enums.GameType(Integer.valueOf(settings.getString("gametype"))));
 				json.put("gameid", settings.getString("gameid"));
 
 				(new EndGameDialog(context, json)).show();
 			} catch (JSONException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			}
 			} else {
 				progress.setText("Retrieving score");
 				net.game_score(settings.getString("gameid"));
 				(new Thread(net)).run();
 			}
 			break;
 		case Enums.ARCHIVE_GAME:
 			settings.putInt("yourcolor", ycol);
 			(new GameStatsDialog(context, settings)).show();
 			break;
 		}
 	}
 
 	public GameState(final Context _context, final Bundle _settings)
 	{
 		self = this;
 		context = _context;
 		settings = _settings;
 
 		callstack = new IntArray();
 		history = new ObjectArray<Move>();
 		board = new Board();
 		progress = new ProgressMsg(context);
 
 		type = settings.getInt("type", Enums.ONLINE_GAME);
 		switch (type) {
 		case Enums.LOCAL_GAME:
 		default:
 			net = null;
 			ycol = Piece.WHITE;
 			break;
 		case Enums.ONLINE_GAME:
 			net = new NetworkClient(context, handle);
 			ycol = settings.getString("username").equals(settings.getString("white"))? 1 : -1;
 			break;
 		case Enums.ARCHIVE_GAME:
 			net = null;
 			ycol = settings.getString("username").equals(settings.getString("white"))? 1 : -1;
 			break;
 		}
 
 		final String tmp = settings.getString("history");
 		if (tmp == null || tmp.length() < 3) {
 			setStm();
 			check_endgame();
 			return;
 		}
 		final String[] movehistory = tmp.trim().split(" +");
 
 		for (int i = 0; i < movehistory.length; i++) {
 			final Move move = new Move();
 			move.parse(movehistory[i]);
 
 			if (board.validMove(move) != Board.VALID_MOVE)
 				break;
 			board.make(move);
 			history.push(move);
 			hindex++;
 		}
 		setBoard();
 		check_endgame();
 	}
 
 	private void setBoard()
 	{
 		// set place piece counts
 		final int[] pieces = board.getPieceCounts();
 		for (int i = -6; i < 0; i++) {
 			final PlaceButton button = (PlaceButton) Game.self.findViewById(i + 100);
 			button.setCount(pieces[i + 6]);
 		}
 		for (int i = 1; i < 7; i++) {
 			final PlaceButton button = (PlaceButton) Game.self.findViewById(i + 100);
 			button.setCount(pieces[i + 6]);
 		}
 
 		// set board pieces
 		final int[] squares = board.getBoardArray();
 		for (int i = 0; i < 64; i++) {
 			final BoardButton button = (BoardButton) Game.self.findViewById(i);
 			button.setPiece(squares[i]);
 		}
 		// move caused check
 		if (board.incheck(board.getStm())) {
 			final int king = board.kingIndex(board.getStm());
 			final BoardButton kingI = (BoardButton) Game.self.findViewById(king);
 			kingI.setCheck(true);
 		}
 		setStm();
 	}
 
 	public final void setStm()
 	{
 		String check, wstr, bstr;
 
 		switch (board.isMate()) {
 		case Board.NOT_MATE:
 		default:
 			if (board.incheck(board.getStm()))
 				check = " (check)";
 			else
 				check = "";
 			break;
 		case Board.CHECK_MATE:
 			check = " (checkmate)";
 			break;
 		case Board.STALE_MATE:
 			check = " (stalemate)";
 			break;
 		}
 		if (type == Enums.LOCAL_GAME) {
 			wstr = "White";
 			bstr = "Black";
 		} else {
 			wstr = settings.getString("white");
 			bstr = settings.getString("black");
 		}
 
 		final TextView white = (TextView) Game.self.findViewById(R.id.white_name);
 		final TextView black = (TextView) Game.self.findViewById(R.id.black_name);
 		if (board.getStm() == Piece.WHITE) {
 			white.setText(wstr + check);
 			black.setText(bstr);
 			white.setTypeface(Typeface.DEFAULT_BOLD);
 			black.setTypeface(Typeface.DEFAULT);
 		} else {
 			white.setText(wstr);
 			black.setText(bstr + check);
 			white.setTypeface(Typeface.DEFAULT);
 			black.setTypeface(Typeface.DEFAULT_BOLD);
 		}
 	}
 
 	public void save(final Context context, final boolean exitgame)
 	{
 		switch (type) {
 		case Enums.LOCAL_GAME:
 			final GameDataDB db = new GameDataDB(context);
 			final int id = Integer.valueOf(settings.getString("id"));
 
 			if (history.size() < 1) {
 				db.deleteLocalGame(id);
 				db.close();
 				return;
 			}
 			if (exitgame) {
 				db.close();
 				return;
 			}
 			final long stime = (new Date()).getTime();
 			final String zfen = board.getPosition().printZfen();
 			final String hist = history.toString();
 
 			db.saveLocalGame(id, stime, zfen, hist);
 			db.close();
 			break;
 		case Enums.ONLINE_GAME:
 			if (exitgame)
 				return;
 			Game.self.displaySubmitMove();
 		case Enums.ARCHIVE_GAME:
 			break;
 		}
 	}
 
 	public void resign()
 	{
 		(new ResignConfirm(context, handle)).show();
 	}
 
 	public void rematch()
 	{
 		final String opp = settings.getString("username").equals(settings.getString("white"))?
 			settings.getString("black") : settings.getString("white");
 		(new RematchConfirm(context, handle, opp)).show();
 	}
 
 	public void resync()
 	{
 		progress.setText("Updating game state");
 		net.game_status(settings.getString("gameid"));
 		(new Thread(net)).run();
 	}
 
 	public void applyRemoteMove(final String hist)
 	{
 		if (hist == null || hist.length() < 3)
 			return;
 
 		final String[] movehistory = hist.trim().split(" +");
 		if (movehistory[movehistory.length - 1].equals(history.top().toString()))
 			return;
 
 		// must be on most current move to apply it
 		currentMove();
 		Toast.makeText(context, "New move loaded...", Toast.LENGTH_LONG).show();
 
 		final Move move = new Move();
 		move.parse(movehistory[movehistory.length - 1]);
 		if (board.validMove(move) != Board.VALID_MOVE)
 			return;
 		applyMove(move, true, false);
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
 		final Move move = history.get(hindex);
 		revertMove(move);
 	}
 
 	public void forwardMove()
 	{
 		if (hindex + 1 >= history.size())
 			return;
 		final Move move = history.get(++hindex);
 		applyMove(move, false, true);
 	}
 
 	public void currentMove()
 	{
 		while (hindex + 1 < history.size()) {
 			final Move move = history.get(++hindex);
 			applyMove(move, false, true);
 		}
 	}
 
 	public void firstMove()
 	{
 		while (hindex > 0) {
 			final Move move = history.get(hindex);
 			revertMove(move);
 		}
 	}
 
 	public void undoMove()
 	{
 		if (hindex < 0)
 			return;
 		final Move move = history.get(hindex);
 		revertMove(move);
 		history.pop();
 	}
 
 	public void submitMove()
 	{
 		progress.setText("Sending move");
 
 		final String gameid = settings.getString("gameid");
 		final String move = history.top().toString();
 
 		net.submit_move(gameid, move);
 		(new Thread(net)).run();
 	}
 
 	private void handleMove()
 	{
 		switch (type) {
 		case Enums.ONLINE_GAME:
 			// you can't edit the past in online games
 			if (hindex + 1 < history.size()) {
 				callstack.pop();
 				return;
 			}
 			break;
 		case Enums.ARCHIVE_GAME:
 			return;
 		}
 
 		final Move move = new Move();
 
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
 		applyMove(move, true, true);
 	}
 
 	private void applyMove(final Move move, final boolean erase, final boolean localmove)
 	{
 		// legal move always ends with king not in check
 		if (hindex > 1) {
 			final int king = board.kingIndex(board.getStm());
 			final BoardButton kingI = (BoardButton) Game.self.findViewById(king);
 			kingI.setCheck(false);
 		}
 
 		if (move.from == Piece.PLACEABLE) {
 			final PlaceButton from = (PlaceButton) Game.self.findViewById(Board.pieceType[move.index] + 100);
 			final BoardButton to = (BoardButton) Game.self.findViewById(move.to);
 
 			from.setHighlight(false);
 			from.minusPiece();
 			to.setPiece(from.getPiece());
 		} else {
 			final BoardButton from = (BoardButton) Game.self.findViewById(move.from);
 			final BoardButton to = (BoardButton) Game.self.findViewById(move.to);
 
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
 			if (localmove)
 				save(Game.self.game_board.getContext(), false);
 		}
 
 		// move caused check
 		if (board.incheck(board.getStm())) {
 			final int king = board.kingIndex(board.getStm());
 			final BoardButton kingI = (BoardButton) Game.self.findViewById(king);
 			kingI.setCheck(true);
 		}
 		setStm();
 	}
 
 	private void revertMove(final Move move)
 	{
 		// legal move always ends with king not in check
 		if (hindex > 1) {
 			final int king = board.kingIndex(board.getStm());
 			final BoardButton kingI = (BoardButton) Game.self.findViewById(king);
 			kingI.setCheck(false);
 		}
 
 		if (move.from == Piece.PLACEABLE) {
 			final PlaceButton from = (PlaceButton) Game.self.findViewById(Board.pieceType[move.index] + 100);
 			final BoardButton to = (BoardButton) Game.self.findViewById(move.to);
 
 			to.setPiece(0);
 			from.plusPiece();
 		} else {
 			final BoardButton from = (BoardButton) Game.self.findViewById(move.from);
 			final BoardButton to = (BoardButton) Game.self.findViewById(move.to);
 
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
 			final int king = board.kingIndex(board.getStm());
 			final BoardButton kingI = (BoardButton) Game.self.findViewById(king);
 			kingI.setCheck(true);
 		}
 		setStm();
 	}
 
 	public void boardClick(final View v)
 	{
 		final BoardButton to = (BoardButton) v;
 		final int index = to.getIndex();
 		final int col = (type == Enums.ONLINE_GAME)? ycol : board.getStm();
 
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
 		} else if (callstack.get(0) > 64) {
 		// Place piece action
 			// can't place on another piece
 			if (to.getPiece() * col < 0) {
 				return;
 			} else if (to.getPiece() * col > 0) {
 				final PlaceButton from = (PlaceButton) Game.self.findViewById(callstack.get(0));
 				from.setHighlight(false);
 				to.setHighlight(true);
 				callstack.set(0, index);
 				return;
 			}
 		} else {
 		// piece move action
 			final BoardButton from = (BoardButton) Game.self.findViewById(callstack.get(0));
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
 		final PlaceButton from = (PlaceButton) v;
 		final int col = (type == Enums.ONLINE_GAME)? ycol : board.getStm();
 		final int ptype = from.getPiece();
 
 		// only select your own pieces where count > 0
 		if (board.getStm() != col || ptype * board.getStm() < 0 || from.getCount() <= 0)
 			return;
 		if (callstack.size() == 0) {
 		// No active clicks
 			callstack.push(ptype + 100);
 			from.setHighlight(true);
 		} else if (callstack.get(0) < 64) {
 		// switching from board to place piece
 			final BoardButton to = (BoardButton) Game.self.findViewById(callstack.get(0));
 			to.setHighlight(false);
 			callstack.set(0, ptype + 100);
 			from.setHighlight(true);
 		} else if (callstack.get(0) == ptype + 100) {
 		// clicking the same square
 			callstack.clear();
 			from.setHighlight(false);
 		} else {
 		// switching to another place piece
 			final PlaceButton fromold = (PlaceButton) Game.self.findViewById(callstack.get(0));
 			fromold.setHighlight(false);
 			callstack.set(0, ptype + 100);
 			from.setHighlight(true);
 		}
 		Game.self.game_board.flip();
 	}
 }
