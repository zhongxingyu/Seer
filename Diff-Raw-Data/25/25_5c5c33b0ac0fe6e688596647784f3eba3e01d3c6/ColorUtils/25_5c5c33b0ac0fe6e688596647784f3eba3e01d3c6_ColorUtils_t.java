 package org.bodytrack.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import gwt.g2d.client.graphics.Color;
 import gwt.g2d.client.graphics.KnownColor;
 
 /**
  * A class containing utilities related to colors.
  *
  * <p>This class contains constants for all the basic CSS colors.
  * The colors are as defined in the CSS3 color module, at
  * <a href="http://www.w3.org/TR/css3-color/">
  * http://www.w3.org/TR/css3-color/</a>.
  * </p>
  */
 public final class ColorUtils {
 	// --------------------------------------------------------------
 	// The CSS basic colors
 	// --------------------------------------------------------------
 
 	public static final Color BLACK = new Color(0x00, 0x00, 0x00);
 	public static final Color SILVER = new Color(0xC0, 0xC0, 0xC0);
 	public static final Color GRAY = new Color(0x80, 0x80, 0x80);
 	public static final Color WHITE = new Color(0xFF, 0xFF, 0xFF);
 	public static final Color MAROON = new Color(0x80, 0x00, 0x00);
 	public static final Color RED = new Color(0xFF, 0x00, 0x00);
 	public static final Color PURPLE = new Color(0x80, 0x00, 0x80);
 	public static final Color FUCHSIA = new Color(0xFF, 0x00, 0xFF);
 	public static final Color GREEN = new Color(0x00, 0x80, 0x00);
 	public static final Color LIME = new Color(0x00, 0xFF, 0x00);
 	public static final Color OLIVE = new Color(0x80, 0x80, 0x00);
 	public static final Color YELLOW = new Color(0xFF, 0xFF, 0x00);
 	public static final Color NAVY = new Color(0x00, 0x00, 0x80);
 	public static final Color BLUE = new Color(0x00, 0x00, 0xFF);
 	public static final Color TEAL = new Color(0x00, 0x80, 0x80);
 	public static final Color AQUA = new Color(0x00, 0xFF, 0xFF);
 
 	// These maps are used for converting between basic names and colors
 	// The names are perfectly valid to use in CSS styles unchanged
 	private static final Map<String, Color> basicNameToColor =
 		new HashMap<String, Color>();
 	private static final Map<Color, String> basicColorToName =
 		new HashMap<Color, String>();
 
 	private static final Map<String, Color> extendedNameToColor =
 		new HashMap<String, Color>();
 	private static final Map<Color, String> extendedColorToName =
 		new HashMap<Color, String>();
 
 	static {
 		// We always use lower-case names
 
 		basicNameToColor.put("black", BLACK);
 		basicNameToColor.put("silver", SILVER);
 		basicNameToColor.put("gray", GRAY);
 		basicNameToColor.put("white", WHITE);
 		basicNameToColor.put("maroon", MAROON);
 		basicNameToColor.put("red", RED);
 		basicNameToColor.put("purple", PURPLE);
 		basicNameToColor.put("fuchsia", FUCHSIA);
 		basicNameToColor.put("green", GREEN);
 		basicNameToColor.put("lime", LIME);
 		basicNameToColor.put("olive", OLIVE);
 		basicNameToColor.put("yellow", YELLOW);
 		basicNameToColor.put("navy", NAVY);
 		basicNameToColor.put("blue", BLUE);
 		basicNameToColor.put("teal", TEAL);
 		basicNameToColor.put("aqua", AQUA);
 
 		// Since the mapping is one-to-one, we can make the reverse
 		// map very easily
 		for (Map.Entry<String, Color> entry: basicNameToColor.entrySet())
 			basicColorToName.put(entry.getValue(), entry.getKey());
 
 		// Now for the extended color palette, which GWT-G2D knows the
 		// names of but doesn't export names for.  See
 		// http://www.w3.org/TR/css3-color/#svg-color for the spec, and
 		// KnownColor.java in GWT-G2D for the list of colors that allowed
 		// this mapping
 		extendedNameToColor.put("aliceblue", KnownColor.ALICE_BLUE);
 		extendedNameToColor.put("antiquewhite", KnownColor.ANTIQUE_WHITE);
 		extendedNameToColor.put("aqua", KnownColor.AQUA);
 		extendedNameToColor.put("aquamarine", KnownColor.AQUAMARINE);
 		extendedNameToColor.put("azure", KnownColor.AZURE);
 		extendedNameToColor.put("beige", KnownColor.BEIGE);
 		extendedNameToColor.put("bisque", KnownColor.BISQUE);
 		extendedNameToColor.put("black", KnownColor.BLACK);
 		extendedNameToColor.put("blanchedalmond", KnownColor.BLANCHED_ALMOND);
 		extendedNameToColor.put("blue", KnownColor.BLUE);
 		extendedNameToColor.put("blueviolet", KnownColor.BLUE_VIOLET);
 		extendedNameToColor.put("brown", KnownColor.BROWN);
 		extendedNameToColor.put("burlywood", KnownColor.BURLY_WOOD);
 		extendedNameToColor.put("cadetblue", KnownColor.CADET_BLUE);
 		extendedNameToColor.put("chartreuse", KnownColor.CHARTREUSE);
 		extendedNameToColor.put("chocolate", KnownColor.CHOCOLATE);
 		extendedNameToColor.put("coral", KnownColor.CORAL);
 		extendedNameToColor.put("cornflowerblue", KnownColor.CORNFLOWER_BLUE);
 		extendedNameToColor.put("cornsilk", KnownColor.CORNSILK);
 		extendedNameToColor.put("crimson", KnownColor.CRIMSON);
 		extendedNameToColor.put("cyan", KnownColor.CYAN);
 		extendedNameToColor.put("darkblue", KnownColor.DARK_BLUE);
 		extendedNameToColor.put("darkcyan", KnownColor.DARK_CYAN);
 		extendedNameToColor.put("darkgoldenrod", KnownColor.DARK_GOLDEN_ROD);
 		extendedNameToColor.put("darkgray", KnownColor.DARK_GRAY);
 		extendedNameToColor.put("darkgreen", KnownColor.DARK_GREEN);
 		extendedNameToColor.put("darkkhaki", KnownColor.DARK_KHAKI);
 		extendedNameToColor.put("darkmagenta", KnownColor.DARK_MAGENTA);
 		extendedNameToColor.put("darkolivegreen", KnownColor.DARK_OLIVE_GREEN);
 		extendedNameToColor.put("darkorange", KnownColor.DARKORANGE);
 		extendedNameToColor.put("darkorchid", KnownColor.DARK_ORCHID);
 		extendedNameToColor.put("darkred", KnownColor.DARK_RED);
 		extendedNameToColor.put("darksalmon", KnownColor.DARK_SALMON);
 		extendedNameToColor.put("darkseagreen", KnownColor.DARK_SEA_GREEN);
 		extendedNameToColor.put("darkslateblue", KnownColor.DARK_SLATE_BLUE);
 		extendedNameToColor.put("darkslategray", KnownColor.DARK_SLATE_GRAY);
 		extendedNameToColor.put("darkturquoise", KnownColor.DARK_TURQUOISE);
 		extendedNameToColor.put("darkviolet", KnownColor.DARK_VIOLET);
 		extendedNameToColor.put("deeppink", KnownColor.DEEP_PINK);
 		extendedNameToColor.put("deepskyblue", KnownColor.DEEP_SKY_BLUE);
 		extendedNameToColor.put("dimgray", KnownColor.DIM_GRAY);
 		extendedNameToColor.put("dodgerblue", KnownColor.DODGER_BLUE);
 		extendedNameToColor.put("firebrick", KnownColor.FIRE_BRICK);
 		extendedNameToColor.put("floralwhite", KnownColor.FLORAL_WHITE);
 		extendedNameToColor.put("forestgreen", KnownColor.FOREST_GREEN);
 		extendedNameToColor.put("fuchsia", KnownColor.FUCHSIA);
 		extendedNameToColor.put("gainsboro", KnownColor.GAINSBORO);
 		extendedNameToColor.put("ghostwhite", KnownColor.GHOST_WHITE);
 		extendedNameToColor.put("gold", KnownColor.GOLD);
 		extendedNameToColor.put("goldenrod", KnownColor.GOLDEN_ROD);
 		extendedNameToColor.put("gray", KnownColor.GRAY);
 		extendedNameToColor.put("green", KnownColor.GREEN);
 		extendedNameToColor.put("greenyellow", KnownColor.GREEN_YELLOW);
 		extendedNameToColor.put("honeydew", KnownColor.HONEY_DEW);
 		extendedNameToColor.put("hotpink", KnownColor.HOT_PINK);
 		extendedNameToColor.put("indianred", KnownColor.INDIAN_RED);
 		extendedNameToColor.put("indigo", KnownColor.INDIGO);
 		extendedNameToColor.put("ivory", KnownColor.IVORY);
 		extendedNameToColor.put("khaki", KnownColor.KHAKI);
 		extendedNameToColor.put("lavender", KnownColor.LAVENDER);
 		extendedNameToColor.put("lavenderblush", KnownColor.LAVENDER_BLUSH);
 		extendedNameToColor.put("lawngreen", KnownColor.LAWN_GREEN);
 		extendedNameToColor.put("lemonchiffon", KnownColor.LEMON_CHIFFON);
 		extendedNameToColor.put("lightblue", KnownColor.LIGHT_BLUE);
 		extendedNameToColor.put("lightcoral", KnownColor.LIGHT_CORAL);
 		extendedNameToColor.put("lightcyan", KnownColor.LIGHT_CYAN);
 		extendedNameToColor.put("lightgoldenrodyellow",
 				KnownColor.LIGHT_GOLDEN_ROD_YELLOW);
 		extendedNameToColor.put("lightgrey", KnownColor.LIGHT_GREY);
 		extendedNameToColor.put("lightgreen", KnownColor.LIGHT_GREEN);
 		extendedNameToColor.put("lightpink", KnownColor.LIGHT_PINK);
 		extendedNameToColor.put("lightsalmon", KnownColor.LIGHT_SALMON);
 		extendedNameToColor.put("lightseagreen", KnownColor.LIGHT_SEA_GREEN);
 		extendedNameToColor.put("lightskyblue", KnownColor.LIGHT_SKY_BLUE);
 		extendedNameToColor.put("lightslategray", KnownColor.LIGHT_SLATE_GRAY);
 		extendedNameToColor.put("lightsteelblue", KnownColor.LIGHT_STEEL_BLUE);
 		extendedNameToColor.put("lightyellow", KnownColor.LIGHT_YELLOW);
 		extendedNameToColor.put("lime", KnownColor.LIME);
 		extendedNameToColor.put("limegreen", KnownColor.LIME_GREEN);
 		extendedNameToColor.put("linen", KnownColor.LINEN);
 		extendedNameToColor.put("magenta", KnownColor.MAGENTA);
 		extendedNameToColor.put("maroon", KnownColor.MAROON);
 		extendedNameToColor.put("mediumaquamarine",
 				KnownColor.MEDIUM_AQUA_MARINE);
 		extendedNameToColor.put("mediumblue", KnownColor.MEDIUM_BLUE);
 		extendedNameToColor.put("mediumorchid", KnownColor.MEDIUM_ORCHID);
 		extendedNameToColor.put("mediumpurple", KnownColor.MEDIUM_PURPLE);
 		extendedNameToColor.put("mediumseagreen", KnownColor.MEDIUM_SEA_GREEN);
 		extendedNameToColor.put("mediumslateblue",
 				KnownColor.MEDIUM_SLATE_BLUE);
 		extendedNameToColor.put("mediumspringgreen",
 				KnownColor.MEDIUM_SPRING_GREEN);
 		extendedNameToColor.put("mediumturquoise",
 				KnownColor.MEDIUM_TURQUOISE);
 		extendedNameToColor.put("mediumvioletred",
 				KnownColor.MEDIUM_VIOLET_RED);
 		extendedNameToColor.put("midnightblue", KnownColor.MIDNIGHT_BLUE);
 		extendedNameToColor.put("mintcream", KnownColor.MINT_CREAM);
 		extendedNameToColor.put("mistyrose", KnownColor.MISTY_ROSE);
 		extendedNameToColor.put("moccasin", KnownColor.MOCCASIN);
 		extendedNameToColor.put("navajowhite", KnownColor.NAVAJO_WHITE);
 		extendedNameToColor.put("navy", KnownColor.NAVY);
 		extendedNameToColor.put("oldlace", KnownColor.OLD_LACE);
 		extendedNameToColor.put("olive", KnownColor.OLIVE);
 		extendedNameToColor.put("olivedrab", KnownColor.OLIVE_DRAB);
 		extendedNameToColor.put("orange", KnownColor.ORANGE);
 		extendedNameToColor.put("orangered", KnownColor.ORANGE_RED);
 		extendedNameToColor.put("orchid", KnownColor.ORCHID);
 		extendedNameToColor.put("palegoldenrod", KnownColor.PALE_GOLDEN_ROD);
 		extendedNameToColor.put("palegreen", KnownColor.PALE_GREEN);
 		extendedNameToColor.put("paleturquoise", KnownColor.PALE_TURQUOISE);
 		extendedNameToColor.put("palevioletred", KnownColor.PALE_VIOLET_RED);
 		extendedNameToColor.put("papayawhip", KnownColor.PAPAYA_WHIP);
 		extendedNameToColor.put("peachpuff", KnownColor.PEACH_PUFF);
 		extendedNameToColor.put("peru", KnownColor.PERU);
 		extendedNameToColor.put("pink", KnownColor.PINK);
 		extendedNameToColor.put("plum", KnownColor.PLUM);
 		extendedNameToColor.put("powderblue", KnownColor.POWDER_BLUE);
 		extendedNameToColor.put("purple", KnownColor.PURPLE);
 		extendedNameToColor.put("red", KnownColor.RED);
 		extendedNameToColor.put("rosybrown", KnownColor.ROSY_BROWN);
 		extendedNameToColor.put("royalblue", KnownColor.ROYAL_BLUE);
 		extendedNameToColor.put("saddlebrown", KnownColor.SADDLE_BROWN);
 		extendedNameToColor.put("salmon", KnownColor.SALMON);
 		extendedNameToColor.put("sandybrown", KnownColor.SANDY_BROWN);
 		extendedNameToColor.put("seagreen", KnownColor.SEA_GREEN);
 		extendedNameToColor.put("seashell", KnownColor.SEA_SHELL);
 		extendedNameToColor.put("sienna", KnownColor.SIENNA);
 		extendedNameToColor.put("silver", KnownColor.SILVER);
 		extendedNameToColor.put("skyblue", KnownColor.SKY_BLUE);
 		extendedNameToColor.put("slateblue", KnownColor.SLATE_BLUE);
 		extendedNameToColor.put("slategray", KnownColor.SLATE_GRAY);
 		extendedNameToColor.put("snow", KnownColor.SNOW);
 		extendedNameToColor.put("springgreen", KnownColor.SPRING_GREEN);
 		extendedNameToColor.put("steelblue", KnownColor.STEEL_BLUE);
 		extendedNameToColor.put("tan", KnownColor.TAN);
 		extendedNameToColor.put("teal", KnownColor.TEAL);
 		extendedNameToColor.put("thistle", KnownColor.THISTLE);
 		extendedNameToColor.put("tomato", KnownColor.TOMATO);
 		extendedNameToColor.put("turquoise", KnownColor.TURQUOISE);
 		extendedNameToColor.put("violet", KnownColor.VIOLET);
 		extendedNameToColor.put("wheat", KnownColor.WHEAT);
 		extendedNameToColor.put("white", KnownColor.WHITE);
 		extendedNameToColor.put("whitesmoke", KnownColor.WHITE_SMOKE);
 		extendedNameToColor.put("yellow", KnownColor.YELLOW);
 		extendedNameToColor.put("yellowgreen", KnownColor.YELLOW_GREEN);
 
 		// Since the mapping is one-to-one, we can make the reverse
 		// map very easily
 		for (Map.Entry<String, Color> entry: extendedNameToColor.entrySet())
 			extendedColorToName.put(entry.getValue(), entry.getKey());
 	}
 
	private ColorUtils() {
		throw new UnsupportedOperationException();
	}

 	public static boolean haveColor(Color c) {
 		return (c != null)
 			&& (basicColorToName.containsKey(c)
 					|| extendedColorToName.containsKey(c));
 	}
 
 	public static boolean haveColor(String name) {
 		if (name != null) {
 			name = name.toLowerCase();
 			return (basicNameToColor.containsKey(name)
 				|| extendedNameToColor.containsKey(name));
 		}
 
 		return false;
 	}
 
 	public static String getColorName(Color c) {
 		if (c == null)
 			throw new NullPointerException("Can't get name for null color");
 
 		if (haveColor(c)) {
 			String name = basicColorToName.get(c);
 			return (name != null) ? name : extendedColorToName.get(c);
 		}
 
 		return c.getColorCode();
 	}
 
 	/**
 	 * Searches the list of basic and extended colors for name
 	 *
 	 * @param name
 	 * 	The name to check in the list of CSS basic and extended colors
 	 * @return
 	 * 	A color object representing the color that name refers to, as long
 	 * 	as name refers to a CSS basic or extended color.  Otherwise,
 	 * 	returns <code>null</code>
 	 * @throws NullPointerException
 	 * 	If name is <code>null</code>
 	 */
 	public static Color getNamedColor(String name) {
 		if (name == null)
 			throw new NullPointerException("Can't get color for null name");
 
 		name = name.toLowerCase();
 
 		if (haveColor(name)) {
 			Color basicColor = basicNameToColor.get(name);
 			return (basicColor != null)
 				? basicColor : extendedNameToColor.get(name);
 		}
 
 		return null;
 	}
 
 	/**
 	 * A smarter version of {@link #getNamedColor(String)}.
 	 *
 	 * <p>This first tries to use {@link #getNamedColor(String)}, and, if
 	 * that fails, tries to parse name for some common color code styles.
 	 * If that also fails, returns <code>null</code>.</p>
 	 *
 	 * @param name
 	 * 	The name of the color to check
 	 * @return
 	 * 	A color for name if possible, <code>null</code> otherwise
 	 * @throws NullPointerException
 	 * 	If name is <code>null</code>
 	 */
 	public static Color buildColor(String name) {
 		Color namedColor = getNamedColor(name);
 		if (namedColor != null)
 			return namedColor;
 
 		name = name.toUpperCase();
 
 		if (name.startsWith("#"))
 			return parseHexColor(name);
 		if (name.startsWith("RGBA"))
 			return parseRgbaColor(name);
 		if (name.startsWith("RGB"))
 			return parseRgbColor(name);
 
 		// Chances are, if we have gotten this far, the KnownColor
 		// check won't help either
 		List<KnownColor> knownColors =
 			new ArrayList<KnownColor>(KnownColor.getKnownColors());
 		for (KnownColor color: knownColors) {
 			if (color.getColorCode().equalsIgnoreCase(name))
 				return color;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Parses a color code of the form &quot;#RGB&quot; or &quot;#RRGGBB&quot;
 	 *
 	 * @param code
 	 * 	The non-<code>null</code> upper-case color code to parse
 	 * @return
 	 * 	A color representing the color a browser would handle code as,
 	 * 	or <code>null</code> if we can't parse it
 	 */
 	private static Color parseHexColor(String code) {
 		if (code.startsWith("#"))
 			code = code.substring(1);
 		else
 			return null; // We only deal with colors of the form "#RRGGBB"
 
 		if (code.length() == 3) {
 			// Case where "#ABC" is interpreted to mean "#AABBCC"
 			char r = code.charAt(0);
 			char g = code.charAt(1);
 			char b = code.charAt(2);
 
 			try {
 				int redHalf = Character.digit(r, 16);
 				int greenHalf = Character.digit(g, 16);
 				int blueHalf = Character.digit(b, 16);
 
 				int red = (redHalf << 4) + redHalf;
 				int green = (greenHalf << 4) + greenHalf;
 				int blue = (blueHalf << 4) + blueHalf;
 
 				return new Color(red, green, blue);
 			} catch (NumberFormatException e) {
 				return null;
 			}
 		} else if (code.length() == 6) {
 			// Normal case
 			String r = code.substring(0, 2);
 			String g = code.substring(2, 4);
 			String b = code.substring(4);
 
 			try {
 				int red = Integer.parseInt(r, 16);
 				int green = Integer.parseInt(g, 16);
 				int blue = Integer.parseInt(b, 16);
 
 				return new Color(red, green, blue);
 			} catch (NumberFormatException e) {
 				return null;
 			}
 		}
 
 		return null; // Ill-formed color code
 	}
 
 	/**
 	 * Parses a color code of the form &quot;RGBA(red,green,blue,alpha)&quot;
 	 *
 	 * <p>This is especially useful, since it parses the default returned value
 	 * of {@link gwt.g2d.client.graphics.Color#getColorCode()
 	 * Color.getColorCode()}.</p>
 	 *
 	 * @param code
 	 * 	The non-<code>null</code> upper-case color code to parse
 	 * @return
 	 * 	A color representing the color a browser would handle code as,
 	 * 	or <code>null</code> if we can't parse it
 	 */
 	private static Color parseRgbaColor(String code) {
 		// Pull off the rgba
 		if (code.startsWith("RGBA"))
 			code = code.substring(4);
 		else
 			return null; // We only deal with colors of the form RGBA(...)
 
 		// Pull off the parens
 		if (code.startsWith("(") && code.endsWith(")"))
 			code = code.substring(1, code.length() - 1);
 		else
 			return null;
 
 		// Now for the real parsing
 		String[] parts = code.split(",");
 		if (parts.length != 4)
 			return null;
 
 		String r = parts[0].trim();
 		String g = parts[1].trim();
 		String b = parts[2].trim();
 		String a = parts[3].trim();
 
 		try {
 			int red = Integer.parseInt(r);
 			int green = Integer.parseInt(g);
 			int blue = Integer.parseInt(b);
 			double alpha = Double.parseDouble(a);
 
 			return new Color(red, green, blue, alpha);
 		} catch (NumberFormatException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Parses a color code of the form &quot;RGB(red,green,blue)&quot;
 	 *
 	 * @param code
 	 * 	The non-<code>null</code> upper-case color code to parse
 	 * @return
 	 * 	A color representing the color a browser would handle code as,
 	 * 	or <code>null</code> if we can't parse it
 	 */
 	private static Color parseRgbColor(String code) {
 		// Pull off the rgb
 		if (code.startsWith("RGB"))
 			code = code.substring(3);
 		else
 			return null; // We only deal with colors of the form RGB(...)
 
 		// Pull off the parens
 		if (code.startsWith("(") && code.endsWith(")"))
 			code = code.substring(1, code.length() - 1);
 		else
 			return null;
 
 		// Now for the real parsing
 		String[] parts = code.split(",");
 		if (parts.length != 3)
 			return null;
 
 		String r = parts[0].trim();
 		String g = parts[1].trim();
 		String b = parts[2].trim();
 
 		try {
 			int red = Integer.parseInt(r);
 			int green = Integer.parseInt(g);
 			int blue = Integer.parseInt(b);
 
 			return new Color(red, green, blue);
 		} catch (NumberFormatException e) {
 			return null;
 		}
 	}
 
 	public static Color getColor(String name, Color defaultColor) {
 		if (name == null)
 			return defaultColor;
 		Color trueColor = buildColor(name);
 		return (trueColor != null) ? trueColor : defaultColor;
 	}
 }
