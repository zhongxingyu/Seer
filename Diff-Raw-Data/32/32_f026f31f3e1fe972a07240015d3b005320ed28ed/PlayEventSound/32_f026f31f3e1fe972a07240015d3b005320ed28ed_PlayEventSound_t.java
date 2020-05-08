 /*
  * PlayEventSound.java
  * Transform
  *
  * Copyright (c) 2001-2010 Flagstone Software Ltd. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  * Neither the name of Flagstone Software Ltd. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package debug;
 
 import java.io.File;
 
 import com.flagstone.transform.Background;
 import com.flagstone.transform.Movie;
 import com.flagstone.transform.ShowFrame;
 import com.flagstone.transform.datatype.Bounds;
 import com.flagstone.transform.datatype.WebPalette;
 import com.flagstone.transform.sound.DefineSound;
 import com.flagstone.transform.sound.SoundInfo;
 import com.flagstone.transform.sound.StartSound;
import com.flagstone.transform.util.sound.SoundFactory;
 
 /**
  * PlayStreamingSound generates a Flash file with a sound file encoded as an
  * event sound.
  */
 public final class PlayEventSound {
     /**
      * Run the test from the command line.
      * @param args array of command line arguments.
      */
     public static void main(final String[] args) {
         final File sourceFile = new File(args[0]);
         final File destFile = new File(args[1]);
 
         final int screenWidth = 8000;
         final int screenHeight = 4000;
 
         try {
             if (!destFile.getParentFile().exists()) {
                 destFile.getParentFile().mkdirs();
             }
 
             final Movie movie = new Movie();

            final SoundFactory factory = new SoundFactory();
            factory.read(sourceFile);
            final DefineSound sound =
                factory.defineSound(movie.nextId());
 
             final float framesPerSecond = 12.0f;
 
             movie.setFrameSize(new Bounds(0, 0, screenWidth, screenHeight));
             movie.setFrameRate(framesPerSecond);
 
             movie.add(new Background(WebPalette.LIGHT_BLUE.color()));
 
             final int soundId = movie.nextId();
 
             /*
              * Calculate the time it takes to play the sound and the number of
              * frames this represents.
              */
             final float duration = ((float) sound.getSampleCount())
                     / ((float) sound.getRate());
             final int numberOfFrames = (int) (duration * framesPerSecond);
 
             /*
              * Add the sound definition and the FSStartSound object which is
              * used to start the sound playing.
              */
 
             movie.add(sound);
             movie.add(new StartSound(new SoundInfo(soundId,
                     SoundInfo.Mode.START, 1, null)));
 
             /*
              * Add frames to give the sound time to play.
              */
             for (int j = 0; j < numberOfFrames; j++) {
                 movie.add(ShowFrame.getInstance());
             }
 
             movie.encodeToFile(destFile);
         } catch (final Exception e) {
             e.printStackTrace();
         }
     }
 
     /** Private constructor. */
     private PlayEventSound() {
         // Private.
     }
 }
