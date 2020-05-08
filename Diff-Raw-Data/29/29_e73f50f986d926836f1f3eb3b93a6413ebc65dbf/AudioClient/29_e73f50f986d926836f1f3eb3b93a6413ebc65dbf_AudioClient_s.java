 package Client;
 
 import javax.sound.sampled.*;
 import javax.swing.*;
 import java.awt.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 import Common.Finals;
 
 public class AudioClient extends Thread //sending data to server socket
 {
     private float level, sampleRate;
     private TargetDataLine targetDataLine;
     private AudioFormat format;
     private int bufferSize;
     private DataLine.Info sendInfo, playbackInfo;
 
     private Socket socket;
     private OutputStream out;
     private InputStream in;
     private byte[] buffer;
     private byte[]  playbackData;
     private SourceDataLine  line;
 
     private boolean isRunning;
 
     public static void main(String[] args){
         String address = args[0];
         int port = Integer.parseInt(args[1]);
         try{
             new AudioClient(new Socket(address, port)); } catch (Exception e) {}
     }
 
     public AudioClient (Socket socket){ //put in socket with server address
         this.socket = socket;
         sampleRate = 8000.0F;
         int sampleSizeInBits = 16;
         int channels = 1;
         boolean signed = true;
         boolean bigEndian = false;
         format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
         sendInfo = new DataLine.Info(TargetDataLine.class, format);
         playbackInfo = new DataLine.Info(SourceDataLine.class, format);
         //bufferSize = (int) format.getSampleRate() * format.getFrameSize();
         buffer = new byte[Finals.BUFFER_SIZE];
         //Thread t = new Thread(this); t.start();
     }
 
     public void run(){
         isRunning = true;
         try{
             targetDataLine = (TargetDataLine)AudioSystem.getLine(sendInfo);
             targetDataLine.open(format, (int)sampleRate);
             targetDataLine.start();
 
             out = socket.getOutputStream();
             in = socket.getInputStream();
             line = (SourceDataLine) AudioSystem.getLine(playbackInfo);
             line.open(format);
             line.start();
             int     nBytesRead = 0;
             playbackData = new byte[Finals.BUFFER_SIZE];
 
             while (isRunning && nBytesRead!=-1) {
                 targetDataLine.read(buffer, 0, buffer.length); //mic input
                 out.write(buffer); //send sound
                 //receive and output sound:
                 nBytesRead = in.read(playbackData, 0, playbackData.length); //receive sound
                 line.write(playbackData,  0, nBytesRead); //speaker output
             }
 
             line.drain();
             line.close();
             socket.close();
         } catch (Exception e) {
            e.printStackTrace();
         }
     }
 
     public void stopRunning(){ try{isRunning = false;} catch(Exception e){} }
 }
