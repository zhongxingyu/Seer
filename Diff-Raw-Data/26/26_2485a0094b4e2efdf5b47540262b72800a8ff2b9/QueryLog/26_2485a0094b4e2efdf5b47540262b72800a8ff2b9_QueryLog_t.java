 package com.iit.projects.search;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
import java.util.Map.Entry;
 
 public class QueryLog {
	private static final int MAX_QUERYLOG_BATCH = 1000;
 	// private static final int MAX_QUERYLOG_BATCH = 5;
 	private static final float BATCH_FRACTION = 0.5f;
 	private String filePath;
 	private BufferedReader reader;
 	private Map<String, List<QueryLogLine>> logGroupedByIPMap;
 	private BufferedWriter queryFileWriter;
 	private BufferedWriter outFileWriter;
 	private int numLinesInMap = 0;
 
 	public QueryLog() {
 
 	}
 
 	public QueryLog(String path) throws QueryLogIOException {
 		filePath = path;
 		setReader();
 	}
 
 	public QueryLog(String path, String queryFile) throws QueryLogIOException,
 			QueryLogOutputException {
 		this(path);
 		try {
 			queryFileWriter = new BufferedWriter(new FileWriter(queryFile));
 		} catch (IOException e) {
 			throw new QueryLogOutputException(e);
 		}
 	}
 
 	public QueryLog(String path, String queryFile, String outFile)
 			throws QueryLogIOException, QueryLogOutputException {
 		this(path, queryFile);
 		try {
 			outFileWriter = new BufferedWriter(new FileWriter(outFile));
 		} catch (IOException e) {
 			throw new QueryLogOutputException(e);
 		}
 	}
 
 	public void outputQuery(QueryLogLine line) throws QueryLogOutputException {
 		String query = null;
 		if (null != line)
 			query = line.getQuery();
 		if (null != query)
 			try {
 				queryFileWriter.write(query);
 				queryFileWriter.newLine();
 			} catch (IOException e) {
 				throw new QueryLogOutputException(e);
 			}
 	}
 
 	public void setFilePath(String filePath) {
 		this.filePath = filePath;
 	}
 
 	public void setReader(BufferedReader reader) {
 		this.reader = reader;
 	}
 
 	public void setReader() throws QueryLogIOException {
 		try {
 			reader = new BufferedReader(new FileReader(filePath));
 		} catch (FileNotFoundException e) {
 			throw new QueryLogIOException(e);
 		}
 	}
 
 	public QueryLogLine getNextLine() throws QueryLogIOException {
 		String line = null;
 		try {
 			line = reader.readLine();
 		} catch (IOException e) {
 			throw new QueryLogIOException(e);
 		}
 		// System.out.println(line);
 		QueryLogLine queryLine = null;
 		if (null != line && !line.equalsIgnoreCase("null"))
 			queryLine = new QueryLogLine(line);
 		return queryLine;
 	}
 
 	public void groupByIP(QueryLogLine line) throws QueryLogOutputException {
 		if (null == logGroupedByIPMap)
 			logGroupedByIPMap = new LinkedHashMap<String, List<QueryLogLine>>(
 					16, 0.75f, true);
 		List<QueryLogLine> queryLines = logGroupedByIPMap.get(line.getIP());
 		if (null == queryLines) {
 			queryLines = new ArrayList<QueryLogLine>();
 			logGroupedByIPMap.put(line.getIP(), queryLines);
 		}
 		queryLines.add(line);
 		if (++numLinesInMap >= MAX_QUERYLOG_BATCH) {
 			unmarshall(false);
 			numLinesInMap = 0;
 		}
 	}
 
 	public void close() throws QueryLogIOException, QueryLogOutputException {
 		if (null != reader)
 			try {
 				reader.close();
 			} catch (IOException e) {
 				throw new QueryLogIOException(e);
 			}
 		if (null != queryFileWriter)
 			try {
 				queryFileWriter.close();
 			} catch (IOException e) {
 				throw new QueryLogOutputException(e);
 			}
 		if (null != outFileWriter)
 			try {
 				outFileWriter.close();
 			} catch (IOException e) {
 				throw new QueryLogOutputException(e);
 			}
 	}
 
 	public void unmarshall(boolean unmarshallAll)
 			throws QueryLogOutputException {
 		try {
			Iterator<Entry<String, List<QueryLogLine>>> iter = logGroupedByIPMap
					.entrySet().iterator();
 			List<QueryLogLine> lines = null;
			int numUnmarshall = unmarshallAll ? logGroupedByIPMap.size()
					: (int) (logGroupedByIPMap.size() * BATCH_FRACTION);
			Entry entry = null;
			while (iter.hasNext() && numUnmarshall-- > 0) {
				entry = iter.next();
				lines = (List<QueryLogLine>) entry.getValue();
 				for (QueryLogLine line : lines) {
 					outFileWriter.write(line.getLine());
 					outFileWriter.newLine();
 				}
				iter.remove();
 			}
 		} catch (IOException e) {
 			throw new QueryLogOutputException(e);
 		}
 
 	}
 }
