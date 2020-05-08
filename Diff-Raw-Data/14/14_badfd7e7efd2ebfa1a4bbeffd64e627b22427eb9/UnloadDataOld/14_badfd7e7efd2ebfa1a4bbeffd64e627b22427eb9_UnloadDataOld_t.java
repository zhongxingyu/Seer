 package jipdbs.admin.commands;
 
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import jipdbs.admin.Command;
 import jipdbs.admin.utils.EntityIterator;
 import jipdbs.admin.utils.EntityIterator.Callback;
 import jipdbs.admin.utils.EscapeChars;
 import jipdbs.admin.utils.Transformer;
 import jipdbs.core.model.Alias;
 import jipdbs.core.model.Player;
 import jipdbs.core.model.Server;
 import jipdbs.core.model.dao.AliasDAO;
 import jipdbs.core.model.dao.ServerDAO;
 import jipdbs.core.model.dao.impl.AliasDAOImpl;
 import jipdbs.core.model.dao.impl.ServerDAOImpl;
 import jipdbs.core.util.Functions;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 
 public class UnloadDataOld extends Command {
 
 	static int count = 0;
 
 	@Override
 	protected void execute(String[] args) throws Exception {
 		final int maxEntities = Integer.MAX_VALUE;
 
 		final AliasDAO aliasDAO = new AliasDAOImpl();
 
 		
 		Writer wrt = new FileWriter(args[0]); 
 		
 		System.out.println("Writing to " + args[0]);
 		
 		int offset = 0;
 		try {
 			offset = Integer.parseInt(args[1]); 
 		} catch (Exception e) {
 		}
 
 		int limit = maxEntities;
 		try {
 			limit = Integer.parseInt(args[2]);
 		} catch (Exception e) {
 		}
 		
 		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
 
 		PreparedQuery pq = ds.prepare(new Query("Player"));
 		final int total = pq.countEntities(withLimit(limit).offset(offset));
 		count = 0;
 
 		System.out.println("Processing " + total + " records.");
 
 		final BufferedWriter out = new BufferedWriter(wrt);
 		
 		EntityIterator.iterate("Player", limit, offset, 100, new Callback() {
 			@SuppressWarnings("deprecation")
 			@Override
 			public void withEntity(Entity entity, DatastoreService ds)
 					throws Exception {
 
 				final Player player = new Player(entity);
 
 				count = count + 1;
 				
 				Alias lastAlias = null;
 				
 				if (player.getNickname() != null) {
 					lastAlias = new Alias(player.getKey());
 					lastAlias.setNickname(player.getNickname());
 					lastAlias.setIp(player.getIp());
 				} else {
 					lastAlias = aliasDAO.getLastUsedAlias(player.getKey());	
 				}
 				if (lastAlias == null) return;
 
 				/* PROCESS SERVER */
 				ServerDAO serverDAO = new ServerDAOImpl();
 				Server server = serverDAO.get(player.getServer());
 				if (server == null) return;
 				StringBuilder serverb = new StringBuilder();
 				serverb.append("\"uid\":").append(EscapeChars.toString(server.getUid())).append(",");
 				serverb.append("\"name\":").append(EscapeChars.toString(server.getName())).append(",");
 				serverb.append("\"admin\":").append(EscapeChars.toString(server.getAdmin().getEmail())).append(",");
 				serverb.append("\"created\":").append(EscapeChars.toString(Transformer.date_to_string(server.getCreated()))).append(",");
 				serverb.append("\"updated\":").append(EscapeChars.toString(Transformer.date_to_string(server.getUpdated()))).append(",");
 				serverb.append("\"address\":").append(EscapeChars.toString(server.getAddress())).append(",");
 				serverb.append("\"pluginversion\":").append(EscapeChars.toString(server.getPluginVersion())).append(",");
 				serverb.append("\"maxlevel\":").append(EscapeChars.toString(server.getMaxLevel())).append(",");
 				
 				/* PROCESS PLAYER ALIASES */
 				final List<String> aliases = new ArrayList<String>();
 				Query aliasQuery = new Query("Alias");
 				aliasQuery.setAncestor(player.getKey());
 				EntityIterator.iterate(aliasQuery, Integer.MAX_VALUE, new Callback() {
 					@Override
 					public void withEntity(Entity entity, DatastoreService ds) throws Exception {
 						Alias alias = new Alias(entity);
 						StringBuilder a = new StringBuilder();
 						a.append("\"nickname\":").append(EscapeChars.toString(EscapeChars.forJSON(alias.getNickname()))).append(",");
 						a.append("\"ip\":").append(EscapeChars.toString(alias.getIp())).append(",");
 						a.append("\"created\":").append(EscapeChars.toString(Transformer.date_to_string(alias.getCreated()))).append(",");
 						a.append("\"updated\":").append(EscapeChars.toString(Transformer.date_to_string(alias.getUpdated()))).append(",");
 						a.append("\"count\":").append(EscapeChars.toString(alias.getCount())).append(",");
 						StringBuilder ngrams = new StringBuilder();
						if (alias.getNgrams() != null) {
							for (Iterator<String> it = alias.getNgrams().iterator(); it.hasNext() ; ) {
								ngrams.append(EscapeChars.toString(EscapeChars.forJSON(it.next())));
								if (it.hasNext()) ngrams.append(",");
							}
 						}
 						a.append("\"ngrams\": [").append(ngrams.toString()).append("]");
 						aliases.add("{" + a.toString() + "}");
 					}
 				});
 				
 				StringBuilder p = new StringBuilder();
 				p.append("\"nickname\":").append(EscapeChars.toString(EscapeChars.forJSON(lastAlias.getNickname()))).append(",");
 				p.append("\"ip\":").append(EscapeChars.toString(lastAlias.getIp())).append(",");
 				p.append("\"guid\":").append(EscapeChars.toString(player.getGuid())).append(",");
 				p.append("\"baninfo\":").append(EscapeChars.toString(player.getBanInfo())).append(",");
 				p.append("\"baninfoupdated\":").append(EscapeChars.toString(Transformer.date_to_string(player.getBanInfoUpdated()))).append(",");
 				p.append("\"created\":").append(EscapeChars.toString(Transformer.date_to_string(player.getCreated()))).append(",");
 				p.append("\"updated\":").append(EscapeChars.toString(Transformer.date_to_string(player.getUpdated()))).append(",");
 				p.append("\"clientid\":").append(EscapeChars.toString(player.getClientId())).append(",");
 				p.append("\"level\":").append(EscapeChars.toString(player.getLevel())).append(",");
 				p.append("\"note\":").append(EscapeChars.toString(EscapeChars.forJSON(player.getNote()))).append(",");
 				p.append("\"server\": {").append(serverb.toString()).append("}").append(",");
 				p.append("\"aliasses\":[").append(Functions.join(aliases, ",")).append("]");
 				
 				//if (count > 1) out.write(",");
 				out.write("{" + p.toString() + "}\n");
 				out.flush();
 				
 				System.out.println("%" + (count * 100) / total);
 
 			}
 		});
 		
 		out.close();
 		wrt.close();
 		
 		System.out.print("Done");
 
 	}
 	
 }
