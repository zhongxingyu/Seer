 package ch.vorburger.leviator;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import ch.vorburger.leviator.CommandPrompt.Command;
 import ch.vorburger.leviator.CommandPrompt.CommandContext;
 
 public class Commands {
 
 	static List<Command> available = new ArrayList<CommandPrompt.Command>(Arrays.asList(
 			new Command("help", "") {
 				@Override boolean isDoneCommand(CommandContext ctx) {
 					ctx.currentPlayer.info("Available commands are:");
 					for (Command command : available) {
 						ctx.currentPlayer.info("   " + command.keyword + " " + command.argsHelp);
 					}
 					return false;
 				}
 			},
 			new Command("go", "<Place>") {
 				@Override void doCommand(CommandContext ctx) {
 					ctx.currentPlayer.go(ctx.getPlace());
 				}
 			},
 			new Command("take", "<Thing>") { // [howMany/1]
 				@Override void doCommand(CommandContext ctx) {
 					ctx.currentPlayer.inPlace.things.transferThing(ctx.getThing(ctx.currentPlayer.inPlace.things), ctx.getNumber(1), ctx.currentPlayer.things);
 				}
 			},
 			new Command("give", "<Player> <Thing> [howMany/1]") {
 				@Override void doCommand(CommandContext ctx) {
 					Things otherPlayerThings = ctx.getPlayer().things;
 					Thing thing = ctx.getThing(ctx.currentPlayer.things);
 					ctx.currentPlayer.things.transferThing(thing, ctx.getNumber(1), otherPlayerThings);
 				}
 			},
 			new Command("eat", "<{Edible}Thing>") { // [howMany/1]
				@Override boolean isDoneCommand(CommandContext ctx) {
 					Thing thing = ctx.getThing(ctx.currentPlayer.things);
 					if (thing instanceof Edible) {
 						ctx.currentPlayer.things.removeThing(thing, 1);
 						ctx.currentPlayer.energyBar += 1;
						return true;
 					} else {
 						ctx.currentPlayer.info("hey you crazy dude, you cannot eat: " + thing.name());
						return false;
 					}
 				}
 			},
 			new Command("plant", "<{Plantable}Thing>") { // [howMany/1]
				@Override boolean isDoneCommand(CommandContext ctx) {
 					Thing thing = ctx.getThing(ctx.currentPlayer.things);
 					if (thing instanceof Plantable) {
 						ctx.currentPlayer.things.transferThing(thing, 1, ctx.currentPlayer.inPlace.things);
						return true;
 					} else {
 						ctx.currentPlayer.info("you cannot plant: " + thing.name());
						return false;
 					}
 				}
 			},
 			
 			new Command("admin_quit", "") {
 				@Override void doCommand(CommandContext ctx) {
 					ctx.currentPlayer.world.isRunning = false;
 				}
 			},
 			new Command("admin_new_user", "<NewPlayer>") {
 				@Override void doCommand(CommandContext ctx) {
 					ctx.currentPlayer.world.players.add(new Player(ctx.getString(), ctx.currentPlayer.world));
 				}
 			},
 			new Command("admin_new_place", "<NewPlace>") {
 				@Override void doCommand(CommandContext ctx) {
 					ctx.currentPlayer.world.places.add(new Place(ctx.getString()));
 				}
 			},
 			new Command("admin_new_thing", "<howMany> <NewThing> <ThingCategory>*") {
 				@Override void doCommand(CommandContext ctx) {
 					int n = ctx.getNumber();
 					Thing newThing = new Thing(ctx.getString());
 					// TODO ThingCategory
 					ctx.currentPlayer.inPlace.things.addThing(newThing, n);
 				}
 			}
 		));
 
 }
