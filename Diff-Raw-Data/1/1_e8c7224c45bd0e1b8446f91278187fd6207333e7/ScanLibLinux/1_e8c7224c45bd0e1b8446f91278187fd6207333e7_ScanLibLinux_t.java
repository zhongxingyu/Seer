 package edu.ualberta.med.scannerconfig.dmscanlib;
 
@SuppressWarnings("nls")
 public class ScanLibLinux extends ScanLib {
 
     public ScanLibLinux() {
 
     }
 
     @Override
     public ScanLibResult isTwainAvailable() {
         return new ScanLibResult(-1, -1, "not supported on linux");
     }
 
     @Override
     public ScanLibResult selectSourceAsDefault() {
         return new ScanLibResult(-1, -1, "not supported on linux");
     }
 
     @Override
     public ScanLibResult getScannerCapability() {
         return new ScanLibResult(-1, -1, "not supported on linux");
     }
 
     @Override
     public ScanLibResult scanImage(long verbose, long dpi,
         int brightness, int contrast, ScanRegion region, String filename) {
         return new ScanLibResult(-1, -1, "not supported on linux");
     }
 
     @Override
     public ScanLibResult scanFlatbed(long verbose, long dpi,
         int brightness, int contrast, String filename) {
         return new ScanLibResult(-1, -1, "not supported on linux");
     }
 
     @Override
     public DecodeResult decodePlate(long verbose, long dpi,
         int brightness, int contrast, long plateNum, ScanRegion region,
         double scanGap, long squareDev, long edgeThresh, long corrections,
         double cellDistance, double gapX, double gapY, long profileA,
         long profileB, long profileC, long orientation) {
         return new DecodeResult(-1, -1, "not supported on linux");
     }
 
     @Override
     public DecodeResult decodeImage(long verbose, long plateNum,
         String filename, double scanGap, long squareDev, long edgeThresh,
         long corrections, double cellDistance, double gapX, double gapY,
         long profileA, long profileB, long profileC, long orientation) {
         return new DecodeResult(-1, -1, "not supported on linux");
     }
 }
