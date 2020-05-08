 package sfs2x.extensions.projectsasha.game.entities;
 
 

public abstract class Quests
 {

 	protected String player;
 	protected String descritption;
 	protected String type;
 	protected String title;
 	protected String goal;
 	protected int reward;
 	
 	//COSTRUCTOR
   
 	public Quests(String player, String description, String title, String goal)
 	{
 		this.player = player;
 		this.descritption = description;
 		this.title = title;
 		this.goal = goal;
 	}
 
 	//GETTERS
   
 	public String getPlayer()
 	{
 		return this.player;
 	}
 	
 	public String getDescription()
 	{
 		return this.descritption;
 	}
 
 	public String getTitle()
 	{
 		return this.title;
 	}
		
 
 	public int getReward()
 	{
 		return this.reward;
 	}
 	
	public void setType(String type)
	{
		this.type = type;
	}
	
 	//SETTER

 	
 
 
 
 
 
 
 
 }
