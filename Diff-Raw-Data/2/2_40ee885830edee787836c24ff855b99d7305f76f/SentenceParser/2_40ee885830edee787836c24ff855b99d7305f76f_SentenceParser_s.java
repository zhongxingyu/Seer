 
 package gps.nmea;
 
 import gps.data.*;
 
 /**
  *
  * @author javarobots
  */
 public class SentenceParser {
 
     private GpsDataModel mDataModel;
     private boolean mBeginOfSentence = false;
 
     public SentenceParser(GpsDataModel model){
         mDataModel = model;
     }
 
     public String[] processData(String data){
         if (!mBeginOfSentence){
             int indexOfStart = data.indexOf("$");
             if (indexOfStart != -1){
                 data = data.substring(indexOfStart);
                 mBeginOfSentence = true;
             }
         }
         //Parse out the sentences on $
         String[] sentences = data.split("[$]");
         for (String sentence : sentences){
             if (!sentence.isEmpty()){
                 parseSentence("$" + sentence);
             }
         }
         return sentences;
     }
 
     public boolean parseSentence(String sentence) {
         boolean sentenceParsed = false;
         sentence = sentence.trim();
         String[] splitMessage = sentence.split(",");
         if (splitMessage[0].equals("$GPGSA")){
            if (mDataModel.getSelectedSentences().isParseGGA()){
                 parseGsa(sentence);
                 sentenceParsed = true;
             }
         } else if (splitMessage[0].equals("$GPRMC")){
             if (mDataModel.getSelectedSentences().isParseRMC()){
                 parseRmc(sentence);
                 sentenceParsed = true;
             }
         } else if (splitMessage[0].equals("$GPGGA")){
             if (mDataModel.getSelectedSentences().isParseGGA()){
                 parseGga(sentence);
                 sentenceParsed = true;
             }
         } else if (splitMessage[0].equals("$GPGLL")){
             if (mDataModel.getSelectedSentences().isParseGLL()){
                 parseGll(sentence);
                 sentenceParsed = true;
             }
         } else if (splitMessage[0].equals("$GPGSV")){
 
         } else if (splitMessage[0].equals("$GPVTG")){
             if (mDataModel.getSelectedSentences().isParseVTG()){
                 parseVtg(sentence);
                 sentenceParsed = true;
             }
         }
         return sentenceParsed;
     }
 
     /**
      * Parse the $GPGGA message
      * @param message
      */
     private void parseGga(String message){
         String[] values = message.split(",");
 
         //Ensure message is proper length
         if (values.length == 15){
 
             //Set GGA UTC time
             UTCTime time = parseUtcTime(values[1]);
             mDataModel.setGGATime(time);
 
             //Set GGA Coordinate
             Coordinate ggaCoordinate = parseCoordinate(values[2], values[3], values[4], values[5]);
             mDataModel.setGGACoordinate(ggaCoordinate);
 
             //Set fix quality
             String fixQuality = values[6];
             if (!fixQuality.isEmpty()){
                 mDataModel.setGgaFixQuality(Integer.parseInt(fixQuality));
             }
 
             //Set number of stellites in use
             String numberOfSatelites = values[7];
             if (!numberOfSatelites.isEmpty()){
                 mDataModel.setGGANumberOfSatelites(Integer.parseInt(numberOfSatelites));
             }
 
             //Set HDOP
             String hdop = values[8];
             if (!hdop.isEmpty()){
                 mDataModel.setGGAHdop(Double.parseDouble(hdop));
             }
             //Set altitude
             String altitude = values[9];
             if (!altitude.isEmpty()){
                 mDataModel.setGGAHeightAboveSeaLevel(Double.parseDouble(altitude));
             }
 
             //Set Geoidal height
             String geodialHeight = values[11];
             if (!geodialHeight.isEmpty()){
                 try{
                     mDataModel.setGGAGeoidalHeight(Double.parseDouble(geodialHeight));
                 } catch (NumberFormatException ex){
                     //Do nothing
                 }
             }
 
             mDataModel.notifyObservers();
         }
     }
 
     /**
      * Parse the $GPRMC message
      * @param message
      */
     private void parseRmc(String message){
         String[] values = message.split(",");
         if (values.length == 13){
 
             //Set UTC Time
             String utcTime = values[1];
             if (!utcTime.isEmpty()){
                 UTCTime time = parseUtcTime(utcTime);
                 mDataModel.setRMCTime(time);
             }
 
             //Set status
             String status = values[2];
             if (!status.isEmpty()){
                 mDataModel.setRMCStatus(status);
             }
 
             //Set coordinate
             Coordinate coordinate = parseCoordinate(values[3], values[4], values[5], values[6]);
             mDataModel.setRMCCoordinate(coordinate);
 
             //Set speed over ground
             String speed = values[7];
             if (!speed.isEmpty()){
                 mDataModel.setRmcSpeedOverGround(Double.parseDouble(speed));
             }
 
             //Set course over ground
             String course = values[8];
             if (!course.isEmpty()){
                 mDataModel.setRmcTrueCourse(Double.parseDouble(course));
             }
 
             //Set the UTC date
             String date = values[9];
             if (!date.isEmpty()){
                 mDataModel.setRMCDate(parseUtcDate(date));
             }
 
             //Set magnetic variation
             String variation = values[10];
             if (!variation.isEmpty()){
                 mDataModel.setRMCMagneticVariation(Double.parseDouble(variation));
             }
 
             //Set magentic variation direcion
             String varDirection = values[11];
             if (!varDirection.isEmpty()){
                 mDataModel.setRMCMagneticVariationDirection(varDirection);
             }
 
             //Set mode indicator
             String mode = values[12];
             if (!mode.isEmpty()){
                 mDataModel.setRMCModeIndicator(mode);
             }
 
         }
     }
 
     /**
      * Parse the $GPGSA message
      * @param message
      */
     private void parseGsa(String message) {
         String[] values = message.split(",");
         if (values.length == 18){
 
             //Set PDOP
             String pdop = values[15];
             if (!pdop.isEmpty()){
                 mDataModel.setGsaPdop(Double.parseDouble(pdop));
             }
 
             //Set HDOP
             String hdop = values[16];
             if (!hdop.isEmpty()){
                 mDataModel.setGsaHdop(Double.parseDouble(hdop));
             }
 
             //Set VDOP
             String vdop = values[17];
             String[] vdopSplit = vdop.split("\\*");
             vdop = vdopSplit[0];
             if (!vdop.isEmpty()){
                 mDataModel.setGsaVdop(Double.parseDouble(vdop));
             }
 
             //Set fix mode
             String fixMode = values[2];
             if (!fixMode.isEmpty()){
                 mDataModel.setGsaFixMode(Integer.parseInt(fixMode));
             }
         }
     }
 
     private void parseVtg(String message) {
         String[] values = message.split(",");
         if (values.length == 9){
             //Set true course over ground
             String trueCourse = values[1];
             if (!trueCourse.isEmpty()){
                 mDataModel.setVTGTrueCourse(Double.parseDouble(trueCourse));
             }
 
             //Set magnetic course
             String magneticCourse = values[3];
             if (!magneticCourse.isEmpty()){
                 mDataModel.setVTGMagneticCourse(Double.parseDouble(magneticCourse));
             }
 
             //Set ground speed in knots
             String speedInKnots = values[5];
             if (!speedInKnots.isEmpty()){
                 mDataModel.setVTGSpeedInKnots(Double.parseDouble(speedInKnots));
             }
 
             //Set ground speed in KPH
             String speedInKPH = values[7];
             if (!speedInKPH.isEmpty()){
                 mDataModel.setVTGSpeedInKilometers(Double.parseDouble(speedInKPH));
             }
 
             //!jdp - the mode indicator value is not present from etrx legend
             //Set mode indicator
 //            String modeIndicator = values[9];
 //            if (!modeIndicator.isEmpty()){
 //                String[] splitMode = modeIndicator.split("*");
 //                mDataModel.setVTGModeIndicator(splitMode[0]);
 //            }
 
         }
     }
 
     private void parseGll(String message){
         String[] values = message.split(",");
 
         //Ensure proper length
         if (values.length == 7){
 
             //Set coordinate
             Coordinate coordinate = parseCoordinate(values[1], values[2], values[3], values[4]);
             mDataModel.setGLLCoordinate(coordinate);
 
             //Set UTC time
             UTCTime time = parseUtcTime(values[5]);
             mDataModel.setGLLTime(time);
 
             //Set status
             String status = values[6];
             if (status.contains("*")){
                 String[] splitStatus = status.split("\\*");
                 mDataModel.setGLLStatus(splitStatus[0]);
             } else {
 
             }
         }
     }
 
     //!jdp -- Need to add error correction
     private UTCTime parseUtcTime(String utcTime){
 
         //Parse time from end of utcTime String
         String utcSecond = utcTime.substring(utcTime.length() - 2);
         String utcMinutes = utcTime.substring(utcTime.length() - 4, utcTime.length() - 2);
         String utcHour = utcTime.substring(0, utcTime.length() - 4);
         UTCTime time = new UTCTime(
                 Integer.parseInt(utcHour),
                 Integer.parseInt(utcMinutes),
                 Integer.parseInt(utcSecond));
 
         return time;
     }
 
     private UTCDate parseUtcDate(String utcDate){
 
         //Parse date from end
         String utcYear = utcDate.substring(utcDate.length() - 2);
         String utcMonth = utcDate.substring(utcDate.length() - 4, utcDate.length() - 2);
         String utcDay = utcDate.substring(0, utcDate.length() - 4);
 
         UTCDate date = new UTCDate(Integer.parseInt(utcDay), Integer.parseInt(utcMonth), Integer.parseInt(utcYear));
         return date;
     }
 
     //!jdp -- Need to add error correction
     private Coordinate parseCoordinate(
             String latitude,
             String latitudeHemisphere,
             String longitude,
             String longitudeHemisphere){
 
             Hemisphere latHemisphere;
             Hemisphere lonHemisphere;
             if (latitudeHemisphere.equals("N")){
                 latHemisphere = Hemisphere.NORTH;
             } else {
                 latHemisphere = Hemisphere.SOUTH;
             }
 
             if (longitudeHemisphere.equals("E")){
                 lonHemisphere = Hemisphere.EAST;
             } else {
                 lonHemisphere = Hemisphere.WEST;
             }
 
             Coordinate coordinate = new Coordinate(
                     Double.parseDouble(latitude),
                     latHemisphere,
                     Double.parseDouble(longitude),
                     lonHemisphere);
 
         return coordinate;
     }
 
 }
