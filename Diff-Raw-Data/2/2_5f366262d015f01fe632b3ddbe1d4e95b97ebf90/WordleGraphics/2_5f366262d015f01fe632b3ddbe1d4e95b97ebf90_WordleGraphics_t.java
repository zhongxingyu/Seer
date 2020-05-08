 package cornell.eickleapp;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Rect;
 import android.view.View;
 
 public class WordleGraphics extends View {
 
 	Context mContext;
 	ArrayList<String[]> nestArrayDrinkList = new ArrayList<String[]>();
 	ArrayList<String[]> nestArrayNoDrinkList = new ArrayList<String[]>();
 	String word, title;
 	int count;
 	ArrayList<Rect> wordRectList = new ArrayList<Rect>();
 	int topEdge = 0;
 
 	public WordleGraphics(Context context, ArrayList<String[]> d,
 			ArrayList<String[]> nd, String t) {
 		super(context);
 		mContext = context;
 		nestArrayDrinkList = d;
 		nestArrayNoDrinkList = nd;
 		title = t;
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		// TODO Auto-generated method stub
 		super.onDraw(canvas);
 
 		float leftX = canvas.getWidth() / 4;
 		float rightX = 3 * canvas.getWidth() / 4;
 
 		// left/drink cloud
		
		
 		setUpDrinkCloud(canvas, true);
 
 		/*
 		 * line to divide the cloud values Paint linePaint = new Paint();
 		 * linePaint.setStrokeWidth(10); linePaint.setTextAlign(Align.CENTER);
 		 * linePaint.setColor(Color.rgb(242, 147, 39));
 		 * canvas.drawLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2,
 		 * canvas.getHeight(), linePaint);
 		 */
 		// right/no drink cloud
 		setUpDrinkCloud(canvas, false);
 
 	}
 
 	private int getTotalCount(ArrayList<String[]> values) {
 		int sum = 0;
 		for (int i = 0; i < values.size(); i++) {
 			sum += Integer.parseInt(values.get(i)[1]);
 		}
 		return sum;
 	}
 
 	private void setUpDrinkCloud(Canvas canvas, Boolean drink) {
 		
 		wordRectList=new ArrayList<Rect>();
 		ArrayList<String[]> arrayList;
 		if (drink)
 			arrayList = nestArrayDrinkList;
 		else
 			arrayList = nestArrayNoDrinkList;
 
 		Paint titlePaint = new Paint();
 		titlePaint.setTextSize(60);
 		titlePaint.setTextAlign(Align.CENTER);
 		titlePaint.setColor(Color.rgb(0, 153, 204));
 		if (drink) {
 			canvas.drawText("Drinking", canvas.getWidth() / 2, 50, titlePaint);
 			topEdge = 50;
 		} else if (!drink) {
 			canvas.drawText("Non-Drinking", canvas.getWidth() / 2,
 					canvas.getHeight() / 2 + 50, titlePaint);
 			topEdge = 50;
 		}
 		if (arrayList == null) {
 
 			if (drink) {
 				canvas.drawText("N/A", canvas.getWidth() / 2,
 						canvas.getHeight() / 4, titlePaint);
 
 			} else if (!drink) {
 				canvas.drawText("N/A", canvas.getWidth() / 2,
 						3 * canvas.getHeight() / 4, titlePaint);
 			}
 		}
 
 		if (arrayList != null) {
 			titlePaint.setTextAlign(Align.LEFT);
 			
 //<---size>			
 			for (int n = 0; n < arrayList.size(); n++) {
 				word = arrayList.get(n)[0];
 				count = Integer.valueOf(arrayList.get(n)[1]);
 
 				Paint textPainter = new Paint();
 				textPainter.setTextSize(16);
 				if (drink)
 					textPainter.setColor(Color.rgb(247, 144, 30));
 				else if (!drink)
 					textPainter.setColor(Color.rgb(14, 109, 97));
 
 				// The sizes should be relative to the total amount
 				int total_cnt = getTotalCount(arrayList);
 
 				double size = 300 * (count / Double.valueOf(total_cnt));
 				if ((int) size > 50) {
 					size = 30.0;
 				}
 
 				// setup the text
 				textPainter.setTextSize((int) size);
 				textPainter.setTextAlign(Align.LEFT);
 				Rect wordBound = new Rect();
 				textPainter.getTextBounds(word, 0, word.length(), wordBound);
 				// root of the branching
 
 				if (n == 0) {
 					if (drink) {
 						canvas.drawText(word, 0, topEdge + wordBound.height(),
 								textPainter);
 						wordBound.set(0, topEdge, wordBound.width(), topEdge
 								+ wordBound.height());
 					} else if (!drink) {
 						canvas.drawText(word, 0, topEdge + canvas.getHeight()
 								/ 2 + wordBound.height(), textPainter);
 
 						wordBound.set(0, topEdge + canvas.getHeight() / 2,
 								wordBound.width(), topEdge + canvas.getHeight()
 										/ 2 + wordBound.height());
 					}
 					wordRectList.add(wordBound);
 				} else {
 					boolean continueSearch = true;
 					boolean intersection = false;
 					while (continueSearch) {
 						intersection = false;
 						Random random = new Random();
 						int randomNode = random.nextInt(wordRectList.size());
 
 						int randomSide = random.nextInt(5 - 1) + 1;
 
 						Rect parentRect = wordRectList.get(randomNode);
 //<---switch>						
 						switch (randomSide) {
 						// <-------------------right side branching
 						case 1:
 							int parentRightSideX = parentRect.right;
 							int parentRightSideY = parentRect.bottom;
 							// checks if intersects anything
 							wordBound.set(parentRightSideX, parentRightSideY
 									- wordBound.height(), parentRightSideX
 									+ wordBound.width(), parentRightSideY);
 							intersection = false;
 							for (int i = 0; i < wordRectList.size(); i++) {
 								if (Rect.intersects(wordBound,
 										wordRectList.get(i))) {
 									intersection = true;
 								}
 							}
 							if (!intersection) {
 								if (checkBoundary(wordBound, canvas, drink)) {
 									canvas.drawText(word, parentRightSideX,
 											parentRightSideY, textPainter);
 									continueSearch = false;
 								}
 
 							}
 							break;
 						// <-------------------bottom side branching
 						case 2:
 							int parentSideX = parentRect.left;
 							int parentSideY = parentRect.bottom;
 							// checks if intersects anything
 							wordBound.set(parentSideX, parentSideY, parentSideX
 									+ wordBound.width(), parentSideY
 									+ wordBound.height());
 							intersection = false;
 							for (int i = 0; i < wordRectList.size(); i++) {
 								if (Rect.intersects(wordBound,
 										wordRectList.get(i))) {
 									intersection = true;
 								}
 							}
 							if (!intersection) {
 								if (checkBoundary(wordBound, canvas, drink)) {
 									canvas.drawText(word, parentSideX,
 											parentSideY + wordBound.height(),
 											textPainter);
 									continueSearch = false;
 								}
 
 							}
 							break;
 						// <-------------------Left side branching
 						case 3:
 							int parentLeftSideX = parentRect.left;
 							int parentLeftSideY = parentRect.bottom;
 							// checks if intersects anything
 							wordBound.set(parentLeftSideX - wordBound.width(),
 									parentLeftSideY - wordBound.height(),
 									parentLeftSideX, parentLeftSideY);
 							intersection = false;
 							for (int i = 0; i < wordRectList.size(); i++) {
 								if (Rect.intersects(wordBound,
 										wordRectList.get(i))) {
 									intersection = true;
 								}
 							}
 							if (!intersection) {
 								if (checkBoundary(wordBound, canvas, drink)) {
 									canvas.drawText(word, parentLeftSideX
 											- wordBound.width(),
 											parentLeftSideY, textPainter);
 									continueSearch = false;
 								}
 
 							}
 							break;
 						// <-------------------Top side branching
 						case 4:
 							int parentTopSideX = parentRect.left;
 							int parentTopSideY = parentRect.top;
 							// checks if intersects anything
 							wordBound.set(parentTopSideX, parentTopSideY
 									- wordBound.height(), parentTopSideX
 									+ wordBound.width(), parentTopSideY);
 							intersection = false;
 							for (int i = 0; i < wordRectList.size(); i++) {
 								if (Rect.intersects(wordBound,
 										wordRectList.get(i))) {
 									intersection = true;
 								}
 							}
 							if (!intersection) {
 								if (checkBoundary(wordBound, canvas, drink)) {
 									canvas.drawText(word, parentTopSideX,
 											parentTopSideY, textPainter);
 									continueSearch = false;
 								}
 
 							}
 							break;
 						}
 
 					}
 				}
 
 				// saves so we can pull up the corner to slap in more words
 				wordRectList.add(wordBound);
 
 			}
 		}
 	}
 
 	private Boolean checkBoundary(Rect bound, Canvas canvas, Boolean drink) {
 		if (drink) {
 			if (bound.left >= 0 && bound.right <= canvas.getWidth()
 					&& bound.top >= 60
 					&& bound.bottom <= canvas.getHeight() / 2) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			if (bound.left >= 0 && bound.right <= canvas.getWidth()
 					&& bound.top >= canvas.getHeight()/2 + 60
 					&& bound.bottom <= canvas.getHeight()) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 	}
 }
