 package com.andrewdutcher.indexcards;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.graphics.drawable.NinePatchDrawable;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ActionMode;
 import android.view.*;
 
 public class CardDrawer extends View {
 	
 	public ArrayList<IndexCard> cards;
 	public IndexCard currentCard;
 	public ArrayList<Integer> zorder;
 	
 	public NinePatchDrawable shadowimg;
 	public NinePatchDrawable selshadowimg;
 	
 	public ActionMode mActionMode;
 	public MainActivity parent;
 	
 	public double[] editspace;  //data describing the location for a card being edited
 	
 	public CardInput input;
 	
 	public int state;		//0 = working //1 = animating to edits //2 = editing
 	
 	public float density;
 	
 	public Bundle saved = null;
 	
 	public void addnew() {
 		if (currentCard != null)
 			return;
 		Rect r = new Rect();
 		r.left = this.getWidth()/2-5;
 		r.top = this.getHeight()/2-3;
 		r.right = r.left+10;
 		r.bottom = r.top+6;
 		String[] nmn = {};
 		IndexCard tc = new IndexCard(this, "", nmn, r, 0);
 		cards.add(tc);
 		int index = cards.size()-1;
 		zorder.add(index);
 		tc.setRotOffset(5,3);
 		tc.rotation = 180;
 		tc.singletap();
 		double[] temp = {};
 		tc.savedSpot = temp;
 		invalidate();
 	}
 	
 	public CardDrawer(Context context) {
 		super(context);
 		init(context);
 	}
 	public CardDrawer(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init(context);
 	}
 	private void init(Context context) {
 		shadowimg = (NinePatchDrawable) context.getResources().getDrawable(R.drawable.shadow);
 		shadowimg.setDither(true);
 		selshadowimg = (NinePatchDrawable) context.getResources().getDrawable(R.drawable.selshadow);
 		selshadowimg.setDither(true);
 		cards = new ArrayList<IndexCard>();
 		zorder = new ArrayList<Integer>();
 		state = 0;
 		density = getResources().getDisplayMetrics().density;
 		//Log.d("andrew", ((Float)density).toString());
 	}
 	public Bundle serialize() {
 		Bundle out = new Bundle();
 		int len = cards.size();
 		Parcelable[] carddata = new Parcelable[len];
 		for (int i = 0; i < len; i++)
 		{
 			IndexCard card = cards.get(i);
 			Bundle mcard = card.serialize();
 			mcard.putInt("zorder", zorder.indexOf(i));
 			carddata[i] = mcard;
 		}
 		out.putParcelableArray("cards", carddata);
 		out.putInt("width", getWidth());
 		out.putInt("height", getHeight());
		if (currentCard == null)
			out.putInt("current", -1);
		else
			out.putInt("current", cards.indexOf(currentCard));
 		return out;
 	}
 	public void restore() {
 		int wid = getWidth();
 		int hei = getHeight();
 		int width = (int) ((500*density > wid)?wid:500*density);
 		double[] facs = {(wid - width)/2, 20*density, width, 0.6f*width, width/2, 0.6f*width/2, 0};
 		editspace = facs;
 		if (saved == null) {
 			return;
 		}
 		cards = new ArrayList<IndexCard>();
 		zorder = new ArrayList<Integer>();
 		boolean resize = (saved.getInt("width") != getWidth()) || (saved.getInt("height") != getHeight());
 		Parcelable[] carddata = saved.getParcelableArray("cards");
 		for (int i = 0; i < carddata.length; i++) {
 			Bundle idata = (Bundle) carddata[i];
 			if (resize) {							//utter BS math
 				int x = idata.getInt("x");
 				int y = idata.getInt("y");
 				int w = idata.getInt("w");
 				int h = idata.getInt("h");
 				float rot = idata.getFloat("rot");
 				double cdx = (Math.cos(Math.toRadians(rot))*w - Math.sin(Math.toRadians(rot))*h)/2;  //ty alex
 				double cdy = (Math.cos(Math.toRadians(rot))*h + Math.sin(Math.toRadians(rot))*w)/2;
 				double cx = x + cdx;
 				double cy = y + cdy;
 				cx = wid*cx/saved.getInt("width");
 				cy = hei*cy/saved.getInt("height");
 				idata.putInt("x", (int) (cx - cdx));
 				idata.putInt("y", (int) (cy - cdy));
 			}
 			if (idata.getInt("zorder") >= 0)
 				zorder.add(i,idata.getInt("zorder"));
 			cards.add(new IndexCard(this, idata));
 			
 		}
		int cur = saved.getInt("current");
		if (cur >= 0)
			currentCard = cards.get(cur);
		currentCard.savedSpot = new double[0];
		input.client = currentCard;
 	}
 	protected void onDraw(Canvas c) {
 		for (int i = 0; i < zorder.size(); i++) {
 			IndexCard a = cards.get(zorder.get(i));
 			if (currentCard != a) {
 				c.save();
 				a.draw(c);
 				c.restore();
 				if (a.deleting && !a.animating) {
 					deleteCard(a);
 					i--;
 				}
 			}
 		}
 		if (currentCard != null) {
 			c.save();
 			currentCard.draw(c);
 			c.restore();
 		}
 	}
 	public boolean onTouchEvent(MotionEvent e) {
 		if (state != 0)
 			return false;
 		Integer action = e.getAction() & MotionEvent.ACTION_MASK;
 		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)
 		{
 			int index = 0;
 			if (action == MotionEvent.ACTION_POINTER_DOWN)
 			{
 				index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
 			}
 			float tx = e.getX(index);
 			float ty = e.getY(index);
 			for (int i = zorder.size() - 1; i >= 0; i--) {
 				IndexCard tc = cards.get(zorder.get(i));
 				if (tc.animating)
 					continue;
 				if (tc.doesPointTouch((int) tx, (int) ty))
 				{
 					if (tc.addPoint(e, index))
 					{
 						invalidate();
 						return true;
 					}
 					else
 						return false;
 					
 				}
 			}
 			return false;
 		}
 		else if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL)
 		{
 			int size = cards.size();
 			for (int i = 0; i < size; i++)
 			{
 				cards.get(i).processTouches(e);
 				processDeltaZs(cards.get(i), i);
 			}
 			invalidate();
 			return true;
 		}
 		else
 		{
 			//return true;
 		}
 		return false;
 	}
 	
 	public void processDeltaZs(IndexCard card) {
 		processDeltaZs(card, cards.indexOf(card));
 	}
 	
 	public void processDeltaZs(IndexCard card, int i)
 	{
 		int deltaz = card.deltaz;
 		if (deltaz != 0)
 		{
 			int currentz = zorder.indexOf(i);
 			int newz = Math.max(0, Math.min(currentz + deltaz, zorder.size()-1)); //new z must be >= 0 and < capacity 		
 			Collections.rotate(zorder.subList(Math.min(currentz, newz), Math.max(currentz, newz)+1), (deltaz > 0) ? -1 : 1); //magic
 			cards.get(i).deltaz = 0;
 		}
 	}
 	
 	public void deleteCard(int target) {
 		int z = zorder.indexOf(target);
 		if (z >= 0)
 			zorder.remove(z);
 	}
 	
 	public void deleteCard(IndexCard target) {
 		int id = cards.indexOf(target);
 		if (id >= 0)
 			deleteCard(id);
 	}
 	
 	public void purge() {
 		ArrayList<Integer> temp = new ArrayList<Integer>();
 		ArrayList<IndexCard> tempCards = new ArrayList<IndexCard>();
 		for (int i = 0; i < zorder.size(); i++) {
 			temp.add(i);
 			tempCards.add(cards.get(zorder.get(i)));
 		}
 		zorder = temp;
 		cards = tempCards;
 	}
 	
 	public ActionMode.Callback singleSelectedAction = new ActionMode.Callback() {
 		@Override
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 			// Inflate a menu resource providing context menu items
 			MenuInflater inflater = mode.getMenuInflater();
 			inflater.inflate(R.menu.edit_single, menu);
 			Log.d("andrew", "a");
 			return true;
 		}
 		
 		@Override
 		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 			return false; // Return false if nothing is done
 		}
 
 		@Override
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 			Log.d("andrew", "d");
 			switch (item.getItemId()) {
 				case R.id.menu_delete_single:
 					input.hide();
 					currentCard.delete();
 					mode.finish();
 					return true;
 				case R.id.menu_cancel_edit:
 					input.revert();
 					mode.finish();
 					return true;
 				default:
 					return false;
 			}
 		}
 
 		@Override
 		public void onDestroyActionMode(ActionMode mode) {
 			mActionMode = null;
 			input.hide();
 			currentCard = null;
 			invalidate();
 		}
 	};
 }
