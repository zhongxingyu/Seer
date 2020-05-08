 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 public class SkillsTimer extends TimerTask{
 	static final Logger log = Logger.getLogger("Minecraft");
 	SkillsListener parent;
 	
 	public SkillsTimer(SkillsListener parent)
 	{
 	    this.parent = parent;
 	}
 	
 	public void run(){
 		try {
 			this.parent.playersFile = new PropertiesFile("Skills.txt");
 			this.parent.playersFile.load();
 			Enumeration<Player> keys = this.parent.playersList.keys();
 			while(keys.hasMoreElements()){
 				SkillsPlayer sp = this.parent.playersList.get(keys.nextElement());
 				String skills = "";
				for(int i = 0; i < this.parent.Props.Skills.length; i++){
					if(i > 0){
 						skills = skills + ":";
 					}
 					skills = skills+String.valueOf(sp.getExp(i));
 				}
 				parent.playersFile.setString(sp.getName(), skills);
 			}
 			parent.playersFile.save();
 		} catch (IOException ioe) {}
 	}
 }
