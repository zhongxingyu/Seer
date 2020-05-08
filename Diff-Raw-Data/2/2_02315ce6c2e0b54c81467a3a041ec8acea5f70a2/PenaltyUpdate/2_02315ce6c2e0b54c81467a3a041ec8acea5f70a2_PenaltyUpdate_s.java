 package jipdbs.admin.commands;
 
 import java.util.Arrays;
 import java.util.Date;
 
 import jipdbs.admin.Command;
 import jipdbs.admin.utils.EntityIterator;
 import jipdbs.admin.utils.EntityIterator.Callback;
 import jipdbs.core.model.Penalty;
 import jipdbs.core.model.Player;
 import jipdbs.core.model.dao.PenaltyDAO;
 import jipdbs.core.model.dao.PlayerDAO;
 import jipdbs.core.model.dao.impl.PenaltyDAOImpl;
 import jipdbs.core.model.dao.impl.PlayerDAOImpl;
 import jipdbs.info.PenaltyInfo;
 import jipdbs.legacy.python.date.DateUtils;
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 
 import com.google.appengine.api.datastore.Cursor;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 
 public class PenaltyUpdate extends Command {
 
 	static int count = 0;
 
 	@Override
 	protected void execute(OptionSet options) throws Exception {
 
 		initializeState();
 		
 		int limit = Integer.MAX_VALUE;
 		if (options.hasArgument("limit")) {
 			limit = (Integer) options.valueOf("limit");
 		}
 		
 		boolean force = false;
 		if (options.has("force")) {
 			force = true;
 		}
 
 		Cursor cursor = null;
 		if (!force) cursor = loadCursor();
 		
 		if (cursor == null) {
 			System.out.println("Starting process");	
 		} else {
 			System.out.println("Resuming from previous state");
 		}
 		
 		count = 0;
 		
 		Query q = new Query("Player");
 		q.addFilter("baninfoupdated", FilterOperator.NOT_EQUAL, null);
 
 		final PlayerDAO playerDAO = new PlayerDAOImpl();
 		final PenaltyDAO penaltyDAO = new PenaltyDAOImpl();
 		
 		EntityIterator.iterate(q, limit, cursor, new Callback() {
 			@Override
 			public void withEntity(Entity entity, DatastoreService ds, Cursor cursor, long total)
 					throws Exception {
 
 				count = count + 1;
 
 				saveCursor(cursor);
 
 				System.out.println(count);
 				
 				try {
 					Player player = new Player(entity);
 					String data = player.getBanInfo();
 					if (!data.startsWith("#")) return;
 						
 					String[] parts = data.split("::");
 					PenaltyInfo info = new PenaltyInfo();
 					info.setType(Penalty.BAN);
 					info.setCreated(DateUtils.timestampToDate(Long.parseLong(parts[1])));
 					info.setDuration(Long.parseLong(parts[2]));
 					if (parts.length == 4) info.setReason(parts[3]);
 
 					if (info.getDuration()>0) {
 						if (info.getExpires().before(new Date())) {
 							/* ban expired. clean */
 							player.setBanInfo(null);
 							player.setBanInfoUpdated(null);
 							playerDAO.save(player);
 							System.out.println("Penalty expired.");
 							return;
 						}
 					}
 
 					player.setBanInfo(info.getRawData());
 					playerDAO.save(player);
 					
					Penalty penalty = new Penalty(player);
 					penalty.setActive(true);
 					penalty.setSynced(true);
 					penalty.setAdmin(null);
 					penalty.setCreated(info.getCreated());
 					penalty.setDuration(info.getDuration());
 					penalty.setType(info.getType());
 					penalty.setUpdated(info.getCreated());
 					penalty.setReason(info.getReason());
 					
 					penaltyDAO.save(penalty);
 					
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 		});
 
 		System.out.println("Done");
 
 	}
 
 	@Override
 	public OptionParser getCommandOptions() {
 		OptionParser parser = new OptionParser() {
             {
                 acceptsAll( Arrays.asList("l", "limit") ).withOptionalArg().ofType(Integer.class)
                     .describedAs( "limit" );
                 acceptsAll( Arrays.asList("f", "force"), "do not resume");
             }
         };
 		return parser;
 	}
 
 }
