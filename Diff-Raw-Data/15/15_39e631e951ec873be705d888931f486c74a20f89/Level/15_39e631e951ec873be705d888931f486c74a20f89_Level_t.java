 package renderer;
 
 public enum Level {
 	LVL_1("./tex/datamap/datamap_1.png", "./tex/background/background_lvl1.jpg", 1),
 	LVL_2("./tex/datamap/datamap_2.png", "./tex/background/background_lvl2.jpg", 2),
 	LVL_3("./tex/datamap/datamap_3.png", "./tex/background/background_lvl3.jpg", 3),
 	LVL_4("./tex/datamap/datamap_4.png", "./tex/background/background.jpg", 4),
 	LVL_5("./tex/datamap/datamap_5.png", "./tex/background/background.jpg", 5),
 	LVL_6("./tex/datamap/datamap_6.png", "./tex/background/background.jpg", 6),
 	LVL_7("./tex/datamap/datamap_7.png", "./tex/background/background.jpg", 7),
 	LVL_8("./tex/datamap/datamap_8.png", "./tex/background/background.jpg", 8),
 	LVL_9("./tex/datamap/datamap_9.png", "./tex/background/background.jpg", 9),
 	LVL_10("./tex/datamap/datamap_10.png", "./tex/background/background.jpg", 10);
 	
 	
 	private String path;
 	private String bgPath;
 	private int id;
	public static final int idOfLastLevel = 10;
 	
 	Level(String path, String bgPath, int id){
 		this.path = path;
 		this.bgPath = bgPath;
 		this.id = id;
 	}
 	
 	public int getId(){
 		return this.id;
 	}
 	
 	public String getPath(){
 		return this.path;
 	}
 	
 	public String getBackgroundPath(){
 		return this.bgPath;
 	}
 	
 	public static Level getLevel(int id){
 		for(Level l:Level.values()){
 			if(l.id == id)
 				return l;
 		}
 		return null;
 	}
 }
