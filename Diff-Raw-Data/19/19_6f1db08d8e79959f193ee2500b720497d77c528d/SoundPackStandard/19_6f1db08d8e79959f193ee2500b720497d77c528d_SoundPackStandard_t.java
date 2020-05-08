 package org.ggj2013;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class SoundPackStandard extends SoundPack {
 	public static final String CAT_MEOW = "CAT_MEOW";
 	public static final String EXPLOSION = "EXPLOSION";
	public static final String BEAT = "BEAT";
 
 	@Override
 	public Map<String, Integer> getAllSounds() {
 		Map<String, Integer> sounds = new HashMap<String, Integer>();
 
 		sounds.put(SoundPackStandard.CAT_MEOW, R.raw.cat);
 		sounds.put(SoundPackStandard.EXPLOSION, R.raw.explosion);
		sounds.put(SoundPackStandard.BEAT, R.raw.beat);
 
 		return sounds;
 	}
 }
