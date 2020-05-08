 package edu.upenn.cis350.voice;
 
 import android.util.AttributeSet;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Picture;
 import android.graphics.RectF;
 import android.view.MotionEvent;
 import android.content.Context;
 import android.view.View;
 
 public class DragQuestionView extends View implements VoiceViewI {
 	//Initial positions of all the shapes
 	private RectF square = new RectF(590, 400, 690, 500);
 	private int pic1topx = 405;
 	private int pic2topx = 695;
 	private int pic3topx = 985;
 	private int pic1topy = 305;
 	private int pic2topy = 205;
 	private int pic3topy = 405;
 	private int pic1bottomx = 505;
 	private int pic2bottomx = 795;
 	private int pic3bottomx = 1085;
 	private int pic1bottomy = 382;
 	private int pic2bottomy = 282;
 	private int pic3bottomy = 482;
 	private RectF pic1 = new RectF(pic1topx, pic1topy, pic1bottomx, pic1bottomy);
 	private RectF pic2 = new RectF(pic2topx, pic2topy, pic2bottomx, pic2bottomy);
 	private RectF pic3 = new RectF(pic3topx, pic3topy, pic3bottomx, pic3bottomy);
 	private int answerInt;
 	private int xtoffset = 0;
 	private int ytoffset = 0;
 	private int xboffset = 0;
 	private int yboffset = 0;
 	private boolean is1, is2, is3, isPressed, isAnimation = false;
 	private Bitmap bit1, bit2, bit3;
 
 	public DragQuestionView(Context c) {
 		super(c);
 		bit1 = BitmapFactory.decodeResource(this.getResources(),
 				R.drawable.happypup);
 		bit2 = BitmapFactory.decodeResource(this.getResources(),
 				R.drawable.neutralpup);
 		bit3 = BitmapFactory.decodeResource(this.getResources(),
 				R.drawable.verysadpup);
 	}
 
 	public DragQuestionView(Context c, AttributeSet a) {
 		super(c, a);
 		bit1 = BitmapFactory.decodeResource(this.getResources(),
 				R.drawable.happypup);
 		bit2 = BitmapFactory.decodeResource(this.getResources(),
 				R.drawable.neutralpup);
 		bit3 = BitmapFactory.decodeResource(this.getResources(),
 				R.drawable.verysadpup);
 	}
 
 	/**
 	 * Draw the canvas as a soft teal. Check if any of the images are
 	 * intersecting the answer box. If they are, change the stored answer to the
 	 * assigned int value of the image. Redraw the pictures.
 	 * 
 	 * @param- The canvas to draw
 	 */
 	protected void onDraw(Canvas canvas) {
 		canvas.drawRGB(153, 255, 255);
 		int colorInt = Color.rgb(51, 204, 204);
 		Paint paint = new Paint();
 		paint.setColor(colorInt);
 		if (!isAnimation && !RectF.intersects(pic1, square)
 				&& !RectF.intersects(pic2, square)
 				&& !RectF.intersects(pic3, square)) {
 			answerInt = 0;
 		} else if (!isAnimation) {
 			// Checks intersection with each picture. Note: cant have if-else
 			// structure here because that
 			// means pic1 will never get kicked out.
 			if (RectF.intersects(pic1, square)) {
 				if (answerInt != 10) {
 					clearSquare();
 					answerInt = 10;
 				}
 			}
 			if (RectF.intersects(pic2, square)) {
 				if (answerInt != 6) {
 					clearSquare();
 					answerInt = 6;
 				}
 			}
 			if (RectF.intersects(pic3, square)) {
 				if (answerInt != 2) {
 					clearSquare();
 					answerInt = 2;
 				}
 			}
 		}
 
 		// Update the x and y coordinates of each image
 		square.set(590, 400, 690, 500);
 		pic1.set((float) pic1topx, (float) pic1topy, (float) pic1bottomx,
 				(float) pic1bottomy);
 		pic2.set((float) pic2topx, (float) pic2topy, (float) pic2bottomx,
 				(float) pic2bottomy);
 		pic3.set((float) pic3topx, (float) pic3topy, (float) pic3bottomx,
 				(float) pic3bottomy);
 		canvas.drawRect(square, paint);
 		canvas.drawBitmap(bit1, null, pic1, paint);
 		canvas.drawBitmap(bit2, null, pic2, paint);
 		canvas.drawBitmap(bit3, null, pic3, paint);
 	}
 
 	/**
 	 * Detects which image is currently in box using answerInt, and throws that image out 
 	 * of the box
 	 */
 	private void clearSquare() {
 		if (answerInt == 0) {
 			return;
 		} 
 		else if (answerInt == 6) {
 			pic2topx = 695;
 			pic2topy = 205;
 			pic2bottomx = 795;
			pic2bottomy = 305;
 			pic2.set((float) pic2topx, (float) pic2topy, (float) pic2bottomx, (float) pic2bottomy);
 		} 
 		else if (answerInt == 2) {
 			pic3topx = 985;
 			pic3topy = 405;
 			pic3bottomx = 1085;
			pic3bottomy = 505;
 			pic3.set((float) pic3topx, (float) pic3topy, (float) pic3bottomx, (float) pic3bottomy);
 		} 
 		else if (answerInt == 10) {
 			pic1topx = 405;
 			pic1topy = 305;
 			pic1bottomx = 505;
			pic1bottomy = 405;
 			pic1.set((float) pic1topx, (float) pic1topy, (float) pic1bottomx, (float) pic1bottomy);
 		}
 	}
 
 	/**
 	 * This method makes it so the user can drag each image if its being
 	 * touched.
 	 * 
 	 * @param- The motion event being queried
 	 * @return- always true
 	 */
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		int eventAction = event.getAction();
 		int actionX = (int) event.getX(event.findPointerIndex(event
 				.getActionIndex()));
 		int actionY = (int) event.getY(event.findPointerIndex(event
 				.getActionIndex()));
 		// Find the offset between the edges and the spot its touched, so that
 		// each image is consistently dragged from the spot it was touched.
 		if (eventAction == 0) {
 			if (actionX < pic1bottomx && actionY < pic1bottomy
 					&& actionX > pic1topx && actionY > pic1topy) {
 				xtoffset = pic1topx - actionX;
 				ytoffset = pic1topy - actionY;
 				xboffset = pic1bottomx - actionX;
 				yboffset = pic1bottomy - actionY;
 				is1 = true;
 				invalidate();
 			} else if (actionX < pic2bottomx && actionY < pic2bottomy
 					&& actionX > pic2topx && actionY > pic2topy) {
 				xtoffset = pic2topx - actionX;
 				ytoffset = pic2topy - actionY;
 				xboffset = pic2bottomx - actionX;
 				yboffset = pic2bottomy - actionY;
 				is2 = true;
 				invalidate();
 			} else if (actionX < pic3bottomx && actionY < pic3bottomy
 					&& actionX > pic3topx && actionY > pic3topy) {
 				xtoffset = pic3topx - actionX;
 				ytoffset = pic3topy - actionY;
 				xboffset = pic3bottomx - actionX;
 				yboffset = pic3bottomy - actionY;
 				is3 = true;
 				invalidate();
 			}
 			isPressed = true;
 		}
 		// Apply the offset to the x and y values of the point of contact, and
 		// change the x and y coordinates of the touched image
 		if (event.getPressure() != 0 && isPressed) {
 			if (is1) {
 				pic1topx = actionX + xtoffset;
 				pic1topy = actionY + ytoffset;
 				pic1bottomx = actionX + xboffset;
 				pic1bottomy = actionY + yboffset;
 				invalidate();
 			} else if (is2) {
 				pic2topx = actionX + xtoffset;
 				pic2topy = actionY + ytoffset;
 				pic2bottomx = actionX + xboffset;
 				pic2bottomy = actionY + yboffset;
 				invalidate();
 			} else if (is3) {
 				pic3topx = actionX + xtoffset;
 				pic3topy = actionY + ytoffset;
 				pic3bottomx = actionX + xboffset;
 				pic3bottomy = actionY + yboffset;
 				invalidate();
 			}
 		}
 		if (isPressed == true && eventAction == 1) {
 			is1 = is2 = is3 = isPressed = false;
 			invalidate();
 		}
 		return true;
 	}
 
 	public int getAnswer() {
 		return answerInt;
 	}
 
 	/**
 	 * Reads the previous answer, and moves the corresponding puppy into the square
 	 * @param prevAnswer- the integer representation of the previous answer given
 	 */
 	public void setAnswer(int prevAnswer){
 		if(prevAnswer==-1){
 			return;
 		} 
 		else if (prevAnswer == 2) {
 			pic3topx = (int) square.left;
 			pic3topy = (int) square.top;
 			pic3bottomx = (int) (pic3topx + pic3.width());
 			pic3bottomy = (int) (pic3topy + pic3.height());
 			answerInt = prevAnswer;
 		} 
 		else if (prevAnswer == 6) {
 			pic2topx = (int) square.left;
 			pic2topy = (int) square.top;
 			pic2bottomx = (int) (pic2topx + pic2.width());
 			pic2bottomy = (int) (pic2topy + pic2.height());
 			answerInt = prevAnswer;
 		}
 		 else if (prevAnswer == 10) {
 			pic1topx = (int) square.left;
 			pic1topy = (int) square.top;
 			pic1bottomx = (int) (pic1topx + pic1.width());
 			pic1bottomy = (int) (pic1topy + pic1.height());
 			answerInt = prevAnswer;
 		}
 		invalidate();
 	}
 
 	/**
 	 * Increases the size of the answer in the box, as a part of the animation
 	 * 
 	 * @param The- image to be blown up in size
 	 */
 	private void grow(int num) {
 
 		if (num == 1) {
 			// Increase the y value incrementally until it hits a max size
 				pic1topy = pic1topy-231;
 				pic1bottomx = pic1bottomx + 150;
 				pic1topx = pic1topx - 150;
 		} else if (num == 2) {
 				pic2topy = pic2topy - 231;
 				pic2bottomx = pic2bottomx + 150;
 				pic2topx = pic2topx - 150;
 		} else if (num == 3) {
 				pic3topy = pic3topy - 231;
 				pic3bottomx = pic3bottomx + 150;
 				pic3topx = pic3topx - 150;
 		}
 
 	}
 
 	/**
 	 * Move the image indicated by num by the given increment
 	 * 
 	 * @param num- the image to be moved
 	 * @param increment- the increment to move the image by
 	 */
 	private void move(int num) {
 		if (num == 1) {
 			pic1topx =200;
 			pic1topy = 200;
 			pic1bottomx = 200;
 			pic1bottomy = 200;
 		} else if (num == 2) {
 			pic2topx = 200;
 			pic2topy = 200;
 			pic2bottomx = 200;
 			pic2bottomy = 200;
 		} else if (num == 3) {
 			pic3topx = 200;
 			pic3topy = 200;
 			pic3bottomx = 200;
 			pic3bottomy = 200;
 		}
 	}
 	/**
 	 * Moves all unselected images off the canvas, and blows up the selected image
 	 * 
 	 */
 	public void animate() {
 		isAnimation = true;
 		if (answerInt == 10) {
 			setAnswer(10);
 			move(2);
 			move(3);
 			grow(1);
 		}
 		if (answerInt == 6) {
 			move(3);
 			move(1);
 			grow(2);
 		}
 		if (answerInt == 2) {
 			move(1);
 			move(2);
 			grow(3);
 		}
 		invalidate();
 		isAnimation = false;
 		
 	}
 
 }
