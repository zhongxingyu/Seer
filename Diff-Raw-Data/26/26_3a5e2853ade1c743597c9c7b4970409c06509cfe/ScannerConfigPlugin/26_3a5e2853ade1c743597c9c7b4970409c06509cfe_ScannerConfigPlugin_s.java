 package edu.ualberta.med.scannerconfig;
 
 import java.net.URL;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.services.ISourceProviderService;
 import org.osgi.framework.BundleContext;
 
 import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
 import edu.ualberta.med.scannerconfig.scanlib.ScanCell;
 import edu.ualberta.med.scannerconfig.scanlib.ScanLib;
 import edu.ualberta.med.scannerconfig.scanlib.ScanLibWin32;
 import edu.ualberta.med.scannerconfig.sourceproviders.PlateEnabledState;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class ScannerConfigPlugin extends AbstractUIPlugin {
 
     public static final String IMG_SCANNER = "scanner";
 
     // The plug-in ID
     public static final String PLUGIN_ID = "scannerConfig";
 
     // The shared instance
     private static ScannerConfigPlugin plugin;
 
     /**
      * The constructor
      */
     public ScannerConfigPlugin() {
         String osname = System.getProperty("os.name");
         if (osname.startsWith("Windows")) {
             System.loadLibrary("scanlib");
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
      * )
      */
     @Override
     public void start(BundleContext context) throws Exception {
         super.start(context);
         plugin = this;
 
         getPreferenceStore().addPropertyChangeListener(
             new IPropertyChangeListener() {
                 public void propertyChange(PropertyChangeEvent event) {
                     if (event.getProperty().startsWith(
                         "scanner.plate.coords.enabled.")) {
                         IWorkbenchWindow window = PlatformUI.getWorkbench()
                             .getActiveWorkbenchWindow();
                         ISourceProviderService service = (ISourceProviderService) window
                             .getService(ISourceProviderService.class);
 
                         PlateEnabledState plateEnabledSourceProvider = (PlateEnabledState) service
                             .getSourceProvider(PlateEnabledState.PLATES_ENABLED);
                         Assert.isNotNull(plateEnabledSourceProvider);
                         plateEnabledSourceProvider.setPlateEnabled();
                     }
                 }
             });
     }
 
     @Override
     protected void initializeImageRegistry(ImageRegistry registry) {
         registerImage(registry, IMG_SCANNER, "selectScanner.png");
     }
 
     private void registerImage(ImageRegistry registry, String key,
         String fileName) {
         try {
             IPath path = new Path("icons/" + fileName);
             URL url = FileLocator.find(getBundle(), path, null);
             if (url != null) {
                 ImageDescriptor desc = ImageDescriptor.createFromURL(url);
                 registry.put(key, desc);
             }
         } catch (Exception e) {
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
      * )
      */
     @Override
     public void stop(BundleContext context) throws Exception {
         plugin = null;
         super.stop(context);
     }
 
     /**
      * Returns the shared instance
      * 
      * @return the shared instance
      */
     public static ScannerConfigPlugin getDefault() {
         return plugin;
     }
 
     public void initialize() {
     }
 
     public static void scanImage(double left, double top, double right,
         double bottom, String filename) throws Exception {
         int dpi = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.SCANNER_DPI);
         int brightness = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.SCANNER_BRIGHTNESS);
         int contrast = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.SCANNER_CONTRAST);
         int debugLevel = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.DLL_DEBUG_LEVEL);
         int res = ScanLibWin32.getInstance().slScanImage(debugLevel, dpi,
             brightness, contrast, left, top, right, bottom, filename);
 
         if (res < ScanLibWin32.SC_SUCCESS) {
             throw new Exception("Could not decode image. "
                 + ScanLib.getErrMsg(res));
         }
     }
 
     public static void scanPlate(int plateNumber, String filename)
         throws Exception {
 
         String[] prefsArr = PreferenceConstants.SCANNER_PALLET_COORDS[plateNumber - 1];
 
         ScannerRegion region = new ScannerRegion("" + plateNumber, getDefault()
             .getPreferenceStore().getDouble(prefsArr[0]), getDefault()
             .getPreferenceStore().getDouble(prefsArr[1]), getDefault()
             .getPreferenceStore().getDouble(prefsArr[2]), getDefault()
             .getPreferenceStore().getDouble(prefsArr[3]));
         scanImage(region.left, region.top, region.right, region.bottom,
             filename);
     }
 
     public static ScanCell[][] scan(int plateNumber) throws Exception {
         int dpi = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.SCANNER_DPI);
         int brightness = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.SCANNER_BRIGHTNESS);
         int contrast = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.SCANNER_CONTRAST);
         int debugLevel = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.DLL_DEBUG_LEVEL);
         int edgeThresh = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.LIBDMTX_EDGE_THRESH);
         double scanGap = getDefault().getPreferenceStore().getDouble(
             PreferenceConstants.LIBDMTX_SCAN_GAP);
         int squareDev = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.LIBDMTX_SQUARE_DEV);
         int corrections = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.LIBDMTX_CORRECTIONS);
         double cellDistance = getDefault().getPreferenceStore().getDouble(
             PreferenceConstants.LIBDMTX_CELL_DISTANCE);
 
         String[] prefsArr = PreferenceConstants.SCANNER_PALLET_COORDS[plateNumber - 1];
 
         ScannerRegion region = new ScannerRegion("" + plateNumber, getDefault()
             .getPreferenceStore().getDouble(prefsArr[0]), getDefault()
             .getPreferenceStore().getDouble(prefsArr[1]), getDefault()
             .getPreferenceStore().getDouble(prefsArr[2]), getDefault()
             .getPreferenceStore().getDouble(prefsArr[3]));
 
         int res = ScanLib.getInstance().slDecodePlate(debugLevel, dpi,
             brightness, contrast, plateNumber, region.left, region.top,
             region.right, region.bottom, scanGap, squareDev, edgeThresh,
             corrections, cellDistance);
 
         if (res < ScanLib.SC_SUCCESS) {
             throw new Exception("Could not decode image. "
                 + ScanLib.getErrMsg(res));
         }
         return ScanCell.getScanLibResults();
     }
 
     public static ScanCell[][] scanMultipleDpi(int plateNumber)
         throws Exception {
         int dpi1 = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.SCANNER_MULTIPLE_DPIS[0]);
         int dpi2 = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.SCANNER_MULTIPLE_DPIS[1]);
         int dpi3 = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.SCANNER_MULTIPLE_DPIS[2]);
         int brightness = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.SCANNER_BRIGHTNESS);
         int contrast = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.SCANNER_CONTRAST);
         int debugLevel = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.DLL_DEBUG_LEVEL);
         int edgeThresh = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.LIBDMTX_EDGE_THRESH);
         double scanGap = getDefault().getPreferenceStore().getDouble(
             PreferenceConstants.LIBDMTX_SCAN_GAP);
         int squareDev = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.LIBDMTX_SQUARE_DEV);
         int corrections = getDefault().getPreferenceStore().getInt(
             PreferenceConstants.LIBDMTX_CORRECTIONS);
         double cellDistance = getDefault().getPreferenceStore().getDouble(
             PreferenceConstants.LIBDMTX_CELL_DISTANCE);
 
         String[] prefsArr = PreferenceConstants.SCANNER_PALLET_COORDS[plateNumber - 1];
 
         ScannerRegion region = new ScannerRegion("" + plateNumber, getDefault()
             .getPreferenceStore().getDouble(prefsArr[0]), getDefault()
             .getPreferenceStore().getDouble(prefsArr[1]), getDefault()
             .getPreferenceStore().getDouble(prefsArr[2]), getDefault()
             .getPreferenceStore().getDouble(prefsArr[3]));
 
         int res = ScanLib.getInstance().slDecodePlateMultipleDpi(debugLevel,
             dpi1, dpi2, dpi3, brightness, contrast, plateNumber, region.left,
             region.top, region.right, region.bottom, scanGap, squareDev,
             edgeThresh, corrections, cellDistance);
 
         if (res < ScanLib.SC_SUCCESS) {
             throw new Exception("Could not decode image. "
                 + ScanLib.getErrMsg(res));
         }
         return ScanCell.getScanLibResults();
     }
 
     public boolean getPlateEnabled(int plateId) {
         Assert.isTrue((plateId > 0)
             && (plateId <= PreferenceConstants.SCANNER_PALLET_ENABLED.length),
             "plate id is invalid: " + plateId);
         return getPreferenceStore().getBoolean(
             PreferenceConstants.SCANNER_PALLET_ENABLED[plateId - 1]);
     }
 
     public int getPlateCount() {
         int result = 0;
         for (int i = 0; i < PreferenceConstants.SCANNER_PALLET_ENABLED.length; ++i) {
             if (getPreferenceStore().getBoolean(
                 PreferenceConstants.SCANNER_PALLET_ENABLED[i]))
                 ++result;
         }
         return result;
     }
 
     public static int getPlatesMax() {
         return PreferenceConstants.SCANNER_PALLET_ENABLED.length;
     }
 
     public int getDpi() {
         return getPreferenceStore().getInt(PreferenceConstants.SCANNER_DPI);
     }
 
     public int getBrightness() {
         return getPreferenceStore().getInt(
             PreferenceConstants.SCANNER_BRIGHTNESS);
     }
 
     public int getContrast() {
         return getPreferenceStore()
             .getInt(PreferenceConstants.SCANNER_CONTRAST);
     }
 
     /**
      * Display an error message
      */
     public static void openError(String title, String message) {
         MessageDialog.openError(PlatformUI.getWorkbench()
             .getActiveWorkbenchWindow().getShell(), title, message);
     }
 
     /**
      * Display an error message asynchronously
      */
     public static void openAsyncError(final String title, final String message) {
         Display.getDefault().asyncExec(new Runnable() {
             public void run() {
                 MessageDialog.openError(PlatformUI.getWorkbench()
                     .getActiveWorkbenchWindow().getShell(), title, message);
             }
         });
     }
 }
