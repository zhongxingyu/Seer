 package jipdbs.admin.commands;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 
 import jipdbs.admin.Command;
 import jipdbs.admin.utils.Transformer;
 import jipdbs.core.model.Alias;
 import jipdbs.core.model.Player;
 import jipdbs.core.model.Server;
 import jipdbs.core.model.dao.PlayerDAO;
 import jipdbs.core.model.dao.ServerDAO;
 import jipdbs.core.model.dao.impl.PlayerDAOImpl;
 import jipdbs.core.model.dao.impl.ServerDAOImpl;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import net.sf.json.JSONSerializer;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Email;
 import com.google.appengine.api.datastore.Entity;
 
 public class LoadDataOld extends Command {
 
 	@Override
 	protected void execute(String[] args) throws Exception {
 		
 		Reader input = new FileReader(args[0]);
 		
 		System.out.println("Reading " + args[0]);
 
 		BufferedReader reader = new BufferedReader(input);
 		
 		String line = reader.readLine();
 		while (line != null) {
 			JSONObject info = (JSONObject) JSONSerializer.toJSON(line);
 			processPlayer(info);
 			line = reader.readLine();
 		}
 		
 		reader.close();
 		input.close();
 		
 		System.out.print("Done");
 
 	}
 
 	private void processPlayer(JSONObject obj) {
 		
 		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
 		
 		Server server = processServer(obj.getJSONObject("server"));
 		
 		PlayerDAO playerDao = new PlayerDAOImpl();
 		
 		Player player = new Player();
 		player.setGuid(obj.getString("guid"));
 		player.setCreated(Transformer.string_to_date(obj.getString("created")));
 		player.setUpdated(Transformer.string_to_date(obj.getString("updated")));
 		player.setBanInfoUpdated(Transformer.string_to_date(obj.getString("baninfoupdated")));
 		player.setBanInfo(Transformer.string_to_null(obj.getString("baninfo")));
 		player.setClientId(Transformer.string_to_long(obj.getString("clientid")));
 		player.setLevel(Transformer.string_to_long(obj.getString("level")));
 		player.setNote(Transformer.string_to_null(obj.getString("note")));
 		player.setConnected(false);
 		player.setServer(server.getKey());
 		playerDao.save(player);
 		
 		/* PROCESS ALIASES */
 		JSONArray aliasses = obj.getJSONArray("aliasses");
 		List<Entity> aliasList = new ArrayList<Entity>();
 		int count = 0; 
 		for (Object aObj : aliasses) {
 			count += 1;
 			JSONObject alias = (JSONObject) aObj;
 			aliasList.add(processAlias(server, player, alias).toEntity());
 			if (count == 100) {
 				ds.put(aliasList);
 				aliasList.clear();
 				count = 0;
 			}
 		}
 		if (aliasList.size()>0) {
 			ds.put(aliasList);
 		}
 
 	}
 
 	private Alias processAlias(Server server, Player player, JSONObject obj) {
 		Alias alias = new Alias(player.getKey());
 		alias.setServer(server.getKey());
 		alias.setNickname(obj.getString("nickname"));
 		alias.setIp(obj.getString("ip"));
 		alias.setCreated(Transformer.string_to_date(obj.getString("created")));
 		alias.setUpdated(Transformer.string_to_date(obj.getString("updated")));
 		alias.setCount(Transformer.string_to_long(obj.getString("count")));
 		
 		JSONArray ngramsObj = obj.getJSONArray("ngrams");
 		List<String> ngrams = new ArrayList<String>();
 		for (int i = 0; i < ngramsObj.size(); i++) {
 			ngrams.add(ngramsObj.getString(i));
 		}
 		alias.setNgrams(ngrams);
 		return alias;
 	}
 
 	private Server processServer(JSONObject obj) {
 
 		ServerDAO serverDAO = new ServerDAOImpl();
 		Server server = serverDAO.findByUid(obj.getString("uid"));
 		
 		if (server == null) {
 			server = new Server();
 			server.setUid(obj.getString("uid"));
 			server.setName(obj.getString("name"));
 			server.setAdmin(new Email(Transformer.string_to_null(obj.getString("admin"))));
 			server.setCreated(Transformer.string_to_date(obj.getString("created")));
 			server.setUpdated(Transformer.string_to_date(obj.getString("updated")));
 			server.setOnlinePlayers(0);
 			server.setAddress(Transformer.string_to_null(obj.getString("address")));
 			server.setPluginVersion(Transformer.string_to_null(obj.getString("pluginversion")));
 			server.setMaxLevel(Transformer.string_to_long(obj.getString("maxlevel")));
 			server.setDirty(false);
 			serverDAO.save(server);
 		}
 		
 		return server;
 	}
 	
 }
