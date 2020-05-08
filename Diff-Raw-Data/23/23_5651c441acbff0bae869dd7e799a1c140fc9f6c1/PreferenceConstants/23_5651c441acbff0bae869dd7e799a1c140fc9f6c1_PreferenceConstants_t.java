 package edu.ualberta.med.scannerconfig.preferences;
 
 public class PreferenceConstants {
 
     public static final String SCANNER_DPI = "scanner.dpi";
 
     public static final String SCANNER_BRIGHTNESS = "scanner.brightness";
 
     public static final String SCANNER_CONTRAST = "scanner.contrast";
 
     public static final String DLL_DEBUG_LEVEL = "scanner.dll_debug_level";
 
     public static final String SCANNER_DRV_TYPE = "scanner.drvtype";
 
     public static final String SCANNER_DRV_TYPE_WIA = "scanner.drvtype.wia";
 
     public static final String SCANNER_DRV_TYPE_TWAIN = "scanner.drvtype.twain";
 
     public static final String LIBDMTX_EDGE_THRESH = "libdmtx.edge_thresh";
 
     public static final String LIBDMTX_SCAN_GAP = "libdmtx.scan_gap";
 
     public static final String LIBDMTX_SQUARE_DEV = "libdmtx.square_dev";
 
    public static final String LIBDMTX_CORRECTIONS = "libdmtx.corrections";

     public static final String[] SCANNER_PALLET_ENABLED = {
         "scanner.plate.coords.enabled.1", "scanner.plate.coords.enabled.2",
         "scanner.plate.coords.enabled.3", "scanner.plate.coords.enabled.4",
         "scanner.plate.coords.enabled.5" };
 
     public static final String[][] SCANNER_PALLET_COORDS = {
         { "scanner.plate.coords.left.1", "scanner.plate.coords.top.1",
             "scanner.plate.coords.right.1", "scanner.plate.coords.bottom.1" },
 
         { "scanner.plate.coords.left.2", "scanner.plate.coords.top.2",
             "scanner.plate.coords.right.2", "scanner.plate.coords.bottom.2" },
 
         { "scanner.plate.coords.left.3", "scanner.plate.coords.top.3",
             "scanner.plate.coords.right.3", "scanner.plate.coords.bottom.3" },
 
         { "scanner.plate.coords.left.4", "scanner.plate.coords.top.4",
             "scanner.plate.coords.right.4", "scanner.plate.coords.bottom.4" },
 
         { "scanner.plate.coords.left.5", "scanner.plate.coords.top.5",
             "scanner.plate.coords.right.5", "scanner.plate.coords.bottom.5" } };
 
 }
