 package fdt;
 
 import java.io.DataInputStream;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 import fdk.frm.FRMFrame;
 import fdk.frm.FRMImage;
 import fdk.lst.ArtMaker;
 import fdk.lst.BasicEntryMaker;
 import fdk.lst.CrittersEntryMaker;
 import fdk.lst.IEntryMaker;
 import fdk.lst.LST;
 import fdk.lst.ScriptLstMaker;
 import fdk.msg.MSG;
 import fdt.util.FID;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class FDT extends AbstractUIPlugin {
 
     // The plug-in ID
     public static final String PLUGIN_ID = "FDT";
 
     // The shared instance
    private static FDT         plugin;
 
     /**
      * The constructor
      */
     public FDT() {}
 
     public void start(BundleContext context) throws Exception {
         super.start(context);
         plugin = this;
 
         IResourceChangeListener listener = new MyResourceChangeReporter();
         ResourcesPlugin.getWorkspace().addResourceChangeListener(
                 listener,
                 IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_BUILD
                         | IResourceChangeEvent.POST_BUILD | IResourceChangeEvent.POST_CHANGE);
 
     }
 
     public void stop(BundleContext context) throws Exception {
         plugin = null;
         super.stop(context);
     }
 
     /**
      * Returns the shared instance
      * 
      * @return the shared instance
      */
     public static FDT getDefault() {
         return plugin;
     }
 
     /**
      * Returns an image descriptor for the image file at the given plug-in
      * relative path
      * 
      * @param path
      *            the path
      * @return the image descriptor
      */
     public static ImageDescriptor getImageDescriptor(String path) {
         return imageDescriptorFromPlugin(PLUGIN_ID, path);
     }
 
     private static Map<String, Image> imageCache = new HashMap<String, Image>();
 
     public static synchronized Image getCachedImage(String path) {
 
         Image image = imageCache.get(path);
         if (image == null) {
             image = getImageDescriptor(path).createImage();
             imageCache.put(path, image);
         }
         return image;
     }
 
     private static Map<IProject, Map<String, FRMFrame>> infoFrmCache = new HashMap<IProject, Map<String, FRMFrame>>();
 
     public static synchronized FRMFrame getFRM(IProject proj, String name) {
         Map<String, FRMFrame> cache = infoFrmCache.get(proj);
         if (cache == null) {
             cache = new HashMap<String, FRMFrame>();
             infoFrmCache.put(proj, cache);
         }
 
         try {
             FRMFrame frm = cache.get(name);
             if (frm == null) {
                 FRMImage img = new FRMImage(getFile(proj, name).getContents());
                 if (img.isRotable()) {
                     frm = img.getFrames(3)[0];
                 } else {
                     frm = img.getFrames(0)[0];
                 }
                 cache.put(name, frm);
             }
             return frm;
         } catch (Exception e) {
             FRMImage reserved;
             try {
                 reserved = new FRMImage(getFile(proj, "art/items/reserved.frm").getContents());
                 return reserved.getFrames(0)[0];
             } catch (Exception e1) {
                 e1.printStackTrace();
             }
         }
         return null;
     }
 
     public static FRMFrame getFRMByFID(IProject proj, int fid) {
         String fname = FID.getFileNameByFID(proj, fid);
         return getFRM(proj, fname);
     }
 
     private static Map<IProject, Map<String, MSG>> cachedMsg = new HashMap<IProject, Map<String, MSG>>();
 
     public static synchronized MSG getCachedMsg(IProject proj, String fname) {
         Map<String, MSG> cache = cachedMsg.get(proj);
         if (cache == null) {
             cache = new HashMap<String, MSG>();
             cachedMsg.put(proj, cache);
         }
 
         // TODO resourses changes, charset changes
         MSG msg = cache.get(fname);
         if (msg == null) {
             try {
                 Charset cs = Charset.forName(proj.getDefaultCharset());
                 msg = new MSG(getFile(proj, fname).getContents(), cs);
                 cache.put(fname, msg);
             } catch (Exception e) {
                 return null;
             }
         }
         return msg;
     }
 
     private static Map<IProject, Map<String, LST>> cachedLst = new HashMap<IProject, Map<String, LST>>();
 
     public static synchronized LST getCachedLST(IProject proj, String name) {
         Map<String, LST> cache = cachedLst.get(proj);
         if (cache == null) {
             cache = new HashMap<String, LST>();
             cachedLst.put(proj, cache);
         }
 
         LST lst = cache.get(name);
         if (lst == null) {
             try {
                 Charset cs = Charset.forName(proj.getDefaultCharset());
                 IEntryMaker maker = getLstMaker(name);
                 lst = new LST(getFile(proj, name).getContents(), cs, maker);
                 cache.put(name, lst);
             } catch (Exception e) {
                 return null;
             }
         }
         return lst;
     }
 
     private static IEntryMaker getLstMaker(String name) {
         final Pattern art = Pattern.compile("art\\/(.*)\\/");
         Matcher m = art.matcher(name);
         if (m.find()) {
             return new ArtMaker(m.group(1));
         } else if (name.equalsIgnoreCase("scripts/scripts.lst")) {
             return new ScriptLstMaker();
         } else if (name.equalsIgnoreCase("proto/critters/critters.lst")) {
             return new CrittersEntryMaker();
         }
         return new BasicEntryMaker();
     }
 
     private static Map<IProject, PaletteData> stdPal = new HashMap<IProject, PaletteData>();
 
     public static PaletteData getStdPal(IProject proj) {
         PaletteData pal = stdPal.get(proj);
         if (pal == null) {
             try {
                 InputStream palStream = getFile(proj, "color.pal").getContents();
                 byte rawpal[] = new byte[256 * 3];
                 DataInputStream ds = new DataInputStream(palStream);
                 ds.readFully(rawpal);
                 addAnimPal(rawpal);
                 pal = FID.getPalette(rawpal);
                 stdPal.put(proj, pal);
             } catch (Exception e) {
                 return null;
             }
         }
         return pal;
     }
 
     private static byte[] shr2(byte[] arr) {
         for (int i = 0; i < arr.length; i++) {
             arr[i] >>= 2;
         }
         return arr;
     }
 
     private static void addAnimPal(byte[] rawpal) {
         final byte[] Slime = shr2(new byte[] { 0, 108, 0, 11, 115, 7, 27, 123, 15, 43, (byte) 131, 27 });
         final byte[] Monitors = shr2(new byte[] { 107, 107, 111, 99, 103, 127, 87, 107, (byte) 143, 0, (byte) 147,
                 (byte) 163, 107, (byte) 187, (byte) 255 });
         final byte[] FireSlow = shr2(new byte[] { (byte) 255, 0, 0, (byte) 215, 0, 0, (byte) 147, 43, 11, (byte) 255,
                 119, 0, (byte) 255, 59, 0 });
         final byte[] FireFast = shr2(new byte[] { 71, 0, 0, 123, 0, 0, (byte) 179, 0, 0, 123, 0, 0, 71, 0, 0 });
         final byte[] Shoreline = shr2(new byte[] { 83, 63, 43, 75, 59, 43, 67, 55, 39, 63, 51, 39, 55, 47, 35, 51, 43,
                 35 });
         final byte[] BlinkRed = shr2(new byte[] { (byte) 252, 0, 0 });
 
         System.arraycopy(Slime, 0, rawpal, 229 * 3, Slime.length);
         System.arraycopy(Monitors, 0, rawpal, 233 * 3, Monitors.length);
         System.arraycopy(FireSlow, 0, rawpal, 238 * 3, FireSlow.length);
         System.arraycopy(FireFast, 0, rawpal, 243 * 3, FireFast.length);
         System.arraycopy(Shoreline, 0, rawpal, 248 * 3, Shoreline.length);
         System.arraycopy(BlinkRed, 0, rawpal, 254 * 3, BlinkRed.length);
 
     }
 
     public static IResource getResourceAbsolute(IProject project, String path) {
         try {
             IPath p = new Path(path);
 
             IContainer prev = null;
             IContainer current = project;
             for (int i = 0; i < p.segmentCount(); i++) {
                 if (current == prev) break;
 
                 for (IResource r : current.members()) {
                     if (r.getName().equalsIgnoreCase(p.segment(i))) {
                         if (r instanceof IContainer) {
                             if (i == p.segmentCount() - 1)
                                 return r;
                             else {
                                 prev = current;
                                 current = (IContainer) r;
                                 break;
                             }
                         } else if (r instanceof IFile && i == p.segmentCount() - 1) {
                             IFile f = (IFile) r;
                             f.refreshLocal(IResource.DEPTH_ZERO, null);
                             return f;
                         }
                     }
                 }
             }
         } catch (CoreException e) {}
 
         return null;
     }
 
     public static IResource getResource(IProject project, String name) {
         IResource result = getResourceAbsolute(project, "Patches/" + name);
         if (result != null) return result;
 
         return getResourceAbsolute(project, "Resources/" + name);
     }
 
     public static IFile getFile(IProject project, String name) {
         return (IFile) getResource(project, name);
     }
 
     public static IFolder getFolder(IProject project, String name) {
         return (IFolder) getResource(project, name);
     }
 
     private static void fcreate(IFolder fld) throws CoreException {
         if (!fld.exists()) {
             fcreate((IFolder) fld.getParent());
             fld.create(true, true, null);
         }
     }
 
     public static IFolder getFolderForce(IProject proj, String name) throws CoreException {
         IResource result = getResourceAbsolute(proj, "Patches/" + name);
         if (result != null) return (IFolder) result;
         IFolder fld = proj.getFolder("Patches/" + name);
         fcreate(fld);
         return fld;
     }
 }
