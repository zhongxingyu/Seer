 package se.kayarr.ircbot.modules;
 
 import java.sql.SQLException;
 
 import org.pircbotx.Channel;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 
 import se.kayarr.ircbot.backend.CommandHandler;
 import se.kayarr.ircbot.backend.CommandManager;
 import se.kayarr.ircbot.backend.Module;
 import se.kayarr.ircbot.database.Database;
 import se.kayarr.ircbot.database.Table;
 import se.kayarr.ircbot.database.TableHandler;
 
 public class DatabaseTestModule extends Module implements CommandHandler, TableHandler {
 	
 	private static final int TEST_TABLE_VERSION = 1;
 	private Table testTable;
 
 	@Override
 	public void initialize() {
 		
 		CommandManager.get().newCommand()
 			.addAlias("dbtest")
 			.handler(this)
 			.finish();
 		
 		testTable = Database.moduleData().table("test_table", TEST_TABLE_VERSION, this);
 	}
 	
 	@Override
 	public void onTableCreate(Table table) throws SQLException {
 		
 		table.createIfNonexistant("TEXT VARCHAR");
 	}
 	
 	@Override
 	public void onTableUpgrade(Table table, int oldVersion, int newVersion) throws SQLException {
 	}
 	
 	@Override
 	public void onHandleCommand(PircBotX bot, Channel channel, User user,
 			String command, String parameters) {
 		
 		bot.sendMessage(channel, "Adding text '" + parameters + "'");
 		
 		try {
			testTable.newInsert().put("TEXT", "'" + parameters + "'").insert();
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
