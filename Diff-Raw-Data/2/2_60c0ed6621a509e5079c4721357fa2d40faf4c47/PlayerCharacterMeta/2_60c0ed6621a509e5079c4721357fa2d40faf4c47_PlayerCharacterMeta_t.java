 package com.censoredsoftware.Demigods.Engine.Object.Player;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.inventory.ItemStack;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.AbilityBind;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.Devotion;
 import com.google.common.collect.Sets;
 
 @Model
 public class PlayerCharacterMeta
 {
 	@Id
 	private Long id;
 	@Attribute
 	private Integer ascensions;
 	@Attribute
 	private Integer favor;
 	@Attribute
 	private Integer maxFavor;
 	@CollectionSet(of = AbilityBind.class)
 	private Set<AbilityBind> binds;
 	@CollectionMap(key = String.class, value = Boolean.class)
 	private Map<String, Boolean> abilityData;
 	@CollectionMap(key = String.class, value = Boolean.class)
 	private Map<String, Boolean> taskData;
 	@CollectionMap(key = String.class, value = Boolean.class)
 	private Map<String, Devotion> devotionData;
 
 	void initialize()
 	{
 		this.binds = Sets.newHashSet();
 		this.abilityData = new HashMap<String, Boolean>();
 		this.taskData = new HashMap<String, Boolean>();
 		this.devotionData = new HashMap<String, Devotion>();
 	}
 
 	public static PlayerCharacterMeta create()
 	{
 		PlayerCharacterMeta charMeta = new PlayerCharacterMeta();
 		charMeta.initialize();
 		charMeta.setAscensions(Demigods.config.getSettingInt("character.defaults.ascensions"));
 		charMeta.setFavor(Demigods.config.getSettingInt("character.defaults.favor"));
 		charMeta.setMaxFavor(Demigods.config.getSettingInt("character.defaults.max_favor"));
 		charMeta.addDevotion(Devotion.create(Devotion.Type.OFFENSE));
 		charMeta.addDevotion(Devotion.create(Devotion.Type.DEFENSE));
 		charMeta.addDevotion(Devotion.create(Devotion.Type.PASSIVE));
 		charMeta.addDevotion(Devotion.create(Devotion.Type.STEALTH));
 		charMeta.addDevotion(Devotion.create(Devotion.Type.SUPPORT));
 		charMeta.addDevotion(Devotion.create(Devotion.Type.ULTIMATE));
 		PlayerCharacterMeta.save(charMeta);
 		return charMeta;
 	}
 
 	public long getId()
 	{
 		return this.id;
 	}
 
 	public void addDevotion(Devotion devotion)
 	{
 		if(!this.devotionData.containsKey(devotion.getType().toString())) this.devotionData.put(devotion.getType().toString(), devotion);
 		save(this);
 	}
 
 	public Devotion getDevotion(Devotion.Type type)
 	{
 		if(this.devotionData.containsKey(type.toString()))
 		{
 			return this.devotionData.get(type.toString());
 		}
 		else
 		{
 			addDevotion(Devotion.create(type));
 			return this.devotionData.get(type.toString());
 		}
 	}
 
 	public boolean checkBind(String ability, ItemStack item)
 	{
 		return(isBound(item) && getBind(item).getAbility().equalsIgnoreCase(ability));
 	}
 
 	public boolean checkBind(String ability, int slot)
 	{
 		return(isBound(slot) && getBind(slot).getAbility().equalsIgnoreCase(ability));
 	}
 
 	public boolean isBound(int slot)
 	{
 		return getBind(slot) != null;
 	}
 
 	public boolean isBound(String ability)
 	{
 		return getBind(ability) != null;
 	}
 
 	public boolean isBound(ItemStack item)
 	{
 		return getBind(item) != null;
 	}
 
 	public void addBind(AbilityBind bind)
 	{
 		this.binds.add(bind);
 	}
 
 	public AbilityBind setBound(String ability, int slot, ItemStack item)
 	{
 		AbilityBind bind = AbilityBind.create(ability, slot, item);
 		this.binds.add(bind);
 		return bind;
 	}
 
 	public AbilityBind getBind(int slot)
 	{
 		for(AbilityBind bind : this.binds)
 		{
 			if(bind.getSlot() == slot) return bind;
 		}
 		return null;
 	}
 
 	public AbilityBind getBind(String ability)
 	{
 		for(AbilityBind bind : this.binds)
 		{
 			if(bind.getAbility().equalsIgnoreCase(ability)) return bind;
 		}
 		return null;
 	}
 
 	public AbilityBind getBind(ItemStack item)
 	{
 		for(AbilityBind bind : this.binds)
 		{
			if(item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().toString().contains(bind.getIdentifier()))
 			{
 				return bind;
 			}
 		}
 		return null;
 	}
 
 	public Set<AbilityBind> getBinds()
 	{
 		return this.binds;
 	}
 
 	public void removeBind(String ability)
 	{
 		if(isBound(ability))
 		{
 			AbilityBind bind = getBind(ability);
 			this.binds.remove(bind);
 			JOhm.delete(AbilityBind.class, bind.getId());
 		}
 	}
 
 	public void removeBind(ItemStack item)
 	{
 		if(isBound(item))
 		{
 			AbilityBind bind = getBind(item);
 			this.binds.remove(bind);
 			JOhm.delete(AbilityBind.class, bind.getId());
 		}
 	}
 
 	public void removeBind(AbilityBind bind)
 	{
 		this.binds.remove(bind);
 		JOhm.delete(AbilityBind.class, bind.getId());
 	}
 
 	public boolean isFinishedTask(String taskName)
 	{
 		return taskData.containsKey(taskName) && taskData.get(taskName);
 	}
 
 	public void finishTask(String taskName, boolean option)
 	{
 		taskData.put(taskName, option);
 	}
 
 	public Integer getAscensions()
 	{
 		return this.ascensions;
 	}
 
 	public void addAscension()
 	{
 		this.ascensions += 1;
 		save(this);
 	}
 
 	public void addAscensions(int amount)
 	{
 		this.ascensions += amount;
 		save(this);
 	}
 
 	public void subtractAscensions(int amount)
 	{
 		this.ascensions -= amount;
 		save(this);
 	}
 
 	public void setAscensions(int amount)
 	{
 		this.ascensions = amount;
 		save(this);
 	}
 
 	public Integer getFavor()
 	{
 		return this.favor;
 	}
 
 	public void setFavor(int amount)
 	{
 		this.favor = amount;
 		save(this);
 	}
 
 	public void addFavor(int amount)
 	{
 		if((this.favor + amount) > this.maxFavor)
 		{
 			this.favor = this.maxFavor;
 		}
 		else
 		{
 			this.favor += amount;
 		}
 		save(this);
 	}
 
 	public void subtractFavor(int amount)
 	{
 		if((this.favor - amount) < 0)
 		{
 			this.favor = 0;
 		}
 		else
 		{
 			this.favor -= amount;
 		}
 		save(this);
 	}
 
 	public Integer getMaxFavor()
 	{
 		return this.maxFavor;
 	}
 
 	public void addMaxFavor(int amount)
 	{
 		if((this.maxFavor + amount) > Demigods.config.getSettingInt("caps.favor"))
 		{
 			this.maxFavor = Demigods.config.getSettingInt("caps.favor");
 		}
 		else
 		{
 			this.maxFavor += amount;
 		}
 		save(this);
 	}
 
 	public void setMaxFavor(int amount)
 	{
 		if(amount < 0) this.maxFavor = 0;
 		if(amount > Demigods.config.getSettingInt("caps.favor")) this.maxFavor = Demigods.config.getSettingInt("caps.favor");
 		else this.maxFavor = amount;
 		save(this);
 	}
 
 	public static void save(PlayerCharacterMeta playerCharacterMeta)
 	{
 		JOhm.save(playerCharacterMeta);
 	}
 
 	public static PlayerCharacterMeta load(long id)
 	{
 		return JOhm.get(PlayerCharacterMeta.class, id);
 	}
 
 	public static Set<PlayerCharacterMeta> loadAll()
 	{
 		return JOhm.getAll(PlayerCharacterMeta.class);
 	}
 
 	@Override
 	public Object clone() throws CloneNotSupportedException
 	{
 		throw new CloneNotSupportedException();
 	}
 }
