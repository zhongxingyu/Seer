 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.Image;
 import java.lang.Long;
 import java.lang.Math;
 
 
 /**
  * Board user interface.
  *
  * This class manages board displaying. It is dumb about
  * what is being one it.
  */
 class BoardView extends UIElementCommon {
 	Image boardImage = null;
 	/**
 	 * Stone size in pixels.
 	 */
 	int stoneSize = 11;
 	long lastTime = 0;
 	long firstTime = 0;
 
 	/**
 	 * Board size in stones.
 	 */
 	int boardSize;
 
 	//int boardColor = 0x0909090;
 	int boardColor = 0x0939565;
 	//int lineColor = 0x0303030;
 	int lineColor = 0x054553a;
 	//int backgroundColor= 0x000A000;
 	int backgroundColor= 0x0075a11;
 	int whiteStoneColor= 0x0FFFFFF;
 	int blackStoneColor= 0x0000000;
 	int koColor = 0xEF00000;
 
 	/**
 	 * Croshair X in stones.
 	 */
 	int cx = 0;
 
 	/**
 	 * Croshair Y in stones.
 	 */
 	int cy = 0;
 
 
 	/**
 	 * Board offset on the screen - X;
 	 */
 	int sx;
 	int lastSx;
 
 	/**
 	 * Board offset on the screen - Y;
 	 */
 	int sy;
 	int lastSy;
 
 	/**
 	 * Board background visible.
 	 */
 	boolean bgRefreshNeeded;
 
 	/**
 	 * Ctor.
 	 */
 	public BoardView(Parent parent) {
 		super(parent);
		firstTime = System.currentTimeMillis();
 		setAutoCellSize();
 	}
 	
 	protected void setAutoCellSize() {
 		stoneSize = parent.getXSize() / 22;
 		if (stoneSize % 2 == 0) {
 			stoneSize++;
 		}
 	}
 
 	public void resetBoard(int boardSize, int stoneSize) {
 
 		int oldcx = getStoneX(cx);
 		int oldcy = getStoneY(cy);
 		setBoardSize(boardSize);
 		setStoneSize(stoneSize);
 
 		lastSx -= getStoneX(cx) - oldcx;
 		lastSy -= getStoneY(cy) - oldcy;
 
 		recreateBoardImage();
 		drawEmptyBoard();
 		int c = boardSize / 2;
 		setCrosshairPosition(c, c);
 
 		markDirty();
 	}
 
 	public void drawEmptyStone(int x, int y, int ss) {
 		Graphics g = boardImage.getGraphics();
 		int cx = getStoneX(x);
 		int cy = getStoneY(y);
 
 		g.setColor(lineColor);
 		float w = stoneSize / 2;
 		if (x != 0) {
 			g.drawLine((int)(cx - w), cy, cx, cy);
 		}
 		if (y != 0) {
 			g.drawLine(cx, (int)(cy - w), cx, cy);
 		}
 		if (x != boardSize - 1) {
 			g.drawLine((int)(cx + w), cy, cx, cy);
 		}
 		if (y != boardSize - 1) {
 			g.drawLine(cx, (int)(cy + w), cx, cy);
 		}
 
 		if (boardSize == 19) {
 			if ((x + 3) % 6  == 0 &&
 				(y + 3) % 6  == 0) {
 				g.drawLine(cx-1, cy-1, cx+1, cy+1);
 				g.drawLine(cx-1, cy+1, cx+1, cy-1);
 				}
 		}
 	}
 
 	public void drawStone(int x, int y, int color, int state) {
 		markDirty();
 
 		int cx = getStoneX(x);
 		int cy = getStoneY(y);
 		int ss = stoneSize - 2;
 		if (ss == 1) { ss = 3; }
 		int gx = cx - ss / 2;
 		int gy = cy - ss / 2;
 
 		Graphics g = boardImage.getGraphics();
 		switch (color) {
 			case Board.COLOR_BLACK:
 				g.setColor(blackStoneColor);
 				break;
 			case Board.COLOR_WHITE:
 				g.setColor(whiteStoneColor);
 				break;
 			case Board.COLOR_NOTHING:
 				g.setColor(boardColor);
 				break;
 		}
 		if (color == Board.COLOR_NOTHING) {
 			g.fillRect(gx, gy, ss, ss);
 		} else {
 			g.fillRoundRect(gx, gy, ss, ss, ss, ss);
 		}
 
 		if (color == Board.COLOR_NOTHING) {
 			drawEmptyStone(x, y, ss);
 		}
 		if (state == Board.STATE_LAST) {
 			int sss = (stoneSize - 1) / 2;
 			sss /= 2;
 			sss *= 2;
 			if (sss < 0) { sss = 0; }
 			int gxx = cx - sss / 2;
 			int gyy = cy - sss / 2;
 
 			switch (color) {
 				case Board.COLOR_WHITE:
 					g.setColor(blackStoneColor);
 					break;
 				case Board.COLOR_BLACK:
 					g.setColor(whiteStoneColor);
 					break;
 			}
 			if (sss <= 2) {
 				g.drawLine(cx, cy, cx, cy);
 			} else {
 				g.drawRoundRect(gxx, gyy, sss, sss, sss, sss);
 			}
 		}
 		if (state == Board.STATE_KO) {
 			int sss = (stoneSize - 1) / 3;
 			sss /= 2;
 			sss *= 2;
 			if (sss < 0) { sss = 0; }
 			int gxx = cx - sss / 2;
 			int gyy = cy - sss / 2;
 
 			g.setColor(koColor);
 			if (sss <= 2) {
 				g.drawLine(cx, cy, cx, cy);
 			} else {
 				g.drawRoundRect(gxx, gyy, sss, sss, sss, sss);
 			}
 		}
 
 	}
 
 	public void setCrosshairPosition(int x, int y) {
 		markDirty();
 
 		if (x < 0) {
 			x = 0;
 		} else if (x >= boardSize) {
 			x = boardSize - 1;
 		}
 		if (y < 0) {
 			y = 0;
 		} else if (y >= boardSize) {
 			y = boardSize - 1;
 		}
 
 		cx = x;
 		cy = y;
 	}
 
 	public int getStoneSize() {
 		return stoneSize;
 	}
 
 	public int getCrosshairX() {
 		return cx;
 	}
 
 	public int getCrosshairY() {
 		return cy;
 	}
 
 	protected void drawEmptyBoard() {
 		Graphics g = boardImage.getGraphics();
 		g.setColor(boardColor);
 		g.fillRect(0, 0, boardImage.getWidth(), boardImage.getHeight());
 
 		g.setColor(lineColor);
 		for (int i = 0; i < boardSize; ++i) {
 			int x = getStoneX(i);
 			int y1 = getStoneY(0);
 			int y2 = getStoneY(boardSize - 1);
 			g.drawLine(x, y1, x, y2);
 
 			int y = getStoneY(i);
 			int x1 = getStoneX(0);
 			int x2 = getStoneX(boardSize - 1);
 			g.drawLine(x1, y, x2, y);
 		}
 
 		if (boardSize == 19) {
 			for (int x = 0; x < boardSize; ++x) {
 				if ((x + 3) % 6  == 0) {
 					for (int y = 0; y < boardSize; ++y) {
 						if ((y + 3) % 6  == 0) {
 							drawStone(x, y, Board.COLOR_NOTHING, Board.STATE_NORMAL);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	protected void recreateBoardImage() {
 		int size = boardImageSize() + 1;
 		bgRefreshNeeded = true;
 
 		boardImage = Image.createImage(size, size);
 		Graphics g = boardImage.getGraphics();
 	}
 
 	protected void setBoardSize(int size) {
 		boardSize = size;
 	}
 
 	protected void setStoneSize(int size) {
 		int maxSize = Math.min(parent.getXDiv(), parent.getYDiv()) / 3;
 		if (size < 3) {
 			size = 3;
 		} else if (size > 30) {
 			size = 30;
 		}
 		stoneSize = size;
 	}
 
 	protected int boardImageSize() {
 		return (1 + boardSize) * stoneSize;
 	}
 
 	/**
 	 * Get offset of the stone on some position relative to board (in pixels).
 	 */
 	protected int getStoneX(int i) {
 		return (int)((1.0 + i) * stoneSize);
 	}
 
 	protected int getStoneY(int i) {
 		return getStoneX(i);
 	}
 
 	int lastXDiv, lastYDiv;
 
 	/**
 	 * Recheck if background is visible and if board needs any offset.
 	 */
 	void checkBoardOffset() {
 		int marginSensitivity = stoneSize * 2;
 		sx = sy = 0;
 		int xDiv = parent.getXDiv();
 		int yDiv = parent.getYDiv();
 
 		if (boardImage == null) {
 			bgRefreshNeeded = true;
 			return;
 		}
 
 		bgRefreshNeeded |= (lastXDiv != xDiv);
 		bgRefreshNeeded |= (lastYDiv != yDiv);
 
 		if (xDiv > boardImage.getWidth()) {
 			sx = (xDiv - boardImage.getWidth()) / 2;
 			bgRefreshNeeded |= true;
 		} else {
 			sx = lastSx;
 			int x = getStoneX(cx);
 			if (x + sx <= marginSensitivity) {
 				bgRefreshNeeded |= true;
 				sx = - (x - getStoneX(1));
 			} else if (x + sx >= xDiv - marginSensitivity) {
 				bgRefreshNeeded |= true;
 				sx = - (x - (xDiv - getStoneX(1)));
 			}
 		}
 
 		if (yDiv > boardImage.getHeight()) {
 			sy = (yDiv - boardImage.getHeight()) / 2;
 			bgRefreshNeeded |= true;
 		} else {
 			sy = lastSy;
 			int y = getStoneY(cy);
 			if (y + sy <= marginSensitivity) {
 				bgRefreshNeeded |= true;
 				sy = - (y - getStoneY(1));
 			} else if (y + sy >= yDiv - marginSensitivity) {
 				bgRefreshNeeded |= true;
 				sy = - (y - (yDiv - getStoneY(1)));
 			}
 		}
 		lastSx = sx;
 		lastSy = sy;
 		lastXDiv = xDiv;
 		lastYDiv = yDiv;
 
 	}
 
 	protected void repaint(Graphics g) {
 		g.setClip(0, 0, parent.getXDiv(), parent.getYDiv());
 
 		if (boardImage != null) {
 			checkBoardOffset();
 		}
 
 		if (bgRefreshNeeded || boardImage == null) {
 			g.setColor(backgroundColor);
 			g.fillRect(0, 0, parent.getXDiv(), parent.getYDiv());
 			bgRefreshNeeded = false;
 		}
 
 		if (boardImage != null) {
 			g.drawImage(boardImage, sx, sy, Graphics.TOP|Graphics.LEFT);
 		}
 
 	}
 
 	/**
 	 * Repaint board.
 	 *
 	 * Board need redrawing crosshair every time, so it overloads
 	 * paint function.
 	 */
 	public void paint(Graphics g) {
 		super.paint(g);
 		paintCrosshair(g);
 		bgRefreshNeeded = false;
 		lastTime = System.currentTimeMillis();
 	}
 
 	void paintCrosshair(Graphics g) {
 		if (boardImage == null) {
 			return;
 		}
 
 		g.clipRect(0, 0, parent.getXDiv(), parent.getYDiv());
 
 		if (parent.isActive()) {
 			long time = System.currentTimeMillis();
 			int c = (int)(Math.sin((firstTime - time) / 100) * 100) + 128;
 			g.setColor(((c * 256) + c) * 256 + c );
 		} else {
 			int c = 128;
 			g.setColor(((c * 256) + c) * 256 + c );
 		}
 
 		int x = getStoneX(cx) + sx;
 		int y = getStoneY(cy) + sy;
 
 		g.drawLine(x-1, y, x+1, y);
 		g.drawLine(x, y-1, x, y+1);
 	}
 
 }
