 package org.araqne.logdb.query.command;
 
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.log.api.CsvParser;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.Row;
 import org.slf4j.LoggerFactory;
 
 public class ParseCsv extends QueryCommand {
 	private final org.slf4j.Logger slog = LoggerFactory.getLogger(ParseCsv.class);
 	private String field;
 	private boolean overlay;
 	private char delimiter;
 	private char escape;
 	private List<String> fieldNames;
 	private CsvParser parser;
 
	public ParseCsv(String field, boolean overlay, boolean useTab, List<String> fieldNames) {
 		this.field = field;
 		this.overlay = overlay;
 		this.fieldNames = fieldNames;
		parser = new CsvParser(useTab, false, fieldNames == null ? null : fieldNames.toArray(new String[0]));
 	}
 
 	@Override
 	public String getName() {
 		return "parsecsv";
 	}
 
 	@Override
 	public void onPush(Row row) {
 		String target = (String) row.get(field);
 		if (target == null) {
 			if (overlay)
 				pushPipe(row);
 			return;
 		}
 
 		try {
 			Map<String, Object> m = parser.parse(target);
 			if (overlay)
 				row.map().putAll(m);
 			else
 				row = new Row(m);
 
 			pushPipe(row);
 		} catch (Throwable t) {
 			if (overlay)
 				pushPipe(row);
 
 			if (slog.isDebugEnabled())
 				slog.debug("araqne logdb: cannot parse csv [{}], error [{}]", target, t.getMessage());
 		}
 	}
 
 	@Override
 	public String toString() {
 		String fields = "";
 		if (fieldNames != null) {
 			for (int i = 0; i < fieldNames.size(); i++) {
 				fields += fieldNames.get(i);
 				if (i != fieldNames.size() - 1)
 					fields += ",";
 			}
 		}
 
 		return "parsecsv field=" + field + " overlay=" + overlay + " delimiter=" + delimiter + " escape=" + escape + " " + fields;
 	}
 }
