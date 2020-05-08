 package sofia.internal;
 
 import java.io.InputStream;
 import java.lang.ref.WeakReference;
 import java.util.Map;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.DisplayMetrics;
 
 /**
  * <p>
  * Since we want to distribute Sofia as a single JAR (or a set of JARs), we
  * cannot make use of the standard Android resource structure that regular
  * APKs use, because all of the resources would have to be copied into the
  * application projects.
  * </p><p>
  * Instead, for things like images, we store them embedded in the JARs, and
  * this class provides a better interface for accessing them.
  * </p>
  *
  * @author Tony Allevato
  * @author  Last changed by $Author: edwards $
 * @version $Date: 2012/08/21 14:15 $
  */
 public class JarResources
 {
 	//~ Methods ...............................................................
 
     // ----------------------------------------------------------
 	/**
      * Get an image resource by name, taking the current device's DPI into
      * account.  The image may be a traditional Android resource, in which
      * case the normal resource mechanism is used to look it up, or it may
      * be stored in one of the packages listed.  Alternatively, the
      * image may be stored in the application/context package. The package(s)
      * specified should have a subpackage named "images", with one or more of
      * its own subpackages that match the DPI identifiers used in resources:
      * "ldpi", "mdpi", "hdpi", and/or "xhdpi". Image files are held in these
      * dpi-based subpackages.  The "images" subpackage itself will also be
      * searched, with images it contains treated at the same DPI as the device.
 	 *
 	 * @param context The context for determining the display resolution,
 	 *                and also the application's package, if the image isn't
 	 *                found in any of the specified package(s).
 	 * @param name    The name of the image file, including its extension.
      * @param packageNames The packages where the images are located
      *                (can be omitted, to only search the application package).
 	 * @return A {@code Bitmap} containing the image, or null if no
 	 *     image could be found.
 	 */
 	public static Bitmap getBitmap(
 	    Context context, String name, String ... packageNames)
 	{
 	    return getBitmap(context, name, true, true, packageNames);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Get an image resource by name, taking the current device's DPI into
      * account.  The image may be a traditional Android resource, in which
      * case the normal resource mechanism is used to look it up, or it may
      * be stored in one of the packages listed.  Alternatively, the
      * image may be stored in the application/context package. The package(s)
      * specified should have a subpackage named "images", with one or more of
      * its own subpackages that match the DPI identifiers used in resources:
      * "ldpi", "mdpi", "hdpi", and/or "xhdpi". Image files are held in these
      * dpi-based subpackages.  The "images" subpackage itself will also be
      * searched, with images it contains treated at the same DPI as the device.
      *
      * @param context The context for determining the display resolution,
      *                and also the application's package, if needed.
      * @param name    The name of the image file, optionally including its
      *                extension.
      * @param searchAppPkg If true, and no image is found with respect to
      *                the specified packages (if any), the search will
      *                also look in the application package determined by the
      *                context.  If false, the application package will not
      *                be searched.
      * @param scaleForDpi If true, the loaded image will be automatically
      *                upscaled or downscaled for the current device display
      *                density by the BitmapFactory.  If false, the image will
      *                be loaded at its stored resolution regardless of the
      *                current display density.
      * @param packageNames The packages where the images are located
      *                (can be omitted, to only search the application package).
      * @return A {@code Bitmap} containing the image, or null if no
      *     image could be found.
      */
     public static Bitmap getBitmap(Context context, String name,
         boolean searchAppPkg, boolean scaleForDpi, String ... packageNames)
     {
         Bitmap result = getBitmapFromResource(context, name, scaleForDpi);
         if (result != null)
         {
             return result;
         }
 
         for (String pkgName : packageNames)
         {
             result = getBitmapFromClasspath(
                 context, name, pkgName, scaleForDpi);
             if (result != null)
             {
                 return result;
             }
         }
 
         if (searchAppPkg)
         {
             result = getBitmapFromClasspath(
                 context, name, context.getPackageName(), scaleForDpi);
         }
 
         return result;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Get an image resource by name, taking the current device's DPI into
      * account.  The image must be a traditional Android resource, but it
      * will be looked up by name, instead of by id.
      *
      * @param context The context for determining the display resolution.
      *                If null, mdpi device resolution will be assumed.
      * @param name    The name of the image file, optionally including its
      *                extension.
      * @param scaleForDpi If true, the loaded image will be automatically
      *                upscaled or downscaled for the current device display
      *                density by the BitmapFactory.  If false, the image will
      *                be loaded at its stored resolution regardless of the
      *                current display density.
      * @return A {@code Bitmap} containing the image, or null if no
      *     image could be found.
      */
     public static Bitmap getBitmapFromResource(
         Context context, String name, boolean scaleForDpi)
     {
         // trim file extension, if present
         int pos = name.lastIndexOf('.');
         if (pos >= 0)
         {
             name = name.substring(0, pos);
         }
         System.out.println("looking for resource named " + name);
 
         // Look for cached bitmap first
         Bitmap result = null;
         {
             WeakReference<Bitmap> ref = RESOURCE_CACHE.get(name);
             if (ref != null)
             {
                 result = ref.get();
                 if (result != null)
                 {
                     System.out.println("found cached resource for " + name);
                     return result;
                 }
             }
         }
 
         BitmapFactory.Options bfo = null;
         if (!scaleForDpi)
         {
             bfo = new BitmapFactory.Options();
             bfo.inScaled = false;
         }
 
         // First, try for a resource by this name:
         int id = context.getResources().getIdentifier(
             name, "drawable", context.getPackageName());
         if (id != 0)
         {
             result = BitmapFactory.decodeResource(
                 context.getResources(), id, bfo);
         }
         if (result != null)
         {
             System.out.println("caching resource id " + id + " for " + name);
             RESOURCE_CACHE.put(name, new WeakReference<Bitmap>(result));
         }
         else
         {
             System.out.println("cannot find resource " + name);
         }
         return result;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Get an image resource by name, taking the current device's DPI into
      * account.  The should be stored in the named package, which should
      * have a subpackage named "images", with one or more of its own
      * subpackages that match the DPI identifiers used in resources:
      * "ldpi", "mdpi", "hdpi", and/or "xhdpi". Image files are held in these
      * dpi-based subpackages.  The "images" subpackage itself will also be
      * searched, with images it contains treated at the same DPI as the device.
      *
      * @param context The context for determining the display resolution.
      *                If null, mdpi device resolution will be assumed.
      * @param name    The name of the image file, optionally including its
      *                extension.
      * @param pkgName The name of the package where the images are
      *                located (i.e., the package containing "images/", not
      *                including the ".images" at the end of the package
      *                name).
      * @param scaleForDpi If true, the loaded image will be automatically
      *                upscaled or downscaled for the current device display
      *                density by the BitmapFactory.  If false, the image will
      *                be loaded at its stored resolution regardless of the
      *                current display density.
      * @return A {@code Bitmap} containing the image, or null if no
      *     image could be found.
      */
     public static Bitmap getBitmapFromClasspath(
         Context context, String name, String pkgName, boolean scaleForDpi)
     {
         if (pkgName == null)
         {
             pkgName = "";
         }
         System.out.println("looking for image named " + name + " in '"
             + pkgName + "'");
 
         // Look for cached bitmap first
         Bitmap result = null;
         Map<String, WeakReference<Bitmap>> pkgMap =
             CLASSPATH_CACHE.get(pkgName);
         if (pkgMap == null)
         {
             System.out.println("no package cache for '" + pkgName + "'");
             pkgMap = new java.util.TreeMap<String, WeakReference<Bitmap>>();
             CLASSPATH_CACHE.put(pkgName, pkgMap);
         }
         else
         {
             WeakReference<Bitmap> ref = pkgMap.get(name);
             if (ref != null)
             {
                 result = ref.get();
                 if (result != null)
                 {
                     System.out.println("found cached image for " + name
                         + " in '" + pkgName + "'");
                     return result;
                 }
             }
         }
 
         boolean hasExtension = (name.lastIndexOf('.') >= 0);
         int foundDensity = -1;    // Density of device
         int pattern = 0;          // search pattern, index in SEARCH_PATTERN
         if (context != null)
         {
             // If no resource was found ...
             DisplayMetrics metrics =
                 context.getResources().getDisplayMetrics();
 
             // Pattern is declared outside the loop because it is intended to
             // be used after the loop
             for (; pattern < CUTOFF.length; pattern++)
             {
                 if (metrics.densityDpi < CUTOFF[pattern])
                 {
                     break;
                 }
             }
             // pattern now contains the search pattern, 0-3.  If no pattern
             // was found in CUTOFF, pattern == CUTOFF.length == 3, which
             // defaults to the xhdpi pattern.
         }
         else
         {
             // Default if no metrics found is to search from highest
             // resolution to lowest, and scale image down if necessary.
             pattern = XHDPI;
         }
 
 		InputStream stream = null;
 	    // OK, now search using the specified package
 	    String base = "";
 	    if (pkgName.length() > 0)
 	    {
 	        base = pkgName.replace('.', '/') + "/";
 	    }
 	    base += "images/";
 	    ClassLoader loader = JarResources.class.getClassLoader();
 	    for (int attempt : SEARCH_PATTERN[pattern])
 	    {
 	        String dir = base + DENSITY_NAME[attempt] + "/";
 
             if (hasExtension)
             {
                 stream = loader.getResourceAsStream(dir + name);
             }
             else
             {
                 for (String extension : EXTENSIONS)
                 {
                     stream =
                         loader.getResourceAsStream(dir + name + extension);
                     if (stream != null)
                     {
                         break;
                     }
                 }
             }
 
 	        if (stream != null)
 	        {
 	            foundDensity = attempt;
 	            break;
 	        }
         }
 
 	    if (stream == null)
 	    {
 	        // If we make it here, try for the default (no density) name
            if (hasExtension)
            {
                stream = loader.getResourceAsStream(base + name);
            }
            else
            {
                for (String extension : EXTENSIONS)
                {
                    stream =
                        loader.getResourceAsStream(base + name + extension);
                    if (stream != null)
                    {
                        break;
                    }
                }
            }
 	    }
 
 		if (stream != null)
 		{
 	        BitmapFactory.Options bfo = null;
             if (foundDensity >= 0 && scaleForDpi)
             {
                 bfo = new BitmapFactory.Options();
                 bfo.inDensity = DENSITY[foundDensity];
             }
             else if (!scaleForDpi)
             {
                 bfo = new BitmapFactory.Options();
                 bfo.inScaled = false;
             }
 		    result = BitmapFactory.decodeStream(stream, null, bfo);
 		}
 		if (result != null)
 		{
             System.out.println("caching image " + name + " for package '"
                 + pkgName + "'");
 		    pkgMap.put(name, new WeakReference<Bitmap>(result));
 		}
 		else
 		{
 		    System.out.println("cannot find image " + name + " in '"
 		        + pkgName + "'");
 		}
 		return result;
 	}
 
 
     //~ Fields ................................................................
 
     // Map from ints 0-3 to corresponding density names here
     private static final String[] DENSITY_NAME = {
         "ldpi",
         "mdpi",
         "hdpi",
         "xhdpi"
     };
 
     // constants for the int codes
     private static final int LDPI  = 0;
     private static final int MDPI  = 1;
     private static final int HDPI  = 2;
     private static final int XHDPI = 3;
 
     // DPI density for each name
     private static final int[] DENSITY = {
         120,
         160,
         240,
         320
     };
 
     // See http://developer.android.com/guide/practices/screens_support.html
 	// Values based on info in Table 1 on that page.
 	private static final int[] CUTOFF = {
 	    140,    // upper limit for ldpi, which is ~120dpi
 	    200,    // upper limit for mdpi, which is ~160dpi
 	    280     // upper limit for hdpi, which is ~240dpi
 	            // 400 is upper limit for xhdpi, which is ~320dpi
 	            // don't worry about xxhigh, since xhdpi should scale well
 	};
 
 	// Search starts with the preferred resolution, and then goes high-to-low
 	// on the assumption that higher res images scale down better than
 	// attempting to scale up lower res images.
 	private static final int[][] SEARCH_PATTERN = {
 	    { LDPI, XHDPI, HDPI, MDPI },  // for ldpi screens
         { MDPI, XHDPI, HDPI, LDPI },  // for mdpi screens
         { HDPI, XHDPI, MDPI, LDPI },  // for hdpi screens
         { XHDPI, HDPI, MDPI, LDPI }   // for xhdpi screens
 	};
 
     private static final String[] EXTENSIONS = {
         ".png", ".PNG", ".gif", ".GIF", ".jpg", ".JPG", ".JPEG", ".JPEG"
     };
 
 
     // These maps use weak references so the bitmaps can be reclaimed once
     // nothing refers to them.  We don't worry about the value-less entries
     // that remain in these maps, since the expected number of keys is
     // only a handful anyway.
     private static final Map<String, WeakReference<Bitmap>> RESOURCE_CACHE =
         java.util.Collections.synchronizedMap(
             new java.util.TreeMap<String, WeakReference<Bitmap>>());
 
     private static final Map<String, Map<String, WeakReference<Bitmap>>>
         CLASSPATH_CACHE =
         java.util.Collections.synchronizedMap(
             new java.util.TreeMap<String, Map<String, WeakReference<Bitmap>>>()
             );
 }
