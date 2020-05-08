 package uk.co.fusefm.fusealysis;
 
 import jouvieje.bass.Bass;
 import jouvieje.bass.BassInit;
 import jouvieje.bass.defines.*;
 import jouvieje.bass.exceptions.BassException;
 import jouvieje.bass.structures.HSTREAM;
 
 /**
  * Analyses tracks' tops and tails using the BASS library
  *
  * @author Andrew Bonney
  */
 public class FileAnalyser {
 
     private final String baseDirectory;
     private int trackFrontVolume, trackBackVolume;
     private boolean loadSuccess = false;
 
     /**
      * @param baseDir Base directory for tracks to analyse
      * @param frontVol Volume level at which to set in points
      * @param backVol Volume level at which to set out points
      */
     public FileAnalyser(String baseDir, int frontVol, int backVol) {
         baseDirectory = baseDir;
         trackFrontVolume = frontVol;
         trackBackVolume = backVol;
         try {
             BassInit.loadLibraries();
         } catch (BassException e) {
             System.out.println("NativeBass error! " + e.getMessage());
             return;
         }
 
         if (BassInit.NATIVEBASS_LIBRARY_VERSION() != BassInit.NATIVEBASS_JAR_VERSION()) {
             System.out.println("Error!  NativeBass library version "
                     + "(" + BassInit.NATIVEBASS_LIBRARY_VERSION() + ") is "
                     + "different to jar version ("
                     + BassInit.NATIVEBASS_JAR_VERSION() + ")\n");
             return;
         }
         if (!Bass.BASS_Init(-1, 44100, BASS_DEVICE.BASS_DEVICE_MONO, null, null)) {
             System.out.println("Could not initialise BASS");
             return;
         }
         loadSuccess = true;
     }
 
     public boolean getInitStatus() {
         return loadSuccess;
     }
 
     public void updateSettings(int frontVol, int backVol) {
         trackFrontVolume = frontVol;
         trackBackVolume = backVol;
     }
 
     /**
      * Analyse the specified track and return its in time
      *
      * @param relativePath
      * @return
      */
     public double getInTime(String relativePath) {
         return getTime(relativePath, false);
     }
 
     /**
      * Analyse the specified track and return its out time
      *
      * @param relativePath
      * @return
      */
     public double getOutTime(String relativePath) {
         return getTime(relativePath, true);
     }
 
     private double getTime(String relativePath, boolean reverse) {
         String extension = relativePath.substring(relativePath.lastIndexOf(".") + 1, relativePath.length());
         if (extension.equalsIgnoreCase("flac") || extension.equalsIgnoreCase("m4a")) {
             //TODO: Support these
             System.out.println("FLAC and M4A currently unsupported.");
             return 0;
         }
         String fileLoc = baseDirectory + relativePath;
         double trackPos = 0;
         HSTREAM stream, revStream = null;
         //if (extension.equalsIgnoreCase("flac")) {
         //long javaResult = BassJNI.BASS_StreamCreateFile(false, fileLoc.getBytes(), BASS_STREAM.BASS_STREAM_AUTOFREE, 0, 0);
         //Pointer point = new Pointer();
         //stream = Bass.BASS_FLAC_StreamCreateURL(fileLoc, 0, BASS_STREAM.BASS_STREAM_AUTOFREE, null, null);
         //stream = Bass.BASS_FLAC_StreamCreateFile(false, point.asPointer(javaResult), BASS_STREAM.BASS_STREAM_AUTOFREE, 0, 0);
         //} else if (extension.equalsIgnoreCase("m4a")) {
         //stream = Bass.BASS_StreamCreateFile(false, fileLoc, BASS_STREAM.BASS_STREAM_AUTOFREE, 0, 0);
         //} else {
         stream = Bass.BASS_StreamCreateFile(false, fileLoc, 0, 0, BASS_STREAM.BASS_STREAM_DECODE | BASS_STREAM.BASS_STREAM_PRESCAN | BASS_SAMPLE.BASS_SAMPLE_FX);
         //}
         int errorCode = Bass.BASS_ErrorGetCode();
         if (errorCode != 0) {
             System.out.println("Error opening file " + fileLoc + " code " + errorCode);
             return 0;
         }
         int streamID = stream.asInt();
         if (reverse) {
             revStream = Bass.BASS_FX_ReverseCreate(streamID, 2, BASS_STREAM.BASS_STREAM_DECODE | BASS_FX.BASS_FX_FREESOURCE);
             streamID = revStream.asInt();
         }
         Bass.BASS_ChannelPlay(streamID, true);
         double trackLength = Bass.BASS_ChannelBytes2Seconds(streamID, Bass.BASS_ChannelGetLength(streamID, BASS_POS.BASS_POS_BYTE));
         while (Bass.BASS_ChannelIsActive(streamID) == BASS_ACTIVE.BASS_ACTIVE_PLAYING) {
             try {
                 Thread.sleep(5);
             } catch (InterruptedException ex) {
                 // Do nothing
             }
             int bassLevel = Bass.BASS_ChannelGetLevel(streamID);
             if (bassLevel <= 0) {
                 continue;
             }
             String levelString = Integer.toBinaryString(bassLevel);
            if (levelString.length() < 16) {
                continue;
            }
             int channelLevel = Integer.parseInt(new StringBuffer(levelString.substring(0, 16)).reverse().toString(), 2);
             long bytePosition = Bass.BASS_ChannelGetPosition(streamID, BASS_POS.BASS_POS_BYTE);
             double currentPos = Bass.BASS_ChannelBytes2Seconds(streamID, bytePosition);
             if (!reverse && currentPos > 15) {
                 System.out.println("Tried first 15 seconds, no audio found. Skipping...");
                 break;
             } else if (reverse && (currentPos + 15) < trackLength) {
                 System.out.println("Tried last 15 seconds, no audio found. Skipping...");
                 break;
             }
             if (!reverse && channelLevel >= trackFrontVolume) {
                 trackPos = currentPos;
                 break;
             } else if (reverse && channelLevel >= trackBackVolume) {
                 trackPos = currentPos;
                 break;
             }
         }
         Bass.BASS_ChannelStop(streamID);
         Bass.BASS_StreamFree(stream);
         if (reverse) {
             Bass.BASS_StreamFree(revStream);
         }
         return trackPos;
     }
 }
