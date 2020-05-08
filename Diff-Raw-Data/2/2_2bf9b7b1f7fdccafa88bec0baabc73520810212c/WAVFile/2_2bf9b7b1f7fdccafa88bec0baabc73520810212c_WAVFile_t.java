 package model;
 
 import audiorender.AudioWaveformCreator;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import utils.WavSplitter;
 
 /**
  * @author diouf
  * @author anasdridi
  * @author yulia
  */
 public class WAVFile {
 
   private String filePath;
   private String fileName;
   private int imageWidth;
   private int imageHeigh;
   private static int SPLITSIZE = 100000000;
   private File soundFile;
   private AudioInputStream audioStream;
   private AudioFormat audioFormat;
 
   public WAVFile(String filePath) {
     this.filePath = filePath;
    String[] elements = filePath.split(File.pathSeparator);
     this.fileName = elements[elements.length - 1];
     soundFile = new File(filePath);
   }
 
   public void playSound() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
 
     String filename = filePath;
     String strFilename = filename;
     int BUFFER_SIZE = 128000;
     //File soundFile;
 //    AudioInputStream audioStream;
 //    AudioFormat audioFormat;
     SourceDataLine sourceLine;
 
     //soundFile = new File(strFilename);
 
 
     audioStream = AudioSystem.getAudioInputStream(soundFile);
 
     audioFormat = audioStream.getFormat();
 
     DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
 
     sourceLine = (SourceDataLine) AudioSystem.getLine(info);
     sourceLine.open(audioFormat);
 
 
     sourceLine.start();
 
     int nBytesRead = 0;
     byte[] abData = new byte[BUFFER_SIZE];
     while (nBytesRead != -1) {
 
       nBytesRead = audioStream.read(abData, 0, abData.length);
 
       if (nBytesRead >= 0) {
         @SuppressWarnings("unused")
         int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
       }
     }
 
     sourceLine.drain();
     sourceLine.close();
   }
 
 
 
   public BufferedImage generateSonogramme() throws UnsupportedAudioFileException, IOException, Exception {
       
        AudioWaveformCreator awc;
         
        int wavSize = (int)new File(filePath).length();
        
        String in = "wavSize = "+wavSize;
        Logger.getLogger(WAVFile.class.getName()).log(Level.INFO, in);
 //       System.out.println("wavSize = "+wavSize);
        
        BufferedImage image;
        
        if(wavSize < 200000000){
        awc = new AudioWaveformCreator(filePath, "test.png");
             image = awc.createAudioInputStream(10000, 350);
        }
        else{
            int nbSplits = (int)Math.ceil((long)wavSize/SPLITSIZE+1)*2;
            Logger.getLogger(WAVFile.class.getName()).log(Level.INFO, "nbSplits : "+nbSplits);
 //           System.out.println("nbSplits : "+nbSplits);
            image = WavSplitter.split(new File(filePath), nbSplits);//, filePath, "test.png");
        }
          imageWidth = image.getWidth();
          imageHeigh = image.getHeight();
        return image;
    
   }
 
     public int getImageWidth() {
         return imageWidth;
     }
 
     public int getImageHeight() {
         return imageHeigh;
     }
 
   public int getDureeWav() {
       //AH 20/02/2013 careful, the use of this function can cause an heap memory error with large files
 //      AudioInputStream audioStream;
 //      AudioFormat audioFormat;
       double duration = 0;
       try{
         audioStream = AudioSystem.getAudioInputStream(soundFile);
         audioFormat = audioStream.getFormat();
         duration = AudioSystem.getAudioInputStream(soundFile).getFrameLength() / (audioFormat.getFrameRate());
       }
       catch(Exception e){
            Logger.getLogger(WAVFile.class.getName()).log(Level.SEVERE, null,e);
 //          e.printStackTrace();
       }
 
       return (int) duration;
   }
 
   public int getTimeCurrentPostion(int positionLarg) {
     int time;
     time = (int) ((this.getDureeWav() * positionLarg) / this.getImageWidth());
     return time;
   }
   
   public int getPositionByTime(int time){
       int position;
       position = (int) ((this.getImageWidth() * time) / this.getDureeWav());
       return position;
   }
 }
