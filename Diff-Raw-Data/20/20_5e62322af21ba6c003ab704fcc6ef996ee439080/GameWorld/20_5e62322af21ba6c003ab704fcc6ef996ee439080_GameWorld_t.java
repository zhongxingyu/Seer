 package game;
 
 import java.util.*;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import serialization.*;
 import ui.isometric.abstractions.IsoSquare;
 import ui.isometric.libraries.IsoRendererLibrary;
 
 public class GameWorld {
 	private final Map<Long, GameThing> allThings = new HashMap<Long, GameThing>();
 	private final Map<Long, Container> allContainers = new HashMap<Long, Container>();
 	private final Map<Integer, Level> levels = new HashMap<Integer, Level>();
 	private final Set<DeltaWatcher> watchers = new HashSet<DeltaWatcher>();
 	private final Map<String, game.things.Player> players = new HashMap<String, game.things.Player>();
 	private final List<game.things.SpawnPoint> spawnpoints = new LinkedList<game.things.SpawnPoint>();
 	
 	private final ReentrantReadWriteLock allThingsLock = new ReentrantReadWriteLock();
 	private final ReentrantReadWriteLock allContainersLock = new ReentrantReadWriteLock();
 	private final ReentrantReadWriteLock levelsLock = new ReentrantReadWriteLock();
 	private final ReentrantReadWriteLock watchersLock = new ReentrantReadWriteLock();
 	private final ReentrantReadWriteLock playersLock = new ReentrantReadWriteLock();
 	private final ReentrantReadWriteLock spawnpointsLock = new ReentrantReadWriteLock();
 
 	public static interface DeltaWatcher {
 		public void delta(WorldDelta d);
 	}
 
 	public void setPlayer(String name, game.things.Player gt){
 		playersLock.writeLock().lock();
 		players.put(name, gt);
 		playersLock.writeLock().unlock();
 	}
 
	public game.things.Player getPlayer(String name, String character){
 		game.things.Player player;
 		
 		if(!players.containsKey(name)){
			player = new game.things.Player(this, character , name);
 		}
 		
 		player = players.get(name);
 		
 		return player;
 	}
 
 	public void addSpawnPoint(game.things.SpawnPoint sp){
 		spawnpointsLock.writeLock().lock();
 		spawnpoints.add(sp);
 		spawnpointsLock.writeLock().unlock();
 	}
 
 	public game.things.SpawnPoint getSpawnPoint(){
 		game.things.SpawnPoint spawnpoint = null;
 		spawnpointsLock.readLock().lock();
 		if(spawnpoints.size() > 0)
 			spawnpoint = spawnpoints.get((int)(Math.random()*spawnpoints.size()));
 		spawnpointsLock.readLock().unlock();
 		return spawnpoint;
 	}
 
 	public GameThing thingWithGID(long gid){
 		return allThings.get(gid);
 	}
 
 	public Container containerWithCID(long cid){
 		return allContainers.get(cid);
 	}
 
 	public Level getLevelFor(GameThing gt){
 		Level out = null;
 		levelsLock.readLock().lock();
 		for(Map.Entry<Integer, Level> l : levels.entrySet())
 			if(l.getValue().contains(gt)) {
 				out = l.getValue();
 				break;
 			}
 		levelsLock.readLock().unlock();
 		return out;
 	}
 
 	private long someUnusedID(Map<Long, ?> m){
 		long r;
 		do
 			r = (long)(Math.random()*((1l << 63) - 1));
 		while(m.containsKey(r));
 		return r;
 	}
 
 	public long introduce(GameThing gt){
 		return introduce(gt, someUnusedID(allThings));
 	}
 
 	public long introduceContainer(Container ct){
 		return introduceContainer(ct, someUnusedID(allContainers));
 	}
 
 	public long introduceContainer(Container ct, long cid){
 		allContainersLock.writeLock().lock();
 		allContainers.put(cid, ct);
 		allContainersLock.writeLock().unlock();
 		return cid;
 	}
 
 	public void forget(GameThing gt){
 		gt.forget();
 		allThingsLock.writeLock().lock();
 		allThings.remove(gt.gid());
 		allThingsLock.writeLock().unlock();
 	}
 
 	// only use on the client
 	public long introduce(GameThing gt, long gid){
 		allThingsLock.writeLock().lock();
 		allThings.put(gid, gt);
 		allThingsLock.writeLock().unlock();
 		return gid;
 	}
 
 	// if a requested level doesn't exist, I'll just create it.
 	public Level level(int n){
 		if(!levels.containsKey(n)) {
 			levelsLock.writeLock().lock();
 			levels.put(n, new Level(this, n));
 			levelsLock.writeLock().unlock();
 		}
 		return levels.get(n);
 	}
 
 	public Set<Integer> levels(){
 		return new HashSet<Integer>(levels.keySet());
 	}
 	
 	public serialization.Tree serialize(){
 		return new serialization.Tree();
 	}
 
 	public void emitPut(GameThing gt, Location where){
 		emit(new WorldDelta(new WorldDelta.Put(gt.gid(), where), -1));
 	}
 
 	public void emitIntroduce(GameThing gt){
 		emit(new WorldDelta(new WorldDelta.Introduce(gt.gid()), -1));
 	}
 
 	public void emitIntroduceContainer(Container gt){
 		emit(new WorldDelta(new WorldDelta.IntroduceContainer(gt.cid()), -1));
 	}
 
 	public void emitForget(GameThing gt){
 		emit(new WorldDelta(new WorldDelta.Forget(gt.gid()), -1));
 	}
 
 	public void emitUpdate(GameThing gt){
 		emit(new WorldDelta(new WorldDelta.Update(new DumbGameThing(gt)), -1));
 	}
 
 	public void emitSay(GameThing gt, String what){
 		emit(new WorldDelta(new WorldDelta.Say(gt.gid(), what), -1));
 	}
 
 	public void emitAnimate(GameThing gt, String what){
 		emit(new WorldDelta(new WorldDelta.Animate(gt.gid(), what), -1));
 	}
 
 	public void emitShowContainer(Container ct, String name, GameThing gt){
 		emit(new WorldDelta(new WorldDelta.ShowContainer(ct.cid(), name), gt.gid()));
 	}
 
 	public void emitEmitSound(GameThing gt, String what){
 		emit(new WorldDelta(new WorldDelta.EmitSound(gt.gid(), what), -1));
 	}
 
 	public void emit(WorldDelta wd){
 		watchersLock.readLock().lock();
 		for(DeltaWatcher dw : watchers)
 			dw.delta(wd);
 		watchersLock.readLock().unlock();
 	}
 
 	public void addDeltaWatcher(DeltaWatcher dw){
 		watchersLock.writeLock().lock();
 		watchers.add(dw);
 		watchersLock.writeLock().unlock();
 	}
 
 	public void removeDeltaWatcher(DeltaWatcher dw){
 		watchersLock.writeLock().lock();
 		watchers.remove(dw);
 		watchersLock.writeLock().unlock();
 	}
 
 	private final Timer timer = new Timer();
 	public void schedule(final Runnable r, long d){
 		timer.schedule(new TimerTask(){
 			public void run(){
 				try{
 					synchronized(GameWorld.this){
 						r.run();
 					}
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 		}, d);
 	}
 
 	public void allDeltas(DeltaWatcher dw){
 		allContainersLock.readLock().lock();
 		for(Container ct : allContainers.values())
 			dw.delta(new WorldDelta(new WorldDelta.IntroduceContainer(ct.cid()), -1));
 		allContainersLock.readLock().unlock();
 		allThingsLock.readLock().lock();
 		for(GameThing gt : allThings.values()){
 			dw.delta(new WorldDelta(new WorldDelta.Introduce(gt.gid()), -1));
 			dw.delta(new WorldDelta(new WorldDelta.Update(new DumbGameThing(gt)), -1));
 			dw.delta(new WorldDelta(new WorldDelta.Put(gt.gid(), gt.location()), -1));
 		}
 		allThingsLock.readLock().unlock();
 	}
 
 	public Tree toTree(){
 		Serializer<GameThing> gts = ThingsS.makeSerializer(this);
 		List<Map.Entry<Location, GameThing>> map = new LinkedList<Map.Entry<Location, GameThing>>();
 		levelsLock.readLock().lock();
 		for(Level level : levels.values()) {
 			level.thingLock().readLock().lock();
 			for(GameThing gt : level) {
 				if(!(gt instanceof game.things.Player))
 					map.add(new AbstractMap.SimpleImmutableEntry<Location, GameThing>(gt.location(), gt));
 			}
 			level.thingLock().readLock().unlock();
 		}
 		levelsLock.readLock().unlock();
 		playersLock.readLock().lock();
 		for(game.things.Player player : players.values())
 			map.add(new AbstractMap.SimpleImmutableEntry<Location, GameThing>(LocationS.NOWHERE, player));
 		playersLock.readLock().unlock();
 		return new Serializers.List<Map.Entry<Location, GameThing>>(Serializers.mapEntry(LocationS.s(null), gts)).write(map);
 	}
 
 	public void fromTree(Tree in) throws ParseException {
 		levelsLock.writeLock().lock();
 		allThingsLock.writeLock().lock();
 		for(GameThing gt : allThings.values()){
 			LocationS.NOWHERE.put(gt);
 			gt.forget();
 		}
 		allThings.clear();
 		allThingsLock.writeLock().unlock();
 //		for(Level l : levels.values())
 //			l.clear();
 		levels.clear();
 		spawnpoints.clear();
 		for(game.things.Player p : players.values())
 			p.clear();
 		levelsLock.writeLock().unlock();
 		Serializer<GameThing> gts = ThingsS.makeSerializer(this);
 		for(Map.Entry<Location, GameThing> lgt : new Serializers.List<Map.Entry<Location, GameThing>>(new Serializers.MapEntry<Location, GameThing>(LocationS.s(this), gts)).read(in))
 			lgt.getKey().put(lgt.getValue());
 	}
 }
