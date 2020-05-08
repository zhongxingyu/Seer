 package info.kyorohiro.helloworld.textviewer.viewer;
 
 import java.io.File;
 import java.io.InputStream;
 
 import android.content.res.Resources;
 import android.graphics.Color;
 import info.kyorohiro.helloworld.display.simple.SimpleDisplayObject;
 import info.kyorohiro.helloworld.display.simple.SimpleDisplayObjectContainer;
 import info.kyorohiro.helloworld.display.simple.SimpleGraphics;
 import info.kyorohiro.helloworld.display.simple.SimpleImage;
 import info.kyorohiro.helloworld.display.widget.SimpleCircleController;
 import info.kyorohiro.helloworld.display.widget.SimpleCircleController.CircleControllerAction;
 import info.kyorohiro.helloworld.display.widget.lineview.FlowingLineDatam;
 import info.kyorohiro.helloworld.display.widget.lineview.LineView;
 import info.kyorohiro.helloworld.display.widget.lineview.MyTouchAndMove;
 import info.kyorohiro.helloworld.display.widget.lineview.MyTouchAndZoom;
 import info.kyorohiro.helloworld.display.widget.lineview.ScrollBar;
 import info.kyorohiro.helloworld.textviewer.KyoroApplication;
 import info.kyorohiro.helloworld.textviewer.KyoroSetting;
 import info.kyorohiro.helloworld.textviewer.R;
 import info.kyorohiro.helloworld.util.BigLineData;
 import info.kyorohiro.helloworld.util.CyclingList;
 import info.kyorohiro.helloworld.util.CyclingListInter;
 
 public class TextViewer extends SimpleDisplayObjectContainer {
 	//	public static int COLOR_BG = Color.parseColor("#FF101030");
 	//	public static int COLOR_BG = Color.parseColor("#FF808080");
 	public static int COLOR_BG = Color.parseColor("#FF000000");
 	public static int COLOR_FONT1 = Color.parseColor("#ff80c9f4");
 	public static int COLOR_FONT2 = Color.parseColor("#fff480c9");
 
 	private String mCurrentCharset = "utf8";
 
 	private CyclingList<FlowingLineDatam> mBuffer = null;
 	private LineView mLineView = null;
 	private int mBufferWidth = 0;
 	private int mTextSize = 0;
 	private SimpleCircleController mCircleController = null;
 	private ScrollBar mScrollBar = null;
 	private SimpleImage mBGImage = null;
 	private SimpleImage mScrollImage = null;
 
 	public TextViewer(int textSize, int width) {
 		mTextSize = textSize;
 		mCurrentCharset = KyoroSetting.getCurrentCharset();
 		mBuffer = getStartupMessageBuffer();
 		mBufferWidth = width;
 
 		mLineView = new LineView(mBuffer, textSize,200);
 		mLineView.isTail(false);
 		mLineView.setBgColor(COLOR_BG);
 
 		mScrollBar = new ScrollBar(mLineView);
 		addChild(mLineView);
 		addChild(new MyTouchAndMove(mLineView));
 		addChild(new MyTouchAndZoom(mLineView));
 		addChild(mCircleController = new SimpleCircleController());
 		addChild(new LayoutManager());
 		addChild(mScrollBar);
 		mCircleController.setEventListener(new MyCircleControllerEvent());
 	}
 
 	@Override
 	public void start() {
 		Resources res = KyoroApplication.getKyoroApplication().getResources();
 		InputStream is = res.openRawResource(R.drawable.tex2res4);
 		mBGImage = new SimpleImage(is);
 		mLineView.setBGImage(mBGImage);		
 		InputStream isScroll = res.openRawResource(R.drawable.tex2res2);
 		mScrollImage = new SimpleImage(isScroll);
 		mScrollBar.setBGImage(mScrollImage);
 	}
 
 	@Override
 	public void stop() {
 		mLineView.setBGImage(null);
 		if(mBGImage.getImage().isRecycled()){
 			mBGImage.getImage().recycle();
 		}
		if(mScrollImage.getImage().isRecycled()){
			mScrollImage.getImage().recycle();
		}
 	}
 
 	@Override
 	public void setRect(int w, int h) {
 		super.setRect(w, h);
 		mLineView.setRect(w, h);
 	}
 
 	public void setCharset(String charset) {
 		if(charset == null|| charset.equals("")){
 			mCurrentCharset = "utf8";
 		}else{
 			mCurrentCharset = charset;
 		}
 	}
 
 	public LineView getLineView() {
 		return mLineView;
 	}
 
 	public void readFile(File file) {
 		if(file == null) {
 			KyoroApplication.showMessage("file can not read null file");
 			return;
 		}
 		if(!file.canRead() || !file.exists()|| !file.isFile()){
 			KyoroApplication.showMessage("file can not read " + file.toString());
 			return;
 		}
 		KyoroSetting.setCurrentFile(file.getAbsolutePath());
 		mBuffer = new ColorFilteredBuffer(20*BigLineData.FILE_LIME, mTextSize, mBufferWidth, file, mCurrentCharset);
 		CyclingListInter<FlowingLineDatam> prevBuffer = mLineView.getCyclingList();
 		mLineView.setCyclingList(mBuffer);
 		prevBuffer.clear();
 		((TextViewerBuffer) mBuffer).startReadFile();
 		KyoroApplication.showMessage("charset="+mCurrentCharset);
 	}
 
 	private class LayoutManager extends SimpleDisplayObject {
 		@Override
 		public void paint(SimpleGraphics graphics) {
 			int x = graphics.getWidth() - mCircleController.getWidth() / 2;
 			int y = graphics.getHeight() - mCircleController.getHeight() / 2;
 			TextViewer.this.mCircleController.setPoint(x, y);
 			updateCircleControllerSize(graphics);
 			paintScroll(graphics);			
 		}
 
 		private void updateCircleControllerSize(SimpleGraphics graphics) {
 			int min = graphics.getWidth();
 			if(min>graphics.getHeight()){
 				min = graphics.getHeight();
 			}
 			min = min/2;
 			if(min<mCircleController.getWidth()){
 				mCircleController.setRadius(min/2);
 			}
 		}
 
 		public void paintScroll(SimpleGraphics graphics) {
 			CyclingListInter<?> viewerBuffer = TextViewer.this.mLineView.getCyclingList();
 			int bufferSize = viewerBuffer.getNumberOfStockedElement(); 
 			int beginPosition = TextViewer.this.mLineView.getShowingTextStartPosition();
 			int endPosition = TextViewer.this.mLineView.getShowingTextEndPosition();
 			mScrollBar.setStatus(
 					beginPosition,
 					endPosition, bufferSize);
 			TextViewer.this.mLineView.setRect(graphics.getWidth(), graphics.getHeight());
 		}
 	}
 
 	private class MyCircleControllerEvent implements SimpleCircleController.CircleControllerAction {
 		public void moveCircle(int action, int degree, int rateDegree) {
 			if (action == CircleControllerAction.ACTION_MOVE) {
 				getLineView().setPositionY(getLineView().getPositionY() + rateDegree*2);
 			}
 		}
 		public void upButton(int action) {
 			getLineView().setPositionY(getLineView().getPositionY() + 5);
 		}
 
 		public void downButton(int action) {
 			getLineView().setPositionY(getLineView().getPositionY() - 5);
 		}
 	}
 
 	private static class ColorFilteredBuffer extends TextViewerBuffer {
 		public ColorFilteredBuffer(int listSize, int textSize, int screenWidth,
 				File path, String charset) {
 			super(listSize, textSize, screenWidth, path, charset);
 		}
 
 		@Override
 		public synchronized void head(FlowingLineDatam element) {
 			super.head(element);
 			element.setColor(COLOR_FONT2);
 		}
 		@Override
 		public synchronized void add(FlowingLineDatam element) {
 			super.add(element);
 			element.setColor(COLOR_FONT1);
 		}
 	}
 
 	private CyclingList<FlowingLineDatam> getStartupMessageBuffer() {
 		String[] message = {
 				"Please open file\n",
 				"Current charset is "+mCurrentCharset+"\n", 
 				"..\n",
 				"Sorry, this application is pre-alpha version",
 				"Testing and Developing.. now",
 				"Please mail kyorohiro.android@gmail.com, ", 
 				"If you have particular questions or comments, ",
 				"please don't hesitate to contact me. Thank you. \n"
 		};
 		int color[] = {
 				Color.CYAN,
 				Color.YELLOW,
 				Color.RED,
 				Color.RED,
 				Color.RED,
 				Color.RED,
 				Color.RED,
 				Color.RED,				
 		};
 		CyclingList<FlowingLineDatam> startupMessage = new CyclingList<FlowingLineDatam>(100);
 		for(int i=0;i<message.length;i++){
 			String m = message[i];
 			int crlf = FlowingLineDatam.INCLUDE_END_OF_LINE;
 			if(!m.endsWith("\n")){
 				crlf = FlowingLineDatam.EXCLUDE_END_OF_LINE;
 			}
 			startupMessage.add(new FlowingLineDatam(m, color[i], crlf));
 		}
 		return startupMessage;
 	}
 
 }
