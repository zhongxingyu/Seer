 package com.hexcore.cas.model;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.hexcore.cas.math.Vector2i;
 /**
  * Class World
  * Contains all information about a world.
  * Generations - end and first, rules, colours,
  * load and saving of a world by calling a worldLoader
  * and WorldSaver function.
  * 
  * @author Megan
  *
  */
 public class World
 {
 	private List<Grid> worldGenerations = null;
 	private String worldFileName = null;
 	private String ruleCode = null;
 	private String colourCode = null;
 	
 	public World()
 	{
 		worldGenerations = Collections.synchronizedList(new ArrayList<Grid>());
 	}
 	
 	public void addGeneration(Grid gen)
 	{
 		worldGenerations.add(gen);
 	}
 	
 	public boolean clearHistory(int genNumber)
 	{
 		if(worldGenerations.size() <= genNumber)
 		{
 			return false;
 		}
 		else
 		{
 			for(int i = 0; i < genNumber; i++)
 				worldGenerations.remove(0);
 			return true;
 		}
 	}
 	
 	public int getNumGenerations()
 	{
 		return worldGenerations.size();
 	}
 	
 	public List<Grid> getGenerations()
 	{
 		return worldGenerations;
 	}
 	
 	public Grid getGeneration(int index)
 	{
 		return worldGenerations.get(index);
 	}
 	
 	public Grid getLastGeneration()
 	{
 		if (worldGenerations.isEmpty()) return null;
 		return worldGenerations.get(worldGenerations.size() - 1);
 	}
 	
 	public String getRuleCode()
 	{
 		return ruleCode;
 	}
 	
 	public String getColourCode()
 	{
 		return colourCode;
 	}
 	
 	public Grid getInitialGeneration()
 	{
 		return worldGenerations.get(0);
 	}
 	
 	public String getFilename()
 	{
 		return worldFileName;
 	}
 	
 	public String getWorldName()
 	{
 		if(worldFileName.lastIndexOf('/') != -1)
 			return worldFileName.substring(worldFileName.lastIndexOf('/') + 1);
 		else
 			return worldFileName;
 	}
 	
 	public void reset()
 	{
 		Grid g = worldGenerations.get(0).clone();
 		worldGenerations.clear();
 		worldGenerations.add(g.clone());
 	}
 	
 	public void resetTo(Grid g)
 	{
 		worldGenerations.clear();
 		worldGenerations.add(g.clone());
 	}
 	
 	public void setRuleCode(String ruleCode)
 	{
 		this.ruleCode = ruleCode;
 	}	
 	
 	public void setColourCode(String colourCode)
 	{
 		this.colourCode = colourCode;
 	}
 
 	public void setFileName(String name)
 	{
 		worldFileName = name;
 	}
 
 	public void setWorldGenerations(Grid[] w)
 	{
 		worldGenerations.clear();
 		for (Grid grid : w) worldGenerations.add(grid);
 	}
 	
 	public boolean load()
 	{
 		try
 		{
 			WorldReader wr = new WorldReader(this);
 			return wr.readWorld(worldFileName);
 		}
 		catch (IOException e)
 		{
 			return false;
 		}
 	}
 	
 	public void save()
 		throws IOException
 	{
 		WorldSaver ws = new WorldSaver();		
 		ws.saveWorld(this);
 	}
 }
