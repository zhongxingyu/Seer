 package com.legit2.Demigods;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.legit2.Demigods.Libraries.Metrics;
 import com.legit2.Demigods.Libraries.Metrics.Graph;
 
 public class DMetrics
 {
 	static Demigods plugin;
 	
 	public DMetrics(Demigods d) 
 	{
 		plugin = d;
 	}
 
 	public static void allianceStatsPastWeek()
 	{
 		try
 		{
 		    Metrics metrics = new Metrics(plugin);
 		
 		    // New Graph
 		    Graph graph = metrics.createGraph("Alliances for the Past Week");
 		
 		    // Alliance List
 		    ArrayList<String> allianceList = new ArrayList<String>();
 		    for(String player : DSave.getAllPlayersData().keySet())
 		    {
 		    	String alliance;
 		    	if(DSave.getPlayerData(player, "alliance") != null && DSave.getPlayerData(player, "alliance") != "null")
 		    	{
 			    	alliance = DSave.getPlayerData(player, "alliance").toString();
 			    	if(allianceList.contains(alliance)) continue;
 			    	allianceList.add(alliance);
 		    	}
 		    	else
 		    	{
 		    		alliance = "mortal";
 			    	if(allianceList.contains(alliance)) continue;
 			    	allianceList.add(alliance);
 		    	}
 		    }
 		    
 		    for(final String ALLIANCE : allianceList)
 		    {
 			    graph.addPlotter(new Metrics.Plotter(ALLIANCE)
 			    {
 		
 		            @Override
 		            public int getValue()
 		            {
 		            	int numAlliance;
 		            	ArrayList<String> allianceMembers = new ArrayList<String>();
 		            	
 		            	if(DUtil.getImmortalList() == null) numAlliance = 0;
 		            	else
 		            	{
 							for (String s : DUtil.getImmortalList())
 							{
 								if (DUtil.getAlliance(s) != null && DUtil.getAlliance(s).equalsIgnoreCase(ALLIANCE))
 								{
 									if (DSave.hasPlayerData(s, "LASTLOGINTIME"))
 									{
										if ((Long.decode(DSave.getPlayerData(s, "LASTLOGINTIME").toString())) < System.currentTimeMillis()-604800000) continue;
 									}
 									allianceMembers.add(s);
 								}
 							}    
 							numAlliance = allianceMembers.size();
 		            	}
 		            	return numAlliance;
 		            }
 			    });
 			  }
 		
 		    metrics.start();
 		}
 		catch (IOException e)
 		{
 		    DUtil.severe(e.getMessage());
 		}
 	}
 	
 	public static void allianceStatsAllTime()
 	{
 		try
 		{
 		    Metrics metrics = new Metrics(plugin);
 		
 		    // New Graph
 		    Graph graph = metrics.createGraph("Alliances for All Time");
 		
 		    // Alliance List
 		    ArrayList<String> allianceList = new ArrayList<String>();
 		    for(String player : DSave.getAllPlayersData().keySet())
 		    {
 		    	String alliance;
 		    	if(DSave.getPlayerData(player, "alliance") != null)
 		    	{
 			    	alliance= DSave.getPlayerData(player, "alliance").toString();
 			    	if(allianceList.contains(alliance)) continue;
 			    	allianceList.add(alliance);
 		    	}
 		    }
 		    
 		    for(final String ALLIANCE : allianceList)
 		    {
 		    	// Better looking name for the graph.
 		    	String graphName = ALLIANCE.substring(0,1).toUpperCase() + ALLIANCE.substring(1) + "s";
 		    	
 			    graph.addPlotter(new Metrics.Plotter(graphName)
 			    {
 		
 		            @Override
 		            public int getValue()
 		            {
 		            	int numAlliance;
 		            	ArrayList<String> allianceMembers = new ArrayList<String>();
 		            	
 		            	if(DUtil.getImmortalList() == null) numAlliance = 0;
 		            	else
 		            	{
 							for (String s : DUtil.getImmortalList())
 							{
 								if (DUtil.getAlliance(s) != null && DUtil.getAlliance(s).equalsIgnoreCase(ALLIANCE))
 								{
 									allianceMembers.add(s);
 								}
 							}    
 							numAlliance = allianceMembers.size();
 		            	}
 		            	return numAlliance;
 		            }
 			    });
 			  }
 		
 		    metrics.start();
 		}
 		catch (IOException e)
 		{
 		    DUtil.severe(e.getMessage());
 		}
 	}
 }
