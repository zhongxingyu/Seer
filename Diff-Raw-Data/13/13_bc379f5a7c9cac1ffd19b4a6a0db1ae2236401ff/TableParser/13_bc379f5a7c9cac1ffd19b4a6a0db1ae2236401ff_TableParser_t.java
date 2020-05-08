 /*
  * Copyright 2013 Future Systems
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.query.parser;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.log.api.LogParserFactoryRegistry;
 import org.araqne.log.api.LogParserRegistry;
 import org.araqne.logdb.AccountService;
 import org.araqne.logdb.LogQueryCommand;
 import org.araqne.logdb.LogQueryCommandParser;
 import org.araqne.logdb.LogQueryContext;
 import org.araqne.logdb.LogQueryParseException;
 import org.araqne.logdb.Permission;
 import org.araqne.logdb.query.command.Table;
 import org.araqne.logstorage.LogStorage;
import org.araqne.logstorage.LogStorageStatus;
 import org.araqne.logstorage.LogTableRegistry;
 
 public class TableParser implements LogQueryCommandParser {
 	private AccountService accountService;
 	private LogStorage logStorage;
 	private LogTableRegistry tableRegistry;
 	private LogParserFactoryRegistry parserFactoryRegistry;
 	private LogParserRegistry parserRegistry;
 
 	public TableParser(AccountService accountService, LogStorage logStorage, LogTableRegistry tableRegistry,
 			LogParserFactoryRegistry parserFactoryRegistry, LogParserRegistry parserRegistry) {
 		this.accountService = accountService;
 		this.logStorage = logStorage;
 		this.tableRegistry = tableRegistry;
 		this.parserFactoryRegistry = parserFactoryRegistry;
 		this.parserRegistry = parserRegistry;
 	}
 
 	@Override
 	public String getCommandName() {
 		return "table";
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public LogQueryCommand parse(LogQueryContext context, String commandString) {
		if (logStorage.getStatus() != LogStorageStatus.Open)
			throw new LogQueryParseException("archive-not-opened", -1);

 		ParseResult r = QueryTokenizer.parseOptions(commandString, getCommandName().length(),
 				Arrays.asList("from", "to", "offset", "limit", "duration"));
 		Map<String, String> options = (Map<String, String>) r.value;
 
 		List<String> tableNames = new ArrayList<String>();
 		for (String tableNameToken : commandString.substring(r.next).split(",")) {
 			String tableName = tableNameToken.trim();
 			if (!tableRegistry.exists(tableName))
 				throw new LogQueryParseException("table-not-found", -1, "table=" + tableName);
 			tableNames.add(tableName);
 		}
 
 		Date from = null;
 		Date to = null;
 		long offset = 0;
 		long limit = 0;
 
 		if (options.containsKey("duration")) {
 			String duration = options.get("duration");
 			int i;
 			for (i = 0; i < duration.length(); i++) {
 				char c = duration.charAt(i);
 				if (!('0' <= c && c <= '9'))
 					break;
 			}
 			int value = Integer.parseInt(duration.substring(0, i));
 			from = QueryTokenizer.getDuration(value, duration.substring(i));
 		}
 
 		if (options.containsKey("from"))
 			from = QueryTokenizer.getDate(options.get("from"));
 		if (options.containsKey("to"))
 			to = QueryTokenizer.getDate(options.get("to"));
 
 		if (options.containsKey("offset"))
 			offset = Integer.parseInt(options.get("offset"));
 
 		if (offset < 0)
 			throw new LogQueryParseException("negative-offset", -1);
 
 		if (options.containsKey("limit"))
 			limit = Integer.parseInt(options.get("limit"));
 
 		if (limit < 0)
 			throw new LogQueryParseException("negative-limit", -1);
 
 		// check read permission
 		for (String tableName : tableNames) {
 			if (!accountService.checkPermission(context.getSession(), tableName, Permission.READ))
 				throw new LogQueryParseException("no-read-permission", -1, "table=" + tableName);
 		}
 
 		Table table = new Table(tableNames, offset, limit, from, to);
 		table.setTableRegistry(tableRegistry);
 		table.setStorage(logStorage);
 		table.setParserFactoryRegistry(parserFactoryRegistry);
 		table.setParserRegistry(parserRegistry);
 		return table;
 	}
 
 }
