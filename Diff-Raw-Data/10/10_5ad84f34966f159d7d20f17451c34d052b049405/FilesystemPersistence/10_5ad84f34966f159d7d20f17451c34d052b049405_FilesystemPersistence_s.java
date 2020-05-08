 package util.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioFileFormat;
 import javax.sound.sampled.AudioFileFormat.Type;
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import data.AudioArray;
 import data.AudioSample;
 import data.impl.MonoAudioArray;
 import data.impl.MonoAudioSample;
 import data.impl.StereoAudioArray;
 import data.impl.StereoAudioSample;
 
 import util.Persistence;
 
 public class FilesystemPersistence implements Persistence {
 
   private File directory;
 
   public FilesystemPersistence(String directoryPath) throws PersistenceException {
     setDirectory(directoryPath);
   }
 
   public void setDirectory(String path) throws PersistenceException {
     directory = new File(path);
     if (!directory.exists()) {
       directory = null;
       throw new PersistenceException("File at path does not exist");
     }
     if (!directory.isDirectory()) {
       directory = null;
       throw new PersistenceException("Path is not a directory");
     }
   }
 
   /**
    * This method should be fixed to determine the AudioArray type from the file
    * instead of requiring arrayType.
    * 
    * Currently only accepts 16-bit WAVs (mono/stereo).
    * 
    * Sample amplitudes are not currently scaled.
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T extends AudioSample> AudioArray<T> load(AudioFileFormat.Type format, Class<T> arrayType, String filename)
       throws PersistenceException {
     // file
     File audioFile = new File(directory, filename);
     if (!audioFile.exists()) {
       throw new PersistenceException("File at path does not exist");
     }
     if (!audioFile.isFile()) {
       throw new PersistenceException("Path is not a file");
     }
 
     // AudioInputStream
     AudioInputStream ais = null;
     try {
       ais = AudioSystem.getAudioInputStream(audioFile);
     }
     catch (UnsupportedAudioFileException e) {
       throw new PersistenceException("Audio file type not supported by javax", e);
     }
     catch (IOException e) {
       throw new PersistenceException("Filesystem error", e);
     }
 
     // deserialisation
     byte[] audioBytes = getAudioData(ais);
 
     if (arrayType.equals(MonoAudioSample.class)) {
       MonoAudioSample[] audioData = new MonoAudioSample[audioBytes.length / 2];
       
       for (int i = 0; i < audioData.length; i++) {
         int sampleAmplitude = (audioBytes[(i * 2) + 1] << 8) + audioBytes[i * 2];
         audioData[i] = new MonoAudioSample(sampleAmplitude);
       }
 
       return (AudioArray<T>) new MonoAudioArray(audioData);
     }
     else if (arrayType.equals(StereoAudioSample.class)) {
       StereoAudioSample[] audioData = new StereoAudioSample[audioBytes.length / 4];
 
       for (int i = 0; i < audioBytes.length; i += 4) {
         int leftSampleValue = (audioBytes[i + 1] << 8) + audioBytes[i];
         int rightSampleValue = (audioBytes[i + 3] << 8) + audioBytes[i + 2];
         audioData[i / 4] = new StereoAudioSample(leftSampleValue, rightSampleValue);
       }
 
       return (AudioArray<T>) new StereoAudioArray(audioData);
     }
     else {
       throw new PersistenceException("Return format not supported");
     }
   }
 
   /**
    * Deletes the existing file if there is one. Only writes 16-bit WAVs.
    */
   @Override
   public <T extends AudioSample> void save(AudioArray<T> audio, Type formatFiletype, Class<T> arrayType, String filename)
       throws PersistenceException {
     File audioFile = new File(directory, filename);
     audioFile.delete();
 
     byte[] audioData = null;
     int channels = 0;
 
     if (arrayType.equals(MonoAudioSample.class)) {
       channels = 1;
       MonoAudioSample[] sampleArray = (MonoAudioSample[]) audio.getRange(0, audio.getLength());
       audioData = new byte[sampleArray.length * 2];
 
       for (int i = 0; i < sampleArray.length; i++) {
         byte lsbs = (byte) (sampleArray[i].getLevels()[0] & ((1 << 8) - 1));
         byte msbs = (byte) (sampleArray[i].getLevels()[0] >> 8);
 
         audioData[i * 2] = lsbs;
         audioData[(i * 2) + 1] = msbs;
       }
     }
    else if (formatFiletype.equals(StereoAudioSample.class)) {
       channels = 2;
      MonoAudioSample[] sampleArray = (MonoAudioSample[]) audio.getRange(0, audio.getLength());
      audioData = new byte[sampleArray.length * 2];
 
       for (int i = 0; i < sampleArray.length; i++) {
         byte leftLsbs = (byte) (sampleArray[i].getLevels()[0] & ((1 << 8) - 1));
         byte leftMsbs = (byte) (sampleArray[i].getLevels()[0] >> 8);
         byte rightLsbs = (byte) (sampleArray[i].getLevels()[1] & ((1 << 8) - 1));
         byte rightMsbs = (byte) (sampleArray[i].getLevels()[1] >> 8);
 
         audioData[i * 4] = leftLsbs;
         audioData[(i * 4) + 1] = leftMsbs;
         audioData[(i * 4) + 2] = rightLsbs;
         audioData[(i * 4) + 3] = rightMsbs;
       }
     }
     else {
       throw new PersistenceException("Save format not supported");
     }
 
     AudioFormat af = new AudioFormat(44100, 16, channels, true, false);
 
    // arg 3 as copied from AudioFormat.class
     AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(audioData), af, -1);
 
     try {
       AudioSystem.write(ais, formatFiletype, audioFile);
     }
     catch (IOException e) {
       throw new PersistenceException("Filesystem error", e);
     }
   }
 
   private byte[] getAudioData(AudioInputStream ais) throws PersistenceException {
     byte[] buffer = new byte[ais.getFormat().getFrameSize()];
     byte[] audioBytes = new byte[(int) (ais.getFormat().getFrameSize() * ais.getFrameLength())];
 
     int frameOffset = 0;
     try {
       while (ais.read(buffer) != -1) {
         for (int i = 0; i < buffer.length; i++) {
           audioBytes[(frameOffset * buffer.length) + i] = buffer[i];
         }
 
         frameOffset++;
       }
     }
     catch (IOException e) {
       throw new PersistenceException("Unable to read from file", e);
     }
 
     return audioBytes;
   }
 
 }
