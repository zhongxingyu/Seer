 package com.theminequest.MineQuest.Quest;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.TreeMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.Editable.CertainBlockEdit;
 import com.theminequest.MineQuest.Editable.CoordinateEdit;
 import com.theminequest.MineQuest.Editable.Edit;
 import com.theminequest.MineQuest.Editable.InsideAreaEdit;
 import com.theminequest.MineQuest.Editable.ItemInHandEdit;
 import com.theminequest.MineQuest.Editable.OutsideAreaEdit;
 import com.theminequest.MineQuest.Target.TargetDetails;
 import com.theminequest.MineQuest.Tasks.Task;
 
 public class QuestParser {
 	
 	/**
 	 * Indicate that this class can handle Quest file details.
 	 * @author Robert Xu <robxu9@gmail.com>
 	 *
 	 */
 	public static interface QHandler {
 		
 		/**
 		 * Parse the details from this line of the quest file.<br>
 		 * If a line contains:<br>
 		 * <code>ID:tas1,tas2,tas3:444</code><br>
 		 * Then the line is split by <code>:</code>. This line
 		 * does NOT contain detail type.
 		 * @param q QuestDescription to manipulate
 		 * @param line Details
 		 */
 		void parseDetails(QuestDescription q, List<String> line);
 		
 	}
 
 	private Map<String,Class<? extends QHandler>> methods = Collections.synchronizedMap(new LinkedHashMap<String,Class<? extends QHandler>>());
 	
 	/**
 	 * Register a QHandler for use in parsing Quests.
 	 * @param name Name to associate with
 	 * @param c Class that implements QHandler
 	 */
 	public void addClassHandler(String name, Class<? extends QHandler> c){
 		methods.put(name.toLowerCase(), c);
 	}
 	
 	/**
 	 * Deregisters a QHandler for use in parsing Quests.
 	 * @param name Name to remove.
 	 */
 	public void rmClassHandler(String name){
 		methods.remove(name.toLowerCase());
 	}
 	
 	protected void parseDefinition(QuestDescription questDescription) throws FileNotFoundException{
 		questDescription.tasks = new LinkedHashMap<Integer, String[]>(0);
 		questDescription.events = new LinkedHashMap<Integer, String>(0);
 		questDescription.targets = new LinkedHashMap<Integer, TargetDetails>(0);
 		questDescription.editables = new LinkedHashMap<Integer,Edit>(0);
		File f = new File(MineQuest.questManager.locationofQuests + File.separator + questDescription.questname);
 		Scanner filereader = new Scanner(f);
 		while (filereader.hasNextLine()) {
 			String nextline = filereader.nextLine();
 			ArrayList<String> ar = new ArrayList<String>();
 			for (String s : nextline.split(":"))
 				ar.add(s);
 			String type = ar.get(0).toLowerCase();
 			Class<? extends QHandler> c = methods.get(type);
 			if (c==null)
 				continue;
 			Method m;
 			try {
 				m = c.getMethod("parseDetails", QuestDescription.class, List.class);
 			} catch (SecurityException e1) {
 				e1.printStackTrace();
 				continue;
 			} catch (NoSuchMethodException e1) {
 				e1.printStackTrace();
 				continue;
 			}
 			/*
 			 * We don't need the type in the array we pass,
 			 * so we remove it.
 			 */
 			ar.remove(0);
 			try {
 				m.invoke(c.newInstance(), questDescription, ar);
 			} catch (IllegalArgumentException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (IllegalAccessException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (InvocationTargetException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (InstantiationException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 	}
 	
 }
