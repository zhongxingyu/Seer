 /*
  * UtilityFunctions.java: GameTable is in the Public Domain.
  */
 
 
 package com.galactanet.gametable.util;
 
 import java.awt.*;
 import java.io.*;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.*;
 import java.util.List;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import com.galactanet.gametable.GametableFrame;
 import com.galactanet.gametable.Log;
 
 
 
 /**
  * A class full of various and sundry static utility functions.
  * 
  * @author sephalon
  */
 public class UtilityFunctions
 {
     public final static int            CANCEL                   = -1;
     private static final Map           ENTITY_NAME_MAP          = getEncodingMap();
 
     /**
      * Simple image cache.
      */
     private static Map                 g_imageCache             = new HashMap();
     static private String              lastDir                  = null;
     public static final char           LOCAL_SEPARATOR          = File.separatorChar;
 
     public final static int            NO                       = 0;
 
     /**
      * The PNG signature to verify PNG data with.
      */
     private static final byte[]        PNG_SIGNATURE            = {
         (byte)(137 & 0xFF), 80, 78, 71, 13, 10, 26, 10
                                                                 };
 
     private static final Random        RANDOM                   = getRandomInstance();
 
     public static final RenderingHints STANDARD_RENDERING_HINTS = getRenderingHints();
 
     // constants
     public static final char           UNIVERSAL_SEPARATOR      = '/';
 
     public final static int            YES                      = 1;
 
     /**
      * @param line String to break into words.
      * @return An array of words found in the string.
      */
     public static String[] breakIntoWords(final String line)
     {
         boolean bDone = false;
         final List words = new ArrayList();
         int start = 0;
         int end;
         while (!bDone)
         {
             end = line.indexOf(" ", start);
             String newWord;
             if (end == -1)
             {
                 bDone = true;
                 newWord = line.substring(start);
                 words.add(newWord);
             }
             else
             {
                 newWord = line.substring(start, end);
                 start = end + 1;
                 words.add(newWord);
             }
         }
 
         if (words.size() == 0)
         {
             return null;
         }
 
         final String[] ret = new String[words.size()];
         for (int i = 0; i < ret.length; i++)
         {
             ret[i] = (String)words.get(i);
         }
 
         return ret;
     }
 
     /**
      * @param source Component to get coordinates relative from.
      * @param destination Component to get coordinates relative to.
      * @param sourcePoint Source-relative coordinates to convert.
      * @return destination-relative coordinates of the given source-relative coordinates.
      */
     public static Point convertCoordinates(final Component source, final Component destination, final Point sourcePoint)
     {
         return getComponentCoordinates(destination, getScreenCoordinates(source, sourcePoint));
     }
 
     public static Image createDrawableImage(final int width, final int height)
     {
         return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
             .createCompatibleImage(width, height, Transparency.TRANSLUCENT);
     }
 
     public static File doFileOpenDialog(final String title, final String extension, final boolean filterFiles)
     {
         final JFileChooser chooser = new JFileChooser();
 
         prepareFileDialog(chooser, title, filterFiles, extension);
 
         final int returnVal = chooser.showOpenDialog(null);
         if (returnVal == JFileChooser.APPROVE_OPTION)
         {
             lastDir = chooser.getSelectedFile().getParent();
             return chooser.getSelectedFile();
         }
 
         return null;
     }
 
     public static File doFileSaveDialog(final String title, final String extension, final boolean filterFiles)
     {
         final JFileChooser chooser = new JFileChooser();
 
         prepareFileDialog(chooser, title, filterFiles, extension);
 
         final int returnVal = chooser.showSaveDialog(null);
         if (returnVal == JFileChooser.APPROVE_OPTION)
         {
             //The selected file's name
             String f = chooser.getSelectedFile().getName();
             //The selected file's path
             lastDir = chooser.getSelectedFile().getParent();
 
             //Check if file extension is .grm
             String ext = "";
             String filename = "";
 
             //Get the selected file's extension
             int i = f.lastIndexOf(".");
             if (i > 0 && i < f.length() - 1)
             {
                ext = f.substring(i + 1).toLowerCase();
             }
            if (ext.equals("grm"))      //if extension is .grm, return the selected file.
            {
                 return chooser.getSelectedFile();
             }
            else if (ext.equals(""))    //if extension is missing, add .grm
            {
                filename = f + ".grm";
            }
            else                        //if any other extension, change to .grm 
            {
                filename = f.replaceAll("." + ext, ".grm");
            }
            //If it hasn't already returned, then the extension was not .grm
            //Create new file using the selected path and file name with .grm extension
             File saveFile = new File(lastDir + "/" + filename);
             //return the file with proper extension
             return saveFile;
         }
         //Only get here if action was canceled, so return null to cancel save
         return null;
     }
 
     public static void getCurrentDir (String args[])
     {
         File dir1 = new File (".");
         File dir2 = new File ("..");
         try
         {
             System.out.println ("Current dir : " + dir1.getCanonicalPath());
             System.out.println ("Parent  dir : " + dir2.getCanonicalPath());
         }
         catch(Exception e) 
         {
             e.printStackTrace();
         }
     }
     
     public static String emitUserLink(final String name)
     {
         return emitUserLink(name, name);
     }
 
     public static String emitUserLink(final String name, final String text)
     {
         try
         {
             final URL url = new URL("gtuser", urlEncode(name), "/");
 
             return "<a class=\"user\" href=\"" + url + "\">" + text + "</a>";
         }
         catch (final MalformedURLException e)
         {
             Log.log(Log.SYS, e);
             return "<a class=\"user\">" + text + "</a>";
         }
     }
 
     public static String getBodyContent(final String html)
     {
         final int end = html.lastIndexOf("</body>");
         int start = html.indexOf("<body") + "<body".length();
         start = html.indexOf('>', start) + 1;
         return html.substring(start, end).trim();
     }
 
     /**
      * Gets an image, caching it if possible.
      * 
      * @param name Name of image to get.
      * @return Image retrieved, or null.
      */
     public static Image getCachedImage(final String name)
     {
         Image image = (Image)g_imageCache.get(name);
         if (image == null)
         {
             image = getImage(name);
             if (image == null)
             {
                 return null;
             }
             g_imageCache.put(name, image);
         }
         return image;
     }
 
     /**
      * Gets the canonical/absolute file of the given file.
      * 
      * @param file File to canonicalize.
      * @return Canonicalized file.
      */
     public static File getCanonicalFile(final File file)
     {
         try
         {
             return file.getCanonicalFile();
         }
         catch (final IOException ioe)
         {
             return file.getAbsoluteFile();
         }
     }
 
     /**
      * @param component Component to get coordinates relative to.
      * @param screenPoint Screen-relative coordinates to convert.
      * @return Component-relative coordinates of the given screen coordinates.
      */
     public static Point getComponentCoordinates(final Component component, final Point screenPoint)
     {
         final Point screenPos = getScreenPosition(component);
         return new Point(screenPoint.x - screenPos.x, screenPoint.y - screenPos.y);
     }
 
     private static Map getEncodingMap()
     {
         final Map retVal = new HashMap();
         retVal.put(new Character('\''), "apos");
         retVal.put(new Character('\"'), "quot");
         retVal.put(new Character('<'), "lt");
         retVal.put(new Character('>'), "gt");
         retVal.put(new Character('&'), "amp");
 
         return Collections.unmodifiableMap(retVal);
     }
 
     public static Image getImage(final String name)
     {
         Image img = getImageFromJar(name);
         if (img == null)
         {
             // couldn't find it in the jar. Try the local directory
             img = loadAndWait(GametableFrame.getGametableFrame().getGametableCanvas(), name);
         }
 
         return img;
     }
 
     private static Image getImageFromJar(final String name)
     {
         final URL imageUrl = GametableFrame.getGametableFrame().getGametableCanvas().getClass().getResource("/" + name);
         if (imageUrl == null)
         {
             return null;
         }
 
         Image image = null;
         try
         {
             image = Toolkit.getDefaultToolkit().createImage(imageUrl);
             if (image == null)
             {
                 return null;
             }
 
             final MediaTracker tracker = new MediaTracker(GametableFrame.getGametableFrame().getGametableCanvas());
             tracker.addImage(image, 0);
             tracker.waitForAll();
         }
         catch (final Exception e)
         {
             Log.log(Log.SYS, e);
             return null;
         }
 
         if ((image.getWidth(null) < 1) || (image.getHeight(null) < 1))
         {
             // Log.log(Log.SYS, "JAR invalid file? " + name + " " + image.getWidth(null) + " x " +
             // image.getHeight(null));
             return null;
         }
 
         return image;
     }
 
     public static String getLine(final DataInputStream in)
     {
         try
         {
             boolean bFoundContent = false;
             final StringBuffer buffer = new StringBuffer();
             int count = 0;
             while (true)
             {
                 // ready an empty string. If we have tl leave early, we'll return an empty string
 
                 final char ch = (char)(in.readByte());
                 if ((ch == '\r') || (ch == '\n'))
                 {
                     // if it's just a blank line, then press on
                     // but if we've already had valid characters,
                     // then don't
                     if (bFoundContent)
                     {
                         // it's the end of the line!
                         return buffer.toString();
                     }
                 }
                 else
                 {
                     // it's a non-CR character.
                     bFoundContent = true;
                     buffer.append(ch);
                 }
 
                 count++;
                 if (count > 300)
                 {
                     return buffer.toString();
                 }
             }
         }
         catch (final Exception e)
         {
             Log.log(Log.SYS, e);
             return null;
         }
     }
 
     /**
      * Converts the filename to use File.seperatorChar.
      * 
      * @param path Path to canonicalize.
      * @return Canonicalized Path.
      */
     public static String getLocalPath(final String path)
     {
         final StringBuffer buffer = new StringBuffer();
         for (int i = 0, size = path.length(); i < size; ++i)
         {
             final char c = path.charAt(i);
             if ((c == '/') || (c == '\\'))
             {
                 buffer.append(LOCAL_SEPARATOR);
             }
             else
             {
                 buffer.append(c);
             }
         }
 
         return buffer.toString();
     }
 
     public static int getRandom(final int max)
     {
         return RANDOM.nextInt(max);
     }
 
     /**
      * @return An instance of Random to use for all RNG.
      */
     private static Random getRandomInstance()
     {
         // SHA1PRNG
         Random rand;
         try
         {
             rand = SecureRandom.getInstance("SHA1PRNG");
         }
         catch (final NoSuchAlgorithmException e)
         {
             Log.log(Log.SYS, e);
             rand = new Random();
         }
         rand.setSeed(System.currentTimeMillis());
         return rand;
     }
 
     /**
      * Gets the child path relative to the parent path.
      * 
      * @param parent Parent path.
      * @param child Child path.
      * @return The relative path.
      */
     public static String getRelativePath(final File parent, final File child)
     {
         String parentPath = getLocalPath(getCanonicalFile(parent).getPath());
         if (parentPath.charAt(parentPath.length() - 1) != LOCAL_SEPARATOR)
         {
             parentPath = parentPath + LOCAL_SEPARATOR;
         }
         final String childPath = getLocalPath(getCanonicalFile(child).getPath());
 
         return new String(childPath.substring(parentPath.length()));
     }
 
     /**
      * @return The standard set of rendering hits for the app.
      */
     private static RenderingHints getRenderingHints()
     {
         final RenderingHints retVal = new RenderingHints(null);
 
         retVal.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
         retVal.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
         retVal.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
         retVal.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
         retVal.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
         retVal.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
         retVal.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
         retVal.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
         return retVal;
     }
 
     public static Image getScaledInstance(final Image image, final float scale)
     {
         if (image == null)
         {
             return null;
         }
 
         waitForImage(image);
 
         final int width = Math.round(image.getWidth(null) * scale);
         final int height = Math.round(image.getHeight(null) * scale);
 
         return getScaledInstance(image, width, height);
     }
 
     public static Image getScaledInstance(final Image image, final int width, final int height)
     {
         if (image == null)
         {
             return null;
         }
 
         // TODO: Option for SMOOTH vs FAST?
         Image scaledImage;
         scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
         if (scaledImage == null)
         {
             return null;
         }
 
         waitForImage(scaledImage);
 
         return scaledImage;
     }
 
     /**
      * @param component Component that componentPoint is relative to.
      * @param componentPoint Point to convert to screen coordinates, relative to the given component.
      * @return The screen-relative coordinates of componentPoint.
      */
     public static Point getScreenCoordinates(final Component component, final Point componentPoint)
     {
         final Point screenPos = getScreenPosition(component);
         return new Point(componentPoint.x + screenPos.x, componentPoint.y + screenPos.y);
     }
 
     /**
      * @param component Component to get screen coordinates of.
      * @return The absolute screen coordinates of this component.
      */
     public static Point getScreenPosition(final Component component)
     {
         final Point retVal = new Point(component.getX(), component.getY());
 
         final Container container = component.getParent();
         if (container != null)
         {
             final Point parentPos = getScreenPosition(container);
             return new Point(retVal.x + parentPos.x, retVal.y + parentPos.y);
         }
         return retVal;
     }
 
     /**
      * Converts the filename to use UNIVERSAL_SEPERATOR.
      * 
      * @param path Path to canonicalize.
      * @return Canonicalized Path.
      */
     public static String getUniversalPath(final String path)
     {
         final StringBuffer buffer = new StringBuffer();
         for (int i = 0, size = path.length(); i < size; ++i)
         {
             final char c = path.charAt(i);
             if ((c == '/') || (c == '\\'))
             {
                 buffer.append(UNIVERSAL_SEPARATOR);
             }
             else
             {
                 buffer.append(c);
             }
         }
 
         return buffer.toString();
     }
 
     /**
      * Checks to see whether one file is an ancestor of another.
      * 
      * @param ancestor The potential ancestor File.
      * @param child The child file.
      * @return True if ancestor is an ancestor of child.
      */
     public static boolean isAncestorFile(final File ancestor, final File child)
     {
         final File parent = child.getParentFile();
         if (parent == null)
         {
             return false;
         }
 
         if (parent.equals(ancestor))
         {
             return true;
         }
 
         final boolean b = isAncestorFile(ancestor, parent);
         return b;
     }
 
     /**
      * Checks the given binary date to see if it is a valid PNG file. It does this by checking the PNG signature.
      * 
      * @param data binary data to check
      * @return true if the binary data is a valid PNG file, false otherwise.
      */
     public static boolean isPngData(final byte[] data)
     {
         for (int i = 0; i < PNG_SIGNATURE.length; i++)
         {
             if (data[i] != PNG_SIGNATURE[i])
             {
                 return false;
             }
         }
 
         return true;
     }
 
     public static void launchBrowser(final String url)
     {
         final String osName = System.getProperty("os.name");
         try
         {
             if (osName.startsWith("Mac OS"))
             {
                 final Class fileMgr = Class.forName("com.apple.eio.FileManager");
                 final Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {
                     String.class
                 });
                 openURL.invoke(null, new Object[] {
                     url
                 });
             }
             else if (osName.startsWith("Windows"))
             {
                 Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
             }
             else
             {
                 // assume Unix or Linux
                 final String[] browsers = {
                     "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"
                 };
                 String browser = null;
                 for (int count = 0; (count < browsers.length) && (browser == null); count++)
                 {
                     if (Runtime.getRuntime().exec(new String[] {
                         "which", browsers[count]
                     }).waitFor() == 0)
                     {
                         browser = browsers[count];
                     }
                 }
 
                 if (browser == null)
                 {
                     throw new Exception("Could not find web browser");
                 }
 
                 Runtime.getRuntime().exec(new String[] {
                     browser, url
                 });
             }
         }
         catch (final Exception e)
         {
             Log.log(Log.NET, e);
         }
     }
 
     private static Image loadAndWait(final Component component, final String name)
     {
         MediaTracker tracker = new MediaTracker(component);
         final Image image = loadImage(name, tracker);
 
         if (image == null)
         {
             return null;
         }
 
         try
         {
             tracker.waitForAll(); // ignore exceptions
         }
         catch (final Exception e)
         {
             Log.log(Log.SYS, e);
             return null;
         }
 
         if ((image.getWidth(null) < 1) || (image.getHeight(null) < 1))
         {
             // Log.log(Log.SYS, "FS invalid file? " + name + " " + image.getWidth(null) + " x " +
             // image.getHeight(null));
             return null;
         }
 
         tracker = null;
         return image;
     }
 
     public static byte[] loadFileToArray(final File file)
     {
         if (!file.exists())
         {
             return null;
         }
 
         try
         {
             final DataInputStream infile = new DataInputStream(new FileInputStream(file));
             final byte[] buffer = new byte[1024];
             final ByteArrayOutputStream fileData = new ByteArrayOutputStream();
             while (true)
             {
                 final int bytesRead = infile.read(buffer);
                 if (bytesRead > 0)
                 {
                     fileData.write(buffer, 0, bytesRead);
                 }
                 else
                 {
                     break;
                 }
             }
             return fileData.toByteArray();
         }
         catch (final IOException ex)
         {
             Log.log(Log.SYS, ex);
             return null;
         }
     }
 
     public static byte[] loadFileToArray(final String filename)
     {
         final File file = new File(filename);
         return loadFileToArray(file);
     }
 
     private static Image loadImage(final String name, final MediaTracker tracker)
     {
         Image image = null;
         try
         {
             image = Toolkit.getDefaultToolkit().createImage(name);
             if (image == null)
             {
                 return null;
             }
             tracker.addImage(image, 0);
         }
         catch (final Exception e)
         {
             Log.log(Log.SYS, e);
         }
         return image;
     }
 
     public static void msgBox(final Component parent, final String msg)
     {
         msgBox(parent, msg, "Error!");
     }
 
     public static void msgBox(final Component parent, final String msg, final String title)
     {
         JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.INFORMATION_MESSAGE);
     }
 
     public static String normalizeName(final String name)
     {
         final String in = name.trim().toLowerCase();
         final int len = in.length();
         final StringBuffer out = new StringBuffer(len);
         for (int i = 0; i < len; ++i)
         {
             final char c = in.charAt(i);
             if (Character.isJavaIdentifierPart(c))
             {
                 out.append(c);
             }
         }
 
         return out.toString();
     }
 
     static private void prepareFileDialog(final JFileChooser chooser, final String title, final boolean filter,
         final String extension)
     {
         if (lastDir != null)
         {
             chooser.setCurrentDirectory(new File(lastDir));
         }
 
         chooser.setDialogTitle(title);
 
         if (filter)
         {
             chooser.setFileFilter(new FileFilter()
             {
                 public boolean accept(final File file)
                 {
                     if (file.getName().endsWith(extension) || file.isDirectory())
                     {
                         return true;
                     }
                     return false;
                 }
 
                 public String getDescription()
                 {
                     return (extension + " files");
                 }
             });
         }
 
     }
 
     /**
      * Removes an image from the image cache.
      * 
      * @param name Name of the image to remove.
      */
     public static void removeCachedImage(final String name)
     {
         g_imageCache.remove(name);
     }
 
     public static String stitchTogetherWords(final String[] words)
     {
         return stitchTogetherWords(words, 0, words.length);
     }
 
     public static String stitchTogetherWords(final String[] words, final int offset)
     {
         return stitchTogetherWords(words, offset, words.length - offset);
     }
 
     public static String stitchTogetherWords(final String[] words, final int offset, final int l)
     {
         final StringBuffer retVal = new StringBuffer();
         int realLength = l;
         if (realLength > words.length - offset)
         {
             realLength = words.length - offset;
         }
 
         for (int i = offset, max = offset + realLength; i < max; ++i)
         {
             retVal.append(words[i]);
             if (i < (max - 1))
             {
                 retVal.append(' ');
             }
         }
 
         return retVal.toString();
     }
 
     /**
      * Decodes the given string using the URL decoding method.
      * 
      * @param in String to decode.
      * @return Decoded string.
      */
     public static String urlDecode(final String in)
     {
         try
         {
             return URLDecoder.decode(in, "UTF-8");
         }
         catch (final UnsupportedEncodingException e)
         {
             try
             {
                 return URLDecoder.decode(in, "ASCII");
             }
             catch (final UnsupportedEncodingException e2)
             {
                 return null;
             }
         }
     }
 
     /**
      * Encodes the given string using the URL encoding method.
      * 
      * @param in String to encode.
      * @return Encoded string.
      */
     public static String urlEncode(final String in)
     {
         try
         {
             return URLEncoder.encode(in, "UTF-8");
         }
         catch (final UnsupportedEncodingException e)
         {
             try
             {
                 return URLEncoder.encode(in, "ASCII");
             }
             catch (final UnsupportedEncodingException e2)
             {
                 return null;
             }
         }
     }
 
     private static void waitForImage(final Image image)
     {
         final MediaTracker tracker = new MediaTracker(GametableFrame.getGametableFrame());
         tracker.addImage(image, 0);
         try
         {
             tracker.waitForAll();
         }
         catch (final Exception e)
         {
             Log.log(Log.SYS, e);
         }
     }
 
     public static String xmlEncode(final String str)
     {
         final StringWriter out = new StringWriter();
         final StringReader in = new StringReader(str);
 
         try
         {
             UtilityFunctions.xmlEncode(out, in);
         }
         catch (final IOException ioe)
         {
             Log.log(Log.SYS, ioe);
             return null;
         }
 
         return out.toString();
     }
 
     public static void xmlEncode(final Writer out, final Reader in) throws IOException
     {
         while (true)
         {
             final int i = in.read();
             if (i < 0)
             {
                 break;
             }
 
             final char c = (char)i;
             final String entity = (String)ENTITY_NAME_MAP.get(new Character(c));
             if (entity != null)
             {
                 out.write('&');
                 out.write(entity);
                 out.write(';');
             }
             else
             {
                 out.write(c);
             }
         }
     }
 
     public static int yesNoCancelDialog(final Component parent, final String msg, final String title)
     {
         final int ret = JOptionPane.showConfirmDialog(parent, msg, title, JOptionPane.YES_NO_CANCEL_OPTION,
             JOptionPane.INFORMATION_MESSAGE);
         switch (ret)
         {
             case JOptionPane.YES_OPTION:
             {
                 return YES;
             }
             case JOptionPane.NO_OPTION:
             {
                 return NO;
             }
             default:
             {
                 return CANCEL;
             }
         }
     }
 
     public static int yesNoDialog(final Component parent, final String msg, final String title)
     {
         final int ret = JOptionPane.showConfirmDialog(parent, msg, title, JOptionPane.YES_NO_OPTION);
         switch (ret)
         {
             default:
             {
                 return YES;
             }
             case JOptionPane.NO_OPTION:
             {
                 return NO;
             }
         }
     }
 
     /**
      * Private constructor so no one can instantiate this.
      */
     private UtilityFunctions()
     {
         throw new RuntimeException("Do not do this.");
     }
 }
