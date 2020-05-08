 package pl.shockah.easyslick;
 
 public enum EFontAlign {
 	TopLeft(0,0), TopCenter(1,0), TopRight(2,0), MiddleLeft(0,1), MiddleCenter(1,1), MiddleRight(2,1), BottomLeft(0,2), BottomCenter(1,2), BottomRight(2,2);
 	
	public static final EFontAlign[] array = new EFontAlign[9];
	public static final EFontAlign[][] pos = new EFontAlign[3][3];
 	
 	public final int x, y;
 	
 	private EFontAlign(int x, int y) {
 		this.x = x;
 		this.y = y;
		init();
	}
	private void init() {
		pos[x][y] = this;
		array[ordinal()] = this;
 	}
 	
 	public EFontAlign mirror(boolean h, boolean v) {
 		return pos[x == 1 ? 1 : (h ? (x == 0 ? 2 : 0) : x)][y == 1 ? 1 : (v ? (y == 0 ? 2 : 0) : y)];
 	}
 }
