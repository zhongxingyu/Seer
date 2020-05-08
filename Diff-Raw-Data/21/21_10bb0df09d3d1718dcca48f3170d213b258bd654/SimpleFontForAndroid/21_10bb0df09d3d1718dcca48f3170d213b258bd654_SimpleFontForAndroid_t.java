 package info.kyorohiro.helloworld.android.adapter;
 
 import android.graphics.Paint;
 import info.kyorohiro.helloworld.display.simple.SimpleFont;
 import info.kyorohiro.helloworld.display.simple.SimpleGraphics;
 import info.kyorohiro.helloworld.display.simple.SimpleTypeface;
 import info.kyorohiro.helloworld.text.KyoroString;
 
 public class SimpleFontForAndroid extends SimpleFont {
 
 	private Paint mPaint = new Paint();
 
 	public SimpleFontForAndroid() {
 		super();
 		setAntiAlias(getAntiAlias());
 		setFontSize(getFontSize());
 		setSimpleTypeface(getSimpleTypeface());
 	}
 
 	@Override
 	public void setAntiAlias(boolean state) {
 		super.setAntiAlias(state);
 		mPaint.setAntiAlias(state);
 	}
 
 	@Override
 	public void setFontSize(float size) {
 		super.setFontSize(size);
 		mPaint.setTextSize(size);
 	}
 
 	@Override
 	public void setSimpleTypeface(SimpleTypeface typeface) {
 		super.setSimpleTypeface(typeface);
 		if(typeface instanceof SimpleTypefaceForAndroid) {
 			mPaint.setTypeface(((SimpleTypefaceForAndroid)typeface).getTypeface());
 		}
 	}
 
 	@Override
	public synchronized int getTextWidths(KyoroString text, int start, int end, float[] widths, float textSize) {
 		float _textSize = mPaint.getTextSize();
 		mPaint.setTextSize(textSize);
 		int ret = mPaint.getTextWidths(text.getChars(), start, end-start, widths);
		mPaint.setTextSize(_textSize);
 		return ret;
 	}
 
 	public int getControlCode(char[] buffer, int len, int start ) {
 		for(int i=start;i<len;i++) {
 			if(buffer[i]<=31||buffer[i]==127){
 				return i;
 			}
 		}
 		return len;
 	}
 
 	@Override
	public synchronized int getTextWidths(char[] buffer, int start, int end, float[] widths, float textSize) {
		float _t = mPaint.getTextSize();
		mPaint.setTextSize(textSize);
		int ret = mPaint.getTextWidths(buffer, start, end-start, widths);
		mPaint.setTextSize(_t);
		return ret;
 	}
 }
