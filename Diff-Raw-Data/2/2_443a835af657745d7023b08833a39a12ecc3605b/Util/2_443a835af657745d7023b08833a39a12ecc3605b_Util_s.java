 package at.yawk.fimfiction.json;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 
 import com.google.gson.JsonObject;
 
 class Util {
     private Util() {}
     
     /**
      * If the given {@link JsonObject} has a member with the given name, that
      * member's value as <code>long</code> is returned, otherwise <code>0</code>
      * .
      */
     static long getLong(final JsonObject object, final String name) {
         return object.has(name) ? object.get(name).getAsLong() : 0;
     }
     
     /**
      * If the given {@link JsonObject} has a member with the given name, that
      * member's value as <code>int</code> is returned, otherwise <code>0</code>.
      * 
      * @see #getInt(JsonObject, String, int)
      */
     static int getInt(final JsonObject object, final String name) {
         return getInt(object, name, 0);
     }
     
     /**
      * If the given {@link JsonObject} has a member with the given name, that
      * member's value as <code>int</code> is returned, otherwise the given
      * default value.
      */
     static int getInt(final JsonObject object, final String name, final int defaultValue) {
         return object.has(name) ? object.get(name).getAsInt() : defaultValue;
     }
     
     /**
      * If the given {@link JsonObject} has a member with the given name, that
      * member's value as <code>short</code> is returned, otherwise
      * <code>0</code>.
      */
     static short getShort(final JsonObject object, final String name) {
         return object.has(name) ? object.get(name).getAsShort() : 0;
     }
     
     /**
      * If the given {@link JsonObject} has a member with the given name, that
      * member's value as <code>byte</code> is returned, otherwise <code>0</code>
      * .
      */
     static byte getByte(final JsonObject object, final String name) {
         return object.has(name) ? object.get(name).getAsByte() : 0;
     }
     
     /**
      * If the given {@link JsonObject} has a member with the given name, that
      * member's value as <code>boolean</code> is returned, otherwise
      * <code>false</code>.
      */
     static boolean getBoolean(final JsonObject object, final String name) {
         return object.has(name) ? object.get(name).getAsBoolean() : false;
     }
     
     /**
      * If the given {@link JsonObject} has a member with the given name, that
      * member's value as <code>double</code> is returned, otherwise
      * <code>0</code>.
      */
     static double getDouble(final JsonObject object, final String name) {
         return object.has(name) ? object.get(name).getAsDouble() : 0;
     }
     
     /**
      * If the given {@link JsonObject} has a member with the given name, that
      * member's value as <code>float</code> is returned, otherwise
      * <code>0</code>.
      */
     static float getFloat(final JsonObject object, final String name) {
         return object.has(name) ? object.get(name).getAsFloat() : 0;
     }
     
     /**
      * If the given {@link JsonObject} has a member with the given name, that
      * member's value as a {@link String} is returned, otherwise
      * <code>null</code>.
      */
     static String getString(final JsonObject object, final String name) {
         return object.has(name) ? StringEscapeUtils.unescapeHtml4(object.get(name).getAsString()) : null;
     }
     
     /**
      * If the given {@link JsonObject} has a member with the given name, that
      * member's value as a {@link String} is returned, otherwise
      * <code>null</code>.
      */
     static URL getUrl(final JsonObject object, final String name, final String protocolPrefixIfNeeded) {
         final String url = getString(object, name);
         if (url != null) {
            final String result = url.startsWith("//") ? protocolPrefixIfNeeded : url;
             try {
                 return new URL(result);
             } catch (final MalformedURLException e) {}
         }
         return null;
     }
 }
