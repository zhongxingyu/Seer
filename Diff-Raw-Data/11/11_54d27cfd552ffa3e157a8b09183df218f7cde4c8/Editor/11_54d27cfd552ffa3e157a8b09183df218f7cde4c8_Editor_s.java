 /**
  * 
  */
 package de.findus.cydonia.player;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import com.jme3.scene.Node;
 
 import de.findus.cydonia.main.MainController;
 import de.findus.cydonia.messages.EditorInfo;
 import de.findus.cydonia.messages.EquipmentInfo;
 
 /**
  * @author Findus
  *
  */
 public abstract class Editor extends AbstractEquipment {
 
 	private static final String TYPENAME = "Editor";
 	
 	private static Image[] hudImgs;
 	
 	private String name;
 	
 	private int objectSpec;
 	
 	private String objectType;
 	
 	private float range;
 	
 	public Editor() {
 		initHUDImgs();
 	}
 	
 	public Editor(String name, float range, String objectType, int objectSpec, Player player, MainController mainController) {
 		super(mainController);
 		
 		this.name = name;
 		this.range = range;
 		this.setObjectType(objectType);
 		this.player = player;
 		
 		initHUDImgs();
 	}
 	
 	private void initHUDImgs() {
 		try {
 			if(hudImgs == null) {
 				hudImgs = new Image[9];
 				hudImgs[0] = ImageIO.read(this.getClass().getResourceAsStream("/de/findus/cydonia/gui/hud/inventory_darkgray.png"));
 				hudImgs[1] = ImageIO.read(this.getClass().getResourceAsStream("/de/findus/cydonia/gui/hud/inventory_lightgray.png"));
 				hudImgs[2] = ImageIO.read(this.getClass().getResourceAsStream("/de/findus/cydonia/gui/hud/inventory_gold.png"));
 				hudImgs[3] = ImageIO.read(this.getClass().getResourceAsStream("/de/findus/cydonia/gui/hud/inventory_blue.png"));
 				hudImgs[4] = ImageIO.read(this.getClass().getResourceAsStream("/de/findus/cydonia/gui/hud/inventory_red.png"));
 				hudImgs[5] = ImageIO.read(this.getClass().getResourceAsStream("/de/findus/cydonia/gui/hud/flag_blue.png"));
 				hudImgs[6] = ImageIO.read(this.getClass().getResourceAsStream("/de/findus/cydonia/gui/hud/flag_red.png"));
 				hudImgs[7] = ImageIO.read(this.getClass().getResourceAsStream("/de/findus/cydonia/gui/hud/spawn_blue.png"));
 				hudImgs[8] = ImageIO.read(this.getClass().getResourceAsStream("/de/findus/cydonia/gui/hud/spawn_red.png"));
 			}
 		} catch (IOException e) {
 		}
 	}
 	
 	@Override
 	public void reset() {
 
 	}
 
 	@Override
 	public EquipmentInfo getInfo() {
 		return new EditorInfo(this);
 	}
 
 	@Override
 	public void loadInfo(EquipmentInfo info) {
 		if(info instanceof EditorInfo) {
 			EditorInfo i = (EditorInfo) info;
 			this.name = i.getName();
 			this.range = i.getRange();
 			this.objectType = i.getObjectType();
 		}
 	}
 
 	@Override
 	public BufferedImage getHUDImage() {
 		BufferedImage tmpimg = new BufferedImage(35, 35, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D gr = (Graphics2D) tmpimg.getGraphics();
 		
 		int imgIndex = -1;
 		if("flube".equalsIgnoreCase(objectType)) {
 			imgIndex = getObjectSpec()+2;
 		}else if("flag".equalsIgnoreCase(objectType)) {
 			imgIndex = getObjectSpec()+4;
 		}else if("spawnpoint".equalsIgnoreCase(objectType)) {
 			imgIndex = getObjectSpec()+6;
 		}
 		gr.drawImage(hudImgs[imgIndex], 0, 0, new Color(0, 0, 0, 0), null);
 
 		return tmpimg;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the range
 	 */
 	public float getRange() {
 		return range;
 	}
 
 	/**
 	 * @param range the range to set
 	 */
 	public void setRange(float range) {
 		this.range = range;
 	}
 
 	@Override
 	public Node getGeometry() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void setActive(boolean active) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public String getTypeName() {
 		return TYPENAME;
 	}
 
 	@Override
 	public void initGeometry() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public String getObjectType() {
 		return objectType;
 	}
 
 	public void setObjectType(String objectType) {
 		this.objectType = objectType;
 	}
 
 	public int getObjectSpec() {
 		return objectSpec;
 	}
 
 	public void setObjectSpec(int objectSpec) {
 		this.objectSpec = objectSpec;
 	}
 }
