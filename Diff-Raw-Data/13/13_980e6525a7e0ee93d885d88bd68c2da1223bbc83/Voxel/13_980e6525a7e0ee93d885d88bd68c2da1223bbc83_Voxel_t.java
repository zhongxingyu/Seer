 package com.vloxlands.game.voxel;
 
 
 
 public class Voxel
 {
 	private static Voxel[] voxelList = new Voxel[256];
 	
 	private String name = "NA";
 	int textureIndex = 0;
 	boolean opaque = true;
 	boolean replaceable = false;
 	float smoothness = 0;
 	private byte id;
 	private float weight = 1f;
 	private float uplift = 0;
 	
 	public static final Voxel AIR = new Voxel(0).setName("Air").setTextureIndex(0).setOpaque(false).setWeight(0);
 	public static final Voxel STONE = new Voxel(-127).setName("Stone").setTextureIndex(1).setSmoothness(0.1f);
 	public static final Voxel DIRT = new Voxel(-126).setName("Dirt").setTextureIndex(34).setSmoothness(0.3f);
	public static final Voxel WEAK_CRYSTAL = new Voxel(-125).setName("Weak Crystal").setTextureIndex(3).setOpaque(false).setWeight(0).setUplift(1);
	public static final Voxel MEDIUM_CRYSTAL = new Voxel(-124).setName("Medium Crystal").setTextureIndex(35).setOpaque(false).setWeight(0).setUplift(5);
	public static final Voxel STRONG_CRYSTAL = new Voxel(-123).setName("Strong Crystal").setTextureIndex(67).setOpaque(false).setWeight(0).setUplift(20);
 	public static final Voxel GRASS = new Voxel(-122).setName("Grass").setTextureIndex(2).setSmoothness(0.5f);
 	
 	public Voxel(int id)
 	{
 		if (voxelList[id + 128] == null) voxelList[id + 128] = this;
 		else
 		{
 			System.err.println("[Voxel]: The ID " + id + " was already taken up by \"" + voxelList[id + 128].name + "\"");
 			System.exit(1);
 		}
 		this.id = (byte) id;
 	}
 	
 	public static Voxel getVoxelForId(byte id)
 	{
 		return voxelList[(int) id + 128];
 	}
 	
 	public synchronized static Voxel getVoxelForId(int id)
 	{
 		return voxelList[id];
 	}
 	
 	public void onTick(int x, int y, int z)
 	{}
 	
 	public void onNeighbourChange(int x, int y, int z)
 	{}
 	
 	public void onPlaced(int x, int y, int z)
 	{}
 	
 	public void onRemoved(int x, int y, int z)
 	{}
 	
 	public void onDestroyed(int x, int y, int z)
 	{}
 	
 	public void onDestroyedByExplosion(int x, int y, int z)
 	{}
 	
 	public boolean isReplaceable()
 	{
 		return false;
 	}
 	
 	public void setReplaceable(boolean replaceable)
 	{
 		this.replaceable = replaceable;
 	}
 	
 	public Voxel setName(String s)
 	{
 		if (this.name.equals("NA")) this.name = s;
 		else System.err.println("[Voxel] [" + this.name + "] already has a name");
 		return this;
 	}
 	
 	public String getName()
 	{
 		return this.name;
 	}
 	
 	public int getIdForName(String name)
 	{
 		for (int i = 0; i < voxelList.length; i++)
 		{
 			Voxel v = Voxel.getVoxelForId(i);
 			if (v.getName().equals(name)) return i;
 		}
 		System.err.println("[Voxel] [" + this.name + "] not found");
 		return -1;
 	}
 	
 	public Voxel setTextureIndex(int i)
 	{
 		this.textureIndex = i;
 		return this;
 	}
 	
 	public int getTextureIndex()
 	{
 		return textureIndex;
 	}
 	
 	public Voxel setOpaque(boolean b)
 	{
 		this.opaque = b;
 		return this;
 	}
 	
 	public boolean isOpaque()
 	{
 		return opaque;
 	}
 	
 	public float getSmoothness()
 	{
 		return smoothness;
 	}
 	
 	public Voxel setSmoothness(float smooth)
 	{
 		this.smoothness = smooth;
 		return this;
 	}
 	
 	public byte getId()
 	{
 		return id;
 	}
 	
 	public float getWeight()
 	{
 		return weight;
 	}
 	
 	public Voxel setWeight(float weight)
 	{
 		this.weight = weight;
 		return this;
 	}
 	
 	public float getUplift()
 	{
 		return uplift;
 	}
 	
 	public Voxel setUplift(float uplift)
 	{
 		this.uplift = uplift;
 		return this;
 	}
 }
