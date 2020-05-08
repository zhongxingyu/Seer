 package uk.co.brotherlogic.mdbweb;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import uk.co.brotherlogic.jarpur.Page;
 import uk.co.brotherlogic.jarpur.TemplatePage;
 import uk.co.brotherlogic.mdb.Connect;
 import uk.co.brotherlogic.mdb.record.Record;
 import uk.co.brotherlogic.mdb.record.RecordUtils;
 
 public class Default extends TemplatePage {
 	@Override
 	public Class generates() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public String linkParams(Object arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	protected Map<String,Object> convertParams(List<String> elems, Map<String, String> params) {
 		Map<String, Object> paramMap = new TreeMap<String, Object>();
 		try {
 			Record recToListenTo = RecordUtils
 					.getRecordToListenTo(new String[] { "12", "10", "7" });
 			Record cdToListenTo = RecordUtils
 					.getRecordToListenTo(new String[] { "CD" });
 			
 			paramMap.put("cd", cdToListenTo);
 			paramMap.put("record", recToListenTo);
			paramMap.put("rip", RecordUtils.getRecordToRip(1).get(0));
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return paramMap;
 	}
 
 
 	public boolean tester() {
 		return false;
 	}
 }
