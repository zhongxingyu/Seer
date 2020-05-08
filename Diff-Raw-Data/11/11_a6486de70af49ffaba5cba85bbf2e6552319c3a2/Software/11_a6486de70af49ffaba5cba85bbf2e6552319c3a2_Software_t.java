 package sfs2x.extensions.projectsasha.game.entities.software;
 
 import sfs2x.extensions.projectsasha.game.GameConsts;
 
 public abstract class Software 
 {
 	protected int version;	//Software version
 	protected String name;	//Software name (ex: firewall, proxy...)
 	protected int slot;
 	protected boolean cumulative;
 	protected String type;
 	protected boolean hasTriggeredAction;
 	
 	
 	//CONSTRUCTORS
 	
 	public Software(String name, int version)
 	{
 		this.name = name;
 		this.version = version;
 		this.cumulative = false;
 		this.type = "SOFTWARE";
 		this.hasTriggeredAction = false;
 	}
 	
 	
 	//GETTERS
 	
 	public int getDefenceLevel()	//The defensive level (if not overridden returns 0)
 	{
 		return 0;
 	}
 	
 	public int getAttackLevel()		//The offensive level (if not overridden returns 0)
 	{
 		return 0;
 	}
 	
 	public int getVersion()
 	{
 		return version;
 	}
 	
 	public String getName()
 	{
 		return name;
 	}
 	
 	public int getSlot()
 	{
 		return slot;
 	}
 	
 	
 	//SETTERS
 	
 	public void setVersion(int version)
 	{
 		
 		this.version = version;
 	}	
 	
 	public void upgrade()
 	{
 		if(GameConsts.DEBUG)
 			System.out.println("Upgrading "+this.name+" in slot "+this.slot+" from V"+this.version+" to V"+(this.version+1));
 		this.version += 1;
 	}	
 	
 	public void downgrade()
 	{
 		if(this.version == 1){
 			if(GameConsts.DEBUG)
 				System.out.println("Unable to downgrade "+this.name+" since it's already at V"+this.version);
 			return;
 		}
 		if(GameConsts.DEBUG)
 			System.out.println("Downgrading "+this.name+" from V"+this.version+" to V"+(this.version-1));
 		this.version -= 1;
 	}	
 	
 	public void setSlot(int theSlot)
 	{
 		this.slot = theSlot;
 	}
 	
 	public boolean isCumulative()
 	{
 		return this.cumulative;
 	}
 	
 	public void setCumulative(boolean cumulative)
 	{
 		this.cumulative = cumulative;
 	}
 	
 	public String getType()
 	{
 		return this.type;
 	}
 	
 	public void setType(String type)
 	{
 		this.type = type;
 	}
 	
 	
 	public void setTriggeredAction(boolean hasTriggeredAction)
 	{
 		this.hasTriggeredAction = hasTriggeredAction;
 	}
 	
 	public boolean getTriggeredAction()
 	{
 		return this.hasTriggeredAction;
 	}
 	
	public void runTriggeredAction(){
 		return;
 	}
 }
