 package gameconcept;
 
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
 import org.lwjgl.openal.AL10;
 import org.lwjgl.util.WaveData;
 
 public class SoundEngine {
     GameConcept gameConcept;
     int buffer = AL10.alGenBuffers();
     int source = AL10.alGenSources();
     
     public SoundEngine(GameConcept instance) {
         gameConcept = instance;
     }
     
     public void loadSound(String wavName) {
        WaveData data = WaveData.create(this.getClass().getResourceAsStream(wavName + ".wav"));
         AL10.alBufferData(buffer, data.format, data.data, data.samplerate);
         AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
         AL10.alSourcef(source, AL10.AL_GAIN, 0.2f);
     }
     
     public void playSound() {
         AL10.alSourcePlay(source);
     }
     
     public void destroy() {
         AL10.alDeleteBuffers(buffer);
         AL10.alDeleteSources(source);
     }
 }
