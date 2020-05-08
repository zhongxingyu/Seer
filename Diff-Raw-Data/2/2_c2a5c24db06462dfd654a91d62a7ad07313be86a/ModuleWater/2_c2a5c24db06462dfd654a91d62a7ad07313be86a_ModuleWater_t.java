 package holo.essentrika.modules;
 
 import holo.essentrika.grid.IConduit;
 import holo.essentrika.grid.IGenerator;
 import holo.essentrika.map.World;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 public class ModuleWater implements IModule
 {
 	Image sprite;
 	public ModuleWater() throws SlickException
 	{
 		sprite = new Image("res/Water.png");
 	}
 	
 	@Override
 	public int getID() 
 	{
 		return ModuleCreator.moduleWaterID;
 	}
 
 	@Override
 	public void update(World world, int x, int y)
 	{
 		
 	}
 
 	@Override
 	public int getUpgradeCost(IModule upgrade) 
 	{
 		int id = upgrade.getID();
		return id == ModuleCreator.moduleWaterGeneratorID ? 350 : 350;
 	}
 
 	@Override
 	public List<Integer> getUpgrades() 
 	{
 		List<Integer> modules = new ArrayList<Integer>();
 		modules.add(ModuleCreator.moduleWaterGeneratorID);
 		return modules;
 	}
 
 	@Override
 	public Image getIcon(World world, int x, int y)
 	{
 		return sprite;
 	}
 
 	@Override
 	public String getModuleName()
 	{
 		return "Water";
 	}
 	
 	public boolean isGridType(IModule mod)
 	{
 		if (mod instanceof IGenerator || mod instanceof IConduit)
 			return true;
 		return false;
 	}
 
 	@Override
 	public int getUpgradeFromKey(int key)
 	{
 		return key == Input.KEY_G ? ModuleCreator.moduleWaterGeneratorID : -1;
 	}
 
 	@Override
 	public int getKeyFromUpgradeID(int id)
 	{
 		return id == ModuleCreator.moduleWaterGeneratorID ? Input.KEY_G : -1;
 	}
 
 	@Override
 	public void removeModule(World world, int x, int y)
 	{
 		
 	}
 }
