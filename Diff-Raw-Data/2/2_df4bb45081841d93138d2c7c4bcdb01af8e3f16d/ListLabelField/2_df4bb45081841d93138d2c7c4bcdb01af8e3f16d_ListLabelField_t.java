 package net.mytcg.topcar.ui.custom;
 
 import net.mytcg.topcar.util.Const;
 import net.rim.device.api.math.Fixed32;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.component.LabelField;
 
 public final class ListLabelField extends LabelField {
 	
 	private int _currentColor;
 	private int _normalColor;
 	private int _changeColor;
 	private int _backColor;
 	private int _width;
 	private Bitmap button_centre;
 	private boolean focus = false;
 
 	public ListLabelField(String text) {
 		super(text);
 		super.setPadding(5, 5, 5, 5);
 		construct(Const.FONT, 0, Font.PLAIN, Const.FONTCOLOR, Const.BACKCOLOR, Const.SELECTEDCOLOR);
 	}
 	
 	public ListLabelField(String text, long style) {
 		super(text, style);
 		construct(Const.FONT, 0, Font.PLAIN, Const.FONTCOLOR, Const.BACKCOLOR, Const.SELECTEDCOLOR);
 	}
 	public ListLabelField(String text, long style, int font) {
 		super(text, style);
 		construct(font, 0, Font.PLAIN, Const.FONTCOLOR, Const.BACKCOLOR, Const.SELECTEDCOLOR);
 	}
 	public ListLabelField(String text, int width, long style) {
 		super(text, style);
 		construct(Const.FONT, width, Font.PLAIN, Const.FONTCOLOR, Const.BACKCOLOR, Const.SELECTEDCOLOR);
 	}
 	public ListLabelField(String text, int fontsize, long style, int color) {
 		super(text, style);
 		construct(fontsize, 0, Font.PLAIN, color, Const.BACKCOLOR, Const.SELECTEDCOLOR);
 	}
 	public ListLabelField(String text, int fontsize, long style, int color, int width) {
 		super(text, style);
 		construct(fontsize, width, Font.PLAIN, color, Const.BACKCOLOR, Const.SELECTEDCOLOR);
 	}
 	public ListLabelField(String text, int color, int font, int fontsize, long style) {
 		super(text, style);
 		construct(fontsize, 0, font, color, Const.BACKCOLOR, Const.SELECTEDCOLOR);
 	}
 	public ListLabelField(String text, int color, int changeColor, int backColor, int font, int fontsize, long style) {
 		super(text, style);
 		construct(fontsize, 0, font, color, changeColor, backColor);
 	}
 	public ListLabelField(String text, int color, int changeColor, int backColor, int font, int fontsize, int width, long style) {
 		super(text,style);
 		construct(fontsize, width, font, color, changeColor, backColor);
 	}
 	
 	private void construct(int fontsize, int width, int fontStyle, int color, int changeColor, int backColor) {
 		_normalColor = color;
 		_changeColor = changeColor;
 		_currentColor = backColor;
 		_backColor = backColor;
 		button_centre = Const.getListboxCentre();
 		_width = width;
 		Font _font = getFont();
 		_font = _font.derive(fontStyle, fontsize);
 		setFont(_font);
 	}
 	
 	public boolean isFocusable() {
     	return true;
     }
 	
 	public int getPreferredWidth() {
 		return _width == 0 ? super.getPreferredWidth() : _width;
 	}
 	public int getPreferredHeight() {
		return this.getContentHeight();
 	}
 	
 	public void paint(Graphics g) {
 		if(!focus){
 			g.setColor(Const.FONTCOLOR);
 		}else{
 			g.setColor(Const.SELECTEDCOLOR);
 		}
 		//int xPts[] = {0,0,getPreferredWidth(),getPreferredWidth()};
 		//int yPts[] = {40,getPreferredHeight()+20,getPreferredHeight()+20,40};
 		//g.drawTexturedPath(xPts,yPts,null,null,0,0,Fixed32.ONE,0,0,Fixed32.ONE,button_centre);
 		super.paint(g);
 	}
 	
 	protected void onFocus(int direction) {
 		_currentColor = _changeColor;
 		focus = true;
 		invalidate();
 	}
 	protected void onUnfocus() {
 		_currentColor = _backColor;
 		focus = false;
 		invalidate();
 	}
 	protected void drawFocus(Graphics graphics, boolean on) {
         //Do nothing
     }
 	protected boolean navigationClick(int status, int time) {
         fieldChangeNotify(1);
         return true;
     }
 }
