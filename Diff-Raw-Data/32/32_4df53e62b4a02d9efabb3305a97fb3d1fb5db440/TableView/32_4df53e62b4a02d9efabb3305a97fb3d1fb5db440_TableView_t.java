 /**
  Copyright (c) 2013 Karl Engstr�m, Sebastian Ivarsson, Jacob Lundberg, Joakim Karlsson, Alexander Persson and Fredrik Westling
  */
 
 /**
  This file is part of TouchDeck.
 
  TouchDeck is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 2 of the License, or
  (at your option) any later version.
 
  TouchDeck is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with TouchDeck.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package se.chalmers.touchdeck.gui;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 import se.chalmers.touchdeck.R;
 import se.chalmers.touchdeck.enums.TableState;
 import se.chalmers.touchdeck.gamecontroller.GameController;
 import se.chalmers.touchdeck.gamecontroller.GameState;
 import se.chalmers.touchdeck.gamecontroller.Operation;
 import se.chalmers.touchdeck.gamecontroller.Operation.Op;
 import se.chalmers.touchdeck.gui.dialogs.DialogText;
 import se.chalmers.touchdeck.gui.dialogs.PileNameDialog;
 import se.chalmers.touchdeck.models.Pile;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * The Activity for the table view, contains a grid with pile positions represented as buttons.
  * 
  * @author group17
  */
 public class TableView extends Activity implements OnClickListener, Observer {
 
 	private static final int				MAX_PILE_NAME_LENGTH	= 20;
 	private static final int				MAX_PILE_NAME_DISPLAYED	= 6;
 	private static final int				PADDING					= 5;
 	private TableLayout						mTableLayout;
 	private final ArrayList<LinearLayout>	mLayouts				= new ArrayList<LinearLayout>();
 	private GuiController					mGuiController;
 
 	private int								mPileId;
 	private boolean							mIsBackPressedBefore;
 	private TableState						mTableState				= TableState.normal;
 	private Operation						mMoveOp;
 	private Toast							mMoveToast;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.table_view);
 		// Create Buttons in the tableview
 		setupButtons();
 		Serializable s = getIntent().getExtras().getSerializable("state");
 		String ipAddr = getIntent().getExtras().getString("IPaddr");
 		GameState gs = (GameState) s;
 
 		mGuiController = GuiController.getInstance();
 		mGuiController.setupConnections(ipAddr);
 		mGuiController.setGameState(gs);
 		mGuiController.setTableView(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/**
 	 * Create the context menus that appear when long-pressing a pile.
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		if (!mTableState.equals(TableState.normal)) {
 			return;
 		}
 		mPileId = v.getId();
 		MenuInflater inflater = getMenuInflater();
 		Pile currentPile = mGuiController.getGameState().getPiles().get(mPileId);
 		if (currentPile != null) {
 			if (currentPile.getSize() > 0) {
 				inflater.inflate(R.menu.pile_menu, menu);
 			} else {
 				inflater.inflate(R.menu.empty_pile_menu, menu);
 			}
 		}
 	}
 
 	/**
 	 * Called when an option in the context menu is chosen.
 	 */
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_item_shuffle:
 			mGuiController.sendOperation(new Operation(Op.shuffle, mPileId));
 			Toast.makeText(this, mGuiController.getGameState().getPiles().get(mPileId).getName() + " shuffled!", Toast.LENGTH_SHORT).show();
 			break;
 		case R.id.menu_item_delete:
 			String pileName = mGuiController.getGameState().getPiles().get(mPileId).getName();
 			mGuiController.sendOperation(new Operation(Op.delete, mPileId));
 			Toast.makeText(this, pileName + " deleted!", Toast.LENGTH_SHORT).show();
 			break;
 		case R.id.menu_item_face_up:
 			mGuiController.sendOperation(new Operation(Op.faceUp, mPileId));
 			break;
 		case R.id.menu_item_face_down:
 			mGuiController.sendOperation(new Operation(Op.faceDown, mPileId));
 			break;
 		case R.id.menu_item_move_all:
			mMoveToast = Toast.makeText(this, "Select pile to move cards to", Toast.LENGTH_LONG);
 			mTableState = TableState.moveAll;
 			mMoveToast.show();
 			break;
 		default:
 		}
 		return true;
 	}
 
 	/**
 	 * Creates the buttons in a grid on the "table". The number of rows and columns are specified by NUM_ROWS and
 	 * NUM_COLUMNS.
 	 */
 	public void setupButtons() {
 		mTableLayout = (TableLayout) findViewById(R.id.tableTable);
 		// Create a number of rows in the table
 		for (int i = 0; i < GameController.NUM_ROWS; i++) {
 			TableRow tr = new TableRow(this);
 			tr.setTag("row" + i);
 			// Create the layout parameters for the table row, all rows should be the same size
 			LayoutParams tp = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1.0f);
 			for (int j = 0; j < GameController.NUM_COLUMNS; j++) {
 
 				LinearLayout ll = new LinearLayout(this);
 				ll.setOrientation(LinearLayout.VERTICAL);
 				ll.setPadding(PADDING, PADDING, PADDING, PADDING);
 				LayoutParams lp = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1);
 
 				Button b = new Button(this);
 				// Create the layout parameters for the button, all buttons should be the same size
 				LayoutParams bp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 9);
 
 				b.setId(GameController.NUM_COLUMNS * i + j);
 				b.setTag("Pile " + (GameController.NUM_ROWS * i + j));
 				// b.setText("Pile " + (num_columns*i + j));
 
 				// Set this interface as the listener to the button
 				b.setOnClickListener(this);
 				registerForContextMenu(b);
 
 				TextView tv = new TextView(this);
 				tv.setTextSize(12);
 
 				// Set this interface as the listener to the text view
 				tv.setOnClickListener(this);
 				registerForContextMenu(tv);
 				LayoutParams ba = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 2);
 
 				tv.setId(GameController.NUM_COLUMNS * i + j);
 				tv.setTag("Pile " + (GameController.NUM_ROWS * i + j));
 
 				ll.addView(b);
 				b.setLayoutParams(bp);
 
 				ll.addView(tv);
 				tv.setLayoutParams(ba);
 
 				tr.addView(ll);
 				ll.setLayoutParams(lp);
 
 				mLayouts.add(ll);
 			}
 			// Add the row to the table and apply the layout
 			mTableLayout.addView(tr);
 			tr.setLayoutParams(tp);
 		}
 	}
 
 	/**
 	 * Called when one of the buttons is clicked
 	 * 
 	 * @param The view(in this case, button) that was clicked
 	 */
 	@Override
 	public void onClick(View v) {
 		if (mTableState.equals(TableState.move)) {
 			Intent pileView = new Intent(this, PileView.class);
 			pileView.putExtra("pileId", mMoveOp.getPile1());
 			mMoveOp.setPile2(v.getId());
 			mGuiController.sendOperation(mMoveOp);
 			mTableState = TableState.normal;
 			startActivity(pileView);
 			mMoveToast.cancel();
 			return;
 		} else if (mTableState.equals(TableState.moveAll)) {
 			mGuiController.sendOperation(new Operation(Op.moveAll, mPileId, v.getId(), null));
 			mMoveToast.cancel();
 			mTableState = TableState.normal;
 			return;
 		}
 
 		// Get which button has been pressed
		mPileId = v.getId();
		Pile p = mGuiController.getGameState().getPiles().get(mPileId);
 
 		// Check if there is a pile on this position
 		if (p != null) {
 			// Open the pile in the pileview
 			Intent pileView = new Intent(this, PileView.class);
			pileView.putExtra("pileId", mPileId);
 			this.startActivity(pileView);
 		} else {
 			// Prompt the user to create a new pile
 			String msg = "Please enter a name for the pile: ";
			PileNameDialog dialog = new PileNameDialog(this, mPileId, msg, mGuiController.getGameState().getDefaultPileName());
 			dialog.show(this);
 		}
 	}
 
 	/**
 	 * Sets the state of the tableView
 	 * 
 	 * @param tableState The state to set
 	 */
 	public void setTableState(TableState tableState) {
 		mTableState = tableState;
 	}
 
 	/**
 	 * Starts a toast to display to the user that they are in move mode
 	 */
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (mTableState == TableState.move) {
			mMoveToast = Toast.makeText(this, "Select pile to move card to", Toast.LENGTH_LONG);
 			mMoveToast.show();
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		android.os.Process.killProcess(android.os.Process.myPid());
 	}
 
 	/**
 	 * Makes the user have to press the back-button twice to exit the app
 	 */
 	@Override
 	public void onBackPressed() {
 		if (mTableState.equals(TableState.move)) {
 			// Abort the move and go back to the pileView
 			Intent i = new Intent(this, PileView.class);
			i.putExtra("pileId", mPileId);
 			mTableState = TableState.normal;
 			mMoveToast.cancel();
 			startActivity(i);
 			return;
 		} else if (mTableState.equals(TableState.moveAll)) {
 			// Abort move
 			mTableState = TableState.normal;
 			mMoveToast.cancel();
 			return;
 		}
 
 		if (mIsBackPressedBefore) {
 			super.onBackPressed();
 			finish();
 			return;
 		}
 		this.mIsBackPressedBefore = true;
 		Toast.makeText(this, "Click back again to exit", Toast.LENGTH_SHORT).show();
 		new Handler().postDelayed(new Runnable() {
 
 			@Override
 			public void run() {
 				mIsBackPressedBefore = false;
 
 			}
 		}, 2000);
 	}
 
 	/**
 	 * Updates the tableView to show the current state of all piles
 	 */
 	public void updateTableView() {
 		int i = 0;
 
 		for (Pile p : mGuiController.getGameState().getPiles()) {
 			LinearLayout ll = mLayouts.get(i);
 			Button b = (Button) ll.getChildAt(0);
 			TextView tv = (TextView) ll.getChildAt(1);
 
 			if (p == null) {
 				b.setBackgroundResource(0);
 				b.setBackgroundColor(0);
 				tv.setText("");
 			} else {
 
 				String name = p.getName();
 				if (name.length() > MAX_PILE_NAME_DISPLAYED) {
 					name = name.substring(0, MAX_PILE_NAME_DISPLAYED);
 				}
 				tv.setText("[" + p.getSize() + "]" + name);
 
 				if (p.getSize() > 0) {
 					// Set the picture of the pile to be the picture of the card on top.
 					String imgName = p.getCard(0).getImageName();
 					int imgRes = this.getResources().getIdentifier(imgName, "drawable", this.getPackageName());
 					b.setBackgroundResource(imgRes);
 				} else {
 					b.setBackgroundColor(0xff00dd00);
 				}
 
 			}
 			i++;
 		}
 
 	}
 
 	/**
 	 * Called when a dialog gets text input. Creates a new pile with the name given in the dialog
 	 * 
 	 * @param obs The object (Dialogtext) that has been updated
 	 * @param param The parameter that is passed along (in the case of the Dialogtext, it's the same object)
 	 */
 	@Override
 	public void update(Observable obs, Object param) {
 		if (obs instanceof DialogText) {
 			GameState gameState = mGuiController.getGameState();
 			DialogText dt = (DialogText) param;
 			// See if the name provided is unique
 			if (gameState.getPileNames().contains(dt.getString())) {
 				// Prompt the user to try again
 				String msg = "Please enter a unique name: ";
 				PileNameDialog dialog = new PileNameDialog(this, dt.getId(), msg, gameState.getDefaultPileName());
 				dialog.show(this);
 			}
 			if (dt.getString().length() > MAX_PILE_NAME_LENGTH) {
 				// Prompt the user to try again
 				String msg = "Please enter a shorter name: ";
 				PileNameDialog dialog = new PileNameDialog(this, dt.getId(), msg, gameState.getDefaultPileName());
 				dialog.show(this);
 
 			} else {
 				// Create the pile
 				mGuiController.sendOperation(new Operation(Op.create, dt.getId(), dt.getString()));
 				updateTableView();
 			}
 		}
 	}
 
 	/**
 	 * @param mMoveOp the mMoveOp to set
 	 */
 	public void setmMoveOp(Operation mMoveOp) {
 		this.mMoveOp = mMoveOp;
 	}
 }
