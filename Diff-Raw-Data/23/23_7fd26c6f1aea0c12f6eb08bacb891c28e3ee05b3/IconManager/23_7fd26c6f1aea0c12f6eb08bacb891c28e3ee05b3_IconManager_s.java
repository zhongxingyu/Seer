 import java.util.Vector;
 
 
 public class IconManager 
 {
 	Vector<BuildStatusIcon> icons = new Vector<BuildStatusIcon>(); 
 	
 	IconManager(Vector<Build> builds)
 	{
 		for(Build b : builds)
 		{
 			BuildStatusIcon icon = new BuildStatusIcon(b);
 			icons.add(icon);
 		}
 	}
 	
 	public void updateIcons(Vector<Build> builds)
 	{
 		for (Build b : builds)
 		{
 			for (BuildStatusIcon i :icons)
 			{
				if (b.name == i.getBuildName())
 				{
 					i.setStatus(b.condition);
 				}
 			}
 		}
 		//TODO Remove any non exisiting builds
 		//TODO Create new status icon for remaining builds
 	}	
 }
