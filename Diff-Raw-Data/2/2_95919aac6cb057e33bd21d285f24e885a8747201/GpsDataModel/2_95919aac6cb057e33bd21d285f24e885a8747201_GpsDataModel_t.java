 
 package gps.data;
 
 import gps.nmea.SelectedSentences;
 import java.util.Observable;
 
 /**
  * An observable model containing parsed GPS data
  * @author javarobots
  */
 public class GpsDataModel extends Observable {
 
     private SelectedSentences mSelectedSentences;
 
     //From $GPGGA sentence
     private UTCTime mGGATime = new UTCTime(0,0,0);
     private Coordinate mGGACoordinate = new Coordinate();
     private int mGGAFixQuality = 0; //0=Invalid 1=GPS 2=DGPS
     private int mGGANumberOfSatelites = 0; //Number of satellites being tracked
     private double mGGAHdop = 99; //Horizontal dilution of precision
     private double mGGAHeightAboveSeaLevel = 0; //Altitude in meters
     private double mGGAGeoidalHeight = 0; //Geoidal height in meters
 
     //From $GPRMC sentence
     private UTCTime mRMCTime = new UTCTime(0,0,0);;
     private String mRMCStatus = "V";
     private Coordinate mRMCCoordinate = new Coordinate();
     private double mRMCSpeedOverGround = 0; //Speed in knots
     private double mRMCCourseOverGround = 0; //Heading over ground
     private UTCDate mRMCDate = new UTCDate();
     private double mRMCMagneticVariation;
     private String mRMCMagneticVariationDirection;
     private String mRMCModeIndicator = "N";
 
     //From $GPGSA sentence
     private String mGSAMode = "M";
     private int mGSAFixType = 1; //1=No Fix 2=2D 3=3D
     private int[] mGSAPRNNumber = {0,0,0,0,0,0,0,0,0,0,0,0};
     private double mGSAPdop = 99;
     private double mGSAHdop = 99;
     private double mGSAVdop = 99;
 
     //From $GPVTG sentence
     private double mVTGTrueCourse = 0;
     private double mVTGMagneticCourse = 0;
     private double mVTGSpeedInKnots = 0;
     private double mVTGSpeedInKilometers = 0;
     private String mVTGModeIndicator = "N";
 
     //From $GPGLL sentence
     private Coordinate mGLLCoordinate = new Coordinate();
     private UTCTime mGLLTime = new UTCTime();
     private String mGLLStatus = "V";
 
     //Common Getter Setter
 
     public SelectedSentences getSelectedSentences() {
         return mSelectedSentences;
     }
 
     public void setSelectedSentences(SelectedSentences selectedSentences) {
         this.mSelectedSentences = selectedSentences;
         setChanged();
     }
 
 
     //---------------------- GGA Getter Setters ----------------------------
 
     public double getGGAHeightAboveSeaLevel() {
         return mGGAHeightAboveSeaLevel;
     }
 
     public void setGGAHeightAboveSeaLevel(double heightAboveSeaLevel) {
         this.mGGAHeightAboveSeaLevel = heightAboveSeaLevel;
         setChanged();
     }
 
     public int getGgaFixQuality() {
         return mGGAFixQuality;
     }
 
     public void setGgaFixQuality(int fixQuality) {
         this.mGGAFixQuality = fixQuality;
         setChanged();
     }
 
     public Coordinate getGGACoordinate() {
         return mGGACoordinate;
     }
 
     public void setGGACoordinate(Coordinate coordinate) {
         this.mGGACoordinate = coordinate;
         setChanged();
     }
 
     public double getGGAGeoidalHeight() {
         return mGGAGeoidalHeight;
     }
 
     public void setGGAGeoidalHeight(double geoidalHeight) {
         this.mGGAGeoidalHeight = geoidalHeight;
         setChanged();
     }
 
     public double getGGAHdop() {
         return mGGAHdop;
     }
 
     public void setGGAHdop(double hdop) {
         this.mGGAHdop = hdop;
         setChanged();
     }
 
     public UTCTime getGGATime() {
         return mGGATime;
     }
 
     public void setGGATime(UTCTime time) {
         this.mGGATime = time;
         setChanged();
     }
 
     public int getGGANumberOfSatelites() {
         return mGGANumberOfSatelites;
     }
 
     public void setGGANumberOfSatelites(int numberOfSatelites) {
         this.mGGANumberOfSatelites = numberOfSatelites;
         setChanged();
     }
 
 
 
     //---------------------- GSA Getter Setter -----------------------------
 
     public int getGsaFixMode() {
         return mGSAFixType;
     }
 
    public void setGsaFixType(int fixMode) {
         this.mGSAFixType = fixMode;
         setChanged();
     }
 
     public double getGsaHdop() {
         return mGSAHdop;
     }
 
     public void setGsaHdop(double hdop) {
         this.mGSAHdop = hdop;
         setChanged();
     }
 
     public double getGsaPdop() {
         return mGSAPdop;
     }
 
     public void setGsaPdop(double pdop) {
         this.mGSAPdop = pdop;
         setChanged();
     }
 
     public double getGsaVdop() {
         return mGSAVdop;
     }
 
     public void setGsaVdop(double vdop) {
         this.mGSAVdop = vdop;
         setChanged();
     }
 
     public String getmGSAMode() {
         return mGSAMode;
     }
 
     public void setmGSAMode(String mode) {
         this.mGSAMode = mode;
         setChanged();
     }
 
     public int[] getmGSAPRNNumber() {
         return mGSAPRNNumber;
     }
 
     public void setmGSAPRNNumber(int[] prnNumber) {
         this.mGSAPRNNumber = prnNumber;
         setChanged();
     }
 
 
 
     //---------------------- RMC Getter Setter -----------------------------
 
     public double getRmcSpeedOverGround() {
         return mRMCSpeedOverGround;
     }
 
     public void setRmcSpeedOverGround(double speedOverGround) {
         this.mRMCSpeedOverGround = speedOverGround;
         setChanged();
     }
 
     public double getRmcTrueCourse() {
         return mRMCCourseOverGround;
     }
 
     public void setRmcTrueCourse(double trueCourse) {
         this.mRMCCourseOverGround = trueCourse;
         setChanged();
     }
 
     public Coordinate getRMCCoordinate() {
         return mRMCCoordinate;
     }
 
     public void setRMCCoordinate(Coordinate coordinate) {
         this.mRMCCoordinate = coordinate;
         setChanged();
     }
 
     public UTCDate getRMCDate() {
         return mRMCDate;
     }
 
     public void setRMCDate(UTCDate date) {
         this.mRMCDate = date;
         setChanged();
     }
 
     public double getRMCMagneticVariation() {
         return mRMCMagneticVariation;
     }
 
     public void setRMCMagneticVariation(double magneticVariation) {
         this.mRMCMagneticVariation = magneticVariation;
         setChanged();
     }
 
     public String getRMCMagneticVariationDirection() {
         return mRMCMagneticVariationDirection;
     }
 
     public void setRMCMagneticVariationDirection(String magneticVariationDirection) {
         this.mRMCMagneticVariationDirection = magneticVariationDirection;
         setChanged();
     }
 
     public String getRMCModeIndicator() {
         return mRMCModeIndicator;
     }
 
     public void setRMCModeIndicator(String modeIndicator) {
         this.mRMCModeIndicator = modeIndicator;
         setChanged();
     }
 
     public String getRMCStatus() {
         return mRMCStatus;
     }
 
     public void setRMCStatus(String status) {
         this.mRMCStatus = status;
         setChanged();
     }
 
     public UTCTime getRMCTime() {
         return mRMCTime;
     }
 
     public void setRMCTime(UTCTime time) {
         this.mRMCTime = time;
         setChanged();
     }
 
     // --------------------------- VTG Getter Setter ------------------------
 
     public double getVTGMagneticCourse() {
         return mVTGMagneticCourse;
     }
 
     public void setVTGMagneticCourse(double magneticCourse) {
         this.mVTGMagneticCourse = magneticCourse;
         setChanged();
     }
 
     public String getVTGModeIndicator() {
         return mVTGModeIndicator;
     }
 
     public void setVTGModeIndicator(String modeIndicator) {
         this.mVTGModeIndicator = modeIndicator;
         setChanged();
     }
 
     public double getVTGSpeedInKilometers() {
         return mVTGSpeedInKilometers;
     }
 
     public void setVTGSpeedInKilometers(double speedInKilometers) {
         this.mVTGSpeedInKilometers = speedInKilometers;
         setChanged();
     }
 
     public double getVTGSpeedInKnots() {
         return mVTGSpeedInKnots;
     }
 
     public void setVTGSpeedInKnots(double speedInKnots) {
         this.mVTGSpeedInKnots = speedInKnots;
         setChanged();
     }
 
     public double getVTGTrueCourse() {
         return mVTGTrueCourse;
     }
 
     public void setVTGTrueCourse(double trueCourse) {
         this.mVTGTrueCourse = trueCourse;
         setChanged();
     }
 
     public Coordinate getGLLCoordinate() {
         return mGLLCoordinate;
     }
 
     public void setGLLCoordinate(Coordinate coordinate) {
         this.mGLLCoordinate = coordinate;
         setChanged();
     }
 
     public String getGLLStatus() {
         return mGLLStatus;
     }
 
     public void setGLLStatus(String status) {
         this.mGLLStatus = status;
         setChanged();
     }
 
     public UTCTime getGLLTime() {
         return mGLLTime;
     }
 
     public void setGLLTime(UTCTime time) {
         this.mGLLTime = time;
         setChanged();
     }
 
 
 
 }
