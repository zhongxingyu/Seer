 package com.github.desmaster.Devio.realm.world;
 
 import org.newdawn.slick.opengl.Texture;
 
 import com.github.desmaster.Devio.cons.Console;
 import com.github.desmaster.Devio.tex.iTexture;
 
 public enum Tile {
 	GRASS("Grass", false, false, false, false, false, iTexture.ID_GRASS),
 	SAND("Sand", false, false, false, false, false, iTexture.ID_SAND),
 	STONE("Stone", false, false, false, false, false, iTexture.ID_STONE),
 	COBBLE_STONE("Cobblestone", false, false, false, false, false, iTexture.ID_COBBLESTONE),
	MOSSY_STONE("Mossy_Cobblestone", false, false, false ,false ,false , iTexture.ID_MOSS_STONE),
 	WATER("Water", false, false, false, false, false, iTexture.ID_WATER);
 
 	private String name;
 	private boolean breakable;
 	private boolean[] solid = new boolean[4];
 	private int TextureID;
 	
 	public int x;
 	public int y;
 
 	Tile(String name, boolean breakable, boolean north_solid, boolean east_solid, boolean south_solid, boolean west_solid,int TextureID) {
 		Console.log("Initialized Tile: " + name);
 		this.name = name;
 		this.breakable = breakable;
 		this.solid = new boolean[]{north_solid, east_solid, south_solid, west_solid};
 		this.TextureID = TextureID;
 	}
 
 	public int getID() {
 		return this.ordinal();
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public String getDisplayName() {
 		return name.replaceAll("_", " ");
 	}
 
 	public boolean isBreakable() {
 		return breakable;
 	}
 
 	public boolean checkCollision(int face) {
 		return solid[face];
 	}
 
 	public Texture getTexture() {
 		return iTexture.textures[TextureID];
 	}
 	public int getTextureID() {
 		return TextureID;
 	}
 	public void setTextureID(int TextureID) {
 		this.TextureID = TextureID;
 	}
 
 }
