 /**
  * 
  */
 package config;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 
 /**
  * @author Mullings
  *
  */
 public enum Section {
 	FOREST_1("Forest_1.tmx", "forest3.png", 20000, Biome.FOREST, "W, A"),
 	FOREST_2("Forest_2.tmx", "forest3.png", 20000, Biome.FOREST, "Jump!"),
 	FOREST_2A("Forest_2a.tmx", "forest3.png", 20000, Biome.FOREST, "Hook!"),
 	FOREST_2B("Forest_2b.tmx", "forest3.png", 20000, Biome.FOREST, "Release!"),
 	FOREST_3("Forest_3.tmx", "forest3.png", 20000, Biome.FOREST, "Bearina"),
 	FOREST_4("Forest_4.tmx", "forest3.png", 20000, Biome.FOREST, "Bearable"),
 	FOREST_5("Forest_5.tmx", "forest3.png", 20000, Biome.FOREST, "Unbearable"),
 	FOREST_6("Forest_6.tmx", "forest3.png", 20000, Biome.FOREST, "Bearinger"),
 	
 	LAKE_1("Forest_8.tmx", "mountainLake3.png", 20000, Biome.LAKE, "Bearant"),
 	LAKE_2("lake1.tmx", "mountainLake3.png", 20000, Biome.LAKE, "Bear"),
 	LAKE_3("Lake_2.tmx", "mountainLake3.png", 20000, Biome.LAKE, "Bearlake"),
	LAKE_4("Lake_4.tmx", "mountainLake3.png", 20000, Biome.LAKE, "Water Bear"),
 	
	DESERT_1("Desert_1.tmx", "desert4.png", 20000, Biome.DESERT, "Bearsert"),
	DESERT_2("Desert_2.tmx", "desert4.png", 20000, Biome.DESERT, "Revenge of Bearsert"),
 	
 	CANYON_1("Canyon_1.tmx", "canyon2.png", 20000, Biome.CANYON, "Canbearyon"),
 	CANYON_2("Canyon_Climb.tmx", "canyon2.png", 30000, Biome.CANYON, "Nothing"),
	CANYON_3("Canyon_3", "canyon2,png", 20000, Biome.CANYON, "Shrooooms"),
 	
 	HELL_1("Hell_1.tmx", "hell2.png", 20000, Biome.HELL, "Bearvents"),
 	HELL_2("Hell_2.tmx", "hell2.png", 20000, Biome.HELL, "Hellbear"), 
 	HELL_3("Hell_3.tmx", "hell2.png", 20000, Biome.HELL, "Childbeard"),  ;
 
 	private final String mapName;
 	private final String backgroundName;
 	private final int goalTime;
 	private Biome biome;
 	private String displayName;
 	private Image instruction;
 	private int x;
 	private int y;
 	
 	static {
 		try {
 			Image bat = new Image("assets/instructions/hookbat.png");
 			FOREST_2A.setInstruction(bat, 30, 30);
 			System.out.println(FOREST_2A.hasInstruction());
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private Section(String map, String background) {
 		this(map, background, 20000, Biome.FOREST, "Make a name!");
 	}
 
 	
 	private Section(String map, String background, int goalTime, Biome biome, String displayName) {
 		this.mapName = map;
 		this.backgroundName = background;
 		this.goalTime = goalTime;
 		this.biome = biome;
 		this.displayName = displayName;
 	}
 	
 	public String getMapPath() {
 		return Config.MAP_PATH + biome.getName() + "/" + mapName;
 	}
 	
 	public String getMapName() {
 		return mapName;
 	}
 	
 	public String getDisplayName() {
 		return displayName;
 	}
 	
 	public String getBackgroundPath() {
 		return Config.BACKGROUND_PATH + backgroundName;
 	}
 	
 	public int getGoalTime() {
 		return goalTime;
 	}
 	
 	public int getID() {
 		return -ordinal() - 1;
 	}
 	public Biome getBiome() {
 		return biome;
 	}
 	
 	public void setInstruction(Image image, int x, int y) {
 		this.instruction = image;
 		this.x = x;
 		this.y = y;
 	}
 	
 	public void renderInstruction(Graphics g) {
 		this.instruction.draw(this.x, this.y);
 	}
 	
 	public boolean hasInstruction() {
 		return this.instruction != null;
 	}
 }
