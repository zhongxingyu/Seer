 package aweber.phandroid;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.FrameLayout.LayoutParams;
 import android.widget.TextView;
 import aweber.phandroid.game.Board;
 import aweber.phandroid.game.BoardPos;
 import aweber.phandroid.game.Piece;
 
 public abstract class GameActivity extends Activity {
 
 	private static final int BOARD_INNER_OUTER_DIFF_DP = 10; // difference between inner and outer board
 
 	private static final String PROPERTY_FILE_NAME = "phandroid.props";
 
 	private static final String PROP_SOUND = "sound"; // property where to store whether sound is enabled
 
 	private String _version; // the software version
 	private FrameLayout _boardLayout;
 	private int _boardLeft, _boardTop; // board frame relative to parent frame
 	protected Board _board;
 
 	/** helper vars for move */
 	private BoardPos _oldPos; // object pos before move
 	private float _xRawOld, _yRawOld; // cursor pos on display before move
 	private AtomicBoolean _isMoving = new AtomicBoolean(false); // are we currently in a move?
 	private AtomicInteger _mover = new AtomicInteger(-1); // which piece is currently moving?
 	private boolean _canMoveX; // can we move horizontally in current move
 	private boolean _canMoveY; // can we move vertically in current move
 	private int _minX; // min. pos. where piece can move
 	private int _maxX; // max. pos. where piece can move
 	private int _minY; // min. pos. where piece can move
 	private int _maxY; // max. pos. where piece can move
 
 	protected int _noOfMoves;
 
 	private MediaPlayer _playerMove, _playerSuccess; // to play sounds
 	private boolean _isSoundEnabled = false; // default -> mute
 	protected Properties _props;
 
 	private int _board_field_size_px;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(getContentView());
 		_boardLayout = (FrameLayout) findViewById(getInnerBoardLayout());
 
 		// locate board in the middle of the screen
 		FrameLayout outerBoardLayout = (FrameLayout) findViewById(getOuterBoardLayout());
 		DisplayMetrics metrics = getResources().getDisplayMetrics();
 		int display_height = metrics.heightPixels;
 		int board_size_px = getBoardHeight();
 		int inner_outer_diff = Math.round(BOARD_INNER_OUTER_DIFF_DP * getResources().getDisplayMetrics().density);
 		int outerBottomMargin = (display_height - board_size_px) / 2;
 		int innerBottomMargin = ((display_height - board_size_px) / 2) + inner_outer_diff;
 		((LayoutParams) _boardLayout.getLayoutParams()).bottomMargin = innerBottomMargin;
 		((LayoutParams) outerBoardLayout.getLayoutParams()).bottomMargin = outerBottomMargin;
 
 		_board_field_size_px = Math.round(getBoardFieldSize());
 
 		_playerMove = MediaPlayer.create(GameActivity.this, R.raw.move);
 		_playerSuccess = MediaPlayer.create(GameActivity.this, R.raw.success);
 		_version = getVersion();
 
 		initBoard();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		saveProperties();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.game_menu, menu);
 		return true; // "return true for the menu to be displayed...".
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// sound menu item
 		if (_isSoundEnabled) {
 			// sound is enabled - show menu item for disabling sound
 			menu.getItem(1).setTitle(R.string.menu_sound_off); // menu item '2' is sound item
 		} else {
 			// sound is disabled - show menu item for enabling sound
 			menu.getItem(1).setTitle(R.string.menu_sound_on); // menu item '2' is sound item
 		}
 		return true; // "return true for the menu to be displayed...".
 	}
 
 	@Override
 	// Return false to allow normal menu processing to proceed, true to consume it here.
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.opt_finish:
 			exit();
 			return true;
 		case R.id.opt_restart:
 			reset();
 			return true;
 		case R.id.opt_sound:
 			if (_isSoundEnabled) {
 				_isSoundEnabled = false; // sound was enabled, now disabled
 			} else {
 				_isSoundEnabled = true; // sound was disabled, now enabled
 			}
 			_props.setProperty(PROP_SOUND, String.valueOf(_isSoundEnabled));
 			return true;
 		case R.id.opt_about:
 			showAboutDialog();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	protected abstract int getContentView();
 
 	protected abstract int getInnerBoardLayout();
 
 	protected abstract int getOuterBoardLayout();
 
 	protected abstract int getBoardHeight(); // in pixels
 
 	protected abstract int getBoardFieldSize(); // in pixels
 
 	protected abstract int getTxtMoves();
 
 	protected abstract int getTxtBestSolution();
 
 	protected abstract boolean containsPieceId(int id);
 
 	protected abstract String getPropBestSolution();
 
 	protected void exit() {
 		if (_playerMove != null) {
 			_playerMove.release();
 		}
 		if (_playerSuccess != null) {
 			_playerSuccess.release();
 		}
 	}
 
 	protected void initBoard() {
 		loadProperties(); // load properties where app state is saved
 		_isSoundEnabled = false;
 		if (_props.containsKey(PROP_SOUND)) {
 			_isSoundEnabled = Boolean.valueOf(_props.getProperty(PROP_SOUND));
 		}
 	}
 
 	final OnTouchListener onTouch = new OnTouchListener() {
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 
 			_boardTop = _boardLayout.getTop();
 			_boardLeft = _boardLayout.getLeft();
 
 			// Check what is being touched
 			if (containsPieceId(v.getId())) {
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_MOVE: { // move happend during pressed gesture
 					if (_isMoving.get() && _mover.get() == v.getId()) {
 
 						// for debugging:
 						//showEventCoordinates(event);
 						//showMoveTendency(event, v.getId());
 
 						final FrameLayout.LayoutParams boardLayoutParams = (LayoutParams) v.getLayoutParams();
						int diffX = 0; // difference between old X position and now
						int diffY = 0; // difference between old Y position and now
 						int x = boardLayoutParams.leftMargin;
 						int y = boardLayoutParams.topMargin;
 						if (_canMoveX) {
							x = (int) event.getRawX() - _boardLeft - (v.getWidth() / 2);
 							if (x < _minX) {
 								x = _minX;
 							} else if (x > _maxX) {
 								x = _maxX;
 							}
							diffX = x - _oldPos.x * _board_field_size_px;
 						}
 						if (_canMoveY) {
							y = (int) event.getRawY() - _boardTop - Math.round(v.getHeight());
 							if (y < _minY) {
 								y = _minY;
 							} else if (y > _maxY) {
 								y = _maxY;
 							}
							diffY = y - _oldPos.y * _board_field_size_px;
 						}
 						String s = "";
 						if (_canMoveX && _canMoveY) {
 							// we only want to move in one direction
 							if (Math.abs(diffX) > Math.abs(diffY)) {
 								y = _oldPos.y * _board_field_size_px; // reset, because we move horizontally
 							} else {
 								x = _oldPos.x * _board_field_size_px; // reset, because we move vertically
 							}
							s = diffX + "," + diffY;
 						}
 						final TextView txtMoves = (TextView) findViewById(getTxtMoves());
 						txtMoves.setText("" + x + "; " + s);
 						final TextView txtSol = (TextView) findViewById(getTxtBestSolution());
 						txtSol.setText("" + y);
 
 						boardLayoutParams.leftMargin = x;
 						boardLayoutParams.topMargin = y;
 						v.setLayoutParams(boardLayoutParams);
 					}
 					break;
 				}
 				case MotionEvent.ACTION_UP: { // finished pressed gesture
 					if (_isMoving.get() && _mover.get() == v.getId()) {
 						final FrameLayout.LayoutParams boardLayoutParams = (LayoutParams) v.getLayoutParams();
 
 						// for debugging:
 						//showEventCoordinates(event);
 						//showMoveTendency(event, v.getId());
 
 						// check if we are within board frame
 						final BoardPos newPos = calculateNewPosition(event, v.getId());
 						if (_board.canMoveTo(v.getId(), newPos.x, newPos.y)) {
 							_board.move(v.getId(), newPos.x, newPos.y);
 							// object left margin to board frame = board field * board field size
 							boardLayoutParams.leftMargin = newPos.x * _board_field_size_px;
 							boardLayoutParams.topMargin = newPos.y * _board_field_size_px;
 							_noOfMoves++;
 							showMoves();
 							// showFreePositions(); // for debugging
 							if (_board.isSolution()) {
 								handleSuccess();
 							} else {
 								playMoveSound();
 							}
 						} else {
 							// we cannot move here -> place piece back on old position
 							boardLayoutParams.leftMargin = _oldPos.x * _board_field_size_px;
 							boardLayoutParams.topMargin = _oldPos.y * _board_field_size_px;
 						}
 						v.setLayoutParams(boardLayoutParams);
 						_isMoving.set(false);
 					}
 					break;
 				}
 				case MotionEvent.ACTION_DOWN: { // started pressed gesture
 					if (_isMoving.compareAndSet(false, true)) {
 						final int pieceId = v.getId();
 						_mover.set(pieceId);
 						if (_board.canMove(pieceId)) {
 							_oldPos = new BoardPos(_board.getXPos(pieceId), _board.getYPos(pieceId));
 							_xRawOld = event.getRawX();
 							_yRawOld = event.getRawY();
 
 							int possibleMovesLeft = _board.canMoveLeft(v.getId(), _oldPos.x, _oldPos.y);
 							int possibleMovesRight = _board.canMoveRight(v.getId(), _oldPos.x, _oldPos.y);
 							int possibleMovesUp = _board.canMoveUp(v.getId(), _oldPos.x, _oldPos.y);
 							int possibleMovesDown = _board.canMoveDown(v.getId(), _oldPos.x, _oldPos.y);
 
 							_minX = (_oldPos.x - possibleMovesLeft) * _board_field_size_px;
 							_maxX = (_oldPos.x + possibleMovesRight) * _board_field_size_px;
 							_minY = (_oldPos.y - possibleMovesUp) * _board_field_size_px;
 							_maxY = (_oldPos.y + possibleMovesDown) * _board_field_size_px;
 
 							if (possibleMovesLeft > 0 || possibleMovesRight > 0) {
 								_canMoveX = true;
 							} else {
 								_canMoveX = false;
 							}
 							if (possibleMovesDown > 0 || possibleMovesUp > 0) {
 								_canMoveY = true;
 							} else {
 								_canMoveY = false;
 							}
 
 						} else {
 							_isMoving.set(false);
 						}
 					}
 					break;
 				}
 				}// inner switch
 			}// if
 			return true;
 		}
 	};
 
 	private BoardPos calculateNewPosition(MotionEvent event, int pieceId) {
 		int xOld = _board.getXPos(pieceId);
 		int yOld = _board.getYPos(pieceId);
 
 		final BoardPos result = new BoardPos(xOld, yOld);
 
 		float tendenceX = (event.getRawX() - _xRawOld) / _board_field_size_px;
 		float tendenceY = (event.getRawY() - _yRawOld) / _board_field_size_px;
 
 		if (_canMoveX && (!_canMoveY || Math.abs(tendenceX) > Math.abs(tendenceY))) {
 			int xDist = Math.round(tendenceX);
 			if (xDist >= 2) {
 				if (_board.canMoveTo(pieceId, xOld + 2, yOld)) {
 					xDist = 2;
 				} else {
 					xDist = 1;
 				}
 			} else if (xDist <= -2) {
 				if (_board.canMoveTo(pieceId, xOld - 2, yOld)) {
 					xDist = -2;
 				} else {
 					xDist = -1;
 				}
 			} else if (xDist == 0) {
 				if (tendenceX > 0.3) {
 					xDist = 1;
 				} else if (tendenceX < -0.3) {
 					xDist = -1;
 				}
 			}
 			result.x += xDist;
 		} else {
 			int yDist = Math.round(tendenceY);
 			if (yDist >= 2) {
 				if (_board.canMoveTo(pieceId, xOld, yOld + 2)) {
 					yDist = 2;
 				} else {
 					yDist = 1;
 				}
 			} else if (yDist <= -2) {
 				if (_board.canMoveTo(pieceId, xOld, yOld - 2)) {
 					yDist = -2;
 				} else {
 					yDist = -1;
 				}
 			} else if (yDist == 0) {
 				if (tendenceY > 0.3) {
 					yDist = 1;
 				} else if (tendenceY < -0.3) {
 					yDist = -1;
 				}
 			}
 			result.y += yDist;
 		}
 		return result;
 	}
 
 	protected void addPiece(int id, Piece p) {
 		_board.addPiece(id, p);
 		final View v = findViewById(id);
 		v.setOnTouchListener(onTouch);
 		final FrameLayout.LayoutParams boardLayoutParams = (LayoutParams) v.getLayoutParams();
 		boardLayoutParams.leftMargin = p.xLeft * _board_field_size_px;
 		boardLayoutParams.topMargin = p.yTop * _board_field_size_px;
 		v.setLayoutParams(boardLayoutParams);
 	}
 
 	private void playMoveSound() {
 		if (_isSoundEnabled) {
 			_playerMove.start();
 		}
 	}
 
 	private void playSuccessSound() {
 		if (_isSoundEnabled) {
 			_playerSuccess.start();
 		}
 	}
 
 	/** reset board, board properties, move counter. */
 	protected void reset() {
 		saveProperties();
 		initBoard();
 	}
 
 	/** load the properties file where app state is stored. */
 	private void loadProperties() {
 		_props = new Properties();
 		FileInputStream propsIn = null;
 		try {
 			propsIn = openFileInput(PROPERTY_FILE_NAME);
 			_props.load(propsIn);
 		} catch (FileNotFoundException e) {
 			// property file doesn't exist yet, that's ok
 		} catch (IOException e) {
 			// error while reading input strean (TODO what to do?)
 			throw new RuntimeException(e);
 		} finally {
 			if (propsIn != null) {
 				try {
 					propsIn.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/** save the properties file where app state is stored. */
 	protected void saveProperties() {
 		FileOutputStream propsOut = null;
 		try {
 			// create property file not readable by other apps
 			propsOut = openFileOutput(PROPERTY_FILE_NAME, MODE_PRIVATE);
 			_props.store(propsOut, null);
 		} catch (IOException e) {
 			e.printStackTrace(); // TODO can this happen? openFileOutput creates the file if it doesn't exist
 		} finally {
 			if (propsOut != null) {
 				try {
 					propsOut.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private void handleSuccess() {
 		playSuccessSound();
 
 		// check if we have a new best solution
 		int oldBest = Integer.MAX_VALUE;
 		try {
 			oldBest = Integer.valueOf(_props.getProperty(getPropBestSolution()));
 		} catch (NumberFormatException e) {
 			// expected when no entry yet
 		}
 		if (_noOfMoves < oldBest) {
 			_props.setProperty(getPropBestSolution(), String.valueOf(_noOfMoves));
 			saveProperties();
 		}
 
 		// show success dialog
 		final Dialog dialog = new Dialog(GameActivity.this);
 		dialog.setContentView(R.layout.dialog_success);
 		final Button button = (Button) dialog.findViewById(R.id.button_new_game);
 		button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				reset();
 				dialog.dismiss();
 			}
 		});
 		dialog.show();
 	}
 
 	protected void showMoves() {
 		final TextView txtMoves = (TextView) findViewById(getTxtMoves());
 		txtMoves.setText(getString(R.string.game_moves) + ": " + String.valueOf(_noOfMoves));
 	}
 
 	protected void showBestSolution() {
 		final TextView txtSol = (TextView) findViewById(getTxtBestSolution());
 		txtSol.setText(getString(R.string.game_best) + ": " + _props.getProperty(getPropBestSolution()));
 		txtSol.setGravity(Gravity.RIGHT);
 	}
 
 	private void showAboutDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(getText(R.string.dialog_help) + "\n\n" + getText(R.string.dialog_about) + " " + _version);
 		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 				return;
 			}
 		});
 		final AlertDialog aboutDialog = builder.create();
 		aboutDialog.show();
 	}
 
 	/** for debugging... */
 	private void showFreePositions() {
 		// final TextView txtFreePos = (TextView) findViewById(R.id.txt_freepositions);
 		// txtFreePos.setText(board.getFreePosAsString());
 	}
 
 	/** for debugging.. */
 	private void showEventCoordinates(MotionEvent event) {
 		final TextView txtMoves = (TextView) findViewById(getTxtMoves());
 		txtMoves.setText("x: " + Math.round(event.getRawX() - _boardLeft) + ", y:"
 				+ Math.round(event.getRawY() - _boardTop));
 	}
 
 	/** for debugging.. */
 	private void showMoveTendency(MotionEvent event, int pieceId) {
 		int xOld = _board.getXPos(pieceId);
 		int yOld = _board.getYPos(pieceId);
 		final BoardPos oldPos = new BoardPos(xOld, yOld);
 		final BoardPos newPos = calculateNewPosition(event, pieceId);
 		final TextView txtMoves = (TextView) findViewById(getTxtBestSolution());
 		txtMoves.setText("old: " + oldPos + ", new: " + newPos);
 		txtMoves.setGravity(Gravity.RIGHT);
 	}
 
 	/** Gets the version from the Manifest. */
 	private String getVersion() {
 		try {
 			final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
 			return packageInfo.versionName;
 		} catch (PackageManager.NameNotFoundException e) {
 		}
 		return "<unknown>";
 	}
 
 }
