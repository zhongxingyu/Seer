 package net.mariusgundersen.bookmarks.domain;
 
 import qvc.executables.Command;
 
 public class AddBookmark extends Command {
	public final String url;
 	public final String name;
 	
	public AddBookmark(String url, String name) {
		this.url = url;
 		this.name = name;
 	}
 }
