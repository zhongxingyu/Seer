 package mmb.foss.aueb.icong;
 
 import java.util.ArrayList;
 
 import mmb.foss.aueb.icong.boxes.Box;
 import mmb.foss.aueb.icong.boxes.SavedState;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class DrawableAreaView extends View {
 
 	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	private ArrayList<Box> boxes = new ArrayList<Box>();
 	private Context mContext;
 	private Box selectedBox = null;
 	private int pressedX, pressedY;
 	private int originalX, originalY;
 	private int[] buttonCenter = new int[2];
 	private int WIDTH, HEIGHT;
 	private ArrayList<BoxButtonPair[]> lines = new ArrayList<BoxButtonPair[]>();
 	private int selectedButton = -1;
 	private Box box = null;
 	private Box selectedButtonBox = null;
 	int buttonPressed = -1;
 	int buttonHovered = -1;
 	boolean drawingline = false;
 	boolean foundPair = false;
 	private int lineStartX, lineStartY, lineCurrentX, lineCurrentY;
 
 	public DrawableAreaView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO Auto-generated constructor stub
 		mContext = context;
 		paint.setColor(Color.BLACK);
 		WIDTH = MainActivity.width;
 		HEIGHT = MainActivity.height;
 		boxes = SavedState.getBoxes();
 		lines = SavedState.getLines();
 	}
 
 	protected void onDraw(Canvas c) {
 		if (WIDTH == 0) {
 			WIDTH = this.getWidth();
 			HEIGHT = this.getHeight();
 		}
 		for (Box box : boxes) {
 			// TODO: Zooming to be removed
 			box.setZoom(1.3);
 			c.drawBitmap(box.getBitmap(), box.getX(), box.getY(), null);
 			for (int i = 0; i < box.getNumOfButtons(); i++) {
 				if (box.isPressed(i)) {
 					buttonCenter = box.getButtonCenter(i);
 					c.drawCircle(buttonCenter[0], buttonCenter[1],
 							box.getButtonRadius(i), paint);
 				}
 			}
 		}
 		for (BoxButtonPair[] line : lines) {
 			Box box0 = line[0].getBox(), box1 = line[1].getBox();
 			int button0 = line[0].getButton(), button1 = line[1].getButton();
 			int[] center0 = box0.getButtonCenter(button0), center1 = box1
 					.getButtonCenter(button1);
 			c.drawLine(center0[0], center0[1], center1[0], center1[1], paint);
 		}
 		if (drawingline) {
 			c.drawLine(lineStartX, lineStartY, lineCurrentX, lineCurrentY,
 					paint);
 		}
 	}
 
 	public void addBox(Box box) {
 		int x, y;
 		if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
 			y = getLower() + 15;
 			x = (WIDTH / 2) - (box.getWidth() / 2);
 		} else {
 			y = (HEIGHT / 2) - (box.getHeight() / 2);
 			x = getRighter() + 15;
 		}
 		box.setY(y);
 		box.setX(x);
 		boxes.add(box);
 		SavedState.addBox(box);
 		invalidate();
 	}
 
 	public boolean onTouchEvent(MotionEvent event) {
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			box = getBoxTouched((int) event.getX(), (int) event.getY());
 			if (box != null) {
 				selectedBox = box;
 				buttonPressed = box.isButton((int) event.getX(),
 						(int) event.getY());
 
 				if (buttonPressed == -1) {
 					pressedX = (int) event.getX();
 					pressedY = (int) event.getY();
 					originalX = box.getX();
 					originalY = box.getY();
 				} else {
 					// my code
 					Log.e("wtf", "a " + buttonPressed);
 					if (!((buttonPressed + 1) <= box.getNoOfInputs())) {
 						int[] center = box.getButtonCenter(buttonPressed);
 						lineStartX = center[0];
 						lineStartY = center[1];
						lineCurrentX = lineStartX;
						lineCurrentY = lineStartY;
 						box.setButtonPressed(buttonPressed);
 						drawingline = true;
 					} else {
 						if (box.isPressed(buttonPressed)) {
 							box.unsetButtonPressed(buttonPressed);
 
 							BoxButtonPair pair = new BoxButtonPair(box,
 									buttonPressed);
 							boolean found = false;
 							for (BoxButtonPair[] line : lines) {
 								if (found = line[0].equals(pair)) {
 									selectedButtonBox = line[1].getBox();
 									selectedButton = line[1].getButton();
 									lines.remove(line);
 									SavedState.removeLine(line);
 									selectedButtonBox
 											.unsetButtonPressed(selectedButton);
 									break;
 								} else if (found = line[1].equals(pair)) {
 									selectedButtonBox = line[0].getBox();
 									selectedButton = line[0].getButton();
 									lines.remove(line);
 									SavedState.removeLine(line);
 									selectedButtonBox
 											.unsetButtonPressed(selectedButton);
 									break;
 								}
 							}
 							if (!found) {
 								selectedButton = -1;
 								selectedButtonBox = null;
 							}
 
 						}
 					}
 					invalidate();
 					selectedBox = null;
 				}
 			}
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if (selectedBox != null) {
 				selectedBox.setX((int) event.getX() - (pressedX - originalX));
 				selectedBox.setY((int) event.getY() - (pressedY - originalY));
 				invalidate();
 			}
 			if (drawingline) {
 				lineCurrentX = (int) event.getX();
 				lineCurrentY = (int) event.getY();
 				Box boxHovered = getBoxTouched((int) event.getX(),
 						(int) event.getY());
 				if (boxHovered != null) {
 					buttonHovered = boxHovered.isButton((int) event.getX(),
 							(int) event.getY());
 					if (buttonHovered != -1) {
 						if (((buttonHovered + 1) <= boxHovered.getNoOfInputs())) {
 							int[] center = boxHovered
 									.getButtonCenter(buttonHovered);
 							lineStartX = center[0];
 							lineStartY = center[1];
 							boxHovered.setButtonPressed(buttonHovered);
 
 							drawingline = false;
 							BoxButtonPair[] line = {
 									new BoxButtonPair(box, buttonPressed),
 									new BoxButtonPair(boxHovered, buttonHovered) };
 							lines.add(line);
 							SavedState.addLine(line);
 							foundPair = true;
 						}
 					}
 				}
 			}
 			invalidate();
 			break;
 		case MotionEvent.ACTION_UP:
 			drawingline = false;
 			selectedBox = null;
 			if (!foundPair && buttonPressed != -1 && box != null) {
 				box.unsetButtonPressed(buttonPressed);
 			}
 			pressedX = pressedY = originalX = originalY = 0;
 			// TODO implement here to pou peftei
 			invalidate();
 			return false;
 		}
 		return true;
 	}
 
 	// returns the lower pixel of the lower element
 	private int getLower() {
 		int y = 0;
 		for (Box box : boxes) {
 			if (y < box.getYY())
 				y = box.getYY();
 		}
 		return y;
 	}
 
 	// returns the righter pixel of the righter element
 	private int getRighter() {
 		int x = 0;
 		for (Box box : boxes) {
 			if (x < box.getXX())
 				x = box.getXX();
 		}
 		return x;
 	}
 
 	// returns the box that was touched
 	private Box getBoxTouched(int x, int y) {
 		for (Box b : boxes) {
 			if (b.isOnBox(x, y)) {
 				return b;
 			}
 		}
 		return null;
 	}
 
 }
