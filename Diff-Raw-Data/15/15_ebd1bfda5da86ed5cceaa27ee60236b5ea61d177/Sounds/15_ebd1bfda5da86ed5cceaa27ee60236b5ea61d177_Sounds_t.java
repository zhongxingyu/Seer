 package ua.org.dector.ludumdare.ld24;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author dector
  */
 public class Sounds {
     public static final String POWER_UP = "ld24/data/PowerUp.wav";
     public static final String CRASH    = "ld24/data/Crash.wav";
    public static final String SELECT   = "ld24/data/Select.wav";
 //    public static final String JUMP     = "ld24/data/Jump.wav";
     public static final String HIT      = "ld24/data/Hit.wav";
 
     private static Sounds instance;
     
     private Map<String, Sound> cache;
 
     float volume = 1;
     boolean mutedOff = true;
 
     private Sounds() {
         cache = new HashMap<String, Sound>();
     }
     
     public static Sounds get() {
         if (instance == null)
             instance = new Sounds();
         
         return instance;
     }
     
     public void play(String sound) {
         if (! cache.containsKey(sound))
             cache.put(sound, Gdx.audio.newSound(Gdx.files.internal(sound)));
 
         if (mutedOff)
             cache.get(sound).play(volume);
     }
 }
