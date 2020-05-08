 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client;
 
 import java.awt.Color;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.Timer;
 
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 
 public class MapToolUtil {
 	
     private static Random random = new Random ( System.currentTimeMillis() );
 
     private static AtomicInteger nextTokenId = new AtomicInteger(1);
     
     /**
      * The map of color names to color values
      */
     private static final Map<String, Color> COLOR_MAP = new LinkedHashMap<String, Color>();
 
     /**
      * Set up the color map
      */
     static {
       COLOR_MAP.put("white", Color.WHITE);
       COLOR_MAP.put("lightgray", Color.LIGHT_GRAY);
       COLOR_MAP.put("gray", Color.GRAY);
       COLOR_MAP.put("darkgray", Color.DARK_GRAY);
       COLOR_MAP.put("black", Color.BLACK);
       COLOR_MAP.put("blue", Color.BLUE);
       COLOR_MAP.put("cyan", Color.CYAN);
       COLOR_MAP.put("green", Color.GREEN);
       COLOR_MAP.put("magenta", Color.MAGENTA);
       COLOR_MAP.put("orange", Color.ORANGE);
       COLOR_MAP.put("pink", Color.PINK);
       COLOR_MAP.put("red", Color.RED);
       COLOR_MAP.put("yellow", Color.YELLOW);
     }
 
     public static int getRandomNumber ( int max )
     {
         return getRandomNumber ( 0, max );
     }
     
     public static int getRandomNumber ( int min, int max )
     {
         return (int)(( ( max - min ) * random.nextDouble() ) + min);
     }
     
     public static float getRandomRealNumber ( float max )
     {
         return getRandomRealNumber ( 0, max );
     }
     
     public static float getRandomRealNumber ( float min, float max )
     {
         return (float)( ( max - min ) * random.nextDouble() ) + min;
     }
     
     public static boolean percentageCheckAbove ( int percentage )
     {
         return (random.nextDouble()* 100) > percentage;
     }
     
     public static boolean percentageCheckBelow ( int percentage )
     {
         double roll = random.nextDouble()* 100; 
         return roll < percentage;
     }
 
 	private static final Pattern NAME_PATTERN = Pattern.compile("(.*) (\\d+)");
     public static String nextTokenId(Zone zone, Token token) {
     	   	
     	boolean isToken = token.isToken();
     	String baseName = token.getName();
     	String newName;
     	
     	if(isToken && AppPreferences.getNewTokenNaming().equals(Token.NAME_USE_CREATURE)) {
     		newName = "Creature";
     	}
     	else if (baseName == null) {
 	    	int nextId = nextTokenId.getAndIncrement();
 	    	char ch = (char)('a' + MapTool.getPlayerList().indexOf(MapTool.getPlayer()));
 	    	return ch + Integer.toString(nextId);
     	}
     	else {
         	baseName = baseName.trim();
     		Matcher m = NAME_PATTERN.matcher(baseName);
     		
     		if (m.find())
     			newName = m.group(1);
     		else
     			newName = baseName;
     	}
 
     	boolean random = (isToken && AppPreferences.getDuplicateTokenNumber().equals(Token.NUM_RANDOM));
     	boolean addNumToGM = !AppPreferences.getTokenNumberDisplay().equals(Token.NUM_ON_NAME);
     	boolean addNumToName = !AppPreferences.getTokenNumberDisplay().equals(Token.NUM_ON_GM);
     	
 		if (zone.getTokenByName(newName) != null || random)
 		{		
 			int num;
 			if (random && isToken) {
 				// When 90 or more tokens, move to incremental naming or may never
 				// find a number if they all have this creature's base name
 				if (zone.getAllTokens().size() >= 89)
 					num = 10;
 				else
 					num = getRandomNumber(10,99);
 			}
 			else {
 				num = 2;
 			}
 
 			// Find the next available token number, this
 			// has to break at some point.
			
			while (zone.getTokenByName(newName + " " + num) != null ||
					// Or, check for repeat in numbering in the GM Name.
					( zone.getTokenByName(newName)!= null && zone.getTokenByGMName(Integer.toString(num)) != null) ) {
 				if (random && zone.getAllTokens().size() < 89)
 					num = getRandomNumber(10,99);
 				else
 					num++;
 			}
 
 			if ( addNumToName ) {
 				newName += " ";
 				newName += num;
 			}
 			
 			if ( addNumToGM ) {
 				token.setGMName(Integer.toString(num));
 			}
 		}	
 
 		return newName;
 		
     }
 
     	
     public static boolean isDebugEnabled() {
     	return System.getProperty("MAPTOOL_DEV") != null;
     }
 
     public static boolean isValidColor(String name) {
     	return COLOR_MAP.containsKey(name);
     }
 	
     public static Color getColor(String name) {
     	return COLOR_MAP.get(name);
     }
     
     public static Set<String> getColorNames() {
     	return COLOR_MAP.keySet();
     }
     
 }
