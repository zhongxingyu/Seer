 package ee.lutsu.alpha.mc.aperf.sys.entity;
 
 import java.util.ArrayList;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.world.World;
 
 import com.google.common.base.Optional;
 
 import ee.lutsu.alpha.mc.aperf.sys.ModuleBase;
 import ee.lutsu.alpha.mc.aperf.sys.objects.Filter;
 import ee.lutsu.alpha.mc.aperf.sys.objects.FilterCollection;
 
 public class EntityModule extends ModuleBase
 {
 	public static EntityModule instance = new EntityModule();
 	public FilterCollection safeList;
 	
 	public EntityModule()
 	{
 		addCommand(new ee.lutsu.alpha.mc.aperf.sys.entity.cmd.EntityList());
 		addCommand(new ee.lutsu.alpha.mc.aperf.sys.entity.cmd.EntityRemoving());
 		
 		visible = false;
 	}
 	
 	public int removeEntities(Filter filter, Entity centered, int range)
 	{
 		int removed = 0;
 		int distSq = range * range;
 		for (World w : MinecraftServer.getServer().worldServers)
 		{
 			ArrayList<Entity> toRemove = new ArrayList<Entity>();
 			for (int i = 0; i < w.loadedEntityList.size(); i++)
 			{
 				Entity ent = (Entity)w.loadedEntityList.get(i);
 				
 				if (ent instanceof EntityPlayer)
 					continue;
 				
 				if (EntitySafeListModule.isEntitySafe(ent))
 					continue;
 				
				if (centered != null && range >= 0 && ent.getDistanceSqToEntity(centered) < distSq)
 					continue;
 
 				if (!filter.hitsAll(ent))
 					continue;
 				
 				toRemove.add(ent);
 			}
 			
 			for (Entity e : toRemove)
 				EntityHelper.removeEntity(e);
 			
 			removed += toRemove.size();
 		}
 		
 		return removed;
 	}
 }
