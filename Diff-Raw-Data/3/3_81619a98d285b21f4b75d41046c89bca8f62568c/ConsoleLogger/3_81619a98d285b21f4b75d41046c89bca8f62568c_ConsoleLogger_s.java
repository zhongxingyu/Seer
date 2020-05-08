 package com.internetitem.sqshy;
 
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import jline.console.ConsoleReader;
 
 import com.internetitem.sqshy.settings.Settings;
 
 public class ConsoleLogger implements Output {
 
 	private Settings settings;
 	private ConsoleReader reader;
 
 	public ConsoleLogger(Settings settings, ConsoleReader reader) {
 		this.settings = settings;
 		this.reader = reader;
 	}
 
 	private void outputLine(String line) {
 		try {
 			reader.println(line);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void connectMessage(String message) {
 		outputLine(message);
 	}
 
 	@Override
 	public void rowCount(String message) {
 		outputLine(message);
 	}
 
 	@Override
 	public void error(String message) {
 		outputLine(message);
 	}
 
 	@Override
 	public void output(String message) {
 		outputLine(message);
 	}
 
 	@Override
 	public void resultSet(ResultSet rs) throws SQLException {
 		ResultSetMetaData rsmd = rs.getMetaData();
 		int cols = rsmd.getColumnCount();
 		int[] types = new int[cols];
 		String[] labels = new String[cols];
 		for (int i = 0; i < cols; i++) {
 			types[i] = rsmd.getColumnType(i + 1);
 			labels[i] = rsmd.getColumnLabel(i + 1);
 		}
 		int[] maxs = new int[cols];
 		List<String[]> valueList = new ArrayList<>();
 		int numRecords = 0;
 		while (rs.next()) {
 			numRecords++;
 			String[] values = new String[cols];
 			for (int i = 0; i < cols; i++) {
 				String value = rs.getString(i + 1);
				values[i] = value;
 				if (value == null) {
 					value = "<null>";
 				}
 				if (value.length() > maxs[i]) {
 					maxs[i] = value.length();
 				}
 			}
 			valueList.add(values);
 		}
 
 		StringBuilder b = new StringBuilder();
 		if (numRecords == 0) {
 			for (String label : labels) {
 				if (b.length() > 0) {
 					b.append(" | ");
 				}
 				b.append(label);
 			}
 			b.append("\n0 Rows");
 			output(b.toString());
 			return;
 		}
 
 		addLine(b, maxs, labels);
 		addHorizLine(b, maxs);
 		for (String[] record : valueList) {
 			addLine(b, maxs, record);
 		}
 		String row = "row";
 		if (numRecords != 1) {
 			row += "s";
 		}
 		b.append(numRecords + " " + row + " returned");
 		output(b.toString());
 	}
 
 	private void addHorizLine(StringBuilder b, int[] maxs) {
 		for (int i = 0; i < maxs.length; i++) {
 			if (i != 0) {
 				b.append("|");
 			}
 			for (int j = 0; j < maxs[i] + 2; j++) {
 				b.append("-");
 			}
 		}
 		b.append("\n");
 	}
 
 	private void addLine(StringBuilder b, int[] maxs, String[] values) {
 		boolean first = true;
 		for (int i = 0; i < maxs.length; i++) {
 			int len = maxs[i];
 			String value = values[i];
 			if (!first) {
 				b.append(" | ");
 			} else {
 				b.append(" ");
 			}
 			if (value.length() >= len) {
 				b.append(value.substring(0, len));
 			} else {
 				b.append(value);
 				for (int j = value.length(); j < len; j++) {
 					b.append(" ");
 				}
 			}
 			first = false;
 		}
 		b.append("\n");
 	}
 }
