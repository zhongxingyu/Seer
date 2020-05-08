 package com.game.world;
 
 import java.util.ArrayList;
 
 import org.lwjgl.opengl.GL11;
 
 import com.game.RenderEngine;
 import com.game.entity.Entity;
 import com.game.entity.EntityPlayer;
 import com.game.world.WorldMap.Collumn;
 
 public class World
 {
 	public EntityPlayer player;
 	private ArrayList<Entity> newEntities;
 	private ArrayList<Entity> entities;
 	public int xScroll;
 	public WorldMap map;
 	
 	public World()
 	{
 		newEntities = new ArrayList<Entity>();
 		entities = new ArrayList<Entity>();
 		
 		player = new EntityPlayer();
 		entities.add(player);
 		
 		map = new WorldMap();
 	}
 	
 	public void tick()
 	{
 		addNewEntities();
 		tickEntities();
 		removeDeadEntities();
 		xScroll = player.getScroll();
 		loadWorld();
 	}
 	
 	public void render()
 	{
 		renderWorldScreen();
 		renderBar();
 	}
 	
 	private void renderWorldScreen()
 	{
 		RenderEngine.push();
 		GL11.glTranslatef(-xScroll, 0, 0);
 		renderWorld();
 		renderEntities();
 		RenderEngine.pop();
 	}
 	
 	private void renderBar()
 	{
 		RenderEngine.push();
 		RenderEngine.setGLColor(0.2f, 0.2f, 0.2f, 1);
 		RenderEngine.fillRect(0, 512, 800, 128);
 		RenderEngine.pop();
 	}
 	
 	public int getBlock(int x, int y)
 	{
		if(map.getCollumn(x) != null && y < map.getCollumn(x).blocks.length & y > 0)
 		{
 			return map.getCollumn(x).blocks[y];
 		}
 		return 0;
 	}
 	
 	public boolean hasBlockCollision(int x, int y)
 	{
 		return Tile.tileList[getBlock(x, y)] != null &&
 				Tile.tileList[getBlock(x, y)].isSolid();
 	}
 	
 	public ArrayList<Entity> getEntityList()
 	{
 		return entities;
 	}
 	
 	public boolean containsEntity(Entity e)
 	{
 		return entities.contains(e);
 	}
 	
 	public void addEntity(Entity e)
 	{
 		newEntities.add(e);
 	}
 	
 	private void addNewEntities()
 	{
 		for(Entity e:newEntities)
 		{
 			if(!entities.contains(e))
 			{
 				entities.add(e);
 			}
 		}
 	}
 	
 	private void tickEntities()
 	{
 		for(Entity e:entities)
 		{
 			e.tick(this);
 		}
 	}
 	
 	private void removeDeadEntities()
 	{
 		for(int i = 0; i < entities.size(); i++)
 		{
 			
 		}
 	}
 		
 	private void renderWorld()
 	{
 		RenderEngine.resetColor();
 		RenderEngine.bindTexture("background.png");
 		RenderEngine.drawTexture(xScroll - xScroll % 800, 0, 800, 512, 0, 0, 256, 256);
 		RenderEngine.drawTexture(xScroll + 800 - xScroll % 800, 0, 800, 512, 0, 0, 256, 256);
 		
 		RenderEngine.bindTexture("sprites.png");
 		
 		int sprite;
 		
 		for(int i = xScroll / 32; i < (xScroll + 800 / 32); i++)
 		{
 			Collumn col = map.getCollumn(i);
 			for(int j = 0; col != null && j < col.blocks.length; j++)
 			{				
 				if((sprite = Tile.tileList[col.blocks[j]].getTexture()) < 0){continue;}
 				
 				RenderEngine.drawTransparentTexture(i * 32, j * 32, 32, 32,
 						sprite * 16, 240, 16, 16);
 			}
 		}
 	}
 	
 	private void renderEntities()
 	{
 		for(Entity e:entities)
 		{
 			e.render();
 		}
 	}
 
 	private void loadWorld()
 	{
 		 if(map.hittingEdge((xScroll + 800) / 32))
 		 {
 			 Collumn[] col = new Collumn[20];
 			 
 			 for(int i = 0; i < 12; i++)
 			 {
 				 col[i] = new Collumn();
 				 col[i].blocks[i] = 1;
 			 }
 			 
 			 map.addNewCollumns(xScroll / 32, col);
 		 }
 	}
 }
