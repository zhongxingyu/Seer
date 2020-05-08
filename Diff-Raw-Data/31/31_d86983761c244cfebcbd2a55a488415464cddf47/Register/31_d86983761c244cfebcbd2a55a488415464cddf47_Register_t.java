 package bitlegend.bitauth.commands;
 
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.security.*;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ConcurrentModificationException;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.*;
 import org.bukkit.entity.Player;
 
 import bitlegend.bitauth.BitAuth;
 
 public class Register implements CommandExecutor {
 	private BitAuth instance;
 	
 	// Database info
 	private String user = "";
 	private String pass = "";
 	private String url = "";
 	private String logintable = "";
 	
 	public Register(BitAuth instance) {
 		this.instance = instance;
 		user = instance.config.readString("DB_User");
 		pass = instance.config.readString("DB_Pass");
 		url = "jdbc:mysql://" + instance.config.readString("DB_Host") + 
 			"/" + instance.config.readString("DB_Name");
 		logintable = instance.config.readString("DB_Table_login");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
 		boolean r = false;
 		Player player = (Player)sender;
 
 		if (split.length == 1) {
 			try {
 				// Create connection
 				Connection conn = DriverManager.getConnection(url, user, pass);
 				
 				// Check for pre-existing user by this name
 				Statement select = conn.createStatement();
 				ResultSet result = select.executeQuery(
 						"SELECT * FROM `" + logintable + "`");
 				
 				boolean playerFound = false;
 				
 				if (result.next()) { // Results found
 					do {
 						String username = result.getString(1);
 						if (username.equals(player.getDisplayName())) { // Player data found
 							player.sendMessage(ChatColor.GREEN
 									+ "Error: user by the name of "
 									+ player.getDisplayName()
 									+ " already found");
 							playerFound = true;
 						}
 					} while (result.next());
 				}
 				
 				if (playerFound == false) { // Player not found
 					// Generate new salt and password from salt and input
 					byte[] salt = GenerateSalt();
 					byte[] hash = GenerateHash(split[0], salt);
 
 					// Get unix time stamp
 					long unixtime = System.currentTimeMillis() / 1000L;
 
 					// Get player IP as long
 					InetAddress iAddress = player.getAddress().getAddress();
 					long playerIP = instance.ipToLong(iAddress.getHostAddress());
 
 					// Set up the query
 					String query = "INSERT INTO `" + logintable + "` "
 							+ "(`username`, `salt`, `password`, `whitelist`, "
							+ "`lastlogintime`, `ipaddress`, `enableipcheck`, "
							+ "`pwreset`, `temppwd`, `tmpsalt`) VALUES (?,?,?,?,?,?,?,?,?,?)";
 					PreparedStatement statement = conn.prepareStatement(query);
 
 					// Set query values; 1 = user, 2 = salt, 3 = password hash,
 					// 4 = white list T/F, 5 = last login time, 6 = player ip,
					// 7 = enable ip checking, 8 = pwreset T/F, 9 = temp pw,
					// 10 = temp salt
 					statement.setString(1, player.getDisplayName());
 					statement.setBytes(2, salt);
 					statement.setBytes(3, hash);
 					statement.setBoolean(4, true);
 					statement.setLong(5, unixtime);
 					statement.setLong(6, playerIP);
 					statement.setBoolean(7, false);
					statement.setBoolean(8, false);
					statement.setBytes(9, null);
					statement.setBytes(10, null);
 
 					// Execute the query
 					statement.executeUpdate();
 
 					// Close statement
 					statement.close();
 
 					// Remove player from unregistered list
 					int index = 0;
 					for (Player p : instance.unregistered) {
 						if (p.getDisplayName().equals(player.getDisplayName()))
 							index = instance.unregistered.indexOf(p);
 					}
 					instance.unregistered.remove(index);
 
 					player.sendMessage(ChatColor.GREEN
 							+ "Thank you for registering with our server!");
 				}
 				// Close result and connection
 				result.close();
 				conn.close();
 				
 			} catch (ConcurrentModificationException cmx) {
 				cmx.printStackTrace();
 			} catch (UnsupportedEncodingException uex) {
 				uex.printStackTrace();
 			} catch (NoSuchAlgorithmException nsax) {
 				nsax.printStackTrace();
 			} catch (SQLException se) {
 				se.printStackTrace();
 			}
 			
 			r = true;
 		}
 
 		return r;
 	}
 	
 	private byte[] GenerateSalt() {
 		Random r = new SecureRandom();
 		byte[] salt = new byte[20];
 		r.nextBytes(salt);
 		
 		return salt;
 	}
 	
 	private byte[] GenerateHash(String input, byte[] salt)
 			throws NoSuchAlgorithmException, UnsupportedEncodingException {
 		MessageDigest md = MessageDigest.getInstance("SHA-512");
 		md.reset();
 		md.update(salt);
 
 		return md.digest(input.getBytes("UTF-8"));
 	}
 }
