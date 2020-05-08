 /*
  * IconButton.java
  * Creating Icon Button with Round Rectangle Border
  * by : wahyusumartha
  * date : 7-12-2010
  */
 package com.framework.ui.component;
 
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Graphics;
 
 public class IconButton extends Field {
 
 	private Bitmap image = null;
 
 	/*
 	 * MARGIN SETTING (inner)
 	 */
 	private static final int TOP_MARGIN = 2;
 	private static final int RIGHT_MARGIN = 2;
 	private static final int BOTTOM_MARGIN = 2;
 	private static final int LEFT_MARGIN = 2;
 
 	/*
 	 * PADDING SETTING
 	 */
	private static final int TOP_PADDING = 2;
 	private static final int RIGHT_PADDING = 2;
	private static final int BOTTOM_PADDING = 2;
 	private static final int LEFT_PADDING = 2;
 
 	/*
 	 * variable to set Height and width
 	 */
 	private int fieldHeight;
 	private int fieldWidth;
 
 	/*
 	 * Border Color
 	 */
 	private int borderColor = Color.SILVER;
 	private int backgroundColor = Color.WHITE;
 
 	/*
 	 * Default Constructor
 	 * 
 	 * @imageLocation
 	 */
 	public IconButton(String imageLocation) {
 		super();
 		setPreferenceMargin();
 		setPreferencePadding();
 		image = Bitmap.getBitmapResource(imageLocation);
 		fieldHeight = TOP_PADDING + image.getHeight();
 		fieldWidth = RIGHT_PADDING + LEFT_MARGIN + image.getHeight();
 	}
 
 	protected void layout(int width, int height) {
 		setExtent(getPreferredWidth(), getPreferredHeight());
 	}
 
 	protected void paint(Graphics graphics) {
 		// int imageX = (getWidth() / 2) + (image.getWidth() / 2);
 		// int imageY = 0;
 
 		/*
 		 * Drawing Box
 		 */
 		graphics.setColor(backgroundColor);
		graphics.fillRoundRect(0, 0, fieldWidth, fieldHeight, 15, 15);
 
 		/*
 		 * Drawing Round Rectangle Border
 		 */
 		graphics.setColor(borderColor);
 		graphics.drawRoundRect(0, 0, fieldWidth, fieldHeight, 15, 15);
 
 		/*
 		 * Put Icon Image
 		 */
 		graphics.drawBitmap(LEFT_PADDING, TOP_PADDING, image.getWidth(), image
 				.getHeight(), image, 0, 0);
 
 	}
 
 	public int getPreferredHeight() {
 		return fieldHeight;
 	}
 
 	public int getPreferredWidth() {
 		return fieldWidth;
 	}
 
 	/*
 	 * This methods tells whether field is focusable or not
 	 */
 	public boolean isFocusable() {
 		return true;
 	}
 
 	protected void onFocus(int direction) {
 		borderColor = Color.BLUEVIOLET;
 		invalidate();
 	}
 
 	protected void onUnfocus() {
 		borderColor = Color.SILVER;
 		invalidate();
 	}
 
 	protected void fieldChangeNotify(int context) {
 		this.getChangeListener().fieldChanged(this, context);
 	}
 
 	protected boolean navigationClick(int status, int time) {
 		fieldChangeNotify(1);
 		return true;
 	}
 
 	/*
 	 * SET MARGIN
 	 */
 	private void setPreferenceMargin() {
 		setMargin(TOP_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN, LEFT_MARGIN);
 	}
 
 	/*
	 * SET PADDING
 	 */
 	private void setPreferencePadding() {
 		setPadding(TOP_PADDING, RIGHT_PADDING, BOTTOM_PADDING, LEFT_PADDING);
 	}
 
	protected void drawFocus(Graphics graphics, boolean on) {
		/*
		 * NOTHING
		 */
	}

 }
