 /*
  * Copyright (C) 2012 Gyver
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.gyver.matrixmover.core;
 
 import java.util.ArrayList;
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.Line;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.Mixer;
 import javax.sound.sampled.TargetDataLine;
 
 /**
  *
  * @author jonas
  */
 public class AudioCapture {
 
     private AudioFormat format = null;
     private Mixer mixer = null;
     private Mixer.Info[] mixerInfo = null;
     TargetDataLine line = null;
     byte[] buffer = null;
     float[] leftChanel = null;
     float[] rightChanel = null;
 
     public AudioCapture() {
 
         // load all supported Mixers into mixerinfo
         Line.Info lineInfo = new Line.Info(TargetDataLine.class);
 
         mixerInfo = AudioSystem.getMixerInfo();
 
         ArrayList<Mixer.Info> supportedMixers = new ArrayList<Mixer.Info>();
 
         for (Mixer.Info mixerInfoItem : mixerInfo) {
             Mixer testMixer = AudioSystem.getMixer(mixerInfoItem);
             if (testMixer.isLineSupported(lineInfo)) {
                 supportedMixers.add(mixerInfoItem);
                 System.out.println("\n" + mixerInfoItem.getName() + "\n" + mixerInfoItem.getDescription() + "\n");
             }
         }
 
         mixerInfo = supportedMixers.toArray(new Mixer.Info[supportedMixers.size()]);
 
         // set the audio format
         float sampleRate = 44100F;
         int sampleSizeInBits = 16;
         int channels = 2;
         boolean signed = true;
         boolean bigEndian = false;
 
         format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
 
         buffer = new byte[2048];
         leftChanel = new float[512];
         rightChanel = new float[512];
 
     }
 
     public void startAudio(Mixer.Info mixerInfo) {
 
         mixer = AudioSystem.getMixer(mixerInfo);
 
         DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
 
         line = null;
 
         try {
             line = (TargetDataLine) AudioSystem.getLine(info);
             line.open(format);
             line.start();
         } catch (LineUnavailableException ex) {
             throw new RuntimeException(ex);
 //            Logger.getLogger(AudioCapture.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public Mixer.Info[] getAvalibalMixer() {
         return mixerInfo;
     }
 
     public void captureAudio() {
         if (line == null) {
             return;
         }
 
         line.read(buffer, 0, buffer.length);
 
         //now split the two signals
         int n = 0;
        for (int i = 0; i < leftChanel.length; n += 4, i++) {
             leftChanel[i] = (((buffer[(n + 1)] << 8) + buffer[n]) / 32767.0F);
             rightChanel[i] = (((buffer[(n + 3)] << 8) + buffer[(n + 2)]) / 32767.0F);
         }
     }
 
     public int[] getLevel() {
         int[] level = {calculateRMSLevel(leftChanel), calculateRMSLevel(rightChanel)};
         return level;
     }
 
     private int calculateRMSLevel(float[] data) {
         //calculade the rms
         double sum = 0;
         for (int i = 0; i < data.length; i++) {
             sum = sum + data[i];
         }
 
         double avg = sum / data.length;
 
         double sumMeanSquare = 0d;
         for (int j = 0; j < data.length; j++) {
             sumMeanSquare = sumMeanSquare + Math.pow(data[j] - avg, 2d);
         }
 
         double avgMeanSquare = sumMeanSquare / data.length;
         return (int) (Math.pow(avgMeanSquare, 0.5d) * 100d);
     }
 }
