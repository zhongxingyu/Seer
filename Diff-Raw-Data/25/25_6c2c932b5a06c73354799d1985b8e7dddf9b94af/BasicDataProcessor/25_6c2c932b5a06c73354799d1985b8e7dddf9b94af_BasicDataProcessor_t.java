 package com.lemondo.commons.db.processor;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.lemondo.commons.db.exception.DataProcessingException;
 
 public class BasicDataProcessor implements DataProcessor<Map<String, Object>, List<Map<String, Object>>> {
 
 	@Override
 	public Map<String, Object> bodyAsMap(Map<String, Object> body) {
 		return new HashMap<String, Object>(body);
 	}
 
 	@Override
 	public Map<String, Object> readRow(ResultSet rs, ResultSetMetaData rsmd, int numColumns) throws SQLException {
 		Map<String, Object> result = new HashMap<String, Object>();
 		for (int i = 1; i <= numColumns; i++) {
			result.put(rsmd.getColumnLabel(i), rs.getObject(i));
 		}
 		return result;
 	}
 
 	@Override
 	public List<Map<String, Object>> readAll(ResultSet rs, ResultSetMetaData rsmd, int numColumns) throws SQLException {
 		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
 		while (rs.next()) {
 			result.add(readRow(rs, rsmd, numColumns));
 		}
 		return result;
 	}
 
 	@Override
 	public void writeRows(OutputStream out, ResultSet rs, ResultSetMetaData rsmd, int numColumns) throws SQLException, DataProcessingException {
 		try {
 			ObjectOutputStream oOut = new ObjectOutputStream(out);
 			while (rs.next()) {
 				oOut.writeObject(readRow(rs, rsmd, numColumns));
 				oOut.flush();
 			}
 		} catch (IOException e) {
 			throw new DataProcessingException("Cannot write into the OutputStream", e);
 		}
 	}
 
 }
