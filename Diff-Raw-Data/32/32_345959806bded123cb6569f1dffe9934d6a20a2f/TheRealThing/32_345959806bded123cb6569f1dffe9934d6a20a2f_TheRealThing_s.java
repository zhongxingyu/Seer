 /*
  * $Id: $
  *
  * Copyright (C) 2009 by Brockmann Consult (info@brockmann-consult.de)
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation. This program is distributed in the hope it will
  * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.esa.beam.chris.operators;
 
 import org.esa.beam.chris.operators.GPSTime.GPSReader;
 import org.esa.beam.chris.operators.ImageCenterTime.ITCReader;
 import org.esa.beam.chris.operators.math.PolynomialSplineFunction;
 import org.esa.beam.chris.operators.math.SplineInterpolator;
import org.esa.beam.util.DateTimeUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 public class TheRealThing {
     
     private static final int SlowDown = 5; // Slowdown factor
 
     //Epoch for a reduced Julian Day (all JD values are substracted by this value). 
     //This way the calculations can be performed more efficiently.
     private static final double jd0 = Conversions.julianDate(2001, 0, 1);
 
     // Time difference between GPS Time and UT1 (year dependent). In 2009 it will be 14.
     // It is CRITICAL to substract it to both GPS and ITC data
     private static final int dTgps = 13; 
     
     // there is a delay of 0.999s between the GPS time tag and the actual time of the reported position/velocity.
     private static final double delay=0.999; 
     
     // Variables used for array subscripting, so the code is more readable.
     private static final int X=0;                      
     private static final int Y=1;
     private static final int Z=2;
 
            
     ///////////////////////////////////////////////////////////
     
     public File ictFile;
     public File gpsFile;
     public int mode;
 
     ///////////////////////////////////////////////////////////
     
     public void doIt() throws IOException {
         // ICT
         ITCReader ictReader = new ImageCenterTime.ITCReader(new FileInputStream(ictFile));
         double[] lastIctValues = ictReader.getLastIctValues();
         ImageCenterTime lastImageCenterTime = ImageCenterTime.create(lastIctValues, dTgps);
         
         // GPS
         GPSReader gpsReader = new GPSTime.GPSReader(new FileInputStream(gpsFile));
         List<String[]> gpsRecords = gpsReader.getReadRecords();
         List<GPSTime> gps = GPSTime.create(gpsRecords, dTgps, delay);
         
         // GCP
         // TODO
         
         ChrisModeConstants modeConstants = ChrisModeConstants.get(mode);
         
         //////////////////////////
         // Prepare Time Frames
         //////////////////////////
         
         // The last element of ict_njd corresponds to the acquisition setup time, that occurs 390s before the start of acquisition.
         double[] ict_njd = {lastImageCenterTime.ict1 - jd0,
                             lastImageCenterTime.ict2 - jd0,
                             lastImageCenterTime.ict3 - jd0,
                             lastImageCenterTime.ict4 - jd0,
                             lastImageCenterTime.ict5 - jd0,
                            lastImageCenterTime.ict1 - (10 + 390)/DateTimeUtils.SECONDS_PER_DAY - jd0};
         
         double[] T_ict = Arrays.copyOfRange(ict_njd, 0, 5);
         
         //---- Nos quedamos con todos los elementos de GPS menos el ultimo ----------------
         //----   ya q es donde se almacena la AngVel media, y perder un dato --------------
         //----   de GPS no es un problema. ------------------------------------------------
         
         // We are left with all elements but the last GPS
         // and q is where stores AngVel half, and losing data
         // GPS is not a problem.
         int numGPS = gps.size() - 2;
         double[] gps_njd = new double[numGPS];
         for (int i = 0; i < gps_njd.length; i++) {
             gps_njd[i] = gps.get(i).jd - jd0;
         }
         
         // ---- Critical Times ---------------------------------------------
         
         double[] T_ini = new double[5]; // imaging start time (imaging lasts ~9.5s in every mode)
         double[] T_end = new double[5]; // imaging stop time
         for (int i = 0; i < T_ini.length; i++) {
            T_ini[i] = ict_njd[i] - (modeConstants.getTimg()/2)/DateTimeUtils.SECONDS_PER_DAY; 
            T_end[i] = ict_njd[i] + (modeConstants.getTimg()/2)/DateTimeUtils.SECONDS_PER_DAY; 
         }
        double T_i = ict_njd[0] - 10/DateTimeUtils.SECONDS_PER_DAY; // "imaging mode" start time
        double T_e = ict_njd[4] + 10/DateTimeUtils.SECONDS_PER_DAY; // "imaging mode" stop time
        
         // Searches the closest values in the telemetry to the Critical Times (just for plotting purposses)
         // skipped
         
         //---- determine per-Line Time Frame -----------------------------------
         
         // Time elapsed since imaging start at each image line
         double[] T_lin = new double[modeConstants.getNLines()];
         for (int i = 0; i < T_lin.length; i++) {
            T_lin[i] = (i * modeConstants.getDt() + modeConstants.getTpl()/2)/DateTimeUtils.SECONDS_PER_DAY;
             // +TpL/2 is added to set the time at the middle of the integration time, i.e. pixel center
         }
         
         double[][] T_img = new double[modeConstants.getNLines()][5];
         for (int j = 0; j < modeConstants.getNLines(); j++) {
             for (int i = 0; i < 5; i++) {
                 T_img[j][i] = T_ini[i] + T_lin[j];
             }
         }
         
         double[] T = new double[ 1 + 5 * modeConstants.getNLines()];
         T[0] = ict_njd[5];
         int Tindex = 1;
         for (int j = 0; j < modeConstants.getNLines(); j++) {
             for (int i = 0; i < 5; i++) {
                 T[Tindex] = T_img[j][i];
                 Tindex++;
             }
         }
         
         double Tobs = T_e - T_i; // Duration of "imaging mode" for each image. (redundant, should be 20s)
         double[] Tobs2 = new double[T_end.length]; // Duration of actual imaging for each image
         for (int i = 0; i < Tobs2.length; i++) {
             Tobs2[i] = T_end[i] - T_ini[i];
         }
         
         // Set the indices of T that correspond to critical times (integration start and stop) for each image
         
         // The first element of T corresponds to the acquisition-setup time, so Tini[0] must skip element 0 of T
         int[] Tini = new int[5];
         int[] Tend = new int[5];
         for (int i = 0; i < Tini.length; i++) {
             Tini[i] = modeConstants.getNLines() * i + 1;
             Tend[i] = Tini[i] + modeConstants.getNLines() - 1; 
         }
         int Tfix = 0; // Index corresponding to the time of fixing the orbit
         int numT = T.length;
         
 
         // ========================================================================
         // ===                     Inertial Coordinates                         ===
         // ==v==v== ==v== Converts coordinates from ECEF to ECI ==v==v== ==v==v== =
         
         // Pos/Vel with Time from Telemetry
         
         double[][] eci = new double[gps.size()][6];
         for (int i = 0; i < gps.size(); i++) {
             GPSTime gpsTime = gps.get(i);
             // position and velocity is given in meters, 
             // we transform to km in order to keep the values smaller. from now all distances in Km
             double[] ecf = {gpsTime.posX/1000, gpsTime.posY/1000, gpsTime.posZ/1000,
                             gpsTime.velX/1000, gpsTime.velY/1000, gpsTime.velZ/1000};
             double gst = Conversions.jdToGST(gpsTime.jd);
             EcefEciConverter.ecefToEci(gst, ecf, eci[i]);
         }
         
         // =======================================================================
         // ===                  Data to per-Line Time Frame                    ===
         // =======================================================================
         // ---- Interpolate GPS ECI position/velocity to per-line time -------
         
         double[] iX = spline(gps_njd, get2ndDim(eci, 0, numGPS), T);
         double[] iY = spline(gps_njd, get2ndDim(eci, 1, numGPS), T);
         double[] iZ = spline(gps_njd, get2ndDim(eci, 2, numGPS), T);
         double[] iVX = spline(gps_njd, get2ndDim(eci, 3, numGPS), T);
         double[] iVY = spline(gps_njd, get2ndDim(eci, 4, numGPS), T);
         double[] iVZ = spline(gps_njd, get2ndDim(eci, 5, numGPS), T);
         
         double[] iR = new double[numGPS];
         for (int i = 0; i < iR.length; i++) {
             iR[i] = Math.sqrt(Math.pow(iX[i], 2) + Math.pow(iY[i], 2) + Math.pow(iZ[i], 2));
         }
         
         // ==v==v== Get Orbital Plane Vector ==================================================
         // ---- Calculates normal vector to orbital plane --------------------------
         double[][] Wop = vect_prod(iX,iY,iZ, iVX,iVY,iVZ);
         double[][] uWop = unit(Wop);
         
         // Fixes orbital plane vector to the corresponding point on earth at the time of acquistion setup
         double gst_opv = Conversions.jdToGST(T[Tfix] + jd0);
         double[] eci_opv = {uWop[X][Tfix], uWop[Y][Tfix], uWop[Z][Tfix]}; 
         double[] uWecf = new double[3]; 
         EcefEciConverter.eciToEcef(gst_opv, eci_opv, uWecf);
 
         double[][] uW = new double[T.length][3];
         for (int i = 0; i < T.length; i++) {
             double gst = Conversions.jdToGST(T[i] + jd0);
             EcefEciConverter.ecefToEci(gst, uWecf, uW[i]);
         }
         
         // ==v==v== Get Angular Velocity ======================================================
         // Angular velocity is not really used in the model, except the AngVel at orbit fixation time (iAngVel[0])
         
         // AngVelRaw = AngVel(gps.secs, eci.x, eci.y, eci.z)
         // AngVel = smooth(AngVelRaw[0:n], 5)
         // iAngVel = spline(gps_njd, AngVel, T, /double)
         
         
     }
     
     private double[][] unit(double[][] vec) {
         double[][] result = new double[vec.length][vec[0].length];
         for (int i = 0; i < vec[0].length; i++) {
             double norm = Math.sqrt (vec[X][i] * vec[X][i] + vec[Y][i] * vec[Y][i] + vec[Z][i] * vec[Z][i]);
             result[X][i] = vec[X][i] / norm;
             result[Y][i] = vec[Y][i] / norm;
             result[Z][i] = vec[Z][i] / norm;
         }
         return result;
     }
     
     
     private double[][] vect_prod(double[] x1, double[] y1,double[] z1, double[] x2, double[] y2,double[] z2) {
         double[][] product = new double[3][x1.length];
         for (int i = 0; i < x1.length; i++) {
             product[X][i] = y1[i] * z2[i] - z1[i] * y2[i];
             product[Y][i] = z1[i] * x2[i] - x1[i] * z2[i];
             product[Z][i] = x1[i] * y2[i] - y1[i] * x2[i];
         }
         return product;
     }
     
     private double[] get2ndDim(double[][] twoDimArray, int secondDimIndex, int numElems) {
         double[] secondDim = new double[numElems];
         for (int i = 0; i < numElems; i++) {
             secondDim[i] = twoDimArray[i][secondDimIndex];
         }
         return secondDim;
     }
     
     private double[] spline(double[]x, double[] y, double[] t) {
         double[] v = new double[x.length];
         PolynomialSplineFunction splineFunction = SplineInterpolator.interpolate(x, y);
         for (int i = 0; i < v.length; i++) {
             v[i] = splineFunction.value(t[i]);
         }
         return v;
     }
 }
