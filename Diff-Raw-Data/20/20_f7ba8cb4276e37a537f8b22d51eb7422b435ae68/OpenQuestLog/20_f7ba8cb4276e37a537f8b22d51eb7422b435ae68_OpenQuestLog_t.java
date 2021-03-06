 package net.skycraftmc.SkyQuest;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class OpenQuestLog
 {
 	private Player player;
 	private Quest selected = null;
 	private InventoryView view;
 	private Inventory inv;
 	private Quest[] curassigned;
 	private Quest[] curcompleted;
 	private PlayerQuestLog log;
 	private int page = 1;
 	private boolean viewcomp = false;
 	public OpenQuestLog(Player player, PlayerQuestLog log, Inventory inv)
 	{
 		this.log = log;
 		this.player = player;
 		this.inv = inv;
		recalculateAssigned();
 		recalculateCompleted();
 		update();
 		view = player.openInventory(inv);
 	}
 	private void recalculateCompleted()
 	{
 		CompletedQuestData[] cqd = log.getCompleted();
 		String[] ca = new String[cqd.length];
 		for(int i = 0; i < cqd.length; i ++)ca[i] = cqd[i].getQuest().getID();
 		QuestManager qm = SkyQuestPlugin.getPlugin().getQuestManager();
 		ArrayList<Quest>c = new ArrayList<Quest>();
 		for(int i = 0; i < ca.length; i ++)
 		{
 			Quest q = qm.getQuest(ca[i]);
 			if(q != null && q.isVisible())c.add(q);
 		}
 		curcompleted = c.toArray(new Quest[c.size()]);
 	}
	private void recalculateAssigned()
	{
		ArrayList<Quest>tempassgn = new ArrayList<Quest>();
		for(Quest q:log.getAssigned())
		{
			if(q.isVisible())tempassgn.add(q);
		}
		curassigned = tempassgn.toArray(new Quest[tempassgn.size()]);
	}
 	public Player getPlayer()
 	{
 		return player;
 	}
 	public Quest getSelected()
 	{
 		return selected;
 	}
 	public void setSelected(Quest value)
 	{
 		selected = value;
 	}
 	public InventoryView getInventoryView()
 	{
 		return view;
 	}
 	public Inventory getInventory()
 	{
 		return inv;
 	}
 	public void update()
 	{
 		inv.clear();
 		if(selected == null)
 		{
 			Quest[] as = viewcomp ? curcompleted : curassigned;
 			for(int i = 0; i < as.length && i < 27; i ++)
 			{
 				Quest q = as[i + (page - 1)*27];
 				Material m = Material.getMaterial(q.getItemIconId());
 				if(m == null || m == Material.AIR)m = Material.BOOK_AND_QUILL;
 				ItemStack qstack = new ItemStack(m);
 				ItemMeta im = player.getServer().getItemFactory().getItemMeta(m);
 				im.setDisplayName(ChatColor.GREEN + q.getName());
 				ArrayList<String>lore = new ArrayList<String>();
 				if(!q.getDescription().isEmpty())
 				{
 					for(String s:q.getDescription())lore.add(s);
 				}
 				if(viewcomp)
 				{
 					CompletedQuestData cqd = log.getCompletedData(q);
 					CompletionState state = cqd.getState();
 					if(state == CompletionState.COMPLETE)
 						lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + "Completed");
 					else if(state == CompletionState.FAILED)
 						lore.add(ChatColor.RED + "" + ChatColor.ITALIC + "Failed");
 				}
 				lore.add(ChatColor.YELLOW + "Click for more information!");
 				im.setLore(lore);
 				qstack.setItemMeta(im);
 				inv.setItem(i, qstack);
 			}
 			Material vm = viewcomp ? Material.BOOK_AND_QUILL : Material.WRITTEN_BOOK;
 			ItemStack viewtoggle = new ItemStack(vm);
 			ItemMeta vtim = player.getServer().getItemFactory().getItemMeta(vm);
 			vtim.setDisplayName(ChatColor.YELLOW + "View " + (viewcomp ? "active" : "completed") + " quests");
 			viewtoggle.setItemMeta(vtim);
 			inv.setItem(27, viewtoggle);
 		}
 		else if(viewcomp)
 		{
 			CompletedQuestData cqd = log.getCompletedData(selected);
 			Objective[] oao = selected.getObjectives();
 			ArrayList<Objective>n = new ArrayList<Objective>();
 			for(int i = 0; i < oao.length; i ++)
 			{
 				if(oao[i].isVisible() && !cqd.unassigned.contains(oao[i].getID()))n.add(oao[i]);
 			}
 			Objective[] oa = n.toArray(new Objective[n.size()]);
 			for(int i = 0; i < oa.length && i < 27; i ++)
 			{
 				Objective o = oa[i];
 				Material m = Material.getMaterial(o.getItemIconId());
 				if(m == null || m == Material.AIR)m = Material.getMaterial(o.getType().getItemIcon());
 				if(m == null || m == Material.AIR)m = Material.WRITTEN_BOOK;
 				ItemStack ostack = new ItemStack(m);
 				ItemMeta im = player.getServer().getItemFactory().getItemMeta(m);
 				String ps; 
 				ArrayList<String>lore = new ArrayList<String>();
 				String[] desc = o.getDescription();
 				for(String s:desc)lore.add(s);
 				if(o.isOptional())
 				{
 					if(desc.length > 0)lore.add("");
 					lore.add(ChatColor.GOLD + "" + ChatColor.ITALIC + "Optional");
 				}
 				ChatColor tcol;
 				if(cqd.isComplete(o.getID()))
 				{
 					lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + "Completed");
 					ps = o.getType().getProgressString(o.getTarget(), o.getTarget());
 					tcol = ChatColor.GREEN;
 				}
 				else
 				{
 					ps = o.getType().getProgressString(o.getTarget(), cqd.getProgress(o.getID()));
 					tcol = ChatColor.YELLOW;
 				}
 				im.setDisplayName(tcol + o.getName() + " " + ps);
 				im.setLore(lore);
 				ostack.setItemMeta(im);
 				inv.setItem(i, ostack);
 			}
 			ItemStack back = new ItemStack(Material.BOOK_AND_QUILL);
 			ItemMeta bim = player.getServer().getItemFactory().getItemMeta(Material.BOOK_AND_QUILL);
 			bim.setDisplayName(ChatColor.YELLOW + "Back to quest log");
 			back.setItemMeta(bim);
 			inv.setItem(27, back);
 		}
 		else
 		{
 			QuestData qd = log.getProgress(selected);
 			Objective[] oao = qd.getQuest().getObjectives();
 			ArrayList<Objective>n = new ArrayList<Objective>();
 			for(int i = 0; i < oao.length; i ++)
 			{
 				if(oao[i].isVisible() && (qd.isAssigned(oao[i].getID()) || 
 						qd.isComplete(oao[i].getID())))n.add(oao[i]);
 			}
 			Objective[] oa = n.toArray(new Objective[n.size()]);
 			for(int i = 0; i < oa.length && i < 27; i ++)
 			{
 				Objective o = oa[i];
 				Material m = Material.getMaterial(o.getItemIconId());
 				if(m == null || m == Material.AIR)m = Material.getMaterial(o.getType().getItemIcon());
 				if(m == null || m == Material.AIR)m = Material.WRITTEN_BOOK;
 				ItemStack ostack = new ItemStack(m);
 				ItemMeta im = player.getServer().getItemFactory().getItemMeta(m);
 				boolean complete = qd.isComplete(o.getID());
 				ArrayList<String>lore = new ArrayList<String>();
 				String[] desc = o.getDescription();
 				for(String s:desc)lore.add(s);
 				if(o.isOptional())
 				{
 					if(desc.length > 0)lore.add("");
 					lore.add(ChatColor.GOLD + "" + ChatColor.ITALIC + "Optional");
 				}
 				if(complete)
 				{
 					String ps = o.getType().getProgressString(o.getTarget(), o.getTarget());
 					im.setDisplayName(ChatColor.GREEN + o.getName() + " " + ps);
 					lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + "Completed");
 				}
 				else
 				{
 					String ps = o.getType().getProgressString(o.getTarget(), qd.getProgress(o.getID()));
 					im.setDisplayName(ChatColor.YELLOW + o.getName() + " " + ps);
 				}
 				im.setLore(lore);
 				ostack.setItemMeta(im);
 				inv.setItem(i, ostack);
 			}
 			ItemStack back = new ItemStack(Material.BOOK_AND_QUILL);
 			ItemMeta bim = player.getServer().getItemFactory().getItemMeta(Material.BOOK_AND_QUILL);
 			bim.setDisplayName(ChatColor.YELLOW + "Back to quest log");
 			back.setItemMeta(bim);
 			inv.setItem(27, back);
 		}
		recalculateAssigned();
 	}
 	public int getPage()
 	{
 		return page;
 	}
 	public void setPage(int page)
 	{
 		this.page = page;
 	}
 	public Quest[] getCurrentQuestList()
 	{
 		return curassigned;
 	}
 	public Quest[] getCurrentCompletedList()
 	{
 		return curcompleted;
 	}
 	public boolean isViewingCompleted()
 	{
 		return viewcomp;
 	}
 	public void setViewingCompleted(boolean value)
 	{
 		viewcomp = value;
 	}
 	void dispose()
 	{
 		player = null;
 		curassigned = null;
 		curcompleted = null;
 		log = null;
 		selected = null;
 		view = null;
 		inv = null;
 	}
 }
