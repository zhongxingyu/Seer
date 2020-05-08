 package com.theminequest.MineQuest.Quest;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.apache.commons.io.output.ByteArrayOutputStream;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 import com.theminequest.MineQuest.I18NMessage;
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.BukkitEvents.CompleteStatus;
 import com.theminequest.MineQuest.Editable.Edit;
 import com.theminequest.MineQuest.Target.TargetDetails;
 import com.theminequest.MineQuest.Tasks.Task;
 import com.theminequest.MineQuest.Utils.ChatUtils;
 import com.theminequest.MineQuest.Utils.FastByteArrayOutputStream;
 
 /**
  * Represents a QuestDescription and associated properties
  * @author Robert
  *
  */
 public class QuestDescription implements Comparable<QuestDescription>, Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 9148896666724411547L;
 
 	public File file;
 
 	public String questname;
 	// always <ID #,OBJECT/DETAILS>
 	public LinkedHashMap<Integer, String[]> tasks;
 	public LinkedHashMap<Integer, String> events;
 	public LinkedHashMap<Integer, TargetDetails> targets;
 	public LinkedHashMap<Integer, Edit> editables;
 	// quest configuration
 	public String displayname;
 	public String displaydesc;
 	public String displayaccept;
 	public String displaycancel;
 	public String displayfinish;
 	public boolean questRepeatable;
 	public boolean spawnReset;
 	/**
 	 * Controls the Spawn Point for the Quest (x,y,z)
 	 */
 	public double[] spawnPoint;
 	/**
 	 * Controls the area to preserve (uneditable) (x,y,z,x,y,z)
 	 */
 	public double[] areaPreserve;
 	public String editMessage;
 	public String world;
 	public boolean loadworld;
 	public boolean nether;
 	
 	/**
 	 * For addons to store their data
 	 */
 	public Map<String,Serializable> database;
 	
 	/*
 	 * Feature Req: Trottimus
 	 */
 	public int groupLimit;
 
 	/**
 	 * Create a QuestDescription from the file
 	 * @param f Valid quest file to read
 	 * @throws FileNotFoundException If file was suddenly disappears
 	 */
 	protected QuestDescription(File f) throws FileNotFoundException{
		String id = f.getName().substring(0, f.getName().indexOf(".quest"));
 		file = f;
 		setDefaults(id);
 		readInFile();
 	}
 	
 	/**
 	 * Sets the defaults. In particular, this initializes default
 	 * objects, sets default names, locales, and spawn points if
 	 * the quest file does not use them.
 	 * @param id Quest Name Identification
 	 */
 	private void setDefaults(String id){
 		questname = id;
 		// DEFAULTS start
 		displayname = questname;
 		displaydesc = I18NMessage.Quest_NODESC.getDescription();
 		displayaccept = I18NMessage.Quest_ACCEPT.getDescription();
 		displaycancel = I18NMessage.Quest_ABORT.getDescription();
 		displayfinish = I18NMessage.Quest_COMPLETE.getDescription();
 		questRepeatable = false;
 		spawnReset = true;
 
 		spawnPoint = new double[3];
 		spawnPoint[0] = 0;
 		spawnPoint[1] = 64;
 		spawnPoint[2] = 0;
 
 		areaPreserve = new double[6];
 		areaPreserve[0] = 0;
 		areaPreserve[1] = 64;
 		areaPreserve[2] = 0;
 		areaPreserve[3] = 0;
 		areaPreserve[4] = 64;
 		areaPreserve[5] = 0;
 
 		editMessage = ChatColor.GRAY + I18NMessage.Quest_NOEDIT.getDescription();
 		world = Bukkit.getWorlds().get(0).getName();
 		loadworld = false;
 		
 		database = Collections.synchronizedMap(new LinkedHashMap<String,Serializable>());
 		
 		groupLimit = MineQuest.groupManager.TEAM_MAX_CAPACITY;
 	}
 	
 	
 	/**
 	 * Reads in the file description from the quest folder.
 	 * @param p 
 	 * @throws FileNotFoundException If the file mysteriously disappeared.
 	 */
 	private void readInFile() throws FileNotFoundException{
 		MineQuest.questManager.parser.parseDefinition(this);
 	}
 	
 	/**
 	 * Get a deep copy of this QuestDescription object. This allows
 	 * for the object to be modified without any adverse side-effects
 	 * and does not affect the original <b>master</b> object.<br>
 	 * <h5>WARNING:</h5> This method uses serialization and a
 	 * byte-based stream to accomplish deep copying. As such, it is an
 	 * intensive operation and should not be used lightly by
 	 * methods. In particular, this method is used by
 	 * {@link com.theminequest.MineQuest.Quest.QuestManager#startQuest(String)}
 	 * to retrieve a mutable object for every quest started.
 	 * @return completely independent copy of the object
 	 * @throws IOException if object could not be copied
 	 */
 	public QuestDescription getCopy() throws IOException{
 		FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
 		ObjectOutputStream os = new ObjectOutputStream(bos);
 		os.writeObject(this);
 		os.flush();
 		os.close();
 		
 		ObjectInputStream is = new ObjectInputStream(bos.getInputStream());
 		QuestDescription q;
 		try {
 			q = (QuestDescription) is.readObject();
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		} finally {
 			is.close();
 		}
 		return q;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof QuestDescription))
 			return false;
 		return questname.equals(((QuestDescription)obj).questname);
 	}
 
 	@Override
 	public int compareTo(QuestDescription arg0) {
 		return questname.compareTo(arg0.questname);
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		String tr = "";
 		tr+=ChatUtils.formatHeader(displayname)+"\n";
 		tr+=ChatUtils.chatify(displaydesc)+"\n";
 		return tr;
 	}
 
 
 }
