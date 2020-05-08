 package config;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 
 public enum Biome {
	FOREST(new Color(76, 178, 76),   "Field19.ogg"),
	LAKE(  new Color(185, 69, 201),  "Field08.ogg"),
	DESERT(new Color(200, 200, 200), "Field12.ogg"),
	CANYON(new Color(200, 200, 200), "Field35.ogg"),
	HELL(  new Color(200, 200, 200), "Field09.ogg");
 	
 	private Color color;
 	private Music music;
 	
 	private Biome(Color color) {
		this(color, "Field19.ogg");
 	}
 	
 	private Biome(Color color, String music) {
 		this.color = color;
 		try {
			this.music = new Music("assets/sounds/" + music, true);
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Color getColor() {
 		return color;
 	}
 	
 	public Music getMusic() {
 		return music;
 	}
 
 }
