 package sfs2x.extensions.projectsasha.game.entities.software;
 
 import sfs2x.extensions.projectsasha.game.GameConsts;
 
 
 public class Antivirus extends Software
 {
 	
 	
 	public Antivirus() 
 	{
 		super(GameConsts.ANTIVIRUS_NAME, 1);
 		setCumulative(GameConsts.ANTIVIRUS_CUMULATIVE);
 		setType(GameConsts.ANTIVIRUS);
 	}
 	
 	
 	public Antivirus(int version) 
 	{
 		super(GameConsts.ANTIVIRUS_NAME, version);
 		setCumulative(GameConsts.ANTIVIRUS_CUMULATIVE);
 		setType(GameConsts.ANTIVIRUS);
 	}
 	
 	@Override
 	public void upgrade()
 	{
 		if (this==null)
 			return;
 		if(this.version < GameConsts.ANTIVIRUS_MAX_LEVEL)
 		{
 			if(GameConsts.DEBUG)
 				System.out.println("Upgrading "+this.name+" from V"+this.version+" to V"+(this.version+1));
 			this.version += 1;
 		}
 		else
 		{
 			if(GameConsts.DEBUG)
 				System.out.println(this.name+" is already at maximum level (V"+GameConsts.ANTIVIRUS_MAX_LEVEL+")");
 		}
 	}	
 	
 	@Override
 	public String toString(){
 		return this.getName() + " V"+this.getVersion();
 	}
 	
 	
 }
