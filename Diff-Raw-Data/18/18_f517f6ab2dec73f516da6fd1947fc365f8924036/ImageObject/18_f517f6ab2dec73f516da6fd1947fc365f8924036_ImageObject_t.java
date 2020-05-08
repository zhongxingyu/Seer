 package com.ooksi.ui;
 
 import java.io.IOException;
 
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.Image;
 import javax.microedition.lcdui.game.Sprite;
 
 import com.google.minijoe.sys.JsArray;
 import com.google.minijoe.sys.JsFunction;
 import com.google.minijoe.sys.JsObject;
 import com.ooksi.Canvas2D;
 import com.ooksi.net.HTTPResultListener;
 import com.ooksi.net.Http;
 
 public class ImageObject extends JsObject implements HTTPResultListener {
 	
 	private static final JsObject IMAGE_PROTOTYPE = new JsObject(
 			JsFunction.OBJECT_PROTOTYPE);
 	
 	private Image midpImage;
 	private JsObject callBackScope;
 	private static Object callbackEventLock;
 	private String src;
 	private Canvas2D canvas;
 
 	public static final int ID_INIT_IMAGE = 2001;
 	private static final int ID_FROM_BYTES = 2002;
 
 	public ImageObject(JsObject scope, Object lock, Canvas2D canvas) {
 		super(IMAGE_PROTOTYPE);
 
 		this.callBackScope = scope;
 		this.callbackEventLock = lock;
 		this.canvas = canvas;
 
 		addNative("fromBytes", ID_FROM_BYTES, 1);
 		addVar("onLoad", null);
 		addVar("src", null);
 	}
 
 	public void evalNative(int index, JsArray stack, int sp, int parCount) {
 
 		switch (index) {
 		case ID_INIT_IMAGE:
 			// Nothing for now
 			break;
 		case ID_FROM_BYTES:
 			byte[] imgData = ((byte[]) stack.getObject(sp + 2));
 			midpImage = Image.createImage(imgData, 0, imgData.length);
 			break;
 		default:
 			super.evalNative(index, stack, sp, parCount);
 		}
 	}
 	
 	private Image getCropped(int x, int y, int width, int height) {
 		return Image.createImage(this.midpImage, x, y, width,
 				height, Sprite.TRANS_NONE);
 	}
 
 	public Image getMidpImage() {
 		return this.midpImage;
 	}
 
 	private Image scaleImage(Image img, int width, int height) {
 		// TODO: need simple scaling impl
 		return img;
 	}
 
 	/**
 	 * Draw to underlying canvas
 	 * @param graphics
 	 * @param x
 	 * @param y
 	 */
 	public void drawToMidpCanvas(Graphics graphics, int x, int y) {
 		graphics.drawImage(this.midpImage, x, y, Graphics.TOP | Graphics.LEFT);
 		canvas.repaint();
 	}
 
 	/**
 	 * Draw to underlying canvas with cropping
 	 * 
 	 * @param graphics
 	 * @param sx
 	 * @param sy
 	 * @param sWidth
 	 * @param sHeight
 	 * @param dx
 	 * @param dy
 	 * @param dWidth
 	 * @param dHeight
 	 */
 	public void drawToMidpCanvas(Graphics graphics, 
 			int sx, int sy, int sWidth, int sHeight, 
 			int dx, int dy, int dWidth, int dHeight) {
 		if (this.midpImage == null) {
 			// FIXME: throw exception?
 			return;
 		}
 		sWidth = sWidth != 0 ? sWidth : this.midpImage.getWidth();
 		sHeight = sHeight != 0 ? sHeight : this.midpImage.getHeight();
 		
 		Image currImage = this.midpImage;
 		if (dWidth != 0 || dHeight != 0) {
 			currImage = scaleImage(currImage, dWidth, dHeight);
 		}
 		
 		graphics.drawRegion(currImage, sx, sy, sWidth, sHeight,
 				Sprite.TRANS_NONE, dx, dy, Graphics.TOP | Graphics.LEFT);
 		canvas.repaint();
 	}
 
 	public String toString() {
 		return "[Image]";
 	}
 
 	// callback from Http obj that loads img data
 	public void httpResult(Http http) {
 		
 		byte[] imgData = http.getResponseBody();
 		this.midpImage = Image.createImage(imgData, 0, imgData.length);
 		addVar("width", new Integer(this.midpImage.getWidth()));
 		addVar("height", new Integer(this.midpImage.getHeight()));
 		
 		System.out.println("http callback:" + http.getResponseCode() + "|"
 				+ imgData.length);
 
 		JsFunction onLoadCallBack = (JsFunction) this.getObject("onLoad");
 		if (onLoadCallBack != null) {
 			doCallBack(onLoadCallBack);
 		} else {
 			System.out.println("NO callback:");
 		}
 	}
 	
 
 	public void setObject(String key, Object v) {
 		super.setObject(key, v);
 		if ("src".equals(key) && (v instanceof String)) {
 			this.loadImage((String) v);
 		}
 	}
 
 	// callback from Http obj that loads img data
 	public void progressUpdate(int progress) {
 		// TODO add impl to track img data load via http
 	}
 
 	private void loadImage(String url) {
 		System.out.println("loading img url:" + url);
		try {			
 			if (url.length() > 0 && url.startsWith("file://")) {
				//TODO: add support for JSR-82 PIM API
			} else if (url.length() > 0 && url.startsWith("http://")) {
				// 1x1px placeholder until actual img loads from net
				this.midpImage = Image.createImage(1, 1);
				Http.fetchUrl(url, Http.GET, this);
			} else {
				this.midpImage = Image.createImage("/"+url); //append / as expect file in / of classpath
 				addVar("width", new Integer(this.midpImage.getWidth()));
 				addVar("height", new Integer(this.midpImage.getHeight()));
 				
 				JsFunction onLoadCallBack = (JsFunction) this.getObject("onLoad");
 				if (onLoadCallBack != null) {
 					doCallBack(onLoadCallBack);
 				} else {
 					System.out.println("NO callback:");
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			// TODO: should throw JS Error ??
 		}
 		addVar("src", url);
 	}
 
 	private void doCallBack(JsFunction callBack) {
 		System.out.println("finished loading img");
 
 		synchronized (callbackEventLock) {
 			JsArray callBackStack = new JsArray();
 			callBackStack.setObject(0, this.callBackScope);
 			callBackStack.setObject(1, callBack);
 
 			callBack.eval(callBackStack, 0, 1);
 		}
 	}
 }
