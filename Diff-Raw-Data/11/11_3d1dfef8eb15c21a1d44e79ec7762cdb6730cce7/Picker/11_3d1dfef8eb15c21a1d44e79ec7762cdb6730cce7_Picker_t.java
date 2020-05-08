 /**
  * 
  */
 package de.findus.cydonia.player;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import com.jme3.scene.Node;
 
 import de.findus.cydonia.level.Flube;
 import de.findus.cydonia.main.MainController;
 import de.findus.cydonia.messages.EquipmentInfo;
 import de.findus.cydonia.messages.PickerInfo;
 
 /**
  * @author Findus
  *
  */
 public abstract class Picker extends AbstractEquipment {
 
 	private static final String TYPENAME = "Picker";
 	
 	private static Image[] hudImgs;
 	
 	private String name;
 	
 	private float range;
 	
 	private int capacity;
 	
 	private List<Flube> repository = new LinkedList<Flube>();
 	
 	public Picker() {
 		initHUDImgs();
 	}
 	
 	public Picker(String name, float range, int capacity, Player player, MainController mainController) {
 		super(mainController);
 		
 		this.name = name;
 		this.range = range;
 		this.capacity = capacity;
 		this.player = player;
 		
 		initHUDImgs();
 	}
 	
 	private void initHUDImgs() {
 		try {
 			if(hudImgs == null) {
 				hudImgs = new Image[3];
 				hudImgs[0] = ImageIO.read(this.getClass().getResourceAsStream("/de/findus/cydonia/gui/hud/inventory_gold.png"));
 				hudImgs[1] = ImageIO.read(this.getClass().getResourceAsStream("/de/findus/cydonia/gui/hud/inventory_blue.png"));
 				hudImgs[2] = ImageIO.read(this.getClass().getResourceAsStream("/de/findus/cydonia/gui/hud/inventory_red.png"));
 			}
 		} catch (IOException e) {
 		}
 	}	
 	
 	@Override
 	public void reset() {
//		this.repository = new LinkedList<Flube>();
 	}
 
 	@Override
 	public EquipmentInfo getInfo() {
 		return new PickerInfo(this);
 	}
 
 	@Override
 	public void loadInfo(EquipmentInfo info) {
 		if(info instanceof PickerInfo) {
 			PickerInfo i = (PickerInfo) info;
 			this.name = i.getName();
 			this.range = i.getRange();
 			this.capacity = i.getCapacity();
 			this.repository = new LinkedList<Flube>();
 			for (Long id : i.getRepository()) {
 				this.repository.add(getMainController().getWorldController().getFlube(id));
 			}
 		}
 	}
 
 	@Override
 	public BufferedImage getHUDImage() {
 		BufferedImage tmpimg = new BufferedImage(35*this.capacity, 35, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D gr = (Graphics2D) tmpimg.getGraphics();
 		
 		int imgpos = 0;
 		for(Flube f : this.repository) {
 			if(f.getType() >= 0) {
 				gr.drawImage(hudImgs[f.getType()], imgpos, 0, new Color(0, 0, 0, 0), null);
 				imgpos += 35;
 			}
 		}
 
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
 
 	/**
 	 * @return the capacity
 	 */
 	public int getCapacity() {
 		return capacity;
 	}
 
 	/**
 	 * @param capacity the capacity to set
 	 */
 	public void setCapacity(int capacity) {
 		this.capacity = capacity;
 	}
 
 	/**
 	 * @return the repository
 	 */
 	public List<Flube> getRepository() {
 		return repository;
 	}
 
 	/**
 	 * @param repository the repository to set
 	 */
 	public void setRepository(List<Flube> repository) {
 		this.repository = repository;
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
 }
