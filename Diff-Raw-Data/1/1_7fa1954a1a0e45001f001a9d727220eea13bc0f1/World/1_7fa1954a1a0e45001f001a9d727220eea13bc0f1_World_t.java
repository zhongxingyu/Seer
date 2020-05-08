 package com.hexcore.cas.model;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.hexcore.cas.utilities.Log;
 /**
  * Class World
  * Contains all information about a world.
  * Generations :
  * 	No history - only current generation being displayed;
  * 	Memory history - all generations that memory can hold; and
  * 	Disk history - all generations from the very beginning. 
  * Rule and colour sets.
  * Load and saving of a world by calling a WorldLoader and WorldSaver.
  * 
  * @author Megan
  */
 public class World
 {
 	private int historyType = 1;
 	private int genAmount = 0;
 	private List<Grid> worldGenerations = null;
 	private String worldFileName = null;
 	private String ruleCode = null;
 	private String colourCode = null;
 	private WorldStreamer streamer = null;
 
 	private static final String TAG = "World";
 	
 	public World()
 	{
 		worldGenerations = Collections.synchronizedList(new ArrayList<Grid>());
 		
 		streamer = new WorldStreamer();
 	}
 	
 	public World(World w)
 	{
 		this.historyType = w.historyType;
 		this.worldFileName = w.worldFileName;
 		this.ruleCode = w.ruleCode;
 		this.colourCode = w.colourCode;
 		this.worldGenerations.clear();
 		this.worldGenerations.addAll(w.worldGenerations);
 		
 		if(historyType == 2)
 		{
 			streamer = new WorldStreamer();
 			streamer.start(this);
 		}
 	}
 	
 	public void addGeneration(Grid gen)
 	{
 		worldGenerations.add(gen);
 		
 		
 		if(historyType == 0 || historyType == 2)
 		{
 			genAmount++;
 			
 			if(worldGenerations.size() > 1)
 				for(int i = 0; i < worldGenerations.size() - 1; i++)
 					worldGenerations.remove(0);
 		}
 		
 		if(historyType == 2)
 			streamer.streamGeneration(gen);
 	}
 	
 	public boolean clearHistory()
 	{
 		int size = worldGenerations.size();
 		for(int i = 0; i < size - 1; i++)
 			worldGenerations.remove(0);
 		
 		genAmount = worldGenerations.size();
 		
 		return true;
 	}
 	
 	public boolean clearHistory(int genNumber)
 	{
 		if(historyType == 0 || historyType == 2)
 			return false;
 		
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
 		if(historyType == 0 || historyType == 2)
 			return genAmount;
 		else if(historyType == 1)
 			return worldGenerations.size();
 		else
 			return -1;
 	}
 	
 	public List<Grid> getGenerations()
 	{
 		if(historyType == 2 && streamer.hasStarted())
 			return streamer.getGenerations(this);
 		else
 			return worldGenerations;
 	}
 	
 	public Grid getGeneration(int index)
 	{
 		if(historyType == 0 && index == 0)
 			return worldGenerations.get(0);
 		else if(historyType == 1)
 		{
 			if(index >= worldGenerations.size())
 			{
 				Log.error(TAG, "Generation " + index + " not found!");
 				return null;
 			}
 			
 			return worldGenerations.get(index);
 		}
 		else if(historyType == 2)
 		{
 			if(index == (genAmount - 1))
 			{
 				return worldGenerations.get(0);
 			}
 			else
 			{
 				worldGenerations.clear();
 				worldGenerations.add(streamer.getGeneration(index));
 				return worldGenerations.get(0);
 			}
 		}
 		else
 			return null;
 	}
 	
 	public int getHistoryType()
 	{
 		return historyType;
 	}
 	
 	public Grid getLastGeneration()
 	{
 		if(historyType == 0 && !worldGenerations.isEmpty())
 			return worldGenerations.get(0);
 		else if(historyType == 1 && !worldGenerations.isEmpty())
 			return worldGenerations.get(worldGenerations.size() - 1);
 		else if(historyType == 2)
 		{
 			worldGenerations.clear();
 			worldGenerations.add(streamer.getLastGeneration());
 			genAmount = worldGenerations.size();
 			return worldGenerations.get(0);
 		}
 		else
 			return null;
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
 		if(worldGenerations.isEmpty())
 			return null;
 		else if(historyType == 2 && streamer.hasStarted())
 		{
 			if(genAmount == 1)
 			{
 				return worldGenerations.get(0);
 			}
 			else
 			{
 				worldGenerations.clear();
 				worldGenerations.add(streamer.getGeneration(0));
 				return worldGenerations.get(0);
 			}
 		}
 		else
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
 	
 	public boolean hasStarted()
 	{
 		return streamer.hasStarted();
 	}
 	
 	public boolean isHistoryKept()
 	{
 		if(historyType == 0)
 			return false;
 		else
 			return true;
 	}
 	
 	public void reset()
 	{
 		Grid g = worldGenerations.get(0).clone();
 		worldGenerations.clear();
 		worldGenerations.add(g.clone());
 
 		if(historyType == 0 || historyType == 2)
 			genAmount = worldGenerations.size();
 		
 		if(historyType == 2)
 			streamer.reset(this);
 	}
 	
 	public void resetTo(Grid g)
 	{
 		Log.debug(TAG, "Resetting world to grid: " + g.getWidth() + "x" + g.getHeight());
 		
 		worldGenerations.clear();
 		worldGenerations.add(g.clone());
 
 		if(historyType == 0 || historyType == 2)
 			genAmount = worldGenerations.size();
 		
 		if(historyType == 2)
 			streamer.reset(this);
 	}
 	
 	public void resetTo(World w)
 	{
 		Log.debug(TAG, "Resetting world.");
 
 		this.worldFileName = w.worldFileName;
 		this.ruleCode = w.ruleCode;
 		this.colourCode = w.colourCode;
 		this.worldGenerations.clear();
 		
 		if(historyType != 0)
 			this.worldGenerations.addAll(w.worldGenerations);
 		else
 			this.worldGenerations.add(w.worldGenerations.get(w.worldGenerations.size() - 1));
 
 		if(historyType == 0 || historyType == 2)
 			genAmount = worldGenerations.size();
 		
 		if(historyType == 2)
 			streamer.reset(this);
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
 
 	public void setKeepHistory(int hT)
 	{
 		historyType = hT;
 	}
 	
 	public void setWorldGenerations(Grid[] w)
 	{
 		worldGenerations.clear();
 		
 		if(historyType == 0)
 		{
 			worldGenerations.add(w[w.length - 1]);
 			return;
 		}
 		
 		for(Grid grid : w) worldGenerations.add(grid);
 		
 		if(historyType == 0 || historyType == 2)
 			genAmount = worldGenerations.size();
 		
 		if(historyType == 2 && streamer.hasStarted())
 			streamer.reset(this);
 	}
 	
 	public void start()
 	{
 		if(historyType != 2)
 			return;
 		
 		if(!streamer.hasStarted())
 		{
 			Log.debug(TAG, "Starting streamer");
 			streamer.start(this);
 		}
 	}
 	
 	public void stop()
 	{
 		if(historyType != 2)
 			return;
 		
 		Log.debug(TAG, "stop() called");
 		
 		if(streamer.hasStarted())
 			streamer.stop();
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
