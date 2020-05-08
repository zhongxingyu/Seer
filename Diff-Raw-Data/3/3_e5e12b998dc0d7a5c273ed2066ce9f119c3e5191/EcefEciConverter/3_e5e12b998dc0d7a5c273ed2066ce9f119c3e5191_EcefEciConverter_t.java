 package org.esa.beam.chris.operators;
 
 class EcefEciConverter {
 
     /**
      * Angular rotational velocity of the Earth (rad s-1)
      */
     private static final double WE = 7.292115854788046E-5;
 
     private final double c;
     private final double s;
 
     public static double[] ecefToEci(double gst, double[] ecef, double[] eci) {
         if (ecef == null) {
             throw new IllegalArgumentException("ecef == null");
         }
         if (eci == null) {
             throw new IllegalArgumentException("eci == null");
         }
         if (ecef.length < 2) {
             throw new IllegalArgumentException("ecef.length < 2");
         }
         if (eci.length < 2) {
             throw new IllegalArgumentException("eci.length < 2");
         }
         if (ecef.length > 6) {
             throw new IllegalArgumentException("ecef.length < 2");
         }
         if (eci.length > 6) {
             throw new IllegalArgumentException("eci.length < 2");
         }
         if (eci.length != ecef.length) {
             throw new IllegalArgumentException("eci.length != ecef.length");
         }
 
         final double c = Math.cos(gst);
         final double s = Math.sin(gst);
 
         return ecefToEci(c, s, ecef, eci);
     }
 
     public static double[] eciToEcef(double gst, double[] eci, double[] ecef) {
         if (eci == null) {
             throw new IllegalArgumentException("eci == null");
         }
         if (ecef == null) {
             throw new IllegalArgumentException("ecef == null");
         }
         if (eci.length < 2) {
             throw new IllegalArgumentException("eci.length < 2");
         }
         if (ecef.length < 2) {
             throw new IllegalArgumentException("ecef.length < 2");
         }
         if (eci.length > 6) {
             throw new IllegalArgumentException("eci.length < 2");
         }
         if (ecef.length > 6) {
             throw new IllegalArgumentException("ecef.length < 2");
         }
         if (ecef.length != eci.length) {
             throw new IllegalArgumentException("ecef.length != eci.length");
         }
 
         final double c = Math.cos(gst);
         final double s = Math.sin(gst);
 
         return eciToEcef(c, s, eci, ecef);
     }
 
     public EcefEciConverter(double gst) {
         c = Math.cos(gst);
         s = Math.sin(gst);
     }
 
     public double[] ecefToEci(double[] ecef, double[] eci) {
         if (ecef == null) {
             throw new IllegalArgumentException("ecef == null");
         }
         if (eci == null) {
             throw new IllegalArgumentException("eci == null");
         }
         if (ecef.length < 2) {
             throw new IllegalArgumentException("ecef.length < 2");
         }
         if (eci.length < 2) {
             throw new IllegalArgumentException("eci.length < 2");
         }
         if (ecef.length > 6) {
             throw new IllegalArgumentException("ecef.length < 2");
         }
         if (eci.length > 6) {
             throw new IllegalArgumentException("eci.length < 2");
         }
         if (eci.length != ecef.length) {
             throw new IllegalArgumentException("eci.length != ecef.length");
         }
 
         return ecefToEci(c, s, ecef, eci);
     }
 
     public double[] eciToEcef(double[] eci, double[] ecef) {
         if (eci == null) {
             throw new IllegalArgumentException("eci == null");
         }
         if (ecef == null) {
             throw new IllegalArgumentException("ecef == null");
         }
         if (eci.length < 2) {
             throw new IllegalArgumentException("eci.length < 2");
         }
         if (ecef.length < 2) {
             throw new IllegalArgumentException("ecef.length < 2");
         }
         if (eci.length > 6) {
             throw new IllegalArgumentException("eci.length < 2");
         }
         if (ecef.length > 6) {
             throw new IllegalArgumentException("ecef.length < 2");
         }
         if (ecef.length != eci.length) {
             throw new IllegalArgumentException("ecef.length != eci.length");
         }
 
         return eciToEcef(c, s, eci, ecef);
     }
 
     static double[] ecefToEci(double c, double s, double[] ecef, double[] eci) {
         final double x = ecefToEciX(c, s, ecef[0], ecef[1]);
         final double y = ecefToEciY(c, s, ecef[0], ecef[1]);
         eci[0] = x;
         eci[1] = y;
 
         if (eci.length == 3) {
             eci[2] = ecef[2];
         } else if (eci.length == 4) {
             final double u = ecefToEciX(c, s, ecef[2], ecef[3]) - WE * y;
             final double v = ecefToEciY(c, s, ecef[2], ecef[3]) + WE * x;
             eci[2] = u;
             eci[3] = v;
         } else if (eci.length == 6) {
             final double u = ecefToEciX(c, s, ecef[3], ecef[4]) - WE * y;
             final double v = ecefToEciY(c, s, ecef[3], ecef[4]) + WE * x;
            eci[2] = ecef[2];
             eci[3] = u;
             eci[4] = v;
             eci[5] = ecef[5];
         }
 
         return eci;
     }
 
     static double[] eciToEcef(double c, double s, double[] eci, double[] ecef) {
         final double x = eciToEcefX(c, s, eci[0], eci[1]);
         final double y = eciToEcefY(c, s, eci[0], eci[1]);
 
         ecef[0] = x;
         ecef[1] = y;
 
         if (ecef.length == 3) {
             ecef[2] = eci[2];
         } else if (ecef.length == 4) {
             final double u = eciToEcefX(c, s, eci[2], eci[3]) - WE * y;
             final double v = eciToEcefY(c, s, eci[2], eci[3]) + WE * x;
             ecef[2] = u;
             ecef[3] = v;
         } else if (ecef.length == 6) {
             final double u = eciToEcefX(c, s, eci[3], eci[4]) - WE * y;
             final double v = eciToEcefY(c, s, eci[3], eci[4]) + WE * x;
            ecef[2] = eci[2];
             ecef[3] = u;
             ecef[4] = v;
             ecef[5] = eci[5];
         }
 
         return ecef;
     }
 
     static double ecefToEciX(double c, double s, double ecefX, double ecefY) {
         return c * ecefX - s * ecefY;
     }
 
     static double ecefToEciY(double c, double s, double ecefX, double ecefY) {
         return s * ecefX + c * ecefY;
     }
 
     static double ecefToEciU(double c, double s, double ecefX, double ecefY, double ecefU, double ecefV) {
         return ecefToEciX(c, s, ecefU, ecefV) - WE * ecefToEciY(c, s, ecefX, ecefY);
     }
 
     static double ecefToEciV(double c, double s, double ecefX, double ecefY, double ecefU, double ecefV) {
         return ecefToEciY(c, s, ecefU, ecefV) + WE * ecefToEciX(c, s, ecefX, ecefY);
     }
 
     static double eciToEcefX(double c, double s, double eciX, double eciY) {
         return c * eciX + s * eciY;
     }
 
     static double eciToEcefY(double c, double s, double eciX, double eciY) {
         return c * eciY - s * eciX;
     }
 
     static double eciToEcefU(double c, double s, double eciX, double eciY, double eciU, double eciV) {
         return eciToEcefX(c, s, eciU, eciV) + WE * eciToEcefY(c, s, eciX, eciY);
     }
 
     static double eciToEcefV(double c, double s, double eciX, double eciY, double eciU, double eciV) {
         return eciToEcefY(c, s, eciU, eciV) - WE * eciToEcefX(c, s, eciX, eciY);
     }
 }
