 package com.adamki11s.questx.sql;
 
 import java.sql.SQLException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 
 import com.adamki11s.questx.QuestX;
 import com.adamki11s.sync.sql.SyncSQL;
 
 public class SQLTables {
 
 	public static void initiateSQLite(SyncSQL sql) {
 		QuestX.logMSG("Connecting to SQLite database...");
 		if (sql.initialise()) {
			QuestX.logMSG("Connection successfull!");
 		} else {
 			QuestX.logMSG("Something went wrong!");
 		}
 
 		/*try {
 			if (!sql.doesTableExist("reputation")) {
 				String createRepTable = "CREATE TABLE reputation ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'player' VARCHAR(16), 'reputation' INTEGER NOT NULL)";
 				String insertRep = "INSERT INTO reputation (player,reputation) VALUES ('default',0)";
 				sql.standardQuery(createRepTable);
 				sql.standardQuery(insertRep);
 				QuestX.logMSG("Created player reuputation table.");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}*/
 
 		for (World w : Bukkit.getServer().getWorlds()) {
 			try {
 				if (!sql.doesTableExist(w.getName())) {
 					String worldTable = "CREATE TABLE " + w.getName() + "('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'x' INTEGER NOT NULL, 'z' INTEGER NOT NULL, 'density' INTEGER NOT NULL)";
 					String insert = "INSERT INTO " + w.getName() + " (x,z,density) VALUES (0,0,0)";
 					QuestX.logMSG("Creating SQLite table '" + w.getName() + "'.");
 					sql.standardQuery(worldTable);
 					sql.standardQuery(insert);
 					QuestX.logMSG("Table created successfully.");
 
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
