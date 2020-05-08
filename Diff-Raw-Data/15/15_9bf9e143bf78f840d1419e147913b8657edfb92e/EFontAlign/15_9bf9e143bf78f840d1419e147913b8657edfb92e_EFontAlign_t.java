 package pl.shockah.easyslick;
 
 public enum EFontAlign {
 	TopLeft(0,0), TopCenter(1,0), TopRight(2,0), MiddleLeft(0,1), MiddleCenter(1,1), MiddleRight(2,1), BottomLeft(0,2), BottomCenter(1,2), BottomRight(2,2);
 	
	public static final EFontAlign[] array;
	public static final EFontAlign[][] pos;
	
	static {
		array = new EFontAlign[]{TopLeft,TopCenter,TopRight,MiddleLeft,MiddleCenter,MiddleRight,BottomLeft,BottomCenter,BottomRight};
		pos = new EFontAlign[][]{{TopLeft,MiddleLeft,BottomLeft},{TopCenter,MiddleCenter,BottomCenter},{TopRight,MiddleRight,BottomRight}};
	}
 	
 	public final int x, y;
 	
 	private EFontAlign(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 	
 	public EFontAlign mirror(boolean h, boolean v) {
 		return pos[x == 1 ? 1 : (h ? (x == 0 ? 2 : 0) : x)][y == 1 ? 1 : (v ? (y == 0 ? 2 : 0) : y)];
 	}
 }
