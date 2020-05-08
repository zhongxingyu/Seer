 package pl.azpal.azrank;
 
 import java.util.Calendar;
 import java.util.List;
 
 
 import java.util.Set;
 import org.bukkit.configuration.ConfigurationSection;
 
 //import ru.tehkode.permissions.PermissionGroup;
 
 
 
 class TimeRankChecker implements Runnable {
 	private AZRank plugin;
 
 	public TimeRankChecker(AZRank origin) {
 		this.plugin = origin;
 	}
 	@Override
 	public void run() {
 		try {
 			plugin.debugmsg("Checking Time Ranks");
 			//List<AZRankUser> users = new LinkedList<AZRankUser>();
 			long now = Calendar.getInstance().getTimeInMillis();
 			//GregorianCalendar
 			ConfigurationSection usersSection = plugin.database.getConfigurationSection("users");
 			
 
 			if (usersSection != null) {
                                 Set<String> usersNames = usersSection.getKeys(false);
                                 plugin.tempranks=usersNames.size();
 				for (String userName : usersNames) {
 					//users.add(plugin.getUser(userName));
 					plugin.debugmsg("Checking user: " + userName);
 					long to = plugin.database.getLong("users." + userName + ".to");
 					if(to > 0) {
 						if(to < now) {
 							List<String> oldGroups = plugin.database.getStringList("users." + userName + ".oldRanks");
                                                         
 							plugin.debugmsg("groups count: " + oldGroups.size());
 							if(oldGroups.size() > 0) {
 								//PermissionGroup[] groups = new PermissionGroup[oldGroups.size()];
 								String[] groups= new String[oldGroups.size()];
 								for(int i=0;i<oldGroups.size();i++) {
 									groups[i] = oldGroups.get(i);
 									plugin.debugmsg("Was in group: " + oldGroups.get(i) );
 								}
 								//AZRank.pex.getUser(userName).setGroups(groups);*/
 								try{
									plugin.setGroups(userName, groups);
 								} catch (Exception e) {
 									plugin.log.severe("[AZRank][ERROR]" + e.getMessage());
 								}
								
								
								plugin.log.info("[AZRank] unranked user " + userName + " to group(s) " + oldGroups);
								plugin.database.set("users." + userName, null);
								plugin.save();
 							} else {
 								plugin.log.severe("[AZRank] Failed to unrank user " + userName + "! He haven't 'oldGroups'");
 								plugin.database.set("users." + userName, null);
 								plugin.save();
 							}
 						} else {
 							plugin.debugmsg("Checked " + userName + " - no changes!");
 						}
 					} else {
 						plugin.log.info("[AZRank] Deleting " + userName + "! He haven't correct time");
 						plugin.database.set("users." + userName, null);
 						plugin.save();
 					}
 				}
 			}
 			/*List<Object> users = plugin.database.getList("users");
 			if(users != null){
 				if(users.size()>0) 
 					plugin.log.info("[AZRank]2 INFO: " + users.size());
 				else
 					plugin.log.info("[AZRank]3 INFO: " + users.size() + " | " + users.get(0).toString() + " | " + users.get(0).getClass().getSimpleName());
 			} else {
 				plugin.log.info("[AZRank]4 IS NULL ");
 			}*/
 			
 			
 			
 		} catch(Exception e) {
 			plugin.log.info("[AZRank]ERROR - " + e.getMessage());
 		}
 	}
 
 }
