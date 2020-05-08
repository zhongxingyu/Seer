 package ww10;
 
 import game.PublicPlayerInfo;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.util.HashMap;
 
 import org.apache.commons.io.IOUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.biotools.meerkat.GameInfo;
 
 import ww10.exception.InvalidInputException;
 import ww10.exception.AuthorizationException;
 import ww10.exception.TableFullException;
 import ww10.DataModel;
 import bots.prologbot.PrologBot;
 
 public class RequestHandler implements Runnable {
 
 	private Socket clientSocket;
 	private PrologBotServer server;
 	
 	public RequestHandler(Socket clientSocket, PrologBotServer server)
 	{
 		this.clientSocket = clientSocket;
 		this.server = server;
 	}
 	@Override
 	public void run() {
 		try {
 			InputStream inputStream = clientSocket.getInputStream();
 			OutputStream outputStream = clientSocket.getOutputStream();
 			handleRequest(inputStream, outputStream);
 			inputStream.close();
 			outputStream.close();
 			clientSocket.close();
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 			System.out.println("Accept failed: 4444");
 			
 			//System.exit(-1);
 		}
 	}
 
 	
 	private void handleRequest(InputStream inputStream, OutputStream outputStream) {
 		
 		DataOutputStream out = new DataOutputStream(outputStream);
 
 		try {		
 			String request = IOUtils.toString(inputStream);
 			//TEST TODO: remove console feedback
 			System.out.println("Incoming request: " + request);
 
 			JSONObject requestO = new JSONObject(request).getJSONObject("request");
 			
 			String requestType = requestO.getString("type");
 			
 			String reply = null;
 			
 			if(requestType.equals( "startTable")){
 				reply = handleStartTableRequest(requestO);
 			}
 			else if(requestType.equals( "joinTable")){
 				reply = handleJoinTableRequest(requestO);			
 			}
 			else if(requestType.equals( "fetchData")){
 				reply = handleFetchDataRequest(requestO);
 			}
 			else if(requestType.equals("listTables")){
 				reply = handleListTablesRequest();
 			}
 			// Admin functionality
 			
 			else if(requestType.equals("stopTable")){
 				reply = handleStopTableRequest(requestO);
 			}
 			else if(requestType.equals("killTable")){
 				reply = handleKillTableRequest(requestO);
 			}
 			else if(requestType.equals("killTestTable")){
 				reply = handleKillTestTableRequest(requestO);
 			}
 			else if(requestType.equals("resumeTable")){
 				reply = handleResumeTableRequest(requestO);
 			}
 			else if(requestType.equals("leaveTable")){
 				reply = handleLeaveTableRequest(requestO);
 			}
 			else if(requestType.equals("kickPlayer")){
 				reply = handleKickTableRequest(requestO);
 			}
 			else{
 				//requestType does not match any known request -> Error
 				throw new InvalidInputException("RequestType " + requestType + 
 						" does not match any known request");
 			}
 			
 			if(reply != null){
 				out.writeBytes(reply);
 			}
 
 
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 			System.out.println("Could not parse from inputstream.");
 			//System.exit(-1);
 		} catch (JSONException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
 			try {
 				out.writeBytes(createError("JSON", "Could not parse from JSON"));
 			} catch (IOException e1) {
 				// Can not reach the webserver - not much can be done about this...
 				e1.printStackTrace();
 			}
 		} catch (InvalidInputException e){
			System.out.println("Invalid input: "+e.getMessage());
 			try {
 				out.writeBytes(createError("InvalidInput", e.getMessage()));
 			} catch (IOException e1) {
 				// Can not reach the webserver - not much can be done about this...
 				e1.printStackTrace();
 			}
 		} catch (TableFullException e){
			System.out.println("Table is full: "+e.getMessage());
 			try {
 				out.writeBytes(createError("TableFull", e.getMessage()));
 			} catch (IOException e1) {
 				// Can not reach the webserver - not much can be done about this...
 				e1.printStackTrace();
 			}
 		} catch (AuthorizationException e) {
			System.out.println("Authorization failure: "+e.getMessage());
 			try {
 				out.writeBytes(createError("Authorization", e.getMessage()));
 			} catch (IOException e1) {
 				// Can not reach the webserver - not much can be done about this...
 				e1.printStackTrace();
 			}
 		} 
 		
 		System.out.println("Message handled");
		try {
			inputStream.close();
		} catch (IOException e) {
			System.out.println("Could not close inputstream");
			e.printStackTrace();
		}
 
 	}
 	
 	private String handleFetchDataRequest(JSONObject requestO) throws JSONException, IOException, InvalidInputException {
 		String tableName = requestO.getString("tableName");
 		
 		JSONObject results = new JSONObject();
 		JSONArray players = new JSONArray();
 		
 		Table table = server.getTable(tableName);
 				
 		if(table == null){
 			throw new InvalidInputException("Table " +tableName + " does not exist");
 		}
 		
 		server.tableReceivedRequest(tableName);
 		
 		DataModel tableResults = table.getDataModel();
 				
 		for(String player: tableResults.getPlayerNames())
 		{
 			players.put(getPlayerStats(player, tableResults));
 		}
 		
 		JSONObject player = new JSONObject();
 		player.put("player", players);
 		results.put("result", player);
 		if(table.isRunning())
 		{
 			results.put("gamesPerSec", tableResults.getGamesPerSecond());
 		}
 		else 
 		{
 			results.put("gamesPerSec", 0);
 		}
 		
 		results.put("bigBlind", server.getGameparameters().getBigBlind());
 		results.put("smallBlind", server.getGameparameters().getSmallBlind());
 		results.put("minimumRaise", server.getGameparameters().getBigBlind());
 		results.put("startMoney", server.getGameparameters().getInitialBankroll());
 		
 		String reply = results.toString();
 		System.out.println("reply");
 		
 		return reply;
 	}
 	
 	private String handleStartTableRequest(JSONObject requestO) throws JSONException, InvalidInputException{
 
 		String tableName = requestO.getString("tableName");
 		int nbPlayers = requestO.getInt("nbPlayers");
 
 		
 		if(server.getTable(tableName) != null){
 			throw new InvalidInputException("Table " + tableName + " already exists.");
 		}
 		if(nbPlayers <= 1 || nbPlayers >= 24){
 			throw new InvalidInputException("Can not support a table with " + nbPlayers + " players." +
 					"Only values between 2 and 23 are accepted.");
 		}
 		
 		String pw = requestO.getString("password");
 		Table currTable = 
 			new Table(tableName, nbPlayers, pw, server.getGameparameters());
 		currTable.run();
 		
 		server.addTable(tableName, currTable);
 		
 		System.out.println("Created table " + tableName + " for " + nbPlayers + " players.");
 		
 		return createReply("Created table " + tableName + " for " + nbPlayers + " players.");
 	}
 	
 	private String handleJoinTableRequest(JSONObject requestO) 
 			throws JSONException, InvalidInputException, TableFullException{
 		String tableName = requestO.getString("tableName");
 		
 		Table table = server.getTable(tableName);
 				
 		if(table == null){
 			throw new InvalidInputException("Table " + tableName + " does not exist.");
 		}
 		
 		server.tableReceivedRequest(tableName);
 		
 		String playerName = requestO.getString("playerName");
 		
 		onNewBot(requestO, table);
 		
 		table.resume();
 		
 		System.out.println("Player " + playerName + " has joined table " + tableName + ".");
 		
 		return createReply("Player " + playerName + " has joined table " + tableName + ".");
 	}
 	
 	private String handleLeaveTableRequest(JSONObject requestO) throws JSONException, InvalidInputException{
 		String tableName = requestO.getString("tableName");
 		
 		Table table = server.getTable(tableName);
 		
 		if(table == null)
 		{
 			if(table == null){
 				throw new InvalidInputException("Table " + tableName + " does not exist.");
 			}
 		}
 		
 		server.tableReceivedRequest(tableName);
 		
 		String playerName = requestO.getString("playerName");		
 		
 		onLeavingTable(playerName, table);
 		
 		table.resume();
 		
 		return createReply("Player " + playerName + " has left table " + tableName + ".");
 	}
 	
 	private String handleKickTableRequest(JSONObject requestO) throws JSONException, InvalidInputException, AuthorizationException{
 		String tableName = requestO.getString("tableName");
 		String pw = requestO.getString("password");
 		Table table = server.getTable(tableName);
 		
 		if(table == null)
 		{
 			if(table == null){
 				throw new InvalidInputException("Table " + tableName + " does not exist.");
 			}
 		}
 		
 		if(table.passwordValid(pw)){
 			return handleLeaveTableRequest(requestO);
 		}
 		else{
 			throw new AuthorizationException("Password is incorrect.");
 		}
 	}
 		
 	private String handleListTablesRequest() throws JSONException{
 		JSONObject result = new JSONObject();
 		
 		JSONArray tables = new JSONArray();
 		
 		for(String tableName: server.listTableNames()){
 			Table currTable = server.getTable(tableName);
 			JSONObject currTableJSON = new JSONObject();
 			currTableJSON.put("name", tableName);
 			currTableJSON.put("nbPlayers", currTable.getGameInfo().getNumPlayers());
 			currTableJSON.put("running", currTable.isRunning());
 			tables.put(currTableJSON);
 		}
 		
 		result.put("tables", tables);
 		
 		return result.toString();
 	}
 	
 	private String handleStopTableRequest(JSONObject requestO) throws JSONException, InvalidInputException, AuthorizationException{
 		String tableName = requestO.getString("tableName");
 		
 		Table table = server.getTable(tableName);
 		
 		if(table==null){
 			throw new InvalidInputException("Simulation of table " + tableName + " cannot be stopped as " +
 					"it does not exist");
 		}
 		
 		if(table.passwordValid(requestO.getString("password"))){
 			table.stop();
 		}
 		else
 		{
 			throw new AuthorizationException("Password is incorrect.");
 		}
 		
 		
 		return createReply("Stopped simulation of table " + tableName + 
 				". Table data are still available");
 	}
 	
 	private String handleKillTableRequest(JSONObject requestO) throws JSONException, InvalidInputException, AuthorizationException{
 		String tableName = requestO.getString("tableName");
 		
 		Table table = server.getTable(tableName);
 		
 		if(table==null){
 			throw new InvalidInputException("Data of table " + tableName + " cannot be removed as " +
 			"it does not exist");
 		}
 		
 		
 		if(table.passwordValid(requestO.getString("password"))){
 			table.terminate();
 			server.removeTable(tableName);
 		}
 		else
 		{
 			throw new AuthorizationException("Password is incorrect.");
 		}
 		
 		return createReply("Removed table " + tableName + 
 				" from records and stopped its simulation");
 	}
 	
 	private String handleKillTestTableRequest(JSONObject requestO) throws InvalidInputException, JSONException{
 		String tableName = requestO.getString("tableName");
 
 		Table table = server.getTable(tableName);
 
 		if(table==null){
 			throw new InvalidInputException("Data of table " + tableName + " cannot be removed as " +
 			"it does not exist");
 		}
 		
 		table.terminate();
 		server.removeTable(tableName);
 		
 		return createReply("Removed test table " + tableName + 
 		" from records and stopped its simulation");
 	}
 
 	private String handleResumeTableRequest(JSONObject requestO) throws JSONException, InvalidInputException, AuthorizationException{
 		String tableName = requestO.getString("tableName");
 		
 		Table table = server.getTable(tableName);
 		
 		if(table==null){
 			throw new InvalidInputException("Data of table " + tableName + " cannot be removed as " +
 			"it does not exist");
 		}
 		
 		
 		if(table.passwordValid(requestO.getString("password"))){
 			table.resume();
 		}
 		else
 		{
 			throw new AuthorizationException("Password is incorrect.");
 		}
 		
 		return createReply("Resumed simulation of table " + tableName + ".");
 	}
 	
 	private JSONObject getPlayerStats(String player, DataModel tableResults) throws JSONException{
 		JSONObject currPlayer = new JSONObject();
 		currPlayer.put("name", tableResults.getChosenName(player));
 		
 		//Put average profit
 		double avg = tableResults.getAvgProfit(player);
 		
 		currPlayer.put("avg profit", avg);
 		
 		//Put actions
 		int nbFold = tableResults.getNbFolds(player);
 		int nbRaise = tableResults.getNbRaises(player);
 		int nbCall = tableResults.getNbCalls(player);
 		currPlayer.put("nbFolds", nbFold);
 		currPlayer.put("nbRaises", nbRaise);
 		currPlayer.put("nbCalls", nbCall);
 		
 		
 		//Put thinking time
 		
 		long thinkingTime = tableResults.getRunTime(player);
 		currPlayer.put("time used", thinkingTime);
 		
 		//Put rules used
 		
 		HashMap<String, Integer> rulesUsed = tableResults.getRulesUsed(player);
 		
 		JSONArray rulesJSON = new JSONArray();
 		
 		for(String ruleName: rulesUsed.keySet()){
 			JSONObject rule = new JSONObject();
 			rule.put("name", ruleName);
 			rule.put("times", rulesUsed.get(ruleName));
 			rulesJSON.put(rule);
 		}
 		
 		currPlayer.put("rulesUsed", rulesJSON);
 		
 		//Put last submit time
 		
 		currPlayer.put("lastSubmit", tableResults.getLastSubmit(player));
 		return currPlayer;
 	}
 
 	protected void onNewBot(JSONObject botDescr, Table table) throws JSONException, TableFullException {
 		String playerName = botDescr.getString("playerName");
 		String fixedName = table.getDataModel().getFixedName(playerName);
 		PublicPlayerInfo playerInfo = table.getGameInfo().getPlayer(playerName);
 				
 		if(fixedName != null)
 		{
 			//player exists -- overwrite profile
 			playerInfo = table.getGameInfo().getPlayer(fixedName);
 		}
 		else
 		{
 			//player does not yet exist -- find and replace a callbot
 			
 			int i=0;
 			boolean found = false;
 			while(!found && i< table.getGameInfo().getNumPlayers())
 			{
 				playerInfo = table.getGameInfo().getPlayer(i);
 				if(playerInfo.isCallbot()){
 					found = true;
 				}
 				i++;
 			}
 			
 			if(!found){
 				//If neither, error, no player profile can be used		
 				throw new TableFullException("There is no more room on the table");
 			}
 		}
 		
 		PrologBot bot = (PrologBot) playerInfo.getBot();
 
 		//playerInfo.setName(playerName);
 		playerInfo.setCallbot(false);
 		bot.setProlog(botDescr.getString("description"));
 		table.getDataModel().onSubmit(bot.getName(), truncate(playerName));
 	}
 	
 	protected void onLeavingTable(String playerName, Table table) throws InvalidInputException{
 		String fixedName = table.getDataModel().getFixedName(playerName);
 		
 		if(fixedName == null){
 			throw new InvalidInputException("Player " + playerName + " is not playing at table " + table.getTableName());
 		}
 
 		PublicPlayerInfo playerInfo = table.getGameInfo().getPlayer(fixedName);
 		GameInfo gameInfo = playerInfo.getGameInfo();
 
 		PrologBot bot = (PrologBot) table.getBotRepository().createBot("PrologBot/PrologBot");
 		bot.setGameInfo(gameInfo);
 		bot.setIngameName(fixedName);
 		playerInfo.setBot(bot);
 		playerInfo.setCallbot(true);
 		table.getDataModel().onSubmit(fixedName, fixedName);
 	}
 
 	
 	private String truncate(String name) {
 		if (name.length() > 20)
 			return name.substring(0, 20);
 		else
 			return name;
 	}
 	
 	private String createError(String name, String message){
 		JSONObject error = new JSONObject();
 		try {
 			error.put("type", "error");
 			error.put("name", name);
 			error.put("message", message);
 		} catch (JSONException e) {
 			// Should not occur!
 			e.printStackTrace();
 		}
 		
 		return error.toString();
 	}
 	
 	private String createReply(String message){
 		JSONObject reply = new JSONObject();
 		try {
 			reply.put("type", "Acknowledge");
 			reply.put("message", message);
 		} catch (JSONException e) {
 			// Should not occur!
 			e.printStackTrace();
 		}
 		
 		return reply.toString();
 	}
 	
 	
 	
 }
