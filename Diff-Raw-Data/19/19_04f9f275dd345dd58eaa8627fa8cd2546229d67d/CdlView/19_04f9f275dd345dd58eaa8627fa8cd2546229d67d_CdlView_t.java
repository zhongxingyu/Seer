 /*
 Copyright 2013 Cadeli
 Contact the authors at cadeli.drummachine@yahoo.com
 See updates at http://github.com/cadeli/CdlUI
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
  */
 
 package com.cadeli.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class CdlView extends View implements OnGestureListener {
 	private static final String TAG = "CdlView";
 
 	public static final int CDL_LAYOUT_GRID = 0;
 	public static final int CDL_LAYOUT_FLOW = 1;
 	public static final int CDL_LAYOUT_ABSOLUTE = 2;
 	protected static final long FLASH_DURATION = 200;
 
 	protected List<CdlBaseButton> cdlBaseButtons = new ArrayList<CdlBaseButton>();
 	protected List<CdlBaseButton> cdlBaseButtonsMenu = new ArrayList<CdlBaseButton>();
 	private int cdlLayoutType = CDL_LAYOUT_GRID; // default val
 	private int grid_nbCols = 3; // defaultval
 	private int padding = 2; // defaultval
 	private boolean sized;
 	private int scrollBarHeight = 8;
 	protected int startXScroll;
 	private int w_btn;
 	private boolean mustDrawMenu = false;
 	protected static RectF urect = new RectF();
 	protected static Rect bounds = new Rect();
 	protected static RectF rectf = new RectF();
 
 	private GestureDetector gestureDetector;
 	private boolean flashing = false;
 	private int timerCountFlash;
 	private Handler handlerFlash = new Handler();
 	private CdlBaseButton flashingBtn;
 	private Runnable runnableFlash = new Runnable() {
 
 		public void run() {
 			timerCountFlash--;
 			if (timerCountFlash <= 0) {
 				flashing = false;
 				flashingBtn.setFlashing(false);
 				invalidate(flashingBtn.getRect());
 			} else {
 				handlerFlash.postDelayed(this, FLASH_DURATION);
 			}
 		}
 	};
 
 	private int w;
 	private int h;
 
 	private int sizeInWCase;
 	private Context context;
 
 	public CdlView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init(context);
 	}
 
 	public CdlView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init(context);
 	}
 
 	public CdlView(Context context) {
 		super(context);
 		init(context);
 	}
 
 	private void init(Context context) {
 		gestureDetector = new GestureDetector(context, this);
 		this.context = context;
 		CdlPalette.createDefaultColors();
 		String fontFaceFile = "fonts/Roboto/Roboto-BoldCondensed.ttf";
 		Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontFaceFile);
 		CdlPalette.setTypeFace(typeface);
 	}
 
 	@Override
 	protected void onLayout(boolean changed, int l, int t, int r, int b) {
 		super.onLayout(changed, l, t, r, b);
 		sized = false;
 	}
 
 	protected void onDraw(Canvas canvas) {
 		// CdlUtils.cdlLog(TAG, " onDraw  getW=" + getWidth() + " layout=" + cdlLayoutType);
 		super.onDraw(canvas);
		majMenu();
 		if (sized == false && cdlLayoutType != CDL_LAYOUT_ABSOLUTE) {
 			size();
 		}
 		if (cdlLayoutType == CDL_LAYOUT_GRID || cdlLayoutType == CDL_LAYOUT_ABSOLUTE) {
 			for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 				cdlBaseButton.draw(canvas);
 			}
 			for (CdlBaseButton cdlBaseButton : cdlBaseButtonsMenu) {
 				cdlBaseButton.draw(canvas);
 			}
 		}
 
 		if (cdlLayoutType == CDL_LAYOUT_FLOW) {
 			sizeInWCase = computeSizeInWCase();
 			int jPosBtn = 0;
 			for (int idBtn = 0; idBtn < cdlBaseButtons.size(); idBtn++) {
 				// CdlUtils.cdlLog(TAG, "draw 0: "+idBtn + " "+ cdlBaseButtons.get(idBtn).getLabel() );
 				if (cdlBaseButtons.get(idBtn).isVisible()) {
 					// CdlUtils.cdlLog(TAG, "draw 1: "+jPosBtn );
 					if ((jPosBtn * w_btn - startXScroll) < w) {
 						// CdlUtils.cdlLog(TAG, "draw 2: "+jPosBtn );
 						drawBtn(idBtn, jPosBtn, canvas);
 					}
 					jPosBtn += cdlBaseButtons.get(idBtn).getGrid_width();
 				}
 			}
 			drawScrollBar(canvas, sizeInWCase);
 		}
 	}
 
	protected void majMenu() {
 		CdlUtils.cdlLog(TAG, "majMenu ");
 		if (cdlBaseButtonsMenu.size() == 0)
 			return;
 		CdlBaseButton menuSelector = cdlBaseButtonsMenu.get(0);
 		if (isMustDrawMenu()) {
 			menuSelector.setHilight(true);
 			for (CdlBaseButton cdlBaseButton : cdlBaseButtonsMenu) {
 				cdlBaseButton.setVisible(true);
 			}
 
 		} else {
 			menuSelector.setHilight(false);
 			for (CdlBaseButton cdlBaseButton : cdlBaseButtonsMenu) {
 				if (cdlBaseButton.getId() > 1) {
 					cdlBaseButton.setVisible(false);
 				}
 			}
 		}
 		CdlUtils.cdlLog(TAG, "majMenu menuSelector hilight = " + menuSelector.isHilight());
 	}
 
 	private int getNbVisibleBtns() { // TODO optimize
 		int ret = 0;
 		for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 			if (cdlBaseButton.isVisible()) {
 				if (!cdlBaseButton.isFloatingPosition()) {
 					ret++;
 				}
 			}
 		}
 		return ret;
 	}
 
 	private int computeSizeInWCase() {
 		int jPosBtn = 0;
 		for (int idBtn = 0; idBtn < cdlBaseButtons.size(); idBtn++) {
 			if (cdlBaseButtons.get(idBtn).isVisible()) {
 				if (!cdlBaseButtons.get(idBtn).isFloatingPosition()) {
 					jPosBtn += cdlBaseButtons.get(idBtn).getGrid_width();
 				}
 			}
 		}
 		return jPosBtn * w_btn;
 	}
 
 	private void drawBtn(int idBtn, int jPosBtn, Canvas canvas) {
 		if (idBtn < 0 || idBtn >= cdlBaseButtons.size())
 			return;
 		CdlBaseButton cdlBaseButton = cdlBaseButtons.get(idBtn);
 		// CdlUtils.cdlLog(TAG, " drawbtn = " + cdlBaseButton.getLabel());
 		if (!cdlBaseButton.isFloatingPosition()) {
 			int sLeft = (int) (jPosBtn * w_btn - startXScroll);
 			cdlBaseButton.setSize(sLeft + padding, padding, w_btn * cdlBaseButton.getGrid_width() - padding, getHeight() - scrollBarHeight - padding);
 		}
 		cdlBaseButton.draw(canvas);
 	}
 
 	private void drawScrollBar(Canvas canvas, int sizeInWCase) {
 		if (cdlBaseButtons.size() == 0)
 			return;
 		if (sizeInWCase == 0)
 			return;
 		if (sizeInWCase <= w)
 			return;
 		int padding = 4;
 		int sLeft = getLeft() + padding;
 		int sRight = getRight() - padding;
 		urect.set(sLeft, getHeight() - scrollBarHeight, sRight, getHeight());
 		canvas.drawRect(urect, CdlPalette.getBlackPaint());
 		int lgr = (int) ((w * w) / sizeInWCase);
 		sLeft = (int) (startXScroll * w / sizeInWCase) + getLeft() + padding;
 		sRight = sLeft + lgr - padding;
 		if (sRight > getRight() - padding) {
 			sRight = getRight() - padding;
 		}
 		urect.set(sLeft - getLeft(), getHeight() - scrollBarHeight, sRight - getLeft(), getHeight());
 		canvas.drawRect(urect, CdlPalette.getHilightPaint());
 	}
 
 	protected void size() {
 		w = getWidth();
 		h = getHeight();
 		w_btn = w / grid_nbCols;
 		if (cdlLayoutType == CDL_LAYOUT_GRID) {
 			size_gridMode(w, h);
 
 		}
 		if (cdlLayoutType == CDL_LAYOUT_FLOW) {
 			size_flawMode(w, h);
 		}
 		size_menu(w, h);
 		sized = true;
 	}
 
 	private void size_menu(int w, int h) {
 		int id = 0;
 		int w_menu = getWidth() / 12;
 		int h_menu = getHeight() / 16;
 		for (CdlBaseButton cdlBaseButton : cdlBaseButtonsMenu) {
 			if (cdlBaseButton.isVisible()) {
 				if (id == 0) {
 					cdlBaseButton.setSize(0, 0, w_menu, h_menu / 2);
 				}
 				if (id == 1) {
 					cdlBaseButton.setSize(w_menu, 0, getWidth() - w_menu, h_menu / 2);
 				}
 				if (id > 1) {
 					cdlBaseButton.setSize(0, (id - 2) * h_menu + h_menu / 2, w_menu * 5, h_menu);
 				}
 				id++;
 			}
 		}
 	}
 
 	private void size_flawMode(int w, int h) {
 		int index = 0;
 		for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 			if (!cdlBaseButton.isFloatingPosition()) {
 				cdlBaseButton.setSize(w_btn * index, 0, w_btn, h); // real pos recomputed after
 				index++;
 			}
 		}
 	}
 
 	private void size_gridMode(int w, int h) {
 		int nbBtn = getNbVisibleBtns();
 		if (nbBtn == 0)
 			return;
 		if (grid_nbCols <= 0)
 			grid_nbCols = 1;
 		int nbRows = (nbBtn) / grid_nbCols;
 		int realNbRow = nbRows;
 		int maxGHforRow = 1;
 		int h_btn = h;
 		if (nbRows != 0) {
 			h_btn = h / nbRows;
 			if (h % nbRows != 0) {
 				h_btn++;
 			}
 		}
 		int col = 0;
 		int row = 0;
 		for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 			if (cdlBaseButton.isVisible()) {
 				if (!cdlBaseButton.isFloatingPosition()) {
 					cdlBaseButton.setPadding(padding);
 					cdlBaseButton.setSize(w_btn * col, h_btn * row, w_btn * cdlBaseButton.grid_width, h_btn * cdlBaseButton.grid_height);
 					col += cdlBaseButton.grid_width;
 					if (maxGHforRow < cdlBaseButton.grid_height) {
 						maxGHforRow = cdlBaseButton.grid_height;
 					}
 					if (cdlLayoutType == CDL_LAYOUT_GRID) {
 						if (col >= grid_nbCols || cdlBaseButton.equals(cdlBaseButtons.get(getNbVisibleBtns() - 1))) {
 							col = 0;
 							row += maxGHforRow;
 							if (row > realNbRow) {
 								realNbRow = row;
 							}
 							maxGHforRow = 1;
 						}
 					}
 				}
 			}
 		}
 		// CdlUtils.cdlLog(TAG, "realrow=" + realNbRow + " row=" + row);
 		if (realNbRow != nbRows) {
 			maxGHforRow = 1;
 			h_btn = h / realNbRow;
 			if (nbRows != 0) {
 				if (h % nbRows != 0) {
 					h_btn++;
 				}
 			}
 			row = 0;
 			for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 				if (!cdlBaseButton.isFloatingPosition()) {
 					if (cdlBaseButton.isVisible()) {
 						cdlBaseButton.setSize(w_btn * col, h_btn * row, w_btn * cdlBaseButton.grid_width, h_btn * cdlBaseButton.grid_height);
 						col += cdlBaseButton.grid_width;
 						if (maxGHforRow < cdlBaseButton.grid_height) {
 							maxGHforRow = cdlBaseButton.grid_height;
 						}
 						if (cdlLayoutType == CDL_LAYOUT_GRID) {
 							if (col >= grid_nbCols) {
 								col = 0;
 								row += maxGHforRow;
 								maxGHforRow = 1;
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	// just for flaw mode
 	protected int getIdBtnFromX(float x) {
 		int id = 0;
 		for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 			if (cdlBaseButton.isVisible()) {
 				if (cdlBaseButton.getLeft() < x && cdlBaseButton.getRight() > x) {
 					return id;
 				}
 			}
 			id++;
 		}
 		return id;
 	}
 
 	public void addCdlBaseButton(CdlBaseButton cdlBaseButton) {
 		int color = cdlBaseButtons.size() % (CdlPalette.getLastColorIndex());// avoid first color index
 		cdlBaseButton.setBackgroundColor(color + 1);
 		cdlBaseButton.setId(cdlBaseButtons.size());
 		cdlBaseButtons.add(cdlBaseButton);
 	}
 
 	public void addCdlBaseButtonMenu(CdlBaseButton cdlBaseButton) {
 		int color = cdlBaseButtons.size() % (CdlPalette.getLastColorIndex());// avoid first color index
 		cdlBaseButton.setBackgroundColor(color + 1);
 		cdlBaseButton.setId(cdlBaseButtonsMenu.size());
 		cdlBaseButtonsMenu.add(cdlBaseButton);
 	}
 
 	public void setButtonsEnabled(boolean b) {
 		for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 			cdlBaseButton.setEnabled(b);
 		}
 	}
 
 	public boolean onTouchEvent(MotionEvent event) {
 		gestureDetector.onTouchEvent(event);
 		invalidate();
 		return true;
 	}
 
 	@Override
 	public boolean onDown(MotionEvent arg0) {
 		return false;
 	}
 
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 		return false;
 	}
 
 	@Override
 	public void onLongPress(MotionEvent e) {
 		if (cdlLayoutType == CDL_LAYOUT_ABSOLUTE || cdlLayoutType == CDL_LAYOUT_GRID) {
 			for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 				if (cdlBaseButton.isXYInControl(e.getX(), e.getY())) {
 					longPressOnCdlBaseButton(cdlBaseButton, e);
 				}
 			}
 			return;
 		}
 		if (cdlLayoutType == CDL_LAYOUT_FLOW) {
 			int idBtn = getIdBtnFromX(e.getX());
 			if (idBtn < 0 || idBtn >= cdlBaseButtons.size())
 				return;
 			CdlBaseButton cdlBaseButton = cdlBaseButtons.get(idBtn);
 			longPressOnCdlBaseButton(cdlBaseButton, e);
 			return;
 		}
 		return;
 	}
 
 	private void longPressOnCdlBaseButton(CdlBaseButton cdlBaseButton, MotionEvent e) {
 		if (cdlBaseButton.isVisible() && cdlBaseButton.isEnabled()) {
 			if (cdlBaseButton.isFlashCapable()) {
 				timerCountFlash = 2;
 				flashingBtn = cdlBaseButton;
 				runnableFlash.run();
 				cdlBaseButton.setFlashing(true);
 				invalidate(cdlBaseButton.getRect());
 			}
 			cdlBaseButton.longPress(e);
 		}
 	}
 
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
 		// CdlUtils.cdlLog(TAG, "onScroll dx:" + distanceX);
 		if (cdlLayoutType == CDL_LAYOUT_FLOW) {
 			sizeInWCase = computeSizeInWCase();
 			if (sizeInWCase <= w) {
 				startXScroll = 0;
 				return false;
 			}
 
 			int d = (int) distanceY;
 			if (Math.abs((int) distanceX) > Math.abs((int) distanceY)) {
 				d = (int) distanceX;
 			}
 			startXScroll += d;
 			if (startXScroll < 0 - padding) {
 				startXScroll = -padding;
 			}
 			if (startXScroll > (sizeInWCase - w) + 2 * padding) {
 				startXScroll = (sizeInWCase - w) + 2 * padding;
 			}
 			invalidate();
 		}
 		if (cdlLayoutType == CDL_LAYOUT_ABSOLUTE || cdlLayoutType == CDL_LAYOUT_GRID) {
 			for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 				if (cdlBaseButton.isXYInControl(e1.getX(), e1.getY())) {
 					cdlBaseButton.scroll(e1, e2, distanceX, distanceY);
 					invalidate(cdlBaseButton.getRect());
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void onShowPress(MotionEvent e) {
 	}
 
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		CdlUtils.cdlLog(TAG, "onSingleTapUp");
 		boolean change = clickOnMenu(e);
 		if (isMustDrawMenu() == true) {
 			if (!change) {
 				setMustDrawMenu(false);
 			}
 			return false;
 		}
 		if (cdlLayoutType == CDL_LAYOUT_ABSOLUTE || cdlLayoutType == CDL_LAYOUT_GRID) {
 			for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 				if (cdlBaseButton.isXYInControl(e.getX(), e.getY())) {
 					tapUpOnCdlBaseButton(cdlBaseButton, e);
 				}
 			}
 			return false;
 		}
 		if (cdlLayoutType == CDL_LAYOUT_FLOW) {
 			int idBtn = getIdBtnFromX(e.getX());
 			// CdlUtils.cdlLog(TAG, "onSingleTapUp idbtn=" + idBtn + " max=" + cdlBaseButtons.size());
 			if (idBtn < 0 || idBtn >= cdlBaseButtons.size())
 				return false;
 			CdlBaseButton cdlBaseButton = cdlBaseButtons.get(idBtn);
 			tapUpOnCdlBaseButton(cdlBaseButton, e);
 			return false;
 		}
 
 		return false;
 	}
 
 	private boolean clickOnMenu(MotionEvent e) {
 		if ( cdlBaseButtonsMenu.size()==0) return false;
 		boolean ret = false;
 		CdlBaseButton menuSelector = cdlBaseButtonsMenu.get(0);
 		CdlUtils.cdlLog(TAG, "Click on menu " + menuSelector.isHilight());
 		if (menuSelector.isVisible()) {
 			if (menuSelector.getRect().contains((int) e.getX(), (int) e.getY())) {
 				setMustDrawMenu(!isMustDrawMenu());
 				CdlUtils.cdlLog(TAG, "click on selector");
 				invalidate();
 				ret =true;
 			}
 		}
 		for (CdlBaseButton cdlBaseButton : cdlBaseButtonsMenu) {
 			if (cdlBaseButton.isVisible()) {
 				if (cdlBaseButton.isXYInControl(e.getX(), e.getY())) {
 					CdlUtils.cdlLog(TAG, "clickOnMenu click= " + cdlBaseButton.getLabel());
 					tapUpOnCdlBaseButton(cdlBaseButton, e);
 				}
 			}
 		}
 		CdlUtils.cdlLog(TAG, "draw  menu= " + isMustDrawMenu());
 		return ret;
 	}
 
 	private void tapUpOnCdlBaseButton(CdlBaseButton cdlBaseButton, MotionEvent e) {
 		if (cdlBaseButton.isVisible() && cdlBaseButton.isEnabled()) {
 			if (cdlBaseButton.isFlashCapable()) {
 				timerCountFlash = 2;
 				flashingBtn = cdlBaseButton;
 				runnableFlash.run();
 				cdlBaseButton.setFlashing(true);
 			}
 			cdlBaseButton.singleTapUp(e);
 			invalidate(cdlBaseButton.getRect());
 			// CdlUtils.cdlLog(TAG, "invalidate : " + cdlBaseButton.getLabel());
 		}
 	}
 
 	public CdlBaseButton getButtonFromLabel(String s) {
 		for (CdlBaseButton cdlBaseButton : cdlBaseButtons) {
 			if (cdlBaseButton.getLabel().equals(s)) {
 				return cdlBaseButton;
 			}
 		}
 		return null;
 	}
 
 	public static void drawCenterTextInrectCase(Canvas canvas, String text, Paint paint) {
 		paint.getTextBounds(text, 0, text.length(), bounds);
 		int x = (int) (rectf.left + rectf.width() / 2 - bounds.centerX());
 		int y = (int) (rectf.top + rectf.height() / 2 - bounds.centerY());
 		canvas.drawText(text, x, y, paint);
 	}
 
 	public CdlBaseButton getButtonFromId(int id) {
 		return cdlBaseButtons.get(id);
 	}
 
 	public void setCdlLayoutType(int cdlLayout) {
 		this.cdlLayoutType = cdlLayout;
 	}
 
 	public void setGrid_nbCols(int grid_nbCols) {
 		this.grid_nbCols = grid_nbCols;
 	}
 
 	public void setPadding(int padding) {
 		this.padding = padding;
 	}
 
 	public List<CdlBaseButton> getCdlBaseButtons() {
 		return cdlBaseButtons;
 	}
 
 	public int getStartXScroll() {
 		return startXScroll;
 	}
 
 	public void setStartXScroll(int startXScroll) {
 		this.startXScroll = startXScroll;
 	}
 
 	public boolean isMustDrawMenu() {
 		return mustDrawMenu;
 	}
 
 	public void setMustDrawMenu(boolean mustDrawMenu) {
 		this.mustDrawMenu = mustDrawMenu;
 	}
 
 }
