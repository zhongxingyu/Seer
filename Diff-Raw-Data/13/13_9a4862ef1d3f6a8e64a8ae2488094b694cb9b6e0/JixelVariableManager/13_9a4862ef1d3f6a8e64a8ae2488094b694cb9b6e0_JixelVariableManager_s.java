 package console;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 
 import stage.JixelGame;
 
 public class JixelVariableManager {
 	
 	private final String SAV_DIR = "profiles";
 	private final String SAV_NAME = "char";
 	private final String SAV_TYPE = ".sav";
 	private JixelGame game;
 
 	private HashMap<String, Object> varMap = new HashMap<String, Object>();
 
 	public JixelVariableManager(JixelGame game){
 		this.game = game;
 	}
 	
 	public <T> void newVar(String name, T value) {
 		if(varMap.containsKey(name)){
 			game.getConsole().print("A variable with the name " + name + " exists.");
 			return;
 		}
 		varMap.put(name, value);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T> T getValue(String name){
		Object o = varMap.get(name);
		if(o == null){
 			game.getConsole().print("No such variable with the name " + name + " exists.");
 		}
		return (T) o;
 	}
 	public <T> void setValue(String name, T value){
 		if(!varMap.containsKey(name)){
 			game.getConsole().print("No such variable with the name " + name + " exists.");
 		}else{
 			varMap.put(name, value);
 		}
 	}
 	
	public boolean exists(String name){
 		return varMap.containsKey(name);
 	}
 	
 	public boolean save(int id){
 		String filepath = String.format("%s%d%s", SAV_NAME, id, SAV_TYPE);
 		return save(filepath);
 	}
 	public boolean save(String file){
 		File dir = new File(SAV_DIR);
 		if(!dir.exists()){
 			dir.mkdir();
 		}
 		File f = new File(SAV_DIR + "\\" + file);
 		try{
 			if(!f.exists()){
 				f.createNewFile();
 			}
 			OutputStream out = new FileOutputStream(f);
 			ObjectOutputStream oos = new ObjectOutputStream(out);
 			setValue("Jixel_paused", true);
 			oos.writeObject(varMap);
 			oos.flush();
 			oos.close();
 			setValue("Jixel_paused", false);
 			return true;
 		}catch(IOException e){
 			setValue("Jixel_paused", false);
 			return false;
 		}
 	}
 	
 	/**
 	 * Loads a profile by its id
 	 * Warning: Will clear all variables
 	 * @param profileID
 	 * @return whether the profile loaded correctly
 	 */
 	public boolean load(int id){
 		String filepath = String.format("%s%d%s", SAV_NAME, id, SAV_TYPE);
 		return load(filepath);
 	}
 	/**
 	 * Loads a file with a given name
 	 * Warning: Will clear all variables
 	 * @param file - File name in /profiles/
 	 * @return whether the profile loaded correctly
 	 */
 	@SuppressWarnings("unchecked")
 	public boolean load(String file){
 		File dir = new File(SAV_DIR);
 		if(!dir.exists()){
 			dir.mkdir();
 		}
 		File f = new File(SAV_DIR + "\\" + file);
 		try{
 			if(!f.exists()){
 				return false;
 			}
 			InputStream in = new FileInputStream(f);
 			ObjectInputStream ois = new ObjectInputStream(in);
 			setValue("Jixel_paused", true);
 			varMap.clear();
 			try {
 				varMap = (HashMap<String,Object>)ois.readObject();
 			} catch (ClassNotFoundException e) {
 				ois.close();
 				return false;
 			}
 			ois.close();
 			setValue("Jixel_paused", false);
 			return true;
 		}catch(IOException e){
 			setValue("Jixel_paused", false);
 			return false;
 		}
 	}
 }
