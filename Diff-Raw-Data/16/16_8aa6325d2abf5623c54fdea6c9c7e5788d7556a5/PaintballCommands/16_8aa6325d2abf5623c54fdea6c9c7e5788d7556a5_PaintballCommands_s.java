 package fr.jules_cesar.Paintball;
 
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.Commands;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.args.CommandArgs;
 import fr.jules_cesar.Paintball.Exception.ArenaAlreadyInGame;
 import fr.jules_cesar.Paintball.Exception.ArenaMissingPlayers;
 import fr.jules_cesar.Paintball.Exception.ArenaNotInitialized;
 import fr.jules_cesar.Paintball.Exception.ArenaNotSet;
 import fr.jules_cesar.Paintball.Exception.PlayerAlreadyInGame;
 import fr.jules_cesar.Paintball.Exception.PlayerNotInGame;
 
 @NestedCommands("paintball")
 public class PaintballCommands implements Commands{
 
 	@Command(name = "aide")
 	public void aide(Player joueur){
 		joueur.chat("/help paintball");
 	}
 	
 	@Command(name = "init")
 	public void initialiser(Player joueur){
 		try{
 			Paintball.getArene().initialiserPartie(joueur);
 		}
 		catch(ArenaNotSet e){ joueur.sendMessage(Paintball.messages.get("arena.notset")); }
 		catch(ArenaAlreadyInGame e){ joueur.sendMessage(Paintball.messages.get("game.already")); }
 	}
 	
 	@Command(name = "join", flags = "br", min = 0, max = 1)
 	public void rejoindre(Player joueur, CommandArgs args){
 		try{
 			if(args.hasFlag('b')) Paintball.getArene().rejoindrePartie(joueur, "bleu");
 			else if(args.hasFlag('r')) Paintball.getArene().rejoindrePartie(joueur, "rouge");
 			else if(args.length() != 0){
 				if(args.get(0).equalsIgnoreCase("bleu")) Paintball.getArene().rejoindrePartie(joueur, "bleu");
 				else if(args.get(0).equalsIgnoreCase("rouge")) Paintball.getArene().rejoindrePartie(joueur, "rouge");
 				else joueur.sendMessage(Paintball.messages.get("command.noteam"));
 			}
 			else{
				joueur.sendMessage(Paintball.messages.get("command.noset"));
 			}
 		}
 		catch(ArenaNotSet e){ joueur.sendMessage(Paintball.messages.get("arena.notset")); }
 		catch(ArenaAlreadyInGame e){ joueur.sendMessage(Paintball.messages.get("game.already")); }
 		catch(PlayerAlreadyInGame e){ joueur.sendMessage(Paintball.messages.get("player.already")); }
 	}
 	
 	@Command(name = "start")
 	public void demarrer(Player joueur){
 		try {
 			Paintball.getArene().demarrerPartie(joueur);
 		}
 		catch (ArenaNotInitialized e) {
 			joueur.sendMessage(Paintball.messages.get("init.no"));
 		}
 		catch (ArenaAlreadyInGame e) {
 			joueur.sendMessage(Paintball.messages.get("game.already"));
 		}
 		catch (ArenaMissingPlayers e) {
 			joueur.sendMessage(Paintball.messages.get("game.minplayer"));
 		} catch (PlayerNotInGame e) {
 			joueur.sendMessage(Paintball.messages.get("game.noplayer"));
 		}
 	}
 	
 	@Command(name = "spectateur")
 	public void spectateur(Player joueur){
 		try {
 			Paintball.getArene().rejoindreSpectateurs(joueur);
 		}
 		catch(ArenaNotSet e){ joueur.sendMessage(Paintball.messages.get("arena.notset")); }
 		catch (ArenaNotInitialized e) { joueur.sendMessage(Paintball.messages.get("init.no")); }
 		catch (PlayerAlreadyInGame e) { joueur.sendMessage(Paintball.messages.get("player.already")); }
 	}
 	
 	@Command(name = "setbleu")
 	public void setBleu(Player joueur){
 		Paintball.getArene().setBleu(joueur.getLocation());
 		joueur.sendMessage(Paintball.messages.get("arena.setblue"));
 	}
 	
 	@Command(name = "setrouge")
 	public void setRouge(Player joueur){
 		Paintball.getArene().setRouge(joueur.getLocation());
 		joueur.sendMessage(Paintball.messages.get("arena.setred"));
 	}
 	
 	@Command(name = "setspectateur")
 	public void setSpectateur(Player joueur){
 		Paintball.getArene().setSpectateur(joueur.getLocation());
 		joueur.sendMessage(Paintball.messages.get("arena.setspectator"));
 	}
 	
 	@Command(name = "quitter")
 	public void quitter(Player joueur){
 		try {
 			Paintball.getArene().quitter(joueur);
 			joueur.sendMessage(Paintball.messages.get("game.quit"));
 		}
 		catch (PlayerNotInGame e) { joueur.sendMessage(Paintball.messages.get("player.no")); }
 		catch (ArenaAlreadyInGame e) { joueur.sendMessage(Paintball.messages.get("player.alreadyingame")); }
 	}
 	
 	@Command(name = "save")
 	public void save(Player joueur){
 		Paintball.saveInventory(joueur, 'r');
 	}
 	
 	@Command(name = "load")
 	public void load(Player joueur){
 		Paintball.loadInventoryIfNecessary(joueur);
 	}
 }
