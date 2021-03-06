 /*
  * $Id$
  *
  * Copyright 2007 Sun Microsystems, Inc., 4150 Network Circle,
  * Santa Clara, California 95054, U.S.A. All rights reserved.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jdesktop.swingx.plaf;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.Shape;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Vector;
 
 import javax.swing.Icon;
 import javax.swing.UIDefaults;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 import javax.swing.plaf.BorderUIResource;
 import javax.swing.plaf.ColorUIResource;
 import javax.swing.plaf.DimensionUIResource;
 import javax.swing.plaf.FontUIResource;
 import javax.swing.plaf.IconUIResource;
 import javax.swing.plaf.InsetsUIResource;
 import javax.swing.plaf.UIResource;
 
 import org.jdesktop.swingx.painter.Painter;
 import org.jdesktop.swingx.util.Contract;
 
 /**
  * A utility class for obtaining configuration properties from the
  * {@code UIDefaults}. This class handles SwingX-specific L&F needs, such as
  * the installation of painters.
  * <p>
  * SwingX supports dynamic localization updates and the {@code UIManagerExt}
  * class enables this. The {@linkplain UIDefaults#addResourceBundle(String)}
  * allows resource bundles to be added to the {@code UIDefaults}. There is a
  * bug with the class loader that prevents user added bundles from working
  * correctly when used via Web Start. Therefore, {@code UIManagerExt} defines
  * methods to add and remove resource bundles. These are the only methods that
  * SwingX classes should use when adding resource bundles to the defaults. Since
  * {@code UIManagerExt} is maintaining the bundles, any localized {@code String}s
  * <b>must</b> be retrieved from the {@code getString} methods in this class.
  * <p>
  * The {@code getSafeXXX} methods are designed for use with
  * {@code LookAndFeelAddon}s. Any addon that attempts to obtain a property
  * defined in the defaults (available from {@code UIManager.get}) to set a
  * property that will be added to the defaults for the addon should use the
  * "safe" methods. The methods ensure that a valid value is always returned and
  * that value is a {@code UIResource}.
  * 
  * @author Karl George Schaefer
  * 
  * @see UIManager
  * @see UIDefaults
  */
 public class UIManagerExt {
     /**
      * Used to replicate the resource bundle behavior from the
      * {@code UIDefaults}.
      */
     private static class UIDefaultsExt {
         //use vector; we want synchronization
         private Vector<String> resourceBundles;
 
         /**
          * Maps from a Locale to a cached Map of the ResourceBundle. This is done
          * so as to avoid an exception being thrown when a value is asked for.
          * Access to this should be done while holding a lock on the
          * UIDefaults, eg synchronized(this).
          */
         private Map<Locale, Map<String, String>> resourceCache;
         
         UIDefaultsExt() {
             resourceCache = new HashMap<Locale, Map<String,String>>();
         }
         
         private Object getFromResourceBundle(Object key, Locale l) {
 
             if( resourceBundles == null ||
                 resourceBundles.isEmpty() ||
                 !(key instanceof String) ) {
                 return null;
             }
 
             // A null locale means use the default locale.
             if( l == null ) {
                     l = Locale.getDefault();
             }
 
             synchronized(this) {
                 return getResourceCache(l).get((String)key);
             }
         }
 
         /**
          * Returns a Map of the known resources for the given locale.
          */
         private Map<String, String> getResourceCache(Locale l) {
             Map<String, String> values = (Map<String, String>) resourceCache.get(l);
 
             if (values == null) {
                 values = new HashMap<String, String>();
                 for (int i=resourceBundles.size()-1; i >= 0; i--) {
                     String bundleName = (String)resourceBundles.get(i);
                     
                     try {
                         ResourceBundle b = ResourceBundle.
                             getBundle(bundleName, l, UIManagerExt.class.getClassLoader());
                         Enumeration<String> keys = b.getKeys();
 
                         while (keys.hasMoreElements()) {
                             String key = (String)keys.nextElement();
 
                             if (values.get(key) == null) {
                                 Object value = b.getObject(key);
 
                                 values.put(key, (String) value);
                             }
                         }
                     } catch( MissingResourceException mre ) {
                         // Keep looking
                     }
                 }
                 resourceCache.put(l, values);
             }
             return values;
         }
 
         public synchronized void addResourceBundle(String bundleName) {
             if( bundleName == null ) {
                 return;
             }
             if( resourceBundles == null ) {
                 resourceBundles = new Vector<String>(5);
             }
             if (!resourceBundles.contains(bundleName)) {
                 resourceBundles.add( bundleName );
                 resourceCache.clear();
             }
         }
         
         public synchronized void removeResourceBundle( String bundleName ) {
             if( resourceBundles != null ) {
                 resourceBundles.remove( bundleName );
             }
             resourceCache.clear();
         }
     }
     
     private static UIDefaultsExt uiDefaultsExt = new UIDefaultsExt();
     
     private UIManagerExt() {
         //does nothing
     }
     
     /**
      * Adds a resource bundle to the list of resource bundles that are searched
      * for localized values. Resource bundles are searched in the reverse order
      * they were added. In other words, the most recently added bundle is
      * searched first.
      * 
      * @param bundleName
      *                the base name of the resource bundle to be added
      * @see java.util.ResourceBundle
      * @see #removeResourceBundle
      */
     public static void addResourceBundle(String bundleName) {
         uiDefaultsExt.addResourceBundle(bundleName);
     }
     
     /**
      * Removes a resource bundle from the list of resource bundles that are
      * searched for localized defaults.
      * 
      * @param bundleName
      *                the base name of the resource bundle to be removed
      * @see java.util.ResourceBundle
      * @see #addResourceBundle
      */
     public static void removeResourceBundle(String bundleName) {
         uiDefaultsExt.removeResourceBundle(bundleName);
     }
     
     /**
      * Returns a string from the defaults. If the value for {@code key} is not a
      * {@code String}, {@code null} is returned.
      * 
      * @param key
      *                an {@code Object} specifying the string
      * @return the {@code String} object
      * @throws NullPointerException
      *                 if {@code key} is {@code null}
      */
     public static String getString(Object key) {
         return getString(key, null);
     }
     
     /**
      * Returns a string from the defaults. If the value for {@code key} is not a
      * {@code String}, {@code null} is returned.
      * 
      * @param key
      *                an {@code Object} specifying the string
      * @param l
      *                the {@code Locale} for which the painter is desired; refer
      *                to {@code UIDefaults} for details on how a {@code null}
      *                {@code Locale} is handled
      * @return the {@code String} object
      * @throws NullPointerException
      *                 if {@code key} is {@code null}
      */
     public static String getString(Object key, Locale l) {
         String value = UIManager.getString(key, l);
         
         if (value == null) {
             value = (String) uiDefaultsExt.getFromResourceBundle(key, l);
         }
         
         return value;
     }
     
     /**
      * Returns a int from the defaults. If the value for {@code key} is not a
      * {@code int}, {@code 0} is returned.
      * 
      * @param key an {@code Object} specifying the int
      * @param l the {@code Locale} for which the int is desired; refer to
      *        {@code UIDefaults} for details on how a {@code null}
      *        {@code Locale} is handled
      * @return the {@code int} object
      * @throws NullPointerException if {@code key} is {@code null}
      */
     public static int getInt(Object key, Locale l) {
         Object value = UIManager.get(key, l);
         if (value instanceof Integer) {
             return (Integer) value;
         }
         Object text = uiDefaultsExt.getFromResourceBundle(key, l);
         if (text instanceof String) {
            return Integer.decode((String) text);
         }
         return 0;
     }
     
     /**
      * Returns a shape from the defaults. If the value for {@code key} is not a
      * {@code Shape}, {@code null} is returned.
      * 
      * @param key an {@code Object} specifying the shape
      * @return the {@code Shape} object
      * @throws NullPointerException if {@code key} is {@code null}
      */
     public static Shape getShape(Object key) {
         Object value = UIManager.getDefaults().get(key);
         return (value instanceof Shape) ? (Shape) value : null;
     }
     
     /**
      * Returns a shape from the defaults that is appropriate for the given
      * locale. If the value for {@code key} is not a {@code Shape},
      * {@code null} is returned.
      * 
      * @param key
      *                an {@code Object} specifying the shape
      * @param l
      *                the {@code Locale} for which the shape is desired; refer
      *                to {@code UIDefaults} for details on how a {@code null}
      *                {@code Locale} is handled
      * @return the {@code Shape} object
      * @throws NullPointerException
      *                 if {@code key} is {@code null}
      */
     public static Shape getShape(Object key, Locale l) {
         Object value = UIManager.getDefaults().get(key, l);
         return (value instanceof Shape) ? (Shape) value : null;
     }
     
     /**
      * Returns a painter from the defaults. If the value for {@code key} is not
      * a {@code Painter}, {@code null} is returned.
      * 
      * @param key
      *                an {@code Object} specifying the painter
      * @return the {@code Painter} object
      * @throws NullPointerException
      *                 if {@code key} is {@code null}
      */
     public static Painter<?> getPainter(Object key) {
         Object value = UIManager.getDefaults().get(key);
         return (value instanceof Painter) ? (Painter<?>) value : null;
     }
     
     /**
      * Returns a painter from the defaults that is appropriate for the given
      * locale. If the value for {@code key} is not a {@code Painter},
      * {@code null} is returned.
      * 
      * @param key
      *                an {@code Object} specifying the painter
      * @param l
      *                the {@code Locale} for which the painter is desired; refer
      *                to {@code UIDefaults} for details on how a {@code null}
      *                {@code Locale} is handled
      * @return the {@code Painter} object
      * @throws NullPointerException
      *                 if {@code key} is {@code null}
      */
     public static Painter<?> getPainter(Object key, Locale l) {
         Object value = UIManager.getDefaults().get(key, l);
         return (value instanceof Painter) ? (Painter<?>) value : null;
     }
     
     /**
      * Returns a border from the defaults. If the value for {@code key} is not a
      * {@code Border}, {@code defaultBorder} is returned.
      * 
      * @param key
      *                an {@code Object} specifying the border
      * @param defaultBorder
      *                the border to return if the border specified by
      *                {@code key} does not exist
      * @return the {@code Border} object
      * @throws NullPointerException
      *                 if {@code key} or {@code defaultBorder} is {@code null}
      */
     public static Border getSafeBorder(Object key, Border defaultBorder) {
         Contract.asNotNull(defaultBorder, "defaultBorder cannot be null");
         
         Border safeBorder = UIManager.getBorder(key);
         
         if (safeBorder == null) {
             safeBorder = defaultBorder;
         }
         
         if (!(safeBorder instanceof UIResource)) {
             safeBorder = new BorderUIResource(safeBorder);
         }
         
         return safeBorder;
     }
     
     /**
      * Returns a color from the defaults. If the value for {@code key} is not a
      * {@code Color}, {@code defaultColor} is returned.
      * 
      * @param key
      *                an {@code Object} specifying the color
      * @param defaultColor
      *                the color to return if the color specified by {@code key}
      *                does not exist
      * @return the {@code Color} object
      * @throws NullPointerException
      *                 if {@code key} or {@code defaultColor} is {@code null}
      */
     public static Color getSafeColor(Object key, Color defaultColor) {
         Contract.asNotNull(defaultColor, "defaultColor cannot be null");
         
         Color safeColor = UIManager.getColor(key);
         
         if (safeColor == null) {
             safeColor = defaultColor;
         }
         
         if (!(safeColor instanceof UIResource)) {
             safeColor = new ColorUIResource(safeColor);
         }
         
         return safeColor;
     }
     
     /**
      * Returns a dimension from the defaults. If the value for {@code key} is
      * not a {@code Dimension}, {@code defaultDimension} is returned.
      * 
      * @param key
      *                an {@code Object} specifying the dimension
      * @param defaultDimension
      *                the dimension to return if the dimension specified by
      *                {@code key} does not exist
      * @return the {@code Dimension} object
      * @throws NullPointerException
      *                 if {@code key} or {@code defaultColor} is {@code null}
      */
     public static Dimension getSafeDimension(Object key, Dimension defaultDimension) {
         Contract.asNotNull(defaultDimension, "defaultDimension cannot be null");
         
         Dimension safeDimension = UIManager.getDimension(key);
         
         if (safeDimension == null) {
             safeDimension = defaultDimension;
         }
         
         if (!(safeDimension instanceof UIResource)) {
             safeDimension = new DimensionUIResource(safeDimension.width, safeDimension.height);
         }
         
         return safeDimension;
     }
     
     /**
      * Returns a font from the defaults. If the value for {@code key} is not a
      * {@code Font}, {@code defaultFont} is returned.
      * 
      * @param key
      *                an {@code Object} specifying the font
      * @param defaultFont
      *                the font to return if the font specified by {@code key}
      *                does not exist
      * @return the {@code Font} object
      * @throws NullPointerException
      *                 if {@code key} or {@code defaultFont} is {@code null}
      */
     public static Font getSafeFont(Object key, Font defaultFont) {
         Contract.asNotNull(defaultFont, "defaultFont cannot be null");
         
         Font safeFont = UIManager.getFont(key);
         
         if (safeFont == null) {
             safeFont = defaultFont;
         }
         
         if (!(safeFont instanceof UIResource)) {
             safeFont = new FontUIResource(safeFont);
         }
         
         return safeFont;
     }
     
     /**
      * Returns an icon from the defaults. If the value for {@code key} is not a
      * {@code Icon}, {@code defaultIcon} is returned.
      * 
      * @param key
      *                an {@code Object} specifying the icon
      * @param defaultIcon
      *                the icon to return if the icon specified by {@code key}
      *                does not exist
      * @return the {@code Icon} object
      * @throws NullPointerException
      *                 if {@code key} or {@code defaultIcon} is {@code null}
      */
     public static Icon getSafeIcon(Object key, Icon defaultIcon) {
         Contract.asNotNull(defaultIcon, "defaultIcon cannot be null");
         
         Icon safeIcon = UIManager.getIcon(key);
         
         if (safeIcon == null) {
             safeIcon = defaultIcon;
         }
         
         if (!(safeIcon instanceof UIResource)) {
             safeIcon = new IconUIResource(safeIcon);
         }
         
         return safeIcon;
     }
     
     /**
      * Returns an insets from the defaults. If the value for {@code key} is not
      * a {@code Insets}, {@code defaultInsets} is returned.
      * 
      * @param key
      *                an {@code Object} specifying the insets
      * @param defaultInsets
      *                the insets to return if the insets specified by
      *                {@code key} does not exist
      * @return the {@code Insets} object
      * @throws NullPointerException
      *                 if {@code key} or {@code defaultInsets} is {@code null}
      */
     public static Insets getSafeInsets(Object key, Insets defaultInsets) {
         Contract.asNotNull(defaultInsets, "defaultInsets cannot be null");
         
         Insets safeInsets = UIManager.getInsets(key);
         
         if (safeInsets == null) {
             safeInsets = defaultInsets;
         }
         
         if (!(safeInsets instanceof UIResource)) {
             safeInsets = new InsetsUIResource(safeInsets.top, safeInsets.left,
                     safeInsets.bottom, safeInsets.right);
         }
         
         return safeInsets;
     }
 }
