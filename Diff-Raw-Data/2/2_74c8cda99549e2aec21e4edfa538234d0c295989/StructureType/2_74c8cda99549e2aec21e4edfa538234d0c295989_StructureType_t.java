 package state;
 
 import java.awt.Image;
 import java.util.HashMap;
 import java.util.Map;
 
 import util.ObjectImageStorage;
 
 public abstract class StructureType {
 	private static Map<String, StructureType> types = new HashMap<String, StructureType>();
 
 	public static Map<String, StructureType> getTypes() {return types;}
 
 	private Image image;
 	private int width;
 	private int height;
 
 	public Image getImage() {return image;}
 	public int getWidth() {return width;}
 	public int getHeight() {return height;}
 
 	public abstract Structure create(int x, int y);
 
 	public StructureType(String image, int w, int h) {
 		this.image = ObjectImageStorage.getOrAdd(image);
 		this.width = w;
 		this.height = h;
 	}
 
 	static {
 		types.put("Stockpile", new StructureType(Crate.IMAGE, 1, 1) {
 			@Override
 			public Structure create(int x, int y) {
 				return new Crate(x, y);
 			}
 		});
		types.put("Tree", new StructureType("Assets/EnvironmentObjects/DarkTree.png", 1, 1) {
 			@Override
 			public Structure create(int x, int y) {
 				return new Tree(x, y);
 			}
 		});
 		types.put("Stalagmite", new StructureType("Assets/EnvironmentObjects/Stalagmite.png",1,1) {
 			@Override
 			public Structure create(int x, int y) {
 
 				return new Structure(x,y,1,1,"Assets/EnvironmentObjects/Stalagmite.png");
 			}
 		});
 	}
 }
