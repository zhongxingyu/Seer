 package net.mytcg.topcar.ui.custom;
 
 import net.mytcg.topcar.util.Const;
 import net.rim.device.api.math.Fixed32;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.component.ButtonField;
 
 public final class FixedButtonField extends Field {
 	private boolean focus;
 	private String label;
 	public boolean empty;
 	
 	private Bitmap button_centre;
 	private Bitmap button_left_edge;
 	private Bitmap button_right_edge;
 	
 	private Bitmap button_sel_centre;
 	private Bitmap button_sel_left_edge;
 	private Bitmap button_sel_right_edge;
 	
 	public FixedButtonField(String label) {
 		this(label, ButtonField.CONSUME_CLICK);
 	}
 	public FixedButtonField(String label, long style) {
 		super(style);
 		construct(label);
 	}
 	
 	public void onUndisplay() {
 		
 	}
 	public void onVisibilityChange(boolean visible) {
 		
 	}
 	public void focusRemove() {
 		
 	}
 	protected void drawFocus(Graphics graphics, boolean on) {
		paint(graphics);
     }
 	
 	public void construct(String label) {
 		button_centre = Const.getButtonCentre();
 		button_left_edge = Const.getButtonLeftEdge();
 		button_right_edge = Const.getButtonRightEdge();
 		
 		button_sel_centre = Const.getButtonSelCentre();
 		button_sel_left_edge = Const.getButtonSelLeftEdge();
 		button_sel_right_edge = Const.getButtonSelRightEdge();
 		
 		Font _font = getFont();
 		_font = _font.derive(Const.TYPE,Const.FONT);
 		setFont(_font);
 		this.label = label;
 		if ((label == null)||(label.length() <= 0)) {
 			empty = true;
 		}
 	}
 	public void setLabel(String label) {
 		this.label = label;
 		invalidate();
 	}
 	
 	public int getPreferredWidth() {
 		return (int)(Const.getWidth()/3);
 	}
 	public int getPreferredHeight() {
 		return button_sel_centre.getHeight();
 	}
 	protected void layout(int width, int height) {
 		setExtent(getPreferredWidth(),getPreferredHeight());
     }
 	public boolean isFocusable() {
 		return !empty;
     }
 	public void paint(Graphics g) {
 		
 		if (!empty) {
 			int xPts[] = {5,5,getPreferredWidth()-5,getPreferredWidth()-5};
 			int yPts[] = {0,getPreferredHeight(),getPreferredHeight(),0};
 			
 			g.setColor(Const.BUTTONCOLOR);
 			if (!focus) {
 				//g.setColor(Const.SELECTEDCOLOR);
 				g.drawTexturedPath(xPts,yPts,null,null,0,getPreferredHeight(),Fixed32.ONE,0,0,Fixed32.ONE,button_centre);
 				g.drawBitmap(0, 0, Const.getWidth(), getPreferredHeight(), button_left_edge, 0, 0);
 				g.drawBitmap(getPreferredWidth()-button_right_edge.getWidth(), 0, Const.getWidth(), getPreferredHeight(), button_right_edge, 0, 0);
 			} else {
 				g.drawTexturedPath(xPts,yPts,null,null,0,getPreferredHeight(),Fixed32.ONE,0,0,Fixed32.ONE,button_sel_centre);
 				g.drawBitmap(0, 0, Const.getWidth(), getPreferredHeight(), button_sel_left_edge, 0, 0);
 				g.drawBitmap(getPreferredWidth()-button_sel_right_edge.getWidth(), 0, Const.getWidth(), getPreferredHeight(), button_sel_right_edge, 0, 0);
 			}
 			g.drawText(label, (int)((getPreferredWidth()-getFont().getAdvance(label))/2), (int)((getPreferredHeight()-Const.FONT)/2));
 		}
 	}
 	protected void onFocus(int direction) {
 		focus = true;
 		invalidate();
 	}
 	protected void onUnfocus() {
 		focus = false;
 		invalidate();
 	}
 	protected boolean navigationClick(int status, int time) {
         fieldChangeNotify(1);
         return true;
     }
 }
