 package se.lannstrom.chesssensei;
 
 import java.util.HashMap;
 
 import se.lannstrom.chesssensei.model.Board.ChessColor;
 import se.lannstrom.chesssensei.model.Board.ChessPiece;
 import se.lannstrom.chesssensei.model.ChessMove.PromotionPiece;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class PromotionSelectionView extends View {
 	private class GuestureDect extends GestureDetector.SimpleOnGestureListener {
 		   @Override
 		   public boolean onDown(MotionEvent e) {
 			   tapped(e);
 			   return true;
 		   }
 	}
 	private GestureDetector detector = new GestureDetector(PromotionSelectionView.this.getContext(), new GuestureDect());
 
 	private static final int PADDING = 10;
 
 	/* Which chess color should be displayed? Defaults to WHITE */
 	private ChessColor color = ChessColor.WHITE;
 
 	private HashMap<ChessPiece, Bitmap> images = new HashMap<ChessPiece, Bitmap>();
 
 	private Context context;
 
 	/**
 	 * Index to selected piece
 	 */
 	private int selectedPiece = -1;
 
 	private int width;
 	private int diag;
 
 	private Rect rect;
 	private Paint drawPaint;
 	private Paint selectedPaint;
 
 	public PromotionSelectionView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		this.context = context;
 		rect = new Rect();
 		drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		selectedPaint.setColor(Color.RED);
 	}
 
 	public ChessColor getColor() {
 		return color;
 	}
 
 	public void setColor(ChessColor color) {
 		this.color = color;
 	}
 
 	/**
 	 * Returns the currently selected piece in the view or null if none is selected.
 	 * Called when the user is done selecting
 	 *
 	 * @return the selected piece
 	 */
 	public PromotionPiece getSelectedPiece() {
 		if (selectedPiece == -1) {
 			return null;
 		} else {
 			PromotionPiece[] values = PromotionPiece.values();
 			return values[selectedPiece];
 		}
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		/* Layout the pieces from left to right. Y-coords remain constant */
 		rect.top = getTop();
 		rect.bottom = rect.top + diag;
 
 		int i = 0;
 		for (ChessPiece cp : images.keySet()) {
 			rect.left = getLeft() + i * (diag + PADDING);
 			rect.right = rect.left + diag;
 
 			/* If selected, draw a red background to indicate this */
 			if (i == selectedPiece) {
 				canvas.drawRect(rect, selectedPaint);
 			}
 
 			Bitmap piece = images.get(cp);
 			canvas.drawBitmap(piece, null, rect, drawPaint);
 			i++;
 		}
 	}
 
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
 
 		width = w;
		diag = (width / PromotionPiece.values().length) - PADDING;
 
 		/* Setup images for new size */
 		images.clear();
 		for (PromotionPiece pp : PromotionPiece.values()) {
 			ChessPiece cp = pp.getChessPiece(color);
 			Bitmap scaled = ChessPieceImages.getInstance(context).
 					getScaledChessPiece(cp, diag);
 			images.put(cp, scaled);
 		}
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int desiredWidth = (512 + PADDING) * PromotionPiece.values().length;
 
 		    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
 		    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
 		    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
 		    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
 
 		    int width;
 		    int height;
 
 		    if (widthMode == MeasureSpec.EXACTLY) {
 		        width = widthSize;
 		    } else if (widthMode == MeasureSpec.AT_MOST) {
 		        width = Math.min(desiredWidth, widthSize);
 		    } else {
 		        width = desiredWidth;
 		    }
 
		    int desiredHeight = (width / PromotionPiece.values().length);
 
 		    if (heightMode == MeasureSpec.EXACTLY) {
 		        height = heightSize;
 		    } else if (heightMode == MeasureSpec.AT_MOST) {
 		        height = Math.min(desiredHeight, heightSize);
 		    } else {
 		        height = desiredHeight;
 		    }
 
 		    setMeasuredDimension(width, height);
 	}
 
 	public double getRelativeX(MotionEvent e) {
 		return e.getX() - getLeft();
 	}
 
 	public double getRelativeY(MotionEvent e) {
 		return e.getY() - getTop();
 	}
 
 	public void tapped(MotionEvent e) {
 	   int selectedX = (int) (getRelativeX(e) / (diag + PADDING));
 	   if (selectedX < 0) {
 		   selectedX = 0;
 	   }
 	   if (selectedX > PromotionPiece.values().length - 1) {
 		   selectedX = PromotionPiece.values().length - 1;
 	   }
 	   selectedPiece = selectedX;
 	   invalidate();
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		boolean result = detector.onTouchEvent(event);
 		return result;
 	}
 
 }
