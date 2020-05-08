 package com.lala.wordrank.sql;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import lib.PatPeter.SQLibrary.SQLite;
 
 import org.bukkit.ChatColor;
 
 import com.lala.wordrank.Word;
 import com.lala.wordrank.WordRank;
 
 public class SQLWord {
 
 	private Word word;
 	private WordRank plugin;
 	private SQLite sql;
 	
 	public SQLWord(WordRank plugin, Word word){
 		this.plugin = plugin;
 		this.word = word;
 		this.sql = this.plugin.sql;
 	}
 	
 	public String addToDB(){
 		if (wordExists()){
 			return String.valueOf(ChatColor.RED + "The word "+ChatColor.GOLD+word.getName()+ChatColor.RED+" already exists!");
 		}else{
 			try {
 				Connection con = sql.getConnection();
 				PreparedStatement p = con.prepareStatement("INSERT INTO wordrank VALUES (?, ?)");
 				p.setString(1, word.getName());
 				p.setString(2, word.getGroup());
 				p.addBatch();
 				con.setAutoCommit(false);
 				p.executeBatch();
 				con.setAutoCommit(true);
				return String.valueOf(ChatColor.GREEN + "Word "+ChatColor.GOLD+word.getName()+" has been successfully added!");
 			} catch (SQLException e){
				plugin.sendErr("Error while adding the word "+word.getName()+" to the DB");
 				e.printStackTrace();
 				return String.valueOf(ChatColor.RED + "Error adding the word "+ChatColor.GOLD+word.getName()+ChatColor.RED+". Please check the console for more info.");
 			}
 		}
 	}
 	
 	public String removeFromDB(){
 		if (!wordExists()){
 			return String.valueOf(ChatColor.RED + "The word "+ChatColor.GOLD+word.getName()+ChatColor.RED+" does not exist!");
 		}else{
 			sql.query("delete from wordrank where name='"+word.getName()+"'");
 			return String.valueOf(ChatColor.GREEN + "Word "+ChatColor.GOLD+word.getName()+ChatColor.GREEN+" has been deleted.");
 		}
 	}
 	
 	public boolean wordExists(){
 		ArrayList<String> w = getWords();
 		if (w.equals(null)) return false;
 		if (w.contains(word.getName())){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	public ArrayList<String> getWords(){
 		ResultSet rs = sql.query("SELECT name FROM wordrank");
 		ArrayList<String> w = new ArrayList<String>();
 		
 		try {
 			while (rs.next()){
 				w.add(rs.getString(1));
 			}
 			return w;
 		}catch (SQLException e){
			plugin.send("SQLException while getting words from the database");
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public String getWordGroup(){
 		ResultSet rs = sql.query("SELECT groupname FROM wordrank WHERE name='"+word.getName()+"'");
 		try {
 			return rs.getString(1);
 		} catch (SQLException e){
			plugin.sendErr("SQLException while getting the word "+word.getName()+"'s group from the DB");
 			e.printStackTrace();
 			return null;
 		}
 	}
 }
