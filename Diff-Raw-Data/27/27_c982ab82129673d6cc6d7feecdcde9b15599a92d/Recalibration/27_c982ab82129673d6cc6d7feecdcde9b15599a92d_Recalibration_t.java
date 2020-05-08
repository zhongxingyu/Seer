 package org.esa.beam.aatsrrecalibration.operators;
 
 import org.esa.beam.framework.gpf.OperatorException;
 
 import java.util.Calendar;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.FileReader;
 import java.io.File;
 
 /**
  * @author Olaf Danne
  * @version $Revision: $ $Date:  $
  */
 public class Recalibration {
 
     public static final int CHANNEL550 = 0;
     public static final int CHANNEL670 = 1;
     public static final int CHANNEL870 = 2;
     public static final int CHANNEL1600 = 3;
 
     private static final Map<String, String> months = new HashMap<String, String>();
 
 
     static {
         months.put("JAN", "1");
         months.put("FEB", "2");
         months.put("MAR", "3");
         months.put("APR", "4");
         months.put("MAY", "5");
         months.put("JUN", "6");
         months.put("JUL", "7");
         months.put("AUG", "8");
         months.put("SEP", "9");
         months.put("OCT", "10");
         months.put("NOV", "11");
         months.put("DEC", "12");
     }
 
    public static final String DRIFT_TABLE_DEFAULT_FILE_NAME = "AATSR_VIS_DRIFT_V00-13.DAT";
     // make sure that the following value corresponds to the file above
     private static final int DRIFT_TABLE_MAX_LENGTH = 5000;
     private static final int DRIFT_TABLE_HEADER_LINES = 6;
 
     private DriftTable driftTable;
     private int driftTableLength;
 
     private boolean useOwnDriftTable;
     private File userDriftTablePath;
 
 
     public Recalibration(boolean useOwnDriftTable, File userDriftTablePath) {
         this.useOwnDriftTable = useOwnDriftTable;
         this.userDriftTablePath = userDriftTablePath;
     }
     
     protected void readDriftTable() throws IOException {
         BufferedReader bufferedReader;
         InputStream inputStream = null;
         if (!useOwnDriftTable || userDriftTablePath == null || userDriftTablePath.length() == 0) {
             inputStream = RecalibrateAATSRReflectancesOp.class.getResourceAsStream(DRIFT_TABLE_DEFAULT_FILE_NAME);
             bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
         } else {
             if (userDriftTablePath.isFile()) {
                 bufferedReader = new BufferedReader(new FileReader(userDriftTablePath));
             } else {
                 throw new OperatorException("Failed to load drift correction table '" + userDriftTablePath + "'.");
             }
         }
 
         StringTokenizer st;
         try {
             driftTable = new DriftTable();
 
             // skip header lines
             for (int i = 0; i < DRIFT_TABLE_HEADER_LINES; i++) {
                 bufferedReader.readLine();
             }
 
             int i = 0;
             String line;
             while ((line = bufferedReader.readLine()) != null && i < DRIFT_TABLE_MAX_LENGTH) {
                 line = line.substring(8); // skip index column
                 line = line.trim();
                 st = new StringTokenizer(line, "   ", false);
                 if (st.hasMoreTokens()) {
                     // date and time (2 tokens)
                     String date = st.nextToken() + " " + st.nextToken();
                     driftTable.setDate(i, date);
                 }
                 if (st.hasMoreTokens()) {
                     // drift560
                     driftTable.setDrift560(i, Double.parseDouble(st.nextToken()));
                 }
                 if (st.hasMoreTokens()) {
                     // drift660
                     driftTable.setDrift670(i, Double.parseDouble(st.nextToken()));
                 }
                 if (st.hasMoreTokens()) {
                     // drift870
                     driftTable.setDrift870(i, Double.parseDouble(st.nextToken()));
                 }
                 if (st.hasMoreTokens()) {
                     // drift1600
                     driftTable.setDrift1600(i, Double.parseDouble(st.nextToken()));
                 }
                 i++;
             }
             driftTableLength = i;
         } catch (IOException e) {
             throw new OperatorException("Failed to load Drift Correction Table: \n" + e.getMessage(), e);
         } catch (NumberFormatException e) {
             throw new OperatorException("Failed to load Drift Correction Table: \n" + e.getMessage(), e);
         } finally {
             if (inputStream != null) {
                 inputStream.close();
             }
         }
     }
 
 
     /**
      * This method performs the nonlinearity correction for the 1.6um channel
      *
      * @param gc1Filename - the GC1 file name (given in DSD.32 metadata)
      * @param reflectance - input reflectance
      *
      * @return correctedReflectance
      */
     protected double getV16NonlinearityCorrectedReflectance(String gc1Filename, double reflectance) {
         double correctedReflectance;
         double volts;
 
         // Nonlinearity coefficients from pre-launch calibration
         final double[] A = new double[]{-0.000027, -0.1093, 0.009393, 0.001013};
 
         // Find out if nonlinearity correction has already been applied - uses name of GC1 file
 
         // Nonlinearity Correction NOT yet applied:
         if (gc1Filename.equals("ATS_GC1_AXVIEC20020123_073430_20020101_000000_20200101_000000")) {
             // Convert 1.6um reflectance back to raw signal using linear conversion
             volts = -0.816 * (reflectance/100.0) / 0.192;
             // Convert 1.6um raw signal to reflectance using non-linear conversion function
             correctedReflectance = Math.PI * (A[0] + A[1]*volts + A[2]*volts*volts + A[3]*volts*volts*volts) / 1.553;
             correctedReflectance *= 100.0;
         } else {
             correctedReflectance = reflectance;
         }
         return correctedReflectance;
     }
 
 /**
      * This method computes the drift correction which has to be removed according to the
      * correction index as determined in {@link #getRemoveDriftCorrectionIndex}
      *
      * @param iChannel    - the input channel index
      * @param correction  - the correction index
      * @param tDiff       - time difference between sensing start and Envisat launch time
      * @param reflectance - input reflectance
      *
      * @return uncorrectedReflectance
      */
     protected double removeDriftCorrection(int iChannel, int correction, double tDiff, double reflectance) {
         double uncorrectedReflectance;
         double drift = 1.0;
 
         // yearly drift rates for exponential drift
         final double[] K = new double[]{0.034, 0.021, 0.013, 0.002};
 
         // Thin film drift model coefficients
         final double[][] A = new double[][]{{0.083, 1.5868E-3},
                 {0.056, 1.2374E-3},
                 {0.041, 9.6111E-4}};
 
 
         if ((iChannel == CHANNEL1600 && correction != 0) ||
                 (iChannel != CHANNEL1600 && correction == 1)) {
             drift = Math.exp(K[iChannel] * tDiff / 365.0);
         }
 
         if (iChannel != CHANNEL1600 && correction == 2) {
             final double s = Math.sin(A[iChannel][1] * tDiff);
             drift = 1.0 + A[iChannel][0] * s * s;
         }
 
         uncorrectedReflectance = reflectance * drift;
 
         return uncorrectedReflectance;
     }
 
     /**
      * This method determines which drift correction had been applied on reflectances.
      * For this, the name of the VC1 file is used.
      *
      * @param vc1Filename - the VC1 file name (given in DSD.31 metadata)
      *
      * @return correctionIndex
      */
     protected int getRemoveDriftCorrectionIndex(String vc1Filename) {
         String year = vc1Filename.substring(14, 18);
         String month = vc1Filename.substring(18, 20);
         String day = vc1Filename.substring(20, 22);
         String hour = vc1Filename.substring(23, 25);
         String mins = vc1Filename.substring(25, 27);
         String secs = vc1Filename.substring(27, 29);
 
         String refTime = day + '-' + month + '-' + year + ' ' + hour + ':' + mins + ':' + secs;
 
         int correctionIndex;
         // Now Identify Which Correction Has Been Applied
         if (getTimeInMillis(refTime) < getTimeInMillis("29-NOV-2005 13:20:26")) {
             correctionIndex = 0; // No Correction is Applied
         } else if (getTimeInMillis(refTime) >= getTimeInMillis("29-NOV-2005 13:20:26") &&
                 getTimeInMillis(refTime) < getTimeInMillis("18-DEC-2006 20:14:15")) {
             correctionIndex = 1; // Exponential Drift Correction is Applied
         } else {
             correctionIndex = 2; // Thin Film Drift Correction is Applied
         }
         return correctionIndex;
     }
 
     /**
      * This method performs a drift correction using a look up table to obtain the drift measurement for a
      * given channel and acquisition time.
      *
      * @param t           - acquisition time
      * @param ati         - acquisition time index in lookup table
      * @param t1          - time in lookup table previous to acquisition time
      * @param t2          - time in lookup table next to acquisition time
      * @param iChannel    - input channel
      * @param reflectance - input reflectance
      *
      * @return correctedReflectance
      */
     protected double applyDriftCorrection(double t, int ati, double t1, double t2, int iChannel, double reflectance) {
         double correctedReflectance;
         double drift = 1.0;
 
         double y1;
         double y2;
 
         switch (iChannel) {
             case 0:
                 y1 = driftTable.getDrift560()[ati];
                 y2 = driftTable.getDrift560()[ati + 1];
                 drift = linearInterpol(t, t1, t2, y1, y2);
                 break;
             case 1:
                 y1 = driftTable.getDrift670()[ati];
                 y2 = driftTable.getDrift670()[ati + 1];
                 drift = linearInterpol(t, t1, t2, y1, y2);
                 break;
             case 2:
                 y1 = driftTable.getDrift870()[ati];
                 y2 = driftTable.getDrift870()[ati + 1];
                 drift = linearInterpol(t, t1, t2, y1, y2);
                 break;
             case 3:
                 y1 = driftTable.getDrift1600()[ati];
                 y2 = driftTable.getDrift1600()[ati + 1];
                 drift = linearInterpol(t, t1, t2, y1, y2);
             default:
                 break;
         }
 
         correctedReflectance = reflectance / drift;
 
         return correctedReflectance;
     }
 
     /**
      * This method provides a simple linear interpolation
      *
      * @param x  , position in [x1,x2] to interpolate at
      * @param x1 , left neighbour of x
      * @param x2 , right neighbour of x
      * @param y1 , y(x1)
      * @param y2 , y(x2)
      *
      * @return double z = y(x), the interpolated value
      */
     private double linearInterpol(double x, double x1, double x2, double y1, double y2) {
         double z;
 
         if (x1 == x2) {
             z = y1;
         } else {
             final double slope = (y2 - y1) / (x2 - x1);
             z = y1 + slope * (x - x1);
         }
 
         return z;
     }
 
     protected int getAcquisitionTimeIndex(String acquisitionTime) {
         int acquisitionTimeIndex = -1;
 
         for (int i = 0; i < driftTableLength; i++) {
             if (getTimeInMillis(acquisitionTime) < getTimeInMillis(driftTable.getDate()[i])) {
                 acquisitionTimeIndex = i;
                 break;
             }
         }
 
         return acquisitionTimeIndex;
     }
 
     protected double getAcquisitionTimeIndexPrevious(int acquisitionTimeIndex) {
         return getTimeInMillis(driftTable.getDate()[acquisitionTimeIndex]);
     }
 
     protected double getAcquisitionTimeIndexNext(int acquisitionTimeIndex) {
         return getTimeInMillis(driftTable.getDate()[acquisitionTimeIndex+1]);
     }
 
     protected void checkAcquisitionTimeRange(String acquisitionTime) throws OperatorException {
         final String envisatLaunch = "01-MAR-2002 00:00:00";
 
         if (getTimeInMillis(acquisitionTime) < getTimeInMillis(envisatLaunch)) {
             throw new OperatorException("ERROR: Acquisition time " + acquisitionTime + " before ENVISAT launch date.\n");
         }
 
         String driftTableStartDate = driftTable.getDate()[0];
         if (getTimeInMillis(acquisitionTime) < getTimeInMillis(driftTableStartDate)) {
             throw new OperatorException(
                     "ERROR: Acquisition time " + acquisitionTime + " before start time of drift table.\n");
         }
 
         String driftTableEndDate = driftTable.getDate()[driftTableLength - 1];
         if (getTimeInMillis(acquisitionTime) > getTimeInMillis(driftTableEndDate)) {
             throw new OperatorException(
                     "ERROR: Acquisition time " + acquisitionTime + " after last time of drift table.\n");
         }
     }
 
     /**
      * This method provides the time in milliseconds from a given time string.
      * Allowed formats:
      * - dd-MMM-yyyy hh:mm:ss
      * - dd-mm-yyyy hh:mm:ss
      *
      * @param timeString the time string
      *
      * @return the time in millis
      */
     protected long getTimeInMillis(String timeString) {
         long driftTableTime;
 
         Calendar driftTableDate = Calendar.getInstance();
         if (Character.isDigit(timeString.charAt(3))) {
             driftTableDate.set(Calendar.YEAR, Integer.parseInt(timeString.substring(6, 10)));
             String driftTableMonth = timeString.substring(3, 5);
             driftTableDate.set(Calendar.MONTH, Integer.parseInt(driftTableMonth));
         } else {
             driftTableDate.set(Calendar.YEAR, Integer.parseInt(timeString.substring(7, 11)));
             String driftTableMonth = timeString.substring(3, 6);
             String month = months.get(driftTableMonth);
             driftTableDate.set(Calendar.MONTH, Integer.parseInt(month));
         }
         driftTableDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeString.substring(0, 2)));
 
         driftTableTime = driftTableDate.getTimeInMillis();
         return driftTableTime;
     }
 
     /**
      * Object providing the drift data for the 4 channels
      * 
      */
     private class DriftTable {
         private String[] date = new String[DRIFT_TABLE_MAX_LENGTH];
         private double[] drift560 = new double[DRIFT_TABLE_MAX_LENGTH];
         private double[] drift670 = new double[DRIFT_TABLE_MAX_LENGTH];
         private double[] drift870 = new double[DRIFT_TABLE_MAX_LENGTH];
         private double[] drift1600 = new double[DRIFT_TABLE_MAX_LENGTH];
 
         public String[] getDate() {
             return date;
         }
 
         public double[] getDrift560() {
             return drift560;
         }
 
         public double[] getDrift670() {
             return drift670;
         }
 
         public double[] getDrift870() {
             return drift870;
         }
 
         public double[] getDrift1600() {
             return drift1600;
         }
 
         public void setDate(int index, String value) {
             date[index] = value;
         }
 
         public void setDrift560(int index, double value) {
             drift560[index] = value;
         }
 
         public void setDrift670(int index, double value) {
             drift670[index] = value;
         }
 
         public void setDrift870(int index, double value) {
             drift870[index] = value;
         }
 
         public void setDrift1600(int index, double value) {
             drift1600[index] = value;
         }
     }
 }
