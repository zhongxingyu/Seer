 package sfs2x.projectsasha.game.entities.software;
 
 import sfs2x.projectsasha.game.GameConsts;
 import sfs2x.projectsasha.game.entities.gateways.Gateway;
 
 
 public class Virus extends Software
 {
 	
 	
 	public Virus() 
 	{
 		super(GameConsts.VIRUS_NAME, 1);
 		setCumulative(GameConsts.VIRUS_CUMULATIVE);
 		setType(GameConsts.VIRUS);
 		setDescription(GameConsts.VIRUS_DESCRIPTION);
 		setCost(GameConsts.VIRUS_COST,1);
 	}
 	
 	
 	public Virus(int version) 
 	{
 		super(GameConsts.VIRUS_NAME, version);
 		setCumulative(GameConsts.VIRUS_CUMULATIVE);
 		setType(GameConsts.VIRUS);
 		setDescription(GameConsts.VIRUS_DESCRIPTION);
 		setCost(GameConsts.VIRUS_COST,version);
 	}
 	
 	@Override
 	public void upgrade()
 	{
 		if (this==null)
 			return;
 		if(this.version < GameConsts.VIRUS_MAX_LEVEL)
 			this.version += 1;
 		else
 			return;
 	}	
 	
 	@Override
 	public void runTriggeredAction(Gateway from, Gateway to)
 	{
		if(from.hasSoftware(GameConsts.ANTIVIRUS))
 		{
			Software s = (Antivirus)from.getInstalledSoftware(GameConsts.ANTIVIRUS);
 			s.runTriggeredAction(from, to);
 		}
		if(to.hasSoftware(GameConsts.VIRUS))
 		{
			Software[] lista = from.getInstalledSoftwares();
 			for(int i=0;i<this.getVersion();i++)
				from.uninstallSoftware(lista[i].getType(), from.getOwner());
 		}
 	}
 }
