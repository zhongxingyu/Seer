 package no.runsafe.warpdrive.database;
 
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.api.database.IDatabase;
 import no.runsafe.framework.api.database.ISchemaUpdate;
 import no.runsafe.framework.api.database.Repository;
 import no.runsafe.framework.api.database.SchemaUpdate;
 import no.runsafe.framework.timer.TimedCache;
 
 import java.util.List;
 
 public class WarpRepository extends Repository
 {
 	public WarpRepository(IDatabase db, IScheduler scheduler)
 	{
 		database = db;
 		cache = new TimedCache<String, ILocation>(scheduler);
 	}
 
 	@Override
 	public String getTableName()
 	{
 		return "warpdrive_locations";
 	}
 
 	@Override
 	public ISchemaUpdate getSchemaUpdateQueries()
 	{
 		ISchemaUpdate update = new SchemaUpdate();
 
 		update.addQueries(
 			"CREATE TABLE warpdrive_locations (" +
				"`creator` varchar(255) NOT NULL," +
 				"`name` varchar(255) NOT NULL," +
 				"`public` bit NOT NULL," +
 				"`world` varchar(255) NOT NULL," +
 				"`x` double NOT NULL," +
 				"`y` double NOT NULL," +
 				"`z` double NOT NULL," +
 				"`yaw` double NOT NULL," +
 				"`pitch` double NOT NULL," +
 				"PRIMARY KEY(`creator`,`name`,`public`)" +
 			")"
 		);
 
 		return update;
 	}
 
 	public void Persist(String creator, String name, boolean publicWarp, ILocation location)
 	{
 		database.update(
 			"INSERT INTO warpdrive_locations (creator, name, `public`, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)" +
 				"ON DUPLICATE KEY UPDATE world=VALUES(world), x=VALUES(x), y=VALUES(y), z=VALUES(z), yaw=VALUES(yaw), pitch=VALUES(pitch)",
 			creator,
 			name,
 			publicWarp,
 			location.getWorld().getName(),
 			location.getX(),
 			location.getY(),
 			location.getZ(),
 			location.getYaw(),
 			location.getPitch()
 		);
 		String key = cacheKey(creator, name, publicWarp);
 		cache.Invalidate(key);
 		cache.Cache(key, location);
 	}
 
 	public List<String> GetPublicList()
 	{
 		return GetWarps(null, true);
 	}
 
 	public List<String> GetPrivateList(String owner)
 	{
 		return GetWarps(owner, false);
 	}
 
 	public ILocation GetPublic(String name)
 	{
 		return GetWarp("", name, true);
 	}
 
 	public ILocation GetPrivate(String owner, String name)
 	{
 		return GetWarp(owner, name, false);
 	}
 
 	public boolean DelPublic(String name)
 	{
 		return DelWarp("", name, true);
 	}
 
 	public boolean DelPrivate(String owner, String name)
 	{
 		return DelWarp(owner, name, false);
 	}
 
 	public void DelAllPrivate(String world)
 	{
 		database.execute("DELETE FROM warpdrive_locations WHERE world=? AND public=?", world, false);
 	}
 
 	private String cacheKey(String creator, String name, boolean publicWarp)
 	{
 		if (publicWarp)
 			return name;
 		return String.format("%s:%s", creator, name);
 	}
 
 	private List<String> GetWarps(String owner, boolean publicWarp)
 	{
 		if (publicWarp)
 			return database.queryStrings("SELECT name FROM warpdrive_locations WHERE `public`=1");
 		else
 			return database.queryStrings("SELECT name FROM warpdrive_locations WHERE `public`=0 AND creator=?", owner);
 	}
 
 	private ILocation GetWarp(String owner, String name, boolean publicWarp)
 	{
 		String key = cacheKey(owner, name, publicWarp);
 		ILocation location = cache.Cache(key);
 		if (location != null)
 			return location;
 
 		if (publicWarp)
 			location = database.queryLocation(
 				"SELECT world, x, y, z, yaw, pitch FROM warpdrive_locations WHERE name=? AND `public`=1",
 				name
 			);
 		else
 			location = database.queryLocation(
 				"SELECT world, x, y, z, yaw, pitch FROM warpdrive_locations WHERE name=? AND `public`=0 AND creator=?",
 				name, owner
 			);
 
 		return cache.Cache(key, location);
 	}
 
 	private boolean DelWarp(String owner, String name, boolean publicWarp)
 	{
 		if (GetWarp(owner, name, publicWarp) == null)
 			return false;
 		boolean success;
 		if (publicWarp)
 			success = database.execute("DELETE FROM warpdrive_locations WHERE name=? AND public=1", name);
 		else
 			success = database.execute("DELETE FROM warpdrive_locations WHERE name=? AND public=0 AND creator=?", name, owner);
 		cache.Invalidate(cacheKey(owner, name, publicWarp));
 		return success;
 	}
 
 	private final IDatabase database;
 	private final TimedCache<String, ILocation> cache;
 }
