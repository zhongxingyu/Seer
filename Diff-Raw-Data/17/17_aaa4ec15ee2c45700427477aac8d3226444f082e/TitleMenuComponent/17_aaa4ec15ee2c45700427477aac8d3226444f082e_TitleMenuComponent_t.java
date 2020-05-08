 package org.adamk33n3r.karthas.gui.components;
 
 import org.adamk33n3r.karthas.Karthas;
 import org.adamk33n3r.karthas.entities.Actor;
 import org.adamk33n3r.karthas.gui.GUI;
 import org.adamk33n3r.karthas.gui.Menu;
 import org.adamk33n3r.karthas.gui.MenuItem;
 import org.adamk33n3r.karthas.gui.MenuItemAction;
 
 public class TitleMenuComponent extends MenuComponent {
 	
 	public TitleMenuComponent() {
 		menu = new Menu(null, new MenuItem("New Game", new MenuItemAction() {
 
 			@Override
 			public void execute() {
 				String in = GUI.getInput("Enter new character name");
 				if(!in.equals("")) {
 					Actor player = Karthas.init(in);
 					Karthas.save(player, true);
 					GUI.changeTo("Main");
 				}
 			}
 			
 		}), new MenuItem("Load Game", new MenuItemAction() {
 
 			@Override
 			public void execute() {
				String in = GUI.getInput("Enter character to load");
 				if(!in.equals("")) {
 					Karthas.load(in, true);
 					if (Karthas.getPlayer() != null)
 						GUI.changeTo("Main");
				}
 			}
 			
 		}), new MenuItem("Quit", MenuItemAction.EXIT));
 	}
 
 }
