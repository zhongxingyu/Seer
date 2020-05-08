 package org.esa.beam.chris.operators;
 
 import org.esa.beam.chris.util.math.internal.Pow;
 import org.esa.beam.framework.datamodel.RationalFunctionModel;
 
 import java.util.Arrays;
 import java.util.List;
 
 class GeometryCalculator {
 
     private static final int SLOW_DOWN_FACTOR = 5;
     private static final double JD2001 = TimeConverter.julianDate(2001, 0, 1);
 
     // constants used for array indexes, so the code is more readable.
     private static final int X = 0;
     private static final int Y = 1;
     private static final int Z = 2;
     // five images for each acquisition
     private static final int IMAGE_COUNT = 5;
 
     final AcquisitionInfo acquisitionInfo;
 
     final double[][] lats;
     final double[][] lons;
     final double[][] vaas;
     final double[][] vzas;
 
     final double[][] pitchAngles;
     final double[][] rollAngles;
 
     public GeometryCalculator(AcquisitionInfo acquisitionInfo) {
         this.acquisitionInfo = acquisitionInfo;
 
         final ModeCharacteristics modeCharacteristics = ModeCharacteristics.get(acquisitionInfo.getMode());
         final int rowCount = modeCharacteristics.getRowCount();
         final int colCount = modeCharacteristics.getColCount();
 
         lats = new double[rowCount][colCount];
         lons = new double[rowCount][colCount];
         vaas = new double[rowCount][colCount];
         vzas = new double[rowCount][colCount];
 
         pitchAngles = new double[rowCount][colCount];
         rollAngles = new double[rowCount][colCount];
     }
 
     void calculate(IctDataRecord ictData, List<GpsDataRecord> gpsData, GCP[] gcps, boolean useTargetAltitude) {
         //////////////////////////
         // Prepare Time Frames
         //////////////////////////
 
         // The last element of ict_njd corresponds to the acquisition setup time, that occurs 390s before the start of acquisition.
         final ModeCharacteristics modeCharacteristics = ModeCharacteristics.get(acquisitionInfo.getMode());
         final double acquisitionSetupTime = ictData.ict1 - (10.0 + 390.0) / TimeConverter.SECONDS_PER_DAY - JD2001;
         double[] ict_njd = {
                 ictData.ict1 - JD2001,
                 ictData.ict2 - JD2001,
                 ictData.ict3 - JD2001,
                 ictData.ict4 - JD2001,
                 ictData.ict5 - JD2001,
                 acquisitionSetupTime
         };
 
         double[] T_ict = Arrays.copyOfRange(ict_njd, 0, IMAGE_COUNT);
 
         //---- Nos quedamos con todos los elementos de GPS menos el ultimo ----------------
         //----   ya q es donde se almacena la AngVel media, y perder un dato --------------
         //----   de GPS no es un problema. ------------------------------------------------
 
         // We are left with all elements but the last GPS
         // and q is where stores AngVel half, and losing data
         // GPS is not a problem.
         final int numGPS = gpsData.size() - 2;
         double[] gps_njd = new double[numGPS];
         for (int i = 0; i < gps_njd.length; i++) {
             gps_njd[i] = gpsData.get(i).jd - JD2001;
         }
 
         // ---- Critical Times ---------------------------------------------
 
         double[] T_ini = new double[IMAGE_COUNT]; // imaging start time (imaging lasts ~9.5s in every mode)
         double[] T_end = new double[IMAGE_COUNT]; // imaging stop time
         for (int i = 0; i < T_ini.length; i++) {
             T_ini[i] = ict_njd[i] - (modeCharacteristics.getTimePerImage() / 2.0) / TimeConverter.SECONDS_PER_DAY;
             T_end[i] = ict_njd[i] + (modeCharacteristics.getTimePerImage() / 2.0) / TimeConverter.SECONDS_PER_DAY;
         }
 //        double T_i = ict_njd[0] - 10 / Conversions.SECONDS_PER_DAY; // "imaging mode" start time
 //        double T_e = ict_njd[4] + 10 / Conversions.SECONDS_PER_DAY; // "imaging mode" stop time
 
         // Searches the closest values in the telemetry to the Critical Times (just for plotting purposses)
         // skipped
 
         //---- determine per-Line Time Frame -----------------------------------
 
         // Time elapsed since imaging start at each image line
         double[] T_lin = new double[modeCharacteristics.getRowCount()];
         for (int line = 0; line < T_lin.length; line++) {
             T_lin[line] = (line * modeCharacteristics.getTotalTimePerLine() + modeCharacteristics.getIntegrationTimePerLine() / 2) / TimeConverter.SECONDS_PER_DAY;
             // +TpL/2 is added to set the time at the middle of the integration time, i.e. pixel center
         }
 
         double[][] T_img = new double[modeCharacteristics.getRowCount()][IMAGE_COUNT];
         for (int line = 0; line < modeCharacteristics.getRowCount(); line++) {
             for (int img = 0; img < IMAGE_COUNT; img++) {
                 T_img[line][img] = T_ini[img] + T_lin[line];
             }
         }
 
         double[] T = new double[1 + IMAGE_COUNT * modeCharacteristics.getRowCount()];
         T[0] = ict_njd[5];
         int Tindex = 1;
         for (int img = 0; img < IMAGE_COUNT; img++) {
             for (int line = 0; line < modeCharacteristics.getRowCount(); line++) {
                 T[Tindex] = T_img[line][img];
                 Tindex++;
             }
         }
 
         // Set the indices of T that correspond to critical times (integration start and stop) for each image
 
         // The first element of T corresponds to the acquisition-setup time, so Tini[0] must skip element 0 of T
         int[] Tini = new int[IMAGE_COUNT];
         int[] Tend = new int[IMAGE_COUNT];
         for (int img = 0; img < IMAGE_COUNT; img++) {
             Tini[img] = modeCharacteristics.getRowCount() * img + 1;
             Tend[img] = Tini[img] + modeCharacteristics.getRowCount() - 1;
         }
         final int Tfix = 0; // Index corresponding to the time of fixing the orbit
 
 
         // ========================================================================
         // ===                     Inertial Coordinates                         ===
         // ==v==v== ==v== Converts coordinates from ECEF to ECI ==v==v== ==v==v== =
 
         // Pos/Vel with Time from Telemetry
 
         double[][] eci = new double[gpsData.size()][6];
         for (int i = 0; i < gpsData.size(); i++) {
             GpsDataRecord gpsDataRecord = gpsData.get(i);
             // position and velocity is given in meters,
             // we transform to km in order to keep the values smaller. from now all distances in Km
             double[] ecef = {
                     gpsDataRecord.posX / 1000.0, gpsDataRecord.posY / 1000.0, gpsDataRecord.posZ / 1000.0,
                     gpsDataRecord.velX / 1000.0, gpsDataRecord.velY / 1000.0, gpsDataRecord.velZ / 1000.0
             };
             double gst = TimeConverter.jdToGST(gpsDataRecord.jd);
             CoordinateConverter.ecefToEci(gst, ecef, eci[i]);
         }
 
         // =======================================================================
         // ===                  Data to per-Line Time Frame                    ===
         // =======================================================================
         // ---- Interpolate GPS ECI position/velocity to per-line time -------
 
         double[] iX = interpolate(gps_njd, get2ndDim(eci, 0, numGPS), T);
         double[] iY = interpolate(gps_njd, get2ndDim(eci, 1, numGPS), T);
         double[] iZ = interpolate(gps_njd, get2ndDim(eci, 2, numGPS), T);
         double[] iVX = interpolate(gps_njd, get2ndDim(eci, 3, numGPS), T);
         double[] iVY = interpolate(gps_njd, get2ndDim(eci, 4, numGPS), T);
         double[] iVZ = interpolate(gps_njd, get2ndDim(eci, 5, numGPS), T);
 
         double[] iR = new double[T.length];
         for (int i = 0; i < iR.length; i++) {
             iR[i] = Math.sqrt(iX[i] * iX[i] + iY[i] * iY[i] + iZ[i] * iZ[i]);
         }
 
         // ==v==v== Get Orbital Plane Vector ==================================================
         // ---- Calculates normal vector to orbital plane --------------------------
         double[][] uWop = toUnitVectors(vectorProducts(iX, iY, iZ, iVX, iVY, iVZ));
 
         // Fixes orbital plane vector to the corresponding point on earth at the time of acquistion setup
         double gst_opv = TimeConverter.jdToGST(T[Tfix] + JD2001);
         double[] uWecf = CoordinateConverter.eciToEcef(gst_opv, uWop[Tfix], new double[3]);
 
         double[][] uW = new double[T.length][3];
         for (int i = 0; i < T.length; i++) {
             double gst = TimeConverter.jdToGST(T[i] + JD2001);
             CoordinateConverter.ecefToEci(gst, uWecf, uW[i]);
         }
 
         // ==v==v== Get Angular Velocity ======================================================
         // Angular velocity is not really used in the model, except the AngVel at orbit fixation time (iAngVel[0])
 
         final double[] gpsSecs = new double[gpsData.size()];
         final double[] eciX = new double[gpsData.size()];
         final double[] eciY = new double[gpsData.size()];
         final double[] eciZ = new double[gpsData.size()];
         for (int i = 0; i < gpsData.size(); i++) {
             gpsSecs[i] = gpsData.get(i).secs;
             eciX[i] = eci[i][X];
             eciY[i] = eci[i][Y];
             eciZ[i] = eci[i][Z];
         }
         final double[] AngVelRaw = VectorMath.angularVelocity(gpsSecs, eciX, eciY, eciZ);
         final double[] AngVelRawSubset = Arrays.copyOfRange(AngVelRaw, 0, numGPS);
         SimpleSmoother smoother = new SimpleSmoother(5);
         final double[] AngVel = new double[AngVelRawSubset.length];
         smoother.smooth(AngVelRawSubset, AngVel);
         double[] iAngVel = interpolate(gps_njd, AngVel, T);
 
         // ===== Process the correct image ==========================================
 
         final int img = acquisitionInfo.getChronologicalImageNumber();
 
         // ---- Target Coordinates in ECI using per-Line Time -------------------
        final double targetAltitude = acquisitionInfo.getTargetAlt();
         final double[] TGTecf = CoordinateConverter.wgsToEcef(acquisitionInfo.getTargetLon(),
                                                               acquisitionInfo.getTargetLat(), targetAltitude,
                                                               new double[3]);
 
         // Case with Moving Target for imaging time
         double[][] iTGT0 = new double[T.length][3];
         for (int i = 0; i < iTGT0.length; i++) {
             double gst = TimeConverter.jdToGST(T[i] + JD2001);
             CoordinateConverter.ecefToEci(gst, TGTecf, iTGT0[i]);
         }
 
         // ==v==v== Rotates TGT to perform scanning ======================================
 
         for (int i = 0; i < modeCharacteristics.getRowCount(); i++) {
             final double x = uW[Tini[img] + i][X];
             final double y = uW[Tini[img] + i][Y];
             final double z = uW[Tini[img] + i][Z];
             final double a = Math.pow(-1.0,
                                       img) * iAngVel[0] / SLOW_DOWN_FACTOR * (T_img[i][img] - T_ict[img]) * TimeConverter.SECONDS_PER_DAY;
             Quaternion.createQuaternion(x, y, z, a).transform(iTGT0[Tini[img] + i], iTGT0[Tini[img] + i]);
         }
 
         final int bestGcpIndex = findBestGCP(modeCharacteristics.getColCount() / 2,
                                              modeCharacteristics.getRowCount() / 2, gcps);
 
         if (bestGcpIndex != -1) {
             final GCP gcp = gcps[bestGcpIndex];
 
             /**
              * 0. Calculate GCP position in ECEF
              */
             final double[] GCP_ecf = new double[3];
             CoordinateConverter.wgsToEcef(gcp.getLon(), gcp.getLat(), gcp.getAlt(), GCP_ecf);
             final double[] wgs = CoordinateConverter.ecefToWgs(GCP_ecf[0], GCP_ecf[1], GCP_ecf[2], new double[3]);
             System.out.println("lon = " + wgs[X]);
             System.out.println("lat = " + wgs[Y]);
 
             /**
              * 1. Transform nominal Moving Target to ECEF
              */
             // Transform Moving Target to ECF in order to find the point closest to GCP0
             // iTGT0_ecf = eci2ecf(T+jd0, iTGT0[X,*], iTGT0[Y,*], iTGT0[Z,*])
             final double[][] iTGT0_ecf = new double[iTGT0.length][3];
             for (int i = 0; i < iTGT0.length; i++) {
                 final double gst = TimeConverter.jdToGST(T[i] + JD2001);
                 CoordinateConverter.eciToEcef(gst, iTGT0[i], iTGT0_ecf[i]);
             }
 
             /**
              * 2. Find time offset dT
              */
             double minDiff = Double.MAX_VALUE;
             double tmin = Double.MAX_VALUE;
             int wmin = -1;
             for (int i = Tini[img]; i <= Tend[img]; i++) {
                 double[] pos = iTGT0_ecf[i];
                 final double diff = Math.sqrt(
                         Pow.pow2(pos[X] - GCP_ecf[X]) + Pow.pow2(pos[Y] - GCP_ecf[Y]) + Pow.pow2(
                                 pos[Z] - GCP_ecf[Z]));
                 if (diff < minDiff) {
                     minDiff = diff;
                     tmin = T[i];
                     wmin = i; // This is necessary in order to recompute the times more easily
                 }
             }
 
             final double dY;
             if (acquisitionInfo.isBackscanning()) {
                 dY = (wmin % modeCharacteristics.getRowCount()) - (modeCharacteristics.getRowCount() - gcp.getY() + 0.5);
             } else {
                 dY = (wmin % modeCharacteristics.getRowCount()) - (gcp.getY() + 0.5);
             }
             final double dT = (dY * modeCharacteristics.getTotalTimePerLine()) / TimeConverter.SECONDS_PER_DAY;
             System.out.println("dT = " + dT);
 
             /**
              * 3. Update T[]: add dT to all times in T[].
              */
             for (int i = 0; i < T.length; i++) {
                 final double newT = T[i] + dT; //tmin + (mode.getDt() * (i - wmin)) / TimeConverter.SECONDS_PER_DAY;
                 T[i] = newT;
             }
             for (int line = 0; line < modeCharacteristics.getRowCount(); line++) {
                 T_img[line][img] += dT;
             }
 
             /**
              * 4. Calculate GCP position in ECI for updated times T[]
              */
             // T_GCP = T[wmin]			; Assigns the acquisition time to the GCP
             // GCP_eci = ecf2eci(T+jd0, GCP_ecf.X, GCP_ecf.Y, GCP_ecf.Z, units = GCP_ecf.units)	; Transform GCP coords to ECI for every time in the acquisition
             final double[][] GCP_eci = new double[T.length][3];
             for (int i = 0; i < T.length; i++) {
                 final double gst = TimeConverter.jdToGST(T[i] + JD2001);
                 CoordinateConverter.ecefToEci(gst, GCP_ecf, GCP_eci[i]);
             }
 
 //// COPIED FROM ABOVE
 
             /**
              * 5. Interpolate satellite positions & velocities for updated times T[]
              */
             iX = interpolate(gps_njd, get2ndDim(eci, 0, numGPS), T);
             iY = interpolate(gps_njd, get2ndDim(eci, 1, numGPS), T);
             iZ = interpolate(gps_njd, get2ndDim(eci, 2, numGPS), T);
             iVX = interpolate(gps_njd, get2ndDim(eci, 3, numGPS), T);
             iVY = interpolate(gps_njd, get2ndDim(eci, 4, numGPS), T);
             iVZ = interpolate(gps_njd, get2ndDim(eci, 5, numGPS), T);
 
             iR = new double[T.length];
             for (int i = 0; i < iR.length; i++) {
                 iR[i] = Math.sqrt(iX[i] * iX[i] + iY[i] * iY[i] + iZ[i] * iZ[i]);
             }
 
             // ==v==v== Get Orbital Plane Vector ==================================================
             // ---- Calculates normal vector to orbital plane --------------------------
             uWop = toUnitVectors(vectorProducts(iX, iY, iZ, iVX, iVY, iVZ));
 
             // Fixes orbital plane vector to the corresponding point on earth at the time of acquistion setup
             gst_opv = TimeConverter.jdToGST(T[Tfix] + JD2001);
             uWecf = new double[3];
             CoordinateConverter.eciToEcef(gst_opv, uWop[Tfix], uWecf);
 
             uW = new double[T.length][3];
             for (int i = 0; i < T.length; i++) {
                 double gst = TimeConverter.jdToGST(T[i] + JD2001);
                 CoordinateConverter.ecefToEci(gst, uWecf, uW[i]);
             }
 
             // ==v==v== Get Angular Velocity ======================================================
             // Angular velocity is not really used in the model, except the AngVel at orbit fixation time (iAngVel[0])
 
             iAngVel = interpolate(gps_njd, AngVel, T);
 ////  EVOBA MORF DEIPOC
 
             for (int i = 0; i < modeCharacteristics.getRowCount(); i++) {
                 final double x = uW[Tini[img] + i][X];
                 final double y = uW[Tini[img] + i][Y];
                 final double z = uW[Tini[img] + i][Z];
                 final double a = Math.pow(-1.0,
                                           img) * iAngVel[0] / SLOW_DOWN_FACTOR * (T_img[i][img] - tmin) * TimeConverter.SECONDS_PER_DAY;
                 Quaternion.createQuaternion(x, y, z, a).transform(GCP_eci[Tini[img] + i], GCP_eci[Tini[img] + i]);
                 System.out.println("i = " + i);
                 final double gst = TimeConverter.jdToGST(T[Tini[img] + i] + JD2001);
                 final double[] ecef = new double[3];
                 CoordinateConverter.eciToEcef(gst, GCP_eci[Tini[img] + i], ecef);
                 final double[] p = CoordinateConverter.ecefToWgs(ecef[0], ecef[1], ecef[2], new double[3]);
                 System.out.println("lon = " + p[X]);
                 System.out.println("lat = " + p[Y]);
                 System.out.println();
             }
 
             iTGT0 = GCP_eci;
         } // gcpCount > 0;
 
         // Once GCP and TT are used iTGT0 will be subsetted to the corrected T, but in the nominal case iTGT0 matches already T
         double[][] iTGT = iTGT0;
 
         // Determine the roll offset due to GCP not being in the middle of the CCD
         // IF info.Mode NE 5 THEN nC2 = nCols/2 ELSE nC2 = nCols-1		; Determine the column number of the middle of the CCD
         // dRoll = (nC2-GCP[X])*IFOV									; calculates the IFOV angle difference from GCP0's pixel column to the image central pixel (the nominal target)
         double dRoll = 0.0;
         if (bestGcpIndex != -1) {
             final int nC2;
             if (acquisitionInfo.getMode() != 5) {
                 nC2 = modeCharacteristics.getColCount() / 2;
             } else {
                 nC2 = modeCharacteristics.getColCount() - 1;
             }
             final GCP gcp = gcps[bestGcpIndex];
             dRoll = (nC2 - gcp.getX()) * modeCharacteristics.getIfov();
         }
 
         //==== Calculates View Angles ==============================================
 
 //            ViewingGeometry[] viewAngs = new ViewingGeometry[mode.getNLines()];
         double[][] viewRange = new double[modeCharacteristics.getRowCount()][3];
         for (int i = 0; i < modeCharacteristics.getRowCount(); i++) {
             double TgtX = iTGT[Tini[img] + i][X];
             double TgtY = iTGT[Tini[img] + i][Y];
             double TgtZ = iTGT[Tini[img] + i][Z];
             double SatX = iX[Tini[img] + i];
             double SatY = iY[Tini[img] + i];
             double SatZ = iZ[Tini[img] + i];
             ViewingGeometry viewingGeometry = ViewingGeometry.create(TgtX, TgtY, TgtZ, SatX, SatY, SatZ);
 
             viewRange[i][X] = viewingGeometry.x;
             viewRange[i][Y] = viewingGeometry.y;
             viewRange[i][Z] = viewingGeometry.z;
         }
 
         // Observation angles are not needed for the geometric correction but they are used for research. They are a by-product.
         // But ViewAngs provides also the range from the target to the satellite, which is needed later (Range, of course could be calculated independently).
 
         // ==== Satellite Rotation Axes ==============================================
 
         double[][] yawAxes = new double[modeCharacteristics.getRowCount()][3];
         for (int i = 0; i < modeCharacteristics.getRowCount(); i++) {
             yawAxes[i][X] = iX[Tini[img] + i];
             yawAxes[i][Y] = iY[Tini[img] + i];
             yawAxes[i][Z] = iZ[Tini[img] + i];
         }
         yawAxes = toUnitVectors(yawAxes);
 
         double[][] pitchAxes = new double[modeCharacteristics.getRowCount()][3];
         for (int i = 0; i < modeCharacteristics.getRowCount(); i++) {
             pitchAxes[i][X] = uWop[Tini[img] + i][X];
             pitchAxes[i][Y] = uWop[Tini[img] + i][Y];
             pitchAxes[i][Z] = uWop[Tini[img] + i][Z];
         }
 
         double[][] rollAxes = vectorProducts(pitchAxes, yawAxes, new double[modeCharacteristics.getRowCount()][3]);
 
         double[][] uRange = toUnitVectors(viewRange);
 
 
         // RollSign:
         int[] uRollSign = new int[modeCharacteristics.getRowCount()];
 
         double[][] uSP = vectorProducts(uRange, pitchAxes, new double[modeCharacteristics.getRowCount()][3]);
         double[][] uSL = vectorProducts(pitchAxes, uSP, new double[modeCharacteristics.getRowCount()][3]);
         double[][] uRoll = toUnitVectors(
                 vectorProducts(uSL, uRange, new double[modeCharacteristics.getRowCount()][3]));
         for (int i = 0; i < modeCharacteristics.getRowCount(); i++) {
             double total = 0;
             total += uRoll[i][X] / uSP[i][X];
             total += uRoll[i][Y] / uSP[i][Y];
             total += uRoll[i][Z] / uSP[i][Z];
             uRollSign[i] = (int) Math.signum(total);
         }
 
         double[] centerPitchAngles = new double[modeCharacteristics.getRowCount()];
         double[] centerRollAngles = new double[modeCharacteristics.getRowCount()];
         System.out.println("dRoll = " + dRoll);
         for (int i = 0; i < modeCharacteristics.getRowCount(); i++) {
             centerPitchAngles[i] = Math.PI / 2.0 - VectorMath.angle(uSP[i][X],
                                                                     uSP[i][Y],
                                                                     uSP[i][Z],
                                                                     yawAxes[i][X],
                                                                     yawAxes[i][Y],
                                                                     yawAxes[i][Z]);
             centerRollAngles[i] = uRollSign[i] * VectorMath.angle(uSL[i][X],
                                                                   uSL[i][Y],
                                                                   uSL[i][Z],
                                                                   uRange[i][X],
                                                                   uRange[i][Y],
                                                                   uRange[i][Z]);
             centerRollAngles[i] += dRoll;
         }
 
         // ==== Rotate the Line of Sight and intercept with Earth ==============================================
 
         double[] ixSubset = new double[modeCharacteristics.getRowCount()];
         double[] iySubset = new double[modeCharacteristics.getRowCount()];
         double[] izSubset = new double[modeCharacteristics.getRowCount()];
         double[] timeSubset = new double[modeCharacteristics.getRowCount()];
         for (int i = 0; i < timeSubset.length; i++) {
             ixSubset[i] = iX[Tini[img] + i];
             iySubset[i] = iY[Tini[img] + i];
             izSubset[i] = iZ[Tini[img] + i];
             timeSubset[i] = T[Tini[img] + i];
         }
 
         calculatePitchAndRollAngles(acquisitionInfo.getMode(),
                                     modeCharacteristics.getFov(),
                                     modeCharacteristics.getIfov(), centerPitchAngles, centerRollAngles,
                                     pitchAngles,
                                     rollAngles);
 
         // a. if there ar more than 2 GCPs we can calculate deltas for the pointing angle
         if (gcps.length > 2) {
             refinePitchAndRollAngles(gcps, timeSubset, ixSubset, iySubset, izSubset, pitchAxes, yawAxes,
                                      pitchAngles, rollAngles);
         }
 
 
         final PositionCalculator positionCalculator = new PositionCalculator(
                 useTargetAltitude ? targetAltitude : 0.0);
         positionCalculator.calculatePositions(
                 timeSubset, ixSubset, iySubset, izSubset, pitchAxes, rollAxes, yawAxes, pitchAngles,
                 rollAngles,
                 lons,
                 lats,
                 vaas,
                 vzas);
     }
 
     final double getLon(int y, int x) {
         return lons[y][x];
     }
 
     final double getLat(int y, int x) {
         return lats[y][x];
     }
 
     final double getVaa(int y, int x) {
         return vaas[y][x];
     }
 
     final double getVza(int y, int x) {
         return vzas[y][x];
     }
 
     final double getPitchAngle(int y, int x) {
         return pitchAngles[y][x];
     }
 
     final double getRollAngle(int y, int x) {
         return rollAngles[y][x];
     }
 
     private static double[] toUnitVector(double[] vector) {
         final double norm = Math.sqrt(vector[X] * vector[X] + vector[Y] * vector[Y] + vector[Z] * vector[Z]);
         vector[X] /= norm;
         vector[Y] /= norm;
         vector[Z] /= norm;
         return vector;
     }
 
     private static double[][] toUnitVectors(double[][] vectors) {
         for (final double[] vector : vectors) {
             toUnitVector(vector);
         }
         return vectors;
     }
 
     private static double[] vectorProduct(double[] u, double[] v, double[] w) {
         w[X] = u[Y] * v[Z] - u[Z] * v[Y];
         w[Y] = u[Z] * v[X] - u[X] * v[Z];
         w[Z] = u[X] * v[Y] - u[Y] * v[X];
         return w;
     }
 
     private static double[][] vectorProducts(double[][] u, double[][] v, double[][] w) {
         for (int i = 0; i < u.length; i++) {
             vectorProduct(u[i], v[i], w[i]);
         }
         return w;
     }
 
     private static double[][] vectorProducts(double[] x, double[] y, double[] z, double[] u, double[] v, double[] w) {
         final double[][] products = new double[x.length][3];
         for (int i = 0; i < x.length; i++) {
             final double[] product = products[i];
             product[X] = y[i] * w[i] - z[i] * v[i];
             product[Y] = z[i] * u[i] - x[i] * w[i];
             product[Z] = x[i] * v[i] - y[i] * u[i];
         }
         return products;
     }
 
     private static double[] get2ndDim(double[][] twoDimArray, int secondDimIndex, int numElems) {
         double[] secondDim = new double[numElems];
         for (int i = 0; i < numElems; i++) {
             secondDim[i] = twoDimArray[i][secondDimIndex];
         }
         return secondDim;
     }
 
     private static void refinePitchAndRollAngles(GCP[] gcps, double[] timeSubset, double[] ixSubset, double[] iySubset,
                                                  double[] izSubset,
                                                  double[][] pitchAxes, double[][] yawAxes, double[][] pitchAngles,
                                                  double[][] rollAngles) {
         final int gcpCount = gcps.length;
         final double[] x = new double[gcpCount];
         final double[] y = new double[gcpCount];
         final double[] deltaPitch = new double[gcpCount];
         final double[] deltaRoll = new double[gcpCount];
         final double[] pitchRoll = new double[2];
 
         for (int i = 0; i < gcpCount; i++) {
             final GCP gcp = gcps[i];
             final int row = gcp.getRow();
             final int col = gcp.getCol();
             final double satT = timeSubset[row];
             final double satX = ixSubset[row];
             final double satY = iySubset[row];
             final double satZ = izSubset[row];
             final double[] pitchAxis = pitchAxes[row];
             final double[] yawAxis = yawAxes[row];
 
             calculatePitchRoll(satT, satX, satY, satZ, gcp, pitchAxis, yawAxis, pitchRoll);
 
             x[i] = gcp.getX();
             y[i] = gcp.getY();
             deltaPitch[i] = pitchRoll[0] - pitchAngles[row][col];
             deltaRoll[i] = pitchRoll[1] - rollAngles[row][col];
         }
 
         final RationalFunctionModel deltaPitchModel = new RationalFunctionModel(2, 0, x, y, deltaPitch);
         final RationalFunctionModel deltaRollModel = new RationalFunctionModel(2, 0, x, y, deltaRoll);
 
         for (int k = 0; k < pitchAngles.length; k++) {
             final double[] rowPitchAngles = pitchAngles[k];
             final double[] rowRollAngles = rollAngles[k];
             for (int l = 0; l < rowPitchAngles.length; l++) {
                 rowPitchAngles[l] += deltaPitchModel.getValue(l + 0.5, k + 0.5);
                 rowRollAngles[l] += deltaRollModel.getValue(l + 0.5, k + 0.5);
             }
         }
     }
 
     private static void calculatePitchRoll(
             double satT,
             double satX,
             double satY,
             double satZ,
             GCP gcp,
             double[] pitchAxis,
             double[] yawAxis,
             double[] angles) {
         final double[] gcpPos = CoordinateConverter.wgsToEcef(gcp.getLon(), gcp.getLat(), gcp.getAlt(), new double[3]);
         CoordinateConverter.ecefToEci(TimeConverter.jdToGST(satT + JD2001), gcpPos, gcpPos);
 
         final double dx = gcpPos[X] - satX;
         final double dy = gcpPos[Y] - satY;
         final double dz = gcpPos[Z] - satZ;
         final double[] pointing = toUnitVector(new double[]{dx, dy, dz});
         final double[] sp = vectorProduct(pointing, pitchAxis, new double[3]);
         final double[] sl = vectorProduct(pitchAxis, sp, new double[3]);
         final double[] rollAxis = toUnitVector(vectorProduct(sl, pointing, new double[3]));
         final double total = rollAxis[X] / sp[X] + rollAxis[Y] / sp[Y] + rollAxis[Z] / sp[Z];
 
         final double pitchAngle = Math.PI / 2.0 - VectorMath.angle(sp[X],
                                                                    sp[Y],
                                                                    sp[Z],
                                                                    yawAxis[X],
                                                                    yawAxis[Y],
                                                                    yawAxis[Z]);
         final double rollSign = Math.signum(total);
         final double rollAngle = rollSign * VectorMath.angle(sl[X],
                                                              sl[Y],
                                                              sl[Z],
                                                              pointing[X],
                                                              pointing[Y],
                                                              pointing[Z]);
 
         angles[0] = pitchAngle;
         angles[1] = rollAngle;
     }
 
     private static void calculatePitchAndRollAngles(int mode,
                                                     double fov,
                                                     double ifov,
                                                     double[] centerPitchAngles,
                                                     double[] centerRollAngles,
                                                     double[][] pitchAngles,
                                                     double[][] rollAngles) {
         final int rowCount = pitchAngles.length;
         final int colCount = pitchAngles[0].length;
 
         final double[] deltas = new double[colCount];
         if (mode == 5) {
             // for Mode 5 the last pixel points to the target, i.e. delta is zero for the last pixel
             for (int i = 0; i < deltas.length; i++) {
                 deltas[i] = (i + 0.5) * ifov - fov;
             }
         } else {
             final double halfFov = fov / 2.0;
             for (int i = 0; i < deltas.length; i++) {
                 deltas[i] = (i + 0.5) * ifov - halfFov;
             }
         }
         for (int l = 0; l < rowCount; l++) {
             for (int c = 0; c < colCount; c++) {
                 pitchAngles[l][c] = centerPitchAngles[l];
                 rollAngles[l][c] = centerRollAngles[l] + deltas[c];
             }
         }
     }
 
     /**
      * Finds the GCP which is nearest to some given pixel coordinates (x, y).
      *
      * @param x    the x pixel coordinate.
      * @param y    the y pixel coordinate.
      * @param gcps the ground control points being searched.
      *
      * @return the index of the GCP nearest to ({@code x}, {@code y}) or {@code -1},
      *         if no such GCP could be found.
      */
     private static int findBestGCP(double x, double y, GCP[] gcps) {
         int bestIndex = -1;
         double bestDelta = Double.POSITIVE_INFINITY;
 
         for (int i = 0; i < gcps.length; i++) {
             final double dx = gcps[i].getX() - x;
             final double dy = gcps[i].getY() - y;
             final double delta = dx * dx + dy * dy;
             if (delta < bestDelta) {
                 bestDelta = delta;
                 bestIndex = i;
             }
         }
         return bestIndex;
     }
 
     /**
      * Interpolates a set of values y(x) using a natural spline.
      *
      * @param x  the original x values.
      * @param y  the original y values.
      * @param x2 the x values used for interpolation.
      *
      * @return the interpolated y values, which is an array of length {@code x2.length}.
      */
     private static double[] interpolate(double[] x, double[] y, double[] x2) {
         final PolynomialSplineFunction splineFunction = new SplineInterpolator().interpolate(x, y);
         final double[] y2 = new double[x2.length];
         for (int i = 0; i < x2.length; i++) {
             y2[i] = splineFunction.value(x2[i]);
         }
         return y2;
     }
 }
