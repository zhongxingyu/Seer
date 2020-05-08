 package game;
 
 import java.io.File;
 
 import javax.script.ScriptException;
 
 import manager.UberManager;
 import rendering.ModelRenderer;
 import rendering.RenderUpdater;
 import script.Script;
 import settings.Settings;
 import util.Factory;
 import util.FolderListener;
 import util.Log;
 import util.Util;
 import world.GameObjectType;
 import world.World;
 
 public class GameWatcher implements FolderListener {
 
 	private Game game;
 
 	public GameWatcher(Game g) {
 		this.game = g;
 	}
 
 	@Override
 	public void added(String s) {
 	}
 
 	@Override
 	public void changed(String s) {
 		Log.log(this, s + " changed");
 		if (s.equals(Settings.INIT_SCRIPT)) {
 			game.restart();
 		} else if (s.equals(Settings.MAIN_SCRIPT)) {
 			Script.scripts.remove(Settings.MAIN_SCRIPT);
 			GameLoop gl = game.loop;
 			gl.startPause();
 			Util.sleep(10);
 			Game.INSTANCE.world = new World();
 			try {
 				Script.executeFunction(Settings.MAIN_SCRIPT, "init",
 						Game.INSTANCE, Factory.INSTANCE);
 			} catch (ScriptException e) {
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				e.printStackTrace();
 			}
 			gl.endPause();
		}
		if (s.equals(Settings.OBJECTS_XML)) {
 			GameLoop gl = game.loop;
 			UberManager.clear();
 			Util.sleep(10);
 			gl.startPause();
 			Util.sleep(10);
 			game.parseXML();
 			gl.endPause();
 		} else {
 			String folder = s.split(File.separator)[0];
 			s = s.replace("\\", "/");
 			Log.log(this, folder);
 			if (folder.equals("scripts")) {
 				game.getManager("script").changed(s);
 			} else if (folder.equals("img")) {
 				UberManager.textureChanged(s);
 			} else if (folder.equals("shader")) {
 				UberManager.shaderChanged(s);
 				// game.getManager("shader").changed(s);
 			} else if (folder.equals("obj")) {
 				GameLoop gl = game.loop;
 				gl.startPause();
 				Util.sleep(10);
 				for (final GameObjectType go : GameObjectType.getTypes()) {
 					if (go.renderer != null && go.renderer.getName().equals(s)) {
 						final ModelRenderer newModel = new ModelRenderer(s,
 								false);
 						RenderUpdater.executeInOpenGLContext(new Runnable() {
 							@Override
 							public void run() {
 								go.renderer = newModel;
 							}
 						});
 					}
 				}
 				gl.endPause();
 			}
 		}
 	}
 
 	@Override
 	public void removed(String s) {
 
 	}
 
 }
