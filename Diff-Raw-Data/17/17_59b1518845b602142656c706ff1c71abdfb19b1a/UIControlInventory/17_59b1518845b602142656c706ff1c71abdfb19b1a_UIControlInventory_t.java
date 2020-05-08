 package com.games.test1.ui;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.util.Log;
 
 import com.games.test1.Inventory;
 import com.games.test1.InventoryItem;
 import com.games.test1.GameView.GameThread;
 
 /** Draw the inventory panel to the screen. */
 public class UIControlInventory extends UIContainer {
 	public static final int DEFAULT_ITEM_WIDTH = 48;
 	public static final int DEFAULT_ITEM_HEIGHT = 48;
 	private static final int PADDING_LEFT = 6;
 	private static final int PADDING_TOP = 6;
 	private static final int COL_WIDTH = 50;
 	private static final int ROW_HEIGHT = 50;
 	
 	private Inventory mInventory;
 	private int mCols;
 	
 	private UIControlDialog mDetailsPanel;
 	
 	private static Paint scratchPaint = new Paint();
 
 	/** Create the inventory control with an attached inventory. */
 	public UIControlInventory(Inventory inv, int width, int height) {
 		super(width, height);
 		mInventory = inv;
 	
 		mCols = mWidth / DEFAULT_ITEM_WIDTH;
 	}
 	
 	/** Respond to a click. */
 	public boolean trigger(GameThread game, int mouseX, int mouseY) {
 		if (!super.trigger(game, mouseX, mouseY)) {
 			int col = (mouseX - PADDING_LEFT) / COL_WIDTH,
 				row = (mouseY - PADDING_TOP) / ROW_HEIGHT;
 			int itemNum = row * mCols + col;		
 			if (itemNum < 0 || itemNum >= mInventory.getItems().size()) {
 				removeSelf();						
 			} else {			
 				Log.w("Miskatonic", "CLICKED ON ITEM:" + mInventory.getItems().get(itemNum).getName());		
 				
 				showDetailsForItem(mInventory.getItems().get(itemNum));
 			}
 		} else {
 			// Remove ourselves along with details panel.
 			if (mDetailsPanel.mShouldRemove) {
 				removeSelf();
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Show the description/context menu for an inventory item.
 	 */
 	public void showDetailsForItem(final InventoryItem i) {
		mDetailsPanel = new UIControlDialog(i.getName(), i.getDescription(), 
 							"Use", 
 							new UIEvent() {
 								public void execute(GameThread game, UIControl caller) {
 									game.getMainGameState().selectInventoryItem(i);
 									caller.removeSelf();
 								}								
 							},
 							"Cancel",
 							new UIEvent() {
 								public void execute(GameThread game, UIControl caller) {									
 									caller.removeSelf();
 								}								
 							},
 							(int) (mWidth * .9),
 							(int) (mHeight * .9));
 		
 		// Establish a back-reference.
 		mInnerUI.addControl(mDetailsPanel, GameUI.POSITION_CENTER);
 	}
 	
 
 	
 	/** Draw the inventory panel. */
 	public void draw(Canvas c, int x, int y) {
 		Rect r = new Rect(x, y, x+mWidth, y+mHeight);
 		
 		scratchPaint.setStyle(Paint.Style.FILL);
 		scratchPaint.setColor(Color.GRAY);
 		c.drawRect(r, scratchPaint);
 		
 		scratchPaint.setStyle(Paint.Style.STROKE);
 		scratchPaint.setColor(Color.WHITE);		
 		c.drawRect(r, scratchPaint);
 		
 		// Draw items in a grid. TODO: Scrolling.
 		int row = 0, col = 0;
 		for (InventoryItem i : mInventory.getItems()) {
 			i.draw(c, x + PADDING_LEFT + col * COL_WIDTH, y + PADDING_TOP + row * ROW_HEIGHT);
 			col++;
 			if (col >= mCols) {
 				col = 0;
 				row++;
 			}
 		}
 		
 		super.draw(c, x, y);
 	}
 }
