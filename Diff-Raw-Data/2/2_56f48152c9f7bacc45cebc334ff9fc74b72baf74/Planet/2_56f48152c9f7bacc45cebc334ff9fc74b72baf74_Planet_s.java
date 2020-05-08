 package com.araeosia.space.util;
 
 import org.bukkit.util.Vector;
 
 public class Planet {
 	private int x;
 	private int y;
 	private int z;
 	private int chunkX;
 	private int chunkZ;
 	private int totalRadius;
 	private int coreRadius;
 	private int shellMaterial;
 	private int coreMaterial;
 
 	public Planet(int coreMaterial, int coreRadius, int shellMaterial, int totalRadius, int x, int y, int z, int chunkX, int chunkZ) {
 		this.coreMaterial = coreMaterial;
 		this.coreRadius = coreRadius;
 		this.shellMaterial = shellMaterial;
 		this.totalRadius = totalRadius;
 		this.x = x;
 		this.y = y;
 		this.z = z;
 		this.chunkX = chunkX;
 		this.chunkZ = chunkZ;
 	}
 
 	public Integer getX() {
 		return x;
 	}
 
 	public Integer getY() {
 		return y;
 	}
 
 	public Integer getZ() {
 		return z;
 	}
 
 	public Integer getChunkX() {
 		return chunkX;
 	}
 
 	public Integer getChunkZ() {
 		return chunkZ;
 	}
 
 	public Vector getVector(){
		return new Vector(x, y, z);
 	}
 
 	public Integer getTotalRadius() {
 		return totalRadius;
 	}
 
 	public Integer getCoreRadius() {
 		return coreRadius;
 	}
 
 	public Integer getShellMaterial() {
 		return shellMaterial;
 	}
 
 	public Integer getCoreMaterial() {
 		return coreMaterial;
 	}
 	public static Planet load(String s){
 		String[] data = s.split("§");
 		return new Planet(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4]), Integer.parseInt(data[5]), Integer.parseInt(data[6]), Integer.parseInt(data[7]), Integer.parseInt(data[8]));
 	}
 	public String save(){
 		return coreMaterial+"§"+coreRadius+"§"+shellMaterial+"§"+totalRadius+"§"+x+"§"+y+"§"+z+"§"+chunkX+"§"+chunkZ;
 	}
 }
