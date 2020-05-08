 package com.googlecode.prmf;
 
 class MafiaTeam extends Team {
 	private String name;
 		
 	public MafiaTeam() {
 		name = "MafiaTeam";
 		super.createList();
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public boolean hasWon(Player[] players)
 	{
 		//itt we check for victory
 		//if mafia make up at least 50% of the living players return true
 		int nonMafia = 0, mafia = 0;
 		for (Player p : players)
 		{
 			if (!p.isAlive())
 				continue;
 			if (p.getRole().getTeam().equals(this)) // TODO this looks like a bug to me // why is that a bug?
 				++mafia;
 			else
 				++nonMafia;
 		}
		System.err.println("mafia: " + mafia);
		System.err.println("notmafia: " + nonMafia);
 		return nonMafia <= mafia;
 	}
 	
 	public boolean contains(Player player) {
 		return getList().contains(player);
 	}
 	
 	public boolean agreeOnTarget()
 	{
 		Player target = null;
 		boolean agree = true;
 		for(Player p : getList())
 		{
 			System.err.println(p);
 			System.err.println("target: " + target);
 			if(target == null)
 			{
 				target = p.getRole().getTarget();
 				System.err.println(p.getRole().getName());
 				System.err.println("target is now: " + target);
 			}
 			try
 			{
 			agree = (target.equals(p.getRole().getTarget()));
 			}
 			catch (Exception e)
 			{
 				System.out.println("still sucks");
 			}
 			System.err.println("agree is: " + agree);
 			if(!agree)
 				break;
 		}
 			
 		return agree;
 	
 	}
 	
 }
