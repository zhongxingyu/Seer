 package model;
 
 public final class Map {
 	private static Map instance = null;
 
 	protected static Map getInstance() {
 		if (instance == null) {
 			instance = new Map();
 			return instance;
 		}
 		return instance;
 	}
 
 	private static boolean map[][];
 	private static int mapColor[][];
 	private int lineCount;
 
 	private Map() {
 		map = new boolean[10][18];
 		mapColor = new int[10][18];
 		lineCount = 0;
 		initMapColor();
 	}
 
 	private void initMapColor() {
 
 		for (int y = 0; y < 18; ++y) {
 			for (int x = 0; x < 10; ++x) {
 				mapColor[x][y] = -1;
 			}
 		}
 
 	}
 
 	protected void addBrick(Brick b) {
 		for (int y = 0; y < 4; ++y) {
 			for (int x = 0; x < 4; ++x) {
 				if (b.get(x, y) && (b.getPosY() + y) < 18
 						&& (b.getPosX() + x) < 10) {
 					map[b.getPosX() + x][b.getPosY() + y] = true;
 					mapColor[b.getPosX() + x][b.getPosY() + y] = b.getScene();
 				}
 			}
 		}
 		checkLineFull();
 	}
 
 	protected void set(int x, int y, boolean value) {
 		map[x][y] = value;
 	}
 
 	protected boolean get(int x, int y) {
 		return map[x][y];
 	}
 
 	private void checkLineFull() {
 
 		for (int j = 17; j >= 0; --j) {
 			int i = 0;
 
 			while (i < 10) {
 				boolean mapvalue = map[i][j];
 				if (!mapvalue) {
 					break;
 				} else if (i == 9) {
 					deleteLine(j);
 					++lineCount;
 					
 					++j;
 					break;
 
 				} else {
 					++i;
 				}
 			}
 
 		}
 
 	}
 
 	private void deleteLine(int i) {
 		if (i != 0) {
 			for (int y = i; y >= 0; --y) {
 				if (y == 0) {
 					break;
 				} else {
 					for (int x = 0; x < 10; ++x) {
 						map[x][y] = map[x][y - 1];
 						mapColor[x][y] = mapColor[x][y-1];
 					}
 
 				}
 			}
 		}
 
 	}
 
 	protected int getMapColor(int x, int y) {
 		return mapColor[x][y];
 	}
 
 	/**
 	 * @return the lineCount
 	 */
 	protected int getLineCount() {
 		return lineCount;
 	}
 
 	/**
 	 * @param lineCount the lineCount to set
 	 */
	public void resetsetLineCount() {
 		this.lineCount = 0;
 	}
 
 }
