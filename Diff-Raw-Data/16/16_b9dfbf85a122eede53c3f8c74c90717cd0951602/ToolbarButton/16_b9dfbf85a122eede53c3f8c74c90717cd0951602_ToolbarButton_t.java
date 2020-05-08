 package com.blox.setgame.model;
 
 import com.blox.framework.v0.ITexture;
 import com.blox.framework.v0.util.Game;
 import com.blox.framework.v0.util.TextureDrawer;
 
 class ToolbarButton extends SetGameButton {
 	private static final int n = 1 << 0;
 	private static final int e = 1 << 1;
 	private static final int s = 1 << 2;
 	private static final int w = 1 << 3;
 
 	public static final int AlignN = n;
 	public static final int AlignNE = n | e;
 	public static final int AlignE = e;
 	public static final int AlignSE = s | e;
 	public static final int AlignS = s;
 	public static final int AlignSW = s | w;
 	public static final int AlignW = w;
 	public static final int AlignNW = n | w;
 	public static final int AlignCentered = 0;
			
 	private ITexture texture;
 	
 	ToolbarButton() {
		setWidth(Game.scale(SetGameImageButton.buttonSize));
		setHeight(Game.scale(SetGameImageButton.buttonSize));
 	}
 	
 	void setTexture(String id) {
 		texture = Game.getResourceManager().getTexture(id);
 	}
 	
 	@Override
 	protected void onDraw() {
 		TextureDrawer.draw(texture, this);
 	}
 	
 	@Override
 	public boolean ignoreViewport() {
 		return true;
 	}
 
 	@Override
 	public boolean ignoreShifting() {
 		return true;
 	}
 
 	void setLocation(int align, float marginX, float marginY) {
 		float x = 0;
 		float y = 0;
 		float width = getWidth();
 		float height = getHeight();
 
 		if ((align & e) == e)
 			x = Game.getScreenWidth() - width - marginX;
 		else if ((align & w) == w)
 			x = marginX;
 		else
 			x = (Game.getScreenWidth() - width) / 2 + marginX;
 
 		if ((align & n) == n)
 			y = Game.getScreenHeight() - height - marginY;
 		else if ((align & s) == s)
 			y = marginY;
 		else
 			y = (Game.getScreenHeight() - height) / 2 + marginY;
 
 		getLocation().set(x, y);
 	}
 }
