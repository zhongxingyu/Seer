 package org.araqne.logdb.metadata;
 
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.araqne.logdb.AccountService;
 import org.araqne.logdb.LogQueryContext;
 import org.araqne.logdb.LogQueryParseException;
 import org.araqne.logdb.Privilege;
import org.araqne.logdb.query.parser.QueryTokenizer;
 import org.araqne.logstorage.LogTableRegistry;
 
 public class MetadataQueryStringParser {
 
 	public static TableScanOption getTableNames(LogQueryContext context, LogTableRegistry tableRegistry,
 			AccountService accountService, String token) {
 		token = token.trim();
 
 		Date from = null;
 		Date to = null;
 
 		Map<String, Object> optionTokens = parseOptions(token);
 
		String duration = (String) optionTokens.get("duration");
		if (duration != null) {
			int i;
			for (i = 0; i < duration.length(); i++) {
				char c = duration.charAt(i);
				if (!('0' <= c && c <= '9'))
					break;
			}
			int value = Integer.parseInt(duration.substring(0, i));
			from = QueryTokenizer.getDuration(value, duration.substring(i));
		}

 		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
 		String fromToken = (String) optionTokens.get("from");
 		if (fromToken != null) {
 			from = df.parse(fromToken, new ParsePosition(0));
 			if (from == null)
 				throw new LogQueryParseException("invalid-from", -1);
 		}
 
 		String toToken = (String) optionTokens.get("to");
 		if (toToken != null) {
 			to = df.parse(toToken, new ParsePosition(0));
 			if (to == null)
 				throw new LogQueryParseException("invalid-to", -1);
 		}
 
 		if (to != null && from != null && to.before(from))
 			throw new LogQueryParseException("invalid-date-range", -1);
 
 		int next = (Integer) optionTokens.get("next");
 		token = token.substring(next).trim();
 
 		HashSet<String> tableFilter = null;
 		String[] argTableNames = null;
 		if (!token.isEmpty()) {
 			tableFilter = new HashSet<String>();
 			argTableNames = token.split(",");
 
 			for (int i = 0; i < argTableNames.length; i++)
 				tableFilter.add(argTableNames[i].trim());
 		}
 
 		List<String> tableNames = new ArrayList<String>();
 
 		if (context.getSession().isAdmin()) {
 			for (String tableName : tableRegistry.getTableNames()) {
 				if (tableFilter != null && !tableFilter.contains(tableName))
 					continue;
 
 				tableNames.add(tableName);
 			}
 		} else {
 			List<Privilege> privileges = accountService.getPrivileges(context.getSession(), context.getSession().getLoginName());
 			for (Privilege p : privileges) {
 				if (p.getPermissions().size() > 0 && tableRegistry.exists(p.getTableName())) {
 					if (tableFilter != null && !tableFilter.contains(p.getTableName()))
 						continue;
 
 					tableNames.add(p.getTableName());
 				}
 			}
 		}
 
 		TableScanOption opt = new TableScanOption();
 		opt.setTableNames(tableNames);
 		opt.setFrom(from);
 		opt.setTo(to);
 
 		if (optionTokens.containsKey("diskonly"))
 			opt.setDiskOnly(Boolean.parseBoolean((String) optionTokens.get("diskonly")));
 
 		return opt;
 	}
 
 	private static Map<String, Object> parseOptions(String token) {
 		Set<String> keys = new HashSet<String>();
		keys.add("duration");
 		keys.add("from");
 		keys.add("to");
 		keys.add("diskonly");
 
 		Map<String, Object> m = new HashMap<String, Object>();
 		int next = 0;
 		for (String pairs : token.split(" ")) {
 			int p = pairs.indexOf('=');
 			if (p < 0)
 				break;
 
 			String key = pairs.substring(0, p);
 			if (!keys.contains(key))
 				throw new LogQueryParseException("invalid-logdb-option", -1);
 
 			String value = pairs.substring(p + 1);
 			m.put(key, value);
 
 			next = token.indexOf(pairs) + pairs.length();
 		}
 
 		m.put("next", next);
 		return m;
 	}
 
 }
