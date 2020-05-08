 package sofia.graphics;
 
 import sofia.app.Persistor;
 import sofia.graphics.internal.ColorPersistor;
 import sofia.util.Random;
 
 //-------------------------------------------------------------------------
 /**
  * Represents a color. Colors are immutable -- once one is created, it is not
  * possible to change its color components. Methods like {@link #lighter} and
  * {@link #darker()} return a new color rather than modify the receiver.
  *
  * @author Tony Allevato
  * @version 2012.03.13
  */
 @Persistor(ColorPersistor.class)
 public class Color
 {
 	//~ Instance/static fields ................................................
 
 	private int rawColor;
 	private float[] hsv;
 
 	private static final double FACTOR = 0.7;
 
 	public static final Color aliceBlue = rgb(0xF0, 0xF8, 0xFF);
 	public static final Color antiqueWhite = rgb(0xFA, 0xEB, 0xD7);
 	public static final Color aqua = rgb(0x00, 0xFF, 0xFF);
 	public static final Color aquamarine = rgb(0x7F, 0xFF, 0xD4);
 	public static final Color azure = rgb(0xF0, 0xFF, 0xFF);
 	public static final Color beige = rgb(0xF5, 0xF5, 0xDC);
 	public static final Color bisque = rgb(0xFF, 0xE4, 0xC4);
 	public static final Color black = rgb(0x00, 0x00, 0x00);
 	public static final Color blanchedAlmond = rgb(0xFF, 0xEB, 0xCD);
 	public static final Color blue = rgb(0x00, 0x00, 0xFF);
 	public static final Color blueViolet = rgb(0x8A, 0x2B, 0xE2);
 	public static final Color brown = rgb(0xA5, 0x2A, 0x2A);
 	public static final Color burlyWood = rgb(0xDE, 0xB8, 0x87);
 	public static final Color cadetBlue = rgb(0x5F, 0x9E, 0xA0);
 	public static final Color chartreuse = rgb(0x7F, 0xFF, 0x00);
 	public static final Color chocolate = rgb(0xD2, 0x69, 0x1E);
 	public static final Color coral = rgb(0xFF, 0x7F, 0x50);
 	public static final Color cornflowerBlue = rgb(0x64, 0x95, 0xED);
 	public static final Color cornsilk = rgb(0xFF, 0xF8, 0xDC);
 	public static final Color crimson = rgb(0xDC, 0x14, 0x3C);
 	public static final Color cyan = rgb(0x00, 0xFF, 0xFF);
 	public static final Color darkBlue = rgb(0x00, 0x00, 0x8B);
 	public static final Color darkCyan = rgb(0x00, 0x8B, 0x8B);
 	public static final Color darkGoldenRod = rgb(0xB8, 0x86, 0x0B);
 	public static final Color darkGray = rgb(0xA9, 0xA9, 0xA9);
 	public static final Color darkGreen = rgb(0x00, 0x64, 0x00);
 	public static final Color darkKhaki = rgb(0xBD, 0xB7, 0x6B);
 	public static final Color darkMagenta = rgb(0x8B, 0x00, 0x8B);
 	public static final Color darkOliveGreen = rgb(0x55, 0x6B, 0x2F);
 	public static final Color darkOrange = rgb(0xFF, 0x8C, 0x00);
 	public static final Color darkOrchid = rgb(0x99, 0x32, 0xCC);
 	public static final Color darkRed = rgb(0x8B, 0x00, 0x00);
 	public static final Color darkSalmon = rgb(0xE9, 0x96, 0x7A);
 	public static final Color darkSeaGreen = rgb(0x8F, 0xBC, 0x8F);
 	public static final Color darkSlateBlue = rgb(0x48, 0x3D, 0x8B);
 	public static final Color darkSlateGray = rgb(0x2F, 0x4F, 0x4F);
 	public static final Color darkTurquoise = rgb(0x00, 0xCE, 0xD1);
 	public static final Color darkViolet = rgb(0x94, 0x00, 0xD3);
 	public static final Color deepPink = rgb(0xFF, 0x14, 0x93);
 	public static final Color deepSkyBlue = rgb(0x00, 0xBF, 0xFF);
 	public static final Color dimGray = rgb(0x69, 0x69, 0x69);
 	public static final Color dodgerBlue = rgb(0x1E, 0x90, 0xFF);
 	public static final Color fireBrick = rgb(0xB2, 0x22, 0x22);
 	public static final Color floralWhite = rgb(0xFF, 0xFA, 0xF0);
 	public static final Color forestGreen = rgb(0x22, 0x8B, 0x22);
 	public static final Color fuchsia = rgb(0xFF, 0x00, 0xFF);
 	public static final Color gainsboro = rgb(0xDC, 0xDC, 0xDC);
 	public static final Color ghostWhite = rgb(0xF8, 0xF8, 0xFF);
 	public static final Color gold = rgb(0xFF, 0xD7, 0x00);
 	public static final Color goldenRod = rgb(0xDA, 0xA5, 0x20);
 	public static final Color gray = rgb(0x80, 0x80, 0x80);
 	public static final Color green = rgb(0x00, 0x80, 0x00);
 	public static final Color greenYellow = rgb(0xAD, 0xFF, 0x2F);
 	public static final Color honeyDew = rgb(0xF0, 0xFF, 0xF0);
 	public static final Color hotPink = rgb(0xFF, 0x69, 0xB4);
 	public static final Color indianRed = rgb(0xCD, 0x5C, 0x5C);
 	public static final Color indigo = rgb(0x4B, 0x00, 0x82);
 	public static final Color ivory = rgb(0xFF, 0xFF, 0xF0);
 	public static final Color khaki = rgb(0xF0, 0xE6, 0x8C);
 	public static final Color lavender = rgb(0xE6, 0xE6, 0xFA);
 	public static final Color lavenderBlush = rgb(0xFF, 0xF0, 0xF5);
 	public static final Color lawnGreen = rgb(0x7C, 0xFC, 0x00);
 	public static final Color lemonChiffon = rgb(0xFF, 0xFA, 0xCD);
 	public static final Color lightBlue = rgb(0xAD, 0xD8, 0xE6);
 	public static final Color lightCoral = rgb(0xF0, 0x80, 0x80);
 	public static final Color lightCyan = rgb(0xE0, 0xFF, 0xFF);
 	public static final Color lightGoldenRodYellow = rgb(0xFA, 0xFA, 0xD2);
 	public static final Color lightGray = rgb(0xD3, 0xD3, 0xD3);
 	public static final Color lightGreen = rgb(0x90, 0xEE, 0x90);
 	public static final Color lightPink = rgb(0xFF, 0xB6, 0xC1);
 	public static final Color lightSalmon = rgb(0xFF, 0xA0, 0x7A);
 	public static final Color lightSeaGreen = rgb(0x20, 0xB2, 0xAA);
 	public static final Color lightSkyBlue = rgb(0x87, 0xCE, 0xFA);
 	public static final Color lightSlateGray = rgb(0x77, 0x88, 0x99);
 	public static final Color lightSteelBlue = rgb(0xB0, 0xC4, 0xDE);
 	public static final Color lightYellow = rgb(0xFF, 0xFF, 0xE0);
 	public static final Color lime = rgb(0x00, 0xFF, 0x00);
 	public static final Color limeGreen = rgb(0x32, 0xCD, 0x32);
 	public static final Color linen = rgb(0xFA, 0xF0, 0xE6);
 	public static final Color magenta = rgb(0xFF, 0x00, 0xFF);
 	public static final Color maroon = rgb(0x80, 0x00, 0x00);
 	public static final Color mediumAquamarine = rgb(0x66, 0xCD, 0xAA);
 	public static final Color mediumBlue = rgb(0x00, 0x00, 0xCD);
 	public static final Color mediumOrchid = rgb(0xBA, 0x55, 0xD3);
 	public static final Color mediumPurple = rgb(0x93, 0x70, 0xD8);
 	public static final Color mediumSeaGreen = rgb(0x3C, 0xB3, 0x71);
 	public static final Color mediumSlateBlue = rgb(0x7B, 0x68, 0xEE);
 	public static final Color mediumSpringGreen = rgb(0x00, 0xFA, 0x9A);
 	public static final Color mediumTurquoise = rgb(0x48, 0xD1, 0xCC);
 	public static final Color mediumVioletRed = rgb(0xC7, 0x15, 0x85);
 	public static final Color midnightBlue = rgb(0x19, 0x19, 0x70);
 	public static final Color mintCream = rgb(0xF5, 0xFF, 0xFA);
 	public static final Color mistyRose = rgb(0xFF, 0xE4, 0xE1);
 	public static final Color moccasin = rgb(0xFF, 0xE4, 0xB5);
 	public static final Color navajoWhite = rgb(0xFF, 0xDE, 0xAD);
 	public static final Color navy = rgb(0x00, 0x00, 0x80);
 	public static final Color oldLace = rgb(0xFD, 0xF5, 0xE6);
 	public static final Color olive = rgb(0x80, 0x80, 0x00);
 	public static final Color oliveDrab = rgb(0x6B, 0x8E, 0x23);
 	public static final Color orange = rgb(0xFF, 0xA5, 0x00);
 	public static final Color orangeRed = rgb(0xFF, 0x45, 0x00);
 	public static final Color orchid = rgb(0xDA, 0x70, 0xD6);
 	public static final Color paleGoldenRod = rgb(0xEE, 0xE8, 0xAA);
 	public static final Color paleGreen = rgb(0x98, 0xFB, 0x98);
 	public static final Color paleTurquoise = rgb(0xAF, 0xEE, 0xEE);
 	public static final Color paleVioletRed = rgb(0xD8, 0x70, 0x93);
 	public static final Color papayaWhip = rgb(0xFF, 0xEF, 0xD5);
 	public static final Color peachPuff = rgb(0xFF, 0xDA, 0xB9);
 	public static final Color peru = rgb(0xCD, 0x85, 0x3F);
 	public static final Color pink = rgb(0xFF, 0xC0, 0xCB);
 	public static final Color plum = rgb(0xDD, 0xA0, 0xDD);
 	public static final Color powderBlue = rgb(0xB0, 0xE0, 0xE6);
 	public static final Color purple = rgb(0x80, 0x00, 0x80);
 	public static final Color red = rgb(0xFF, 0x00, 0x00);
 	public static final Color rosyBrown = rgb(0xBC, 0x8F, 0x8F);
 	public static final Color royalBlue = rgb(0x41, 0x69, 0xE1);
 	public static final Color saddleBrown = rgb(0x8B, 0x45, 0x13);
 	public static final Color salmon = rgb(0xFA, 0x80, 0x72);
 	public static final Color sandyBrown = rgb(0xF4, 0xA4, 0x60);
 	public static final Color seaGreen = rgb(0x2E, 0x8B, 0x57);
 	public static final Color seaShell = rgb(0xFF, 0xF5, 0xEE);
 	public static final Color sienna = rgb(0xA0, 0x52, 0x2D);
 	public static final Color silver = rgb(0xC0, 0xC0, 0xC0);
 	public static final Color skyBlue = rgb(0x87, 0xCE, 0xEB);
 	public static final Color slateBlue = rgb(0x6A, 0x5A, 0xCD);
 	public static final Color slateGray = rgb(0x70, 0x80, 0x90);
 	public static final Color snow = rgb(0xFF, 0xFA, 0xFA);
 	public static final Color springGreen = rgb(0x00, 0xFF, 0x7F);
 	public static final Color steelBlue = rgb(0x46, 0x82, 0xB4);
 	public static final Color tan = rgb(0xD2, 0xB4, 0x8C);
 	public static final Color teal = rgb(0x00, 0x80, 0x80);
 	public static final Color thistle = rgb(0xD8, 0xBF, 0xD8);
 	public static final Color tomato = rgb(0xFF, 0x63, 0x47);
 	public static final Color turquoise = rgb(0x40, 0xE0, 0xD0);
 	public static final Color violet = rgb(0xEE, 0x82, 0xEE);
 	public static final Color wheat = rgb(0xF5, 0xDE, 0xB3);
 	public static final Color white = rgb(0xFF, 0xFF, 0xFF);
 	public static final Color whiteSmoke = rgb(0xF5, 0xF5, 0xF5);
 	public static final Color yellow = rgb(0xFF, 0xFF, 0x00);
 	public static final Color yellowGreen = rgb(0x9A, 0xCD, 0x32);
 
 
 	//~ Constructors ..........................................................
 
 	// ----------------------------------------------------------
 	private Color(int rawColor)
 	{
 		this.rawColor = rawColor;
 	}
 
 
 	//~ Methods ...............................................................
 
 	// ----------------------------------------------------------
 	public static Color gray(int gray)
 	{
 		return fromRawColor(android.graphics.Color.rgb(gray, gray, gray));
 	}
 
 
 	// ----------------------------------------------------------
 	public static Color gray(int gray, int alpha)
 	{
 		return fromRawColor(android.graphics.Color.argb(
 				alpha, gray, gray, gray));
 	}
 
 
 	// ----------------------------------------------------------
 	public static Color rgb(int red, int green, int blue)
 	{
 		return fromRawColor(android.graphics.Color.rgb(red, green, blue));
 	}
 
 
 	// ----------------------------------------------------------
 	public static Color rgb(int red, int green, int blue, int alpha)
 	{
 		return fromRawColor(android.graphics.Color.argb(
 				alpha, red, green, blue));
 	}
 
 
 	// ----------------------------------------------------------
 	public static Color hsb(float hue, float saturation, float brightness)
 	{
 		return fromRawColor(android.graphics.Color.HSVToColor(
 				new float[] { hue, saturation, brightness }));
 	}
 
 
 	// ----------------------------------------------------------
 	public static Color hsb(float hue, float saturation, float brightness,
 			int alpha)
 	{
 		return fromRawColor(android.graphics.Color.HSVToColor(alpha,
 				new float[] { hue, saturation, brightness }));
 	}
 
 
 	// ----------------------------------------------------------
 	public static Color fromRawColor(int rawColor)
 	{
 		return new Color(rawColor);
 	}
 
 
     // ----------------------------------------------------------
     public static Color getRandomColor()
     {
         Random gen = Random.generator();
         return fromRawColor(android.graphics.Color.rgb(
             gen.nextInt(256), gen.nextInt(256), gen.nextInt(256)));
     }
 
 
 	// ----------------------------------------------------------
 	public int red()
 	{
 		return android.graphics.Color.red(rawColor);
 	}
 
 
 	// ----------------------------------------------------------
 	public int green()
 	{
 		return android.graphics.Color.green(rawColor);
 	}
 
 
 	// ----------------------------------------------------------
 	public int blue()
 	{
 		return android.graphics.Color.blue(rawColor);
 	}
 
 
 	// ----------------------------------------------------------
 	public int alpha()
 	{
 		return android.graphics.Color.alpha(rawColor);
 	}
 
 
 	// ----------------------------------------------------------
 	public float hue()
 	{
 		computeHSVIfNecessary();
 		return hsv[0];
 	}
 
 
 	// ----------------------------------------------------------
 	public float saturation()
 	{
 		computeHSVIfNecessary();
 		return hsv[1];
 	}
 
 
 	// ----------------------------------------------------------
 	public float brightness()
 	{
 		computeHSVIfNecessary();
 		return hsv[2];
 	}
 
 
 	// ----------------------------------------------------------
 	public Color lighter()
 	{
 	    int r = red();
 	    int g = green();
 	    int b = blue();
 	    int alpha = alpha();
 
 	    int i = (int) (1.0 / (1.0 - FACTOR));
 
 	    if (r == 0 && g == 0 && b == 0)
 	    {
 	        return rgb(i, i, i, alpha);
 	    }
 
 	    if (r > 0 && r < i) r = i;
 	    if (g > 0 && g < i) g = i;
 	    if (b > 0 && b < i) b = i;
 
 	    return rgb(Math.min((int) (r / FACTOR), 255),
 	    		   Math.min((int) (g / FACTOR), 255),
 	    		   Math.min((int) (b / FACTOR), 255),
 	               alpha);
 	}
 
 
 	// ----------------------------------------------------------
 	public Color darker()
 	{
 		return rgb(Math.max((int) (red() * FACTOR), 0),
 				   Math.max((int) (green() * FACTOR), 0),
 				   Math.max((int) (blue() * FACTOR), 0),
 				   alpha());
 	}
 
 
 	// ----------------------------------------------------------
 	public Color withAlpha(int alpha)
 	{
 		return rgb(red(), green(), blue(), alpha);
 	}
 
 
 	// ----------------------------------------------------------
 	public int toRawColor()
 	{
 		return rawColor;
 	}
 
 
 	// ----------------------------------------------------------
 	@Override
 	public boolean equals(Object other)
 	{
 		if (other instanceof Color)
 		{
 			return (rawColor == ((Color) other).rawColor);
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 
 	// ----------------------------------------------------------
 	@Override
 	public int hashCode()
 	{
 		return rawColor;
 	}
 
 
 	// ----------------------------------------------------------
 	@Override
 	public String toString()
 	{
 		StringBuffer buffer = new StringBuffer();
 
 		buffer.append("Color(");
		buffer.append(android.graphics.Color.red(rawColor));
 		buffer.append(", ");
		buffer.append(android.graphics.Color.green(rawColor));
 		buffer.append(", ");
		buffer.append(android.graphics.Color.blue(rawColor));
        buffer.append(", ");
        buffer.append(android.graphics.Color.alpha(rawColor));
 		buffer.append(")");
 
 		return buffer.toString();
 	}
 
 
 	// ----------------------------------------------------------
 	private void computeHSVIfNecessary()
 	{
 		if (hsv == null)
 		{
 			hsv = new float[3];
 			android.graphics.Color.colorToHSV(rawColor, hsv);
 		}
 	}
 }
