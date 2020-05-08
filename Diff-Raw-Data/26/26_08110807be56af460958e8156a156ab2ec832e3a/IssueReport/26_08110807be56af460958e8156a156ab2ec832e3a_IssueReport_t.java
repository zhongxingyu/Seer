 package me.makskay.bukkit.tidy;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.makskay.bukkit.tidy.TidyPlugin;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class IssueReport {
 	private String ownerName, description;
 	private int uid; // the ID number by which the issue is uniquely identifiable
 	private Location location; // the location from which this issue was filed
 	private ArrayList<String> comments;
 	private boolean isOpen, isSticky, hasChanged, mustDelete;
 	private long timestamp;
 	
 	public IssueReport(String ownerName, String description, int uid, Location location) {
 		this.ownerName   = ownerName;
 		this.description = description;
 		this.uid         = uid;
 		this.location    = location;
 		this.comments    = new ArrayList<String>();
 		this.isOpen      = true;
 		this.isSticky    = false;
 		this.hasChanged  = false;
 		this.mustDelete  = false;
 		this.timestamp   = System.currentTimeMillis();
 	}
 	
 	public IssueReport(String ownerName, String description, int uid, Location location, 
 			List<String> comments, boolean isOpen, boolean isSticky, long timestamp) {
 		this.ownerName   = ownerName;
 		this.description = description;
 		this.uid         = uid;
 		this.location    = location;
 		this.comments    = new ArrayList<String>();
 		this.isOpen      = isOpen;
 		this.isSticky    = isSticky;
 		this.hasChanged  = false;
 		this.mustDelete  = false;
 		this.timestamp   = timestamp;
 		
 		this.comments.addAll(comments);
 	}
 	
 	public void addComment(String authorName, String comment) {
 		String fullComment = TidyPlugin.NEUTRAL_COLOR + authorName + ": " + comment;
 		comments.add(fullComment);
		
		Player player = null;
		if (!this.ownerName.equals(authorName)) { // if the person who made the change isn't the person who owns the issue
			player = Bukkit.getOfflinePlayer(this.ownerName).getPlayer();
		}
 		if (player != null) { // if the owner of this issue is online
			player.sendMessage(TidyPlugin.NEUTRAL_COLOR + "One of your issues just changed:");
 			player.sendMessage(this.shortSummary());
 			player.sendMessage("  " + fullComment);
 		}
		
 		this.setHasChanged(true);
 	}
 	
 	public void setHasChanged(boolean hasChanged) {
 		this.hasChanged = hasChanged;
 	}
 
 	public boolean markAsOpen(String authorName, String comment) { //returns false if issue was already open
 		if (isOpen) {
 			return false;
 		}
 		
 		if (isSticky) {
 			markAsNonSticky(authorName, comment);
 			comment = "see preceding de-sticky message for details";
 		}
 		
 		isOpen = true;
 		addComment(authorName, "Marked issue as " + statusColor() + "open " + TidyPlugin.NEUTRAL_COLOR + "(" + comment.trim() + ")");
 		return true;
 	}
 	
 	public boolean markAsClosed(String authorName, String comment) { //returns false if issue was already closed
 		if (!isOpen) {
 			return false;
 		}
 		
 		isOpen = false;
 		addComment(authorName, "Marked issue as " + statusColor() + "resolved " + TidyPlugin.NEUTRAL_COLOR + "(" + comment.trim() + ")");
 		return true;
 	}
 	
 	public boolean markAsSticky(String authorName, String comment) {
 		if (isSticky) {
 			return false;
 		}
 		
 		if (isOpen) {
 			markAsClosed(authorName, comment);
 			comment = "see preceding close message for details";
 		}
 		
 		isSticky = true;
 		addComment(authorName, "Marked issue as " + statusColor() + "sticky " + TidyPlugin.NEUTRAL_COLOR + "(" + comment.trim() + ")");
 		return true;
 	}
 	
 	public boolean markAsNonSticky(String authorName, String comment) {
 		if (!isSticky) {
 			return false;
 		}
 		
 		isSticky = false;
 		addComment(authorName, statusColor() + "De-stickied" + TidyPlugin.NEUTRAL_COLOR + " issue (" + comment.trim() + ")");
 		return true;
 	}
 	
 	public String getLocationString() {
 		return location.getWorld().getName() + "," + location.getBlockX() + "," 
 				+ location.getBlockY() + "," + location.getBlockZ();
 	}
 	
 	public boolean canBeEditedBy(CommandSender sender) {
 		return (sender.getName().equals(ownerName)) || (sender.hasPermission("tidy.staff"));
 	}
 	
 	public boolean isIntact() { // used to verify that this issue is valid on disk (and not producing corrupted cache objects)
 		return (ownerName != null) && (description != null) && (uid > 0) && 
 				(location != null) && (comments != null);
 	}
 	
 	public void setShouldBeDeleted(boolean mustDelete) {
 		this.mustDelete = mustDelete;
 	}
 	
 	public boolean shouldBeDeleted() {
 		if (mustDelete) {
 			return true;
 		}
 		
 		return ((!isOpen) && (!hasChanged) && (System.currentTimeMillis() - timestamp > TidyPlugin.issueLifetime));
 	}
 	
 	public String shortSummary() {
 		int charactersPerLine = 80; // TODO this is a placeholder, not a guaranteed value
 		String truncatedDescription = description;
 		if (description.length() > charactersPerLine) {
 			truncatedDescription = description.substring(0, charactersPerLine - 4) + "...";
 		}
 		return statusColor() + "#" + uid + TidyPlugin.NEUTRAL_COLOR + " (" + TidyPlugin.PLAYERNAME_COLOR + 
 				ownerName + TidyPlugin.NEUTRAL_COLOR + "): " + TidyPlugin.NEUTRAL_COLOR + truncatedDescription; 
 	}
 	
 	public List<String> fullSummary() {
 		List<String> summary = new ArrayList<String>();
 		summary.add(TidyPlugin.NEUTRAL_COLOR + "Issue " + TidyPlugin.HIGHLIGHT_COLOR + "#" + uid  + TidyPlugin.NEUTRAL_COLOR +  
 				" ("+ statusDescriptor() + TidyPlugin.NEUTRAL_COLOR + ") by " + TidyPlugin.PLAYERNAME_COLOR + ownerName);
 		summary.add("  " + TidyPlugin.NEUTRAL_COLOR + ownerName + ": " + description);
 		for (String comment : comments) {
 			summary.add("  " + comment);
 		}
 		return summary;
 	}
 	
 	public ChatColor statusColor() {
 		if (isOpen) {
 			return TidyPlugin.UNRESOLVED_COLOR;
 		}
 		
 		if (isSticky) {
 			return TidyPlugin.STICKY_COLOR;
 		}
 		
 		return TidyPlugin.RESOLVED_COLOR;
 	}
 	
 	private String statusDescriptor() {
 		String descriptor = "";
 		
 		if (isOpen) {
 			descriptor = "unresolved";
 		} else if (isSticky) {
 			descriptor = "sticky";
 		} else {
 			descriptor = "resolved";
 		}
 		
 		return statusColor() + descriptor;
 	}
 	
 	public boolean isOpen() {
 		return isOpen;
 	}
 	
 	public boolean isSticky() {
 		return isSticky;
 	}
 	
 	public ArrayList<String> getComments() {
 		return comments;
 	}
 	
 	public String getOwnerName() {
 		return ownerName;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 	
 	public int getUid() {
 		return uid;
 	}
 	
 	public Location getLocation() {
 		return location;
 	}
 	
 	public boolean hasChanged() {
 		return hasChanged;
 	}
 }
