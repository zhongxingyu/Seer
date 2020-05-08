 package annahack.nethackinformation.nethackplayer;
 
 public class PlayerWriter 
 {
 	private Player player;
 	public PlayerWriter()
 	{
 		player = new Player();
 	}
 	
	public PlayerWriter(PlayerClass pclass, PlayerRace race, boolean isFem, byte alignment)
 	{
 		player = new Player(pclass, race, isFem, alignment);
 	}
 	
 	public Player getPlayer()
 	{
 		return player;
 	}
 	
 	public void setHp(int hp)
 	{
 		player.setHp(hp);
 	}
 	
 	public void setAlign(byte align)
 	{
 		player.setAlign(align);
 	}
 	
 	public void setCh(int ch)
 	{
 		player.setCh(ch);
 	}
 	
 	public void setDx(int dx)
 	{
 		player.setDx(dx);
 	}
 	
 	public void setCo(int co)
 	{
 		player.setCo(co);
 	}
 	
 	public void setSt(int st)
 	{
 		player.setSt(st);
 	}
 	
 	public void setIn(int in)
 	{
 		player.setIn(in);
 	}
 	
 	public void setWi(int wi)
 	{
 		player.setWi(wi);
 	}
 	
 	public void setFemale(boolean fem)
 	{
 		player.setFemale(fem);
 	}
 	
 	public void setMale(boolean mal)
 	{
 		player.setMale(mal);
 	}
 	
 	public void setGender(String gender)
 	{
 		if(gender.toLowerCase().equals("male"))
 		{
 			setMale(true);
 			setFemale(false);
 		}
 		else if(gender.toLowerCase().equals("female"))
 		{
 			setMale(false);
 			setFemale(true);
 		}
 		else if(gender.toLowerCase().equals("neuter")||gender.toLowerCase().equals("eunuch"))
 		{
 			setMale(false);
 			setFemale(false);
 		}
 	}
 	
 	public void setLevel(int level)
 	{
 		player.setLevel(level);
 	}
 	
 	public void setMaxHp(int maxhp)
 	{
 		player.setMaxhp(maxhp);
 	}
 	
 	public void setMaxMp(int maxmp)
 	{
 		player.setMaxmp(maxmp);
 	}
 	
 	public void setMp(int mp)
 	{
 		player.setMp(mp);
 	}
 	
 	public void setXp(int xp)
 	{
 		player.setXp(xp);
 	}
 	
 }
