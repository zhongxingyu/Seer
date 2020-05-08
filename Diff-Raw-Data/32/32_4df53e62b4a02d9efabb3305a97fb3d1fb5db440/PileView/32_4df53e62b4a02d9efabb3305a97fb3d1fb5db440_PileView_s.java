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
 
 import java.util.LinkedList;
 
 import se.chalmers.touchdeck.R;
 import se.chalmers.touchdeck.enums.TableState;
 import se.chalmers.touchdeck.gamecontroller.Operation;
 import se.chalmers.touchdeck.gamecontroller.Operation.Op;
 import se.chalmers.touchdeck.models.Card;
 import se.chalmers.touchdeck.models.Pile;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 /**
  * Activity for inspecting a pile.
  * 
  * @author Group 17
  */
 public class PileView extends Activity implements OnClickListener, OnLongClickListener {
 
 	private GuiController				mGuiController;
 	private final LinkedList<Button>	mButtons	= new LinkedList<Button>();
 
 	private int							mPileId;
 	private Card						mCard;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.pile_view);
 		mGuiController = GuiController.getInstance();
 		mPileId = getIntent().getExtras().getInt("pileId");
 		setupButtons();
 		mGuiController.setPileView(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	/**
 	 * Create the context menus that appear when clicking a card.
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		Pile currentPile = mGuiController.getGameState().getPiles().get(mPileId);
 		mCard = currentPile.getCard(v.getId());
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.card_menu, menu);
 	}
 
 	/**
 	 * Called when an option in the context menu is chosen
 	 */
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_item_flip:
 			mGuiController.sendOperation(new Operation(Op.flip, mPileId, mCard));
 			break;
 		case R.id.menu_item_move:
 			// Launch the TableView in move state
 			mGuiController.setTableState(TableState.move);
 			mGuiController.setMoveOp(new Operation(Op.move, mPileId, -1, mCard));
 			Intent table = new Intent(this, TableView.class);
 			// Don't start a new tableView, use the one already running.
 			table.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 			startActivity(table);
 			break;
 		default:
 		}
 		return true;
 	}
 
 	/**
 	 * Creates buttons that represents the cards in the pile.
 	 */
 	public void setupButtons() {
 		LinearLayout layout = (LinearLayout) findViewById(R.id.pileLinear);
 		layout.removeAllViewsInLayout();
 		layout.invalidate();
 		Pile currentPile = mGuiController.getGameState().getPiles().get(mPileId);
 
 		if (currentPile != null) {
 
 			LinkedList<Card> cards = currentPile.getCards();
 			for (int i = 0; i < currentPile.getSize(); i++) {
 
 				Button b = new Button(this);
 				LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f);
 				bp.setMargins(3, 0, 3, 0);
 
 				b.setId(i);
 				b.setTag("Card " + i);
 
 				Card card = cards.get(i);
 
 				int image = getResources().getIdentifier(card.getImageName(), "drawable", getPackageName());
 				b.setBackgroundResource(image);
 
 				// Calculate the size of the button
 				Display display = getWindowManager().getDefaultDisplay();
 				Point size = new Point();
 				display.getSize(size);
 
 				int y = size.y / 2;
 				int x = (int) (y * 0.73);
 
 				b.setHeight(y);
 				b.setWidth(x);
 
 				layout.addView(b);
 				mButtons.add(b);
 				b.setOnClickListener(this);
 				b.setOnLongClickListener(this);
 				registerForContextMenu(b);
 
 				b.setLayoutParams(bp);
 			}
 		}
 	}
 
 	/**
 	 * Called when one of the buttons is clicked
 	 * 
 	 * @param The view(in this case, button) that was clicked
 	 */
 	@Override
 	public void onClick(View v) {
 		openContextMenu(v);
 	}
 
 	/**
 	 * Called when one of the buttons is long clicked
 	 */
 	@Override
 	public boolean onLongClick(View v) {
 		this.closeContextMenu();
 		return true;
 	}
 }
