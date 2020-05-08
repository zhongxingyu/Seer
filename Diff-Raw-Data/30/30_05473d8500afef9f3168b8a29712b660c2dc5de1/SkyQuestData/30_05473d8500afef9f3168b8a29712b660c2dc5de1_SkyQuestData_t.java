 package net.skycraftmc.SkyQuest;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import net.skycraftmc.SkyQuest.quest.Objective;
 import net.skycraftmc.SkyQuest.quest.ObjectiveType;
 import net.skycraftmc.SkyQuest.quest.PlayerQuestData;
 import net.skycraftmc.SkyQuest.quest.Quest;
 
 import org.bukkit.entity.Player;
 
 public class SkyQuestData 
 {
 	private SkyQuestMain plugin;
 	public SkyQuestData(SkyQuestMain plugin)
 	{
 		this.plugin = plugin;
 	}
 	public void saveData(Player player)
 	{
 		File file = plugin.getDataFolder();
 		if(!file.exists())file.mkdir();
 		File f = new File(file.getPath() + File.separator + "Players");
 		if(!f.exists())f.mkdir();
 		File data = new File(f.getPath() + File.separator + player.getName() + ".txt");
 		try
 		{
 			if(!data.exists())
 			{
 				FileOutputStream fos = new FileOutputStream(data);
 				fos.flush();
 				fos.close();
 			}
 			BufferedWriter bw= new BufferedWriter(new FileWriter(data));
 			bw.write("#Player data, DO NOT EDIT AT ANY COST");
 			bw.newLine();
 			for(Quest quest: plugin.getQuestManager().getData(player).getQuests())
 			{
 				bw.write("quest: " + quest.getName());
 				bw.newLine();
 				int i = 1;
 				for(Objective o:quest.getObjectives())
 				{
 					bw.write("    objective " + i + ": " + (o.isComplete() ? "Complete" : o.getProgress()));
 					bw.newLine();
 					i ++;
 				}
 				bw.write("end quest");
 				bw.newLine();
 			}
 			bw.flush();
 			bw.close();
 		}
 		catch(IOException ioe)
 		{
 			plugin.log.severe("Failed to save \"" + player.getName() + "\"'s data: " + ioe.getMessage());
 		}
 	}
 	public void loadData(Player player)
 	{
 		File file = plugin.getDataFolder();
 		if(!file.exists())file.mkdir();
 		File quests = new File(file.getPath() + File.separator + "Quests");
 		if(!quests.exists())quests.mkdir();
 		File data = new File(quests.getPath() + File.separator + player.getName() + ".txt");
 		if(!data.exists())
 		{
 			plugin.getQuestManager().addData(player);
 			return;
 		}
 		try
 		{
 			BufferedReader br = new BufferedReader(new FileReader(data));
 			String l;
 			boolean q = false;
 			PlayerQuestData d = new PlayerQuestData(player);
 			Quest quest = null;
 			while((l=br.readLine()) != null)
 			{
 				if(l.startsWith("#"))continue;
 				String[] tokens = l.split("[:]", 2);
 				if(!q)
 				{
 					if(tokens.length != 2)continue;
 					if(tokens[0].equalsIgnoreCase("quest"))
 					{
 						q = true;
 						if((quest=plugin.getQuestManager().getQuest(tokens[1].trim())) != null)d.addQuest(quest.clone());
 					}
 				}
 				else
 				{
 					if(tokens.length != 2)
 					{
 						if(l.replaceAll(" ", "").equalsIgnoreCase("endquest"))
 						{
 							q = false;
 							quest = null;
 						}
 					}
 					if(quest == null)return;
 					String[] s = tokens[0].trim().split("[ ]+", 2);
 					if(s.length != 2)continue;
 					if(s[0].equalsIgnoreCase("objective"))
 					{
 						int objn;
 						try{objn = Integer.parseInt(s[1]);
 						}catch(NumberFormatException nfe){continue;}
 						quest.getObjectives()[objn - 1].setProgress(tokens[1].trim());
 					}
 				}
 			}
			br.close();
			plugin.getQuestManager().addData(d);
 		}
 		catch(IOException ioe)
 		{
			plugin.log.severe("Failed to load player data of " + player.getName() + ": " + ioe.getMessage());
 		}
 	}
 	public void loadQuests()
 	{
 		File file = plugin.getDataFolder();
 		if(!file.exists())file.mkdir();
 		File quests = new File(file.getPath() + File.separator + "Quests");
 		if(!quests.exists())quests.mkdir();
 		for(File f:quests.listFiles())
 		{
 			if(!f.getName().endsWith(".txt"))continue;
 			if(f.getName().length() <= 4)continue;
 			String qname = f.getName().substring(0, f.getName().length() - 4);
 			try
 			{
 				BufferedReader br = new BufferedReader(new FileReader(f));
 				String l;
 				boolean obj = false;
 				boolean desc = false;
 				boolean def = true;
 				ArrayList<Integer>onext = new ArrayList<Integer>();
 				boolean onextb = false;
 				String name = null;
 				ArrayList<String>d = new ArrayList<String>();
 				ArrayList<String>next = new ArrayList<String>();
 				String rewards = null;
 				boolean opt = false;
 				ObjectiveType type = null;
 				String target = null;
 				ArrayList<Objective> objs = new ArrayList<Objective>();
 				while((l=br.readLine()) != null)
 				{
 					if(desc)
 					{
 						if(l.replaceAll(" ", "").equalsIgnoreCase("enddescription"))
 						{
 							desc = false;
 							continue;
 						}
 						d.add(l.trim());
 					}
 					else if(onextb)
 					{
 						if(l.replaceAll(" ", "").equalsIgnoreCase("endnext"))
 						{
 							onextb = false;
 							continue;
 						}
 						int i;
 						try{i = Integer.parseInt(l.replaceAll(" ", ""));}catch(NumberFormatException nfe){continue;}
 						if(!onext.contains(i))onext.add(i);
 					}
 					else if(obj)
 					{
 						String[] tokens = l.split("[:]" ,2);
 						if(tokens.length != 2 && tokens[0].replaceAll(" ", "").equalsIgnoreCase("endobjective"))
 						{
 							obj = false;
 							if(rewards != null && type != null && target != null)
 							{
 								String[] r = rewards.split("[;]");
 								ArrayList<String>ra = new ArrayList<String>();
 								for(String x:r)ra.add(x);
 								ArrayList<String>da = new ArrayList<String>();
 								for(String x:d)da.add(x);
 								ArrayList<Integer>na = new ArrayList<Integer>();
 								for(int i:onext)na.add(i);
 								Objective o = new Objective(name, target, da, ra, na, type, opt, def);
 								objs.add(o);
 							}
 							d.clear();
 							type = null;
 							name = null;
 							target = null;
 							rewards = null;
 							opt = false;
 							def = true;
 							onext.clear();
							continue;
 						}
 						String t = tokens[0].trim();
 						if(t.equalsIgnoreCase("rewards"))rewards = tokens[1].trim();
 						else if(t.equalsIgnoreCase("type"))type = ObjectiveType.getType(tokens[1].toUpperCase());
 						else if(t.equalsIgnoreCase("target"))target = tokens[1].trim();
 						else if(t.equalsIgnoreCase("description"))
 						{
 							desc = true;
 							d.add(tokens[1].trim());
 						}
 						else if(t.equalsIgnoreCase("next"))onextb = true;
 						else if(t.equalsIgnoreCase("optional"))opt = tokens[1].replaceAll(" ", "").equalsIgnoreCase("true");
 						else if(t.equalsIgnoreCase("default"))def = tokens[1].replaceAll(" ", "").equalsIgnoreCase("true");
 
 					}
 					else
 					{
 						String[] tokens = l.split("[:]",2);
 						if(tokens.length != 2)continue;
 						if(tokens[0].startsWith("objective"))
 						{
 							name = tokens[1].trim();
 							obj = true;
 						}
 						else if(tokens[0].equalsIgnoreCase("next"))next.add(tokens[1].trim());
 					}
 				}
 				br.close();
 				ArrayList<String>n = new ArrayList<String>();
 				for(String x:next)n.add(x);
 				Quest q = new Quest(qname, objs, n);
 				plugin.getQuestManager().addQuest(q);
 				System.out.println("Loaded quest: " + q.getName());
 			}
 			catch(IOException ioe)
 			{
 				plugin.log.severe("Failed to load quest \"" + qname + "\":" + ioe.getMessage());
 			}
 		}
 	}
 }
