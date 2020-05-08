 package nz.kapsy.bassbender;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.TextView;
 
 public class SplashView extends View{
 
 	Canvas maincanvas = new Canvas();
 	TextView instructions = new TextView(this.getContext());
 	
	Bitmap logo = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.dmg_logo_001);
 	RectF lrectf;
 	
 	private static float width = 0F;
 	private static float height = 0F;
 	
 	protected SplashView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		// TODO 自動生成されたコンストラクター・スタブ
 		initView();
 	}
 
 	protected SplashView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO 自動生成されたコンストラクター・スタブ
 		initView();
 	}
 
 	protected SplashView(Context context) {
 		super(context);
 		// TODO 自動生成されたコンストラクター・スタブ
 		initView();
 	}
 	
 	private void initView() {
 		this.setBackgroundColor(Color.argb(255, 0, 140, 255));
 		onDraw(maincanvas);
 	}
 	
 	// screen percentage to pixels
 	public static float percToPixX(float percent) {
 		float pixx = ( width / 100F) * percent;
 		return pixx;
 	}
 	
 	public static float percToPixY(float percent) {
 		float pixy = ( height / 100F) * percent;
 		return pixy;
 	}
 	
 	public static float percToPixX(float width, float percent) {
 		float pixx = ( width / 100F) * percent;
 		return pixx;
 	}
 	
 	public static float percToPixY(float height, float percent) {
 		float pixy = ( height / 100F) * percent;
 		return pixy;
 	}
 	
 	
     public void onDraw(Canvas c) {
     	
 		width = this.getWidth();
 		height = this.getHeight();
     	lrectf = new RectF();
         
         setRecDims(lrectf, 70F);
         c.drawBitmap(logo, null, lrectf, null);
                 
         Paint title_1 = new Paint();
         title_1.setStyle(Paint.Style.FILL);
         title_1.setAntiAlias(true);
         title_1.setColor(Color.argb(255, 0, 255, 255));
         title_1.setTextSize(percToPixX(12F));
         title_1.setTextAlign(Align.CENTER);
         title_1.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC));
         
         Paint question_1 = new Paint();
         question_1.setStyle(Paint.Style.FILL);
         question_1.setAntiAlias(true);
         question_1.setColor(Color.argb(255, 255, 230, 230));
         question_1.setTextSize(percToPixX(16F));
         question_1.setTextAlign(Align.RIGHT);
         question_1.setTypeface(Typeface.create("Ariel", Typeface.NORMAL));
         
         c.drawText("bass bender~", percToPixX(48F), percToPixY(16F), title_1);
         
         title_1.setTextSize(percToPixX(3F));
         title_1.setTypeface(Typeface.create("Ariel", Typeface.ITALIC));
         c.drawText("by", percToPixX(16F), percToPixY(27F), title_1);
         
         title_1.setTextSize(percToPixX(4F));
         title_1.setTypeface(Typeface.create("Ariel", Typeface.NORMAL));
         title_1.setColor(Color.argb(255, 0, 255, 255));
         
         c.drawText("built for headphones", percToPixX(50F), percToPixY(80F), title_1);
        
         title_1.setTextSize(percToPixX(5F));
         title_1.setTypeface(Typeface.create("Ariel", Typeface.NORMAL));
         title_1.setColor(Color.argb(255, 0, 255, 0));
         
         c.drawText("touch to continue...", percToPixX(52F), percToPixY(90F), title_1);
         c.drawText("?", (float) getWidth() - percToPixX(5F), (float) getHeight() - percToPixX(5F), question_1);
     }
     
     // calcs the rec to same ratio as bitmap
     public void setRecDims(RectF rect, float widthpercent) {
     	
     	rect.left = (getWidth() / 2F) - percToPixX(widthpercent / 2F); 
     	rect.right = (getWidth() / 2F) + percToPixX(widthpercent / 2F);
 		float rheight = rectHeightToBitmapDims((lrectf.right - lrectf.left), logo);
 		lrectf.top = (getHeight() / 2F) - (rheight / 2F);
 		lrectf.bottom = lrectf.top + rheight;
     }
         
     public float rectHeightToBitmapDims(float rwidth, Bitmap bm) {
     	float height = rwidth * ((float) bm.getHeight() / (float) bm.getWidth());
     		return height;
     }
 }
