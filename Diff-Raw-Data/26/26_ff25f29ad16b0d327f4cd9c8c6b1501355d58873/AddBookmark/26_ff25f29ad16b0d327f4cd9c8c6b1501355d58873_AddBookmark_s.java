 package net.mariusgundersen.bookmarks.domain;
 
 import qvc.executables.Command;
 
 public class AddBookmark extends Command {
	public final String link;
 	public final String name;
 	
	public AddBookmark(String link, String name) {
		this.link = link;
 		this.name = name;
 	}
 }
