 package com.egeniq.utils;
 
 import java.lang.ref.SoftReference;
 import java.util.Hashtable;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Typeface;
 import android.text.TextUtils;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 
 /**
  * Loader to load custom fonts into views. The fonts are cached after initial loading, to ensure it is loaded only once in memory.
  * 
  * Fonts are searched at the following paths:
  * - assets/fonts/
  * - assets/shared/fonts/
  * 
  * @author zcan Kaymak
  */
 public class FontLoader {
     public static final String TAG = FontLoader.class.getName();
     public static final boolean DEBUG = false;
 
     /**
      * Cache to have each font only load once in memory
      */
     private static final Hashtable<String, SoftReference<Typeface>> _fontCache = new Hashtable<String, SoftReference<Typeface>>();
 
     /**
      * Set a custom font for this view. Font is defined in layout-xml (or style).
      * 
      * @param view         The view
      * @param context      The context
      * @param attrs        The default attributeset
      * @param attributeSet Declarable style for this font
      * @param font         Fontname set from layout or style
      * @param style        Style, set in textStyle from layout or style
      */
     public static void setCustomFont(View view, Context context, AttributeSet attrs, int[] attributeSet, int font, int style) {
         TypedArray styledAttrs = context.obtainStyledAttributes(attrs, attributeSet);
         String customFont = styledAttrs.getString(font);
         setFont(view, context, customFont, style);
         styledAttrs.recycle();
     }
 
     /**
      * Set the Font.
      * 
      * @param view    The view
      * @param context The context
      * @param font    The font
      * @param style   The style
      * 
      * @return true if font is set, else false
      */
     private static boolean setFont(View view, Context context, String font, int style) {
         if (TextUtils.isEmpty(font)) {
             return false;
         }
 
         String name = "";
         switch (style) {
             case Typeface.BOLD:
                 name = String.format("%s-Bold.ttf", font);
                 break;
             case Typeface.BOLD_ITALIC:
                 name = String.format("%s-BoldItalic.ttf", font);
                 break;
             case Typeface.ITALIC:
                 name = String.format("%s-Italic.ttf", font);
                 break;
             default:
                 name = String.format("%s-Regular.ttf", font);
                 break;
         }
 
        Typeface typeface = getFont(context, name);
         if (typeface == null) {
             if (DEBUG) {
                 Log.e(TAG, "Could not get typeface: " + name + ". Retrying with regular style.");
             }
             
             typeface = getFont(context, String.format("%s-Regular.ttf", font));
         }
         
         if (typeface == null) {
             switch (style) {
                 case Typeface.BOLD:
                     name = String.format("%s-Bold.otf", font);
                     break;
                 case Typeface.BOLD_ITALIC:
                     name = String.format("%s-BoldItalic.otf", font);
                     break;
                 case Typeface.ITALIC:
                     name = String.format("%s-Italic.otf", font);
                     break;
                 default:
                     name = String.format("%s-Regular.otf", font);
                     break;
             }
         }
        
         if (typeface == null) {
             if (DEBUG) {
                 Log.e(TAG, "Could not get typeface: " + name + ". Retrying with regular style.");
             }
             
             typeface = getFont(context, String.format("%s-Regular.otf", font));
         }
         
         if (typeface == null) {
             if (DEBUG) {
                 Log.e(TAG, "Could not get typeface: " + name + ". Retrying without applying style and presuming a ttf font.");
             }
             
             typeface = getFont(context, String.format("%s.ttf", font));
         }
 
         if (typeface == null) {
             if (DEBUG) {
                 Log.e(TAG, "Could not get typeface: " + name + ". Retrying without applying style and presuming a otf font.");
             }
             
             typeface = getFont(context, String.format("%s.otf", font));
         }
         
         if (typeface == null) {
             if (DEBUG) {
                 Log.e(TAG, "Could not get typeface: " + font);
             }
                 
             return false;
         }
         
         if (typeface != null && view instanceof TextView) { // covers TextView and Button
             ((TextView)view).setTypeface(typeface);
         } else if (DEBUG) {
             Log.e(TAG, "Don't know how to apply typeface to class: " + view.getClass());
         }
 
         return true;
     }
 
     /**
      * Get font from assets or fontcache
      * 
      * @param context The context
      * @param name    The full name for the font
      * 
      * @return Typeface
      */
     public static Typeface getFont(Context context, String name) {
         synchronized (_fontCache) {
             if (_fontCache.get(name) != null) {
                 SoftReference<Typeface> ref = _fontCache.get(name);
                 if (ref.get() != null) {
                     return ref.get();
                 }
             }
             
             Typeface typeface = null;
             
             try {
                 typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + name);
             } catch (Exception e1) {
                 try {
                     typeface = Typeface.createFromAsset(context.getAssets(), "shared/fonts/" + name);
                 } catch (Exception e2) {
                 }
             } 
             
             if (typeface != null) {
                 _fontCache.put(name, new SoftReference<Typeface>(typeface));
             }
 
             return typeface;
         }
     }
 }
