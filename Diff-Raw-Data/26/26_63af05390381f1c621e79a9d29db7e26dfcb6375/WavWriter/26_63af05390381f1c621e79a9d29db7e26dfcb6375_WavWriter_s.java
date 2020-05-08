 /*
  * Copyright (C) 2012 René Jeschke <rene_jeschke@yahoo.de>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.rjeschke.neetutils.audio;
 
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import com.github.rjeschke.neetutils.io.NOutputStream;
 import com.github.rjeschke.neetutils.io.NOutputStreamLE;
 import com.github.rjeschke.neetutils.math.NMath;
 
 /**
  * <p>
  * Simple RIFF WAVE writer class for quick audio dumps.
  * </p>
  * 
  * @author René Jeschke (rene_jeschke@yahoo.de)
  */
 public final class WavWriter
 {
     /** The sample rate. */
     private final int sampleRate;
     /** Bits per sample. */
     private final int bitsPerSample;
     /** Number of channels. */
     private final int channels;
 
     /** Temporary file for wave data. */
     private File tempFile = null;
     /** Output stream for writing temporary wave data. */
     private NOutputStream tempStream = null;
 
     /** RIFF. */
     private final static byte[] RIFF =
         { 'R', 'I', 'F', 'F' };
     /** WAVE. */
     private final static byte[] WAVE =
         { 'W', 'A', 'V', 'E' };
     /** fmt . */
     private final static byte[] FMT_ =
         { 'f', 'm', 't', ' ' };
     /** data. */
     private final static byte[] DATA =
         { 'd', 'a', 't', 'a' };
 
     /**
      * Constructor.
      * 
      * @param sampleRate
      *            A valid RIFF WAVE sample rate.
      * @param bitsPerSample
      *            May be 8, 16 or 24.
      * @param channels
      *            At least one channel.
      */
     public WavWriter(final int sampleRate, final int bitsPerSample, final int channels)
     {
         this.sampleRate = sampleRate;
         this.bitsPerSample = bitsPerSample;
         this.channels = channels;
     }
 
     /**
      * Opens temporary file stream.
      * 
      * @throws IOException
      *             if an IO error occurred.
      */
     private void openTemp() throws IOException
     {
         this.tempFile = File.createTempFile("wavwrite", "bin");
         this.tempFile.deleteOnExit();
         this.tempStream = new NOutputStreamLE(new BufferedOutputStream(new FileOutputStream(this.tempFile)));
     }
 
     /**
      * Saves this RIFF WAVE to a file.
      * 
      * @param filename
      *            The filename.
      * @throws IOException
      *             if an IO error occurred.
      */
     public void save(final String filename) throws IOException
     {
         this.save(new File(filename));
     }
 
     /**
      * Saves this RIFF WAVE to a file.
      * 
      * @param file
      *            The file to save to.
      * @throws IOException
      *             if an IO error occurred.
      */
     public void save(final File file) throws IOException
     {
         OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
         try
         {
             this.save(out);
         }
         finally
         {
             out.close();
         }
     }
 
     public final static double[] normalize(double[] wave)
     {
         double max = 0;
         for(int i = 0; i < wave.length; i++)
             max = Math.max(Math.abs(wave[i]), max);
         if(max > 0)
         {
             for(int i = 0; i < wave.length; i++)
                 wave[i] /= max;
         }
         return wave;
     }
     
     public final static float[] normalize(float[] wave)
     {
         float max = 0;
         for(int i = 0; i < wave.length; i++)
             max = Math.max(Math.abs(wave[i]), max);
         if(max > 0)
         {
             for(int i = 0; i < wave.length; i++)
                 wave[i] /= max;
         }
         return wave;
     }
     
     /**
      * Saves this RIFF WAVE to an output stream.
      * 
     * @param out
      *            the output stream to write to.
      * @throws IOException
      *             if an IO error occurred.
      */
     public void save(final OutputStream output) throws IOException
     {
         final NOutputStreamLE out = new NOutputStreamLE(output);
         try
         {
             if(this.tempFile == null)
                 throw new IOException("No data supplied.");
 
             if(this.tempStream != null)
             {
                 // close also flushes ...
                 this.tempStream.close();
                 this.tempStream = null;
             }
 
             if(this.channels < 1)
                 throw new IOException("Illegal channel count: " + this.channels);
 
             if(this.sampleRate < 1)
                 throw new IOException("Illegal sample rate: " + this.sampleRate);
 
             // dLen ... somehow reminds me of Babylon 5 ... hmmm
             final int dLen = (int)this.tempFile.length();
 
             if(dLen <= 0 || (dLen + 36) <= 0)
                 throw new IOException("Too much wave data. RIFF WAVE is only 32 bits.");
 
             final int samples = dLen / (this.bitsPerSample >> 3);
             final int frames = samples / this.channels;
             final int blockAlign = this.channels * (this.bitsPerSample >> 3);
 
             if(dLen != frames * this.channels * (this.bitsPerSample >> 3))
                 throw new IOException("Unfinished frame.");
 
             out.write(RIFF);
             out.write32(dLen + 36);
 
             out.write(WAVE);
 
             out.write(FMT_);
             out.write32(16);
 
             out.write16(1); // PCM
             out.write16(this.channels);
             out.write32(this.sampleRate);
             out.write32(this.sampleRate * blockAlign);
             out.write16(blockAlign);
             out.write16(this.bitsPerSample);
 
             out.write(DATA);
             out.write32(dLen);
 
             final FileInputStream in = new FileInputStream(this.tempFile);
             try
             {
                 final byte[] b = new byte[8192];
                 int todo = dLen;
                 while(todo > 0)
                 {
                     final int read = in.read(b);
                     out.write(b, 0, read);
                     todo -= read;
                 }
             }
             finally
             {
                 in.close();
             }
         }
         finally
         {
             this.dispose();
         }
     }
 
     /**
      * Removes all temporary files and streams.
      */
     public void dispose()
     {
         if(this.tempStream != null)
         {
             try
             {
                 this.tempStream.close();
             }
             catch (IOException e)
             {
                 // gnah ... dispose should not throw nothing ... so:
                 // let's eat it ... eat it ... eat it ... *singz*
             }
             finally
             {
                 this.tempStream = null;
             }
         }
         if(this.tempFile != null)
         {
             this.tempFile.delete();
             this.tempFile = null;
         }
     }
 
     /**
      * <p>
      * Writes the given (signed) sample(s) into this RIFF WAVE clamped to the
      * given bit depth.
      * </p>
      * <p>
      * Samples are expected to be in [-128,128[, [-32768,32768[ or
      * [-8388608,8388608[ range depending on bit depth.
      * </p>
      * 
      * @param samples
      *            The samples to write.
      * @throws IOException
      *             if an IO error occurred.
      */
     public void write(int... samples) throws IOException
     {
         if(this.tempStream == null)
             this.openTemp();
 
         switch(this.bitsPerSample)
         {
         case 8:
             for(int i = 0; i < samples.length; i++)
                 this.tempStream.write(NMath.clamp(samples[i], -0x80, 0x7f) + 0x80);
             break;
         case 16:
             for(int i = 0; i < samples.length; i++)
                 this.tempStream.write16(NMath.clamp(samples[i], -0x8000, 0x7fff));
             break;
         case 24:
             for(int i = 0; i < samples.length; i++)
                 this.tempStream.write24(NMath.clamp(samples[i], -0x800000, 0x7fffff));
             break;
         default:
             throw new IOException("Unsupported bit depth: " + this.bitsPerSample);
         }
     }
 
     /**
      * <p>
      * Writes the given sample(s) into this RIFF WAVE clamped to the given bit
      * depth.
      * </p>
      * <p>
      * Samples are expected to be in [-1.0,1.0] range.
      * </p>
      * 
      * @param samples
      *            The samples to write.
      * @throws IOException
      *             if an IO error occurred.
      */
     public void write(float... samples) throws IOException
     {
         if(this.tempStream == null)
             this.openTemp();
 
         switch(this.bitsPerSample)
         {
         case 8:
             for(int i = 0; i < samples.length; i++)
                 this.tempStream.write8(NMath.clamp((int)(samples[i] * 128.0f), -0x80, 0x7f) + 0x80);
             break;
         case 16:
             for(int i = 0; i < samples.length; i++)
                 this.tempStream.write16(NMath.clamp((int)(samples[i] * 32768.0f), -0x8000, 0x7fff));
             break;
         case 24:
             for(int i = 0; i < samples.length; i++)
                 this.tempStream.write24(NMath.clamp((int)(samples[i] * 8388608.0f), -0x800000, 0x7fffff));
             break;
         default:
             throw new IOException("Unsupported bit depth: " + this.bitsPerSample);
         }
     }
 
     /**
      * <p>
      * Writes the given sample(s) into this RIFF WAVE clamped to the given bit
      * depth.
      * </p>
      * <p>
      * Samples are expected to be in [-1.0,1.0] range.
      * </p>
      * 
      * @param samples
      *            The samples to write.
      * @throws IOException
      *             if an IO error occurred.
      */
     public void write(double... samples) throws IOException
     {
         if(this.tempStream == null)
             this.openTemp();
 
         switch(this.bitsPerSample)
         {
         case 8:
             for(int i = 0; i < samples.length; i++)
                 this.tempStream.write8(NMath.clamp((int)(samples[i] * 128.0), -0x80, 0x7f) + 0x80);
             break;
         case 16:
             for(int i = 0; i < samples.length; i++)
                 this.tempStream.write16(NMath.clamp((int)(samples[i] * 32768.0), -0x8000, 0x7fff));
             break;
         case 24:
             for(int i = 0; i < samples.length; i++)
                 this.tempStream.write24(NMath.clamp((int)(samples[i] * 8388608.0), -0x800000, 0x7fffff));
             break;
         default:
             throw new IOException("Unsupported bit depth: " + this.bitsPerSample);
         }
     }
 }
