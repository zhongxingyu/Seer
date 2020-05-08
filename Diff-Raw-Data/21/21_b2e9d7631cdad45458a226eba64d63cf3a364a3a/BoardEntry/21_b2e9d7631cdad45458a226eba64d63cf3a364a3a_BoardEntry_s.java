 //
 //  BoardEntry.java
 //  ChatServer
 //
 //  Created by John McKisson on 4/3/08.
 //  Copyright 2008 __MyCompanyName__. All rights reserved.
 //
 
 import java.io.Serializable;
 import java.lang.reflect.*;
 import java.text.*;
 import java.util.Date;
 
 import com.presence.chat.*;
 
 import static com.presence.chat.ANSIColor.*;
 
 class BoardEntry implements Serializable {
 	Date date;
 	String author;
 	String title;
 	String content;
 	
 	//DateFormat used for displaying creation date of board entries
	transient static final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
	
 	transient static final String TEMPLATE = String.format("%s[%s%%03d%s][%s%%s%s][%s%%13s%s][%s%%-67s%s]", WHT, YEL, WHT, CYN, WHT, GRN, WHT, CYN, WHT);
 	
 	public BoardEntry(ChatClient client, String title, String content) {
 		author = client.getAccount().getName();
 		
 		if (client.getAccount().getLevel() >= 4) {
 			content = content.replace("&k", BLK);
 			content = content.replace("&r", RED);
 			content = content.replace("&g", GRN);
 			content = content.replace("&y", YEL);
 			content = content.replace("&b", BLU);
 			content = content.replace("&m", MAG);
 			content = content.replace("&c", CYN);
 			content = content.replace("&w", WHT);
 			content = content.replace("&N", NRM);
 			content = content.replace("&B", BLD);
 			
 			title = title.replace("&k", BLK);
 			title = title.replace("&r", RED);
 			title = title.replace("&g", GRN);
 			title = title.replace("&y", YEL);
 			title = title.replace("&b", BLU);
 			title = title.replace("&m", MAG);
 			title = title.replace("&c", CYN);
 			title = title.replace("&w", WHT);
 			title = title.replace("&N", NRM);
 			title = title.replace("&B", BLD);
 			
 			//Do some stupid stuff to pad the end of the title so brackets line up...
 			int escLoc, loc = 0, escLen = 0;
 			while ((escLoc = title.indexOf("\u001b[", loc)) != -1) {
 				int len = (1 + title.indexOf('m', escLoc + 2) - escLoc);
 				escLen += len;
 				loc = escLoc + len;
 			}
 			
 			for (int i = 0; title.length() < (67 + escLen); i++)
 				title = title.concat(" ");
 		}
 	
 		this.title = title;
 		this.content = content;
 		
 		date = new Date(System.currentTimeMillis());
 	}
 	
 	public String getAuthor() {
 		return author;
 	}
 	
 	public String getContent() {
 		return content;
 	}
 
 	public String getHeader() {
 		return String.format(TEMPLATE, Bulletin_Board.indexOf(this) + 1, df.format(date), author, title);
 	}
 }
