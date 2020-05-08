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
 
 import com.bc.ceres.core.ProgressMonitor;
 
 import org.esa.beam.chris.operators.AuxDataFinder.TelemetryFiles;
 import org.esa.beam.chris.operators.CoordinateUtils.ViewAng;
 import org.esa.beam.chris.operators.GPSTime.GPSReader;
 import org.esa.beam.chris.operators.ImageCenterTime.ITCReader;
 import org.esa.beam.chris.operators.math.PolynomialSplineFunction;
 import org.esa.beam.chris.operators.math.SplineInterpolator;
 import org.esa.beam.chris.util.OpUtils;
 import org.esa.beam.dataio.chris.ChrisConstants;
 import org.esa.beam.framework.dataio.ProductIO;
 import org.esa.beam.framework.datamodel.Band;
 import org.esa.beam.framework.datamodel.PixelGeoCoding;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.datamodel.ProductData;
 import org.esa.beam.framework.gpf.Operator;
 import org.esa.beam.framework.gpf.OperatorException;
 import org.esa.beam.framework.gpf.Tile;
 import org.esa.beam.framework.gpf.annotations.Parameter;
 import org.esa.beam.framework.gpf.annotations.SourceProduct;
 import org.esa.beam.util.ProductUtils;
 
 import java.awt.Rectangle;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 public class TheRealThing extends Operator {
 
     public class ChrisInfo {
 
         int mode;
         double alt;
         double lat;
         double lon;
         int imageNumber;
     }
 
     private static final int SlowDown = 5; // Slowdown factor
 
     //Epoch for a reduced Julian Day (all JD values are substracted by this value). 
     //This way the calculations can be performed more efficiently.
     private static final double jd0 = Conversions.julianDate(2001, 0, 1);
 
     // there is a delay of 0.999s between the GPS time tag and the actual time of the reported position/velocity.
     private static final double delay = 0.999;
 
     // Variables used for array subscripting, so the code is more readable.
     private static final int X = 0;
     private static final int Y = 1;
     private static final int Z = 2;
     
     private static final int NUM_IMG = 5;
 
 
     public ChrisInfo info;
     ///////////////////////////////////////////////////////////
     @Parameter
     public File ictFile;
     @Parameter
     public File gpsFile;
     @SourceProduct
     public Product chrisProduct;
 
     private double[][][] igm;
 
     ///////////////////////////////////////////////////////////
     @Override
     public void initialize() throws OperatorException {
         fillInfo();
         // ICT
         ITCReader ictReader;
         try {
             ictReader = new ImageCenterTime.ITCReader(new FileInputStream(ictFile));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             throw new OperatorException(e);
         }
         double[] lastIctValues = ictReader.getLastIctValues();
         final ImageCenterTime lastImageCenterTime;
         final double dTgps = getDeltaGPS(chrisProduct);
         try {
             lastImageCenterTime = ImageCenterTime.create(lastIctValues, dTgps);
         } catch (IOException e) {
             throw new OperatorException(e);
         }
 
         // GPS
         GPSReader gpsReader;
         try {
             gpsReader = new GPSTime.GPSReader(new FileInputStream(gpsFile));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             throw new OperatorException(e);
         }
         List<String[]> gpsRecords = gpsReader.getReadRecords();
         List<GPSTime> gps;
         try {
             gps = GPSTime.create(gpsRecords, dTgps, delay);
         } catch (IOException e) {
             throw new OperatorException(e);
         }
 
         // GCP
         // TODO
 
         ChrisModeConstants mode = ChrisModeConstants.get(info.mode);
 
         //////////////////////////
         // Prepare Time Frames
         //////////////////////////
 
         // The last element of ict_njd corresponds to the acquisition setup time, that occurs 390s before the start of acquisition.
         final double acquisitionSetupTime = lastImageCenterTime.ict1 - (10.0 + 390.0) / Conversions.SECONDS_PER_DAY - jd0;
         double[] ict_njd = {
                 lastImageCenterTime.ict1 - jd0,
                 lastImageCenterTime.ict2 - jd0,
                 lastImageCenterTime.ict3 - jd0,
                 lastImageCenterTime.ict4 - jd0,
                 lastImageCenterTime.ict5 - jd0,
                 acquisitionSetupTime
         };
 
         double[] T_ict = Arrays.copyOfRange(ict_njd, 0, NUM_IMG);
 
         //---- Nos quedamos con todos los elementos de GPS menos el ultimo ----------------
         //----   ya q es donde se almacena la AngVel media, y perder un dato --------------
         //----   de GPS no es un problema. ------------------------------------------------
 
         // We are left with all elements but the last GPS
         // and q is where stores AngVel half, and losing data
         // GPS is not a problem.
         final int numGPS = gps.size() - 2;
         double[] gps_njd = new double[numGPS];
         for (int i = 0; i < gps_njd.length; i++) {
             gps_njd[i] = gps.get(i).jd - jd0;
         }
 
         // ---- Critical Times ---------------------------------------------
 
         double[] T_ini = new double[NUM_IMG]; // imaging start time (imaging lasts ~9.5s in every mode)
         double[] T_end = new double[NUM_IMG]; // imaging stop time
         for (int i = 0; i < T_ini.length; i++) {
             T_ini[i] = ict_njd[i] - (mode.getTimg() / 2.0) / Conversions.SECONDS_PER_DAY;
             T_end[i] = ict_njd[i] + (mode.getTimg() / 2.0) / Conversions.SECONDS_PER_DAY;
         }
 //        double T_i = ict_njd[0] - 10 / Conversions.SECONDS_PER_DAY; // "imaging mode" start time
 //        double T_e = ict_njd[4] + 10 / Conversions.SECONDS_PER_DAY; // "imaging mode" stop time
 
         // Searches the closest values in the telemetry to the Critical Times (just for plotting purposses)
         // skipped
 
         //---- determine per-Line Time Frame -----------------------------------
 
         // Time elapsed since imaging start at each image line
         double[] T_lin = new double[mode.getNLines()];
         for (int line = 0; line < T_lin.length; line++) {
             T_lin[line] = (line * mode.getDt() + mode.getTpl() / 2) / Conversions.SECONDS_PER_DAY;
             // +TpL/2 is added to set the time at the middle of the integration time, i.e. pixel center
         }
 
         double[][] T_img = new double[mode.getNLines()][NUM_IMG];
         for (int line = 0; line < mode.getNLines(); line++) {
             for (int img = 0; img < NUM_IMG; img++) {
                 T_img[line][img] = T_ini[img] + T_lin[line];
             }
         }
 
         double[] T = new double[1 + NUM_IMG * mode.getNLines()];
         T[0] = ict_njd[5];
         int Tindex = 1;
         for (int img = 0; img < NUM_IMG; img++) {
             for (int line = 0; line < mode.getNLines(); line++) {
                 T[Tindex] = T_img[line][img];
                 Tindex++;
             }
         }
 
         //double Tobs = T_e - T_i; // Duration of "imaging mode" for each image. (redundant, should be 20s)
 //        double[] Tobs2 = new double[T_end.length]; // Duration of actual imaging for each image
 //        for (int i = 0; i < Tobs2.length; i++) {
 //            Tobs2[i] = T_end[i] - T_ini[i];
 //        }
 
         // Set the indices of T that correspond to critical times (integration start and stop) for each image
 
         // The first element of T corresponds to the acquisition-setup time, so Tini[0] must skip element 0 of T
         int[] Tini = new int[NUM_IMG];
         int[] Tend = new int[NUM_IMG];
         for (int img = 0; img < NUM_IMG; img++) {
             Tini[img] = mode.getNLines() * img + 1;
             Tend[img] = Tini[img] + mode.getNLines() - 1;
         }
         final int Tfix = 0; // Index corresponding to the time of fixing the orbit
 
 
         // ========================================================================
         // ===                     Inertial Coordinates                         ===
         // ==v==v== ==v== Converts coordinates from ECEF to ECI ==v==v== ==v==v== =
 
         // Pos/Vel with Time from Telemetry
 
         double[][] eci = new double[gps.size()][6];
         for (int i = 0; i < gps.size(); i++) {
             GPSTime gpsTime = gps.get(i);
             // position and velocity is given in meters, 
             // we transform to km in order to keep the values smaller. from now all distances in Km
             double[] ecf = {
                     gpsTime.posX / 1000.0, gpsTime.posY / 1000.0, gpsTime.posZ / 1000.0,
                     gpsTime.velX / 1000.0, gpsTime.velY / 1000.0, gpsTime.velZ / 1000.0
             };
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
 
         double[] iR = new double[T.length];
         for (int i = 0; i < iR.length; i++) {
             iR[i] = Math.sqrt(iX[i] * iX[i] + iY[i] * iY[i] + iZ[i] * iZ[i]);
         }
 
         // ==v==v== Get Orbital Plane Vector ==================================================
         // ---- Calculates normal vector to orbital plane --------------------------
         double[][] Wop = vect_prod(iX, iY, iZ, iVX, iVY, iVZ);
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
 
         double[] gpsSecs = new double[gps.size()];
         double[] eciX = new double[gps.size()];
         double[] eciY = new double[gps.size()];
         double[] eciZ = new double[gps.size()];
         for (int i = 0; i < gps.size(); i++) {
             gpsSecs[i] = gps.get(i).secs;
             eciX[i] = eci[i][X];
             eciY[i] = eci[i][Y];
             eciZ[i] = eci[i][Z];
         }
         double[] AngVelRaw = CoordinateUtils.angVel(gpsSecs, eciX, eciY, eciZ);
         double[] AngVelRawSubset = Arrays.copyOfRange(AngVelRaw, 0, numGPS);
         SimpleSmoother smoother = new SimpleSmoother(5);
         double[] AngVel = new double[AngVelRawSubset.length];
         smoother.smooth(AngVelRawSubset, AngVel);
         double[] iAngVel = spline(gps_njd, AngVel, T);
 
         // ==v==v== Initialize Variables ======================================================
 
         //double[][] Range = new double[3][mode.getNLines()];
         //double[][] EjeYaw = new double[3][mode.getNLines()];
         //double[][] EjePitch = new double[3][mode.getNLines()];
         //double[][] EjeRoll = new double[3][mode.getNLines()];
         //double[][] SP = new double[3][mode.getNLines()];
         //double[][] SL = new double[3][mode.getNLines()];
         //double[] PitchAng = new double[mode.getNLines()];
         //double[] RollAng = new double[mode.getNLines()];
         //double[][] PitchAngR = new double[mode.getNCols()][mode.getNLines()];
         //double[][] RollAngR = new double[mode.getNCols()][mode.getNLines()];
         //double[] ObsAngAzi = new double[mode.getNLines()];
         //double[] ObsAngZen = new double[mode.getNLines()];
 
         // ===== Process the correct image ==========================================
 
         int img = info.imageNumber;
         System.out.println("Initiating calculation for image " + img);
 
         // ==v==v== Find the closest point in the Moving Target to the GCPs ==============
         // TODO
 
         // ---- Target Coordinates in ECI using per-Line Time -------------------
         double TgtAlt = info.alt / 1000;
         double[] TGTecf = new double[3];
         Conversions.wgsToEcef(info.lon, info.lat, TgtAlt, TGTecf);
 //            if ( use_GCP) { TODO
 //                Conversions.wgsToEcef(GCP0[3], GCP0[2], GCP0[4], TGTecf);
 //            }
         // Case with Moving Target for imaging time
         double[][] iTGT0 = new double[T.length][3];
         for (int i = 0; i < iTGT0.length; i++) {
             double gst = Conversions.jdToGST(T[i] + jd0);
             EcefEciConverter.ecefToEci(gst, TGTecf, iTGT0[i]);
         }
 
         // ==v==v== Rotates TGT to perform scanning ======================================
 
         double[] x = new double[mode.getNLines()];
         double[] y = new double[mode.getNLines()];
         double[] z = new double[mode.getNLines()];
         double[] angles = new double[mode.getNLines()];
         for (int i = 0; i < mode.getNLines(); i++) {
             x[i] = uW[Tini[img] + i][X];
             y[i] = uW[Tini[img] + i][Y];
             z[i] = uW[Tini[img] + i][Z];
             angles[i] = Math.pow(-1, img) * iAngVel[0] / SlowDown * (T_img[i][img] - T_ict[img]) * Conversions.SECONDS_PER_DAY;
         }
         Quaternion[] quaternions = Quaternion.createQuaternions(x, y, z, angles);
         for (int i = 0; i < mode.getNLines(); i++) {
             x[i] = iTGT0[Tini[img] + i][X];
             y[i] = iTGT0[Tini[img] + i][Y];
             z[i] = iTGT0[Tini[img] + i][Z];
         }
         QuaternionRotations.rotateVectors(quaternions, x, y, z);
         for (int i = 0; i < mode.getNLines(); i++) {
             iTGT0[Tini[img] + i][X] = x[i];
             iTGT0[Tini[img] + i][Y] = y[i];
             iTGT0[Tini[img] + i][Z] = z[i];
         }
 
         // Once GCP and TT are used iTGT0 will be subsetted to the corrected T, but in the nominal case iTGT0 matches already T
         double[][] iTGT = iTGT0;
 
         //==== Calculates View Angles ==============================================
 
 //            ViewAng[] viewAngs = new ViewAng[mode.getNLines()];
         double[][] viewRange = new double[3][mode.getNLines()];
         for (int i = 0; i < mode.getNLines(); i++) {
             double TgtX = iTGT[Tini[img] + i][X];
             double TgtY = iTGT[Tini[img] + i][Y];
             double TgtZ = iTGT[Tini[img] + i][Z];
             double SatX = iX[Tini[img] + i];
             double SatY = iY[Tini[img] + i];
             double SatZ = iZ[Tini[img] + i];
             double TgtLat = info.lat;
             ViewAng viewAng = CoordinateUtils.computeViewAng(TgtX, TgtY, TgtZ, SatX, SatY, SatZ, TgtLat);
 
             // ---- View.Rang[XYZ] is the vector pointing from TGT to SAT,
             // ----  but for the calculations it is nevessary the oposite one, therefore (-) appears.
             viewRange[X][i] = -viewAng.rangeX;
             viewRange[Y][i] = -viewAng.rangeY;
             viewRange[Z][i] = -viewAng.rangeZ;
 
             //ObsAngAzi[i] = viewAng.azi;
             //ObsAngZen[i] = viewAng.zen;
         }
 
         // Observation angles are not needed for the geometric correction but they are used for research. They are a by-product.
         // But ViewAngs provides also the range from the target to the satellite, which is needed later (Range, of course could be calculated independently).
 
         // ==== Satellite Rotation Axes ==============================================
 
         double[][] uEjeYaw = new double[3][mode.getNLines()];
         for (int i = 0; i < mode.getNLines(); i++) {
             uEjeYaw[X][i] = iX[Tini[img] + i];
             uEjeYaw[Y][i] = iY[Tini[img] + i];
             uEjeYaw[Z][i] = iZ[Tini[img] + i];
         }
         uEjeYaw = unit(uEjeYaw);
 //        for (int i = 0; i < mode.getNLines(); i++) {
 //            EjeYaw[X][i] = uEjeYaw[X][i];
 //            EjeYaw[Y][i] = uEjeYaw[Y][i];
 //            EjeYaw[Z][i] = uEjeYaw[Z][i];
 //        }
 
         double[][] uEjePitch = new double[3][mode.getNLines()];
         for (int i = 0; i < mode.getNLines(); i++) {
             uEjePitch[X][i] = uWop[X][Tini[img] + i];
             uEjePitch[Y][i] = uWop[Y][Tini[img] + i];
             uEjePitch[Z][i] = uWop[Z][Tini[img] + i];
         }
 //        for (int i = 0; i < mode.getNLines(); i++) {
 //            EjePitch[X][i] = uEjePitch[X][i] = uWop[X][Tini[img] + i];
 //            EjePitch[Y][i] = uEjePitch[Y][i] = uWop[Y][Tini[img] + i];
 //            EjePitch[Z][i] = uEjePitch[Z][i] = uWop[Z][Tini[img] + i];
 //        }
 
         double[][] uEjeRoll = vect_prod(uEjePitch, uEjeYaw);
 //        for (int i = 0; i < mode.getNLines(); i++) {
 //            EjeRoll[X][i] = uEjeRoll[X][i];
 //            EjeRoll[Y][i] = uEjeRoll[Y][i];
 //            EjeRoll[Z][i] = uEjeRoll[Z][i];
 //        }
 
         double[][] uRange = unit(viewRange);
 
         // ScanPlane:
         //double[][] uSP = new double[3][mode.getNLines()];
         //double[][] uSPr = new double[3][mode.getNLines() * mode.getNCols()];
 
         // SightLine:
         //double[][] uSL = new double[3][mode.getNLines()];
         //double[][] uSLr = new double[3][mode.getNLines() * mode.getNCols()];
 
         // RollSign:
         int[] uRollSign = new int[mode.getNLines()];
         //int[] uRollSignR = new int[mode.getNLines() * mode.getNCols()];
 
         double[][] uSP = vect_prod(uRange, uEjePitch);
         double[][] uSL = vect_prod(uEjePitch, uSP);
         double[][] uRoll = unit(vect_prod(uSL, uRange));
         for (int i = 0; i < mode.getNLines(); i++) {
             double total = 0;
             total += uRoll[X][i] / uSP[X][i];
             total += uRoll[Y][i] / uSP[Y][i];
             total += uRoll[Z][i] / uSP[Z][i];
             uRollSign[i] = (int) Math.signum(total);
         }
 
 //        for (int i = 0; i < mode.getNLines(); i++) {
 //            SP[X][i] = uSP[X][i];
 //            SP[Y][i] = uSP[Y][i];
 //            SP[Z][i] = uSP[Z][i];
 //
 //            SL[X][i] = uSL[X][i];
 //            SL[Y][i] = uSL[Y][i];
 //            SL[Z][i] = uSL[Z][i];
 //        }
 
         double[] uPitchAng = new double[mode.getNLines()];
         double[] uRollAng = new double[mode.getNLines()];
         for (int i = 0; i < mode.getNLines(); i++) {
             uPitchAng[i] = Math.PI / 2 - CoordinateUtils.vectAngle(uSP[X][i], uSP[Y][i], uSP[Z][i],
                                                                    uEjeYaw[X][i], uEjeYaw[Y][i], uEjeYaw[Z][i]);
             uRollAng[i] = uRollSign[i] * CoordinateUtils.vectAngle(uSL[X][i], uSL[Y][i], uSL[Z][i],
                                                                    uRange[X][i], uRange[Y][i], uRange[Z][i]);
             // Stores the results for each image
             //PitchAng[i] = uPitchAng[i];
             //RollAng[i] = uRollAng[i];
         }
 
         // ==== Rotate the Line of Sight and intercept with Earth ==============================================
 
         System.out.println("Starting model-IGM generation");
 
         double[] ixSubset = new double[mode.getNLines()];
         double[] iySubset = new double[mode.getNLines()];
         double[] izSubset = new double[mode.getNLines()];
         double[] timeSubset = new double[mode.getNLines()];
         for (int i = 0; i < timeSubset.length; i++) {
             ixSubset[i] = iX[Tini[img] + i];
             iySubset[i] = iY[Tini[img] + i];
             izSubset[i] = iZ[Tini[img] + i];
             timeSubset[i] = T[Tini[img] + i];
         }
         igm = ProbaIgmDoer.doIgmJava(mode.getNLines(), mode.getNCols(), mode.getFov(), mode.getIfov(), 
                                      uEjePitch, uPitchAng,
                                      uEjeRoll, uRollAng, 
                                      uEjeYaw, 
                                      TgtAlt, 
                                      ixSubset, iySubset, izSubset, 
                                      timeSubset, info.mode);
 
         final int width = chrisProduct.getSceneRasterWidth();
         final int height = chrisProduct.getSceneRasterHeight();
         Product targetProduct = new Product("chris_geo_corrected", "foo", width, height);
         targetProduct.addBand("latitude", ProductData.TYPE_FLOAT64);
         targetProduct.addBand("longitude", ProductData.TYPE_FLOAT64);
         String[] bandNames = chrisProduct.getBandNames();
         for (String bandName : bandNames) {
             Band targetBand = ProductUtils.copyBand(bandName, chrisProduct, targetProduct);
             targetBand.setSourceImage(chrisProduct.getBand(bandName).getSourceImage());
         }
         setTargetProduct(targetProduct);
     }
 
     @Override
     public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {
         int index = 0;
        if (targetBand.getName().equals("latitude")) {
             index = 1;
         }
         Rectangle rectangle = targetTile.getRectangle();
         for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
             for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
                 targetTile.setSample(x, y, igm[index][y][x]);
             }
             checkForCancelation(pm);
         }
     }
 
     private void fillInfo() {
         info = new ChrisInfo();
         info.mode = OpUtils.getAnnotationInt(chrisProduct, ChrisConstants.ATTR_NAME_CHRIS_MODE);
         info.lat = OpUtils.getAnnotationDouble(chrisProduct, ChrisConstants.ATTR_NAME_TARGET_LAT);
         info.lon = OpUtils.getAnnotationDouble(chrisProduct, ChrisConstants.ATTR_NAME_TARGET_LON);
         info.alt = OpUtils.getAnnotationDouble(chrisProduct, ChrisConstants.ATTR_NAME_TARGET_ALT);
         String image = OpUtils.getAnnotationString(chrisProduct, ChrisConstants.ATTR_NAME_IMAGE_NUMBER);
         info.imageNumber = Integer.valueOf(image.substring(0, 1)) - 1;
     }
 
     private double[][] unit(double[][] vec) {
         double[][] result = new double[vec.length][vec[0].length];
         for (int i = 0; i < vec[0].length; i++) {
             double norm = Math.sqrt(vec[X][i] * vec[X][i] + vec[Y][i] * vec[Y][i] + vec[Z][i] * vec[Z][i]);
             result[X][i] = vec[X][i] / norm;
             result[Y][i] = vec[Y][i] / norm;
             result[Z][i] = vec[Z][i] / norm;
         }
         return result;
     }
 
     private double[][] vect_prod(double[] x1, double[] y1, double[] z1, double[] x2, double[] y2, double[] z2) {
         double[][] product = new double[3][x1.length];
         for (int i = 0; i < x1.length; i++) {
             product[X][i] = y1[i] * z2[i] - z1[i] * y2[i];
             product[Y][i] = z1[i] * x2[i] - x1[i] * z2[i];
             product[Z][i] = x1[i] * y2[i] - y1[i] * x2[i];
         }
         return product;
     }
 
     private double[][] vect_prod(double[][] a1, double[][] a2) {
         double[][] product = new double[3][a1[0].length];
         for (int i = 0; i < a1[0].length; i++) {
             product[X][i] = a1[Y][i] * a2[Z][i] - a1[Z][i] * a2[Y][i];
             product[Y][i] = a1[Z][i] * a2[X][i] - a1[X][i] * a2[Z][i];
             product[Z][i] = a1[X][i] * a2[Y][i] - a1[Y][i] * a2[X][i];
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
 
     private double[] spline(double[] x, double[] y, double[] t) {
         double[] v = new double[t.length];
         PolynomialSplineFunction splineFunction = SplineInterpolator.interpolate(x, y);
         for (int i = 0; i < v.length; i++) {
             v[i] = splineFunction.value(t[i]);
         }
         return v;
     }
 
     public static void main(String[] args) throws IOException {
 
         File baseDir = new File("/home/marcoz/EOData/Chris/geoc/");
         File productFile = new File(baseDir, "CHRIS_AM_080805_A348_41_NR.dim");
         doCorrectFile(productFile);
     }
     
     public static void doCorrectFile(File productFile) throws IOException {
         File baseDir = productFile.getParentFile();
         TheRealThing thing = new TheRealThing();
         Product sourceProduct = ProductIO.readProduct(productFile, null);
         thing.chrisProduct = sourceProduct;
         TelemetryFiles telemetryFiles = AuxDataFinder.findTelemetryFiles(sourceProduct, baseDir, baseDir);
         thing.gpsFile = telemetryFiles.gpsFile;
         thing.ictFile = telemetryFiles.telemetryFile;
         Product targetProduct = thing.getTargetProduct();
         File targetFile = new File(baseDir, sourceProduct.getName()+"_GEOCOR.dim");
         ProductIO.writeProduct(targetProduct, targetFile.getAbsolutePath(), null);
     }
 
     private static double getDeltaGPS(Product product) {
         final Date date = product.getStartTime().getAsDate();
         final double mjd = Conversions.dateToMJD(date);
 
         try {
             return TimeCalculator.getInstance().deltaGPS(mjd);
         } catch (IOException e) {
             throw new OperatorException(e);
         }
     }
 
 }
