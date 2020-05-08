 package com.comphenix.xp.messages;
 
 import org.apache.commons.lang.NullArgumentException;
 import org.apache.commons.lang.ObjectUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class MessageFormatter {
 
 	private Player source;
 	private Integer experience;
 	private Integer count;
 	
 	// Default
 	public MessageFormatter() {
 		setCount(1);
 	}
 
 	public MessageFormatter(Player player, Integer experience) {
 		this(player, experience, 1);
 	}
 	
 	public MessageFormatter(Player player, Integer experience, Integer count) {
 		setSource(player);
 		setExperience(experience);
 		setCount(count);
 	}
 	
 	/**
 	 * Replaces parameters in the text with their respective value.
 	 * @param message - message to format.
 	 * @return Message with every parameter replaced with the corresponding value.
 	 */
 	public String formatMessage(String message) {
 		
 		if (message == null)
 			return null;
 		
 		// A simple system for now. 
 		// TODO: Add more variables.
 		return message.
 				replace("{player}", source != null ? source.getDisplayName() : "Unknown").
 				replace("{experience}", experience != null ? experience.toString() : "N/A").
 				replace("{count}", count != null ? count.toString() : "N/A").
 				replaceAll("&(?=[0-9a-fxA-FX])", Character.toString(ChatColor.COLOR_CHAR));
 	}
 
 	public Integer getCount() {
 		return count;
 	}
 
 	public void setCount(Integer count) {
 		this.count = count;
 	}
 
 	public Player getSource() {
 		return source;
 	}
 
 	public void setSource(Player source) {
 		this.source = source;
 	}
 
 	public Integer getExperience() {
 		return experience;
 	}
 	
 	public void setExperience(Integer experience) {
 		this.experience = experience;
 	}
 	
 	/**
 	 * Adds every parameter in both message formatters. Note that a and b
 	 * must be non-null and have the same player source.
 	 * @param a - first message formatter to add.
 	 * @param b - second message formatter to add.
 	 * @return The resulting message formatter.
 	 */
 	public static MessageFormatter add(MessageFormatter a, MessageFormatter b) {
 		if (a == null)
 			throw new NullArgumentException("a");
 		if (b == null)
 			throw new NullArgumentException("b");
 		if (!ObjectUtils.equals(a.getSource(), b.getSource()))
 			throw new IllegalArgumentException("Message formatters for different players cannot be added.");
 		
 		// Add values
 		return new MessageFormatter(
 				a.getSource(), 
 				getInt(a.getExperience()) + getInt(b.getExperience()),
 				getInt(a.getCount()) + getInt(b.getCount())
 		);
 	}
 	
 	private static int getInt(Integer value) {
 		return value != null ? value : 0;
 	}
 	
 	// Create a copy of the message formatter with the given parameters
 	public MessageFormatter createView(Player player, Integer experience) {
 		return new MessageFormatter(player, experience);
 	}
 	
 	// Create a copy of the message formatter with the given parameters
 	public MessageFormatter createView(Integer experience) {
 		return createView(null, experience);
 	}
 }
