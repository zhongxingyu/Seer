 package com.zand.areaguard.area.cache;
 
 import java.util.ArrayList;
 
 import com.zand.areaguard.area.Cuboid;
 import com.zand.areaguard.area.World;
 import com.zand.areaguard.area.error.ErrorCuboid;
 
 public class CacheWorld extends World implements CacheData {
 	final private CacheStorage storage;
 	final private World world;
 	static private int updateTime = 30000;
 	private long lastUpdate = 0;
 	
 	// Cached Data
 	private boolean exsists;
 	final protected ArrayList<Cuboid> cuboids = new ArrayList<Cuboid>();
 	private String name;
 	
 	public CacheWorld(CacheStorage storage, World world) {
 		super(world.getId());
 		this.storage = storage;
 		this.world = world;
 	}
 
 	@Override
 	public boolean deleteCuboids() {
 		if (deleteCuboids()) {
 			cuboids.clear();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public Cuboid getCuboid(int x, int y, int z) {
 		for (Cuboid cuboid : getCuboids()) {
 			if (cuboid.pointInside(this, x, y, z))
 				return cuboid;
 		}
 		
 		return ErrorCuboid.NOT_FOUND;
 	}
 
 	@Override
 	public Cuboid getCuboid(boolean active, int x, int y, int z) {
 		for (Cuboid cuboid : getCuboids()) {
 			if (cuboid.pointInside(this, x, y, z))
 				return cuboid;
 		}
 		
 		return ErrorCuboid.NOT_FOUND;
 	}
 
 	@Override
 	public ArrayList<Cuboid> getCuboids() {
 		update();
 		return cuboids;
 	}
 
 	@Override
 	public ArrayList<Cuboid> getCuboids(int x, int y, int z) {
 		ArrayList<Cuboid> ret = new ArrayList<Cuboid>();
 		for (Cuboid cuboid : getCuboids()) {
 			if (cuboid.pointInside(this, x, y, z))
 				ret.add(cuboid);
 		}
 		
 		return ret;
 	}
 
 	@Override
 	public String getName() {
 		update();
 		return name;
 	}
 
 	@Override
 	public boolean exsists() {
 		update();
 		return exsists;
 	}
 
 	@Override
 	public boolean update() {
 		long time = System.currentTimeMillis();
 		
 		if (time - lastUpdate > updateTime) {
 			lastUpdate = time;
 			
 			exsists = world.exsists();
 			name = world.getName();
 			cuboids.clear();
 			for (Cuboid cuboid : world.getCuboids())
				if (cuboid.getWorld().getId() == getId() && !cuboids.contains(cuboid))
					cuboids.add(new CacheCuboid(storage, cuboid));
 			
 			System.out.println("Updated World " + getName());
 		}
 		
 		return true;
 	}
 
 }
