 package rs2.environment;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import rs2.environment.events.Event;
 import rs2.environment.model.Model;
 import rs2.environment.script.ObjectScript;
 import rs2.environment.script.Script;
 import rs2.environment.script.ScriptBase;
 import rs2.environment.wrapper.World;
 import rs2.environment.wrapper.Wrapper;
 import rs2.environment.wrapper.entity.NPC;
 import rs2.environment.wrapper.entity.Player;
 
 /**
  * @author Lazaro Brito.
  * 
  */
 public class RS2Environment {
 	/**
 	 * Gets the wrapper provided by the model.
 	 * 
 	 * Checks if the model is null to prevent null pointer exceptions.
 	 * 
 	 * @param model
 	 *            The model to wrap.
 	 * @return The wrapper.
 	 */
 	public static <W extends Wrapper<?>> W wrap(Model<W> model) {
 		if (model != null) {
 			return model.get_wrapper();
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Queues the event into it's context entity.
 	 * 
 	 * @param event
 	 *            The event the queue.
 	 */
 	public static void queue(Event<?> event) {
 		if (event != null) {
 			event.context.queueEvent(event);
 		}
 	}
 
 	public static void main(String[] args) throws IOException {
 		if (args[0].equals("imports")) {
 			File dir = new File(args[1]);
 			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
 					args[2])));
 			ScriptBase.generateImports(dir, dir, writer);
 			writer.close();
 		} else {
 			System.err.println("Invalid argument: " + args[0]);
 		}
 	}
 
 	private final World world;
 
 	private final ScriptBase scripts;
 	private final Map<Integer, List<ObjectScript>> objectScripts = new HashMap<Integer, List<ObjectScript>>();
 
 	public RS2Environment(World world) {
 		this.world = world;
 
		scripts = new ScriptBase(new String[] { world.getWorkingDirectory() + "script/" });
 
 		try {
 			loadScripts();
 		} catch (IOException e) {
 			System.err.println("Failed to load scripts!");
 			e.printStackTrace();
 		}
 	}
 
 	private void loadScripts() throws IOException {
		BufferedReader cfg = new BufferedReader(new FileReader(
				"data/script_list.txt"));
 		try {
 			int lineNumber = 0;
 			String line;
 			while ((line = cfg.readLine()) != null) {
 				if (line.startsWith("#") || line.trim().length() == 0) {
 					continue;
 				}
 
 				Script script = null;
 				script = scripts.initScript(line.trim());
 
 				if (script == null) {
 					System.err.println("Failed to load script (line #"
 							+ lineNumber + ") : " + line);
 				} else {
 					try {
 						script.initialize(this);
 					} catch (Exception e) {
 						System.err.println("Failed to initiate script (line #"
 								+ lineNumber + ") : " + line);
 						e.printStackTrace();
 					}
 				}
 
 				lineNumber++;
 			}
 		} finally {
 			if (cfg != null) {
 				cfg.close();
 			}
 		}
 	}
 
 	/**
 	 * Registers an object script with the object's id.
 	 * @param ids The ids to register with.
 	 * @param script The script to register.
 	 */
 	public void registerObjectScript(int[] ids, ObjectScript script) {
 		for (int i : ids) {
 			List<ObjectScript> scripts = objectScripts.get(i);
 			if (scripts == null) {
 				objectScripts.put(i, (scripts = new ArrayList<ObjectScript>()));
 			}
 			scripts.add(script);
 		}
 	}
 
 	/**
 	 * Gets all the object scripts for a specified object id.
 	 * @param id The object id.
 	 * @return The object script.
 	 */
 	public List<ObjectScript> getObjectScripts(int id) {
 		return objectScripts.get(id);
 	}
 
 	/**
 	 * - Gets the world instance associated with this environment.
 	 * 
 	 * @return The world.
 	 */
 	public World getWorld() {
 		return world;
 	}
 
 	/**
 	 * Processes all the events in the world's context.
 	 * 
 	 * @param world
 	 *            The world context.
 	 */
 	public void handle() {
 		for (Player player : world.getPlayers()) {
 			player.handleEvents();
 		}
 
 		for (NPC npc : world.getNPCs()) {
 			npc.handleEvents();
 		}
 	}
 }
