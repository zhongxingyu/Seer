 package com.wolvencraft.prison.mines.util.constants;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.flags.*;
 
 public enum MineFlag {
 	
 	MoneyReward(MoneyRewardFlag.class, "moneyreward", true, false),
 	MoneyRewardPlus(MoneyRewardPlusFlag.class, "moneyrewardpluss", true, false),
	NoExpDrop(NoExpDropFlag.class, "noexpdrop", false, false),
 	NoHungerLoss(NoHungerLossFlag.class, "nohungerloss", false, false),
 	NoPlayerDamage(NoPlayerDamageFlag.class, "noplayerdamage", false, false),
 	NoToolDamage(NoToolDamageFlag.class, "notooldamage", false, false),
 	PlayerEffect(PlayerEffectFlag.class, "playereffect", true, true),
 	ResetSound(ResetSoundFlag.class, "resetsound", true, false),
 	Silent(SilentFlag.class, "silent", false, false),
 	SuperTools(SuperToolsFlag.class, "supertools", false, false),
 	SurfaceOre(SurfaceOreFlag.class, "surfaceore", true, false),
 	ToolReplace(ToolReplaceFlag.class, "toolreplace", false, false);
 	
 	MineFlag(Class<?> clazz, String alias, boolean hasOptions, boolean acceptDuplicates) {
 		this.clazz = clazz;
 		this.alias = alias;
 		this.hasOptions = hasOptions;
 		this.acceptDuplicates = acceptDuplicates;
 	}
 	
 	Class<?> clazz;
 	String alias;
 	boolean hasOptions;
 	boolean acceptDuplicates;
 	
 	public BaseFlag dispatch() {
 		try{
 			return (BaseFlag) clazz.newInstance();
 		} catch (InstantiationException e) {
 			Message.log(Level.SEVERE, "Error while instantiating a command! InstantiationException");
 			return null;
 		} catch (IllegalAccessException e) {
 			Message.log(Level.SEVERE, "Error while instantiating a command! IllegalAccessException");
 			return null;
 		}
 	}
 	
 	public boolean hasOptions() { return hasOptions; }
 	public boolean acceptDuplicates() { return acceptDuplicates; }
 	public String getAlias() { return alias; }
 	
 	public static MineFlag get(String alias) {
 		for(MineFlag flag : MineFlag.values()) {
 			if(flag.getAlias().equalsIgnoreCase(alias)) return flag;
 		}
 		return null;
 	}
 	
     /**
      * Converts the list of Protection constants to a list of their aliases. This method is used during the serialization of Mine objects
      * @param source List of Protection constants
      * @return List of Strings
      */
 	public static List<String> toStringList(List<BaseFlag> source) {
 		List<String> list = new ArrayList<String>();
 		for(BaseFlag flag : source) {
			if(MineFlag.get(flag.getName()).hasOptions()) list.add(flag.getName() + "," + flag.getOption());
 			else list.add(flag.getName());
 		}
 		return list;
 	}
 	
 	/**
 	 * Converts the list of String protection aliases back to the list of Protection constants. This method is used during the deserialization of Mine objects.
 	 * @param source List of String protection aliases
 	 * @return List of Protection constants
 	 */
 	public static List<BaseFlag> toMineFlagList(List<String> source) {
 		List<BaseFlag> list = new ArrayList<BaseFlag>();
 		for(String string : source) {
 			String[] parts = string.split(",");
 			if(parts.length == 1) { 
 				list.add(get(string).dispatch());
 			} else {
 				Message.debug("'" + parts[0] + "' :: '" + parts[1] + "'");
 				BaseFlag flag = get(parts[0]).dispatch();
 				flag.setOption(parts[1]);
 				list.add(flag);
 			}
 		}
 		return list;
 	}
 }
