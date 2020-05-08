 package org.araqne.logdb.query.parser;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.logdb.AbstractQueryCommandParser;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.query.command.ParseCsv;
 
 public class ParseCsvParser extends AbstractQueryCommandParser {
 
 	@Override
 	public String getCommandName() {
 		return "parsecsv";
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public QueryCommand parse(QueryContext context, String commandString) {
 		ParseResult r = QueryTokenizer.parseOptions(context, commandString, getCommandName().length(),
				Arrays.asList("field", "overlay", "useTab", "useDoubleQuote"), getFunctionRegistry());
 
 		Map<String, String> options = (Map<String, String>) r.value;
 		String field = options.get("field");
 		if (field == null)
 			field = "line";
 
 		boolean overlay = CommandOptions.parseBoolean(options.get("overlay"));
		boolean useTab = CommandOptions.parseBoolean(options.get("useTab"));
		boolean useDoubleQuote = CommandOptions.parseBoolean(options.get("useDoubleQuote"));
 
 		String nameField = commandString.substring(r.next).trim();
 		List<String> fieldNames = null;
 		if (!nameField.isEmpty()) {
 			fieldNames = new ArrayList<String>();
 			for (String fieldName : nameField.split(",")) {
 				fieldNames.add(fieldName.trim());
 			}
 		}
		return new ParseCsv(field, overlay, useTab, useDoubleQuote, fieldNames);
 	}
 }
