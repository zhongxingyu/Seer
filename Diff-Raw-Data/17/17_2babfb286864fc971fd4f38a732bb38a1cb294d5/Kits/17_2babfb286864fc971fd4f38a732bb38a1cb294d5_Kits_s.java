 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author jonathan
  */
 public class Kits extends Mod {
 	protected static Kitlist kits;
 
 	@Override
 	public void activate() {
 		Kits.kits = new Kitlist();
 		Kits.kits.load();
 	}
 
 	public boolean parseCommand(Player player, String[] tokens) {
 		if( tokens.length < 1 ) return false;
		String command = tokens[0].substring(1);
 
		if((command.equalsIgnoreCase("kit") && tokens.length < 2) ||
						(command.equalsIgnoreCase("kits")) ) {
 			player.sendChat("Available Kits:", Color.Gray);
 			kits.printKits(player);
 			return true;
 		}
		else if( command.equalsIgnoreCase("kit") ) {
 			this.generateKit(player, tokens[1]);
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return "!kits; !kit <name>";
 	}
 	
 	@Override
 	public boolean onPlayerChat(Player player, String command) {
 		String[] tokens = command.split(" ");
 		return this.parseCommand(player, tokens);
 	}
 
 	@Override
 	public boolean onPlayerCommand(Player player, String[] tokens) {
 		return this.parseCommand(player, tokens);
 	}
 	
 	protected void generateKit(Player player, String name) {
 		if( kits.kits.containsKey(name) ) {
 			player.sendChat("Generating Kit:"+name);
 			kits.kits.get(name).giveTo(player);
 		}
 		else {
 			player.sendChat("Unknown Kit: "+name);
 		}
 	}
 	
 }
