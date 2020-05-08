 package nanofuntas.fbl;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.PointF;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 
 public class HexView extends View {
 	private final boolean DEBUG = true;
 	private final String TAG = "HexView";
 	
 	// tuning ratio to size
 	private final float R_RATIO_TO_SIZE = 0.7f; //Radius of HEX
 	private final float DELTA_X_RATIO_TO_SIZE = 0.12f;
 	private final float DELTA_Y_RATIO_TO_SIZE = 0.06f;
 	private final float RATIO_TEXT = 1.25f; // (RATIO_TEXT*R) is the center point where text shows
 	private final float POINT_SIZE_RATIO_TO_SIZE = 0.14f;
 	private final float F = (float) (Math.sqrt(3) / 2);
 	
 	// Default size
 	private float DEFAULT_SIZE = 120;
 	private float SIZE = DEFAULT_SIZE; // View will be shown in Rectangle of (0,0,2SIZE,2SIZE), resized to SIZE = density * PIXEL
 	private float X = SIZE;
 	private float Y = SIZE;
 	private float R = R_RATIO_TO_SIZE * SIZE;
 	private float TEXT_DELTA_X = DELTA_X_RATIO_TO_SIZE * SIZE;
 	private float TEXT_DELTA_Y = DELTA_Y_RATIO_TO_SIZE * SIZE;
 	private float PONT_SIZE = POINT_SIZE_RATIO_TO_SIZE * SIZE;
 	
 	// ratio of HEX value
 	private float ratioATK = 0.0f;
 	private float ratioTEC = 0.0f;
 	private float ratioTWK = 0.0f;
 	private float ratioDFS = 0.0f;
 	private float ratioMTL = 0.0f;
 	private float ratioPHY = 0.0f;
 	
 	private final PointF POINT_ATK = new PointF();
 	private final PointF POINT_TEC = new PointF();
 	private final PointF POINT_TWK = new PointF();
 	private final PointF POINT_DFS = new PointF();
 	private final PointF POINT_MTL = new PointF();
 	private final PointF POINT_PHY = new PointF();
 	
 	private final PointF POINT_ATK_RATING = new PointF();
 	private final PointF POINT_TEC_RATING = new PointF();
 	private final PointF POINT_TWK_RATING = new PointF();
 	private final PointF POINT_DFS_RATING = new PointF();
 	private final PointF POINT_MTL_RATING = new PointF();
 	private final PointF POINT_PHY_RATING = new PointF();	
 	
 	private final PointF POINT_ATK_TEXT = new PointF();
 	private final PointF POINT_TEC_TEXT = new PointF();
 	private final PointF POINT_TWK_TEXT = new PointF();
 	private final PointF POINT_DFS_TEXT = new PointF();
 	private final PointF POINT_MTL_TEXT = new PointF();
 	private final PointF POINT_PHY_TEXT = new PointF();
 	
 	private Paint p = new Paint();
 	private Path hexFrame = new Path();		
 	private Path hexValue = new Path();
 	
 	public HexView(Context context) {
 		super(context);
 	}
 	public HexView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 	
 	public HexView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 	
 	//rATK, rTEC, rTWK, rDFS, rMTL, rPHY is ratio smaller than 1
 	public HexView(Context context, 
 			float rATK, float rTEC, float rTWK, float rDFS, float rMTL, float rPHY) {
 		super(context);
 		this.ratioATK = rATK;
 		this.ratioTEC = rTEC;
 		this.ratioTWK = rTWK;
 		this.ratioDFS = rDFS;
 		this.ratioMTL = rMTL;
 		this.ratioPHY = rPHY;
 	}
 	
 	private void setSize(int s) {
 		SIZE = s;
 		X = SIZE;
 		Y = SIZE;
 		R = R_RATIO_TO_SIZE * SIZE;
 		TEXT_DELTA_X = DELTA_X_RATIO_TO_SIZE * SIZE;
 		TEXT_DELTA_Y = DELTA_Y_RATIO_TO_SIZE * SIZE;
 		PONT_SIZE = POINT_SIZE_RATIO_TO_SIZE * SIZE;
 	}
 	
 	public void setRatingAndDraw(float rATK, float rTEC, float rTWK, float rDFS, float rMTL, float rPHY) {
 		if (DEBUG) Log.d(TAG, "setRatingAndDraw()");
 		
 		this.ratioATK = rATK;
 		this.ratioTEC = rTEC;
 		this.ratioTWK = rTWK;
 		this.ratioDFS = rDFS;
 		this.ratioMTL = rMTL;
 		this.ratioPHY = rPHY;
 		refreshDrawableState();
 	}
 	
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		Log.e(TAG, "onMeasure()");
 		
 		int height = getLayoutParams().height;
 		int width = getLayoutParams().width;
 		int size = height > width ? height : width;
 
 		if (height == LayoutParams.WRAP_CONTENT || width == LayoutParams.WRAP_CONTENT) {
 			Log.d (TAG, "LayoutParams.WRAP_CONTENT");
 			size = (int) DEFAULT_SIZE;
 		}
 		
 		setSize(size);		
 		setMeasuredDimension(size*2, size*2);
 	}
 	
 	protected void onDraw(Canvas canvas) {		
 		if (DEBUG) Log.d(TAG, "onDraw()");
 		
 		// draw HEX on rectangle of (0,0,2X,2Y)
 		p.setColor(Color.GRAY);
 		canvas.drawRect(0, 0, 2*X, 2*Y, p);
 		
 		// points of HEX frame
 		POINT_ATK.set( X, Y-R );
 		POINT_TEC.set( X-R*F, Y-R/2 );
 		POINT_TWK.set( X-R*F, Y+R/2 );
 		POINT_DFS.set( X, Y+R );
 		POINT_MTL.set( X+R*F, Y+R/2 );
 		POINT_PHY.set( X+R*F, Y-R/2 );
 				
 		// draw HEX frame
 		p.setColor(Color.RED);
		hexFrame.rewind();
 		hexFrame.moveTo(POINT_ATK.x, POINT_ATK.y);
 		hexFrame.lineTo(POINT_TEC.x, POINT_TEC.y);
 		hexFrame.lineTo(POINT_TWK.x, POINT_TWK.y);
 		hexFrame.lineTo(POINT_DFS.x, POINT_DFS.y);
 		hexFrame.lineTo(POINT_MTL.x, POINT_MTL.y);
 		hexFrame.lineTo(POINT_PHY.x, POINT_PHY.y);
 		hexFrame.lineTo(POINT_ATK.x, POINT_ATK.y);
 		canvas.drawPath(hexFrame, p);
 				
 		// points of rating HEX
 		POINT_ATK_RATING.set( X, Y-ratioATK*R );
 		POINT_TEC_RATING.set( X-ratioTEC*R*F, Y-ratioTEC*R/2 );
 		POINT_TWK_RATING.set( X-ratioTWK*R*F, Y+ratioTWK*R/2 );
 		POINT_DFS_RATING.set( X, Y+ratioDFS*R );
 		POINT_MTL_RATING.set( X+ratioMTL*R*F, Y+ratioMTL*R/2 );
 		POINT_PHY_RATING.set( X+ratioPHY*R*F, Y-ratioPHY*R/2 );	
 								
 		// draw rating HEXx
 		p.setColor(Color.GREEN);
		hexValue.rewind();
 		hexValue.moveTo(POINT_ATK_RATING.x, POINT_ATK_RATING.y);
 		hexValue.lineTo(POINT_TEC_RATING.x, POINT_TEC_RATING.y);
 		hexValue.lineTo(POINT_TWK_RATING.x, POINT_TWK_RATING.y);
 		hexValue.lineTo(POINT_DFS_RATING.x, POINT_DFS_RATING.y);
 		hexValue.lineTo(POINT_MTL_RATING.x, POINT_MTL_RATING.y);
 		hexValue.lineTo(POINT_PHY_RATING.x, POINT_PHY_RATING.y);
 		hexValue.lineTo(POINT_ATK_RATING.x, POINT_ATK_RATING.y);
 		canvas.drawPath(hexValue, p);	
 		
 		// draw lines
 		p.setColor(Color.BLACK);
 		canvas.drawLine(POINT_ATK.x, POINT_ATK.y, POINT_DFS.x, POINT_DFS.y, p);
 		canvas.drawLine(POINT_TEC.x, POINT_TEC.y, POINT_MTL.x, POINT_MTL.y, p);
 		canvas.drawLine(POINT_TWK.x, POINT_TWK.y, POINT_PHY.x, POINT_PHY.y, p);				
 		
 		// drawing text on the ratio of R
 		p.setColor(Color.BLUE);
 		p.setTextSize(PONT_SIZE);
 		// text drawing points
 		POINT_ATK_TEXT.set( X - TEXT_DELTA_X, Y-RATIO_TEXT*R + TEXT_DELTA_Y );
 		POINT_TEC_TEXT.set( X-RATIO_TEXT*R*F - TEXT_DELTA_X, Y-RATIO_TEXT*R/2 + TEXT_DELTA_Y );
 		POINT_TWK_TEXT.set( X-RATIO_TEXT*R*F - TEXT_DELTA_X, Y+RATIO_TEXT*R/2 + TEXT_DELTA_Y );
 		POINT_DFS_TEXT.set( X - TEXT_DELTA_X, Y+RATIO_TEXT*R + TEXT_DELTA_Y );
 		POINT_MTL_TEXT.set( X+RATIO_TEXT*R*F - TEXT_DELTA_X, Y+RATIO_TEXT*R/2 + TEXT_DELTA_Y);
 		POINT_PHY_TEXT.set( X+RATIO_TEXT*R*F - TEXT_DELTA_X, Y-RATIO_TEXT*R/2 + TEXT_DELTA_Y );
 		
 		// draw text
 		canvas.drawText("ATK", POINT_ATK_TEXT.x, POINT_ATK_TEXT.y, p);
 		canvas.drawText("TEC", POINT_TEC_TEXT.x, POINT_TEC_TEXT.y, p);
 		canvas.drawText("TWK", POINT_TWK_TEXT.x, POINT_TWK_TEXT.y, p);
 		canvas.drawText("DFS", POINT_DFS_TEXT.x, POINT_DFS_TEXT.y, p);
 		canvas.drawText("MTL", POINT_MTL_TEXT.x, POINT_MTL_TEXT.y, p);
 		canvas.drawText("PHY", POINT_PHY_TEXT.x, POINT_PHY_TEXT.y, p);
 	}
 
 }
