 /**
  * 
  */
 package de.findus.cydonia.player;
 
 import java.awt.image.BufferedImage;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 
 import de.findus.cydonia.level.Flube;
 import de.findus.cydonia.messages.EquipmentInfo;
 
 /**
  * @author Findus
  *
  */
 public class Flucob extends AbstractEquipment {
 
 	private int id;
 	
 	private String name;
 	
 	private float range;
 	
 	private List<Flube> repository = new LinkedList<Flube>();
 	
 	private Spatial model;
 	
 	private RigidBodyControl control;
 	
 	public Flucob() {
 		
 	}
 	
 	
 	@Override
 	public void usePrimary(boolean activate) {
 		
 	}
 
 	@Override
 	public void useSecondary(boolean activate) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void reset() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public BufferedImage getHUDImage() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public EquipmentInfo getInfo() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void loadInfo(EquipmentInfo info) {
 		// TODO Auto-generated method stub
 
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
 
 }
