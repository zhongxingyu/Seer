 package edu.selu.android.classygames;
 
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Build;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewTreeObserver;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import edu.selu.android.classygames.games.Coordinate;
 import edu.selu.android.classygames.games.Position;
 import edu.selu.android.classygames.games.checkers.Board;
 import edu.selu.android.classygames.games.checkers.Piece;
 import edu.selu.android.classygames.utilities.Utilities;
 
 
 public class CheckersGameFragment extends GenericGameFragment
 {
 
 
 	private final static String LOG_TAG = Utilities.LOG_TAG + " - CheckersGameFragment";
 
 
 	/**
 	 * Bitmap representing the player's normal piece.
 	 */
 	private BitmapDrawable playerNormal;
 
 
 	/**
 	 * Bitmap representing the player's king piece.
 	 */
 	private BitmapDrawable playerKing;
 
 
 	/**
 	 * Bitmap representing the opponent's normal piece.
 	 */
 	private BitmapDrawable opponentNormal;
 
 
 	/**
 	 * Bitmap representing the opponent's king piece.
 	 */
 	private BitmapDrawable opponentKing;
 
 
 
 
 	@Override
 	protected void onCreateView()
 	{
 
 	}
 
 
 	@Override
 	protected void buildTeam(final JSONArray team, final byte whichTeam)
 	{
 		if (board == null)
 		{
 			board = new Board();
 		}
 
 		for (int i = 0; i < team.length(); ++i)
 		{
 			try
 			{
 				final JSONObject piece = team.getJSONObject(i);
 				final JSONArray coordinates = piece.getJSONArray("coordinate");
 
 				if (coordinates.length() == 2)
 				{
 					final Coordinate coordinate = new Coordinate(coordinates.getInt(0), coordinates.getInt(1));
 
 					if (board.isPositionValid(coordinate))
 					{
 						final int type = piece.getInt("type");
 						board.getPosition(coordinate).setPiece(new Piece(whichTeam, type));
 					}
 					else
 					{
 						Log.e(LOG_TAG, "Coordinate outside proper range: " + coordinate + ".");
 					}
 				}
 				else
 				{
 					Log.e(LOG_TAG, "A piece had an improper number of coordinate values.");
 				}
 			}
 			catch (final JSONException e)
 			{
 				Log.e(LOG_TAG, "A team's piece was massively malformed.");
 			}
 		}
 	}
 
 
 	@Override
 	protected JSONObject createJSONPiece(final byte whichTeam, final Position position) throws JSONException
 	{
 		JSONObject JSONPiece = null;
 
 		if (position.hasPiece())
 		{
 			final Piece piece = (Piece) position.getPiece();
 
 			if (piece.isTeam(whichTeam))
 			{
 				final Coordinate coordinate = position.getCoordinate();
 				final JSONArray JSONCoordinate = new JSONArray();
 				JSONCoordinate.put(coordinate.getX());
 				JSONCoordinate.put(coordinate.getY());
 
 				JSONPiece = new JSONObject();
 				JSONPiece.put("coordinate", JSONCoordinate);
 				JSONPiece.put("type", piece.getType());
 			}
 		}
 
 		return JSONPiece;
 	}
 
 
 	@Override
 	protected void flush(final Position position)
 	{
 		final String tag = createTag(position.getCoordinate());
 		final Piece piece = (Piece) position.getPiece();
 		final ImageButton imageButton = (ImageButton) getView().findViewWithTag(tag);
 
 		switch (piece.getType())
 		{
 			case Piece.TYPE_NORMAL:
 				if (piece.isTeamPlayer())
 					imageButton.setImageDrawable(playerNormal);
 				else
 					imageButton.setImageDrawable(opponentNormal);
 				break;
 
 			case Piece.TYPE_KING:
 				if (piece.isTeamPlayer())
 					imageButton.setImageDrawable(playerKing);
 				else
 					imageButton.setImageDrawable(opponentKing);
 				break;
 		}
 	}
 
 
 	@Override
 	protected int getGameView()
 	{
 		return R.layout.checkers_game_fragment;
 	}
 
 
 	@Override
 	protected int getLoadingText()
 	{
 		return R.string.checkers_game_fragment_loading_text;
 	}
 
 
 	@Override
 	protected int getTitle()
 	{
 		return R.string.checkers_game_fragment_title;
 	}
 
 
 	@Override
 	protected void initNewBoard()
 	{
 		board = new Board();
 
 		// set up the pieces for the current player's team
 		board.getPosition(1, 0).setPiece(new Piece(Piece.TEAM_PLAYER));
 		board.getPosition(3, 0).setPiece(new Piece(Piece.TEAM_PLAYER));
 		board.getPosition(5, 0).setPiece(new Piece(Piece.TEAM_PLAYER));
 		board.getPosition(7, 0).setPiece(new Piece(Piece.TEAM_PLAYER));
 		board.getPosition(0, 1).setPiece(new Piece(Piece.TEAM_PLAYER));
 		board.getPosition(2, 1).setPiece(new Piece(Piece.TEAM_PLAYER));
 		board.getPosition(4, 1).setPiece(new Piece(Piece.TEAM_PLAYER));
 		board.getPosition(6, 1).setPiece(new Piece(Piece.TEAM_PLAYER));
 		board.getPosition(1, 2).setPiece(new Piece(Piece.TEAM_PLAYER));
 		board.getPosition(3, 2).setPiece(new Piece(Piece.TEAM_PLAYER));
 		board.getPosition(5, 2).setPiece(new Piece(Piece.TEAM_PLAYER));
 		board.getPosition(7, 2).setPiece(new Piece(Piece.TEAM_PLAYER));
 
 		// set up the pieces for the opponent player's team
 		board.getPosition(0, 5).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		board.getPosition(2, 5).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		board.getPosition(4, 5).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		board.getPosition(6, 5).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		board.getPosition(1, 6).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		board.getPosition(3, 6).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		board.getPosition(5, 6).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		board.getPosition(7, 6).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		board.getPosition(0, 7).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		board.getPosition(2, 7).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		board.getPosition(4, 7).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		board.getPosition(6, 7).setPiece(new Piece(Piece.TEAM_OPPONENT));
 	}
 
 
 	@Override
 	protected void initViews()
 	{
 		final View view = getView();
 
 		view.findViewById(R.id.checkers_game_fragment_x0y0).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x1y0).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x2y0).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x3y0).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x4y0).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x5y0).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x6y0).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x7y0).setOnClickListener(onBoardClick);
 
 		view.findViewById(R.id.checkers_game_fragment_x0y1).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x1y1).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x2y1).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x3y1).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x4y1).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x5y1).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x6y1).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x7y1).setOnClickListener(onBoardClick);
 
 		view.findViewById(R.id.checkers_game_fragment_x0y2).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x1y2).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x2y2).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x3y2).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x4y2).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x5y2).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x6y2).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x7y2).setOnClickListener(onBoardClick);
 
 		view.findViewById(R.id.checkers_game_fragment_x0y3).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x1y3).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x2y3).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x3y3).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x4y3).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x5y3).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x6y3).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x7y3).setOnClickListener(onBoardClick);
 
 		view.findViewById(R.id.checkers_game_fragment_x0y4).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x1y4).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x2y4).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x3y4).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x4y4).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x5y4).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x6y4).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x7y4).setOnClickListener(onBoardClick);
 
 		view.findViewById(R.id.checkers_game_fragment_x0y5).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x1y5).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x2y5).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x3y5).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x4y5).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x5y5).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x6y5).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x7y5).setOnClickListener(onBoardClick);
 
 		view.findViewById(R.id.checkers_game_fragment_x0y6).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x1y6).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x2y6).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x3y6).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x4y6).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x5y6).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x6y6).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x7y6).setOnClickListener(onBoardClick);
 
 		view.findViewById(R.id.checkers_game_fragment_x0y7).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x1y7).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x2y7).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x3y7).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x4y7).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x5y7).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x6y7).setOnClickListener(onBoardClick);
 		view.findViewById(R.id.checkers_game_fragment_x7y7).setOnClickListener(onBoardClick);
 
 		final Resources resources = getResources();
 		final ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
 
 		viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener()
 		{
 			@SuppressLint("NewApi")
 			@SuppressWarnings("deprecation")
 			@Override
 			public void onGlobalLayout()
 			{
 				final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 				final View boardPosition = view.findViewById(R.id.checkers_game_fragment_x7y7);
 
				if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
				{
 					final int width = boardPosition.getWidth();
 					layoutParams.height = width;
				}
				else
				{
					final int height = boardPosition.getHeight();
					layoutParams.width = height;
				}
 
 				view.findViewById(R.id.checkers_game_fragment_y0).setLayoutParams(layoutParams);
 				view.findViewById(R.id.checkers_game_fragment_y1).setLayoutParams(layoutParams);
 				view.findViewById(R.id.checkers_game_fragment_y2).setLayoutParams(layoutParams);
 				view.findViewById(R.id.checkers_game_fragment_y3).setLayoutParams(layoutParams);
 				view.findViewById(R.id.checkers_game_fragment_y4).setLayoutParams(layoutParams);
 				view.findViewById(R.id.checkers_game_fragment_y5).setLayoutParams(layoutParams);
 				view.findViewById(R.id.checkers_game_fragment_y6).setLayoutParams(layoutParams);
 				view.findViewById(R.id.checkers_game_fragment_y7).setLayoutParams(layoutParams);
 
 				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
 				{
 					viewTreeObserver.removeOnGlobalLayoutListener(this);
 				}
 				else
 				{
 					viewTreeObserver.removeGlobalOnLayoutListener(this);
 				}
 			}
 		});
 
 		// Load Drawables for checkers pieces into memory. This is done so that
 		// later when we draw these checkers pieces onto the board, that draw
 		// process can be done very quickly as all of the picture data has
 		// already been loaded.
 		playerNormal = (BitmapDrawable) resources.getDrawable(R.drawable.piece_checkers_green_normal);
 		playerKing = (BitmapDrawable) resources.getDrawable(R.drawable.piece_checkers_green_king);
 		opponentNormal = (BitmapDrawable) resources.getDrawable(R.drawable.piece_checkers_orange_normal);
 		opponentKing = (BitmapDrawable) resources.getDrawable(R.drawable.piece_checkers_orange_king);
 	}
 
 
 	@Override
 	protected void onBoardClick(final ImageButton positionPreviousSelected, final ImageButton positionCurrentSelected)
 	{
 		final Coordinate coordinate = new Coordinate((String) positionCurrentSelected.getTag());
 		final Position position = board.getPosition(coordinate);
 		Log.d(LOG_TAG, "Click! " + coordinate + " - has piece? " + position.getPiece());
 	}
 
 
 }
