 package com.core.util;
 
 import java.io.*;
 import java.awt.event.*;
 import javax.sound.sampled.*;
 
 public class RecordPlay {
 
     boolean stopCapture = false ;          //控制录音标志
     boolean inRecording = false ;          //控制录音标志
     boolean hasCaptured = false ;          //控制录音标志
     AudioFormat audioFormat ;             //录音格式
     private EventListener listener;
 
     //读取数据: TargetDataLine -> ByteArrayOutputStream
     ByteArrayOutputStream byteArrayOutputStream ;
     int totaldatasize = 0 ;
     long totalDuration = 0 ;
 
     long recordTimeout = 0 ;
     long recordStart;
     long recordEnd;
     TargetDataLine targetDataLine ;      //音频输入设备
 
     //播放数据: 从AudioInputStream 写入 SourceDataLine 播放
     AudioInputStream audioInputStream ;
     SourceDataLine sourceDataLine ;     //音频输出设备
 
     public RecordPlay(){
 
     }
 
     public void addListener(EventListener lsn) {
         listener = lsn;
     }
 
     //录音事件，保存到ByteArrayOutPutStream
     public void capture(){
 
         class CaptureThread extends Thread {
 
             byte []tempBuffer = new byte[10000] ;
 
             public void run(){
 
                 byteArrayOutputStream = new ByteArrayOutputStream() ;
                 totaldatasize = 0 ;
                 stopCapture = false ;
                 recordStart = System.currentTimeMillis();
                 try{
                     while(!stopCapture){
                         if (isTimeout()) {
                             if (listener != null)
                                 listener.eventTriggered();
                             break;
                         }
 
                         int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length) ;
                         if(cnt > 0){
                             //读取10000个数据
                             byteArrayOutputStream.write(tempBuffer,0,cnt) ;
                             totaldatasize += cnt ;
                         }
                     }
                     byteArrayOutputStream.close() ;
                     inRecording = false;
 
                 }catch(Exception e){
                     e.printStackTrace() ;
                     System.exit(0) ;
                 }
                 recordEnd = System.currentTimeMillis();
                 totalDuration = recordEnd - recordStart;
             }
         }
 
         try{
             //open the record
             audioFormat = getAudioFormat() ;
             //取得输入设备信息
             DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat) ;
             //取得输入设备
             targetDataLine = (TargetDataLine)AudioSystem.getLine(dataLineInfo) ;
             //打开输入设备
             targetDataLine.open(audioFormat) ;
             //启动录音设备
             targetDataLine.start() ;
 
             //创建独立线程进行录音
             Thread captureThread = new Thread(new CaptureThread()) ;
             captureThread.start();
             inRecording = true;
             hasCaptured = true;
 
         }catch(Exception e){
                 e.printStackTrace() ;
         }
     }
 
     //播放ByteArrayOutputStream中的数据
     public void play(){
 
         class PlayThread extends Thread {
 
             byte []tempBuffer = new byte[10000] ;
 
             public void run(){
 
                 try{
                     int cnt ;
                     //读取数据到缓存区
                     while((cnt = audioInputStream.read(tempBuffer,0,tempBuffer.length)) != -1){
 
                         if(cnt > 0){
                             //写入(播放)
                             sourceDataLine.write(tempBuffer, 0, cnt) ;
                         }
                     }
                     //Block等待临时数据被输出为空
                     sourceDataLine.drain() ;
                     sourceDataLine.close() ;
 
                 }catch(Exception e){
                     e.printStackTrace() ;
                     System.exit(0) ;
                 }
             }
         }
         try{
             //取得录音数据
             byte audioData[] = byteArrayOutputStream.toByteArray() ;
             //转换成输入流
             InputStream byteArrayInputStream = new ByteArrayInputStream(audioData) ;
             AudioFormat audioFormat = getAudioFormat() ;
             audioInputStream = new AudioInputStream(byteArrayInputStream,audioFormat,audioData.length/audioFormat.getFrameSize()) ;
             DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,audioFormat) ;
             sourceDataLine = (SourceDataLine)AudioSystem.getLine(dataLineInfo) ;
             sourceDataLine.open(audioFormat) ;
             sourceDataLine.start() ;
 
             //创建独立线程播放
             Thread playThread = new Thread(new PlayThread()) ;
             playThread.start() ;
 
         } catch(Exception e){
             e.printStackTrace() ;
             System.exit(0) ;
         }
     }
 
     //停止录音
     public void stop(){
         stopCapture = true ;
     }
 
     //保存文件
     public void save(String FileName){
         //取得录音输入流
         AudioFormat audioFormat = getAudioFormat() ;
         byte audioData[] = byteArrayOutputStream.toByteArray() ;
         InputStream byteArrayInputStream = new ByteArrayInputStream(audioData) ;
         audioInputStream = new AudioInputStream(byteArrayInputStream,audioFormat,audioData.length/audioFormat.getFrameSize()) ;
 
         //写入文件
         try{
 
             File file = new File(FileName) ;
             AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file) ;
 
         }catch(Exception e){
             e.printStackTrace() ;
         }
     }
 
     public boolean isStopped() {
         return !stopCapture;
     }
 
     public long getDuration() {
         return totalDuration;
     }
 
     public boolean isInRecording() {
         return inRecording;
     }
 
     public boolean hasCaptured() {
         return hasCaptured;
     }
 
     public void setTimeout(long timeout) {
         recordTimeout = timeout;
     }
 
     //取得AudioFormat
     private AudioFormat getAudioFormat(){
         float sampleRate = 16000.0f ;
         int sampleSizeInBits = 16 ;
         int channels = 1 ;
         boolean signed = true ;
         boolean bigEndian = false ;
         return new AudioFormat(sampleRate,sampleSizeInBits,channels,signed,bigEndian) ;
     }
 
     private boolean isTimeout() {
         if (recordTimeout>0 && System.currentTimeMillis()-recordStart>recordTimeout)
             return true;
         return false;
     }
 
 }
 
