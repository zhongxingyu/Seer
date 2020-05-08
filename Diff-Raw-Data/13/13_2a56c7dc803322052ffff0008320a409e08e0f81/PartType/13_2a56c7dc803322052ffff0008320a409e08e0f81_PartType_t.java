 package factory;
 
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.io.Serializable;
 
 import Utils.Constants;
 import Utils.StringUtil;
 
 public class PartType implements Serializable, FactoryData {
 	private static final long serialVersionUID = 1;
 	private String name = "";
 	private final String id;
 	private int partNum;
 	private float badChance = 0;
 	private String description = "";
 	private String imagePath;
 
 	// private Image image;
 
 	/**
 	 * 
 	 * @param s
 	 *            - a number indicating part type
 	 */
 	public PartType(String s) {
 		name = s;
 		this.id = StringUtil.md5(name);
 		// setImage();
 	}
 
 	public PartType(String s, int num, String desc) {
 		name = s;
 		partNum = num;
 		description = desc;
 		this.id = StringUtil.md5(name);
 		imagePath = name;
 	}
 	
 	public PartType(String s, int num, String desc, float chance) {
 		name = s;
 		partNum = num;
 		description = desc;
 		badChance = chance;
 		this.id = StringUtil.md5(name);
 	}
 
 	public String toString() {
 		return getName();
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setPartNum(int partNum) {
 		this.partNum = partNum;
 	}
 
 	public int getPartNum() {
 		return partNum;
 	}
 
 	public void setDescription(String desc) {
 		this.description = desc;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public float getBadChance() {
 		return badChance;
 	}
 
 	public void setBadChance(float badChance) {
 		this.badChance = badChance;
 	}
 
 	public String getID() {
 		return id;
 	}
 
 	public Image getImage() {
 		return Toolkit.getDefaultToolkit().getImage(
 				Constants.PART_IMAGE_PATH + imagePath + ".png");
 	}
 	
 	//Added this for testing --Neetu
 	public Image getPokeballImage() {
 		return Toolkit.getDefaultToolkit().getImage(Constants.BALL_IMAGE + imagePath + ".png");
 	}
 
 	public Image getBinImage() {
 		return Toolkit.getDefaultToolkit().getImage(
 				Constants.BIN_IMAGE_PATH + imagePath + ".png");
 	}
 
 	public boolean equals(PartType pt) {
 		return this.id.equals(pt.getID());
 	}
 	
 	public void setImagePath (String newImagePath) {
 		imagePath = newImagePath;
 	}
	
	public String getImagePath () {
		return imagePath;
	}
 }
