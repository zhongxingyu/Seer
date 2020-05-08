 package net.mytcg.topcar.ui.custom;
 
 import net.mytcg.topcar.util.Const;
 import net.rim.device.api.math.Fixed32;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Graphics;
 
 public final class MenuThumbnailField extends Field {	
 	private boolean focus;
 	private boolean focusable = true;
 	
 	private Bitmap button_thumbnail;
 	private Bitmap main_image;
 	private Bitmap button_select;
 	MenuField menu;
 	
 	public MenuThumbnailField(Bitmap menuOption, Bitmap mainImage, MenuField menu) {
 		button_thumbnail = menuOption;
 		main_image = mainImage;
 		button_select = Const.getSelect();
 		this.menu = menu;
 	}
 	
 	protected void drawFocus(Graphics graphics, boolean on) {
        invalidate();
     }
 	
 	public boolean isFocus (){
 		return focus;
 	}
 	public Bitmap getThumbnail(){
 		return button_thumbnail;
 	}
 	public void setThumbnail(Bitmap thumb){
 		this.button_thumbnail = thumb;
 	}
 	public int getPreferredWidth() {
 		return 54 + (Const.getWidth() % 54)/(Const.getWidth()/54) ;
 	}
 	public int getPreferredHeight() {
 		return Const.getAlbumThumb().getHeight();
 	}
 	protected void layout(int width, int height) {
 		setExtent(getPreferredWidth(),getPreferredHeight());
     }
 	public boolean isFocusable() {
     	return focusable;
     }
 	public void setFocusable(boolean focusable){
 		this.focusable = focusable;
 	}
 	public void paint(Graphics g) {
 		
 		if (focus) {
 			g.drawBitmap(0, 7, button_select.getWidth(), button_select.getHeight(), button_select, 0, 0);
 		}	
 		
 		g.drawBitmap(4, 0, button_thumbnail.getWidth(), button_thumbnail.getHeight(), button_thumbnail, 0, 0);
 	}
 	protected void onFocus(int direction) {
 		focus = true;
 		menu.setImage(main_image);
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
