 /*
  * Created on Dec 31, 2004 at the Interface Ecology Lab.
  */
 package ecologylab.serialization.types.scalar;
 
 import java.lang.reflect.Field;
 
 import ecologylab.serialization.ScalarUnmarshallingContext;
 import ecologylab.serialization.TranslationContext;
 
 /**
  * Type system entry for java.awt.Color. Uses a hex string as initialization.
  * 
  * @author andruid
  */
 public class KMLColorType extends ScalarType<KMLColor>
 {
 
 	/**
 	 * This constructor should only be called once per session, through a static initializer,
 	 * typically in TypeRegistry.
 	 * <p>
 	 * To get the instance of this type object for use in translations, call
 	 * <code>TypeRegistry.get("java.awt.Color")</code>.
 	 * 
 	 */
 	public KMLColorType()
 	{
 		super(KMLColor.class);
 	}
 
 	/**
 	 * @param value
 	 *          is interpreted as hex-encoded RGB value, in the same style as HTML & CSS. A #
 	 *          character at the start is unneccesary, but acceptable.
 	 * 
 	 * @see ecologylab.serialization.types.scalar.ScalarType#getInstance(java.lang.String)
 	 */
 	@Override
 	public KMLColor getInstance(String value)
 	{
 		if (value.indexOf('#') == 0)
 			value = value.substring(1);
 
 		// we *should* be able to use int and parseInt() here, but
 		// apparently, there's a bug in the JDK, so we use long
 		// and then cast down
 		long abgr = Long.parseLong(value, 16);
 
 		long a = abgr >>> 24;
 		long r = abgr & 0xFF;
 		long g = abgr >>> 8 & 0xFF;
 		long b = abgr >>> 16 & 0xFF;
 
 		return new KMLColor(r, g, b, a);
 	}
 
 	/**
 	 * The string representation for a Field of this type
 	 */
 	@Override
 	public String toString(Field field, Object context)
 	{
 		String result = "COULDN'T CONVERT!";
 		try
 		{
 			KMLColor color = (KMLColor) field.get(context);
 			// the api says "getRGB()", but they return getARGB()!
 
 			result = marshall(color, null);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	/**
 	 * Get a String representation of the instance, using this. The default just calls the toString()
 	 * method on the instance.
 	 * 
 	 * @param color
 	 * @return
 	 */
 	@Override
 	public String marshall(KMLColor color, TranslationContext serializationContext)
 	{
 		String result;
 
 		int a = color.getAlpha();
 		int r = color.getRed();
 		int g = color.getGreen();
 		int b = color.getBlue();
 
 		result = (a < 16 ? "0" : "") + Integer.toHexString(a) + (b < 16 ? "0" : "")
 				+ Integer.toHexString(b) + (g < 16 ? "0" : "") + Integer.toHexString(g)
 				+ (r < 16 ? "0" : "") + Integer.toHexString(r);
 		return result;
 	}
 
 	@Override
 	public KMLColor getInstance(String value, String[] formatStrings,
 			ScalarUnmarshallingContext scalarUnmarshallingContext)
 	{
 		return this.getInstance(value);
 	}
 
 	@Override
 	public String getCSharptType()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getDbType()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getObjectiveCType()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}

	@Override
	public String getJavaType()
	{
		// TODO Auto-generated method stub
		return null;
	}
 }
