 package me.arno.blocklog.schedules;
 
 import java.sql.SQLException;
 
 import me.arno.blocklog.BlockLog;
 import me.arno.blocklog.util.Util;
 
 public class AliveSchedule implements Runnable {
 	
 	@Override
 	public void run() {
 		try {
			BlockLog.getInstance().getConnection().createStatement().executeUpdate("UPDATE " + BlockLog.getInstance().getDatabaseManager().getPrefix() + "data SET `id`=1 WHERE `id`=1");
 		} catch(SQLException e) {
 			Util.sendNotice("Something went wrong while sending the alive query");
 		}
 	}
 }
