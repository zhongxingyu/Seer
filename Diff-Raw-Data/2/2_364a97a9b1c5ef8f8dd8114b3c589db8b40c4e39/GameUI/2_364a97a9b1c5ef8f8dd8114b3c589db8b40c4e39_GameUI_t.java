 package ui;
 
 import jcurses.system.*;
 import map.Map;
 
 public class GameUI {
 	private static CharColor mapFontColor;
 	private static int windowWidth;
 	private static int windowHeight;
 	
 	public static void init() {
 		Toolkit.init();
 		mapFontColor = new CharColor(CharColor.BLACK, CharColor.WHITE);
 		windowWidth = Toolkit.getScreenWidth();
 		windowHeight = Toolkit.getScreenHeight();
 	}
 	
 	public static void drawMap() {
 		Toolkit.clearScreen(mapFontColor);
 		String[] mapInStrings = Map.toStringArray();
 		for (int i = 0; i < windowHeight; i++)
			Toolkit.printString(mapInStrings[i], 0, windowHeight - i - 1, mapFontColor);
 	}
 	
 	public static char getInputChar() {
 		InputChar input = Toolkit.readCharacter();
 		while (input.isSpecialCode())
 			input = Toolkit.readCharacter();
 		return input.getCharacter();
 	}
 	
 	public static void showMessage(String msg) {
 		Toolkit.clearScreen(mapFontColor);
 		Toolkit.printString(msg, 0, 0, mapFontColor);
 		if (Toolkit.readCharacter() != null) return;
 	}
 	
 	public static void close() {
 		Toolkit.clearScreen(mapFontColor);
 		Toolkit.shutdown();
 	}
 	
 	public static int getMapWidth() {
 		return windowWidth;
 	}
 	
 	public static int getMapHeight() {
 		return windowHeight;
 	}
 
 }
