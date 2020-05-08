 package no.runsafe.framework.server;
 
 import no.runsafe.framework.minecraft.Sound;
 import no.runsafe.framework.server.block.RunsafeBlock;
 import no.runsafe.framework.server.entity.RunsafeEntity;
 import no.runsafe.framework.server.entity.RunsafeFallingBlock;
 import no.runsafe.framework.server.entity.RunsafeItem;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.metadata.RunsafeMetadata;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class RunsafeWorld extends RunsafeMetadata
 {
 	public RunsafeWorld(World toWrap)
 	{
 		super(toWrap);
 		world = toWrap;
 	}
 
 	public RunsafeWorld(String worldName)
 	{
 		this(RunsafeServer.Instance.getWorld(worldName).getRaw());
 	}
 
 	public String getName()
 	{
 		return world.getName();
 	}
 
 	public boolean equals(RunsafeWorld world)
 	{
 		return getName().equals(world.getName());
 	}
 
 	public RunsafeBlock getBlockAt(RunsafeLocation location)
 	{
 		return new RunsafeBlock(world.getBlockAt(location.getRaw()));
 	}
 
 	public RunsafeBlock getBlockAt(int x, int y, int z)
 	{
 		return new RunsafeBlock(world.getBlockAt(x, y, z));
 	}
 
 	public int getBlockTypeIdAt(RunsafeLocation location)
 	{
 		return world.getBlockTypeIdAt(location.getRaw());
 	}
 
 	public int getBlockTypeIdAt(int x, int y, int z)
 	{
 		return world.getBlockTypeIdAt(x, y, z);
 	}
 
 	public RunsafeItem dropItem(RunsafeLocation location, RunsafeItemStack itemStack)
 	{
 		return new RunsafeItem((world.dropItem(location.getRaw(), itemStack.getRaw())));
 	}
 
 	public void strikeLightning(RunsafeLocation location)
 	{
 		world.strikeLightning(location.getRaw());
 	}
 
 	public void createExplosion(RunsafeLocation location, float power, boolean setFire)
 	{
 		world.createExplosion(location.getRaw(), power, setFire);
 	}
 
 	public void createExplosion(RunsafeLocation location, float power, boolean setFire, boolean breakBlocks)
 	{
 		world.createExplosion(location.getX(), location.getY(), location.getZ(), power, setFire, breakBlocks);
 	}
 
 	public void createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks)
 	{
 		world.createExplosion(x, y, z, power, setFire, breakBlocks);
 	}
 
 	public RunsafeFallingBlock spawnFallingBlock(RunsafeLocation location, Material material, Byte blockData)
 	{
 		return ObjectWrapper.convert(world.spawnFallingBlock(location.getRaw(), material, blockData));
 	}
 
 	public World getRaw()
 	{
 		return this.world;
 	}
 
 	public int getMaxHeight()
 	{
 		return world.getMaxHeight();
 	}
 
 	public RunsafeEntity spawnCreature(RunsafeLocation location, String type)
 	{
 		return ObjectWrapper.convert(world.spawnEntity(location.getRaw(), EntityType.fromName(type)));
 	}
 
 	public RunsafeEntity spawnCreature(RunsafeLocation location, int id)
 	{
 		return ObjectWrapper.convert(world.spawnEntity(location.getRaw(), EntityType.fromId(id)));
 	}
 
 	public void strikeLightningEffect(RunsafeLocation location)
 	{
 		world.strikeLightningEffect(location.getRaw());
 	}
 
 	public List<RunsafePlayer> getPlayers()
 	{
 		ArrayList<RunsafePlayer> result = new ArrayList<RunsafePlayer>();
 		for (Player p : world.getPlayers())
 			result.add(new RunsafePlayer(p));
 		return result;
 	}
 
 	public List<RunsafeEntity> getEntities()
 	{
 		return ObjectWrapper.convert(world.getEntities());
 	}
 
 	public RunsafeEntity getEntityById(int id)
 	{
 		for (Entity entity : world.getEntities())
 			if (entity.getEntityId() == id)
 				return ObjectWrapper.convert(entity);
 		return null;
 	}
 
 	public <T extends Entity> T spawn(RunsafeLocation location, Class<T> mob)
 	{
 		return this.world.spawn(location.getRaw(), mob);
 	}
 
 	public void playEffect(RunsafeLocation location, Effect effect, int data)
 	{
 		this.world.playEffect(location.getRaw(), effect, data);
 	}
 
 	public void playSound(RunsafeLocation location, Sound sound, float volume, float pitch)
 	{
		this.world.playSound(location.getRaw(), sound.getSound(), volume, pitch);
 	}
 
 	private final World world;
 }
